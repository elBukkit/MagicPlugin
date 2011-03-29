package com.elmakers.mine.bukkit.magic.dao;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.magic.Spell;
import com.elmakers.mine.bukkit.persisted.PersistClass;
import com.elmakers.mine.bukkit.persisted.PersistField;
import com.elmakers.mine.bukkit.persisted.Persisted;
import com.elmakers.mine.bukkit.persistence.dao.ParameterData;
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

    public SpellVariant(String name, String spellType)
    {
        this.name = name;
        this.spell = spellType;
        this.cooldown = 0;
        this.parameters = new ArrayList<ParameterData>();
        this.tags = new ArrayList<String>();
        this.description = "";
    }

    public boolean cast(Spell playerSpell, String[] extraParameters)
    {
        String[] spellParameters = (String[]) parameters.toArray();

        if (extraParameters.length > 0)
        {
            spellParameters = new String[extraParameters.length + parameters.size()];
            parameters.toArray(extraParameters);
            for (int i = 0; i < extraParameters.length; i++)
            {
                spellParameters[i + parameters.size()] = extraParameters[i];
            }
        }

        return playerSpell.cast(spellParameters);
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