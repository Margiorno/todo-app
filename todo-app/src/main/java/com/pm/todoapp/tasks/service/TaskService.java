package com.pm.todoapp.tasks.service;

import com.pm.todoapp.core.team.model.Team;
import com.pm.todoapp.core.team.port.TeamValidationPort;
import com.pm.todoapp.core.team.repository.TeamRepository;
import com.pm.todoapp.core.user.model.User;
import com.pm.todoapp.core.user.port.UserValidationPort;
import com.pm.todoapp.core.user.repository.UserRepository;
import com.pm.todoapp.tasks.dto.TaskFetchScope;
import com.pm.todoapp.tasks.dto.TaskRequestDTO;
import com.pm.todoapp.tasks.dto.TaskResponseDTO;
import com.pm.todoapp.core.exceptions.TaskAccessDeniedException;
import com.pm.todoapp.core.exceptions.TaskNotFoundException;
import com.pm.todoapp.tasks.mapper.TaskMapper;
import com.pm.todoapp.tasks.model.Priority;
import com.pm.todoapp.tasks.model.Status;
import com.pm.todoapp.tasks.model.Task;
import com.pm.todoapp.tasks.repository.TaskDAO;
import com.pm.todoapp.tasks.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.StreamSupport;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskDAO taskDAO;
    private final TaskMapper taskMapper;
    private final TaskValidationService taskAuthorizationService;
    private final TaskValidationService taskValidationService;

    @Autowired
    public TaskService(TaskRepository taskRepository, TaskDAO taskDAO,
                       UserValidationPort userValidationPort, UserRepository userRepository, TaskMapper taskMapper, TeamRepository teamRepository, TeamValidationPort teamValidationPort, TaskValidationService taskAuthorizationService, TaskValidationService taskValidationService) {
        this.taskRepository = taskRepository;
        this.taskDAO = taskDAO;
        this.taskMapper = taskMapper;
        this.taskAuthorizationService = taskAuthorizationService;
        this.taskValidationService = taskValidationService;
    }

    public TaskResponseDTO save(TaskRequestDTO taskDto, UUID userId, UUID teamId) {

        User user = taskValidationService.getValidatedUser(userId);
        Task task = TaskMapper.toEntity(taskDto, Set.of(user));

        if (teamId != null) {
            Team team = taskValidationService.getValidatedTeam(teamId);
            task.setTeam(team);
        }

        Task savedTask = taskRepository.save(task);
        return taskMapper.toResponseDTO(savedTask);
    }

    @Transactional
    public TaskResponseDTO update(TaskRequestDTO taskDto, UUID taskId, UUID userId, UUID teamId) {

        User user = taskValidationService.getValidatedUser(userId);
        Task fromDb = findRawById(taskId);

        taskAuthorizationService.validateUserAssignedToTask(fromDb, user);
        taskAuthorizationService.validateTeamMatches(fromDb, teamId);

        Task task = TaskMapper.toEntity(taskDto, fromDb.getAssignees(), taskId);
        task.setTeam(fromDb.getTeam());

        Task savedTask = taskRepository.save(task);
        return taskMapper.toResponseDTO(savedTask);
    }


    public List<TaskResponseDTO> findByUserId(UUID userId) {

        User user = taskValidationService.getValidatedUser(userId);

        Iterable<Task> tasks = taskRepository.findByAssigneesContaining(user);
        return StreamSupport.stream(tasks.spliterator(), false).map(taskMapper::toResponseDTO).toList();
    }


    private Task findRawById(UUID id) {
        return taskRepository.findById(id).orElseThrow(
                ()->new TaskNotFoundException("Task with this id does not exists: %s".formatted(id)));
    }

    public TaskResponseDTO findById(UUID id) {
        return taskMapper.toResponseDTO(findRawById(id));
    }

    public List<TaskResponseDTO> findByDate(LocalDate centerDate, UUID userId, UUID teamId, TaskFetchScope taskFetchScope) {

        User user = taskValidationService.getValidatedUser(userId);

        Optional<Team> team = Optional.ofNullable(teamId).map(taskValidationService::getValidatedTeam);
        Iterable<Task> tasks = team.map(
                t -> switch (taskFetchScope) {
                    case TEAM_TASKS -> taskRepository
                            .findByTeamAndTaskDate(t, centerDate);
                    case USER_TASKS -> taskRepository
                            .findByAssigneesContainingAndTeamAndTaskDate(user, t, centerDate);
                })
                .orElseGet(() -> taskRepository.findByAssigneesContainingAndTaskDate(user, centerDate));

        return StreamSupport.stream(tasks.spliterator(), false).map(taskMapper::toResponseDTO).toList();
    }

    public List<TaskResponseDTO> findByTeam(UUID teamId, UUID userId, TaskFetchScope taskFetchScope) {

        User user = taskValidationService.getValidatedUser(userId);
        Team team = taskValidationService.getValidatedTeam(teamId);

        Iterable<Task> tasks = switch (taskFetchScope){
            case TEAM_TASKS -> taskRepository.findByTeam(team);
            case USER_TASKS -> taskRepository.findByAssigneesContainingAndTeam(user, team);
        };

        return StreamSupport.stream(tasks.spliterator(), false).map(taskMapper::toResponseDTO).toList();
    }

    public List<TaskResponseDTO> findByBasicFilters(
            Priority priority,
            Status status,
            LocalDate startDate,
            LocalDate endDate,
            UUID userId,
            UUID teamId,
            TaskFetchScope taskFetchScope
    ) {

        Optional<User> user = Optional.empty();
        Optional<Team> team = Optional.empty();

        switch (taskFetchScope) {
            case USER_TASKS -> {
                user = Optional.of(taskValidationService.getValidatedUser(userId));
                team = Optional.ofNullable(teamId).map(taskValidationService::getValidatedTeam);
            }

            case TEAM_TASKS -> team = Optional.of(taskValidationService.getValidatedTeam(teamId));
        }

        Iterable<Task> tasks = taskDAO.findByBasicFilters(
                priority,
                status,
                startDate,
                endDate,
                user.orElse(null),
                team.orElse(null)
        );

        return StreamSupport.stream(tasks.spliterator(), false).map(taskMapper::toResponseDTO).toList();
    }


}
