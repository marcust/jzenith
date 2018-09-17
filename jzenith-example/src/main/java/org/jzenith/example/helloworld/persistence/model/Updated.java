package org.jzenith.example.helloworld.persistence.model;

public enum Updated {
    YES, NO;

    public static boolean isUpdated(Updated updated) {
        return updated == YES;
    }
}
