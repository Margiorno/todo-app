package com.pm.todoapp.notifications.controller;

import com.pm.todoapp.notifications.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> readNotification(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {

        notificationService.readNotification(id, userId);
        return ResponseEntity.ok().build();
    }


}
