package org.jzenith.core.metrics;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.hotspot.DefaultExports;

public class JZenithDefaultExports {

    public static void initialize() {
        DefaultExports.initialize();

        final CollectorRegistry defaultRegistry = CollectorRegistry.defaultRegistry;
        defaultRegistry.register(new JvmOptionMetricCollector());
    }


}
