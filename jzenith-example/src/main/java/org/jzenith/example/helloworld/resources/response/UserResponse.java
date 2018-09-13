package org.jzenith.example.helloworld.resources.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.util.UUID;

@Data
@AllArgsConstructor
public class UserResponse {

    @NonNull
    private UUID id;

    @NonNull
    private String name;
}
