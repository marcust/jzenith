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
package org.jzenith.example;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.jzenith.core.JZenith;

public class JZenithResourceIT extends AbstractJZenithResourcesTest {

    private static JZenith jZenith;

    @BeforeClass
    public static void startup() throws Exception {
        jZenith = ExampleApp.configureApplication();
        jZenith.run();
    }

    @AfterClass
    public static void shutdown() {
        if (jZenith != null) {
            jZenith.stop();
        }
    }
}
