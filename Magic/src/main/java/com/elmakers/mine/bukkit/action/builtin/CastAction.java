package com.elmakers.mine.bukkit.action.builtin;

import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class CastAction extends BaseSpellAction {
    private @Nullable String spellKey;
    private boolean costFree;
    private boolean cooldownFree;
    private @Nullable ConfigurationSection spellParameters;
    private boolean asConsole;
    private boolean asTarget;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);

        spellKey = parameters.getString("spell");
        spellParameters = parameters.getConfigurationSection("spell_parameters");
        costFree = parameters.getBoolean("cost_free", true);
        cooldownFree = parameters.getBoolean("cooldown_free", false);
        asConsole = parameters.getBoolean("console", false);
        asTarget = parameters.getBoolean("as_target", true);
    }

    @Override
    public SpellResult perform(CastContext context) {
        if (spellKey == null) {
            return SpellResult.FAIL;
        }

        Mage targetMage = null;
        if (asConsole) {
            targetMage = context.getController().getConsoleMage();
        } else if (asTarget) {
            Entity target = context.getTargetEntity();
            if (target != null) {
                targetMage = context.getController().getMage(target);
            }
        } else {
            targetMage = context.getMage();
        }

        if (targetMage == null) {
            return SpellResult.NO_TARGET;
        }

        Spell spell = targetMage.getSpell(spellKey);
        if (spell == null) {
            return SpellResult.FAIL;
        }

        boolean originalCostFree = targetMage.isCostFree();
        boolean originalCooldownFree = targetMage.isCooldownFree();

        targetMage.setCostFree(costFree);
        targetMage.setCooldownFree(cooldownFree);

        try {
            return spell.cast(spellParameters)
                    ? SpellResult.CAST
                    : SpellResult.FAIL;
        } finally {
            targetMage.setCostFree(originalCostFree);
            targetMage.setCooldownFree(originalCooldownFree);
        }
    }

    @Override
    public boolean requiresTarget() {
        return asTarget;
    }

    @Override
    public boolean requiresTargetEntity() {
        return asTarget;
    }
}
