# TODO

PsychoFad - could add GriefPrevention checks in a future version

## Towny!

@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        public void onBlockPlace(BlockPlaceEvent event) {

                if (plugin.isError()) {
                        event.setCancelled(true);
                        return;
                }

                Player player = event.getPlayer();
                Block block = event.getBlock();
                WorldCoord worldCoord;

                try {
                        TownyWorld world = TownyUniverse.getDataSource().getWorld(block.getWorld().getName());
                        worldCoord = new WorldCoord(world.getName(), Coord.parseCoord(block));

                        //Get build permissions (updates if none exist)
                        boolean bBuild = PlayerCacheUtil.getCachePermission(player, block.getLocation(), block.getTypeId(), block.getData(), TownyPermission.ActionType.BUILD);

                        // Allow build if we are permitted
                        if (bBuild)
                                return;

                        /*
                        * Fetch the players cache
                        */
                        PlayerCache cache = plugin.getCache(player);
                        TownBlockStatus status = cache.getStatus();

                        /*
                        * Flag war
                        */
                        if (((status == TownBlockStatus.ENEMY) && TownyWarConfig.isAllowingAttacks()) && (event.getBlock().getType() == TownyWarConfig.getFlagBaseMaterial())) {

                                try {
                                        if (TownyWar.callAttackCellEvent(plugin, player, block, worldCoord))
                                                return;
                                } catch (TownyException e) {
                                        TownyMessaging.sendErrorMsg(player, e.getMessage());
                                }

                                event.setBuild(false);
                                event.setCancelled(true);

                        } else if (status == TownBlockStatus.WARZONE) {
                                if (!TownyWarConfig.isEditableMaterialInWarZone(block.getType())) {
                                        event.setBuild(false);
                                        event.setCancelled(true);
                                        TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_warzone_cannot_edit_material"), "build", block.getType().toString().toLowerCase()));
                                }
                                return;
                        } else {
                                event.setBuild(false);
                                event.setCancelled(true);
                        }

                        /*
                        * display any error recorded for this plot
                        */
                        if ((cache.hasBlockErrMsg()) && (event.isCancelled()))
                                TownyMessaging.sendErrorMsg(player, cache.getBlockErrMsg());

                } catch (NotRegisteredException e1) {
                        TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_not_configured"));
                        event.setCancelled(true);
                }

        }



## Transitioning

This list is being transitioned to the issue tracker:

http://jira.elmakers.com/browse/MAGIC
 
## For 3.0.0
 
 - Tweaking/Balancing - haste, cooldowns, etc
 - Finish localization
 
## To Add to JIRA

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
 
## DOCUMENTATION / DEMO STUFF

 - Thank Aireesan for NodeCraft recommendation

 - Add "wand properties" infobook, other admin-oriented books?
 - Update web site wand instructions

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
 - Add fall protection to bounce spell (generalize it)
 - Make volley multi-sample?
 - Alter names sheep "jeb_", - others "Dinnerbone" ?
 - Separate material list for "super" building materials?
 
 - Fix up alter spell, remove id-based lists
 
 - Need separate activate/deactivate costs. Fill vs levitate :\
   - Variable costs would be nice, too- for fill and superconstruct.
 - prevent pillar from passing through non-air blocks of different materials than the target
 - If possible, label more material data like stair direction.
 - Add locale option to suffix messages.yml
 
 - Show active spells in a different color
 
 - Add location to /magic populate when used in-game
 - Add /magic depopulate, to remove all wands from chests
 
 - See if you can disable the fire from a fireball when not incendiary

 - Persist player spell data
   - familiar
 
 - Fix populate command to detect a full ring of broken chunks, or some other method to get all chunks.
 - Add chunk generate and stop options to "populate" for creating square maps.. ?
 
 - Continue work on combining wands on avils
 - Specific protection for suffocation damage, underwater breathing (air regeneration)
 - Maybe make wands regenerate while you're not holding them (timer-based)?
 
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

Also cool downs seem not to work at all and also a message would be nice that when a spell is on cool down should pop up saying "Cool-down blah"
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

	