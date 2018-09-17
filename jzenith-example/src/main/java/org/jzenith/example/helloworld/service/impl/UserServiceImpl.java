package org.jzenith.example.helloworld.service.impl;

import io.reactivex.Maybe;
import io.reactivex.Single;
import lombok.NonNull;
import org.jzenith.example.helloworld.persistence.UserDao;
import org.jzenith.example.helloworld.service.UserService;
import org.jzenith.example.helloworld.service.model.User;
import org.jzenith.rest.model.Page;

import javax.inject.Inject;
import java.util.UUID;

public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    @Inject
    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public Single<User> createUser(@NonNull String name) {
        return userDao.save(new User(UUID.randomUUID(), name));
    }

    @Override
    public Maybe<User> getById(@NonNull UUID id) {
        return userDao.getById(id);
    }

    @Override
    public Maybe<User> updateById(@NonNull UUID id, @NonNull String name) {
        return userDao.updateNameById(id, name);
    }

    @Override
    public Single<Page<User>> listUsers(@NonNull Integer offset, @NonNull Integer limit) {
        return userDao.listUsers(offset, limit);
    }
}
