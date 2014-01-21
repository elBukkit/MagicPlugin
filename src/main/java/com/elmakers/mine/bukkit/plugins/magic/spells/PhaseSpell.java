package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.effects.EffectUtils;
import com.elmakers.mine.bukkit.effects.ParticleType;
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
		Location playerLocation = getPlayer().getEyeLocation();
		String worldName = playerLocation.getWorld().getName();
		Location targetLocation = null;
		
		if (worldName.contains("_nether")) {
			worldName = worldName.replace("_nether", "");
			World targetWorld = Bukkit.getWorld(worldName);
			if (targetWorld != null) {
				targetLocation = new Location(targetWorld, playerLocation.getX() * 8, playerLocation.getY(), playerLocation.getZ() * 8);
			}
		} else {
			worldName = worldName + "_nether";
			World targetWorld = Bukkit.getWorld(worldName);
			if (targetWorld != null) {
				targetLocation = new Location(targetWorld, playerLocation.getX() / 8, playerLocation.getY(), playerLocation.getZ() / 8);
			}
		}
		
		if (targetLocation == null) {
			return SpellResult.NO_TARGET;
		}
		
		retryCount = 0;
		tryPhase(targetLocation);
		
		return SpellResult.SUCCESS;
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
			player.teleport(tryFindPlaceToStand(targetLocation));
			EffectUtils.playEffect(playerLocation, ParticleType.PORTAL, 1, 16);
			playerLocation.getWorld().playSound(playerLocation, Sound.ENDERMAN_TELEPORT, 1.0f, 1.5f);
			EffectUtils.playEffect(targetLocation, ParticleType.PORTAL, 1, 16);
			playerLocation.getWorld().playSound(targetLocation, Sound.ENDERMAN_TELEPORT, 1.0f, 1.5f);
		}
	}
}
