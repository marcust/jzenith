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
package org.jzenith.kafka.consumer;

public abstract class HandlerResult {

    public static HandlerResult fail(final Throwable t) {
        return new HandlerResult() {

            @Override
            public boolean hasThrowable() {
                return true;
            }

            @Override
            public Throwable getThrowable() {
                return t;
            }
        };
    }

    public static HandlerResult messageHandled() {
        return new HandlerResult() {

            @Override
            public boolean hasThrowable() {
                return false;
            }

            @Override
            public Throwable getThrowable() {
                throw new IllegalStateException("This result is successful");
            }
        };
    }

    public abstract boolean hasThrowable();

    public abstract Throwable getThrowable();
}
