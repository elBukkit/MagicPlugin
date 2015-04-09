# TODO

 - Fix prices on web site
 - Recall still losing waypoints
 - Maximum enchant count for wands

 - Banners don't work from schematics
 - Support for restoring armor stands- might have to drop 1.7 support for this.
 - Clean up MaterialBrush target system, automate somehow?
 - Copy target brush action for tandem replication
 - Automata aren't cleaning up cmd block + redstone
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
 - /magic describe should show info about current spell?
 - Add repeat option for schematic brushes
 - Support for locking/unlocking chests
 - Configurable FOV weight for targeting (Survival Sense)

## Old Stuff

 - Arena spell/schematic
 - Magic stats (that persist) - block modified, etc. (Statistics API?)
 - Collapse blocklist chains on save (?)
 - Enchant count limit for wands
 - EnderDragon familiars that don't do block damage or spawn a portal on death?

## NEW SPELLS

 - level - levels the ground
 - avatar - Goes into "avatar" state- free spells, super power, levitating, special effects- but rapidly decreasing mana while active.
    - will need a spell-based power/protection system, or use potion effects? 
    - would be nice if this could extend from potioneffect and levitate- maybe refactor?
    - will need a separate "active cost reduction" property
 - banish - sets a player's home and spawn?
 - Fix/finish stairs spell
 - Fix tunnel spell
 - Some kind of "ramp" version of fill, like stairs but with arbitrary blocks?
 - Disguise spells / clone spell
 - Biome modification (mega-frost).
 - Decorate, places paintings at target

## OTHER STUFF
 
 - Customize dynmap map wand pop-ups? Red with black shadows looked cool.. use wand effect color?
 - Make volley multi-sample?
 - Alter names sheep "jeb_", - others "Dinnerbone" ?
 
 - Fix up alter spell, remove id-based lists
 
 - Need separate activate/deactivate costs. Fill vs levitate :\
   - Variable costs would be nice, too- for fill and superconstruct.
 - prevent pillar from passing through non-air blocks of different materials than the target
 - If possible, label more material data like stair direction.
 - Add locale option to suffix messages.yml
 
 - Show active spells in a different color

 - Continue work on combining wands on anvils
 
## TESTING / TWEAKING:
 
 - Test larger undo queue sizes, or count size in blocks?
 - Add console logging of massive construction spell casting

## STACK TRACES / EXTRA DEBUG INFO


## PLAYER REQUESTS:

yoheius
Feb 13, 2014 at 11:51 - 1 like Unlike
Very good Please a Integration with diablodrops Thanks

--- Contacted diablodrops dev, never heard back. Grabbed source code from github, will check it out.

Mre30
Jan 9, 2014 at 09:36 - 1 like Unlike

For sure, GlobalMarket will and does work. But its only good for player2player sales.

- DTLTraders

---

Gitpw3d
Wants scoreboard-based mana display

-------

alek123222
Can you make it compatible so when a user has over 20 health he can still be healed because at the moment the plugin just sets them back to 20 again.
I am using a custom plugin thats based of this one: http://dev.bukkit.org/bukkit-plugins/lore-attrubites-revival/ 

----

Gitpw3d
My Mistake When the wand is selected the health is set to 20(normal)
but ils messes with that becauses of the lvs the when the wand is unslected the health reverts to normal execpt needing to regen ~100-1000 hearts

Gitpw3d
Hi elMaker im using ils lore stats which increases the health you get per lv but mana being displayed messed it up, do you have a solution? other then that this plugin is great

----

diannetea
We've narrowed down the wands breaking to the "clicksort" plugin, when someone organizes their inventory it breaks and you have to create a new one.

---

Peda1996
Can you add the Feature that it is working on the Plugin shopkeepers?

-----

	