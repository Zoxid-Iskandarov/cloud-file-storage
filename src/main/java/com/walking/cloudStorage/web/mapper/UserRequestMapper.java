package com.walking.cloudStorage.web.mapper;

import com.walking.cloudStorage.domain.model.User;
import com.walking.cloudStorage.web.dto.user.UserRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserRequestMapper extends Mappable<User, UserRequest> {
}
