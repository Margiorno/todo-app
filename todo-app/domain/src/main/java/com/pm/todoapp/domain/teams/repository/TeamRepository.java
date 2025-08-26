package com.pm.todoapp.domain.teams.repository;

import com.pm.todoapp.domain.teams.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository("TeamReferenceRepository")
public interface TeamRepository extends JpaRepository<Team, UUID> {}
