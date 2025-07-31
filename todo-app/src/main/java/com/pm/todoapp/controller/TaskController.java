package com.pm.todoapp.controller;

import com.pm.todoapp.dto.TaskRequestDTO;
import com.pm.todoapp.dto.TaskResponseDTO;
import com.pm.todoapp.model.Priority;
import com.pm.todoapp.model.Status;
import com.pm.todoapp.model.User;
import com.pm.todoapp.service.TaskService;
import com.pm.todoapp.service.UsersService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/task")
public class TaskController {

    private final TaskService taskService;
    private final UsersService usersService;

    @Autowired
    public TaskController(TaskService taskService, UsersService usersService) {
        this.taskService = taskService;
        this.usersService = usersService;
    }

    @GetMapping("/new")
    public String showNewTaskForm(@RequestParam(name = "team", required = false) UUID teamId,
                                  Model model) {

        model.addAttribute("task", new TaskRequestDTO());
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("formAction", "/task/new");
        model.addAttribute("isEditMode", false);

        if (teamId != null) {
            model.addAttribute("teamId", teamId);
        }

        return "task-form";
    }

    @PostMapping("/new")
    public String save(@ModelAttribute("task") @Valid TaskRequestDTO taskDto,
                       @RequestParam(name = "team", required = false) UUID teamId,
                       BindingResult bindingResult,
                       Model model) {

        // TODO refactor
        UUID userId = getCurrentUserId();

        if (bindingResult.hasErrors()) {
            model.addAttribute("priorities", Priority.values());
            model.addAttribute("formAction", "/task/new");
            return "task-form";
        }

        TaskResponseDTO response = taskService.save(taskDto, userId, teamId);

        model.addAttribute("taskResponse", response);
        model.addAttribute("message", "Task saved successfully!");

        return "task-details";
    }

    @GetMapping("/{id}")
    public String showTask(@PathVariable UUID id, Model model) {

        TaskResponseDTO response = taskService.findByTaskId(id);
        model.addAttribute("taskResponse", response);
        return "task-details";
    }

    @GetMapping("/edit/{id}")
    public String editTaskForm(@PathVariable UUID id, Model model) {

        TaskResponseDTO taskResponse = taskService.findByTaskId(id);
        TaskRequestDTO taskRequest = taskService.findTaskRequestById(id);

        model.addAttribute("task", taskRequest);
        model.addAttribute("taskId", id);
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("statuses", Status.values());

        model.addAttribute("isEditMode", true);

        if (taskResponse.getTeam() != null)
            model.addAttribute("teamId", taskResponse.getTeam().getId());

        return "task-form";
    }

    @PostMapping("/update/{id}")
    public String updateTask(@PathVariable UUID id,
                             @ModelAttribute("task") @Valid TaskRequestDTO taskDto,
                             @RequestParam(name = "team", required = false) UUID teamId,
                             BindingResult bindingResult,
                             Model model) {

        UUID userId = getCurrentUserId();

        if (bindingResult.hasErrors()) {
            model.addAttribute("taskId", id);
            model.addAttribute("priorities", Priority.values());
            model.addAttribute("statuses", Status.values());
            model.addAttribute("isEditMode", true);
            if (teamId != null) {
                model.addAttribute("teamId", teamId);
            }
            return "task-form";
        }

        TaskResponseDTO updated = taskService.update(taskDto, id, userId, teamId);
        model.addAttribute("taskResponse", updated);
        model.addAttribute("message", "Task updated successfully!");

        return "task-details";
    }

    private UUID getCurrentUserId() {
        // TODO: to refactor
        return usersService.getTestUser().getId();
    }

}
