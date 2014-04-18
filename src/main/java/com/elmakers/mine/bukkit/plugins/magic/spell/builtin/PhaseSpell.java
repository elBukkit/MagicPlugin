package com.elmakers.mine.bukkit.plugins.magic.spell.builtin;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.spell.TargetingSpell;
import com.elmakers.mine.bukkit.utilities.Messages;

public class PhaseSpell extends TargetingSpell
{
	private static int MAX_RETRY_COUNT = 8;
	private static int RETRY_INTERVAL = 10;
	
	private int retryCount = 0;
	private String targetWorldName = "";
	
	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		Location playerLocation = getLocation();
		String worldName = playerLocation.getWorld().getName();
		Location targetLocation = null;
		
		if (parameters.contains("target_world"))
		{
			World targetWorld = Bukkit.getWorld(parameters.getString("target_world"));
			if (targetWorld == null) {
				return SpellResult.INVALID_WORLD;
			}
			float scale = (float)parameters.getDouble("scale", 1.0f);
			if (targetWorld != null) {
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
			World targetWorld = Bukkit.getWorld(worldNode.getString("target"));
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
		tryPhase(targetLocation);
		
		return SpellResult.CAST;
	}
	
	protected void tryPhase(final Location targetLocation) {
		Chunk chunk = targetLocation.getBlock().getChunk();
		if (!chunk.isLoaded()) {
			chunk.load(true);
			if (retryCount < MAX_RETRY_COUNT) {
				Plugin plugin = controller.getPlugin();
				final PhaseSpell me = this;
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					public void run() {
						me.tryPhase(targetLocation);
					}
				}, RETRY_INTERVAL);
				
				return;
			}
		}
		
		Player player = getPlayer();
		if (player != null) {
			Location playerLocation = player.getLocation();
			targetLocation.setYaw(playerLocation.getYaw());
			targetLocation.setPitch(playerLocation.getPitch());
			final int maxY = targetLocation.getWorld().getEnvironment() == Environment.NETHER ? 118 : 255;
			
			Location destination = tryFindPlaceToStand(targetLocation, 4, maxY);
			
			// TODO : Failure notification? Sounds at least? The async nature is difficult.
			if (destination != null) {
				targetWorldName = destination.getWorld().getName();
				player.teleport(destination);
			}
		}
	}
	
	@Override
	public String getMessage(String messageKey, String def) {
		String message = super.getMessage(messageKey, def);
		targetWorldName = Messages.get("worlds." + targetWorldName + ".name", targetWorldName);
		return message.replace("$world_name", targetWorldName);
	}
}
