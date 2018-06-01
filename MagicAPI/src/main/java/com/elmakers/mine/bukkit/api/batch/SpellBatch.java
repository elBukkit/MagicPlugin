package com.elmakers.mine.bukkit.api.batch;

import javax.annotation.Nullable;

import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.spell.Spell;

public interface SpellBatch extends Batch {
    @Nullable
    Spell getSpell();
    @Nullable
    UndoList getUndoList();
}
