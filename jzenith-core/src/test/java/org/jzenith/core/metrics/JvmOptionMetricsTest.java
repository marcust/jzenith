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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JvmOptionMetricsTest {

    @Test
    public void testJvmOptionMetrics() {
        final JvmOptionMetrics metrics = new JvmOptionMetrics();

        assertThat(metrics.getMaxDirectMemorySize()).isEqualTo(0.0D);
        assertThat(metrics.getThreadStackSize()).isEqualTo(1048576.0D);
    }

}
