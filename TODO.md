# TODO

## For 2.9.7

 - Make spells usable without a player, clean up strong Player references
   - Allow using Projectile spell without a player- maybe generally fix arrows.
 - Retry limit on construction batches
 - Undo fails in some cases (overlapping undo queues)
  - IDEAS: Make BlockData immutable (based on a BlockLocation- maybe repurpose BlockVector?)
  - BlockData tracks only the first recorded materialdata, commit will replace
    - I think the following is related, might be caused by a construct batch someone overlapping itself - which also shouldn't happen.
    - Undo issue, maybe having to do with cancelling batches- leaves weird z-axis-aligned strips that skip an x coordinate?
 - Customizable wand crafting recipes, check NMS data for "icon"
 - Add "wand unenchant" and "wand enchant" commands
 - Fix weird wand inventory issues, duplicate items, etc
 - dtlTraders integration
  
## For 2.9.8

 - Add Mana Boost effect for projectile spells, (add to Blessing - cost 100 Mana, regenerates 50.. ?)
 - Maybe use player UUID's instead of names. Might need to migrate player data for this?
 - Support schematic entity import for paintings and item frames (at least)
 - Add "Simulate" spell
 - Configurable randomization for Familiar spell
 - Fix / Improve Recall spell
 - Maybe allow spells and building materials to spawn out of wands... sold on signs, etc?

## For 2.9.9

 - Commandbook kit integration .. ?
 - Fix materials in cast messages (hm, only applies to copy - fix is tricky.)
 - Fix message cooldowns- something is perhaps wrong there?
 - Purge player data on save (When possible)
 - Improve tab completion
 - Configurable passthrough material list for Blink

## For 3.0.0

 - Config-driven WandLevel limits.
 - Make sure adding spells/materials never removes any, also opening/closing the inventory, organizing
 - Add "wand duplicate" command
 - Tweaking/Balancing - haste, cooldowns, etc
 - Fix wand inventory glitches and special-cases
 - Make ores destructible by most spells (except blast?)
 - Magic stats (that persist) - block modified, etc. (Statistics API?)
 - Add schematic list command
 - Finish localization
 - Make push spell reflect projectiles (set fireball velocity)
 - Fireball / grenade "rain" spells (Meteor Shower, Carpet Bomb?)
 - Fix website, can't parse new effect nodes
 - New spawn on dev server
 - Create new YouTube video(s)
 - Create API library, example plugin and example spell plugin.

NEAR-FUTURE STUFF:

 - Async player data save/load (... undo queue locking?)
 - investigate lag/slowdown - new dynmap stuff?

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
 - Biome modification (mega-frost). Chunk regeneration would be awesome.

WAND PROGRESSION:

 - Wand auto-colorization (via spell categories)
 - Add upgrade paths for wands, maybe a "combine" requirement of one or more wands
 - Add option to not be able to use another player's wand

OTHER STUFF:
 
 - Apply potion effects while holding wand, Could replace haste with this.
 - Add fall protection to bounce spell (generalize it)
 - Make volley multi-sample?
 - Alter names sheep "jeb_", - others "Dinnerbone" ?
 - Separate material list for "super" building materials?
 
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
 - If possible, label more material data like stair direction.
 - Add locale option to suffix messages.yml
 
 - Show active spells in a different color
 
 - Add location to /magic populate when used in-game
 - Add /magic depopulate, to remove all wands from chests
 
 - See if you can disable the fire from a fireball when not incendiary

 - Persist player spell data
   - familiar
 
 - Fix populate command to detect a full ring of broken chunks, or some other method to get all chunks.
 - Add chunk generate and stop options to "populate" for creating square maps.. ?
 
 - Continue work on combining wands on avils
 - Specific protection for suffocation damage, underwater breathing (air regeneration)
 - Maybe make wands regenerate while you're not holding them (timer-based)?
 
 TESTING / TWEAKING:
 
 - Test larger undo queue sizes, or count size in blocks?
 - Add console logging of massive construction spell casting

DEMO SERVER / DOCS STUFF:

 - Add "wand properties" infobook
 - Build and have fun! ;)

 - .. update docs, .. make new video
 - Customize map wand pop-ups? Red with black shadows looked cool.. use wand effect color?

STACK TRACES / EXTRA DEBUG INFO


PLAYER REQUESTS:

yoheius
Feb 13, 2014 at 11:51 - 1 like Unlike
Very good Please a Integration with diablodrops Thanks

--- Contacted diablodrops dev, never heard back. Grabbed source code from github, will check it out.

Mre30
Jan 9, 2014 at 09:36 - 1 like Unlike

For sure, GlobalMarket will and does work. But its only good for player2player sales.

- DTLTraders



	