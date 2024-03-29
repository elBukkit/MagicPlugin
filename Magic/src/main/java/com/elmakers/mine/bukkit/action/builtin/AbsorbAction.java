package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.block.DefaultMaterials;
import com.elmakers.mine.bukkit.block.MaterialAndData;

public class AbsorbAction extends BaseSpellAction
{
    private boolean wildcard = false;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        wildcard = parameters.getBoolean("wildcard");
    }

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
        if (mage.isRestricted(material)) {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }

        MaterialSet buildingMaterials = controller.getBuildingMaterialSet();
        MaterialSet restrictedMaterials = mage.getRestrictedMaterialSet();
        if (material == null || DefaultMaterials.isAir(material))
        {
            return SpellResult.NO_TARGET;
        }
        if (!mage.getCommandSender().hasPermission("magic.bypass_restricted") && (!buildingMaterials.testBlock(target) || restrictedMaterials.testBlock(target)))
        {
            return SpellResult.NO_TARGET;
        }

        // Add to the wand
        MaterialAndData mat = new MaterialAndData(target);
        if (wildcard) {
            mat.setData(null);
        }
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
