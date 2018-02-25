package com.example.schedulemai;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

/**
 * Created by Илья on 13.02.2016.
 */
public class StudentLessonDetailsActivity extends Activity {

    public class Position
    {
        public int pos;
        Position(int position){pos = position;}
    }

    Position pos = new Position(-1);
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_details);
        final Intent prevIntent = getIntent();



        Log.e("", "Started");
        //final int position = savedInstanceState.getInt("position");
        // String position = (String)savedInstanceState.get("position");
        final int position = getIntent().getExtras().getInt("position");
        Log.e("position", Integer.toString(position));
        // Log.e("pos",  Integer.toString(position));
        Button remove = (Button)findViewById(R.id.delete_button);
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Lesson oldLesson = StudentScheduleActivity.dc.get_from_db(position);
                Log.e("dcsize", Integer.toString(StudentScheduleActivity.dc.size_db()));
                Intent intent = new Intent(StudentLessonDetailsActivity.this, StudentScheduleActivity.class);
                intent.putExtra("key", "remove");
                intent.putExtra("dateSt", oldLesson.date);
                StudentScheduleActivity.dc.remove_from_db(position, StudentLessonDetailsActivity.this);
                startActivity(intent);
                finish();
            }
        });


        Button modify = (Button)findViewById(R.id.modify_button);
        modify.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                Intent intent1 = new Intent(StudentLessonDetailsActivity.this, StudentModifyLessonActivity.class);
                Log.e("", "Intent1 created");
                pos.pos = position;
                Lesson oldLesson = StudentScheduleActivity.dc.get_from_db(pos.pos);
                setLessonExtras(intent1, oldLesson); //кладем extras, чтобы вывести предыдущие значения на форму
                startActivityForResult(intent1, 1);
            }
        });
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == 1)
        {
            Log.e("RESULT_OK", "");
            Bundle extras = data.getExtras();
            if(extras == null)
            {
                //вообше такого не должно быть
                Log.e("dgfjgkerg", "");
                return;
            }
            Lesson oldLesson = StudentScheduleActivity.dc.get_from_db(pos.pos);
            StudentScheduleActivity.dc.modify_db(pos.pos, new_lesson(extras, oldLesson), StudentLessonDetailsActivity.this);
            Intent intent2 = new Intent(StudentLessonDetailsActivity.this, StudentScheduleActivity.class);
            intent2.putExtra("key", "update");
            intent2.putExtra("dateSt", oldLesson.date);
            startActivity(intent2);
            finish();
        }
    }


    private Lesson new_lesson(Bundle savedInstanceState, Lesson oldLesson) {
        String name = (String) savedInstanceState.get("lesson_name");
        String teacher = (String) savedInstanceState.get("teacher_fn");
        String type = (String) savedInstanceState.get("lesson_type");
        String begin_time = savedInstanceState.getString("time_begin");
        String end_time = savedInstanceState.getString("time_end");
        String date = oldLesson.date;
        String classroom = savedInstanceState.getString("lecture_room");
        String groups = null;
        return Lesson.CreateNewLesson(type, name, teacher, begin_time, end_time, classroom, date, groups);
    }

    private void setLessonExtras(Intent intent, Lesson lesson){
        intent.putExtra("lesson_name", lesson.name);
        intent.putExtra("lesson_type", lesson.type);
        intent.putExtra("lecture_room", lesson.classroom);
        intent.putExtra("time_begin", lesson.begin_time);
        intent.putExtra("time_end", lesson.end_time);
        intent.putExtra("lesson_date", lesson.date);
        intent.putExtra("teacher_fn", lesson.teacher);
    }
}
