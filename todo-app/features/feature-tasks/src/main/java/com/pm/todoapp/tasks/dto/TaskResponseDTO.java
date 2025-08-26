package com.pm.todoapp.tasks.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponseDTO {
    private String id;
    private String title;
    private String description;
    private String priority;
    private String status;
    private TaskTeamDTO team;
    private Set<TaskUserDTO> assignees;
    private String taskDate;
    private String startTime;
    private String endTime;
}
