package com.pm.todoapp.model;


import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.*;

@Data
@Entity
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToMany
    @JoinTable(
            name = "conversation_participant",
            joinColumns = @JoinColumn(name = "conversation_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @EqualsAndHashCode.Exclude
    private Set<User> participants = new HashSet<>();

    @OneToMany(
            mappedBy = "conversation",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @EqualsAndHashCode.Exclude
    private List<Message> messages = new ArrayList<>();

    public void addMessage(Message message) {
        messages.add(message);
        message.setConversation(this);
    }
}