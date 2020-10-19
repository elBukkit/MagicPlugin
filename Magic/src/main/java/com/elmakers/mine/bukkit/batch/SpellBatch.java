package com.elmakers.mine.bukkit.batch;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.spell.UndoableSpell;

public abstract class SpellBatch extends UndoableBatch implements com.elmakers.mine.bukkit.api.batch.SpellBatch {
    protected final UndoableSpell spell;
    protected final CastContext context;

    public SpellBatch(UndoableSpell spell) {
        super(spell.getMage(), spell.getUndoList());
        this.spell = spell;
        this.context = spell.getCurrentCast();
    }

    @Override
    public void finish() {
        if (!finished) {
            if (context != null) {
                context.finish();
            }
            super.finish();
        }
    }

    @Override
    public Spell getSpell() {
        return spell;
    }

    @Override
    public String getName() {
        if (spell == null) return "Unknown";
        return spell.getName();
    }

    @Override
    public UndoList getUndoList() {
        return undoList;
    }

    @Override
    public void cancel() {
        context.cancelEffects();
        spell.cancel();
        finish();
    }
}
