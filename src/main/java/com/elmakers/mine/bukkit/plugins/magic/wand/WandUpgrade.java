package com.elmakers.mine.bukkit.plugins.magic.wand;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.plugins.magic.MagicController;
import com.elmakers.mine.bukkit.utilities.InventoryUtils;
import com.elmakers.mine.bukkit.utilities.Messages;

public class WandUpgrade extends Wand {
	public static Material DefaultUpgradeMaterial = Material.NETHER_STAR;
	
	public WandUpgrade(MagicController spells) {
		this(spells, DefaultUpgradeMaterial, (short)0);
	}
	
	public WandUpgrade(MagicController controller, Material icon, short iconData) {
		// This will make the Bukkit ItemStack into a real ItemStack with NBT data.
		super(InventoryUtils.getCopy(new ItemStack(icon, 1, iconData)));
		if (EnableGlow) {
			InventoryUtils.addGlow(item);
		}
		InventoryUtils.setMeta(item, "wand_upgrade", "true");
		this.controller = controller;
		wandName = Messages.get("wand.upgrade_name");
		updateName(true);
		updateLore();
	}
	
	public WandUpgrade(MagicController controller, String templateName) {
		super(controller, templateName);
		InventoryUtils.setMeta(item, "wand_upgrade", "true");
		wandName = Messages.get("wand.upgrade_name");
		setIcon(DefaultUpgradeMaterial, (byte)0);
		updateName(true);
	}
	
	protected List<String> getLore(int spellCount, int materialCount) 
	{
		List<String> lore = new ArrayList<String>();
		
		if (spellCount > 0) {	
			lore.add(Messages.get("wand.upgrade_spell_count").replace("$count", ((Integer)spellCount).toString()));
		}
		if (materialCount > 0) {
			lore.add(Messages.get("wand.upgrade_material_count").replace("$count", ((Integer)materialCount).toString()));
		}
		
		int remaining = getRemainingUses();
		if (remaining > 0) {
			lore.add(ChatColor.RED + Messages.get("wand.upgrade_uses").replace("$count", ((Integer)remaining).toString()));
		}
		addPropertyLore(lore);
		lore.add(Messages.get("wand.upgrade_item_description"));
		return lore;
	}

	public static boolean isWandUpgrade(ItemStack item) {
		return item != null && InventoryUtils.hasMeta(item, "wand") && InventoryUtils.hasMeta(item, "wand_upgrade");
	}
}
