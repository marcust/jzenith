package org.jzenith.example.helloworld.mapper;

import org.jzenith.example.helloworld.resources.response.UserResponse;
import org.jzenith.example.helloworld.service.model.User;
import org.jzenith.rest.model.Page;

import java.util.List;

public class UserMapper {

    public UserResponse mapToUserResponse(User user) {
        return new UserResponse(user.getId(), user.getName());
    }

    public Page<UserResponse> mapToPageUserResponse(Page<User> users) {
        return users.map(this::mapToUserResponse);
    }
}
