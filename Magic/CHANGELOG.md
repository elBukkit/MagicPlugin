# CHANGELOG

# 8.3

 - Rename Wear action to Equip (Wear still works). Supports any inventory slot, main_hand or "free" to find a free slot.
 - Add "snowy" automaton for making a 32x32 snowy area
 - Use Vanish for Cloak and Decoy spells
 - Re-work appraiser NPC shop, re-balance emerald/diamond/gold/iron prices
 - Some potter wand upgrades changed keys to include `_upgrade` and not  conflict with survival wands (e.g staff_upgrade)

# 8.2.3

# 8.2.2

 - Fix a bug when using a broomstick in an offhand and landing while your wand inventory is open
 - Fix temporary blocks becoming permanent after using Peek on them

# 8.2.1

 - Revert default behavior of CheckBlock action targeting, 8.2 may have broken some spells that use it. Sorry about that!
 - Fix potter, stars and bending examples also containing all survival spells
 - Add off-by-default target_pet parameter to spells, will ignore pets from SimplePets
 - Fix crafting the magic sword in legacy MC versions
 - Fix temporary items being able to be picked up on older MC versions (persistent metadata API was broken)

# 8.2

 - Happy Holidays!
   Build a skating rink out of ice and then use `/mnpc add skater` for some festive fun!

 - Many, many optimizations - memory leaks plugged, synchronous chunk loads eliminated (on Paper),
   hard entity references removed

##  MagicWorlds merged

 - Magic can now customize world generation and mob spawning without needing MagicWorlds
 - Configs go into the "worlds" folder, and follow the same format as MagicWorlds
 - There is editor support
 - Fix issues with NPCs and Automata getting lost if in unloaded worlds
 - Added new example configs: `spawnmobs`, `chests`, `netherside` and `otherside`
 - Spawn rules can now use weighted probability maps
 - Spawn rules can have a priority, don't need to rely on key order
 - Replace rules can specify special directives: `stop` to stop further processing,
   `remove` to just remove a mob without replacing
 - Fix several issues with mob spawning in MagicWorlds

## Crafting improvements

  - Allow using "vanilla: true" to easily override a vanilla recipe (just set the output to a vanilla item type)
    - Does not remove the default recipe from players' knowledge book (not sure if that's possible)
    - Does not work with vanilla recipes that have multiple possible ingredients (e.g. different wood variants)
  - Add "locked" option for recipes, to prevent crafting by default
  - Add "craftable" list to classes, specifying locked recipes that class may craft
  - The "disable_default" option for recipes now works again, letting you replace vanilla crafting recipes

## Fixes

 - Fix support for wildcard brushes (use with SuperReplace to replace stairs, fences, etc in-place)
 - Fix spell inventory issues when using the ajParkour plugin
 - Fix strange behavior when using a broom while already on a broom
 - Fix `/getrp url` to return the correct URL when a custom RP has been selected
 - Fix ChangeContext not being able to set the source entity to nothing (affects Mob, Monster, etc spells)
 - Fix auto_close on shops not working
 - Fix NPCs sometimes not respawning
 - The Decoy spell doesn't use vanish

## Additions and Improvements

 - Add alt-cast (sneak) to levelled up fling to get the original speed
 - Add Alter level 2, with alt-cast (sneak) to absorb a wildcard brush
 - Add replacement options to ChangeBiome, Brush actions and ConstructSpell
 - Add `mconfig language` command to easily ues a builtin localization file
 - Add FR (French) translation files (thank you, Brushette!)
 - Add in-game notifications to ops about magic errors and warnings (can be controlled via configs or permissions)
 - Cloak now hides you from mobs
 - Allow specifying item_attributes as sections, so you can set a slot per-attribute
 - Use `/mgive book:fling` to give a descriptive book about one spell
 - Add `/mgive learnbook:spell` to give a book that describes a spell and can be used to learn it
 - Add active_spells_exclusive and active_spells_restricted spell properties, to make spells only castable if some
   other spell(s) is or is not already active
 - Added WildStacker integration to avoid stacking Magic Mobs (or any custom-spawned mob).
 - Broomsticks will now put a temporary "broomstick handle" item in the player's inventory so the slot
   doesn't get taken up by new items and the broomstick has nowhere to go when finished
 - Improved Recall-to-bed behavior
 - Improved support for persistent mobs (in 1.14 and up)
 - Add "land" trigger and "fall_distance" attribute
 - Allow ChangeContext sourceLocation and targetLocation parameters to specify feet, head, etc entity locations
 - Phantoms can be shrunk and grown (wither skeletons can also be shrunk again, optionally)
 - Wand commands when used from the console can now bypass locked wands (same as a player with `Magic.wand.override_locked` permission)
   Use `console_bypass_locked_wands: false` to disable this
 - Triggers can now be a simple list or a one-liner
 - Jedi and Sith will auto-learn recipes, nether stars removed (still needed for hilt)
 - Add simpler `slot` parameter to Wear action, boots,helmet,chesplate or leggings
 - Add `direction_count` and `source_location` parameters to CheckBlock action, more control over where it looks
 - Add "rain" option to Weather action

# 8.1.1

 - Fix compatibility with older versions of LibsDisguises
 - Fix some issues absorbing spell items, now requires you to be holding your wand
 - Fix cure not working on the caster
 - Fix releasing player data locks on shutdown, causing warnings on next login
 - Fix lightsaber appearances in legacy MC versions
 - Allow crafting a magic sword with a damaged netherite sword
 - Fix /mnpc player command
 - Fix OP damage of Magic Bow regular arrows

# 8.1

## Migration Notes

 - There was an off-by-one error in the Skip action. I'm hoping this won't have a big impact, but if this has
   messed up the timing of your spells I apologize! You will need to increase the `skip` parameter by one, the
   action was skipping once more than it should have been.
 - PlayEffects and PlaySound no longer give cast credit. Use `effects_count_as_cast: true` to change this back
 - Added "active_wand" target to ModifyProperties, won't switch wands by default
 - The following wand properties were removed, and had been broken for a while- use `potion_effects` instead:
   health_regeneration, hunger_regeneration, haste

## Major Changes

 - Reworked broomstick:
   - Crafting now requires a phantom membrane instead of an elytra
   - Brooms start out slightly faster
   - The broom skill now only increases duration and handling
   - Add crafting recipes for two different upgrades, players must choose which one:
     - Broom Booster, increases speed to previously levelled amounts
     - Broom Glider, allows for infinite flight
 - Reworked magic armor:
   - Retired apprentice gear
   - Apprentice gear crafting recipes no longer enabled by robes example
   - Items and recipes will stay in case you want them or players have them already
   - Wizard gear recipes changed to apprentice gear recipes
   - Wizard gear no longer indestructible
   - Wizard gear can be enchanted, does not come with enchantments
   - Wizard boots cost 2x magic hearts, grant speed boost
 - Reworking wand buffs
    - Wand buffshop removed for now.
    - Will be replaced by something else in the future.
    - Players that have purchased wand protection will get to keep it for now, but my plan is to remove it in the
      future and reimburse them in some way.
    - If you want, you can set up an NPE using the `wanddebuffs` spell if you'd like players to be able to refund
      the protection they bought. I am concerned about the possibility of abuse if players have been able to
      duplicate wands, though.
 - Builtin Magic Mobs Nerfed (mostly just reduced health)

## Other Changes of Note

 - Re-worked Gather, added to beginner path as an optional spell
 - Fling is no longer allowed while gliding (OP)
 - Add "enchantable" wand property, allowing it to be vanilla enchanted
 - Add instructions to builtin examples, will show how to use them when added
 - Add alternate resource packs, available with the `/getrp` command
 - Add buysellshop template for a combo buy/sell shop
 - Allow potion_effects lists on classes and modifiers
 - Added `/mnpc command` command to easily setup command NPCs
 - Added Flare Level 3
 - Modifiers can now be put on wand (see vader_lightsaber from stars example)
 - Triggers can be put directly on classes and modifiers, work like mob configs
 - Rankups will now be broadcast to the server by default.
   Use `/mconfig configure path default upgrade_broadcast ""` to disable this.

## Miscellaneous Changes

 - Add "jump" trigger (for players only)
 - Add ignored_by_mobs wand/class/modifier property
 - Add use_target_mage parameter to ChangeContext action
   Useful for actions such as Selector and CheckRequirement that only use the mage context and not the target
 - Improved spellbook inventory behavior
 - Add "Magic.allowed_spell_categories" permission list, of spell categories a player may cast
 - The Blind spell now makes mobs confused and lose track of their targets. Added upgrade levels to Blind.
 - Poison now damages a little bit even if you're immune to poison
 - PlayEffects and PlaySound no longer give cast credit. Use `effects_count_as_cast: true` to change this back
 - Add allowed_entity_classes and denied_entity_classes parameters to CheckEntity action
 - Add delay and warmup parameters and "wait", "teleporting" and "menu" effects to Recall
 - Added "active_wand" target to ModifyProperties, won't switch wands by default
 - Add rainbow/technicolor armor set (not craftable)
 - Add "not_permission" requirement
 - Add earns_cooldown_scale parameter, use in missile to make reducing or changing the overall sp cooldown easier
 - Add /mage forget command, to make a player forget crafting recipes
 - When selecting an effect color on the spellbook, it will apply to skill items when cast (it goes to the mage class)
 - The `/mwarp import` command will now import name, description and icon from your Recall configuration
   After using this command, you can remove the `warps` section from `recall` because they will be added automatically.
 - Added the `/mwarp map` command, to add all of your Recall warps to Dynmap
 - Fix using SkillAPI and Heroes attributes
 - Add DeadSouls integration in Recall, also avoids items getting lost when picking up a soul with the wand inventory open
   https://www.spigotmc.org/resources/dead-souls.69409/
 - Editor improvements, including in-game warnings and errors

# 8.0.3

 - Fix NPE in legacy minecraft versions using ModifyBlock
 - Another tweak to death handling to work better with keep-inv plugins (Dead Souls this time)
 - Fix torches next to ore getting eaten by Mine spell
 - Fix crafting issues on Bedrock

# 8.0.2

 - Fix wands in offhand slot being lost on death
 - Fix icons for magic bow arrow spells and longbow appearance
 - Fix magic sword "valk" appearance
 - Fix XP exploit putting magic armor in a grindstone
 - Fix armor wands not activating when right-clicked to wear
 - Fix `/getrp off` command
 - Fix `/wand list` command

# 8.0.1

 - Fix Secure spell
   - Previously secured containers should be unlockable if you summon a new key
   - Aim up and cast Secure
 - Fix "language" config option
 - Fix loading inherited examples directly (e.g. `/mconfig example set 7`)
 - Fix Cloak instantly turning off at beginner rank
 - Fix arrows from Arrow spell being able to be picked up

# 8.0

 - See MIGRATION.md for important information!

 Note that some of the changes below may only apply to modern (1.16+) versions of Spigot.

 ## Important Notes

 - Minecraft 1.16 will now use a new resource pack:
   - If you want or need to turn this off, you can still update. Just put `example: 7` in your config.yml
   - All icons and models now use CustomModelData instead of damage predicates
   - Spells and magical items are represented by recognizable vanilla items
   - Wands should now be much easier to use without a resource pack
   - Combining Magic's RP with other RPs may now be somewhat more complicated,
     so we have created a tool to do the work for you at https://merge.elmakers.com/
   - Spells and wands now use a mix of vanilla and custom sounds
 - Most magic crafting recipes have changed
   - Now requires an intermediate item, the "Heart of Magic" - crafted with 4 emeralds and a diamond
 - General Rebalancing
   - Wizard gear buffed a bit
   - Mana Talisman given some protection buffs
   - Mana progression changed, slowed down and lowered cap
   - Cooldowns tweaked on some spells

 - Removed `add_uses` path parameter. You can use `uses` to add uses to an object via a path
   (I really doubt anyone was using this, please let me know if I am wrong)
 - Resource pack sending is now off by default. Players will be prompted on join about how to configure the resource pack.
 - All players' resource pack preferences will be reset and stick to the defaults until they use the /getrp command
 - Light spell added to architect path
 - Magic torch crafting recipe enabled by default
 - Magic torch can be thrown to provide a light source

 ## Improvements

 - Web editor (beta)
   - Use `/mconfig editor spell blink` to open an editor session for a specific spell
   - Currently only spells are well supported
 - Added `/mconfig example` command to easily add/remove example configs
   - Can use `/mconfig fetch` to load remote configs such as BetterPotter
 - Added `/mconfig reset` command to reset customized configs to the defaults (deletes your custom file, keeps a backup)
 - The `/mconfig clean` command has been disabled (hopefully temporarily)

 - Brush improvements (engineering configs)
   - The Absorb spell now copies block data
   - Brush inventory reorganized, wills how block data variants
   - Better tab-completion for /wand(or mage) remove brush <some long brush key>

 - Add NPC template system via mob configs
   - Potter configs now have `ollivander` and `year1` through `year7` templates
   - Can be spawned simply with `/mnpc add ollivander` and it's ready to go
   - Added spellshop and healer NPC templates to survival configs

 - Crafting recipes can be set to `auto_discover: true`
   - They can also contain a list of other recipes that the player will discover once they craft a certain recipe
   - Survival recipes are now automatically discovered as appropriate

 - Classes and modifiers can now use `overrides` to modify spell behavior

 ## Fixes

 - Fix using the replicate and clone brushes with the Place spell
 - Fix pasting schematics with torches or other wall attachables
 - Fix hotloading recipe changes, and optimize recipe loading
 - Fix warps getting corrupted if the target world is not loaded at server start
 - Fix secure/lockpick spells when non-opped
 - Fix having multiple crafting recipes with similar ingredients
 - Fix using `thickness` parameter with Volume action
 - Disabled spell inventory quick-casting (using the drop button) by default
   You can turn this back on with `allow_inventory_casting: true` but it is not recommended on modern MC versions,
   since you'll also get a cast of the main spell.
 - Fix blinking on top of the top half of slabs
 - Fix wands not auto-opening on login if they were open on logout

 ## Other Changes

 - SP items are stackable, as are custom skulls
 - SuperConvert will now ignore block data (replace all leaves, water, etc without hassle) shift+cast to match data.
 - Broomsticks are bound again, mainly so they can show instructions on first hold
 - Add "reactivate" spell property, for spells that should reactivate on login.
   Used for Levitate and broomsticks so you don't fall and die and relog
 - Wand instructions updated to be more flexible, this may require some localization updates if you're customized
 - Add "spawn" actions on SpawnEntity action, to run some actions after spawning the entity ("actions" runs on entity death)
 - Decoy improved, added to apprentice path
 - Add ignore_damage option for crafting recipes
 - Add auto_block_state for ModifyBlock, used by Place spell, attempts to place a block as if a player placed it
 - Add `/mwarp configure` command, used for Dynmap and Recall integration
   - `/mwarp configure marker_icon portal` To add a warp to dynmap with the specified (built in to dynmap icon)
   - `/mwarp configure icon diamond` to add a warp to the recall spell
 - Add `/mwarp describe` command
 - The `/mwarp go` and `/send` commands will try to load the target world if it is not loaded
 - Add a third alt-cast spell to wands, for a total of 4 possible fixed spells
 - Add SuperWalls spells to default configs
 - Allow numbers (0 - 1) in reset_mana_on_activate wand property, to reset to a fixed number
 - Improvements to LightAPI integration (magictorch)
 - Add "cast_interval_cost_free" parameter to wands, interval ("aura") casts are no longer cost-free by default
 - Add vault.enabled config property to turn off Vault integration
 - Add appearance swapping for magic sword
 - Add `show_name` parameter for mobs
 - Improved combining various location transforms within a single ChangeContext action
 - Add cast_console_feedback config option, /cast and /castp from console now will not print feedback
   by default. This is to reduce log spam when using automated console casting from other plugins.
 - Add support for phantom size
 - Add support for using JSON to spawn mobs, e.g. `/mmob spawn slime{size:5}`
 - Add `/mage promote` command for easily moving a player up a path
 - Add mega_phantom mob to survival
 - Add "random" option for villager_profession
 - Add "slimefun" example, which will add RP items from this RP:
   https://www.planetminecraft.com/texture-pack/slimefun-texture-by-raulh22/
 - Add "safe" example, which modifies the survival configs to avoid all world changes
 - Add "search" effects to BlockSearch action, played for each step of the search
 - The Absorb spell can now absorb most block types (changed from whitelist to blacklist)
 - Added `page_free_space` wand property, if you want your wands to keep more space free when adding new pages
 - Added CheckModifiers action
 - Added "toggleair" modifier to architect path, toggles air replacement on/off for several architect spells
 - Cars fall down go boom

# 7.11.1

 - Fix /castp command when used on mobs (MythicMobs psuedo-integration)

# 7.11

 - Fix resource_pack_default_auto config option (won't change anything except for new players though)
 - Fix potter wands not activating right away after opening the box
 - Give the divinghelmet item night vision
 - Fix broomstick dismount behavior when the duration expires
 - Fix sharpness buff being allowed on a wand
 - Fix ModifyEntity changing mob types to zombies unintentionally
 - Fix getting recipe books with a single recipe, add tab-completion
 - A "deactivate" action handler can be added to perform actions when a spell is toggled off
 - Add Levitate level 6, conduit-only, works like Superfly
 - Add glow option to appearance menu
 - Prevent NPCs from burning

# 7.10.7

 - Wands are once again stashable by default. I'll quit waffling on this now!
 - Fix the ModifyEntity, ShrinkEntity and GrowEntity actions destroying magic mob data (giant Warlocks!)
 - Fix an issue with duplicate entity attributes on classes and modifiers
 - Fix Thestral bone in potter configs
 - Fix NPC interaction when using villagers that have an assigned profession
 - Fix shop behavior when clicking an empty slot, auto_close shops will close, otherwise it will not
 - Fix right-clicking stacked SP items not giving all of the SP
 - Some improvements to NPCs invulnerability
 - Drop action will spawn a falling block in the center of a block
 - Selector and Shops now set put_in_hand to true by default to avoid weird stacking behavior
 - Add apply_name_to_icon parameter to Selector, set to false to use vanilla item names
 - Add Magic.cast_via_command permission. This is a list, just like Magic.cast, but only applies to using the /cast
   command. Players will still need Magic.cast permission for the spell as well as Magic.commands.cast permission for
   the command.
 - Add cast_spell requirement, can be used for making spell combos (see combo example spell)
 - Update painterly example to use modern RP. To use the old one:
   resource_pack: "https://s3-us-west-2.amazonaws.com/elmakers/Magic/Magic-painterly-RP-8-1.zip"

# 7.10.6

 - Fix loading on old versions of Spigot
 - Fix errors using `/mgive someitem someamount` without a player target
 - Some improvements to Wolf House
 - Fix issues with SQLLite on older Spigot versions
 - Fix "Entity is already tracked!" errors
 - Improved undo of broken paintings

# 7.10.5

 - Wands are once again unstashable by default. Set `unstashable: false` on base_bound to turn this back off
 - Fix EntityProjectile mob parameters getting ignored
 - Fix issues with attributes on armor and wands not applying when worn/held
 - Fix error in Commit spell
 - Add support for .schem schematics
 - Fix cage, wolfhouse spells (updated all builtin schematics)
 - Fix pasting schematics on older Spigot versions
 - Fix Wolf House spell/item
 - Fix off-by-one error selecting NPCs by index
 - Add ModifyEntity action (see snowify example in elworld configs)
 - Change "passive" wand/armor property to "worn" ("passive" will still work though)
 - The wands in the "ranks" example are now static wands, they are not on a path nor are they bound.
 - Allow using a mob type/key in a "mount" config in another mob
 - Extend the getrp command to let players turn automatic resource packs on and off
 - Fix error in /spells
 - Add netherrite armor and tools to wearable and melee material lists
 - Armor can now be made to be worn in a slot other than the vanilla slot it's supposed to go in (see: hardhat)
 - Undroppable wands can not be used as crafting ingredients
 - Command variables changed such that @tx, etc prints integer and $tx prints with decimals
 - Add "orient" parameter to Atom effect
 - Support color codes in NPC names and dialog
 - Support selectors in wandp, mgive and castp commands
 - Add hide_messages, optional list on a spell to hide specific messages
 - Add builtin Russian (RU) localization, thanks to Phantom

# 7.10.4

 - Fix exploit/dupe bugs with keep-inventory plugins and wands
 - Fix how using `example: engineering` or `example: automata` removes survival configs
 - Fix copper_chunk item log-spam on older Spigot versions
 - Fix paste spell and building with schematics stalling the server
 - Allow mana_max_boost and mana_regeneration boost on classes and modifiers
 - The deprecated xp_max_boost and xp_regeneration_boost properties no longer work
 - Add "angry" option for wolf mobs
 - Default mob interval to 1 second if not specified and using interval triggers
 - Add Equation action .. not sure what you'd use this for, but there is a spiral example in elworld/spells
 - Added Sound and Particle effectlib effects, and "probability" parameter
 - The Tame action/spell can now be used to make foxes trust you
 - Add "fox_type" mob parameter
 - Add EquationVolume action, see surface example
 - The Commit spell will now clear all of a player's undo queues, preventing cross-server undo from staying forever

# 7.10.3

 - Support for 1.16.3
 - Fix tag spell
 - Add FakeBlock action, use for peek spell
 - Add simpler format for triggered spells on mobs
 - Paginate `/spells` and `/wand list` output
 - Anvils will no longer bind wands by default, set `enable_anvil_binding: true` to turn this back on
 - Add prevent_melee and prevent_projectiles options to mobs
 - Add new WG custom flags: always-allowed(-spells,-spell-categories,-wands) - to override blocked flags

# 7.10.2

 - Fix RedProtect failed integration log-spam
 - Fix Recall failing and log-spam
 - Fix randomtp spell and other spells using random vectors
 - Fix setting NPCs to configured magic mob types
 - Fix a certain spell dupe exploit that shall go unnamed
 - Fix SpawnEntity action not using parameters
 - Fix NPCs not respawning when an error happens, and make them silent by default
 - Fix items getting lost when wearing wearable wands
 - Improved support for JSON tags in configs, including skulls from sites like minecraft-heads.com
 - Some improvements to automata spawners, including range-based leashing
 - General improvements to admin commands for NPC and Automata management
 - Player disguises will be applied twice as a hacky work-around to them not working sometimes
 - Add several Bukkit events to mage debug information

# 7.10.1

 - Fix saving lots of player data backup files
 - Add RedProtect integration, for build/break/pvp/mob targeting permissions, and Recall warps
 - Add support for Residence regions in Recall
 - Add auto_wands config option, for automatically converting items of a specific type into wands
 - Improved parsing and error messages for vectors in effect configs
 - Add "deactivate_self" parameter to Deactivate action, off by default.
   This reverses a change made in 7.9.1 where Deactivate would cancel its own spell.
 - Add "transformable" mob parameter, can be used to prevent vanilla transformations (e.g. zombie->drowned)
 - Add "robes" example, add this in to get the crafting recipes and textures back for apprentice/wizard armor
 - Upgrade items, spells and brushes are now added to a wand by dropping them onto a wand
 - Add "wearable" property for wands, so they can be worn like a normal armor piece

# 7.10

 - Fix compatibility with older versions of Spigot (whoops, sorry!)
 - Add "/mage bypass" command, a simple replacement for using "/op" to get magic bypass permissions
 - Fix Cleric chestplate preventing Recall
 - Fixes and improvements to "mmob clear" and "mmob list" commands
 - Allow protected and powered properties on classes
 - Add max_item_count and stack_size parameters to GiveItem action,
   for filling a player's item up to a certain amount, and allowing illegal stack sizes
 - Expose some Wand methods to manipulate wand spell/brush inventories

# 7.9.13

 - Magic NPCs
   - Manage via new /mnpc command
   - Lightweight replacement for simple use-cases of Citizens
   - Can import from Citizens
   - Can run spells and commands, talk to nearby players
   - Can do anything Magic Mobs can do
   - Add dialog scripts in-game using written books
   - Will stay put!
 - General improvements to Recall:
   - Allow using an "options" list much like Selector
   - Maintain order of "warps" and "commands" map
   - Allow for multiple markers
   - Add Recall level 4 and 5 to survival configs, 2 and 3 markers respectively.
   - Waypoints waiting to be unlocked will now show in the menu (configurable)
 - Fix mob triggers not repeating
 - Add proper Residence integration
 - Fix Skript pre-cast ('on casting of') event
 - Fix trinketshop skull icons in 1.16
 - Crafting recipes for wizard and apprentice armor are now disabled by default
 - Bypass permissions are no longer applied to ops by default.
   I changed this to reduce confusion about when protection is and is not working.
   If you prefer to bypass, give yourself the "Magic.bypass" permission.
 - Allow using titles and other escape prefixes in resoure_pack.sending message
 - Set persist:false on broomsticks and entity projectiles, hoping they won't stick around
 - Add "split" flag for slime mobs, to prevent them from splitting. Add "cube" mob to survival.
 - Add "/mwarp list" command
 - Improvements to making sure data files don't get wiped
 - Use chunk ticket API for performance improvements in large construction and undo tasks in unloaded areas
 - Automaton will no longer force-load chunks
 - Some fixes to undoing spells on world save, to prevent leftover spell changes
 - Fix firework action (e.g. stun spell) bypassing pvp restriction
 - Fix projectile target_self_timeout always turning on self-targeting
 - Fix arenas example "battle" sword, it was supposed to have abilities
 - Add target_potion_effects and ignore_potion_effects parameters,
   for spells that only target entities with specific potion effects applied
 - Allow wand overrides to work with sections, such as "requirement.permission somepermission"
 - Add "requirement" option to CheckRequirements, for using a single requirement instead of a list
 - Add permission_teams option for permission-based friendly fire grouping
 - Fix an exploit that could be used to place water in the nether
 - Fix exploit duplicating treasure loot, though containers will stay on the indestructible list for now
 - Add "casted" event for Skript, StartCastEvent for API

# 7.9.12

 - 1.16.2 Compatibility fixes
 - Automata hearts ("automata" example) may no longer be used to respawn automata
 - Add EarnEvent to API and Skript, for tracking SP earn
 - Improve MySQL connection robustness
 - Add support for material lists as costs
 - Try to prevent players glitching through blocks via brooms
 - Add show_no_permission parameter to SpellShop action, for allowing purchasing of spells even if a
   player does not have permission to cast that spell.
 - Removed wizard clothes texture from RP
 - Fix Cloaked players losing their armor if they die while cloaked.

# 7.9.11

 - Some changes and improvements to unbinding wands
 - Add "color" parameter for thrown potions in Projectile action
 - Add custom_name to disguise parameters
 - Villagers and wandering traders can't be Disarm'd
 - Fix damage-type-specific protection/weakness/strength
 - Consumable items won't be consumed if their spell cast fails
 - Fix attributes not updating max health, etc until reload
 - Fix bending water whip spell
 - Fix Blink ascend (level 3)
 - Update PAPI integration to new API
 - Add weather requirement (storm/thunder/clear)
 - Add drop_skull parameter to ShrinkEntity action, so it can be used to shrink but not drop skulls
 - Add SpellInventoryEvent (BetonQuest compatibility)
 - Add DropSkull action, works like ShrinkEntity + ShrinkBlock, but without killing the entity or breaking the block.
 - Fix spell dupe exploit if players get ahold of a spell item

# 7.9.10

 - Hotfix for players not getting items from shops

# 7.9.9

 - Add dismount flag for Mount action
 - Support infinite, infinity and forever as Repeat parameters
 - Put in some fixes for wall-clipping Rocket spell exploits
 - Fix error when using a graduation NPC in potter configs without holding a wand
 - Add StopSound action
 - Add "phase" parameter for ender dragon mobs
 - All "spells: *" in ApplyCooldown action to clear all cooldowns
 - Add "current" parameter to Cancel action, to try to avoid canceling the current cast
 - Fix error loading emerald sword on startup in 1.15 and earlier
 - Add "allow_dropped_items" Selector parameter, will cancel a purchase if your inventory is full
 - Fix for mob triggers not properly re-triggering
 - Fix incorrect data type for HideFlags, which could cause issues with protocol plugins (e.g. Protocolize)
 - Add "mount" property to mobs, which can be another mob to use as a mount
 - Add SetSpawn action
 - Allow wands to be used as crafting ingredients
 - Fix issues when dynmap is not fully working

# 7.9.8

 - 1.16.1 support
 - The emerald sword is no longer unbreakable (in 1.16 and up only)
 - All builtin mobs using zombie pigmen changed to husks (RIP)
 - Add support for class-locked armor
 - Add "interact" mob trigger for more advanced right-click functionality
 - Prevent putting wands into a grindstone
 - Fix wands disappearing under certain circumstances when dropped.
   However, they will drop now under those same circumstances. It's the best I could do!
 - Add "undoable" material list, for tweaking the undo system (e.g. excluding fire from undo)
 - Put "collapse" spell back in survival configs (whoops, sorry!)
 - Add support for dyed leather horse armor

# 7.9.7

 - Moved engineering magic into a separate example
 - Add left_click_sneak, right_click_sneak, drop_sneak and swap_sneak wand actions
 - The "examples" list in config.yml can now be put on a single line
 - Fix log spam when swinging a wand that has no active spell
 - Fix mana not saving properly on droppable wands
 - Fix Colloportus in 1.14+
 - Add require_mana_percentage parameter to CheckMana action
 - Add item_count parameter to GiveItem action so it can be used with variables
 - Fix dupe bug/exploit with the swap key
 - Fix simple potion_effect maps

# 7.9.6

 - EffectLib effects can now take "forever" as a duration and "random" as a color
 - The Cast action supports "console: true" to cast as the console (take care with this)
 - Add "persist" and "invulnerable" flags to mob configs, for proto-npcs
 - Add "tags" parameter to ApplyCooldown action, for applying cooldowns to all spells with a one or more specific tags
 - Fix glitches with broomsticks leveling up
 - Include offhand and armor slots when checking/removing items from a player's inventory
 - Moved automata into a separate example.

# 7.9.5

 - Fix gamemode targeting that was broken in 7.9.4
 - Action parameters no longer inherit from parent actions to child actions. See MIGRATION.md for more info.

# 7.9.4

 - Fix some items showing as "not trained" by mistake
 - Added ClearInventory action
 - Add "mwarp send" command, for using the builtin warp system from the console, NPCs or command blocks
 - Spells will only target survival and adventure mode players. This can be controlled with the target_game_modes parameter.

# 7.9.3
 - PLEASE see important notes in MIGRATION.md *before* upgrading! Mostly if you have custom wands with "attributes" set.
 - Modifier system added- for custom status effects
   - Effects act very similar to temporary mage classes
   - Can apply vanilla/magic attributes, mana modifiers, cost/cooldown adjustments and more
   - Can have a duration or be manually removed
   - Use the Modifier action to add/remove, similar syntax to PotionEffect
   - Modifiers must be pre-defined in modifiers.yml
   - There is a new "mage modifier" command for adding and removing modifiers
   - Can have spells with added/removed triggers
 - Added triggers: sneak, stop_sneak, sprint, stop_sprint, glide, stop_glide, left_click, right_click  
 - Add lock/unlock triggers for classes.
 - Add "magic arrows", a way to attach spells to arrows that can be fired from a normal bow
 - Add "bowpower" attribute, for arrow spells to react to enchanted bows
 - Add FX to Aqualung and Critical passives
 - Add upgrade levels to Critical, fix FX showing every time
 - Spell shop and advancement can now work without a wand. Changed messaging a little.
 - Spell upgrades now work without a wand
 - Better potion effect undo
 - Add Interval action, like a combo of Repeat + Delay
 - Fix overlapping casts overwriting each others' variables
 - Fix blocked-spells and allowed-spells flags (broken in 7.9.2)
 - The SpawnEntity action now forces spawned mobs to ignore their owner by default. Set "owned: false" to prevent this.
 - Variables can now be pre-defined in a spell, in case you want to use them in base spell parameters
 - Add Warp action, for simple warping (Recall can be complicated for this purpose)

# 7.9.2

 - Passive spell system!
   - Add "triggers" option to a spell, it can be auto-cast under various circumstances
   - Add "passive" flag for spells that can not be directly cast but still show up in the spell inventory
   - Add CheckTrigger action to make a spell see if a trigger has been activated since the spell started
   - See here for examples: https://github.com/elBukkit/MagicPlugin/tree/master/Magic/src/examples/elworld/spells/passives
   - Survival passives added: Aqualung, Critical
 - Add RemoveSpell action
 - Add CastPermissionManager hook to API
 - Fix crafting permissions in the 2x2 inventory crafting slots
 - Add Essentials integration for vanish detection
 - Andesite, diorite and granite are now in the same destructible lists as stone
 - Added "damage" and "damage_dealt" attributes
 - Add "scope" to ModifyVariable, can be cast (the default), spell or mage
 - Attributes can now be used in Message and Command actions
 - Add "direction", "use_target" and "set_target" parameters to CheckBlock action, for easily checking the block under your feet
 - Fix some consistency issues with the "target_self" parameter
 - Add "target_mount" parameter (false by default) so spells won't kill your horse
 - Fix Dragonball not earning SP on hit
 - Add more detailed support for potion effect lists in PotionEffect action
 - Add "invert" flag to all CheckXXX actions
 - Add magma wand appearance (thanks, Toffy!)
 - Allow using "examples" instead of "add_examples" in config.yml
 - Add "air: max" option to AirSupply action
 - Additional customization options for time and range displays in spell lore
 - Added ResetMage action, "reset" spell for player reset GUI

# 7.9.1

 - RecallSpell was removed, use RecallAction instead.
   - Take note of all the deprecated spells you're using (basically any "class: something" in your main spell properties)
   - These are all old, before the action system, and have been deprecated for years
   - They will show in logs in startup
   - They WILL be removed in future versions!
   - The exception being engineering magic. Look for "is using a deprecated spell class" in your startup logs.
 - Add support for %placeholders% in spell, wand and class parameters
 - Add ModifyVariable action, adding concept of user-defined variables that can be used in spell parameters
   - Use $variable in a spell parameter, command, or message
   - You can also use @variable in commands for an integer version (rounding down)
 - Add "variables" requirement
 - Fix potter wand boxes not properly turning into a wand (icon wasn't working)
 - Potter wand boxes now open on right-click
 - Fix missing IMPORTANT headers in defaults files
 - Add TakeCosts action
 - Add "use_mode" wand property, which can be one of: success, precast, always. This replaces "preuse".
 - Fix spells being unable to cancel themselves (e.g. cancel_on_death)
 - Fix swapping a wand or gun into the offhand restoring its ammo or mana
 - Add upgrade levels to Smite
 - Add "height" requirement
 - Add several builtin attributes: health, health_max, time, moon, location_x, location_y, location_z, level, xp, mana, mana_max
 - Add source_use_movement_direction and target_use_movement_direction options to ChangeContext action
 - Add use_wand and wand_slot parameters to ArmorStandProjectile action
 - Improvements to TakeItem action, can now look for a specific item and also return it on undo
 - Improvements to Damage action scaling by distance

# 7.9

 - Add in-game customization commands (/mconfig enable|disable|configure)
   - See: https://github.com/elBukkit/MagicPlugin/wiki/In-Game-Customization
 - Add support for storing player data in MySQL, Postgres or SQLite
   - See: https://github.com/elBukkit/MagicPlugin/wiki/Player-Data-Storage
 - Fix broken undo for some blocks with extended data
 - Fix broom offset problem in MC version 1.12 and below
 - Fix sword-based wands having broken icons in 1.12 and below
 - Add BattleArena integration, friendly fire flags will respect teams
 - Add magic_sp, magic_spell_count, magic_mana and magic_mana_max Placeholders
 - More graceful handling of invalid wand icons
 - Add "/mage skin" command, which dumps a block of text you can put in a "skin" property of a player disguise
 - Add "ironman" mob to survival configs (as an example of using a stored skin)

# 7.8.4

 - Use SkillAPI allies for friendly fire checks
 - Fix "requires" items in Citizens traits getting removed on restart
 - Add "language" config option, to easily change the language of in-game text.
   Thanks to the generous help of translators, the following built-in languages are available:
   CN, ES, ES2, ES3, KO, ZH
 - Fix an issue where the vanilla /give command could duplicate items when a wand inventory is open
 - Add /mmob egg command, for getting a spawn egg of a mob or magic mob

# 7.8.3

 - Fix some issues using CustomModelData icons in wands and spells
 - All containers are now indestructible to magic (an effort to prevent dupe exploits and bad interactions with other plugins)
 - Fix Mine spell by adding a "tool" parameter to the Drop action, which defaults to diamond pickaxe
 - Add Mine level 5 and 6, will drop bonus ores, upgrades now available without engineer rank

# 7.8.2

 - Fix Recall, will now place you on top of your bed instead of failing.
 - Fix Recall not teleporting to half-blocks
 - Fix using the drop button on a wand also casting a spell in 1.15
 - Fix skill items being useable if the player switches classes to one that does not have that spell

# 7.8.1

 - Fix Lockpick spell
 - Update Towny integration to latest Towny. You may need to update Towny if you use it.
 - Add invert_distance parameter to Damage action, to allow spells to do less damage up close.
 - Add refund_on_no_target spell property, to refund costs on miss
 - Fix broomsticks in 1.15
 - Fix broomsticks insta-crashing if you're not looking up when mounting
 - Disallow using broomsticks in creative mode (PLEASE STOP REPORTING THIS, THE PLUGIN DOES NOT SUPPORT CREATIVE MODE!)
 - Fix custom model data not carrying over from an item to a wand icon

# 7.8

 - Update for 1.15 support
 - Add worth_type and earns_type spell options, to make it easier to switch the economy
 - Fix parsing wand overrides with spaces in them
 - Allow commands in Command actions to be overridden by wand or /cast parameters
 - Add support for using custom model data for RP icons, rather than damage
   - Icons can be specified like wooden_hoe{CustomModelData:1}
   - A future version of Magic (v8, probably) will switch to this method rather than using damage
 - Potential fix for a mage data loading issue with a complex class hierarchy

# 7.7.6

 - Fix undoing block data changes, fixes Admission not closing the door.
 - Add CheckPotionEffects action, for seeing if a target has some specific effects
 - Add light and time requirements, can be used in CheckRequirements or Selector for time of day or light level checks
 - Make it easier to change the items used for spell and wand icons (see items/wands.yml)
 - Fix cycle_reverse wand action causing errors
 - Cycle mode wands show spell lore by default (use show_cycle_lore: false in wand properties to turn this off)
 - Wand overrides now apply to consume_spell casts
 - Fix PowerBlock action (EMP/Aparecium) in 1.13+
 - EMP now works on buttons, but not on redstone blocks
 - Fix FlowerAction/Pollinate not properly creating double-high plants
 - Fix Capture not working on newer mobs
 - Fix spells not damaging the ender dragon in 1.13+

# 7.7.5

 - Fix wands limited to path spells not being able to learn extra spells from their path
 - Fix Portal spell in 1.13+
 - Put a max size on the spellshop to prevent it from failing in 1.14.4+
 - Add set_source option to SpawnEntity, see piggyback example spell
 - Fix unstashable wands being stashable in barrels
 - Fix some issues with custom crafting
 - Fix the Music spell (PlayRecord action fix)
 - Add one new wand and two new bow appearances (Thank you, Mutim_Endymion!)
 - The Cloak spell now toggles on and off, and hides armor while active (Thanks, NeoFalcon!)
 - Reduce particle use on a number of spells since Mojang apparently has no desire to fix their horrible performance issues :(
 - The Cylinder effect can now be rotated, used in Superlaser

# 7.7.4

 - Hotfix for error in 7.7.3 loading examples that don't inherit (think this only effects sandbox?)

# 7.7.3

 - Fix the Wear action in 1.14 (Hat spell, etc)
 - Fix the Air Scooter spell in 1.14
 - Fix poison arrows not dealing any poison damage
 - Fix schematic loading in 1.14 (signs still don't work, not sure that's going to be fixable)
 - Fix cat spell in 1.14 (changed to use SpawnEntity action, may work slightly differently)
 - Fix progression when using legacy configs (example: 6)
 - Fix colored wool, carpet etc in 1.14 using legacy keys (e.g. "carpet:2")

# 7.7.2

 - Fix the Fireball spell
 - Fix the Admission spell
 - Fix setting arrows to allow pickup in 1.14.1
 - Fix the Horse spell going on cooldown even if there wasn't enough room for the horse to spawn
 - Add "reset_mana_on_activate" wand property, can be used to make items you must hold to charge up

# 7.7.1

 - Fix errors in 1.14 when trying to copy villagers (trade API is gone)
 - Fix /wand destroy not clearing keep, undroppable and other flags
 - Fix projectile spells in 1.13 and below
 - Fix using block-material-based particle effects in 1.14

# 7.7

 - Fix NMS bindings for 1.14
 - Add CheckVelocity action
 - Removed Vault-based "fancy" item name feature, since the feature it relied on has been removed from Vault
 - The broom item was changed in the resource pack to not have an offset

# 7.6.20

 - Update Essentials ItemDb to latest EssentialsX
   - You MUST update Essentials if you want Magic item integration to continue to work
   - There was no easy way to keep compatibility with both versions. Sorry!
 - Fix icons on selector options using cast_spell with skull-based icons
 - Boon changed to only work on caster, avoid strange behavior when turned off
 - Fix boon not levelling up
 - The Horse spell requires empty space, to avoid exploits
 - Add Horse spell to Apprentice path
 - Fix the anvil spell
 - Errors in warnings in logs will be colored to stand out. This can be turned off with `colored_logs: false" in config.yml
 - Fix support for map items in 1.13.2

# 7.6.19

 - Fix for some internal CraftBukkit changes somewhere in 1.13.2
 - Block and mob photo improvements (Thanks to Yanis48!)
 - Reduce log spam by default (use log_verbosity in config.yml to get all those logs back, if you want)

# 7.6.18

 - Add NCP exemptions for creative flight as well as survival flight
 - Fix a block dupe exploit when using spells that destroy blocks
 - Fix appearanceshop not being usable with other wands having the transformable tag
 - Add tweakable wand tag for use in controlsmenu
 - Add option to delay logout, for compatibility with plugins that kill players on logout
 - Fix Blink ability to pass through ceilings unintentionally in certain cases
 - Add "/mage deactivate" command, to force-close a player's wand inventory and cancel any active spells

# 7.6.17

 - Some fixes to work with latest WorldGuard/WorldEdit
 - Fix bug from 7.1.16 that makes construction spells take longer to build
 - Tone down particles on shell and blob because apparently Mojang doesn't like fixing things they broke. But hey, pandas, amirite?
 - Fix some issues with strength (damage multipliers)

# 7.6.16

 - Fix some hangs that could happen when using an infinite Repeat action
 - Fix using `repeat: infinite`
 - Add `offhand` parameter for mobs holding offhand items
 - Allow RP links to include redirects. This may fix some issues using Dropbox to host RPs.
 - Glide will register a flight exemption with NCP

# 7.6.15

 - Currency items are now unbreakable so custom item icons can be used
 - Add support for 1.13.2
 - Reduce laggy Blast undo effects

# 7.6.14

 - Add some new config options to help synchronize Bungee servers that are using a linked filesystem to share Magic player data
 - Add "numeric" option to ModifyLore action
 - Fix some spells, like Torch, not working in natural caves in 1.13
 - Fix the appearance shop and other wand customizations

# 7.6.13

 - Add "permission" parameter to Recall options, to allow locking warps/commands with permissions
 - Add "rotation" and "orient" parameters to image effects
 - Fix Chop spell in 1.13
 - Fix engineering spells (maybe only broken in 1.13 ... ?)
 - Add Grapple Arrow (WIP, not available yet- thanks to NeoFalcon)
 - Allow negative values for strength/weakness/protection (if stack is set to true)

# 7.6.12

 - Add ignore_region_overrides spell parameter, for making spells ignore worldguard flags that would allow them
   to break/build when they otherwise wouldn't be able to (yes, a super specific request)
 - Engineering magic will now ignore allowed-spells: *, so no permanent changes can be made in regions that allow
   spells to change blocks. (This is changeable via "override" in spells.yml if you want)
 - Fix the Drop action not being undoable
 - Partial block damage (cracks) now undo in a more consistent way
 - Some fixes to mob counts shown in /mmob list
 - Improved magic bow behavior, now supports arrow spells with delays
 - Bring back hack that switches to chest mode when in creative mode, optional and off by default: wand_creative_chest_switch
 - Fix certain issues switching a wand from chest to inventory mode
 - Add ModifyLore action, for making changes to vanilla item lore

# 7.6.11

 - Fix WorldGuard, factions, various other integrations breaking after /magic load

# 7.6.10

 - Add "as_percentages" parameter to CheckHealth action, to use percentages instead of raw health values.
 - Add "/mgive recipe:wand" (or "recipe:*") item style, for giving recipe knowledge books (1.13 only)

# 7.6.9

 - Some more fixes to spellshop progression
 - Action parameters embedded in the "actions" tree no longer inherit down to child actions
   This is possibly a breaking change, though only if you've set up your actions in kind of a strange way.
 - Fix projectiles not passing through tall grass in 1.13
 - Fix brushmod when using the Brush action (fixes the Projector spell)
 - Fixes to ignore plugins that fail to start up (should fix issues with LibsDisguises in 1.13)

# 7.6.8

 - Fix effect bugs introduced in 7.6.7
 - Add "track" parameter for automata spawners, to avoid despawning spawned mobs

# 7.6.7

 - Fix spell descriptions not showing in the spellshop
 - EMP will only affect levers and redstone blocks
 - Fix spellshop not always auto-levelling after buying the last spell
 - Add wand tagging system for use with requirements, make progressmenu easier to customize
 - Some specific internal FX fixes (please let me know if any effects look weird now)
 - Fix mana/regen boosts applied to classes
 - /mtrait invisible works on armor stand NPCs
 - Add CheckInventory action
 - Add /mage+/wand add brush *, for adding all brushes to a wand (won't add variants pre 1.13)
 - Make pistons indestructible to magic, to avoid issues restoring them (for now?)
 - Fix player's health not restoring on relog if max health is buffed by a class
 - Add "reduce" option to ApplyCooldown action to reduce cooldowns instead of clearing or increasing them
 - Magic mobs configs now support adding drops or other behaviors to vanilla mobs or even players.
 - Add mob option to avoid dropping loot if not killed by a player

# 7.6.6

 - Add spawn tag API, to support MagicWorlds 1.6
 - Alter now works on flower pots (1.13 only)
 - Add some safety checks for class entity attributes getting stuck on server crash
 - Add jump_cast option for brooms and vehicles, to cast a spell with spacebar while riding
 - Add additional automata options, including nearby player, time of day and phase of moon requirements
 - Add werewolf mob and spawner (example of only spawning at night during a full moon)
 - Add CheckRequirements action, can be used for branching spell logic based on attributes or other things

# 7.6.5

 - Fix sniper scope in 1.13
 - Add fall protection to Levitate and Superfly
 - Fix arrow spell throwing exceptions in 1.13
 - Fix Magic/Master swords not doing melee damage
 - Some fixes to schematic loading in 1.13, still not perfect though.
 - Some attempts to fix Wolf House, still not 100% there yet (+ spigot seems to have command block bugs)
 - Mega-engineering throttled down for performance
 - Added Force Choke to stars configs
 - Add LogBlock integration
 - Fix costs of more than 1 item
 - Fix EMP + Admission undo on 1.12 and below
 - Some fixes to SpawnEntity action getting stuck

# 7.6.4

 - Fix torch spell not always working in 1.12
 - Fix undo system not always restoring complex block states in 1.13 (sign/torch/door direction, for instance)
 - Fix headshots
 - Fix a few specific cases of the undo system failing
 - Allow resource packs to work on hosts that don't provide a Last-Modified header (dropboxusercontent)
 - Add missing 1.13 materials to the destructible lists
 - Add configurable support for arbitrary protection plugins, preloaded for Residence
 - Fix lore on shop items, and Philbert egg being free
 - Fix Ocarina in 1.12 and below
 - Fix spawnershop mob spawners in 1.13
 - Fix mobshop mob eggs in 1.13
 - Add support for WorldGuard 7
 - Fix PreciousStones fields not showing up in Recall menu
 - Add /mwarp command, a very simple warp system
   This is basically just here to make Recall work stand-alone. EssentialsX hasn't updated to 1.13 yet and I'm impatient.

# 7.6.3

 - Support/Fixes for 1.13 (Mostly functional but maybe still risky!****)
 - Known issues:
   - Block undo doesn't properly restore certain block states (sign/torch/door direction, for instance)
   - Bed in Tent schematic is broken

# 7.6.2

 - The magic bow held by the archer mob is now inert (not useable)
 - Add Factions support for friendly fire flags in spells
 - Add support for villager professions in a disguised mob
 - Fix cast messages not going to action bar
 - Infer types in "/mitem configure", this allows vanilla items to be tagged as undroppable, or keep on death
 - Fix dupe glitch involving [redacted for safety] chests

# 7.6.1

 - Fix Growth crops not being harvestable
 - Fix internal errors when placing a Wolf House
 - Fix Lockette integration
 - Fix Mirror Shield
 - Fix double-casting when targeting an entity within arm's reach
 - Fix shops with inline item name/lore definitions
 - Some fixes to the ModifyProperties action
 - Add CheckHealth action
 - Allow actions on SpawnEntity which will run when the entity dies (See: kamikaze spell)
 - Allow multiple commands in a command NPC via the ; delimiter
 - Add SpawnEntity disguise_target parameter (See: decoy spell)
 - Add support for colored potion icons (potion:AA00FF)
 - Add mana potions to the RPG configs

# 7.6

 - Fix Heroes integration
 - Use 1.13 resource packs by default, unless running on 1.12 and using default configs.
 - Fix some specific issues Multiply actions interfering with one another
 - Add extraspellshop, for selling spells not included in the normal paths.
 - Add Forest and Mob to Conduit and Wizard paths, respectively
 - Fix scale parameter on sell shops
 - Support attribute equations in wand properties and configs
 - Fix incompatibilities with certain GUI plugins such as Jobs Reloaded
 - Support custom items as spell/wand icons (doesn't really do much except let you define them in items.yml)
 - Fix Capture when used on pigmen
 - Nerfed Poison Arrow a bit
 - Add "visibility" effect parameter, can be all (the default),origin or target.
 - Fix random lag issues when spells are running on world save

# 7.5.4

 - Add entity_attributes support to mage classes (e.g. classes can grant additional health, attack speed, etc)
 - Add health_scale property to mage classes for scaled health display
 - Support attribute equations in mage class properties
 - Add drop_original_block option, on by default, blocks now drop when breaking magically modified blocks
 - Fix wand cost/cooldown reduction not respecting the max settings in config.yml
 - Add support for LockettePro
 - Don't allow spamming Blink up into the air
 - Fix strange behavior of Flash Bang when hitting multiple targets
 - Fix exploits with certain spells (Stash, Workbench) quick-cast
 - Add randomize_yaw and randomize_pitch spawner parameters
 - Fix some issues restoring wands


# 7.5.3

 - Add /mtrait mob <mob> command to attach a magic mob profile to an NPC (Sentinel spellcasting)
 - Really allow spells to target Sentinel NPCs
 - Cure level 5 can cure zombie villagers
 - Tightened up alternate_up/down thresholds so it's harder to accidentally alt-cast
 - Fix server lag when creating player skull items or blocks (update MagicArenas if you're using it!)
 - Fix error on server shutdown if there are players riding brooms

# 7.5.2

 - Fix Magic bow
 - Fix renaming wands on an anvil
 - Fix cost free not applying to magic bows
 - Fix random missed spots inside of large Flood Fill casts
 - Fix wands that are given spell levels via config losing those levels when adding a new spell

# 7.5.1

 - Improve Flatten spell using a new reverse_spiral build type
 - Multi-line message support to all of messages.yml using \n
 - Colorized all path names, mainly for Placeholder API support
 - Fix some odd effects when deactiving Boon
 - Fix magic mobs using Arrow and other projectile spells
 - Improved support for using attributes in base spell parameters (cooldown, range, etc will show correctly in lore)
 - Fixed graduate spell list in potter configs
 - Cars in war configs can now go up 1 block slopes, and don't go invisible when shot
 - Add Growth spell (applies bonemeal in a radius)

# 7.5

 - Please check MIGRATION.md for this release if updating! There were a few potentially breaking changes.

 - Maybe 1.13 support (not tested yet)
 - The survival configs are now contained in an example
   - Defaults are mostly empty but load the survival example by default.
   - The files put on your server in the defaults folder are the merged configs, and so will include survival as before
   - This shouldn't affect most of you, but if you notice something weird about your custom configs please let me know!
 - The map brush can now work with concrete, glazed terracotta and several other block types
 - Add support for parrot variants in mob configs
 - Fix mobs spawned with a disguise looking glitchy for 1 tick
 - Add "cast.spells" property to automata
 - Add "launch" trigger to magic mobs, support using magic bow spells (See: archer mob)
 - Add "display_name" property to wands, for making a custom name (no spell info, etc)
 - Allow for custom mob triggers, add Trigger action to trigger via spells
 - Add support for custom currencies
   - Currencies defined in config.yml, named in messages.yml
   - Can be used in casting costs, Selector/Shop costs/earns, spell earning (in place of SP) and worth
   - Can be given with /mgive, in virtual or item form (right-click item to deposit)
   - Available via the API for plugins to add custom currencies
   - Ledger action is now a dynamic book that supports all known currencies
 - Add use_wand_color option to Firework action
 - Colors can now use simple color names (red, blue, etc) or hex codes
 - Add Raise and Flatten engineering spells
 - Fix setting boolean values via /mage setdata, See: https://github.com/elBukkit/MagicPlugin/wiki/Vanity-Items
 - Add class_items option for classes, for items given when unlocking a class and taken away when locking.
 - Fix specifying a custom item-based currency in config.yml
 - Added Book and Cast actions (Thanks, MineStein!)

# 7.4.4

 - Fix the switch_class option in a Selector menu keeping the menu open
 - Fix Chop and Mine spells, broken in 7.4.3

# 7.4.3

 - Add item pickup sound for items picked up with a wand inventory open
 - Fix some issues with vanilla item attributes not stacking
 - Added /mitem spawn command, for spawning magic items in the world (mythic mob drops, command blocks, etc)
 - Fix a few ways players could wear skull-based spells
 - Fix some strange behavior with riding a broom and chair plugins
 - Limited-use wands will consume uses when their spell misses
 - Fix Vanish action not working if a player relogs
 - Mob configs support inheritance
 - Fix some issues with certain actions getting cancelled and not cleaning up properly

# 7.4.2

 - Fix breaking some plugins' auto-updaters
 - Fix filled wands re-organizing their spell inventory
 - Fix lore display of health and hunger costs
 - Fix boon and levitate filling mana when the spell ends

# 7.4.1

 - Fix Heroes attribute integration
 - Add support for generic_knockback_resistance attribute on items
 - Fix spawned mobs having no AI by default
 - Fix annoying warning from PaperSpigot about crafting recipes
 - Better handling for wearing pumpkins
 - Add a couple of new parameters to Orient and ChangeContext actions

# 7.4

 - Add /mauto command for creating "automatic blocks", or automata
   - yes, I know, I already have a thing called automata...
   - Create mobs spawners:
     `/mauto add spawner`
     `/mauto add spawner creeper`
   - Create persistent effects:
     `/mauto add fountain`
   - More info: https://github.com/elBukkit/MagicPlugin/wiki/AutomaticBlocks
 - Add support for randomized drop tables to magic mobs
 - Add "spawn" trigger to magic mobs
 - Fix auto-targeting on Gatling Gun and Laser
 - Fix sand blocks breaking in doubles

# 7.3.9

 - Wands will now cancel their passive particle effects when deactivated, allowing for long-running effects while held
 - Fix wand effect bubbles having lost their color
 - Improve support for complex combined material lists
 - Levitate now has a height limit, increases with level up
 - LevitateSpell is deprecated, please use the new Fly action instead
 - Fix issues toggling off spells that have levelled up
 - Fix magic mob spawners

# 7.3.8

 - Fix an unintentional behavioral change in undo animations that was introduced in 7.2
 - Add support for falling block projectiles
 - Fix issues using /reload command (still don't use it, though...)
 - Add /mage clear command for clearing player inventories of magic items
 - Add default_mage_class config option
 - Improve and optimize Citizens integration, target Sentinel NPCs by default
 - Add ModifyMaxHealth action
 - Add ModifyMage action
 - Add Chat and ClearChat actions (thanks, MineStein!)
 - Add bypass_cooldown parameter
 - Fix some issues with toggleable spells restoring health when toggled off
 - Fix unstashable wands being stashable until held

# 7.3.7

 - Update MobArena integration to work with latest version, magic items now available as rewards
   - Will need further support from MobArena for giving Things to classes for items to work in classes.
   - For now, my fork will still work for that.
 - Added reference guide! http://magic.elmakers.com/reference/
 - Fix Factions integration for the *other* version of Factions. Yeesh.
 - Night spell changed to "A Dark And Stormy Night", makes it rain
 - Added Citadel integration, spells will not break reinforced blocks
 - Fixed /mitem configure command when removing tags
 - Add EntitySelector action
 - Mine and Chop can't be cast in no-break regions
 - Add "cost_heading" message option for shops
 - Add "fx" spell for quickly testing particle types
 - Add more options for disguised entities
 - Fix spellshop messaging with non-class wands

# 7.3.6

 - Added warnings for use of deprecated spell classes.
   Please check your startup logs!
   Better to fix these spells now, the classes will be removed in 8.0
 - Fix Shock Arrows spewing an error to logs
 - Fix Factions integration, add support for Factions safezone (treated as a no-pvp region)
 - EffectLib effect parameters can now follow the Magic underscore_format
 - Fix wand_undroppable config option not working (though you may want to switch to wand_unstashable)

# 7.3.5

 - Add Light, Fire, Ice and Shock arrows to Magic Bow progression
 - Fix automata spawning via MagicWorlds
 - Fix magic armor not always updating due to inventory drag event
 - Fix stars configs, bending configs and Magic Sword not starting off with a bunch of spells
 - Better handling for color codes in wand names
 - Players can no longer take bound wands out of chests if the wand doesn't belong to them
 - Add some config.yml options to disable casting and selection of spells directly from an inventory
 - Fix a few undo issues revolving around blocks breaking during the undo process

# 7.3.4

 - Fix broken selector icons.
 - Fix donatorshop.
 - Expanded support for message formatting in wand property lore (stacking, non-stacking, positive/negative)

## 7.3.3

 - Fix progressmenu (and other things, possibly) being broken without Vault
 - Allow using \n when configuring a wand description via configure command
 - Changes to classes will take effect properly when using /magic load
 - Camera now requires 1 paper to use
 - Skill properties moved to a "skills" section on classes and spellbook, added "keep" option to keep skills on death
 - Wands can have a "classes" list instead of a single class if they can work with more than one class

## 7.3.2

 - Fixed Magic Bow progression, *bow progress may get reset* - Sorry!
 - The Magic Sword no longer starts with additional spells. This may or may not be temporary, some technical issues came up.
 - Fix non-battle spells and brushes showing in the Magic Sword inventory (they weren't supposed to)
 - Fix active costs for Air Scooter, also Levitate/Air Scooter and Boon will prevent mana regen while active.
 - Fix wand organizing and alphabetizing
 - Fix wand inventory going weird on rankup (when earning additional hotbars)
 - Fix Blink Arrows not working if you hit a wall
 - Fix Recall last death location tracking
 - Fix/improve teleportation ledge detection
 - Fix wand current hotbar not saving
 - Add builtin attribute system
   - Attributes can be defined in attributes.yml, none exist in the default configuration.
     See: https://github.com/elBukkit/MagicPlugin/blob/master/Magic/src/main/resources/examples/rpg/attributes.yml
   - Add /mage attribute command for listing and setting attributes
   - Attributes can be added to wands, and stack player attributes (or increase them to a limit)
   - The wand property "attributes" has been renamed to "item_attributes"
   - The mob property "attributes" has been renamed to "entity_attributes"
 - Add "Magic.commands.mage.others" to allow someone to use /mage commands on other players
 - Add /mage switch command to switch classes for a player (combo of lock, unlock and activate)
 - The Magic.wand.override_drop permission changed to Magic.wand.override_stash
 - Added "unstashable" and "unmoveable" wand properties
 - Capture added to Conduit path (was supposed to be on there, forgot to give it an SP cost)
 - Hostile mobs can't be targeted by Heal, Cure or other boon spells (see: friendly_entity_types in config.yml)
 - Some improvements to Blink Arrows that were way more complicated then they were worth, but the arrows work great now.
 - Add "ai: false" option to magic mobs

## 7.3.1

 - Beginner, Apprentice, Student, and Master wands are now for demo purposes only and not tied to player progression.
 - Fix sniper rifle damage not undoing
 - The /mskills menu will now work with a player's class skills
 - Add quick_cast and skill_limit properties for classes to control /mskills behavior
 - Add auto undo to the Lightning spell
 - Fix flashbang, singularity and earthquake detonating when hitting the caster
 - Fix the Disposal spell
 - Add /mage lock command, for locking a class without removing its progress
 - Add /wand levelspells command, for levelling up player or wand spells
 - Default sword controls changed to right-click instead of Q
 - Fix normal arrows disappearing from magic bow temporarily

## 7.3

 - Update visual FX for nearly all default spells
 - All new high-res spell icons! Thanks to my generous donators!
 - Added 3 new wand appearances, courtesy of lix3nn
 - Add MP5 to war configs (Thanks again to Sidewalk and NavalClash.net)
 - Master Sword changed to Magic Sword, new appearance added courtesy of lix3nn.
 - New Master Sword item that works more like the Zelda equivalent.
 - Fix issues using /wand save to override an existing wand with a modified version of that wand.
 - Fix for FX that should go on a targets' eyes but use the old "use_hit_location: false" method. (See: Blind)
 - Headshot FX now default to normal hit effects if not specified
 - EffectLib updates:
   - Added "transparency" option to ColoredImageEffect and ImageEffect
   - Image effects can now load files from a URL
   - Add ModifiedEffect, which allows you to parameterized any existing effect.
 - Add "resource_pack_prompt" option to config.yml, and /getrp command for players to use to get the RP when they want it.
 - Fixed sandblast
 - Renaming a wand on an anvil no longer organizes its inventory (changed default option to false)
 - Add active spell expression to Skript
 - Add basic support for magic bows
 - Fix wand_undroppable behavior with shulker boxes
 - Fix sexist camera (wasn't working with alex skins)

## 7.2.5

 - Magic mobs can now have a list of triggers, can cast spells or run commands on death/interval/damage.
 - Magic mobs will now focus on the player that has damaged them the most
 - Non-trivial magic mobs will despawn on chunk unload and shutdown
 - Add support for clearing magic mobs from a specific location
 - Add option to the wand buff shop to sharpen the the Master Sword
 - Add physical protection option to buff shop
 - Add Aggro action, to force a mob to target the caster (or the target, via some ChangeContext trickery.. see mob and monster)
 - Add target_tamed property to allow spells to ignore tamed mobs
 - The Cleric Chestplate now heals more the closer players are to the wearer
 - Add hide_flags wand attribute for specific flag visibility per wand
 - Enable headshots on husks, vindicators, evokers and illusioners
   Examples:
    /mmob clear warlock 20                Clear all warlocks within 20 blocks of your location
    /mmob clear all 20 0 64 0 world   Clear all magic mobs within 20 blocks of a specific location

## 7.2.4

 - Added "damage_type" parameter to Damage action, for using custom damage types.
 - Changed bending configs to define earth/air/fire/water damage types, each bending class has protection against
   its own elements.
 - Implemented "weakness" and "strength" properties, which can be applied to classes or mobs for type-specific or overall
   damage weaknesses or bonuses.
 - Added optional count parameter to /mmob spawn command
 - Added Skript integration: https://github.com/elBukkit/MagicPlugin/wiki/Skript
 - Some changes/fixes to how cost/cooldown reductions work
   - They are mostly back to only applying to spells cast with the wand that has the reduction
   - Passive wands (armor, generally) can have reductions that stack and apply to the mage, but I don't have anything
     like that in configs.
   - This was done to prevent possible exploits with certain cost-free wands by holding them in the offhand.
 - Add "boostable" wand property for making wands that aren't affected by mana boosts

## 7.2.3

 - Fix painterly resource pack
 - Fix cooldowns on NPCs, console and command block casting
 - Fix blast protection migration

## 7.2.2

 - Another hotfix for weird behavior with offhand wands
 - Broomstick progress gets stored on a new class instead of the broom item

## 7.2.1

 - Hotfix for infinite recursion error when migrating legacy protection
 - Make sure spellbooks don't put the spell name in their title
 - Some improvements to /describe commands

## 7.2

 - Add Magic Torch (magictorch) item, not craftable, requires LightAPI
 - Added support for spells requiring SkillAPI classes or skills.
   Thanks @robotnikthingy and my other helpful contributors!
 - Add commands and generic requirements support to Selector action
 - Sniper scope is now toggleable
 - Added 4 new guns to war configs, and rebalanced existing guns a bit.
   Thanks to Sidewalk and NavalClash.net!
 - Add "infinite" option for repeat and delay parameters
 - The Boon spell has been changed, it now toggles on and off and gains additional effects as it levels
 - Add wand buff shop to progressmenu, the new way to get protection on your wand
 - Add "damage_type" parameter to Damage action which can be used to fake any vanilla damage type.
   Note that the damage source is lost, so other plugins (like quest plugins, etc) won't see the caster as the
   source of damage and give credit for kills. This is a vanilla limitation.
 - Wand protection format changed to a map (e.g. protection.fall) for greater flexibility. See MIGRATION.md
 - Add "size" parameter for slimes to magic mobs
 - Add requirement, cost scaling, and many other features to Selector action

## 7.1.5

 - Unbreak wands broken in 7.1.4 (your data should be fine)
 - Add CheckEntity action and Holy Light example spell (currently unobtainable)
 - Add ModifyProperties action, WIP- can be used to modify wand or class properties

## 7.1.4

 - Really fix Camera, use online player game profiles and fallback to Mojang API. No more mcskinsearch hackiness.
 - Fix Automata not respecting region permissions

## 7.1.3

 - Default RP changed to Magic-7
 - Added Snoop spell for peeking inside chests, staff-only
 - Make it easier to create one-off simple configs
   - Add load_default_configs option
   - Allow adding wands, spells, etc directly in config.yml sections
 - Re-work appearanceshop, colorshop and effectshop to allow for unlocking wand appearances and changing wands at will
 - Added progressmenu, which is now the default enchantment table spell
   - Provides access to the spellshop and spell upgrades progress menu
   - Allows access to appearanceshop, colorshop and effectshop
 - Add support for completely custom wand lore (WIP, supports a limited set of wand data)
 - Add option for non-quick-cast skill items
 - Add PlaceholderAPI integration. Supported placeholders: magic.path, class, wand, spell
 - Added Magic.commands.mgive.others permission
 - Add wildcard permissions to plugin.yml to support perm plugins that don't handle wildcards (looking at you, zperms :P)
 - Add Light spell, not obtainable, requires LightAPI
 - Sort-of fix the Camera, courtesy (sort of) of mcskinsearch.com
 - Added "equation" spell for easily testing the equation system and attributes
 - The configure commands now support equations, e.g. "/wand configure cooldown_reduction x*2+1"
 - Add MobArena integration to optionally respect arena protection, globally based on protected status or per-arena

## 7.1.2

 - Fix stars and bending class progression
 - Fix /wand save so that the saved template inherits from the one you're editing

## 7.1.1

 - Hotfix for a startup crash that would occur if using SkillAPI with attributes disabled

## 7.1

 - Protection progression removed from wands.
   Old wands will still have protection on them if they earned it.

 - Mana progression changed to only happen on rank up
 - Fix ranking up (was only happening after getting all spells then using an enchant table again)

 - SkillAPI attribute parameters no longer use an underscore prefix.
   See: https://github.com/elBukkit/MagicPlugin/wiki/SkillAPI

 - Heroes attributes can now also be used in spell parameters

 - Crafting recipes can now be added on the fly with /magic load
   - They can not be modified or removed, however, due to Spigot/Mojang limitations.
 - Implement class chooser, used in bending configs. Bending classes are now locked by default!
 - Stars classes are now locked by default. Starshop unlocks classes and gives ingredients for crafting a saber
 - Fix lightsaber crafting

## 7.0.1

 - Potter wands converted to be class-based
 - Lightsabers converted to be class-based
 - Blaster skill upgrades with use
 - Bending powers converted to be class-based and work like the spell book
 - Added support for SkillAPI attributes in spell configs
 - Added support for spells using SkillAPI mana (this is off by default, see config.yml defaults)
 - Fix setting properties on base classes (e.g. /mage configure mana_max 30)
 - Fix mana buffs on armor (wizard armor)
 - Fix quick cast wands

## 7.0

### IMPORTANT NOTE

7.0 Represents a major change for Magic "under the hood", and is hopefully the beginning of a more flexible framework for the future.

However, this means that the initial release of 7.0 could potentially have issues and should be considered semi-experimental.
I've tried my best to test it as much as I could, but with a plugin as configurable as Magic there is always the possibility
that I've missed some use-case or that it won't work quite right with your configs.

So please, if at all possible, when upgrading to 7.0 do some testing with your configs in a controlled environment, or at least
make sure to backup MC player data and Magic player data first. Note that without backups it will be *very difficult* to
downgrade from 7.0, as wand data will migrate to player data, and older versions won't know what to do with it.

If you are using completely customized configs there shouldn't be much change, I'm hoping everything will work the same as before.

If you are using the default configs or inheriting from them, everything should *appear* the same as before. However, things
are going to work a little differently behind the scenes.

Magic is evolving past a custom items and spells plugin, and hopefully will become more like the RPG framework that, I think,
many users have always wanted it to be.

For now what this mainly means is that you can configure how data is stored, instead of it always being stored on the wand items.

The default wands are now set up to store mana, spells and a few other attributes to the owning player. This means that
player progression is now tied to the player, not the wand.

If a player were to craft a new wand, they will see it has their mana and spells on it.

Mana and spells are shared between the Wand, Master Sword and Spell Book, so players are free to mix and match and choose
their own play style without having to re-earn the same spells over and over.

The war configs also make use of this new setup, allowing players to increase their "skill" with certain weapons.

Potter and stars configs have not yet been converted, but they will be, probably in the next release.
Mana and spells will be stored in separate classes, so that configs can still be mixed together and make sense.

In the future I hope to expand on all of this with a class system in the new "rpg" configs. This isn't ready yet, however,
but I have been working on these changes for so long, now that they seem functional and stable I wanted to get them out there.

I hope you enjoy! Please let me know if you run into any issues.

### General Changes

 - War configs: some weapon skills now upgrade with use
 - Add protection option for magic mobs, warlocks no longer take fall damage
 - The ModifyMana action now acts on the target rather than caster
 - Check for lore as well when comparing crafting ingredients
 - Color and word wrap support for recall icon descriptions
 - Fix right-click behaviors with skull wands

### Player Data and Classes

 - Add /mage configure, upgrade and describe commands for dealing with Mage data
 - Add /mage add and remove commands for dealing with Mage spells and brushes
 - Add /mage reset (class) option, for resetting a class progress on a player
 - Add /mage activate and unlock commands, for manipulating mage classes
 - Add classes config file, defaults have a basic setup in each example config. Generally:
   - Spells and mana are held by a base class that may be shared (like by the Master Sword, Wand and Spellbook)
   - Progression path is stored on sub-classes
   - All other properties are still tied to the wand items

## 6.9.21

 - Add support for magic items as crafting recipe ingredients. Only display names are checked!
 - Add support for "\n" to make multiple lines in wand and spell "description" fields.
 - Catch the BlockFadeEvent for undo purposes, fixes path blocks not restoring properly when covered by magic blocks
 - Fix gravity's affect on a projectile's speed. In particular fixes odd behavior with projectiles shot straight up.
 - Fix an issue where blocks undoing after a piston has extended into their space would break the piston
 - Fix a potential for a stashed wand (e.g. air glider) to disappear when using with a wand inventory open
 - Some fixes to the Tag spell
 - Cooldown and "not enough mana" sounds are only heard by the caster.
 - Fix strange undo behavior when players' undo queues are full.
 - Entities are now modified prior to being spawned. This fixes some issues, such as not being able to take weapons
   away from pig zombies or vexes.
 - Fix an exploit that may allow players to put spells in chests and duplicate them

## 6.9.20

 - Fix some undo issues involving overlapping casts that remove sand blocks
 - Optimize large numbers of Magic NPCs
 - Fix an error in the /magic list entities command
 - Fix a potential error when a player logs out while a spell is being cast on them

## 6.9.19

 - Fix some potential spell exploit issues revolving around Disarm
 - Add "bind_on_give" config property, if you want wands to bind when given with /mgive or /wand
 - Fix ability to sneak a wand into a check with "wand_undroppable: true" via hotbar buttons
 - Fix issues with item frames and block-creating spells
 - Fix an issue with the wand sometimes not saving to item when unequipping

## 6.9.18

 - Fix wands breaking blocks like tools even if cancel_interact_on_cast was true
 - Fix double-cast bugs: return click_cooldown to previous default value
 - Allow &4-style color codes in wand descriptions
 - Warlocks no longer cast spells after being disarmed
 - Fix minor block exploit when breaking a block that has been modified by spells more than once before undo

## 6.9.17

 - Wands now use right-click instead of drop by default!
 - Some technical changes for performance and anti-glitching:
   - Remove several calls to Player.updateInventory
   - Remove unnecessary wand data saving, reducing item bobbing
   - Change casting to use animate event rather than interact event
 - Wand inventory, hotbar and brush instructions will now change to reflect wand controls

## 6.9.16

 - Fix tile entity support ("log spam") in 1.12.1

## 6.9.15

 - Add new Snake automata
 - Defenders are a 50% reflective
 - Add max_velocity config option, defaults to 10 but if you're running Paper you may need to change it to 4.
 - Fix more glitches with undoing changes to doors (grrrr doors)
 - Fix spells in inventory (as items) disappearing when opening/closing wand

## 6.9.14
 - Automata:
   - No longer have hearts, will lose blocks as they are destroyed and eventually die
   - Can't be spawned from command blocks, will erase any old command blocks from previous automata
   - Added some more name suffixes and prefixes
   - Increase spell attack range
 - Add support for setting permission nodes on shop items
 - Blast and Sticky Bomb use block cracking effects
 - Fix hollow Cylinder and Disc casts at larger radii
 - Add Ring action (hollow Disc)
 - Fix Blink looking broken when casting into the air or out of range

## 6.9.13
 - More Automata changes:
   - Automata no longer use command blocks
   - Automata are reflective, except for the redstone
   - Re-balance automata drops and resurrection costs to avoid abuse
   - Removed automata.yml, automata now just despawn on chunk onload and server restart
   - Defenders use a different heart (lapis block)
 - Blast and Collapse are now projectiles
 - SP Earn Bonus (Mana Talisman) will work on SP item pickups
 - Mana Talisman set to keep on death
 - Fix Recall warmup FX
 - Add "/mconfig clean" command, for cleaning up configs that have had the defaults copy+pasted into them
 - Add Vault integration support for MagicArenas
 - Implement faster block changes, currently in limited use (MagicWorlds, maybe Automata)
 - Add SP potion (potion_sp100 wand)

## 6.9.12

 - Add enable_map_animations config option for turning off GIF maps for whatever reason
 - Add different colored blasters to star configs
 - Fix Projector spell, giphy doesn't like my direct-linking anymore.
 - Don't allow placing wands in furnaces
 - Removed TNT and creeper auto-rollback from war configs
 - Fix SuperAlter
 - Fix issue with Blast + Push leaving behind permanent blocks
 - Fix Magic Missile glitching out and duping doors
 - Add ModifyGameMode action
 - Automata changes:
   - Drop SP and gold as well as hearts, drop less XP
   - Fix targeting, worms no longer run away
   - Will clean up after themselves (WIP)
   - Improved aim, rebalanced to use more powerful spells, but may be easier to kill
   - Naturally spawned automata can use restricted materials

## 6.9.11

 - Fix shop text bugs introduced in 6.9.10
 - Fix Disarm acting strangely when holding an open wand

## 6.9.10

 - Fix some issues with 1.10 compatibility having to do with the Guava version
 - Add "shops" section in messages.yml, for localizing default text in shops
 - Add "target_name" spell parameter, for only targeting an entity with a specific name.
 - Added TakeItem action, can be used for stealing or picking up specific items at a distance (for Quidditch, maybe)

## 6.9.9

 - Wands can now be put in chests or dropped by dragging them out of the inventory. Set "wand_undroppable: true" in config.yml to change back.
 - Example configs (stars, potter, etc) no longer use enchantment table
 - Fix dupe bug with Magic Missile/Laser used on chests
 - Fix cooldown on sniper rifle
 - Fix item dupe exploit from exploding flower pots that auto-undo, they no longer drop their contents
 - Fix dropping stackable wands (like C4, thermal detonators)
 - Fix armor stand projectiles getting stuck in the world when hit with an explosion
 - Add exclude_spells option to ApplyCooldown. May not be super performant.
 - Add more support for special disguises with LibsDisugises (baby=true, material=stone, etc)
 - Add carshop to war configs, and single-use wand items for spawning cars
 - Players are invisible and untargetable while riding in a car
 - /mmob clear will also clear non-mage mobs (like cars), though only in loaded chunks
 - Add MobArena integration (PR to MobArena pending), use "magic:wand" in MobArena configs for Magic items, magic mobs can also be used
 - Fix being able to put wands in chests with wand_undroppable on by using the hotbar buttons
 - Add staff/glider to bending configs WIP (Thanks, dejakob!)
 - The Glide spell no longer puts an Elytra on you, it just makes you glide
 - Add "sp_items_enabled" config option, just in case you've, idk, given a bunch of creative players SP items that they
   of course duped and now you need to turn off SP items.
 - Fix cycle mode with upgraded spells
 - The GiveItem action now needs to have "target: self" added if you want the item given to the player, see giveitem spell

## 6.9.8

 - 1.12 Compatibility
 - Update Alter spell to be much more flexible, group blocks like logs and leaves, work on new 1.12 blocks
 - Add model3_red, model3_silver, model3_black, model3_white to war configs, now have a real model

## 6.9.7

 - Fix airscooter crash, add general protection for invalid velocities
 - Headshot parameters for missile,railgun and blind changed to use multipliers. If you have overridden these
   in your configs you may need to update. (set damage_multiplier or duration_multiplier to 1 in headshot_parameters)

## 6.9.6

 - Fix image map file not saving in some environments (Windows, I think?)
 - Fix broom disappearing glitch if you open a wand inventory while riding
 - Move vehicle improvements
 - Make it possible to combine war configs with other configs (using add_examples)
 - Guns are now less accurate if used while moving
 - Add "ignore_types" optional list to spells, for ignoring specific entity types when targeting
 - Fix broomsticks randomly re-mounting players while riding
 - Fix Polymorph spell not undoing after 20 seconds as advertised
 - The Silence spell will now work on Heroes skills as well (applies a cooldown to them)

## 6.9.5

 - Fix broken build from 6.9.4 (Sorry about that!)
 - Fix vehicle control in 1.10 and 1.9
 - Cars can drive in reverse
 - Broomsticks now fully controllable, including strafing and using spacebar to ascend
 - Fix brooms randomly kicking players off (again- working around a Spigot/MC bug)
 - Add Ferrari to war configs (Thanks, BlueYoshi68!)

## 6.9.4

 - Magic Missile and Laser now use block-breaking effects
 - Fix compatibility with PerWorldInventory plugin via a "close_wand_on_world_change: true" config.yml option
 - Put the Heroes wand back, I removed it prematurely.
 - Fix mob spawners that use cave spiders (entity id changed?)
 - Fix the Stash Chest schematic
 - Add sp_default config option, for starting out players with some spell points
 - Add "warmup" option to spells, to show a warmup in spell lore.
 - Add some spell lore options to show cooldowns less than a second, and show spell categories (not used in default configs)
 - Fix an issue with loading resource packs from outside the U.S.
 - Add driveable car to war configs, WIP
 - Fix imagemaps not saving if the save file does not already exist
 - Add /mitem configure command, mostly for me to test things, maybe dangerous to use

## 6.9.3

 - Add "war" example configs, WIP
 - Fix quiet, loud and silent wand upgrades
 - Brooms will stop working when they hit a world border
 - Add Extinguisher and game mode switcher items, just for fun
 - EffectLib optimizations
 - Fix an error that happens if a projectile is cancelled before it launches (e.g. Disarm someone during Kill warmup)
 - Sell shops will differentiate between custom-named items
 - Add /magic rpsend command to re-send current RP to all online players
 - Resource packs are now sent to players on join instead of relying on vanilla mechanics. They are delayed by 1 second by default.
 - Wand lore colors moved to messages config, you may need to update any customizations if you want to keep the colors
 - Add optional parameter to /mage unbind to unbind a specific wand type
 - Add AreaOfEffectCloud action as a shortcut to using SpawnEntity for clouds

## 6.9.2

 - Fix broom duplication/wand destruction glitch when using a broom in the offhand
 - Change spell names for all force powers in stars configs
 - Fix wildcard (*) and negated material sets
 - The blocks Blast can break are now based on vanilla durability rather than a material list
 - Add appearanceshop, for selling wand appearance upgrades
 - Add Uberwand, the go-to "what's the most powerful wand" for showing off or plugin testing
 - Added crafting recipe for the spellbook

## 6.9.1

 - Reflect now snaps to even 45 degree angles
 - Fix Laser level 2, level 3 - upgrade SuperLaser
 - Cleaned up trinketshop, added a few new skulls.
 - Add Projector spell (architect), just a bit of showing off
 - Add "bypass" parameter to spells, to simply bypass all permission, build, pvp and other checks. Useful for NPC casting.
 - Improvements to /mtrait command:
   - Add "target_player" option to turn off auto-targeting the clicker
   - Add "message_player" option to relay cast messages to the clicker
   - /mtrait parameters now lets you edit individual parameters instead of having to replace them all
 - Improvements to /mmap command:
   - A backup file will be saved in case the imagemaps.yml file gets lost or corrupted
   - Added /mmap name command to name existing maps, makes them easier to find with /mmap list <name>
 - Fix wand template migrate_to, old wands should once again migrate to new
 - Fix /wand create command
 - Add a ton of example shops to the default configs, see defaults/spells/shops.yml
 - Add mage.earned_sp message, goes to action bar by default whenever a player gains Spell Points
 - Staff wands no longer earn SP

## 6.9

 - Using "example: 6" in config.yml will restore previous default wand behavior
 - Moved lightsabers back to the stars config only. Use "example: stars", or see MIGRATION.md
 - Fix Stash (maybe others) not working in chest mode
 - Remove creative mode switching wands to chest mode. Click on wand to cycle inventory pages, it's a little glitchy though.
 - Fix SuperRing spell
 - Add cast_location property to Wands, a vector that will determine the source location of spells
 - Add configurable cast location offset when a player is sneaking
 - Potter wands start with Vermillious, not Flagrate
 - Potter wand box is now "wandbox", and wand is "potterwand". The "wand" template was left in for backwards compatibility.
 - Add "replace_on_activate" wand property, for a wand that will change into a different wand when held
 - Wands will attempt to keep some inventory space free, to make organizing easier and to allow item pickup
 - Add SuperLaser spell (to Architect path)
 - Fix engineering spell bug with erase brush when switching from a non-engineering wand, would fill with dirt
 - Fix passive wand potion effects (like night vision goggles) not always restoring after death
 - Add sp_multiplier property to wands. Can be used for bonuses, or to turn off SP earn.
 - Mana Talisman got a new icon and some buffs
 - Fix some issues with NPC casting on custom environments (don't load NPCs as player data)
 - Fix enchantwand spell giving 30 levels instead of 1

## 6.8

 - Cast messages now go to the action bar, this can be changed via the cast_message_prefix in config.yml
 - Chest mode wands no longer have weird virtual hotbars, and has a configurable inventory_rows property.
 - Add spellbook item which is like a cross between a chest and inventory mode wand and the skill selector.
   Won't be really useful in default configs until 7.0, but may be useful with Heroes skills.
   The heroes example configs override the spellbook to use Heroes skills.
 - The enchantment table will still show the spell shop if SP is disabled.
   Set enchant_block: "" in config.yml if you don't want this.
 - EffectLib memory optimizations
 - Fix Pollinate double-high flowers
 - Blink and other teleport actions will respect WorldGuard exit deny regions (can be turned off via require_exit parameter)
 - Some additional protection against magic fire spread (esp. Grenade Level 2) not undoing properly.
   You may still lose some leaves due to untracked leaf decay when surrounding blocks burn.
 - Wands with chest mode and quick cast will cast spells on clicking in the spell inventory
 - Fix Torch/SuperTorch insta-breaking at close range
 - Throttle offhand casts so it's not so weird and spammy
 - Fix some edge-case uses of location offset parameters (otx, oty) that were breaking the Wolf House since .. a long time
 - Fix wand dupe issue with the swap item button when the wand has no handler for swap
 - Add title and action bar support to Message action, add fun title to Kill spell
 - Add hotbar_changed message (by request), will show on hotbar change, but empty by default
 - Add support for sending any message as a title or action bar message with "t:" or "a:' prefixes in messages.yml
 - Fix Sith lightsaber paths not increasing mana

## 6.7.2

 - Enchantments on wands are now hidden
 - Renamed Skill Points to Spell Points. Use "example: 6" in config.yml to undo this change.
 - Add /magic help command, simple command that relies on /help for more info
 - Add heroes_skill_prefix config property, to prefix skill names from the Magic Heroes skill pack
 - Add /mitem damage command
 - Heroes skills sorted by level (then name) in skill selector
 - Add Heroes party support, attack spells will not target players in your party
 - Add "only_friendly" spell parameter (added to Heal, Cure, etc). If using scoreboard teams or Heroes parties, these
   spells may only be cast at friendly targets.
 - Magic Heroes skills will advertise inability to cast in hotbar icons just like the base spell would
 - RIP mcstats. Switched to bStats.
 - Added /magic clean command, removes all Magic data from item but leaves lore and other metadata intact
 - Added support for spawning area of effect clouds, Neutron Bomb spell re-worked to use it
 - Fix /mitem duplicate command
 - Fix mobs not being able to target players with spells in no-pvp areas
 - Fix wand "glow" property, now uses a hidden luck enchantment
 - Fix Velocity actions glitching out dropped items and TNT

## 6.7.1

 - Magic Heroes skills will broadcast their use-text like other skills
 - Change Talisman recipe to use gold nuggets for 1.10 and below support

## 6.7

 - The active spell inventory page will be saved to wands
 - Fix bug where active hotbar number didn't get saved to wand
 - Fix item dupe bug with droppable wands that have a spell inventory (ocarina)
 - Broomsticks will upgrade on use in the potter configs, too
 - Some fixes to the potter config progression
 - Add dementor and deatheater mobs to the potter configs
 - Fix Ocarina 7F# note (note07)
 - Fix some wand effect colors not working (hex with no letters)
 - Add a disabled icon for skull-based configs to use
 - Add resource_pack_check_interval, defaults to 5 minutes, to auto-update RP SHA while running
 - Heroes integration improvements:
   - Allow using disabled icons and skull-based disabled icons.
   - Disabled icons will show in skill selector and hotbar for unavailable skills
   - Can't take an unavailable skill item out of the skill selector
   - Hotbar skill timers will reflect mana costs
   - Skills can provide display names for the skill icon
   - Fix the Heroes Wand
   - Add "example: heroes" configs with craftable Heroes wand
   - Add Magic Skill Pack, all default Magic spells packaged as skills:
     https://s3-us-west-2.amazonaws.com/elmakers/Magic/MagicHeroesSkills.zip
   - Support pass-through parameters from skill configs to Magic spell skills
   - Add Skill Book item, craftable in heroes example configs
   - Allow "tier" in a skill config to use a levelled-up version of a spell as a skill.

## 6.6

 - Wand creation permissions have changed, negation simplified:
   - Magic.create.<wand> : Permission to create a specific wand
   - Magic.create.* : Permission to create any wand, ops have this by default
   - Magic.commands.wand : Permission to use the /wand command to create wands
 - The Magic.create permission can also be used to limit /mgive access
 - Cast permissions changed to avoid complicated negation inheritance issues
   - Add "-Magic.cast.*" to prevent players casting any spells
   - Add "Magic.cast.<spell>" to add back individual spells
 - The /mage delete command changed to /mage reset
 - Enchanting configs renamed to "paths".
   - Old enchanting.yml file will be renamed (one-time migration)
   - enchanting folder no longer used, please move files by hand if you have any in there.
 - Wands no longer have unique ids by default, unless they are tracked and dropped on the ground.
 - New properties (you may want to set on your wands if you have completely custom configs):
   - unique: if true, wand will always have a unique id (in case you need it for API integration, or want to avoid stacking)
   - track: if true, wand will be tracked when dropped on the ground
   - immortal: if true, dropped wands will not be allowed to despawn
   - invulnerable: if true, dropped wands cannot be destroyed
 - Fix Heroes skill selector breaking when there are unavailable skills shown
 - Simplified path configs so they are easier to understand and override.
 - Fix the bending example configs
 - /mage configure and /mage describe commands changed to /mage setdata and /mage getdata
 - /wand describe can now be given a parameter to list a specific wand property
 - Changed /wand enchant command to work with no parameters, add /wand create, rename /wand unenchant to destroy
 - The Magic.commands.wand.create permission is now used for the /wand create command, not required for /wand
 - Add /mitem destroy command, just destroys your held item
 - Fix some metadata items like skulls and banners
 - Add optional entity_damage_reduction config
 - Add craftable Mana Talisman item
 - Broomsticks no longer bind and are droppable

## 6.5

 - Wands now default to no actions if none are specified in the configs. You may need to add the following to your custom wands:
   - left_click: cast
   - drop: toggle (or right_click: toggle, as you prefer)
   - mode: inventory
 - Wands now reference templates, rather than copy all of the configuration to item data. Wands should auto-migrate
 - Wand upgrade item format changed, existing upgrades should migrate
 - Broomsticks improve with use
 - Fix sound effects when interacting with an enchant table
 - Some tweaks to the laser spell
 - Really fix bypass_pvp permission
 - Add support for dyed leather armor wands, change apprentice gear to blue armor
 - Offhand wands are now truly "active", including lightsabers. Blocking with an offhand saber now works.
 - Some fixes for limited and single-use items.
 - Add flying sound effects back to broomsticks
 - Add cancel_on_no_permission parameter, brooms will deactivate when entering an area where they are not allowed
 - Fixed not being able to purchase optional spells anymore after reaching the end of the Wizard path
 - Replace wand "bubble FX" with something custom and less volatile
 - Tweak Force FX, reduce mana cost and range

## 6.4.1

 - Fix SuperBroom, riding the Wolf Staff
 - Fix One-Way Ticket to Boomstown spell
 - Add DropItem action, for dropping items on the ground via a spell
 - Fix Map Brush
 - Some more fixes, hopefully, for dual-weilding and right-click behavior
 - Fix bypass_pvp permission, when trying to target a player in a no-pvp zone
 - Fix lightsaber progression

## 6.4

 - Some fixes to disabled spell icons getting stuck on
 - Fix crafting recipes with material variants, like the coal -> ink recipe
 - Complete overhaul of Broomstick mechanics (Still kind of WIP)
 - Update Air Scooter as well, add to default configs
 - Add craftable Emerald Sword, just for fun
 - Fixes to Mirror Shield
 - Miscellaneous fixes to wands put in chests, interacting with doors
 - Fix saved wands not working correctly until after reload

## 6.3.5

 - Spells now show when they are disabled with a "no" icon
 - Add Mirror Shield, a craftable shield (nether star + shield) that can reflect spells (WIP)
 - Fix weird offhand item behavior while holding a wand
 - Recall, Cure, Heal, Phase all now cancel if you cast another spell during their warmup period.
 - Combined the survival, bending, stars and potter RPs into a single RP
 - Update/fix light saber appearances, add sound effects, add all colors to survival configs
 - The (optional) zombie flesh -> leather recipe now requires 4 zombie flesh instead of 9
 - Added some very limited "friend list" mechanics to Recall, mostly for my own use.

## 6.3.4

 - Add ModifyMana action
 - Fix/Improve PVPManager integration (don't target players with PVP disabled)
 - Added "example: skulls" option to easily re-enable skull-based icons
 - Add live_hotbar_skills option to allow skills to have a cooldown timer
 - Reduce Tornado velocity to avoid server-side velocity checks
 - Add icon_disabled_icon option to spells, to show a different icon when they are not castable
 - Restore compatibility with WGCustomFlags plugin as a fallback

## 6.3.3

 - Add work-around for broken zombie villagers in 1.11.
   Not sure I can really fix this while keeping backwards compatibility :(
 - Update resource pack, fix Ocarina sounds

## 6.3.2

 - Fix compatibility with recent version of 1.11, which have changed the CraftWorld.spawn method signature.

## 6.3.1

 - Fix spells that create colored blocks (Blob, Reflect, etc)

## 6.3

 - Add support for 1.11
 - Fix the ModifyBreakable action making air blocks targetable (mainly affects Frost)
 - Add support for casting while in GM2 (Adventure mode)
 - Fix broken /wand enchant command

## 6.2.4

 - Add target_self parameter to ChangeContext and CustomProjectile actions
 - Minor nerf to wand power affects on construction radii
 - Fix for out-of-control Tornado (and possibly other spells) with increased wand power
 - EffectLib fix for icon_crack particles crashing clients.

## 6.2.3

 - Fixes for PVPManager and Heroes integration

## 6.2.2

 - Fix broomsticks in 1.9.4
 - Fix for command-block casting and automata
 - Don't apply velocity to bound wands when an authorized player tries to pick one up
   Prevents wands getting lost, and possible issues with velocity going too high.

## 6.2.1

 - Fix for Broomsticks, though a larger re-work is probably coming
 - Fix Capture spell
 - Some fixes to wand migration, won't affect most configs

## 6.2

 - Fix CraftBukkit compatibility
 - Fix Heroes integration throwing NPEs
 - Add compatibility for Spigot/MC 1.10
 - The RP checker will now compute an actual SHA1 for your RP, since 1.10 now verifies it

## 6.1.2

 - Add "Soul Wand" system (currently work-in-progress)
 - Fix cyclemode upgrade
 - Add Magic.commands.cast.parameters permission, in case you really really want to give your players /cast permission.
 - Add Magic.spawners permission, to allow non-op'd players to place custom mob spawners
 - Converted tweak/upgrade icons to RP-based icons
 - Several spells are now optional, players don't need to acquire them to rank up
 - Add "/mtrait hat" command, for putting spells, wands and hats on an NPC's head (See: http://imgur.com/a/FQLnp)
 - Add support for an "override" entry in spells.yml, for settings that should effect ALL spells
 - Fix compatibility issues with keep-on-death wands and CombatTagPlus kill-on-quit
 - Offhand casts now show a visibile swing animation
 - Add "filter_bound" parameter to ItemShop, to not show bound items a player already has

## 6.1.1

 - Fix broken wand progression when spells have been disabled. (Sorry!)
 - Fix architect/engineer upgrade shops (Also Sorry, bleh)
 - Mine/Chop will no longer make drops out of temporary blocks
 - Second page of book can be used to set name/lore in /mitem skull command
 - Spell casts now originate from correct hand for Lefties
 - Add "save_default_configs" option to config.yml, is kinda wonky but works

## 6.1

 - All spell icons are now in the Resource Pack, player skulls are no longer used.
 - The default files are no longer saved to your server. I found this too confusing for people. Check github for the defaults.
 - Updated "trinketshop" with lots more skulls
 - All decorative player head names prefixed with "skull_"
 - Fix warlock wand appearance
 - Add /mitem add/remove unplaceable command, for making a block unplaceable
 - Add Dark Wizard, Mega Spider and Dark Spider mobs
 - Add Webbing spell (for spider mobs)
 - Add /mmob clear command, for getting rid of magic mobs
 - Add "Magic.use.<wand>" permission list, for controlling individual wand permissions.
 - Add spsellshop, for exchanging SP for money
 - Add disguise support (via LibsDisguises) to magic mobs
 - Add madscientist mob (example of using player disguises)
 - Add DisguiseAction, Polymorph spell (not on any enchanting paths since it relies on LibsDisguises)
 - Nerfed Blessing
 - Item worth specified in items.yml will be used by shops, using a simple list of items

## 6.0.11

 - Don't allow water spreading when water naturally flows as a result of magic
   This fixes a server-crashing issue with player skulls and flowing water (Triggers internal Spigot bug)
 - Add support for custom mob spawners that spawn magic mobs
 - Add spawnershop and custom mob spawners (including Warlocks)
 - Warlocks now have fall protection and thorns armor

## 6.0.10

 - Fix wand enchanting (when sp is disabled), was broken in 6.0.9
 - Update brooms, the boost power (space bar)c will now directly affect speed.
 - Add /mmob list command, to list all custom mob types. Tab-complete now includes vanilla mobs.
 - Add "allow_pvp_restricted" config option, to allow pvp-restricted spells to be cast anywhere.
 - Add health and hunger options for spell casting costs

## 6.0.9

 - No longer blocking crafing of the wood hoe and diamond sword
 - Default emerald worth changed to 100 to make Vault-less economy less expensive
 - Currency system tweaked, deposits/withdrawals only work with emeralds and gold nuggets
 - Add survival_fly NCP exemptions to brooms/levitate
 - NCP integration is on by default (if NCP is present)
 - Fix for magic wearables not getting worn on right-click
 - Add /mitem export command, dump items, names and costs to a CSV file
 - The FallProtection action will now work an unlimited number of times by default (within the given duration)
   Use the protection_count parameter to reduce this.
 - Fix wands still having right-click turned on by default (Toggle button is Q now)
 - Fix for "circular riding" issues with Mount
 - Add "sp_earn_enabled" option

## 6.0.8

 - Possible breaking API changes: Refactored casting costs, changed range values to double
 - Add Dragon Ball spell (shoots an Ender Dragon fireball)
 - All mana-related configuration and properties changed from "xp" to "mana". Should be backwards-compatible.
 - Improved ender dragon targeting, fixed ender dragon direct damage
 - Add support for spells consuming SP to cast
 - Tweaked Petrify spell, add 2 more levels
 - Add "/mitem type" sub-command for changing an item's type
 - Fix wands interfering with infinite potion effects added by other plugins
 - Changed "mode_cast" property of wands to "quick_cast". Backwards compatible.
 - Add more customization options for wands, each of left/right click, drop and swap can now
   be assigned a specific function from cast, toggle inv, cycle hotbar, cycle spells.
 - Add Lightsaber configs (W.I.P.)
 - Added EquationEffect to EffectLib, for creating custom effects

## 6.0.7

 - Falling blocks won't cause physics updates if the spell that launched them doesn't
 - Some fixes to tracking attached blocks, particularly fire, for undo

## 6.0.6

 - Fix the default wand having no mana- SORRY about that!
 - Fix issues with cooldowns resetting when they shouldn't
 - Fix welcome_wand feature
 - Add "Capture" spell- Conduit (staff) only for now. Captures any mob into a spawn egg.
 - Add support for a few attributes to the /mitem command (attack damage, attack speed, movement speed)
 - Add "Philbert Egg" item, a charged creeper egg. DANGER!
 - Add "mobshop" for selling mob eggs, several more custom spawn eggs
 - Add relative location support to the "mmob spawn" command

## 6.0.5

 - REQUIRES Spigot build 697 or higher!
 - The offhand swap button (F) is now used to cycle hotbars
 - Fix brooms

## 6.0.4

 - Add craftable Ocarina item (Thank you, SexyToad!)
 - Cloak of Invisibility is now invisible while worn
 - Fix wand effect_bubbles
 - Add items.yml, configuration for setting up non-wand items and item values
 - Add new permissions: Magic.wand.overwrite, Magic.wand.overwrite_own for controlling "/wand save"
 - Default wand is now called "wand" rather than "default"
 - Added "/wand delete" command for deleting saved wands
 - Add /mitem command:
   - save : Save your held item, can be used in crafting, /mgive, etc
   - delete : Delete a saved item
   - describe : Show item details
   - name : Set a custom display name for an item
   - duplicate : Clone an item
   - worth : Show how much an item is worth, can be defined with /mitem save
   - add/remove : Add and remove lore, flags, unbreakable, enchants (coming soon: attributes)
 - Add "hylian_shield" item (non-magical, just a cool Link shield)
 - Add "trinketshop" including several decorative player skulls (try /mgive aquarium)

## 6.0.3

 - Add "Glide" spell- an instant Elytra!
 - Add "bypass_upgrade_commands" permission, useful for staff if your command contain rank-ups
 - Lift now applies the Levitation effect when used on a mob/player
 - Add Elytra crafting recipe, disabled by default
 - Add attributes support for magic mobs (used only on Mutant Captain for now)
 - Fix issues right-clicking signs

## 6.0.2

 - Fix issues with pasting signs in schematics (Fixes Wolf House crashing clients!)
 - The mana bar is now completely virtual, should fix compatibility issues with other plugins
 - Levitate deactivates on dismount rather than crashing (for now?)

## 6.0.1

 - Fix Magic Hat appearance
 - Fix wands not being craftable
 - Fixed wands being placeable in droppers
 - Only bound+undroppable wands are completely undroppable now, but can be put in ender chests
 - /wand restore won't restore wands that you have in your ender chest

## 6.0

 - Dropped support for 1.8!
 - Wands in the offhand slot can cast spells with right-click. Dual-wielding wands!
 - All wand templates changed to "Q to drop" by default! This is to support the off hand.
 - Fix sounds not playing
 - Fix PermaFrost and Forest spells, biome lists needed updating
 - Add custom villager recipes to magic mob spawning. Yay economy rebalance!
 - Tracking Sense now applies the glowing effect to the target entity
 - Fix for Arrow-based spells
 - All wand icons moved to the wooden hoe, other icons are now normal craftable items.
 - Add enable_resource_pack_check option to work around new RP behavior where it won't update on changes.

## 5.5.1

 - 1.9 Fixes: offhand/armor getting wiped, broomsticks (mostly) unbroken, offhand spell-equiping exploits

## 5.5

 - Added support for Spigot 1.9
 - Added /mmob spawn command, for spawning vanilla or custom mobs
 - Added mobs.yml, mobs.defaults.yml - for creating custom Magic Mobs
   - Magic Mobs can have custom equipment, health and display names
   - They can also randomly cast spells!
   - Use the MagicWorlds plugin if you want to spawn them randomly in your worlds
 - Added /wand commands: bind, unbind
 - Added /mage command: unbindall
 - Re-balanced Engineer and Architect mana, Architect spell prices. All icons made unique.
 - Fixed exploit with Monster+Shrink/Grow, added Shrink and Grow back into spell paths.
 - Fix Rollback giving back blocks for Architect spells cast with a Manifestation wand

## 5.4.8.1

 - Fixed a progression-stopping bug in the spell shop from 5.4.8
 - All economy values scaled to be the same by default. This means that 1 SP = 1 XP = $1 = 1 Emerald. You can use the
   following config.yml options to re-scale if needed: worth_sp, worth_xp, currency (see config.defaults.yml!)
 - Add scale parameter to SpellShop and ItemShop for easy price scaling up/down
 - Fix some issues with Flood Fill, Recurse, Mine and Chop
 - Architect spells now consume blocks like Engineer spells.
 - Re-enabled Toss in Engineer rank.

## 5.4.8

 - Added Fury spell (uses new Asynchronous action, and "plan" CustomProjectile flight plan config)
 - Added Chain Lightning spell
 - Wands (including the Master Sword and magic armor) can no longer be enchanted via an anvil.
   Set enable_combining: true in config.yml if you want this back.
 - Update FX on all Engineering Spells
 - Tag signs and SuperTorch torches are returned when broken or rolled back
 - Fix Tag signs sometimes facing the wrong way
 - Tree spell will work with any sapling type
 - Engineering brushes can mix block variants (data values)
 - Cooldowns fixed, they were slightly (up to 1 second) shorter than they should've been.
 - Added Workbench spell to Beginner rank, now just opens a workbench inventory directly
 - Pickpocket can now shift+cast to view target player's Ender chest
 - Add ability to sell spell upgrades in SpellShop

## 5.4.7

 - Engineer and Architect spells being re-balanced to consume blocks required for building
 - Add "Conduit" rank (after Architect), bypasses block consume ("Manifestation")
 - Undroppable wands can no longer be dragged out of the inventory or placed in containers.
   Set "wand_undroppable: false" in config.yml for old behavior. Players with "Magic.wand.override_drop"
   permission can still drop and put wands in chests.
 - Wands are only enchantable if there are spells left to get, brushes no longer count
 - Add EntityProjectile and PlaySound actions
 - Add Patronus spell to potter configs (damages Withers)
 - Add the Brush Selector as a spell that Engineers get
 - Prevent locked wands from learning new spells
 - Placing a Recall marker requires build permission
 - Skulls and banners now work better with the Hat spell, Hat spell can level up
 - Update Stream spell
 - Rebalance emeralds in deposits/withdrawals, added iron ingots and blocks
 - Fix incompatibilities with Wizards pickups (and possibly other special plugin drops)
 - Add "drop_changes_pages" config option, to work-around compatibility issues with UltraCosmetics plugin.
 - Some fixes to renaming wands on anvils

## 5.4.6

 - Add warmup back to Kill
 - Separate hit/miss actions and effects for projectiles, nothing happens on miss now by default.
   ("miss" generally meaning it flies past its range without hitting a block or entity)
 - Add Levitate to master, add levels to levitate- starts out very slow
 - Broomsticks will work in regions with entity spawning denied
 - Renamed the "master" path to "Wizard" (just the label, not the key)
 - Wool is now destructible for levelled up spells
 - Disable several OP crafting recipes by default.
 - Add 5-second warmup to Phase spell

## 5.4.5

 - Allow players to re-Secure chests even if they don't have their old key
 - Add confirmation GUI for moving Recall marker
 - Mobs will ignore superprotected Mages
 - Add alt-template to Secure, to make transitioning from old keys easier.

## 5.4.4

 - ! Secure key format changed to use masked UUIDs. Old keys will work on old chests, old chests
   can be converted to new format by casting at them with the old key in your inventory.

 - Nerfed Broomsticks, now have a 60-second duration and a 10-second cooldown.
 - Secure keys are now kept on death
 - Add sneak-cast alt to Secure to unlock
 - Add "Tame" spell
 - Add "world_border_restricted" spell option, to prevent spell casts or broom flying outside the world border. Off by default.
 - Fix spells not upgrading on Master Sword
 - Add PermaFrost spell, ChangeBiome action
 - Add Tracking Sense Level 4, sneak-cast to look only for players
 - Wands now gain physical protection, up to 75%
 - Added melee_damage_reduction, off by default, can be used to globally nerf melee damage
 - Fix for temporary entities not getting removed if the chunk unloads before the timer expires.
 - Add Flood Fill spell, Action-ize Drop and Recurse actions
 - Added Recall support for owned/rented PreciousStones fields (requires a dev build of PS plugin!)

## 5.4.3

 - Optimize /magic load, almost entirely async, load Mage spell data only when needed.
 - Added several more Blink/Fling levels
 - Added "warpunlock" and "warpmenu" spells, for region or NPC use (works with Recall)
 - Prevent Blink passthrough on obsidian, iron blocks and iron bars
 - Fix Railgun not having auto-undo (!!)
 - Re-work color shops:
   - Bubble and particles effects are now separate from color upgrades and in a separate effectshop
   - Added 15 colors to color shop, plus a color randomizer
 - Fix Wolf House spell/item
 - Add questscompass, ledger for Quests/Vault integration
 - Add several new shops: ledgershop, withdrawals, deposits, lootshop, effectshop

## 5.4.2

 - Added Blink Level 4, can now blink up into the air
 - Renamed "Disc" to "Platform"
 - Added "headshot" functionality, some spells will have different effects when you hit the target's head:
   Railgun, Magic Missile
 - Added a completely separate progression path for the Master Sword (Warrior->Squire->Soldier->Knight)
 - Added Minigames plugin integration to prevent issues with storing inventories when going to a minigame

## 5.4.1

 - Building against 1.8.8 now- plugin may not function properly in older versions of Spigot.
 - Add auto-rollback for creeper or other natural explosions. Disabled by default, see auto_rollback_duration parameter.
 - Add options for relative effect locations (Thanks, SexyToad!)
 - Add "track_range" CustomProjectile parameter, for projectiles that track the player's cursor at a fixed range (Thanks again, SexyToad!)
 - Modify "Whip" spell to use the above
 - Add Railgun spell, ability for CustomProjectile to hit multiple targets  
 - Add mathematically-controlled custom projectile velocity (Thanks yet again, SexyToad!)
 - Sticky Bomb will attach to entities
 - Fix apprentice gear (and other non-indestructible items) getting their durability auto-replenished (it wasn't supposed to!)
 - Add TransformEffect to builtin effects. Build your own effectlib-style effects via configs!
 - Glass is no longer transparent by default (spells won't shoot through glass, too exploitable!)
 - Fix Tree/Forest spells
 - The Drop spell will now collapse trees and only drop part of the tree

## 5.4

 - Move Tag back to Beginner, but it requires a sign.
 - Brooms now used tamed horses, which should play nicer with WorldGuard regions that block mob spawning.
 - Add SuperBackup, Backup is now a simpler one-click cast
 - Add Architect Staff, a wand for admins with architect spells
 - Rebalanced SP costs for longer progression
 - Moved Portal, Anvil to Engineer (for now?)
 - Fixed issue with spell shops and spell permissions
 - Fix "block crack" particle FX
 - Make Sandblast a projectile spell

## 5.3.3

 - Enchanting paths can have a simple list of spells instead of a probability map
 - Change "max_damage_reduction" (and related) to "max_protection" (this has apparently been broken for a while!)
 - Updated Meteor Shower to use CustomProjectile (EPIC! Thanks, Droobledore!)
 - Added destructible2 and destructible3 lists, no non-engineering spells use check_destructible: false anymore.
 - Add more levels to Breach, Peek and Sandblast
 - Updated admin wands, added developer wand.
 - Added SuperGather (old-school Gather), PhaseBackup - admin/dev spells, SuperPhase
 - Add Town support to Recall (Towny)
 - Add CreatePlot action, for automatic Towny plot generation
 - Fix XP-based shops
 - Update to work against latest PreciousStones
 - Added "undo_speed" parameter, many spells that break blocks no undo slowly instead of all at once.
 - Spells respect PreciousStones protect_mob/villager/animal flags
 - Update Shuriken to be a bouncy custom projectile
 - Add "sp:amount" format for giving SP as items, e.g. "/mgive sp:100"

## 5.3.2

 - Add sound FX on SP earn.
 - Fix GUI actions deactivating the wand (e.g. Recall, brush selector)
 - Add support for selling multiples in shops. Kind of hacky right now, like "carrot_item@64: 100"
 - Add spell-overrides region flag, for region-specific spell tweaks (e.g. broom max height)
 - Add mob protection to Towny integration
 - Added "console" parameter and @p, @pn etc replacement to Recall commands
 - Fixed issues with wands feeding mana into enchanting tables when enchanting other items

## 5.3.1

 - Fix long-standing occasional undo issue
   - With thanks to the grief-o-tron 9000: https://www.youtube.com/watch?v=XBCF5rFUszA
 - Improved physics handler (to keep Bubble from popping, mainly)
 - Add skill point system.
   - By default, SP's are displayed above the mana bar while holding a wand (see: config.yml, sp_display)
   - Some spells reward SP's for successful spell casts
   - SP's can be spent to purchase new spells (The spellshop now works with SP by default)
   - Players can access the spell shop and upgrades GUIs via an enchantment table
   - When purchasing a spell from a spell shop, player's wands are upgraded a little
 - Add alternate_up_parameters, alternate_down_parameters and alternate_sneak_parameters.
 - Store player EXP and level in backup data, in case of server crash with active wand
 - Changed /magic commands to /mage:
   - debug, check, configure, describe
   - add "delete" command, for completely resetting a player data (does not affect wand items!)
 - Add default messaging for shops and shop-related actions
 - Add "apply_to_wand" and "require_template" parameters for shops, so you can require wands/paths without upgrades
 - Disabled combining wands on an anvil by default (use enable_combining: true in config.yml to change back)
 - Add option to only apply "magic" damage to other players, never to entities (use magic_entity_damage: false)
 - Add "Test Dummy" spell, summons some poor villagers for spell testing purposes.
 - Add Magic.wand.use.protected and Magic.wand.use.powered permission nodes, true by default
 - Fixed some very specific item-drop exploit issues with attached blocks
 - Fix wand dupe issues with inventory mode and armor stands with arms
 - Add "destructible" and "reflective" custom flags, for region-specific reflect/destruct behavior
 - Add enable_libsdisguises config option, to turn off LibsDisguise integration

## 5.3

 - Add "fxdemo" spell, to demo all of the EffectLib FX (well, most of them)
 - Converted spells to projectiles:
   - Magic Missile
   - Curse
   - Silence
   - Torture
   - Kill
   - Boom
   - Frost
   - Disarm
   - Fire
   - Bubble
   - Blob
   - Disintegrate
   - Blind
   - Petrify
   - Poison
 - Added Homing Missile spell
 - Added Sticky Bomb spell
 - Added Whip spell
 - Add hidden "Projectile" spell for testing CustomProjectile actions
 - Implemented break mechanics for projectiles (Reflect, Shell, Blob, etc)
 - Spells and projectiles now hit (and reflect off of) the actual block intersection location
 - Allow @p and @t as simple CommandAction parameters (same as @pn and @tn)
 - Fixed some performance and timing issues with the scheduler
 - Added tab-completion to /wand override command
 - Added Cancel action, for cancelling a target's in-progress spell.

## 5.2.2

 - Add CustomProjectile action, re-work Kill spell and Blaster item
 - Add /wand save command, for saving an in-game wand back to a template configuration
 - Add "particle_range" config option, increase visible particle range by default
 - Add "wand_self_destruct_key", for self-destructing old wands after a reset
 - Update bending configs
   - Add Air Scooter, Breath of Wind, Air Bomb, Enhanced Speed
   - Add Water Whip, Ice Shield
   - Add Earth Line, Compression
   - Update powers to canon names and descriptions
 - Add support for EffectLib variables (see: frost)
 - Add "targetable" parameter for a targetable block type list
 - Undo action "target_self" parameter changed to "target_caster"
 - Fix /mgive blink|2 (giving spell-level items)


## 5.2.1

 - The Magic.commands.mskills permission (for /mskills) now defaults to op-only
 - Added several new tweakshop actions to configure quick-cast and drop/right-click modes
 - Add Cursed Meat item (use it if you dare dare dare dare!)
 - Add Debugger spell, for debugging spell issues
 - Fix/Restore previous /cast command behavior with cooldowns
 - Add mana_display: none option, to turn off the XP mana display
 - Add skills_use_permissions option, to use the /mskills menu with Magic spells
 - Add /magic list entities command, for showing entity bounding boxes and HP by type
 - Tweak entity hitboxes a bit, should be a little easier to hit now
 - Add new spell (currently unobtainable), "One-Way Ticket to Boomstown"
 - Fix duplicate spell icon issue with chest mode

## 5.2

 - Several fixes/changes to wand tracking. Hoping this fixes a slew of wand dupe/glitch bugs.
 - Create pluggable player data system. Currently just an API, eventually may allow for DB storage.
 - Add global cooldown (Exhaustion) option, apply to Kill spell
 - Add ApplyCooldown action, resurrect Silence spell
 - Confusion no longer prevents spell casting (by default, use bypass_confusion: false if you want to switch it back)
 - Add some new sound effects
 - Add Cure Level 3, to cure Silence
 - Separate console/command-block reductions, console casts no longer have cooldowns by default
 - Add Wave and Shuriken spells
 - Add "Quick Cast" option to spells, apply to Sunny Day and Night spells
 - Add support for upgrades that change wand templates and icon behavior

## 5.1.9

 - Changed icon for architect wand, you may need to tweak existing ones with
   /wand configure icon gold_pickaxe
 - Changed icon for invisibility cloak back to ender eye
 - Fixed PVP checks with GriefPrevention integration
 - Add Blaster item, very WIP

## 5.1.8

 - Add Nuke Level 2 (Breaks all block types, larger radius)
 - Add Smite spell (Lightning effect + Blast + Ignite/Damage)
 - Fix Recall using ad-hoc warps via "/cast recall warp <warpname>"
 - GriefPrevention integration now checks for PVP not being allowed in player claims
 - Disarm will undo if you change items (but not if you move them around)
 - Add "rage" sword, gathers mana via damage
 - Add "requires" parameter for NPC trait, for item cost requirements
 - Add per-recipe crafting permissions (e.g. Magic.craft.broomstick)
 - Add Vault-based economy costs to spells ("currency" in costs)

## 5.1.7

 - NOTE: Reflect will need "axis: z" added to its parameters if you've got your own configs
 - Add Tendril and Ghast spells (master level)
 - Fix Sandblast
 - Tweak Nuke and Tornado
 - Add auto-undo to Disarm spell
 - Add LibsDisguise integration, most spells cannot be cast while disguised.
   This can be controlled via the "allow_disguised" parameter on spells.
 - Fix/Tweak shapes of Shell and Walls
 - Add max_enchant_count option for wands, see "starter" wand
 - Add skeleton_type parameter to SpawnEntity action
 - Add "ghast" spell, mainly for playing with the SpawnEntity action
 - Add arbitrary rotation for constructions, used in new "Tendril" spell
 - Spells won't target armor stands (use target_armor_stand: true to change this in a spell's config)
 - Wands can have colored names with & codes

## 5.1.6

 - Fix falling block velocity
 - Add much more detail to /help for Magic commands
 - Levitate will auto-deactivate upon landing
 - Add cooldown/re-cast timers to hotbar
 - Add item requirement option for the enchantwand NPC trait
 - Some changes to enchanting to allow for upgrades that don't always give a new spell

## 5.1.5

 - Fix "upgrades" spell, NPE on creating spell items outside of wands
 - Fix mob spawner dupe issue with SilkSpawners
 - Fix a spell dupe bug/exploit with creative mode

## 5.1.4

 - Update to latest Citizens. This may not be backwards compatible, but should
   work with recent Citizens versions.
 - Fix very short range of custom sound effects
 - Fix NPC "caster" parameter
 - Fix rate of active spell costs
 - Add BlockPhysics plugin integration (WIP)

## 5.1.3

 - Fix horrible recursion error on physical damage with a wand
 - Check for schematics in the Magic/schematics folder.
 - Add "/magic list schematics" command
 - Fall back to chest mode when in creative
 - Fix yet another item-frame-related dupe exploit
 - Some more chest mode fixes

## 5.1.2

 - Fix Rocket Boots getting you kicked for flying
 - Several performance and memory optimizations
 - Proper rollback and replication of armor stands
 - Fix projectile hit FX
 - Some fixes for wand melee damage and short-range spell casting
 - Fix the Wolf House and other single-use items that build things
 - Brooms now crash properly in water instead of leaving you in a weird state
 - Fixed/Improved Lockette integration

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
