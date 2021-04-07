package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.ProgressionPath;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.spell.BaseSpell;

public class UpgradePathAction extends CompoundAction {
    private int upgradeLevels;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        upgradeLevels = parameters.getInt("upgrade_levels", 0);
    }

    @Override
    public SpellResult step(CastContext context) {
        Mage mage = context.getMage();
        Wand wand = mage.getActiveWand();
        CasterProperties caster = mage.getActiveProperties();
        if (upgradeLevels > 0) {
            if (caster.randomize(upgradeLevels, false) > 0) {
                return SpellResult.CAST;
            }
        }
        ProgressionPath path = caster.getPath();
        ProgressionPath nextPath = path != null ? path.getNextPath() : null;
        if (nextPath != null && path.checkUpgradeRequirements(caster, true) && !path.canProgress(caster)) {
            path.upgrade(mage, wand);
            return startActions();
        }

        return SpellResult.NO_TARGET;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);

        parameters.add("upgrade_levels");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("upgrade_levels")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_SIZES));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
