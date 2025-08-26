package com.pm.todoapp.domain.user.repository;

import com.pm.todoapp.core.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository("UserReferenceRepository")
public interface UserRepository extends JpaRepository<User, UUID> {}
