package com.elmakers.mine.bukkit.action.builtin;

import static com.google.common.base.Verify.verifyNotNull;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class SuperProtectionAction extends BaseSpellAction
{
    private int duration;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        duration = parameters.getInt("duration");
    }

    @Override
    public SpellResult perform(CastContext context) {
        Entity targetEntity = verifyNotNull(context.getTargetEntity());
        Mage mage = context.getController().getMage(targetEntity);
        mage.enableSuperProtection(duration);
        return SpellResult.CAST;
    }

    @Override
    public boolean requiresTarget()
    {
        return true;
    }

    @Override
    public boolean requiresTargetEntity()
    {
        return true;
    }
}
