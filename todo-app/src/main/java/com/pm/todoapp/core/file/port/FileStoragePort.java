package com.pm.todoapp.core.file.port;

import com.pm.todoapp.core.file.dto.FileType;
import org.springframework.web.multipart.MultipartFile;

public interface FileStoragePort {
    String saveFile(MultipartFile file, FileType fileType);
    void deleteFile(String filePath, FileType fileType);
}
