package com.elmakers.mine.bukkit.action.builtin;

import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.MageClass;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.magic.MagicPlugin;

public class RemoveSpellAction extends BaseSpellAction
{
    private String spellKey;

    @Override
    public void processParameters(CastContext context, ConfigurationSection parameters) {
        super.processParameters(context, parameters);
        spellKey = parameters.getString("spell");
    }

    @Override
    public SpellResult perform(CastContext context) {
        Wand wand = context.getWand();
        MageClass mageClass = context.getMageClass();
        if (wand != null && wand.hasSpell(spellKey) && wand.removeSpell(spellKey)) {
            return SpellResult.CAST;
        }
        if (mageClass != null && mageClass.hasSpell(spellKey) && mageClass.removeSpell(spellKey)) {
            return SpellResult.CAST;
        }
        return SpellResult.NO_TARGET;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters)
    {
        super.getParameterNames(spell, parameters);
        parameters.add("spell");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples)
    {
        if (parameterKey.equals("spell")) {
            Collection<SpellTemplate> spellList = MagicPlugin.getAPI().getSpellTemplates();
            for (SpellTemplate spellTemplate : spellList) {
                examples.add(spellTemplate.getKey());
            }
        }
    }
}
