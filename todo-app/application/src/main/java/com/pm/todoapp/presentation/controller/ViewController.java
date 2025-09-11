package com.pm.todoapp.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RequiredArgsConstructor
@Controller
@RequestMapping()
public class ViewController {

    @Operation(summary = "Serves the main dashboard page")
    @GetMapping({"/", "/calendar", "/filter"})
    public String showAllTasks() {
        return "dashboard";
    }

    @Operation(summary = "Serves the chat page")
    @GetMapping("/chat")
    public String showChatView() {
        return "chat";
    }

    @Operation(summary = "Serves the profile page for a specific user")
    @GetMapping("/profile/{profileId}")
    public String showProfilePage(@PathVariable String profileId) {
        return "profile";
    }

    @Operation(summary = "Redirects the currently authenticated user to their own profile page")
    @GetMapping("/profile")
    public String redirectToMyProfile(@AuthenticationPrincipal UUID userId) {
        return "redirect:/profile/" + userId.toString();
    }

    @Operation(summary = "Serves the notifications page")
    @GetMapping("/notifications")
    public String showNotifications() {
        return "notifications";
    }


}
