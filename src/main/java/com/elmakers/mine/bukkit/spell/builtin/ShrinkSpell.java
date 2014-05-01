package com.elmakers.mine.bukkit.spell.builtin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BlockSpell;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import com.elmakers.mine.bukkit.utility.Target;

public class ShrinkSpell extends BlockSpell
{
	private int             DEFAULT_PLAYER_DAMAGE = 1;
	private int             DEFAULT_ENTITY_DAMAGE = 100;
	
	@SuppressWarnings("deprecation")
	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		String castType = parameters.getString("type");
		if (castType != null && castType.equalsIgnoreCase("self")) {
			dropHead(getLocation(), getPlayer().getName(), null, (byte)3);
			return SpellResult.CAST;
		}
		String giveName = parameters.getString("name");
		if (giveName != null) {
			dropHead(getLocation(), giveName, null, (byte)3);
			return SpellResult.CAST;
		}
		
		Target target = getTarget();

		if (target.hasEntity()) {
			if (!(target.getEntity() instanceof LivingEntity)) return SpellResult.NO_TARGET;
			
			// Register for undo in advance to catch entity death.
			registerForUndo();
			
			int damage =  parameters.getInt("entity_damage", DEFAULT_ENTITY_DAMAGE);

			Entity targetEntity = target.getEntity();
			LivingEntity li = (LivingEntity)targetEntity;
			boolean alreadyDead = li.isDead() || li.getHealth() <= 0;
			String ownerName = null;
			String itemName = null;
			byte data = 3;
			
			if (li instanceof Player)
			{
				damage = parameters.getInt("player_damage", DEFAULT_PLAYER_DAMAGE);
				ownerName = ((Player)li).getName();
			}
			else
			{	
				switch (li.getType()) {
					case CREEPER:
						data = 4;
						ownerName = null;
					break;
					case ZOMBIE:
						data = 2;
						ownerName = null;
					break;
					case SKELETON:
						Skeleton skeleton = (Skeleton)li;
						data = (byte)(skeleton.getSkeletonType() == SkeletonType.NORMAL ? 0 : 1);
						ownerName = null;
					break;
					default:
						ownerName = getMobSkin(li.getType());
						if (ownerName != null) {
							itemName = li.getType().getName() + " Head";
						}
				}
				
			}
			
			Location targetLocation = targetEntity.getLocation();
			if (li instanceof Player) {
				li.damage(damage, getPlayer());
				if (ownerName != null || data != 3 && li.isDead() && !alreadyDead) {
					dropHead(targetEntity.getLocation(), ownerName, itemName, data);
				}
			}
			else if (li.getType() == EntityType.GIANT) {
				li.remove();
				Entity zombie = targetLocation.getWorld().spawnEntity(targetLocation, EntityType.ZOMBIE);
				registerForUndo(zombie);
			}
			else if (li instanceof Ageable && ((Ageable)li).isAdult() && !(li instanceof Player)) {
				((Ageable)li).setBaby();
			} else  if (li instanceof Zombie && !((Zombie)li).isBaby()) {
				((Zombie)li).setBaby(true);
			} else  if (li instanceof PigZombie && !((PigZombie)li).isBaby()) {
				((PigZombie)li).setBaby(true);
			} else  if (li instanceof Slime && ((Slime)li).getSize() > 1) {
				Slime slime = (Slime)li;
				slime.setSize(slime.getSize() - 1);
			} else {
				li.damage(damage, getPlayer());
				if (ownerName != null || data != 3 && (li.isDead() || li.getHealth() == 0) && !alreadyDead) {
					dropHead(targetEntity.getLocation(), ownerName, itemName, data);
				}
			}
		} else {
			Block targetBlock = target.getBlock();
			if (targetBlock == null) {
				return SpellResult.NO_TARGET;
			}
			String blockSkin = getBlockSkin(targetBlock.getType());
			if (blockSkin == null) return SpellResult.NO_TARGET;
			
			if (!hasBuildPermission(targetBlock)) 
			{
				return SpellResult.INSUFFICIENT_PERMISSION;
			}
			if (mage.isIndestructible(targetBlock)) 
			{
				return SpellResult.NO_TARGET;
			}
			
			registerForUndo(targetBlock);		
			registerForUndo();

			dropHead(targetBlock.getLocation(), blockSkin, targetBlock.getType().name(), (byte)3);
			targetBlock.setType(Material.AIR);
		}
		
		return SpellResult.CAST;
	}
	
	@SuppressWarnings("deprecation")
	protected void dropHead(Location location, String ownerName, String itemName, byte data) {
		ItemStack shrunkenHead = new ItemStack(Material.SKULL_ITEM, 1, (short)0, (byte)data);
		if (ownerName != null) {
			shrunkenHead = InventoryUtils.getCopy(shrunkenHead);
			ItemMeta itemMeta = shrunkenHead.getItemMeta();
			if (itemName != null) {
				itemMeta.setDisplayName(itemName);
			}
			shrunkenHead.setItemMeta(itemMeta);
			InventoryUtils.setMeta(shrunkenHead, "SkullOwner", ownerName);
		}
		location.getWorld().dropItemNaturally(location, shrunkenHead);
	}
}
