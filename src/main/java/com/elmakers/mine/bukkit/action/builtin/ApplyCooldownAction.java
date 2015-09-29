package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.MageSpell;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import java.util.Arrays;
import java.util.Collection;

public class ApplyCooldownAction extends BaseSpellAction
{
    private int cooldownAmount;
	private String spellName;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
		cooldownAmount = parameters.getInt("duration", 1000);
		spellName = parameters.getString("spell", null);
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
		if (spellName == null) {
			targetMage.setRemainingCooldown(cooldownAmount);
		} else {
			MageSpell spell = targetMage.getSpell(spellName);
			if (spell != null) {
				spell.setRemainingCooldown(cooldownAmount);
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
