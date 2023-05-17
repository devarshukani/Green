package com.devarshukani.green_user.model;

public class AwarenessModel {
    private String title;
    private String description;

    public AwarenessModel(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}

