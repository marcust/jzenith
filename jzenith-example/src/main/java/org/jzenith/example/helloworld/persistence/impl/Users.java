package org.jzenith.example.helloworld.persistence.impl;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;

import java.util.UUID;

import static org.jooq.impl.DSL.*;

class Users {

    static final Table<Record> USERS_TABLE = table(name("users"));

    static final Field<UUID> ID_FIELD = field(name("id"), UUID.class);
    static final Field<String> NAME_FIELD = field(name("name"), String.class);

}
