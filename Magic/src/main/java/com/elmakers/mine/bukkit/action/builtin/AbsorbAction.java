package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.action.BaseSpellAction;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class AbsorbAction extends BaseSpellAction
{   
	@SuppressWarnings("deprecation")
	@Override
	public SpellResult perform(CastContext context)
	{
        Block target = context.getTargetBlock();
        Mage mage = context.getMage();
		Wand wand = context.getWand();
		if (wand == null)
		{
			return SpellResult.FAIL;
		}

        MageController controller = context.getController();
		Material material = target.getType();
		byte data = target.getData();

		MaterialSet buildingMaterials = controller.getBuildingMaterialSet();
		MaterialSet restrictedMaterials = mage.getRestrictedMaterialSet();
		if (material == null || material == Material.AIR)
		{
			return SpellResult.NO_TARGET;
		}
		if (!mage.getCommandSender().hasPermission("Magic.bypass_restricted") && (!buildingMaterials.testBlock(target) || restrictedMaterials.testBlock(target)))
		{
			return SpellResult.NO_TARGET;
		}
		
		// Add to the wand
		MaterialAndData mat = new MaterialAndData(material, data);
		if (!wand.addBrush(mat.getKey())) {
			// Still try and activate it
			wand.setActiveBrush(mat.getKey());
			return SpellResult.NO_TARGET;
		}
		
		// And activate it
		wand.setActiveBrush(mat.getKey());
		
		return SpellResult.CAST;
	}

    @Override
    public boolean requiresTarget() {
        return true;
    }
}
