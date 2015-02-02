package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.EntityAction;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.spell.BaseSpellAction;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import java.util.Arrays;
import java.util.Collection;

public class IgniteAction extends BaseSpellAction implements EntityAction
{
	@Override
	public SpellResult perform(ConfigurationSection parameters, Entity entity)
	{
        int duration = parameters.getInt("duration", 5000);
        int ticks = duration * 20 / 1000;

        registerModified(entity);

		MageController controller = getController();
		Mage mage = getMage();
		if (controller.isElemental(entity)) {
			controller.damageElemental(entity, 0, ticks, mage.getCommandSender());
		} else {
			entity.setFireTicks(ticks);
		}
		return SpellResult.CAST;
	}

	@Override
	public boolean isUndoable()
	{
		return true;
	}

	@Override
	public void getParameterNames(Collection<String> parameters) {
		super.getParameterNames(parameters);
		parameters.add("duration");
	}

	@Override
	public void getParameterOptions(Collection<String> examples, String parameterKey) {
		if (parameterKey.equals("duration")) {
			examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_DURATIONS)));
		} else {
			super.getParameterOptions(examples, parameterKey);
		}
	}
}
