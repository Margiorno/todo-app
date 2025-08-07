package com.pm.todoapp.controller;

import com.pm.todoapp.service.UsersService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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

    @PostMapping("/profile-picture")
    public ResponseEntity<Map<String,String>> uploadProfilePicture(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal UUID userId) {

        String filename = usersService.updateProfilePicture(file, userId);

        return ResponseEntity.ok(Collections.singletonMap("filename", filename));
    }


}
