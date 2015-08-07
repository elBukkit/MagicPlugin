package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseTeleportAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.Arrays;
import java.util.Collection;

public class ChangeWorldAction extends BaseTeleportAction
{
	private String targetWorldMessage = "";
    private Location targetLocation = null;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);

        World world = context.getWorld();
        Location playerLocation = context.getLocation();
        targetLocation = null;
        if (world == null) {
            return;
        }

        String worldName = world.getName();
        if (parameters.contains("target_world")) {
            World targetWorld = getWorld(context, parameters.getString("target_world"), parameters.getBoolean("load", true));
            if (targetWorld == null) {
                return;
            }
            if (targetWorld.getEnvironment() == World.Environment.THE_END) {
                targetLocation = targetWorld.getSpawnLocation();
            } else {
                double scale = parameters.getDouble("scale", 1);
                targetLocation = new Location(targetWorld, playerLocation.getX() * scale, playerLocation.getY(), playerLocation.getZ() * scale);
            }
        }
        else if (parameters.contains("worlds"))
        {
            ConfigurationSection worldMap = parameters.getConfigurationSection("worlds");
            if (worldMap == null || !worldMap.contains(worldName)) {
                return;
            }

            ConfigurationSection worldNode = worldMap.getConfigurationSection(worldName);
            World targetWorld = getWorld(context, worldNode.getString("target"), worldNode.getBoolean("load", true));
            if (targetWorld != null) {
                double scale = worldNode.getDouble("scale", 1);
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
    }

    @Override
    public boolean isUndoable() {
        return true;
    }

    @Override
    public boolean requiresTargetEntity() {
        return true;
    }

	@Override
    public SpellResult perform(CastContext context) {
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
        context.teleport(entity, targetLocation, verticalSearchDistance);
		
		return SpellResult.CAST;
	}

    protected World getWorld(CastContext context, String worldName, boolean loadWorld) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            if (loadWorld) {
                context.getLogger().info("Loading world: " + worldName);
                world = Bukkit.createWorld(new WorldCreator(worldName));
                if (world == null) {
                    context.getLogger().warning("Failed to load world: " + worldName);
                    return null;
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
