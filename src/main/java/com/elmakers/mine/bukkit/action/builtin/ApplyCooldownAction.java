package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.MageSpell;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import java.util.Arrays;
import java.util.Collection;

public class ApplyCooldownAction extends BaseSpellAction
{
    private int cooldownAmount;
	private String[] spells;
	private boolean clear;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
		cooldownAmount = parameters.getInt("duration", 0);
		clear = parameters.getBoolean("clear", false);
		String spellCSV = parameters.getString("spells", null);
		if (spellCSV != null)
		{
			spells = StringUtils.split(spellCSV, ',');
		}
		else
		{
			spells = null;
		}
    }

	@Override
	public SpellResult perform(CastContext context)
	{
		Entity entity = context.getTargetEntity();
		MageController controller = context.getController();
		if (entity == null || !controller.isMage(entity)) {
			return SpellResult.NO_TARGET;
		}
		Mage targetMage = controller.getMage(entity);
		if (spells == null) {
			if (clear) {
				targetMage.clearCooldown();
			}
			if (cooldownAmount > 0) {
				targetMage.setRemainingCooldown(cooldownAmount);
			}
		} else {
			for (String spellName : spells) {
				MageSpell spell = targetMage.getSpell(spellName);
				if (spell != null) {
					if (clear) {
						spell.clearCooldown();
					}
					if (cooldownAmount > 0) {
						spell.setRemainingCooldown(cooldownAmount);
					}
				}
			}
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

	@Override
	public void getParameterNames(Spell spell, Collection<String> parameters) {
		super.getParameterNames(spell, parameters);
		parameters.add("air");
	}

	@Override
	public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
		if (parameterKey.equals("air")) {
			examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_SIZES)));
		} else {
			super.getParameterOptions(spell, parameterKey, examples);
		}
	}
}
