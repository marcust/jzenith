package org.jzenith.example.helloworld.mapper;

import org.jzenith.example.helloworld.resources.response.UserResponse;
import org.jzenith.example.helloworld.service.model.User;

public class UserMapper {

    public UserResponse mapToUserResponse(User user) {
        return new UserResponse(user.getId(), user.getName());
    }

}
