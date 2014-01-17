package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.Set;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.effects.EffectTrail;
import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.wand.Wand;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class AbsorbSpell extends Spell 
{
    private final static int effectSpeed = 1;
    private final static int effectPeriod = 1;
    private final static int maxEffectRange = 4;
    
	@SuppressWarnings("deprecation")
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		Wand wand = mage.getActiveWand();
		if (wand == null) {
			castMessage("This spell requires a wand");
			return SpellResult.NO_TARGET;
		}
		
		Material material = Material.AIR;
		Set<Material> buildingMaterials = controller.getBuildingMaterials();
		byte data = 0;
		if (!isUnderwater())
		{
			noTargetThrough(Material.STATIONARY_WATER);
			noTargetThrough(Material.WATER);
		}
		Block target = getTargetBlock();

		if (target == null) 
		{
			castMessage("No target");
			return SpellResult.NO_TARGET;
		}
	
		material = target.getType();
		data = target.getData();
		
		// Visual effect
		int effectRange = Math.min(getMaxRange(), maxEffectRange / effectSpeed);
		Location effectLocation = getPlayer().getEyeLocation();
		Vector effectDirection = effectLocation.getDirection();

		effectDirection.normalize();
		effectDirection.multiply(effectSpeed * effectRange);
		effectLocation.add(effectDirection);
		effectDirection.multiply(-1);

		EffectTrail effect = new EffectTrail(controller.getPlugin(), effectLocation, effectDirection, effectRange);
		effect.setEffect(Effect.STEP_SOUND);
		effect.setData(material.getId());
		effect.setSpeed(effectSpeed);
		effect.setPeriod(effectPeriod);
		effect.start();
		
		if (material == null || material == Material.AIR || !buildingMaterials.contains(material))
		{
			return SpellResult.NO_TARGET;
		}
		
		// Add to the wand
		wand.addMaterial(material, data, true, true);
		castMessage("Absorbing some " + material.name().toLowerCase());
		
		return SpellResult.SUCCESS;
	}
	
	@Override
	public boolean usesBrush() {
		return true;
	}
}
