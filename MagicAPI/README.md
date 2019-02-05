MagicAPI
========

The public API for the Magic plugin.

This can be used in one of two ways:

## Integrating With Magic

If you wish to integrate with magic, you can use MagicAPI. You may either build it directly into your plugin (the API is meant to be very lightweight), or reference it as "provided" if you intend to hard-depend on Magic.

Alternately, you can use the API in a soft-depend, provided manner if you are sure to use wrapper classes that don't load unless you know Magic is present.

## Extending Magic

Other plugins may deeply extend Magic, such as by adding new spell or effect classes. 

To do so, you may directly link to Magic, or you can try to work within the confines of MagicAPI - think of it as MagicAPI:Magic :: Bukkit:Craftbukkit.

In either case, you will likely be making a hard dependency to make, in which case you can reference both MagicAPI and Magic as "provided" in your pom.xml (for Maven), since the classes will be provided by Magic at runtime.

## Issues

Issues, feature requests, or suggestions for this API should be made at our issue tracker:

https://github.com/elBukkit/MagicPlugin/issues