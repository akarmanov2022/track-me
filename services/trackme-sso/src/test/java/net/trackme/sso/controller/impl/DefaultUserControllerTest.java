package net.trackme.sso.controller.impl;

import net.trackme.sso.AbstractIntegrationTest;
import net.trackme.sso.config.security.SecurityConfiguration;
import net.trackme.sso.dao.entity.UserEntity;
import net.trackme.sso.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DefaultUserControllerTest extends AbstractIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserDetailsService userDetailsService;

  @Autowired
  private UserService userService;

  @BeforeEach
  void setUp() {
    try {
      UserEntity tracker = userService.findByUsername("tracker");
      if (tracker != null) {
        userService.disableUser("tracker");
        if (!tracker.getAccountNonLocked()) {
          userService.unlockUser("tracker");
        }
      }
    } catch (Exception e) {
    }
  }

  @Test
  @WithMockUser(username = "superadmin", roles = "SUPER_ADMIN")
  void enableUser_success() throws Exception {

    mockMvc.perform(formLogin(SecurityConfiguration.LOGIN_PAGE)
            .user("tracker")
            .password("tracker"))
        .andExpect(status().isFound())
        .andExpect(header().string("Location", containsString("error")));

    mockMvc.perform(post("/api/v1/users/enable")
            .param("username", "tracker")
            .with(csrf()))
        .andExpect(status().isOk());

    mockMvc.perform(formLogin(SecurityConfiguration.LOGIN_PAGE)
            .user("tracker")
            .password("tracker"))
        .andExpect(status().isFound())
        .andExpect(header().string("Location", "/"));

    assertThat(userDetailsService.loadUserByUsername("tracker").isEnabled())
        .isTrue();
    userService.disableUser("tracker");
  }

  @Test
  @WithMockUser(username = "superadmin", roles = "SUPER_ADMIN")
  void disableUser_success() throws Exception {

    userService.enableUser("tracker");

    mockMvc.perform(formLogin(SecurityConfiguration.LOGIN_PAGE)
            .user("tracker")
            .password("tracker"))
        .andExpect(status().isFound())
        .andExpect(header().string("Location", not(containsString("error"))));

    mockMvc.perform(post("/api/v1/users/disable")
            .param("username", "tracker")
            .with(csrf()))
        .andExpect(status().isOk());

    mockMvc.perform(formLogin(SecurityConfiguration.LOGIN_PAGE)
            .user("tracker")
            .password("tracker"))
        .andExpect(status().isFound())
        .andExpect(header().string("Location", containsString("error")));

    assertThat(userDetailsService.loadUserByUsername("tracker").isEnabled())
        .isFalse();

    mockMvc.perform(post("/api/v1/users/disable")
           .param("username", "tracker")
           .with(csrf()))
        .andExpect(status().isOk());

    assertThat(userService.findByUsername("tracker").getAccountNonLocked())
        .isFalse();

    userService.enableUser("tracker");
  }

  @Test
  @WithMockUser(username = "superadmin", roles = "SUPER_ADMIN")
  void enableUser_notFound() throws Exception {
    mockMvc.perform(post("/api/v1/users/enable")
            .param("username", "notfound")
            .with(csrf()))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(username = "tracker", roles = "TRACKER")
  void enableUser_notSuperAdmin() throws Exception {
    mockMvc.perform(post("/api/v1/users/enable")
            .param("username", "tracker")
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "tracker", roles = "TRACKER")
  void disableUser_notSuperAdmin() throws Exception {
    mockMvc.perform(post("/api/v1/users/disable")
            .param("username", "tracker")
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "superadmin", roles = "SUPER_ADMIN")
  void getUserInfo_success() throws Exception {
    mockMvc.perform(get("/api/v1/users/{username}/info", "tracker"))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Type", "application/json"))
        .andExpect(jsonPath("$.username").value("tracker"));
  }

  @Test
  @WithMockUser(username = "tracker", roles = "TRACKER")
  void getUserInfo_notSuperAdmin() throws Exception {
    mockMvc.perform(get("/api/v1/users/{username}/info", "tracker"))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "superadmin", roles = "SUPER_ADMIN")
  void findAllTrackers_success() throws Exception {
    mockMvc.perform(post("/api/v1/users/trackers")
            .contentType("application/json")
            .content("""
                {"filters": []}
                """)
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Type", "application/json"))
        .andExpect(jsonPath("$.content[*].username").value(hasItem("tracker")))
        .andExpect(jsonPath("$.content[*].username").value(hasItem("ronin")))
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.page.totalElements").value(2));
  }

  @Test
  @WithMockUser(username = "superadmin", roles = "SUPER_ADMIN")
  void findAllTrackers_withFilters_success() throws Exception {
    mockMvc.perform(post("/api/v1/users/trackers")
            .contentType("application/json")
            .content("""
                {"filters": [{"fieldName": "username", "type": "EQ", "value": "tracker"}]}
                """)
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Type", "application/json"))
        .andExpect(jsonPath("$.content[0].username").value("tracker"));
  }

  @Test
  @WithMockUser(username = "superadmin", roles = "SUPER_ADMIN")
  void findAllTrackers_withFilters_badRequest() throws Exception {
    mockMvc.perform(post("/api/v1/users/trackers")
            .contentType("application/json")
            .content("""
                {"filters": [{"fieldName": "username123", "type": "EQ", "value": "notfound"}]}
                """)
            .with(csrf()))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(username = "tracker", roles = "TRACKER")
  void findAllTrackers_notSuperAdmin() throws Exception {
    mockMvc.perform(post("/api/v1/users/trackers")
            .contentType("application/json")
            .content("""
                {"filters": []}
                """)
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "superadmin", roles = "SUPER_ADMIN")
  void findAllAdmins_success() throws Exception {
    mockMvc.perform(post("/api/v1/users/administrators")
            .contentType("application/json")
            .content("""
                {"filters": []}
                """)
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Type", "application/json"))
        .andExpect(jsonPath("$.content[0].username").value("admin"));
  }
}