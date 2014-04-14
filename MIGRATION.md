# Migration Notes

## 3.0

* I removed the legacy player data migration. If you still have old playername-based player data, you will need to
  use 2.9.9 to migrate, and the player must log in. Player data has been UUID-based since 2.9.0 so hopefully you're all migrated by now.

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
  
  
  