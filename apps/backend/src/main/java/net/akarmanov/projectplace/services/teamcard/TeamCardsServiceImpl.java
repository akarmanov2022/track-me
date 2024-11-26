package net.akarmanov.projectplace.services.teamcard;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.akarmanov.projectplace.domain.TeamCard;
import net.akarmanov.projectplace.repos.TeamCardsRepository;
import net.akarmanov.projectplace.services.exceptions.TeamCardNotFoundException;
import net.akarmanov.projectplace.services.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static net.akarmanov.projectplace.domain.spec.TeamCardSpecification.*;
import static org.springframework.data.jpa.domain.Specification.where;

@Service
@RequiredArgsConstructor
public class TeamCardsServiceImpl implements TeamCardsService {

    private final TeamCardsRepository teamCardsRepository;

    private final UserService userService;

    private final MutableAclService aclService;

    @Override
    @Transactional
    public TeamCard createTeamCard(TeamCard createTeamCard) {
        createTeamCard = teamCardsRepository.save(createTeamCard);
        aclService.createAcl(createTeamCard);
        return createTeamCard;
    }

    @Override
    @Transactional
    @PreAuthorize("hasPermission(#teamCardId, 'net.akarmanov.projectplace.domain.TeamCard', 'WRITE')")
    public TeamCard updateTeamCard(UUID teamCardId, TeamCard teamCardDto) {
        var teamCard = get(teamCardId);
        updateTeamCard(teamCardDto, teamCard);
        teamCard = teamCardsRepository.save(teamCard);
        return teamCardsRepository.save(teamCard);
    }

    private TeamCard get(UUID teamCardId) {
        return teamCardsRepository.findById(teamCardId)
                .orElseThrow(() -> new TeamCardNotFoundException(teamCardId));
    }

    @Override
    public Page<TeamCard> getTeamCards(String name, String status, Pageable pageable, UUID userId) {
        return teamCardsRepository.findAll(
                where(nameLike(name).or(statusEquals(status)))
                        .and(userEquals(userId)), pageable);
    }

    @Override
    @PostAuthorize("hasPermission(#id, 'net.akarmanov.projectplace.domain.TeamCard', 'READ')")
    public TeamCard getTeamCard(UUID id) {
        return teamCardsRepository.findById(id)
                .orElseThrow(() -> new TeamCardNotFoundException(id));
    }

    @Override
    @Transactional
    @PreAuthorize("hasPermission(#id, 'net.akarmanov.projectplace.domain.TeamCard', 'DELETE')")
    public void deleteTeamCard(UUID id) {
        var user = userService.getCurrentUser();
        var teamCard = teamCardsRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new TeamCardNotFoundException(id));
        teamCardsRepository.delete(teamCard);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public TeamCard createTeamCard(TeamCard create, UUID userId) {
        var user = userService.getUser(userId);
        create.setUser(user);
        create = teamCardsRepository.save(create);
        aclService.createAcl(create);
        return create;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public TeamCard updateTeamCard(UUID teamCardId, TeamCard teamCardDto, UUID userId) {
        var teamCard = get(teamCardId, userId);
        var acl = aclService.readAclById(teamCard);
        updateTeamCard(teamCardDto, teamCard);
        teamCard.setUser(userService.getUser(userId));
        teamCard = teamCardsRepository.save(teamCard);
        aclService.updateAcl((MutableAcl) acl);
        return teamCard;
    }

    @Override
    public Page<TeamCard> findAll(String name, String status, Pageable pageable) {
        return teamCardsRepository.findAll(
                where(nameLike(name).or(statusEquals(status))), pageable);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public TeamCard getTeamCard(UUID id, UUID userId) {
        return get(id, userId);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteTeamCard(UUID id, UUID userId) {
        teamCardsRepository.deleteByIdAndUserId(id, userId);
    }

    private TeamCard get(UUID teamCardId, UUID userId) {
        return teamCardsRepository.findByIdAndUserId(teamCardId, userId)
                .orElseThrow(() -> new TeamCardNotFoundException(teamCardId, userId));
    }

    private void updateTeamCard(TeamCard source, TeamCard target) {
        if (source.getName() != null) {
            target.setName(source.getName());
        }
        if (source.getDescription() != null) {
            target.setDescription(source.getDescription());
        }
        if (source.getStatus() != null) {
            target.setStatus(source.getStatus());
        }
    }
}
