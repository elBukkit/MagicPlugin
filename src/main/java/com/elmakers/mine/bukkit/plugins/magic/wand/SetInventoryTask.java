package com.elmakers.mine.bukkit.plugins.magic.wand;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SetInventoryTask implements Runnable
{
    protected HashMap<Integer, ItemStack>       items = new HashMap<Integer, ItemStack>();
    protected Player                            player;
    
    public SetInventoryTask(Player player)
    {
        this.player = player;
    }
    
    public void addItem(int slot, ItemStack stack)
    {
        items.put(slot, stack);
    }
    
    public void run()
    {
        for (Integer key : items.keySet())
        {
            player.getInventory().setItem(key, items.get(key));
        }
    }
}
