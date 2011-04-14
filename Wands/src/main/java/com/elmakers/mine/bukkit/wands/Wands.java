package com.elmakers.mine.bukkit.wands;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.magic.Magic;
import com.elmakers.mine.bukkit.persisted.Persistence;
import com.elmakers.mine.bukkit.utilities.PluginUtilities;
import com.elmakers.mine.bukkit.wands.dao.Wand;

public class Wands
{
    protected final Magic       magic;
    protected final Persistence     persistence;
    protected final Server          server;
    protected final PluginUtilities utilities;

    
    public Wands(Server server, Persistence persistence, PluginUtilities utilities, Magic magic)
    {
        this.server = server;
        this.magic = magic;
        this.persistence = persistence;
        this.utilities = utilities;
    }
    
    public Wand getWand(Material iconType)
    {
        Wand wand = persistence.get(iconType.getId(), Wand.class);
        if (wand == null)
        {
            wand = new Wand(iconType);
            persistence.put(wand);
        }
        
        return wand;
    }
    
    public Wand getActiveWand(Player player)
    {
        Material wandMaterial = Material.AIR;
        ItemStack currentItem = player.getItemInHand();
        if (currentItem != null)
        {
            wandMaterial = currentItem.getType();
        }
        return getWand(wandMaterial);
    }
}
