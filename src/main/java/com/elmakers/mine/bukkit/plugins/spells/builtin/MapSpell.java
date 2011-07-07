package com.elmakers.mine.bukkit.plugins.spells.builtin;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.plugins.spells.Spell;

public class MapSpell extends Spell
{

    @Override
    public boolean onCast(String[] parameters)
    {
        CraftInventory ci = (CraftInventory)player.getInventory();
        if (!ci.contains(Material.MAP))
        {
            castMessage(player, "Here's a map!");
            ci.addItem(new ItemStack(Material.MAP));
            return true;
        }
        HashMap<Integer,ItemStack> currentMap = ci.all(Material.MAP);
        ItemStack first = currentMap.values().iterator().next();
        short mapId = first.getDurability();
        castMessage(player, "You've got map#" + mapId);
               
        return false;
    }

    @Override
    public String getName()
    {
        return "map";
    }

    @Override
    public String getCategory()
    {
        return "exploration";
    }

    @Override
    public String getDescription()
    {
        return "Produce a map";
    }

    @Override
    public Material getMaterial()
    {
        return Material.PAPER;
    }

}
