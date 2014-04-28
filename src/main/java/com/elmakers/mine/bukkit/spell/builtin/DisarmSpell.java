package com.elmakers.mine.bukkit.spell.builtin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.spell.TargetingSpell;
import com.elmakers.mine.bukkit.utility.Target;
import com.elmakers.mine.wand.Wand;

public class DisarmSpell extends TargetingSpell 
{   
	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		Target target = getTarget();
		if (!target.hasEntity() || !(target.getEntity() instanceof LivingEntity)) {
			return SpellResult.NO_TARGET;    
		}
		LivingEntity entity = (LivingEntity)target.getEntity();
		
		EntityEquipment equipment = entity.getEquipment();
		ItemStack stack = equipment.getItemInHand();
		
		if (stack == null || stack.getType() == Material.AIR)
		{
			return SpellResult.NO_TARGET;
		}
		
		// Special case for wands
		if (Wand.isWand(stack) && entity instanceof Player) {
			Player targetPlayer = (Player)entity;
			Mage targetMage = controller.getMage(targetPlayer);
			
			// Check for protected players (admins, generally...)
			// This gets overridden by superpower...
			if (!mage.isSuperPowered() && targetMage.isSuperProtected()) {
				return SpellResult.NO_TARGET;
			}
			
			if (targetMage != null && targetMage.getActiveWand() != null) {
				targetMage.getActiveWand().deactivate();
			}
		}
		
		equipment.setItemInHand(null);
		Location location = entity.getLocation();
		location.setY(location.getY() + 1);
		Item item = entity.getWorld().dropItemNaturally(location, stack);
		Vector velocity = item.getVelocity();
		velocity.setY(velocity.getY() * 5);
		item.setVelocity(velocity);
		
		return SpellResult.CAST;
		
	}
}
