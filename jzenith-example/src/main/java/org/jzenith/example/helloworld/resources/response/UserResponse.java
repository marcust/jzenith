package org.jzenith.example.helloworld.resources.response;

import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserResponse {

    @NonNull
    private UUID id;

    @NonNull
    private String name;
}
