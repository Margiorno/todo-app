package com.pm.todoapp.domain.user.model;

import com.pm.todoapp.domain.chat.model.Conversation;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "User")
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


    //TODO FULL MOVING CONVERSATION MANAGEMENT TO CONVERSATION ENTITY
    @ManyToMany(mappedBy = "participants")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Builder.Default
    private Set<Conversation> conversations = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "user_friends",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "friend_id")
    )
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Builder.Default
    private Set<User> friends = new HashSet<>();



    public void addFriend(User friend) {
        this.friends.add(friend);
        friend.getFriends().add(this);
    }

    public void removeFriend(User friend) {
        this.friends.remove(friend);
        friend.getFriends().remove(this);
    }

}
