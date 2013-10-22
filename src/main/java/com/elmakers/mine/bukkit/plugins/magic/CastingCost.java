package com.elmakers.mine.bukkit.plugins.magic;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class CastingCost
{
	protected Material item;
	protected byte data;
	protected double amount;
	protected int xp;

	public CastingCost(ConfigurationNode config)
	{
		this.item = config.getMaterial("material");
		this.amount = config.getDouble("amount", 1);
		this.xp = config.getInt("xp", 0);
		this.data = 0;
	}

	public CastingCost(Material item, double amount)
	{
		this.item = item;
		this.data = 0;
		this.amount = amount;
	}

	public CastingCost(Material item, byte data, double amount)
	{
		this.item = item;
		this.data = data;
		this.amount = amount;
	}
	
	public Material getMaterial() {
		return item;
	}
	
	public int getXP() {
		return xp;
	}

	public Map<String, Object> export()
	{
		Map<String, Object> cost = new HashMap<String, Object>();
		cost.put("material", item.name().toLowerCase());
		cost.put("amount", amount);
		cost.put("xp", xp);

		return cost;
	}

	public boolean has(Player player, Inventory inventory)
	{
		boolean hasItem = item == null || inventory.contains(item, getAmount());
		boolean hasXp = xp <= 0 || player.getTotalExperience() >= xp;
		return hasItem && hasXp;
	}

	public void use(Player player, Inventory inventory)
	{
		if (item != null) {
			ItemStack itemStack = getItemStack();
			inventory.removeItem(itemStack);
		}
		if (xp > 0) {
			removeExperience(player, xp);
		}
	}
	
	public static void removeExperience(Player player, int amount) {
		int currentExperience = player.getTotalExperience();
		
		// This is very hacky and prone to issues, but player.setTotalExperience doesn't seem to update the UI.
		player.setTotalExperience(0);
		player.setLevel(0);
		player.setExp(0);
		player.giveExp(Math.max(0, currentExperience - amount));
	}

	@SuppressWarnings("deprecation")
	protected ItemStack getItemStack()
	{
		return new ItemStack(item, getAmount(), (short)0, data);
	}

	protected int getAmount()
	{
		return (int)Math.ceil(amount);
	}

	public String getDescription()
	{
		if (item != null) {
			return item.name().toLowerCase().replace("_", " ").replace(" block", "");
		}
		return "XP";
	}

	public String getFullDescription()
	{
		if (item != null) {
			return (int)amount + " " + item.name().toLowerCase().replace("_", " ").replace(" block", "");
		}
		return xp + " XP";
	}
}
