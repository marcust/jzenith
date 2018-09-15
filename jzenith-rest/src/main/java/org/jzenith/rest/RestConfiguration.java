package org.jzenith.rest;

import org.jzenith.core.configuration.ConfigDefault;

public interface RestConfiguration {

    @ConfigDefault("8080")
    int getPort();

    @ConfigDefault("localhost")
    String getHost();


}
