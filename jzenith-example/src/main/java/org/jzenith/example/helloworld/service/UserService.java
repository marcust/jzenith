package org.jzenith.example.helloworld.service;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import org.jzenith.example.helloworld.service.model.User;
import org.jzenith.rest.model.Page;

import java.util.UUID;

public interface UserService {

    Single<User> createUser(String name);

    Single<User> getById(UUID id);

    Single<User> updateById(UUID id, String name);

    Single<Page<User>> listUsers(Integer offset, Integer limit);

    Completable deleteById(UUID id);
}
