package com.pm.todoapp.users.social.controller;

import com.pm.todoapp.users.profile.service.UsersService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/friend-requests")
public class FriendRequestController {
    private final UsersService usersService;

    public FriendRequestController(UsersService usersService) {
        this.usersService = usersService;
    }

    @PostMapping("/{requestId}/accept")
    public ResponseEntity<Void> acceptFriendRequest(
            @PathVariable UUID requestId,
            @AuthenticationPrincipal UUID currentUserId) {

        usersService.acceptFriendRequest(requestId, currentUserId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{requestId}/decline")
    public ResponseEntity<Void> declineFriendRequest(
            @PathVariable UUID requestId,
            @AuthenticationPrincipal UUID currentUserId) {

        usersService.declineFriendRequest(requestId, currentUserId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{requestId}/cancel")
    public ResponseEntity<Void> cancelFriendRequest(
            @PathVariable UUID requestId,
            @AuthenticationPrincipal UUID currentUserId) {

        usersService.cancelFriendRequest(requestId, currentUserId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/remove")
    public ResponseEntity<Void> removeFriendRequest(
            @AuthenticationPrincipal UUID currentUserId,
            @PathVariable UUID userId
    ){

        usersService.remove(currentUserId, userId);
        return ResponseEntity.ok().build();
    }
}
