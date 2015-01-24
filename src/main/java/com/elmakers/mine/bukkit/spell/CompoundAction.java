package com.elmakers.mine.bukkit.spell;

import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import java.util.Collection;

public abstract class CompoundAction extends BaseSpellAction
{
	private boolean usesBrush = false;
	private boolean undoable = false;
	protected ActionHandler actions = null;

	@Override
	public void load(Spell spell, ConfigurationSection template)
	{
		super.load(spell, template);

		usesBrush = false;
		undoable = false;
		if (template.contains("actions"))
		{
			actions = new ActionHandler(getSpell());
			actions.load(template, "actions");
			usesBrush = usesBrush || actions.usesBrush();
			undoable = undoable || actions.isUndoable();
		}
		undoable = template.getBoolean("undoable", undoable);
	}

	protected SpellResult perform(ConfigurationSection parameters, Location targetLocation, Collection<Entity> targetEntities) {
		if (actions == null) {
			return SpellResult.FAIL;
		}

		for (Entity entity : targetEntities) {
			addTargetEntity(entity);
		}
		return actions.perform(parameters, targetLocation, targetEntities);
	}

	@Override
	public boolean isUndoable()
	{
		return undoable;
	}

	@Override
	public boolean usesBrush()
	{
		return usesBrush;
	}

	@Override
	public void getParameterNames(Collection<String> parameters)
	{
		if (actions != null)
		{
			actions.getParameterNames(parameters);
		}
	}

	@Override
	public void getParameterOptions(Collection<String> examples, String parameterKey)
	{
		if (actions != null)
		{
			actions.getParameterOptions(examples, parameterKey);
		}
	}

	@Override
	public String transformMessage(String message)
	{
		if (actions == null)
		{
			return message;
		}
		return actions.transformMessage(message);
	}
}
