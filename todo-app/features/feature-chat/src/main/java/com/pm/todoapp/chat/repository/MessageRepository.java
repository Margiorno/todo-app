package com.pm.todoapp.chat.repository;

import com.pm.todoapp.domain.chat.model.Message;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MessageRepository extends CrudRepository<Message, UUID> {}
