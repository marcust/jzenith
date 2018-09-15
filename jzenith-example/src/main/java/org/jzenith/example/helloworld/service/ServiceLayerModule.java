package org.jzenith.example.helloworld.service;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import org.jzenith.example.helloworld.service.impl.HelloWorldServiceImpl;
import org.jzenith.example.helloworld.service.impl.UserServiceImpl;

public class ServiceLayerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(HelloWorldService.class).to(HelloWorldServiceImpl.class).asEagerSingleton();
        bind(UserService.class).to(UserServiceImpl.class).asEagerSingleton();
    }
}

