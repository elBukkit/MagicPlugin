package com.elmakers.mine.bukkit.wand;

import java.util.ArrayDeque;
import java.util.Deque;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.wand.LostWand;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.RunnableJob;

public class WandCleanupRunnable extends RunnableJob {
    private final Deque<LostWand> lostWands = new ArrayDeque<>();
    private final World world;
    private final MagicAPI api;
    private final String owner;
    private final boolean removeAll;
    private final boolean check;

    public WandCleanupRunnable(MagicAPI api, World world, String owner, boolean check) {
        super(api.getLogger());
        this.world = world;
        this.api = api;
        lostWands.addAll(api.getLostWands());
        this.removeAll = owner != null && owner.equals("ALL");
        this.check = check;
        this.owner = owner == null ? "" : owner;
    }

    public WandCleanupRunnable(MagicAPI api, World world) {
        super(api.getLogger());
        this.world = world;
        this.api = api;
        this.removeAll = true;
        this.check = false;
        this.owner = "";
        lostWands.addAll(api.getLostWands());
    }

    @Override
    public void finish() {
        super.finish();
        lostWands.clear();
    }

    @Override
    public void run() {
        if (lostWands.isEmpty()) {
            finish();
            return;
        }
        LostWand lostWand = lostWands.getFirst();
        Location location = lostWand.getLocation();
        if (world != null && location.getWorld() != world) {
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
        if (!CompatibilityLib.getCompatibilityUtils().checkChunk(location)) {
            return;
        }

        Chunk chunk = location.getChunk();
        Entity[] entities = chunk.getEntities();
        for (Entity entity : entities) {
            if (!(entity instanceof Item)) continue;
            Item item = (Item)entity;
            ItemStack itemStack = item.getItemStack();
            if (api.isWand(itemStack)) {
                String lostId = Wand.getWandId(itemStack);
                if (lostId != null && lostWand.getId().equals(lostId)) {
                    String description = check ? "Found" : "Removed";
                    logger.info(description + " lost wand " + lostWand.getName()
                            + " (" + lostWand.getOwner() + "), id " + lostWand.getId() + " in "
                            + location.getWorld().getName()
                            + " at " + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ());
                    if (check) {
                        if (lostWand instanceof com.elmakers.mine.bukkit.wand.LostWand) {
                            ((com.elmakers.mine.bukkit.wand.LostWand)lostWand).setLocation(entity.getLocation());
                        }
                    } else {
                        api.removeLostWand(lostWand.getId());
                        item.remove();
                    }
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
