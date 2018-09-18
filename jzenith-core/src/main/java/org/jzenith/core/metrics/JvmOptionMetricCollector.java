/**
 * Copyright (C) 2018 Marcus Thiesen (marcus@thiesen.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jzenith.core.metrics;

import com.google.common.collect.ImmutableList;
import com.sun.management.HotSpotDiagnosticMXBean;
import com.sun.management.VMOption;
import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;
import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;
import java.util.List;


@Slf4j
class JvmOptionMetricCollector extends Collector {

    private final long maxDirectMemoryValueInBytes;
    private final long threadStackSize;

    JvmOptionMetricCollector() {
        this.maxDirectMemoryValueInBytes = accessMaxDirectMemoryValue("MaxDirectMemorySize");
        this.threadStackSize = accessMaxDirectMemoryValue("ThreadStackSize") * 1024; // is in KB for some reason
    }

    private static long accessMaxDirectMemoryValue(final String optionName) {
        try {
            final HotSpotDiagnosticMXBean platformMXBean = ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class);
            final VMOption maxDirectMemorySize = platformMXBean.getVMOption(optionName);

            return Long.parseLong(maxDirectMemorySize.getValue());
        } catch (Throwable e) {
            log.warn("Can not get {}, will return -1", optionName, e);

            return -1L;
        }
    }

    @Override
    public List<MetricFamilySamples> collect() {
        return ImmutableList.of(
                new GaugeMetricFamily(
                        "jvm_memory_direct_memory_bytes_max",
                        "Configured max for direct memory.",
                        maxDirectMemoryValueInBytes),
                new GaugeMetricFamily(
                        "jvm_threads_stack_size_bytes_max",
                        "Configured max for thread stack size.",
                        threadStackSize)
        );
    }
}
