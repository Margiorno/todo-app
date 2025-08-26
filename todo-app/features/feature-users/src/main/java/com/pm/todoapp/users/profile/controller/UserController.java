package com.pm.todoapp.users.profile.controller;

import com.pm.todoapp.users.profile.dto.UserResponseDTO;
import com.pm.todoapp.users.profile.service.UsersService;
import com.pm.todoapp.users.social.service.SocialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

    private final UsersService usersService;
    private final SocialService socialService;

    @PostMapping("/profile/avatar")
    public ResponseEntity<Map<String,String>> uploadProfilePicture(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal UUID userId) {

        String filename = usersService.updateProfilePicture(file, userId);

        return ResponseEntity.ok(Collections.singletonMap("filename", filename));
    }

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

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> getUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(usersService.findById(userId));
    }

}
