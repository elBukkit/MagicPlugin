package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.utilities.PluginProperties;

public class AbsorbSpell extends Spell 
{
    private int giveAmount = 1;
    
	@Override
	public boolean onCast(Map<String, Object> parameters) 
	{
	    Material material = Material.AIR;
        List<Material> buildingMaterials = spells.getBuildingMaterials();
        
        if (parameters.containsKey("material"))
        {
            material = (Material)parameters.get("materials");
            if (buildingMaterials.contains(material))
            {
                int amount = giveAmount;
                byte data = 0;
                castMessage(player, "Manifesting some " + material.name().toLowerCase());
                return giveMaterial(material, amount, (short)0 , data);
            }
        }
        
		if (!isUnderwater())
		{
			noTargetThrough(Material.STATIONARY_WATER);
			noTargetThrough(Material.WATER);
		}
		Block target = getTargetBlock();
		
		if (target == null) 
		{
			castMessage(player, "No target");
			return false;
		}
		int amount = 1;
			
		castMessage(player, "Absorbing some " + target.getType().name().toLowerCase());
			
		return giveMaterial(target.getType(), amount, (short)0 , target.getData());
	}
	
	@Override
	public void onLoad(PluginProperties properties)
	{
		//defaultAmount = properties.getInteger("spells-absorb-amount", defaultAmount);
	}
}
