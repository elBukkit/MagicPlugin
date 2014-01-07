# CHANGELOG

## 2.8.1

 - The blob spell will now auto-undo after 30 seconds.
 - Some fixes to the Essentials integration.
 - Add /spellp command, for remotely casting a spell as another player.
 - Add y_offset and y_value parameters to the FlingSpell.
 - Add some additional safety checks to the Essentials integration.
 - WIP: Add double-click-to-reorganize wand inventory functionality.
 - Fix the farming wand, add additional safety for unusable materials.
 - Allow tesseract to pass through glass
 - Fix interaction with Essentials signs and Wands interfering with each other.

## 2.8.0

 - Make wands really indestructible (thanks JRL1004 and Jogy34)!
 - Add "keep_wands_on_death" option, on by default
 - Add "default" block in wands.yml to control the wand given with /wand
 - Add stained glass to the painter wand
 - Add click cooldown as a work-around for squirrely interact event behavior
 - Add "commit" spell to clear your undo queue.
 - Add "modifiable" wand property
 - Integrates with dynmap for tile updates.
 - Fix logger to use the standard Plugin logger.
 - Fix wand names reverting when closing the wand inventory.
 - Make the light spell targeted
 - Add /wand upgrade command
 - Add /wand fill command
 - Removed /magic reset command
 - Modified superblob spell to use a new "select" version of construct.
 - Add supershell, superbox, convert and superconvert spells.

## 2.7.6

 - Change players.yml undo format to be more sane.
 - Add /wandp command for remote wand manipulation, or command blocks.
 - Make wands indestructible (controlled via indestructible_wands config option) - except in lava ;(
 - Fix the camera spell
 - Persist blocks that are scheduled for cleanup.

## 2.7.5

 - Fix /magic populate never finishing.
 - Move all wand and spell names and descriptions to message.yml

## 2.7.4

 - Deactivating a spell bypasses cooldowns and costs.
 - Fix "Insufficient Mana" message to show mana instead of XP for mana-base wands.
 - Add /magic populate command to populate existing chests with wands
 - Persist player undo queues to players.yml

## 2.7.3

 - Add a "fill wand" feature which will add all known spells to a wand when creating.
 - Support buying and selling wands via Essentials.

## 2.7.2
 
 - Added effects to fill and blink.
 - Fix boon spell's icon.
 - Add effects to disintegrate, shrink, fire, frost, disarm.
 - Put a hack in to work-around annoying double-interact event breaking fill.
 - Add effects to recall, remove item.
 - Add messages.yml for localization
 
## 2.7.1

 - Added raw particle effects
 - Add effects to heal spell
 - Right-clicking an item in the wand inventory will cycle pages, never close the inventory.
 - Add a "size" parameter to the recurse spell
 - Fix saving new camera maps

## 2.7.0

 - Fix wand cooldown reduction
 - Fix adding a batch to an undo queue after the player has logged out.
 - Add a config option to disable hacky WIP wand-combining on an anvil.
 - Add a customizeable web site (like at http://mine.elmakers.com)
 - Change spell costs to be simpler YML. (** This is a breaking change for custom configs, sorry!**)
 - Removed the old wand state restore method. Make sure you've run a version since 2.5.0 before upgrading.
 - Re-work the wand inventory system completely, support multiple inventory pages.

## 2.6.4

 - Load player skins asynchronously, and various fixes/optimizations to the camera spell.
 - Player portraits show up immediately, instead of slowly loading.
 - Abstract map renderer to handle any URL image.

## 2.6.3

 - Make the undo system use batched block updates
 - Ensure chunks are loaded when processing batch block updates
 - Fix a potential issue with changes to magic.yml getting overwritten.
 - The "flare" spell now only spawns a firework effect
 - Allow two spells to use the same material icon.
 - Add a parameter to push/pull to specify how many entities to effect. Pull now does only 1 again (for Yoinking)
 - Add "camera" spell, which takes a portrait of the target player.
 - Fix a bug in XP tracking with an active Mana-based wand.

## 2.6.2

 - Add carpet to the special-case material variant list.
 - Make push + pull target multiple entities
 - Maybe fixed some issues with "force"
 - Added "bending" wands (yes, I love the Avatar!)
 - Fix "breach" and other auto-undo versions of construct
 - The frost spell will slow target entities
 - Fix an exception in the disarm spell when the target is not holding anything.

## 2.6.1

 - Use delayed block changes in batches to allow large construction without killing the server.
 - Fix issues with XP (non mana-based) wands
 - Add material names to magic build materials (including sub-types for certain materials, wool, stained glass, wood, etc)
 - Fix targeting issues related to stained glass (only blink should target through them)

## 2.6.0

 - Show maximum mana in Wand lore.
 - "damage_reduction" properties changed to "protection".
 - Add boon + curse spells (PotionEffectSpell)
 - Add "light" spell.

## 2.5.9

 - Fix cooldown and cost reduction being tied together.
 - Change the mana display to only use one XP bar.

## 2.5.8

 - Add disarm spell
 - Fix armor not dropping on death if the wand inventory is open
 - Some wand randomization fixes
 - Add duration property (supported by levitate and invincible)
 - Fix spawn spell when the actual spawn point is underground.
 - Cap enchanting wand levels to 40, and fix the way levels are presented when enchanting.
 - Add right-click-to-cycle option, for all the wand inventory haters out there (er, everyone but me?)

## 2.5.7

 - Make sure when a spell is removed that a wand still has an active spell (same with materials)
 - Implement activating and deactivating of spells like levitate and invincible, with cost draining.

## 2.5.6

 - Fixes for 1.7.2 release (woohoo!)
 - Fix the broken arrow spell.

## 2.5.5

 - Add "toss" spell.
 - Add "power" property to wands, supported by: arrow, boom, construct, disintegrate, familiar, fire, fireball, frost, grenade, lightning, shrink, toss, wolf.
 - Some fixes to wand randomization, provide limiting for added properties.
 - Enchanting (randomizing) a wand won't change the active spell. Seemed confusing and dangerous.
 - Add materials to random wands.
 - Random wands will always use the mana system.
 - TRY to: Fix spell and material names to be simpler when the inventory is open. (thwarted by client-side-only inventory open event)

## 2.5.4
 
 - Allow altering the profession of villagers
 - Update xp regeneration system, make it a separate "mana" system.
 - Fix some strange behavior with how material positions are saved when there are too many for the wand to hold.
 - When randomizing a wand, make sure it has at least enough max xp to cast its most expensive spell.
 - Fix some of the xp-related issues, particularly enchanting and the mana system

## 2.5.3

 - Fix "quiet" option to still show error messages and failures (e.g. cooldown)
 - Add throttle for messages, defaults to one every 5 seconds
 - Fire protection now puts out fires
 - Add break_blocks option to boom
 - Add speed parameter to levitate spell
 - Add destructible and indestructible material lists to magic.yml
 - The "alter" spell now works on some entities (horses, etc)

## 2.5.2

 - Fix right-click interact with beds, signs and command-blocks while holding a wand
 - Some fixes to the "haste" wand property
 - Add damage senders when possible for spell damage
 - Add config options for using casting costs and cooldowns with the /cast command

## 2.5.1

 - Add stricter checks to wand crafting
 - Keep trying to add spells to a wand if the randomly chosen one was already on it
 - Allow for crafting limited-use wands (off by default), and adding uses to wands via enchanting
 - Add randomized wand properties

## 2.5.0

 - Fix renaming wands on an anvil
 - Add basics of wand crafting mechanic
 - Add randomized wand functionality (used in world population and crafting)
 - Changed default wand item to blaze rod
 - Add functionality to enchant wands to add more spells

## 2.4.0
 
 - Add an optional block generator, which will randomly put wands in naturally spawned chests
 - Update spells with cooldowns
 - Add cooldown reduction option for wands
 - Add "/wand configure" command for setting wand properties
 - Bugfixes: NPE on last use of wand, wands with no inventory, displayed uses, NPE on player death
 - Don't let the player keep or use XP given by wand regeneration
 - Fix interaction with inventories, chests, etc
 - Prevent name jumpiness when switching spells/materials
 - Added "leap", "time" and "sniper" spells
 - Fix "sandblast"
 - Changed wand data NBT format, though old wands should get updated.
 
## 2.3.2

 - Fix absorb spell to work with new wand inventory system
 - Fix erase material
 - Add clone material for more explicit control over clone behavior
 - Fix alter spell, add new materials up to 1.7.2

## 2.3.1

 - Add WorldGuard integration for construction spells
 - Add sound effect feedback for spell failures

## 2.3.0

 - Implement new Wand inventory system.

## 2.2.6

 - Some blink fixes
 - Add special mode to "shrink" to give you your own head, or any named player's head.
 - Add emeralds and nether quartz to the mine spell
 - Make levitate just put you in fly mode. Saved the old spell as "ThrustSpell" just in case anyone wants it.

## 2.2.5
 - Implement XP as a casting cost
 - Implement wand properties for protection and cost reduction
 - Implement wand xp/health/hunger regeneration

## 2.2.4
 - Allow use of /wand list and /spells from the console
 - Colorize active spell text
 - Fix cycling materials if you use slot 8 to hold the wand
 - Add "cat" spell (OcelotSpell)
 - Add Head Shrinker spell
 - Add flare spell
 
## 2.2.3

 - Experiment with a hacky method of showing the active spell as you cycle spells and materials.
 - Add wands.yml configuration file

## 2.2.2

 - Implement sulpher-as-erase material
 - Fix issues taking wands out of chests
 - Some tweaks to absorb, make it so you don't collect a mass of materials unless you want to.

## 2.2.1

 - Fix materials system
 - Fix bogus enchantments on items to make them glow
 - Lots of miscellaneous bugs + fixes to the new spell inventory system.

## 2.2.0

 - First release with "Spell Inventory" system.

## 2.1.1

 - Use enchanted items, names and lore for a more immersive experience.

## 2.1.0

 - Updates for 1.6.4

## 2.0.8

 - Updates for mcserver 1.8

## 2.0.7

 - Add casting costs to spells

## 2.0.6

 - Another massive refactor, spells now loading out of spells.yml
 - Added /magic reset and /magic reload commands
 - Collapsed peek, breach, blast and superblast into variants of construct

## 2.0.5

 - Some experimental changes to fling and levitate
 - Drop leap
 - Add "breach", air-based peek variant
 - Nerf frost (shorter range, auto-rewind)
 - Nerf tesseract (no passing through things)

## 2.0.4

 - Massive refactor, in preparation for configurable spell variants
 - Some tweaks to pull and push
 - Fix familiar so it spawns a random mob again
 - Fix shell
 - Fix portal (?)
 - Fix levitate (?), it at least protects you from death now.

## 2.0.3

 - Add cloak spell (invisibility until you move).

## 2.0.2

 - Nerf frost and distegrate- not with cooldowns, but by making them "ping" spells (1/2 heart damage)
 - ++ wolf spell - can now target people to sick wolves on them. nerf'd by capping number of wolves.
 - fling (+leap) now protect from falling damage

## 2.0.1

 - Add kamikazee spell.

## 2.0

 - Merge everything together, toss Persistence.

## 1.18

 - Update for 1.7 and RB#953

## 1.17

 - Arrows shot with the arrow spell can't be picked up
 - Modify portal so that it creates two instant-travel connected portals
 - monster now spawns actual monsters (use /familiar mob for random evil mobs)
 - familiar targets entities, makes spawned creature attack target

## 1.16

 - Add leap
 - More levitate tweaks/changes

## 1.15

 - Add "erase" spell, a targeted version of "rewind"
 - Tweak blob, make it more sphere-y
 - Tweak levitate

## 1.14

 - Add "tag" spell, places a "You were here" sign at your target

## 1.13

 - levitate improvements
 - rename "goto" to "gather", change the way it works

## 1.12

 - Add map spell (WIP! Very broken right now.)
 - Add levitate spell
 - Add ironskin + leatherskin, always show messages on invincible variants

## 1.11

 - Re-enable fling spell - it works! YOU CAN FLY!
 - Add force (+push), only works on living entities right now (for some reason)
 - Turn back on tower + stairs, but give no one permission to use them
 - Fix torch underwater
 - Some tweaks to blink
 - Randomize tree spell
 - Fix wolf spell
 - Fix gills spell
 - Add up/down push/pull all variants to "force"
 - Don't ever remove a recall marker (no real reason to, and it's confusing)
 - goto should only target Players, use "pull" for other entities
 - rename "force" to "pull"

## 1.10

 - Fill (recurse, etc) will target water now.
 - Make "goto" get the person farthest from you when aimed up
 - Add paging to /spells list
 - Add arrowrain spell variant

## 1.09

 - Use a generic "coversurface" function to refactor several spells (fire, frost) and make lightning -> storm
 - Add grenade spell, drops a primed block of TNT

## 1.08

 - Fix material cycling (Wand), and filling with the native material.

## 1.07

 - Add portal, 1.6-style, remove rest of nether spells.
 - Add sign spell
 - Use bukkitsched for cleanup, fix cushion
 - Fix recurse (?) It's working now, anyway.

## 1.06

 - Entity targeting update, with LOS checks

## 1.05

 - Bring back NG integration
 - Favor targeting monsters over players (or wolves)

## 1.04

 - Fix filling with air!
 - Add count parameter to "familiar", add "mob" variant
 - Make disintegrate and construct target other entities.
 - Fix score function in entity targeting, only target living entities

## 1.03
 
 - Add weather and lightning spells, modify fireball.

## 1.02

 - Tame target wolves!

## 1.01

 - Add back in the "wolf" spell.

## 1.0

 - Try to get this working with latest Persistence.

## 0.991

 - Yes, I'm doing that with the version numbers until 1.0 :P
 - Update config.yml to support internal permissions

## 0.99

 - Bring back tunnel, torches disabled.
 - Update permissions support for Persistence 0.55, drop Groups
 - Simplify the spells list 

## 0.98

 - But blast paramaters back in :)
 - Invincible!

## 0.97

 - Update for portal, NetherGate 0.45
 - More Gameplay integration - use BlockList from Gameplay
 - Auto-expanding undo (cave-in/breakage prevention) temporarily disabled
 - Finally fix findPlaceToStand so blink doesn't take you to 255 y!

## 0.96

 - Update for NetherGate 0.44
 - Add first-rev fire and lava spells - DANGEROUS!
 - Fix some torch bugs
 - Frost now puts out fires

## 0.95

 - Refactor to work with Persistence 0.49

## 0.94

 - Torch now casts "night" when pointing down. Don't know why I didn't do that earlier... (yes I do)

## 0.93

 - Mmmm... default constructor good!
 - Also, Mavenize!

## 0.92

 - Fix "shell", add "box"
 - Remove non-sticky materials like doors, torches, etc from the buildable list
 - Move material-giving code to common library

## 0.91

 - Make spell permission node names consistent
 - Colored wool! Yes! Thanks, SqualSeeD31
 - Add "with" command for construct, "sandblast" variant - thanks, anon!

## 0.90

 - Fix a really nasty material selection bug that allowed selection of items!

## 0.89

 - Merge "with" variant code to base fill- though I had done this already!
 - Separate "peek" and "window".

## 0.88

 - Keep trying to undo blocks in an unloaded chunk
 - Modify absorb to give you blocks starting at the right of your inventory
 - Always use the right-most inventory slot for construction, unless you have no building materials

## 0.87

 - Move BlockRequestListener to Persistence
 - Modify torch to turn netherrak to glowstone

## 0.86

 - Use NetherGate to create a portal-less portal for the portal spell!

## 0.85

 - First-round Persistence integration.

## 0.84

 - Add "world" variant of "peek" (!)

## 0.83

 - Add Netherrak and Slowsand to the destructible materials list.

## 0.82

 - Add "default" permission group.

## 0.81

 - Fix undo system - I was checking for chunk load in a bad way!
 - Add "portal" and "phase" spell, NetherGateintegration.

## 0.80

 - Check to see if a chunk is loaded before undoing a block, fail undo
 - Temporarily remove player death - the auto-recall drop is not multi-world compliant.
 - Fix a problem if you specify a player before its group

## 0.79

 - Add "peek" spell

## 0.78

 - Absorb and mine now handle variants properly (mine can mine LL now)- thanks to Firestar for sharing the code that clued me in!
 - Absorb and manifest now give you the material directly, instead of dropping it at your feet.

## 0.77
 
 - API release, many protected Spell methods made public. Some javadocs added.

## 0.76

 - Updated to work with Bukkit#210

## 0.75

 - Fix the familiar spell. I needed mc-dev, now!
 - Fix spell variants when used on the console.
 - Tree variants now work, by the way! So does auto-recall-on-death.

## 0.74

 - Fix a really heinous bug that was keeping all spells from saving/loading their properties.
 - Update to work with newest CraftBukkit changes.

## 0.73

 - Recall automatically drops a marker on death. (Requires a Craftbukkit update!)
 - Moved code out of plugin handler. LOTS of refactoring to make API cleaner.
 - Make ops automatically spell admins.

## 0.72

 - Fix "player tried command /cast" messages. Now only shown for unauthorized users.

## 0.71

 - Change around the way default material selection works will a few spells.
 - Make rewind and transmute targetable by default. Remove revert.
 - Remove wip spells. I can add them back as I test them- I'm tired of people reporting bugs on them...
 - Fix an NPE when trying to access permissions for a player who had none!

## 0.70

 - Disallow air selection by default- right now, only transmute and fillwith allow it. I'm not sure how intuitive this is!

## 0.69

 - Add revert, a targeted variant of rewind.
 - Add blob and superblob, variants of construct.
 - Add manifest, for getting a material by name.

## 0.68

 - Don't allow material selection for non-buildable materials (such as items!)

## 0.67

 - Fixed command-line use with spell variants!

## 0.66

 - Alter now knows what data values are valid for alterable materials.

## 0.65
 
 - More work on the undo system, make the cave-in-proof thing optional.
 - Add paint and shell spells.
 - Fix variants with multiple parameters.
 - Re-arrange spell materials, again- now that I can use right-clickable items again.

## 0.64

 - Improved the undo system to automatically add sticky blocks to the undo list, as well
  as auto-fill in sand and gravel that would fall with dirt.

## 0.63

- Added "selected material" system, which is more elegant than the "material selection" system :)
- Transmute now fills with the selected material in one click.
- Add "fillwith" variant to fill with the selected material.

## 0.62

- Added "disintegrate" spell.

## 0.61

- Added "map" spell, re-renders a dynmap tile.

## 0.60

- Added "recall" spell
- Removed the "upload" command from UndoableBlock, and all its uses. It seems it was unnecessary!

## 0.59

- Remove time, ascend and descend- make them variants of torch and blink instead.
- Add "night" spell, another torch variant.
- Make blink smart about putting you up on ledges.

## 0.58

- Fix the "allow-command-use" flag, which was backwards!

## 0.57

- Added a transformation list to "mine", so it can convert diamond ore to diamonds and coal ore to coal.

## 0.56

- Re-arrange the materials used for certain spells

## 0.55

- Some bug fixes, fix case-insenstive permissions
- Add "giants" to "familiar", add some spell variants: "monster" and "superblast"
- More "blink" awesome

## 0.50

- Support release, first release required by Wand
- Spells can now register more than one variant
- Each spell variant is associated with a unique material

## 0.41

- Update "blink", add awesomeness

## 0.40

- Fix "familiar"
- Add "transmute"

## 0.38

- Lots of bug fixes
- Added "construct"

## 0.36

- Added "familiar"

## 0.33

- Added "gills" spell, made lots of spells work well underwater

## 0.32

- Made "alter" recurse

## 0.31

- Added "frost"

## 0.30

- Added "arrow" and "tree"

## 0.29

- Fix multiplayer use of "fill" 
- Added "mine"
- Added "quiet" and "silent" plugin options

## 0.28

- Added "blast"

## 0.27

- Added "rewind" and undo system

## 0.26

- Adopted player animation hook, requires Bukkit update.
- Added "cushion", "tunnel", "pillar down"
- "heal" now working
- Updated "blink" to "ascend" or "descend" automatically
- Updated "torch" to cast "time day" when pointed at the sky
- Implemented material choosing mechanic
- Added lots of configuration properties

## 0.2?

- Permissions system implemented

## 0.17

- Added "fill" and "time"

## 0.16

- Got "fireball" working
- Added "absorb"
- Renamed "extend" to "bridge"
- Shelving "tower"

## 0.10

- First release