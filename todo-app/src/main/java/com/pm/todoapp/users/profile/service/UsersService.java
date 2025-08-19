package com.pm.todoapp.users.profile.service;

import com.pm.todoapp.users.profile.dto.ProfileStatus;
import com.pm.todoapp.users.profile.model.FriendRequest;
import com.pm.todoapp.users.profile.model.Gender;
import com.pm.todoapp.users.profile.model.User;
import com.pm.todoapp.users.social.dto.FriendRequestDTO;
import com.pm.todoapp.notifications.dto.NotificationDTO;
import com.pm.todoapp.users.profile.dto.ProfileStatusDTO;
import com.pm.todoapp.users.profile.dto.UserResponseDTO;
import com.pm.todoapp.core.exceptions.InvalidFieldException;
import com.pm.todoapp.core.exceptions.InvalidFriendInviteException;
import com.pm.todoapp.core.exceptions.UnauthorizedException;
import com.pm.todoapp.core.exceptions.UserNotFoundException;
import com.pm.todoapp.file.service.FileService;
import com.pm.todoapp.file.dto.FileType;
import com.pm.todoapp.users.social.mapper.FriendRequestMapper;
import com.pm.todoapp.users.profile.mapper.UserMapper;
import com.pm.todoapp.notifications.model.NotificationType;
import com.pm.todoapp.notifications.service.NotificationService;
import com.pm.todoapp.users.social.repository.FriendsRequestRepository;
import com.pm.todoapp.users.profile.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// TODO DIVIDE TO PROFILE AND SOCIAL(FRIEND REQUESTS)

@Service
public class UsersService {

    private final UsersRepository usersRepository;
    private final FileService fileService;
    private final FriendsRequestRepository friendsRequestRepository;
    private final NotificationService notificationService;

    @Autowired
    public UsersService(UsersRepository usersRepository, FileService fileService, FriendsRequestRepository friendsRequestRepository, @Lazy NotificationService notificationService) {
        this.usersRepository = usersRepository;
        this.fileService = fileService;
        this.friendsRequestRepository = friendsRequestRepository;
        this.notificationService = notificationService;
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
                .sentAt(LocalDateTime.now())
                .build();

        return FriendRequestMapper.toDTO(friendsRequestRepository.save(friendRequest));
    }

    public boolean areFriends(UUID userId, UUID profileId) {
        findRawById(userId);
        findRawById(profileId);

        return usersRepository.existsByIdAndFriendsId(userId,profileId);
    }

    public ProfileStatusDTO determineFriendshipStatus(UUID userId, UUID profileId) {

        if (userId.equals(profileId))
            return new ProfileStatusDTO(ProfileStatus.OWNER, null);
        if (areFriends(userId, profileId))
            return new ProfileStatusDTO(ProfileStatus.FRIEND, null);

        User user = findRawById(userId);
        User profile = findRawById(profileId);

        if (friendsRequestRepository.existsBySenderAndReceiver(user, profile))

            return new ProfileStatusDTO(
                    ProfileStatus.INVITATION_SENT,
                    findRawRequestBySenderAndReceiver(userId, profileId).getId());
        if (friendsRequestRepository.existsBySenderAndReceiver(profile, user))
            return new ProfileStatusDTO(
                    ProfileStatus.INVITATION_RECEIVED,
                    findRawRequestBySenderAndReceiver(profileId, userId).getId());
        else
            return new ProfileStatusDTO(ProfileStatus.NOT_FRIENDS, null);
    }

    @Transactional
    public void acceptFriendRequest(UUID requestId, UUID currentUserId) {

        FriendRequest request = findRawFriendRequest(requestId);
        resolveFriendRequest(requestId);

        if (!request.getReceiver().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You must be the receiver to accept a friend request");
        }

        friendsRequestRepository.delete(request);

        User currentUser = findRawById(currentUserId);
        User sender = request.getSender();
        currentUser.addFriend(sender);
        usersRepository.save(sender);
        usersRepository.save(currentUser);

        String notificationMessage = currentUser.getFirstName() + " " + currentUser.getLastName() + " accepted your friend request.";
        NotificationDTO notification = notificationService.createNotification(
                sender,
                currentUser,
                NotificationType.FRIEND_REQUEST_ACCEPTED,
                notificationMessage
        );

        notificationService.sendNotification(notification, sender.getId());
    }

    @Transactional
    public void declineFriendRequest(UUID requestId, UUID currentUserId) {
        FriendRequest request = findRawFriendRequest(requestId);
        resolveFriendRequest(requestId);
        if (!request.getReceiver().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You must be the receiver to accept a friend request");
        }
        friendsRequestRepository.delete(request);
    }

    @Transactional
    public void cancelFriendRequest(UUID requestId, UUID currentUserId) {
        FriendRequest request = findRawFriendRequest(requestId);
        deleteFriendRequest(requestId);
        if (!request.getSender().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You must be the sender to cancel a friend request");
        }
        friendsRequestRepository.delete(request);
    }

    private FriendRequest findRawFriendRequest(UUID requestId) {
        return friendsRequestRepository.findById(requestId).orElseThrow(
                () -> new InvalidFriendInviteException("Friend request not found")
        );
    }

    public FriendRequest findRawRequestBySenderAndReceiver(UUID senderId, UUID receiverId) {
        User sender = findRawById(senderId);
        User receiver = findRawById(receiverId);

        return friendsRequestRepository.findBySenderAndReceiver(sender, receiver).orElse(null);
    }

    public void remove(UUID currentUserId, UUID userId) {
        User currentUser = findRawById(currentUserId);
        User unfriend = findRawById(userId);

        if (!areFriends(currentUserId, userId))
            throw new InvalidFriendInviteException("You cannot remove this user from friends, because you are not friends");

        currentUser.removeFriend(unfriend);
        usersRepository.save(currentUser);
        usersRepository.save(unfriend);
    }

    private void resolveFriendRequest(UUID requestId) {
        notificationService.resolveNotification(requestId);
    }

    private void deleteFriendRequest(UUID requestId) {
        notificationService.deleteNotification(requestId);
    }

    public List<UserResponseDTO> getFriends(UUID userId) {
        User user = findRawById(userId);

        return user.getFriends().stream().map(UserMapper::toUserResponseDTO).toList();
    }
}
