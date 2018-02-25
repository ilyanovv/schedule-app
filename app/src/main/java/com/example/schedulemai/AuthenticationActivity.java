package com.example.schedulemai;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

//TODO: сделать кнопку "обновить": onStop() и onRestart() - когда доступа к интернету нет
//TODO: версию расписания для группы получить в этом же json-е
public class AuthenticationActivity extends AppCompatActivity {
    private String[] groupIDData;
    private int[] groupVersionData;
    private String[] teacherIDData;
    private int[] teacherVersionData;
    private String[] users = {"Студент", "Преподаватель"};
    ArrayAdapter<String> groupNumberAdapter;
    ArrayAdapter<String> teacherNameAdapter;
    Button gnButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        final SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(this);
        String groupID = sPref.getString(SP.SP_GROUP_ID, null);
        String teacherID = sPref.getString(SP.SP_TEACHER_ID, null);
        try {
            Log.e("groupID", groupID);
        }catch (Exception e){;};
        //если уже был сделан выбор ранее
        if(groupID != null) {
            Intent intent = new Intent(AuthenticationActivity.this, StudentScheduleActivity.class);
            startActivity(intent);
            finish();
        }
        else if (teacherID != null){
            Intent intent = new Intent(AuthenticationActivity.this, TeacherScheduleActivity.class);
            startActivity(intent);
            finish();
        }
        else {
            gnButton = (Button) findViewById(R.id.buttonGroupNumberOrTeacherName);
            final Spinner userTypeSpinner = (Spinner) findViewById(R.id.spinnerUserType);
            ArrayAdapter<String> userTypeAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, users);
            userTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            userTypeSpinner.setAdapter(userTypeAdapter);
            userTypeSpinner.setSelection(0);
            userTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    TextView tvTeacherName = (TextView) findViewById(R.id.tvTeacherName);
                    TextView tvGroupNumber = (TextView) findViewById(R.id.tvGroupNumber);
                    Spinner spTeacherName = (Spinner) findViewById(R.id.spinnerTeacherName);
                    Spinner spGroupNumber = (Spinner) findViewById(R.id.spinnerGroupNumber);
                    switch (users[position]) {
                        case "Студент":
                            tvTeacherName.setVisibility(View.GONE);
                            tvGroupNumber.setVisibility(View.VISIBLE);
                            spTeacherName.setVisibility(View.GONE);
                            spGroupNumber.setVisibility(View.VISIBLE);
                            break;
                        case "Преподаватель":
                            tvTeacherName.setVisibility(View.VISIBLE);
                            tvGroupNumber.setVisibility(View.GONE);
                            spTeacherName.setVisibility(View.VISIBLE);
                            spGroupNumber.setVisibility(View.GONE);
                            break;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });


            new GroupNumberParseTask().execute();
            new TeacherNameParseTask().execute();
            gnButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences.Editor editor = sPref.edit();
                    int position = userTypeSpinner.getSelectedItemPosition();
                    switch (users[position]) {
                        case "Студент":
                            Spinner type = (Spinner) findViewById(R.id.spinnerGroupNumber);
                            editor.putString(SP.SP_GROUP_ID, groupIDData[type.getSelectedItemPosition()]);
                            editor.putInt(SP.DB_VERSION, groupVersionData[type.getSelectedItemPosition()]);
                            Log.e("GROUP_NUMBER", sPref.getString(SP.SP_GROUP_ID, "NO_ID"));
                            break;
                        case "Преподаватель":
                            type = (Spinner) findViewById(R.id.spinnerTeacherName);
                            editor.putString(SP.SP_TEACHER_ID, teacherIDData[type.getSelectedItemPosition()]);
                            editor.putInt(SP.DB_VERSION, teacherVersionData[type.getSelectedItemPosition()]);
                            break;
                    }
                    editor.apply(); //вместо commit
                    Intent intent = new Intent(AuthenticationActivity.this, StudentScheduleActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }


    private class GroupNumberParseTask extends AsyncTask<Void, Void, JSONArray> {

        String LOG_TAG = "AsyncTask";
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";
        @Override
        protected JSONArray doInBackground(Void... params) {
            // получаем данные с внешнего ресурса
            JSONArray jsonArray = null;
            try {
                URL url = new URL("http://fromcloud-vj7.rhcloud.com/get_all_groups");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                // while(true) {
                try {
                    urlConnection.connect();
                    //       break;
                } catch (Exception e) {
                    //TODO: вроде как не всплывает, посмотреть
                    Toast toast = Toast.makeText(AuthenticationActivity.this,
                            "Не удалось подключиться к серверу", Toast.LENGTH_SHORT);
                    toast.show();
                    onStop();
                }
                //  }
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                resultJson = buffer.toString();

                jsonArray = new JSONArray(resultJson);
                Log.e("groups", jsonArray.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return jsonArray;
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {
            super.onPostExecute(jsonArray);
            if(jsonArray == null){
                onCancelled();
                gnButton.setEnabled(false);
                onStop(); //TODO: тут подумать, что делать
            }
            else{
                Log.e("JSON", jsonArray.toString());
                gnButton.setEnabled(true);
                int jsonArrayLength = jsonArray.length();
                String[] data = new String[jsonArrayLength];
                groupIDData = new String[jsonArrayLength];
                groupVersionData = new int[jsonArrayLength];
                for (int i = 0; i < jsonArrayLength; i++) {
                    try {
                        data[i] = jsonArray.getJSONObject(i).getString("group_number");
                        groupIDData[i] = jsonArray.getJSONObject(i).getString("group_id");
                        groupVersionData[i] = jsonArray.getJSONObject(i).getInt("version");
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                }
                groupNumberAdapter = new ArrayAdapter<String>(AuthenticationActivity.this,
                        android.R.layout.simple_spinner_item, data);
                groupNumberAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                Spinner type = (Spinner) findViewById(R.id.spinnerGroupNumber);
                type.setAdapter(groupNumberAdapter);
                type.setSelection(0);
            }
        }

        @Override
        protected  void onCancelled() {
            Log.e(LOG_TAG, "IN CANCEL");
            Toast toast = Toast.makeText(AuthenticationActivity.this,
                    "Не удалось подключиться к серверу", Toast.LENGTH_SHORT);
            toast.show();
        }
    }



    private class TeacherNameParseTask extends AsyncTask<Void, Void, JSONArray> {

        String LOG_TAG = "AsyncTask";
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";
        @Override
        protected JSONArray doInBackground(Void... params) {
            // получаем данные с внешнего ресурса
            JSONArray jsonArray = null;
            try {
                URL url = new URL("http://fromcloud-vj7.rhcloud.com/teachers");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                // while(true) {
                try {
                    urlConnection.connect();
                    //       break;
                } catch (Exception e) {
                    //TODO: вроде как не всплывает, посмотреть
                    Toast toast = Toast.makeText(AuthenticationActivity.this,
                            "Не удалось подключиться к серверу", Toast.LENGTH_SHORT);
                    toast.show();
                    onStop();
                }
                //  }
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                resultJson = buffer.toString();

                jsonArray = new JSONArray(resultJson);
                Log.e("groups", jsonArray.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return jsonArray;
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {
            super.onPostExecute(jsonArray);
            if(jsonArray == null){
                onCancelled();
                gnButton.setEnabled(false);
                onStop(); //TODO: тут подумать, что делать
            }
            else{
                Log.e("JSON", jsonArray.toString());
                gnButton.setEnabled(true);
                int jsonArrayLength = jsonArray.length();
                String[] data = new String[jsonArrayLength];
                teacherIDData = new String[jsonArrayLength];
                teacherVersionData = new int[jsonArrayLength];
                for (int i = 0; i < jsonArrayLength; i++) {
                    try {
                        data[i] = jsonArray.getJSONObject(i).getString("last_name") + " " +
                                jsonArray.getJSONObject(i).getString("first_name") +  " " +
                                jsonArray.getJSONObject(i).getString("patronymic_name");
                        teacherIDData[i] = jsonArray.getJSONObject(i).getString("teacher_id");
                        teacherVersionData[i] = jsonArray.getJSONObject(i).getInt("version");
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                }
                teacherNameAdapter = new ArrayAdapter<String>(AuthenticationActivity.this,
                        android.R.layout.simple_spinner_item, data);
                teacherNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                Spinner type = (Spinner) findViewById(R.id.spinnerTeacherName);
                type.setAdapter(teacherNameAdapter);
                type.setSelection(0);
            }
        }

        @Override
        protected  void onCancelled() {
            Log.e(LOG_TAG, "IN CANCEL");
            Toast toast = Toast.makeText(AuthenticationActivity.this,
                    "Не удалось подключиться к серверу", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}


