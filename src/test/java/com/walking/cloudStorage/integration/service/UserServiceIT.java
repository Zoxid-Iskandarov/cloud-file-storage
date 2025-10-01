package com.walking.cloudStorage.integration.service;

import com.walking.cloudStorage.config.security.UserPrincipal;
import com.walking.cloudStorage.domain.exception.DuplicateException;
import com.walking.cloudStorage.domain.exception.ObjectNotFoundException;
import com.walking.cloudStorage.integration.IntegrationTestBase;
import com.walking.cloudStorage.service.UserService;
import com.walking.cloudStorage.web.dto.user.UserRequest;
import com.walking.cloudStorage.web.dto.user.UserResponse;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@RequiredArgsConstructor
public class UserServiceIT extends IntegrationTestBase {
    private final UserService userService;

    @Test
    @Sql(scripts = {"classpath:data/sql/cleanup.sql", "classpath:data/sql/data.sql"})
    void getById_whenUserExists_success() {
        UserResponse userResponse = userService.getById(USER_ID);

        assertThat(userResponse).isNotNull();
        assertThat(userResponse.getId()).isEqualTo(USER_ID);
        assertThat(userResponse.getUsername()).isEqualTo(USERNAME);
    }

    @Test
    @Sql(scripts = "classpath:data/sql/cleanup.sql")
    void getById_whenUserNotExists_failed() {
        assertThrows(ObjectNotFoundException.class, () -> userService.getById(USER_ID));
    }

    @Test
    @Sql(scripts = "classpath:data/sql/cleanup.sql")
    void create_whenValidRequest_success() {
        UserResponse userResponse = userService.create(getUserRequest());

        assertThat(userResponse).isNotNull();
        assertThat(userResponse.getId()).isEqualTo(USER_ID);
        assertThat(userResponse.getUsername()).isEqualTo(USERNAME);
        assertThat(userResponse.getCreated()).isNotNull();
    }

    @Test
    @Sql(scripts = {"classpath:data/sql/cleanup.sql", "classpath:data/sql/data.sql"})
    void create_whenUsernameAlreadyExists_failed() {
        assertThrows(DuplicateException.class, () -> userService.create(getUserRequest()));
    }

    @Test
    @Sql(scripts = {"classpath:data/sql/cleanup.sql", "classpath:data/sql/data.sql"})
    void loadUserByUsername_whenUserExists_success() {
        UserPrincipal userPrincipal = userService.loadUserByUsername(USERNAME);

        assertThat(userPrincipal).isNotNull();
        assertThat(userPrincipal.getId()).isEqualTo(USER_ID);
        assertThat(userPrincipal.getUsername()).isEqualTo(USERNAME);
        assertThat(userPrincipal.getPassword()).isNotBlank();
    }

    @Test
    @Sql(scripts = "classpath:data/sql/cleanup.sql")
    void loadUserByUsername_whenUserNotExists_failed() {
        assertThrows(UsernameNotFoundException.class, () ->  userService.loadUserByUsername(USERNAME));
    }

    private UserRequest getUserRequest() {
        return UserRequest.builder()
                .username(USERNAME)
                .password(VALID_PASSWORD)
                .build();
    }
}
