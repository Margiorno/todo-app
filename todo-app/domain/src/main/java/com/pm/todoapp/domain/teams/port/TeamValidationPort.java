package com.pm.todoapp.domain.teams.port;

import java.util.UUID;

public interface TeamValidationPort {
    void ensureTeamExistsById(UUID teamId);
}
