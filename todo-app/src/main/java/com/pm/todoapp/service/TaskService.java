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

    @Autowired
    public TaskService(TaskRepository taskRepository, TaskDAO taskDAO, TeamService teamService) {
        this.taskRepository = taskRepository;
        this.taskDAO = taskDAO;
        this.teamService = teamService;
    }

    public List<TaskResponseDTO> findAll() {
        Iterable<Task> tasks = taskRepository.findAll();

        return StreamSupport.stream(tasks.spliterator(), false).map(TaskMapper::toResponseDTO).toList();
    }

    public TaskResponseDTO save(TaskRequestDTO taskDto, UUID teamId) {

        // TODO not random id
        Task task = TaskMapper.toEntity(taskDto, new User());

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

    public TaskResponseDTO findById(UUID id) {
        Task task = taskRepository.findById(id).orElseThrow(
                ()->new TaskNotFoundException("Task with this id does not exists: %s".formatted(id)));

        return TaskMapper.toResponseDTO(task);
    }

    public List<TaskResponseDTO> findByDate(LocalDate centerDate) {
        Iterable<Task> tasks = taskRepository.findByTaskDate(centerDate);

        return StreamSupport.stream(tasks.spliterator(), false).map(TaskMapper::toResponseDTO).toList();
    }

    public List<TaskResponseDTO> findByTeam(UUID teamId) {
        Iterable<Task> tasks = taskRepository.findByTeamId(teamId);

        return StreamSupport.stream(tasks.spliterator(), false).map(TaskMapper::toResponseDTO).toList();
    }

    public List<TaskResponseDTO> findByBasicFilters(
            Priority priority,
            Status status,
            LocalDate startDate,
            LocalDate endDate
    ) {
        Iterable<Task> tasks = taskDAO.findByBasicFilters(priority, status, startDate, endDate);

        return StreamSupport.stream(tasks.spliterator(), false).map(TaskMapper::toResponseDTO).toList();
    }
}
