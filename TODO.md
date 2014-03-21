# TODO

## For 2.9.9

 - Add "wand duplicate" command
 - Config-driven WandLevel limits.
 - Collapse blocklist chains on save (?)
 - Improve tab completion, add special brushes + schematics (preload schematics), add sounds and particle types, etc
 - Configurable passthrough material list for Blink
 - Retry limit on construction batches

 - Customizable wand crafting recipes, check NMS data for "icon"
 - Batch block populator for chests
 - Make "regengerate" undoable and safe.
 - Commandbook kit integration

## For 3.0.0

 - Scoreboard mana/spell display
 - Add Mana Boost effect for projectile spells, (add to Blessing - cost 100 Mana, regenerates 50.. ?)
 - Add Multi-Spell Spell
 - Add Spell effect to projectile spell
 - Add Command spell
 - Configurable randomization for Familiar spell

 - Make push spell reflect projectiles (set fireball velocity)
 - Cast spells on WG region enter/exit (Darkrael)
 - Fix first construct of a schematic. (?)
 - A way to /wand enchant with auto-fill but not filling wand... ?
 - Magic stats (that persist) - block modified, etc. (Statistics API?)
 - Hopefully get dtlTraders integration working

 - Tweaking/Balancing - haste, cooldowns, etc
 - Finish localization
 - Arena spell/schematic
 - Add cast messages for all spells
 - Player-specific wands, check player data- CoOoD3R, lathame, rileycross, treed, Art1001 .. ?
 
## Post-3.0.0

 - Magic bows that fire arrows that trigger spells... ?
 - Phase scaling in the End? Experiment...
 - Maybe allow spells and building materials to spawn out of wands... sold on signs, etc?
 - Fix dtlTraders integration
 - Add schematic list command (just for magic-specific schematics, and/or WE ones, too)
  - Support schematic entity import for paintings and item frames (at least)
 - EnderDragon familiars that don't do block damage or spawn a portal on death?
 - Async player data loading/saving
 
## DOCUMENTATION / DEMO STUFF

 - Add "wand properties" infobook

 - New spawn on dev server

 - Create new YouTube video(s)
 - Update "scripting" and "customization" documentation once tab completion is done.
 - API Tutorials, when API is complete.
 - Get Factions running on the demo server?
 
 - Build and have fun! ;)

##  API STUFF

 - Separate "Magic" API with a few simple methods like cast, undo, commit
 - ImageMap API
 - Spell API for custom spells
 - Sample custom spell plugin (soft-depend)
 - Sample API integration plugin (soft-depend)

## NEAR-FUTURE STUFF

 - Async player data save/load (... undo queue locking?)
 - investigate lag/slowdown - new dynmap stuff?

## NEW SPELLS

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
 - Biome modification (mega-frost).
 - Decorate, places paintings at target

## WAND PROGRESSION

 - Wand auto-colorization (via spell categories)
 - Add upgrade paths for wands, maybe a "combine" requirement of one or more wands
 - Add option to not be able to use another player's wand

## OTHER STUFF
 
 - Customize map wand pop-ups? Red with black shadows looked cool.. use wand effect color?
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
 
## TESTING / TWEAKING:
 
 - Test larger undo queue sizes, or count size in blocks?
 - Add console logging of massive construction spell casting

## STACK TRACES / EXTRA DEBUG INFO


## PLAYER REQUESTS:

yoheius
Feb 13, 2014 at 11:51 - 1 like Unlike
Very good Please a Integration with diablodrops Thanks

--- Contacted diablodrops dev, never heard back. Grabbed source code from github, will check it out.

Mre30
Jan 9, 2014 at 09:36 - 1 like Unlike

For sure, GlobalMarket will and does work. But its only good for player2player sales.

- DTLTraders

---

Gitpw3d
Wants scoreboard-based mana display

-------

alek123222
Nov 27, 2013 at 15:35 - 1 like Unlike
Can you make it compatible so when a user has over 20 health he can still be healed because at the moment the plugin just sets them back to 20 again.
I am using a custom plugin thats based of this one: http://dev.bukkit.org/bukkit-plugins/lore-attrubites-revival/ Also cool downs seem not to work at all and also a message would be nice that when a spell is on cool down should pop up saying "Cool-down blah"
----

Gitpw3d
2 days, 11 hours ago - 0 likes Like
My Mistake When the wand is selected the health is set to 20(normal)
but ils messes with that becauses of the lvs the when the wand is unslected the health reverts to normal execpt needing to regen ~100-1000 hearts
reply report create ticket delete #339
Gitpw3d
2 days, 16 hours ago - 1 like Unlike
Hi elMaker im using ils lore stats which increases the health you get per lv but mana being displayed messed it up, do you have a solution? other then that this plugin is great

----

diannetea
4 minutes ago - 1 like Unlike
We've narrowed down the wands breaking to the "clicksort" plugin, when someone organizes their inventory it breaks and you have to create a new one.

---

	