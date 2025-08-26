package com.pm.todoapp.notifications.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FriendRequestUserDTO {
    private String id;
    private String profilePicturePath;
    private String firstName;
    private String lastName;
}
