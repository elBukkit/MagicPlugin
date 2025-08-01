package com.elmakers.mine.bukkit.world.spawn.builtin;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import javax.annotation.Nonnull;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.entity.EntityData;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.StringUtils;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.utility.random.WeightedPair;
import com.elmakers.mine.bukkit.world.spawn.SpawnOption;
import com.elmakers.mine.bukkit.world.spawn.SpawnOptionParser;
import com.elmakers.mine.bukkit.world.spawn.SpawnResult;
import com.elmakers.mine.bukkit.world.spawn.SpawnRule;

public class ReplaceRule extends SpawnRule {
    // Keep these separate for efficiency
    protected Deque<WeightedPair<SpawnOption>> replaceProbability;
    protected int yOffset;
    protected boolean atHighestBlock;

    @Override
    public void finalizeLoad(String worldName) {
        // Legacy support
        if (!parameters.contains("type")) {
            parameters.set("type", parameters.get("replace_type"));
        }
        if (!parameters.contains("sub_type")) {
            parameters.set("sub_type", parameters.get("replace_sub_type"));
        }

        replaceProbability = new ArrayDeque<>();
        SpawnOptionParser parser = SpawnOptionParser.getInstance(controller);
        RandomUtils.populateProbabilityMap(parser, replaceProbability, parameters, "type");
        if (replaceProbability.isEmpty()) {
            controller.getLogger().warning("Error reading in configuration for custom mob in " + worldName + " for rule " + key);
            return;
        }
        List<String> names = new ArrayList<>();
        for (WeightedPair<SpawnOption> option : replaceProbability) {
            names.add(option.getValue().describe());
        }
        String replaceDescription = StringUtils.join(names, ",");
        replaceDescription = ChatColor.stripColor(replaceDescription);
        atHighestBlock = parameters.getBoolean("highest_block", false);
        yOffset = parameters.getInt("y_offset");
        logSpawnRule("Replacing " + getTargetEntityTypeName() + " in " + worldName + " with " + replaceDescription);
    }

    @Override
    @Nonnull
    public SpawnResult onProcess(Plugin plugin, LivingEntity entity) {
        if (replaceProbability.isEmpty()) return SpawnResult.SKIP;
        SpawnOption option = RandomUtils.weightedRandom(replaceProbability);
        if (option == null) return SpawnResult.SKIP;
        if (option.getType() == SpawnResult.REPLACE) {
            EntityData replacement = option.getReplacement();

            // This makes replacing the same type of mob have better balance,
            // particularly with mob spawners
            if (entity.getType() == replacement.getType()) {
                replacement.modify(entity);
                // Don't cancel spawning in this case, but also stop further processing
                // since that is how a replacement normally works.
                return SpawnResult.STOP;
            }

            Location location = entity.getLocation();
            if (atHighestBlock) {
                location.setY(location.getWorld().getHighestBlockYAt(location));
            }
            location.setY(location.getY() + yOffset);
            Entity spawned = replacement.spawn(location);
            if (spawned == null) {
                return SpawnResult.SKIP;
            }

            // Make sure to flag mobs as non-persistent, unless they are explicitly set to persistent
            // Otherwise we get pile-ups of mobs
            if (!replacement.isPersistent()) {
                CompatibilityLib.getCompatibilityUtils().setPersist(spawned, false);
                CompatibilityLib.getCompatibilityUtils().setRemoveWhenFarAway(spawned, true);
            }
        }

        return option.getType();
    }
}
