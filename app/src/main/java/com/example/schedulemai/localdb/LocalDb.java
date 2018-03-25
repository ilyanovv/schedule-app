package com.example.schedulemai.localdb;

import com.example.schedulemai.lesson.Lesson;

import java.util.ArrayList;

/**
 * Created by Илья on 13.02.2016.
 */
public class LocalDb {
    private static ArrayList<Lesson> lessons;
    public LocalDb() {
        lessons = new ArrayList<Lesson>();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    public int add(Lesson lesson) {
        lessons.add(lesson);
        return lessons.size();
    }
    public Lesson get(int i) {
        return  lessons.get(i);
    }

    public void remove(int i){lessons.remove(i);}

    public void modify(int i, Lesson les){lessons.set(i, les);}

    public int size() {
        return lessons.size();
    }
}

