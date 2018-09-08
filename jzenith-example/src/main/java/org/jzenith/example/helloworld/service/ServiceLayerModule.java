package org.jzenith.example.helloworld.service;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import org.jzenith.example.helloworld.service.impl.HelloWorldServiceImpl;

public class ServiceLayerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(HelloWorldService.class).to(HelloWorldServiceImpl.class).in(Singleton.class);

    }
}

