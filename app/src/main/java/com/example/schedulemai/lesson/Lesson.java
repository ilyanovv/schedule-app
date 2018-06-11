package com.example.schedulemai.lesson;

import android.graphics.Color;

import com.example.schedulemai.R;


/**
 * Created by Илья on 13.02.2016.
 */

abstract public class Lesson {
    private String recordId;
    private String name;
    private String teacher;
    private String lessonType;
    private String timeBegin;
    private String timeEnd;
    private String classroom;
    private String lessonDate;
    private String groupNumber;

    abstract public int getBackgroundStyle();

    public Lesson() {
    }

    public Lesson(String recordId, String name, String teacher, String lessonType, String timeBegin,
                  String timeEnd, String classroom, String lessonDate, String groupNumber) {
        this.recordId = recordId;
        this.name = name;
        this.teacher = teacher;
        this.lessonType = lessonType;
        this.timeBegin = timeBegin;
        this.timeEnd = timeEnd;
        this.classroom = classroom;
        this.lessonDate = lessonDate;
        this.groupNumber = groupNumber;
    }

    public static int getBackgroundStyle(String lessonType) {
        switch (lessonType){
            case "Лекция":
                return R.drawable.lecture_back_selector;
            case "Практическое занятие":
                return R.drawable.sem_back_selector;
            case "Лабораторная":
                return R.drawable.lab_back_selector;
            case "Экзамен":
                return R.drawable.exam_back_selector;
            case "Зачет":
                return R.drawable.condition_back_selector;
            case "Консультация":
                return R.drawable.cons_back_selector;
            case "unknown":
                return R.drawable.cons_back_selector;
            default:
                return R.drawable.cons_back_selector;
        }
    }




    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getLessonType() {
        return lessonType;
    }

    public void setLessonType(String lessonType) {
        this.lessonType = lessonType;
    }

    public String getTimeBegin() {
        return timeBegin;
    }

    public void setTimeBegin(String timeBegin) {
        this.timeBegin = timeBegin;
    }

    public String getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(String timeEnd) {
        this.timeEnd = timeEnd;
    }

    public String getClassroom() {
        return classroom;
    }

    public void setClassroom(String classroom) {
        this.classroom = classroom;
    }

    public String getLessonDate() {
        return lessonDate;
    }

    public void setLessonDate(String lessonDate) {
        this.lessonDate = lessonDate;
    }

    public String getGroupNumber() {
        return groupNumber;
    }

    public void setGroupNumber(String groupNumber) {
        this.groupNumber = groupNumber;
    }
}




