# TODO

 - Only bound+undroppable should not go in chests?
 - Add option to only cast spells when damaging an entity

 - Can still drop your wand by holding while closing your inv
 - Can also move your wand into a hopper/dropper directly
 - Spell organization by path?

 - Use hunger for mana.. ?

 - Pull/Accio on top of an entity sends it skyrocketing
 - Column's radius doesn't seem to work
 - Action to turn spell into a spawn egg
   
 - CoreProtect integration
   http://minerealm.com/community/viewtopic.php?f=32&t=16362

 - PS TODO:
   - Fix formatting of allowed list, single-column

 - Add effects template, for wands to stay one template but act like another (? - for cane)
 - Prevent TNT breaking secured chests

 - There's an area in the RoR where you can cast alohomora and it will spawn doors, in the center of the right side when you enter~
 - Need to fix some door-related undo issues, e.g. casting admission then breaking door sometimes drops door

 - Alter *really* needs an update!
 - Fix block targeting cast messages
 - Fix Shrink/Grow losing track of entities (zombie->giant)

 - Fix that horrible inv-dupe issue :(
 
 - Add spell or brush or something to auto-convert from MagicWorlds configs
 
 - Try to support projectile hitbox modifiers that aren't cubes
 
 - Overrides with commas in them are broken again
 
 - Reducto hay blocks in the PW park, some drop :|

 - Spells acting oddly with Copy - Box, Iterate

 - Don't logout players on server change until they quit

 - Undo action re-work
   - Incorporate UndoBatch into Undo action
     - Make sure to handle special cases, non-cancellable

 - Re-work "active", "deactivate" and "cancel" behaviors?

 - Lead on possible undo issue- undo another player's spell while building, it still builds another tick or so?

 - Spell lore should reflect reduced cooldowns
 - Goggles deactivate on death (maybe only in Azkaban?)
 - Can we save 1 slot in wand inv?
 - Test out silent firework entities
 - Test baby/giant wither bosses:
   You have to set (every tick) the invulnerability of the wither.
   Either you get the data watcher and watch 20 with a value big but below than 1000. I use 600 not to have a too small wither. But there is an easier method which is r() which does it.
   ((CraftWither)wither).getHandle().r(600);
 - Need a way for /cast to be cooldown-free without affecting NPC's, etc.
   - Add list of cast-command params?
   - Would be nice to replace the current toggleCastCommand stuff, but need something special for NPCs.. ?
 - Aliases are broken /wand add doesn't activate, spell items don't work

 - Show countdown in hotbar for duration-based or active-cost spells
 - Add templates to messages.yml for all the various forms a wand and spell
   name can take.

 - Can't hitbox-target entities standing in a corner? Issues with spiders?
 - Look at/expand "item" parameter to SpawnEntity (why do wither skellies start with a bow?)
 - Add "drops" parameter to Damage action to prevent mobs dropping loot.

 - Add /mmap reload command

 - Hover text for spells
 - Cat GIF generator? http://catdb.yawk.at/images?tag=gif
 - Check Regenerate - got stuck, couldn't cancel?
 - Make Portal spell portals avoid creating a frame on the other side (maybe handle TP'ing on portal event?)
 - Logout on death still buggy? (can not reproduce)
 - Wand dupe issue: tl;dr : he was able to drag a wand while in the spell inventory.

 - Clean up MaterialBrush target system, automate somehow?
 - Copy target brush action for tandem replication
 - Automata aren't cleaning up cmd block + redstone
 - Automata don't re-activate un chunk load events (?)
 - Glitching Fill behavior
 - Spell shop improvements:
   - Color spells player can't afford
   - Some sort of ability to set up in-game?
   - Add alphabetize option to base shop (hrm, no, config is a map.. bleh)
   - Allow air as filler blocks
 - Trait improvements
   - Add /parameter command for inspecting single param
 - Nerf Force (shift to break free? Fall protection?)
 - Make map work like replicate, clone - with start point set on activate
   - and maybe not repeat
 - Add repeat option for schematic brushes
   - or generalized option that works with map, too
 - /magic describe should show info about current spell?

## Old Stuff

 - Arena spell/schematic
 - Magic stats (that persist) - block modified, etc. (Statistics API?)
 - Collapse blocklist chains on save (?)
 - Enchant count limit for wands
 - EnderDragon familiars that don't do block damage or spawn a portal on death?

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
 - Biome modification (mega-frost).
 - Decorate, places paintings at target

## OTHER STUFF
 
 - Customize dynmap map wand pop-ups? Red with black shadows looked cool.. use wand effect color?
 - Make volley multi-sample?
 - Alter names sheep "jeb_", - others "Dinnerbone" ?
 
 - Fix up alter spell, remove id-based lists
 
 - Need separate activate/deactivate costs. Fill vs levitate :\
   - Variable costs would be nice, too- for fill and superconstruct.
 - prevent pillar from passing through non-air blocks of different materials than the target
 - If possible, label more material data like stair direction.
 - Add locale option to suffix messages.yml
 
 - Show active spells in a different color

 - Continue work on combining wands on anvils
 
## TESTING / TWEAKING:
 
 - Test larger undo queue sizes, or count size in blocks?
 - Add console logging of massive construction spell casting

## STACK TRACES / EXTRA DEBUG INFO


## PLAYER REQUESTS:

yoheius
Feb 13, 2014 at 11:51 - 1 like Unlike
Very good Please a Integration with diablodrops Thanks

--- Contacted diablodrops dev, never heard back. Grabbed source code from github, will check it out.

	