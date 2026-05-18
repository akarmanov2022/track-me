package net.trackme.meetingservice.services;

import net.trackme.commons.acl.AclService;
import net.trackme.meetingservice.api.dto.MeetingUpdateDto;
import net.trackme.meetingservice.dao.MeetingRepository;
import net.trackme.meetingservice.entities.Meeting;
import net.trackme.meetingservice.entities.MeetingStatus;
import net.trackme.meetingservice.entities.TeamStatus;
import net.trackme.meetingservice.mapping.MeetingMapper;
import net.trackme.meetingservice.messaging.own.MeetingEventsProducer;
import net.trackme.meetingservice.services.exceptions.MeetingCompletedException;
import net.trackme.meetingservice.services.exceptions.MeetingNotFoundException;
import net.trackme.meetingservice.services.integration.backend.BackendApiClient;
import net.trackme.meetingservice.services.integration.sso.SsoApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeetingServiceImplSuperAdminTest {

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private MeetingMapper meetingMapper;

    @Mock
    private AclService aclService;

    @Mock
    private MeetingEventsProducer meetingEventsProducer;

    @Mock(name = "userBackendApiClient")
    private BackendApiClient userBackendClient;

    @Mock
    private SsoApiClient ssoApiClient;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private MeetingServiceImpl meetingService;

    private UUID meetingId;
    private UUID teamCardId;
    private Meeting meeting;
    private MeetingUpdateDto updateDto;

    @BeforeEach
    void setUp() {
        meetingId = UUID.randomUUID();
        teamCardId = UUID.randomUUID();
        meeting = Meeting.builder()
                .id(meetingId)
                .teamCardId(teamCardId)
                .status(MeetingStatus.SCHEDULED)
                .startDate(OffsetDateTime.now())
                .build();
        updateDto = MeetingUpdateDto.builder()
                .recordLink("https://example.com/updated")
                .number("2")
                .teamStatus(TeamStatus.OK)
                .build();

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
    }

    @Test
    void updateMeeting_RegularAdmin_UpdatesScheduledMeeting_Success() {
        when(authentication.getAuthorities()).thenAnswer(invocation ->
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        when(meetingRepository.findOne(any(Specification.class))).thenReturn(Optional.of(meeting));
        when(meetingRepository.saveAndFlush(any(Meeting.class))).thenReturn(meeting);
        when(meetingMapper.mapToDto(any())).thenReturn(null);

        meetingService.updateMeeting(meetingId, teamCardId, updateDto);

        verify(meetingRepository).saveAndFlush(meeting);
    }

    @Test
    void updateMeeting_RegularAdmin_ThrowsExceptionWhenMeetingCompleted() {
        meeting.setStatus(MeetingStatus.COMPLETED);
        when(authentication.getAuthorities()).thenAnswer(invocation ->
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        when(meetingRepository.findOne(any(Specification.class))).thenReturn(Optional.of(meeting));

        assertThatThrownBy(() -> meetingService.updateMeeting(meetingId, teamCardId, updateDto))
                .isInstanceOf(MeetingCompletedException.class);
        verify(meetingRepository, never()).saveAndFlush(any());
    }

    @Test
    void updateMeeting_SuperAdmin_UpdatesCompletedMeeting_Success() {
        meeting.setStatus(MeetingStatus.COMPLETED);
        when(authentication.getAuthorities()).thenAnswer(invocation ->
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")));
        when(meetingRepository.findOne(any(Specification.class))).thenReturn(Optional.of(meeting));
        when(meetingRepository.saveAndFlush(any(Meeting.class))).thenReturn(meeting);
        when(meetingMapper.mapToDto(any())).thenReturn(null);

        meetingService.updateMeeting(meetingId, teamCardId, updateDto);

        verify(meetingRepository).saveAndFlush(meeting);
    }

    @Test
    void updateMeeting_SuperAdmin_UpdatesFinallyCompletedMeeting_Success() {
        meeting.setStatus(MeetingStatus.FINALLY_COMPLETED);
        when(authentication.getAuthorities()).thenAnswer(invocation ->
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")));
        when(meetingRepository.findOne(any(Specification.class))).thenReturn(Optional.of(meeting));
        when(meetingRepository.saveAndFlush(any(Meeting.class))).thenReturn(meeting);
        when(meetingMapper.mapToDto(any())).thenReturn(null);

        meetingService.updateMeeting(meetingId, teamCardId, updateDto);

        verify(meetingRepository).saveAndFlush(meeting);
    }

    @Test
    void updateMeeting_SuperAdmin_UpdatesCompletedAsNotHappened_Success() {
        meeting.setStatus(MeetingStatus.COMPLETED_AS_NOT_HAPPENED);
        when(authentication.getAuthorities()).thenAnswer(invocation ->
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")));
        when(meetingRepository.findOne(any(Specification.class))).thenReturn(Optional.of(meeting));
        when(meetingRepository.saveAndFlush(any(Meeting.class))).thenReturn(meeting);
        when(meetingMapper.mapToDto(any())).thenReturn(null);

        meetingService.updateMeeting(meetingId, teamCardId, updateDto);

        verify(meetingRepository).saveAndFlush(meeting);
    }

    @Test
    void updateMeeting_MeetingNotFound_ThrowsException() {
        when(meetingRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> meetingService.updateMeeting(meetingId, teamCardId, updateDto))
                .isInstanceOf(MeetingNotFoundException.class);
    }

    @Test
    void meetingStatus_isEditableBySuperAdmin_ReturnsTrueForFinallyCompleted() {
        assertThat(MeetingStatus.FINALLY_COMPLETED.isEditableBySuperAdmin()).isTrue();
    }

    @Test
    void meetingStatus_isEditableBySuperAdmin_ReturnsTrueForCompletedAsNotHappened() {
        assertThat(MeetingStatus.COMPLETED_AS_NOT_HAPPENED.isEditableBySuperAdmin()).isTrue();
    }

    @Test
    void meetingStatus_isEditableBySuperAdmin_ReturnsFalseForOtherStatuses() {
        assertThat(MeetingStatus.SCHEDULED.isEditableBySuperAdmin()).isFalse();
        assertThat(MeetingStatus.COMPLETED.isEditableBySuperAdmin()).isFalse();
    }
}