package com.example.schedulemai.admin;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.schedulemai.R;
import com.example.schedulemai.SP;
import com.example.schedulemai.lesson.Lesson;
import com.example.schedulemai.lesson.LessonFactory;
import com.example.schedulemai.localdb.Dao;
import com.example.schedulemai.localdb.Tables;
import com.example.schedulemai.utils.search.HintSearch;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.schedulemai.localdb.Dao.getListOfValues;

//TODO: добавить новое занятие и в dc
public class AdminNewLessonActivity extends AppCompatActivity {

    Button saveButton, refreshButton;
    EditText lessonNameEditText, lessonTypeEditText, teacherNameEditText, lessonRoomEditText;
    ListView lessonNameListView, lessonTypeListView, teacherNameListView, lessonRoomListView;
    HintSearch lessonNameSearch, lessonTypeSearch, teacherNameSearch, lessonRoomSearch;
    List<Map<String, String>> lessonNameList, lessonTypeList, teacherNameList, lessonRoomList;

    MaskedEditText etBeginTime, etEndTime;
    Dao dao;
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_new_lesson);
        initViews();
        dao = AdminScheduleActivity.dc.getDao();
        setData();


        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(AdminNewLessonActivity.this);
                String[] reqParam = new String[11];
                reqParam[0] = sPref.getString(SP.SP_LOGIN, null);
                reqParam[1] = sPref.getString(SP.SP_PASSWORD, null);
                reqParam[2] = teacherNameList.get(teacherNameSearch.getClickedPosition()).get("teacher_id");
                reqParam[3] = lessonTypeList.get(lessonTypeSearch.getClickedPosition()).get("lesson_type_id");
                reqParam[4] = lessonNameList.get(lessonNameSearch.getClickedPosition()).get("lesson_id");
                reqParam[5] = lessonRoomList.get(lessonRoomSearch.getClickedPosition()).get("lecture_room_id");
                reqParam[6] = sPref.getString(SP.SP_GROUP_ID, null);
                reqParam[7] = etBeginTime.getUnmaskedText();
                reqParam[8] = etEndTime.getUnmaskedText();
                reqParam[9] = getIntent().getExtras().getString("curDate");
                reqParam[10] = "0";
                new AddNewLessonParseTask().execute(reqParam);
            }
        });

    }

    private void setData() {
        lessonNameList = dao.getValues(Tables.LESSON);
        lessonTypeList = dao.getValues(Tables.LESSON_TYPE);
        teacherNameList = dao.getTeachers();
        lessonRoomList = dao.getValues(Tables.LESSON_ROOM);


        List<String> lessonNames = getListOfValues(lessonNameList, "lesson_name");
        List<String> lessonTypes = getListOfValues(lessonTypeList, "lesson_type_name");
        List<String> teacherNames = getListOfValues(teacherNameList, "teacher_name");
        List<String> lessonRooms = getListOfValues(lessonRoomList, "lecture_room_number");

        lessonNameSearch = new HintSearch(lessonNameEditText, lessonNameListView, context, lessonNames);
        lessonTypeSearch = new HintSearch(lessonTypeEditText, lessonTypeListView, context, lessonTypes);
        teacherNameSearch = new HintSearch(teacherNameEditText, teacherNameListView, context, teacherNames);
        lessonRoomSearch = new HintSearch(lessonRoomEditText, lessonRoomListView, context, lessonRooms);

        lessonNameSearch.setUp();
        lessonTypeSearch.setUp();
        teacherNameSearch.setUp();
        lessonRoomSearch.setUp();
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
                urlConnection.setConnectTimeout(5000);
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("login", params[0])
                        .appendQueryParameter("password", params[1])
                        .appendQueryParameter("teacher_id", params[2])
                        .appendQueryParameter("lesson_type_id", params[3])
                        .appendQueryParameter("lesson_id", params[4])
                        .appendQueryParameter("lecture_room_id", params[5])
                        .appendQueryParameter("group_id", params[6])
                        .appendQueryParameter("time_begin", params[7])
                        .appendQueryParameter("time_end", params[8])
                        .appendQueryParameter("lesson_date", params[9])
                        .appendQueryParameter("param", params[10]);
                String query = builder.build().getEncodedQuery();
                Log.e("query", query.toString());
                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                Log.e("OUTPUT", urlConnection.getResponseMessage());
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
                AdminScheduleActivity.dc.update_db(getIntent().getExtras().getString("curDate"), context);
                AdminScheduleActivity.data.clear();
                AdminScheduleActivity.data.addAll(AdminScheduleActivity.setAdapterValues());
                AdminScheduleActivity.adapter.notifyDataSetChanged();
                SharedPreferences.Editor editor =
                        PreferenceManager.getDefaultSharedPreferences(
                                AdminNewLessonActivity.this).edit();
                editor.putInt(SP.SP_LOCAL_DB_VERSION, -1);
                editor.apply();
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

    private void initViews() {
        saveButton = findViewById(R.id.save_button_a);
        refreshButton = findViewById(R.id.buttonRefresh_a);
        lessonNameEditText = findViewById(R.id.lessonNameAdminEditText);
        lessonTypeEditText = findViewById(R.id.lessonTypeAdminEditText);
        teacherNameEditText = findViewById(R.id.teacherNameAdminEditText);
        lessonRoomEditText = findViewById(R.id.lessonRoomAdminEditText);
        lessonNameListView = findViewById(R.id.lessonNameAdminListView);
        lessonTypeListView = findViewById(R.id.lessonTypeAdminListView);
        teacherNameListView = findViewById(R.id.teacherNameAdminListView);
        lessonRoomListView = findViewById(R.id.lessonRoomAdminListView);
        etBeginTime = findViewById(R.id.editTextBeginTime_a);
        etEndTime = findViewById(R.id.editTextEndTime_a);
    }
}
