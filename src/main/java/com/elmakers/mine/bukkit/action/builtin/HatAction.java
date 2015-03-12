package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import com.elmakers.mine.bukkit.utility.NMSUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class HatAction extends BaseSpellAction
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
	public SpellResult perform(CastContext context)
	{
        Entity entity = context.getTargetEntity();
        if (entity == null) {
            entity = context.getEntity();
        }
		MaterialAndData material = context.getBrush();
		boolean usesBrush = context.getSpell().usesBrush() || context.getSpell().hasBrushOverride();
		if (material == null || !usesBrush)
		{
			Block targetBlock = context.getTargetBlock();
            if (targetBlock != null)
            {
                material = new com.elmakers.mine.bukkit.block.MaterialAndData(targetBlock);
            }
		}

        if (entity == null || !(entity instanceof Player) || material == null || material.getMaterial() == Material.AIR)
        {
            return SpellResult.NO_TARGET;
        }

		Player player = (Player)entity;
		ItemStack hatItem = material.getItemStack(1);
		ItemMeta meta = hatItem.getItemMeta();
		meta.setDisplayName(context.getMessage("hat_name").replace("$material", material.getName()));
		List<String> lore = new ArrayList<String>();
		lore.add(context.getMessage("hat_lore"));
		meta.setLore(lore);
		hatItem.setItemMeta(meta);
		hatItem = InventoryUtils.makeReal(hatItem);
		NMSUtils.makeTemporary(hatItem, context.getMessage("removed").replace("$material", material.getName()));

		ItemStack itemStack = player.getInventory().getHelmet();
		if (itemStack != null && itemStack.getType() != Material.AIR)
		{
			if (NMSUtils.isTemporary(itemStack))
			{
				itemStack = NMSUtils.getReplacement(itemStack);
			}
			if (itemStack != null)
			{
				NMSUtils.setReplacement(hatItem, itemStack);
			}
		}

		player.getInventory().setHelmet(hatItem);
        context.registerForUndo(new HatUndoAction(player));
		return SpellResult.CAST;
	}

	@Override
	public boolean isUndoable()
	{
		return true;
	}
}
