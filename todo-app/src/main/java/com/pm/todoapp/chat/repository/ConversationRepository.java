package com.pm.todoapp.chat.repository;

import com.pm.todoapp.chat.model.Conversation;
import com.pm.todoapp.core.user.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends CrudRepository<Conversation, UUID> {
    Iterable<Conversation> findByParticipantsContains(User user);

    @Query("SELECT c " +
            "FROM Conversation c " +
            "WHERE c.conversationType = 'PRIVATE' " +
            "AND SIZE(c.participants) = 2 " +
            "AND :user1 MEMBER OF c.participants " +
            "AND :user2 MEMBER OF c.participants")
    Optional<Conversation> findPrivateConversationBetweenUsers(@Param("user1") User user1,
                                                               @Param("user2") User user2);
}
