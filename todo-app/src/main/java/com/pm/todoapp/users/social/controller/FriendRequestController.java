package com.pm.todoapp.users.social.controller;

import com.pm.todoapp.notifications.dto.NotificationDTO;
import com.pm.todoapp.users.profile.service.UsersService;
import com.pm.todoapp.users.social.dto.FriendRequestDTO;
import com.pm.todoapp.users.social.service.FriendRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/friend-requests")
public class FriendRequestController {
    private final FriendRequestService friendRequestService;
    private final UsersService usersService;

    @PostMapping("/{receiverId}/invite")
    public ResponseEntity<Void> sendFriendInvitation(
            @PathVariable UUID receiverId,
            @AuthenticationPrincipal UUID senderId) {

        friendRequestService.newFriendRequest(senderId, receiverId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{requestId}/accept")
    public ResponseEntity<Void> acceptFriendRequest(
            @PathVariable UUID requestId,
            @AuthenticationPrincipal UUID currentUserId) {

        friendRequestService.acceptFriendRequest(requestId, currentUserId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{requestId}/decline")
    public ResponseEntity<Void> declineFriendRequest(
            @PathVariable UUID requestId,
            @AuthenticationPrincipal UUID currentUserId) {

        friendRequestService.declineFriendRequest(requestId, currentUserId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{requestId}/cancel")
    public ResponseEntity<Void> cancelFriendRequest(
            @PathVariable UUID requestId,
            @AuthenticationPrincipal UUID currentUserId) {

        friendRequestService.cancelFriendRequest(requestId, currentUserId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/remove")
    public ResponseEntity<Void> removeFriend(
            @AuthenticationPrincipal UUID currentUserId,
            @PathVariable UUID userId
    ){

        usersService.removeFriend(currentUserId, userId);
        return ResponseEntity.ok().build();
    }
}
