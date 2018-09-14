package org.jzenith.example.helloworld.resources;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import org.jzenith.example.helloworld.service.HelloWorldService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Path("/")
public class HelloWorldResource {

    private final HelloWorldService service;

    @Inject
    public HelloWorldResource(HelloWorldService service) {
        this.service = service;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public void getQuery(
            @Suspended final AsyncResponse response,
            @Context HttpServerRequest vertxRequest,
            @Context Vertx vertx) {

        vertx.runOnContext(new Handler<Void>() {
            @Override
            public void handle(Void aVoid) {
                try {
                    response.resume(service.getResponse());
                } catch (final Exception e) {
                    response.resume(e);
                }
            }
        });
    }

}
