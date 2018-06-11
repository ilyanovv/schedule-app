package com.example.schedulemai.lesson;

import com.example.schedulemai.R;

/**
 * Created by IO.Novikov on 11.06.2018.
 */

public class Condition extends Lesson {

    public Condition(String recordId, String name, String teacher, String lessonType, String timeBegin,
               String timeEnd, String classroom, String lessonDate, String groupNumber) {
        super(recordId, name, teacher, lessonType, timeBegin, timeEnd, classroom, lessonDate, groupNumber);
    }

    @Override
    public int getBackgroundStyle() {
        return R.drawable.condition_back;
    }
}
