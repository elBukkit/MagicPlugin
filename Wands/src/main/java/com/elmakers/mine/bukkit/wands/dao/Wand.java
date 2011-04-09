package com.elmakers.mine.bukkit.wands.dao;

import java.util.List;

import org.bukkit.Material;

import com.elmakers.mine.bukkit.persisted.PersistClass;
import com.elmakers.mine.bukkit.persisted.PersistField;
import com.elmakers.mine.bukkit.persisted.Persisted;
import com.elmakers.mine.bukkit.persistence.dao.MaterialData;

@PersistClass(schema="magic", name="wand")
public class Wand extends Persisted
{
    protected MaterialData   item;
    protected String         name;
    protected String         description;
    protected List<WandSlot> slots;
    
    public Wand()
    {
        
    }
    
    public Wand(Material itemType)
    {
       item = new MaterialData(itemType);
    }
    
    @PersistField
    public String getName()
    {
        return name;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    @PersistField
    public String getDescription()
    {
        return description;
    }
    
    public void setDescription(String description)
    {
        this.description = description;
    }
    
    @PersistField(id=true)
    public MaterialData getItem()
    {
        return item;
    }
    
    public void setItem(MaterialData item)
    {
        this.item = item;
    }

    @PersistField
    public List<WandSlot> getSlots()
    {
        return slots;
    }

    public void setSlots(List<WandSlot> slots)
    {
        this.slots = slots;
    }
}
