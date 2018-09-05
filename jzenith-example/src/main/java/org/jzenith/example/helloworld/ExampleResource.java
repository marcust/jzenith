package org.jzenith.example.helloworld;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import org.glassfish.jersey.server.ContainerRequest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Path("/")
public class ExampleResource {

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
                response.resume("Hello World!");
            }
        });
    }

}
