package net.trackme.sso.messaging;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.trackme.sso.services.NotificationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TeamCardEventsListener {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "meeting-not-happened",
            containerFactory = "meetingNotHappenedListenerContainerFactory")
    public void onMeetingNotHappenedEvent(
            ConsumerRecord<String, MeetingNotHappenedEvent> record) {
        var meetingNotHappenedEvent = record.value();
        log.info("Received meeting not happened event: {}", meetingNotHappenedEvent);
        notificationService.sendMeetingNotHappenedNotification(
                meetingNotHappenedEvent.teamCardUsername(),
                meetingNotHappenedEvent.teamCardName(),
                meetingNotHappenedEvent.streamName(),
                meetingNotHappenedEvent.meetingLink());
    }

    @KafkaListener(
            topics = "team-card-summary",
            containerFactory = "teamCardSummaryListenerContainerFactory")
    public void onTeamCardSummaryEvent(
            ConsumerRecord<String, List<LinkedHashMap<String, String>>> record) {
        var teamCardSummaryEvents = record.value();
        log.info("Received team card summary event: {}", teamCardSummaryEvents);
        notificationService.sendTeamCardSummary(teamCardSummaryEvents);
    }
}
