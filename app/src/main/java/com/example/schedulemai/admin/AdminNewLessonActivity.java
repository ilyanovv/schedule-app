package com.example.schedulemai.admin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
import android.widget.Toast;

import com.example.schedulemai.R;
import com.example.schedulemai.SP;
import com.github.pinball83.maskededittext.MaskedEditText;

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

//TODO: добавить новое занятие и в dc
public class AdminNewLessonActivity extends AppCompatActivity {
    String[] teachersData, teachersIDData;
    String[] disciplineData, disciplineIDData;
    String[] buildingsData, buildingsIDData;
    String[] roomsData, roomsIDData;
    String[] lessonTypesData, lessonTypesIDData;
    ArrayAdapter teacherNameAdapter, lessonAdapter, lessonTypesAdapter, buildingsAdapter, roomsAdapter;


    Button saveButton, refreshButton;
    Spinner spLessonName, spLessonType, spBuilding, spRoom, spTeacher;
    MaskedEditText etBeginTime, etEndTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_new_lesson);

        saveButton = (Button) findViewById(R.id.save_button_a);
        refreshButton = (Button) findViewById(R.id.buttonRefresh_a);
        spTeacher = (Spinner) findViewById(R.id.sp_teachername_a);
        spLessonName = (Spinner) findViewById(R.id.sp_lessonname_a);
        spLessonType = (Spinner) findViewById(R.id.sp_lessontype_a);
        spRoom = (Spinner) findViewById(R.id.sp_room_a);
        spBuilding = (Spinner) findViewById(R.id.sp_building_a);
        etBeginTime = (MaskedEditText) findViewById(R.id.editTextBeginTime_a);
        etEndTime = (MaskedEditText) findViewById(R.id.editTextEndTime_a);

        new TeacherNameParseTask().execute();
        new BuildingsParseTask().execute();
        new LessonTypesParseTask().execute();

        spTeacher.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.e("teacherIDData", teachersIDData[position]);
                new LessonsParseTask().execute(teachersIDData[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spBuilding.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.e("buildingsIDData", buildingsIDData[position]);
                new RoomsParseTask().execute(buildingsIDData[position]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TeacherNameParseTask().execute();
                new BuildingsParseTask().execute();
                new LessonTypesParseTask().execute();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(AdminNewLessonActivity.this);
                String[] reqParam = new String[10];
                reqParam[0] = sPref.getString(SP.SP_LOGIN, null);
                reqParam[1] = sPref.getString(SP.SP_PASSWORD, null);
                reqParam[2] = sPref.getString(SP.SP_GROUP_ID, null);
                reqParam[3] = disciplineIDData[spLessonName.getSelectedItemPosition()];
                reqParam[4] = lessonTypesIDData[spLessonType.getSelectedItemPosition()];
                reqParam[5] = roomsIDData[spRoom.getSelectedItemPosition()];
                reqParam[6] = getIntent().getExtras().getString("curDate");
                reqParam[7] = etBeginTime.getUnmaskedText();
                reqParam[8] = etEndTime.getUnmaskedText();

                //TODO: добавить другие параметры
                reqParam[9] = "0";
                new AddNewLessonParseTask().execute(reqParam);
            }
        });

    }


    private class TeacherNameParseTask extends AsyncTask<Void, Void, JSONArray> {
        @Override
        protected JSONArray doInBackground(Void... params) {
            JSONArray jsonArray = null;
            try {
                URL url = new URL(SP.ROOT_SERVICE_URL + "/teachers");
                jsonArray = doInBackGroundFunction(url);
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
            if(jsonArray == null){
                onCancelled();
            } else{
                Log.e("JSON", jsonArray.toString());
                int jsonArrayLength = jsonArray.length();
                teachersData = new String[jsonArrayLength];
                teachersIDData = new String[jsonArrayLength];
                for (int i = 0; i < jsonArrayLength; i++) {
                    try {
                        teachersData[i] = jsonArray.getJSONObject(i).getString("last_name") + " " +
                                jsonArray.getJSONObject(i).getString("first_name") +  " " +
                                jsonArray.getJSONObject(i).getString("patronymic_name");
                        teachersIDData[i] = jsonArray.getJSONObject(i).getString("teacher_id");
                        Log.e("AsyncteachersIDData", teachersIDData[i]);
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                }
                teacherNameAdapter = new ArrayAdapter<String>(AdminNewLessonActivity.this,
                        android.R.layout.simple_spinner_item, teachersData);
                teacherNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spTeacher.setAdapter(teacherNameAdapter);
                spTeacher.setSelection(0);
               // new LessonsParseTask().execute(teachersIDData[spTeacher.getSelectedItemPosition()]);
            }
        }

        @Override
        protected  void onCancelled() {
            onCancelledFunction();
        }
    }

    private class LessonsParseTask extends AsyncTask<String, Void, JSONArray> {
        @Override
        protected JSONArray doInBackground(String... params) {
            JSONArray jsonArray = null;
            try {
                Log.e("params[0]", params[0]);
                URL url = new URL(SP.ROOT_SERVICE_URL + "/lessons?teacherID=" + params[0]);
                jsonArray = doInBackGroundFunction(url);
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
            if(jsonArray == null){
                onCancelled();
            } else{
                Log.e("JSON", jsonArray.toString());
                int jsonArrayLength = jsonArray.length();
                disciplineData = new String[jsonArrayLength];
                disciplineIDData = new String[jsonArrayLength];
                for (int i = 0; i < jsonArrayLength; i++) {
                    try {
                        disciplineData[i] = jsonArray.getJSONObject(i).getString("lesson_name");
                        disciplineIDData[i] = jsonArray.getJSONObject(i).getString("discipline_id");
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                }
                lessonAdapter = new ArrayAdapter<String>(AdminNewLessonActivity.this,
                        android.R.layout.simple_spinner_item, disciplineData);
                lessonAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spLessonName.setAdapter(lessonAdapter);
                spLessonName.setSelection(0);
                setSaveButtonEnabled();
            }
        }

        @Override
        protected  void onCancelled() {
            onCancelledFunction();
        }
    }

    private class BuildingsParseTask extends AsyncTask<Void, Void, JSONArray> {
        @Override
        protected JSONArray doInBackground(Void... params) {
            JSONArray jsonArray = null;
            try {
                URL url = new URL(SP.ROOT_SERVICE_URL + "/all_buildings");
                jsonArray = doInBackGroundFunction(url);
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
            if(jsonArray == null){
                onCancelled();
            } else{
                Log.e("JSON", jsonArray.toString());
                int jsonArrayLength = jsonArray.length();
                buildingsData = new String[jsonArrayLength];
                buildingsIDData = new String[jsonArrayLength];
                for (int i = 0; i < jsonArrayLength; i++) {
                    try {

                        buildingsData[i] = jsonArray.getJSONObject(i).getString("building_name");
                        buildingsIDData[i] = jsonArray.getJSONObject(i).getString("building_id");
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                }
                buildingsAdapter = new ArrayAdapter<String>(AdminNewLessonActivity.this,
                        android.R.layout.simple_spinner_item, buildingsData);
                buildingsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spBuilding.setAdapter(buildingsAdapter);
                spBuilding.setSelection(0);
               // new RoomsParseTask().execute(buildingsIDData[spBuilding.getSelectedItemPosition()]);
            }
        }

        @Override
        protected  void onCancelled() {
           onCancelledFunction();
        }
    }

    private class RoomsParseTask extends AsyncTask<String, Void, JSONArray> {
        @Override
        protected JSONArray doInBackground(String... params) {
            JSONArray jsonArray = null;
            try {
                Log.e("params[0]", params[0]);
                URL url = new URL(SP.ROOT_SERVICE_URL + "/lesson_rooms?buildingID=" + params[0]);
                Log.e("URL", url.toString());
                jsonArray = doInBackGroundFunction(url);
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
            if(jsonArray == null){
                onCancelled();
            } else{
                Log.e("JSON", jsonArray.toString());
                int jsonArrayLength = jsonArray.length();
                roomsData = new String[jsonArrayLength];
                roomsIDData = new String[jsonArrayLength];
                for (int i = 0; i < jsonArrayLength; i++) {
                    try {
                        roomsData[i] = jsonArray.getJSONObject(i).getString("lecture_room_number");
                        roomsIDData[i] = jsonArray.getJSONObject(i).getString("lecture_room_id");
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                }
                roomsAdapter = new ArrayAdapter<String>(AdminNewLessonActivity.this,
                        android.R.layout.simple_spinner_item, roomsData);
                roomsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spRoom.setAdapter(roomsAdapter);
                spRoom.setSelection(0);
                setSaveButtonEnabled();
            }
        }

        @Override
        protected  void onCancelled() {
            onCancelledFunction();
        }
    }

    private class LessonTypesParseTask extends AsyncTask<Void, Void, JSONArray> {
        @Override
        protected JSONArray doInBackground(Void... params) {
            JSONArray jsonArray = null;
            try {
                URL url = new URL(SP.ROOT_SERVICE_URL + "/lesson_types");
                jsonArray = doInBackGroundFunction(url);
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
            if(jsonArray == null){
                onCancelled();
            } else{
                Log.e("JSON", jsonArray.toString());
                int jsonArrayLength = jsonArray.length();
                lessonTypesData = new String[jsonArrayLength];
                lessonTypesIDData = new String[jsonArrayLength];
                for (int i = 0; i < jsonArrayLength; i++) {
                    try {

                        lessonTypesData[i] = jsonArray.getJSONObject(i).getString("lesson_type_name");
                        lessonTypesIDData[i] = jsonArray.getJSONObject(i).getString("lesson_type_id");
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                }
                lessonTypesAdapter = new ArrayAdapter<String>(AdminNewLessonActivity.this,
                        android.R.layout.simple_spinner_item, lessonTypesData);
                lessonTypesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spLessonType.setAdapter(lessonTypesAdapter);
                spLessonType.setSelection(0);
                setSaveButtonEnabled();
            }
        }

        @Override
        protected  void onCancelled() {
            onCancelledFunction();
        }
    }

    void  onCancelledFunction(){
        Toast toast = Toast.makeText(AdminNewLessonActivity.this,
                "Не удалось подключиться к серверу", Toast.LENGTH_SHORT);
        toast.show();
        refreshButton.setEnabled(true);
        refreshButton.setVisibility(View.VISIBLE);
        saveButton.setEnabled(false);
        saveButton.setVisibility(View.GONE);
    }

    void setSaveButtonEnabled(){
        refreshButton.setEnabled(false);
        refreshButton.setVisibility(View.GONE);
        saveButton.setEnabled(true);
        saveButton.setVisibility(View.VISIBLE);
    }

    JSONArray doInBackGroundFunction(URL url) throws Exception {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.connect();
        InputStream inputStream = urlConnection.getInputStream();
        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        String resultJson = buffer.toString();
        return new JSONArray(resultJson);
    }

    private class AddNewLessonParseTask extends AsyncTask<String, Void, Integer> {
        HttpURLConnection urlConnection = null;
        @Override
        //params[0] = login, params[1] = password
        protected Integer doInBackground(String... params) {
            // получаем данные с внешнего ресурса
            JSONArray jsonArray = null;
            try {
                URL url = new URL(SP.ROOT_SERVICE_URL + "/add_new_lesson");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("login", params[0])
                        .appendQueryParameter("password", params[1])
                        .appendQueryParameter("group_id", params[2])
                        .appendQueryParameter("discipline_id", params[3])
                        .appendQueryParameter("lesson_type_id", params[4])
                        .appendQueryParameter("lecture_room_id", params[5])
                        .appendQueryParameter("lesson_date", params[6])
                        .appendQueryParameter("time_begin", params[7])
                        .appendQueryParameter("time_end", params[8])
                        .appendQueryParameter("param", params[9]);
                String query = builder.build().getEncodedQuery();
                Log.e("query", query.toString());
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
                Toast toast = Toast.makeText(AdminNewLessonActivity.this,
                        "Занятия успешно добавлены", Toast.LENGTH_SHORT);
                toast.show();
                Intent intent = new Intent(AdminNewLessonActivity.this, AdminScheduleActivity.class);
                intent.putExtra("Reload", "YES");
                intent.putExtra("dateSt", getIntent().getExtras().getString("curDate"));
                startActivity(intent);
                finish();
            }
            else{
                Toast toast = Toast.makeText(AdminNewLessonActivity.this,
                        "Ошибка", Toast.LENGTH_SHORT);
                toast.show();
            }
        }


        @Override
        protected  void onCancelled() {
            Toast toast = Toast.makeText(AdminNewLessonActivity.this,
                    "Ошибка подлючения к серверу", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
