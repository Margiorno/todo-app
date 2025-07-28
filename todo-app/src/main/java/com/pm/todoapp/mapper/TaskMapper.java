package com.pm.todoapp.mapper;

import com.pm.todoapp.dto.TaskRequestDTO;
import com.pm.todoapp.dto.TaskResponseDTO;
import com.pm.todoapp.model.Task;

import java.util.UUID;

public class TaskMapper {
    public static Task toEntity(TaskRequestDTO dto, UUID userId) {
        return Task.builder()
                .userId(userId)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .priority(dto.getPriority())
                .taskDate(dto.getTaskDate())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .build();
    }

    public static TaskResponseDTO toResponseDTO(Task task) {
        return TaskResponseDTO.builder()
                .id(task.getId().toString())
                .title(task.getTitle())
                .description(task.getDescription())
                .priority(task.getPriority().name())
                .status(task.getStatus() != null ? task.getStatus().name() : null)
                .taskDate(task.getTaskDate() != null ? task.getTaskDate().toString() : null)
                .startTime(task.getStartTime() != null ? task.getStartTime().toString() : null)
                .endTime(task.getEndTime() != null ? task.getEndTime().toString() : null)
                .build();
    }
}
