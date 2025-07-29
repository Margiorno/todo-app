package com.pm.todoapp.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToMany
    @JoinTable(
            name = "user_team",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "team_id")
    )
    @EqualsAndHashCode.Exclude
    private Set<Team> teams = new HashSet<>();

    @ManyToMany(mappedBy = "assignees")
    @EqualsAndHashCode.Exclude
    private Set<Task> tasks = new HashSet<>();

    //TODO FINISH USER
}
