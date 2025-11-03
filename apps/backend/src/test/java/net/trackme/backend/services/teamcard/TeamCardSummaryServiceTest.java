package net.trackme.backend.services.teamcard;

import net.trackme.backend.BaseApplicationTest;
import net.trackme.backend.domain.ReadinessLevel;
import net.trackme.backend.domain.Stream;
import net.trackme.backend.domain.TeamCard;
import net.trackme.backend.models.TeamCardStatus;
import net.trackme.backend.repos.StreamRepository;
import net.trackme.backend.repos.TeamCardsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

class TeamCardSummaryServiceTest extends BaseApplicationTest {

    @Autowired
    private TeamCardsRepository teamCardsRepository;

    @Autowired
    private StreamRepository streamRepository;

    @Autowired
    private TeamCardSummaryService teamCardSummaryService;

    @MockitoBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void sendTeamCardsSummary_success() {
        // Arrange
        var stream = streamRepository.save(Stream.builder()
                .name("stream 2")
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(1))
                .build());

        TeamCard teamCard = teamCardsRepository.save(TeamCard.builder()
                .name("test team card")
                .enabled(true)
                .username("tracker")
                .readinessLevel(ReadinessLevel.LEVEL_2)
                .streams(Set.of(stream))
                .status(TeamCardStatus.OK)
                .build());

        LinkedHashMap<String, String> meetingSummaryEvent =
                new LinkedHashMap<>(){{
                    put("teamCardId", String.valueOf(teamCard.getId()));
                    put("meetingNumber", "test meeting");
                    put("meetingLink", "test link");
                }};

        List<LinkedHashMap<String, String>> teamCardSummaryEvents =
                new ArrayList<>(){{
                    add(meetingSummaryEvent);
                }};

        // Act
        teamCardSummaryService.sendTeamCardsSummary(teamCardSummaryEvents);

        // Assert
        verify(kafkaTemplate).send(any(Message.class));
    }
}