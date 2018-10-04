package org.jzenith.kafka.consumer;

public abstract class HandlerResult {

    public static HandlerResult fail(final Throwable t) {
        return new HandlerResult() {
            @Override
            public boolean isSuccessful() {
                return false;
            }

            @Override
            public boolean hasException() {
                return true;
            }

            @Override
            public Throwable getThrowable() {
                return t;
            }
        };
    }

    public static HandlerResult success() {
        return new HandlerResult() {
            @Override
            public boolean isSuccessful() {
                return true;
            }

            @Override
            public boolean hasException() {
                return false;
            }

            @Override
            public Throwable getThrowable() {
                throw new IllegalStateException("This result is successful");
            }
        };
    }

    public abstract boolean isSuccessful();

    public abstract boolean hasException();

    public abstract Throwable getThrowable();
}
