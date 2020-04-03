package com.elmakers.mine.bukkit.automata;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.magic.Mage;
import com.elmakers.mine.bukkit.utility.RandomUtils;
import com.elmakers.mine.bukkit.utility.WeightedPair;

public class Caster {
    @Nonnull
    private final Deque<WeightedPair<String>> spells;
    private final boolean recast;
    private final boolean undoAll;

    public Caster(@Nonnull AutomatonTemplate automaton, ConfigurationSection configuration) {
        spells = new ArrayDeque<>();
        RandomUtils.populateStringProbabilityMap(spells, configuration, "spells");
        recast = configuration.getBoolean("recast", true);
        undoAll = configuration.getBoolean("undo_all", true);
    }

    public void cast(Mage mage) {
        String castSpell = RandomUtils.weightedRandom(spells);
        if (castSpell.length() > 0) {
            String[] parameters = null;
            Spell spell = null;
            if (!castSpell.equalsIgnoreCase("none")) {
                if (castSpell.contains(" ")) {
                    parameters = StringUtils.split(castSpell, ' ');
                    castSpell = parameters[0];
                    parameters = Arrays.copyOfRange(parameters, 1, parameters.length);
                }
                spell = mage.getSpell(castSpell);
            }
            if (spell != null) {
                spell.cast(parameters);
            }
        }
    }

    public boolean isRecast() {
        return recast;
    }

    public boolean isUndoAll() {
        return undoAll;
    }
}
