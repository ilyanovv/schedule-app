package com.example.schedulemai.student;

/**
 * Created by Илья on 13.02.2016.
 */
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.schedulemai.R;
import com.example.schedulemai.lesson.Lesson;

import java.util.List;


public class StudentModifyLessonActivity extends Activity {
    List<String> data;
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
        data = StudentScheduleActivity.dc.getDao().getLessonTypes();


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        type.setAdapter(adapter);
        //lessonType.setSelection(0);
        setPreviousSelection();

        Button save = (Button) findViewById(R.id.save_button);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(begin_time.getText().toString())){ //null or zero-length
                    begin_time.requestFocus();
                    begin_time.setError("Это поле должно быть заполнено");
                    return;
                }
                Intent intent = new Intent(StudentModifyLessonActivity.this, StudentLessonDetailsActivity.class);
                // Intent intent = new Intent();
                intent.putExtra("lesson_name", name.getText().toString());
                intent.putExtra("lesson_type", type.getSelectedItem().toString());

                intent.putExtra("teacher_fn", teacher.getText().toString());
                intent.putExtra("lecture_room", lesson_room.getText().toString());
                intent.putExtra("time_begin", begin_time.getText().toString());
                intent.putExtra("time_end", end_time.getText().toString());
                setResult(RESULT_OK, intent);

                finish();
            }
        });
    }

    void setPreviousSelection(){
        Bundle extras = getIntent().getExtras();
        name.setText(extras.getString("lesson_name"));
        teacher.setText(extras.getString("teacher_fn"));
        begin_time.setText(extras.getString("time_begin"));
        end_time.setText(extras.getString("time_end"));
        lesson_room.setText(extras.getString("lecture_room"));
        String lesson_type = (String)extras.getString("lesson_type");
        for(int i=0; i<data.size(); i++){
            if (data.get(i).equals(lesson_type)){
                type.setSelection(i);
                break;
            }
        }
    };
}



