package org.jzenith.example.helloworld.service.impl;

import io.reactivex.Single;
import org.jzenith.example.helloworld.service.UserService;
import org.jzenith.example.helloworld.service.model.User;

public class UserServiceImpl implements UserService {
    @Override
    public Single<User> createUser(String name) {
        return null;
    }
}
