package com.example.schedulemai.localdb;

/**
 * Created by Илья on 13.02.2016.
 */
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.schedulemai.DownloadActivity;
import com.example.schedulemai.asynctasks.DbTaskFactory;
import com.example.schedulemai.lesson.Lesson;


//TODO:закрывать БД при смене пользователя - DONE
//TODO: посмотреть AdminScheduleActivity
abstract public class DataController {
    protected LocalDb local_db;
    //TODO: сделать private
    public SQLiteDatabase database;
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

    abstract public SQLiteDatabase getOpenedCloudDatabase(Context cont, String daySt);
    abstract public SQLiteDatabase GetOpenedUserDatabase(Context cont);
    abstract public void insert_into_db(Lesson les, Context cont);
    abstract public void remove_from_db(int i, Context cont);
    abstract public void modify_db(int i, Lesson les, Context cont);
    abstract public void update_db(String date, Context cont);

    public Dao getDao() {
        return new Dao(database);
    }

    //TODO: заменить на функцию без логов
    protected int logCursor(Cursor c) {
        int count = 0;

        if (c != null) {
            if (c.moveToFirst()) {
                String str;
                do {
                    str = "";
                    for (String cn : c.getColumnNames()) {
                     //   Log.e("cn = ", cn);
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
                    //Log.e("GetValues", str[i][j-1] == null ? "null" :  str[i][j-1]);
                }
                i++;
            } while (c.moveToNext());
        }
    }


    protected void createTables() {
        DbTaskFactory dbTaskFactory = new DbTaskFactory(database, getDao());
        dbTaskFactory.createTable(Tables.TEACHER);
        dbTaskFactory.createTable(Tables.GROUP);
        dbTaskFactory.createTable(Tables.LESSON);
        dbTaskFactory.createTable(Tables.LESSON_TYPE);
    }
}


