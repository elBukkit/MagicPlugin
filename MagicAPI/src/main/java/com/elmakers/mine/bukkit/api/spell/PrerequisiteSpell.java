package com.elmakers.mine.bukkit.api.spell;

import com.elmakers.mine.bukkit.api.wand.Wand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Represents a spell that is required to learn another spell or path.
 */
public class PrerequisiteSpell {

    private final SpellKey spellKey;
    private final long progressLevel;

    public PrerequisiteSpell(String spellKey, long progressLevel) {
        this(new SpellKey(spellKey), progressLevel);
    }

    public PrerequisiteSpell(SpellKey spellKey, long progressLevel) {
        this.spellKey = spellKey;
        this.progressLevel = progressLevel;
    }

    public SpellKey getSpellKey() {
        return spellKey;
    }

    public long getProgressLevel() {
        return progressLevel;
    }

    @Override
    public String toString() {
        return "PrerequisiteSpell{" +
                "spellKey=" + spellKey.getKey() +
                ", progressLevel=" + progressLevel +
                '}';
    }

    public static Collection<PrerequisiteSpell> getMissingRequirements(Wand wand, SpellTemplate spell) {
        Collection<PrerequisiteSpell> missingRequirements = new ArrayList<>(spell.getPrerequisiteSpells());
        if (wand == null) {
            return missingRequirements;
        }
        Iterator<PrerequisiteSpell> it = missingRequirements.iterator();
        while (it.hasNext()) {
            PrerequisiteSpell prereq = it.next();
            Spell mageSpell = wand.getSpell(prereq.getSpellKey().getKey());
            if (isSpellSatisfyingPrerequisite(mageSpell, prereq)) {
                it.remove();
            }
        }
        return missingRequirements;
    }

    public static boolean hasPrerequisites(Wand wand, SpellTemplate spell) {
        if (spell == null) {
            return true;
        }
        if (wand == null) {
            return false;
        }
        for (PrerequisiteSpell prereq : spell.getPrerequisiteSpells()) {
            Spell mageSpell = wand.getSpell(prereq.getSpellKey().getKey());
            if (!isSpellSatisfyingPrerequisite(mageSpell, prereq)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isSpellSatisfyingPrerequisite(Spell spell, PrerequisiteSpell prerequisiteSpell) {
        if (spell == null) {
            return false;
        } else if (!spell.getController().isSpellProgressionEnabled()) {
            return spell.getSpellKey().getLevel() >= prerequisiteSpell.getSpellKey().getLevel();
        } else {
            return spell.getSpellKey().getLevel() > prerequisiteSpell.getSpellKey().getLevel()
                || (spell.getProgressLevel() >= prerequisiteSpell.getProgressLevel()
                && spell.getSpellKey().getLevel() == prerequisiteSpell.getSpellKey().getLevel());
        }
    }

}
