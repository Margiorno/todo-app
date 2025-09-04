package com.pm.todoapp.users.profile.repository;

import com.pm.todoapp.users.profile.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

//TODO DIVIDE AUTH/PROFILE/SOCIAL
@Repository
public interface UsersRepository extends CrudRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    boolean existsByIdAndFriendsId(UUID id, UUID friendsId);
    default boolean areFriends(UUID userA, UUID userB) {
        return existsByIdAndFriendsId(userA, userB) || existsByIdAndFriendsId(userB, userA);
    }
}
