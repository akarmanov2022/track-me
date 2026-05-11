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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ImportAutoConfiguration(exclude = {
        MailSenderAutoConfiguration.class,
        MailSenderValidatorAutoConfiguration.class
})
class NotificationServiceImplTest extends AbstractIntegrationTest {

    private static final String TEST_TEAM_NAME = "test team";
    private static final String TEST_STREAM_NAME = "test stream";
    private static final String TEST_MEETING_LINK = "test link";

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AppProperties appProperties;
    @Autowired
    private EmailService emailService;
    @MockitoBean
    private JavaMailSender javaMailSender;

    @BeforeEach
    void setUpUsers() {
        var admin = userRepository.findByUsername("superadmin").stream().findFirst().orElseThrow();
        admin.setEmail("superadmin@superadmin.ru");
        userRepository.save(admin);
    }

    @Test
    void sendMeetingNotHappenedNotification_success() {
        MimeMessage mimeMessage = new JavaMailSenderImpl().createMimeMessage();
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        NotificationService notificationService = new NotificationServiceImpl(
                userRepository, appProperties, emailService);

        notificationService.sendMeetingNotHappenedNotification(
                "tracker", TEST_TEAM_NAME, TEST_STREAM_NAME, TEST_MEETING_LINK, "Петров Петр Петрович");

        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendMeetingNotHappenedNotification_userIsEmpty() {
        MimeMessage mimeMessage = new JavaMailSenderImpl().createMimeMessage();
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        NotificationService notificationService = new NotificationServiceImpl(
                userRepository, appProperties, emailService);

        assertThrows(NoSuchElementException.class,
                () -> notificationService.sendMeetingNotHappenedNotification(
                        "", TEST_TEAM_NAME, TEST_STREAM_NAME, TEST_MEETING_LINK, null));
    }

    @Test
    void sendMeetingNotHappenedNotification_withNullTrackerFullName() {
        MimeMessage mimeMessage = new JavaMailSenderImpl().createMimeMessage();
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        NotificationService notificationService = new NotificationServiceImpl(
                userRepository, appProperties, emailService);

        notificationService.sendMeetingNotHappenedNotification(
                "admin", TEST_TEAM_NAME, TEST_STREAM_NAME, TEST_MEETING_LINK, null);

        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
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
                userRepository, appProperties, emailService);

        notificationService.sendTeamCardSummary(teamCardSummaryEvents);

        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendTeamCardSummary_multipleStreams_sorted() {
        LinkedHashMap<String, String> event1 = new LinkedHashMap<>();
        event1.put("streamName", "Поток A");
        event1.put("teamCardName", "AAA Team");
        event1.put("trackerFullName", "Иванов Иван");
        event1.put("meetingNumber", "1");
        event1.put("meetingLink", "http://example.com/1");

        LinkedHashMap<String, String> event2 = new LinkedHashMap<>();
        event2.put("streamName", "Поток A");
        event2.put("teamCardName", "BBB Team");
        event2.put("trackerFullName", "Петров Петр");
        event2.put("meetingNumber", "2");
        event2.put("meetingLink", "http://example.com/2");

        List<LinkedHashMap<String, String>> events = List.of(event2, event1);

        MimeMessage mimeMessage = new JavaMailSenderImpl().createMimeMessage();
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        NotificationService notificationService = new NotificationServiceImpl(
                userRepository, appProperties, emailService);

        notificationService.sendTeamCardSummary(events);

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
                userRepository, appProperties, emailService);

        notificationService.sendTeamCardLowGradeSummary(teamCardLowGradeSummaryEvents);

        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }
}
