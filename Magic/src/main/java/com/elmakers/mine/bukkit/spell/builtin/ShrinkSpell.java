package com.elmakers.mine.bukkit.spell.builtin;

import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import org.bukkit.Bukkit;
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
import com.elmakers.mine.bukkit.utility.Target;
import org.bukkit.inventory.meta.SkullMeta;

@Deprecated
public class ShrinkSpell extends BlockSpell
{
	private int             DEFAULT_PLAYER_DAMAGE = 1;
	private int             DEFAULT_ENTITY_DAMAGE = 100;
	
	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		String giveName = parameters.getString("name");
		if (giveName != null) {
            String itemName = giveName + "'s Head";
            Player player = Bukkit.getPlayer(giveName);
            if (player != null) {
                dropPlayerHead(getLocation(), player, itemName);
            } else {
                dropPlayerHead(getLocation(), giveName, itemName);
            }
			return SpellResult.CAST;
		}
		
		Target target = getTarget();

		if (target.hasEntity()) {
			Entity targetEntity = target.getEntity();
			if (controller.isElemental(targetEntity))
			{
				double elementalSize = controller.getElementalScale(targetEntity);
				if (elementalSize < 0.1) {
					int elementalDamage = parameters.getInt("elemental_damage", DEFAULT_ENTITY_DAMAGE);
					controller.damageElemental(targetEntity, elementalDamage, 0, mage.getCommandSender());
				} else {
					elementalSize /= 2;
					controller.setElementalScale(targetEntity, elementalSize);
				}
				
				return SpellResult.CAST;
			}
			
			if (!(targetEntity instanceof LivingEntity)) return SpellResult.NO_TARGET;
			
			// Register for undo in advance to catch entity death.
			registerForUndo();
			
			int damage =  parameters.getInt("entity_damage", DEFAULT_ENTITY_DAMAGE);

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
                itemName = li.getType().getName() + " Head";
				switch (li.getType()) {
					case CREEPER:
						data = 4;
					break;
					case ZOMBIE:
						data = 2;
					break;
					case SKELETON:
						Skeleton skeleton = (Skeleton)li;
						data = (byte)(skeleton.getSkeletonType() == SkeletonType.NORMAL ? 0 : 1);
					break;
					default:
						ownerName = getMobSkin(li.getType());
				}
			}

            if (itemName == null && ownerName != null) {
                itemName = ownerName + "'s Head";
            }
			
			Location targetLocation = targetEntity.getLocation();
			if (li instanceof Player) {
                CompatibilityUtils.magicDamage(li, damage, mage.getEntity());
				if (li.isDead() && !alreadyDead) {
					dropPlayerHead(targetEntity.getLocation(), (Player)li, itemName);
				}
			}
			else if (li.getType() == EntityType.GIANT) {
				UndoList spawnedList = com.elmakers.mine.bukkit.block.UndoList.getUndoList(li);
				registerModified(li);
				li.remove();
				Entity zombie = targetLocation.getWorld().spawnEntity(targetLocation, EntityType.ZOMBIE);
				if (zombie instanceof Zombie) {
					((Zombie)zombie).setBaby(false);
				}
				registerForUndo(zombie);
				if (spawnedList != null) {
					spawnedList.add(zombie);
				}
			}
			else if (li instanceof Ageable && ((Ageable)li).isAdult() && !(li instanceof Player)) {
				registerModified(li);
				((Ageable)li).setBaby();
			} else  if (li instanceof Zombie && !((Zombie)li).isBaby()) {
				registerModified(li);
				((Zombie)li).setBaby(true);
			} else  if (li instanceof PigZombie && !((PigZombie)li).isBaby()) {
				registerModified(li);
				((PigZombie)li).setBaby(true);
			} else  if (li instanceof Slime && ((Slime)li).getSize() > 1) {
				registerModified(li);
				Slime slime = (Slime)li;
				slime.setSize(slime.getSize() - 1);
			} else {
                CompatibilityUtils.magicDamage(li, damage, mage.getEntity());
				if ((ownerName != null || data != 3) && (li.isDead() || li.getHealth() == 0) && !alreadyDead) {
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
			
			if (!hasBreakPermission(targetBlock))
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

    protected void dropPlayerHead(Location location, Player player, String itemName) {
		dropPlayerHead(location, player.getName(), itemName);
    }

    protected void dropPlayerHead(Location location, String playerName, String itemName) {
		dropHead(location, playerName, itemName, (byte)3);
    }
	
	protected void dropHead(Location location, String ownerName, String itemName, byte data) {
        ItemStack shrunkenHead = new ItemStack(Material.SKULL_ITEM, 1, (short)0, data);
        ItemMeta meta = shrunkenHead.getItemMeta();
        if (itemName != null) {
            meta.setDisplayName(itemName);
        }
        if (meta instanceof SkullMeta && ownerName != null) {
            SkullMeta skullData = (SkullMeta)meta;
            skullData.setOwner(ownerName);
        }
        shrunkenHead.setItemMeta(meta);
        location.getWorld().dropItemNaturally(location, shrunkenHead);
	}
}
