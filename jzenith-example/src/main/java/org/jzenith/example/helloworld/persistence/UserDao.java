package org.jzenith.example.helloworld.persistence;

import io.reactivex.Maybe;
import io.reactivex.Single;
import org.jzenith.example.helloworld.service.model.User;

import java.util.UUID;

public interface UserDao {

    Single<User> save(User user);

    Maybe<User> getById(UUID id);
}
