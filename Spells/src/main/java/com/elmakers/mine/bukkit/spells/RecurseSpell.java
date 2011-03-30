package com.elmakers.mine.bukkit.spells;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.magic.Spell;
import com.elmakers.mine.bukkit.persistence.dao.MaterialData;
import com.elmakers.mine.bukkit.persistence.dao.MaterialList;
import com.elmakers.mine.bukkit.persistence.dao.ParameterData;
import com.elmakers.mine.bukkit.utilities.BlockRecurse;

public class RecurseSpell extends Spell
{
    private final BlockRecurse blockRecurse            = new BlockRecurse();
    private MaterialList       destructibleMaterials   = new MaterialList();
    private int                defaultSearchDistance   = 32;
    
    @Override
    public String getDescription()
    {
        return "Recursively modify an object";
    }

    @Override
    public String getName()
    {
        return "recurse";
    }

    @Override
    public boolean onCast(ParameterMap parameters)
    {
        targeting.setMaxRange(defaultSearchDistance, true);
        Block target = targeting.getTargetBlock();
        if (target == null)
        {
            castMessage(player, "No target");
            return false;
        }
        
        Material material = target.getType();
        byte data = target.getData();

        ItemStack buildWith = getBuildingMaterial();
        if (buildWith != null)
        {
            MaterialData md = new MaterialData(buildWith);
            material = md.getType();
            data = md.getData();
        }

        for (ParameterData parameter : parameters)
        {
            if (parameter.isMatch("with"))
            {
                material = parameter.getMaterial();
                data = 0;
                continue;
            }
        }
        
        return true;
    }

}
