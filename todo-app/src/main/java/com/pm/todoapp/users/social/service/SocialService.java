package com.pm.todoapp.users.social.service;

import com.pm.todoapp.core.exceptions.InvalidFriendInviteException;
import com.pm.todoapp.core.exceptions.UnauthorizedException;
import com.pm.todoapp.core.user.event.FriendRequestAcceptedEvent;
import com.pm.todoapp.core.user.event.FriendRequestResolvedEvent;
import com.pm.todoapp.core.user.event.FriendRequestSentEvent;
import com.pm.todoapp.users.profile.dto.ProfileStatus;
import com.pm.todoapp.users.profile.dto.UserResponseDTO;
import com.pm.todoapp.users.profile.mapper.UserMapper;
import com.pm.todoapp.users.social.dto.ProfileStatusDTO;
import com.pm.todoapp.users.profile.model.User;
import com.pm.todoapp.users.profile.repository.UsersRepository;
import com.pm.todoapp.users.profile.service.UsersService;
import com.pm.todoapp.users.social.model.FriendRequest;
import com.pm.todoapp.users.social.repository.FriendsRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

// TODO DIVIDE TO PROFILE AND SOCIAL(FRIEND REQUESTS)

@Service
@RequiredArgsConstructor
public class SocialService {

    private final UsersRepository usersRepository;
    private final FriendsRequestRepository friendsRequestRepository;
    private final UsersService usersService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void newFriendRequest(UUID senderId, UUID receiverId) {

        if (usersRepository.areFriends(senderId, receiverId))
            throw new InvalidFriendInviteException("Users are already friends");

        User sender = usersService.findRawById(senderId);
        User receiver = usersService.findRawById(receiverId);

        FriendRequest request = FriendRequest.builder().sender(sender).receiver(receiver).build();
        FriendRequest saved = friendsRequestRepository.save(request);

        eventPublisher.publishEvent(new FriendRequestSentEvent(
                saved.getId(), senderId, receiverId
        ));
    }

    @Transactional
    public void acceptFriendRequest(UUID requestId, UUID currentUserId) {

        FriendRequest request = findRawFriendRequest(requestId);

        if (!request.getReceiver().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You must be the receiver to accept a friend request");
        }

        User currentUser = usersService.findRawById(currentUserId);
        User sender = request.getSender();
        currentUser.addFriend(sender);
        usersRepository.save(sender);
        usersRepository.save(currentUser);

        friendsRequestRepository.delete(request);

        eventPublisher.publishEvent(
                new FriendRequestAcceptedEvent(currentUserId, sender.getId())
        );

        eventPublisher.publishEvent(
                new FriendRequestResolvedEvent(requestId)
        );
    }

    @Transactional
    public void declineFriendRequest(UUID requestId, UUID currentUserId) {
        FriendRequest request = findRawFriendRequest(requestId);
        if (!request.getReceiver().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You must be the receiver to accept a friend request");
        }
        friendsRequestRepository.delete(request);
        eventPublisher.publishEvent(
                new FriendRequestResolvedEvent(requestId)
        );
    }

    @Transactional
    public void cancelFriendRequest(UUID requestId, UUID currentUserId) {
        FriendRequest request = findRawFriendRequest(requestId);
        if (!request.getSender().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You must be the sender to cancel a friend request");
        }
        friendsRequestRepository.delete(request);
        eventPublisher.publishEvent(
                new FriendRequestResolvedEvent(requestId)
        );
    }

    public ProfileStatusDTO determineFriendshipStatus(UUID userId, UUID profileId) {

        if (userId.equals(profileId))
            return new ProfileStatusDTO(ProfileStatus.OWNER, null);
        if (areFriends(userId, profileId))
            return new ProfileStatusDTO(ProfileStatus.FRIEND, null);

        User user = usersService.findRawById(userId);
        User profile = usersService.findRawById(profileId);

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

    private FriendRequest findRawFriendRequest(UUID requestId) {
        return friendsRequestRepository.findById(requestId).orElseThrow(
                () -> new InvalidFriendInviteException("Friend request not found")
        );
    }

    public FriendRequest findRawRequestBySenderAndReceiver(UUID senderId, UUID receiverId) {
        User sender = usersService.findRawById(senderId);
        User receiver = usersService.findRawById(receiverId);

        return friendsRequestRepository.findBySenderAndReceiver(sender, receiver).orElse(null);
    }

    public boolean areFriends(UUID userId, UUID profileId) {
        usersService.ensureUserExistsById(userId);
        usersService.ensureUserExistsById(profileId);

        return usersRepository.existsByIdAndFriendsId(userId,profileId);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getFriends(UUID userId) {
        User user = usersService.findRawById(userId);

        return user.getFriends().stream().map(UserMapper::toUserResponseDTO).toList();
    }

    public void removeFriend(UUID currentUserId, UUID userId) {
        User currentUser = usersService.findRawById(currentUserId);
        User unfriend = usersService.findRawById(userId);

        if (!usersRepository.areFriends(currentUserId, userId))
            throw new InvalidFriendInviteException("You cannot remove this user from friends, because you are not friends");

        currentUser.removeFriend(unfriend);
        usersRepository.save(currentUser);
        usersRepository.save(unfriend);
    }

}
