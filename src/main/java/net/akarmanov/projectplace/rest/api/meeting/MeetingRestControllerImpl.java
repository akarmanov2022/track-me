package net.akarmanov.projectplace.rest.api.meeting;

import lombok.RequiredArgsConstructor;
import net.akarmanov.projectplace.services.meeting.MeetingService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
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

    @Override
    public ResponseEntity<PagedModel<MeetingDto>> getMeetings(Pageable pageable) {
        var meetings = meetingService.getMeetingsForCurrentUser(pageable);
        return ResponseEntity.ok(new PagedModel<>(meetings));
    }

    @Override
    public ResponseEntity<MeetingDto> updateMeeting(UUID meetingId, UUID teamCardId,
                                                    MeetingUpdateDto meetingCreateDto) {
        var meeting = meetingService.updateMeeting(meetingId, teamCardId, meetingCreateDto);
        return ResponseEntity.ok(meeting);
    }

}
