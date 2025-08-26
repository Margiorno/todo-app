package com.pm.todoapp.domain.file.port;


import com.pm.todoapp.domain.file.dto.FileType;

public interface FileStoragePort {
    String saveFile(MultipartFile file, FileType fileType);
    void deleteFile(String filePath, FileType fileType);
}
