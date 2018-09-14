package org.jzenith.rest;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;

import java.util.List;

public class ResourceModule extends AbstractModule {
    private final List<Class<?>> resources;

    public ResourceModule(List<Class<?>> resources) {
        this.resources = ImmutableList.copyOf(resources);
    }

    @Override
    protected void configure() {
        resources.forEach(this::bind);
    }
}
