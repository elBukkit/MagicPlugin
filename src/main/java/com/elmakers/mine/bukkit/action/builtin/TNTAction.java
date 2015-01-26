package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.GeneralAction;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.ActionHandler;
import com.elmakers.mine.bukkit.spell.BaseSpellAction;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.Random;

public class TNTAction extends BaseSpellAction implements GeneralAction
{
	private ActionHandler actions = null;

	@Override
	public SpellResult perform(ConfigurationSection parameters) {
		Mage mage = getMage();
		MageController controller = getController();
		int size = parameters.getInt("size", 6);
		int count = parameters.getInt("count", 1);
		size = (int)(mage.getRadiusMultiplier() * size);		
		int fuse = parameters.getInt("fuse", 80);
		boolean useFire = parameters.getBoolean("fire", false);
		boolean breakBlocks = parameters.getBoolean("break_blocks", true);

		if (actions != null) {
			actions.setParameters(parameters);
		}

		Location loc = getEyeLocation();
		if (loc == null) {
			return SpellResult.LOCATION_REQUIRED;
		}
		if (!hasBuildPermission(loc.getBlock())) {
			return SpellResult.INSUFFICIENT_PERMISSION;
		}

		final Random rand = new Random();
		for (int i = 0; i < count; i++)
		{
			Location targetLoc = loc.clone();
			if (count > 1)
			{
				targetLoc.setX(targetLoc.getX() + rand.nextInt(2 * count) - count);
				targetLoc.setZ(targetLoc.getZ() + rand.nextInt(2 * count) - count);
			}
			TNTPrimed grenade = (TNTPrimed)getWorld().spawnEntity(targetLoc, EntityType.PRIMED_TNT);
			if (grenade == null) {
				return SpellResult.FAIL;
			}
			Vector aim = getDirection();
			grenade.setVelocity(aim);
			grenade.setYield(size);
			grenade.setFuseTicks(fuse);
			grenade.setIsIncendiary(useFire);
			registerForUndo(grenade);
			if (!breakBlocks)
			{
				grenade.setMetadata("cancel_explosion", new FixedMetadataValue(controller.getPlugin(), true));
			}
			ActionHandler.setActions(grenade, actions, "indirect_player_message");
			ActionHandler.setEffects(grenade, getSpell(), "explode");
		}
		
		return SpellResult.CAST;
	}

	@Override
	public void load(Spell spell, ConfigurationSection template)
	{
		super.load(spell, template);

		if (template != null && template.contains("actions"))
		{
			actions = new ActionHandler(getSpell());
			actions.load(template, "actions");
		}
	}
}
