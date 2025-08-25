package com.pm.todoapp.presentation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RequiredArgsConstructor
@Controller
@RequestMapping()
public class ViewController {

    @GetMapping({"/", "/calendar", "/filter"})
    public String showAllTasks() {
        return "dashboard";
    }

    @GetMapping("/chat")
    public String showChatView() {
        return "chat";
    }

    @GetMapping("/profile/{profileId}")
    public String showProfilePage(@PathVariable String profileId) {
        return "profile";
    }

    @GetMapping("/profile")
    public String redirectToMyProfile(@AuthenticationPrincipal UUID userId) {
        return "redirect:/profile/" + userId.toString();
    }

    @GetMapping("/notifications")
    public String showNotifications() {
        return "notifications";
    }


}
