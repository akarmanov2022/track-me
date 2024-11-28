package net.akarmanov.projectplace;

import net.akarmanov.projectplace.domain.User;
import net.akarmanov.projectplace.models.UserRole;
import net.akarmanov.projectplace.repos.UserRepository;
import net.akarmanov.projectplace.services.user.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.acls.model.AclService;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(
        properties = {
                "JWT_SECRET=12345678905675675674564564566756756756745645656"
        })
@Sql(value = {"classpath:init-test-schema.sql", "classpath:initial-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(value = "classpath:cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
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
