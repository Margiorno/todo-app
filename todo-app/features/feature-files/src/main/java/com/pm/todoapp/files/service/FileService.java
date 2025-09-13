package com.pm.todoapp.files.service;

import com.pm.todoapp.common.exceptions.InvalidFileTypeException;
import com.pm.todoapp.common.exceptions.StorageException;
import com.pm.todoapp.common.exceptions.StorageFileNotFoundException;
import com.pm.todoapp.domain.file.dto.FileType;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileService {

    private final Path rootLocation;

    public FileService(@Value("${storage.location}") Path location) {
        this.rootLocation = Paths.get(location.toUri());
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    public String saveFile(MultipartFile file, FileType fileType) {
        try {
            return saveFileInternal(file.getInputStream(), file.getOriginalFilename(), fileType);
        } catch (IOException e) {
            throw new StorageException("Failed to read file for saving: " + e.getMessage());
        }
    }

    public String saveFile(InputStream fileContent, String originalFilename, FileType fileType) {
        return saveFileInternal(fileContent, originalFilename, fileType);
    }

    private String saveFileInternal(InputStream originalInputStream, String originalFilename, FileType fileType) {
        try (InputStream inputStream = new BufferedInputStream(originalInputStream)) {
            inputStream.mark(Integer.MAX_VALUE);

            Tika tika = new Tika();
            String actualMimeType = tika.detect(inputStream);
            inputStream.reset();

            if (!actualMimeType.startsWith(fileType.getType())) {
                throw new InvalidFileTypeException(
                        "Invalid file type. Expected " + fileType.getType() + " but was " + actualMimeType
                );
            }

            String cleanFilename = StringUtils.cleanPath(Objects.requireNonNull(originalFilename));
            String extension = cleanFilename.contains(".")
                    ? cleanFilename.substring(cleanFilename.lastIndexOf("."))
                    : "";
            String uniqueFilename = UUID.randomUUID().toString() + extension;

            Path targetDirectory = this.rootLocation.resolve(fileType.getPath()).normalize();
            Files.createDirectories(targetDirectory);
            Path destinationFile = targetDirectory.resolve(uniqueFilename);

            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);

            return uniqueFilename;

        } catch (IOException e) {
            throw new StorageException("Failed to store file: " + e.getMessage());
        }
    }

    public Resource loadFileAsResource(String filename, FileType fileType) {
        try {
            Path file = rootLocation.resolve(fileType.getPath()).resolve(filename).normalize();

            if (!file.startsWith(rootLocation.resolve(fileType.getPath()))) {
                throw new StorageFileNotFoundException("Invalid file path: " + filename);
            }

            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable())
                return resource;

            if (fileType == FileType.PROFILE_PICTURE) {
                return loadDefaultProfilePicture();
            }

            throw new StorageFileNotFoundException("Could not read file: " + filename);

        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename);
        }
    }

    public void deleteFile(String filename, FileType fileType) {
        try {
            Path file = rootLocation.resolve(fileType.getPath()).resolve(filename).normalize();
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new StorageException("Failed to delete file: " + filename);
        }
    }

    private Resource loadDefaultProfilePicture() throws MalformedURLException {
        Path defaultFile = rootLocation.resolve(FileType.PROFILE_PICTURE.getPath()).resolve("default.jpg").normalize();
        Resource defaultResource = new UrlResource(defaultFile.toUri());

        if (defaultResource.exists() && defaultResource.isReadable()) {
            return defaultResource;
        }

        throw new StorageFileNotFoundException("Could not read profile picture");
    }
}
