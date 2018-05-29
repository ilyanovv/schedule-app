package com.example.schedulemai.localdb;

/**
 * Created by IO.Novikov on 29.05.2018.
 */

public enum Tables {
    TEACHER("teacher", "teachers"),
    SCHEDULE("schedule", "url");

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