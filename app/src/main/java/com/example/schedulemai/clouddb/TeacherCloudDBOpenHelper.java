package com.example.schedulemai.clouddb;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.example.schedulemai.SP;
import com.example.schedulemai.teacher.TeacherScheduleActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TeacherCloudDBOpenHelper extends CloudDBOpenHelper {
    public TeacherCloudDBOpenHelper(Context context, String databaseName, String dateSt) {
        super(context, databaseName, dateSt);
    }

    /**
     * Создание новой БД
     */
    @Override
    void createDatabase() {
        Log.e("DB", "--- beforeCreate database ---");
        //SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(DB_PATH, null);
        db = getWritableDatabase();

        Log.e("DB", "--- onCreate Teacher database ---");
        db.execSQL("create table schedule_tab ("
                + "id integer primary key autoincrement,"
                + "lesson_name string, "
                + "lesson_type string, "
                + "lecture_room string, "
                + "time_begin time,"
                + "time_end, "
                + "lesson_date, "
                + "groups, "
                + "record_id"
                + ");");

        ParseTask mt = new ParseTask();
        mt.execute();
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,
                          int newVersion) {
    }


    private class ParseTask extends AsyncTask<Void, Void, JSONArray> {

        String LOG_TAG = "AsyncTask";
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";

        @Override
        protected JSONArray doInBackground(Void... params) {
            // получаем данные с внешнего ресурса
            JSONArray jsonArray = null;
            try {
                String teacherID = PreferenceManager
                        .getDefaultSharedPreferences(context).getString(SP.SP_TEACHER_ID, null);
                URL url = new URL(SP.ROOT_SERVICE_URL + "/ts?teacherID=" + teacherID);
                Log.e("URL =", url.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                try {
                    urlConnection.connect();
                } catch (Exception e) {
                    cancel(true);
                }
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                resultJson = buffer.toString();

                jsonArray = new JSONArray(resultJson);
                Log.e(LOG_TAG + "rJ", jsonArray.toString());

            } catch (Exception e) {
                e.printStackTrace();
            }
            return jsonArray;
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {
            super.onPostExecute(jsonArray);
            if (jsonArray != null) {
                Log.e("JSON", jsonArray.toString());
            } else {
                onCancelled();
                return;
            }

            try {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    Log.e("JSON", obj.toString());
                    ContentValues cv = new ContentValues();
                    cv.put("lesson_name", obj.getString("lesson_name"));
                    cv.put("lesson_type", obj.getString("lesson_type_name"));
//                    cv.put("lecture_room", obj.getString("lecture_room_number") + " " + obj.getString("building_name"));
                    cv.put("lecture_room", obj.getString("lecture_room_number"));
                    cv.put("time_begin", obj.getString("time_begin"));
                    cv.put("time_end", obj.getString("time_end"));
                    cv.put("lesson_date", obj.getString("lesson_date"));
                    cv.put("record_id", obj.getString("record_id"));
                    //TODO:отдебажить
                    JSONArray gr = obj.getJSONArray("groups");
                    String groups="";
                    for(int j = 0; j < gr.length()-1; j++){
                        groups += gr.getString(j) + ", ";
                    }
                    groups += gr.getString(gr.length()-1);
                    cv.put("groups", groups);

                    int count = (int) db.insert("schedule_tab", null, cv);
                    Log.e("COUNT cL", Integer.toString(count));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(SP.SP_LOCAL_DB_VERSION, getDBVersion(context));
            Log.e("SP_GLOBAL_DB_VERSION", Integer.toString(getDBVersion(context)));
            editor.apply(); //вместо commit
            Toast toast = Toast.makeText(context,
                    "Расписание успешно обновлено", Toast.LENGTH_SHORT);
            toast.show();
            TeacherScheduleActivity.dc.update_db(dateSt, context);
            TeacherScheduleActivity.data.clear();
            TeacherScheduleActivity.data.addAll(TeacherScheduleActivity.setAdapterValues());
            TeacherScheduleActivity.adapter.notifyDataSetChanged();
        }

        @Override
        protected void onCancelled() {
            Log.e(LOG_TAG, "IN CANCEL");
            Toast toast = Toast.makeText(context,
                    "Не удалось загрузить новое расписание", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
