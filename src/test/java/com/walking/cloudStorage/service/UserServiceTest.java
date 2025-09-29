package com.walking.cloudStorage.service;

import com.walking.cloudStorage.config.security.UserPrincipal;
import com.walking.cloudStorage.domain.exception.DuplicateException;
import com.walking.cloudStorage.domain.exception.ObjectNotFoundException;
import com.walking.cloudStorage.domain.model.User;
import com.walking.cloudStorage.respository.UserRepository;
import com.walking.cloudStorage.service.impl.UserServiceImpl;
import com.walking.cloudStorage.web.dto.user.UserRequest;
import com.walking.cloudStorage.web.dto.user.UserResponse;
import com.walking.cloudStorage.web.mapper.UserRequestMapper;
import com.walking.cloudStorage.web.mapper.UserResponseMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    private static final Long ID = 1L;
    private static final String USERNAME = "Ivanov";
    private static final String PASSWORD = "Password123";
    private static final String ENCODED_PASSWORD = "{bcrypt}$2a$12$3JhmqB.ydw22drw/XpFf..Fu7aMMGqsOl/loNJnqeELPYoyxwcwM2";
    private static final LocalDateTime NOW = LocalDateTime.now();

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRequestMapper userRequestMapper;

    @Mock
    private UserResponseMapper userResponseMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getById_whenUserExists_success() {
        User user = getUser();
        UserResponse userResponse = getUserResponse();

        doReturn(Optional.of(user)).when(userRepository).findById(ID);
        doReturn(userResponse).when(userResponseMapper).toDto(user);

        UserResponse actual = userService.getById(ID);

        assertEquals(user.getId(), actual.getId());
        assertEquals(user.getUsername(), actual.getUsername());
        assertEquals(user.getCreated(), actual.getCreated());

        verify(userRepository).findById(ID);
        verify(userResponseMapper).toDto(user);
        verifyNoMoreInteractions(userRepository, userResponseMapper);
    }

    @Test
    void getById_whenUserNotExists_failed() {
        doReturn(Optional.empty()).when(userRepository).findById(ID);

        assertThrows(ObjectNotFoundException.class, () -> userService.getById(ID));

        verify(userRepository).findById(ID);
        verify(userResponseMapper, never()).toDto(any(User.class));
        verifyNoMoreInteractions(userRepository, userResponseMapper);
    }

    @Test
    void create_whenValidRequest_success() {
        UserRequest userRequest = getUserRequest();
        User user = getUser();
        UserResponse userResponse = getUserResponse();

        doReturn(false).when(userRepository).existsByUsername(userRequest.getUsername());
        doReturn(user).when(userRequestMapper).toEntity(userRequest);
        doReturn(ENCODED_PASSWORD).when(passwordEncoder).encode(userRequest.getPassword());
        doReturn(user).when(userRepository).save(user);
        doReturn(userResponse).when(userResponseMapper).toDto(user);

        UserResponse actual = userService.create(userRequest);

        assertEquals(user.getId(), actual.getId());
        assertEquals(user.getUsername(), actual.getUsername());
        assertEquals(user.getCreated(), actual.getCreated());

        verify(userRepository).existsByUsername(userRequest.getUsername());
        verify(userRequestMapper).toEntity(userRequest);
        verify(passwordEncoder).encode(userRequest.getPassword());
        verify(userRepository).save(user);
        verify(userResponseMapper).toDto(user);
        verifyNoMoreInteractions(userRepository, userRequestMapper, userResponseMapper, passwordEncoder);
    }

    @Test
    void create_whenUsernameAlreadyExists_failed() {
        doReturn(true).when(userRepository).existsByUsername(USERNAME);

        assertThrows(DuplicateException.class, () -> userService.create(getUserRequest()));

        verify(userRepository).existsByUsername(USERNAME);
        verify(userRequestMapper, never()).toEntity(any(UserRequest.class));
        verify(passwordEncoder, never()).encode(any(String.class));
        verify(userRepository, never()).save(any(User.class));
        verify(userResponseMapper, never()).toDto(any(User.class));
        verifyNoMoreInteractions(userRepository, userRequestMapper, userResponseMapper, passwordEncoder);
    }

    @Test
    void loadUserByUsername_whenUserExists_success() {
        User user = getUser();

        doReturn(Optional.of(user)).when(userRepository).findByUsername(USERNAME);

        UserPrincipal userPrincipal = userService.loadUserByUsername(USERNAME);

        assertEquals(user.getId(), userPrincipal.getId());
        assertEquals(user.getUsername(), userPrincipal.getUsername());
        assertEquals(user.getPassword(), userPrincipal.getPassword());

        verify(userRepository).findByUsername(USERNAME);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void loadUserByUsername_whenUserNotExists_failed() {
        doReturn(Optional.empty()).when(userRepository).findByUsername(USERNAME);

        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername(USERNAME));

        verify(userRepository).findByUsername(USERNAME);
        verifyNoMoreInteractions(userRepository);
    }

    private User getUser() {
        return User.builder()
                .id(ID)
                .username(USERNAME)
                .password(PASSWORD)
                .created(NOW)
                .build();
    }

    private UserRequest getUserRequest() {
        return UserRequest.builder()
                .username(USERNAME)
                .password(PASSWORD)
                .build();
    }

    private UserResponse getUserResponse() {
        return UserResponse.builder()
                .id(ID)
                .username(USERNAME)
                .created(NOW)
                .build();
    }
}