package com.elmakers.mine.bukkit.wands.spells;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.magic.Spell;
import com.elmakers.mine.bukkit.persistence.dao.ParameterMap;
import com.elmakers.mine.bukkit.wands.Wands;
import com.elmakers.mine.bukkit.wands.dao.Wand;
import com.elmakers.mine.bukkit.wands.dao.WandSlot;

public class CycleSpells extends Spell
{
    protected final Wands wands;
    
    public CycleSpells(Wands wands)
    {
        this.wands = wands;
    }
    
    @Override
    public String getDescription()
    {
        return "Cycle your active spell inventory";
    }

    @Override
    public String getName()
    {
        return "spells";
    }

    @Override
    public boolean onCast(ParameterMap parameters)
    {
        Wand wand = wands.getActiveWand(player);
        if (wand == null)
        {
            return false;
        }
        
        Inventory inventory = player.getInventory();
        ItemStack[] contents = inventory.getContents();
        ItemStack[] active = new ItemStack[9];
        
        for (int i = 0; i < 9; i++)
        {
            active[i] = contents[i];
        }

        int maxSpellSlot = 0;
        int firstSpellSlot = -1;
        for (int i = 0; i < 9; i++)
        {
            boolean isWand = player.getInventory().getHeldItemSlot() == i;
            WandSlot slot = null;
            Material item = active[i].getType();
            if (item != Material.AIR)
            {
                slot = wand.getSlot(item);
            }

            if (slot != null)
            {
                if (firstSpellSlot < 0)
                {
                    firstSpellSlot = i;
                }
                maxSpellSlot = i;
            }
            else
            {
                if (!isWand && firstSpellSlot >= 0)
                {
                    break;
                }
            }

        }

        int numSpellSlots = firstSpellSlot < 0 ? 0 : maxSpellSlot - firstSpellSlot + 1;

        if (numSpellSlots < 2)
        {
            return false;
        }

        for (int ddi = 0; ddi < numSpellSlots; ddi++)
        {
            int i = ddi + firstSpellSlot;
            boolean isWand = player.getInventory().getHeldItemSlot() == i;
            if (!isWand)
            {
                for (int di = 1; di < numSpellSlots; di++)
                {
                    int dni = (ddi + di) % numSpellSlots;
                    int ni = dni + firstSpellSlot;
                    if (!isWand)
                    {
                        contents[i] = active[ni];
                        break;
                    }
                }
            }
        }

        inventory.setContents(contents);
        
        return true;
    }
}
