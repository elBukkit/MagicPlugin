package com.elmakers.mine.bukkit.world.spawn;

import javax.annotation.Nullable;

import com.elmakers.mine.bukkit.utility.ValueParser;

public class CastSpellParser extends ValueParser<CastSpell> {
    private static CastSpellParser instance = null;

    public static CastSpellParser getInstance() {
        if (instance == null) {
            instance = new CastSpellParser();
        }
        return instance;
    }

    @Override
    @Nullable
    public CastSpell parse(String value) {
        return new CastSpell(value);
    }
}
