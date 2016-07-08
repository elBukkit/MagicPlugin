# Customizing Magic

Magic is meant to be a highly customizable plugin to tailor to the style of the server.

Configuring Magic is a little different, but easy once you know how. Magic is set up to allow for deep alteration
of all of the plugin's functionality, while still allowing for upgrades. It does this by providing a default, read-only
configuration that you may override. 

The default configuration will update when you update the plugin. Magic is highly configuration-driven, so new spells, bug
fixes, or rebalancing will be done in the config files, and if your configs don't update, you don't get those fixes.

The default files can be found here:

https://github.com/elBukkit/MagicPlugin/tree/master/src/main/resources/defaults

Or in your plugin/Magic/defaults folder once installed.

These files are good reference for all of the available configuration options, and the default behavior of the plugin.

If you want to change something you see in the defaults, add an entry to the corresponding file in plugins/Magic/.

For instance, if you want to disable wand crafting, add

```
enable_crafting: false
```

to plugins/Magic/config.yml

If you want to give the "kaboom" spell a longer cooldown, this is all you need to add to spells.yml:

```
kaboom:
  parameters:
    cooldown: 60000
```

Note that all duration values in Magic are in milliseconds. This can be confusing at first, but is consistent and tick-agnostic.

So, this would give the KaBoom spell a 1-minute cooldown.

# Messaging and Localization

All in-game text is contained in messages.defaults.yml. You may override any of the messaging as you would any of the
other configuration files. For instance, to change the cast message for nuke, you would add the following to plugins/Magic/messages.yml

```
spells:
  nuke:
    cast: Oh dear lord, no!!!!
```
