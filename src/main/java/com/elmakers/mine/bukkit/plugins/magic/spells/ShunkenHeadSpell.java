package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.Target;
import com.elmakers.mine.bukkit.utilities.InventoryUtils;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class ShunkenHeadSpell extends Spell
{
	private int             playerDamage = 1;
	private int             entityDamage = 100;
	
	@Override
	public boolean onCast(ConfigurationNode parameters) 
	{
		this.targetEntity(LivingEntity.class);
		Target target = getTarget();
		if (target == null || ! target.isEntity() || !(target.getEntity() instanceof LivingEntity))
		{
			return false;
		}
		
	Entity targetEntity = target.getEntity();
		LivingEntity li = (LivingEntity)targetEntity;
		String ownerName = null;
		String itemName = null;
		if (li instanceof Player)
		{
			li.damage(playerDamage);
			ownerName = ((Player)li).getName();
			// ... No nathan heads for you! :P
			if (ownerName.equals("NathanWolf")) {
				ownerName = "MHF_Herobrine";
			}
		}
		else
		{
			li.damage(entityDamage);
			switch (li.getType()) {
				case BLAZE:
					ownerName = "MHF_Blaze";
					itemName = "Blaze Skull";
					break;
				case CAVE_SPIDER:
					ownerName = "MHF_CaveSpider";
					itemName = "Cave Spider Skull";
					break;
				case CHICKEN:
					ownerName = "MHF_Chicken";
					itemName = "Chicken Skull";
					break;
				case COW:
					ownerName = "MHF_Cow";
					itemName = "Cow Skull";
					break;
				case ENDERMAN:
					ownerName = "MHF_Enderman";
					itemName = "Enderman Skull";
					break;
				case GHAST:
					ownerName = "MHF_Ghast";
					itemName = "Ghast Skull";
					break;
				case IRON_GOLEM:
					ownerName = "MHF_Golem";
					itemName = "Golem Skull";
					break;
				case MAGMA_CUBE:
					ownerName = "MHF_LavaSlime";
					itemName = "Magma Cube Skull";
					break;
				case MUSHROOM_COW:
					ownerName = "MHF_MushroomCow";
					itemName = "Mushroom Cow Skull";
					break;
				case OCELOT:
					ownerName = "MHF_Ocelot";
					itemName = "Ocelot Skull";
					break;
				case PIG:
					ownerName = "MHF_Pig";
					itemName = "Pig Skull";
					break;
				case PIG_ZOMBIE:
					ownerName = "MHF_PigZombie";
					itemName = "Zombie Pigman Skull";
					break;
				case SHEEP:
					ownerName = "MHF_Sheep";
					itemName = "Sheep Skull";
					break;
				case SLIME:
					ownerName = "MHF_Slime";
					itemName = "Slime Skull";
					break;
				case SPIDER:
					ownerName = "MHF_Spider";
					itemName = "Spider Skull";
					break;
				case SQUID:
					ownerName = "MHF_Squid";
					itemName = "Squid Skull";
					break;
				case VILLAGER:
					ownerName = "MHF_Villager";
					itemName = "Villager Skull";
				default:
			}
			
		}
		if (ownerName != null && li.isDead()) {
			@SuppressWarnings("deprecation")
			ItemStack shrunkenHead = new ItemStack(Material.SKULL_ITEM, 1, (short)0, (byte)3);
			shrunkenHead = InventoryUtils.getCopy(shrunkenHead);
			ItemMeta itemMeta = shrunkenHead.getItemMeta();
			if (itemName != null) {
				itemMeta.setDisplayName(itemName);
			}
			shrunkenHead.setItemMeta(itemMeta);
			InventoryUtils.setMeta(shrunkenHead, "SkullOwner", ownerName);
			targetEntity.getWorld().dropItemNaturally(targetEntity.getLocation(), shrunkenHead);
		}
		castMessage(player, "Boogidie Boogidie");
		return true;
	}

	@Override
	public void onLoad(ConfigurationNode properties)
	{
		playerDamage = properties.getInteger("player_damage", playerDamage);
		entityDamage = properties.getInteger("entity_damage", entityDamage);
	}

}
