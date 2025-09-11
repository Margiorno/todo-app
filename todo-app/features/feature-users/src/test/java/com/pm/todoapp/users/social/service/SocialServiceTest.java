package com.pm.todoapp.users.social.service;

import com.pm.todoapp.common.exceptions.InvalidFriendInviteException;
import com.pm.todoapp.common.exceptions.UnauthorizedException;
import com.pm.todoapp.domain.user.event.FriendRequestAcceptedEvent;
import com.pm.todoapp.domain.user.event.FriendRequestResolvedEvent;
import com.pm.todoapp.domain.user.event.FriendRequestSentEvent;
import com.pm.todoapp.users.profile.model.User;
import com.pm.todoapp.users.profile.repository.UsersRepository;
import com.pm.todoapp.users.profile.service.UsersService;
import com.pm.todoapp.users.social.model.FriendRequest;
import com.pm.todoapp.users.social.repository.FriendsRequestRepository;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SocialServiceTest {

    @Mock
    private UsersRepository usersRepository;
    @Mock
    private FriendsRequestRepository friendsRequestRepository;
    @Mock
    private UsersService usersService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private SocialService socialService;

    private User sender;
    private User receiver;
    private FriendRequest friendRequest;

    @BeforeEach
    void setUp() {
        sender = Instancio.create(User.class);

        receiver = Instancio.create(User.class);

        friendRequest = Instancio.of(FriendRequest.class)
                .set(field(FriendRequest::getSender),sender)
                .set(field(FriendRequest::getReceiver),receiver)
                .create();
    }

    @Test
    void newFriendRequest_shouldSaveRequestAndPublishEvent_whenUsersAreNotFriends() {

        when(usersRepository.areFriends(sender.getId(), receiver.getId())).thenReturn(false);
        when(usersService.findRawById(sender.getId())).thenReturn(sender);
        when(usersService.findRawById(receiver.getId())).thenReturn(receiver);
        when(friendsRequestRepository.save(any(FriendRequest.class))).thenReturn(friendRequest);
        doNothing().when(eventPublisher).publishEvent(any(FriendRequestSentEvent.class));

        socialService.newFriendRequest(sender.getId(), receiver.getId());

        verify(friendsRequestRepository).save(any(FriendRequest.class));
        verify(eventPublisher).publishEvent(any(FriendRequestSentEvent.class));
    }

    @Test
    void newFriendRequest_shouldThrowException_whenUsersAreAlreadyFriends() {
        when(usersRepository.areFriends(sender.getId(), receiver.getId())).thenReturn(true);

        assertThatThrownBy(() -> socialService.newFriendRequest(sender.getId(), receiver.getId()))
                .isInstanceOf(InvalidFriendInviteException.class)
                .hasMessage("Users are already friends");

        verify(friendsRequestRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void acceptFriendRequest_shouldAddFriendAndDeleteRequest_whenUserIsReceiver() {
        when(friendsRequestRepository.findById(friendRequest.getId())).thenReturn(Optional.of(friendRequest));
        when(usersService.findRawById(receiver.getId())).thenReturn(receiver);

        socialService.acceptFriendRequest(friendRequest.getId(), receiver.getId());

        verify(usersRepository, times(2)).save(any(User.class));
        verify(friendsRequestRepository).delete(friendRequest);
        verify(eventPublisher).publishEvent(any(FriendRequestAcceptedEvent.class));
        verify(eventPublisher).publishEvent(any(FriendRequestResolvedEvent.class));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(usersRepository, atLeastOnce()).save(userCaptor.capture());
        assertThat(userCaptor.getAllValues()).anySatisfy(user ->
                assertThat(user.getFriends()).contains(sender)
        );
    }

    @Test
    void acceptFriendRequest_shouldThrowException_whenUserIsNotReceiver() {
        when(friendsRequestRepository.findById(friendRequest.getId())).thenReturn(Optional.of(friendRequest));

        assertThatThrownBy(() -> socialService.acceptFriendRequest(friendRequest.getId(), sender.getId()))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("You must be the receiver to accept a friend request");

        verify(usersRepository, never()).save(any());
        verify(friendsRequestRepository, never()).delete(any());
    }
}