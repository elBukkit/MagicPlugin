package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class CastAction extends BaseSpellAction {
    private String spellKey;
    private boolean costFree;
    private boolean cooldownFree;
    private ConfigurationSection spellParameters;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);

        this.spellKey = parameters.getString("spell");
        this.spellParameters = parameters.getConfigurationSection("spell_parameters");
        this.costFree = parameters.getBoolean("cost_free", true);
        this.cooldownFree = parameters.getBoolean("cooldown_free", false);
    }

    @Override
    public SpellResult perform(CastContext context) {
        if (context.getTargetEntity() == null) {
            return SpellResult.NO_TARGET;
        }
        if (spellKey == null) {
            return SpellResult.FAIL;
        }

        Mage targetMage = context.getController().getMage(context.getTargetEntity());

        boolean originalCostFree = targetMage.isCostFree();
        boolean originalCooldownFree = targetMage.isCooldownFree();

        targetMage.setCostFree(costFree);
        targetMage.setCooldownFree(cooldownFree);

        boolean success = false;
        try {
            Spell spell = targetMage.getSpell(spellKey);
            if (spell != null) {
                success = spell.cast(spellParameters);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            success = false;
        }

        targetMage.setCostFree(originalCostFree);
        targetMage.setCooldownFree(originalCooldownFree);

        return success ? SpellResult.CAST : SpellResult.FAIL;
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