package com.walking.cloudStorage.integration.service;

import com.walking.cloudStorage.config.init.MinioInitializer;
import com.walking.cloudStorage.config.security.UserPrincipal;
import com.walking.cloudStorage.domain.exception.DuplicateException;
import com.walking.cloudStorage.domain.exception.ObjectNotFoundException;
import com.walking.cloudStorage.service.UserService;
import com.walking.cloudStorage.web.dto.user.UserRequest;
import com.walking.cloudStorage.web.dto.user.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Testcontainers
public class UserServiceIT {
    private static final String POSTGRES_IMAGE_NAME = "postgres:17.2";
    private static final Long USER_ID = 1L;
    private static final String USERNAME = "Zoxid27";
    private static final String PASSWORD = "Password123";

    @ServiceConnection
    @Container
    private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(POSTGRES_IMAGE_NAME);

    @Autowired
    private UserService userService;

    @MockitoBean
    private MinioInitializer minioInitializer;

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
                .password(PASSWORD)
                .build();
    }
}
