package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

public class VanishAction extends BaseSpellAction
{
	private class UndoVanish implements Runnable
	{
		private final Mage mage;

		public UndoVanish(Mage mage)
		{
			this.mage = mage;
		}

		@Override
		public void run()
		{
			mage.setVanished(false);
		}
	}

	private boolean vanish = true;
	
	@Override
	public void prepare(CastContext context, ConfigurationSection parameters) {
		super.prepare(context, parameters);
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
