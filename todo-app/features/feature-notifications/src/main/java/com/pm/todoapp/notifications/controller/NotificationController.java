package com.pm.todoapp.notifications.controller;

import com.pm.todoapp.notifications.dto.NotificationDTO;
import com.pm.todoapp.notifications.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;


import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/notifications")
@Tag(name = "Notifications API", description = "Endpoints for retrieving user notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(
            summary = "Get all notifications for the authenticated user",
            description = "Retrieves a list of all notifications for the currently logged-in user. After the notifications are retrieved, they are all marked as read on the server. The list is sorted by creation date in descending order.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved notifications"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized if the user is not authenticated")
            }
    )
    @GetMapping("/getAll")
    public ResponseEntity<List<NotificationDTO>> getAllNotifications(@AuthenticationPrincipal UUID userId) {

        List<NotificationDTO> notifications = notificationService.getAllNotificationsByUserId(userId);
        notificationService.markAllReadByUserId(userId);

        return ResponseEntity.ok(notifications);
    }
}
