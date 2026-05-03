package net.trackme.sso.services.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.trackme.sso.config.AppProperties;
import net.trackme.sso.dao.entity.UserEntity;
import net.trackme.sso.dao.repository.UserRepository;
import net.trackme.sso.services.EmailService;
import net.trackme.sso.services.NotificationService;

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
                                                   String meetingLink,
                                                   String trackerFullName) {
        var emailTo = userRepository.findByUsername(teamCardUsername)
                .orElseThrow();

        String fullName = (trackerFullName != null && !trackerFullName.isBlank()) 
        ? getShortName(trackerFullName) 
        : emailTo.getFullName();        

        emailService.sendMail(
                emailTo.getEmail(),
                appProperties.getMail().getFrom(),
                "[" + appProperties.getMail().getSubject() + "] Пропущена встреча",
                "email-meeting-not-happened.html",
                Map.of(
                        "email", emailTo,
                        "fullName", fullName,
                        "appName", appProperties.getMail().getSubject(),
                        "supportEmail", appProperties.getMail().getFrom(),
                        "teamCardName", teamCardName,
                        "streamName", streamName,
                        "meetingLink", meetingLink));
    }

    @Override
    public void sendTeamCardSummary(
            List<LinkedHashMap<String, String>> teamCardSummaryEvents) {
        int count = 1;
        List<String> infos = new ArrayList<>();
        for (var teamCardSummaryEvent : teamCardSummaryEvents) {
            String trackerInfo = getShortName(teamCardSummaryEvent.getOrDefault("trackerFullName", "Не назначен"));
            
            var info = String.format("%d. Поток: %s, Команда: %s, Трекер: %s, Встреча: %s<br>Ссылка на встречу: %s",
                    count,
                    teamCardSummaryEvent.get("streamName"),
                    teamCardSummaryEvent.get("teamCardName"),
                    trackerInfo,
                    teamCardSummaryEvent.get("meetingNumber"),
                    teamCardSummaryEvent.get("meetingLink"));
            count++;
            infos.add(info);
        }

        String summary = String.join("<br><br>", infos);

        sendMessageToAnyEmails(
                "[" + appProperties.getMail().getSubject() + "] Сводка по пропущенным встречам",
                "email-not-happened-meetings-summary.html",
                summary);
    }

    @Override
    public void sendTeamCardLowGradeSummary(
            List<LinkedHashMap<String, String>> teamCardLowGradeSummaryEvents) {
        int count = 1;
        List<String> infos = new ArrayList<>();
        for (var teamCardSummaryEvent : teamCardLowGradeSummaryEvents) {
            String trackerInfo = getShortName(teamCardSummaryEvent.getOrDefault("trackerFullName", "Не назначен"));
            
            var info = String.format("%d. Поток: %s, Команда: %s, Трекер: %s, Рейтинг: %s",
                    count,
                    teamCardSummaryEvent.get("streamName"),
                    teamCardSummaryEvent.get("teamCardName"),
                    trackerInfo,
                    teamCardSummaryEvent.get("averageGrade"));
            count++;
            infos.add(info);
        }

        String summary = String.join("<br><br>", infos);

        sendMessageToAnyEmails(
                "[" + appProperties.getMail().getSubject() + "] Сводка по командам с низким рейтингом",
                "email-team-card-low-grade-summary.html",
                summary);
    }

    private void sendMessageToAnyEmails(String subject, String templateName, String summary) {
        var emailTos = userRepository.findAll()
                .stream()
                .filter(userEntity -> userEntity
                        .getRoles()
                        .stream()
                        .anyMatch(roleEntity -> appProperties
                                .getMail()
                                 .getSummarySendRoles()
                                .contains(roleEntity.getCode())))
                .filter(UserEntity::getActive)
                .toList();


        if (emailTos.isEmpty()) {
            return;
        }
        for (var emailTo : emailTos) {
            emailService.sendMail(
                    emailTo.getEmail(),
                    appProperties.getMail().getFrom(),
                    subject,
                    templateName,
                    Map.of(
                            "email", emailTo.getEmail(),
                            "fullName", emailTo.getFullName(),
                            "appName", appProperties.getMail().getSubject(),
                            "supportEmail", appProperties.getMail().getFrom(),
                            "summary", summary));
        }
    }

    private String getShortName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "Не назначен";
        }
        String[] parts = fullName.trim().split(" ");
        if (parts.length >= 2) {
            return parts[0] + " " + parts[1];
        }
        return fullName;
    }
}