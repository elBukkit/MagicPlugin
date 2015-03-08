package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.spell.TargetingSpell;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class AllEntitiesAction extends CompoundAction
{
    private boolean targetSelf;
    private boolean targetAllWorlds;

	@Override
	public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        targetSelf = parameters.getBoolean("target_self", false);
        targetAllWorlds = parameters.getBoolean("target_all_worlds", false);
    }

    @Override
    public SpellResult perform(CastContext context) {
        Spell spell = context.getSpell();
		Entity sourceEntity = context.getEntity();
		Location sourceLocation = context.getLocation();

		if (sourceLocation == null && !targetAllWorlds)
		{
			return SpellResult.LOCATION_REQUIRED;
		}

		Class<?> targetType = Player.class;
		if (spell instanceof TargetingSpell)
		{
			targetType = ((TargetingSpell)spell).getTargetEntityType();
		}

        SpellResult result = SpellResult.NO_TARGET;
        CastContext actionContext = createContext(context);
		if (targetType == Player.class)
		{
			Player[] players = Bukkit.getOnlinePlayers();
			for (Player player : players)
			{
				if ((targetSelf || player != sourceEntity) && (targetAllWorlds || (sourceLocation != null && sourceLocation.getWorld().equals(player.getWorld()))) && spell.canTarget(player))
				{
                    actionContext.setTargetEntity(player);
                    actionContext.setTargetLocation(player.getLocation());
                    SpellResult entityResult = performActions(actionContext);
                    result = result.min(entityResult);
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
				List<Entity> entities = world.getEntities();
				for (Entity entity : entities)
				{
					if (spell.canTarget(entity) && (targetSelf || entity != sourceEntity))
					{
                        actionContext.setTargetEntity(entity);
                        actionContext.setTargetLocation(entity.getLocation());
                        SpellResult entityResult = performActions(actionContext);
                        result = result.min(entityResult);
					}
				}
			}
		}

		return result;
	}

    @Override
    public void getParameterNames(Collection<String> parameters) {
        parameters.add("target_self");
        parameters.add("target_all_worlds");
    }

    @Override
    public void getParameterOptions(Collection<String> examples, String parameterKey) {
        if (parameterKey.equals("target_self") || parameterKey.equals("target_all_worlds")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_BOOLEANS)));
        }
    }
}
