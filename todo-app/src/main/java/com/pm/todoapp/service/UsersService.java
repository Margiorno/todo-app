package com.pm.todoapp.service;

import com.pm.todoapp.dto.UserResponseDTO;
import com.pm.todoapp.exceptions.UserNotFoundException;
import com.pm.todoapp.file.FileService;
import com.pm.todoapp.file.FileType;
import com.pm.todoapp.mapper.UserMapper;
import com.pm.todoapp.model.User;
import com.pm.todoapp.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class UsersService {

    private final UsersRepository usersRepository;
    private final FileService fileService;

    @Autowired
    public UsersService(UsersRepository usersRepository, FileService fileService) {
        this.usersRepository = usersRepository;
        this.fileService = fileService;
    }

    public User findRawById(UUID userId) {
        return usersRepository.findById(userId).orElseThrow(
                ()->new UserNotFoundException("User with this id does not exist: " + userId.toString())
        );
    }

    public UserResponseDTO findById(UUID userId) {
        return UserMapper.toUserResponseDTO(findRawById(userId));
    }

    public User save(User user) {
        return usersRepository.save(user);
    }

    public User findByEmail(String email) {
        return usersRepository.findByEmail(email).orElseThrow(
                ()-> new UserNotFoundException("User with this email does not exist: " + email)
        );
    }

    public boolean existByEmail(String email) {
        return usersRepository.existsByEmail(email);
    }

    @Transactional
    public String updateProfilePicture(MultipartFile file, UUID userId) {

        User user = findRawById(userId);
        String oldPicturePath = user.getProfilePicturePath();

        String newPicturePath = fileService.saveFile(file, FileType.PROFILE_PICTURE);

        user.setProfilePicturePath(newPicturePath);
        usersRepository.save(user);

        if (oldPicturePath != null && !oldPicturePath.isEmpty() && !oldPicturePath.isBlank()) {
            fileService.deleteFile(oldPicturePath, FileType.PROFILE_PICTURE);
        }

        return newPicturePath;
    }
}
