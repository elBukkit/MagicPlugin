package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Location;
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
		String castType = parameters.getString("type");
		if (castType != null && castType.equalsIgnoreCase("self")) {
			dropHead(player.getLocation(), player.getName(), null);
			return true;
		}
		String giveName = parameters.getString("name");
		if (giveName != null) {
			dropHead(player.getLocation(), giveName, null);
			return true;
		}
		
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
					itemName = "Shrunken Blaze Head";
					break;
				case CAVE_SPIDER:
					ownerName = "MHF_CaveSpider";
					itemName = "Shrunken Cave Spider Head";
					break;
				case CHICKEN:
					ownerName = "MHF_Chicken";
					itemName = "Shrunken Chicken Head";
					break;
				case COW:
					ownerName = "MHF_Cow";
					itemName = "Shrunken Cow Head";
					break;
				case ENDERMAN:
					ownerName = "MHF_Enderman";
					itemName = "Shrunken Enderman Head";
					break;
				case GHAST:
					ownerName = "MHF_Ghast";
					itemName = "Shrunken Ghast Head";
					break;
				case IRON_GOLEM:
					ownerName = "MHF_Golem";
					itemName = "Shrunken Golem Head";
					break;
				case MAGMA_CUBE:
					ownerName = "MHF_LavaSlime";
					itemName = "Shrunken Magma Cube Head";
					break;
				case MUSHROOM_COW:
					ownerName = "MHF_MushroomCow";
					itemName = "Shrunken Mushroom Cow Head";
					break;
				case OCELOT:
					ownerName = "MHF_Ocelot";
					itemName = "Shrunken Ocelot Head";
					break;
				case PIG:
					ownerName = "MHF_Pig";
					itemName = "Shrunken Pig Head";
					break;
				case PIG_ZOMBIE:
					ownerName = "MHF_PigZombie";
					itemName = "Shrunken Zombie Pigman Head";
					break;
				case SHEEP:
					ownerName = "MHF_Sheep";
					itemName = "Shrunken Sheep Head";
					break;
				case SLIME:
					ownerName = "MHF_Slime";
					itemName = "Shrunken Slime Head";
					break;
				case SPIDER:
					ownerName = "MHF_Spider";
					itemName = "Shrunken Spider Head";
					break;
				case SQUID:
					ownerName = "MHF_Squid";
					itemName = "Shrunken Squid Head";
					break;
				case VILLAGER:
					ownerName = "MHF_Villager";
					itemName = "Shrunken Villager Head";
				default:
			}
			
		}
		if (ownerName != null && li.isDead()) {
			dropHead(targetEntity.getLocation(), ownerName, itemName);
		}
		castMessage(player, "Boogidie Boogidie");
		return true;
	}
	
	@SuppressWarnings("deprecation")
	protected void dropHead(Location location, String ownerName, String itemName) {
		ItemStack shrunkenHead = new ItemStack(Material.SKULL_ITEM, 1, (short)0, (byte)3);
		shrunkenHead = InventoryUtils.getCopy(shrunkenHead);
		ItemMeta itemMeta = shrunkenHead.getItemMeta();
		if (itemName != null) {
			itemMeta.setDisplayName(itemName);
		}
		shrunkenHead.setItemMeta(itemMeta);
		InventoryUtils.setMeta(shrunkenHead, "SkullOwner", ownerName);
		location.getWorld().dropItemNaturally(location, shrunkenHead);
	}

	@Override
	public void onLoad(ConfigurationNode properties)
	{
		playerDamage = properties.getInteger("player_damage", playerDamage);
		entityDamage = properties.getInteger("entity_damage", entityDamage);
	}

}
