package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class PhaseSpell extends Spell
{
	private static int MAX_RETRY_COUNT = 8;
	private static int RETRY_INTERVAL = 10;
	
	private int retryCount = 0;
	
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		int maxY = 250;
		Location playerLocation = getLocation();
		String worldName = playerLocation.getWorld().getName();
		Location targetLocation = null;
		
		if (parameters.containsKey("worlds"))
		{
			ConfigurationNode worldMap = parameters.getNode("worlds");
			if (!worldMap.containsKey(worldName)) {
				return SpellResult.NO_TARGET;
			}
			
			ConfigurationNode worldNode = worldMap.getNode(worldName);
			World targetWorld = Bukkit.getWorld(worldNode.getString("target"));
			float scale = worldNode.getFloat("scale", 1.0f);
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
				maxY = 118;
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
		tryPhase(targetLocation, maxY);
		
		return SpellResult.CAST;
	}
	
	protected void tryPhase(final Location targetLocation, final int maxY) {
		Chunk chunk = targetLocation.getBlock().getChunk();
		if (!chunk.isLoaded()) {
			chunk.load(true);
			if (retryCount < MAX_RETRY_COUNT) {
				Plugin plugin = controller.getPlugin();
				final PhaseSpell me = this;
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					public void run() {
						me.tryPhase(targetLocation, maxY);
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
			
			Location destination = tryFindPlaceToStand(targetLocation, 4, maxY);
			
			// TODO : Failure notification? Sounds at least? The async nature is difficult.
			if (destination != null) {
				player.teleport(destination);
			}
		}
	}
}
