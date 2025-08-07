package com.pm.todoapp.file;

import lombok.*;

@Getter
public enum FileType {
    PROFILE_PICTURE("image/", "profile_picture");

    private final String type;
    private final String path;

    FileType(String type, String path) {
        this.type = type;
        this.path = path;
    }

}
