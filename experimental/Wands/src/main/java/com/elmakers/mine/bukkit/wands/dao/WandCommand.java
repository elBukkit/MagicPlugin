package com.elmakers.mine.bukkit.wands.dao;

import java.util.List;

import com.elmakers.mine.bukkit.magic.dao.SpellVariant;
import com.elmakers.mine.bukkit.persisted.PersistClass;
import com.elmakers.mine.bukkit.persisted.PersistField;
import com.elmakers.mine.bukkit.persisted.Persisted;
import com.elmakers.mine.bukkit.persistence.dao.ParameterData;

@PersistClass(schema="magic", name="command")
public class WandCommand extends Persisted
{
    protected int id;
    protected List<WandRule> rules;
    protected SpellVariant spell;
    protected List<ParameterData> parameters;
    
    @PersistField(id=true, auto=true)
    public int getId()
    {
        return id;
    }
    
    public void setId(int id)
    {
        this.id = id;
    }
    
    @PersistField(contained=true)
    public List<WandRule> getRules()
    {
        return rules;
    }
    
    public void setRules(List<WandRule> rules)
    {
        this.rules = rules;
    }
    
    @PersistField
    public SpellVariant getSpell()
    {
        return spell;
    }
    
    public void setSpell(SpellVariant spell)
    {
        this.spell = spell;
    }
    
    @PersistField(contained=true)
    public List<ParameterData> getParameters()
    {
        return parameters;
    }
    
    public void setParameters(List<ParameterData> parameters)
    {
        this.parameters = parameters;
    }
}
