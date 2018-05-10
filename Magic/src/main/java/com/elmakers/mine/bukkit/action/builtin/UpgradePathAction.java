package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.api.wand.WandUpgradePath;
import com.elmakers.mine.bukkit.spell.BaseSpell;

public class UpgradePathAction extends BaseSpellAction {
    private int upgradeLevels;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        upgradeLevels = parameters.getInt("upgrade_levels", 0);
    }

    @Override
    public SpellResult perform(CastContext context) {
        Mage mage = context.getMage();
        Wand wand = mage.getActiveWand();

        // For now... eventually I need to separate out the wand part, but not doing it now.
        if (wand == null) {
            return SpellResult.NO_TARGET;
        }

        if (upgradeLevels > 0) {
            if (wand.enchant(upgradeLevels, mage, false) > 0) {
                return SpellResult.CAST;
            }
        } else {
            com.elmakers.mine.bukkit.api.wand.WandUpgradePath path = wand.getPath();
            WandUpgradePath nextPath = path != null ? path.getUpgrade() : null;
            if (nextPath != null && path.checkUpgradeRequirements(wand, null) && !path.canEnchant(wand)) {
                path.upgrade(wand, mage);
                return SpellResult.CAST;
            }
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
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_SIZES)));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
