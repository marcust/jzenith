# Redis Plugin

```xml
<dependency>
  <groupId>org.jzenith</groupId>
  <artifactId>jzenith-redis</artifactId>
  <version>0.1</version>
</dependency>
```

The Redis Plugin gives you
* Vert.x Redis
* FST for fast serialization

The Redis Plugin can be enabled by calling 

```java
JZenith.application(args)
       .withPlugins(
         RedisPlugin.create()
       )
```

Typical DAO implements look like 

```java
public class UserDaoImpl extends RedisDao<User> implements UserDao {

    @Inject
    public UserDaoImpl(FSTConfiguration configuration, RedisClient client) {
        super(configuration, client, User.class);
    }

    @Override
    public Single<User> save(@NonNull User user) {
        return set(user.getId().toString(), user)
                .andThen(Single.just(user));
    }
}
```

Your bean should be `Serializable`, you should also subclass `RedisDao`
for a couple of methods to load and store data.

## Configuration properties
*defined in `RedisConfiguration`*

* `redis.port`: The port PostgreSQL listens to
* `redis.host`: The host to connect to
* `redis.encoding`: The encoding to use for string values

