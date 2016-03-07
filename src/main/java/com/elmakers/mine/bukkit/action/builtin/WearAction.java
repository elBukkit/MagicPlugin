package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.magic.MagicPlugin;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import com.elmakers.mine.bukkit.utility.NMSUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WearAction extends BaseSpellAction
{
    private MaterialAndData material;
    private boolean useItem;
    private Map<Enchantment, Integer> enchantments;

    @Override
    public void initialize(Spell spell, ConfigurationSection parameters)
    {
        super.initialize(spell, parameters);
        if (parameters.contains("enchantments"))
        {
            enchantments = new HashMap<Enchantment, Integer>();
            ConfigurationSection enchantConfig = ConfigurationUtils.getConfigurationSection(parameters, "enchantments");
            Collection<String> enchantKeys = enchantConfig.getKeys(false);
            for (String enchantKey : enchantKeys)
            {
                try {
                    Enchantment enchantment = Enchantment.getByName(enchantKey.toUpperCase());
                    enchantments.put(enchantment, enchantConfig.getInt(enchantKey));
                } catch (Exception ex) {
                    spell.getController().getLogger().warning("Invalid enchantment: " + enchantKey);
                }
            }
        }
    }

	private class HatUndoAction implements Runnable
	{
		private final Mage mage;

		public HatUndoAction(Mage mage) {
			this.mage = mage;
		}

		@Override
		public void run() {
            Player player = mage.getPlayer();
            if (player == null) return;

			ItemStack helmetItem = player.getInventory().getHelmet();
			if (NMSUtils.isTemporary(helmetItem)) {
				ItemStack replacement = NMSUtils.getReplacement(helmetItem);
				player.getInventory().setHelmet(replacement);
			}
            if (mage instanceof com.elmakers.mine.bukkit.magic.Mage) {
                ((com.elmakers.mine.bukkit.magic.Mage)mage).armorUpdated();
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
            if (!context.getTargetsCaster()) return SpellResult.NO_TARGET;
            entity = context.getEntity();
        }
        if (entity == null || !(entity instanceof Player))
        {
            return SpellResult.NO_TARGET;
        }

        Player player = (Player)entity;
		MaterialAndData material = this.material;
        MageController controller = context.getController();
        Mage mage = controller.getMage(player);
        if (useItem)
        {
            Wand activeWand = mage.getActiveWand();
            // Check for trying to wear a hat from the offhand slot
            // Not handling this for now.
            if (activeWand != context.getWand()) {
                return SpellResult.NO_TARGET;
            }
            
            if (activeWand != null) {
                activeWand.deactivate();
            }
            
            ItemStack itemInHand = player.getItemInHand();
            if (itemInHand == null || itemInHand.getType() == Material.AIR)
            {
                return SpellResult.FAIL;
            }
            ItemStack currentItem = player.getInventory().getHelmet();
            player.getInventory().setHelmet(itemInHand);
            if (!InventoryUtils.isTemporary(currentItem)) {
                player.setItemInHand(currentItem);
            } else {
                player.setItemInHand(new ItemStack(Material.AIR));
            }

            if (mage instanceof com.elmakers.mine.bukkit.magic.Mage) {
                ((com.elmakers.mine.bukkit.magic.Mage)mage).armorUpdated();
            }
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
        if (enchantments != null) {
            hatItem.addUnsafeEnchantments(enchantments);
        }
		ItemStack itemStack = player.getInventory().getHelmet();
		if (itemStack != null && itemStack.getType() != Material.AIR)
		{
			if (NMSUtils.isTemporary(itemStack))
			{
				ItemStack replacement = NMSUtils.getReplacement(itemStack);
                if (replacement != null) {
                    itemStack = replacement;
                }
			}
			if (itemStack != null)
			{
				NMSUtils.setReplacement(hatItem, itemStack);
			}
		}

		player.getInventory().setHelmet(hatItem);

        // Sanity check to make sure the block was allowed to be created
        ItemStack helmetItem = player.getInventory().getHelmet();
        if (!NMSUtils.isTemporary(helmetItem)) {
            player.getInventory().setHelmet(itemStack);
            return SpellResult.NO_TARGET;
        }

        context.registerForUndo(new HatUndoAction(mage));

        if (mage instanceof com.elmakers.mine.bukkit.magic.Mage) {
            ((com.elmakers.mine.bukkit.magic.Mage)mage).armorUpdated();
        }
		return SpellResult.CAST;
	}

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters)
    {
        super.getParameterNames(spell, parameters);
        parameters.add("material");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples)
    {
        if (parameterKey.equals("material")) {
            examples.addAll(MagicPlugin.getAPI().getBrushes());
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }

	@Override
	public boolean isUndoable()
	{
		return true;
	}
}
