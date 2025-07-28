package com.pm.todoapp.mapper;

import com.pm.todoapp.dto.TaskRequestDTO;
import com.pm.todoapp.dto.TaskResponseDTO;
import com.pm.todoapp.model.Priority;
import com.pm.todoapp.model.Status;
import com.pm.todoapp.model.Task;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class TaskMapper {
    public static Task toEntity(TaskRequestDTO dto, UUID userId) {
        return Task.builder()
                .userId(userId)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .priority(dto.getPriority())
                .status(dto.getStatus())
                .taskDate(dto.getTaskDate())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .build();
    }

    public static Task toEntity(TaskRequestDTO dto, UUID userId, UUID taskId) {
        Task task = toEntity(dto, userId);
        task.setId(taskId);

        return task;
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

    public static TaskRequestDTO fromResponseToRequest(TaskResponseDTO task) {
        return TaskRequestDTO.builder()
                .title(task.getTitle())
                .description(task.getDescription())
                .priority(Priority.valueOf(task.getPriority()))
                .status(Status.valueOf(task.getStatus()))
                .taskDate(LocalDate.parse(task.getTaskDate()))
                .startTime(LocalTime.parse(task.getStartTime()))
                .endTime(LocalTime.parse(task.getEndTime()))
                .build();
    }
}
