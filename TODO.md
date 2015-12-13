# TODO

 - Add tags to wand and spell templates
 - Alter *really* needs an update!
 - Spells will run partially but then not undo if the player's batch queue is full?
 - Effects don't cancel if you switch away from your wand? Whip stops moving? Wha?
 - Fix block targeting cast messages
 - Need a way for arena deaths to prevent a Recall change of death location
 - Automata don't resume anymore
 - Maybe add a floor/ceil option to Volume for more control over sizes
 - Somehow make brooms work when mob spawning is denied?
 - Fix Shrink/Grow losing track of entities (zombie->giant)

 - Fix that horrible inv-dupe issue :(
 
 - Add spell or brush or something to auto-convert from MagicWorlds configs
   
 - Need to clean up location offset issues for effects. Can't use updateLocations: false with 
   an offset, for instance. Relative locations would be great, too- maybe in Magic and EffectLib.
 
 - Fix shop price scaling
 
 - Try to support projectile hitbox modifiers that aren't cubes
 
 - Additive Velocity parameter?
 
 - KidBear says spawned animated statues don't disappear if the player dies
 
 - Overrides with commas in them are broken again
 
 - Stillll have an undo issue somewhere. Make sure unregistering attachables isn't going wrong
   - was able to build a schematic (nathanwolf) and undo piece by piece, but last piece wouldn't register.

 - Reducto hay blocks in the PW park, some drop :|

 - damage_resistance_protection not working? (Check Fianto Duri in PW configs)

 - Spells acting oddly with Copy - Box, Iterate
   
 - Herobrines - XP exploit?
   hold wand and go to /ma j Fight
   you will get xp equivalent to your mana

 - Don't logout players on server change until they quit

 - MagicArenas: Add wand path to announcement messages

 - Undo action re-work
   - Incorporate UndoBatch into Undo action
     - Make sure to handle special cases, non-cancellable

 - Re-work "active", "deactivate" and "cancel" behaviors?

 - Lead on possible undo issue- undo another player's spell while building, it still builds another tick or so?

 - Spell lore should reflect reduced cooldowns
 - Goggles deactivate on death (maybe only in Azkaban?)
 - Can we save 1 slot in wand inv?
 - Test out silent firework entities
 - Can magic items stack somehow ... ?
 - Test baby/giant wither bosses:
   You have to set (every tick) the invulnerability of the wither.
   Either you get the data watcher and watch 20 with a value big but below than 1000. I use 600 not to have a too small wither. But there is an easier method which is r() which does it.
   ((CraftWither)wither).getHandle().r(600);
 - Need a way for /cast to be cooldown-free without affecting NPC's, etc.
   - Add list of cast-command params?
   - Would be nice to replace the current toggleCastCommand stuff, but need something special for NPCs.. ?
 - Aliases are broken /wand add doesn't activate, spell items don't work
 - Add "Protect" action
 - Fix Anvil renaming + binding (KingBohica1)

 - Show countdown in hotbar for duration-based or active-cost spells
 - Add templates to messages.yml for all the various forms a wand and spell
   name can take.

 - Add an "override" option for spells, to skip loading the defaults for that spell
 - Can't hitbox-target entities standing in a corner? Issues with spiders?
 - Look at/expand "item" parameter to SpawnEntity (why do wither skellies start with a bow?)
 - Add "drops" parameter to Damage action to prevent mobs dropping loot.
 - Add Music action, with disc parameter

 - Add /mmap reload command
 - Registered blocks aren't clearing on /magic commit ?
 - CoreProtect integration
   http://minerealm.com/community/viewtopic.php?f=32&t=16362

 - Hover text for spells
 - Cat GIF generator? http://catdb.yawk.at/images?tag=gif
 - Check Regenerate - got stuck, couldn't cancel?
 - Make Portal spell portals avoid creating a frame on the other side (maybe handle TP'ing on portal event?)
 - Logout on death still buggy? (can not reproduce)
 - Wand dupe issue: tl;dr : he was able to drag a wand while in the spell inventory.
 - Recall still losing waypoints .. also can grab items out?

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

	