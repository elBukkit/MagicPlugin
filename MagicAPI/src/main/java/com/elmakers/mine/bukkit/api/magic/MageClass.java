package com.elmakers.mine.bukkit.api.magic;

import java.util.Collection;
import javax.annotation.Nullable;

import com.elmakers.mine.bukkit.api.spell.CooldownReducer;
import com.elmakers.mine.bukkit.api.spell.CostReducer;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;

public interface MageClass extends CasterProperties, CostReducer, CooldownReducer {
    @Override
    Collection<String> getSpells();
    @Override
    boolean addSpell(String spellKey);
    boolean addBrush(String brushKey);
    boolean removeSpell(String spellKey);
    boolean removeBrush(String brushKey);
    String getKey();
    @Override
    ProgressionPath getPath();
    String getName();

    /**
     * Use getSpellTemplate instead.
     */
    @Deprecated
    @Nullable
    SpellTemplate getBaseSpell(String spellKey);
}
