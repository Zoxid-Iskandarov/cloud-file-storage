package com.walking.cloudStorage.integration.controller;

import com.walking.cloudStorage.integration.IntegrationTestBase;
import com.walking.cloudStorage.integration.annotation.WithMockUserPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class UserControllerIT extends IntegrationTestBase {

    @Test
    @WithMockUserPrincipal
    @Sql(scripts = {"classpath:data/sql/cleanup.sql", "classpath:data/sql/data.sql"})
    void me_whenUserAuthenticated_success() throws Exception {
        mockMvc.perform(get("/api/user/me"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.username").value(USERNAME))
                .andExpect(jsonPath("$.created").exists());
    }

    @Test
    @Sql(scripts = "classpath:data/sql/cleanup.sql")
    void me_whenUserNotAuthenticated_failed() throws Exception {
        mockMvc.perform(get("/api/user/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Authentication required"))
                .andExpect(jsonPath("$.path").value("/api/user/me"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
