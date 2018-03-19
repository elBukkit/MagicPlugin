# Magic Roadmap

This is a *very rough* approximation of where Magic is headed in future versions.

## 7.4

 - Class improvements and additions:
     - A way to lock a class without removing it
     - Classbound items that are given on unlock and taken away on lock
     - Wand "unstashable tag", sets a generic item tag
     - "Unmoveable" tag, for use with classbound items that go in a specific slot
 - Status effect system for temporary (timed) property changes

## 7.5

 - Ability to apply attribute equations to properties like protection
 - Internal attribute system
   - Including damage reduction
   - Mana bonuses
   - Maybe others, see TODO
 
## 7.6

 - First draft of RPG-style configs with a variety of classes to choose from
 - Players may choose one class, and then add more classes as they max out (up to some limit)
 
## 7.7

 - Persistent effect support
   - Replacing old automata tracking
   - Ability to have spells cast on chunk load and cancel on unload
   - Also support for mob spawners
   - Configured locations with support for adding locations in-game

## 8.0
 
 - 1.13 support
 - Depending on how much the API changes, it seems likely that this version will *not* be backwards compatible!