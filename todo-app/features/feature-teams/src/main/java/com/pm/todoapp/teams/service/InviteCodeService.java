package com.pm.todoapp.teams.service;

import com.pm.todoapp.common.exceptions.InvalidTeamInviteException;
import com.pm.todoapp.teams.model.Invite;
import com.pm.todoapp.teams.model.Team;
import com.pm.todoapp.teams.repository.TeamInviteRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class InviteCodeService {
    private final TeamInviteRepository teamInviteRepository;

    public Invite createAndSaveInvite(Team team) {
        Invite invite = new Invite();
        invite.setTeam(team);
        invite.setCode(generateInvitationCode());
        invite.setExpirationDate(LocalDateTime.now().plusMinutes(5));
        return teamInviteRepository.save(invite);
    }


    public Team resolveInvitationCode(String invitationCode) {
        Invite invite = teamInviteRepository.findByCode(invitationCode).orElseThrow(
                ()->new InvalidTeamInviteException("There is no invitation with this code")
        );
        if(invite.getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new InvalidTeamInviteException("Invitation has expired");
        }

        teamInviteRepository.delete(invite);

        return invite.getTeam();
    }

    private String generateInvitationCode() {
        String generatedCode;
        do{
            RandomStringUtils generator = RandomStringUtils.insecure();
            generatedCode = generator.next(6, 'A', 'Z' + 1, false, false);
        } while (teamInviteRepository.existsByCode(generatedCode));

        return generatedCode;
    }
}
