package com.elmakers.mine.bukkit.magic.dao;

import org.bukkit.Material;

import com.elmakers.mine.bukkit.persisted.PersistClass;
import com.elmakers.mine.bukkit.persisted.PersistField;
import com.elmakers.mine.bukkit.persistence.dao.MaterialData;

@PersistClass(schema="magic", name="cost", contained=true)
public class CastingCost
{
    protected CastingCostType type   = CastingCostType.REAGENT;
    protected double          amount = 0;
    protected MaterialData    item   = null;
    
    public void setItem(Material material, int count)
    {
        type = CastingCostType.REAGENT;
        item = new MaterialData(material);
        amount = count;
    }
    
    public void setMana(float amount)
    {
        type = CastingCostType.MANA;
        item = null;
        this.amount = amount;
    }
    
    public void setHealth(float amount)
    {
        type = CastingCostType.HEALTH;
        item = null;
        this.amount = amount;
    }
    
    @PersistField
    public MaterialData getItem()
    {
        return item;
    }
    
    public void setItem(MaterialData item)
    {
        this.item = item;
    }
    
    @PersistField
    public double getAmount()
    {
        return amount;
    }
    
    public void setAmount(double amount)
    {
        this.amount = amount;
    }
}
