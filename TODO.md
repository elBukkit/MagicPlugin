# TODO

IMPORTANT STUFF:

 - Map went funky, got broken - getMapView caused NPE .. ?
   - NPE at:  at com.elmakers.mine.bukkit.utilities.URLMap.getMapItem(URLMap.java:138) ~[?:?]
 - Debug wands on map on death and pickup, add log messages.
 - [Magic] Failed to save undo data: null, massive server lag :\
 - Update categories, balance them out. Bending may not work.

[17:18:03] [Server thread/WARN]: [Magic] Failed to get map id 147 for key 8,8|8,8|http://s3.amazonaws.com/MinecraftSkins/epikwun.png
[17:18:03] [Server thread/WARN]: [Magic] Failed to get map id 148 for key 8,8|8,8|http://s3.amazonaws.com/MinecraftSkins/jtm740.png

NEW SPELLS:

 - banish - sets a player's home and spawn?
 - Fix/finish stairs spell
 - Fix tunnel spell
 - Some kind of "ramp" version of fill, like stairs but with arbitrary blocks?
 - Disguise spells / clone spell
 - vortex type spells.. singularity, black hole, tornado - fun ideas there.
 - separate alter into superalter and alter

OTHER STUFF:
 
 - Add visual effects to spawn and recall spells
 - Customize map wand pop-ups? Red with black shadows looked cool.. use wand effect color?
 
 - Need separate activate/deactivate costs. Fill vs levitate :\
   - Variable costs would be nice, too- for fill and superconstruct.
 - Add wand combine and combine_upgrade(?) commands
 - Add upgrade paths for wands, maybe a "combine" requirement of one or more wands
 - Add priorities to maps for throttling
 - Add max radius to construct spell
   - Override? Same as fill.. maybe use cooldown reduction?
 - Respect WorldGuard's pvp setting, global PvP ?
 - Add option to not be able to use another player's wand
 - can't alter "null" message from alter on bad target?
   - fix up alter in general, remove id-based lists
 - supershell undo left a ring
 - Add safe undo:
   - Track falling block entities (sandblast, toss, etc)
   - Auto-expand?
   - Save sign text ... chest contents, maybe?
 - prevent pillar from passing through non-air blocks .. ? what about stalactite, though?
 - Some sort of workaround for wands getting killed by the creative inventory
 - Make spells usable without a player, clean up strong Player references
 - Add location, direction, target parameters to spell
 - Migrate all properties to parameters
 - Look into data values disappearing on materials in wand inventory like stairs
 - If possible, label more material data like stair direction.
 - Add locale option to suffix messages.yml
 - Make sure I haven't broken Essentials' ItemDB- it's not working for adding items to signs.. ? (e.g. xpbottle, netherstar)
 
 - Make per-spell, per-wand config files?
 - first spell on wand is getting lost (?) - debug with elder wand and fling. 
 - Show active spells in a different color
 - Make portal spell portals avoid taking the player to the nether
 
 - add wand organize capability.. shift+click or something? Actual use for categories?
 - Add tab completion
 - Add location to /magic populate when used in-game
 - Add /magic depopulate, to remove all wands from chests
 
 - See if you can disable the fire from a fireball when not incindiary
 - Light indindiary tnt on fire? (rename frag to incindiary?)

 - Persist player spell data
   - portal
   - familiar (?)
 - Localize all spell-specific messages.
 
 - Fix populate command to detect a full ring of broken chunks, or some other method to get all chunks.
 - Add chunk generate and stop options to "populate" for creating square maps.. ?

DEMO SERVER STUFF:

 - Update WorldGuard, hope item frames are safe?
 - Build and have fun! ;)

LONG-TERM OR INCOMPLETE:
 
 - Cooldowns broken.. ?
 - Continue work on combining wands on avils
 - apply potion effects while holding wand? Could replace haste with this.
 - Specific protection for suffocation damage, underwater breathing (air regeneration)
 - Maybe data-drive blink's special list of things it will try to target through?
 - managed to shoot myself with sniper :(
 - Maybe make wands regenerate while you're not holding them (timer-based)?

 - invincibility doesn't protect against magic or lightning?
 - .. update docs, .. make new video
 - .. Factions support .. ?

