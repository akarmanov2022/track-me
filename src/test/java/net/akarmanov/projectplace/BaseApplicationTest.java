package net.akarmanov.projectplace;

import net.akarmanov.projectplace.domain.User;
import net.akarmanov.projectplace.models.UserRole;
import net.akarmanov.projectplace.repos.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(
        properties = {
                "JWT_SECRET=12345678905675675674564564566756756756745645656"
        })
public abstract class BaseApplicationTest {

    public static final String USERNAME = "BASE_TRACKER";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    protected User user;

    @BeforeEach
    void initUser() {
        user = userRepository.save(User.builder()
                .enabled(true)
                .firstName("Иван")
                .lastName("Иванов")
                .telegramId(USERNAME)
                .password("123456")
                .role(UserRole.TRACKER)
                .build()
        );
    }
}
