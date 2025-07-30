package com.pm.todoapp.repository;

import com.pm.todoapp.model.Team;
import com.pm.todoapp.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UsersRepository extends CrudRepository<User, UUID> {
}
