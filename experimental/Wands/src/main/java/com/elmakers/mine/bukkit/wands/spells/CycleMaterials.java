package com.elmakers.mine.bukkit.wands.spells;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.elmakers.mine.bukkit.magic.Spell;
import com.elmakers.mine.bukkit.persistence.dao.ParameterMap;
import com.elmakers.mine.bukkit.wands.Wands;

public class CycleMaterials extends Spell
{
    protected final Wands wands;
    
    public CycleMaterials(Wands wands)
    {
        this.wands = wands;
    }
    
    @Override
    public String getDescription()
    {
        return "Cycle your active material inventory";
    }

    @Override
    public String getName()
    {
        return "materials";
    }

    @Override
    public boolean onCast(ParameterMap parameters)
    {
        List<Material> buildingMaterials = magic.getBuildingMaterials();
        PlayerInventory inventory = player.getInventory();
        ItemStack[] contents = inventory.getContents();
        int firstMaterialSlot = 8;
        boolean foundAir = false;

        for (int i = 8; i >= 0; i--)
        {
            Material mat = contents[i].getType();
            if (mat == Material.AIR)
            {
                if (foundAir)
                {
                    break;
                }
                else
                {
                    foundAir = true;
                    firstMaterialSlot = i;
                    continue;
                }
            }
            else
            {
                if (buildingMaterials.contains(mat))
                {
                    firstMaterialSlot = i;
                    continue;
                }
                else
                {
                    break;
                }
            }
        }

        if (firstMaterialSlot == 8)
        {
            return false;
        }

        ItemStack lastSlot = contents[8];
        for (int i = 7; i >= firstMaterialSlot; i--)
        {
            contents[i + 1] = contents[i];
        }
        contents[firstMaterialSlot] = lastSlot;

        inventory.setContents(contents);
        return true;
    }
}
