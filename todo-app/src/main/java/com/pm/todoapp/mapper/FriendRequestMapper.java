package com.pm.todoapp.mapper;

import com.pm.todoapp.dto.FriendRequestDTO;
import com.pm.todoapp.dto.TeamInviteResponseDTO;
import com.pm.todoapp.model.FriendRequest;
import com.pm.todoapp.model.Invite;


public class FriendRequestMapper {
    public static FriendRequestDTO toDTO(FriendRequest friendRequest) {

        return FriendRequestDTO.builder()
                .id(friendRequest.getId())
                .senderId(friendRequest.getSender().getId())
                .receiverId(friendRequest.getReceiver().getId())
                .status(friendRequest.getStatus())
                .build();
    }

}
