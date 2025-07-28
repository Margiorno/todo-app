package com.pm.todoapp.controller;

import com.pm.todoapp.dto.TaskRequestDTO;
import com.pm.todoapp.dto.TaskResponseDTO;
import com.pm.todoapp.exceptions.TaskNotFoundException;
import com.pm.todoapp.model.Priority;
import com.pm.todoapp.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/task")
public class TaskController {

    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/new")
    public String showNewTaskForm(Model model) {

        model.addAttribute("task", new TaskRequestDTO());
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("formAction", "/task/new");

        return "task-form";
    }


    // TODO USERS IDENTIFICATION
    @PostMapping("/new")
    public String save(@ModelAttribute("task") @Valid TaskRequestDTO taskDto,
                             BindingResult bindingResult,
                             Model model) {

        // TODO FORM HANDLING

        if (bindingResult.hasErrors()) {
            model.addAttribute("priorities", Priority.values());
            model.addAttribute("formAction", "/task/new");
            return "task-form";
        }

        TaskResponseDTO response = taskService.save(taskDto);
        model.addAttribute("taskResponse", response);
        model.addAttribute("message", "Task saved successfully!");

        return "task-result";
    }

    @GetMapping("/list")
    public String showTasks(Model model) {

        List<TaskResponseDTO> tasks = taskService.findAll();

        model.addAttribute("tasks", tasks);

        return "task-list";
    }

    @GetMapping("/{id}")
    public String showTask(@PathVariable UUID id, Model model) {

        try{
            TaskResponseDTO task = taskService.findById(id);
            model.addAttribute("task", task);
            return "task-details";
        } catch (TaskNotFoundException e) {

            // TODO logic of not found
            model.addAttribute("message", "Task not found!");
            return "task-list";
        }
    }

    @GetMapping("/task/edit/{id}")
    public String editTaskForm(@PathVariable UUID id, Model model) {
        try{
            TaskResponseDTO task = taskService.findById(id);
            model.addAttribute("task", task);
            return "task-edit";
        } catch (TaskNotFoundException e) {

            // TODO logic of not found
            model.addAttribute("message", "Task not found!");
            return "task-list";
        }
    }

}
