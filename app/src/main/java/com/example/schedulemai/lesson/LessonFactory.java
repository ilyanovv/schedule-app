package com.example.schedulemai.lesson;



/**
 * Created by IO.Novikov on 01.04.2018.
 */

public class LessonFactory {
    public static Lesson createLesson(
            String recordId,
            String name,
            String teacher,
            String lessonType,
            String timeBegin,
            String timeEnd,
            String classroom,
            String lessonDate,
            String groupNumber) {
        Lesson lesson;
        LessonType type = LessonType.fromString(lessonType);
        switch (type){
            case LAB :
                lesson = new Lab(recordId, name, teacher, type.getName(), timeBegin, timeEnd,
                        classroom, lessonDate, groupNumber);
                break;
            case LECTURE:
                lesson = new Lecture(recordId, name, teacher, type.getName(), timeBegin, timeEnd,
                        classroom, lessonDate, groupNumber);
                break;
            case SEMINAR:
                lesson = new Seminar(recordId, name, teacher, type.getName(), timeBegin, timeEnd,
                        classroom, lessonDate, groupNumber);
                break;
            default:
                lesson = new UnknownLesson(recordId, name, teacher, type.getName(), timeBegin, timeEnd,
                        classroom, lessonDate, groupNumber);
                break;
        }
        return lesson;
    }

}
