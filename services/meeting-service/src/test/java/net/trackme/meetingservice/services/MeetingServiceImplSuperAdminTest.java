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
import net.trackme.meetingservice.services.integration.backend.dto.TeamCardDto;
import net.trackme.meetingservice.services.integration.sso.SsoApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
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
@MockitoSettings(strictness = Strictness.LENIENT)
class MeetingServiceImplSuperAdminTest {

    @Mock private MeetingRepository meetingRepository;
    @Mock private MeetingMapper meetingMapper;
    @Mock private AclService aclService;
    @Mock private MeetingEventsProducer meetingEventsProducer;
    @Mock(name = "userBackendApiClient") private BackendApiClient userBackendClient;
    @Mock private SsoApiClient ssoApiClient;
    @Mock private Authentication authentication;
    @Mock private SecurityContext securityContext;

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
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(meetingRepository.findOne(any(Specification.class))).thenReturn(Optional.of(meeting));
        lenient().when(meetingRepository.saveAndFlush(any(Meeting.class))).thenReturn(meeting);
        lenient().doNothing().when(meetingMapper).updateEntityFromDto(any(), any());
        lenient().when(meetingMapper.mapToDto(any())).thenReturn(null);
        // Заглушка для userBackendClient, чтобы избежать NPE при enrichWithRoomLink
        lenient().when(userBackendClient.getTeamCardById(any())).thenReturn(TeamCardDto.builder().build());
    }

    @Test
    void regularAdmin_canUpdateScheduledMeeting() {
        when(authentication.getAuthorities()).thenAnswer(invocation ->
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        meeting.setStatus(MeetingStatus.SCHEDULED);
        meetingService.updateMeeting(meetingId, teamCardId, updateDto);
        verify(meetingRepository).saveAndFlush(meeting);
    }

    @Test
    void regularAdmin_cannotUpdateCompletedMeeting() {
        when(authentication.getAuthorities()).thenAnswer(invocation ->
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        meeting.setStatus(MeetingStatus.COMPLETED);
        assertThatThrownBy(() -> meetingService.updateMeeting(meetingId, teamCardId, updateDto))
                .isInstanceOf(MeetingCompletedException.class);
        verify(meetingRepository, never()).saveAndFlush(any());
    }

    @Test
    void regularAdmin_cannotUpdateFinallyCompletedMeeting() {
        when(authentication.getAuthorities()).thenAnswer(invocation ->
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        meeting.setStatus(MeetingStatus.FINALLY_COMPLETED);
        assertThatThrownBy(() -> meetingService.updateMeeting(meetingId, teamCardId, updateDto))
                .isInstanceOf(MeetingCompletedException.class);
        verify(meetingRepository, never()).saveAndFlush(any());
    }

    @Test
    void regularAdmin_cannotUpdateCompletedAsNotHappenedMeeting() {
        when(authentication.getAuthorities()).thenAnswer(invocation ->
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        meeting.setStatus(MeetingStatus.COMPLETED_AS_NOT_HAPPENED);
        assertThatThrownBy(() -> meetingService.updateMeeting(meetingId, teamCardId, updateDto))
                .isInstanceOf(MeetingCompletedException.class);
        verify(meetingRepository, never()).saveAndFlush(any());
    }

    @Test
    void superAdmin_canUpdateFinallyCompletedMeeting() {
        when(authentication.getAuthorities()).thenAnswer(invocation ->
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")));
        meeting.setStatus(MeetingStatus.FINALLY_COMPLETED);
        meetingService.updateMeeting(meetingId, teamCardId, updateDto);
        verify(meetingRepository).saveAndFlush(meeting);
    }

    @Test
    void superAdmin_canUpdateCompletedAsNotHappenedMeeting() {
        when(authentication.getAuthorities()).thenAnswer(invocation ->
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")));
        meeting.setStatus(MeetingStatus.COMPLETED_AS_NOT_HAPPENED);
        meetingService.updateMeeting(meetingId, teamCardId, updateDto);
        verify(meetingRepository).saveAndFlush(meeting);
    }

    @Test
    void superAdmin_canUpdateOrdinaryCompletedMeeting() {
        when(authentication.getAuthorities()).thenAnswer(invocation ->
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")));
        meeting.setStatus(MeetingStatus.COMPLETED);
        meetingService.updateMeeting(meetingId, teamCardId, updateDto);
        verify(meetingRepository).saveAndFlush(meeting);
    }

    @Test
    void updateMeeting_throwsWhenMeetingNotFound() {
        when(authentication.getAuthorities()).thenAnswer(invocation ->
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
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
    void meetingStatus_isEditableBySuperAdmin_ReturnsFalseForScheduled() {
        assertThat(MeetingStatus.SCHEDULED.isEditableBySuperAdmin()).isFalse();
    }

    @Test
    void meetingStatus_isEditableBySuperAdmin_ReturnsFalseForCompleted() {
        assertThat(MeetingStatus.COMPLETED.isEditableBySuperAdmin()).isFalse();
    }
}