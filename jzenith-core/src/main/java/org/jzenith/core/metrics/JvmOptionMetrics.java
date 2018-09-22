/**
 * Copyright © 2018 Marcus Thiesen (marcus@thiesen.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jzenith.core.metrics;

import com.sun.management.HotSpotDiagnosticMXBean;
import com.sun.management.VMOption;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;


@Slf4j
public class JvmOptionMetrics implements MeterBinder {


    private HotSpotDiagnosticMXBean platformMXBean;

    public JvmOptionMetrics() {
        platformMXBean = ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class);
    }

    public double getThreadStackSize() {
        return accessOptionValue("ThreadStackSize") * 1024;
    }

    public double getMaxDirectMemorySize() {
        return accessOptionValue("MaxDirectMemorySize");
    }

    private double accessOptionValue(final String optionName) {
        try {
            final VMOption maxDirectMemorySize = platformMXBean.getVMOption(optionName);

            return Double.parseDouble(maxDirectMemorySize.getValue());
        } catch (Exception e) {
            log.warn("Can not get {}, will return NaN", optionName, e);

            return Double.NaN;
        }
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        Gauge.builder("jvm.memory.direct.memory.bytes.max", this, JvmOptionMetrics::getMaxDirectMemorySize)
                .baseUnit("bytes")
                .description("Configured maximum value for direct memory")
                .register(registry);

        Gauge.builder("jvm.threads.stack.size.bytes.max", this, JvmOptionMetrics::getThreadStackSize)
                .baseUnit("bytes")
                .description("Configured maximum for thread stack size")
                .register(registry);

    }
}
