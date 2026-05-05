package net.trackme.sso.services.impl;

import jakarta.mail.internet.MimeMessage;
import net.trackme.sso.AbstractIntegrationTest;
import net.trackme.sso.config.AppProperties;
import net.trackme.sso.dao.repository.UserRepository;
import net.trackme.sso.services.EmailService;
import net.trackme.sso.services.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderValidatorAutoConfiguration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ImportAutoConfiguration(exclude = {
        MailSenderAutoConfiguration.class,
        MailSenderValidatorAutoConfiguration.class
})
class NotificationServiceImplTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AppProperties appProperties;
    @Autowired
    private EmailService emailService;
    @MockitoBean
    private JavaMailSender javaMailSender;

    @BeforeEach
    void setUpUsers()
    {
        var admin = userRepository.findByUsername("superadmin").stream().findFirst().orElseThrow();
        admin.setEmail("superadmin@superadmin.ru");
        userRepository.save(admin);
    }

    @Test
    void sendMeetingNotHappenedNotification_success() {
        // Arrange
        String teamCardUsername = "tracker";
        String teamCardName = "test team";
        String streamName = "test stream";
        String meetingLink = "test link";
        String trackerFullName = "Петров Петр Петрович";

        MimeMessage mimeMessage = new JavaMailSenderImpl().createMimeMessage();
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        NotificationService notificationService = new NotificationServiceImpl(
                userRepository,
                appProperties,
                emailService);

        // Act
        notificationService.sendMeetingNotHappenedNotification(
                teamCardUsername,
                teamCardName,
                streamName,
                meetingLink,
                trackerFullName);

        // Assert
        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendMeetingNotHappenedNotification_userIsEmpty() {
        // Arrange
        String teamCardUsername = "";
        String teamCardName = "test team";
        String streamName = "test stream";
        String meetingLink = "test link";
        
        MimeMessage mimeMessage = new JavaMailSenderImpl().createMimeMessage();
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        NotificationService notificationService = new NotificationServiceImpl(
                userRepository,
                appProperties,
                emailService);

        // Act & Assert
        assertThrows(NoSuchElementException.class,
                () -> notificationService.sendMeetingNotHappenedNotification(
                        teamCardUsername,
                        teamCardName,
                        streamName,
                        meetingLink,
                        null));
    }

    @Test
    void sendTeamCardSummary_success() {
        LinkedHashMap<String, String> event1 = new LinkedHashMap<>();
        event1.put("streamName", "Поток 1");
        event1.put("teamCardName", "Команда 1");
        event1.put("trackerFullName", "Иванов Иван Иванович");
        event1.put("meetingNumber", "1");
        event1.put("meetingLink", "http://example.com/meeting/1");

        List<LinkedHashMap<String, String>> teamCardSummaryEvents = List.of(event1);

        MimeMessage mimeMessage = new JavaMailSenderImpl().createMimeMessage();
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        NotificationService notificationService = new NotificationServiceImpl(
                userRepository,
                appProperties,
                emailService);

        notificationService.sendTeamCardSummary(teamCardSummaryEvents);

        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendTeamCardLowGradeSummary_success() {
        LinkedHashMap<String, String> event1 = new LinkedHashMap<>();
        event1.put("streamName", "Поток 1");
        event1.put("teamCardName", "Команда 1");
        event1.put("trackerFullName", "Иванов Иван Иванович");
        event1.put("averageGrade", "0.25");

        List<LinkedHashMap<String, String>> teamCardLowGradeSummaryEvents = List.of(event1);

        MimeMessage mimeMessage = new JavaMailSenderImpl().createMimeMessage();
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        NotificationService notificationService = new NotificationServiceImpl(
                userRepository,
                appProperties,
                emailService);

        notificationService.sendTeamCardLowGradeSummary(teamCardLowGradeSummaryEvents);

        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }
}