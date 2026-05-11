package net.trackme.sso.services.impl;

import net.trackme.sso.AbstractIntegrationTest;
import net.trackme.sso.exception.EmailNotFoundException;
import net.trackme.sso.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultUserServiceTest extends AbstractIntegrationTest {

    private static final String TRACKER = "tracker";
    private static final String TRACKER_EMAIL = "tracker@tracker.com";

    @Autowired
    private UserService userService;

    @Test
    void resetPassword_emailNotFound() {
        var email = "john@john.john";
        var password = "testPassword@123";

        assertThrows(EmailNotFoundException.class, () -> userService.resetPassword(email, password));
    }

    @Test
    @WithMockUser(username = "superadmin", roles = "SUPER_ADMIN")
    void unlockUser_success() {
        userService.enableUser(TRACKER);
        userService.disableUser(TRACKER);
        userService.disableUser(TRACKER);

        userService.unlockUser(TRACKER);

        var user = userService.findByUsername(TRACKER);
        assertTrue(user.getAccountNonLocked());
    }

    @Test
    @WithMockUser(username = "superadmin", roles = "SUPER_ADMIN")
    void unlockUser_alreadyUnlocked_doesNothing() {
        userService.enableUser(TRACKER);
        userService.unlockUser(TRACKER);

        var user = userService.findByUsername(TRACKER);
        assertTrue(user.getAccountNonLocked());
    }

    @Test
    @WithMockUser(username = "superadmin", roles = "SUPER_ADMIN")
    void getUserTeams_returnsList() {
        var teams = userService.getUserTeams(TRACKER);
        assertNotNull(teams);
    }

    @Test
    @WithMockUser(username = "superadmin", roles = "SUPER_ADMIN")
    void existsByEmailOrUsername_existingUser_returnsTrue() {
        var exists = userService.existsByEmailOrUsername(TRACKER_EMAIL, TRACKER);
        assertTrue(exists);
    }
}
