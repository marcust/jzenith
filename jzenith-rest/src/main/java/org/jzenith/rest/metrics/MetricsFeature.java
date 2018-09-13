package org.jzenith.rest.metrics;

import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;

public class MetricsFeature implements DynamicFeature {

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        context.register(new MetricsInterceptor(resourceInfo));
    }
}
