package com.pm.todoapp.service;

import com.pm.todoapp.dto.TaskRequestDTO;
import com.pm.todoapp.dto.TaskResponseDTO;
import com.pm.todoapp.exceptions.TaskNotFoundException;
import com.pm.todoapp.mapper.TaskMapper;
import com.pm.todoapp.model.Priority;
import com.pm.todoapp.model.Status;
import com.pm.todoapp.model.Task;
import com.pm.todoapp.model.User;
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

    @Autowired
    public TaskService(TaskRepository taskRepository, TaskDAO taskDAO) {
        this.taskRepository = taskRepository;
        this.taskDAO = taskDAO;
    }

    public List<TaskResponseDTO> findAll() {
        Iterable<Task> tasks = taskRepository.findAll();

        return StreamSupport.stream(tasks.spliterator(), false).map(TaskMapper::toResponseDTO).toList();
    }

    public TaskResponseDTO save(TaskRequestDTO taskDto) {

        // TODO not random id
        Task task = TaskMapper.toEntity(taskDto, new User());
        Task savedTask = taskRepository.save(task);

        return TaskMapper.toResponseDTO(savedTask);
    }

    public TaskResponseDTO findById(UUID id) {
        Task task = taskRepository.findById(id).orElseThrow(
                ()->new TaskNotFoundException("Task with this id does not exists: %s".formatted(id)));

        return TaskMapper.toResponseDTO(task);
    }

    public TaskResponseDTO update(TaskRequestDTO taskDto, UUID taskId) {

        if(taskRepository.existsById(taskId))
            throw new TaskNotFoundException("Task with this id does not exists: %s".formatted(taskId));

        Task task = TaskMapper.toEntity(taskDto, new User(), taskId);
        Task savedTask = taskRepository.save(task);

        return TaskMapper.toResponseDTO(savedTask);
    }

    public List<TaskResponseDTO> findByDate(LocalDate centerDate) {
        Iterable<Task> tasks = taskRepository.findByTaskDate(centerDate);

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
