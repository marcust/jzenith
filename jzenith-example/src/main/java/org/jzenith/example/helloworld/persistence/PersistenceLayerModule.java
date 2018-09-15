package org.jzenith.example.helloworld.persistence;

import com.google.inject.AbstractModule;
import org.jzenith.example.helloworld.persistence.impl.UserDaoImpl;

public class PersistenceLayerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(UserDao.class).to(UserDaoImpl.class).asEagerSingleton();
    }
}
