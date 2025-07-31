package com.pm.todoapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Email
    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

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
