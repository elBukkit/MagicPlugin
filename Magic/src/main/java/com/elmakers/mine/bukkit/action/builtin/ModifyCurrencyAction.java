package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;

public class ModifyCurrencyAction extends BaseSpellAction {
    private String currencyKey;
    private double value;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        value = parameters.getDouble("value", 0);
        currencyKey = parameters.getString("currency", "currency");
    }

    @Override
    public SpellResult perform(CastContext context) {
        Entity target = context.getTargetEntity();
        if (target == null) {
            return SpellResult.NO_TARGET;
        }
        MageController controller = context.getController();
        Mage mage = controller.getRegisteredMage(target);
        if (mage == null) {
            return SpellResult.NO_TARGET;
        }
        Player player = mage.getPlayer();
        if (player == null) {
            return SpellResult.PLAYER_REQUIRED;
        }
        mage.setCurrency(currencyKey, mage.getCurrency(currencyKey) + value);
        return SpellResult.CAST;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("value");
        parameters.add("currency");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("value")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_INTEGERS));
        } else if (parameterKey.equals("currency")) {
            examples.addAll(spell.getController().getCurrencyKeys());
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
