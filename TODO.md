# TODO

 - Glitching Fill behavior
 - Spell shop improvements:
   - Color spells player can't afford
 - Trait improvements
   - Add /parameter command for inspecting single param
   - Cleaner list of list parameters
 - Nerf Force (shift to break free? Fall protection?)
 - Make map work like replicate, clone - with start point set on activate
 - Fix blink when passing through transparent blocks
 - battle wand stuck at "enchant at higher level"
 - /magic describe should show info about current spell?
 - Fix Chop on data variants..?
 - Add repeat option for schematic brushes
 - Test Clone with entities?

 - Fix recall home spawning in the ground?
 - Fix spells getting lost on config load failure
 - Add spellbook-choosing action
 - Add general shop action (?)
 - Add Cylinder construct (SuperWalls, Walls, SuperCylinder)
 - Support for locking/unlocking chests
 - Configurable FOV weight for targeting (Survival Sense)

## Old Stuff

 - Arena spell/schematic
 - A way to /wand enchant with auto-fill but not filling wand... ?
 - Magic stats (that persist) - block modified, etc. (Statistics API?)
 - Peek wiped a chest - I couldn't repro, but saw the results from ap_bagel.
 - Collapse blocklist chains on save (?)
 - Walls spell
 - Enchant count limit for wands
 - Support schematic entity import for paintings and item frames (at least)
 - EnderDragon familiars that don't do block damage or spawn a portal on death?
 - Async player data loading/saving
 - Add upgrade paths for wands, maybe a "combine" requirement of one or more wands
 
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
 - vortex type spells.. singularity, black hole, tornado - fun ideas there.
 - Biome modification (mega-frost).
 - Decorate, places paintings at target

## OTHER STUFF
 
 - Customize map wand pop-ups? Red with black shadows looked cool.. use wand effect color?
= - Make volley multi-sample?
 - Alter names sheep "jeb_", - others "Dinnerbone" ?
 
 - Fix up alter spell, remove id-based lists
 
 - Need separate activate/deactivate costs. Fill vs levitate :\
   - Variable costs would be nice, too- for fill and superconstruct.
 - prevent pillar from passing through non-air blocks of different materials than the target
 - If possible, label more material data like stair direction.
 - Add locale option to suffix messages.yml
 
 - Show active spells in a different color
 
 - Add /magic depopulate, to remove all wands from chests

  - Persist player spell data
   - familiar
 
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

	