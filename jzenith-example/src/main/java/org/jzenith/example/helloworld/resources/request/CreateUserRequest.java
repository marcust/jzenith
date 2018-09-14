package org.jzenith.example.helloworld.resources.request;

import lombok.*;

@Data
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor
public class CreateUserRequest {

    @NonNull
    private String name;

}
