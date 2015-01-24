package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.GeneralAction;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.CompoundAction;
import com.elmakers.mine.bukkit.spell.TargetingSpell;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AllEntitiesAction extends CompoundAction implements GeneralAction
{
	@Override
	public SpellResult perform(ConfigurationSection parameters) {
		boolean targetSelf = parameters.getBoolean("target_self", false);
		boolean targetAllWorlds = parameters.getBoolean("target_all_worlds", false);
		Spell spell = getSpell();
		Mage mage = getMage();
		Entity sourceEntity = mage == null ? null : mage.getEntity();
		Location sourceLocation = getLocation();
		List<Entity> targetEntities = new ArrayList<Entity>();

		if (sourceLocation == null && !targetAllWorlds)
		{
			return SpellResult.LOCATION_REQUIRED;
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
					targetEntities.add(player);
				}
			}
		}
		else if (sourceLocation != null)
		{
			List<World> worlds = null;
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
						targetEntities.add(entity);
					}
				}
			}
		}

		return perform(parameters, sourceLocation, targetEntities);
	}
}
