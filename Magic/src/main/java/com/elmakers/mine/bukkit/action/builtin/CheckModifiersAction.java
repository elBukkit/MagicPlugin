package com.elmakers.mine.bukkit.action.builtin;

import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.action.CheckAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class CheckModifiersAction extends CheckAction {
    private Collection<String> required;
    private Collection<String> blocked;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        required = ConfigurationUtils.getStringList(parameters, "required");
        blocked = ConfigurationUtils.getStringList(parameters, "blocked");
    }

    @Override
    protected boolean isAllowed(CastContext context) {
        Entity targetEntity = context.getTargetEntity();
        Mage mage = context.getController().getRegisteredMage(targetEntity);

        if (blocked != null && mage != null) {
            for (String check : blocked) {
                if (mage.hasModifier(check)) {
                    return false;
                }
            }
        }

        if (required != null) {
            if (mage == null) {
                return false;
            }
            for (String check : required) {
                if (mage.hasModifier(check)) {
                    return true;
                }
            }
        }

        return required == null || required.isEmpty();
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }

    @Override
    public boolean requiresTargetEntity() {
        return true;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("required");
        parameters.add("blocked");
    }
}
