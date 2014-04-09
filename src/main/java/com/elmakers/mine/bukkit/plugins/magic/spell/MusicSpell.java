package com.elmakers.mine.bukkit.plugins.magic.spell;

import java.util.Random;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.Target;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class MusicSpell extends Spell 
{
	protected Random random = new Random();
	
	protected static Material[] RECORDS = {
	    Material.GOLD_RECORD,
	    Material.GREEN_RECORD,
	    Material.RECORD_3,
	    Material.RECORD_4,
	    Material.RECORD_5,
	    Material.RECORD_6,
	    Material.RECORD_7,
	    Material.RECORD_8,
	    Material.RECORD_9,
	    Material.RECORD_10,
	    Material.RECORD_11,
	    Material.RECORD_12
	};
	
	@SuppressWarnings("deprecation")
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		Target target = getTarget();
		if (!target.hasTarget()) {
			return SpellResult.NO_TARGET;
		}
		Location location = target.getLocation();
		location.getWorld().playEffect(location, Effect.RECORD_PLAY, RECORDS[random.nextInt(RECORDS.length)].getId());

		return SpellResult.CAST;
	}
}
