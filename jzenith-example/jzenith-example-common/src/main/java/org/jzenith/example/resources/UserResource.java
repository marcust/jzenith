/**
 * Copyright © 2018 Marcus Thiesen (marcus@thiesen.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jzenith.example.resources;

import io.reactivex.Single;
import lombok.NonNull;
import org.jzenith.example.mapper.UserMapper;
import org.jzenith.example.resources.request.CreateUserRequest;
import org.jzenith.example.resources.request.UpdateUserRequest;
import org.jzenith.example.resources.response.UserResponse;
import org.jzenith.example.service.UserService;
import org.jzenith.rest.model.Page;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
    public Single<UserResponse> createUser(@NonNull @Valid final CreateUserRequest createUserRequest) {
        return Single.just(createUserRequest)
                .flatMap(request -> userService.createUser(request.getName()))
                .map(userMapper::mapToUserResponse);
    }

    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public Single<Page<UserResponse>> listUsers(@QueryParam("offset") @DefaultValue("0") Integer offset,
                                                @QueryParam("limit") @DefaultValue("20") @Min(1) @Max(100) Integer limit) {
        return userService
                .listUsers(offset, limit)
                .map(userMapper::mapToPageUserResponse);
    }

    @Produces(MediaType.APPLICATION_JSON)
    @GET
    @Path("/{id}")
    public Single<UserResponse> getUser(@NonNull @PathParam("id") final UUID id) {
        return  userService.getById(id)
                .map(userMapper::mapToUserResponse);
    }

    @Produces(MediaType.APPLICATION_JSON)
    @DELETE
    @Path("/{id}")
    public Single<Response> deleteUser(@NonNull @PathParam("id") final UUID id) {
        return userService.deleteById(id)
                .andThen(Single.just(Response.ok().build()));
    }

    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @PUT
    @Path("/{id}")
    public Single<UserResponse> updateUser(@NonNull @PathParam("id") final UUID id, @Valid() final UpdateUserRequest updateUserRequest) {
        return  userService.updateById(id, updateUserRequest.getName())
                .map(userMapper::mapToUserResponse);
    }


}
