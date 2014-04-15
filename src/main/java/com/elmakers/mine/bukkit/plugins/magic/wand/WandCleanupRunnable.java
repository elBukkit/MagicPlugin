package com.elmakers.mine.bukkit.plugins.magic.wand;

import java.util.LinkedList;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.wand.LostWand;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.utility.RunnableJob;

public class WandCleanupRunnable extends RunnableJob {
	private final LinkedList<LostWand> lostWands = new LinkedList<LostWand>();
	private final World world;
	private final MagicAPI api;
	private final String owner;
	private final boolean removeAll;
	
	public WandCleanupRunnable(MagicAPI api, World world, String owner) {
		super(api.getLogger());
		this.world = world;
		this.api = api;
		lostWands.addAll(api.getLostWands());
		this.removeAll = false;
		this.owner = owner == null ? "" : owner;
	}
	
	public WandCleanupRunnable(MagicAPI api, World world) {
		super(api.getLogger());
		this.world = world;
		this.api = api;
		this.removeAll = true;
		this.owner = "";
		lostWands.addAll(api.getLostWands());
	}
	
	public void finish() {
		super.finish();
		lostWands.clear();
	}
	
	public void run() {
		if (lostWands.isEmpty()) {
			finish();
			return;
		}
		LostWand lostWand = lostWands.getFirst();
		Location location = lostWand.getLocation();
		if (world != null && !location.getWorld().getName().equals(world.getName())) {
			lostWands.removeFirst();
			return;
		}
		String lostWandOwner = lostWand.getOwner();
		lostWandOwner = lostWandOwner == null ? "" : lostWandOwner;
		if (!removeAll) {
			// If no owner was specified, skip wands that have any owner
			if (owner.length() == 0 && lostWandOwner.length() > 0) {
				lostWands.removeFirst();
				return;
			}
			// Skip wands that don't match the specified owner
			if (owner.length() > 0 && !lostWandOwner.equals(owner)) {
				lostWands.removeFirst();
				return;
			}
		}
		Chunk chunk = location.getChunk();
		if (!chunk.isLoaded()) {
			chunk.load();
			return;
		}
		
		Entity[] entities = chunk.getEntities();
		for (Entity entity : entities) {
			if (!(entity instanceof Item)) continue;
			Item item = (Item)entity;
			ItemStack itemStack = item.getItemStack();
			if (api.isWand(itemStack)) {
				Wand wand = api.getWand(itemStack);
				if (wand.getId().equals(lostWand.getId())) {
					logger.info("Removed lost wand " + lostWand.getName() + " (" + lostWand.getOwner() + "), id " + lostWand.getId() + " in " +
							location.getWorld().getName() + " at " + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ());					
					api.removeLostWand(lostWand.getId());
					item.remove();
					lostWands.removeFirst();
					return;
				}
			}
		}
		
		lostWands.removeFirst();
		api.removeLostWand(lostWand.getId());
		logger.info("Could not find wand " + lostWand.getName() + " (" + lostWand.getOwner() + "), id " + lostWand.getId() + ", removing from list");
	}
}
