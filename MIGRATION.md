# Migration Notes

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
  
  
  