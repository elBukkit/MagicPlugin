package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyright Tyler Grissom 2018
 */
public class ModifyAttributeAction extends BaseSpellAction {

    private Map<String, Double> attributes;
    private Mage targetMage;

    private class ModifyAttributeUndoAction implements Runnable {

        @Override
        public void run() {
            undoModifyAttributes();
        }
    }

    private void undoModifyAttributes() {
        for (Map.Entry<String, Double> entry : attributes.entrySet()) {
            double value = targetMage.getAttribute(entry.getKey()) - entry.getValue();

            targetMage.getProperties().setAttribute(entry.getKey(), value);
        }
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);

        ConfigurationSection section = parameters.getConfigurationSection("attributes");

        if (section == null) return;

        this.attributes = new HashMap<>();

        for (String key : section.getKeys(false)) {
            attributes.put(key, section.getDouble(key, 0));
        }
    }

    @Override
    public SpellResult perform(CastContext context) {
        if (context.getTargetEntity() == null) {
            return SpellResult.NO_TARGET;
        }

        this.targetMage = context.getController().getMage(context.getTargetEntity());

        for (Map.Entry<String, Double> entry : attributes.entrySet()) {
            double value = targetMage.getProperties().getAttribute(entry.getKey()) + entry.getValue();

            targetMage.getProperties().setAttribute(entry.getKey(), value);
        }

        context.registerForUndo(new ModifyAttributeUndoAction());

        return SpellResult.CAST;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);

        parameters.add("attributes");
    }

    @Override
    public boolean isUndoable() {
        return true;
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }

    @Override
    public boolean requiresTargetEntity() {
        return true;
    }
}
