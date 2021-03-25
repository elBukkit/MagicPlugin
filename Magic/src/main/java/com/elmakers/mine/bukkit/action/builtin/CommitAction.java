package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.UndoQueue;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class CommitAction extends BaseSpellAction {
    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
    }

    @Override
    public SpellResult perform(CastContext context) {
        UndoQueue queue = context.getMage().getUndoQueue();
        int totalSize = queue.getSize();
        if (totalSize == 0) {
            return SpellResult.NO_ACTION;
        }

        boolean moreToUndo = true;
        while (context.getWorkAllowed() > 0) {
            context.addWork(1);
            if (!queue.commitNext()) {
                moreToUndo = false;
                break;
            }
        }

        return moreToUndo ? SpellResult.PENDING : SpellResult.CAST;
    }
}
