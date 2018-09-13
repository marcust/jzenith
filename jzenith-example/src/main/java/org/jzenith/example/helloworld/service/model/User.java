package org.jzenith.example.helloworld.service.model;

import lombok.Data;

import java.util.UUID;

@Data
public class User {
    private final UUID id;
    private final String name;

}
