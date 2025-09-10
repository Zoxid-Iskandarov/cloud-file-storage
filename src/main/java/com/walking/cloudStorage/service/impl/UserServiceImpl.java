package com.walking.cloudStorage.service.impl;

import com.walking.cloudStorage.config.security.UserPrincipal;
import com.walking.cloudStorage.domain.exception.DuplicateException;
import com.walking.cloudStorage.domain.exception.ObjectNotFoundException;
import com.walking.cloudStorage.respository.UserRepository;
import com.walking.cloudStorage.service.UserService;
import com.walking.cloudStorage.web.dto.UserRequest;
import com.walking.cloudStorage.web.dto.UserResponse;
import com.walking.cloudStorage.web.mapper.UserRequestMapper;
import com.walking.cloudStorage.web.mapper.UserResponseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRequestMapper userRequestMapper;
    private final UserResponseMapper userResponseMapper;

    @Override
    public UserResponse getById(Long id) {
        return userRepository.findById(id)
                .map(userResponseMapper::toDto)
                .orElseThrow(() -> new ObjectNotFoundException("User with id '%d' not found".formatted(id)));
    }

    @Override
    public UserResponse create(UserRequest userRequest) {
        if (userRepository.existsByUsername(userRequest.getUsername())) {
            throw new DuplicateException("Username '%s' already exists".formatted(userRequest.getUsername()));
        }

        return Optional.of(userRequest)
                .map(userRequestMapper::toEntity)
                .map(user -> {
                    user.setPassword(passwordEncoder.encode(user.getPassword()));
                    return userRepository.save(user);
                })
                .map(userResponseMapper::toDto)
                .orElseThrow();
    }

    @Override
    public UserPrincipal loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .map(user -> new UserPrincipal(
                        user.getId(),
                        user.getUsername(),
                        user.getPassword()))
                .orElseThrow(() -> new UsernameNotFoundException("Username '%s' not found".formatted(username)));
    }
}
