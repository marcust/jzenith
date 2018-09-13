package org.jzenith.example.helloworld.service;

import io.reactivex.Single;
import org.jzenith.example.helloworld.service.model.User;

public interface UserService {

    Single<User> createUser(final String name);

}
