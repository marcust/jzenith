package org.jzenith.example.helloworld.resources;

import io.reactivex.Maybe;
import io.reactivex.Single;
import lombok.NonNull;
import org.jzenith.example.helloworld.mapper.UserMapper;
import org.jzenith.example.helloworld.resources.request.CreateUserRequest;
import org.jzenith.example.helloworld.resources.response.UserResponse;
import org.jzenith.example.helloworld.service.UserService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.UUID;

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
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    public Single<UserResponse> createUser(@NonNull final CreateUserRequest createUserRequest) {
        return Single.just(createUserRequest)
                .flatMap(request -> userService.createUser(request.getName()))
                .map(userMapper::mapToUserResponse);
    }

    @Produces(MediaType.APPLICATION_JSON)
    @GET
    @Path("/{id}")
    public Single<UserResponse> getUser(@NonNull @PathParam("id") final UUID id) {
        return  userService.getById(id)
                .switchIfEmpty(Single.error(new NotFoundException("No user with " + id + " found")))
                .map(userMapper::mapToUserResponse);
    }



}
