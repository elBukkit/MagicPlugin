package com.elmakers.mine.bukkit.spells;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.magic.Spell;
import com.elmakers.mine.bukkit.magic.dao.SpellVariant;
import com.elmakers.mine.bukkit.persistence.dao.MaterialList;
import com.elmakers.mine.bukkit.persistence.dao.ParameterMap;
import com.elmakers.mine.bukkit.wands.dao.Wand;

public class CycleSpells extends Spell
{
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
        Inventory inventory = player.getInventory();
        ItemStack[] contents = inventory.getContents();
        ItemStack[] active = new ItemStack[9];
        List<Wand> wands = persistence.getAll(Wand.class);
        MaterialList wandItems = new MaterialList();
        for (Wand wand : wands)
        {
            wandItems.add(wand.getItem());
        }
        
        for (int i = 0; i < 9; i++)
        {
            active[i] = contents[i];
        }

        int maxSpellSlot = 0;
        int firstSpellSlot = -1;
        for (int i = 0; i < 9; i++)
        {
            boolean isWand = wandItems.contains(active[i].getType());
            boolean isSpell = false;
            if (active[i].getType() != Material.AIR)
            {
                SpellVariant spell = null;// magic.getSpell(active[i].getType(),
                                          // player);
                isSpell = spell != null;
            }

            if (isSpell)
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
            if (!wandItems.contains(contents[i].getType()))
            {
                for (int di = 1; di < numSpellSlots; di++)
                {
                    int dni = (ddi + di) % numSpellSlots;
                    int ni = dni + firstSpellSlot;
                    if (!wandItems.contains(active[ni].getType()))
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
