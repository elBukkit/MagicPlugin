package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;

public class ModifySPAction extends BaseSpellAction
{
    private int sp;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        sp = parameters.getInt("sp", 1);
    }

    @Override
    public SpellResult perform(CastContext context) {
        Mage mage = context.getMage();
        MageController controller = context.getController();
        int currentSP = mage.getSkillPoints();
        if (sp < 0 && currentSP <= 0) {
            return SpellResult.NO_TARGET;
        }
        int spMax = controller.getSPMaximum();
        if (sp > 0 && currentSP >= spMax) {
            return SpellResult.NO_TARGET;
        }
        mage.addSkillPoints(sp);
        return SpellResult.CAST;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters)
    {
        super.getParameterNames(spell, parameters);
        parameters.add("sp");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples)
    {
        if (parameterKey.equals("sp")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_INTEGERS));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
