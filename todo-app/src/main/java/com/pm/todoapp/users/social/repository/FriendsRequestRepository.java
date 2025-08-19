package com.pm.todoapp.users.social.repository;

import com.pm.todoapp.users.profile.model.FriendRequest;
import com.pm.todoapp.users.profile.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FriendsRequestRepository extends CrudRepository<FriendRequest, UUID> {
    boolean existsBySenderAndReceiver(User sender, User receiver);
    Optional<FriendRequest> findBySenderAndReceiver(User sender, User receiver);
}
