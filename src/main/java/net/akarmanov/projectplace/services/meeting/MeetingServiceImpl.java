package net.akarmanov.projectplace.services.meeting;

import lombok.RequiredArgsConstructor;
import net.akarmanov.projectplace.models.MeetingStatus;
import net.akarmanov.projectplace.repos.MeetingRepository;
import net.akarmanov.projectplace.rest.api.meeting.MeetingCreateDto;
import net.akarmanov.projectplace.rest.api.meeting.MeetingDto;
import net.akarmanov.projectplace.services.meeting.mapping.MeetingMapper;
import net.akarmanov.projectplace.services.teamcard.TeamCardsService;
import net.akarmanov.projectplace.services.user.UserService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService {

    private final MeetingRepository meetingRepository;

    private final TeamCardsService teamCardsService;

    private final UserService userService;

    private final MeetingMapper meetingMapper;

    @Override
    public MeetingDto createMeeting(UUID teamCardId, MeetingCreateDto meetingCreateDto) {
        var user = userService.getCurrentUser();
        var teamCard = teamCardsService.getTeamCardEntity(teamCardId, user.getId());
        var meeting = meetingMapper.mapToEntity(meetingCreateDto);

        meeting.setTeamCard(teamCard);
        meeting.setStatus(MeetingStatus.OK);

        meeting = meetingRepository.save(meeting);
        return meetingMapper.mapToDto(meeting);
    }
}
