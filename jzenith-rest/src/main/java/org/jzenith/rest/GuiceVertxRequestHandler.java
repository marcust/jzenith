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
package org.jzenith.rest;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import org.jboss.resteasy.plugins.server.vertx.VertxRequestHandler;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

public class GuiceVertxRequestHandler extends VertxRequestHandler {
    public GuiceVertxRequestHandler(Vertx vertx, ResteasyDeployment deployment) {
        super(vertx, deployment);
    }

    @Override
    public void handle(HttpServerRequest request) {
        ResteasyProviderFactory.getContextDataMap().put(HttpServerRequest.class, request);
        super.handle(request);
    }
}
