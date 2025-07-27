package com.pm.todoapp.controller;

import com.pm.todoapp.dto.TaskRequestDTO;
import com.pm.todoapp.model.Priority;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/task")
public class TaskController {

    @GetMapping("/new")
    public String showNewTaskForm(Model model) {

        model.addAttribute("task", new TaskRequestDTO());
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("formAction", "/task/new");

        return "task-form";
    }

    @PostMapping("/new")
    public String createTask(@ModelAttribute("task") @Valid TaskRequestDTO taskDto,
                             BindingResult bindingResult,
                             Model model) {

        // TODO FORM HANDLING

        if (bindingResult.hasErrors())
            model.addAttribute("message", "Error: \n" + bindingResult.getAllErrors());
        else
            model.addAttribute("message", "Task saved: " + taskDto.toString());

        return "task-result";
    }

}
