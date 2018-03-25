package com.example.schedulemai.lesson;

import android.graphics.Color;

import com.example.schedulemai.R;

/**
 * Created by Илья on 13.02.2016.
 */
//TODO: перепилить это говно
abstract public class Lesson {
    public String record_id;
    public String name;
    public String teacher;
    public String type;
    public String begin_time;
    public String end_time;
    public String classroom;
    public String date;
    public String group_number;

    public static Lesson CreateNewLesson(String type, String lesson_name, String teacher_name, String begin_time,
                                         String end_time, String classroom, String date, String group_number, String record_id)
    {
        switch (type){
            case "Лабораторная":
                return new Lab(lesson_name, teacher_name, begin_time, end_time, classroom, date, group_number, record_id);
            case "Семинар":
                return new Sem(lesson_name, teacher_name, begin_time, end_time, classroom, date, group_number, record_id);
            case "Лекция":
                return new Lect(lesson_name, teacher_name, begin_time, end_time, classroom, date, group_number, record_id);
            case "Экзамен":
                return new Exam(lesson_name, teacher_name, begin_time, end_time, classroom, date, group_number, record_id);
            case "Зачет":
                return new Cond(lesson_name, teacher_name, begin_time, end_time, classroom, date, group_number, record_id);
            case "Консультация":
                return new Cons(lesson_name, teacher_name, begin_time, end_time, classroom, date, group_number, record_id);
            default:
                return new Lab(lesson_name, teacher_name, begin_time, end_time, classroom, date, group_number, record_id);
        }
    }

    public static int getBackgroundStyle(String lessonType) {
        switch (lessonType){
            case "Лабораторная":
                return R.drawable.lab_back;
            case "Семинар":
                return R.drawable.sem_back;
            case "Лекция":
                return R.drawable.lection_back;
            case "Экзамен":
                return R.drawable.exam_back;
            case "Зачет":
                return R.drawable.condition_back;
            case "Консультация":
                return R.drawable.cons_back;
            default:
                return Color.RED;
        }
    }

    public static String[] getLessonTypes() {
        return new String[] {"Лекция", "Семинар", "Лабораторная", "Экзамен", "Зачет", "Консультация"};
    }
}

class Lab extends Lesson {
    Lab(String name, String teacher,
        String begin_time, String end_time, String classroom, String date, String group_number, String record_id) {
        this.name = name;
        this.teacher = teacher;
        this.type = "Лабораторная";
        this.begin_time = begin_time;
        this.end_time = end_time;
        this.classroom = classroom;
        this.date = date;
        this.group_number = group_number;
        this.record_id = record_id;
    };
}

class Lect extends Lesson {
    Lect(String name, String teacher,
        String begin_time, String end_time, String classroom, String date, String group_number, String record_id) {
        this.name = name;
        this.teacher = teacher;
        this.type = "Лекция";
        this.begin_time = begin_time;
        this.end_time = end_time;
        this.classroom = classroom;
        this.date = date;
        this.group_number = group_number;
        this.record_id = record_id;
    };
}

class Sem extends Lesson {
    Sem(String name, String teacher,
        String begin_time, String end_time, String classroom, String date, String group_number, String record_id) {
        this.name = name;
        this.teacher = teacher;
        this.type = "Семинар";
        this.begin_time = begin_time;
        this.end_time = end_time;
        this.classroom = classroom;
        this.date = date;
        this.group_number = group_number;
        this.record_id = record_id;
    };
}

class Exam extends Lesson {
    Exam(String name, String teacher,
        String begin_time, String end_time, String classroom, String date, String group_number, String record_id) {
        this.name = name;
        this.teacher = teacher;
        this.type = "Экзамен";
        this.begin_time = begin_time;
        this.end_time = end_time;
        this.classroom = classroom;
        this.date = date;
        this.group_number = group_number;
        this.record_id = record_id;
    };
}

class Cond extends Lesson {
    Cond(String name, String teacher,
        String begin_time, String end_time, String classroom, String date, String group_number, String record_id) {
        this.name = name;
        this.teacher = teacher;
        this.type = "Зачет";
        this.begin_time = begin_time;
        this.end_time = end_time;
        this.classroom = classroom;
        this.date = date;
        this.group_number = group_number;
        this.record_id = record_id;
    };
}

class Cons extends Lesson {
    Cons(String name, String teacher,
        String begin_time, String end_time, String classroom, String date, String group_number, String record_id) {
        this.name = name;
        this.teacher = teacher;
        this.type = "Консультация";
        this.begin_time = begin_time;
        this.end_time = end_time;
        this.classroom = classroom;
        this.date = date;
        this.group_number = group_number;
        this.record_id = record_id;
    };
}


