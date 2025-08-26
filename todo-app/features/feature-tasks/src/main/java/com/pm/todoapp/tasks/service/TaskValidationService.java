package com.pm.todoapp.tasks.service;

import com.pm.todoapp.common.exceptions.TaskAccessDeniedException;
import com.pm.todoapp.domain.team.model.Team;
import com.pm.todoapp.domain.team.port.TeamValidationPort;
import com.pm.todoapp.domain.team.repository.TeamRepository;
import com.pm.todoapp.domain.user.model.User;
import com.pm.todoapp.domain.user.port.UserValidationPort;
import com.pm.todoapp.domain.user.repository.UserRepository;
import com.pm.todoapp.tasks.model.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class TaskValidationService {

    private final TeamValidationPort teamValidationPort;
    private final UserValidationPort userValidationPort;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    public User getValidatedUser(UUID userId) {
        if (userId == null) {
            throw new TaskAccessDeniedException("UserId is required");
        }
        userValidationPort.ensureUserExistsById(userId);
        return userRepository.getReferenceById(userId);
    }

    public Team getValidatedTeam(UUID teamId) {
        if (teamId == null) {
            throw new TaskAccessDeniedException("TeamId is required");
        }

        teamValidationPort.ensureTeamExistsById(teamId);
        return teamRepository.getReferenceById(teamId);
    }

    public void validateUserAssignedToTask(Task task, User user) {
        if (!task.getAssignees().contains(user)) {
            throw new TaskAccessDeniedException("User '%s' is not authorized to modify task '%s'"
                    .formatted(user.getId(), task.getId()));
        }
    }

    public void validateTeamMatches(Task task, UUID teamId){
        UUID existingTeamId = task.getTeam() != null ? task.getTeam().getId() : null;

        if (!Objects.equals(existingTeamId, teamId)) {
            throw new TaskAccessDeniedException(
                    "Cannot change team of task '%s': existingTeamId='%s', newTeamId='%s'"
                    .formatted(task.getId(), existingTeamId, teamId));
        }
    }
}
