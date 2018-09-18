# Example App

The package `jzenith-example` contains a simple example app that
I use to play around with the functionality. As it is using the 
[Postgres Plugin](POSTGRES_PLUGIN.md) it depends on a running
PostgreSQL server. 

In order to build do a regular

`mvn clean package`

This gives you a fat jar file in `target/` that can be run with

`java -jar jzenith-example-0.1-SNAPSHOT-fat.jar`

You can start a dockerized PostgreSQL with 

`mvn docker:start`


