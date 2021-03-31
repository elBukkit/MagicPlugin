package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.RandomUtils;
import com.elmakers.mine.bukkit.utility.WeightedPair;
import com.elmakers.mine.bukkit.world.populator.MagicChunkPopulator;

public class ChestPopulator extends MagicChunkPopulator {
    private final Deque<WeightedPair<Integer>> baseProbability = new ArrayDeque<>();
    private final Deque<WeightedPair<String>> itemProbability = new ArrayDeque<>();
    private final Set<Material> removeItems = new HashSet<>();
    private boolean clearItems = false;
    private int maxY = 255;
    private int minY = 0;

    @Override
    public boolean onLoad(ConfigurationSection config) {
        baseProbability.clear();
        itemProbability.clear();

        maxY = config.getInt("max_y", maxY);
        minY = config.getInt("min_y", minY);

        // Fetch base probabilities
        ConfigurationSection base = config.getConfigurationSection("base_probability");
        if (base != null) {
            RandomUtils.populateIntegerProbabilityMap(baseProbability, base);
        }

        // Fetch wand probabilities
        ConfigurationSection wands = config.getConfigurationSection("item_probability");
        if (wands != null) {
            RandomUtils.populateStringProbabilityMap(itemProbability, wands);
        }
        clearItems = config.getBoolean("clear_items", false);
        List<String> removeItemKeys = ConfigurationUtils.getStringList(config, "remove_items");
        if (removeItemKeys != null && !removeItemKeys.isEmpty()) {
            for (String removeItemKey : removeItemKeys) {
                try {
                    Material itemType = Material.valueOf(removeItemKey.toUpperCase());
                    removeItems.add(itemType);
                } catch (Exception ex) {
                    controller.getLogger().warning("Invalid material in remove_items list: " + removeItemKey);
                }
            }
        }

        return clearItems || (baseProbability.size() > 0 && itemProbability.size() > 0) || !removeItems.isEmpty();
    }

    @Nullable
    protected int clearChest(Chest chest) {
        int itemsRemoved = 0;
        if (clearItems) {
            chest.getInventory().clear();
        } else if (!removeItems.isEmpty()) {
            Inventory inventory = chest.getInventory();
            for (int i = 0; i < inventory.getSize(); i++) {
                ItemStack item = inventory.getItem(i);
                if (item != null && removeItems.contains(item.getType())) {
                    inventory.setItem(i, null);
                    itemsRemoved++;
                }
            }
        }
        return itemsRemoved;
    }

    @Nullable
    protected String[] populateChest(Chest chest) {
        String[] itemsAdded = null;
        if (!baseProbability.isEmpty()) {
            // First determine how many items to add
            Integer itemCount = RandomUtils.weightedRandom(baseProbability);
            itemsAdded = new String[itemCount];
            for (int i = 0; i < itemCount; i++) {
                String wandName = RandomUtils.weightedRandom(itemProbability);
                ItemStack item = controller.createItem(wandName);
                if (item != null) {
                    chest.getInventory().addItem(item);
                } else {
                    wandName = "*" + wandName;
                }
                itemsAdded[i] = wandName;
            }
        }

        return itemsAdded;
    }

    public void setMaxY(int maxY) {
        this.maxY = maxY;
    }

    @Override
    public void populate(World world, Random random, Chunk chunk) {
        BlockState[] tiles = chunk.getTileEntities();
        for (BlockState block : tiles) {
            if (block.getType() != Material.CHEST || !(block instanceof Chest)) continue;
            if (block.getY() < minY || block.getY() > maxY) continue;

            Chest chest = (Chest)block;
            if (block.getType() == Material.CHEST) {
                String[] itemsAdded = populateChest(chest);
                int itemsRemoved = clearChest(chest);
                if (controller != null) {
                    Location location = block.getLocation();
                    if (clearItems) {
                        controller.info("Cleared chest at: " + location.getWorld().getName() + "," + location.toVector());
                    } else if (itemsRemoved > 0) {
                        controller.info("Removed " + itemsRemoved + " items from chest at: " + location.getWorld().getName() + "," + location.toVector());
                    }
                    if (itemsAdded != null && itemsAdded.length > 0) {
                        controller.info("Added items to chest: " + StringUtils.join(itemsAdded, ", ") + " at "
                                + location.getWorld().getName() + "," + location.toVector());
                    }
                }
            }
        }
    }
}
