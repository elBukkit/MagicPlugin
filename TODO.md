# TODO

IMPORTANT STUFF:

NEW SPELLS:

 - banish - sets a player's home and spawn?
 - Fix/finish stairs spell
 - Fix tunnel spell
 - Some kind of "ramp" version of fill, like stairs but with arbitrary blocks?
 - Disguise spells / clone spell
 - vortex type spells.. singularity, black hole, tornado - fun ideas there.

OTHER STUFF:
 
 - Add feedback to Sender when using wandp, castp, etc.
 - Add wand owner data
   - with option to not be able to use another player's wand?
 - Make dynmap integration optional
 - can't alter "null" message from alter on bad target?
   - fix up alter in general, remove id-based lists
 - wands show up on dynmap?
 - curse effects
 - supershell undo left a ring
 - boon effects/targeting
 - Add messages to wandp ?
 - Fix alter spell with stairs ?
 - check that limited-use wands break
 - Add safe undo:
   - Track falling block entities (sandblast, toss, etc)
   - Auto-expand?
   - Save sign text ... chest contents, maybe?
 - Add visual effects to spawn and recall spells
 - prevent pillar from passing through non-air blocks .. ? what about stalactite, though?
 - Some sort of workaround for wands getting killed by the creative inventory
 - Make spells usable without a player, clean up strong Player references
 - Add location, direction, target parameters to spell
 - Migrate all properties to parameters
 - Look into data values disappearing on materials in wand inventory like stairs
 - If possible, label more material data like stair direction.
 - Add log messages about how to use config files, and which ones were loaded
 - Add locale option to suffix messages.yml
 - Make sure I haven't broken Essentials' ItemDB- it's not working for adding items to signs.. ? (e.g. xpbottle, netherstar)
 
 - Test using small fireball for low-powered versions
 - Add comments to yml files
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

 - Add buttons to give back the info books (/give @p server, etc)
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

