package com.example.schedulemai;

/**
 * Created by Ilya on 23.03.2016.
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;

import com.example.schedulemai.admin.AdminScheduleActivity;
import com.example.schedulemai.student.StudentScheduleActivity;
import com.example.schedulemai.teacher.TeacherScheduleActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class DownloadActivity extends Activity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        final CalendarView calView = findViewById(R.id.downloadCalendar);
        calView.setOnDateChangeListener((calendarView, year, month, day) -> {
            Locale locale = new Locale("ru", "RU");
            Calendar c = Calendar.getInstance();
            c.set(year, month, day);
            calendarView.setDate(c.getTimeInMillis());
        });


        Button buttonOK = (Button) findViewById(R.id.buttonOK);
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = null;
                String userType = PreferenceManager.
                        getDefaultSharedPreferences(
                                DownloadActivity.this).getString(SP.SP_USER_TYPE, "");
                switch (userType) {
                    case SP.STUDENT_TYPE:
                        intent = new Intent(DownloadActivity.this, StudentScheduleActivity.class);
                        break;
                    case SP.ADMIN_TYPE:
                        intent = new Intent(DownloadActivity.this, AdminScheduleActivity.class);
                        break;
                    case SP.TEACHER_TYPE:
                        intent = new Intent(DownloadActivity.this, TeacherScheduleActivity.class);
                        break;
                    default:
                        Log.e("DOWNLOAD_ACTIVITY", "unknown user lessonType");
                        break;
                }
                Locale locale = new Locale("ru", "RU");
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", locale);
                String dateSt = sdf.format(calView.getDate());

                intent.putExtra("dateSt", dateSt);
                intent.putExtra("key", "download");
                startActivity(intent);
                //setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}

