package com.pm.todoapp.teams.controller;

import com.pm.todoapp.teams.dto.TeamInviteResponseDTO;
import com.pm.todoapp.teams.dto.JoinTeamRequestDTO;
import com.pm.todoapp.teams.dto.TeamMemberDTO;
import com.pm.todoapp.teams.dto.TeamResponseDTO;
import com.pm.todoapp.teams.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Controller
@RequestMapping("/teams")
@Tag(name = "Teams API", description = "Endpoints for creating and managing teams and their members")
public class TeamsController {

    private final TeamService teamService;

    @Operation(
            summary = "Create a new team",
            description = "Creates a new team with the specified name. The authenticated user is automatically assigned as the team owner.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Team created successfully"),
                    @ApiResponse(responseCode = "400", description = "Bad Request if the team name is invalid"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized if the user is not authenticated")
            }
    )
    @PostMapping("/create")
    public ResponseEntity<TeamResponseDTO> createTeam(
            @AuthenticationPrincipal UUID userId,
            @RequestParam String teamName
    ) {
        return ResponseEntity.ok(teamService.createTeam(teamName, userId));
    }

    @Operation(
            summary = "Generate a team invite code",
            description = "Generates a new single-use invite code that allows other users to join the specified team.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Invite code generated successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden if the user does not have permission to generate codes for this team"),
                    @ApiResponse(responseCode = "404", description = "Not Found if the team does not exist")
            }
    )
    @PostMapping("{teamId}/generate-invite-code")
    public ResponseEntity<TeamInviteResponseDTO> generateInviteCode(@PathVariable UUID teamId) {

        TeamInviteResponseDTO code = teamService.generateInviteCode(teamId);

        return ResponseEntity.ok(code);
    }

    @Operation(
            summary = "Remove a member from a team",
            description = "Removes a specified user from a team. This action is typically restricted to team owners or admins.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User removed from the team successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden if the user does not have permission to remove members"),
                    @ApiResponse(responseCode = "404", description = "Not Found if the team or user does not exist")
            }
    )
    @PostMapping("{teamId}/delete-member")
    public ResponseEntity<TeamInviteResponseDTO> deleteUser(
            @PathVariable UUID teamId,
            @RequestParam UUID userId
    ) {

        teamService.deleteUserFromTeam(teamId, userId);

        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Join a team using an invite code",
            description = "Allows the authenticated user to join a team by providing a valid, non-expired invite code.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Joined team successfully"),
                    @ApiResponse(responseCode = "400", description = "Bad Request if the invite code is invalid, expired, or already used"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    @PostMapping("/join")
    public ResponseEntity<Map<String, String>> joinTeam(
            @AuthenticationPrincipal UUID userId,
            @RequestBody JoinTeamRequestDTO request
    ){
        teamService.join(request.getCode(), userId);
        return ResponseEntity.ok(Map.of("message", "Joined successfully"));
    }

    @Operation(
            summary = "Get all teams for the authenticated user",
            description = "Retrieves a list of all teams that the currently authenticated user is a member of.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of teams"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    @GetMapping("/all")
    public ResponseEntity<List<TeamResponseDTO>> getAllTeams(
            @AuthenticationPrincipal UUID userId
    ){
        return ResponseEntity.ok(teamService.findAllByUserId(userId));
    }

    @Operation(
            summary = "Get all members of a specific team",
            description = "Retrieves a list of all members for a given team. The authenticated user must be a member of the team to access this information.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved team members"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden if the user is not a member of the team"),
                    @ApiResponse(responseCode = "404", description = "Not Found if the team does not exist")
            }
    )
    @GetMapping("/{teamId}/members")
    public ResponseEntity<Set<TeamMemberDTO>> getTeamMembers(@PathVariable UUID teamId) {
        return ResponseEntity.ok(teamService.findTeamMembers(teamId));
    }
}
