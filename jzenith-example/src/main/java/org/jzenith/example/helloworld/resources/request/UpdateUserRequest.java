package org.jzenith.example.helloworld.resources.request;

import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class UpdateUserRequest {

    @NonNull
    @NotBlank
    @Size(max = 100)
    private String name;
}
