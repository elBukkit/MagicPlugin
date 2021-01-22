package com.elmakers.mine.bukkit.action.builtin;

import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.CheckAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.item.Cost;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class TakeCostsAction extends CheckAction {
    private List<Cost> costs = null;

    @Override
    public void processParameters(CastContext context, ConfigurationSection parameters) {
        super.processParameters(context, parameters);
        costs = Cost.parseCosts(ConfigurationUtils.getConfigurationSection(parameters, "costs"), context.getController());
    }

    @Override
    protected boolean isAllowed(CastContext context) {
        if (costs == null) {
            return true;
        }
        for (Cost cost : costs) {
            if (!cost.has(context.getMage(), context.getWand(), null)) {
                String baseMessage = context.getMessage("insufficient");
                String costDescription = cost.getFullDescription(context.getController().getMessages(), null);
                costDescription = baseMessage.replace("$cost", costDescription);
                context.showMessage(costDescription);
                return false;
            }
        }
        if (costs != null) {
            for (Cost cost : costs) {
                cost.deduct(context.getMage(), context.getWand(), null);
            }
        }

        return true;
    }
}
