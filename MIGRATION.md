# Migration Notes

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
  
  
  