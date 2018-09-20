[![Build Status](https://travis-ci.com/marcust/jzenith.svg?branch=master)](https://travis-ci.com/marcust/jzenith)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=org.jzenith%3Aroot-pom&metric=alert_status)](https://sonarcloud.io/dashboard?id=org.jzenith%3Aroot-pom)
[![Coverage Status](https://coveralls.io/repos/github/marcust/jzenith/badge.svg?branch=master)](https://coveralls.io/github/marcust/jzenith?branch=master)

**Disclaimer**: *This is basically me tinkering with tech and keeping
myself busy while I'm looking for a new challenge to take on. I don't
know if it will ever be released, I don't know if it will ever reach
1.0. Due to this fact the best documentation is currently
`jzenith-example`, though I try to keep this up to date as well. If
you want to know what I'm up to check out the [Project
Plan](https://github.com/marcust/jzenith/projects/1)*

## Overview

jZenith aims to provide out of the box Java based server
applications. It tries to keep up to date with the fast changing Java
world by having a minimal dependency footprint. 

jZenith consists of a *core* that can be extended by using different
*plugins*, currently there is a [PostgreSQL](docs/POSTGRES_PLUGIN.md)
and a [REST](docs/REST_PLUGIN.md) plugin. All bindings are configured
in code in order to allow for a application that is fully initialized
at startup time, hopefully allowing GraalVM support in the future. 

jZenith is basically some glue code between existing Java
libraries. It uses Guice for dependency injection and Vert.x as it's
core technologies. 

Try running the [Example App](docs/EXAMPLE_APP.md) and read about the
different [Plugins](docs/PLUGINS.md).

## Getting started

Snapshots are published to the Sonatype OSS repository, see *Using Snapshots Version*
at the end.

```xml
<dependency>
  <groupId>org.jzenith</groupId>
  <artifactId>jzenith-core</artifactId>
  <version>0.1-SNAPSHOT</version>
</dependency>
```

Clone the repository, run `mvn clean install`. jZenith currently
expects Java 10 and a fairly current maven. 

A typical jZenith main class will look like:
```java
JZenith.application(args)
       .withPlugins(
           RestPlugin.withResources(UserResource.class),
           PostgresqlPlugin.create()
       )
       .withModules(new ServiceLayerModule(), new PersistenceLayerModule(), new MapperModule())
       .withConfiguration("postgresql.database", "test")
       .run();
```

Modules are simply Guice Modules, as jZenith uses Guice for dependency
injection.

Read more about [Plugins](docs/PLUGINS.md) and [Configuration](docs/CONFIGURATION.md).

## Using Snapshot Versions

Add the following repository configurations to your `pom.xml` to enable snapshot versions of this
plugin to be used.

```xml
  <repositories>
    <repository>
      <id>sonatype-nexus-snapshots</id>
      <url>https://oss.sonatype.org/content/repositories/snapshots</url>
      <releases>
        <enabled>false</enabled>
      </releases>
      <snapshots>
        <enabled>true</enabled>
      </snapshots>
    </repository>
  </repositories>
  <pluginRepositories>
    <pluginRepository>
      <id>sonatype-nexus-snapshot</id>
      <url>https://oss.sonatype.org/content/repositories/snapshots</url>
      <releases>
        <enabled>false</enabled>
       </releases>
      <snapshots>
        <enabled>true</enabled>
      </snapshots>
    </pluginRepository>
  </pluginRepositories>
```
