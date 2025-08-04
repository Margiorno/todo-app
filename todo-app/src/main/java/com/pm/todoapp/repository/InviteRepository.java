package com.pm.todoapp.repository;

import com.pm.todoapp.model.Invite;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface InviteRepository extends CrudRepository<Invite, UUID> {
    boolean existsByCode(String code);
    Optional<Invite> findByCode(String code);
    void deleteByCode(String code);
}
