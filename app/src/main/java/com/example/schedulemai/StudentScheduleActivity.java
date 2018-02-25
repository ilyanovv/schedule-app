package com.example.schedulemai;

/**
 * Created by Илья on 13.02.2016.
 */
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

//TODO: при удалении/изменении занятия возвращаться на ту же дату
public class StudentScheduleActivity extends Activity {

    Context cont = this;
    public static DataController dc = new StudentDataController();

    final String ATTRIBUTE_LESSON_NAME = "lesson_name";
    final String ATTRIBUTE_LESSON_TYPE = "lesson_type";
    final String ATTRIBUTE_LESSON_TEACHER = "lesson_teacher";
    final String ATTRIBUTE_LESSON_TIME = "lesson_time";
    final String ATTRIBUTE_LESSON_ROOM = "lesson_room";
    final String ATTRIBUTE_LESSON_COLOR = "lesson_color";

    String dateSt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_schedule);
    }

    @Override
    protected  void onResume(){
        super.onResume();
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
        TextView dateTextView = (TextView) findViewById(R.id.dateText);
        String str = sdf.format(curDate);
        dateTextView.setText(str);

        sdf = new SimpleDateFormat("dd.MM.yyyy", locale); //Date and time
        dateSt = sdf.format(curDate);
        Log.e("dateSt = ", dateSt);
        dc.update_db(dateSt, cont);

        Lesson lesson = null;
        ListView lv = (ListView) findViewById(R.id.lessons);
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
        int[] to = { R.id.schedule_item1, R.id.schedule_item2, R.id.schedule_item3,
                R.id.schedule_item4, R.id.schedule_item5, R.id.schedule_item_layout};



        /*ArrayAdapter<String> groupNumberAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                lessons);*/
        SimpleAdapter adapter = new SimpleAdapter(this, data, R.layout.schedule_item_student, from, to);
        adapter.setViewBinder(new MyViewBinder());
        lv.setAdapter(adapter);


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemClicked, int position,
                                    long id) {
                Intent intent = new Intent(StudentScheduleActivity.this, StudentLessonDetailsActivity.class);
                intent.putExtra("position", position);
                //Log.e("pos", Integer.toString(position));
                startActivity(intent);
                // finish();
            }
        });



        Button add_button = (Button) findViewById(R.id.add_button);
        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StudentScheduleActivity.this, StudentNewLessonActivity.class);
                intent.putExtra("curDate", dateSt);
                startActivity(intent);
                //finish();
            }
        });


        Button downloadButton = (Button) findViewById(R.id.download_button);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StudentScheduleActivity.this, DownloadActivity.class);
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
        Log.e("onNewIntent", intent.getExtras().getString("dateSt"));
        setIntent(intent);

    }

  /*  @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        onCreate(intent.getExtras());
        setIntent(intent);
    }*/


    class MyViewBinder implements SimpleAdapter.ViewBinder {

        @Override
        public boolean setViewValue(View view, Object data,
                                    String textRepresentation) {
            switch (view.getId()) {
                // LinearLayout
                case R.id.schedule_item_layout:
                    String lessonType =  data.toString();
                    view.setBackgroundResource(getBackgroundStyle(lessonType));
                    return true;
            }
            return false;
        }
    }


    public static int getBackgroundStyle(String lessonType) {
        switch (lessonType){
            case "Лабораторная":
                return R.drawable.lab_back;
            case "Семинар":
                return R.drawable.sem_back;
            case "Лекция":
                return R.drawable.lection_back;
            default:
                return Color.RED;
        }
    }
}





