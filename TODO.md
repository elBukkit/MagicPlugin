# TODO

## For 2.9.0

 - Separate player data files
 - Async player data save/load (... undo queue locking?)
 - PvP permissions (Respect WorldGuard's pvp setting, global PvP)
 - Add option for removing building material Items? (set ticksLived)
 - Undo fails in some cases (overlapping undo queues)
  - IDEAS: Make BlockData immutable (based on a BlockLocation- maybe repurpose BlockVector?)
  - BlockData tracks only the first recorded materialdata, commit will replace
 - Configurable effects system
 - Retry limit on construction batches

## For 3.0.0

 - Fix wand inventory glitches and special-cases
 - URLMap image file cache
 - Replicate entities, attachables, signs, chest contents
 - Same as above, for undo?
 - Persist material brush
 - Make ores destructible by most spells (except blast?)
 - Magic stats (that persist) - block modified, etc.

NEAR-FUTURE STUFF:

 - make push spell reflect projectiles (set fireball velocity)
 - investigate lag/slowdown - new dynmap stuff?
 - Essentials signs add to wand...

 - Familiar- my cast kills their familiars?

 - Fix underwater targeting on non-construction spells, or generally if in the water

NEW SPELLS:

 - level - levels the ground
 - avatar - Goes into "avatar" state- free spells, super power, levitating, special effects- but rapidly decreasing mana while active.
    - will need a spell-based power/protection system, or use potion effects? 
    - would be nice if this could extend from potioneffect and levitate- maybe refactor?
    - will need a separate "active cost reduction" property
 - banish - sets a player's home and spawn?
 - Fix/finish stairs spell
 - Fix tunnel spell
 - Some kind of "ramp" version of fill, like stairs but with arbitrary blocks?
 - Disguise spells / clone spell
 - vortex type spells.. singularity, black hole, tornado - fun ideas there.
 - Column and Disc building spells
 - Biome modification (mega-frost). Chunk regeneration would be awesome.

OTHER STUFF:
 
 - Wand auto-colorization (via spell categories)
 - Apply potion effects while holding wand Could replace haste with this.
 - Add fall protection to bounce spell (generalize it)
 - Add option to open chest on right-click (for wand inventory)
 - Add count parameter to projectile spell, make wither shoot a few
 - Make volley multi-sample?
 - Make Phase return from the End
 - Alter names sheep "_jeb", - others "Dinnerbone" ?
 - Add "wand duplicate" command
 - Organize inventory by spell usage (favorites page)
 - Complete work on tab completion
 - Separate material list for "super" building materials?
 - Test reloads- not duplicating undo queues, etc
 - Scale fling and levitate falling effects based on distance fallen
 
 - Fix up alter spell, remove id-based lists
 - urlmaps.yml got truncated?
   - Check for duplicate keys when searching for a new one (?)
 - Make sure adding spells/materials never removes any, also opening/closing the inventory
  - first spell on wand is getting lost (?) - debug with elder wand and fling. (haven't seen this in a while) 
  
 - Add option to bypass all costs to magic.yml
 - Add visual effects to spawn and recall spells
 
 - Need separate activate/deactivate costs. Fill vs levitate :\
   - Variable costs would be nice, too- for fill and superconstruct.
 - Add upgrade paths for wands, maybe a "combine" requirement of one or more wands
 - Add option to not be able to use another player's wand
 - supershell undo left a ring
 - Add safe undo:
   - Track falling block entities (sandblast, toss, etc)
   - Auto-expand?
   - Save sign text ... chest contents, maybe?
 - prevent pillar from passing through non-air blocks .. ? what about stalactite, though?
 - Some sort of workaround for wands getting killed by the creative inventory
 - Make spells usable without a player, clean up strong Player references
 - Add location, direction, target parameters to spell
 - Look into data values disappearing on materials in wand inventory like stairs
 - If possible, label more material data like stair direction.
 - Add locale option to suffix messages.yml
 
 - Show active spells in a different color
 
 - Add location to /magic populate when used in-game
 - Add /magic depopulate, to remove all wands from chests
 
 - See if you can disable the fire from a fireball when not incendiary
 - Light incendiary tnt on fire? (rename frag to incendiary?)

 - Persist player spell data
   - familiar
 - Localize all spell-specific messages.
 
 - Fix populate command to detect a full ring of broken chunks, or some other method to get all chunks.
 - Add chunk generate and stop options to "populate" for creating square maps.. ?
 
 - Continue work on combining wands on avils
 - Specific protection for suffocation damage, underwater breathing (air regeneration)
 - Maybe data-drive blink's special list of things it will try to target through?
 - Maybe make wands regenerate while you're not holding them (timer-based)?
 - .. Factions support .. ?
 
 TESTING / TWEAKING:
 
 - Test larger undo queue sizes, or count size in blocks?
 - Make sure I haven't broken Essentials' ItemDB- it's not working for adding items to signs.. ? (e.g. xpbottle, netherstar)
 - Add console logging of massive construction spell casting
 - Raise default construction limits again.. or mess with power?
 - Test block populator - doesn't seem to work anymore?

DEMO SERVER / DOCS STUFF:

 - Add "wand properties" infobook
 - Build and have fun! ;)

 - .. update docs, .. make new video
 - Customize map wand pop-ups? Red with black shadows looked cool.. use wand effect color?

STACK TRACES / EXTRA DEBUG INFO

	