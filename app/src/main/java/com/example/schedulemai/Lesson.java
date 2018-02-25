package com.example.schedulemai;

/**
 * Created by Илья on 13.02.2016.
 */
abstract public class Lesson {
    public String name;
    public String teacher;
    public String type;
    public String begin_time;
    public String end_time;
    public String classroom;
    public String date;
    public String group_number;

    public static Lesson CreateNewLesson(String type, String lesson_name, String teacher_name, String begin_time,
                                         String end_time, String classroom, String date, String group_number)
    {
        switch (type){
            case "Лабораторная":
                return new Lab(lesson_name, teacher_name, begin_time, end_time, classroom, date, group_number);
            case "Семинар":
                return new Sem(lesson_name, teacher_name, begin_time, end_time, classroom, date, group_number);
            case "Лекция":
                return new Lect(lesson_name, teacher_name, begin_time, end_time, classroom, date, group_number);
            default:
                return new Lab(lesson_name, teacher_name, begin_time, end_time, classroom, date, group_number);
        }
    }
}

class Lab extends Lesson {
    Lab(String name, String teacher,
        String begin_time, String end_time, String classroom, String date, String group_number) {
        this.name = name;
        this.teacher = teacher;
        this.type = "Лабораторная";
        this.begin_time = begin_time;
        this.end_time = end_time;
        this.classroom = classroom;
        this.date = date;
        this.group_number = group_number;
    };
}

class Lect extends Lesson {
    Lect(String name, String teacher,
        String begin_time, String end_time, String classroom, String date, String group_number) {
        this.name = name;
        this.teacher = teacher;
        this.type = "Лекция";
        this.begin_time = begin_time;
        this.end_time = end_time;
        this.classroom = classroom;
        this.date = date;
        this.group_number = group_number;
    };
}

class Sem extends Lesson {
    Sem(String name, String teacher,
        String begin_time, String end_time, String classroom, String date, String group_number) {
        this.name = name;
        this.teacher = teacher;
        this.type = "Семинар";
        this.begin_time = begin_time;
        this.end_time = end_time;
        this.classroom = classroom;
        this.date = date;
        this.group_number = group_number;
    };
}

