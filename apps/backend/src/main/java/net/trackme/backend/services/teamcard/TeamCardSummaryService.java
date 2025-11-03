package net.trackme.backend.services.teamcard;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.trackme.backend.domain.Stream;
import net.trackme.backend.messaging.TeamCardSummaryEvent;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamCardSummaryService {

    private final TeamCardsService teamCardsService;

    private final TeamCardEventsProducer teamCardEventsProducer;

    public void sendTeamCardsSummary(
            List<LinkedHashMap<String, String>> meetingSummaryEvents) {
        List<TeamCardSummaryEvent> teamCardSummaryEvents = new ArrayList<>();

        for (var meetingSummaryEvent : meetingSummaryEvents) {
            var teamCard = teamCardsService
                    .getTeamCard(UUID.fromString(meetingSummaryEvent.get("teamCardId")));

            var streamName = teamCard.getStreams().stream()
                    .filter(Stream::isActive).map(Stream::getName)
                    .findFirst().orElseThrow();

            TeamCardSummaryEvent teamCardSummaryEvent =
                    TeamCardSummaryEvent.builder()
                            .teamCardName(teamCard.getName())
                            .streamName(streamName)
                            .meetingNumber(meetingSummaryEvent.get("meetingNumber"))
                            .meetingLink(meetingSummaryEvent.get("meetingLink"))
                            .build();

            teamCardSummaryEvents.add(teamCardSummaryEvent);
        }

        teamCardEventsProducer.sendTeamCardSummaryEvent(teamCardSummaryEvents);
    }
}
