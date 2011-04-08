package com.elmakers.mine.bukkit.magic;
import java.util.List;

import com.elmakers.mine.bukkit.magic.dao.SpellVariant;


public interface SpellProvider
{
    public List<Spell> getSpells();
    public List<SpellVariant> getDefaultVariants(String spellSet);
}
