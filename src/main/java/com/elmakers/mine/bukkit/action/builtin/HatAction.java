package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.BlockAction;
import com.elmakers.mine.bukkit.api.action.EntityAction;
import com.elmakers.mine.bukkit.api.action.GeneralAction;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.spell.BaseSpellAction;
import com.elmakers.mine.bukkit.spell.TargetingSpell;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import com.elmakers.mine.bukkit.utility.NMSUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class HatAction extends BaseSpellAction implements BlockAction, GeneralAction
{
	@Override
	public SpellResult perform(ConfigurationSection parameters)
	{
		if (!parameters.contains("brush"))
		{
			return SpellResult.NO_ACTION;
		}

		return equip(ConfigurationUtils.getMaterialAndData(parameters, "brush"));
	}

	@Override
	public SpellResult perform(ConfigurationSection parameters, Block block)
	{
		if (parameters.contains("brush"))
		{
			return SpellResult.NO_ACTION;
		}
		return equip(new MaterialAndData(block));
	}

	protected SpellResult equip(MaterialAndData material)
	{
		if (material.getMaterial() == Material.AIR)
		{
			return SpellResult.NO_TARGET;
		}
		Mage mage = getMage();
		Player player = mage.getPlayer();
		if (player == null)
		{
			return SpellResult.PLAYER_REQUIRED;
		}
		ItemStack hatItem = material.getItemStack(1);
		ItemStack itemStack = player.getInventory().getHelmet();
		ItemMeta meta = hatItem.getItemMeta();
		meta.setDisplayName(getMessage("hat_name").replace("$material", material.getName()));
		List<String> lore = new ArrayList<String>();
		lore.add(getMessage("hat_lore"));
		meta.setLore(lore);
		hatItem.setItemMeta(meta);
		hatItem = InventoryUtils.makeReal(hatItem);
		NMSUtils.makeTemporary(hatItem, getMessage("removed").replace("$material", material.getName()));
		player.getInventory().setHelmet(hatItem);
		if (itemStack != null && itemStack.getType() != Material.AIR && !NMSUtils.isTemporary(itemStack))
		{
			player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
		}
		return SpellResult.CAST;
	}
}
