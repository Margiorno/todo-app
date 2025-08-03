package com.pm.todoapp.service;

import com.pm.todoapp.exceptions.UserNotFoundException;
import com.pm.todoapp.model.User;
import com.pm.todoapp.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UsersService {

    private final UsersRepository usersRepository;

    @Autowired
    public UsersService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    public User findById(UUID userId) {
        return usersRepository.findById(userId).orElseThrow(
                ()->new UserNotFoundException("User with this id does not exist: " + userId.toString())
        );
    }

    public User save(User user) {
        return usersRepository.save(user);
    }

    public User findByEmail(String email) {
        return usersRepository.findByEmail(email).orElseThrow(
                ()-> new UserNotFoundException("User with this email does not exist: " + email)
        );
    }
}
