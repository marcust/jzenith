package org.jzenith.core.util;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.concurrent.CompletableFuture;

public class CompletableHandler<T> extends CompletableFuture<T> {

    public Handler<AsyncResult<T>> handler() {
        return event -> {
            if (event.failed()) {
                completeExceptionally(event.cause());
            } else {
                complete(event.result());
            }
        };
    }


}
