package com.elmakers.mine.bukkit.action;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.action.SpellAction;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import java.util.Collection;

public abstract class TriggeredCompoundAction extends BaseSpellAction implements Cloneable
{
	private boolean usesBrush = false;
	private boolean undoable = false;
    private boolean requiresBuildPermission = false;
	protected ActionHandler actions = null;
    protected ConfigurationSection parameters;
    protected CastContext actionContext;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        this.parameters = parameters;
    }

    @Override
	public void initialize(Spell spell, ConfigurationSection parameters)
	{
		super.initialize(spell, parameters);

		usesBrush = false;
		undoable = false;
        requiresBuildPermission = false;
        if (parameters != null)
        {
            if (parameters.contains("actions"))
            {
                actions = new ActionHandler();
                actions.load(parameters, "actions");
            }
        }
        if (actions != null)
        {
            actions.initialize(spell, parameters);
            updateFlags();
        }
    }

    protected void updateFlags() {
        usesBrush = usesBrush || actions.usesBrush();
        undoable = undoable || actions.isUndoable();
        requiresBuildPermission = requiresBuildPermission || actions.requiresBuildPermission();
    }

    public void addAction(SpellAction action) {
        addAction(action, null);
    }

    public void addAction(SpellAction action, ConfigurationSection parameters) {
        if (actions == null) {
            actions = new ActionHandler();
        }
        actions.loadAction(action, parameters);
        updateFlags();
    }

    @Override
    public void load(Mage mage, ConfigurationSection data)
    {
        if (actions != null)
        {
            actions.loadData(mage, data);
        }
    }

    @Override
    public void save(Mage mage, ConfigurationSection data)
    {
        if (actions != null)
        {
            actions.saveData(mage, data);
        }
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
    public boolean requiresBuildPermission()
    {
        return requiresBuildPermission;
    }

	@Override
	public void getParameterNames(Spell spell, Collection<String> parameters)
	{
        super.getParameterNames(spell, parameters);
		if (actions != null)
		{
			actions.getParameterNames(spell, parameters);
		}
	}

	@Override
	public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples)
	{
        super.getParameterOptions(spell, parameterKey, examples);
		if (actions != null)
		{
			actions.getParameterOptions(spell, parameterKey, examples);
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

    public void createActionContext(CastContext context) {
        actionContext = createContext(context);
    }

    public void createActionContext(CastContext context, Entity sourceEntity, Location sourceLocation) {
        actionContext = createContext(context, sourceEntity, sourceLocation);
    }

    public CastContext createContext(CastContext context) {
        return new com.elmakers.mine.bukkit.action.CastContext(context);
    }

    public CastContext createContext(CastContext context, Entity sourceEntity, Location sourceLocation) {
        return new com.elmakers.mine.bukkit.action.CastContext(context, sourceEntity, sourceLocation);
    }

    public CastContext createContext(CastContext context, Entity sourceEntity, Location sourceLocation, Entity targetEntity, Location targetLocation) {
        CastContext newContext = new com.elmakers.mine.bukkit.action.CastContext(context, sourceEntity, sourceLocation);
        newContext.setTargetEntity(targetEntity);
        newContext.setTargetLocation(targetLocation);
        return newContext;
    }

    @Override
    public int getActionCount() {
        return actions == null ? 0 : actions.getActionCount();
    }

    protected void skippedActions(CastContext context) {
        if (actions == null) return;
        int actionCount = actions.getActionCount();
        context.performedActions(actionCount);
        context.addWork(actionCount);
    }

    @Override
    public Object clone()
    {
        TriggeredCompoundAction action = (TriggeredCompoundAction)super.clone();
        if (action != null)
        {
            action.actions = this.actions == null ? null : (ActionHandler)this.actions.clone();
        }
        return action;
    }

    public CastContext getActionContext(CastContext context) {
        if (actionContext == null)
        {
            actionContext = context;
        }
        return actionContext;
    }
}
