package com.pm.todoapp.notifications.controller;

import com.pm.todoapp.notifications.dto.NotificationDTO;
import com.pm.todoapp.notifications.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/getAll")
    public ResponseEntity<List<NotificationDTO>> getAllNotifications(@AuthenticationPrincipal UUID userId) {

        List<NotificationDTO> notifications = notificationService.getAllNotificationsByUserId(userId);
        notificationService.markAllReadByUserId(userId);

        return ResponseEntity.ok(notifications);
    }
}
