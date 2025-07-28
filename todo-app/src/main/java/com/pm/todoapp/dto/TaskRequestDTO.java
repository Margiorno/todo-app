package com.pm.todoapp.dto;

import com.pm.todoapp.model.Priority;
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

    private String status;

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
