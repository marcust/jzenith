package org.jzenith.example.helloworld.service;

import io.reactivex.Maybe;
import io.reactivex.Single;
import org.jzenith.example.helloworld.service.model.User;

import java.util.UUID;

public interface UserService {

    Single<User> createUser(String name);

    Maybe<User> getById(UUID id);
}
