package com.elmakers.mine.bukkit.plugins.magic.spell;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.spell.CostReducer;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.block.MaterialBrush;
import com.elmakers.mine.bukkit.plugins.magic.Mage;
import com.elmakers.mine.bukkit.utilities.Messages;

public class CastingCost implements com.elmakers.mine.bukkit.api.spell.CastingCost
{
	protected MaterialAndData item;
	protected double amount;
	protected int xp;

	public CastingCost(String key, double cost)
	{
		if (key.toLowerCase().equals("xp")) {
			this.xp = (int)cost;
		} else {
			this.item = MaterialBrush.parseMaterialKey(key, true);
			this.amount = cost;
		}
	}

	public CastingCost(Material item, double amount)
	{
		this.item = new MaterialAndData(item, (byte)0);
		this.amount = amount;
	}

	public CastingCost(Material item, byte data, double amount)
	{
		this.item = new MaterialAndData(item, data);
		this.amount = amount;
	}
	
	public MaterialAndData getMaterial() {
		return item;
	}

	public Map<String, Object> export()
	{
		Map<String, Object> cost = new HashMap<String, Object>();
		cost.put("material", MaterialBrush.getMaterialName(item));
		cost.put("amount", amount);
		cost.put("xp", xp);

		return cost;
	}

	public boolean has(Spell spell)
	{
		Mage mage = spell.getMage();
		Inventory inventory = mage.getInventory();
		int amount = getAmount(spell);
		boolean hasItem = item == null || amount <= 0 || inventory.containsAtLeast(item.getItemStack(amount), amount);
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

	protected ItemStack getItemStack()
	{
		return item.getItemStack(getAmount());
	}

	protected ItemStack getItemStack(CostReducer reducer)
	{
		return item.getItemStack(getAmount(reducer));
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
		float reduction = reducer == null ? 0 : reducer.getCostReduction();
		if (reduction > 0) {
			reducedAmount = (1.0f - reduction) * reducedAmount;
		}
		return (int)Math.ceil(reducedAmount);
	}

	protected int getXP(CostReducer reducer)
	{
		float reducedAmount = xp;
		float reduction = reducer == null ? 0 : reducer.getCostReduction();
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
			return MaterialBrush.getMaterialName(item);
		}
		if (reducer != null && !reducer.usesMana()) {
			return Messages.get("costs.xp");
		}

		return Messages.get("costs.mana");
	}

	public String getFullDescription(CostReducer reducer)
	{
		if (item != null) {
			return (int)getAmount(reducer) + " " + MaterialBrush.getMaterialName(item);
		}
		if (reducer != null && !reducer.usesMana()) {
			return Messages.get("costs.xp_amount").replace("$amount", ((Integer)getXP(reducer)).toString());
		}
		return Messages.get("costs.mana_amount").replace("$amount", ((Integer)getXP(reducer)).toString());
	}
}
