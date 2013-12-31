package com.elmakers.mine.bukkit.essentials;

import net.ess3.api.IEssentials;

import org.bukkit.inventory.ItemStack;

import com.earth2me.essentials.ItemDb;
import com.elmakers.mine.bukkit.plugins.magic.Spells;
import com.elmakers.mine.bukkit.plugins.magic.Wand;

public class MagicItemDb extends ItemDb {
	
	private final Spells spells;
	
	public MagicItemDb(final Spells spells, final Object ess) {
		super((IEssentials)ess);
		this.spells = spells;
	}
	
	@Override
	public ItemStack get(final String id) throws Exception
	{
		if (id.startsWith("wand:")) {
			String wandId = id.replace("wand:", "");
			Wand wand = Wand.createWand(spells, wandId.trim());
			if (wand != null) {
				return wand.getItem();
			} 
		}
		
		return super.get(id);
	}
}
