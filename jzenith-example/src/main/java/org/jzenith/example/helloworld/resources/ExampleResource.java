package org.jzenith.example.helloworld.resources;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import org.glassfish.jersey.server.ContainerRequest;
import org.jzenith.example.helloworld.service.HelloWorldService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Path("/")
public class ExampleResource {

    private final HelloWorldService service;

    public ExampleResource(@Context HelloWorldService service) {
        this.service = service;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public void getQuery(
            @Suspended final AsyncResponse response,
            @Context ContainerRequest jerseyRequest,
            @Context HttpServerRequest vertxRequest,
            @Context Vertx vertx) {

        vertx.runOnContext(new Handler<Void>() {
            @Override
            public void handle(Void aVoid) {
                response.resume(service.getResponse());
            }
        });
    }

}
