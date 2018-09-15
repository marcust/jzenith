package org.jzenith.example.helloworld.persistence;

import io.reactivex.Single;
import org.jzenith.example.helloworld.service.model.User;

public interface UserDao {

    Single<User> save(User user);
}
