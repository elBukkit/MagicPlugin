# TODO

IMPORTANT STUFF:

 - Disintegrate block damage is broken (?)
 - Familiar- my cast kills their familiars?
 - Investigate performance, maybe remove move listener (and also portal, gills spells)
 - Fix underwater targeting on non-construction spells, or generally if in the water
 - Investigate long disable lag after undo queues are cleared
 - Save off dynmap marker chunks on disable, load chunks on startup
 - Persist material brush
 - Undo fails in some cases (overlapping construction jobs? - Need to implement cancel)
 - Add console logging of massive construction spell casting
 - Make ores destructible by most spells (except blast?)
 - Raise default construction limits again.. or mess with power?

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
 - Phase spell
 - Column and Disc building spells

OTHER STUFF:
 
 - Show spell casting on dynmap
 - Alter names sheep "_jeb", - others "Dinnerbone" ?
 - Test larger undo queue sizes, or count size in blocks?
 - Add "wand duplicate" command
 - Organize inventory by spell usage (favorites page)
 - Complete work on tab completion
 - Make /wand name take ownership
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
 - Add max radius to construct spell
   - Override? Same as fill.. maybe use cooldown reduction?
 - Respect WorldGuard's pvp setting, global PvP ?
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
 - Make sure I haven't broken Essentials' ItemDB- it's not working for adding items to signs.. ? (e.g. xpbottle, netherstar)
 
 - Make per-spell, per-wand config files?
 - Show active spells in a different color
 - Make portal spell portals avoid taking the player to the nether
 
 - Add location to /magic populate when used in-game
 - Add /magic depopulate, to remove all wands from chests
 
 - See if you can disable the fire from a fireball when not incendiary
 - Light incendiary tnt on fire? (rename frag to incendiary?)

 - Persist player spell data
   - portal
   - familiar (?)
 - Localize all spell-specific messages.
 
 - Fix populate command to detect a full ring of broken chunks, or some other method to get all chunks.
 - Add chunk generate and stop options to "populate" for creating square maps.. ?

DEMO SERVER STUFF:

 - Add "wand properties" infobook
 - Move spawn?
 - Build and have fun! ;)

LONG-TERM OR INCOMPLETE:
 
 - Continue work on combining wands on avils
 - apply potion effects while holding wand? Could replace haste with this.
 - Specific protection for suffocation damage, underwater breathing (air regeneration)
 - Maybe data-drive blink's special list of things it will try to target through?
 - managed to shoot myself with sniper :(
 - Maybe make wands regenerate while you're not holding them (timer-based)?

 - invincibility doesn't protect against magic or lightning?
 - .. update docs, .. make new video
 - .. Factions support .. ?

 - Customize map wand pop-ups? Red with black shadows looked cool.. use wand effect color?

STACK TRACES / EXTRA DEBUG INFO

	