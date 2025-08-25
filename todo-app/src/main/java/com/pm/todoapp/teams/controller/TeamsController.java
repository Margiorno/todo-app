package com.pm.todoapp.teams.controller;

import com.pm.todoapp.teams.dto.TeamInviteResponseDTO;
import com.pm.todoapp.teams.dto.JoinTeamRequestDTO;
import com.pm.todoapp.teams.dto.TeamResponseDTO;
import com.pm.todoapp.teams.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@Controller
@RequestMapping("/teams")
public class TeamsController {

    private final TeamService teamService;

    @PostMapping("/create")
    public ResponseEntity<TeamResponseDTO> createTeam(
            @AuthenticationPrincipal UUID userId,
            @RequestParam String teamName
    ) {
        return ResponseEntity.ok(teamService.createTeam(teamName, userId));
    }

    @PostMapping("{teamId}/generate-invite-code")
    public ResponseEntity<TeamInviteResponseDTO> generateInviteCode(@PathVariable UUID teamId) {

        TeamInviteResponseDTO code = teamService.generateInviteCode(teamId);

        return ResponseEntity.ok(code);
    }

    //TODO team task with deleted member (find solution)
    @PostMapping("{teamId}/delete-member")
    public ResponseEntity<TeamInviteResponseDTO> deleteUser(
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
