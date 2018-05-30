package com.example.schedulemai.asynctasks;

import android.app.ActionBar;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.example.schedulemai.SP;
import com.example.schedulemai.localdb.Dao;
import com.example.schedulemai.localdb.Tables;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by IO.Novikov on 29.05.2018.
 */

public class DbTaskFactory {
    private SQLiteDatabase db;
    private Dao dao;

    public DbTaskFactory(SQLiteDatabase db) {
        this.db = db;
        this.dao = new Dao(db);
    }

    public void createTable(Tables tableName)  {
        new DbTask().execute(tableName);
    }

    private class DbTask extends AsyncTask<Tables, Void, JSONArray> {

        String LOG_TAG = "DbTask";
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";
        Tables table = null;

        @Override
        protected JSONArray doInBackground(Tables... params) {
            // получаем данные с внешнего ресурса
            JSONArray jsonArray = null;
            try {
                table = params[0];
                URL url = new URL(SP.ROOT_SERVICE_URL + "/" + params[0].getUrl());
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
            if (jsonArray != null) {
                Log.e("JSON", jsonArray.toString());
                dao.createTableAndInsertValues(table, jsonArray);
            } else {
                onCancelled();
            }
        }

        @Override
        protected  void onCancelled() {
            Log.e(LOG_TAG, "IN CANCEL: failed to get JSON for URL = " +  table.getUrl());

        }


    }
}
