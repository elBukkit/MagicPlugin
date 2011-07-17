package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.plugins.magic.Spell;

public class MapSpell extends Spell
{
    @Override
    public boolean onCast(ConfigurationNode parameters) 
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
}
