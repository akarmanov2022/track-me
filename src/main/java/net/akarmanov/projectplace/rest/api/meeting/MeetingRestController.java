package net.akarmanov.projectplace.rest.api.meeting;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Tag(name = "Meeting API", description = "API для работы с встречами команды")
@RequestMapping("/api/v1/meeting")
public interface MeetingRestController {
    @Operation(summary = "Создание встречи команды")
    @PostMapping(value = "/create", consumes = "application/json", produces = "application/json")
    ResponseEntity<MeetingDto> createMeeting(
            @RequestParam UUID teamCardId,
            @RequestBody MeetingCreateDto meetingCreateDto);
}
