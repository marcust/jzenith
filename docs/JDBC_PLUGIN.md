# JDBC Plugin

```xml
<dependency>
  <groupId>org.jzenith</groupId>
  <artifactId>jzenith-jdbc</artifactId>
  <version>0.1-SNAPSHOT</version>
</dependency>
```

The JDBC Plugin gives you
* jOOQ
* running with rxjava2-jdbc
* Flyway for db setup and migration

The JDBC Plugin can be enabled by calling 

```java
JZenith.application(args)
       .withPlugins(
         JdbcPlugin.create(dataSource, JdbcDatabaseType.POSTGRES)
       )
```

Typical DAO implements look like 

```java
    public Maybe<User> getById(@NonNull UUID id) {
        final Select<?> select = dslContext.select(ID_FIELD, NAME_FIELD)
                .from(USERS_TABLE)
                .where(ID_FIELD.eq(id));

        return client.executeForSingleRow(select)
                .map(this::toUser);
    }
```

Where `DSLContext dslContext` and `JdbcClient client` are
injectable. 

## DB Setup and migration
jZenith uses Flyway to do DB migrations. A file in 

`src/main/resources/db/migrations`

with the naming scheme 

`V1__some_string.sql`

is enough to get the DB schema created on startup.

## Configuration properties
None, because the JDBC Plugin requires you to give it 
a `DataSource` instance. 

You can initialize that completely manually, like e.g. for
Postgres

```java
final PGSimpleDataSource dataSource = new PGSimpleDataSource();
dataSource.setPortNumber(5433);
dataSource.setServerName("localhost");
dataSource.setDatabaseName("test");
dataSource.setUser("test");
dataSource.setPassword("test");
```


