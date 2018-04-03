package com.example.schedulemai.teacher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.schedulemai.R;
import com.example.schedulemai.lesson.Lesson;
import com.example.schedulemai.lesson.LessonFactory;

/**
 * Created by Илья on 13.02.2016.
 */
public class TeacherLessonDetailsActivity extends Activity {

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
                Lesson oldLesson = TeacherScheduleActivity.dc.get_from_db(position);
                Log.e("dcsize", Integer.toString(TeacherScheduleActivity.dc.size_db()));
                Intent intent = new Intent(TeacherLessonDetailsActivity.this, TeacherScheduleActivity.class);
                intent.putExtra("key", "remove");
                intent.putExtra("dateSt", oldLesson.getLessonDate());
                TeacherScheduleActivity.dc.remove_from_db(position, TeacherLessonDetailsActivity.this);
                startActivity(intent);
                finish();
            }
        });


        Button modify = (Button)findViewById(R.id.modify_button);
        modify.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                Intent intent1 = new Intent(TeacherLessonDetailsActivity.this, TeacherModifyLessonActivity.class);
                Log.e("", "Intent1 created");
                pos.pos = position;
                Lesson oldLesson = TeacherScheduleActivity.dc.get_from_db(pos.pos);
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
            Lesson oldLesson = TeacherScheduleActivity.dc.get_from_db(pos.pos);
            TeacherScheduleActivity.dc.modify_db(pos.pos, new_lesson(extras, oldLesson), TeacherLessonDetailsActivity.this);
            Intent intent2 = new Intent(TeacherLessonDetailsActivity.this, TeacherScheduleActivity.class);
            intent2.putExtra("key", "update");
            intent2.putExtra("dateSt", oldLesson.getLessonDate());
            startActivity(intent2);
            finish();
        }
    }


    private Lesson new_lesson(Bundle savedInstanceState, Lesson oldLesson) {
        String name = (String) savedInstanceState.get("lesson_name");
        String teacher = null;
        String type = (String) savedInstanceState.get("lesson_type");
        String timeBegin = savedInstanceState.getString("time_begin");
        String timeEnd = savedInstanceState.getString("time_end");
        String date = oldLesson.getLessonDate();
        String classroom = savedInstanceState.getString("lecture_room");
        String groups = (String) savedInstanceState.get("groups");
        return LessonFactory.createLesson(null, name, teacher, type, timeBegin, timeEnd,
                classroom, date, groups);
    }

    private void setLessonExtras(Intent intent, Lesson lesson){
        intent.putExtra("lesson_name", lesson.getName());
        intent.putExtra("lesson_type", lesson.getLessonType());
        intent.putExtra("lecture_room", lesson.getClassroom());
        intent.putExtra("time_begin", lesson.getTimeBegin());
        intent.putExtra("time_end", lesson.getTimeEnd());
        intent.putExtra("lesson_date", lesson.getLessonDate());
        intent.putExtra("groups", lesson.getGroupNumber());
        intent.putExtra("record_id", lesson.getRecordId());
    }
}
