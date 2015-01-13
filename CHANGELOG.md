# CHANGELOG

## 2.0

 - Add hitbox-based targeting.
 - Add SpellAction system.

## 1.9

 - Fix some issues parsing potion effects from strings.

## 1.8

 - Fix multiple-respawn glitch with entity undo after death
 - Don't undo entity effects from spells with auto-undo by default
 - Add temporary entity metadata flag
 - Add cooldown and permission accessors to spell class
 - Add findBlockUnder method
 - Make Messages API available to other plugins
 - Make MapController API available to other plugins
 - Add additional configuration parsing utilities
 - Ignore unknown entity types when targeting (avoids targeting pets)

## 1.7

 - Removed compatibility layer for Bukkit Metadata API (a PR of mine). Don't think that's gonna happen at this point.
 - Added separate mana/xp costs system
 - Add spell levelling system
 - Add "blocking" scheduled undo tasks

## 1.6

 - Drop support for 1.6
 - Update to EffectLib 2.0 - all effect class names have changed!
 - Per-spell color and particle overrides
 - Add canTarget helper for Entity filtering
 - Fix bypass_confusion parameter
 - Improve "magic" damage, with entity source

## 1.5
 
  - Brush system improvements
  - Effects improvements, additional EffectLib effects
  - Variable FX system (wand color/particle overrides)
  - Undo system improvements, physics handling
  - Better schematic pasting
  - "Magic damage" source (WIP)
  - Configurable location randomization
  
## 1.4

 - Fixes to general "find place to stand" helpers
 - Entity undo is optional in an undo batch
 - Breakable/Reflective block system
 - Undo system improvements/additions
 - Fix 1.6.4 compatibility
 - Full Effect lib integration (mainly affects particle names)

## 1.3

 - Add extra forms of entity-based targeting
 - Add basic HoloText functionality
 - Add Mercator projection utilities (for mapping...)
 - Add "bypass_protection" parameter for spells
 - Always show non-cast messages, don't throttle.

## 1.2

 - Add Entity-based Mage support.
 - Add EntityEffect support to the EffectPlayer.
 - Add EffectLib integration to EffectPlayer.

## 1.1

 - Add "*1" format for multiplying directions.
 - Add MaterialBrush
 - Add UndoQueue and BlockList, expand undo system to handle entities and falling blocks
 - Add raw explosion method for assigning a source entity

## 1.0

 - First release, broken out from MagicAPI
 - Includes: Effects, material system, Image Map, Localization, NMS and other various utilities.