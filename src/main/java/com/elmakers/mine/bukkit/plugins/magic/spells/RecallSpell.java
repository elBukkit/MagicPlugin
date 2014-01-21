package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.effects.EffectUtils;
import com.elmakers.mine.bukkit.effects.ParticleType;
import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.wand.LostWand;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class RecallSpell extends Spell
{
	public Location location;
	public boolean isActive;
	private int disableDistance = 5;

	private static int MAX_RETRY_COUNT = 8;
	private static int RETRY_INTERVAL = 10;
	
	private int retryCount = 0;

	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		boolean autoResurrect = parameters.getBoolean("auto_resurrect", true);
		boolean autoSpawn = parameters.getBoolean("allow_spawn", true);

		String typeString = parameters.getString("type", "");
		if (typeString.equals("spawn"))
		{
			castMessage("Returning you home");
			tryTeleport(getPlayer().getWorld().getSpawnLocation());
			return SpellResult.SUCCESS; 
		}
		if (typeString.equals("wand"))
		{
			List<LostWand> lostWands = mage.getLostWands();
			Location wandLocation = lostWands.size() > 0 ? lostWands.get(0).getLocation() : null;
			if (wandLocation == null)
			{
				sendMessage("No recorded lost wands for you. Sorry!");
				return SpellResult.NO_TARGET;
			}
			
			tryTeleport(wandLocation);
			return SpellResult.SUCCESS; 
		}
		if (typeString.equals("death") || getYRotation() < -70  && autoResurrect)
		{
			Location deathLocation = mage.getLastDeathLocation();
			if (deathLocation == null)
			{
				sendMessage("No recorded death location. Sorry!");
				return SpellResult.NO_TARGET;
			}
			
			tryTeleport(deathLocation);
			return SpellResult.SUCCESS; 
		}
		
		if (getYRotation() > 70)
		{
			if (!isActive && autoSpawn)
			{
				castMessage("Returning you home");
				tryTeleport(getPlayer().getWorld().getSpawnLocation());
			}
			else
			{
				if (!isActive) return SpellResult.NO_TARGET;

				double distanceSquared = getPlayer().getLocation().distanceSquared(location);

				if (distanceSquared < disableDistance * disableDistance && autoSpawn)
				{
					castMessage("Returning you home");
					tryTeleport(getPlayer().getWorld().getSpawnLocation());
				}
				else
				{
					castMessage("Returning you to your marker");
					tryTeleport(location);
				}
			}
			return SpellResult.SUCCESS;
		}

		if (!isActive)
		{
			return placeMarker(getTargetBlock());
		}

		return placeMarker(getTargetBlock());
	}

	protected boolean removeMarker()
	{
		if (!isActive || location == null) return false;
		isActive = false;
		return true;
	}
	
	protected void tryTeleport(final Location targetLocation) {
		Chunk chunk = targetLocation.getBlock().getChunk();
		if (!chunk.isLoaded()) {
			chunk.load(true);
			if (retryCount < MAX_RETRY_COUNT) {
				Plugin plugin = controller.getPlugin();
				final RecallSpell me = this;
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					public void run() {
						me.tryTeleport(targetLocation);
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
			player.teleport(tryFindPlaceToStand(targetLocation));
		}
	}

	protected SpellResult placeMarker(Block target)
	{
		if (target == null)
		{
			castMessage("No target");
			return SpellResult.NO_TARGET;
		}
		Block targetBlock = target.getRelative(BlockFace.UP);
		if (targetBlock.getType() != Material.AIR)
		{
			targetBlock = getFaceBlock();
		}
		if (targetBlock.getType() != Material.AIR)
		{
			castMessage("Can't place a marker there");
			return SpellResult.NO_TARGET;
		}

		if (removeMarker())
		{
			castMessage("You move your recall marker");
		}
		else
		{
			castMessage("You place a recall marker");
		}

		location = getPlayer().getLocation();
		location.setX(targetBlock.getX());
		location.setY(targetBlock.getY());
		location.setZ(targetBlock.getZ());

		getPlayer().setCompassTarget(location);
		EffectUtils.playEffect(targetBlock.getLocation(), ParticleType.CLOUD, 1, 1);
		
		isActive = true;

		return SpellResult.SUCCESS;
	}
	
	@Override
	public void onLoad(ConfigurationNode node)
	{
		isActive = node.getBoolean("active", false);
		location = node.getLocation("location");
	}

	@Override
	public void onSave(ConfigurationNode node)
	{
		node.setProperty("active", isActive);
		node.setProperty("location", location);
	}
}
