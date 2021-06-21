package com.elmakers.mine.bukkit.automata;

import java.util.ArrayDeque;
import java.util.Deque;

import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.batch.SimulateBatch;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.utility.random.WeightedPair;

public class AutomatonLevel {
    private final Deque<WeightedPair<String>> tickSpells;
    private final Deque<WeightedPair<String>> deathSpells;
    private final String spellParameters;
    private final Integer delay;
    private final Integer moveRange;
    private final Integer dropXp;
    private final Integer birthRange;
    private final Integer liveRange;
    private final Integer radius;
    private final Integer yRadius;
    private final Integer maxBlocks;
    private final Integer minBlocks;

    public AutomatonLevel(int level, Integer[] levels, ConfigurationSection template) {
        int levelIndex = 0;
        int nextLevelIndex = 0;
        float distance = 0;
        if (levels != null) {
            for (levelIndex = 0; levelIndex < levels.length; levelIndex++) {
                if (level == levels[levelIndex] || levelIndex == levels.length - 1) {
                    nextLevelIndex = levelIndex;
                    distance = 0;
                    break;
                }

                if (level > levels[levelIndex]) {
                    nextLevelIndex = levelIndex + 1;
                    int previousLevel = levels[levelIndex];
                    int nextLevel = levels[nextLevelIndex];
                    distance = (float)(level - previousLevel) / (float)(nextLevel - previousLevel);
                }
            }
        }

        if (template.contains("cast")) {
            tickSpells = new ArrayDeque<>();
            RandomUtils.populateStringProbabilityMap(tickSpells, template.getConfigurationSection("cast"), levelIndex, nextLevelIndex, distance);
        } else {
            tickSpells = null;
        }
        if (template.contains("death_cast")) {
            deathSpells = new ArrayDeque<>();
            RandomUtils.populateStringProbabilityMap(deathSpells, template.getConfigurationSection("death_cast"), levelIndex, nextLevelIndex, distance);
        } else {
            deathSpells = null;
        }
        if (template.contains("delay")) {
            delay = (int)RandomUtils.lerp(StringUtils.split(template.getString("delay"), ','), levelIndex, nextLevelIndex, distance);
        } else {
            delay = null;
        }
        if (template.contains("move")) {
            moveRange = (int)RandomUtils.lerp(StringUtils.split(template.getString("move"), ','), levelIndex, nextLevelIndex, distance);
        } else {
            moveRange = null;
        }
        if (template.contains("drop_xp")) {
            dropXp = (int)RandomUtils.lerp(StringUtils.split(template.getString("drop_xp"), ','), levelIndex, nextLevelIndex, distance);
        } else {
            dropXp = null;
        }
        if (template.contains("birth_range")) {
            birthRange = (int)RandomUtils.lerp(StringUtils.split(template.getString("birth_range"), ','), levelIndex, nextLevelIndex, distance);
        } else {
            birthRange = null;
        }
        if (template.contains("live_range")) {
            liveRange = (int)RandomUtils.lerp(StringUtils.split(template.getString("live_range"), ','), levelIndex, nextLevelIndex, distance);
        } else {
            liveRange = null;
        }
        if (template.contains("radius")) {
            radius = (int)RandomUtils.lerp(StringUtils.split(template.getString("radius"),','), levelIndex, nextLevelIndex, distance);
        } else {
            radius = null;
        }
        if (template.contains("yradius")) {
            yRadius = (int)RandomUtils.lerp(StringUtils.split(template.getString("yradius"), ','), levelIndex, nextLevelIndex, distance);
        } else {
            yRadius = null;
        }
        if (template.contains("min_blocks")) {
            minBlocks = (int)RandomUtils.lerp(StringUtils.split(template.getString("min_blocks"), ','), levelIndex, nextLevelIndex, distance);
        } else {
            minBlocks = null;
        }
        if (template.contains("max_blocks")) {
            maxBlocks = (int)RandomUtils.lerp(StringUtils.split(template.getString("max_blocks"), ','), levelIndex, nextLevelIndex, distance);
        } else {
            maxBlocks = null;
        }
        spellParameters = template.getString("spell_parameters");
    }

    public int getYRadius(int yRadius) {
        return this.yRadius != null ? this.yRadius : yRadius;
    }

    public int getRadius(int radius) {
        return this.radius != null ? this.radius : radius;
    }

    public int getMinBlocks(int minBlocks) {
        return this.minBlocks != null ? this.minBlocks : minBlocks;
    }

    public int getMaxBlocks(int maxBlocks) {
        return this.maxBlocks != null ? this.maxBlocks : maxBlocks;
    }

    public int getLiveRangeSquared(int liveRangeSquared) {
        return this.liveRange != null ? this.liveRange * this.liveRange : liveRangeSquared;
    }

    public int getBirthRangeSquared(int birthRangeSquared) {
        return this.birthRange != null ? this.birthRange * this.birthRange : birthRangeSquared;
    }

    public int getDelay(int delay) {
        return this.delay != null ? this.delay : delay;
    }

    public int getMoveRangeSquared(int moveRangeSquared) {
        return this.moveRange != null ? this.moveRange * this.moveRange : moveRangeSquared;
    }

    public int getDropXp(int dropXp) {
        return this.dropXp != null ? this.dropXp : dropXp;
    }

    public void onDeath(Mage mage, MaterialAndData birthMaterial) {
        if (deathSpells != null && deathSpells.size() > 0) {
            String deathSpell = RandomUtils.weightedRandom(deathSpells);
            if (deathSpell != null && deathSpell.length() > 0) {
                castSpell(deathSpell, mage, birthMaterial);
            }
        }
    }

    public void onTick(Mage mage, MaterialAndData birthMaterial) {
        if (tickSpells != null && tickSpells.size() > 0) {
            String tickSpell = RandomUtils.weightedRandom(tickSpells);
            if (tickSpell.length() > 0) {
                castSpell(tickSpell, mage, birthMaterial);
            }
        }
    }

    protected void castSpell(String spellCommand, Mage mage, MaterialAndData birthMaterial) {
        if (spellCommand == null || spellCommand.length() == 0 || spellCommand.equals("none")) return;

        spellCommand = spellCommand + " " + spellParameters;
        String[] pieces = null;
        if (spellCommand.contains(" ")) {
            pieces = StringUtils.split(spellCommand, ' ');
            spellCommand = pieces[0];
        }

        String[] parameters = null;
        if (pieces != null && pieces.length > 1 && birthMaterial != null && birthMaterial.getMaterial() != null) {
            parameters = new String[pieces.length - 1];
            for (int i = 1; i < pieces.length; i++) {
                parameters[i - 1] = pieces[i].replace("$birth", birthMaterial.getMaterial().name().toLowerCase());
            }
        }

        if (SimulateBatch.DEBUG) {
            mage.getController().getLogger().info("Casting " + spellCommand + " " + StringUtils.join(parameters, ' '));
        }

        Spell spell = mage.getSpell(spellCommand);
        if (spell != null) {
            spell.cast(parameters);
        }
    }
}
