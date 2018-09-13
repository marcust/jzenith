package org.jzenith.example.helloworld.resources;

import io.reactivex.Single;
import org.jzenith.example.helloworld.mapper.UserMapper;
import org.jzenith.example.helloworld.resources.request.CreateUserRequest;
import org.jzenith.example.helloworld.resources.response.UserResponse;
import org.jzenith.example.helloworld.service.UserService;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

@Path("/user")
public class UserResource {

    private final UserService userService;
    private final UserMapper userMapper;

    public UserResource(@Context UserService userService, @Context UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @POST
    public Single<UserResponse> createUser(final Single<CreateUserRequest> createUserRequest) {
        return createUserRequest
                .flatMap(request -> userService.createUser(request.getName()))
                .map(userMapper::mapToUserResponse);
    }


}
