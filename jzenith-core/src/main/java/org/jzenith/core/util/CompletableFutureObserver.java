package org.jzenith.core.util;

import io.reactivex.CompletableObserver;
import io.reactivex.disposables.Disposable;

import java.util.concurrent.CompletableFuture;

public class CompletableFutureObserver extends CompletableFuture<Void> {

    public CompletableObserver observer() {
        return new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onComplete() {
                complete(null);
            }

            @Override
            public void onError(Throwable e) {
                completeExceptionally(e);
            }
        };
    }
}
