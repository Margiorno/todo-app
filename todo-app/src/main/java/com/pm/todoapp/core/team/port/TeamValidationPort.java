package com.pm.todoapp.core.team.port;

import java.util.UUID;

public interface TeamValidationPort {
    void ensureTeamExistsById(UUID teamId);
}
