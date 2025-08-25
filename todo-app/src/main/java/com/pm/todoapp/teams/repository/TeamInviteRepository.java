package com.pm.todoapp.teams.repository;

import com.pm.todoapp.teams.model.Invite;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamInviteRepository extends CrudRepository<Invite, UUID> {
    boolean existsByCode(String code);
    Optional<Invite> findByCode(String code);
}
