package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.action.BaseSpellAction;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.Set;

public class AbsorbAction extends BaseSpellAction
{   
	@SuppressWarnings("deprecation")
	@Override
	public SpellResult perform(CastContext context)
	{
        Block target = context.getTargetBlock();
        Mage mage = context.getMage();
		Wand wand = mage.getActiveWand();
		if (wand == null)
		{
			return SpellResult.FAIL;
		}

        MageController controller = context.getController();
		Material material = target.getType();
		byte data = target.getData();

		Set<Material> buildingMaterials = controller.getBuildingMaterials();
        Set<Material> restrictedMaterials = mage.getRestrictedMaterials();
		if (material == null || material == Material.AIR || !buildingMaterials.contains(material) || restrictedMaterials.contains(material))
		{
			return SpellResult.NO_TARGET;
		}
		
		// Add to the wand
		MaterialAndData mat = new MaterialAndData(material, data);
		wand.addBrush(mat.getKey());
		
		// And activate it
		wand.setActiveBrush(mat.getKey());
		
		return SpellResult.CAST;
	}

    @Override
    public boolean requiresTarget() {
        return true;
    }
}
