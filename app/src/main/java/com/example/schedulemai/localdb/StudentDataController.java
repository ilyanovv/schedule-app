package com.example.schedulemai.localdb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.schedulemai.lesson.Lesson;
import com.example.schedulemai.SP;
import com.example.schedulemai.student.StudentScheduleActivity;
import com.example.schedulemai.clouddb.CloudDBOpenHelper;
import com.example.schedulemai.clouddb.StudentCloudDBOpenHelper;

public class StudentDataController extends DataController{
    public StudentDataController(){
        super();
    }
     public SQLiteDatabase GetOpenedCloudDatabase(Context cont, String daySt) {
        if(database == null || !database.isOpen())
        {
            final String DB_NAME = "schedule3.sqlite3";
            CloudDBOpenHelper dbOpenHelper = new StudentCloudDBOpenHelper(cont, DB_NAME, daySt);
            database = dbOpenHelper.getReadableDatabase();
        }
         int globVer = PreferenceManager.getDefaultSharedPreferences(cont).getInt(SP.SP_GLOBAL_DB_VERSION, 1);
         int locVer = PreferenceManager.getDefaultSharedPreferences(cont).getInt(SP.SP_LOCAL_DB_VERSION, 1);
         if(database.isOpen() && (globVer > locVer)){
             database.close();
             final String DB_NAME = "schedule3.sqlite3";
             CloudDBOpenHelper dbOpenHelper = new StudentCloudDBOpenHelper(cont, DB_NAME, daySt);
             database = dbOpenHelper.getReadableDatabase();
         }
        Log.e("PATH = ", database.getPath());
        return database;
    }

     public SQLiteDatabase GetOpenedUserDatabase(Context cont) {
        if(archiveDatabase == null)
        {
            final String DB_NAME = "archiveDB.sqlite3";
            UserDBOpenHelper dbOpenHelper = new UserDBOpenHelper(cont, DB_NAME);
            archiveDatabase = dbOpenHelper.getReadableDatabase();
        }
        Log.e("PATH = ", archiveDatabase.getPath());
        return archiveDatabase;
    }

    public void insert_into_db(Lesson les, Context cont)
    {
        SQLiteDatabase database = GetOpenedCloudDatabase(cont, "");
        ContentValues cv = new ContentValues();
        cv.put("lesson_name", les.name);
        cv.put("lesson_type", les.type);
        cv.put("lecture_room", les.classroom);
        cv.put("time_begin", les.begin_time);
        cv.put("time_end", les.end_time);
        cv.put("lesson_date", les.date);
        cv.put("teacher_fn", les.teacher);
        cv.put("record_id", les.record_id);
        database.insert("schedule_tab", null, cv);
    }

    public void remove_from_db(int i, Context cont){
        Lesson oldLesson = get_from_db(i);
        SQLiteDatabase database = GetOpenedCloudDatabase(cont, "");
        String whereClause = "lesson_name = ? AND lesson_type = ? AND time_begin = ? AND lesson_date = ?";
        String[] whereArgs = new String[] {oldLesson.name, oldLesson.type, oldLesson.begin_time, oldLesson.date};
        int del = database.delete("schedule_tab", whereClause, whereArgs);
        Log.e("DEL_ROWS  = ", Integer.toString(del));
        local_db.remove(i);
    }

    public void modify_db(int i, Lesson les, Context cont){
        Lesson oldLesson = get_from_db(i);
        SQLiteDatabase database = GetOpenedCloudDatabase(cont, "");

        ContentValues cv = new ContentValues();
        cv.put("lesson_name", les.name);
        cv.put("lesson_type", les.type);
        cv.put("lecture_room", les.classroom);
        cv.put("time_begin", les.begin_time);
        cv.put("time_end", les.end_time);
        cv.put("lesson_date", les.date);
        cv.put("teacher_fn", les.teacher);
        cv.put("record_id", les.record_id);
        String whereClause = "lesson_name = ? AND lesson_type = ? AND time_begin = ? AND lesson_date = ?";
        String[] whereArgs = new String[] {oldLesson.name, oldLesson.type, oldLesson.begin_time, oldLesson.date};

        int upd = database.update("schedule_tab", cv, whereClause, whereArgs);
        Log.e("UPD_ROWS  = ", Integer.toString(upd));

        local_db.modify(i, les);
    }

    public void update_db(String date, Context cont) {
        SQLiteDatabase database = GetOpenedCloudDatabase(cont, date);
        while(StudentScheduleActivity.dc.size_db()>0)
            local_db.remove(0);
        Log.e("DATE in UPD", date);

        //TODO заменить массив строк на Map
        String sqlQuery = "select lesson_name, lesson_type, lecture_room, " +
                "time_begin, time_end, lesson_date, teacher_fn, record_id from schedule_tab where lesson_date = ? " +
                "order by time_begin";


        Cursor c = database.rawQuery(sqlQuery, new String[] {date});

        //TODO: найти, как считать лучше. А то бред какой-то
        int count = logCursor(c);
        Log.e("COUNT = ", Integer.toString(count));
        int COLUMN_COUNT = 8;
        String[][] values = new String[count][COLUMN_COUNT];
        GetValues(count, values, 0, c);

        for (int i = 0; i < count; i++) {
            String lesson_type = values[i][1];
            String lesson_name = values[i][0];
            String teacher_fn = values[i][6];
            String classroom = values[i][2];
            String begin_time = values[i][3];
            String end_time = values[i][4];
            String dateSt = values[i][5];
            String recordID = values[i][7];
            Lesson lesson = Lesson.CreateNewLesson(lesson_type, lesson_name, teacher_fn,
                    begin_time, end_time, classroom, dateSt, null, recordID);
            StudentScheduleActivity.dc.add_to_db(lesson);
        }
        c.close();
        // AddToDB(count, values, database);
        // database.close();
    }

}
