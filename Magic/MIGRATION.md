# Migration Notes

# 10.0

 - Please see change notes video for important update information: https://youtu.be/Xi6TpnBDgYU

 - Wand control defaults changed to new cycle mode to support GUI and ease the learning curve
   Use `/mconfig config wand_slots.controls.default_slotted controls_inventory` to put the default back to the old mode.

 - Crafting recipes changed again, magic heart is gone.
   Use `/mexample add magicheart` to bring it back.

 - All permission nodes changed to lowercase (magic.) GMs don't seem to care, but let me know if this causes trouble for you!

 - Changed the meaning of the `locked` tag on wands, to match the behavior of items, classes and recipes
   Removed this tag from all builtin wands
   The "locked" properyy now makes a wand that can not be used unless the player has a class or modifier that allows it
   Use "modifiable: false" for the same behavior as "locked: true" used to have
   If this is a big issue to update, use `/mconfig config old_wand_locked_behavior true`
   - The `locked_allow_upgrades` option was removed from wands. Please transition to the slotted upgrade system
     or match up your wand/upgrades via paths
   - The `magic.wand.override_locked` permission was changed to `magic.wand.override_modifiable`
   - The `console_bypass_locked_wands` parameter was changed to `console_bypass_modifiable`
     but should use the old name if present in your configs

 - `6` and `7` examples have been removed. If you still need them, please download the configs
   and place them in your `plugins/Magic` folder.
   https://github.com/elBukkit/MagicPlugin/tree/5f3942e89deb758ad0df1fd1bafe53cb2ff8fc30/Magic/src/main/resources/examples/6
   https://github.com/elBukkit/MagicPlugin/tree/a6004374fd857b62e0f83c02c3be8f7c7a6ba015/Magic/src/main/resources/examples/7

 - Wand controls menu has changed to a slotted system. If you have custom wands with the "tweakable" tag and you'd
   still like to be able to change their controls, add `slots: controls,gui,spellmode,swappable` to the wand config.

 - Arena start/end commands delimiter changed to ; (from ,)

 - Wands no longer inherit certain config sections like "attributes" from their classes, nor classes from their parents.
 
   This was never intended behavior, I hope this does not mess with anyone's configs but it had to be fixed.

   If you want a wand to use certain class parameters, adjust the `storage` property (see `base` class)
   If you want a class to inherit from its parent, use `inherit: ` (as well as `parent: `)

 - The `base` class now defines mana as coming specifically from `subclass`. If you have a setup
   with mana shared between classes and inheriting from `base`, you may need to adjust the `storage`
   section to get mana properties from `class`.

 - Setting `url_icons_enabled: true` will now force all spell icons to use url skulls, even if it has no url. A
   default icon will be used. This replaces the old behavior of falling back to an item icon.
   This behavior can be changed by `/meditor icon default` and setting `url: ""`.
   
 - The way magic modifiers are saved was changed, any long-running or permanent (via `/mage modifier add`) modifiers
   will need to be re-applied. I apologize for any inconvenience this causes, let me know if it's a big problem for you.
   
 - Changed `mage_update_frequency` default value to 1, for smoother GUI animations
   If you think this will have a performance impact on your server, use 
   `/mconfig config mage_update_frequency 4`
   to change it back
      
 - If using some builtin configs but disabled, configs must now be explicitly re-enabled with `enabled: true`
   I am hoping this doesn't affect anyone, the implicit re-enabling was a confusing and hard-to-maintain edge case.

# 9.4

 - The `beginner` wand has been made into a demo wand and no longer turns into `wand`
   If you have been using this wand as a `welcome_wand` or in a kit, please change it to `wand`

 - `Automata` have been re-branded as `Magic Blocks`. This had a lot of trickle-down effects,
   actions changed, world parameters, the `/mauto` command is now `/mblock`. Everything *should* continue
   to work without updates, but you may get warnings if you are using the now-deprecated features. 

# 9.3

 - Removed `sp_display` global config option. 
   Wand SP and other displays are now configured on the wand using level_display and xp_display

# 9.0

 - Persistent mobs now have separate `spawn` and `respawn` triggers. You may need to 
   listen for both if you have been relying on `spawn` to trigger when a persistent mob reloads.

# 8.6

 - Builtin placeholder replacement was optimized.
   This may have changed behavior slightly, in particular if you have replacements up against text such as "$target_manaMP" 
   you may need to change it to "$target_mana$MP" (enclose the placeholder)

# 8.5

 - Mana and hotbar count are now read from paths directly and not stored on classes.
   If you have a setup where hotbars can be added or mana can be increased, you will need to
   revert this change by using `/mconfig configure path default allow_property_overrides false`
 - The lowres, painterly and slimefun examples no longer implicity include survival.
   If you were using one of these examples you may need to add the survival example back
   in, e.g. `/mconfig example set survival painterly`
 - Currencies were refactored:
   sp, xp and "currency" (item-based currency) configuration is now in a builtin_currency section
   sp localization is now in the currency section with the rest
 - EntityProjectile and ArmorStandProjectile no longer teleport by default, since this can be glitchy in Paper
   for some entity types. Add `teleport: true` to your spell parameters if you need this behavior back.

# 8.4

 - The API for interfaces between EffectContext and CastContext was restructured.
   This will mainly effect plugins that implement custom RequirementsProcessor classes, but as far as I know no one
   has been using that. I'm hoping this API change doens't break anyone, I really try not to ever change the API.
 - Removed the destructible2 and destructible3 lists, most spells break all solid blocks by default.
   The original destructible list can be found in git if you want it back
 - The message key $damage should be replaced by @damage_dealt (only used in magic missile in survival configs)

# 8.3

 - The builtin economy was rebalanced, prices of items may have changed.
   There is no "revert" example to undo this one, but contact me if you need the old prices.
 - Colored leather armor *must* use a # in the color code to differentiate between damage, e.g.
   `/mgive leather_chestplate:#002233`
 - Wands now must have an explicit "craftable: true" parameter to be allowed as a crafting ingredient
 - Magic explosions (boom, grenade, etc) will now check block destructibility per-block

# 8.2

 - Projectile "start" actions have been renamed to "projectile" to avoid conflict with the "start" distance parameter.
   This was used, for instance, in the rocket spell. Please change start to launch if you were using these.
 - The Magic.create.* permissions is now given by default. This means if you have /mgive permission, you can use it
   to create any item. This permission can be negated to go back to the previous behavior.
 - The crafting recipe parameter "substitue" was mis-spelled and has been fixed to "substitute". I'm guessing no one
   has ever used this.

# 8.1

 - There was an off-by-one error in the Skip action. I'm hoping this won't have a big impact, but if this has
   messed up the timing of your spells I apologize! You will need to increase the `skip` parameter by one, the
   action was skipping once more than it should have been.
 - PlayEffects and PlaySound no longer give cast credit. Use `effects_count_as_cast: true` to change this back
 - Added "active_wand" target to ModifyProperties, won't switch wands by default
 - The following wand properties were removed, and had been broken for a while- use `potion_effects` instead:
   health_regeneration, hunger_regeneration, haste

# 8.0

 - If you would like to upgrade, but keep things mostly the same as in previous versions of Magic, add
   `example: 7`
   to your plugins/Magic/config.yml file.
   If you're already using an example, add `7` to your `examples` list instead.

 - Default survival crafting recipes changed dramatically
 - Resource pack sending is now off by default. Set this in config.yml to re-enable:
   resource_pack_default_auto: true
 - All players' resource pack preferences will be reset and stick to the defaults until they use the /getrp command

 - There is a new resource pack structure that uses custom model data instead of damage values.
   - If you need to use a resource pack with damage values but are using the builtin configs, you can
     add `legacy_icons_enabled: true` to your config.yml
   - If you have been using Magic's RP combined with your own, I will try to provide a script to combine
     it with the new RP instead if you want to move forward
 - The Magic Sword recipe name has changed from `battle` to `magicsword`.
   This is important if you've disabled the recipe, please update your crafting.yml!

 - The `allow_passthrough` parameter of the Teleport action is now false by default. You may need to tweak any
   custom spell configs using that action accordingly.

 - Wands with interval casts are no longer cost-free, set cast_interval_cost_free: true to put this back.
 - Removed `add_uses` path parameter. You can use `uses` to add uses to a wand via a path.

 - Disabled spell inventory quick-casting (using the drop button) by default.
   You can turn this back on with `allow_inventory_casting: true` but it is not recommended on modern MC versions,
   since you'll also get a cast of the main spell.

 - Configuration files will now be read out of `plugins/Magic/examples`, please take care if you have files in that
   folder for some reason.

# 7.9.7

 - Engineering magic has been moved out of the survival configs and into a separate example. 
   Put this in config.yml to bring it back, it will now be available to players who have 
   completed the master path:
   
   examples: engineering
   
# 7.9.6

 - Automata have been moved out of the survival configs and into a separate example. Put this in config.yml to bring them back:
   
   add_examples: 
   - automata 

## 7.9.5

 - Action parameters no longer inherit from parent actions to child actions
   This means that if you have a CheckBlock action with "invert: true".
   which has another CheckBlock in its "actions" list, the second CheckBlock
   will not be inverted.
   I think this was confusing behavior that I hope no one was relying on.
  
## 7.9.3

 - Vanilla attributes on wand migration removed
   Look for this message in your logs:
   "You have vanilla item attributes in the 'attributes' property of wand template"
   If you see this, please update your wand configs by changing it to "item_attributes"!
 - Rank up broadcast disabled by default. See here for how to turn back on: https://github.com/elBukkit/MagicPlugin/blob/master/Magic/src/main/resources/defaults/paths/default.yml

## 7.9.1

 - RecallSpell was removed, use RecallAction instead.
 - Take note of all the deprecated spells you're using, they will show in logs in startup and will be removed in future versions!

## 7.6.9

 - Action parameters embedded in the "actions" tree no longer inherit down to child actions

## 7.6

 - If using an example witha  custom RP, such as skulls or war, you may need to specify the version 7 RP
   link in your config.yml, unless you've updated to 1.13

## 7.5
 
 - Now requires Java 8
 - Make sure you do not have any material sets defined in materials.yml that contain a material of the same name as the set.
 - MusicSpell deleted, use MusicAction instead
 - BlockPhysics integration removed
 - sp_multiplier wand/class property changed to earn_multiplier
 - All buy and sell shops converted to use the new Shop action. 
   If you have customized shops and are having issues, you may need to update configs or just change it back 
   to ItemShop, but I'm hoping everything translates seamlessly.
   If you have copied the "ollivander" spell you may need to modify it, "worth_item" is not supported by the new Shop action.
   If you have used SpellShop for single-spell or other type of shops you will need to change to Shop.
 
## 7.4

 - The Retarget action now uses hitbox targeting by default. Use "hitbox: false" to turn this off.
 - API change: getProperty(String, Class) no longer returns an Optional
 - The wands named "beginner", "student", "apprentice" and "master" no longer have any progression.
   They should really not be used except for demonstration purposes, and are now labelled as such.
 - The Magic.wand.override_drop permission changed to Magic.wand.override_stash
 - The wand property "attributes" has been renamed to "item_attributes"
 - The mob property "attributes" has been renamed to "entity_attributes"
 - The following entries in messages.yml have changed, $value needs to be replace with $percent as below:
   - mana_boost: "&eMana Boost: $percent%"
   - mana_regeneration_boost: "&eMana Regeneration Boost: $percent%"
   - sp_multiplier: "&6SP Earn Boost: $percent%"
 - EntityProjectile projectile_effects changed to projectile_potion_effects to avoid overlap
 - The /mskills command now requires the skills spell to be enabled in your spell configs

## 7.3

 - Spell upgrade prefix made global. If you have customized upgrade descriptions and would like to keep them unique, set
   spell.upgrade_description_prefix to "" in messages.yml

## 7.2

 - Default spell used for enchant table changed from spellshop to progressmenu
 - Wand protection properties have changed, they are now grouped into a "protection" map.
   Properties will migrate initially, but if you have anything (paths, commands, etc) that modifies or
   upgrades wand protection, you will need to adjust it accordingly!
   - protection -> protection.overall
   - protection_physical -> protection.physical
   - protection_projectiles -> protection.projectile
   - protection_falling -> protection.fall
   - protection_fire -> protection.fire
   - protection_explosions -> protection.explosion
 - The format for giving protection, cost_reduction and power via path upgrades has changed. They now need to be
   enclosed in a "properties" block. Ask for help if you need it!
 - The format for max damage reduction based on protection in config.yml has changed to allow for custom damage types.

## 7.1

 - Removed "wand" from potter configs, please use "potterwand" instead.

## 7.0
### Restoring Previous Behavior
 - You may use "example: 6" in config.yml to undo the following config changes:
   - Skill Points renamed to Spell Points
   - Cast messages go to chat instead of action bar
   - Wands can't be put in chests
   - Default wand configs will use old behavior for actions and flags (per below)
   - Wands aren't class-based and store data as normal

## 6.9.22

 - The ModifyMana action now acts on the target, not just the caster. This may mean your spell configs need changing
   if you are using this action, it may require "target: self" or a ChangeContext action.

## 6.9.12

 - Removed deprecated "mode_drop" wand property. See mode_drop in wands.defaults.yml to see how to reproduce this behavior

## 6.9.9

 - Wands can now be put in chests or dropped by dragging them out of the inventory. Set "wand_undroppable: true" in config.yml to change back.
 - The GiveItem action now needs to have "target: self" added if you want the item given to the player, see giveitem spell

## 6.9.3

 - The "stars" config was changed, all force powers now have different names from their survival counterpart.
   If this has broken you in a bad way, you can copy the configs from here to revert back:
   https://github.com/elBukkit/MagicPlugin/tree/ca8e853b5f5d6bef3b6a6d050080177cc351f47f/Magic/src/main/resources/examples/stars

 - Wand lore colors moved to messages config, you may need to update any customizations if you want to keep the colors

## 6.9

 - Lightsabers, Blaster and Thermal Detonator moved to stars configs, and removed from default configs. 
   Use "example: stars" for just the Star Wars configs
   Use the following if you want to continue to mix the default survival configs and stars:
```
add_examples:
- stars
```

 - The default potter wand was changed to "potterwand". "wand" will still work for now, but I'd like to remove it eventually.
   - Use "potterwand" to give a wand directly
   - Use "wandbox" to give a wand box that, when opened, will produce a wand

## 6.6

 - Wand creation permissions have changed, no negation required anymore:
   - Magic.create.[wand] : Permission to create a specific wand
   - Magic.create.* : Permission to create any wand, ops have this by default
   - Magic.commands.wand : Permission to use the /wand command to create wands
 - Enchanting configs renamed to "paths". 
   - Old enchanting.yml file will be renamed (one-time migration)
   - enchanting folder no longer used, please move files by hand if you have any in there.
   - load_default_enchanting changed to load_default_paths
 - New properties (you may want to set on your wands if you have completely custom configs):
   - unique: if true, wand will always have a unique id (in case you need it for API integration, or want to avoid stacking)
   - track: if true, wand will be tracked when dropped on the ground
   - immortal: if true, dropped wands will not be allowed to despawn
   - invulnerable: if true, dropped wands cannot be destroyed
 - The Magic.commands.wand.create permission is now used for the /wand create command, not required for /wand
   
## 6.5
 
 - Wands now default to no actions if none are specified in the configs. You may need to add the following to your custom wands:
     - left_click: cast
     - drop: toggle (or right_click: toggle, as you prefer)
     - mode: inventory
     - swap: cycle_hotbar
 
## 6.1

 - The default wand was changed to "wand", configurable via config.yml

## 6.0

 - If you are using the official Magic Resource Pack, clear out the resource-pack entry in your server.properties.
   Magic will automatically use the correct RP version.

## 5.5

 - Glass is no longer transparent by default (spells won't shoot through glass, too exploitable!)
   This can be modified by copying the "transparent" list into your materials.yml and adding glass, etc back in.
 - The price of SP, XP, Vault currency and Emeralds are now equivalent by default. This can be tweaked via config.yml.

## 5.4

 - Spell worth values are now defined in SP. There is a "worth_sp" parameter in config.yml that is used to scale
   them back up to currency prices, this defaults to 10.
 - The spell shop now defaults to working with skill points. To disable this, set "use_sp: false" on the spellshop in spells.yml.
 - Wands now use a Skill Point system for progression. This replaces the enchanting system.
   To switch this back, set "sp_enabled: false" in config.yml.
 - Disabled combining wands on an anvil by default (use enable_combining: true in config.yml to change back)
 - Projectile, TNT, ThrowItem, ThrowBlock and Firework all track their projectiles now.
   This means the actions won't complete until the projectile hits or expires, this may 
   impact existing spells that use these actions coupled with Repeat.
 - Crafting once again gives you the "default" wand, not "beginner". Default wand changed to
   include the Magic Missile spell.
 - Wand crafting recipe changed from "beginner" to "wand"
 - Updated PreciousStones integration to latest version (unfortunately NOT backwards compatible!)

## 5.2.2

 - Undo action "target_self" parameter changed to "target_caster"
 - Range parameter now defaults to zero. Target type defaults to "none" unless range is set.
 - The target_self flag has been generalized, defaults to true if target type is "self".
   - May affect Hat-based spells, add "target_self: true" if necessary.

## 5.2.0

 - The /cast command now bypasses cooldowns. If you don't want this, set "cast_console_cooldown_reduction: 0" in config.yml!
   (This was undone in the next version- I'm still looking for a good way to change this)
   
## 5.1.9

 - Changed icon for architect wand, you may need to tweak existing ones with
   /wand configure icon gold_pickaxe

## 5.1

 - Remove the "elder" wand from survival configs.
   - You can copy it back in if this is a problem for you
   - It conflicted with the potter elder wand in combined configurations

## 4.9.7

 * Build permissions for spells have been split into build vs break.
   This mainly affects Towny integration, but if you are using the bypass_build
   config option, or the bypass_build permission you may need to include bypass_break
   for the same effect.

## 4.9

 * Haste, health regen and hunger regen were removed in place of potion effects.
 * You may have (Admin, Wolf) wands that made you invulnerable but no longer do.
   You'll need to get a new one, or do "/wand configure protected true"

## 4.8.5

 * The worth_items list in config.yml has changed to "currency", and the format is different.
   If you have a customized physical economy, please take note!

## 4.8

Spell shops were re-balanced. You can now tweak the "worth_base" value in config.yml to scale
spell prices up or down.

Spell shops now default to an emerald-based economy (rather than XP) if Vault is not found.
This is configurable via worth_items in config.yml.

## 4.6

4.6 is a major change under the hood, the biggest since 4.0. I've tried to keep everything
mostly the same, but there are some changes to be aware of.

### General updates

 * MagicWorlds will need to be updated to 1.2
 * Changed configuration options related to construction interval / max blocks
 * The default restricted material list is much less restrictive. Building with TNT, emeralds, ore, etc is now allowed.
 * Updated and fixed dtlTraders integration.
   - You will need to fix your spell and wand shops- sorry
   - You may need to disable brush/spell/wand glow until an issue in dtlTraders is resolved

### Custom spell configurations

If you have a custom spell configuration with "actions:" in it, you may need to update:

 * Iterate action renamed to Line
 * ReplaceMaterial action renamed to ModifyBlock
 * CoverAction split into Disc and Cover
   * To reproduce previous behavior, change "Cover" to "Disc" and add a "Cover" under it
   * Disc builds a Disc, Cover searches for the topmost block, up or down, on one block

### API

Just in case anyone is actually using the API:

 * Changed API cast() methods to return success/failure

## 4.5

 * The "undo" spell changed its key to "rollback" to match the spell's name.

## 4.4

 * FlowerSpell flower lists moved to parameters section
 * Multi-target base spells no longer supported, must use ConeOfEffectAction (see: laser, push)
 * API Change: Spell.playEffects changed to use a CastContext
 * Engineering brush selection has changed- use "brush_mode: inventory" for old behavior, or hook up brushselect

## 4.0

 * Many default spells migrated to the new "action" system.
 * Default configs changed drastically, all auto-undo.
 * Enchanting path options changed from max_xp_max and max_xp_regeneration to max_mana and max_mana_regeneration
 * Commands in command spell parameters have changed slightly.
   (@t -> @tn, @p -> @pn for target and player names)

## 3.9.4

 * All of the particle effect names have changed (internal EffectLib change).
   There is a PHP script available to help migrate custom configs.
 * WorldGuard 6 or higher is now required for region support!

## 3.8

 * The "enable_creative_mode_ejecting" in config.yml now controls the creative mode special
   behavior. If you have "enable_custom_item_hacks" set to false, you probably want to set
   enable_creative_mode_ejecting to false also.

## 3.7

 * Some magic items (spells, upgrades) may need to be updated. Sorry!
 * The "xp" casting cost has changed to "mana" ("xp" is still valid, but consumes player XP)
   * If you have a completely custom spells.yml, search are replace "xp:" for "mana:" to fix.

## 3.6

 * Drop support for Cratbukkit 1.6 - 1.7.2 is now the minimum required version
 * Update to EffectLib 2.0 - all effect class names have changed

## 3.5

 * The configuration option "wand_enchantable_item" is now off by default. This means
   that wands won't change to a wooden sword when enchanting. If you are using a pre-1.7.9
   version of Bukkit, you may (or may not) need to put this option back in config.yml

## 3.4

 * The particle effect names have changed to the EffectLib versions is all places:
 
 https://github.com/Slikey/EffectLib/blob/master/src/main/java/de/slikey/effectlib/util/ParticleEffect.java

## 3.3

 * Enchanting is now customizable via multiple enchanting paths. If you had previously modified the "random"
   template in wands.yml, you will need to transfer your work to enchanting.yml.
   There are also now multiple different enchanting paths, review wands.defaults.yml,
   you will need to override "path" for wands if you want them to continue to all use
   the same enchanting path (now called "master").

 * Crafting is now customizable, there are several new recipes apart from the old
   blaze rod + nether star default. Please review crafting.defaults.yml and adjust
   accordingly, especially if you have a customized crafting configuration.


## 3.2

 * FamiliarSpell has been modified, you will need to update any customized
   configurations of Mob, Monster, Familiar or Farm. The spell now supports
   custom weighted probability lists for mob spawns.

## 3.1

* I removed the swapping out of enchantable items by default, custom enchanting works in recent CB builds. Change "wand_item_enchantable" in
  config.yml back if you still need this functionality.

* The auto_undo configuration option is deprecated, now that nearly all spells undo it will produce strange behavior. You
  will need to add "undo" to individual spells if you want auto-undo behavior .. sorry!

## 3.0

* I removed the legacy player data migration. If you still have old playername-based player data, you will need to
  use 2.9.9 to migrate, and the player must log in. Player data has been UUID-based since 2.9.0 so hopefully you're all migrated by now.

* I changed more spell parameters, mostly the material/brush related ones. Sorry, trying to get this settled for 3.0.

* I made the wood_hoe the default wand item, instead of blaze_rod. You can change this back in config.yml if you wish, however this
  was made to support the official Magic resource pack, which are free to use- simply add the following to your server.properties file:

```  
resource-pack=http://rp.elmakers.com/Magic-RP.zip
```

## 2.9.9

* I removed the block populator. It will be part of a separate plugin, MagicWorlds: http://dev.bukkit.org/bukkit-plugins/magicworlds/

* I have renamed many of the default spell parameters, if you've been scripting, plesae check SPELLS.md: https://github.com/elBukkit/MagicPlugin/blob/master/SPELLS.md

* I broke any Wolf House's you may have made already, sorry! (Related to the above- you can fix it by re-casting at the center of your control booth)

* I removed many of the default wands, trying to de-clutter. If you were using any of them or want them back,
simply copy+paste from here into your wands.yml file (in plugins/Magic):

https://raw.githubusercontent.com/elBukkit/MagicPlugin/330f6b0e3721471bc6e101e97109a695d17e09dd/src/main/resources/defaults/wands.defaults.yml

* I changed the "modifiable" flag in wand data to "locked" (and inverted it). If you had any un-modifiable wands in your worlds, they will
now be modifiable sorry about that, please let me know if it's a concern (I'm assuming it's not) and I can add in some migration code.

I don't think anyone was using locked wands, hopefully not.

* I renamed the bending wands- *TAKE CARE* if you were giving out the "air" "water" "earth" or "fire" wands- those are now
the Master wands, not the Student!

## Some general notes

At this point I will try and ensure that migration is generally hassle-free, and plugin updates are
drop-in. I am hesitant to integrate any kind of auto-updater, but if you wanted to automate pulling
from the latest devbukkit or DEV build, I will try to make sure that won't cause problems.

To be clear, *to make migration easy* going forward, generally do NOT copy and paste from the defaults
into your custom configuration file.

Proper customization involves only putting *what you want to change* into your spells.yml, wands.yml config.yml
or messages.yml (etc) files. The files in defaults/ are always loaded first, and what you've put in the
customized configuration files overrides them.

For instance, if you want to give "fireball" a 10-second cooldown, you would put the following in plugins/Magic/spells.yml

```
fireball:
    parameters:
        cooldown: 10000
```

Note that all duration values will be in *milliseconds* (not seconds or ticks).

If you copy and paste the entire defaults file (or even sections of it, like the definition of a whole
spell), migration may get trickier. I will sometimes change the class a spell uses, fix effects, tweak
icons, or something else that you probably don't really want to tweak. 
If you've overridden the defaults, my changes won't have
any effect on your server, and that spell might break (or not get fixed) on update.

Going forward I will assume that you are following this rule when customizing. I'm not going to mention
every tweak I make to the default config files, because if you haven't overridden it, it shouldn't matter.

If you have overridden something, I'm going to assume it was intentional. I will try to summarize all of this
in the "admin instructions" section on devbukkit.

## 2.9.4

### spells.yml

The definitions of "Disc" and "SuperDisc" spells have changed, y_max and y_min have changed to 
orient_dimension_max and orient_dimension_min to support different orientations.

## 2.9.3

### spells.yml

The "Recurse" spell is now RecurseSpell, not FillSpell.

## 2.9.2

### config.yml

If you are using right-click-to-cycle, change the following

right_click_cycles: false

to

default_wand_mode: cycle

## 2.9.1

### spells.yml

 - ShrunkenHeadSpell changed to ShrinkSpell
 - MineSpell changed to DropSpell (and "chop" variant added)

## 2.9.0

 BIG BIG ONE:
 
 The configuration files have been split up and reorganized, and magic.yml has been renamed to config.yml.
 The defaults have moved to a separate folder, as have the data files (which you generally shouldn't edit).
 
 Specific material list and block populator configurations have been moved into separate files for clarity.
 
 Now would be a really good time to start with a clean, fresh install of Magic, and then re-apply your customizations, if any.
 
 I am going to make a big effort from here out to keep the configuration format consistent, and only have a few things planned
 before 3.0. The new additive configuration system, if used properly (only change what you need to change) will make migration
 in the future much, much easier.
 
 Also:

 Chests and signs are no longer indestructible. The undo system will save their contents (though not across a reload, yet).
 You can modify the indestructible material list in magic.yml.
 
 Additionally, player data and image map data has changed, but this should be automatically migrated on first run.
 Make sure you run some version of Magic 2.9.X before 3.0, I plan on removing the migration in 3.0. (Only really
 important if you have image maps).
 
 Detailed notes:
 
 - Potion effects (boon, curse) parameters now prefixed with "effect_"
 - Arrow, Sniper and ArrowRain spells now use ProjectileSpell
 

## 2.8.9

 BIG ONE: All config files have had their root removed. So there is no "general" in magic.yml, it's all flat.
 
 Additional note: in magic.yml the material lists (destructible, indestructible, etc) have moved under a "materials" 
 node. 
 
 This should be easy enough to fix in your customized configurations, but otherwise they will be ignored- so make
 sure you catch this on upgrade.
 
 Note that now you only need to modify what you want to have different now. So it might be a good time to review your
 custom configurations!
 
 If you want to disable a wand or spell, set the "enabled" property to "false" (instead of removing it)
 
 I'm trying to get the config formats more or less solidified by 2.9.0, and I hope the overall format won't change at all
 after official release (3.0). 

## 2.8.8

### spells.yml

 All FillSpell variants need
 
            check_destructible: false
            
 To continue to be able to fill all materials.

### magic.yml

 This config file is now additive. See the notes in magic.defaults.yml
 You only need to set what you wish to change from the default in magic.yml

## 2.8.7

### magic.yml
 
 quiet changed to show_cast_messages
 silent changed to show_messages
 message_prefix and cast_message prefix added
 "size" parameter added to IterateSpell, though the default is the same
 "effect_color" in wands.yml changed to a string

## 2.8.5

### magic.yml

 - max_power_multiplier removed, replaced with the following:
   
    max_power_construction_multiplier: 5.0
    max_power_radius_multiplier: 1.5
    max_power_range_multiplier: 3.0

## 2.8.3

### spells.yml

 - Added earth and stream spells
 - lava spell needs updating in spells.yml: LavaSpell -> IterateSpell
 - properties and parameters have been combined into "parameters". This one might be a pain, sorry!
 - Reconfigured the gills spell to be a PotionEffectSpell. 
 
## 2.8.2
 
### spells.yml
 
  - The fireball and icbm spells need updating: FireballSpell -> ProjectileSpell

## 2.8.0

### messages.yml

 - All name and description nodes from spells.yml and wands.yml should be moved to messages.yml

## 2.7.0
 
### spells.yml
 
  - Casting costs changes to a flat list of "material|xp: value" key/value paits.
  
  
  
