package com.pm.todoapp.users.social.controller;

import com.pm.todoapp.domain.user.model.Gender;
import com.pm.todoapp.users.profile.dto.UserResponseDTO;
import com.pm.todoapp.users.social.dto.ProfileStatusDTO;
import com.pm.todoapp.users.social.service.SocialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Social API", description = "Endpoints for managing social interactions and friendships")
public class SocialController {
    private final SocialService socialService;

    @Operation(
            summary = "Send a friend invitation",
            description = "Sends a friend request from the currently authenticated user to another user specified by their ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Friend invitation sent successfully"),
                    @ApiResponse(responseCode = "400", description = "Bad Request (e.g., users are already friends or an invitation already exists)"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized if the user is not authenticated"),
                    @ApiResponse(responseCode = "404", description = "Not Found if the receiver does not exist")
            }
    )
    @PostMapping("/{receiverId}/invite")
    public ResponseEntity<Void> sendFriendInvitation(
            @PathVariable UUID receiverId,
            @AuthenticationPrincipal UUID senderId
    ){
        socialService.newFriendRequest(senderId, receiverId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Accept a friend request",
            description = "Accepts a pending friend request. The authenticated user must be the receiver of this request.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Friend request accepted successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden if the user is not the receiver of the request"),
                    @ApiResponse(responseCode = "404", description = "Not Found if the request does not exist")
            }
    )
    @PostMapping("/{requestId}/accept")
    public ResponseEntity<Void> acceptFriendRequest(
            @PathVariable UUID requestId,
            @AuthenticationPrincipal UUID currentUserId
    ){
        socialService.acceptFriendRequest(requestId, currentUserId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Decline a friend request",
            description = "Declines a pending friend request. The authenticated user must be the receiver of this request.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Friend request declined successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden if the user is not the receiver of the request"),
                    @ApiResponse(responseCode = "404", description = "Not Found if the request does not exist")
            }
    )
    @PostMapping("/{requestId}/decline")
    public ResponseEntity<Void> declineFriendRequest(
            @PathVariable UUID requestId,
            @AuthenticationPrincipal UUID currentUserId
    ){
        socialService.declineFriendRequest(requestId, currentUserId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Cancel a sent friend request",
            description = "Cancels an outgoing friend request. The authenticated user must be the original sender of this request.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Friend request cancelled successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden if the user is not the sender of the request"),
                    @ApiResponse(responseCode = "404", description = "Not Found if the request does not exist")
            }
    )
    @PostMapping("/{requestId}/cancel")
    public ResponseEntity<Void> cancelFriendRequest(
            @PathVariable UUID requestId,
            @AuthenticationPrincipal UUID currentUserId
    ){
        socialService.cancelFriendRequest(requestId, currentUserId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Remove a friend",
            description = "Removes a user from the authenticated user's friend list. The friendship is removed for both users.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Friend removed successfully"),
                    @ApiResponse(responseCode = "400", description = "Bad Request if the users are not friends"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    @PostMapping("/{userId}/remove")
    public ResponseEntity<Void> removeFriend(
            @AuthenticationPrincipal UUID currentUserId,
            @PathVariable UUID userId
    ){
        socialService.removeFriend(currentUserId, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Get the user's friend list",
            description = "Retrieves a list of all users who are friends with the currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved friend list"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    @GetMapping("/friends")
    public ResponseEntity<List<UserResponseDTO>> getFriendRequests(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(socialService.getFriends(userId));
    }

    @Operation(
            summary = "Get friendship status with another user",
            description = "Determines the social relationship status (e.g., FRIEND, INVITATION_SENT, NOT_FRIENDS) between the authenticated user and another user specified by their ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully determined the friendship status"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "404", description = "Not Found if the other user does not exist")
            }
    )
    @PostMapping("/{userId}/status")
    public ResponseEntity<ProfileStatusDTO> getFriendStatus(
            @AuthenticationPrincipal UUID currentUserId,
            @PathVariable UUID userId
    ){

        return ResponseEntity.ok(socialService.determineFriendshipStatus(currentUserId, userId));
    }

    @Operation(
            summary = "Get available gender options",
            description = "Returns a list of all possible string values for the Gender enum, which can be used in user profiles."
    )
    @GetMapping("/genders")
    public ResponseEntity<List<String>> getGenders() {
        List<String> genders = Arrays.stream(Gender.values())
                .map(Enum::name)
                .toList();
        return ResponseEntity.ok(genders);
    }
}
