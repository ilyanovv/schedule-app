package com.example.schedulemai.localdb;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IO.Novikov on 29.05.2018.
 */

public class Dao {
    private SQLiteDatabase db;
    public Dao(SQLiteDatabase db) {
        this.db = db;
    }

    public void createTableAndInsertValues(Tables tableName, JSONArray jsonArray) {
        try {
            db.beginTransaction();
            dropIfExists(tableName);
            createTable(tableName, jsonArray.getJSONObject(0).keys());
            insertValues(tableName, jsonArray);
            db.setTransactionSuccessful();
        } catch (JSONException e) {
            Log.e("SQLLite", "rollback transaction : ");
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }


    public List<String> getTeachersList() {
        List<String> result = new ArrayList<>();
        String query = "SELECT last_name, first_name, patronymic_name FROM "
                + Tables.TEACHER.getTableName();
        Cursor cursor = db.rawQuery(query, null);
        while (cursor.moveToNext()) {
            result.add(cursor.getString(0) + " " +
                cursor.getString(1) + " " +
                cursor.getString(2)
            );
        }
        cursor.close();
        return result;
    }



    private void dropIfExists(Tables tableName) {
        String query =  "DROP TABLE IF EXISTS " + tableName.name();
        Log.e("SQLite", query);
        db.execSQL(query);
    }


    private void createTable(Tables tableName, Iterator<String> keys) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("CREATE TABLE ")
                .append(tableName.getTableName())
                .append("(");
//                .append("id INTEGER PRIMARY KEY, ");
        while (keys.hasNext()){
            stringBuilder.append(keys.next())
                    .append(" TEXT ,");
        }
        // заменяем последнюю запятую
        stringBuilder.replace(stringBuilder.length() - 1, stringBuilder.length(), " );");
        Log.e("SQLite", stringBuilder.toString());
        db.execSQL(stringBuilder.toString());
    }

    private void insertValues(Tables tableName, JSONArray jsonArray) throws JSONException {
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            insertValue(tableName, jsonObject);
        }
    }


    private void insertValue(Tables tableName, JSONObject jsonObject) {
        StringBuilder stringBuilder = new StringBuilder();
        List<String> valuesList = new ArrayList<>();
        stringBuilder.append("INSERT INTO ")
                .append(tableName.getTableName())
                .append(" (");
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            stringBuilder.append(key)
                    .append(", ");
            valuesList.add(jsonObject.optString(key));
        }
        stringBuilder.replace(stringBuilder.length() - 2, stringBuilder.length(), ") ");
        stringBuilder.append("VALUES (");

        for (String value : valuesList) {
            stringBuilder.append("'")
                    .append(value)
                    .append("', ");
        }
        stringBuilder.replace(stringBuilder.length() - 2, stringBuilder.length(), ");");
        Log.e("SQLite", stringBuilder.toString());
        db.execSQL(stringBuilder.toString());
    }
}
