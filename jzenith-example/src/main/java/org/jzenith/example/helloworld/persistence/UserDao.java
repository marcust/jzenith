package org.jzenith.example.helloworld.persistence;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import org.jzenith.example.helloworld.persistence.model.Deleted;
import org.jzenith.example.helloworld.persistence.model.Updated;
import org.jzenith.example.helloworld.service.model.User;
import org.jzenith.rest.model.Page;

import java.util.UUID;

public interface UserDao {

    Single<User> save(User user);

    Maybe<User> getById(UUID id);

    Maybe<Updated> updateNameById(UUID id, String name);

    Single<Page<User>> listUsers(Integer offset, Integer limit);

    Maybe<Deleted> deleteById(UUID id);
}
