package com.pm.todoapp.users.social.controller;

import com.pm.todoapp.domain.user.model.Gender;
import com.pm.todoapp.users.profile.dto.UserResponseDTO;
import com.pm.todoapp.users.social.dto.ProfileStatusDTO;
import com.pm.todoapp.users.social.service.SocialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/social")
public class SocialController {
    private final SocialService socialService;

    @PostMapping("/{receiverId}/invite")
    public ResponseEntity<Void> sendFriendInvitation(
            @PathVariable UUID receiverId,
            @AuthenticationPrincipal UUID senderId
    ){
        socialService.newFriendRequest(senderId, receiverId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{requestId}/accept")
    public ResponseEntity<Void> acceptFriendRequest(
            @PathVariable UUID requestId,
            @AuthenticationPrincipal UUID currentUserId
    ){
        socialService.acceptFriendRequest(requestId, currentUserId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{requestId}/decline")
    public ResponseEntity<Void> declineFriendRequest(
            @PathVariable UUID requestId,
            @AuthenticationPrincipal UUID currentUserId
    ){
        socialService.declineFriendRequest(requestId, currentUserId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{requestId}/cancel")
    public ResponseEntity<Void> cancelFriendRequest(
            @PathVariable UUID requestId,
            @AuthenticationPrincipal UUID currentUserId
    ){
        socialService.cancelFriendRequest(requestId, currentUserId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/remove")
    public ResponseEntity<Void> removeFriend(
            @AuthenticationPrincipal UUID currentUserId,
            @PathVariable UUID userId
    ){
        socialService.removeFriend(currentUserId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/friends")
    public ResponseEntity<List<UserResponseDTO>> getFriendRequests(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(socialService.getFriends(userId));
    }

    @PostMapping("/{userId}/status")
    public ResponseEntity<ProfileStatusDTO> getFriendStatus(
            @AuthenticationPrincipal UUID currentUserId,
            @PathVariable UUID userId
    ){

        return ResponseEntity.ok(socialService.determineFriendshipStatus(currentUserId, userId));
    }

    @GetMapping("/genders")
    public ResponseEntity<List<String>> getGenders() {
        List<String> genders = Arrays.stream(Gender.values())
                .map(Enum::name)
                .toList();
        return ResponseEntity.ok(genders);
    }
}
