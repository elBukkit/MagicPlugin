package com.elmakers.mine.bukkit.magic.dao;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.magic.Spell;
import com.elmakers.mine.bukkit.persisted.PersistClass;
import com.elmakers.mine.bukkit.persisted.PersistField;
import com.elmakers.mine.bukkit.persisted.Persisted;
import com.elmakers.mine.bukkit.persistence.dao.ParameterData;
import com.elmakers.mine.bukkit.persistence.dao.ParameterMap;
import com.elmakers.mine.bukkit.persistence.dao.PlayerData;

@PersistClass(schema = "magic", name = "spell")
public class SpellVariant extends Persisted implements Comparable<SpellVariant>
{
    private double              cooldown;
    private String              description;
    private String              name;
    private List<ParameterData> parameters;
    private String              spell;
    private List<String>        tags;
    private List<CastingCost>   costs;

    public SpellVariant()
    {
    }

    public SpellVariant(String spellType, String name, String description, String parameters)
    {
        this.name = name;
        this.description = description;
        this.spell = spellType;
        this.cooldown = 0;
        this.parameters = parseParameters(parameters);
        this.tags = new ArrayList<String>();
    }

    public SpellVariant(String spellType, String name, String description, String parameters, double cooldown)
    {
        this.name = name;
        this.description = description;
        this.spell = spellType;
        this.cooldown = cooldown;
        this.parameters = parseParameters(parameters);
        this.tags = new ArrayList<String>();
    }

    public boolean cast(Spell playerSpell)
    {
        return cast(playerSpell, null);
    }
    
    public boolean cast(Spell playerSpell, String[] extraParameters)
    {
        // TODO: accept command-line parameters!

        ParameterMap parameterMap = new ParameterMap();
        parameterMap.addAll(parameters);
        return playerSpell.cast(parameterMap);
    }
    
    protected List<ParameterData> parseParameters(String paramString)
    {
        List<ParameterData> parameters = new ArrayList<ParameterData>();
        
        // TODO!
        
        return parameters;
    }

    public int compareTo(SpellVariant other)
    {
        return getName().compareTo(other.getName());
    }

    public double getCooldown()
    {
        return cooldown;
    }

    @PersistField
    public String getDescription()
    {
        return description;
    }

    @PersistField(id = true)
    public String getName()
    {
        return name;
    }

    @PersistField
    public List<ParameterData> getParameters()
    {
        return parameters;
    }

    public String getPermissionNode()
    {
        return "Magic.spells." + getName();
    }

    @PersistField
    public String getSpell()
    {
        return spell;
    }

    @PersistField
    public List<String> getTags()
    {
        return tags;
    }

    public boolean hasSpellPermission(Player player)
    {
        if (player == null)
        {
            return false;
        }
        PlayerData playerData = persistence.get(player.getName(), PlayerData.class);
        if (playerData == null)
        {
            return false;
        }
        return playerData.isSet(getPermissionNode());
    }

    public boolean hasTag(String tag)
    {
        for (String checkTag : tags)
        {
            if (checkTag.equalsIgnoreCase(tag))
            {
                return true;
            }
        }

        return false;
    }

    public boolean isMatch(String spell, String[] params)
    {
        if (params == null)
        {
            params = new String[0];
        }
        return name.equalsIgnoreCase(spell) && parameters.equals(params);
    }

    public void setCooldown(double cooldown)
    {
        this.cooldown = cooldown;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setParameters(List<ParameterData> parameters)
    {
        this.parameters = parameters;
    }

    public void setSpell(String spell)
    {
        this.spell = spell;
    }

    public void setTags(List<String> tags)
    {
        this.tags = tags;
    }

    @PersistField
    public void setCosts(List<CastingCost> costs)
    {
        this.costs = costs;
    }

    public List<CastingCost> getCosts()
    {
        return costs;
    }
}