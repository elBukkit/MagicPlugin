package com.elmakers.mine.bukkit.api.magic;

import java.util.Collection;

public interface CasterProperties extends MagicConfigurable {
    boolean hasSpell(String spellKey);
    Collection<String> getSpells();
    Mage getMage();
    void removeMana(float mana);
    float getMana();
    int getManaMax();
    void setMana(float mana);
    void setManaMax(int manaMax);
    int getManaRegeneration();
    void setManaRegeneration(int manaRegeneration);
    ProgressionPath getPath();
    boolean canProgress();
}
