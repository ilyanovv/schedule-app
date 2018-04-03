package com.example.schedulemai.student;

/**
 * Created by Илья on 13.02.2016.
 */
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.schedulemai.R;
import com.example.schedulemai.lesson.Lesson;
import com.example.schedulemai.lesson.LessonFactory;

//TODO: РЕАЛИЗОВАТЬ ДОБАВЛЕНИЕ ЗАНЯТИЯ В ЛОКАЛЬНУЮ БАЗУ ДАННЫХ
//TODO: Обработать получаемые из базы значения: они могут быть равны null
public class StudentNewLessonActivity extends Activity {
    String[] data = Lesson.getLessonTypes();
    EditText name;
    EditText teacher;
    EditText begin_time;
    EditText end_time;
    EditText lesson_room;
    Spinner type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_new_lesson);

        name = (EditText) findViewById(R.id.name1);
        teacher = (EditText) findViewById(R.id.teacher1);
        begin_time = (EditText) findViewById(R.id.editTextBeginTime);
        end_time = (EditText) findViewById(R.id.editTextEndTime);
        lesson_room = (EditText) findViewById(R.id.editTextLessonRoom);
        type = (Spinner) findViewById(R.id.type1);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        type.setAdapter(adapter);
        type.setSelection(0);

        Button save = (Button) findViewById(R.id.save_button);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(begin_time.getText().toString())){ //null or zero-length
                    //TODO:почему не всплывает ошибка?  solved: requestFocus() помогает
                    begin_time.requestFocus();
                    begin_time.setError("Это поле должно быть заполнено");
                    //Toast.makeText(StudentNewLessonActivity.this, "Поле \"Начало\" должно быть заполнено ", Toast.LENGTH_SHORT).show();
                    Log.e("ERROR", "ERROR");
                    return;
                }
                Intent intent = new Intent(StudentNewLessonActivity.this, StudentScheduleActivity.class);
                String nameStr = name.getText().toString();
                String typeStr = type.getSelectedItem().toString();
                String dateStr = (String)getIntent().getExtras().get("curDate");
                String beginTimeStr = begin_time.getText().toString();
                String endTimeStr = end_time.getText().toString();
                String classroomStr = lesson_room.getText().toString();
                String teacherStr = teacher.getText().toString();
                String groupsStr = null;
                Lesson newLesson =  LessonFactory.createLesson(null, nameStr,
                        teacherStr, typeStr, beginTimeStr, endTimeStr,
                        classroomStr, dateStr, groupsStr);
                StudentScheduleActivity.dc.insert_into_db(newLesson, StudentNewLessonActivity.this);
                intent.putExtra("dateSt", newLesson.getLessonDate());
                startActivity(intent);
                finish();
            }
        });
    }
}
/*    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_lesson, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}*/

