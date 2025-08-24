package com.pm.todoapp.users.adapter;

import com.pm.todoapp.core.user.dto.UserDTO;
import com.pm.todoapp.core.user.port.UserProviderPort;
import com.pm.todoapp.users.profile.model.User;
import com.pm.todoapp.users.profile.service.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class UserProviderAdapter implements UserProviderPort {
    private final UsersService usersService;


    @Override
    public UserDTO getUserById(UUID userId) {

        User user = usersService.findRawById(userId);

        return mapToDTO(user);
    }

    @Override
    public Set<UserDTO> getUsersByIds(Set<UUID> participantsIds) {
        Set<User> users = participantsIds
                .stream()
                .map(usersService::findRawById)
                .collect(Collectors.toSet());

        return users.stream()
                .map(UserProviderAdapter::mapToDTO)
                .collect(Collectors.toSet());
    }

    @Override
    public String getUserName(UUID senderId) {

        User user = usersService.findRawById(senderId);
        return "%s %s".formatted(user.getFirstName(), user.getLastName());
    }

    private static UserDTO mapToDTO(User user) {
        return UserDTO.builder().
                id(user.getId())
                .email(user.getEmail())
                .profilePicturePath(user.getProfilePicturePath())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .dateOfBirth(user.getDateOfBirth())
                .gender(user.getGender())
                .build();
    }
}
