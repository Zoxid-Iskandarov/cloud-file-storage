package com.walking.cloudStorage.web.mapper;

import com.walking.cloudStorage.domain.model.User;
import com.walking.cloudStorage.web.dto.user.UserResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserResponseMapper {
    UserResponse toDto(User user);
}
