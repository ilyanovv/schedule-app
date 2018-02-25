package com.example.schedulemai;

/**
 * Created by Ilya on 23.03.2016.
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;

import java.text.SimpleDateFormat;
import java.util.Locale;


public class DownloadActivity extends Activity {


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        final CalendarView calView = (CalendarView)findViewById(R.id.downloadCalendar);
        Button buttonOK = (Button)findViewById(R.id.buttonOK);
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(DownloadActivity.this, StudentScheduleActivity.class);
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

