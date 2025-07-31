package com.pm.todoapp.controller;

import com.pm.todoapp.dto.TaskFetchScope;
import com.pm.todoapp.dto.TaskResponseDTO;
import com.pm.todoapp.model.Priority;
import com.pm.todoapp.model.Status;
import com.pm.todoapp.service.AuthService;
import com.pm.todoapp.service.TaskService;
import com.pm.todoapp.service.TeamService;
import com.pm.todoapp.service.UsersService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/")
public class DashboardController {

    private final TaskService taskService;
    private final TeamService teamService;

    @Autowired
    public DashboardController(TaskService taskService, TeamService teamService) {
        this.taskService = taskService;
        this.teamService = teamService;
    }

    @Data
    public static class TaskFilterCriteria {
        private Priority priority;
        private Status status;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate startDate;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate endDate;
    }

    @GetMapping
    public String showAllTasks(
            @RequestAttribute("userId") UUID userId,
            @RequestParam(name = "team", required = false) UUID teamId,
            @RequestParam(name = "scope", required = false, defaultValue = "USER_TASKS") TaskFetchScope scope,
            Model model) {

        List<TaskResponseDTO> tasks = (teamId != null)
                ? taskService.findByTeam(teamId, userId, scope)
                : taskService.findByUserId(userId);

        model.addAttribute("tasks", tasks);
        model.addAttribute("view", "all");
        populateCommonModelAttributes(model, userId, teamId, scope);

        return "dashboard";
    }

    @GetMapping("/calendar")
    public String showCalendarView(
            @RequestAttribute("userId") UUID userId,
            @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate selectedDate,
            @RequestParam(name = "team", required = false) UUID teamId,
            @RequestParam(name = "scope", required = false, defaultValue = "USER_TASKS") TaskFetchScope scope,
            Model model) {

        LocalDate centerDate = (selectedDate != null) ? selectedDate : LocalDate.now();

        List<TaskResponseDTO> tasks = taskService.findByDate(centerDate, userId, teamId, scope);

        model.addAttribute("tasks", tasks);
        model.addAttribute("view", "calendar");
        model.addAttribute("centerDate", centerDate);
        populateCommonModelAttributes(model, userId, teamId, scope);

        return "dashboard";
    }

    @GetMapping("/filter")
    public String showFilteredTasks(
            @RequestAttribute("userId") UUID userId,
            @ModelAttribute TaskFilterCriteria criteria,
            @RequestParam(name = "team", required = false) UUID teamId,
            @RequestParam(name = "scope", required = false, defaultValue = "USER_TASKS") TaskFetchScope scope,
            Model model) {

        List<TaskResponseDTO> tasks = taskService.findByBasicFilters(
                criteria.getPriority(), criteria.getStatus(), criteria.getStartDate(), criteria.getEndDate(),
                userId, teamId, scope);

        model.addAttribute("tasks", tasks);
        model.addAttribute("view", "filter");

        model.addAttribute("selectedPriority", criteria.getPriority());
        model.addAttribute("selectedStatus", criteria.getStatus());
        model.addAttribute("selectedStartDate", criteria.getStartDate());
        model.addAttribute("selectedEndDate", criteria.getEndDate());

        populateCommonModelAttributes(model, userId, teamId, scope);

        return "dashboard";
    }

    private void populateCommonModelAttributes(Model model, UUID userId, UUID teamId, TaskFetchScope scope) {
        if (teamId != null) {
            model.addAttribute("selectedTeamId", teamId.toString());
            model.addAttribute("selectedTeamName", teamService.findById(teamId).getName());
        }

        model.addAttribute("allTeams", teamService.findAllByUserId(userId));
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("statuses", Status.values());
        model.addAttribute("scopes", TaskFetchScope.values());
        model.addAttribute("selectedScope", scope);
    }


}
