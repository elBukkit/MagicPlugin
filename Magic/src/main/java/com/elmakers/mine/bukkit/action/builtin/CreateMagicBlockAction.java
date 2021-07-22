package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.magic.MagicBlock;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class CreateMagicBlockAction extends BaseSpellAction {
    protected String templateKey;
    protected ConfigurationSection parameters;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        templateKey = parameters.getString("template");
        this.parameters = parameters.getConfigurationSection("block_parameters");
    }

    @Override
    public SpellResult perform(CastContext context) {
        Mage mage = context.getMage();
        MageController controller = context.getController();
        Location location = context.getTargetBlock().getLocation();
        MagicBlock magicBlock = controller.addMagicBlock(location, templateKey, mage.getId(), mage.getName(), parameters);
        return magicBlock == null ? SpellResult.FAIL : SpellResult.CAST;
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }
}
