package net.trackme.meetingservice.services;

import net.trackme.meetingservice.entities.MeetingStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MeetingServiceImplSuperAdminTest {

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

    // Дополнительные тесты для всех статусов
    @Test
    void meetingStatus_EditableBySuperAdmin_OnlyForTwoStatuses() {
        for (MeetingStatus status : MeetingStatus.values()) {
            if (status == MeetingStatus.FINALLY_COMPLETED || status == MeetingStatus.COMPLETED_AS_NOT_HAPPENED) {
                assertThat(status.isEditableBySuperAdmin())
                        .as("Status %s should be editable", status)
                        .isTrue();
            } else {
                assertThat(status.isEditableBySuperAdmin())
                        .as("Status %s should NOT be editable", status)
                        .isFalse();
            }
        }
    }
}