package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.ItemAttr;

public class InventoryItemAttr extends NBTItemAttr {
	public InventoryItemAttr(String key, String tag, String subtag) {
		super(key, tag, subtag, "");
	}
	
	/**
	 * Called when a week equality is needed. Allows sometimes a value to be in range of another value, used for priority requests
	 * @return
	 *    true when equal, false instead 
	 */
	public boolean equalsWeak(ItemAttr other)
	{
		return equalsStrong(other);
	}

	/**
	 * Called when a strong equality is needed. Values are compared strict.
	 * @return
	 *    true when equal, false instead 
	 */
	public boolean equalsStrong(ItemAttr other)
	{
		// TODO: Parse into list, ignore @ positional modifier and ignore order.
		return super.equalsStrong(other);
	}
}
