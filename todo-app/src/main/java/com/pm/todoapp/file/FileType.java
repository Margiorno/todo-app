package com.pm.todoapp.file;

import lombok.*;

public enum FileType {
    PROFILE_PICTURE("image/", "/profile_picture");

    private final String type;
    private final String path;

    FileType(String type, String path) {
        this.type = type;
        this.path = path;
    }

    public String getType() {
        return type;
    }

    public String getPath() {
        return path;
    }
}
