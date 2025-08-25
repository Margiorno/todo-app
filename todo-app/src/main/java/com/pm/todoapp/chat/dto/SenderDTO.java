package com.pm.todoapp.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SenderDTO {
    private String id;
    //private String email;
    private String profilePicturePath;
    private String firstName;
    private String lastName;
    //private String dateOfBirth;
    //private String gender;
}
