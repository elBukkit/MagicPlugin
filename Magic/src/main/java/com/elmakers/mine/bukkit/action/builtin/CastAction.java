package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.spell.MageSpell;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

import javax.annotation.Nonnull;

public class CastAction extends BaseSpellAction {

    @Nonnull
    private String spell = "";
    @Nonnull
    private Boolean costFree = false, cooldownFree = false;
    private ConfigurationSection spellParameters;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);

        this.spell = parameters.getString("spell", null);
        this.spellParameters = parameters.getConfigurationSection("spell_parameters");
        this.costFree = parameters.getBoolean("cost_free", false);
        this.cooldownFree = parameters.getBoolean("cooldown_free", false);
    }

    @Override
    public SpellResult perform(CastContext context) {
        if (context.getTargetEntity() == null) {
            return SpellResult.NO_TARGET;
        }

        Mage targetMage = context.getController().getMage(context.getTargetEntity());

        boolean originalCostFree = targetMage.isCostFree();
        boolean originalCooldownFree = targetMage.isCooldownFree();

        targetMage.setCostFree(costFree);
        targetMage.setCooldownFree(cooldownFree);

        try {
            MageSpell mageSpell = targetMage.getSpell(spell);

            if (mageSpell == null) {
                return SpellResult.FAIL;
            }

            boolean casted = mageSpell.cast(spellParameters);

            if (!casted) {
                return SpellResult.FAIL;
            }
        } catch (NullPointerException exception) {
            exception.printStackTrace();
        }

        targetMage.setCostFree(originalCostFree);
        targetMage.setCooldownFree(originalCooldownFree);

        return SpellResult.CAST;
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