package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.plugins.magic.Mage;
import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.wand.Wand;
import com.elmakers.mine.bukkit.utilities.Target;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class DisarmSpell extends Spell 
{   
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
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
