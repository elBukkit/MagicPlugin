package com.elmakers.mine.bukkit.api.spell;

/**
 * Represents a spell that is required to learn another spell or path.
 */
public class PrerequisiteSpell {

    private final SpellKey spellKey;
    private final long progressLevel;

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
}
