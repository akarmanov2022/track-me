package net.trackme.sso.messaging;

import net.trackme.sso.AbstractIntegrationTest;
import net.trackme.sso.services.NotificationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamCardEventsListenerTest extends AbstractIntegrationTest {

    @Mock
    private ConsumerRecord<String, MeetingNotHappenedEvent> record;

    @Autowired
    private TeamCardEventsListener teamCardEventsListener;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void onMeetingNotHappenedEvent() {
        // Arrange
        var teamCardUsername = "test username";
        var teamCardName = "test team card";
        var streamName = "test stream";
        var meetingLink = "test link";
        MeetingNotHappenedEvent event = new MeetingNotHappenedEvent(
                teamCardUsername,
                teamCardName,
                streamName,
                meetingLink
        );
        when(record.value()).thenReturn(event);

        // Act
        teamCardEventsListener.onMeetingNotHappenedEvent(record);

        // Assert
        verify(notificationService).sendMeetingNotHappenedNotification(
                teamCardUsername,
                teamCardName,
                streamName,
                meetingLink);
    }
}