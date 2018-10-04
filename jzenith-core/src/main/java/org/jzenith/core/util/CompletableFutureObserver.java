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
