package com.walking.cloudStorage.web.mapper;

import com.walking.cloudStorage.domain.model.User;
import com.walking.cloudStorage.web.dto.user.UserRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserRequestMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created", ignore = true)
    User toEntity(UserRequest userRequest);
}
