package com.pm.todoapp.domain.teams.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "TeamReference")
@Table(name = "teams")
public class Team {
    @Id
    private UUID id;
}
