package com.elmakers.mine.bukkit.wands.dao;

import com.elmakers.mine.bukkit.persisted.PersistClass;
import com.elmakers.mine.bukkit.persisted.PersistField;
import com.elmakers.mine.bukkit.persistence.dao.MaterialData;

@PersistClass(schema="magic", name="wand")
public class Wand
{
    protected String       id;
    protected String       description;
    protected MaterialData item;
    
    @PersistField(id=true)
    public String getId()
    {
        return id;
    }
    
    public void setId(String id)
    {
        this.id = id;
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
    
    @PersistField
    public MaterialData getItem()
    {
        return item;
    }
    
    public void setItem(MaterialData item)
    {
        this.item = item;
    }
}
