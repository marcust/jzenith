# MongoDB Plugin

```xml
<dependency>
  <groupId>org.jzenith</groupId>
  <artifactId>jzenith-mongodb</artifactId>
  <version>0.1</version>
</dependency>
```

The MongoDB Plugin gives you
* Vert.x MongoDB

The MongoDB Plugin can be enabled by calling 

```java
JZenith.application(args)
       .withPlugins(
         MongoDbPlugin.create("mongodb://localhost:27017")
       )
```

Typical DAO implements look like 

```java
public class UserDaoImpl extends MongoDbDao<User> implements UserDao {

    @Inject
    public UserDaoImpl(MongoClient client) {
        super(client, User.class, user -> user.getId().toString());
    }
   
    @Override
    public Single<User> save(@NonNull User user) {
        return super.insert(user)
                    .andThen(Single.just(user));
    }
}
```

You should also subclass `MongoDbDao`
for a couple of methods to load and store data.

## Configuration properties
*defined in `MongoDbConfiguration`*

* `mongo.db.connect.string`: The connect string for mongodb
* `mongo.db.database.name`: The database to use (defaults to "database")

