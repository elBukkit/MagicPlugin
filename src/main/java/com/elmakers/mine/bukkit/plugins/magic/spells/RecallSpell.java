package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.ArrayList;
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
import com.elmakers.mine.bukkit.utilities.Target;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class RecallSpell extends Spell
{
	public Location location;
	public boolean isActive;

	private static int MAX_RETRY_COUNT = 8;
	private static int RETRY_INTERVAL = 10;
	
	private int retryCount = 0;
	private boolean allowCrossWorld = false;
	
	private enum RecallType
	{
		MARKER,
		DEATH,
		SPAWN,
		HOME,
		WAND,
	//	FHOME,
	//	WARPS
	};
	
	private RecallType selectedType = RecallType.MARKER;

	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		int selectedTypeIndex = 0;
		boolean allowMarker = true;
		allowCrossWorld = parameters.getBoolean("cross_world", false);
		List<RecallType> enabledTypes = new ArrayList<RecallType>();
		for (RecallType testType : RecallType.values()) {
			if (parameters.getBoolean("allow_" + testType.name().toLowerCase(), true)) {
				enabledTypes.add(testType);
				if (testType == selectedType) selectedTypeIndex = enabledTypes.size() - 1;
			} else {
				if (testType == RecallType.MARKER) allowMarker = false;
			}
		}

		if (parameters.containsKey("type")) {
			String typeString = parameters.getString("type", "");
			if (isActive && typeString.equalsIgnoreCase("remove")) {
				removeMarker();
				return SpellResult.TARGET_SELECTED;
			}
			RecallType newType = RecallType.valueOf(typeString.toUpperCase());
			if (newType == null) {
				controller.getLogger().warning("Unknown recall type " + typeString);
				return SpellResult.FAIL;
			}
			
			selectedType = newType;
		} 
		else if (getYRotation() > 70 || getYRotation() < -70 || !allowMarker)
		{
			if (getYRotation() > 70) selectedTypeIndex++;
			else selectedTypeIndex--;
			if (selectedTypeIndex < 0) selectedTypeIndex = enabledTypes.size() - 1;
			if (selectedTypeIndex >= enabledTypes.size()) selectedTypeIndex = 0;
			selectedType = enabledTypes.get(selectedTypeIndex);
		}
		else
		{
			Target target = getTarget();
			if (!target.isBlock()) {
				return SpellResult.NO_TARGET;
			}
			
			return placeMarker(target.getBlock());
		}
		
		Player player = getPlayer();
		if (player == null) return SpellResult.PLAYER_REQUIRED;
		
		switch (selectedType) {
		case MARKER:
			if (!isActive) {
				return placeMarker(getLocation().getBlock());
			}
			
			castMessage(getMessage("cast_marker"));
			return tryTeleport(location);
		case DEATH:
			Location deathLocation = mage.getLastDeathLocation();
			if (deathLocation == null)
			{
				sendMessage(getMessage("no_target_death"));
				return SpellResult.NO_TARGET;
			}

			castMessage(getMessage("cast_death"));
			return tryTeleport(deathLocation);
		case SPAWN:
			castMessage(getMessage("cast_spawn"));
			return tryTeleport(getWorld().getSpawnLocation());
		case HOME:
			Location bedLocation = player.getBedSpawnLocation();
			if (bedLocation == null) {
				castMessage(getMessage("no_target_home"));
				return SpellResult.NO_TARGET;
			} else {
				castMessage(getMessage("cast_home"));				
			}
			return tryTeleport(bedLocation);
		case WAND:
			List<LostWand> lostWands = mage.getLostWands();
			Location wandLocation = lostWands.size() > 0 ? lostWands.get(0).getLocation() : null;
			if (wandLocation == null)
			{
				sendMessage(getMessage("no_target_wand"));
				return SpellResult.NO_TARGET;
			}
			
			castMessage(getMessage("cast_wand"));
			return tryTeleport(wandLocation);
		}

		return SpellResult.FAIL;
	}

	protected boolean removeMarker()
	{
		if (!isActive || location == null) return false;
		isActive = false;
		return true;
	}
	
	protected SpellResult tryTeleport(final Location targetLocation) {
		if (!allowCrossWorld && !mage.getLocation().getWorld().equals(targetLocation.getWorld())) {
			sendMessage(getMessage("cross_world_disallowed"));
			return SpellResult.NO_TARGET;
		}
		
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
				
				return SpellResult.CAST;
			}
		}
		
		Player player = getPlayer();
		if (player != null) {
			Location playerLocation = player.getLocation();
			targetLocation.setYaw(playerLocation.getYaw());
			targetLocation.setPitch(playerLocation.getPitch());
			player.teleport(tryFindPlaceToStand(targetLocation));
		}
		return SpellResult.CAST;
	}

	protected SpellResult placeMarker(Block target)
	{
		if (target == null)
		{
			return SpellResult.NO_TARGET;
		}
		Block targetBlock = target.getRelative(BlockFace.UP);
		if (targetBlock.getType() != Material.AIR)
		{
			targetBlock = getFaceBlock();
		}
		if (targetBlock.getType() != Material.AIR)
		{
			return SpellResult.NO_TARGET;
		}

		if (removeMarker())
		{
			castMessage(getMessage("cast_marker_move"));
		}
		else
		{
			castMessage(getMessage("cast_marker_place"));
		}

		location = getLocation();
		location.setX(targetBlock.getX());
		location.setY(targetBlock.getY());
		location.setZ(targetBlock.getZ());

		getPlayer().setCompassTarget(location);
		EffectUtils.playEffect(targetBlock.getLocation(), ParticleType.CLOUD, 1, 1);
		
		isActive = true;

		return SpellResult.CAST;
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
