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
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.spell.BaseSpell;

public class WandInstructionsAction extends BaseSpellAction {
    private boolean messageTarget = true;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        messageTarget = parameters.getBoolean("message_target", false);
    }

    @Override
    public SpellResult perform(CastContext context) {
        MageController controller = context.getController();
        Mage mage = null;
        if (messageTarget) {
            Entity targetEntity = context.getTargetEntity();
            if (targetEntity == null || !(targetEntity instanceof Player)) {
                return SpellResult.NO_TARGET;
            }
            mage = controller.getRegisteredMage(targetEntity);
        } else {
            mage = context.getMage();
        }
        Wand wand = mage == null ? null : mage.getActiveWand();
        if (wand == null) {
            return SpellResult.NO_TARGET;
        }
        wand.showInstructions();
        return SpellResult.CAST;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("message_target");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("message_target")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_BOOLEANS));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
