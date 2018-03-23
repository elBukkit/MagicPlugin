package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.protection.TownyManager;

public class CreatePlotAction extends BaseSpellAction {
    private Double price;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        if (parameters.contains("price")) {
            price = parameters.getDouble("price", 0);
        } else {
            price = null;
        }
    }

    @Override
    public SpellResult perform(CastContext context) {
        MageController apiController = context.getController();
        if (!(apiController instanceof MagicController)) {
            return SpellResult.FAIL;
        }
        MagicController controller = (MagicController)apiController;
        TownyManager towny = controller.getTowny();
        if (!towny.createPlot(context.getTargetLocation(), price)) {
            return SpellResult.NO_TARGET;
        }
        return SpellResult.CAST;
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }
}
