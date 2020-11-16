# TODO

## List is too long

 - wand can only be crafted from bottom row?
 - Change talisman and broomstick recipes 
 - Make blind actually prevent mobs from targeting
 - Make poison work on mobs
 - Gather title = Recall
 - Mnpc select can't target underwater
 - Can we make pets?
 - Make "untame" spell
 - EntityData improvements:
   - cat type
   - sitting, other poses
   - tamed, owner
   - llama/donkey properties, inventory
 - Some kind of pets?
 - Spells ignore tamed mobs of friends ?

## Hot Issues

 - Offhand wands don't keep on death sometimes ?
 - Get items when using erase brush? ... this would be really hard, undo would require having all of the blocks.

 - Can fly through glass panes on a broomstick

 - Allow customizing effects, or lists in general?
 - Paginate wands and spells command output, make base class of SelectionManager, like Paginator<T extends Named>
 - Automata re-activation delay ?

 - Make melee a tag instead of a material list
 
 - Passives:
   - Some way to tag/group passives, where only N spells from a group can be active at a time

 - Add support for Sponge schematics

 - Add support for Bukkit projectiles, when there is an entity shooter
 
 - Add option to reset mana on wand activation

 - Add denied_item parameter to CheckInventory action
 - Look at bug with equations from Reuben
 - Some way to specify order of icons in Recall

 - while moving or flying (it happens more often while doing this) around and right clicking to cast, it throws this error: https://hastebin.com/ixilazasig.makefile  it doesn't seem to happen with left click. Not an expert but by reading a bit I think it's because minecraft thinks the player item is being replaced by air and acts like we want to place such air as a block, which is imposible. Hope it helps!
   (This may be a creative mode only bug?)
 - Rocket boots + rocket spell underwater = profit? https://www.youtube.com/watch?v=mAOrgu8P4r8
 - Tweak controllable air scooter, why is initial acceleration so slow?
 - Freezing water changes it to a source block when undone... ?? https://forge.avatarmc.com/T1993
 - Using alter on stained glass pane makes it go funky in 1.13

 - Broom crafting, wheat isn't needed in 1.13.1 - something weird definitely going on there, maybe just need to 
   convert legacy ingredients before registering?
 - Look at lightsaber crafting, seems broken? Maybe only in 1.13? Starshop also always gives white stained glass...

 - Some clean way to make magic bow arrow spells useable on a wand?
   Mainly to avoid people always reporting them as bugs :\
 - Issues with wand/bows migrating to new version (waiting on details)
 
 - (PS - /ps allowall lags?)

 - Small bug! When you die, it makes your wand auto-select the first spell in your hotbar:
   https://youtu.be/wExxOdFWCV4

 - /m strange tab-completion behavior
           
 - Test attributes when reloading mage class configs live
 - Option to specify a slot for class items to auto-equip
                       
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
 
 - Some way to copy the last spell cast by your target.
 - Support for "FationsOne" (sic, sp?) - I guess? Good lord, Factions, get your act together.
 - Special blocks that act as automata when placed, remove automata when used
 - Selector support for scaling costs based on lore values
 - Add "deactivate" actions for toggle spells
 - Add damage lore to spells   
 - Allow use of equations in book action, somehow.
 - Custom spell lore showing arbitrary parameters (or something)

## Future
  
 - Ability to earn SP (Or whatever) from various actions, sword use, bow use, mining, etc.
   Generalized XP system?
  
 - Take a look at this, maybe use accurate block hitboxes?
   https://bitbucket.org/BillyGalbreath/pl3xsigns/src/c4ce6a50592aca67be0aef26117cc8b7e069c3eb/src/main/java/net/pl3x/bukkit/pl3xsigns/lineofsight/BoundingBox.java?at=master&fileviewer=file-view-default
   
 - FOV change action?
   https://wiki.vg/Protocol#Player_Abilities_.28clientbound.29
   
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
 
## STACK TRACES / EXTRA DEBUG INFO

## PLAYER REQUESTS:

 - Add some sort of integration with Partec plugin (Deprecated since 2015, so probably not)
   https://www.spigotmc.org/resources/partec-custom-particles-visuals-plugin-api.15386/
