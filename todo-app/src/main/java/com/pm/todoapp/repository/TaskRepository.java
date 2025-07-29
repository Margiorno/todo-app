package com.pm.todoapp.repository;

import com.pm.todoapp.model.Task;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.UUID;

public interface TaskRepository extends CrudRepository<Task, UUID> {

    Iterable<Task> findByTaskDate(LocalDate taskDate);
    Iterable<Task> findByTaskDateAndTeamId(LocalDate taskDate, UUID teamId);

    Iterable<Task> findByTeamId(UUID teamId);
}
