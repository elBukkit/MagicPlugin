package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.BlockAction;
import com.elmakers.mine.bukkit.api.action.EntityAction;
import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.spell.BaseSpellAction;
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

public class HatAction extends BaseSpellAction implements BlockAction, EntityAction
{
	private class HatUndoAction implements Runnable
	{
		private final Player player;

		public HatUndoAction(Player player) {
			this.player = player;
		}

		@Override
		public void run() {
			ItemStack helmetItem = player.getInventory().getHelmet();
			if (NMSUtils.isTemporary(helmetItem)) {
				ItemStack replacement = NMSUtils.getReplacement(helmetItem);
				player.getInventory().setHelmet(replacement);
			}
		}
	}

	@Override
	public SpellResult perform(ConfigurationSection parameters, Entity entity)
	{
		MaterialBrush brush = getBrush();
		boolean usesBrush = getSpell().usesBrush() || getSpell().hasBrushOverride();
		if (brush == null || !usesBrush)
		{
			return SpellResult.NO_ACTION;
		}

		return equip(entity, brush);
	}

	@Override
	public SpellResult perform(ConfigurationSection parameters, Block block)
	{
		boolean usesBrush = getSpell().usesBrush() || getSpell().hasBrushOverride();
		if (usesBrush)
		{
			return SpellResult.NO_ACTION;
		}
		return equip(getMage().getEntity(), new MaterialAndData(block));
	}

	protected SpellResult equip(Entity entity,com.elmakers.mine.bukkit.api.block.MaterialAndData material)
	{
		if (entity == null || !(entity instanceof Player))
		{
			return SpellResult.NO_TARGET;
		}

		if (material.getMaterial() == Material.AIR)
		{
			return SpellResult.NO_TARGET;
		}

		Player player = (Player)entity;
		ItemStack hatItem = material.getItemStack(1);
		ItemMeta meta = hatItem.getItemMeta();
		meta.setDisplayName(getMessage("hat_name").replace("$material", material.getName()));
		List<String> lore = new ArrayList<String>();
		lore.add(getMessage("hat_lore"));
		meta.setLore(lore);
		hatItem.setItemMeta(meta);
		hatItem = InventoryUtils.makeReal(hatItem);
		NMSUtils.makeTemporary(hatItem, getMessage("removed").replace("$material", material.getName()));

		ItemStack itemStack = player.getInventory().getHelmet();
		if (itemStack != null && itemStack.getType() != Material.AIR)
		{
			if (NMSUtils.isTemporary(itemStack))
			{
				itemStack = NMSUtils.getReplacement(itemStack);
			}
			if (itemStack != null)
			{
				org.bukkit.Bukkit.getLogger().info("Setting replacement: " + itemStack);
				NMSUtils.setReplacement(hatItem, itemStack);
			}
		}

		player.getInventory().setHelmet(hatItem);
		registerForUndo(new HatUndoAction(player));
		return SpellResult.CAST;
	}

	@Override
	public boolean isUndoable()
	{
		return true;
	}
}
