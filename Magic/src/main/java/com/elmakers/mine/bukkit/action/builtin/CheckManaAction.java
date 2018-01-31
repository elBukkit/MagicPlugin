package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.ActionHandler;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import org.bukkit.configuration.ConfigurationSection;

public class CheckManaAction extends CompoundAction {
    private boolean requireNotFull = false;
    private boolean requireEmpty = false;
    private double requireAmount = 0;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        requireAmount = parameters.getDouble("require_mana", 0);
        requireNotFull = parameters.getBoolean("require_mana_not_full", false);
        requireEmpty = parameters.getBoolean("require_mana_empty", false);
    }

    protected boolean isAllowed(CastContext context) {
        Mage mage = context.getMage();
        double currentMana = mage.getMana();
        if (requireAmount > 0 && currentMana < requireAmount) {
            return false;
        }
        if (requireEmpty && currentMana > 0) {
            return false;
        }
        int manaMax = mage.getManaMax();
        if (requireNotFull && currentMana >= manaMax) {
            return false;
        }
        return true;
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
        return false;
    }
}