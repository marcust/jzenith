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

    private final int port;

    private final String host;

    @NonNull
    @Builder.Default
    private final ModuleBindMode moduleBindMode = ModuleBindMode.PARENT;

}
