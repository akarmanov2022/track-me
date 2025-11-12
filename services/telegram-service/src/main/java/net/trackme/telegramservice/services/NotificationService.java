package net.trackme.telegramservice.services;

public interface NotificationService {
    void sendMeetingNotHappenedMessage(String teamCardUsername,
                                       String teamCardName,
                                       String streamName,
                                       String meetingLink);
}