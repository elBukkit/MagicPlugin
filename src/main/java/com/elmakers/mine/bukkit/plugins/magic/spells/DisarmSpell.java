package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.plugins.magic.Mage;
import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.Target;
import com.elmakers.mine.bukkit.plugins.magic.wand.Wand;
import com.elmakers.mine.bukkit.utilities.EffectTrail;
import com.elmakers.mine.bukkit.utilities.ParticleType;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class DisarmSpell extends Spell 
{
	private final static int 		maxEffectRange = 16;
	private final static int 		effectSpeed = 1;
	private final static float 		particleData = 2f;
	private final static int 		effectPeriod = 2;
	private final static int 		particleCount = 6;
    
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		int effectRange = Math.min(getMaxRange(), maxEffectRange / effectSpeed);
		Location effectLocation = player.getEyeLocation();
		Vector effectDirection = effectLocation.getDirection();
		EffectTrail effectTrail = new EffectTrail(controller.getPlugin(), effectLocation, effectDirection, effectRange);
		effectTrail.setParticleType(ParticleType.MOB_SPELL);
		effectTrail.setParticleCount(particleCount);
		effectTrail.setEffectData(particleData);
		effectTrail.setParticleOffset(0.2f, 0.2f, 0.2f);
		effectTrail.setSpeed(effectSpeed);
		effectTrail.setPeriod(effectPeriod);
		effectTrail.start();
		
		Target target = getTarget();
		Entity targetEntity = target.getEntity();
		if (targetEntity == null || !(targetEntity instanceof LivingEntity))
		{
			return SpellResult.NO_TARGET;    
		}
		LivingEntity entity = (LivingEntity)targetEntity;
		EntityEquipment equipment = entity.getEquipment();
		ItemStack stack = equipment.getItemInHand();
		
		if (stack == null || stack.getType() == Material.AIR)
		{
			return SpellResult.NO_TARGET;
		}
		
		// Special case for wands
		if (Wand.isWand(stack) && entity instanceof Player) {
			Player targetPlayer = (Player)entity;
			Mage playerSpells = controller.getPlayerSpells(targetPlayer);
			if (playerSpells != null && playerSpells.getActiveWand() != null) {
				playerSpells.getActiveWand().deactivate();
			}
		}
		
		equipment.setItemInHand(null);
		Location location = entity.getLocation();
		location.setY(location.getY() + 1);
		Item item = entity.getWorld().dropItemNaturally(location, stack);
		Vector velocity = item.getVelocity();
		velocity.setY(velocity.getY() * 5);
		item.setVelocity(velocity);
		
		return SpellResult.SUCCESS;
		
	}
}
