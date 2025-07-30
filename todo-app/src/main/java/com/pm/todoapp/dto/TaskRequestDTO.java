package com.pm.todoapp.dto;

import com.pm.todoapp.model.Priority;
import com.pm.todoapp.model.Status;
import com.pm.todoapp.model.Team;
import com.pm.todoapp.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequestDTO {

    @NotBlank
    private String title;

    private String description;

    @NotNull
    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    @NotNull
    @Builder.Default
    private Status status = Status.TODO;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Builder.Default
    private LocalDate taskDate = LocalDate.now();

    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    @Builder.Default
    private LocalTime startTime = LocalTime.of(8, 0);

    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    @Builder.Default
    private LocalTime endTime = LocalTime.of(16, 0);
}
