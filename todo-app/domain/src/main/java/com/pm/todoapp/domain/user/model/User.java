package com.pm.todoapp.domain.user.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "UserReference")
@Table(name = "users")
public class User {

    @Id
    private UUID id;
}
