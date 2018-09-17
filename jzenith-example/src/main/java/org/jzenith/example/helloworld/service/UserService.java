package org.jzenith.example.helloworld.service;

import io.reactivex.Maybe;
import io.reactivex.Single;
import org.jzenith.example.helloworld.service.model.User;
import org.jzenith.rest.model.Page;

import java.util.UUID;

public interface UserService {

    Single<User> createUser(String name);

    Maybe<User> getById(UUID id);

    Maybe<User> updateById(UUID id, String name);

    Single<Page<User>> listUsers(Integer offset, Integer limit);
}
