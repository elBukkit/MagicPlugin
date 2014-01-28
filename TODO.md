# TODO

## For 2.9.0

 - I broke my build queue using cancel/undo? Command-line??

 - Configurable effects system
 
 - Paint is broken with/clone and/or replicate?
 - Fix dynmap spell markers

## For 3.0.0

 - Add configurable persist block limit for undo queue
 - Add option to bypass all costs to magic.yml
 - Add visual effects to spawn and recall spells
 - Make sure adding spells/materials never removes any, also opening/closing the inventory
 - Add "wand duplicate" command
 - Tweaking/Balancing - haste, cooldowns, etc
 - Retry limit on construction batches
 - Undo fails in some cases (overlapping undo queues)
  - IDEAS: Make BlockData immutable (based on a BlockLocation- maybe repurpose BlockVector?)
  - BlockData tracks only the first recorded materialdata, commit will replace
 - PvP permissions (Respect WorldGuard's pvp setting, global PvP)
 - Replicate/clone should delete entities in area
 - Fix wand inventory glitches and special-cases
 - Persist material brush
 - Make ores destructible by most spells (except blast?)
 - Magic stats (that persist) - block modified, etc.
 
 - Make spells usable without a player, clean up strong Player references
 - Add location, direction, target parameters to spell
 - Make the mine spell more flexible

NEAR-FUTURE STUFF:

 - Async player data save/load (... undo queue locking?)
 - make push spell reflect projectiles (set fireball velocity)
 - investigate lag/slowdown - new dynmap stuff?
 - Essentials signs add to wand...

 - Familiar- my cast kills their familiars?

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

WAND PROGRESSION:

 - Wand auto-colorization (via spell categories)
 - Add upgrade paths for wands, maybe a "combine" requirement of one or more wands
 - Add option to not be able to use another player's wand

OTHER STUFF:
 
 - Apply potion effects while holding wand Could replace haste with this.
 - Add fall protection to bounce spell (generalize it)
 - Add option to open chest on right-click (for wand inventory)
 - Add count parameter to projectile spell, make wither shoot a few
 - Make volley multi-sample?
 - Alter names sheep "jeb_", - others "Dinnerbone" ?
 - Organize inventory by spell usage (favorites page)
 - Complete work on tab completion
 - Separate material list for "super" building materials?
 - Scale fling and levitate falling effects based on distance fallen
 
 - Fix up alter spell, remove id-based lists
 - urlmaps.yml got truncated?
   - Check for duplicate keys when searching for a new one (?)
  - first spell on wand is getting lost (?) - debug with elder wand and fling. (haven't seen this in a while) 
 
 - Need separate activate/deactivate costs. Fill vs levitate :\
   - Variable costs would be nice, too- for fill and superconstruct.
 - Add safe undo:
   - Track falling block entities (sandblast, toss, etc)
   - Auto-expand?
 - prevent pillar from passing through non-air blocks of different materials than the target
 - Some sort of workaround for wands getting killed by the creative inventory
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
 
 - Factions support .. ?
 
 TESTING / TWEAKING:
 
 - Test larger undo queue sizes, or count size in blocks?
 - Make sure I haven't broken Essentials' ItemDB- it's not working for adding items to signs.. ? (e.g. xpbottle, netherstar)
 - Add console logging of massive construction spell casting
 - Raise default construction limits again.. or mess with power?

DEMO SERVER / DOCS STUFF:

 - Add "wand properties" infobook
 - Build and have fun! ;)

 - .. update docs, .. make new video
 - Customize map wand pop-ups? Red with black shadows looked cool.. use wand effect color?

STACK TRACES / EXTRA DEBUG INFO

	