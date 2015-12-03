package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.protection.PreciousStonesManager;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

public class CreateFieldAction extends ModifyBlockAction {
    private Material fieldType;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        fieldType = ConfigurationUtils.getMaterial(parameters, "field_type");
    }

    @Override
    public SpellResult perform(CastContext context) {
        SpellResult result = super.perform(context);
        if (!result.isSuccess() || fieldType != context.getTargetBlock().getType()) {
            return result;
        }

        MageController apiController = context.getController();
        if (!(apiController instanceof MagicController)) {
            return SpellResult.FAIL;
        }
        MagicController controller = (MagicController)apiController;
        PreciousStonesManager preciousStones = controller.getPreciousStones();
        context.getMage().sendDebugMessage(ChatColor.GRAY + "Placing field", 7);
        if (!preciousStones.createField(context.getTargetLocation(), context.getMage().getPlayer())) {
            context.getMage().sendDebugMessage(ChatColor.RED + "Could not place field", 2);
            return SpellResult.NO_TARGET;
        }
        return SpellResult.CAST;
    }
}
