package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.Target;
import com.elmakers.mine.bukkit.utilities.InventoryUtils;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class ShunkenHeadSpell extends Spell
{
	private int             playerDamage = 1;
	private int             entityDamage = 100;
	
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		String castType = parameters.getString("type");
		if (castType != null && castType.equalsIgnoreCase("self")) {
			dropHead(player.getLocation(), player.getName(), null, (byte)3);
			return SpellResult.SUCCESS;
		}
		String giveName = parameters.getString("name");
		if (giveName != null) {
			dropHead(player.getLocation(), giveName, null, (byte)3);
			return SpellResult.SUCCESS;
		}
		
		this.targetEntity(LivingEntity.class);
		Target target = getTarget();
		if (target == null || ! target.isEntity() || !(target.getEntity() instanceof LivingEntity))
		{
			return SpellResult.NO_TARGET;
		}
		
		Entity targetEntity = target.getEntity();
		LivingEntity li = (LivingEntity)targetEntity;
		String ownerName = null;
		String itemName = null;
		byte data = 3;
		if (li instanceof Player)
		{
			li.damage(playerSpells.getPowerMultiplier() * playerDamage, player);
			ownerName = ((Player)li).getName();
		}
		else
		{
			li.damage(playerSpells.getPowerMultiplier() * entityDamage);
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
				case BLAZE:
					ownerName = "MHF_Blaze";
					itemName = "Blaze Head";
					break;
				case CAVE_SPIDER:
					ownerName = "MHF_CaveSpider";
					itemName = "Cave Spider Head";
					break;
				case CHICKEN:
					ownerName = "MHF_Chicken";
					itemName = "Chicken Head";
					break;
				case COW:
					ownerName = "MHF_Cow";
					itemName = "Cow Head";
					break;
				case ENDERMAN:
					ownerName = "MHF_Enderman";
					itemName = "Enderman Head";
					break;
				case GHAST:
					ownerName = "MHF_Ghast";
					itemName = "Ghast Head";
					break;
				case IRON_GOLEM:
					ownerName = "MHF_Golem";
					itemName = "Golem Head";
					break;
				case MAGMA_CUBE:
					ownerName = "MHF_LavaSlime";
					itemName = "Magma Cube Head";
					break;
				case MUSHROOM_COW:
					ownerName = "MHF_MushroomCow";
					itemName = "Mushroom Cow Head";
					break;
				case OCELOT:
					ownerName = "MHF_Ocelot";
					itemName = "Ocelot Head";
					break;
				case PIG:
					ownerName = "MHF_Pig";
					itemName = "Pig Head";
					break;
				case PIG_ZOMBIE:
					ownerName = "MHF_PigZombie";
					itemName = "Zombie Pigman Head";
					break;
				case SHEEP:
					ownerName = "MHF_Sheep";
					itemName = "Sheep Head";
					break;
				case SLIME:
					ownerName = "MHF_Slime";
					itemName = "Slime Head";
					break;
				case SPIDER:
					ownerName = "MHF_Spider";
					itemName = "Spider Head";
					break;
				case SQUID:
					ownerName = "MHF_Squid";
					itemName = "Squid Head";
					break;
				case VILLAGER:
					ownerName = "MHF_Villager";
					itemName = "Villager Head";
				default:
			}
			
		}
		if ((ownerName != null || data != 3) && li.isDead()) {
			dropHead(targetEntity.getLocation(), ownerName, itemName, data);
		}
		castMessage("Boogidie Boogidie");
		return SpellResult.SUCCESS;
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

	@Override
	public void onLoad(ConfigurationNode properties)
	{
		playerDamage = properties.getInteger("player_damage", playerDamage);
		entityDamage = properties.getInteger("entity_damage", entityDamage);
	}

}
