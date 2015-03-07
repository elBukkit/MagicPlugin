package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.action.BaseSpellAction;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import java.util.Arrays;
import java.util.Collection;

public class FallProtectionAction extends BaseSpellAction {
    private int fallProtection;
    private int fallProtectionCount;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        fallProtection = parameters.getInt("duration", 5000);
        fallProtectionCount = parameters.getInt("protection_count", 1);
    }

    @Override
    public SpellResult perform(CastContext context) {
        Entity targetEntity = context.getTargetEntity();
        MageController controller = context.getController();
        Mage targetMage = controller.isMage(targetEntity) ? controller.getMage(targetEntity) : null;

        if (targetMage == null) {
            return SpellResult.NO_TARGET;
        }

        targetMage.enableFallProtection(fallProtection, fallProtectionCount, context.getSpell());
        return SpellResult.CAST;
    }

    @Override
    public void getParameterNames(Collection<String> parameters) {
        super.getParameterNames(parameters);
        parameters.add("duration");
        parameters.add("protection_count");
    }

    @Override
    public void getParameterOptions(Collection<String> examples, String parameterKey) {
        if (parameterKey.equals("duration")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_DURATIONS)));
        } else if (parameterKey.equals("protection_count")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_SIZES)));
        } else {
            super.getParameterOptions(examples, parameterKey);
        }
    }

    @Override
    public boolean requiresTargetEntity()
    {
        return true;
    }
}
