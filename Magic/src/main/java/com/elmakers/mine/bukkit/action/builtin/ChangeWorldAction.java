package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.action.BaseTeleportAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.platform.CompatibilityUtils;

public class ChangeWorldAction extends BaseTeleportAction
{
    private String targetWorldMessage = "";
    private String targetWorldName;
    private boolean loadWorld;
    private double scale;
    ConfigurationSection worldMap;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);

        scale = parameters.getDouble("scale", 1);
        if (parameters.contains("target_world")) {
            targetWorldName = parameters.getString("target_world");
            loadWorld = parameters.getBoolean("load", true);
        } else if (parameters.contains("worlds")) {
            worldMap = ConfigurationUtils.getConfigurationSection(parameters, "worlds");
        }
    }

    @Override
    public boolean isUndoable() {
        return true;
    }

    @Override
    public boolean requiresTargetEntity() {
        return true;
    }

    @Nullable
    protected Location prepareTargetLocation(CastContext context) {
        World world = context.getWorld();
        if (world == null) {
            return null;
        }
        Location playerLocation = context.getLocation();
        if (playerLocation == null) {
            return null;
        }
        String worldName = world.getName();
        Location targetLocation = null;
        if (targetWorldName != null && !targetWorldName.isEmpty()) {
            World targetWorld = getWorld(context, targetWorldName, loadWorld, null);
            if (targetWorld == null) {
                return null;
            }
            targetLocation = new Location(targetWorld, playerLocation.getX() * scale, playerLocation.getY(), playerLocation.getZ() * scale);
        } else if (worldMap != null) {
            if (!worldMap.contains(worldName)) {
                return null;
            }

            ConfigurationSection worldNode = ConfigurationUtils.getConfigurationSection(worldMap, worldName);
            boolean useSpawnLocations = worldNode.getBoolean("use_spawns", false);
            Vector minLocation = ConfigurationUtils.getVector(worldNode, "bounds_min");
            Vector maxLocation = ConfigurationUtils.getVector(worldNode, "bounds_max");
            World targetWorld = getWorld(context, worldNode.getString("target"), worldNode.getBoolean("load", true), worldNode.getBoolean("copy", false) ? world : null);
            if (targetWorld != null) {
                String envName = worldNode.getString("environment");
                if (envName != null && !envName.isEmpty()) {
                    try {
                        World.Environment env = World.Environment.valueOf(envName.toUpperCase());
                        CompatibilityUtils.setEnvironment(targetWorld, env);
                    } catch (Exception ex) {
                        context.getLogger().warning("Unknown environment type: " + envName);
                    }
                }

                double scale = worldNode.getDouble("scale", 1);
                if (useSpawnLocations) {
                    Location currentSpawn = playerLocation.getWorld().getSpawnLocation();
                    playerLocation.setX(playerLocation.getX() - currentSpawn.getX());
                    playerLocation.setZ(playerLocation.getZ() - currentSpawn.getZ());
                }
                targetLocation = new Location(targetWorld, playerLocation.getX() * scale, playerLocation.getY(), playerLocation.getZ() * scale);
                if (useSpawnLocations) {
                    Location targetSpawn = targetWorld.getSpawnLocation();
                    targetLocation.setX(targetLocation.getX() + targetSpawn.getX());
                    targetLocation.setZ(targetLocation.getZ() + targetSpawn.getZ());
                }
                if (minLocation != null) {
                    if (targetLocation.getX() < minLocation.getX()) targetLocation.setX(minLocation.getX());
                    if (targetLocation.getZ() < minLocation.getZ()) targetLocation.setZ(minLocation.getZ());
                }
                if (maxLocation != null) {
                    if (targetLocation.getX() > maxLocation.getX()) targetLocation.setX(maxLocation.getX());
                    if (targetLocation.getZ() > maxLocation.getZ()) targetLocation.setZ(maxLocation.getZ());
                }
            }
        } else {
            // Auto-determine target world and scale
            if (worldName.contains("_the_end")) {
                worldName = worldName.replace("_the_end", "");
                World targetWorld = Bukkit.getWorld(worldName);
                if (targetWorld != null) {
                    targetLocation = new Location(targetWorld, playerLocation.getX(), playerLocation.getY(), playerLocation.getZ());
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

        return targetLocation;
    }

    @Override
    public SpellResult perform(CastContext context) {
        Location targetLocation = prepareTargetLocation(context);
        if (targetLocation == null) {
            return SpellResult.NO_TARGET;
        }
        Entity e = context.getTargetEntity();
        LivingEntity entity = e != null && e instanceof LivingEntity ? (LivingEntity)e : null;
        if (entity == null) {
            return SpellResult.NO_TARGET;
        }

        Location playerLocation = entity.getLocation();
        if (targetLocation == null) {
            return SpellResult.NO_TARGET;
        }

        // Sanity check!
        targetLocation.setX(Math.min(targetLocation.getX(), 3.0E7D));
        targetLocation.setZ(Math.min(targetLocation.getZ(), 3.0E7D));

        targetLocation.setYaw(playerLocation.getYaw());
        targetLocation.setPitch(playerLocation.getPitch());

        setTargetWorldName(context, targetLocation.getWorld().getName());
        return teleport(context, entity, targetLocation);
    }

    protected World getWorld(CastContext context, String worldName, boolean loadWorld, World copyFrom) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            if (loadWorld) {
                if (copyFrom != null) {
                    context.getLogger().info("Creating world: " + worldName + " as copy of " + copyFrom.getName());
                    world = Bukkit.createWorld(new WorldCreator(worldName).copy(copyFrom));
                } else {
                    context.getLogger().info("Loading world: " + worldName);
                    world = Bukkit.createWorld(new WorldCreator(worldName));
                }
            }
        }

        if (world == null) {
            Bukkit.getLogger().warning("Could not load world: " + worldName);
        }

        return world;
    }

    protected void setTargetWorldName(CastContext context, String worldName) {
        Messages messages = context.getController().getMessages();
        targetWorldMessage = messages.get("worlds." + worldName + ".name", worldName);
    }

    @Override
    public String transformMessage(String message) {
        return message.replace("$world_name", targetWorldMessage);
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters)
    {
        super.getParameterNames(spell, parameters);
        parameters.add("target_world");
        parameters.add("load");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples)
    {
        if (parameterKey.equals("target_world")) {
            Collection<World> worlds = Bukkit.getWorlds();
            for (World world : worlds) {
                examples.add(world.getName());
            }
        } else if (parameterKey.equals("load")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_BOOLEANS));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
