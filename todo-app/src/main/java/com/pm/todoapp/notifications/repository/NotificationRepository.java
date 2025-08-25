package com.pm.todoapp.notifications.repository;

import com.pm.todoapp.core.user.model.User;
import com.pm.todoapp.notifications.model.FriendRequestNotification;
import com.pm.todoapp.notifications.model.Notification;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends CrudRepository<Notification, UUID> {
    Iterable<Notification> findAllByReceiver(User user);
    Optional<FriendRequestNotification> findFriendRequestNotificationById(UUID id);
    Optional<FriendRequestNotification> findNotificationByRequestId(UUID id);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.receiver.id = :userId")
    int markAllReadByUserId(@Param("userId") UUID userId);
}
