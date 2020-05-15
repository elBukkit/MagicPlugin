package com.elmakers.mine.bukkit.spell;

import com.elmakers.mine.bukkit.api.magic.Trigger;

public class TriggeredSpell {
    private final String spellKey;
    private final Trigger trigger;

    public TriggeredSpell(String spellKey, Trigger trigger) {
        this.spellKey = spellKey;
        this.trigger = trigger;
    }

    public String getSpellKey() {
        return spellKey;
    }

    public Trigger getTrigger() {
        return trigger;
    }
}
