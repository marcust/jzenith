package org.jzenith.example.helloworld.mapper;

import com.google.inject.AbstractModule;

public class MapperModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(UserMapper.class).toInstance(new UserMapper());

    }
}
