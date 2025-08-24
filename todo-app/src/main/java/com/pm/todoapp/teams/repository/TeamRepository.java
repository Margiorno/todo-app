package com.pm.todoapp.teams.repository;

import com.pm.todoapp.core.user.model.User;
import com.pm.todoapp.teams.model.Team;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TeamRepository extends CrudRepository<Team, UUID> {

    Iterable<Team> findByMembersContaining(User member);
}
