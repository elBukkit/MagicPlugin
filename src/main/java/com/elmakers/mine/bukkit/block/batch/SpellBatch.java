package com.elmakers.mine.bukkit.block.batch;

import com.elmakers.mine.bukkit.spell.UndoableSpell;

public abstract class SpellBatch extends UndoableBatch {
    protected final UndoableSpell spell;

    public SpellBatch(UndoableSpell spell) {
        super(spell.getMage(), spell.getUndoList());
        this.spell = spell;
    }

    @Override
    public void finish() {
        if (!finished) {
            spell.sendMessage(spell.getMessage("cast_finish"));
            super.finish();
        }
    }

    public UndoableSpell getSpell() {
        return spell;
    }
}
