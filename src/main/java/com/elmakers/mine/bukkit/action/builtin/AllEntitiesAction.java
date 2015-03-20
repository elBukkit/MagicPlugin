package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.CompoundEntityAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.spell.TargetingSpell;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class AllEntitiesAction extends CompoundEntityAction
{
    private boolean targetAllWorlds;

	@Override
	public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        targetAllWorlds = parameters.getBoolean("target_all_worlds", false);
    }

    @Override
    public void reset(CastContext context) {
        super.reset(context);
        createActionContext(context);
    }

    @Override
    public SpellResult perform(CastContext context)
    {
        Location sourceLocation = context.getLocation();
        if (sourceLocation == null && !targetAllWorlds)
        {
            return SpellResult.LOCATION_REQUIRED;
        }

        return super.perform(context);
    }

    @Override
    public void addEntities(CastContext context, List<WeakReference<Entity>> entities)
    {
        Spell spell = context.getSpell();
		Entity sourceEntity = context.getEntity();
		Location sourceLocation = context.getLocation();

		if (sourceLocation == null && !targetAllWorlds)
		{
			return;
		}

		Class<?> targetType = Player.class;
		if (spell instanceof TargetingSpell)
		{
			targetType = ((TargetingSpell)spell).getTargetEntityType();
		}

		if (targetType == Player.class)
		{
			Player[] players = Bukkit.getOnlinePlayers();
			for (Player player : players)
			{
				if ((targetSelf || player != sourceEntity) && (targetAllWorlds || (sourceLocation != null && sourceLocation.getWorld().equals(player.getWorld()))) && spell.canTarget(player))
				{
                    entities.add(new WeakReference<Entity>(player));
				}
			}
		}
		else if (sourceLocation != null)
		{
			List<World> worlds;
			if (targetAllWorlds) {
				worlds = Bukkit.getWorlds();
			} else {
				worlds = new ArrayList<World>();
				worlds.add(sourceLocation.getWorld());
			}
			for (World world : worlds)
			{
				List<Entity> candidates = world.getEntities();
				for (Entity entity : candidates)
				{
					if (spell.canTarget(entity) && (targetSelf || entity != sourceEntity))
					{
                        entities.add(new WeakReference<Entity>(entity));
					}
				}
			}
		}
	}

    @Override
    public void getParameterNames(Collection<String> parameters) {
        super.getParameterNames(parameters);
        parameters.add("target_all_worlds");
    }

    @Override
    public void getParameterOptions(Collection<String> examples, String parameterKey) {
        if (parameterKey.equals("target_all_worlds")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_BOOLEANS)));
        } else {
            super.getParameterOptions(examples, parameterKey);
        }
    }
}
