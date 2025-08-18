package com.pm.todoapp.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "notification_type")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String message;

    private LocalDateTime createdAt;

    private boolean isRead;

    @PrePersist
    protected void prePersist() {
        createdAt = LocalDateTime.now();
        isRead = false;
    }


}


