MagicLib
========

This is a utility library used by Magic and MagicWorlds.

You are also welcome to use it, though I'm working out the details. You have a few options:

- Shade in this library and MagicAPI (which this lib relies on) to avoid conficting with Magic. Do
  this if you just want to use this library, but not interact with Magic at all.
- Include this library and MagicAPI as "provided", and depend on the Magic plugin to provide it
- Shade in this library but leave the API as "provided" to work as as stand-alone plugin, or integrate with Magic.
  Still working this one out, currently MaterialAndData is an issue.

Issues, feature requests, or suggestions for this lib should be made at our issue tracker:

http://jira.elmakers.com/browse/LIB/