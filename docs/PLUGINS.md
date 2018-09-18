# Plugins

* [PostgreSQL](POSTGRES_PLUGIN.md)
* [REST](REST_PLUGIN.md)

## Writing Plugins

*TODO Document*

Basically implement `AbstractPlugin` from core, that gives you the
injector and expect a `CompletableFuture` once you are done with your
setup. 
