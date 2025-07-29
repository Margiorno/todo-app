package com.pm.todoapp.repository;

import com.pm.todoapp.model.Team;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TeamRepository extends CrudRepository<Team, UUID> {
}
