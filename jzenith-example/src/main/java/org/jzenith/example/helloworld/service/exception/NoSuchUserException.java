package org.jzenith.example.helloworld.service.exception;

import java.util.UUID;

public class NoSuchUserException extends Exception {

    private final UUID uuid;

    public NoSuchUserException(UUID uuid) {
        super("No such user " + uuid);
        this.uuid = uuid;
    }
}
