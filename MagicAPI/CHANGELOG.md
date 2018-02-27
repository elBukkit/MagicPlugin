# CHANGELOG

## 6.7

 - MagicAPI is now integrated with the Magic plugin, please check Magic's changelog,
   or just look at the git history for API changes.

## 3.0

 - MagicAPI now builds against Bukkit API 1.8
 - Fixes to GUI items being glitched out of inventories
 - Cure cures wither

## 2.7

 - Extend/change effect API to allow for overlapping effects

## 2.6

 - Implemented/Re-Worked Action API

## 2.1

 - Add IconURL to SpellTemplate for 1.8 custom icons

## 2.0

 - Add Action API, the new way to create custom spells
 - Add Mage.isSneaking

## 1.9

 - Add wand inventory alphabetizing
 - Add createGenericItem, for creating non-enchanted items from templates.

## 1.8

 - Add cooldown and permission accessors to spell class
 - Add Messages API for localization
 - Add MapController API for custom image maps (loaded from URLs)
 - Add save event for other plugins to hook into Magic's autosave mechanism

## 1.7

 - Added separate mana/xp costs system
 - Add "stealthy" mages
 - Allow "blocking" scheduled undo tasks
 - Add spell levelling API

## 1.6

 - More effect player controls
 - Add MageController.isPassthrough(Location)
 - Add Mage.getDisplayName
 - Drop support for 1.6

## 1.5

 - Various API additions.. I haven't been doing a proper job keeping track.
 - Added Mage.restoreWand, and a few other Mage methods

## 1.3.3

 - Add helper methods for dealing with Magic items
 - Add custom PreCastEvent and CastEvent events
 - Add additional targeting types for finer grained targeting control

## 1.3.2

 - Add getEntityName, helper method for making a nice name for any entity
 - Add several getMage variants for using Entity-based Mages

## 1.3.1

 - Re-work the Wand/Lost wand relationship. Id's are only for lost wands.
 - I'm making this a point release mainly to try and sync up the Magic and Magic API version #'s.

## 1.3

 - Add some automata-related API methods.
 - Add getDuration and getCooldown methods to SpellTemplate.
 - Add a ton of new API interfaces and methods for interacting with Mages and Spells.

## 1.2

 - Add URLMap API
 - Add Target utility, a basic class for an entity/block targeting system
 - Add BufferedMapCanvas API, for interrogating a Map image.

## 1.0

 - Can create wands, cast spells, and interrogate the Magic plugin
 - Added MaterialAndData utility for dealing with Material variants and tile entities

## 0.9.0

- First release, broken out from Magic plugin
