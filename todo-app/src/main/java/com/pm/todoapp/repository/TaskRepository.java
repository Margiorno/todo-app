package com.pm.todoapp.repository;

import com.pm.todoapp.model.Task;
import com.pm.todoapp.model.Team;
import com.pm.todoapp.model.User;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.UUID;

public interface TaskRepository extends CrudRepository<Task, UUID> {

    Iterable<Task> findByAssigneesContainingAndTaskDate(User user, LocalDate taskDate);
    Iterable<Task> findByAssigneesContainingAndTeamAndTaskDate(User user, Team team, LocalDate taskDate);

    Iterable<Task> findByTeamId(UUID teamId);
}
