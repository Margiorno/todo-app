package com.pm.todoapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    private String profilePicturePath;

    private String firstName;

    private String lastName;

    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Gender gender;

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

    @ManyToMany(mappedBy = "participants")
    @EqualsAndHashCode.Exclude
    private Set<Conversation> conversations = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "user_friends",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "friend_id")
    )
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<User> friends = new HashSet<>();

}
