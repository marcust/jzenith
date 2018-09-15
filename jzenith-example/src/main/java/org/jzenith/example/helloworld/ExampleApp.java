package org.jzenith.example.helloworld;

import org.jzenith.core.JZenith;
import org.jzenith.example.helloworld.mapper.MapperModule;
import org.jzenith.example.helloworld.persistence.PersistenceLayerModule;
import org.jzenith.example.helloworld.resources.HelloWorldResource;
import org.jzenith.example.helloworld.resources.UserResource;
import org.jzenith.example.helloworld.service.ServiceLayerModule;
import org.jzenith.rest.RestPlugin;
import org.jzenith.postgresql.PostgresqlPlugin;

/**
 * Example app for simple Rest ExampleApp
 */
public class ExampleApp {
    public static void main(String[] args) {
        JZenith.application(args)
                .withPlugins(
                        RestPlugin.withResources(HelloWorldResource.class, UserResource.class),
                        PostgresqlPlugin.create()
                )
                .withModules(new ServiceLayerModule(), new PersistenceLayerModule(), new MapperModule())
                .withConfiguration("postgresql.database", "test")
                .withConfiguration("postgresql.username", "test")
                .withConfiguration("postgresql.password", "test")
                .run();
    }
}
