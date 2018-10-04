package org.jzenith.kafka.consumer;

import io.reactivex.Single;

public interface TopicHandler<T> {

    Single<HandlerResult> handleMessage(Single<T> messages);

}
