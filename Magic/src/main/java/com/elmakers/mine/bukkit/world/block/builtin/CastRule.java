package com.elmakers.mine.bukkit.world.block.builtin;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Random;
import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.utility.RandomUtils;
import com.elmakers.mine.bukkit.utility.WeightedPair;
import com.elmakers.mine.bukkit.world.BlockResult;
import com.elmakers.mine.bukkit.world.CastSpell;
import com.elmakers.mine.bukkit.world.CastSpellParser;
import com.elmakers.mine.bukkit.world.block.BlockRule;

public class CastRule extends BlockRule {
    protected List<CastSpell>        spells;
    protected Deque<WeightedPair<CastSpell>> spellProbability;

    @Override
    public boolean onLoad(ConfigurationSection parameters) {
        Collection<String> spells = parameters.getStringList("spells");
        if (spells == null || spells.size() == 0) {
            ConfigurationSection spellMap = parameters.getConfigurationSection("spells");
            spellProbability = new ArrayDeque<>();
            RandomUtils.populateProbabilityMap(CastSpellParser.getInstance(), spellProbability, spellMap);
            if (spellProbability.isEmpty()) {
                return false;
            }
            logBlockRule("Casting one of " + StringUtils.join(this.spellProbability, ","));
        } else {
            this.spells = new ArrayList<>();
            for (String spellName : spells) {
                this.spells.add(new CastSpell(spellName));
            }
            logBlockRule("Casting " + StringUtils.join(this.spells, ","));
        }
        return !spells.isEmpty() || !spellProbability.isEmpty();
    }

    @Override
    @Nonnull
    public BlockResult onHandle(Block block, Random random, Player cause) {
        String[] standardParameters = {
            "tworld", block.getLocation().getWorld().getName(),
            "tx", Integer.toString(block.getLocation().getBlockX()),
            "ty", Integer.toString(block.getLocation().getBlockY()),
            "tz", Integer.toString(block.getLocation().getBlockZ()),
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
        Mage mage = controller.getMage(cause);
        boolean casted = false;
        for (CastSpell castSpell : spells) {
            if (castSpell.isEmpty()) {
                BlockResult result = castSpell.getBlockResult();
                if (result != BlockResult.SKIP) {
                    return result;
                }
                continue;
            }
            Spell spell = mage.getSpell(castSpell.getName());
            if (spell == null) continue;

            String[] fullParameters = new String[castSpell.getParameters().length + standardParameters.length];
            for (int index = 0; index < standardParameters.length; index++) {
                fullParameters[index] = standardParameters[index];

            }
            for (int index = 0; index < castSpell.getParameters().length; index++) {
                fullParameters[index  + standardParameters.length] = castSpell.getParameters()[index];
            }

            casted = spell.cast(fullParameters) || casted;
        }

        return casted ? BlockResult.REPLACED_DROPS : BlockResult.REMOVE_DROPS;
    }
}
