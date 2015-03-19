package com.elmakers.mine.bukkit.batch;

import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.spell.UndoableSpell;

public abstract class SpellBatch extends UndoableBatch implements com.elmakers.mine.bukkit.api.batch.SpellBatch {
    protected final UndoableSpell spell;

    public SpellBatch(UndoableSpell spell) {
        super(spell.getMage(), spell.getUndoList());
        this.spell = spell;
    }

    @Override
    public void finish() {
        if (!finished) {
            spell.castMessage(spell.getMessage("cast_finish"));
            super.finish();
        }
    }

    public Spell getSpell() {
        return spell;
    }
}
