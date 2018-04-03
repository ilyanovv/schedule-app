package com.example.schedulemai.lesson;

import org.jetbrains.annotations.NotNull;

/**
 * Created by IO.Novikov on 01.04.2018.
 */

public enum LessonType {
    LAB("ЛК"),
    SEMINAR("ПЗ"),
    LECTURE("ЛР"),
    UNKNOWN("unknown");

    private final String type;

    LessonType(final String type) {
        this.type = type;
    }

    @NotNull
    public static LessonType fromString(String text) {
        for (LessonType lessonType : LessonType.values()) {
            if (lessonType.getType().equals(text)) {
                return lessonType;
            }
        }
        return UNKNOWN;
    }

    public String getType() {
        return type;

    }


    @Override
    public String toString() {
        return "LessonType{" +
                "type='" + type + '\'' +
                '}';
    }
}
