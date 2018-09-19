# Postgres Plugin

```
<dependency>
  <groupId>org.jzenith</groupId>
  <artifactId>jzenith-postgresql</artifactId>
  <version>0.1-SNAPSHOT</version>
</dependency>
```

The Postgres Plugin gives you
* jOOQ
* running with reactive-pg-client
* Flyway for db setup and migration

The Postgres Plugin can be enabled by calling 

```
JZenith.application(args)
       .withPlugins(
         PostgresqlPlugin.create()
       )
```

Typical DAO implements look like 

```
    public Maybe<User> getById(@NonNull UUID id) {
        final Select<?> select = dslContext.select(ID_FIELD, NAME_FIELD)
                .from(USERS_TABLE)
                .where(ID_FIELD.eq(id));

        return client.executeForSingleRow(select)
                .map(this::toUser);
    }
```

Where `DSLContext dslContext` and `PostgresClient client` are
injectable. 

## DB Setup and migration
jZenith uses Flyway to do DB migrations. A file in 

`src/main/resources/db/migrations`

with the naming scheme 

`V1__some_string.sql`

is enough to get the DB schema created on startup.

## Configuration properties
*defined in `PostgresqlConfiguration`*

* `postgresql.port`: The port PostgreSQL listens to
* `postgresql.host`: The host to connect to
* `postgresql.database`: The database to connect to
* `postgresql.username`: The username to use for authentication
* `postgresql.password`: The password to use for authentication
* `postgresql.pool.size`: The pool size for the connection pool.


