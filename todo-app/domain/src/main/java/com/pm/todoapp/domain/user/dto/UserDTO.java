package com.pm.todoapp.domain.user.dto;

import com.pm.todoapp.domain.user.model.Gender;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private UUID id;
    private String email;
    private String profilePicturePath;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private Gender gender;
}
