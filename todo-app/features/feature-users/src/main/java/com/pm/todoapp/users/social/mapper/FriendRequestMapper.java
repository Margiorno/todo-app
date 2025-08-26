package com.pm.todoapp.users.social.mapper;

import com.pm.todoapp.users.social.dto.FriendRequestDTO;
import com.pm.todoapp.domain.user.model.FriendRequest;


public class FriendRequestMapper {
    public static FriendRequestDTO toDTO(FriendRequest friendRequest) {

        return FriendRequestDTO.builder()
                .id(friendRequest.getId())
                .senderId(friendRequest.getSender().getId())
                .receiverId(friendRequest.getReceiver().getId())
                .build();
    }

}
