# Migration Notes

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
  
  
  