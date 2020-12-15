package com.elmakers.mine.bukkit.world.spawn.builtin;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.entity.EntityData;
import com.elmakers.mine.bukkit.utility.RandomUtils;
import com.elmakers.mine.bukkit.utility.WeightedPair;
import com.elmakers.mine.bukkit.world.spawn.SpawnOption;
import com.elmakers.mine.bukkit.world.spawn.SpawnOptionParser;
import com.elmakers.mine.bukkit.world.spawn.SpawnResult;
import com.elmakers.mine.bukkit.world.spawn.SpawnRule;

public class ReplaceRule extends SpawnRule {
    // Keep these separate for efficiency
    protected SpawnOption replaceWith;
    protected Deque<WeightedPair<SpawnOption>> replaceProbability;

    @Override
    public void finalizeLoad(String worldName) {
        // Legacy support
        if (!parameters.contains("type")) {
            parameters.set("type", parameters.get("replace_type"));
        }
        if (!parameters.contains("sub_type")) {
            parameters.set("sub_type", parameters.get("replace_sub_type"));
        }

        ConfigurationSection section = parameters.getConfigurationSection("type");
        SpawnOptionParser parser = SpawnOptionParser.getInstance(controller);
        if (section == null) {
            replaceWith = parser.parse(parameters.getString("type"));
        } else {
            replaceProbability = new ArrayDeque<>();
            RandomUtils.populateProbabilityMap(parser, replaceProbability, section);
        }

        if (replaceProbability == null && (replaceWith == null || replaceWith.getType() == null)) {
            controller.getLogger().warning("Error reading in configuration for custom mob in " + worldName + " for rule " + key);
            return;
        }
        String replaceDescription;
        if (replaceWith != null) {
            replaceDescription = replaceWith.describe();
        } else {
            List<String> names = new ArrayList<>();
            for (WeightedPair<SpawnOption> option : replaceProbability) {
                names.add(option.getValue().describe());
            }
            replaceDescription = StringUtils.join(names, ",");
        }
        logSpawnRule("Replacing " + getTargetEntityTypeName() + " in " + worldName + " with " + replaceDescription);
    }

    @Override
    @Nonnull
    public SpawnResult onProcess(Plugin plugin, LivingEntity entity) {
        if (replaceWith == null && replaceProbability == null) return SpawnResult.SKIP;
        SpawnOption option = this.replaceWith;
        if (replaceProbability != null) {
            option = RandomUtils.weightedRandom(replaceProbability);
        }
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

            Entity spawned = replacement.spawn(entity.getLocation());
            if (replacement == null) {
                return SpawnResult.SKIP;
            }
        }

        return option.getType();
    }
}
