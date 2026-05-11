package net.trackme.telegramservice.messaging;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import net.trackme.telegramservice.AbstractIntegrationTest;
import net.trackme.telegramservice.services.NotificationService;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
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
        MeetingNotHappenedEvent event = new MeetingNotHappenedEvent(
                "test username",
                "test team card",
                "test stream",
                "test link",
                "Петров Петр Петрович"
        );

        when(record.value()).thenReturn(event);

        // Act
        teamCardEventsListener.onMeetingNotHappenedEvent(record);

        // Assert
        verify(notificationService).sendMeetingNotHappenedMessage(
                "test username",
                "test team card",
                "test stream",
                "test link",
                "Петров Петр Петрович");
    }
}