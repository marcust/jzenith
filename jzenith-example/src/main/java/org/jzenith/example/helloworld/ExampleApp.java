package org.jzenith.example.helloworld;

import org.jzenith.core.JZenith;
import org.jzenith.example.helloworld.persistence.PersistenceLayerModule;
import org.jzenith.example.helloworld.resources.ExampleResource;
import org.jzenith.example.helloworld.service.ServiceLayerModule;
import org.jzenith.rest.RestPlugin;
import org.jzenith.sql.SqlPlugin;
import org.postgresql.ds.PGSimpleDataSource;

/**
 * Example app for simple Rest ExampleApp
 */
public class ExampleApp {
    public static void main(String[] args) {
        JZenith.application(args)
                .withPlugins(
                        RestPlugin.withResources(ExampleResource.class),
                        SqlPlugin.forDataSource(PGSimpleDataSource.class)
                                .username("test")
                                .password("test")
                )
                .withModules(new ServiceLayerModule(), new PersistenceLayerModule())
                .bind(8080)
                .run();
    }
}
