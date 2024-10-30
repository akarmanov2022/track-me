package net.akarmanov.projectplace.repos;

import net.akarmanov.projectplace.domain.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface MeetingRepository extends JpaRepository<Meeting, UUID>, JpaSpecificationExecutor<Meeting> {
    Optional<Meeting> findByIdAndTeamCardId(UUID id, UUID teamCardId);
}
