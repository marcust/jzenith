# jZenith

An opinionated approach to building modern Java Microservices

**Disclaimer**: *This is basically me tinkering with tech and keeping
myself busy while I'm looking for a new challenge to take on. I don't
know if it will ever be released, I don't know if it will ever reach
1.0. Due to this fact the best documentation is currently
`jzenith-example`, though I try to keep this up to date as well.*

## Overview

jZenith aims to provide out of the box Java based server
applications. It tries to keep up to date with the fast changing Java
world by having a minimal dependency footprint. 

jZenith consists of a *core* that can be extended by using different
*plugins*, currently there is a [PostgreSQL](docs/POSTGRES_PLUGIN.md)
and a [REST](docs/REST_PLUGIN.md) plugin.

jZenith is basically some glue code between existing Java
libraries. It uses Guice for dependency injection and Vert.x as it's
core technologies. 

Try running the [Example App](docs/EXAMPLE_APP.md) and read about the
different [Plugins](docs/PLUGINS.md).

## Getting started

*No Maven artefacts yet*

Clone the repository, run `mvn clean install`. jZenith currently
expects Java 10 and a fairly current maven. 

A typical jZenith main class will look like:
```
JZenith.application(args)
                .withPlugins(
                        RestPlugin.withResources(HelloWorldResource.class, UserResource.class)
                        PostgresqlPlugin.create()
                )
                .withModules(new ServiceLayerModule(), new PersistenceLayerModule(), new MapperModule())
                .withConfiguration("postgresql.database", "test")
				.run();
```

