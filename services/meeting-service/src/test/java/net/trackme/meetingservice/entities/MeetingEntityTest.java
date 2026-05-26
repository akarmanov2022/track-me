package net.trackme.meetingservice.entities;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class MeetingTest {

    @Test
    void updateTeamStatusValue_whenPassive_shouldNotChangeStatusValue() {
        // Arrange
        Meeting meeting = Meeting.builder()
                .teamCardPassive(true)
                .teamStatus(TeamStatus.OK) // даже если OK, не должно измениться
                .teamStatusValue(BigDecimal.valueOf(999)) // какое-то старое значение
                .build();

        // Act
        meeting.updateTeamStatusValue();

        // Assert
        assertEquals(BigDecimal.valueOf(999), meeting.getTeamStatusValue());
    }

    @Test
    void updateTeamStatusValue_whenNotPassive_shouldUpdateBasedOnStatus() {
        // Arrange
        Meeting meeting = Meeting.builder()
                .teamCardPassive(false)
                .teamStatus(TeamStatus.OK)
                .build();

        // Act
        meeting.updateTeamStatusValue();

        // Assert
        assertEquals(BigDecimal.valueOf(1.0), meeting.getTeamStatusValue());
    }

    @Test
    void updateTeamStatusValue_whenStatusCompletedAsNotHappened_shouldSetMinusOneEvenIfPassiveIsFalse() {
        // Проверяем приоритет: статус COMPLETED_AS_NOT_HAPPENED важнее passive
        Meeting meeting = Meeting.builder()
                .teamCardPassive(false)
                .status(MeetingStatus.COMPLETED_AS_NOT_HAPPENED)
                .build();

        meeting.updateTeamStatusValue();

        assertEquals(BigDecimal.valueOf(-1.0), meeting.getTeamStatusValue());
    }

    @Test
    void updateTeamStatusValue_whenNotPassiveAndStatusWithIssues_shouldSetZeroPointFive() {
        Meeting meeting = Meeting.builder()
                .teamCardPassive(false)
                .teamStatus(TeamStatus.WITH_ISSUES)
                .build();

        meeting.updateTeamStatusValue();

        assertEquals(BigDecimal.valueOf(0.5), meeting.getTeamStatusValue());
    }

    @Test
    void updateTeamStatusValue_whenNotPassiveAndStatusManyIssues_shouldSetZeroPointTwoFive() {
        Meeting meeting = Meeting.builder()
                .teamCardPassive(false)
                .teamStatus(TeamStatus.MANY_ISSUES)
                .build();

        meeting.updateTeamStatusValue();

        assertEquals(BigDecimal.valueOf(0.25), meeting.getTeamStatusValue());
    }

    @Test
    void updateTeamStatusValue_whenNotPassiveAndStatusScheduled_shouldSetZero() {
        Meeting meeting = Meeting.builder()
                .teamCardPassive(false)
                .status(MeetingStatus.SCHEDULED)
                .build();

        meeting.updateTeamStatusValue();

        assertEquals(BigDecimal.valueOf(0.0), meeting.getTeamStatusValue());
    }

    @Test
    void updateTeamStatusValue_whenNotPassiveAndTeamStatusNull_shouldSetZero() {
        Meeting meeting = Meeting.builder()
                .teamCardPassive(false)
                .teamStatus(null)
                .build();

        meeting.updateTeamStatusValue();

        assertEquals(BigDecimal.valueOf(0.0), meeting.getTeamStatusValue());
    }

    @Test
    void teamCardPassive_defaultValueIsFalse() {
        Meeting meeting = new Meeting();
        assertFalse(meeting.getTeamCardPassive());
    }

    @Test
    void teamCardPassive_canBeSetToTrue() {
        Meeting meeting = Meeting.builder()
                .teamCardPassive(true)
                .build();
        assertTrue(meeting.getTeamCardPassive());
    }
}