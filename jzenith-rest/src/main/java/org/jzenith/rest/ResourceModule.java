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
package org.jzenith.rest;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.integration.api.OpenAPIConfiguration;

import java.util.List;
import java.util.stream.Collectors;

public class ResourceModule extends AbstractModule {
    private final List<Class<?>> resources;

    public ResourceModule(List<Class<?>> resources) {
        this.resources = ImmutableList.copyOf(resources);
    }

    @Override
    protected void configure() {
        resources.forEach(this::bind);

        bind(OpenAPIConfiguration.class).toInstance(makeSwaggerConfiguration());
    }

    private OpenAPIConfiguration makeSwaggerConfiguration() {
        final SwaggerConfiguration configuration = new SwaggerConfiguration();
        configuration.setResourceClasses(resources.stream().map(Class::getName).collect(ImmutableSet.toImmutableSet()));
        configuration.setPrettyPrint(Boolean.TRUE);
        return configuration;
    }
}
