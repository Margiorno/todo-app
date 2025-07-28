package com.pm.todoapp.service;

import com.pm.todoapp.dto.TaskRequestDTO;
import com.pm.todoapp.dto.TaskResponseDTO;
import com.pm.todoapp.exceptions.TaskNotFoundException;
import com.pm.todoapp.mapper.TaskMapper;
import com.pm.todoapp.model.Task;
import com.pm.todoapp.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;

@Service
public class TaskService {
    private final TaskRepository taskRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<TaskResponseDTO> findAll() {
        Iterable<Task> tasks = taskRepository.findAll();

        return StreamSupport.stream(tasks.spliterator(), false).map(TaskMapper::toResponseDTO).toList();
    }

    public TaskResponseDTO save(TaskRequestDTO taskDto) {

        // TODO not random id
        Task task = TaskMapper.toEntity(taskDto, UUID.randomUUID());
        Task savedTask = taskRepository.save(task);

        return TaskMapper.toResponseDTO(savedTask);
    }

    public TaskResponseDTO findById(UUID id) {
        Task task = taskRepository.findById(id).orElseThrow(
                ()->new TaskNotFoundException("Task with this id does not exists"));

        return TaskMapper.toResponseDTO(task);
    }
}
