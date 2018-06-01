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
import com.example.schedulemai.student.StudentScheduleActivity;
import com.example.schedulemai.admin.AdminScheduleActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class StudentCloudDBOpenHelper extends CloudDBOpenHelper{
    public StudentCloudDBOpenHelper(Context context, String databaseName, String dateSt) {
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

        Log.e("DB", "--- onCreate database ---");
        db.execSQL("create table schedule_tab ("
                + "id integer primary key autoincrement,"
                + "lesson_name string, "
                + "lesson_type string, "
                + "lecture_room string, "
                + "time_begin time,"
                + "time_end, "
                + "lesson_date, "
                + "teacher_fn, "
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
                String groupID = PreferenceManager
                        .getDefaultSharedPreferences(context).getString(SP.SP_GROUP_ID, null);
                URL url = new URL(SP.ROOT_SERVICE_URL + "/schedule?groupID=" + groupID);
                Log.e("URL =", url.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                try {
                    urlConnection.connect();
                } catch (Exception e){
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
            if(jsonArray != null){
                Log.e("JSON", jsonArray.toString());
            } else{
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
                    //cv.put("lecture_room", obj.getString("lecture_room_number") + " " + obj.getString("building_name"));
                    cv.put("lecture_room", obj.getString("lecture_room_number"));
                    cv.put("time_begin", obj.getString("time_begin"));
                    cv.put("time_end", obj.getString("time_end"));
                    cv.put("lesson_date", obj.getString("lesson_date"));
                    cv.put("teacher_fn", obj.getString("last_name") + " " + obj.getString("first_name") + " "
                            + obj.getString("patronymic_name"));
                    cv.put("record_id", obj.getString("record_id"));
                    int count = (int) db.insert("schedule_tab", null, cv);
                    Log.e("COUNT cL", Integer.toString(count));
                }
            } catch (Exception e){
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
            String userType = prefs.getString(SP.SP_USER_TYPE, "");
            if(userType.equals(SP.ADMIN_TYPE)) {
              /*  Intent intent = new Intent(context, AdminScheduleActivity.class);
                intent.putExtra("dateSt", dateSt);
                Log.e("DBupdated, start intent", intent.getStringExtra("dateSt"));
                //TODO: возможно, положить что-то в extras, проверить в scheduleactivity и сделать restart
                context.startActivity(intent);*/
              AdminScheduleActivity.dc.update_db(dateSt, context);
              AdminScheduleActivity.data.clear();
              AdminScheduleActivity.data.addAll(AdminScheduleActivity.setAdapterValues());
              AdminScheduleActivity.adapter.notifyDataSetChanged();
            }
            else if(userType.equals(SP.STUDENT_TYPE)) {
                StudentScheduleActivity.dc.update_db(dateSt, context);
                StudentScheduleActivity.data.clear();
                StudentScheduleActivity.data.addAll(StudentScheduleActivity.setAdapterValues());
                StudentScheduleActivity.adapter.notifyDataSetChanged();
            }
        }

        @Override
        protected  void onCancelled() {
            Log.e(LOG_TAG, "IN CANCEL");
            Toast toast = Toast.makeText(context,
                    "Не удалось загрузить новое расписание", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
