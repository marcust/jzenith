package org.jzenith.kafka.consumer;

import org.junit.jupiter.api.Test;
import org.jzenith.core.JZenithException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HandlerResultTest {

    @Test
    public void testSuccess() {
        final HandlerResult messageHandled = HandlerResult.messageHandled();
        assertThat(messageHandled.hasThrowable()).isFalse();
    }

    @Test
    public void testSuccessThowsException() {
        final HandlerResult messageHandled = HandlerResult.messageHandled();
        assertThrows(IllegalStateException.class, () -> messageHandled.getThrowable());
    }

}
