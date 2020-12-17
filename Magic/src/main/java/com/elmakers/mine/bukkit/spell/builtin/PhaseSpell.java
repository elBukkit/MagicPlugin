package com.elmakers.mine.bukkit.spell.builtin;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.TargetingSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.Target;

@Deprecated
public class PhaseSpell extends TargetingSpell
{
    private static int MAX_RETRY_COUNT = 8;
    private static int RETRY_INTERVAL = 10;

    private int retryCount = 0;
    private String targetWorldName = "";

    @Override
    public SpellResult onCast(ConfigurationSection parameters)
    {
        Location targetLocation = null;
        Target target = getTarget();
        Entity e = target.getEntity();
        LivingEntity entity = e != null && e instanceof LivingEntity ? (LivingEntity)e : null;
        if (entity == null) {
            return SpellResult.NO_TARGET;
        }

        if (entity != mage.getEntity() && controller.isMage(entity)) {
            Mage mage = controller.getMage(entity);
            if (mage.isSuperProtected()) {
                return SpellResult.NO_TARGET;
            }
        }
        Location playerLocation = entity.getLocation();
        String worldName = playerLocation.getWorld().getName();

        if (parameters.contains("target_world"))
        {
            World targetWorld = getWorld(parameters.getString("target_world"), parameters.getBoolean("load", true));
            if (targetWorld == null) {
                return SpellResult.INVALID_WORLD;
            }
            float scale = (float)parameters.getDouble("scale", 1.0f);
                if (targetWorld.getEnvironment() == World.Environment.THE_END) {
                    targetLocation = targetWorld.getSpawnLocation();
                } else {
                    targetLocation = new Location(targetWorld, playerLocation.getX() * scale, playerLocation.getY(), playerLocation.getZ() * scale);
                }
        }
        else
        if (parameters.contains("worlds"))
        {
            ConfigurationSection worldMap = parameters.getConfigurationSection("worlds");
            if (!worldMap.contains(worldName)) {
                return SpellResult.NO_TARGET;
            }

            ConfigurationSection worldNode = worldMap.getConfigurationSection(worldName);
            World targetWorld = getWorld(worldNode.getString("target"), worldNode.getBoolean("load", true));
            float scale = (float)worldNode.getDouble("scale", 1.0f);
            if (targetWorld != null) {
                targetLocation = new Location(targetWorld, playerLocation.getX() * scale, playerLocation.getY(), playerLocation.getZ() * scale);
            }
        }
        else {
            if (worldName.contains("_the_end")) {
                worldName = worldName.replace("_the_end", "");
                World targetWorld = Bukkit.getWorld(worldName);
                if (targetWorld != null) {
                    // No scaling here?
                    // Just send them to spawn... this is kind of to fix players finding the real spawn
                    // on my own server, but I'm not just sure how best to handle this anyway.
                    targetLocation = targetWorld.getSpawnLocation();
                }
            } else if (worldName.contains("_nether")) {
                worldName = worldName.replace("_nether", "");
                World targetWorld = Bukkit.getWorld(worldName);
                if (targetWorld != null) {
                    targetLocation = new Location(targetWorld, playerLocation.getX() * 8, playerLocation.getY(), playerLocation.getZ() * 8);
                }
            } else {
                worldName = worldName + "_nether";
                World targetWorld = Bukkit.getWorld(worldName);
                if (targetWorld != null) {
                    targetLocation = new Location(targetWorld, playerLocation.getX() / 8, Math.min(125, playerLocation.getY()), playerLocation.getZ() / 8);
                }
            }
        }

        if (targetLocation == null) {
            return SpellResult.NO_TARGET;
        }

        retryCount = 0;
        tryPhase(entity, targetLocation);

        return SpellResult.CAST;
    }

    @Nullable
    protected World getWorld(String worldName, boolean loadWorld) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            if (loadWorld) {
                Bukkit.getLogger().info("Loading world: " + worldName);
                world = Bukkit.createWorld(new WorldCreator(worldName));
                if (world == null) {
                    Bukkit.getLogger().warning("Failed to load world: " + worldName);
                    return null;
                }
            }
        }

        if (world == null) {
            Bukkit.getLogger().warning("Could not load world: " + worldName);
        }

        return world;
    }

    protected void tryPhase(final LivingEntity entity, final Location targetLocation) {
        if (!CompatibilityUtils.checkChunk(targetLocation, true)) {
            if (retryCount < MAX_RETRY_COUNT) {
                Plugin plugin = controller.getPlugin();
                final PhaseSpell me = this;
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        me.tryPhase(entity, targetLocation);
                    }
                }, RETRY_INTERVAL);

                return;
            }
        }

        Location playerLocation = entity.getLocation();
        targetLocation.setYaw(playerLocation.getYaw());
        targetLocation.setPitch(playerLocation.getPitch());
        Location destination = tryFindPlaceToStand(targetLocation);

        // TODO : Failure notification? Sounds at least? The async nature is difficult.
        if (destination != null) {
            targetWorldName = destination.getWorld().getName();
            entity.teleport(destination);
        }
    }

    @Override
    public String getMessage(String messageKey, String def) {
        String message = super.getMessage(messageKey, def);
        targetWorldName = controller.getMessages().get("worlds." + targetWorldName + ".name", targetWorldName);
        return message.replace("$world_name", targetWorldName);
    }
}
