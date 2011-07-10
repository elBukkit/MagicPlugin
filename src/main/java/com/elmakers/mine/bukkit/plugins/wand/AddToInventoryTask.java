package com.elmakers.mine.bukkit.plugins.wand;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AddToInventoryTask implements Runnable
{
    protected List<ItemStack> items;
    protected Player          player;
    
    public AddToInventoryTask(Player player, List<ItemStack> items)
    {
        this.items = items;
        this.player = player;
    }
    
    public void run()
    {
        for (ItemStack item : items)
        {
            player.getInventory().addItem(item);
        }
    }
}
