# Spell Scripting

Any of these parameters may be used in spells.yml, or via command-line, command blocks or any
plugins that support forcing Bukkit commands. If used in spells.yml, these go in the "parameters" map.

Examples of usage can always be found in the spells.defaults.yml file:

https://github.com/elBukkit/MagicPlugin/blob/master/src/main/resources/defaults/spells.defaults.yml

If used command-line, they are paired as parameters, and can be used with /cast or /castp. Some examples:

Encase the player in a glass shell
castp @p shell target self material glass

Create an automated flare sentry, will always fire straight up
cast flare pdy 1 pdx 0 pdz 0 py ~5

For clarity, it is best to use the full parameter name in spells.yml - the shorthand names are meant for command block use.

## Common Parameters

| Name                | Shorthand | Values       | Description
| cooldown            | cool      | milliseconds | How long to wait between casts
| range               |           | # blocks     | How far away the spell may target
| allow_max_range     |           | true/false   | Whether or not a spell will cast without a target
| prevent_passthrough |           | materials    | A list of materials that can't be passed through
| transparent         |           | materials    | Which materials a spell considers transparent and can target through
| fizzle_chance       |           | 0.0 - 1.0    | Percent chance a spell will fizzle
| backfire_chance     |           | 0.0 - 1.0    | Percent chance a spell will backfire
| target              |           | target type  | How this spell's targeting will work (block, self, other, any)
| target_npc          |           | true/false   | Whether or not to target NPCs
| target_type         |           | entity type  | The type of entity to target (Player, LivingEntity)
| px, py, pz, pworld  |           | block coords | Override the source (Player) location for this spell cast
| pdx, pdy, pdz       |           | vector       | Directional vector override for player source
| tx, ty, tz, tworld  |           | block coords | Target location override - relative positions will be from player location
| otx, oty, otz       |           | vector       | Offset for target location, relative to targeted block
| bypass_build        | bb        | true/false   | Bypass build restrictions (e.g. WorldGuard / Factions permissions)
| bypass_pvp          | bp        | true/false   | Bypass PVP restrictions (e.g. WorldGuard permissions)
| cost_reduction      |           | 0.0 - 1.0    | Percent cost reduction, may not be needed for /cast
| cooldown_reduction  |           | 0.0 - 1.0    | Percent cooldown reduction, use "cooldown" to disable cooldowns

## Activated Spells

These are spells that are activated, and generally drain mana or are
deactivated after a certain time.

| Name         | Shorthand | Values   | Description
| duration     | duration  | millis   | How long before the spell deactivates

## Selection (Two-Click) Spells

| Name          | Shorthand | Values         | Description
| t2x, t2y, t2z |           | block coords   | Initial (first-click) target, relative positions will be from player location

## Block Modification Spells

| Name               | Shorthand | Values         | Description
| indestructible     | id        | materials      | Which materials a spell will not ever modify
| destructible       |           | materials      | Which materials a spell will modify if check_destructible is true (the default)
| check_destructible | cd        | true/false     | Whether or not to check the "destructible" list when modifying blocks
| bypass_undo        | bu        | true/false     | Use to bypass the undo queue, useful for command blocks that use /cast and not /castp (and won't ever undo)

## Brush Spells

| Name             | Shorthand | Values         | Description
| material         | m         | material       | The material to use, overriding the player or wand brush
| mm               |           | material       | The "modification" material, generally used with "erase" in a schematic, clone or replicate spell.
| omx, omy, omz    |           | vector         | The offset from the target location to use for material sampling (e.g. clone, schematic)
| undo             |           | milliseconds   | How long to wait before auto-undoing this spell (instead of adding it to an undo queue)

## ConstructSpell

| Name             | Shorthand | Values         | Description
| type             |           | cuboid, sphere, pyramid | The type of construction
| radius           | r         | blocks         | The radius of this construction - the length from the center to the farthest side.
| falling          |           | true/false     | Whether or not to spawn falling blocks
| speed            |           | magnitude      | The speed at which to eject falling blocks
| max_dimsension   | md        | blocks         | The maximum allowed radius, including Power. May need to adjust for large command-block constructions
| thickness        |           | blocks         | Zero for a solid construction, or the thickness of the "walls"
| commands         |           | String map     | A map of commands to replace in command blocks, for use with schematics
| orient_dimension_max | odmax | blocks         | The maximum "orientation" dimension (usually y-axis), use for making non-cubic shapes
| orient_dimension_min | odmin | blocks         | The minimum "orientation" dimension (usually y-axis), use for making non-cubic shapes
| power            |           | true/false     | Turn on "power" mode, will modify redstone instead of blocks

## Legend

* material : Taken from the Bukkit Material enum. Cast-insensitive.
See: https://raw.githubusercontent.com/Bukkit/Bukkit/master/src/main/java/org/bukkit/Material.java
* materials : A name of a material list from materials.yml (or materials.defaults.yml)
* target type : One of "block", "self", "other" or "any" - who is valid for targeting. Some spells only work with blocks or entities, 
but most should try to do something reasonable for either target type.
See https://raw.githubusercontent.com/elBukkit/MagicPlugin/master/src/main/java/com/elmakers/mine/bukkit/plugins/magic/TargetType.java
* entity type : Taken from Bukkit's EntityType enum
See https://raw.githubusercontent.com/Bukkit/Bukkit/master/src/main/java/org/bukkit/entity/EntityType.java
* block coords : Can use "~" for relative coords, e.g. "px ~-10 py ~0 pz ~0". There is always a corresponding "world", e.g. "pworld"
* vector : Values from 0 - 1, Can use "~" for relative coords, e.g. "pdx ~0 pdy ~0.7 pdz ~0"

#

# Other Customization

The following may be used outside of the "parameters" block in spells.yml to further customize spells:

| Name             | Values         | Description
| icon             | material       | The icon to use for this spell in a wand. If ommited, this spell is command-line only.
| category         | string         | A key that maps to a specific category. Used for grouping spells in a list, and wand organization
| class            | Class name     | The class name for this spell. May be a builtin, or a fully-qualified classname for an external Spell
| costs            | Casting Cost   | The mana, xp or reagent costs for this spell
| active_costs     | Casting Cost   | The cost drain per second (Activated spells only)
| effects          | Effects        | The visual and audio effects associated with this spell
| name             | string         | A readable name for this spell- mainly for easy customized spells, builtins use messages.yml
| description      | string         | A readable description for this spell- mainly for easy customized spells, builtins use messages.yml

# Costs

A casting cost is a map of either "xp" or a material name - to an amount.

For material names, see https://raw.githubusercontent.com/Bukkit/Bukkit/master/src/main/java/org/bukkit/Material.java

If "xp" is used, the spell will require either XP or Mana. If cast with a mana-based wand, the wand's mana will be used. Otherwise, 
the player's experience will be drained.

# Effects

Each effect is configured using a "player" class and several common parameters:

| Name              | Values         | Description
| class             | Class name     | An effect player class, either a builtin or a fully-qualified external EffectPlayer
| location          | target/origin/both | Where to play the effect
| color             | RRGGBB         | The main color to use, only applies to some effects (like Fireworks)
| color1            | RRGGBB         | The secondary color to use, only applies to some effects (like Fireworks)
| delay             | milliseconds   | How long to wait before starting the effect
| effect            | Effect name    | A Bukkit effect, often includes sound and particles
| sound             | Sound name     | A sound to play, taken from the Bukkit Sound constant
| sound_volume      | 0.0 - 2.0      | The volume at which to play a sound
| sound_pitch       | 0.0 - 2.0      | The pitch to use for playing a sound
| particle          | Particle name  | A Particle type to use, taken from a Magic constant
| particle_count    | count          | The number of particles to spawn
| particle_offset_x | integer        | How much random x-jitter to apply to particles
| particle_offset_y | integer        | How much random y-jitter to apply to particles
| particle_offset_z | integer        | How much random z-jitter to apply to particles
| particle_data     | integer        | A special magic data number, use with care, it can crash the client!!
| particle_sub_type | string         | A special magic data value, use with care, it can crash the client!!
| firework          | Firework Effect | The type of firework to launch, if other firework params are specified without this it will be random
| firework_power    | integer        | The firework power
| firework_flicker  | true/false     | Whether or not to flicker the firework effect

## Repeating Effects

| Name              | Values         | Description
| iterations        | count          | How many times to repeat the effect
| period            | milliseconds   | How often to repeat the effect
| reverse           | true/false     | Whether or not to play the effect in reverse

## EffectRing

| Name              | Values         | Description
| radius            | blocks         | How large the ring should get
| size              | count          | How many effects to spawn per ring

## EFfectTrail

| Name              | Values         | Description
| length            | blocks         | The length of the trail, may be automatic if a target was provided.

enum reference:

Sound: https://raw.githubusercontent.com/Bukkit/Bukkit/master/src/main/java/org/bukkit/Sound.java
Effect: https://raw.githubusercontent.com/Bukkit/Bukkit/master/src/main/java/org/bukkit/Effect.java
ParticleType: https://raw.githubusercontent.com/elBukkit/MagicPlugin/master/src/main/java/com/elmakers/mine/bukkit/effects/ParticleType.java
FireworkEffect: https://raw.githubusercontent.com/Bukkit/Bukkit/master/src/main/java/org/bukkit/FireworkEffect.java

# Messages

All spell text is contained in messages.yml, though non-builtin spells may 
include a default name and description in the spells.yml definition.


