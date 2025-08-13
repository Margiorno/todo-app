package com.pm.todoapp.repository;

import com.pm.todoapp.model.Invite;
import com.pm.todoapp.model.Notification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends CrudRepository<Notification, UUID> {
}
