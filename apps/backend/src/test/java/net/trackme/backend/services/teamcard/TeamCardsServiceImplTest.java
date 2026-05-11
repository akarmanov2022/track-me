package net.trackme.backend.services.teamcard;

import net.trackme.backend.BaseApplicationTest;
import net.trackme.backend.domain.ReadinessLevel;
import net.trackme.backend.domain.TeamCard;
import net.trackme.backend.models.TeamCardStatus;
import net.trackme.backend.repos.NtiMarketRepository;
import net.trackme.backend.repos.TeamCardsRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TeamCardsServiceImplTest extends BaseApplicationTest {

    @Autowired
    private TeamCardsService teamCardsService;

    @Autowired
    private TeamCardsRepository teamCardsRepository;

    @Autowired
    private NtiMarketRepository ntiMarketRepository;

    @AfterEach
    void tearDown() {
        teamCardsRepository.deleteAll();
    }

    @Test
    @WithMockUser(value = BaseApplicationTest.USER, roles = "ADMIN")
    void getTeamCardsByUser_shouldReturnTeamsForUser() {
        var ntiMarket = ntiMarketRepository.findAll().getFirst();
        
        teamCardsService.createTeamCard(
                TeamCard.builder()
                        .status(TeamCardStatus.OK)
                        .ntiMarkets(List.of(ntiMarket))
                        .name("Team 1")
                        .readinessLevel(ReadinessLevel.LEVEL_1)
                        .meetingRoomLink("link1@test.com")
                        .build(),
                "user1");

        teamCardsService.createTeamCard(
                TeamCard.builder()
                        .status(TeamCardStatus.OK)
                        .ntiMarkets(List.of(ntiMarket))
                        .name("Team 2")
                        .readinessLevel(ReadinessLevel.LEVEL_1)
                        .meetingRoomLink("link2@test.com")
                        .build(),
                "user2");

        var user1Teams = teamCardsService.getTeamCardsByUser("user1");
        assertThat(user1Teams).hasSize(1);
        assertThat(user1Teams.get(0).getName()).isEqualTo("Team 1");

        var user2Teams = teamCardsService.getTeamCardsByUser("user2");
        assertThat(user2Teams).hasSize(1);
        assertThat(user2Teams.get(0).getName()).isEqualTo("Team 2");
    }

    @Test
    @WithMockUser(value = BaseApplicationTest.USER, roles = "ADMIN")
    void reassignTeams_shouldTransferAllTeamsToNewUser() {
        var ntiMarket = ntiMarketRepository.findAll().getFirst();
        
        teamCardsService.createTeamCard(
                TeamCard.builder()
                        .status(TeamCardStatus.OK)
                        .ntiMarkets(List.of(ntiMarket))
                        .name("Team A")
                        .readinessLevel(ReadinessLevel.LEVEL_1)
                        .meetingRoomLink("linkA@test.com")
                        .build(),
                "olduser");

        teamCardsService.createTeamCard(
                TeamCard.builder()
                        .status(TeamCardStatus.OK)
                        .ntiMarkets(List.of(ntiMarket))
                        .name("Team B")
                        .readinessLevel(ReadinessLevel.LEVEL_1)
                        .meetingRoomLink("linkB@test.com")
                        .build(),
                "olduser");

        teamCardsService.reassignTeams("olduser", "newuser");

        var oldUserTeams = teamCardsService.getTeamCardsByUser("olduser");
        assertThat(oldUserTeams).isEmpty();

        var newUserTeams = teamCardsService.getTeamCardsByUser("newuser");
        assertThat(newUserTeams).hasSize(2);
        assertThat(newUserTeams).extracting(TeamCard::getName)
                .containsExactlyInAnyOrder("Team A", "Team B");
    }

    @Test
    @WithMockUser(value = BaseApplicationTest.USER, roles = "ADMIN")
    void getTeamCardNamesByUser_shouldReturnOnlyNames() {
        var ntiMarket = ntiMarketRepository.findAll().getFirst();
        
        teamCardsService.createTeamCard(
                TeamCard.builder()
                        .status(TeamCardStatus.OK)
                        .ntiMarkets(List.of(ntiMarket))
                        .name("Team X")
                        .readinessLevel(ReadinessLevel.LEVEL_1)
                        .meetingRoomLink("linkX@test.com")
                        .build(),
                "userX");

        var names = teamCardsService.getTeamCardNamesByUser("userX");
        assertThat(names).containsExactly("Team X");
    }
}