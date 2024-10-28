package net.akarmanov.projectplace.rest.api.meeting;

import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record MeetingCreateDto(
        String link,
        String number,
        OffsetDateTime startDate
) {
}
