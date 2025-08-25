package com.pm.todoapp.tasks.controller;

import com.pm.todoapp.tasks.dto.TaskFetchScope;
import com.pm.todoapp.tasks.dto.TaskRequestDTO;
import com.pm.todoapp.tasks.dto.TaskResponseDTO;
import com.pm.todoapp.tasks.model.Priority;
import com.pm.todoapp.tasks.model.Status;
import com.pm.todoapp.tasks.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/task")
public class TaskController {

    private final TaskService taskService;

    @PostMapping("/new")
    public ResponseEntity<TaskResponseDTO> create(
            @AuthenticationPrincipal UUID userId,
            @RequestBody @Valid TaskRequestDTO taskDto,
            @RequestParam(name = "team", required = false) UUID teamId) {

        TaskResponseDTO saved = taskService.save(taskDto, userId, teamId);

        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<TaskResponseDTO> updateTask(@PathVariable UUID id,
                                                      @Valid @RequestBody TaskRequestDTO taskDto,
                                                      @AuthenticationPrincipal UUID userId,
                                                      @RequestParam(name = "team", required = false) UUID teamId) {

        TaskResponseDTO updatedTask = taskService.update(taskDto, id, userId, teamId);
        return ResponseEntity.ok(updatedTask);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> getTaskById(@PathVariable UUID id) {
        TaskResponseDTO taskResponse = taskService.findById(id);

        return ResponseEntity.ok(taskResponse);
    }

    @GetMapping
    public ResponseEntity<List<TaskResponseDTO>> getTasks(
            @AuthenticationPrincipal UUID userId,
            @RequestParam(required = false) UUID teamId,
            @RequestParam(required = false, defaultValue = "USER_TASKS") TaskFetchScope scope,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<TaskResponseDTO> tasks;
        if (date != null) {
            tasks = taskService.findByDate(date, userId, teamId, scope);
        } else if (priority != null || status != null || startDate != null || endDate != null) {
            tasks = taskService.findByBasicFilters(priority, status, startDate, endDate, userId, teamId, scope);
        } else if (teamId != null) {
            tasks = taskService.findByTeam(teamId, userId, scope);
        } else {
            tasks = taskService.findByUserId(userId);
        }
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/priorities")
    public ResponseEntity<List<String>> getPriorities() {
        return ResponseEntity.ok(Arrays.stream(Priority.values()).map(Enum::name).toList());
    }

    @GetMapping("/statuses")
    public ResponseEntity<List<String>> getStatuses() {
        return ResponseEntity.ok(Arrays.stream(Status.values()).map(Enum::name).toList());
    }

    @GetMapping("/scopes")
    public ResponseEntity<List<String>> getScopes() {
        return ResponseEntity.ok(Arrays.stream(TaskFetchScope.values()).map(Enum::name).toList());
    }
}
