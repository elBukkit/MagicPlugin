# TODO

## Hot Issues

 - Active/inactive wand icons are broken:
   - Open wand, select spell, use jump spell, start gliding and it will happem
   - The active wand state will only appear when a player selects a move And not when you open the wand
 - /m strange tab-completion behavior
          
 - Option to disguise a mob as the caster
 - SpawnEntity action should be able to run actions on death of spawned mob
 
 - Test attributes when reloading mage class configs live
 - Option to specify a slot for class items to auto-equip
                     
 - Look at mobs that die while casting a spell, does the spell get to finish? Does it undo correctly?
  
 - Creating blocks (e.g. Reflect level 5) on the right-bottom corner of a painting breaks the painting but doesn't 
   catch the dropped item.
   
 - There seems to be an undo issue here somewhere.
   - Goldwalker blocks didn't undo once on sandbox (Can not reproduce)
   - Nathanwolf auto at dev spawn has some permanent blocks now
   - Random blocks show up in mob arena sometimes (web, broken floor)
   - Can't find a pattern. Tried world save... don't know. :(
  
 - Change potter progression, maybe add spells per year graduation?
 
 - Cars look floaty
 
 - Need some option to orient an EffectRing, or some other way to make a ring of effects that follows a projectile
  
## Recent Requests
      
 - "Residence" integration (requested by Frerduro on Discord)
   https://github.com/Zrips/Residence
   
 - Allow use of equations in book action, somehow.
 - Custom spell lore showing arbitrary parameters (or something)

## 1.13

### Things to verify 

 - Wand brush migration
 - Check schematics
 - Check book -> skull feature
 - Check torch action
 - Tag sign facing
 - Check capture spell
 - Check map brush
 - Check custom colored blob/reflect/platform/shell
 - Check secure/lockpick

## Future
  
 - Take a look at this, maybe use accurate block hitboxes?
   https://bitbucket.org/BillyGalbreath/pl3xsigns/src/c4ce6a50592aca67be0aef26117cc8b7e069c3eb/src/main/java/net/pl3x/bukkit/pl3xsigns/lineofsight/BoundingBox.java?at=master&fileviewer=file-view-default
  
 - Maybe optimize automata chunk effects/spells with player range
 
 - It'd be really cool to support configs from remote repos....
   Github has an API for fetching a list of files, maybe can use that?
   https://api.github.com/repos/grisstyl/BetterPotter/contents/spells/spells
       
 - Add path 2nd parameter to enchant command to stop when reaching end of a path
  
 - Optional lore on wands to say which classes they work with
     
 - Update skill icon lore when armor updates (to take buffs into account)
 
 - Brushette requests damage reduction (as in subtract an amount)
 - Status effect system
   - Some way to temporarily modify properties that works with stacking and is guaranteed to undo
   - Invoke via action, similar to ModifyProperties (maybe extend from it)
   
 - Make editor work with selector options
   
## Attributes

 - Attributes can improve with rankup (spell purchase? need new progression mechanism...)
 - Also allow classes to define attribute global effects
   - Physical damage
   - Speed
   - Magic damage (physical versus magic- maybe allow other damage classes?)
   - Cooldowns
   - Max mana / regen
   - Mana costs
   - SP costs or SP earn bonuses
   - Attack speed
   - Damage protection
   - Anti-hunger
   
## Suggestions

 - mauto spawner option to randomize facing direction
 - Option to /wand fill to fill a specific category
 - Cast location offset override per spell.
 - Flag to prevent putting SP in chests
 - Check knockback resistance in Velocity action, or add Mage knockback resistance.
 
 - Add an action to simulate the red screen you get while out of bounds. See:
   https://gist.github.com/leonardosnt/ffa8e72b60df197c762d1f2e903cc67f

 - Placeholder API integration: https://www.spigotmc.org/wiki/hooking-into-placeholderapi/
   - Allow placeholder-driven attributes
 
 - Mana regen cooldown, so that casting a spell puts mana regen on a cooldown
 
 - Paginate wand and spell lists
 
 - TreeAction should grow the right type of tree for the given sapling

## On Hold

 - Casting blob on an item frame makes the frame disappear. User reports dropped frame, too, but could not reproduce.
 - Spells drop on death with lag (maybe)?
 - PerWorldInventory logout issues, can't reproduce
 - Broom issues when in creative mode (re-opened spigot issue for this)
 - Wand disappearing during duels- maybe via disarm, maybe drop action? (red 0 on PW)
 - PW would like some custom lore for wand "quiet" and quick cast settings.
 - Recall warps don't show up with /mage getdata?
   
## Fast Blocks

 - The easiest way to deal with that is to probably pretend the client doesn't have the chunk yet
 - just entityplayer.playerConnection.sendPacket(new PacketPlayOutMapChunk(this.chunk, '\uffff')); should work
 - or even easier .. set the dirtyCount of the player chunk to 64, and update the h field with the chunk sections that you modified
 - and make sure to call playerChunkMap.a(playerChunk); to schedule an update

  https://github.com/tastybento/askyblock/blob/master/src/com/wasteofplastic/askyblock/nms/v1_12_R1/NMSHandler.java
  
## Not so High-Priority
 
 - Per-player language settings (See https://www.spigotmc.org/resources/api-languages.22305/)
 - Would be cool to have a configurable max # of maps, and start re-using map ids when limit is hit, LRU
 - Ability to alter flower pots and beds (need to tweak TileEntity data.. doable, but messy to track with Material keys)
 - Aliases don't work with levels 
 - Ability to specify map dimensions/offset in percentage

## Sabers

 - Paths being able to upgrade blocking/reflect power

## Reported Issues

### Requests

 - Async config load on startup option
 - Allow multiple welcome_wand entries
 - An attribute that lets spells level up more quickly
 - Wand Power based on Strength potion effect
 - Add ModifyPower action
 
 ZQuest API: https://www.spigotmc.org/resources/zquestapi-feel-the-might-of-java.35327/
 ZQuest How to make Extensions: http://zquestwiki.com/index.php?title=APIcreatingExtensions
 ZQuest Page: https://www.spigotmc.org/resources/zquest-feel-the-might-of-creating-1-9-1-10-1-11.18045/
 
### Suggestions

 - Bonuses to wands:
   - If you could do this with a command like "/wand configure mana_regen/max_mana <amount in seconds>" would be great as I could make my own shops for this
   - Also by combining in an anvil with an item that has these buffs that you can add to your wand
     - Temporary max mana
     - Temporary faster mana regeneration
     - Temporary anything else you can think of really, the more we can add, the better :)
 - Add a TeamProvider for Factions
 - Upgrades to Spells
   - Can only choose 1 of the 3 and only when applicable (actual bonus could be customized)
   - Bonus to max distance x2
   - Bonus to travel speed of the spell 40% faster
   - 10% bonus to damage
 - Ability to set the max amount of spells that a wand (or a path?) can have
 - With the ability to add/remove spells to your list
   - bonus to that would be if you could set a configurable cooldown for being able to change out the spells on your wand
 - If you add permanent bonuses I think it should work more like rune stones with the ability to add certain bonuses to specific armor pieces in the way you would add an enchant to an item. This would allow you to add the buffs to existing armor that already has enchants on them. This would allow for creating an economy around these buffs as you would need to eventually obtain more rune stones to enchant more armor. You could possibly even have a whitelist/blacklist of items that could/could not be enchanted with the rune stone. Of course these buffs would all have to do with making you a better wand caster that fit with the Magic plugin theme.
 - Chance to fizzle should only happen if you have been cursed (I saw a spell that say's reduced chance of fizzle)
 
## To Review

 - MagicArenas: Doesn't TP players out on a draw.. ?
 - Getting hit with aqua erecto says "cast unknown" on you?
 - Admission+Break door = dupe door (thought this was fixed???)
 - Grow/Shrink doesn't work on rabbits?
 - Put a size cap on slimes with Grow
 	
 - Shops/Items:
   - Add new command, mshop
     - /mshop create [shoptype] [title] : Create a new shop NPC, default type "buyshop" in configs
     - /mshop add [worth] <add item in hand to shop, or update worth if present>
     - /mshop remove <remove item in hand from shop>
     - Should look up itemstack (with count of 1) first, save as key if found
     - Eventually a GUI would be nice.
 
 - Alter/grow/shrink/rollback should prevent mobs dropping loot
  
 - See if snow that falls on something undoable can undo?
 
 - Undo bugs with pistons.. yuck. https://youtu.be/S-d-XT2r_vM
 
 - Arenas preventing last death Recall isn't working?
  
 - Show mana from offhand wand (if no wand in main hand)
 - Add builtin "help" command, check messaging for missing commands- shows "no permission"?
 - Expand armor to include power, other modifiers?
 
 - "Triggered" spells.. ? Apparently MagicSpells does this or somesuch.
   Examples- Activated spells/ passive spells Upon taking damage- activate a heal spell effect Upon dealing damage- explosion in line of sight

 - Add option to only cast spells when damaging an entity

 - Can still drop your wand by holding while closing your inv
 - Spell organization by path?

 - Pull/Accio on top of an entity sends it skyrocketing
 - Column's radius doesn't seem to work
   
 - CoreProtect integration
   http://minerealm.com/community/viewtopic.php?f=32&t=16362

 - Add effects template, for wands to stay one template but act like another (? - for cane)
 - Prevent TNT breaking secured chests

 - There's an area in the RoR where you can cast alohomora and it will spawn doors, in the center of the right side when you enter~
 - Need to fix some door-related undo issues, e.g. casting admission then breaking door sometimes drops door

 - Fix block targeting cast messages

 - Fix that horrible inv-dupe issue :(
 
 - Add spell or brush or something to auto-convert from MagicWorlds configs
 
 - Try to support projectile hitbox modifiers that aren't cubes
 
 - Overrides with commas in them are broken again
 
 - Spells acting oddly with Copy - Box, Iterate

 - Lead on possible undo issue- undo another player's spell while building, it still builds another tick or so?

 - Goggles deactivate on death (maybe only in Azkaban?)
 - Test baby/giant wither bosses:
   You have to set (every tick) the invulnerability of the wither.
   Either you get the data watcher and watch 20 with a value big but below than 1000. I use 600 not to have a too small wither. But there is an easier method which is r() which does it.
   ((CraftWither)wither).getHandle().r(600);
 - Aliases are broken /wand add doesn't activate, spell items don't work

 - Can't hitbox-target entities standing in a corner? Issues with spiders?
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

# Old, Possibly Ignored

 - Arena spell/schematic
 - Magic stats (that persist) - block modified, etc. (Statistics API?)
 - Collapse blocklist chains on save (?)
 - EnderDragon familiars that don't do block damage or spawn a portal on death?

## NEW SPELLS

 - avatar - Goes into "avatar" state- free spells, super power, levitating, special effects- but rapidly decreasing mana while active.
    - will need a spell-based power/protection system, or use potion effects? 
    - would be nice if this could extend from potioneffect and levitate- maybe refactor?
    - will need a separate "active cost reduction" property
 - banish - sets a player's home and spawn?
 - Fix/finish stairs spell
 - Fix tunnel spell
 - Some kind of "ramp" version of fill, like stairs but with arbitrary blocks?
 - Decorate, places paintings at target

## OTHER STUFF
 
 - Customize dynmap map wand pop-ups? Red with black shadows looked cool.. use wand effect color?
 - Make volley multi-sample?
 - Alter names sheep "jeb_", - others "Dinnerbone" ?
  
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

 - Add Kingdoms support (It's a premium plugin, sooooooo) 
 - Add support for LockettePro
 - Add some sort of integration with Partec plugin (Deprecated since 2015, so probably not)
   https://www.spigotmc.org/resources/partec-custom-particles-visuals-plugin-api.15386/

yoheius
Feb 13, 2014 at 11:51 - 1 like Unlike
Very good Please a Integration with diablodrops Thanks

--- Contacted diablodrops dev, never heard back. Grabbed source code from github, will check it out.

	
