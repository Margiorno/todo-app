package com.pm.todoapp.users.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private String id;
    private String email;
    private String profilePicturePath;
    private String firstName;
    private String lastName;
    private String dateOfBirth;
    private String gender;
}
