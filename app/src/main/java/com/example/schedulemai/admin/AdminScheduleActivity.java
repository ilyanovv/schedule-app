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
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.schedulemai.authentication.AuthenticationActivity;
import com.example.schedulemai.localdb.DataController;
import com.example.schedulemai.DownloadActivity;
import com.example.schedulemai.lesson.Lesson;
import com.example.schedulemai.R;
import com.example.schedulemai.SP;
import com.example.schedulemai.localdb.StudentDataController;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AdminScheduleActivity extends AppCompatActivity {

    Context cont = this;
    public static DataController dc = new StudentDataController();
    public static SimpleAdapter adapter;

    final String ATTRIBUTE_LESSON_NAME = "lesson_name";
    final String ATTRIBUTE_LESSON_TYPE = "lesson_type";
    final String ATTRIBUTE_LESSON_TEACHER = "lesson_teacher";
    final String ATTRIBUTE_LESSON_TIME = "lesson_time";
    final String ATTRIBUTE_LESSON_ROOM = "lesson_room";
    final String ATTRIBUTE_LESSON_COLOR = "lesson_color";

    String dateSt;
    SharedPreferences sPref;
    SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_schedule);
        sPref = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sPref.edit();
        editor.putInt(SP.SP_LOCAL_DB_VERSION, -1);
        editor.apply();

    }

    @Override
    protected  void onResume(){
        super.onResume();
        Log.e("onResume ", "AdminScheduleActivity");
        dateSt = null;
        Bundle extras = getIntent().getExtras();
        Date curDate;
        if(extras == null)
            Log.e("EXTRAS = ", "NULL");


        try{
            dateSt = extras.getString("dateSt");
            Log.e("dateSt = ", dateSt);
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            curDate = sdf.parse(dateSt);
        }catch (Exception e) {
            e.printStackTrace();
            curDate = new Date();
        }


        /*
            Установим дату; если она не выбрана, то ставим текущую
         */
        Locale locale = new Locale("ru", "RU");
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy, EEEE", locale); //Date and time
        TextView dateTextView = (TextView) findViewById(R.id.dateText_a);
        String str = sdf.format(curDate);
        dateTextView.setText(str);

        sdf = new SimpleDateFormat("dd.MM.yyyy", locale); //Date and time
        dateSt = sdf.format(curDate);
        Log.e("dateSt = ", dateSt);
        dc.update_db(dateSt, cont);

        Lesson lesson = null;
        ListView lv = (ListView) findViewById(R.id.lessons_a);
        String[] lessons_names = new String[dc.size_db()];
        String[] lessons_types = new String[dc.size_db()];
        String[] lessons_teachers = new String[dc.size_db()];
        String[] lessons_time = new String[dc.size_db()];
        String[] lessons_rooms = new String[dc.size_db()];
        int[] lessons_colors = new int[dc.size_db()];
        for (int i = 0; i < dc.size_db(); i++) {
            lesson = dc.get_from_db(i);
            lessons_names[i] = lesson.name;
            lessons_types[i] = lesson.type;
            lessons_teachers[i] = lesson.teacher;
            lessons_time[i] = lesson.begin_time + " - " + lesson.end_time;
            lessons_rooms[i] = lesson.classroom;
            lessons_colors[i] = 0xaaff00;
        }


        // упаковываем данные в понятную для адаптера структуру
        ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>(
                lessons_names.length);
        Map<String, Object> m;
        for (int i = 0; i < lessons_names.length; i++) {
            m = new HashMap<String, Object>();
            m.put(ATTRIBUTE_LESSON_NAME, lessons_names[i]);
            m.put(ATTRIBUTE_LESSON_TYPE, lessons_types[i]);
            m.put(ATTRIBUTE_LESSON_TEACHER, lessons_teachers[i]);
            m.put(ATTRIBUTE_LESSON_COLOR, lessons_colors[i]);
            m.put(ATTRIBUTE_LESSON_TIME, lessons_time[i]);
            m.put(ATTRIBUTE_LESSON_ROOM, lessons_rooms[i]);
            data.add(m);
        }

        // массив имен атрибутов, из которых будут читаться данные
        String[] from = { ATTRIBUTE_LESSON_NAME, ATTRIBUTE_LESSON_TYPE, ATTRIBUTE_LESSON_TEACHER,
                ATTRIBUTE_LESSON_TIME, ATTRIBUTE_LESSON_ROOM, ATTRIBUTE_LESSON_TYPE};
        // массив ID View-компонентов, в которые будут вставлять данные
        int[] to = { R.id.schedule_item1a, R.id.schedule_item2a, R.id.schedule_item3a,
                R.id.schedule_item4a, R.id.schedule_item5a, R.id.admin_item_layout};



        /*ArrayAdapter<String> groupNumberAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                lessons);*/
        adapter = new SimpleAdapter(this, data, R.layout.schedule_item_admin, from, to);
        adapter.setViewBinder(new MyViewBinder());
        lv.setAdapter(adapter);

        //Создадим контекстное меню для ListView
        lv.setOnCreateContextMenuListener(this); //то же, что и registerForContextMenu(lv)



        Button add_button = (Button) findViewById(R.id.add_button_a);
        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminScheduleActivity.this, AdminNewLessonActivity.class);
                intent.putExtra("curDate", dateSt);
                startActivity(intent);
            }
        });


        Button downloadButton = (Button) findViewById(R.id.download_button_a);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminScheduleActivity.this, DownloadActivity.class);
                Log.e("start", "");
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // getIntent() should always return the most recent
        Log.e("onNewIntent", Integer.toString(dc.size_db()));
        try {
            Log.e("onNewIntent", intent.getExtras().getString("dateSt"));
        }catch (Exception e){};
        try {
            String ReloadSt = intent.getExtras().getString("Reload");
            if(ReloadSt.equals("YES")){
                editor.putInt(SP.SP_LOCAL_DB_VERSION, -1);
                editor.apply();
            }
        }catch (Exception e){e.printStackTrace();};
        setIntent(intent);
    }


    @Override
    public void onDestroy(){
        // Очистите все ресурсы. Это касается завершения работы
        // потоков, закрытия соединений с базой данных и т. д.
        dc.database.close();
        dc.database = null;
        Log.e("DB", "Closed");
        super.onDestroy();
    }


    class MyViewBinder implements SimpleAdapter.ViewBinder {

        @Override
        public boolean setViewValue(View view, Object data,
                                    String textRepresentation) {
            switch (view.getId()) {
                // LinearLayout
                case R.id.admin_item_layout:
                    String lessonType =  data.toString();
                    view.setBackgroundResource(Lesson.getBackgroundStyle(lessonType));
                    return true;
            }
            return false;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_change_user:
                editor.putString(SP.SP_GROUP_ID, null);
                editor.putString(SP.SP_TEACHER_ID, null);
                editor.putInt(SP.SP_LOCAL_DB_VERSION, -1);
                editor.putInt(SP.SP_GLOBAL_DB_VERSION, -1);
                editor.putString(SP.SP_USER_TYPE, null);
                editor.apply();
                Intent intent = new Intent(AdminScheduleActivity.this, AuthenticationActivity.class);
                startActivity(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    //контекстное меню для listView
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
        MenuItem subMenu = menu.findItem(R.id.delete_option);
        getMenuInflater().inflate(R.menu.sub_menu, subMenu.getSubMenu());
        //subMenu.getSubMenu().findItem(R.id.delete_today_option).setChecked(true);
    }


    private int mParentContextMenuListIndex;
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        String login = sPref.getString(SP.SP_LOGIN, "");
        String password = sPref.getString(SP.SP_PASSWORD, "");
        // получаем инфу о пункте списка
        AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        //if info == null, it means we have a submenu to deal with, use the saved info.position (int position = acmi.position;)
        int position = (acmi != null) ? acmi.position : this.mParentContextMenuListIndex;
        switch (item.getItemId()) {
            case R.id.update_option:
                Intent intent = new Intent(AdminScheduleActivity.this, AdminModifyLessonActivity.class);
                Lesson selectedLesson = dc.get_from_db(position);
                setLessonExtras(intent, selectedLesson);
                startActivity(intent);
                return true;
            /*case R.id.delete_option:
                return true;*/
            case R.id.delete_today_option:
                selectedLesson = dc.get_from_db(position);
                new DeleteLessonParseTask().execute(login, password, selectedLesson.record_id, "0");
                return true;
            case R.id.delete_always_option:
                selectedLesson = dc.get_from_db(position);
                new DeleteLessonParseTask().execute(login, password, selectedLesson.record_id, "1");
                return true;
            default: //can handle submenus if we save off acmi.position
                this.mParentContextMenuListIndex = position;
        }
        return super.onContextItemSelected(item);
    }





    private void setLessonExtras(Intent intent, Lesson lesson){
        intent.putExtra("lesson_name", lesson.name);
        intent.putExtra("lesson_type", lesson.type);
        intent.putExtra("lecture_room", lesson.classroom);
        intent.putExtra("time_begin", lesson.begin_time);
        intent.putExtra("time_end", lesson.end_time);
        intent.putExtra("lesson_date", lesson.date);
        intent.putExtra("teacher_fn", lesson.teacher);
        intent.putExtra("record_id", lesson.record_id);
    }


    private class DeleteLessonParseTask extends AsyncTask<String, Void, Integer> {
        HttpURLConnection urlConnection = null;
        @Override
        //params[0] = login, params[1] = password
        protected Integer doInBackground(String... params) {
            try {
                URL url = new URL(SP.ROOT_SERVICE_URL + "/delete_lesson");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("login", params[0])
                        .appendQueryParameter("password", params[1])
                        .appendQueryParameter("record_id", params[2])
                        .appendQueryParameter("param", params[3]);
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
                Toast toast = Toast.makeText(AdminScheduleActivity.this,
                        "Занятия успешно удалены", Toast.LENGTH_SHORT);
                toast.show();
                //Intent intent = getIntent();
                Intent intent = new Intent(AdminScheduleActivity.this, AdminScheduleActivity.class);
                intent.putExtra("Reload", "YES");
                intent.putExtra("dateSt", dateSt);
                startActivity(intent);
            }
            else{
                Toast toast = Toast.makeText(AdminScheduleActivity.this,
                        "Ошибка: код " + responseCode.toString(), Toast.LENGTH_SHORT );
                toast.show();
            }
        }


        @Override
        protected  void onCancelled() {
            Toast toast = Toast.makeText(AdminScheduleActivity.this,
                    "Ошибка подлючения к серверу", Toast.LENGTH_SHORT);
            toast.show();
        }
    }


}
