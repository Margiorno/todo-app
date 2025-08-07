package com.pm.todoapp.file;

import com.pm.todoapp.exceptions.InvalidFileTypeException;
import com.pm.todoapp.exceptions.StorageException;
import com.pm.todoapp.exceptions.StorageFileNotFoundException;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;

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
        try (InputStream inputStream = file.getInputStream()) {
            Tika tika = new Tika();

            String actualMimeType = tika.detect(inputStream);

            if (!actualMimeType.startsWith(fileType.getType())) {
                throw new InvalidFileTypeException("Invalid file type. Expected " + fileType.getType() + " but was " + actualMimeType);
            }

            String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFilename = UUID.randomUUID().toString() + extension;



            System.out.println("Root file storage location: " + rootLocation.toAbsolutePath());

            Path targetDirectory = this.rootLocation.resolve(fileType.getPath());
            Files.createDirectories(targetDirectory);

            Path destinationFile = targetDirectory.resolve(uniqueFilename).normalize().toAbsolutePath();

            System.out.println("Saving file to: " + destinationFile);

            try (InputStream newInputStream = file.getInputStream()) {
                Files.copy(newInputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            return uniqueFilename;

        } catch (IOException e) {
            throw new StorageException("Failed to store file: %s".formatted(e.getMessage()));
        }
    }

    public Resource loadFileAsResource(String filename, FileType fileType) {
        try {
            Path file = rootLocation.resolve(fileType.getPath()).resolve(filename).normalize();
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException("Could not read file: " + filename);
            }
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
}
