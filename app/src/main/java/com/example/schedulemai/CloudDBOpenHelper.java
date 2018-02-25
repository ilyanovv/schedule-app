package com.example.schedulemai;

/**
 * Created by Илья on 13.02.2016.
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.xml.transform.Result;


abstract public class CloudDBOpenHelper extends SQLiteOpenHelper {

    public static String DB_PARENT_PATH;
    public static String DB_PATH;    //Путь к папке с базами на устройстве
    public static String DB_NAME;    //Имя файла с базой
    protected SQLiteDatabase db;
    //TODO:версию забирать из удаленной бд, и не здесь, а при загрузке. Здесь только получать будем
    protected final Context context;
    protected static int getDBVersion(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(SP.DB_VERSION, 1);
    };


    public CloudDBOpenHelper(Context context, String databaseName) {
        super(context, databaseName, null, getDBVersion(context));
        this.context = context;
        //Составим полный путь к базам для вашего приложения
        DB_PARENT_PATH = this.context.getDatabasePath(databaseName).getParent();
        DB_PATH = this.context.getDatabasePath(databaseName).getPath();
        DB_NAME = databaseName;
        initialize();
    }

    /**
     * Инициализация БД. Создание новой если ранее не существовала.
     */
    private void initialize() {
        if (databaseExists()) {
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(context);
            int dbVersion = prefs.getInt(SP.SP_KEY_DB_VER, 1);
            //TODO:заменить на >
            if (getDBVersion(context) != dbVersion) {
                File dbFile = context.getDatabasePath(DB_NAME);
                boolean dbDeleted = dbFile.delete();
                Log.e("DELETED =", Boolean.toString(dbDeleted));
                if (!dbDeleted && databaseExists())
                    Toast.makeText(context, "Не удалось удалить старое расписание", Toast.LENGTH_SHORT).show();
            }
        }
        if (!databaseExists()) {
            createDatabase();
        }
    }

    /**
     * Проверка существования файла БД. Если существует - возвращает true.
     * @return
     */
    private boolean databaseExists() {
        File dbFile = context.getDatabasePath(DB_NAME);
        Log.e("DB", context.getDatabasePath(DB_NAME).toString());
        return dbFile.exists();
    }

    /**
     * Создание новой БД.
     */
    abstract void createDatabase();
}

class StudentCloudDBOpenHelper extends CloudDBOpenHelper{
    public StudentCloudDBOpenHelper(Context context, String databaseName) {
        super(context, databaseName);
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
        db.execSQL("create table schedule80_308 ("
                + "id integer primary key autoincrement,"
                + "lesson_name string, "
                + "lesson_type string, "
                + "lecture_room string, "
                + "time_begin time,"
                + "time_end, "
                + "lesson_date, "
                + "teacher_fn"
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
                URL url = new URL("http://fromcloud-vj7.rhcloud.com/schedule?groupID=" + groupID);
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
                    cv.put("lecture_room", obj.getString("lecture_room_number") + " " + obj.getString("building_name"));
                    cv.put("time_begin", obj.getString("time_begin"));
                    cv.put("time_end", obj.getString("time_end"));
                    cv.put("lesson_date", obj.getString("lesson_date"));
                    cv.put("teacher_fn", obj.getString("last_name") + " " + obj.getString("first_name") + " "
                            + obj.getString("patronymic_name"));
                    int count = (int) db.insert("schedule80_308", null, cv);
                    Log.e("COUNT cL", Integer.toString(count));
                }
            } catch (Exception e){
                e.printStackTrace();
            }


            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(SP.SP_KEY_DB_VER, getDBVersion(context));
            Log.e("DB_VERSION", Integer.toString(getDBVersion(context)));
            editor.apply(); //вместо commit
            /*db.close(); // тут мб не нужно закрывать, мы же продолжаем работать
            db = null;*/
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

class TeacherCloudDBOpenHelper extends CloudDBOpenHelper {
    public TeacherCloudDBOpenHelper(Context context, String databaseName) {
        super(context, databaseName);
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
        db.execSQL("create table schedule80_308 ("
                + "id integer primary key autoincrement,"
                + "lesson_name string, "
                + "lesson_type string, "
                + "lecture_room string, "
                + "time_begin time,"
                + "time_end, "
                + "lesson_date, "
                + "groups"
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
                URL url = new URL("http://fromcloud-vj7.rhcloud.com/ts?teacherID=" + teacherID);
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
                    cv.put("lecture_room", obj.getString("lecture_room_number") + " " + obj.getString("building_name"));
                    cv.put("time_begin", obj.getString("time_begin"));
                    cv.put("time_end", obj.getString("time_end"));
                    cv.put("lesson_date", obj.getString("lesson_date"));
                    //TODO:отдебажить
                    JSONArray gr = obj.getJSONArray("groups");
                    String groups="";
                    for(int j = 0; j < gr.length()-1; j++){
                        groups += gr.getString(j) + ", ";
                    }
                    groups += gr.getString(gr.length()-1);
                    cv.put("groups", groups);

                    int count = (int) db.insert("schedule80_308", null, cv);
                    Log.e("COUNT cL", Integer.toString(count));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(SP.SP_KEY_DB_VER, getDBVersion(context));
            Log.e("DB_VERSION", Integer.toString(getDBVersion(context)));
            editor.apply(); //вместо commit
            /*db.close(); // тут мб не нужно закрывать, мы же продолжаем работать
            db = null;*/
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