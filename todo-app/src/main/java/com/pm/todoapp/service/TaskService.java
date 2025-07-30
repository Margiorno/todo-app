package com.pm.todoapp.service;

import com.pm.todoapp.dto.TaskRequestDTO;
import com.pm.todoapp.dto.TaskResponseDTO;
import com.pm.todoapp.exceptions.TaskNotFoundException;
import com.pm.todoapp.mapper.TaskMapper;
import com.pm.todoapp.model.*;
import com.pm.todoapp.repository.TaskDAO;
import com.pm.todoapp.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskDAO taskDAO;

    private final TeamService teamService;
    private final UsersService usersService;

    @Autowired
    public TaskService(TaskRepository taskRepository, TaskDAO taskDAO, TeamService teamService, UsersService usersService) {
        this.taskRepository = taskRepository;
        this.taskDAO = taskDAO;
        this.teamService = teamService;
        this.usersService = usersService;
    }

    public TaskResponseDTO save(TaskRequestDTO taskDto, UUID userId, UUID teamId) {

        User user = usersService.findById(userId);
        Task task = TaskMapper.toEntity(taskDto, user);

        if (teamId != null) {
            Team team = teamService.findById(teamId);
            task.setTeam(team);
        }

        Task savedTask = taskRepository.save(task);
        return TaskMapper.toResponseDTO(savedTask);
    }






    public TaskResponseDTO update(TaskRequestDTO taskDto, UUID taskId) {

        if(!taskRepository.existsById(taskId))
            throw new TaskNotFoundException("Task with this id does not exists: %s".formatted(taskId));

        //TODO USERS
        Task task = TaskMapper.toEntity(taskDto, new User(), taskId);
        Task savedTask = taskRepository.save(task);

        return TaskMapper.toResponseDTO(savedTask);
    }


    // FINDING

    public List<TaskResponseDTO> findByUserId(UUID userId) {
        User user = usersService.findById(userId);

        Iterable<Task> tasks = taskRepository.findByAssigneesContaining(user);
        return StreamSupport.stream(tasks.spliterator(), false).map(TaskMapper::toResponseDTO).toList();
    }

    public TaskResponseDTO findByTaskId(UUID id) {
        Task task = taskRepository.findById(id).orElseThrow(
                ()->new TaskNotFoundException("Task with this id does not exists: %s".formatted(id)));

        return TaskMapper.toResponseDTO(task);
    }

    public List<TaskResponseDTO> findByDate(LocalDate centerDate, UUID userId, UUID teamId, boolean allTeamTasksFlag) {

        User user = usersService.findById(userId);

        Iterable<Task> tasks = switch (teamId){
            case null -> taskRepository.findByAssigneesContainingAndTaskDate(user, centerDate);
            default -> {
                Team team = teamService.findById(teamId);
                yield allTeamTasksFlag?
                        taskRepository.findByTeamAndTaskDate(team, centerDate)
                        : taskRepository.findByAssigneesContainingAndTeamAndTaskDate(user, team, centerDate);
            }
        };

        return StreamSupport.stream(tasks.spliterator(), false).map(TaskMapper::toResponseDTO).toList();
    }

    public List<TaskResponseDTO> findByTeam(UUID teamId, UUID userId, boolean allTeamTasksFlag) {

        User user = usersService.findById(userId);
        Team team = teamService.findById(teamId);

        Iterable<Task> tasks;

        tasks = allTeamTasksFlag?
                taskRepository.findByTeam(team) :
                taskRepository.findByAssigneesContainingAndTeam(user, team);

        return StreamSupport.stream(tasks.spliterator(), false).map(TaskMapper::toResponseDTO).toList();
    }

    public List<TaskResponseDTO> findByBasicFilters(
            Priority priority,
            Status status,
            LocalDate startDate,
            LocalDate endDate,
            UUID userId,
            UUID teamId,
            boolean allTeamTasksFlag
    ) {

        User user = allTeamTasksFlag? null : usersService.findById(userId);
        Team team = teamService.findById(teamId);

        Iterable<Task> tasks = taskDAO.findByBasicFilters(priority, status, startDate, endDate, user, team);

        return StreamSupport.stream(tasks.spliterator(), false).map(TaskMapper::toResponseDTO).toList();
    }
}
