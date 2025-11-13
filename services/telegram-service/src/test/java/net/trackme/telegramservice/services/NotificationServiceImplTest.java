package net.trackme.telegramservice.services;

import net.trackme.telegramservice.AbstractIntegrationTest;
import net.trackme.telegramservice.configuration.MessageTemplates;
import net.trackme.telegramservice.configuration.NotificationBot;
import net.trackme.telegramservice.dao.ChatRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.Mockito.verify;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class NotificationServiceImplTest extends AbstractIntegrationTest {

    @Autowired
    private ChatService chatService;

    @Autowired
    private ChatRepository chatRepository;

    @Mock
    private NotificationBot notificationBot;

    @Test
    void sendMeetingNotHappenedMessage_success() {
        // Arrange
        Long chatId = 1L;
        String teamCardUsername = "test username";
        String teamCardName = "test team";
        String streamName = "test stream";
        String meetingLink = "test link";

        chatService.createChat(chatId, teamCardUsername);

        var message = MessageTemplates.MEETING_NOT_HAPPENED_MESSAGE_TEMPLATE
                .replace("{teamCardName}", teamCardName)
                .replace("{streamName}", streamName)
                .replace("{meetingLink}", meetingLink);

        NotificationService notificationService = new NotificationServiceImpl(notificationBot, chatRepository);

        // Act
        notificationService.sendMeetingNotHappenedMessage(
                teamCardUsername,
                teamCardName,
                streamName,
                meetingLink);

        // Assert
        verify(notificationBot).sendMessage(chatId, message);
    }
}