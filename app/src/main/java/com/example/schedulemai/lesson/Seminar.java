package com.example.schedulemai.lesson;

import com.example.schedulemai.R;


/**
 * Created by IO.Novikov on 01.04.2018.
 */


public class Seminar extends Lesson {

    public Seminar(String recordId, String name, String teacher, String lessonType, String timeBegin,
                   String timeEnd, String classroom, String lessonDate, String groupNumber) {
        super(recordId, name, teacher, lessonType, timeBegin, timeEnd, classroom, lessonDate, groupNumber);
    }

    @Override
    public int getBackgroundStyle() {
        return R.drawable.sem_back;
    }
}
