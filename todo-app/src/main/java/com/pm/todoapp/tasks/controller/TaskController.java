package com.pm.todoapp.tasks.controller;

import com.pm.todoapp.tasks.dto.TaskRequestDTO;
import com.pm.todoapp.tasks.dto.TaskResponseDTO;
import com.pm.todoapp.tasks.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/task")
public class TaskController {

    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

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
        TaskResponseDTO taskResponse = taskService.findByTaskId(id);

        return ResponseEntity.ok(taskResponse);
    }
}
