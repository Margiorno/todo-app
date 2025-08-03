package com.pm.todoapp.controller;

import com.pm.todoapp.service.TeamService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Controller
@RequestMapping("/teams")
public class TeamsController {

    private final TeamService teamService;

    public TeamsController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping("/create")
    public String createTeam(
            @AuthenticationPrincipal UUID userId,
            @RequestParam String teamName
    ) {

        teamService.createTeam(teamName, userId);

        return "redirect:/";
    }
}
