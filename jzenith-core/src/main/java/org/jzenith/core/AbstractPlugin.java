package org.jzenith.core;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import com.google.inject.Module;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractPlugin {

    protected List<Module> getModules() {
        return ImmutableList.of();
    }

    protected abstract CompletableFuture<String> start(Injector deploymentOptions);

}
