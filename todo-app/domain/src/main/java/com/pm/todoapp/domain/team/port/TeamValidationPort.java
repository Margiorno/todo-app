package com.pm.todoapp.domain.team.port;

import java.util.UUID;

public interface TeamValidationPort {
    void ensureTeamExistsById(UUID teamId);
}
