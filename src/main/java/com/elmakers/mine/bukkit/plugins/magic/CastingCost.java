package com.elmakers.mine.bukkit.plugins.magic;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class CastingCost
{
    protected Material item;
    protected byte data;
    protected double amount;
    
    public CastingCost(ConfigurationNode config)
    {
        this.item = config.getMaterial("material");
        this.amount = config.getDouble("amount", 1);
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
    
    public Map<String, Object> export()
    {
        Map<String, Object> cost = new HashMap<String, Object>();
        cost.put("material", item.name().toLowerCase());
        cost.put("amount", amount);
        
        return cost;
    }
    
    public boolean has(PlayerInventory inventory)
    {
        return inventory.contains(item, getAmount());
    }
    
    public void use(PlayerInventory inventory)
    {
        ItemStack itemStack = getItemStack();
        inventory.removeItem(itemStack);
    }
    
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
        return item.name().toLowerCase().replace("_", " ").replace(" block", "");
    }
}
