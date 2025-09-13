package com.pm.todoapp.tasks.controller;

import com.pm.todoapp.tasks.dto.TaskFetchScope;
import com.pm.todoapp.tasks.dto.TaskRequestDTO;
import com.pm.todoapp.tasks.dto.TaskResponseDTO;
import com.pm.todoapp.tasks.model.Priority;
import com.pm.todoapp.tasks.model.Status;
import com.pm.todoapp.tasks.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Task API", description = "Endpoints for managing tasks")
public class TaskController {

    private final TaskService taskService;

    @Operation(
            summary = "Create a new task",
            description = "Creates a new task for the authenticated user. The task can optionally be associated with a team by providing a 'team' query parameter.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Task created successfully"),
                    @ApiResponse(responseCode = "400", description = "Bad Request if the task data is invalid"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized if the user is not authenticated"),
                    @ApiResponse(responseCode = "404", description = "Not Found if the specified team does not exist")
            }
    )
    @PostMapping("/new")
    public ResponseEntity<TaskResponseDTO> create(
            @AuthenticationPrincipal UUID userId,
            @RequestBody @Valid TaskRequestDTO taskDto,
            @RequestParam(name = "team", required = false) UUID teamId) {

        TaskResponseDTO saved = taskService.save(taskDto, userId, teamId);

        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Update an existing task",
            description = "Updates the details of an existing task by its ID. The user must be assigned to the task to perform the update.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Task updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Bad Request if the task data is invalid"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized if the user is not authenticated"),
                    @ApiResponse(responseCode = "403", description = "Forbidden if the user is not assigned to the task or if the team context is incorrect"),
                    @ApiResponse(responseCode = "404", description = "Not Found if the task does not exist")
            }
    )
    @PutMapping("/update/{id}")
    public ResponseEntity<TaskResponseDTO> updateTask(@PathVariable UUID id,
                                                      @Valid @RequestBody TaskRequestDTO taskDto,
                                                      @AuthenticationPrincipal UUID userId,
                                                      @RequestParam(name = "team", required = false) UUID teamId) {

        TaskResponseDTO updatedTask = taskService.update(taskDto, id, userId, teamId);
        return ResponseEntity.ok(updatedTask);
    }

    @Operation(
            summary = "Get a task by its ID",
            description = "Retrieves the full details of a single task by its unique identifier.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved the task"),
                    @ApiResponse(responseCode = "404", description = "Not Found if the task with the given ID does not exist")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> getTaskById(@PathVariable UUID id) {
        TaskResponseDTO taskResponse = taskService.findById(id);

        return ResponseEntity.ok(taskResponse);
    }

    @Operation(
            summary = "Get a list of tasks with optional filters",
            description = "Retrieves a list of tasks based on various filtering criteria. The endpoint's behavior changes based on the parameters provided:\n" +
                    "- **By Date:** If 'date' is provided, it returns tasks for that specific date.\n" +
                    "- **By Filters:** If 'priority', 'status', 'startDate', or 'endDate' are provided, it performs a search based on these criteria.\n" +
                    "- **By Team:** If only 'teamId' is provided, it returns tasks for that team (respecting the 'scope').\n" +
                    "- **By User:** If no other parameters are provided, it returns all tasks assigned to the authenticated user.\n" +
                    "The 'scope' parameter ('USER_TASKS' or 'TEAM_TASKS') refines the search when a 'teamId' is present.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of tasks"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized if the user is not authenticated")
            }
    )
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

    @Operation(summary = "Get available task priorities", description = "Returns a list of all possible string values for task priorities.")
    @GetMapping("/priorities")
    public ResponseEntity<List<String>> getPriorities() {
        return ResponseEntity.ok(Arrays.stream(Priority.values()).map(Enum::name).toList());
    }

    @Operation(summary = "Get available task statuses", description = "Returns a list of all possible string values for task statuses.")
    @GetMapping("/statuses")
    public ResponseEntity<List<String>> getStatuses() {
        return ResponseEntity.ok(Arrays.stream(Status.values()).map(Enum::name).toList());
    }

    @Operation(summary = "Get available task fetch scopes", description = "Returns a list of all possible string values for task fetch scopes (e.g., 'USER_TASKS', 'TEAM_TASKS').")
    @GetMapping("/scopes")
    public ResponseEntity<List<String>> getScopes() {
        return ResponseEntity.ok(Arrays.stream(TaskFetchScope.values()).map(Enum::name).toList());
    }
}
