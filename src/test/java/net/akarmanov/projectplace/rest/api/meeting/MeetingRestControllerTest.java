package net.akarmanov.projectplace.rest.api.meeting;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.akarmanov.projectplace.BaseApplicationTest;
import net.akarmanov.projectplace.domain.TeamCard;
import net.akarmanov.projectplace.repos.TeamCardsRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.OffsetDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser(username = BaseApplicationTest.USERNAME, roles = "TRACKER")
class MeetingRestControllerTest extends BaseApplicationTest {

    @Autowired
    private TeamCardsRepository teamCardsRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private TeamCard teamCard;

    @BeforeEach
    void setUp() {
        teamCard = teamCardsRepository.save(TeamCard.builder()
                .name("Test")
                .description("Test")
                .user(user)
                .build());
    }

    @AfterEach
    void tearDown() {
        teamCardsRepository.deleteAll();
    }

    @Test
    void createMeeting_success() throws Exception {

        var meetingCreateDto = MeetingCreateDto.builder()
                .link("https://example.com/meeting")
                .number("12345")
                .startDate(OffsetDateTime.now())
                .build();

        mockMvc.perform(post("/api/v1/meeting/create")
                        .param("teamCardId", teamCard.getId().toString())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(meetingCreateDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.teamCardId").value(teamCard.getId().toString()));
    }

    @Test
    void createMeeting_teamCardNotFound() throws Exception {

        var meetingCreateDto = MeetingCreateDto.builder()
                .link("https://example.com/meeting")
                .number("12345")
                .startDate(OffsetDateTime.now())
                .build();

        mockMvc.perform(post("/api/v1/meeting/create")
                        .param("teamCardId", "00000000-0000-0000-0000-000000000000")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(meetingCreateDto)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/json"));
    }
}