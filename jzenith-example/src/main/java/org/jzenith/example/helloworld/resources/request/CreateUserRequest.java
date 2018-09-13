package org.jzenith.example.helloworld.resources.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class CreateUserRequest {

    @NonNull
    private final String name;

}
