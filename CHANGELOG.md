# CHANGELOG

## 1.6

 - Drop support for 1.6
 - Per-spell color and particle overrides
 - Add canTarget helper for Entity filtering
 - Fix bypass_confusion parameter

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