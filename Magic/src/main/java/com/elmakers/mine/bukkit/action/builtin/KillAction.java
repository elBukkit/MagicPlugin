package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;

public class KillAction extends BaseSpellAction
{
    private boolean magicDamage;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        magicDamage = parameters.getBoolean("magic_damage", false);
    }

    @Override
    public SpellResult perform(CastContext context)
    {
        Entity entity = context.getTargetEntity();
        if (!(entity instanceof Damageable)) {
            return SpellResult.NO_TARGET;
        }

        Damageable targetEntity = (Damageable)entity;
        if (targetEntity.isDead()) {
            return SpellResult.NO_TARGET;
        }
        // Overkill to bypass protection
        context.registerModified(targetEntity);
        if (magicDamage) {
            CompatibilityLib.getCompatibilityUtils().magicDamage(targetEntity, CompatibilityLib.getCompatibilityUtils().getMaxHealth(targetEntity) * 100, context.getEntity());
        } else {
            targetEntity.damage(CompatibilityLib.getCompatibilityUtils().getMaxHealth(targetEntity) * 100);
        }
        return SpellResult.CAST;
    }

    @Override
    public boolean isUndoable()
    {
        return true;
    }

    @Override
    public boolean requiresTargetEntity()
    {
        return true;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("magic_damage");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("magic_damage")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_BOOLEANS));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
