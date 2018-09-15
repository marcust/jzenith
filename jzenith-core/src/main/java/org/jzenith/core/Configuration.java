package org.jzenith.core;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.List;

@Builder
@Getter
public class Configuration {

    @NonNull
    private final List<String> commandLineArguments;

    @Builder.Default
    private final int port = 8080;

    @Builder.Default
    private final String host = "localhost";

}
