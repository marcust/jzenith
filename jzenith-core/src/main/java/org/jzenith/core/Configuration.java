package org.jzenith.core;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class Configuration {

    private final List<String> commandLineArguments;
    private final int port;
    private final String host;

}
