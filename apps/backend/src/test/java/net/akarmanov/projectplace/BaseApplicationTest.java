package net.akarmanov.projectplace;

import net.akarmanov.projectplace.domain.User;
import net.akarmanov.projectplace.models.UserRole;
import net.akarmanov.projectplace.repos.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    public static final String USERNAME = "test_superadmin";

    public static final String PASSWORD = "123456";

    @Autowired
    protected MockMvc mockMvc;

    protected User user;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void initUser() {
        user = userRepository.save(User.builder()
                .enabled(true)
                .firstName("Иван")
                .lastName("Иванов")
                .telegramId(USERNAME)
                .email("")
                .password(passwordEncoder.encode(PASSWORD))
                .role(UserRole.SUPER_ADMIN)
                .build()
        );
    }

    @AfterEach()
    void deleteUser() {
        userRepository.delete(user);
    }
}
