package com.pm.todoapp.files.adapter;


import com.pm.todoapp.domain.file.dto.FileType;
import com.pm.todoapp.domain.file.port.FileStoragePort;
import com.pm.todoapp.files.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@RequiredArgsConstructor
@Component
public class FileStorageAdapter implements FileStoragePort {
    private final FileService fileService;

    @Override
    public String saveFile(InputStream fileContent, String originalFilename, String contentType, FileType fileType) {
        return fileService.saveFile(fileContent, originalFilename, contentType, fileType);
    }

    @Override
    public void deleteFile(String filePath, FileType fileType) {
        fileService.deleteFile(filePath, fileType);
    }
}
