package com.walking.cloudStorage.web.mapper;

import com.walking.cloudStorage.domain.model.User;
import com.walking.cloudStorage.web.dto.UserResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserResponseMapper extends Mappable<User, UserResponse> {
}
