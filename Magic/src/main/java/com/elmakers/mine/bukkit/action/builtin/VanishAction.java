package com.elmakers.mine.bukkit.action.builtin;

import java.lang.ref.WeakReference;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class VanishAction extends BaseSpellAction
{
    private static class UndoVanish implements Runnable
    {
        private final WeakReference<Mage> mage;

        public UndoVanish(Mage mage)
        {
            this.mage = new WeakReference<>(mage);
        }

        @Override
        public void run()
        {
            Mage mage = this.mage.get();
            if (mage != null) {
                mage.setVanished(false);
            }
        }
    }

    private boolean vanish = true;

    @Override
    public void processParameters(CastContext context, ConfigurationSection parameters) {
        super.processParameters(context, parameters);
        vanish = parameters.getBoolean("vanish", true);
    }

    @Override
    public SpellResult perform(CastContext context)
    {
        Entity entity = context.getTargetEntity();
        if (entity == null)
        {
            return SpellResult.NO_TARGET;
        }
        MageController controller = context.getController();
        Mage mage = controller.getMage(entity);
        mage.setVanished(vanish);
        if (vanish) {
            context.registerForUndo(new UndoVanish(mage));
        }
        return SpellResult.CAST;
    }

    @Override
    public boolean isUndoable()
    {
        return true;
    }

    @Override
    public boolean requiresTargetEntity()
    {
        return true;
    }
}
