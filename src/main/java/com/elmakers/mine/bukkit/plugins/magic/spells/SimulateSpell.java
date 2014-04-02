package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.blocks.SimulateBatch;
import com.elmakers.mine.bukkit.plugins.magic.BlockSpell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.Target;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class SimulateSpell extends BlockSpell {
	
	private static final int DEFAULT_RADIUS = 32;
	
	private Integer taskId = null;

	@Override
	public SpellResult onCast(ConfigurationNode parameters) {
		Target t = getTarget();
		if (t == null) {
			return SpellResult.NO_TARGET;
		}
		
		Block target = t.getBlock();
		if (target == null) {
			return SpellResult.NO_TARGET;
		}
		
		if (!hasBuildPermission(target)) {
			return SpellResult.INSUFFICIENT_PERMISSION;
		}
		
		int radius = parameters.getInt("radius", DEFAULT_RADIUS);
		radius = parameters.getInt("r", radius);
		radius *= mage.getConstructionMultiplier();
		int yRadius = parameters.getInt("yradius", 0);
		yRadius *= mage.getConstructionMultiplier();
		
		Material birthMaterial = target.getType();
		birthMaterial = parameters.getMaterial("material", birthMaterial);
		birthMaterial = parameters.getMaterial("m", birthMaterial);
		
		// Should this maybe use a brush?
		Double dmxValue = parameters.getDouble("dmx", null);
		Double dmyValue = parameters.getDouble("dmy", null);
		Double dmzValue = parameters.getDouble("dmz", null);
		if (dmxValue != null || dmyValue != null || dmzValue != null) {
			Vector offset = new Vector( 
					dmxValue == null ? 0 : dmxValue, 
					dmyValue == null ? 0 : dmyValue, 
					dmzValue == null ? 0 : dmzValue);
			Location targetLocation = target.getLocation().add(offset);
			if (!targetLocation.getBlock().getChunk().isLoaded()) return SpellResult.FAIL;
			birthMaterial = targetLocation.getBlock().getType();
		}
		
		Material deathMaterial = parameters.getMaterial("death_material", Material.AIR);
		
		Set<Integer> birthCounts = new HashSet<Integer>();
		Set<Integer> liveCounts = new HashSet<Integer>();
		
		if (parameters.containsKey("live_rules")) {
			liveCounts.addAll(parameters.getIntList("live_rules", new ArrayList<Integer>()));
		} else {
			liveCounts.add(2);
			liveCounts.add(3);
		}
		
		if (parameters.containsKey("birth_rules")) {
			birthCounts.addAll(parameters.getIntList("birth_rules", new ArrayList<Integer>()));
		} else {
			birthCounts.add(3);
		}
		
		if (liveCounts.size() == 0 || birthCounts.size() == 0) {
			return SpellResult.FAIL;
		}
		
		// use the target location with the player's facing
		Location location = getLocation();
		Location targetLocation = target.getLocation();
		targetLocation.setPitch(location.getPitch());
		targetLocation.setYaw(location.getYaw());
		final SimulateBatch batch = new SimulateBatch(this, targetLocation, radius, yRadius, birthMaterial, deathMaterial, liveCounts, birthCounts);
		
		boolean includeCommands = parameters.getBoolean("auto", false);
		if (includeCommands) {
			if (mage.getCommandSender() instanceof BlockCommandSender) {
				BlockCommandSender commandBlock = (BlockCommandSender)mage.getCommandSender();
				batch.setCommandBlock(commandBlock.getBlock());
			} else if (target.getType() == Material.COMMAND) {
				batch.setCommandBlock(target);
			}
			
			SimulateBatch.TargetMode targetMode = null;
			String targetModeString = parameters.getString("target_mode", "");
			if (targetModeString.length() > 0) {
				try {
					targetMode = SimulateBatch.TargetMode.valueOf(targetModeString.toUpperCase());
				} catch (Exception ex) {
					controller.getLogger().warning(ex.getMessage());
				}
			}
			batch.setCommandMoveRange(parameters.getInt("move", 3),  parameters.getBoolean("reload", true), targetMode);
		}
		
		// delay is in ms, gets converted.
		int delay = parameters.getInt("delay", 0);
		// 1000 ms in a second, 20 ticks in a second - 1000 / 20 = 50.
		delay /= 50;
		
		if (delay > 0 && taskId == null) {
			taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(controller.getPlugin(), new Runnable() {
				public void run() {
					taskId = null;
					mage.addPendingBlockBatch(batch);
				}
			}, delay);
		} else {
			mage.addPendingBlockBatch(batch);
		}
		return SpellResult.CAST;
	}
}
