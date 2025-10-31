package net.trackme.sso.services;

public interface NotificationService {
    void sendMeetingNotHappenedNotification(String teamCardUsername,
                                            String teamCardName,
                                            String streamName,
                                            String meetingLink);
}
