# Configuration

Plugin Configuration is based on a per plugin base. All configuration
properties come in multiple forms, most common the property form
`rest.port` and the environment variable form `REST_PORT`. 

jZenith will automatically pick up configuation in the following
order:
* bound via the `JZenith.withConfiguration` method
* set via command line argument (`--restPort`)
* directly from an environment variable (`REST_PORT`)
* from a property file in the class root, either per plugin
  (`rest.properties`) or the global configuration file
  (`jzenith.properties`)
* the value that is actually defined as default on the configuration
  interface 
  
Wherever jZenith get the value from, it will still support environment
variable expansion, thus something like 

```
JZenith.withConfiguration("rest.port","$PORT");
```

will behave as expected. 

