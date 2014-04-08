package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.blocks.MaterialAndData;
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
		
		MaterialAndData birthMaterial = new MaterialAndData(target);
		birthMaterial = parameters.getMaterialAndData("material", birthMaterial);
		birthMaterial = parameters.getMaterialAndData("m", birthMaterial);
		
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
			birthMaterial = new MaterialAndData(targetLocation.getBlock());
		}
		
		Material deathMaterial = parameters.getMaterial("death_material", Material.AIR);
		
		// use the target location with the player's facing
		Location location = getLocation();
		Location targetLocation = target.getLocation();
		targetLocation.setPitch(location.getPitch());
		targetLocation.setYaw(location.getYaw());

		// Look for relative-positioned chests that define the rulesets.

		Set<Integer> birthCounts = new HashSet<Integer>();
		Set<Integer> liveCounts = new HashSet<Integer>();
		
		Double dlcxValue = parameters.getDouble("dlcx", null);
		Double dlcyValue = parameters.getDouble("dlcy", null);
		Double dlczValue = parameters.getDouble("dlcz", null);
		if (dlcxValue != null || dlczValue != null || dlczValue != null) {
			Location liveChestLocation = targetLocation.clone().add(new Vector(dlcxValue == null ? 0 : dlcxValue, dlcyValue == null ? 0 : 
				dlcyValue, dlczValue == null ? 0 : dlczValue));
			Block chestBlock = liveChestLocation.getBlock();
			BlockState chestState = chestBlock.getState();
			if (chestState instanceof InventoryHolder) {
				ItemStack[] items = ((InventoryHolder)chestState).getInventory().getContents();
				for (int index = 0; index < items.length; index++) {
					if (items[index] != null && items[index].getType() != Material.AIR) {
						liveCounts.add(index + 1);
						controller.getLogger().info("SimulateSpell: Added live rules for index " + (index + 1)  + " from chest at " + liveChestLocation.toVector());
					}
				}
			} else {
				controller.getLogger().warning("SimulateSpell: Chest for live rules not found at " + liveChestLocation.toVector());
			}
		} else if (parameters.containsKey("live_rules")) {
			liveCounts.addAll(parameters.getIntList("live_rules", new ArrayList<Integer>()));
		} else {
			liveCounts.add(2);
			liveCounts.add(3);
		}
		
		Double dbcxValue = parameters.getDouble("dbcx", null);
		Double dbcyValue = parameters.getDouble("dbcy", null);
		Double dbczValue = parameters.getDouble("dbcz", null);
		if (dbcxValue != null || dbczValue != null || dbczValue != null) {
			Location birthChestLocation = targetLocation.clone().add(new Vector(dbcxValue == null ? 0 : dbcxValue, dbcyValue == null ? 0 : 
				dbcyValue, dbczValue == null ? 0 : dbczValue));
			Block chestBlock = birthChestLocation.getBlock();
			BlockState chestState = chestBlock.getState();
			if (chestState instanceof InventoryHolder) {
				ItemStack[] items = ((InventoryHolder)chestState).getInventory().getContents();
				for (int index = 0; index < items.length; index++) {
					if (items[index] != null && items[index].getType() != Material.AIR) {
						birthCounts.add(index + 1);
						controller.getLogger().info("SimulateSpell: Added birth rules for index " + (index + 1) + " from chest at " + birthChestLocation.toVector());
					}
				}
			} else {
				controller.getLogger().warning("SimulateSpell: Chest for birth rules not found at " + birthChestLocation.toVector());
			}
		} else if (parameters.containsKey("birth_rules")) {
			birthCounts.addAll(parameters.getIntList("birth_rules", new ArrayList<Integer>()));
		} else {
			birthCounts.add(3);
		}
		
		if (liveCounts.size() == 0 || birthCounts.size() == 0) {
			return SpellResult.FAIL;
		}
		
		final SimulateBatch batch = new SimulateBatch(this, targetLocation, radius, yRadius, birthMaterial, deathMaterial, liveCounts, birthCounts);
		
		boolean includeCommands = parameters.getBoolean("animate", false);
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
			batch.setCommandMoveRange(parameters.getInt("move", 3),  parameters.getBoolean("reload", true));
			
			SimulateBatch.TargetType targetType = null;
			String targetTypeString = parameters.getString("target_types", "");
			if (targetTypeString.length() > 0) {
				try {
					targetType = SimulateBatch.TargetType.valueOf(targetTypeString.toUpperCase());
				} catch (Exception ex) {
					controller.getLogger().warning(ex.getMessage());
				}
			}
			batch.setTargetType(targetType);
			batch.setMinHuntRange(parameters.getInt("target_min_range", 4));
			batch.setMaxHuntRange(parameters.getInt("target_max_range", 128));

			batch.setCast(parameters.getString("cast", ""));
			
			batch.target(targetMode);
		}
		
		batch.setBirthRange(parameters.getInt("birth_range", 0));
		batch.setLiveRange(parameters.getInt("live_range", 0));
		
		// delay is in ms, gets converted.
		int delay = parameters.getInt("delay", 0);
		// 1000 ms in a second, 20 ticks in a second - 1000 / 20 = 50.
		delay /= 50;
		
		boolean success = true;
		if (delay > 0 && taskId == null) {
			taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(controller.getPlugin(), new Runnable() {
				public void run() {
					taskId = null;
					mage.addPendingBlockBatch(batch);
				}
			}, delay);
		} else {
			success = mage.addPendingBlockBatch(batch);
		}
		
		// This is a bit of a hack, but it forces dynmap to show the spell cast direction
		// instead of the target (also for effects), which looks cool with
		// Automata
		clearTarget();
		
		return success ? SpellResult.CAST : SpellResult.FAIL;
	}
}
