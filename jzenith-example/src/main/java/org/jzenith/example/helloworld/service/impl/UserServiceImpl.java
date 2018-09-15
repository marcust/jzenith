package org.jzenith.example.helloworld.service.impl;

import io.reactivex.Single;
import org.jzenith.example.helloworld.persistence.UserDao;
import org.jzenith.example.helloworld.service.UserService;
import org.jzenith.example.helloworld.service.model.User;

import javax.inject.Inject;
import java.util.UUID;

public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    @Inject
    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public Single<User> createUser(String name) {
        return userDao.save(new User(UUID.randomUUID(), name));
    }
}
