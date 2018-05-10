package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class CastAction extends BaseSpellAction {

    private String spell;
    private ConfigurationSection spellParameters;
    private boolean costFree, cooldownFree;

    private Mage targetMage;
    private boolean originalCostFree, originalCooldownFree;

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

        targetMage = context.getController().getMage(context.getTargetEntity());

        originalCostFree = targetMage.isCostFree();
        originalCooldownFree = targetMage.isCooldownFree();

        targetMage.setCostFree(costFree);
        targetMage.setCooldownFree(cooldownFree);

        try {
            targetMage.getSpell(spell).cast(spellParameters);
        } catch (NullPointerException exception) {
            exception.printStackTrace();
        }

        return SpellResult.CAST;
    }

    @Override
    public void finish(CastContext context) {
        super.finish(context);

        targetMage.setCostFree(originalCostFree);
        targetMage.setCooldownFree(originalCooldownFree);
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