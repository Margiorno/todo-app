package com.pm.todoapp.teams.repository;

import com.pm.todoapp.teams.model.Team;
import com.pm.todoapp.users.profile.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TeamRepository extends CrudRepository<Team, UUID> {

    Iterable<Team> findByMembersContaining(User member);
}
