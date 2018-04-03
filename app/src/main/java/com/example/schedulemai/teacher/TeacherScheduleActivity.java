package com.example.schedulemai.teacher;

/**
 * Created by Илья on 13.02.2016.
 */
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.schedulemai.DownloadActivity;
import com.example.schedulemai.R;
import com.example.schedulemai.SP;
import com.example.schedulemai.authentication.AuthenticationActivity;
import com.example.schedulemai.lesson.Lesson;
import com.example.schedulemai.localdb.DataController;
import com.example.schedulemai.localdb.TeacherDataController;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

//TODO: при удалении/изменении занятия возвращаться на ту же дату
public class TeacherScheduleActivity extends AppCompatActivity {

    Context cont = this;
    public static DataController dc = new TeacherDataController();
    SimpleAdapter adapter;

    final String ATTRIBUTE_LESSON_NAME = "lesson_name";
    final String ATTRIBUTE_LESSON_TYPE = "lesson_type";
    final String ATTRIBUTE_LESSON_GROUPS = "lesson_groups";
    final String ATTRIBUTE_LESSON_TIME = "lesson_time";
    final String ATTRIBUTE_LESSON_ROOM = "lesson_room";
    final String ATTRIBUTE_LESSON_COLOR = "lesson_color";

    String dateSt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_schedule);
    }

    @Override
    protected  void onResume(){
        super.onResume();
        Log.e("onResume:", "TeacherScheduleActivity");

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
        TextView dateTextView = (TextView) findViewById(R.id.dateText_t);
        String str = sdf.format(curDate);
        dateTextView.setText(str);

        sdf = new SimpleDateFormat("dd.MM.yyyy", locale); //Date and time
        dateSt = sdf.format(curDate);
        Log.e("dateSt = ", dateSt);
        dc.update_db(dateSt, cont);

        Lesson lesson = null;
        ListView lv = (ListView) findViewById(R.id.lessons_t);
        String[] lessons_names = new String[dc.size_db()];
        String[] lessons_types = new String[dc.size_db()];
        String[] lessons_groups = new String[dc.size_db()];
        String[] lessons_time = new String[dc.size_db()];
        String[] lessons_rooms = new String[dc.size_db()];
        int[] lessons_colors = new int[dc.size_db()];
        for (int i = 0; i < dc.size_db(); i++) {
            lesson = dc.get_from_db(i);
            lessons_names[i] = lesson.getName();
            lessons_types[i] = lesson.getLessonType();
            lessons_groups[i] = lesson.getGroupNumber();
            lessons_time[i] = lesson.getTimeBegin() + " - " + lesson.getTimeEnd();
            lessons_rooms[i] = lesson.getClassroom();
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
            m.put(ATTRIBUTE_LESSON_GROUPS, lessons_groups[i]);
            m.put(ATTRIBUTE_LESSON_COLOR, lessons_colors[i]);
            m.put(ATTRIBUTE_LESSON_TIME, lessons_time[i]);
            m.put(ATTRIBUTE_LESSON_ROOM, lessons_rooms[i]);
            data.add(m);
        }

        // массив имен атрибутов, из которых будут читаться данные
        String[] from = { ATTRIBUTE_LESSON_NAME, ATTRIBUTE_LESSON_TYPE, ATTRIBUTE_LESSON_GROUPS,
                ATTRIBUTE_LESSON_TIME, ATTRIBUTE_LESSON_ROOM, ATTRIBUTE_LESSON_TYPE};
        // массив ID View-компонентов, в которые будут вставлять данные
        int[] to = { R.id.schedule_item1t, R.id.schedule_item2t, R.id.schedule_item3t,
                R.id.schedule_item4t, R.id.schedule_item5t, R.id.schedule_item_layout_t};



        /*ArrayAdapter<String> groupNumberAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                lessons);*/
        adapter = new SimpleAdapter(this, data, R.layout.schedule_item_teacher, from, to);
        adapter.setViewBinder(new MyViewBinder());
        lv.setAdapter(adapter);


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemClicked, int position,
                                    long id) {
                Intent intent = new Intent(TeacherScheduleActivity.this, TeacherLessonDetailsActivity.class);
                intent.putExtra("position", position);
                //Log.e("pos", Integer.toString(position));
                startActivity(intent);
                // finish();
            }
        });



        Button add_button = (Button) findViewById(R.id.add_button_t);
        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TeacherScheduleActivity.this, TeacherNewLessonActivity.class);
                intent.putExtra("curDate", dateSt);
                startActivity(intent);
                //finish();
            }
        });


        Button downloadButton = (Button) findViewById(R.id.download_button_t);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TeacherScheduleActivity.this, DownloadActivity.class);
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
        setIntent(intent);

    }


    @Override
    public void onDestroy(){
        // Очистите все ресурсы. Это касается завершения работы
        // потоков, закрытия соединений с базой данных и т. д.
        dc.database.close();
        dc.database = null;
        super.onDestroy();
    }


    class MyViewBinder implements SimpleAdapter.ViewBinder {

        @Override
        public boolean setViewValue(View view, Object data,
                                    String textRepresentation) {
            switch (view.getId()) {
                // LinearLayout
                case R.id.schedule_item_layout_t:
                    String lessonType =  data.toString();
                    view.setBackgroundResource(Lesson.getBackgroundStyle(lessonType));
                    return true;
            }
            return false;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /*super.onCreateOptionsMenu(menu);

        menu.add(0        // Группа
                ,0        // id
                ,0        //порядок
                ,"Сменить пользователя");  // заголовок*/
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_change_user:
                new TestConnectionTask().execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class TestConnectionTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(TeacherScheduleActivity.this);
            SharedPreferences.Editor editor = sPref.edit();
            try { //пробуем подключиться к серверу: если не получилось, то пользователя не меняем
                URL url = new URL(SP.ROOT_SERVICE_URL + "/get_all_groups");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
            } catch (Exception e) {
                cancel(false);
                while (!isCancelled()){
                    //  Log.e("HERE", "notCancelled");
                }
                return null;
            }
            editor.putString(SP.SP_GROUP_ID, null);
            editor.putString(SP.SP_TEACHER_ID, null);
            editor.putInt(SP.SP_LOCAL_DB_VERSION, -1);
            editor.putInt(SP.SP_GLOBAL_DB_VERSION, -1);
            editor.putString(SP.SP_USER_TYPE, null);
            editor.apply();
            Intent intent = new Intent(TeacherScheduleActivity.this, AuthenticationActivity.class);
            startActivity(intent);
            finish();
            return null;
        }

        @Override
        protected void onCancelled() {
            Toast toast = Toast.makeText(TeacherScheduleActivity.this,
                    "Не удалось подключиться к серверу", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

}






