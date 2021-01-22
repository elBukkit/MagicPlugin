package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageModifier;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class ModifierAction extends BaseSpellAction {
    private List<String> removeModifiers;
    private Map<String, ConfigurationSection> addModifiers;
    private int duration;

    @Override
    public void processParameters(CastContext context, ConfigurationSection parameters) {
        super.processParameters(context, parameters);

        removeModifiers = ConfigurationUtils.getStringList(parameters, "remove_modifiers");
        ConfigurationSection addSection = parameters.getConfigurationSection("add_modifiers");
        if (addSection == null) {
            List<String> addList = ConfigurationUtils.getStringList(parameters, "add_modifiers");
            if (addList != null) {
                addModifiers = new HashMap<>();
                for (String addKey : addList) {
                    addModifiers.put(addKey, null);
                }
            }
        } else {
            addModifiers = new HashMap<>();
            for (String addKey : addSection.getKeys(false)) {
                addModifiers.put(addKey, addSection.getConfigurationSection(addKey));
            }
        }

        duration = parameters.getInt("duration");
        if (parameters.contains("duration_multiplier")) {
            duration = (int)Math.ceil(parameters.getDouble("duration_multiplier") * duration);
        }
    }

    @Override
    public SpellResult perform(CastContext context) {
        Entity targetEntity = context.getTargetEntity();
        Mage targetMage = context.getController().getMage(targetEntity);
        boolean effected = false;
        if (removeModifiers != null) {
            for (String remove : removeModifiers) {
                MageModifier removed = targetMage.removeModifier(remove);
                if (removed != null) {
                    context.registerModifier(targetEntity, removed);
                    effected = true;
                }
            }
        }
        if (addModifiers != null && !addModifiers.isEmpty()) {
            effected = true;
            for (String key : addModifiers.keySet()) {
                int duration = this.duration;
                ConfigurationSection configuration = addModifiers.get(key);
                if (configuration != null) {
                    duration = configuration.getInt("duration", duration);
                }
                if (targetMage.addModifier(key, duration, configuration)) {
                    context.registerModifierForRemoval(targetEntity, key);
                    effected = true;
                }
            }
        }

        return effected ? SpellResult.CAST : SpellResult.NO_TARGET;
    }

    @Override
    public boolean isUndoable() {
        return true;
    }

    @Override
    public boolean requiresTargetEntity() {
        return true;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("duration");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("duration")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_DURATIONS));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
