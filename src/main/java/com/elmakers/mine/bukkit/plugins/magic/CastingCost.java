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

	public boolean has(Spell spell)
	{
		Mage mage = spell.getMage();
		Inventory inventory = mage.getInventory();
		int amount = getAmount(spell);
		boolean hasItem = item == null || amount <= 0 || inventory.contains(item, amount);
		boolean hasXp = xp <= 0 || mage.getExperience() >= getXP(spell);
		return hasItem && hasXp;
	}

	public void use(Spell spell)
	{
		Mage mage = spell.getMage();
		Inventory inventory = mage.getInventory();
		int amount = getAmount(spell);
		if (item != null && amount > 0) {
			ItemStack itemStack = getItemStack(spell);
			inventory.removeItem(itemStack);
		}
		int xp = getXP(spell);
		if (xp > 0) {
			mage.removeExperience(xp);
		}
	}

	@SuppressWarnings("deprecation")
	protected ItemStack getItemStack()
	{
		return new ItemStack(item, getAmount(), (short)0, data);
	}

	@SuppressWarnings("deprecation")
	protected ItemStack getItemStack(CostReducer reducer)
	{
		return new ItemStack(item, getAmount(reducer), (short)0, data);
	}

	public int getAmount()
	{
		return (int)Math.ceil(amount);
	}

	public int getXP()
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

	public String getDescription(CostReducer reducer)
	{
		if (item != null && getAmount() != 0) {
			return item.name().toLowerCase().replace("_", " ").replace(" block", "");
		}
		if (reducer.usesMana()) {
			return Messages.get("costs.mana");
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
