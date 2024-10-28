package net.akarmanov.projectplace.services.meeting;

import net.akarmanov.projectplace.rest.api.meeting.MeetingCreateDto;
import net.akarmanov.projectplace.rest.api.meeting.MeetingDto;

import java.util.UUID;

public interface MeetingService {
    MeetingDto createMeeting(UUID teamCardId, MeetingCreateDto meetingCreateDto);
}
