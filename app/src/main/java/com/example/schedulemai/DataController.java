package com.example.schedulemai;

/**
 * Created by Илья on 13.02.2016.
 */
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


abstract public class DataController {
    protected LocalDb local_db;
    protected SQLiteDatabase database;
    protected SQLiteDatabase archiveDatabase;

    public DataController() {
        local_db = new LocalDb();
    }
    public void add_to_db(Lesson lesson) {
        local_db.add(lesson);
    };

    public Lesson get_from_db(int i) {
        return local_db.get(i);
    }
    public int size_db() {
        return local_db.size();
    }

    abstract public SQLiteDatabase GetOpenedCloudDatabase(Context cont);
    abstract public SQLiteDatabase GetOpenedUserDatabase(Context cont);
    abstract public void insert_into_db(Lesson les, Context cont);
    abstract public void remove_from_db(int i, Context cont);
    abstract public void modify_db(int i, Lesson les, Context cont);
    abstract public void update_db(String date, Context cont);

    //TODO: заменить на функцию без логов
    int logCursor(Cursor c) {
        int count = 0;

        if (c != null) {
            if (c.moveToFirst()) {
                String str;
                do {
                    str = "";
                    for (String cn : c.getColumnNames()) {
                        Log.e("cn = ", cn);
                        str = str.concat(cn + " = " + c.getString(c.getColumnIndex(cn)) + "; ");
                    }
                    Log.e("LOG_TAG", str);
                    count++;
                } while (c.moveToNext());
            }
        } else
            Log.e("LOG_TAG", "Cursor is null");
        return count;
    }

    void GetValues(int count, String[][] str, int offset, Cursor c)
    {
        if (c.moveToFirst()) {
            int i=offset;
            do {
                int j=0;
                for (String cn : c.getColumnNames()) {
                    str[i][j++] = c.getString(c.getColumnIndex(cn));
                    if(str[i][j-1] == null)
                        str[i][j-1] = "null";
                    Log.e("GetValues", str[i][j-1] == null ? "null" :  str[i][j-1]);
                }
                i++;
            } while (c.moveToNext());
        }
    }
}

class StudentDataController extends DataController{
    public StudentDataController(){
        super();
    }
     public SQLiteDatabase GetOpenedCloudDatabase(Context cont) {
        if(database == null || !database.isOpen())
        {
            final String DB_NAME = "schedule3.sqlite3";
            CloudDBOpenHelper dbOpenHelper = new StudentCloudDBOpenHelper(cont, DB_NAME);
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
        SQLiteDatabase database = GetOpenedCloudDatabase(cont);
        ContentValues cv = new ContentValues();
        cv.put("lesson_name", les.name);
        cv.put("lesson_type", les.type);
        cv.put("lecture_room", les.classroom);
        cv.put("time_begin", les.begin_time);
        cv.put("time_end", les.end_time);
        cv.put("lesson_date", les.date);
        cv.put("teacher_fn", les.teacher);
        database.insert("schedule80_308", null, cv);
    }

    public void remove_from_db(int i, Context cont){
        Lesson oldLesson = get_from_db(i);
        SQLiteDatabase database = GetOpenedCloudDatabase(cont);
        String whereClause = "lesson_name = ? AND lesson_type = ? AND time_begin = ? AND lesson_date = ?";
        String[] whereArgs = new String[] {oldLesson.name, oldLesson.type, oldLesson.begin_time, oldLesson.date};
        int del = database.delete("schedule80_308", whereClause, whereArgs);
        Log.e("DEL_ROWS  = ", Integer.toString(del));
        local_db.remove(i);
    }

    public void modify_db(int i, Lesson les, Context cont){
        Lesson oldLesson = get_from_db(i);
        SQLiteDatabase database = GetOpenedCloudDatabase(cont);

        ContentValues cv = new ContentValues();
        cv.put("lesson_name", les.name);
        cv.put("lesson_type", les.type);
        cv.put("lecture_room", les.classroom);
        cv.put("time_begin", les.begin_time);
        cv.put("time_end", les.end_time);
        cv.put("lesson_date", les.date);
        cv.put("teacher_fn", les.teacher);
        String whereClause = "lesson_name = ? AND lesson_type = ? AND time_begin = ? AND lesson_date = ?";
        String[] whereArgs = new String[] {oldLesson.name, oldLesson.type, oldLesson.begin_time, oldLesson.date};

        int upd = database.update("schedule80_308", cv, whereClause, whereArgs);
        Log.e("UPD_ROWS  = ", Integer.toString(upd));

        local_db.modify(i, les);
    }

    public void update_db(String date, Context cont) {
        SQLiteDatabase database = GetOpenedCloudDatabase(cont);
        while(StudentScheduleActivity.dc.size_db()>0)
            local_db.remove(0);
        Log.e("DATE in UPD", date);

        //TODO заменить массив строк на Map
        String sqlQuery = "select lesson_name, lesson_type, lecture_room, " +
                "time_begin, time_end, lesson_date, teacher_fn from schedule80_308 where lesson_date = ? " +
                "order by time_begin";


        Cursor c = database.rawQuery(sqlQuery, new String[] {date});

        //TODO: найти, как считать лучше. А то бред какой-то
        int count = logCursor(c);
        Log.e("COUNT = ", Integer.toString(count));
        int COLUMN_COUNT = 7;
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
            Lesson lesson = Lesson.CreateNewLesson(lesson_type, lesson_name, teacher_fn,
                    begin_time, end_time, classroom, dateSt, null);
            StudentScheduleActivity.dc.add_to_db(lesson);
        }
        c.close();
        // AddToDB(count, values, database);
        // database.close();
    }

}


class TeacherDataController extends DataController{
    public TeacherDataController(){
        super();
    }
    public SQLiteDatabase GetOpenedCloudDatabase(Context cont) {
        if(database == null || !database.isOpen())
        {
            final String DB_NAME = "schedule3.sqlite3";
            CloudDBOpenHelper dbOpenHelper = new TeacherCloudDBOpenHelper(cont, DB_NAME);
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
        SQLiteDatabase database = GetOpenedCloudDatabase(cont);
        ContentValues cv = new ContentValues();
        cv.put("lesson_name", les.name);
        cv.put("lesson_type", les.type);
        cv.put("lecture_room", les.classroom);
        cv.put("time_begin", les.begin_time);
        cv.put("time_end", les.end_time);
        cv.put("lesson_date", les.date);
        cv.put("groups", les.group_number);
        database.insert("schedule80_308", null, cv);
    }

    public void remove_from_db(int i, Context cont){
        Lesson oldLesson = get_from_db(i);
        SQLiteDatabase database = GetOpenedCloudDatabase(cont);
        String whereClause = "lesson_name = ? AND lesson_type = ? AND time_begin = ? AND lesson_date = ?";
        String[] whereArgs = new String[] {oldLesson.name, oldLesson.type, oldLesson.begin_time, oldLesson.date};
        int del = database.delete("schedule80_308", whereClause, whereArgs);
        Log.e("DEL_ROWS  = ", Integer.toString(del));
        local_db.remove(i);
    }

    public void modify_db(int i, Lesson les, Context cont){
        Lesson oldLesson = get_from_db(i);
        SQLiteDatabase database = GetOpenedCloudDatabase(cont);

        ContentValues cv = new ContentValues();
        cv.put("lesson_name", les.name);
        cv.put("lesson_type", les.type);
        cv.put("lecture_room", les.classroom);
        cv.put("time_begin", les.begin_time);
        cv.put("time_end", les.end_time);
        cv.put("lesson_date", les.date);
        cv.put("groups", les.group_number);
        String whereClause = "lesson_name = ? AND lesson_type = ? AND time_begin = ? AND lesson_date = ?";
        String[] whereArgs = new String[] {oldLesson.name, oldLesson.type, oldLesson.begin_time, oldLesson.date};

        int upd = database.update("schedule80_308", cv, whereClause, whereArgs);
        Log.e("UPD_ROWS  = ", Integer.toString(upd));

        local_db.modify(i, les);
    }

    public void update_db(String date, Context cont) {
        SQLiteDatabase database = GetOpenedCloudDatabase(cont);
        while(StudentScheduleActivity.dc.size_db()>0)
            local_db.remove(0);
        Log.e("DATE in UPD", date);

        //TODO заменить массив строк на Map
        String sqlQuery = "select lesson_name, lesson_type, lecture_room, " +
                "time_begin, time_end, lesson_date, groups from schedule80_308 where lesson_date = ? " +
                "order by time_begin";


        Cursor c = database.rawQuery(sqlQuery, new String[] {date});

        //TODO: найти, как считать лучше. А то бред какой-то
        int count = logCursor(c);
        Log.e("COUNT = ", Integer.toString(count));
        int COLUMN_COUNT = 7;
        String[][] values = new String[count][COLUMN_COUNT];
        GetValues(count, values, 0, c);

        for (int i = 0; i < count; i++) {
            String lesson_type = values[i][1];
            String lesson_name = values[i][0];
            String groups = values[i][6];
            String classroom = values[i][2];
            String begin_time = values[i][3];
            String end_time = values[i][4];
            String dateSt = values[i][5];
            Lesson lesson = Lesson.CreateNewLesson(lesson_type, lesson_name, null,
                    begin_time, end_time, classroom, dateSt, groups);
            StudentScheduleActivity.dc.add_to_db(lesson);
        }
        c.close();
        // AddToDB(count, values, database);
        // database.close();
    }

}