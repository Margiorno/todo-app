package com.pm.todoapp.repository;

import com.pm.todoapp.model.FriendRequestNotification;
import com.pm.todoapp.model.Invite;
import com.pm.todoapp.model.Notification;
import com.pm.todoapp.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends CrudRepository<Notification, UUID> {
    Iterable<Notification> findAllByReceiver(User user);
    Optional<FriendRequestNotification> findFriendRequestNotificationById(UUID id);
}
