# Wand Customization

Every wand is a unique item, though they may be created from templates. The builtin templates can be found in wands.defaults.yml:

https://github.com/elBukkit/MagicPlugin/blob/master/src/main/resources/defaults/wands.defaults.yml

Any of the parameters below may be used in a template, or applied to a wand in-game via any of the following commands

/wand configure
/wand upgrade
/wandp @p configure
/wandp @p upgrade

The "upgrade" command will never lower a value on a wand, and is meant for in-game command block or other
use, to allow players to upgrade their wands, such incrase mana regeneration to a set limit, while not
affecting a wand that already has a higher xp regen.

Note (mainly to other plugin developers) that these parameters are also the NBT tags used to store wand data, under the "wand"
parent node.

## Parameters

| cooldown            | cool      | milliseconds | How long to wait between casts



| Name              	  	| Values       | Description
| active_spell				| spell name   | The currently active spell
| active_material			| material     | The currently active material brush
| xp						| integer      | Current stored xp (Mana)
| xp_regeneration			| integer      | XP (Mana) regeneration per second
| xp_max					| integer      | Maximum XP (mana)
| bound						| TODO         | 
| uses						| TODO         | 
| upgrade					| TODO         | 
| indestructible			| TODO         | 
| cost_reduction			| TODO         | 
| cooldown_reduction		| TODO         | 
| effect_bubbles			| TODO         | 
| effect_color				|  TODO         | 
| effect_particle			| TODO         | 
| effect_particle_count		| TODO         | 
| effect_particle_data		| TODO         | 
| effect_particle_interval	|  TODO         | 
| effect_sound				| TODO         | 
| effect_sound_interval		| TODO         | 
| effect_sound_pitch		| TODO         | 
| effect_sound_volume		| TODO         | 
| haste						|  TODO         | 
| health_regeneration		| TODO         | 
| hunger_regeneration		|  TODO         | 
| icon						| TODO         | 
| mode						| TODO         | 
| keep						| TODO         | 
| locked					| TODO         | 
| quiet						|  TODO         | 
| power						|  TODO         | 
| protection				| TODO         | 
| protection_physical		| TODO         | 
| protection_projectiles	|  TODO         | 
| protection_falling		| TODO         | 
| protection_fire			| TODO         | 
| protection_explosions		| TODO         | 
| materials					| TODO         | 
| spells					|  TODO         | 
| id						| TODO         | 
| owner						| TODO         | 
| name						| TODO         | 
| description				| TODO         | 
| template					| TODO         | 
| organize					| TODO         | 
| fill 						| TODO         | 

# Messages

All wand template names and descriptions are contained in messages.yml, 
though non-builtin wands may include a default name and description in the wands.yml definition.
