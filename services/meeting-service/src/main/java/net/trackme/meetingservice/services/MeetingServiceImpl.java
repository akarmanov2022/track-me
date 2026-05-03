package net.trackme.meetingservice.services;

import lombok.extern.slf4j.Slf4j;
import net.trackme.commons.acl.AclService;
import net.trackme.meetingservice.api.dto.MeetingCreateDto;
import net.trackme.meetingservice.api.dto.MeetingDto;
import net.trackme.meetingservice.api.dto.MeetingUpdateDto;
import net.trackme.meetingservice.dao.MeetingRepository;
import net.trackme.meetingservice.entities.Meeting;
import net.trackme.meetingservice.entities.MeetingStatus;
import net.trackme.meetingservice.messaging.own.MeetingCreatedEvent;
import net.trackme.meetingservice.messaging.own.MeetingUpdatedEvent;
import net.trackme.meetingservice.mapping.MeetingMapper;
import net.trackme.meetingservice.services.exceptions.*;
import net.trackme.meetingservice.services.integration.backend.BackendApiClient;
import net.trackme.meetingservice.services.integration.backend.dto.StreamDto;
import net.trackme.meetingservice.services.integration.sso.SsoApiClient;
import net.trackme.meetingservice.services.integration.sso.dto.UserDto;
import net.trackme.meetingservice.messaging.own.MeetingEventsProducer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Sort;
import net.trackme.meetingservice.messaging.own.MeetingDeletedEvent;

import java.time.OffsetDateTime;
import java.util.UUID;

import static java.util.stream.Collectors.toSet;
import static net.trackme.meetingservice.entities.MeetingSpecification.meetingIdEquals;
import static net.trackme.meetingservice.entities.MeetingSpecification.teamCardIdEquals;

@Slf4j
@Service
public class MeetingServiceImpl implements MeetingService {

    private final MeetingMapper meetingMapper;

    private final MeetingRepository meetingRepository;

    private final AclService aclService;

    private final MeetingEventsProducer meetingEventsProducer;

    private final BackendApiClient userBackendClient;

    private final SsoApiClient ssoApiClient;

    public MeetingServiceImpl(
            MeetingMapper meetingMapper,
            MeetingRepository meetingRepository,
            AclService aclService,
            MeetingEventsProducer meetingEventsProducer,
            @Qualifier("userBackendApiClient") BackendApiClient userBackendClient,
            SsoApiClient ssoApiClient) {

        this.meetingMapper = meetingMapper;
        this.meetingRepository = meetingRepository;
        this.aclService = aclService;
        this.meetingEventsProducer = meetingEventsProducer;
        this.userBackendClient = userBackendClient;
        this.ssoApiClient = ssoApiClient;
    }

    @Override
    @Transactional
    public MeetingDto createMeeting(UUID teamCardId, MeetingCreateDto createDto) {
        validateNoMeetingOnSameDay(teamCardId, createDto.startDate(), null);

        var meeting = meetingMapper.mapToEntity(createDto);
        var teamData = userBackendClient.getTeamCardById(teamCardId);
        var trackerUsername = teamData.getUsername();

        meeting.setTeamCardId(teamCardId);
        meeting.setStatus(MeetingStatus.SCHEDULED);

        meeting.setNumber("0");

        // Denormalize (Backend)
        meeting.setTeamName(teamData.getName());
        meeting.setStreamIds(teamData.getStreams().stream().map(StreamDto::getId).collect(toSet()));
        meeting.setTrackerUsername(trackerUsername);

        // Denormalize (SSO)
        if (trackerUsername != null) {
            var tracker = ssoApiClient.getTrackers().stream()
                    .filter(u -> trackerUsername.equalsIgnoreCase(u.getUsername()))
                    .findFirst();

            if (tracker.isPresent()) {
                UserDto user = tracker.get();
                meeting.setTrackerId(user.getId());
                meeting.setTrackerFullName(user.getFullName());
            } else {
                meeting.setTrackerFullName(trackerUsername);
                log.warn("Tracker with username {} not found in SSO during meeting creation", trackerUsername);
            }
        }

        var savedMeeting = meetingRepository.saveAndFlush(meeting);
        renumberMeetingsAfterDeletion(teamCardId);

        var refreshedMeeting = meetingRepository.findById(savedMeeting.getId())
                .orElseThrow(() -> new MeetingNotFoundException(savedMeeting.getId()));

        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        aclService.createAclForUser(refreshedMeeting, username);
        sendMeetingCreatedEvent(refreshedMeeting);

        return enrichWithRoomLink(meetingMapper.mapToDto(refreshedMeeting), teamCardId);
    }

    @Override
    public Page<MeetingDto> getMeetings(UUID teamCardId, Pageable pageable) {
        var meetings = meetingRepository.findAll(teamCardIdEquals(teamCardId), pageable);
        var roomLink = fetchRoomLink(teamCardId);
        return meetings.map(m -> withRoomLink(meetingMapper.mapToDto(m), roomLink));
    }

    @Override
    @Transactional
    @PreAuthorize(
            "hasPermission(#meetingId,'net.trackme.meetingservice.entities.Meeting', 'WRITE') or hasRole('ADMIN')")
    public MeetingDto updateMeeting(UUID meetingId, UUID teamCardId, MeetingUpdateDto updateDto) {
        var meeting = meetingRepository.findOne(teamCardIdEquals(teamCardId)
                        .and(meetingIdEquals(meetingId)))
                .orElseThrow(() -> new MeetingNotFoundException(meetingId, teamCardId));

        if (MeetingStatus.COMPLETED_STATUSES.contains(meeting.getStatus())) {
            throw new MeetingCompletedException(meetingId, teamCardId);
        }
        
        OffsetDateTime oldStartDate = meeting.getStartDate();
        boolean dateChanged = updateDto.startDate() != null && 
                             !updateDto.startDate().equals(oldStartDate);

        if (dateChanged) {
            validateNoMeetingOnSameDay(teamCardId, updateDto.startDate(), meetingId);
        }
        
        var oldStatus = meeting.getStatus();
        meetingMapper.updateEntityFromDto(updateDto, meeting);
        var savedMeeting = meetingRepository.saveAndFlush(meeting);

        if (dateChanged) {
            renumberMeetingsAfterDateChange(teamCardId);
            meetingRepository.flush();
            savedMeeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new MeetingNotFoundException(meetingId));
        }

        if (oldStatus != meeting.getStatus()) {
            sendMeetingUpdatedEvent(savedMeeting, oldStatus);
        }

        return enrichWithRoomLink(meetingMapper.mapToDto(savedMeeting), teamCardId);
    }


    @Override
    @Transactional
    @PreAuthorize(
            "hasPermission(#meetingId,'net.trackme.meetingservice.entities.Meeting', 'READ') or hasRole('ADMIN')")
    public void deleteMeeting(UUID meetingId) {
    var meeting = meetingRepository.findById(meetingId) 
            .orElseThrow(() -> new MeetingNotFoundException(meetingId));
    
    UUID teamCardId = meeting.getTeamCardId(); 
    
    meetingRepository.delete(meeting);
    aclService.deleteAcl(meeting);
    
    renumberMeetingsAfterDeletion(teamCardId);
    meetingRepository.flush();
    
    var event = MeetingDeletedEvent.builder()
            .meetingId(meetingId)
            .teamCardId(teamCardId)
            .startDate(meeting.getStartDate())
            .build();
    meetingEventsProducer.sendMeetingDeletedEvent(event);
    
    log.info("Meeting {} deleted and meetings renumbered for team card {}", 
             meetingId, teamCardId);
    }
    

    @Override
    @Transactional
    @PreAuthorize(
            "hasPermission(#meetingId,'net.trackme.meetingservice.entities.Meeting', 'WRITE') or hasRole('ADMIN')")
    public void addMeetingImage(UUID meetingId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new MeetingEmptyImageException();
        }

        if (file.getSize() > MeetingService.MAX_FILE_SIZE) {
            throw new MeetingLargeImageSizeException(file.getSize());
        }

        String contentType = file.getContentType();
        if (!MediaType.IMAGE_PNG_VALUE.equals(contentType)
                && !MediaType.IMAGE_JPEG_VALUE.equals(contentType)) {
            throw new MeetingMIMETypeException(contentType);
        }

        String originalName = file.getOriginalFilename();
        if (originalName != null) {
            String ext = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();
            if (!ext.equals("png") && !ext.equals("jpg") && !ext.equals("jpeg")) {
                throw new MeetingImageExtensionException(ext);
            }
        }

        var meeting = meetingRepository.getReferenceById(meetingId);
        try {
            meeting.setImageBytes(file.getBytes());
            meetingRepository.save(meeting);
        } catch (Exception e) {
            throw new MeetingImageUploadException(meetingId, e);
        }
    }

    @Override
    @PreAuthorize(
            "hasPermission(#meetingId,'net.trackme.meetingservice.entities.Meeting', 'READ') or hasRole('ADMIN')")
    public Resource getMeetingImage(UUID meetingId) {
        var meeting = getMeeting(meetingId);
        if (meeting.getImageBytes() == null) {
            throw new MeetingImageNotFoundException(meetingId);
        }
        return new ByteArrayResource(meeting.getImageBytes());
    }

    /**
     * Пересчитывает номера встреч после удаления.
     * Встречи сортируются по дате (от ранних к поздним) и получают новые номера.
     */
    private void renumberMeetingsAfterDeletion(UUID teamCardId) {
        var meetings = meetingRepository.findAll(
                teamCardIdEquals(teamCardId),
                Sort.by(Sort.Direction.ASC, "startDate")
        );
        
        int number = 1;
        for (Meeting m : meetings) {
            m.setNumber(String.valueOf(number));
            number++;
        }

        meetingRepository.saveAllAndFlush(meetings);
        
        log.debug("Renumbered {} meetings for team card {}", meetings.size(), teamCardId);
    }

    /**
     * Перенумеровывает встречи после изменения даты.
     */
    private void renumberMeetingsAfterDateChange(UUID teamCardId) {
        renumberMeetingsAfterDeletion(teamCardId);
    }

    /**
     * Проверяет отсутствие встречи для карточки команды в указанный день.
     */
    private void validateNoMeetingOnSameDay(UUID teamCardId, OffsetDateTime startDate, UUID excludeId) {
        var date = startDate.toLocalDate();
        var from = date.atStartOfDay().atOffset(startDate.getOffset());
        var to = date.plusDays(1).atStartOfDay().atOffset(startDate.getOffset());

        boolean existsOnSameDay = excludeId == null
                ? meetingRepository.existsByTeamCardIdAndStartDateGreaterThanEqualAndStartDateLessThan(
                    teamCardId, from, to)
                : meetingRepository.existsByTeamCardIdAndStartDateGreaterThanEqualAndStartDateLessThanAndIdNot(
                    teamCardId, from, to, excludeId);

        if (existsOnSameDay) {
            throw new MeetingAlreadyExistsInSameDayException(
                    "В этот день уже запланирована встреча для данной команды.");
        }
    }

    private Meeting getMeeting(UUID meetingId) {
        return meetingRepository.findById(meetingId)
                .orElseThrow(() -> new MeetingNotFoundException(meetingId));
    }

    private void sendMeetingUpdatedEvent(Meeting meeting, MeetingStatus oldStatus) {
        var event = MeetingUpdatedEvent.builder()
                .meetingId(meeting.getId())
                .newStatus(meeting.getStatus())
                .oldStatus(oldStatus)
                .teamStatus(meeting.getTeamStatus())
                .teamCardId(meeting.getTeamCardId())
                .teamGrade(
                        meeting.getTeamStatus() == null
                                ? 0
                                : meeting.getTeamStatus().getValue()
                )
                .build();

        meetingEventsProducer.sendMeetingUpdatedEvent(event);
    }

    private void sendMeetingCreatedEvent(Meeting meeting) {
        var event = MeetingCreatedEvent.builder()
                .meetingId(meeting.getId())
                .teamCardId(meeting.getTeamCardId())
                .build();
        meetingEventsProducer.sendMeetingCreatedEvent(event);
    }

    private String fetchRoomLink(UUID teamCardId) {
        try {
            var teamCard = userBackendClient.getTeamCardById(teamCardId);

            return teamCard != null
                    ? teamCard.getMeetingRoomLink()
                    : null;

        } catch (Exception e) {
            log.warn("Не удалось получить roomLink для teamCardId={}: {} | cause: {}",
                    teamCardId, e.getMessage(),
                    e.getCause() != null ? e.getCause().getMessage() : "no cause");
            return null;
        }
    }

    private MeetingDto enrichWithRoomLink(MeetingDto dto, UUID teamCardId) {
        return withRoomLink(dto, fetchRoomLink(teamCardId));
    }

    private MeetingDto withRoomLink(MeetingDto dto, String roomLink) {
        return new MeetingDto(
                dto.id(),
                dto.recordLink(),
                roomLink,
                dto.number(),
                dto.startDate(),
                dto.teamStatus(),
                dto.status(),
                dto.teamCardId(),
                dto.tasksCurrentMeeting(),
                dto.tasksNextMeeting()
        );
    }
}
