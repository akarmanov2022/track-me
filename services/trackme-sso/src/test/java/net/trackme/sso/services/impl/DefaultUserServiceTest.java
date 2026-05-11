package net.trackme.sso.services.impl;

import net.trackme.commons.filters.FilterRequest;
import net.trackme.sso.AbstractIntegrationTest;
import net.trackme.sso.exception.EmailNotFoundException;
import net.trackme.sso.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
        assertTrue(userService.existsByEmailOrUsername(TRACKER_EMAIL, TRACKER));
    }

    @Test
    void existsByEmailOrUsername_nonExisting_returnsFalse() {
        assertFalse(userService.existsByEmailOrUsername("no@no.com", "nouser"));
    }

    @Test
    void existsByEmail_existingUser_returnsTrue() {
        assertTrue(userService.existsByEmail(TRACKER_EMAIL));
    }

    @Test
    void existsByEmail_nonExisting_returnsFalse() {
        assertFalse(userService.existsByEmail("no@no.com"));
    }

    @Test
    void findByUsername_existingUser_returnsUser() {
        var user = userService.findByUsername(TRACKER);
        assertEquals(TRACKER, user.getUsername());
    }

    @Test
    void getUserInfo_returnsCorrectData() {
        var dto = userService.getUserInfo(TRACKER);
        assertEquals(TRACKER, dto.username());
        assertEquals(TRACKER_EMAIL, dto.email());
    }

    @Test
    void getTrackers_returnsPage() {
        var page = userService.getTrackers(
                new FilterRequest(List.of()),
                Pageable.ofSize(10));
        assertNotNull(page);
    }

    @Test
    void getAdmins_returnsPage() {
        var page = userService.getAdmins(
                new FilterRequest(List.of()),
                Pageable.ofSize(10));
        assertNotNull(page);
    }
}