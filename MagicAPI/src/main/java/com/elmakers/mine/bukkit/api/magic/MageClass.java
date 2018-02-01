package com.elmakers.mine.bukkit.api.magic;

import com.elmakers.mine.bukkit.api.spell.SpellTemplate;

import java.util.Collection;

public interface MageClass extends MagicConfigurable {
    Collection<String> getSpells();
    boolean addSpell(String spellKey);
    boolean addBrush(String brushKey);
    boolean removeSpell(String spellKey);
    boolean removeBrush(String brushKey);
    SpellTemplate getBaseSpell(String spellKey);
}
