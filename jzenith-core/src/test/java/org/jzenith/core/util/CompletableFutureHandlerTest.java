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
package org.jzenith.core.util;

import io.vertx.core.Future;
import org.junit.Test;
import org.jzenith.core.JZenithException;

import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class CompletableFutureHandlerTest {

    @Test
    public void testResult() throws ExecutionException, InterruptedException {
        final CompletableFutureHandler<String> completableFutureHandler = new CompletableFutureHandler<>();
        completableFutureHandler.handler().handle(Future.succeededFuture("test"));

        assertThat(completableFutureHandler.get()).isEqualTo("test");
    }

    @Test
    public void testError() throws InterruptedException {
        final CompletableFutureHandler<String> completableFutureHandler = new CompletableFutureHandler<>();
        completableFutureHandler.handler().handle(Future.failedFuture(new JZenithException("error")));

        try {
            completableFutureHandler.get();
            fail("Should not get here");
        } catch (ExecutionException e) {
            assertThat(e.getCause()).isInstanceOf(JZenithException.class);
        }
    }

}
