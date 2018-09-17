package org.jzenith.rest.docs;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.api.OpenAPIConfiguration;

import javax.inject.Inject;

public class CustomOpenApiResource extends OpenApiResource {

    @Inject
    public CustomOpenApiResource(OpenAPIConfiguration openApiConfiguration) {
        setOpenApiConfiguration(openApiConfiguration);
    }

}
