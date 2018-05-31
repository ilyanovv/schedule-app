package com.example.schedulemai.localdb;

/**
 * Created by IO.Novikov on 29.05.2018.
 */

public enum Tables {
    TEACHER("teacher", "teachers"),
    GROUP("group_tab", "get_all_groups"),
    LESSON("lesson", "lessons"),
    LESSON_ROOM("lesson_room", "lesson_rooms"),
    LESSON_TYPE("lesson_type", "lesson_types");


    private final String tableName;
    private final String url;

    Tables(String tableName, String url) {
        this.tableName = tableName;
        this.url = url;
    }

    public String getTableName() {
        return tableName;
    }

    public String getUrl() {
        return url;
    }
}
