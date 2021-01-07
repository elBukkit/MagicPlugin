MagicAPI
========

The public API for the Magic plugin.

This can be used in one of two ways:

## Integrating With Magic

If you wish to integrate with magic, you can easily add it to your project with Maven, or download and reference it directly.

Make sure to add Magic to your `depends` list if you intend to hard-depend on Magic.

Alternately, you can use the API as a `soft-depends`, if you are sure to use wrapper classes that don't load unless you know Magic is present.

## Using Maven

You can easily import Magic's API into your project like this:

```
<dependencies>
  <dependency>
      <groupId>com.elmakers.mine.bukkit</groupId>
      <artifactId>MagicAPI</artifactId>
      <version>8.2</version>
      <scope>provided</scope>
  </dependency>
</dependencies>
<repositories>
  <!-- Releases are published to github -->
  <repository>
      <id>Maven Central</id>
      <url>https://repo1.maven.org/maven2/</url>
  </repository>
  <!-- Snapshots and the full plugin are published to the elMakers repository, which also has all of the dependencies. Only use this if necessary! -->
  <repository>
      <id>elmakers-repo</id>
      <url>http://maven.elmakers.com/repository/</url>
  </repository>
</repositories>
```

## Using Gradle

Alternatively, you can import Magic's API with Gradle like this:

```
repositories {
    maven {
            url 'http://maven.elmakers.com/repository/'
    }
}

dependencies {
    compileOnly 'com.elmakers.mine.bukkit:MagicAPI:7.8'
}
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

## Extending Magic

Other plugins may deeply extend Magic, such as by adding new spell or effect classes. 

To do so, you may directly link to Magic, or you can try to work within the confines of MagicAPI - think of it as MagicAPI:Magic :: Bukkit:Craftbukkit.

In either case, you will likely be making a hard dependency to Magic, in which case you can reference both MagicAPI and Magic as "provided" in your pom.xml (for Maven), since the classes will be provided by Magic at runtime.

## Issues

Issues, feature requests, or suggestions for this API should be made at our issue tracker:

https://github.com/elBukkit/MagicPlugin/issues
