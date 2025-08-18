package com.pm.todoapp.controller;

import com.pm.todoapp.dto.FriendRequestDTO;
import com.pm.todoapp.dto.FriendRequestNotificationDTO;
import com.pm.todoapp.dto.JoinTeamRequestDTO;
import com.pm.todoapp.model.FriendRequest;
import com.pm.todoapp.service.UsersService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UsersService usersService;

    public UserController(UsersService usersService) {
        this.usersService = usersService;
    }

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
}
