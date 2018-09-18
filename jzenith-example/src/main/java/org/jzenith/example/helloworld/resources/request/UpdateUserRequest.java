package org.jzenith.example.helloworld.resources.request;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class UpdateUserRequest {

    @NonNull
    @NotBlank
    @Size(max = 100)
    private String name;
}
