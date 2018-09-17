package org.jzenith.example.helloworld.persistence.model;

public enum Deleted {

    YES, NO;

    public static boolean isDeleted(Deleted deleted) {
        return deleted == YES;
    }
}
