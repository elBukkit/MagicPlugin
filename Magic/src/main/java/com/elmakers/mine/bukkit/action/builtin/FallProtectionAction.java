package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;

public class FallProtectionAction extends BaseSpellAction {
    private int fallProtection;
    private int fallProtectionCount;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        fallProtection = parameters.getInt("duration", 5000);
        fallProtectionCount = parameters.getInt("protection_count", Integer.MAX_VALUE);
    }

    @Override
    public SpellResult perform(CastContext context) {
        Entity targetEntity = context.getTargetEntity();
        MageController controller = context.getController();
        Mage targetMage = targetEntity != null && controller.isMage(targetEntity)
                ? controller.getMage(targetEntity)
                : null;

        if (targetMage == null) {
            return SpellResult.NO_TARGET;
        }

        targetMage.enableFallProtection(fallProtection, fallProtectionCount, context.getSpell());
        return SpellResult.CAST;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("duration");
        parameters.add("protection_count");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("duration")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_DURATIONS));
        } else if (parameterKey.equals("protection_count")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_SIZES));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }

    @Override
    public boolean requiresTargetEntity()
    {
        return true;
    }
}
