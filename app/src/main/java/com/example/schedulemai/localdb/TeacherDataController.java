package com.example.schedulemai.localdb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.schedulemai.lesson.Lesson;
import com.example.schedulemai.SP;
import com.example.schedulemai.lesson.LessonFactory;
import com.example.schedulemai.lesson.LessonType;
import com.example.schedulemai.teacher.TeacherScheduleActivity;
import com.example.schedulemai.clouddb.CloudDBOpenHelper;
import com.example.schedulemai.clouddb.TeacherCloudDBOpenHelper;

public class TeacherDataController extends DataController{
    public TeacherDataController(){
        super();
    }
    public SQLiteDatabase getOpenedCloudDatabase(Context cont, String daySt) {
        if(database == null || !database.isOpen())
        {
            final String DB_NAME = "schedule3.sqlite3";
            CloudDBOpenHelper dbOpenHelper = new TeacherCloudDBOpenHelper(cont, DB_NAME, daySt);
            database = dbOpenHelper.getReadableDatabase();
        }
        int globVer = PreferenceManager.getDefaultSharedPreferences(cont).getInt(SP.SP_GLOBAL_DB_VERSION, 1);
        int locVer = PreferenceManager.getDefaultSharedPreferences(cont).getInt(SP.SP_LOCAL_DB_VERSION, 1);
        if(database.isOpen() && (globVer > locVer)){
            database.close();
            final String DB_NAME = "schedule3.sqlite3";
            CloudDBOpenHelper dbOpenHelper = new TeacherCloudDBOpenHelper(cont, DB_NAME, daySt);
            database = dbOpenHelper.getReadableDatabase();
            createTables();
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
        SQLiteDatabase database = getOpenedCloudDatabase(cont, "");
        ContentValues cv = new ContentValues();
        cv.put("lesson_name", les.getName());
        cv.put("lesson_type", les.getLessonType());
        cv.put("lecture_room", les.getClassroom());
        cv.put("time_begin", les.getTimeBegin());
        cv.put("time_end", les.getTimeEnd());
        cv.put("lesson_date", les.getLessonDate());
        cv.put("groups", les.getGroupNumber());
        cv.put("record_id", les.getRecordId());
        database.insert("schedule_tab", null, cv);
    }

    public void remove_from_db(int i, Context cont){
        Lesson oldLesson = get_from_db(i);
        SQLiteDatabase database = getOpenedCloudDatabase(cont, "");
        String whereClause = "lesson_name = ? AND lesson_type = ? AND time_begin = ? AND lesson_date = ?";
        String[] whereArgs = new String[] {
                oldLesson.getName(),
                oldLesson.getLessonType(),
                oldLesson.getTimeBegin(),
                oldLesson.getLessonDate()
        };
        int del = database.delete("schedule_tab", whereClause, whereArgs);
        Log.e("DEL_ROWS  = ", Integer.toString(del));
        local_db.remove(i);
    }

    public void modify_db(int i, Lesson les, Context cont){
        Lesson oldLesson = get_from_db(i);
        SQLiteDatabase database = getOpenedCloudDatabase(cont, "");

        ContentValues cv = new ContentValues();
        cv.put("lesson_name", les.getName());
        cv.put("lesson_type", les.getLessonType());
        cv.put("lecture_room", les.getClassroom());
        cv.put("time_begin", les.getTimeBegin());
        cv.put("time_end", les.getTimeEnd());
        cv.put("lesson_date", les.getLessonDate());
        cv.put("groups", les.getGroupNumber());
        cv.put("record_id", les.getRecordId());
        String whereClause = "lesson_name = ? AND (lesson_type = ? OR lesson_type = ?)" +
                " AND time_begin = ? AND lesson_date = ?";
        LessonType lessonType = LessonType.fromString(oldLesson.getLessonType());
        String[] whereArgs = new String[] {
                oldLesson.getName(),
                lessonType.getType(),
                lessonType.getName(),
                oldLesson.getTimeBegin(),
                oldLesson.getLessonDate()
        };

        int upd = database.update("schedule_tab", cv, whereClause, whereArgs);
        Log.e("UPD_ROWS  = ", Integer.toString(upd));

        local_db.modify(i, les);
    }

    public void update_db(String date, Context cont) {
        SQLiteDatabase database = getOpenedCloudDatabase(cont, date);
        while(TeacherScheduleActivity.dc.size_db()>0)
            local_db.remove(0);
        Log.e("DATE in UPD", date);

        //TODO заменить массив строк на Map
        String sqlQuery = "select lesson_name, lesson_type, lecture_room, " +
                "time_begin, time_end, lesson_date, groups, record_id from schedule_tab where lesson_date = ? " +
                "order by time_begin";


        Cursor c = database.rawQuery(sqlQuery, new String[] {date});

        //TODO: найти, как считать лучше. А то бред какой-то
        int count = logCursor(c);
        Log.e("COUNT = ", Integer.toString(count));
        int COLUMN_COUNT = 8;
        String[][] values = new String[count][COLUMN_COUNT];
        GetValues(count, values, 0, c);

        for (int i = 0; i < count; i++) {
            String lessonType = values[i][1];
            String lessonName = values[i][0];
            String groups = values[i][6];
            String classroom = values[i][2];
            String timeBegin = values[i][3];
            String timeEnd = values[i][4];
            String dateSt = values[i][5];
            String recordID = values[i][6];
            Lesson lesson = LessonFactory.createLesson(recordID, lessonName,
                    null, lessonType, timeBegin, timeEnd,
                    classroom, dateSt, groups);
            TeacherScheduleActivity.dc.add_to_db(lesson);
        }
        c.close();
        // AddToDB(count, values, database);
        // database.close();
    }

}
