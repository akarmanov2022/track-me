package net.trackme.meetingservice.services;

import net.trackme.meetingservice.entities.MeetingStatus;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MeetingServiceImplSuperAdminTest {

    // ========== БАЗОВЫЕ ТЕСТЫ ДЛЯ КАЖДОГО СТАТУСА ==========

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

    // ========== ДОПОЛНИТЕЛЬНЫЕ ТЕСТЫ ДЛЯ ПОКРЫТИЯ ВСЕХ ВОЗМОЖНЫХ СТАТУСОВ ==========

    @Test
    void meetingStatus_isEditableBySuperAdmin_ShouldBeTrueOnlyForTwoSpecificStatuses() {
        List<MeetingStatus> allStatuses = Arrays.asList(MeetingStatus.values());
        
        for (MeetingStatus status : allStatuses) {
            if (status == MeetingStatus.FINALLY_COMPLETED || status == MeetingStatus.COMPLETED_AS_NOT_HAPPENED) {
                assertThat(status.isEditableBySuperAdmin())
                        .as("Status %s должен быть доступен для редактирования суперадмином", status)
                        .isTrue();
            } else {
                assertThat(status.isEditableBySuperAdmin())
                        .as("Status %s НЕ должен быть доступен для редактирования суперадмином", status)
                        .isFalse();
            }
        }
    }

    // ========== ТЕСТЫ ДЛЯ ПРОВЕРКИ СТРОГИХ РАВЕНСТВ ==========

    @Test
    void meetingStatus_FINALLY_COMPLETED_ShouldBeEditable() {
        MeetingStatus status = MeetingStatus.FINALLY_COMPLETED;
        assertThat(status.isEditableBySuperAdmin()).isTrue();
        assertThat(status.getDescription()).isEqualTo("Окончательно завершена");
    }

    @Test
    void meetingStatus_COMPLETED_AS_NOT_HAPPENED_ShouldBeEditable() {
        MeetingStatus status = MeetingStatus.COMPLETED_AS_NOT_HAPPENED;
        assertThat(status.isEditableBySuperAdmin()).isTrue();
        assertThat(status.getDescription()).isEqualTo("Завершена как не состоявшаяся");
    }

    @Test
    void meetingStatus_SCHEDULED_ShouldNotBeEditable() {
        MeetingStatus status = MeetingStatus.SCHEDULED;
        assertThat(status.isEditableBySuperAdmin()).isFalse();
        assertThat(status.getDescription()).isEqualTo("Запланирована");
    }

    @Test
    void meetingStatus_COMPLETED_ShouldNotBeEditable() {
        MeetingStatus status = MeetingStatus.COMPLETED;
        assertThat(status.isEditableBySuperAdmin()).isFalse();
        assertThat(status.getDescription()).isEqualTo("Завершена");
    }

    // ========== ТЕСТЫ ДЛЯ ПРОВЕРКИ УНИКАЛЬНОСТИ ==========

    @Test
    void meetingStatus_OnlyTwoStatusesShouldBeEditable() {
        long editableCount = Arrays.stream(MeetingStatus.values())
                .filter(MeetingStatus::isEditableBySuperAdmin)
                .count();
        assertThat(editableCount).isEqualTo(2);
    }

    @Test
    void meetingStatus_EditableStatusesAreExactlyFinallyCompletedAndCompletedAsNotHappened() {
        List<MeetingStatus> editableStatuses = Arrays.stream(MeetingStatus.values())
                .filter(MeetingStatus::isEditableBySuperAdmin)
                .toList();
        
        assertThat(editableStatuses).containsExactlyInAnyOrder(
                MeetingStatus.FINALLY_COMPLETED,
                MeetingStatus.COMPLETED_AS_NOT_HAPPENED
        );
    }

    // ========== ТЕСТЫ ДЛЯ ПРОВЕРКИ МЕТОДОВ GETTER ==========

    @Test
    void meetingStatus_GetDescription_ReturnsCorrectValue() {
        assertThat(MeetingStatus.FINALLY_COMPLETED.getDescription()).isEqualTo("Окончательно завершена");
        assertThat(MeetingStatus.COMPLETED_AS_NOT_HAPPENED.getDescription()).isEqualTo("Завершена как не состоявшаяся");
        assertThat(MeetingStatus.SCHEDULED.getDescription()).isEqualTo("Запланирована");
        assertThat(MeetingStatus.COMPLETED.getDescription()).isEqualTo("Завершена");
    }

    // ========== ТЕСТЫ ДЛЯ COMPLETED_STATUSES ==========

    @Test
    void meetingStatus_COMPLETED_STATUSES_ContainsAllCompletedStatuses() {
        assertThat(MeetingStatus.COMPLETED_STATUSES).containsExactlyInAnyOrder(
                MeetingStatus.COMPLETED,
                MeetingStatus.FINALLY_COMPLETED,
                MeetingStatus.COMPLETED_AS_NOT_HAPPENED
        );
    }

    @Test
    void meetingStatus_COMPLETED_STATUSES_ShouldContainThreeElements() {
        assertThat(MeetingStatus.COMPLETED_STATUSES).hasSize(3);
    }

    // ========== ТЕСТЫ ДЛЯ toString() ==========

    @Test
    void meetingStatus_toString_ReturnsEnumName() {
        assertThat(MeetingStatus.FINALLY_COMPLETED.toString()).isEqualTo("FINALLY_COMPLETED");
        assertThat(MeetingStatus.COMPLETED_AS_NOT_HAPPENED.toString()).isEqualTo("COMPLETED_AS_NOT_HAPPENED");
        assertThat(MeetingStatus.SCHEDULED.toString()).isEqualTo("SCHEDULED");
        assertThat(MeetingStatus.COMPLETED.toString()).isEqualTo("COMPLETED");
    }

    // ========== ТЕСТЫ ДЛЯ ПРОВЕРКИ ОТРИЦАНИЙ ==========

    @Test
    void meetingStatus_SCHEDULED_IsNotEditable_And_IsNotCompletedStatus() {
        assertThat(MeetingStatus.SCHEDULED.isEditableBySuperAdmin()).isFalse();
        assertThat(MeetingStatus.COMPLETED_STATUSES).doesNotContain(MeetingStatus.SCHEDULED);
    }

    @Test
    void meetingStatus_COMPLETED_IsNotEditable_But_IsCompletedStatus() {
        assertThat(MeetingStatus.COMPLETED.isEditableBySuperAdmin()).isFalse();
        assertThat(MeetingStatus.COMPLETED_STATUSES).contains(MeetingStatus.COMPLETED);
    }
}