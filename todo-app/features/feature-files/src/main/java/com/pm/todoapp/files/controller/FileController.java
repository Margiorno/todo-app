package com.pm.todoapp.files.controller;


import com.pm.todoapp.domain.file.dto.FileType;
import com.pm.todoapp.files.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "File API", description = "Endpoints for retrieving stored files")
public class FileController {

    private final FileService fileService;

    @Operation(
            summary = "Get a profile picture by filename",
            description = "Retrieves a profile picture by its unique, system-generated filename and serves it as a resource. " +
                    "The response is configured with a 'Content-Disposition: inline' header to be displayed directly by the browser. " +
                    "If the requested file is not found, a default placeholder profile picture is served instead.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved the image file (either the requested one or the default)."),
                    @ApiResponse(responseCode = "404", description = "Not Found if both the requested file and the default placeholder image are missing.")
            }
    )
    @GetMapping("/profile-pictures/{filename}")
    public ResponseEntity<Resource> loadProfilePicture(@PathVariable String filename) {

        Resource file = fileService.loadFileAsResource(filename, FileType.PROFILE_PICTURE);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("image/png"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; ...")
                .body(file);
    }
}
