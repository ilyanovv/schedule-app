package com.example.schedulemai.localdb;

/**
 * Created by Илья on 13.02.2016.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;

/* локальная база данных изменений и добавлений, сделанных пользователем.
При загрузке новых данных из удаленной БД данные из этой базы также добавляются
 */
public class UserDBOpenHelper extends SQLiteOpenHelper {

    public static String DB_PARENT_PATH;
    public static String DB_PATH;    //Путь к папке с базами на устройстве
    public static String DB_NAME;    //Имя файла с базой
    //TODO:версию забирать из удаленной бд, и не здесь, а при загрузке. Здесь только получать будем
    private final Context context;



    public UserDBOpenHelper(Context context, String databaseName) {
        super(context, databaseName, null, 1);
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
        if (!databaseExists())
            createDatabase();
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
     * Создание новой БД
     */
    private void createDatabase() {
        SQLiteDatabase db = getWritableDatabase();
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
        db.close();
    }




    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,
                          int newVersion) {
    }

}

