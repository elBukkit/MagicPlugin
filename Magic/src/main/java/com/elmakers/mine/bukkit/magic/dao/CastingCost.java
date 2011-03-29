package com.elmakers.mine.bukkit.magic.dao;

import org.bukkit.Material;

import com.elmakers.mine.bukkit.persisted.PersistClass;
import com.elmakers.mine.bukkit.persisted.PersistField;
import com.elmakers.mine.bukkit.persistence.dao.MaterialData;

@PersistClass(schema="magic", name="cost", contained=true)
public class CastingCost
{
    public void addItem(Material material, int count)
    {
        this.item = new MaterialData(material);
        this.count = count;
    }
    
    @PersistField
    public float getHealth()
    {
        return health;
    }
    
    public void setHealth(float health)
    {
        this.health = health;
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
    public int getCount()
    {
        return count;
    }
    
    public void setCount(int count)
    {
        this.count = count;
    }
    
    protected float        health = 0;
    protected MaterialData item   = null;
    protected int          count  = 0;
}
