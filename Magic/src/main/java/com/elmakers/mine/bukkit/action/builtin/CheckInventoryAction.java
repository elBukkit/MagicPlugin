package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.ActionHandler;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class CheckInventoryAction extends CompoundAction {
    private ItemStack item;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        String itemKey = parameters.getString("item");
        item = context.getController().createItem(itemKey);
        if (item == null) {
            context.getLogger().warning("Invalid item: " + itemKey);
        }
    }

    protected boolean isAllowed(CastContext context) {
        Entity targetEntity = context.getTargetEntity();
        if (targetEntity == null || !(targetEntity instanceof Player)) return false;
        Mage mage = context.getController().getMage(targetEntity);
        return mage.hasItem(item);
    }

    @Override
    public SpellResult step(CastContext context) {
        boolean allowed = isAllowed(context);
        ActionHandler actions = getHandler("actions");
        if (actions == null || actions.size() == 0) {
            return allowed ? SpellResult.CAST : SpellResult.STOP;
        }

        if (!allowed) {
            return SpellResult.NO_TARGET;
        }
        return startActions();
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }

    @Override
    public boolean requiresTargetEntity() {
        return true;
    }
}