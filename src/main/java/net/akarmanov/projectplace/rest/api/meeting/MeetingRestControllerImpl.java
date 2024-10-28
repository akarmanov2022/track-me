package net.akarmanov.projectplace.rest.api.meeting;

import lombok.RequiredArgsConstructor;
import net.akarmanov.projectplace.services.meeting.MeetingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class MeetingRestControllerImpl implements MeetingRestController {

    private final MeetingService meetingService;

    @Override
    public ResponseEntity<MeetingDto> createMeeting(UUID teamCardId,
                                                    MeetingCreateDto meetingCreateDto) {
        var meeting = meetingService.createMeeting(teamCardId, meetingCreateDto);
        return ResponseEntity.ok(meeting);
    }
}
