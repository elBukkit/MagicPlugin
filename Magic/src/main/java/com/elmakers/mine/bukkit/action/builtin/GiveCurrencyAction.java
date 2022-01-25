package com.elmakers.mine.bukkit.action.builtin;

import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.magic.MagicPlugin;

public class GiveCurrencyAction extends BaseSpellAction {
    private String currencyKey;
    private int currencyAmount;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        currencyKey = parameters.getString("currency");
        currencyAmount = parameters.getInt("amount");
    }

    @Override
    public SpellResult perform(CastContext context) {
        Entity targetEntity = context.getTargetEntity();
        if (targetEntity == null) {
            return SpellResult.NO_TARGET;
        }
        if (!(targetEntity instanceof Player)) {
            return SpellResult.PLAYER_REQUIRED;
        }
        MageController controller = context.getController();
        Mage mage = controller.getMage((Player)targetEntity);

        mage.addCurrency(currencyKey, currencyAmount);
        return SpellResult.CAST;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("currency");
        parameters.add("amount");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("currency")) {
            MagicAPI api = MagicPlugin.getAPI();
            Collection<String> currencies = api.getController().getCurrencyKeys();
            for (String currencyKey : currencies) {
                examples.add(currencyKey);
            }
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
