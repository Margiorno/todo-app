package com.pm.todoapp.domain.file.port;


import com.pm.todoapp.domain.file.dto.FileType;

import java.io.InputStream;

public interface FileStoragePort {
    String saveFile(InputStream fileContent, String originalFilename, String contentType, FileType fileType);
    void deleteFile(String filePath, FileType fileType);
}
