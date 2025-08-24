package com.pm.todoapp.tasks.mapper;

import com.pm.todoapp.core.user.model.User;
import com.pm.todoapp.tasks.dto.TaskRequestDTO;
import com.pm.todoapp.tasks.dto.TaskResponseDTO;
import com.pm.todoapp.tasks.model.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class TaskMapper {

    private final TaskUserMapper taskUserConverter;

    @Autowired
    public TaskMapper(TaskUserMapper taskUserConverter) {
        this.taskUserConverter = taskUserConverter;
    }

    public static Task toEntity(TaskRequestDTO dto, Set<User> users) {

        return Task.builder()
                .title(dto.getTitle())
                .assignees(users)
                .description(dto.getDescription())
                .priority(dto.getPriority())
                .status(dto.getStatus())
                .taskDate(dto.getTaskDate())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .build();
    }

    public static Task toEntity(TaskRequestDTO dto, Set<User> users, UUID taskId) {
        Task task = toEntity(dto, users);
        task.setId(taskId);

        return task;
    }

    public TaskResponseDTO toResponseDTO(Task task) {
        return TaskResponseDTO.builder()
                .id(task.getId().toString())
                .title(task.getTitle())
                .description(task.getDescription())
                .priority(task.getPriority().name())
                .assignees(task.getAssignees().stream().map(taskUserConverter::toDTO)
                                .collect(Collectors.toSet()))
                .team(task.getTeam() == null ? null : TaskTeamMapper.toDTO(task.getTeam()))
                .status(task.getStatus() != null ? task.getStatus().name() : null)
                .taskDate(task.getTaskDate() != null ? task.getTaskDate().toString() : null)
                .startTime(task.getStartTime() != null ? task.getStartTime().toString() : null)
                .endTime(task.getEndTime() != null ? task.getEndTime().toString() : null)
                .build();
    }
}
