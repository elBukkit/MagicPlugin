package com.elmakers.mine.bukkit.action.builtin;

import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.configuration.SpellParameters;
import com.elmakers.mine.bukkit.utility.SpellUtils;

public class ModifyAttributeAction extends BaseSpellAction
{
    private String attribute;
    private String originalVariable;
    private String valueString;
    private boolean bypassUndo;
    private String modifyTarget;
    private SpellParameters spellParameters;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        modifyTarget = parameters.getString("modify_target", "player");
        bypassUndo = parameters.getBoolean("bypass_undo", false);
        attribute = parameters.getString("attribute");
        originalVariable = parameters.getString("original_variable", "x");
        valueString = parameters.getString("value");
        if (parameters instanceof SpellParameters) {
            spellParameters = (SpellParameters)parameters;
        }
    }

    @Override
    public SpellResult perform(CastContext context)
    {
        if (attribute == null) {
            return SpellResult.FAIL;
        }
        CasterProperties properties = context.getTargetCasterProperties(modifyTarget);
        if (properties == null) {
            return SpellResult.NO_TARGET;
        }
        Double value = null;
        Double original = properties.getAttribute(attribute);
        if (original != null && valueString != null && spellParameters != null) {
            Double transformedValue = SpellUtils.modifyProperty(original, valueString, originalVariable, spellParameters);
            if (transformedValue != null) {
                value = transformedValue;
            }
        }
        if (value == null) {
            try {
                value = Double.parseDouble(valueString);
            } catch (Exception ex) {
                context.getController().getLogger().warning("Unable to parse value in ModifyAttribute: " + valueString);
                return SpellResult.FAIL;
            }
        }
        if (original != null && original.equals(value)) {
            return SpellResult.NO_TARGET;
        }
        properties.setAttribute(attribute, value);
        if (!bypassUndo) {
            context.registerForUndo(new ModifyAttributeUndoAction(properties, attribute, original));
        }
        return SpellResult.CAST;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters)
    {
        super.getParameterNames(spell, parameters);
        parameters.add("attribute");
        parameters.add("value");
        parameters.add("modify_target");
        parameters.add("original_variable");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples)
    {
        if (parameterKey.equals("modify_target")) {
            examples.add("player");
            examples.add("wand");
            examples.addAll(spell.getController().getMageClassKeys());
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }

    @Override
    public boolean isUndoable()
    {
        return true;
    }

    private static class ModifyAttributeUndoAction implements Runnable
    {
        private final CasterProperties properties;
        private final String attribute;
        private final double value;

        public ModifyAttributeUndoAction(CasterProperties properties, String attribute, double value) {
            this.properties = properties;
            this.attribute = attribute;
            this.value = value;
        }

        @Override
        public void run() {
            properties.setAttribute(attribute, value);
        }
    }
}
