# CHANGELOG

## 5.2

 - Fix Rocket Boots getting you kicked for flying
 - Several performance and memory optimizations
 - Proper rollback and replication of armor stands
 - Fix projectile hit FX
 - Some fixes for wand melee damage and short-range spell casting
 - Fix the Wolf House and other single-use items that build things

## 5.1.1

 - Disable NCP integration by default, fix concurrency issue
 - Disable wand melee damage (except swords)
 - Fix NPE when blocking with a non-magic sword

## 5.1

 - Remove the "elder" wand from survival configs. Just use the Wolf! (or copy it back in if this is a problem for you)
 - Add flight exemptions for NCP and vanilla flight checks for Fling and Lift
 - Add /mserver command, useful for moving other players between Bungee servers
 - Fix Hat eating your helmet if you cast it on water or lava
 - Fix some Master Sword glitches
 - Add some protection against losing player inventories on server crash
 - Balance/Fix Disintegrate, Torture, Monster/Familiar/Mob/Farm (add auto-undo)
 - Add smarter handling of "sound" property in FX, deprecate custom_sound
 - Disable crafting of a gold sword by default
 - Fall protection (and other protection) will protect your mount as well (Fling+Mount!)
 - Chests locked with Secure are now unbreakable unless you have the key
 - Brooms/Levitate no longer re-activate on login. Fall protection will, though.
 - Tracking Sense will prefer Players versus other mobs
 - Add Star Wars configs - Work-in-Progress!

## 5.0

 - Magic is now targeted for 1.8. See http://dev.bukkit.org/plugins/magic for 1.7-compatible builds.
 - Fix Blink/Phase taking you to the top of the nether
 - Make Secure keys unplaceable, aim up and cast for a replacement key
 - Brooms use the 3D model while riding
 - Fix command-block casting and automata with Factions and Lockette
 - Magic Sword, Invisibility Cloak and Night Vision Goggles all have a custom item model
 - Renamed Magic Sword to Master Sword, spells now upgradeable
 - Master wand now gets the engineer item, architect has a staff
 - Add some new mechanics for the Master Sword
 - Fix custom configurations with randomized enchanting
 - Add "Clear Effects" item to color shop
 - Multiple wands will be tracked for "/wand restore"
 - Broomsticks are now bound to the player, as is the Master Sword

## 4.9.8

 - Cure casts no longer count towards level up unless it has something to cure
 - Wound now does physical damage (added magic_damage parameter to turn this on/off per spell)
 - Non-engineering spells now cancel and deactivate if you let go of your wand or are Cursed
 - Schematics load asynchronously
 - Fix build/break permission checks on incendiary/exploding projectiles
 - Turned off pvp restrictions on most spells, preventing targeting players in PVP regions
 - Add support for Towny warzones, friendly fire, and arenas
 - Fix an item frame related dupe exploit

## 4.9.7

 - Separate break/build permissions- check MIGRATION log.
 - Expanded Towny integration, added "towny_wilderness_bypass" flag.
 - Add ability to apply damage directly via a FallingBlock hit to ThrowBlock and ModifyBlock
 - Some quick-cast fixes (Stash spell)
 - Fixes to enchanting progress with disabled spells (disabling Rollback will disable upgrading to Engineer now)
 - Add Secure Level 2
 - Added Tree to the Engineering path
 - Nerf stream, earth- add Stream Level 2 and 3
 - Fix an item dupe issue with /give'ing items directly into the wand inventory
 - Fix wands eating XP given directly to a player

## 4.9.6

 - Add quick-cast from inventory, hit Q on a spell.
 - Add brush selector paging
 - Fix wand duplication glitch on certain death cases
 - Fix compatibility with Scavenger plugin
 - Add "use_magic_damage" config option to turn off "magic" (splash potion) damage source
 - Fix XP scale to 1.8 standards (effects XP-based shops)
 - Add "regenerate_while_inactive" config option to turn off wands regen'ing mana while not held
 - Tweaks: Push, Mine, Flash Bang, Flare (lvl 2)

## 4.9.5.1

 - Hotfix for 1.7 compatibilitiy
 - /mmap list supports regex searching
 - Add /mmap player command for creating player portraits without the Camera spell
 - Add Lockpick spell (admin-only)

## 4.9.5

 - Add "mmap" command for dealing with image maps (load, give, list)
 - Add ability to import Pixelator images into Magic
 - Optimize '/magic load'
 - Tweak spell FX so they originate at the wand and end at the actual hit location
 - Fix issue with inventory-mode wands when the default mode is chest
 - Fix glitchy Admission behavior
 - Add Secure spell
 - Add Levels 2 and 3 to Wound, Kill and Torture spells
 - Support animated GIFs in image maps (though you may not want to use it, bandwidth hog!)

## 4.9.4

 - Fix very short visibility range of firework effects
 - Add "command" trait for simple command NPCs
 - Group /mskills skills by class
 - Fix losing your broom if you die while riding
 - Add "/mtrait invisible" for invisible NPCs
 - Add configurable entity hitbox dimensions
 - Some fixes the player death / inventory handling for compatibility
 - Update Elder wand to be more like the Wolf
 - Update Student,Apprentice,Master,Engineer and Architect wands to match enchanting paths
 - Add Wand Shop

## 4.9.3

 - Restore custom icons!
 - Add Magnetic Leggings
 - Try to work-around other plugins (/hat) putting the temporary hat in the player's inv.
 - More fixes to chest mode
 - Fix blocks not dropping when placed inside a magically constructed house
 - Some fixes to death behavior that may have interfered with other plugins
 - Fix loading banners from schematics
 - Add page# to /mskills command
 - Fix errors sending firework effects to other worlds

## 4.9.2

 - Temporarily turned off custom item icons due to complications with 1.8.4 client changes.

 - Added Heroes integration (can use Heroes skills with the "heroes" wand or /mskills items)
 - Added Hulk Boots
 - Add Cleric's Robes
 - Allow crafting recipes for spells, brushes and normal items
 - Firework effects are produced entirely client-side.
   Hoping this addresses some "entity tracker" lag a few servers have been seeing. It might not.
 - EffectLib now runs effects asynchronously. Should help with lag from spamming complex effects like Helix.
 - Fix "indestructible: false" on wands not working
 - Hunters will mostly usually clean up after themselves on death
 - Some fixes to EMP and SuperEMP
 - Add allowed-wands and blocked-wands custom region flags

## 4.9.1

 - Fix chest mode (some spells not showing up)
 - Fix a potential conflict with other crafting plugins
 - Magically created blocks won't drop items when broken
   * This is not a 100% exploit fix with engineering magic
   * But it should cover auto-undo spells.

## 4.9

 - Fix running on CraftBukkit 1.8
 - Added tons of new items (available in the "magicitemshop")
   - Add Wizards' Robes set (chainmail armor, grants +10% mana each)
   - Add Apprentice Robes set (chainmail armor, grants +5% mana each)
   - Add Rabbit Boots, Rocket Boots, Flash Boots
   - Add Invisible Cloak
   - Add Night Vision Goggles
   - Add Diving Helmet
 - Magic Hat grants +10% mana regen and +20% max mana
 - Fix item shop "price" lore not being removed when purchasing
 - Removed wand health/hunger regen and haste properties. Replaced by potion effects, wands should auto-migrate.
 - Removed Gills from the enchanting path (in favor of Diving Helmet item)
 - Fix another Rollback exploit (Thanks, LordBoos!)
 - Fix wands wiped if logout on death
 - Some general safety precautions for saving player data, not sure if it fixes anything

## 4.8.5

 - Add GriefPrevention support
 - Add Walls and SuperCylinder spells
 - Some fixes to prevent exploits and bugs with double chest rollback
 - TNT, fireballs and thrown blocks won't affect a protected region
 - Fix Chop and Mine recursion, add more levels
 - Add support for custom-named physical currency
 - Add Magic.wand.enchant and Magic.wand.craft permissions (true by default)
 - Add native schematic loading- supports paintings, item frames, flower pots, and more.
 - Removed WorldEdit soft-dependency
 - Fix some chest mode issues, remove "fake hotbar"
 - Add Magic.undo_others permission node for Rollback spell
 - Some additional protection for dragging items in a GUI (still needs work)
 - Remove unused "signs" parameter from ConstructSpell, using raw NBT for sign updates now
 - Some fixes/improvements to Blink
 - Magic Hat now grants a 20% max mana boost
 - Added Tornado Level 2

## 4.8

 - Fix Magic Hats disappearing, make them craftable (nether star + leather helmet)
 - Magic hats now perform one of a random set of actions
 - Add configurable list of entities to auto-undo. Now includes armor stands and dropped items.
 - Re-balance enchanting and economy a bit
 - Fix restoring chest contents creating glitched "0" amount items
 - Fix items sometimes dropping from exploded chests set to auto-undo
 - Remove "bounce" spell
 - Add support for commands in Recall (for /f home, /spawn with warmup, etc)
 - Fix Recall putting you in the ground/walls sometimes
 - Add basic physical economy to shops (defaults to emeralds if Vault is not found!)
 - Add Broom shop and Automata Heart buyback shop

## 4.7.1

 - Fix mana regen rate (it's 4x what it should be)
 - Allow mana to regen while not holding a wand
 - Add Magic Hat item

## 4.7

 - Remove special characters from messages.defaults.yml files
   (Seems to be causing issues on Windows servers)
 - Various fixes for copying and restoring paintings and item frames
 - Add Gatling Gun spell
 - Add Tornado spell
 - Add Rocket spell - ride a firework
 - Add Forest spell - biome-specific forests
 - Add Firework action for launched fireworks with effects
 - Time spells are now undoable
 - Re-balance Laser, Stun and Curse
 - Action-ize several more spells
 - Add Magic.bypass_restricted permission
 - Allow overlapping spell FX
 - Fix SuperAlter
 - Allow wearing Banners with Hat spell

## 4.6.1

 - Fix for Laser not undoing when double-cast

## 4.6

Check MIGRATION notes for important information on this release.
This applies if you have customized spells, or use MagicWorlds or dtlTraders.

 - Big under-the-hood changes to support batched actions
 - Lift spell works on entities
 - Fling works while mounted
 - Earthquake throws entities
 - Laser lasts for a few seconds, has continuous effect
 - Reflect works at arbitrary angles
 - Improved handling of config file errors
 - Spell shop improvements:
   - Added customized spell shops via "spells" parameter
   - See engineershop and architectshop
   - Add balance to inventory title
   - Has confirmation screen

## 4.5.5
 - Fix Map and schematic brushes (broken in 4.5)
 - Add match_data option to Construct spell to turn on replacing variants
 - Add Vault integration for economy and item/block names
 - Add "cost" option to Magic citizens trait
 - Add "spellshop", "addspell" and "enchantwand" spells for simple one-spell NPC shops
 - Add support for Factions 1.8.2 build
 - Updated/Fixed dtlTraders integration
 - Some fixes to casting from console

## 4.5
 - Implement brush selection. Shift+right click to change brushes.
 - Allow different map scales (/mgive brush:map:128)
 - Add custom icons for special brushes
 - Add custom icons for all spells
 - Fix progression to Engineer/Architect (undo spell changed to rollback)
 - Removed the Kamikazee, Pyramid, Stalactite, Storm and Shields spells
 - Some fixes to tracking of spell casts for upgrades
 - Add config option to enable/disable PS field "cast anything" for owner

## 4.4

 - Update Arrow to work with 1.8.3
 - Add Tracking Sense spell
 - Add Flash Bang spell
 - Update/Improve Pollinate spell
 - Add FX for spell level up and undo
 - Fix PVP flag check in PreciousStones fields
 - Add "/magic check" command for debugging permissions
 - Add "/magic debug" command for debugging spell casts
 - Fix Kill perma-breaking blocks
 - Improved "keep" behavior for wands

## 4.3

 - Fix Reflect
 - Updates to Potter and Bending configs
 - Fix reloading configs with customizations and spell levels
 - Fix some spell balancing issues (Magic Missile Level 2)
 - Add Citizens "magic" trait for casting spells
 - Fix the manaboost item (though it's still kinda wonky and useless)
 - Some fixes to player data saving
 - Fix Architect levelling and some general OP-wand enchanting issues
 - PS fields now allow the owners to cast any spell, regardless of pvp flag
 - Fix destroying Shell, Blob with spells leaving permanent damage

## 4.2

 - Custom sound effects (requires resource pack)
 - Add glow to wand brush icons (configurable)
 - Fix some overlapping undo glitches
 - Spells now level up with use, not when enchanting
 - Add allowed-spell-categories and blocked-spell-categories WGCustomFlags
 - Some fixes to explosion undo, PreciousStones integration
 - Add configurable (per-wand) item glow
 - Earth, Stream and Laser all do entity damage

## 4.1

 - Added custom spell icons (!!!111oneone11!)
 - Fixed Counterspell
 - Allow for transparency in images on maps
 - Improved WorldGuard integration to properly handle flags set with -g member, -g nonmember, etc
 - Add instructional messages to the wand to guide new players.
 - Undo system improvements, will now handle flowing lava/water, falling blocks (again),
   and broken attachments (signs, torches, etc)
 - The Gather spell is now a wrapper for /tpa, to be less griefy
 - Fix Magic damage (magic missile, etc) against witches
 - Some fixes/improvements to Automata death behavior
 - Auto-undo explosions won't drop blocks
 - Fix the camera spell (new Mojang skins location)
 - Fixes/work-arounds for non-working player skulls (Shrink spell)
 - Fix compatibility with WGKeepInventoryFlags plugin
 - Fix locked wands (magic items) absorbing upgrades
 - Add "can't use" icons to spells in hotbar

## 4.0

 - All default spells, wands, and enchanting configs changed
   - All world-modifying spells now auto-undo
   - Rebalanced, with a focus on dueling and not being "Too OP"
   - Brooms and cameras are now craftable
   - Only one wand type is craftable, and it now levels up from Beginner to Master
   - Added level progression to a variety of spells
   - Engineering spells are unavailable naturally.
     - A Master wand can be upgraded to an Engineering wand
     - This requires some spells, ideally provided via a server tutorial
     - Engineering wands can upgrade to Architect in the same way
 - Many spells converted to new action system (WIP)
 - Add Lockette integration
 - Fixed/Updated Factions integration
 - Add "loud" configuration option for spells
 - Undo system now tracks fire spreading

## 3.9.4

 - Update EffectLib to latest ParticleEffect code (see migration notes!)
 - Add support for PreciousStones and Towny
 - Add hotbar_count to wands, multiple hotbars!
 - Update to WorldGuard 6.0

## 3.9.1

 - Hotfix for wand inventory saving issues, particularly in 1.8
 - Add hitbox-based targeting. Only used in Magic Missile, for now.

## 3.9

 - 1.8 Compatibility
 - Some improvements to wand spell tracking to avoid lost spells
 - Fix ability to glitch through walls on a broom

## 3.8

 - Fix combining two wands of different paths on an anvil
 - Toggle hotbar spell glow to show cooldowns/casting ability
 - Add cooldowns to spell lore
 - Fix "crafting_enabled" option- sorry about that! :(
 - Allow overriding wand inventory sounds
 - Avoid targeting pets (tested with EchoPet)

## 3.7

 - Add spell levelling system (spell progression via level variants)
 - Changed wand inventory navigation mechanics (left/right click outside inv to change pages)
 - Add per-spell protection pnodes (must be enabled per-spell)
 - Fixed several bugs with tracking hanging items
 - Fix dtlTraders support.
 - Fix a bad bug with the "chest" inventory mode
 - More wand data saving / duplication fixes
 - Add some new mana display modes (durability, glow) - experimental
 - Some undo fixes and improvements, always completely undo on logout (blocking undo)
 - Add dynmap option to only show spells from players (not mobs or automata)
 - Some fixes to Familiar spell (long-standing bug re: multiple players)
 - Bailed on the Bukkit Metadata API PR, simplifying/optimizing data storage instead.
 - Added "Earthquake" spell (and directional vector parameter for ConstructSpell)
 - Some fixes related to mana-based casting costs (separate "mana" vs "xp" costs now)
 - Add "stealthy" casting while crouching (or via wand property)

## 3.6

 - Drop support for Bukkit 1.6
 - Update to EffectLib 2.0 - all effect class names have changed!
 - Add "color" and "particle parameters to spells, for one-off FX color overrides.
 - Completely remove Magic.powered and Magic.protected pnodes, for good this time. (Pex!)
 - Add /wand restore command for re-creating a bound wand
 - Fix bypass_confusion parameter
 - Improve "magic damage", now with entity source
 - Familiars don't drop loot when killed
 - Projectiles and explosions will add hanging entities to the undo queue when broken
 - Fix shrunken heads (UUIDs.. checking for lag)
 - Fix target direction ovveride parameters, useful for FX
 - Fix painting copy/undo (Thanks, Chilinot!)
 - Fix /magic load working with spells that have changed class
 - Fix enchantment path/level scaling (all levels were maxed)
 
## 3.5

 - Fix stack overflow error in the welcome wand feature.
 - Disable inventory backup feature by default. I may end up removing it, it causes issues.
 - Fix tab-completion of particle and SFX, as in /wand configure effect_particle <tab>
 - Lots of various bug fixes, particularly wand duplication/overwriting bugs
 - Add Wand particle FX overrides
 - Add DoorSpell, to open/close doors
 - SuperConvert/Convert will now work with material variants
 - Improved clone/replicate messaging
 - Add Paste spell
 - Add Grow spell (the opposite of Shrink, but doesn't deal damage)  (HP: Engorgio)
 - Fix /mgive, /wand configure particle_effect tab completion
 - More undoable spells (Blink, Fling)

## 3.4

 - Put safety backups in place for server crashes with a wand inventory open
 - Add Reflect spell, tweak Shields Spell
 - Spells can now construct blocks that non-engineering spells break (Shields spell)
 - Magic blocks can reflect spells on target (Reflect spell)
 - Wand levelling system (not currently used in the default config- see HP enchanting.yml)
 - Re-worked undo system, auto-undo spells are also counterable/Rollback'able
 - Fixes and improvements to wand enchanting

## 3.3.1

 - Fix 1.6.4 support
 - Add Magic Missile, Silence and Blind spells
 - Fix EMP and Fill permissions and destructibility checks
 - Add WGCustomFlags integration (allowed-spells and blocked-spells set flags)

## 3.3

 - Wands can now have multiple distinct enchanting paths, which are configurable
 - Crafting recipes are now configurable, and several new default recipes have been added
 - Add CommandSpell, can run any command as a spell. See:
   http://jenkins.elmakers.com/job/MagicPlugin/doxygen/classcom_1_1elmakers_1_1mine_1_1bukkit_1_1spell_1_1builtin_1_1_command_spell.html
 - Add Respawn spell and Respawn Token items.
 - Add /wand unlock command
 - Integrate EffectLib directly (no dependency needed)
 - Tons of spell tweaks for new custom Harry Potter configuration
 - Add "overrides" wand property, great for custom items
 - Add "example" and "add_examples" configuration options, for easy example config testing

## 3.2

 - Fix for permissions issues in 3.1 (also in 3.1.1 hotfix)
 - Fix automata passing through diagonal walls (like a dome)
 - Store owner UUID in wand data as well as owner name
 - Add some economy helper functions and price automation
 - Show custom entity names in targeting messages
 - Add support for Entity-based Mages (MythicMobs integration)
 - Add Cure spell
 - Add Counterspell spell
 - Add Shields spell. Placeholder until 1.8's Barrier block arrives :)
 - Add EffectLib integration (awesomesauce)

## 3.1

### The Rollback Release!

 - The Curse spell will now temporarily cripple a Mage
 - Improved Shrink - works on giants, pigmen, slimes.
 - Add Pyramid spell, Blob is now a sphere again
 - Spells will automatically reactivate on login (Fly, Levitate, etc)
 - Fix Hat being exploitable for blocks
 - Add Mount spell
 - Curse, Boon, Wither etc effects stack properly
 - Large code refactor, move base spell classes to MagicLib.
   * Devs can now add custom spells to your Plugin without building against MagicPlugin.
   * MagicLib has no dependencies other than MagicAPI and Bukkit.
 - Many spells are now undoable (with Rollback or Rewind):
   * Collapse, SuperCollapse, Blast
   * Grenade, Cluster Bomb, Incendiary Grenade, Cluster Grenade
   * Fireball, ICBM, Wither, Meteor Shower, Carpet Bomb
   * Boom, KaBoom, Nuke
   * Sandblast, other falling blocks
   * Curse, Boon, Perk, Blessing
   * Recall, Gather
 - Entity changes are now undable - even spells like Arrow Rain, Meteor Shower .. bring back the dead with Recall!
 - Rewind and Rollback will show the name of the spell they undid
 - Automata won't target cross-world
 - Recall won't warp cross-world, too confusing
 - Make the wand enchantable item swapping optional, and off by default.
 - Automata can now randomly choose from a set of spells to cast, and have multiple difficultly levels
 - A slightly better/hackier work-around for annoying Bukkit drag event
 - Wand color will morph depending on spells cast with it
 - Wand haste, hunger and health regen use potion effects (looks better, avoids conflicts)
 - Add Elementals integration

## 3.0

 - Fix shading of mcstats to avoid conflicting with other plugins that use it.
 - Add "hybrid" mana display, which is the new default.
 - Add an official resource pack, with a special wand item (wood hoe) and command block texture.
 - Config-drive wand randomization limits, see new section in config.defaults.yml.
 - Make Regenerate safe and undoable, add SuperRegenerate.
 - Add SuperRepair spell
 - Automata will have randomly generated names (thanks, @lathame!)
 - Allow removing brushes and spells from your wand
 - Finalize dtlTraders integration
 
 - ... WHOOHOO!

## 3.0-RC2

 - Fix enchanting in CB 1.7.2 R3 and above (Woohoo! I guess this is not a CB bug, but a "feature"- though I did submit a pull request)
 - MCStats integration - this can be turned off in config.yml.
 - Fix short-lived arrows in CB 1.7.5 and above
 - Added "Worm", a new Automaton. Improved and differentiated the others.
 - Improvements / Mechanics changes to Recall (and Recall Token item)
 - Improvements / Mechanics changes to Gather
 - The "Backup" and "Repair" spells will auto-create worlds (can be disabled in config.yml)
 - Added "Backup" and "Repair" to the natural enchant list, and the Wolf and Admin wands.
 - Improve /magic give, add tab-completion.

## 3.0-RC1

 - First official release candidate!

 - Add Backup and Repair spells.
 - Default "protected" and "powered" permissions to false
 - Add commit_on_quit config.yml option to save memory (at the expense of undo ability after logout)
 - Improved tab completion for /cast and /wand parameters, useful for experimentation
 - Add/Remove physics handler dynamically (thanks, @Silversbane!)
 - HUGE code cleanup, API preparation.

## 2.9.9

 - Change enchanting range to 10 - 35
 - /magic cancel will now cancel any pending construction batches
 - Add "Gasp" spell for underwater bubble (Thanks, kylieveronica!)
 - Some tweaks and improvements to Blink and Phase, won't passthrough bedrock
 - A lot of wand nerfing and balancing
 - Randomized effect colors for randomized wands
 - Add "/magic list autonoma" command
 - Claiming a wand (on an anvil) will set wands to "keep" and "bound" by default.
 - Add "Recall Token", the first magic item. Lets you cast Recall 10 times, and you keep it on death.
 - Automata will respect build permissions for WG regions and Factions claims
 - Mob spawners are now restricted, but copyable by supermages.
 - Add "powered" pnode for always-on superpower (for mods, mainly to bypass restriction list)
 - Updated Life spell, added Life 3D spell for some real mad science. (And for me to breed new Virus rulesets)
 - Add very basic beginnings of an API. Changed a lot of spell parameters for consistency.
 - Recall can now support CommandBook warps.
 - Renaming a wand on an anvil will clear its description.
 - Shrink will change a mob to a baby before it turns it to a skull
 - Add "wand duplicate" command

## 2.9.8.AUTOMATA

 - Add "Hat" spell.
 - Add "lantern" schematic. Add several of the newer spells (Fly, Cluster Bomb, etc) to the enchant list.
 - Add "Simulate 3D" spell.
 - Add "Virus" spell. Mad science, be careful!
 - Add "Life" spell. Also mad science, but less dangerous (it does require a 128x128 block area though!)
 - Add "Animate" spell- more mad science! ... also "Hunter" .. playing around here, lots of currently-OP-dangerous spells.
 - Add "SuperEMP" and "SuperCube" spells.
 - Allow clicking (or hotbar button) on an empty space to clear active spell. Useful for enchanted swords and other tools.
 - Hopefully really fix my pesky undo bug (Block data hash code fail!)
 - Add customization station in Wolf House hub room.
 - Two-phase undo for cleaner cleanup (attachable blocks)
 - Enable periodic auto-save by default, once an hour.
 - Add configurable pending batch limit for constructions. Mostly a safety thing for out-of-control Automata.
 - Command blocks, portal and ender portal blocks are restricted, except for superpowered wands.
 
## 2.9.8.1

 - Add "Pickpocket" spell, like invsee at close range. (er, maybe not working yet, not sure!)
 - Add "Music" spell, plays a record for the target player.
 - Add "keep" and "bound" wand options, for wands that you keep on death and/or are bound to a specific player.
 - Fix dtlTraders integration. Wands, spells and material brushes can be sold by traders. (Requires v 3.1.0, currently a dev build! Thanks again @dandielo !)
 - Add /magic give command, mainly for use with dtlTraders, gives a spell, material brush, or wand upgrade item.
 - Add "allow_cross_world" parameter to Recall spell, the default is to disallow Recall'ing to another world now.
 - Allow selling spells and material brushes on Essentials signs.
 - Citizens integration (spells will ignore NPC's)
 - Some multi-world fixes to the Camera spell. Allow targeting NPC's.
 - Fix permission use with wildcards... umm.. I thought the GM handled this??
 - Added "Magic.wand.destruct", false by default- use if you want to wipe player inventories of wands if they get somewhere they don't have permission to use wands.
 - Added "Magic.wand.override_bind", a perm node for ops so they can handle bound wands.
 - Make Phase configurale (e.g. if you have two worlds, you can link them with a specific scale)
 - Add "Magic.protected" pnode which makes you protected without a wand.
 - Add tx2, ty3, tz3 and dtx2, dty2, dtz2 parameters to Fill, Construct and Regenerate.
 - Fix players getting kicked if they drop their wand while the "chest" inventory is open.

## 2.9.8

 - Add "Carpet Bomb" spell, rains down TNT from above.
 - Add "Meteor Shower" spell, rains down fireballs from above.
 - Add "EMP" spell, fries/activates nearby electricals
 - Add "Perk" spell, a low-powered version of Boon
 - Add "Fly" spell, a cost-free and sped-up Levitate.
 - Make arrow rain more like a rain of arrows.
 - Improve undo system and overlapping regions (out of order undo, etc)
 - Projectiles can be used without a player (e.g. command blocks)
 - All spell messages are now standardized and localized in messages.yml. All the in-game text can be changed.
 - Spells can have a random backfire/fizzle chance. Use with Disarm.
 - Improve Recall - now cycles between various waypoints when targeting up/down.
 - Familiar skeletons will have bows.
 - Add "Simulate" spell (a pet project of mine.. may not interest you unless you have a thing for Conway's Life)
 - Add "Glider" spell (WIP, potentially destructive or server intensive!)
 - Add "enable_glow" option to spells.yml (for @broluigi)
 - Add global "bypass_build" and "bypass_pvp" parameters to config.yml (for @LHammonds)
 - Fix cooldown messages for lengths longer than a minute (up to hours)
 - Fix material names in cast messages.
 - Fix /spells <category> NPE (Thanks, @Felikahn)
 - Player data is now stored by UUID instead of name. Legacy data should auto-migrate.
 - Fix Construct batches modifying more blocks than necessary (and hopefully fix issues with undo on large batches)
 - Fix hotbar spells getting lost on wand organize
 - Add "quiet" option to wands, useful for custom message display or for recording (quiet = 1 or 2 for less chat spam)
 - Added "magic list maps <keyword>" command (magic list -> magic list wands)
 - Add configurable sound effects for wands

## 2.9.7

 - Fix 1.6 Support (sorry!)
 - Add dtlTraders integration (WIP), for selling wands with Citizens NPCs.
 - Update Stash spell, make it awesomer.
 - Change default wand mode to "chest", I think it's more accessible.
 - Fix Blessing not targeting yourself if you're superprotected.
 - Fix targeting direction command overrides with /cast (dx, dy, dz)
 - Add "beacon" and "flare_beacon" material schematics.
 - Add support for "undo" parameter in fill spell.
 - Fix wand inventory reorganizing, duplicating spells.
 - Scale maps by 1/8 to make portraits look better, and allow for vertical orientations.
 - Fix some weird targeting issues at low elevations.
 - Add "wand enchant" and "wand unenchant" commands for managing custom wand items.
 - Support material variants (e.g. double_plant:2) in casting costs.
 - Some fixes for the overlapping undo problem, may not be 100% done but seems to work ok.

## 2.9.6

 - Allow use of /cast without a player, as in a command block.
 - Add "count" parameter to grenade spell. (Add "Cluster Bomb" spell)
 - Add default "name" and "description" nodes back to wands and spells for easy customization.
 - Add "Wolf House" and "Tent" and "Cage" spells. WIP.
 - Removed/Disabled the "Light" spell (for now.. ?)
 - Support different item types for wand templates.
 - Copy command blocks from schematics.
 - Add "px, py, pz" and "dx, dy, dz" parameters to all spells, to force player location and direction. (Scripting!)
 - Add "tx, ty, tz" parameters for target location.
 - Add "bypass_undo" parameter for scripted spells that won't store Rollback information.
 - Allow special materials in "material" parameter (e.g. clone, erase, schematics)
 - Add "Extend" spell, wip.
 - Add "bypass_pvp", "bypass_build", "cost_reduction" and "cooldown_reduction" parameters.
 - Add "dmx,dmy,dmz" and "dtx, dty, dtz" parameters for controlling relative targeting and material targeting.
 - Add "mm" parameter for modifying a material selection (e.g. "erase" with "schematic" for "material")
 - Add "clearcache" command to clear schematic and image map cache files.
 - Add "max_power" config.yml option to limit crazy uses of /wand configure power XXXXXXXXX.
 - Add "undo_max_persist_size" config.yml option to avoid huge player yml files.

## 2.9.5

 - Add Factions support for build perms.
 - Add "Blessing" spell.
 - Fix castp permission (target player doesn't need permission, only command executor needs it)
 - Fix alter on entities. (Superalter will still not target entities)
 - Some overall balance tweaks to various spells.

## 2.9.4

 - Fix Essentials' ItemDB integration
 - Fix using overridden command names (use label, not command name)
 - Reduce item pickup event priority (fixes using inventory mode with special pickups, like from MobMoney)
 - Add Regenerate spell (admins only, no undo!)
 - Fix blast and superblast ("destructible" spell property changed)
 - Add "SuperRing" spell, allow different ring/disc orientations.
 - Add "mana_display" config property, which can be used to display mana as a number rather than a bar.
 - An empty wand will fill on activate if fill_wands is set to true.
 - Allow "wand" on Essentials sign for an empty wand.
 - Some fixes to blink's ledge detection that would let you pass through something unintentionally.
 - Fix permissions if you have Magic.commands.wand.fill but not Magic.commands.wand, it should still work. (lathame)

## 2.9.3

 - Add configurable spell effects. See spells.default.yml for instructions.
 - Fix "magic load" command loading changes to spells.yml
 - Removed the "IronSkin" and "LeatherSkin" spells- not really useful.
 - Add "Stash" and "Flamethrower" spells.
 - Only load player data on demand, save on quit.
 
## 2.9.2 
 
 - Add "schematic" brushes, requires WorldEdit for schematic loading.
 - Make superconvert work better for cleaning up lava and water.
 - Implement "load_defaults" parameter for spells.yml and wands.yml, if you don't want any of the default spells or wands.
 - Added a new wand mode for interaction using a chest inventory. Start the basics of per-player wand mechanics.
 - Fixed a really bad bug that could cause a wand to overwrite another wand when dropped. This may or
   may not have been a recent bug, but I'm releasing this as a patch fix for it just in case.
 - Several fixes for lesser wand inventory glitches.
 - Add a safety check for the Essentials Mailer integration. (for @ghosttractor's issue)

## 2.9.1

 - Respect PVP flags for WorldGuard regions.
 - Add global cost_reduction and cooldown_reduction parameters to config.yml
 - Add some hacks to prevent using wands while in creative mode.
 - Add "map" material brush. Experimental :)
 - Add Laser spell
 - Add "Chop" spell. Also make "Box" sit on the target. (MIGRATION NOTE)
 - Gather, push, push, force and disarm will ignore targets with Protection X
 - Material brushes will copy player skulls (clone, copy, replicate- not absorb though)
 - Add "Sunny Day" spell, remove day and night spells.
 - The camera spell will now work on most mobs (not zombies or creepers or skeletons, though...)
 - Shrink and camera will now work on some blocks (cactus, tnt, chests, melons, logs and pumpkins) (MIGRATION NOTE)
 - Some improvements to replicate and clone, safety checks for attachable blocks
 - Split alter up into SuperAlter and Alter
 - Some improvements to wand organization and owner display
 - Fix facial accessories not showing up in player portraits.

## 2.9.0

 - Completely re-organize the config files. (MIGRATION NOTE!)
 - Add file cache for URLMap images.
 - Added disc, superdisc, dome and superdome spells.
 - Add "/magic clean" command to remove unowned lost wands.
 - Add owner to wand description, if no other description is present.
 - Add owner filtering to "/magic list" command.
 - Make arrows fired with the arrow spell short-lived. Kinda hacky.
 - Add option to age dropped items. Might try to make more specific in the future.
 - Tag spell adds to undo queue.
 - Undo system (and clone+replicate) handle signs, chests, and command blocks. Remove those from the indestructible list.
 - Don't allow building anything at all from the building list. Affects copy, clone and replicate.
 - Superconvert now only replaces the target block's material.
 - Fix a glitch involving dropping spells out of wands.
 - The phase spell will return you from The End, should work better in general.
 - Replace reload command with save/load
 - Some improvements to the frost spell.
 - Reset player walk/fly speed each wand tick (to override Essentials :P)
 - Fix wands losing their effect colors.
 - Dynmap will now show spell casts (enabled by default)
 - Allow wand randomization for levels past 40 - e.g. "80 levels" will add level-40 enchants twice.
 - Added "Column" (superiterate) spell.
 - Added "Neutron Bomb" spell, make wither apply wither effects (MIGRATION NOTE)
 - Prevent destroying wands via crafting.
 
## 2.8.9.1

 - Fix right-click-to-cycle option.

## 2.8.9

 - Fix cancelled batches (via undo) not being undoable after cancelling.
 - Add active (mana per second) casting costs to spell descriptions
 - Add "self" parameter to disarm spell (for remote disarming with castp)
 - Fix the iterate spell using the replicate material. Strange effects though :)
 - Save lost wands for easier re-location.
 - Optimize the pending block construction task
 - Simplify config files. (MIGRATION NOTE!!)
 - Add /magic list command (WIP, currently lists lost
 - Add phase spell
 - Superblob will now replace more block types. Blob remains the same.
 - Some general fixes to the "find place to stand" code (ascend, descend, phase)
 - Add "wand" recall type, some general recall improvements.
 - Add spell casts to dynmap (WIP)
 - Fix not being able to drop items with a wand active.
 - Add a hard limit to range and radius multipliers
 - Make wands.yml and spells.yml additive
 - Restore 1.6 support. The blink spell needs some cleanup.
 - Add "bounce" spell

## 2.8.8

 - Add log to destructible material list
 - Implement global "auto_undo" configuration option. Disable undo on most spells by default.
 - Undo will now cancel any pending constructions before undoing a finished one.
 - Add "check_destructible" spell property to clearly indicate which construction spells use the destructible list.
 - Add /magic commit command
 - Add flag to disable WG protection
 - magic.yml is now additive to magic.defaults.yml, if present (MIGRATION NOTE)
 
## 2.8.7

 - Add /wand organize command
 - Override shift+click in wand inventory to quick-select a spell or material
 - Add "mail" spell, not made for wands, but for setting up "polling stations".
 - Fix permission on fire spell
 - Add visual effects to fling and levitate
 - Add message colorization (MIGRATION NOTE)
 - Make the blast spell a little more interesting.
 - Add "commitall" variant to UndoSpell, to clear everyone's undo queue.
 - Fix tag spell to use a better date formatter.
 - Refactor wands to not store materials by id (though the data byte is still rate)
 - Implement replicate and clone materials (!)
 - Nerf a lot of the construction spells. Wand's "power" can be used to create larger constructions.
 - Better labelling of spells in the wand inventory.
 - Fix "disabled map" spam

## 2.8.5

 - Tab completion improvements
 - Collapse and Toss will no longer drop blocks (thanks, Drkmaster83!)
 - Break max_power_multiplier up, allow a wider range of wand power effects (MIGRATION NOTE)
 - Separate out permission and indestructibility tests- weird behavior with bedrock, etc.
 - Some updates to the recall spell, add "death" type and reliable tp-to-death-point.

## 2.8.4

 - Added priority to map urls, default is to use mcserver throttling
 - Added visual effects to absorb spell
 - Add permission check to arrow spell
 - Add some work-arounds for players using the armor slots to smuggle items in/out of the wand inventory.
 - Add a work-around for a player placing a block out of their wand inventory.
 - Add Iterate spell. Make Earth spell temporary.
 - Add Collapse and SuperCollapse spells.
 - Add volume and dimension limits to SuperConstruct spells.
 - Fix a potential NPE when saving a player's undo list, if it is empty.
 - Fix ICBM Spell
 - Some tweaks to wand organizing
 - Make alter pass through entities that can't be altered.
 - Add tab-completion. Very basic at the moment, but very helpful for /castp and /wandp admin commands.
 
## 2.8.3

 - Add /wand combine command
 - Implement wand organization (using anvil)
 - Add more scripting ability to fling spell
 - Combining two wands takes ownership of the new wand
 - Wand effect colors will mix when combined
 - Match flare and boom effects to wand color
 - Use volume-based rendering triggers in dynmap for large constructions
 - Add earth and stream spells (MIGRATION NOTE: lava spell needs updating in spells.yml, LavaSpell -> IterateSpell)
 - Add "/magic search" and "/magic cancel" commands.
 - Add effects to pillar, earth, stream, lava, bridge and stalactite spells.
 - Add build permission check to the grenade, projectile and familiar spells.
 - Collapse properties and parameters (MIGRATION NOTE)

## 2.8.2

 - "name" parameter works with url and id maps.
 - Fix WG build permissions on boom spell. (Oops, sorry!)
 - Enable combining wands on an anvil- WIP, hacky.
 - Some fixes for the camera spell involving the world not getting saved after a new map was created.
 - Fix cooldowns on fill and superconstruct spells
 - Allow casting boon on another player (it's targeted now- aim up for yourself, like heal)
 - Implement wand descriptions and ownership
 - Fix limited-use wands (er, fix it so they break!)
 - Added better feedback to wand and wandp commands
 - Added /wand describe command (technical)
 - Add visual effects to boon and curse
 - Added additional projectile spell types (MIGRATION NOTE: the fireball and icbm spells need updating)
 - Added "wither" spell

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
 - Add dropped wands to the dynmap (!)
 - Add permission nodes for Magic.wand.add.spell.<spellname>, Magic.wand.<wandname> and Magic.wand.add.material.

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