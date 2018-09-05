package org.jzenith.example.helloworld;

import org.jzenith.core.JZenith;
import org.jzenith.rest.RestModule;

/**
 * Example app for simple Rest ExampleApp
 */
public class ExampleApp {
    public static void main(String[] args) {
        JZenith.application(args)
                .withModule(
                        RestModule.withResources(ExampleResource.class)
                )
                .bind(8080)
                .run();
    }
}
