package org.jzenith.example.helloworld;

import org.jzenith.core.JZenith;
import org.jzenith.example.helloworld.persistence.PersistenceLayerModule;
import org.jzenith.example.helloworld.service.ServiceLayerModule;
import org.jzenith.rest.RestPlugin;

/**
 * Example app for simple Rest ExampleApp
 */
public class ExampleApp {
    public static void main(String[] args) {
        JZenith.application(args)
                .withPlugins(
                        RestPlugin.withResources(ExampleResource.class)
                )
                .withModules(new ServiceLayerModule(), new PersistenceLayerModule())
                .bind(8080)
                .run();
    }
}
