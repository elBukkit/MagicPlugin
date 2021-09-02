package com.elmakers.mine.bukkit.world.spawn.builtin;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.bukkit.World.Environment;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.utility.random.WeightedPair;
import com.elmakers.mine.bukkit.world.CastSpell;
import com.elmakers.mine.bukkit.world.CastSpellParser;
import com.elmakers.mine.bukkit.world.spawn.SpawnResult;
import com.elmakers.mine.bukkit.world.spawn.SpawnRule;

public class CastRule extends SpawnRule {
    protected List<CastSpell>        spells;
    protected Deque<WeightedPair<CastSpell>> spellProbability;
    protected int                     yOffset;

    @Override
    public void finalizeLoad(String worldName) {
        yOffset = parameters.getInt("y_offset", 0);
        Collection<String> spells = parameters.getStringList("spells");
        if (spells == null || spells.size() == 0) {
            ConfigurationSection spellMap = parameters.getConfigurationSection("spells");
            spellProbability = new ArrayDeque<>();
            RandomUtils.populateProbabilityMap(CastSpellParser.getInstance(), spellProbability, spellMap);
            if (spellProbability.isEmpty()) {
                return;
            }
            logSpawnRule("Casting one of " + StringUtils.join(this.spellProbability, ",") + " on " + getTargetEntityTypeName() + " in " + worldName);
        } else {
            this.spells = new ArrayList<>();
            for (String spellName : spells) {
                this.spells.add(new CastSpell(spellName));
            }
            logSpawnRule("Casting " + StringUtils.join(this.spells, ",") + " on " + getTargetEntityTypeName() + " in " + worldName);
        }
    }

    @Override
    @Nonnull
    public SpawnResult onProcess(Plugin plugin, LivingEntity entity) {
        int y = entity.getLocation().getBlockY() + yOffset;
        if (y > 250) y = 250;
        if (entity.getWorld().getEnvironment() == Environment.NETHER && y > 118) {
            y = 118;
        }

        String[] standardParameters = {
            "pworld", entity.getLocation().getWorld().getName(),
            "px", Integer.toString(entity.getLocation().getBlockX()),
            "py", Integer.toString(y),
            "pz", Integer.toString(entity.getLocation().getBlockZ()),
            "quiet", "true"
        };

        if (spells == null) {
            spells = new ArrayList<>();
        }
        if (spellProbability != null) {
            CastSpell spell = RandomUtils.weightedRandom(spellProbability);
            spells.clear();
            spells.add(spell);
        }
        boolean casted = false;
        for (CastSpell spell : spells) {
            if (spell == null || spell.isEmpty()) {
                SpawnResult result = spell.getSpawnResult();
                if (result != SpawnResult.SKIP) {
                    return result;
                }
                continue;
            }
            String[] fullParameters = new String[spell.getParameters().length + standardParameters.length];
            for (int index = 0; index < standardParameters.length; index++) {
                fullParameters[index] = standardParameters[index];

            }
            for (int index = 0; index < spell.getParameters().length; index++) {
                fullParameters[index  + standardParameters.length] = spell.getParameters()[index];
            }

            casted = controller.cast(spell.getName(), fullParameters) || casted;
            controller.info("Spawn rule casting: " + spell.getName() + " " + StringUtils.join(fullParameters, ' ') + " at " + entity.getLocation().toVector());
        }

        return casted ? SpawnResult.REPLACE : SpawnResult.REMOVE;
    }
}
