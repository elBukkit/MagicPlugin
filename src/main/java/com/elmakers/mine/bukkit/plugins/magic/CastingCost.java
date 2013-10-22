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

	public Map<String, Object> export()
	{
		Map<String, Object> cost = new HashMap<String, Object>();
		cost.put("material", item.name().toLowerCase());
		cost.put("amount", amount);
		cost.put("xp", xp);

		return cost;
	}

	public boolean has(PlayerSpells playerSpells)
	{
		Player player = playerSpells.getPlayer();
		Inventory inventory = playerSpells.getInventory();
		boolean hasItem = item == null || inventory.contains(item, getAmount(playerSpells));
		boolean hasXp = xp <= 0 || player.getTotalExperience() >= getXP(playerSpells);
		return hasItem && hasXp;
	}

	public void use(PlayerSpells playerSpells)
	{
		Player player = playerSpells.getPlayer();
		Inventory inventory = playerSpells.getInventory();
		if (item != null) {
			ItemStack itemStack = getItemStack();
			inventory.removeItem(itemStack);
		}
		int xp = getXP(playerSpells);
		if (getXP(playerSpells) > 0) {
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

	protected int getXP()
	{
		return xp;
	}

	protected int getAmount(PlayerSpells spells)
	{
		double reducedAmount = amount;
		float reduction = spells.getCostReduction();
		if (reduction > 0) {
			reducedAmount = (1.0f - reduction) * reducedAmount;
		}
		return (int)Math.ceil(reducedAmount);
	}

	protected int getXP(PlayerSpells spells)
	{
		float reducedAmount = xp;
		float reduction = spells.getCostReduction();
		if (reduction > 0) {
			reducedAmount = (1.0f - reduction) * reducedAmount;
		}
		return (int)Math.ceil(reducedAmount);
	}
	
	public boolean hasCosts(PlayerSpells spells) {
		return (item != null && getAmount(spells) > 0) || getXP(spells) > 0;
	}

	public String getDescription()
	{
		if (item != null && getAmount() != 0) {
			return item.name().toLowerCase().replace("_", " ").replace(" block", "");
		}
		return "XP";
	}

	public String getFullDescription(PlayerSpells spells)
	{
		if (item != null) {
			return (int)getAmount(spells) + " " + item.name().toLowerCase().replace("_", " ").replace(" block", "");
		}
		return getXP(spells) + " XP";
	}
}
