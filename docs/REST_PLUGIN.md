# REST Plugin

Snapshots are published to the Sonatype OSS repository.

```
<dependency>
  <groupId>org.jzenith</groupId>
  <artifactId>jzenith-rest</artifactId>
  <version>0.1-SNAPSHOT</version>
</dependency>
```

The REST Plugin gives you
* Resteasy with enabled validation
* A health check endpoint at `/health`
* A Prometheus metriecs endpoing at `/metrics/prometheus`
* A Swagger/OpenAPI endpoint at `/openapi.json` 

The Rest Plugin can be enabled by calling

```
JZenith.application(args)
       .withPlugins(
         RestPlugin.withResources(UserResource.class)
                   .withMapping(NoSuchUserException.class, 404));
```

*Resources* are default JAX-RS resources that will run via Resteasy. 
*Mappings* are custom exception mappings to allow custom error codes
for your domain exceptions. 

Due to the great support for RxJava in Resteasy you can define
Resources that look like this 

```
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    @Path("/{id}")
    public Single<UserResponse> getUser(@NonNull @PathParam("id") final UUID id) {
        return userService.getById(id)
               .map(userMapper::mapToUserResponse);
    }
```

## Configuration properties
*defined in `RestConfiguration`*

* `rest.port`: port to bind to
* `rest.host`: host to bind to
