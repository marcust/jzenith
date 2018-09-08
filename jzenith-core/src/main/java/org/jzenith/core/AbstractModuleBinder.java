package org.jzenith.core;

import com.google.inject.AbstractModule;

import java.util.List;

public abstract class AbstractModuleBinder extends AbstractModule {

    @Override
    protected void configure() {
        for (final AbstractModule module : getModules()) {
            install(module);
        }

    }

    protected abstract List<AbstractModule> getModules();
}
