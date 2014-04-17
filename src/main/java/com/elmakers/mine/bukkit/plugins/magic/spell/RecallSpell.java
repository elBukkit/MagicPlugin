package com.elmakers.mine.bukkit.plugins.magic.spell;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.effects.EffectUtils;
import com.elmakers.mine.bukkit.effects.ParticleType;
import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.wand.LostWand;
import com.elmakers.mine.bukkit.utilities.ConfigurationUtils;
import com.elmakers.mine.bukkit.utilities.Target;

public class RecallSpell extends Spell
{
	public Location location;
	public boolean isActive;

	private static int MAX_RETRY_COUNT = 8;
	private static int RETRY_INTERVAL = 10;
	
	private int retryCount = 0;
	private boolean allowCrossWorld = true;
	private int selectedWarpIndex = 0;
	private List<String> warps = new ArrayList<String>();
	
	private RecallType selectedType = RecallType.MARKER;
	private int selectedTypeIndex = 0;
	private List<RecallType> enabledTypes = new ArrayList<RecallType>();
	
	private enum RecallType
	{
		MARKER,
		DEATH,
		SPAWN,
		HOME,
		WAND,
		WARPS
	//	FHOME,
	};

	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		boolean allowMarker = true;
		selectedTypeIndex = 0;
		int cycleRetries = 5;
		enabledTypes.clear();
		warps = null;
		
		allowCrossWorld = parameters.getBoolean("cross_world", true);
		for (RecallType testType : RecallType.values()) {
			// Special-case for warps
			if (testType == RecallType.WARPS) {
				if (parameters.contains("allow_warps")) {
					warps = parameters.getStringList("allow_warps");
					enabledTypes.add(testType);
					if (testType == selectedType) selectedTypeIndex = enabledTypes.size() - 1;
				}
			} else {
				if (parameters.getBoolean("allow_" + testType.name().toLowerCase(), true)) {
					enabledTypes.add(testType);
					if (testType == selectedType) selectedTypeIndex = enabledTypes.size() - 1;
				} else {
					if (testType == RecallType.MARKER) allowMarker = false;
				}
			}
		}

		boolean reverseDirection = false;
		if (parameters.contains("type")) {
			cycleRetries = 0;
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
			reverseDirection = getYRotation() < 70;
			cycleTarget(reverseDirection);
		}
		else
		{
			Target target = getTarget();
			if (!target.isValid()) {
				return SpellResult.NO_TARGET;
			}
			
			return placeMarker(target.getBlock()) ? SpellResult.CAST : SpellResult.FAIL;
		}

		if (selectedType == null) {
			if (enabledTypes.size() == 0) return SpellResult.FAIL;
			selectedType = enabledTypes.get(0);
		}
		
		Player player = getPlayer();
		if (player == null) return SpellResult.PLAYER_REQUIRED;
		
		boolean success = false;
		while (!success && cycleRetries >= 0) {
			success = tryCurrentType(player, cycleRetries == 0);
			if (!success && cycleRetries > 0) {
				cycleTarget(reverseDirection);
			}
			cycleRetries--;
		}

		return success ? SpellResult.CAST : SpellResult.FAIL;
	}
	
	protected void cycleTargetType(boolean reverse) {
		if (reverse) selectedTypeIndex--;
		else selectedTypeIndex++;
		if (selectedTypeIndex < 0) selectedTypeIndex = enabledTypes.size() - 1;
		if (selectedTypeIndex >= enabledTypes.size()) selectedTypeIndex = 0;
		selectedType = enabledTypes.get(selectedTypeIndex);
		if (selectedType == RecallType.WARPS) {
			if (reverse) selectedWarpIndex = warps.size() - 1;
			else selectedWarpIndex = 0;
		}
	}
	
	protected void cycleTarget(boolean reverse) {
		// Special-case for warps
		if (selectedType == RecallType.WARPS) {
			if (reverse) {
				selectedWarpIndex--;
				if (selectedWarpIndex < 0) {
					selectedWarpIndex = warps.size() - 1;
				} else {
					return;
				}
			}
			else {
				selectedWarpIndex++;
				if (selectedWarpIndex >= warps.size()) {
					selectedWarpIndex = 0;
				} else {
					return;
				}
			}
		}
		
		cycleTargetType(reverse);
	}
	
	protected boolean tryCurrentType(Player player, boolean showFailMessage) {
		switch (selectedType) {
		case MARKER:
			if (!isActive) {
				return placeMarker(getLocation().getBlock());
			}
			
			return tryTeleport(location, getMessage("cast_marker"));
		case DEATH:
			Location deathLocation = mage.getLastDeathLocation();
			if (deathLocation == null)
			{
				if (showFailMessage) sendMessage(getMessage("no_target_death"));
				return false;
			}

			return tryTeleport(deathLocation, getMessage("cast_death"));
		case SPAWN:
			return tryTeleport(getWorld().getSpawnLocation(), getMessage("cast_spawn"));
		case HOME:
			Location bedLocation = player.getBedSpawnLocation();
			if (bedLocation == null) {
				if (showFailMessage) castMessage(getMessage("no_target_home"));
				return false;
			}
			return tryTeleport(bedLocation, getMessage("cast_home"));
		case WAND:
			List<LostWand> lostWands = mage.getLostWands();
			Location wandLocation = lostWands.size() > 0 ? lostWands.get(0).getLocation() : null;
			if (wandLocation == null)
			{
				if (showFailMessage) sendMessage(getMessage("no_target_wand"));
				return false;
			}
			
			return tryTeleport(wandLocation, getMessage("cast_wand"));
		case WARPS:
			if (warps == null || selectedWarpIndex < 0 || selectedWarpIndex >= warps.size()) return false;
			String warpName = warps.get(selectedWarpIndex);
			Location warpLocation = controller.getWarp(warpName);
			if (warpLocation == null) return false;
			return tryTeleport(warpLocation, getMessage("cast_warp").replace("$name", warpName));
		}
		
		return false;
	}

	protected boolean removeMarker()
	{
		if (!isActive || location == null) return false;
		isActive = false;
		return true;
	}
	
	protected boolean tryTeleport(final Location targetLocation, final String message) {
		if (!allowCrossWorld && !mage.getLocation().getWorld().equals(targetLocation.getWorld())) {
			sendMessage(getMessage("cross_world_disallowed"));
			return false;
		}
		
		Chunk chunk = targetLocation.getBlock().getChunk();
		if (!chunk.isLoaded()) {
			chunk.load(true);
			if (retryCount < MAX_RETRY_COUNT) {
				Plugin plugin = controller.getPlugin();
				final RecallSpell me = this;
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					public void run() {
						me.tryTeleport(targetLocation, message);
					}
				}, RETRY_INTERVAL);
				
				return true;
			}
		}
		
		Player player = getPlayer();
		if (player != null) {
			// Update the marker so they can get back, if there is no marker set.
			if (!isActive) {
				placeMarker(getLocation().getBlock());
			}
			
			Location playerLocation = player.getLocation();
			targetLocation.setYaw(playerLocation.getYaw());
			targetLocation.setPitch(playerLocation.getPitch());
			player.teleport(tryFindPlaceToStand(targetLocation));
			castMessage(message);
		}
		return true;
	}

	protected boolean placeMarker(Block target)
	{
		if (target == null)
		{
			return false;
		}
		Block targetBlock = target.getRelative(BlockFace.UP);
		if (targetBlock.getType() != Material.AIR)
		{
			targetBlock = getFaceBlock();
		}
		if (targetBlock.getType() != Material.AIR)
		{
			return false;
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

		return true;
	}
	
	@Override
	public void onLoad(ConfigurationSection node)
	{
		isActive = node.getBoolean("active", false);
		location = ConfigurationUtils.getLocation(node, "location");
	}

	@Override
	public void onSave(ConfigurationSection node)
	{
		node.set("active", isActive);
		node.set("location", ConfigurationUtils.fromLocation(location));
	}
}
