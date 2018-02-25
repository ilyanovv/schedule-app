package com.example.schedulemai;

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

//TODO: РЕАЛИЗОВАТЬ ДОБАВЛЕНИЕ ЗАНЯТИЯ В ЛОКАЛЬНУЮ БАЗУ ДАННЫХ
//TODO: Обработать получаемые из базы значения: они могут быть равны null
public class TeacherNewLessonActivity extends Activity {
    String[] data = {"Лекция", "Семинар", "Лабораторная"};
    EditText name;
    EditText groups;
    EditText begin_time;
    EditText end_time;
    EditText lesson_room;
    Spinner type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_new_lesson);

        name = (EditText) findViewById(R.id.name1_t);
        groups = (EditText) findViewById(R.id.groups1_t);
        begin_time = (EditText) findViewById(R.id.editTextBeginTime_t);
        end_time = (EditText) findViewById(R.id.editTextEndTime_t);
        lesson_room = (EditText) findViewById(R.id.editTextLessonRoom_t);
        type = (Spinner) findViewById(R.id.type1_t);

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
                Intent intent = new Intent(TeacherNewLessonActivity.this, TeacherScheduleActivity.class);
                String nameStr = name.getText().toString();
                String typeStr = type.getSelectedItem().toString();
                String dateStr = (String)getIntent().getExtras().get("curDate");
                String beginTimeStr = begin_time.getText().toString();
                String endTimeStr = end_time.getText().toString();
                String classroomStr = lesson_room.getText().toString();
                String groupsStr = groups.getText().toString();
                String teacherStr = null;
                Lesson newLesson = Lesson.CreateNewLesson(typeStr, nameStr, teacherStr, beginTimeStr, endTimeStr,
                        classroomStr, dateStr, groupsStr);
                TeacherScheduleActivity.dc.insert_into_db(newLesson, TeacherNewLessonActivity.this);
                intent.putExtra("dateSt", newLesson.date);
                startActivity(intent);
                finish();
            }
        });
    }
}

