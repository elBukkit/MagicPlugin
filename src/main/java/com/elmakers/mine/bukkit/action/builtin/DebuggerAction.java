package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import java.util.Arrays;
import java.util.Collection;

public class DebuggerAction extends BaseSpellAction
{
    private int debugLevel;
	private boolean check;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
		debugLevel = parameters.getInt("level", 1);
		check = parameters.getBoolean("check", false);
    }

	@Override
	public SpellResult perform(CastContext context)
	{
        Entity entity = context.getTargetEntity();
		MageController controller = context.getController();
		if (!controller.isMage(entity)) {
			return SpellResult.NO_TARGET;
		}
		Mage mage = controller.getMage(entity);
		int currentLevel = mage.getDebugLevel();
		if (currentLevel == debugLevel || debugLevel == 0) {
			mage.setDebugLevel(0);
			mage.setDebugger(null);
			return SpellResult.DEACTIVATE;
		}

		mage.setDebugLevel(debugLevel);
		mage.setDebugger(context.getMage().getCommandSender());

		if (check) {
			mage.debugPermissions();
		}

		return SpellResult.CAST;
	}

	@Override
	public boolean isUndoable()
	{
		return false;
	}

    @Override
    public boolean requiresTargetEntity()
    {
        return true;
    }

	@Override
	public void getParameterNames(Spell spell, Collection<String> parameters) {
		super.getParameterNames(spell, parameters);
		parameters.add("level");
	}

	@Override
	public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
		if (parameterKey.equals("level")) {
			examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_SIZES)));
		} else {
			super.getParameterOptions(spell, parameterKey, examples);
		}
	}
}
