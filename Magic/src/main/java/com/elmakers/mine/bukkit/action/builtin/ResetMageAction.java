package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.tasks.DeleteMageTask;

public class ResetMageAction extends BaseSpellAction
{
    @Override
    public SpellResult perform(CastContext context)
    {
        // This always needs to be delayed so we're not resetting while in a spell
        final Mage mage = context.getMage();
        final MageController controller = context.getController();
        Plugin plugin = controller.getPlugin();
        plugin.getServer().getScheduler().runTaskLater(plugin, new DeleteMageTask(mage), 1);
        return SpellResult.CAST;
    }

    @Override
    public boolean isUndoable()
    {
        return false;
    }
}
