MagicAPI
========

The public API for the Magic plugin.

This can be used in one of two ways:

## Integrating With Magic

If you wish to integrate with magic, you can use MagicAPI. You may either build it directly into your plugin (the API is meant to be very lightweight), or reference it as "provided" if you intend to hard-depend on Magic.

Alternately, you can use the API in a soft-depend, provided manner if you are sure to use wrapper classes that don't load unless you know Magic is present.

## Using Maven

You can easily import Magic's API into your project like this:

```
<dependencies>
  <dependency>
      <groupId>com.elmakers.mine.bukkit</groupId>
      <artifactId>MagicAPI</artifactId>
      <version>7.9.6</version>
      <scope>provided</scope>
  </dependency>
</dependencies>
<repositories>
  <!-- Releases are published to github -->
  <repository>
      <id>elmakers-github</id>
      <url>https://maven.pkg.github.com/elBukkit/MagicPlugin</url>
  </repository>
  <!-- Snapshots are published to the elMakers repository, which also has all of the dependencies -->
  <repository>
      <id>elmakers-repo</id>
      <url>http://maven.elmakers.com/repository/</url>
  </repository>
</repositories>
```

## Getting a Reference to the API

```
      MagicAPI getMagicAPI() {
          Plugin magicPlugin = Bukkit.getPluginManager().getPlugin("Magic");
            if (magicPlugin == null || !(magicPlugin instanceof MagicAPI)) {
                return null;
            }
          return (MagicAPI)magicPlugin;
      }
```

## Custom Events

There are a few custom events in the API, but possibly most important is the `PreLoadEvent`. You may want to listent to this even to register custom currencies, protection, team or other providers with Magic.

## Extending Magic

Other plugins may deeply extend Magic, such as by adding new spell or effect classes. 

To do so, you may directly link to Magic, or you can try to work within the confines of MagicAPI - think of it as MagicAPI:Magic :: Bukkit:Craftbukkit.

In either case, you will likely be making a hard dependency to make, in which case you can reference both MagicAPI and Magic as "provided" in your pom.xml (for Maven), since the classes will be provided by Magic at runtime.

## Issues

Issues, feature requests, or suggestions for this API should be made at our issue tracker:

https://github.com/elBukkit/MagicPlugin/issues
