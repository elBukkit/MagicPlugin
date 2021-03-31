package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.utility.RandomUtils;
import com.elmakers.mine.bukkit.utility.WeightedPair;
import com.elmakers.mine.bukkit.world.populator.MagicChunkPopulator;

public class ChestPopulator extends MagicChunkPopulator {
    private final Deque<WeightedPair<Integer>> baseProbability = new ArrayDeque<>();
    private final Deque<WeightedPair<String>> itemProbability = new ArrayDeque<>();
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

        return clearItems || (baseProbability.size() > 0 && itemProbability.size() > 0);
    }

    @Nullable
    protected String[] populateChest(Chest chest) {
        if (clearItems) {
            chest.getInventory().clear();
        }
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
                if (controller != null) {
                    if (itemsAdded != null && itemsAdded.length > 0) {
                        Location location = block.getLocation();
                        controller.info("Added items to chest: " + StringUtils.join(itemsAdded, ", ") + " at "
                                + location.getWorld().getName() + "," + location.toVector());
                    } else if (clearItems) {
                        Location location = block.getLocation();
                        controller.info("Cleared chest at: " + location.getWorld().getName() + "," + location.toVector());
                    }
                }
            }
        }
    }
}
