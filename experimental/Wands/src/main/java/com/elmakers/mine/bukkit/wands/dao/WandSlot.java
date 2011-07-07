package com.elmakers.mine.bukkit.wands.dao;

import java.util.List;

import com.elmakers.mine.bukkit.persisted.PersistClass;
import com.elmakers.mine.bukkit.persisted.PersistField;
import com.elmakers.mine.bukkit.persisted.Persisted;
import com.elmakers.mine.bukkit.persistence.dao.MaterialData;

@PersistClass(schema="magic", name="slot")
public class WandSlot extends Persisted
{
    protected MaterialData      id;
    protected String            name;
    protected String            description;
    protected List<WandCommand> commands;

    @PersistField(id=true)
    public MaterialData getId()
    {
        return id;
    }
    
    public void setId(MaterialData id)
    {
        this.id = id;
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
    
    @PersistField
    public List<WandCommand> getCommands()
    {
        return commands;
    }
    
    public void setCommands(List<WandCommand> commands)
    {
        this.commands = commands;
    }
}
