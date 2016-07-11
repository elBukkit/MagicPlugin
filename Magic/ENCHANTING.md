# Wand Enchanting

Wand enchanting works using one or more enchanting paths. These paths are defined in enchanting.defaults.yml
and enchanting.yml.

As always, any customizations should be made in enchanting.yml. If you want to disable one of the default
paths, set "enabled" to "false", such as:

random:
  enabled: false

Wands as defined in wands.defaults.yml or wands.yml can be assigned an enchanting path, and generally default to the
"random" path, which has access to nearly all spells.

There are additional paths set up for lowered powered wands or specific paths like Engineering.

SEE WANDS.md for a complete list of available wand properties, and wands.defaults.yml for a detailed description on
customizing wand enchanting.