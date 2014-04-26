package com.elmakers.mine.bukkit.traders;

import java.util.HashSet;
import java.util.Set;

import net.dandielo.citizens.traders_v3.utils.items.ItemAttr;

import org.apache.commons.lang.StringUtils;

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
		if (!(other instanceof InventoryItemAttr)) return false;
		
		Set<String> inventory = getInventorySet(this.getData());
		Set<String> otherInventory = getInventorySet(((InventoryItemAttr)other).getData());
		
		return inventory.containsAll(otherInventory);
	}

	/**
	 * Called when a strong equality is needed. Values are compared strict.
	 * @return
	 *    true when equal, false instead 
	 */
	public boolean equalsStrong(ItemAttr other)
	{
		if (!(other instanceof InventoryItemAttr)) return false;
		
		Set<String> inventory = getInventorySet(this.getData());
		Set<String> otherInventory = getInventorySet(((InventoryItemAttr)other).getData());
		
		return inventory.equals(otherInventory);
	}
	
	public static Set<String> getInventorySet(String csv) {
		Set<String> inventory = new HashSet<String>();
		String[] pieces = StringUtils.split(csv, ',');
		for (String piece : pieces) {
			if (piece.contains("@")) {
				String[] bits = StringUtils.split(piece, '@');
				piece = bits[0];
			}
			
			inventory.add(piece);
		}
		
		return inventory;
	}
}
