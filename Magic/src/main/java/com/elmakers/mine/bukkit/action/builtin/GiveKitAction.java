package com.elmakers.mine.bukkit.action.builtin;

import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.kit.Kit;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class GiveKitAction extends BaseSpellAction {
    private String kit;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        kit = parameters.getString("kit");
    }

    @Override
    public SpellResult perform(CastContext context) {
        if (kit == null || kit.isEmpty()) {
            return SpellResult.FAIL;
        }
        MageController controller = context.getController();
        Kit kit = controller.getKit(this.kit);
        if (kit == null) {
            return SpellResult.FAIL;
        }

        Entity targetEntity = context.getTargetEntity();
        if (targetEntity == null) {
            return SpellResult.NO_TARGET;
        }
        if (!(targetEntity instanceof Player)) {
            return SpellResult.PLAYER_REQUIRED;
        }

        Mage mage = controller.getMage(targetEntity);
        long cooldownRemaining = kit.getRemainingCooldown(mage);
        if (cooldownRemaining > 0) {
            String timeDescription = controller.getMessages().getTimeDescription(cooldownRemaining, "wait", "cooldown");
            String message = controller.getMessages().get("commands.mkit.cooldown");
            mage.sendMessage(message.replace("$time", timeDescription));
            return SpellResult.COOLDOWN;
        }
        if (!kit.isAllowed(mage)) {
            mage.sendMessage(controller.getMessages().get("commands.mkit.no_requirements"));
            return SpellResult.INSUFFICIENT_PERMISSION;
        }
        kit.give(mage);

        return SpellResult.CAST;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("kit");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("kit")) {
            examples.addAll(spell.getController().getKitKeys());
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
