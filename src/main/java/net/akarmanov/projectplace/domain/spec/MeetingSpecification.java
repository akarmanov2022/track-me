package net.akarmanov.projectplace.domain.spec;

import jakarta.persistence.criteria.Join;
import net.akarmanov.projectplace.domain.Meeting;
import net.akarmanov.projectplace.domain.TeamCard;
import net.akarmanov.projectplace.domain.User;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class MeetingSpecification {
    public static Specification<Meeting> userEquals(UUID userId) {
        return (root, query, criteriaBuilder) -> {
            Join<Meeting, TeamCard> teamCardJoin = root.join("teamCard");
            Join<TeamCard, User> userJoin = teamCardJoin.join("user");
            return criteriaBuilder.equal(userJoin.get("id"), userId);
        };
    }

    public static Specification<Meeting> teamCardIdEquals(UUID teamCardId) {
        return (root, query, criteriaBuilder) -> {
            Join<Meeting, TeamCard> teamCardJoin = root.join("teamCard");
            return criteriaBuilder.equal(teamCardJoin.get("id"), teamCardId);
        };
    }

    public static Specification<Meeting> meetingIdEquals(UUID meetingId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("id"), meetingId);
    }
}
