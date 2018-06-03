package com.example.schedulemai.authentication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.schedulemai.R;
import com.example.schedulemai.SP;
import com.example.schedulemai.student.StudentScheduleActivity;
import com.example.schedulemai.teacher.TeacherScheduleActivity;
import com.example.schedulemai.admin.AdminScheduleActivity;
import com.example.schedulemai.utils.search.HintSearch;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

//TODO: версию расписания для группы получить в этом же json-е
public class AuthenticationActivity extends AppCompatActivity {
    private String[] groupIDData;
    private int[] groupVersionData;
    private String[] teacherIDData;
    private int[] teacherVersionData;
    private String[] users = {"Студент", "Преподаватель", "Администратор"};
    Button gnButton;
    Button refreshButton;
    EditText etLogin;
    EditText etPassword;

    Intent prevIntent;
    SharedPreferences sPref;
    SharedPreferences.Editor editor;
    HintSearch teacherSearch;
    HintSearch groupSearch;
    EditText teacherEditText;
    EditText groupEditText;
    Context context = this;

    public static final Object lock = new Object();
    public volatile int enabledCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        sPref = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sPref.edit();

        String userType = sPref.getString(SP.SP_USER_TYPE, "NO_TYPE");
        teacherEditText = findViewById(R.id.teacherEditText);
        groupEditText = findViewById(R.id.groupEditText);


        //если уже был сделан выбор ранее
        if(userType.equals(SP.STUDENT_TYPE)) {
            prevIntent = new Intent(AuthenticationActivity.this, StudentScheduleActivity.class);
            new CheckUpdateParseTask().execute("groupID", sPref.getString(SP.SP_GROUP_ID, null));
        }
        else if (userType.equals(SP.TEACHER_TYPE)){
            prevIntent = new Intent(AuthenticationActivity.this, TeacherScheduleActivity.class);
            new CheckUpdateParseTask().execute("teacherID", sPref.getString(SP.SP_TEACHER_ID, null));
        }
        else {
            gnButton = (Button) findViewById(R.id.buttonGroupNumberOrTeacherName);
            refreshButton = (Button) findViewById(R.id.buttonRefresh);
            final Spinner userTypeSpinner = (Spinner) findViewById(R.id.spinnerUserType);
            ArrayAdapter<String> userTypeAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, users);
            userTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            userTypeSpinner.setAdapter(userTypeAdapter);
            userTypeSpinner.setSelection(0);
            etLogin = (EditText) findViewById(R.id.editTextLogin);
            etPassword = (EditText) findViewById(R.id.editTextPassword);
            etLogin.setText(sPref.getString(SP.SP_LOGIN, ""));
            etPassword.setText(sPref.getString(SP.SP_PASSWORD, ""));

            userTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    TextView tvTeacherName = (TextView) findViewById(R.id.tvTeacherName);
                    TextView tvGroupNumber = (TextView) findViewById(R.id.tvGroupNumber);
                    TextView tvLogin = (TextView) findViewById(R.id.tvLogin);
                    TextView tvPassword = (TextView) findViewById(R.id.tvPassword);
                    switch (users[position]) {
                        case "Студент":
                            tvTeacherName.setVisibility(View.GONE);
                            tvGroupNumber.setVisibility(View.VISIBLE);
                            teacherEditText.setVisibility(View.GONE);
                            groupEditText.setVisibility(View.VISIBLE);
                            tvLogin.setVisibility(View.GONE);
                            tvPassword.setVisibility(View.GONE);
                            etLogin.setVisibility(View.GONE);
                            etPassword.setVisibility(View.GONE);
                            break;
                        case "Преподаватель":
                            tvTeacherName.setVisibility(View.VISIBLE);
                            tvGroupNumber.setVisibility(View.GONE);
                            teacherEditText.setVisibility(View.VISIBLE);
                            groupEditText.setVisibility(View.GONE);
                            tvLogin.setVisibility(View.GONE);
                            tvPassword.setVisibility(View.GONE);
                            etLogin.setVisibility(View.GONE);
                            etPassword.setVisibility(View.GONE);
                            break;
                        case "Администратор":
                            tvTeacherName.setVisibility(View.GONE);
                            tvGroupNumber.setVisibility(View.VISIBLE);
                            teacherEditText.setVisibility(View.GONE);
                            groupEditText.setVisibility(View.VISIBLE);
                            tvLogin.setVisibility(View.VISIBLE);
                            tvPassword.setVisibility(View.VISIBLE);
                            etLogin.setVisibility(View.VISIBLE);
                            etPassword.setVisibility(View.VISIBLE);
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
                    int position = userTypeSpinner.getSelectedItemPosition();
                    Intent intent = null;
                    EditText teacherEditText = findViewById(R.id.teacherEditText);

                    switch (users[position]) {
                        case "Студент":
                            editor.putString(SP.SP_GROUP_ID, groupIDData[groupSearch.getClickedPosition()]);
                            editor.putString(SP.SP_GROUP, groupSearch.getClickedValue());
                            editor.putInt(SP.SP_GLOBAL_DB_VERSION, groupVersionData[groupSearch.getClickedPosition()]);
                            editor.putString(SP.SP_USER_TYPE, SP.STUDENT_TYPE);
                            intent = new Intent(AuthenticationActivity.this, StudentScheduleActivity.class);
                            break;
                        case "Преподаватель":
                            editor.putString(SP.SP_TEACHER_ID, teacherIDData[teacherSearch.getClickedPosition()]);
                            editor.putInt(SP.SP_GLOBAL_DB_VERSION, teacherVersionData[teacherSearch.getClickedPosition()]);
                            editor.putString(SP.SP_TEACHER, teacherSearch.getClickedValue());
                            editor.putString(SP.SP_USER_TYPE, SP.TEACHER_TYPE);
                            intent = new Intent(AuthenticationActivity.this, TeacherScheduleActivity.class);
                            break;
                        case "Администратор":
                            editor.putString(SP.SP_GROUP_ID, groupIDData[groupSearch.getClickedPosition()]);
                            editor.putInt(SP.SP_GLOBAL_DB_VERSION, groupVersionData[groupSearch.getClickedPosition()]);
                            editor.putString(SP.SP_GROUP, groupSearch.getClickedValue());
                            //editor.putString(SP.SP_USER_TYPE, SP.ADMIN_TYPE);
                            //intent = new Intent(AuthenticationActivity.this, StudentScheduleActivity.class);
                            new TestConnectionParseTask().execute(etLogin.getText().toString(),
                                    etPassword.getText().toString());
                            return;
                    }
                    editor.apply(); //вместо commit
                    Log.e("GROUP_NUMBER", sPref.getString(SP.SP_GROUP_ID, "NO_ID"));
                    Log.e("TEACHER_ID", sPref.getString(SP.SP_TEACHER_ID, "NO_ID"));
                    startActivity(intent);
                    finish();
                }
            });

            refreshButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v){
                    new GroupNumberParseTask().execute();
                    new TeacherNameParseTask().execute();
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
                URL url = new URL(SP.ROOT_SERVICE_URL + "/get_all_groups");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                // while(true) {
                try {
                    urlConnection.setConnectTimeout(5000);
                    urlConnection.connect();
                    //       break;
                } catch (Exception e) {
                    cancel(true);
                    while (!isCancelled()){cancel(true);}
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
            }
            else{
                Log.e("JSON", jsonArray.toString());
                gnButton.setEnabled(true);
                int jsonArrayLength = jsonArray.length();
                ArrayList<String> data = new ArrayList<>();
                groupIDData = new String[jsonArrayLength];
                groupVersionData = new int[jsonArrayLength];
                for (int i = 0; i < jsonArrayLength; i++) {
                    try {
                        data.add(jsonArray.getJSONObject(i).getString("group_number"));
                        groupIDData[i] = jsonArray.getJSONObject(i).getString("group_id");
                        groupVersionData[i] = jsonArray.getJSONObject(i).getInt("version");
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                }
                ListView groupListView = findViewById(R.id.groupListView);
                groupSearch = new HintSearch(groupEditText, groupListView, context, data);
                groupSearch.setUp();

                synchronized (lock) {
                    enabledCount++;
                }

                if (enabledCount == 2) {
                    refreshButton.setEnabled(false);
                    refreshButton.setVisibility(View.GONE);
                    gnButton.setEnabled(true);
                    gnButton.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        protected  void onCancelled() {
            Log.e(LOG_TAG, "IN CANCEL");
            Toast toast = Toast.makeText(AuthenticationActivity.this,
                    "Не удалось подключиться к серверу", Toast.LENGTH_SHORT);
            toast.show();
            refreshButton.setEnabled(true);
            refreshButton.setVisibility(View.VISIBLE);
            gnButton.setEnabled(false);
            gnButton.setVisibility(View.GONE);
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
                URL url = new URL(SP.ROOT_SERVICE_URL + "/teachers");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                // while(true) {
                try {
                    urlConnection.setConnectTimeout(5000);
                    urlConnection.connect();
                    //       break;
                } catch (Exception e) {
                    cancel(true);
                    while (!isCancelled()){cancel(true);}
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
            }
            else{
                Log.e("JSON", jsonArray.toString());
                gnButton.setEnabled(true);
                int jsonArrayLength = jsonArray.length();
                List<String> data = new ArrayList<>();
                teacherIDData = new String[jsonArrayLength];
                teacherVersionData = new int[jsonArrayLength];
                for (int i = 0; i < jsonArrayLength; i++) {
                    try {
                        data.add(jsonArray.getJSONObject(i).getString("last_name") + " " +
                                jsonArray.getJSONObject(i).getString("first_name") +  " " +
                                jsonArray.getJSONObject(i).getString("patronymic_name"));
                        teacherIDData[i] = jsonArray.getJSONObject(i).getString("teacher_id");
                        teacherVersionData[i] = jsonArray.getJSONObject(i).getInt("version");
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                }

                ListView teacherListView = findViewById(R.id.teacherListView);
                teacherSearch = new HintSearch(teacherEditText, teacherListView, context, data);
                teacherSearch.setUp();
                synchronized (lock) {
                    enabledCount++;
                }

                if (enabledCount == 2) {
                    refreshButton.setEnabled(false);
                    refreshButton.setVisibility(View.GONE);
                    gnButton.setEnabled(true);
                    gnButton.setVisibility(View.VISIBLE);
                }

            }
        }

        @Override
        protected  void onCancelled() {
            Log.e(LOG_TAG, "IN CANCEL");
            Toast toast = Toast.makeText(AuthenticationActivity.this,
                    "Не удалось подключиться к серверу", Toast.LENGTH_SHORT);
            toast.show();
            refreshButton.setEnabled(true);
            refreshButton.setVisibility(View.VISIBLE);
            gnButton.setEnabled(false);
            gnButton.setVisibility(View.GONE);
        }
    }

    private class CheckUpdateParseTask extends AsyncTask<String, Void, JSONArray> {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";
        @Override
        //params[0] in {groupID, teacherID}, params[1] - appropriate ID of group or teacher
        protected JSONArray doInBackground(String... params) {
            // получаем данные с внешнего ресурса
            JSONArray jsonArray = null;
            try {
                URL url = new URL(SP.ROOT_SERVICE_URL + "/get_db_version?" + params[0] + "=" + params[1]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                resultJson = buffer.toString();
                jsonArray = new JSONArray(resultJson);
            } catch (Exception e) {
                e.printStackTrace();
                cancel(false);
                while (!isCancelled()){}
            }
            return jsonArray;
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {
            super.onPostExecute(jsonArray);

            int jsonArrayLength = jsonArray.length();
            String ID = null;
            try {
                ID = jsonArray.getString(0);
                SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(AuthenticationActivity.this);
                SharedPreferences.Editor editor = sPref.edit();
                editor.putInt(SP.SP_GLOBAL_DB_VERSION, Integer.parseInt(ID));
                editor.apply();
                startActivity(prevIntent);
                finish();
            } catch (JSONException e1) {
                e1.printStackTrace();
                cancel(false);
            }
        }


        @Override
        protected  void onCancelled() {
            Toast toast = Toast.makeText(AuthenticationActivity.this,
                    "Не удалось проверить наличие обновлений", Toast.LENGTH_SHORT);
            toast.show();
            startActivity(prevIntent);
            finish();
        }
    }



    private class TestConnectionParseTask extends AsyncTask<String, Void, Integer> {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";
        @Override
        //params[0] = login, params[1] = password
        protected Integer doInBackground(String... params) {
            // получаем данные с внешнего ресурса
            JSONArray jsonArray = null;
            try {
                URL url = new URL(SP.ROOT_SERVICE_URL + "/test_connection");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("login", params[0])
                        .appendQueryParameter("password", params[1]);
                String query = builder.build().getEncodedQuery();
                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                return urlConnection.getResponseCode();

            } catch (Exception e) {
                e.printStackTrace();
                cancel(false);
                while (!isCancelled()){}
            }
            return null;
        }


        @Override
        protected void onPostExecute(Integer responseCode) {
            super.onPostExecute(responseCode);
            if(responseCode == HttpURLConnection.HTTP_NO_CONTENT){
                editor.putString(SP.SP_LOGIN, etLogin.getText().toString());
                editor.putString(SP.SP_PASSWORD, etPassword.getText().toString());
                editor.putString(SP.SP_USER_TYPE, SP.ADMIN_TYPE);
                editor.apply();
                Toast toast = Toast.makeText(AuthenticationActivity.this,
                        "Авторизация прошла успешно", Toast.LENGTH_SHORT);
                toast.show();
                Intent intent = new Intent(AuthenticationActivity.this, AdminScheduleActivity.class);
                startActivity(intent);
                finish();
            }
            else{
                Toast toast = Toast.makeText(AuthenticationActivity.this,
                        "Авторизация отклонена", Toast.LENGTH_SHORT);
                toast.show();
            }
        }


        @Override
        protected  void onCancelled() {
            Toast toast = Toast.makeText(AuthenticationActivity.this,
                    "Ошибка подлючения к серверу", Toast.LENGTH_SHORT);
            toast.show();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        enabledCount = 0;
    }
}


