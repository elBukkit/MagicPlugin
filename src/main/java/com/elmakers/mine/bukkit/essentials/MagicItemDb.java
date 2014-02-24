package com.elmakers.mine.bukkit.essentials;

import net.ess3.api.IEssentials;

import org.bukkit.inventory.ItemStack;

import com.earth2me.essentials.ItemDb;
import com.elmakers.mine.bukkit.plugins.magic.MagicController;
import com.elmakers.mine.bukkit.plugins.magic.wand.Wand;

public class MagicItemDb extends ItemDb {
	
	private final MagicController controller;
	
	public MagicItemDb(final MagicController controller, final Object ess) {
		super((IEssentials)ess);
		this.controller = controller;
		this.reloadConfig();
	}
	
	@Override
	public ItemStack get(final String id) throws Exception
	{
		if (id.equals("wand")) {
			Wand wand = Wand.createWand(controller, "");
			if (wand != null) {
				return wand.getItem();
			} 
		} else if (id.startsWith("wand:")) {
			String wandId = id.replace("wand:", "");
			Wand wand = Wand.createWand(controller, wandId.trim());
			if (wand != null) {
				return wand.getItem();
			} 
		}
		
		return super.get(id);
	}
}
