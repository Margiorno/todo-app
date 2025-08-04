package com.pm.todoapp.controller;

import com.pm.todoapp.dto.InviteResponseDTO;
import com.pm.todoapp.dto.JoinTeamRequestDTO;
import com.pm.todoapp.service.TeamService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("{teamId}/generate-invite-code")
    public ResponseEntity<InviteResponseDTO> generateInviteCode(@PathVariable UUID teamId) {

        InviteResponseDTO code = teamService.generateInviteCode(teamId);

        return ResponseEntity.ok(code);
    }

    @PostMapping("{teamId}/delete-member")
    public ResponseEntity<InviteResponseDTO> deleteUser(
            @PathVariable UUID teamId,
            @RequestParam UUID userId
    ) {

        teamService.deleteUserFromTeam(teamId, userId);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/join")
    public ResponseEntity<String> joinTeam(
            @AuthenticationPrincipal UUID userId,
            @RequestBody JoinTeamRequestDTO request
    ){

        teamService.join(request.getCode(), userId);
        return ResponseEntity.ok("Joined");
    }



}
