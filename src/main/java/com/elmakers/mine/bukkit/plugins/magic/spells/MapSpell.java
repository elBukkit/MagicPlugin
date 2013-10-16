package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class MapSpell extends Spell
{
    @Override
    public boolean onCast(ConfigurationNode parameters) 
    {
    	Inventory inventory = player.getInventory();
        if (!inventory.contains(Material.MAP))
        {
            castMessage(player, "Here's a map!");
            inventory.addItem(new ItemStack(Material.MAP));
            return true;
        }
        HashMap<Integer,? extends ItemStack> currentMap = inventory.all(Material.MAP);
        ItemStack first = currentMap.values().iterator().next();
        short mapId = first.getDurability();
        castMessage(player, "You've got map#" + mapId);
               
        return false;
    }

    @Override
    public void onLoad(ConfigurationNode node)
    {
        disableTargeting();
    }
}
