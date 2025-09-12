package com.pm.todoapp.users.profile.controller;

import com.pm.todoapp.users.profile.dto.UserResponseDTO;
import com.pm.todoapp.users.profile.service.UsersService;
import com.pm.todoapp.users.social.service.SocialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@Tag(name = "User Profile API", description = "Endpoints for managing user profiles")
@RequestMapping("/users")
public class UserController {

    private final UsersService usersService;

    @Operation(
            summary = "Upload a profile picture",
            description = "Uploads a new profile picture for the authenticated user. The old picture, if it exists, will be deleted. This endpoint accepts multipart/form-data.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Profile picture updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Bad Request if the file is missing or invalid"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized if the user is not authenticated"),
            }
    )
    @PostMapping("/profile/avatar")
    public ResponseEntity<Map<String,String>> uploadProfilePicture(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal UUID userId) {

        String filename = usersService.updateProfilePicture(file, userId);

        return ResponseEntity.ok(Collections.singletonMap("filename", filename));
    }

    @Operation(
            summary = "Partially update a user's profile",
            description = "Updates a single field of the user's profile, such as 'firstName', 'lastName', or 'email'. The request body should be a JSON object with a single key-value pair.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "A JSON object with the field to update and its new value.",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(type = "object", example = "{\"firstName\": \"Jane\"}")
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Field updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Bad Request if the payload is empty or the field name is not supported"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized if the user is not authenticated")
            }
    )
    @PatchMapping("/profile/update")
    public ResponseEntity<Map<String, Object>> updateProfileField(@RequestBody Map<String, String> payload,
                                                                  @AuthenticationPrincipal UUID userId) {
        if (payload.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String field = payload.keySet().iterator().next();
        String value = payload.get(field);

        return ResponseEntity.ok(usersService.update(field, value, userId));
    }

    @Operation(
            summary = "Get user profile by ID",
            description = "Retrieves the public-facing profile information for a user based on their unique identifier (UUID).",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved user profile"),
                    @ApiResponse(responseCode = "404", description = "Not Found if no user exists with the given ID")
            }
    )
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> getUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(usersService.findById(userId));
    }

}
