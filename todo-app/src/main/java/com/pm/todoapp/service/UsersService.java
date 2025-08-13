package com.pm.todoapp.service;

import com.pm.todoapp.dto.FriendRequestDTO;
import com.pm.todoapp.dto.UserResponseDTO;
import com.pm.todoapp.exceptions.InvalidFieldException;
import com.pm.todoapp.exceptions.InvalidFriendInviteException;
import com.pm.todoapp.exceptions.UserNotFoundException;
import com.pm.todoapp.file.FileService;
import com.pm.todoapp.file.FileType;
import com.pm.todoapp.mapper.FriendRequestMapper;
import com.pm.todoapp.mapper.UserMapper;
import com.pm.todoapp.model.FriendRequest;
import com.pm.todoapp.model.FriendRequestStatus;
import com.pm.todoapp.model.Gender;
import com.pm.todoapp.model.User;
import com.pm.todoapp.repository.FriendsRequestRepository;
import com.pm.todoapp.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Service
public class UsersService {

    private final UsersRepository usersRepository;
    private final FileService fileService;
    private final FriendsRequestRepository friendsRequestRepository;

    @Autowired
    public UsersService(UsersRepository usersRepository, FileService fileService, FriendsRequestRepository friendsRequestRepository) {
        this.usersRepository = usersRepository;
        this.fileService = fileService;
        this.friendsRequestRepository = friendsRequestRepository;
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

    public Map<String, Object> update(String field, String value, UUID userId) {
        User user = findRawById(userId);

        Object updatedValue;

        switch (field) {
            case "firstName" -> updatedValue = updateFirstName(user, value);
            case "lastName" -> updatedValue = updateLastName(user, value);
            case "dateOfBirth" -> updatedValue = updateDateOfBirth(user, LocalDate.parse(value));
            case "gender" -> updatedValue = updateGender(user, Gender.valueOf(value));
            case "email" -> updatedValue = updateEmail(user, value);
            default -> throw new InvalidFieldException("Unsupported field: " + field);
        }

        return Collections.singletonMap(field, updatedValue);
    }


    private String updateEmail(User user, String value) {
        user.setEmail(value);
        return usersRepository.save(user).getEmail();
    }

    private String updateFirstName(User user, String value) {
        user.setFirstName(value);
        return usersRepository.save(user).getFirstName();
    }
    private String updateLastName(User user, String value) {
        user.setLastName(value);
        return usersRepository.save(user).getLastName();
    }
    private LocalDate updateDateOfBirth(User user, LocalDate parse) {
        user.setDateOfBirth(parse);
        return usersRepository.save(user).getDateOfBirth();
    }
    private Gender updateGender(User user, Gender gender) {
        user.setGender(gender);
        return usersRepository.save(user).getGender();
    }

    public FriendRequestDTO saveFriendRequest(UUID senderId, UUID receiverId) {

        if (usersRepository.existsByIdAndFriendsId(senderId,receiverId))
            throw new InvalidFriendInviteException("Users are already friends");

        User sender = findRawById(senderId);
        User receiver = findRawById(receiverId);

        FriendRequest friendRequest = FriendRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .status(FriendRequestStatus.PENDING)
                .sentAt(LocalDateTime.now())
                .build();

        return FriendRequestMapper.toDTO(friendsRequestRepository.save(friendRequest));
    }

    public boolean areFriends(UUID userId, UUID profileId) {
        //Users validation
        findRawById(userId);
        findRawById(profileId);

        return usersRepository.existsByIdAndFriendsId(userId,profileId);
    }
}
