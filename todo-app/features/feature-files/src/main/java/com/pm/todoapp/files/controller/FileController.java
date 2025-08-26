package com.pm.todoapp.files.controller;

import com.pm.todoapp.file.service.FileService;
import com.pm.todoapp.core.file.dto.FileType;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;

    @GetMapping("/profile-pictures/{filename}")
    public ResponseEntity<Resource> loadProfilePicture(@PathVariable String filename) {

        Resource file = fileService.loadFileAsResource(filename, FileType.PROFILE_PICTURE);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("image/png"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; ...")
                .body(file);
    }
}
