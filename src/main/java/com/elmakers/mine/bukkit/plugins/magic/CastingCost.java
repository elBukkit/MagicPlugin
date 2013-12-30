package com.elmakers.mine.bukkit.plugins.magic;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.utilities.Messages;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class CastingCost
{
	protected Material item;
	protected byte data;
	protected double amount;
	protected int xp;

	public CastingCost(String key, double cost)
	{
		if (key.toLowerCase().equals("xp")) {
			this.xp = (int)cost;
		} else {
			this.item = ConfigurationNode.toMaterial(key);
			this.amount = cost;
		}
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
		Inventory inventory = playerSpells.getInventory();
		boolean hasItem = item == null || inventory.contains(item, getAmount(playerSpells));
		boolean hasXp = xp <= 0 || playerSpells.getExperience() >= getXP(playerSpells);
		return hasItem && hasXp;
	}

	public void use(PlayerSpells playerSpells)
	{
		Inventory inventory = playerSpells.getInventory();
		if (item != null) {
			ItemStack itemStack = getItemStack();
			inventory.removeItem(itemStack);
		}
		int xp = getXP(playerSpells);
		if (xp > 0) {
			playerSpells.removeExperience(xp);
		}
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

	protected int getAmount(CostReducer reducer)
	{
		double reducedAmount = amount;
		float reduction = reducer.getCostReduction();
		if (reduction > 0) {
			reducedAmount = (1.0f - reduction) * reducedAmount;
		}
		return (int)Math.ceil(reducedAmount);
	}

	protected int getXP(CostReducer reducer)
	{
		float reducedAmount = xp;
		float reduction = reducer.getCostReduction();
		if (reduction > 0) {
			reducedAmount = (1.0f - reduction) * reducedAmount;
		}
		return (int)Math.ceil(reducedAmount);
	}
	
	public boolean hasCosts(CostReducer reducer) {
		return (item != null && getAmount(reducer) > 0) || getXP(reducer) > 0;
	}

	public String getDescription()
	{
		if (item != null && getAmount() != 0) {
			return item.name().toLowerCase().replace("_", " ").replace(" block", "");
		}
		return Messages.get("costs.xp");
	}

	public String getFullDescription(CostReducer reducer)
	{
		if (item != null) {
			return (int)getAmount(reducer) + " " + item.name().toLowerCase().replace("_", " ").replace(" block", "");
		}
		if (reducer.usesMana()) {
			return Messages.get("costs.mana_amount").replace("$amount", ((Integer)getXP(reducer)).toString());
		}
		return Messages.get("costs.xp_amount").replace("$amount", ((Integer)getXP(reducer)).toString());
	}
}
