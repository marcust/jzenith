package org.jzenith.rest;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.integration.api.OpenAPIConfiguration;

import java.util.List;
import java.util.stream.Collectors;

public class ResourceModule extends AbstractModule {
    private final List<Class<?>> resources;

    public ResourceModule(List<Class<?>> resources) {
        this.resources = ImmutableList.copyOf(resources);
    }

    @Override
    protected void configure() {
        resources.forEach(this::bind);

        bind(OpenAPIConfiguration.class).toInstance(makeSwaggerConfiguration());
    }

    private OpenAPIConfiguration makeSwaggerConfiguration() {
        final SwaggerConfiguration configuration = new SwaggerConfiguration();
        configuration.setResourceClasses(resources.stream().map(Class::getName).collect(ImmutableSet.toImmutableSet()));
        configuration.setPrettyPrint(Boolean.TRUE);
        return configuration;
    }
}
