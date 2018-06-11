package com.example.schedulemai.lesson;

import org.jetbrains.annotations.NotNull;

/**
 * Created by IO.Novikov on 01.04.2018.
 */

public enum LessonType {
    LAB("ЛК", "Лекция"),
    SEMINAR("ПЗ", "Практическое занятие"),
    LECTURE("ЛР", "Лабораторная"),
    EXAM("Экзамен", "Экзамен"),
    CONDITION("Зачет", "Зачет"),
    UNKNOWN("Занятие", "Занятие");

    private final String type;
    private final String name;

    LessonType(final String type, final String name) {
        this.type = type;
        this.name = name;
    }

    @NotNull
    public static LessonType fromString(String text) {
        for (LessonType lessonType : LessonType.values()) {
            if (lessonType.getType().equals(text) || lessonType.getName().equals(text)) {
                return lessonType;
            }
        }
        return UNKNOWN;
    }

    public String getType() {
        return type;

    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "LessonType{" +
                "type='" + type + '\'' +
                '}';
    }
}
