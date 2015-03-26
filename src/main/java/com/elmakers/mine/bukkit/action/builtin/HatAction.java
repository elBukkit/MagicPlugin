package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.magic.MagicPlugin;
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
import java.util.Collection;
import java.util.List;

public class HatAction extends BaseSpellAction
{
    private MaterialAndData material;
    private boolean useItem;

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
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        material = ConfigurationUtils.getMaterialAndData(parameters, "material");
        useItem = parameters.getBoolean("use_item", false);
    }

	@Override
	public SpellResult perform(CastContext context)
	{
        Entity entity = context.getTargetEntity();
        if (entity == null) {
            entity = context.getEntity();
        }
        if (entity == null || !(entity instanceof Player))
        {
            return SpellResult.NO_TARGET;
        }

        Player player = (Player)entity;
		MaterialAndData material = this.material;
        if (useItem)
        {
            ItemStack itemInHand = player.getItemInHand();
            if (itemInHand == null || itemInHand.getType() == Material.AIR)
            {
                return SpellResult.FAIL;
            }
            ItemStack currentItem = player.getInventory().getHelmet();
            player.getInventory().setHelmet(itemInHand);
            player.setItemInHand(currentItem);
            return SpellResult.CAST;
        }
        if (material == null && (context.getSpell().usesBrush() || context.getSpell().hasBrushOverride())) {
            material = context.getBrush();
        }
		if (material == null)
		{
			Block targetBlock = context.getTargetBlock();
            if (targetBlock != null)
            {
                material = new com.elmakers.mine.bukkit.block.MaterialAndData(targetBlock);
                // Check for Banners with 1.7 support
                Material baseMaterial = material.getMaterial();
                if (baseMaterial.getId() == 176 || baseMaterial.getId() == 177)
                {
                    ((com.elmakers.mine.bukkit.block.MaterialAndData)material).setMaterialId(425);
                }
            }
		}

        if (entity == null || !(entity instanceof Player) || material == null || material.getMaterial() == Material.AIR)
        {
            return SpellResult.NO_TARGET;
        }

		ItemStack hatItem = material.getItemStack(1);
		ItemMeta meta = hatItem.getItemMeta();
        String hatName = context.getMessage("hat_name", "");
        String materialName = material.getName();
        if (materialName == null || materialName.isEmpty())
        {
            materialName = "?";
        }
        if (hatName != null && !hatName.isEmpty())
        {
            meta.setDisplayName(hatName.replace("$hat", materialName));
        }
		List<String> lore = new ArrayList<String>();
		lore.add(context.getMessage("hat_lore"));
		meta.setLore(lore);
		hatItem.setItemMeta(meta);
		hatItem = InventoryUtils.makeReal(hatItem);
		NMSUtils.makeTemporary(hatItem, context.getMessage("removed").replace("$hat", materialName));

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
    public void getParameterNames(Collection<String> parameters)
    {
        super.getParameterNames(parameters);
        parameters.add("material");
    }

    @Override
    public void getParameterOptions(Collection<String> examples, String parameterKey)
    {
        if (parameterKey.equals("material")) {
            examples.addAll(MagicPlugin.getAPI().getBrushes());
        } else {
            super.getParameterOptions(examples, parameterKey);
        }
    }

	@Override
	public boolean isUndoable()
	{
		return true;
	}
}
