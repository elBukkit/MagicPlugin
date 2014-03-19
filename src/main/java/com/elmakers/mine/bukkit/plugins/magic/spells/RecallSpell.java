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
				castMessage("Unknown recall type " + typeString);
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
			if (target == null) {
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
			
			castMessage("Returning you to your marker");
			tryTeleport(location);
			break;
			case DEATH:
				Location deathLocation = mage.getLastDeathLocation();
				if (deathLocation == null)
				{
					sendMessage("No recorded death location. Sorry!");
					return SpellResult.NO_TARGET;
				}

				castMessage("Returning you to your last death location");
				tryTeleport(deathLocation);
				return SpellResult.CAST; 
			case SPAWN:
				castMessage("Returning you to spawn");
				tryTeleport(getWorld().getSpawnLocation());
				break;
			case HOME:
				castMessage("Returning you home");
				tryTeleport(player.getBedSpawnLocation());
				break;
			case WAND:
				List<LostWand> lostWands = mage.getLostWands();
				Location wandLocation = lostWands.size() > 0 ? lostWands.get(0).getLocation() : null;
				if (wandLocation == null)
				{
					sendMessage("No recorded lost wands for you");
					return SpellResult.NO_TARGET;
				}
				
				castMessage("Returning you to your lost wand");
				tryTeleport(wandLocation);
				return SpellResult.CAST; 
			default:
				return SpellResult.FAIL;
		}

		return SpellResult.CAST;
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
