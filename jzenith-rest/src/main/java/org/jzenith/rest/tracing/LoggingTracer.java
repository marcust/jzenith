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
package org.jzenith.rest.tracing;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.contrib.reporter.TracerR;
import io.opentracing.contrib.reporter.slf4j.Slf4jReporter;
import io.opentracing.noop.NoopTracerFactory;
import org.slf4j.LoggerFactory;

public class LoggingTracer extends TracerR implements Tracer {

    public LoggingTracer() {
        super(NoopTracerFactory.create(),
                new Slf4jReporter(LoggerFactory.getLogger("opentracing"), true),
                new RequestScopedScopeManager());
    }


    @Override
    public Scope activateSpan(Span span) {
        return scopeManager().activate(span);
    }

    @Override
    public void close() {

    }
}
