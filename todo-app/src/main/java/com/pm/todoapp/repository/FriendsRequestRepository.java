package com.pm.todoapp.repository;

import com.pm.todoapp.model.Conversation;
import com.pm.todoapp.model.FriendRequest;
import com.pm.todoapp.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FriendsRequestRepository extends CrudRepository<FriendRequest, UUID> {

}
