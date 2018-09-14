package org.jzenith.example.helloworld.resources;

import io.reactivex.Single;
import lombok.NonNull;
import org.jzenith.example.helloworld.mapper.UserMapper;
import org.jzenith.example.helloworld.resources.request.CreateUserRequest;
import org.jzenith.example.helloworld.resources.response.UserResponse;
import org.jzenith.example.helloworld.service.UserService;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

@Path("/user")
public class UserResource {

    private final UserService userService;
    private final UserMapper userMapper;

    @Inject
    public UserResource(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    public Single<UserResponse> createUser(@NonNull final CreateUserRequest createUserRequest) {
        return Single.just(createUserRequest)
                .flatMap(request -> userService.createUser(request.getName()))
                .map(userMapper::mapToUserResponse);
    }


}
