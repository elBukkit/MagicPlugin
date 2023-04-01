package com.elmakers.mine.bukkit.protection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.Association;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.association.Associables;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.events.DisallowedPVPEvent;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;

public class WorldGuardAPI {
    // In its own class to prevent ABI problems on other versions
    private static final class AllowPvpWhenDamagingController
            implements Listener {
        @EventHandler
        public void onDisallowedPVPEvent(DisallowedPVPEvent ev) {
            // Worldguard considers it PVP when a mob damages a player that was
            // targeting another player. This means that any (magic) mob casting
            // an AOE spell, or a spell that otherwise hits a player that it was
            // not targeting triggers this event, and does not do damage by
            // default.
            //
            // Here at Magic inc. we don't that this behaviour makes sense for
            // spells, as mobs casting spells are typically highly customised,
            // and can do their own PVP/player-targeting checks. That is why we
            // will cancel this event when we are the ones that caused the
            // damage.
            if (CompatibilityLib.getCompatibilityUtils().isDamaging()) {
                ev.setCancelled(true);
            }
        }
    }

    private final Plugin owningPlugin;
    private Object worldGuard = null;
    private WorldGuardPlugin worldGuardPlugin = null;
    private WorldGuardFlags customFlags = null;
    private Object regionContainer = null;
    private Method regionContainerGetMethod = null;
    private Method createQueryMethod = null;
    private Method regionQueryTestStateMethod = null;
    private Method locationAdaptMethod = null;
    private Method worldAdaptMethod = null;
    private Method regionManagerGetMethod = null;
    private Constructor<?> vectorConstructor = null;
    private Method vectorConstructorAsAMethodBecauseWhyNot = null;
    private StateFlag buildFlag;
    private StateFlag pvpFlag;
    private StateFlag exitFlag;
    private boolean initialized = false;
    private int cacheDuration;
    private long lastCacheClear;
    private Map<Location, ApplicableRegionSet> cachedSets = new HashMap<>();

    public boolean isEnabled() {
        return worldGuardPlugin != null;
    }

    public WorldGuardAPI(Plugin plugin, Plugin owningPlugin) {
        this.owningPlugin = owningPlugin;
        if (plugin instanceof WorldGuardPlugin) {
            worldGuardPlugin = (WorldGuardPlugin)plugin;

            try {
                Class<?> worldGuardClass = Class.forName("com.sk89q.worldguard.WorldGuard");
                Method getInstanceMethod = worldGuardClass.getMethod("getInstance");
                worldGuard = getInstanceMethod.invoke(null);
                owningPlugin.getLogger().info("Found WorldGuard 7+");
            } catch (Exception ex) {
                owningPlugin.getLogger().info("Found WorldGuard <7");
            }

            try {
                owningPlugin.getLogger().info("Pre-check for WorldGuard custom flag registration");
                customFlags = new WorldGuardFlagsManager(owningPlugin, worldGuardPlugin, worldGuard);
            } catch (NoSuchMethodError incompatible) {
                owningPlugin.getLogger().log(Level.WARNING, "NOFLAGS", incompatible);
                // Ignored, will follow up in checkFlagSupport
            } catch (Throwable ex) {
                owningPlugin.getLogger().log(Level.WARNING, "Unexpected error setting up custom flags, please make sure you are on WorldGuard 6.2 or above", ex);
            }
        }
    }

    public void configurePvpListener(Plugin owningPlugin) {
        if (worldGuard == null) {
            Bukkit.getPluginManager().registerEvents(
                    new AllowPvpWhenDamagingController(),
                    owningPlugin);
        } else {
            // TODO: 7+ version?
        }
    }

    public void checkFlagSupport() {
        if (customFlags == null) {
            try {
                Plugin customFlagsPlugin = owningPlugin.getServer().getPluginManager().getPlugin("WGCustomFlags");
                if (customFlagsPlugin != null) {
                    customFlags = new WGCustomFlagsManager(customFlagsPlugin);
                }
            } catch (Throwable ex) {
                owningPlugin.getLogger().log(Level.WARNING, "Error integration with WGCustomFlags", ex);
            }

            if (customFlags != null) {
                owningPlugin.getLogger().info("WGCustomFlags found, added custom flags");
            } else {
                owningPlugin.getLogger().log(Level.WARNING, "Failed to set up custom flags, please make sure you are on WorldGuard 6.2 or above, or use the WGCustomFlags plugin");
            }
        }
    }

    protected RegionAssociable getAssociable(Player player) {
        RegionAssociable associable;
        if (player == null) {
            associable = Associables.constant(Association.NON_MEMBER);
        } else {
            associable = worldGuardPlugin.wrapPlayer(player);
        }

        return associable;
    }

    private void initialize() {
        if (!initialized) {
            initialized = true;
            // Super hacky reflection to deal with differences in WorldGuard 6 and 7+
            if (worldGuard != null) {
                try {
                    Method getPlatFormMethod = worldGuard.getClass().getMethod("getPlatform");
                    Object platform = getPlatFormMethod.invoke(worldGuard);
                    Method getRegionContainerMethod = platform.getClass().getMethod("getRegionContainer");
                    regionContainer = getRegionContainerMethod.invoke(platform);
                    createQueryMethod = regionContainer.getClass().getMethod("createQuery");
                    Class<?> worldEditLocationClass = Class.forName("com.sk89q.worldedit.util.Location");
                    Class<?> worldEditWorldClass = Class.forName("com.sk89q.worldedit.world.World");
                    Class<?> worldEditAdapterClass = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
                    worldAdaptMethod = worldEditAdapterClass.getMethod("adapt", World.class);
                    locationAdaptMethod = worldEditAdapterClass.getMethod("adapt", Location.class);
                    regionContainerGetMethod = regionContainer.getClass().getMethod("get", worldEditWorldClass);
                    Class<?> regionQueryClass = Class.forName("com.sk89q.worldguard.protection.regions.RegionQuery");
                    regionQueryTestStateMethod = regionQueryClass.getMethod("testState", worldEditLocationClass, RegionAssociable.class, StateFlag[].class);

                    Class<?> flagsClass = Class.forName("com.sk89q.worldguard.protection.flags.Flags");
                    buildFlag = (StateFlag)flagsClass.getField("BUILD").get(null);
                    pvpFlag = (StateFlag)flagsClass.getField("PVP").get(null);
                    exitFlag = (StateFlag)flagsClass.getField("EXIT").get(null);
                } catch (Exception ex) {
                    owningPlugin.getLogger().log(Level.WARNING, "Failed to bind to WorldGuard, integration will not work!", ex);
                    regionContainer = null;
                    return;
                }
            } else {
                regionContainer = worldGuardPlugin.getRegionContainer();
                try {
                    createQueryMethod = regionContainer.getClass().getMethod("createQuery");
                    regionContainerGetMethod = regionContainer.getClass().getMethod("get", World.class);
                    Class<?> regionQueryClass = Class.forName("com.sk89q.worldguard.bukkit.RegionQuery");
                    regionQueryTestStateMethod = regionQueryClass.getMethod("testState", Location.class, RegionAssociable.class, StateFlag[].class);

                    Class<?> flagsClass = Class.forName("com.sk89q.worldguard.protection.flags.DefaultFlag");
                    buildFlag = (StateFlag)flagsClass.getField("BUILD").get(null);
                    pvpFlag = (StateFlag)flagsClass.getField("PVP").get(null);
                    exitFlag = (StateFlag)flagsClass.getField("EXIT").get(null);
                } catch (Exception ex) {
                    owningPlugin.getLogger().log(Level.WARNING, "Failed to bind to WorldGuard, integration will not work!", ex);
                    regionContainer = null;
                    return;
                }
            }

            // Ugh guys, API much?
            try {
                Class<?> vectorClass = Class.forName("com.sk89q.worldedit.Vector");
                vectorConstructor = vectorClass.getConstructor(Double.TYPE, Double.TYPE, Double.TYPE);
                regionManagerGetMethod = RegionManager.class.getMethod("getApplicableRegions", vectorClass);
            } catch (Exception ex) {
                try {
                    Class<?> vectorClass = Class.forName("com.sk89q.worldedit.math.BlockVector3");
                    vectorConstructorAsAMethodBecauseWhyNot = vectorClass.getMethod("at", Double.TYPE, Double.TYPE, Double.TYPE);
                    regionManagerGetMethod = RegionManager.class.getMethod("getApplicableRegions", vectorClass);
                } catch (Exception sodonewiththis) {
                    owningPlugin.getLogger().log(Level.WARNING, "Failed to bind to WorldGuard (no Vector class?), integration will not work!", ex);
                    regionContainer = null;
                    return;
                }
            }

            if (regionContainer == null) {
                owningPlugin.getLogger().warning("Failed to find RegionContainer, WorldGuard integration will not function!");
            }
        }
    }

    @Nullable
    private RegionManager getRegionManager(World world) {
        initialize();
        if (regionContainer == null || regionContainerGetMethod == null) return null;
        RegionManager regionManager = null;
        try {
            if (worldAdaptMethod != null) {
                Object worldEditWorld = worldAdaptMethod.invoke(null, world);
                regionManager = (RegionManager)regionContainerGetMethod.invoke(regionContainer, worldEditWorld);
            } else {
                regionManager = (RegionManager)regionContainerGetMethod.invoke(regionContainer, world);
            }
        } catch (Exception ex) {
            owningPlugin.getLogger().log(Level.WARNING, "An error occurred looking up a WorldGuard RegionManager", ex);
        }
        return regionManager;
    }

    @Nullable
    private ApplicableRegionSet getRegionSet(Location location) {
        long now = System.currentTimeMillis();
        if (cacheDuration > 0) {
            if (now > lastCacheClear + cacheDuration) {
                cachedSets.clear();
                lastCacheClear = now;
            }
            ApplicableRegionSet regionSet = cachedSets.get(location);
            if (regionSet != null) {
                return regionSet;
            }
        }
        RegionManager regionManager = getRegionManager(location.getWorld());
        if (regionManager == null) return null;
        // The Location version of this method is gone in 7.0
        // Oh and then they also randomly changed the Vector class at some point without even a version bump.
        // So awesome!
        try {
            Object vector = vectorConstructorAsAMethodBecauseWhyNot == null
                    ? vectorConstructor.newInstance(location.getX(), location.getY(), location.getZ())
                    : vectorConstructorAsAMethodBecauseWhyNot.invoke(null, location.getX(), location.getY(), location.getZ());
            ApplicableRegionSet regionSet = (ApplicableRegionSet)regionManagerGetMethod.invoke(regionManager, vector);
            if (cacheDuration > 0) {
                cachedSets.put(location, regionSet);
            }
            return regionSet;
        } catch (Exception ex) {
            owningPlugin.getLogger().log(Level.WARNING, "An error occurred looking up a WorldGuard ApplicableRegionSet", ex);
        }
        return null;
    }

    public boolean isPVPAllowed(Player player, Location location) {
        if (worldGuardPlugin == null || location == null) return true;

        ApplicableRegionSet checkSet = getRegionSet(location);
        if (checkSet == null) return true;

        return checkSet.queryState(getAssociable(player), pvpFlag) != StateFlag.State.DENY;
    }

    public boolean isExitAllowed(Player player, Location location) {
        if (worldGuardPlugin == null || location == null) return true;

        ApplicableRegionSet checkSet = getRegionSet(location);
        if (checkSet == null) return true;

        return checkSet.queryState(getAssociable(player), exitFlag) != StateFlag.State.DENY;
    }

    public boolean hasBuildPermission(Player player, Block block) {
        initialize();
        if (block != null && createQueryMethod != null && regionContainer != null) {
            try {
                boolean result;
                Object query = createQueryMethod.invoke(regionContainer);
                if (locationAdaptMethod != null) {
                    Object location = locationAdaptMethod.invoke(null, block.getLocation());
                    result = (boolean)regionQueryTestStateMethod.invoke(query, location, getAssociable(player), new StateFlag[]{buildFlag});
                } else {
                    result = (boolean)regionQueryTestStateMethod.invoke(query, block.getLocation(), getAssociable(player), new StateFlag[]{buildFlag});
                }
                return result;
            } catch (Exception ex) {
                owningPlugin.getLogger().log(Level.WARNING, "An error occurred querying WorldGuard", ex);
            }
        }

        return true;
    }

    @Nullable
    public Boolean getCastPermission(Player player, SpellTemplate spell, Location location) {
        if (location != null && worldGuardPlugin != null && customFlags != null)
        {
            ApplicableRegionSet checkSet = getRegionSet(location);
            if (checkSet == null) {
                return null;
            }

            return customFlags.getCastPermission(getAssociable(player), checkSet, spell);
        }
        return null;
    }

    @Nullable
    public Boolean getWandPermission(Player player, Wand wand, Location location) {
        if (location != null && worldGuardPlugin != null && customFlags != null)
        {
            ApplicableRegionSet checkSet = getRegionSet(location);
            if (checkSet == null) {
                return null;
            }

            return customFlags.getWandPermission(getAssociable(player), checkSet, wand);
        }
        return null;
    }

    @Nullable
    public String getReflective(Player player, Location location) {
        if (location != null && worldGuardPlugin != null && customFlags != null)
        {
            ApplicableRegionSet checkSet = getRegionSet(location);
            if (checkSet == null) {
                return null;
            }

            return customFlags.getReflective(getAssociable(player), checkSet);
        }
        return null;
    }

    @Nullable
    public Set<String> getSpellOverrides(Player player, Location location) {
        if (location != null && worldGuardPlugin != null && customFlags != null)
        {
            ApplicableRegionSet checkSet = getRegionSet(location);
            if (checkSet == null) {
                return null;
            }

            return customFlags.getSpellOverrides(getAssociable(player), checkSet);
        }
        return null;
    }

    @Nullable
    public String getDestructible(Player player, Location location) {
        if (location != null && worldGuardPlugin != null && customFlags != null)
        {
            ApplicableRegionSet checkSet = getRegionSet(location);
            if (checkSet == null) {
                return null;
            }

            return customFlags.getDestructible(getAssociable(player), checkSet);
        }
        return null;
    }

    @Nullable
    public String getPortalSpell(Player player, Location location) {
        if (location != null && worldGuardPlugin != null && customFlags != null) {
            ApplicableRegionSet checkSet = getRegionSet(location);
            if (checkSet == null) {
                return null;
            }

            return customFlags.getPortalSpell(getAssociable(player), checkSet);
        }
        return null;
    }

    @Nullable
    public String getPortalWarp(Player player, Location location) {
        if (location != null && worldGuardPlugin != null && customFlags != null) {
            ApplicableRegionSet checkSet = getRegionSet(location);
            if (checkSet == null) {
                return null;
            }

            return customFlags.getPortalWarp(getAssociable(player), checkSet);
        }
        return null;
    }

    public boolean inTaggedRegion(Location location, Set<String> tags) {
        if (location != null && worldGuardPlugin != null && customFlags != null)
        {
            ApplicableRegionSet checkSet = getRegionSet(location);
            if (checkSet == null) {
                return false;
            }

            return customFlags.inTaggedRegion(Associables.constant(Association.NON_MEMBER), checkSet, tags);
        }
        return false;
    }

    public void setCacheDuration(int cacheDuration) {
        this.cacheDuration = cacheDuration;
        cachedSets.clear();
    }
}
