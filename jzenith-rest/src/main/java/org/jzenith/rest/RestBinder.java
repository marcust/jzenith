package org.jzenith.rest;

import com.google.inject.AbstractModule;
import org.jzenith.rest.metrics.MetricsFeature;

public class RestBinder extends AbstractModule {

    @Override
    protected void configure() {
        bind(MetricsFeature.class);

    }


}
