package net.trackme.sso.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.trackme.sso.config.AppProperties;
import net.trackme.sso.dao.entity.UserEntity;
import net.trackme.sso.dao.repository.UserRepository;
import net.trackme.sso.services.EmailService;
import net.trackme.sso.services.NotificationService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final UserRepository userRepository;

    private final AppProperties appProperties;

    private final EmailService emailService;

    @Override
    public void sendMeetingNotHappenedNotification(String teamCardUsername,
                                                   String teamCardName,
                                                   String streamName,
                                                   String meetingLink)
    {
        var emailTo = userRepository.findByUsername(teamCardUsername)
                .map(UserEntity::getEmail)
                .orElseThrow();

        emailService.sendMail(
                emailTo,
                appProperties.getMail().getFrom(),
                "[" + appProperties.getMail().getSubject() + "] Пропущена встреча",
                "email-meeting-not-happened.html",
                Map.of(
                        "email", emailTo,
                        "appName", appProperties.getMail().getSubject(),
                        "supportEmail", appProperties.getMail().getFrom(),
                        "teamCardName", teamCardName,
                        "streamName", streamName,
                        "meetingLink", meetingLink));
    }
}
