package com.elmakers.mine.bukkit.spells;

import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.magic.Spell;
import com.elmakers.mine.bukkit.persistence.dao.MaterialData;
import com.elmakers.mine.bukkit.persistence.dao.MaterialList;
import com.elmakers.mine.bukkit.persistence.dao.ParameterMap;
import com.elmakers.mine.bukkit.utilities.BlockRecurse;
import com.elmakers.mine.bukkit.utilities.ReplaceMaterialAction;

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
        
        MaterialData material = getBuildingMaterial(parameters, target);
        
        ReplaceMaterialAction action = new ReplaceMaterialAction(target, material);
        blockRecurse.recurse(target, action);
        magic.addToUndoQueue(player, action.getBlocks());
        castMessage(player, "Filled " + action.getBlocks().size() + " blocks with " + material.getName()); 
        
        return true;
    }

}
