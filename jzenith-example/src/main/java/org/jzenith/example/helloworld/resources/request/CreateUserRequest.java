package org.jzenith.example.helloworld.resources.request;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreateUserRequest {

    @NonNull
    @NotBlank
    @Size(max = 100)
    private String name;

}
