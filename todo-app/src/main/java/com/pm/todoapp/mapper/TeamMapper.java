package com.pm.todoapp.mapper;

import com.pm.todoapp.dto.TeamResponseDTO;
import com.pm.todoapp.model.Team;


public class TeamMapper {
    public static TeamResponseDTO toResponseDTO(Team team) {
        return TeamResponseDTO.builder()
                .id(team.getId().toString())
                .name(team.getName())
                .members(team.getMembers().stream().map(
                        member -> member.getId().toString()
                ).toList()).build();
    }


}
