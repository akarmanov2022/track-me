package net.akarmanov.projectplace.services.meeting.mapping;

import net.akarmanov.projectplace.domain.Meeting;
import net.akarmanov.projectplace.rest.api.meeting.MeetingCreateDto;
import net.akarmanov.projectplace.rest.api.meeting.MeetingDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MeetingMapper {
    Meeting mapToEntity(MeetingCreateDto meetingCreateDto);

    @Mapping(target = "teamCardId", source = "teamCard.id")
    MeetingDto mapToDto(Meeting meeting);
}
