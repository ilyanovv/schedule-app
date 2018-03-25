package com.example.schedulemai.clouddb;

/**
 * Created by Илья on 13.02.2016.
 */
import java.io.File;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.example.schedulemai.SP;

abstract public class CloudDBOpenHelper extends SQLiteOpenHelper {

    public static String DB_PARENT_PATH;
    public static String DB_PATH;    //Путь к папке с базами на устройстве
    public static String DB_NAME;    //Имя файла с базой
    protected SQLiteDatabase db;
    protected final String dateSt;
    //TODO:версию забирать из удаленной бд, и не здесь, а при загрузке. Здесь только получать будем
    protected final Context context;
    protected static int getDBVersion(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(SP.SP_GLOBAL_DB_VERSION, 1);
    };


    public CloudDBOpenHelper(Context context, String databaseName, String dateSt) {
        super(context, databaseName, null, getDBVersion(context));
        this.context = context;
        this.dateSt = dateSt;
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
            int dbVersion = prefs.getInt(SP.SP_LOCAL_DB_VERSION, 1);
            Log.e("localdbVersion", Integer.toString(dbVersion));
            int gldbVersion = prefs.getInt(SP.SP_GLOBAL_DB_VERSION, 1);
            Log.e("globaldbVersion", Integer.toString(gldbVersion));
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

