package com.elmakers.mine.bukkit.protection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.entity.TeamProvider;
import com.elmakers.mine.bukkit.api.protection.BlockBreakManager;
import com.elmakers.mine.bukkit.api.protection.BlockBuildManager;
import com.elmakers.mine.bukkit.api.protection.PVPManager;

public class FactionsManager implements BlockBuildManager, BlockBreakManager, PVPManager, TeamProvider {
    private boolean enabled = false;
    private Class<?> factionsManager = null;
    private Method factionsCanBuildMethod = null;
    private boolean methodIsBlock = false;
    private Object board = null;
    private Method getFactionAtMethod = null;
    private Method isSafeZoneMethod = null;
    private Method isNoneMethod = null;

    private Method getFlagMethod = null;

    private Constructor<?> flocationConstructor = null;
    private Method psFactoryMethod = null;

    private Class<?> playerClass;
    private Method playerGetMethod;
    private Method playerGetFactionMethod;
    private Method factionGetRelationMethod;
    private Class<?> relationClass;
    private Method relationIsFriendMethod;

    private Method relationIsAllyMethod;
    private Method relationIsMemberMethod;
    private Method relationIsTruceMethod;

    private Object fPlayers;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled && factionsManager != null;
    }

    public void initialize(Plugin plugin) {
        if (enabled) {
            Plugin factionsPlugin = plugin.getServer().getPluginManager().getPlugin("Factions");
            if (factionsPlugin != null)
            {
                try {
                    Class<?> handler = Class.forName("com.massivecraft.factions.integration.Magic");
                    if (handler != null) {
                        plugin.getLogger().info("Modern Factions found, skipping integration and letting Factions handle it. (Thanks, mbaxter!)");
                        factionsManager = null;
                        return;
                    }
                } catch (Exception ignore) {
                }

                try {
                    Class<?> psClass = Class.forName("com.massivecraft.massivecore.ps.PS");
                    factionsManager = Class.forName("com.massivecraft.factions.engine.EngineMain");
                    factionsCanBuildMethod = factionsManager.getMethod("canPlayerBuildAt", Object.class, psClass, Boolean.TYPE);
                    psFactoryMethod = psClass.getMethod("valueOf", Location.class);

                    Class<?> boardClass = Class.forName("com.massivecraft.factions.entity.BoardColl");
                    Method boardSingleton = boardClass.getMethod("get");
                    board = boardSingleton.invoke(null);
                    getFactionAtMethod = boardClass.getMethod("getFactionAt", psClass);
                    Class<?> factionClass = Class.forName("com.massivecraft.factions.entity.Faction");
                    isNoneMethod = factionClass.getMethod("isNone");

                    if (factionsManager == null || factionsCanBuildMethod == null || psFactoryMethod == null) {
                        factionsManager = null;
                        factionsCanBuildMethod = null;
                        psFactoryMethod = null;
                    }

                    try {
                        getFlagMethod = factionClass.getMethod("getFlag", String.class);
                    } catch (Exception ex) {
                        isSafeZoneMethod =  factionClass.getMethod("isSafeZone");
                    }

                    try {
                        playerClass = Class.forName("com.massivecraft.factions.entity.MPlayer");
                        playerGetMethod = playerClass.getMethod("get", Object.class);
                        playerGetFactionMethod = playerClass.getMethod("getFaction");
                        Class<?> relationParticipator = Class.forName("com.massivecraft.factions.RelationParticipator");
                        factionGetRelationMethod = factionClass.getMethod("getRelationTo", relationParticipator);
                        relationClass = Class.forName("com.massivecraft.factions.Rel");
                        relationIsFriendMethod = relationClass.getMethod("isFriend");
                    } catch (Exception ex) {
                        playerClass = null;
                        plugin.getLogger().log(Level.WARNING, "Error binding to Factions, team provider will not work", ex);
                    }

                } catch (Throwable ex) {
                    // Try for Factions "Limited"
                    psFactoryMethod = null;
                    try {
                        factionsManager = Class.forName("com.massivecraft.factions.listeners.FactionsBlockListener");
                        try {
                            factionsCanBuildMethod = factionsManager.getMethod("playerCanBuildDestroyBlock", Player.class, Location.class, String.class, Boolean.TYPE);
                        } catch (Exception notLoc) {
                            methodIsBlock = true;
                            factionsCanBuildMethod = factionsManager.getMethod("playerCanBuildDestroyBlock", Player.class, Block.class, String.class, Boolean.TYPE);
                        }
                        if (factionsManager == null || factionsCanBuildMethod == null) {
                            factionsManager = null;
                            factionsCanBuildMethod = null;
                        } else {
                            plugin.getLogger().info("Factions 1.8.2+ build found");
                        }

                        Class<?> factionClass = Class.forName("com.massivecraft.factions.Faction");
                        isSafeZoneMethod =  factionClass.getMethod("isSafeZone");
                        isNoneMethod = factionClass.getMethod("isNone");

                        Class<?> flocationClass = Class.forName("com.massivecraft.factions.FLocation");
                        flocationConstructor = flocationClass.getConstructor(Location.class);

                        Class<?> boardClass = Class.forName("com.massivecraft.factions.Board");
                        Method boardSingleton = boardClass.getMethod("getInstance");
                        board = boardSingleton.invoke(null);

                        getFactionAtMethod = boardClass.getMethod("getFactionAt", flocationClass);

                        try {
                            Class<?> fPlayersClass = Class.forName("com.massivecraft.factions.FPlayers");
                            Method fPlayersGetMethod = fPlayersClass.getMethod("getInstance");
                            fPlayers = fPlayersGetMethod.invoke(null);

                            playerClass = Class.forName("com.massivecraft.factions.FPlayer");
                            playerGetMethod = fPlayersClass.getMethod("getByPlayer", Player.class);
                            playerGetFactionMethod = playerClass.getMethod("getFaction");
                            Class<?> relationParticipator = Class.forName("com.massivecraft.factions.iface.RelationParticipator");
                            factionGetRelationMethod = factionClass.getMethod("getRelationTo", relationParticipator);
                            relationClass = Class.forName("com.massivecraft.factions.struct.Relation");
                            relationIsAllyMethod = relationClass.getMethod("isAlly");
                            relationIsTruceMethod = relationClass.getMethod("isTruce");
                            relationIsMemberMethod = relationClass.getMethod("isMember");
                        } catch (Exception ex3) {
                            playerClass = null;
                            plugin.getLogger().log(Level.WARNING, "Error binding to Factions, team provider will not work", ex3);
                        }
                    } catch (Throwable ex2) {
                        plugin.getLogger().log(Level.WARNING, "Failed to find mcore", ex);
                        plugin.getLogger().log(Level.WARNING, "Failed to find FactionsBlockListener", ex2);
                        factionsManager = null;
                        factionsCanBuildMethod = null;
                    }
                }

                if (factionsManager == null) {
                    plugin.getLogger().info("Factions integration failed.");
                } else {
                    plugin.getLogger().info("Factions found, will integrate for build, friendly fire and safe zone checks.");
                }
            } else {
                plugin.getLogger().info("Factions not found");
            }
        } else {
            plugin.getLogger().info("Factions integration disabled");
        }
    }

    @Nullable
    private Object getFLocation(Location location) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        if (flocationConstructor != null) {
            return flocationConstructor.newInstance(location);
        } else if (psFactoryMethod != null) {
            return psFactoryMethod.invoke(null, location);
        }

        return null;
    }

    @Nullable
    private Object getFactionAt(Location location) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        if (board == null || getFactionAtMethod == null) {
            return null;
        }

        Object loc = getFLocation(location);
        if (loc == null) {
            return null;
        }
        return getFactionAtMethod.invoke(board, loc);
    }

    @Override
    public boolean hasBuildPermission(Player player, Block block) {
        if (enabled && block != null && factionsManager != null && factionsCanBuildMethod != null) {

            // Check for wilderness, mobs and command blocks can only build there
            if (player == null) {
                if (isNoneMethod == null || board == null) return false;
                try {
                    Object faction = getFactionAt(block.getLocation());
                    return (faction == null || (Boolean)isNoneMethod.invoke(faction));
                } catch (Throwable ex) {
                    ex.printStackTrace();
                    return false;
                }
            }

            try {
                if (psFactoryMethod != null) {
                    Object loc = psFactoryMethod.invoke(null, block.getLocation());
                    return loc != null && (Boolean)factionsCanBuildMethod.invoke(null, player, loc, false);
                }
                Object checkObject = methodIsBlock ? block : block.getLocation();
                return (Boolean)factionsCanBuildMethod.invoke(null, player, checkObject, "destroy", true);
            } catch (Throwable ex) {
                ex.printStackTrace();
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean hasBreakPermission(Player player, Block block) {
        return hasBuildPermission(player, block);
    }

    @Override
    public boolean isPVPAllowed(Player player, Location location) {
        if (!enabled || location == null || factionsManager == null || board == null) return true;

        if (getFlagMethod != null) {
            try {
                Object faction = getFactionAt(location);
                return faction == null || (Boolean)getFlagMethod.invoke(faction, "pvp");
            } catch (Throwable ex) {
                ex.printStackTrace();
                return false;
            }
        } else if (isSafeZoneMethod != null) {
            try {
                Object faction = getFactionAt(location);
                return faction == null || !(Boolean)isSafeZoneMethod.invoke(faction);
            } catch (Throwable ex) {
                ex.printStackTrace();
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean isFriendly(Entity attacker, Entity entity) {
        if (playerGetFactionMethod == null || playerGetMethod == null || factionGetRelationMethod == null) return false;
        if (attacker instanceof Player && entity instanceof Player && playerClass != null) {
            try {
                Object player1 = playerGetMethod.invoke(fPlayers, attacker);
                Object player2 = playerGetMethod.invoke(fPlayers, entity);
                if (player1 == null || player2 == null) return false;

                Object faction1 = playerGetFactionMethod.invoke(player1);
                Object faction2 = playerGetFactionMethod.invoke(player2);
                if ((boolean)isNoneMethod.invoke(faction1) || (boolean)isNoneMethod.invoke(faction2)) {
                    return false;
                }
                Object relation = factionGetRelationMethod.invoke(faction1, faction2);
                if (relationIsFriendMethod != null) {
                    return (boolean)relationIsFriendMethod.invoke(relation);
                }

                if (relationIsMemberMethod == null || relationIsAllyMethod == null || relationIsTruceMethod == null) return false;

                if ((boolean)relationIsMemberMethod.invoke(relation)) return true;
                if ((boolean)relationIsAllyMethod.invoke(relation)) return true;
                if ((boolean)relationIsTruceMethod.invoke(relation)) return true;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }
}
