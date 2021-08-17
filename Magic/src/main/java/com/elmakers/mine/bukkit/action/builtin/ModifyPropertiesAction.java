package com.elmakers.mine.bukkit.action.builtin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.NumberConversions;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.configuration.SpellParameters;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.SpellUtils;

public class ModifyPropertiesAction extends BaseSpellAction
{
    private static class ModifyProperty {
        public String path;
        public Object value;
        public Double min;
        public Double max;
        public Double defaultValue;

        public ModifyProperty(String path, Object value) {
            this.path = path;
            this.value = value;
        }

        public ModifyProperty(ConfigurationSection configuration) {
            path = configuration.getString("property", configuration.getString("path"));
            value = configuration.get("value");
            if (configuration.contains("min"))
                min = configuration.getDouble("min");
            if (configuration.contains("max"))
                max = configuration.getDouble("max");
            if (configuration.contains("default"))
                defaultValue = configuration.getDouble("default");
        }
    }

    private List<ModifyProperty> modify;
    private String modifyTarget;
    private String originalVariable;
    private SpellParameters extraParameters;
    private boolean upgrade;
    private boolean bypassUndo;

    private static class ModifyPropertyUndoAction implements Runnable
    {
        private final CasterProperties properties;
        private final ConfigurationSection original;

        public ModifyPropertyUndoAction(ConfigurationSection original, CasterProperties properties) {
            this.original = original;
            this.properties = properties;
        }

        @Override
        public void run() {
            properties.configure(original);
        }
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        modifyTarget = parameters.getString("modify_target", "wand");
        upgrade = parameters.getBoolean("upgrade", false);
        bypassUndo = parameters.getBoolean("bypass_undo", false);
        originalVariable = parameters.getString("original_variable", "x");

        modify = new ArrayList<>();
        Object modifyObject = parameters.get("modify");
        if (modifyObject instanceof ConfigurationSection) {
            ConfigurationSection simple = (ConfigurationSection)modifyObject;
            Set<String> keys = simple.getKeys(true);
            for (String key : keys) {
                ModifyProperty property = new ModifyProperty(key, simple.get(key));
                modify.add(property);
            }
        } else {
            Collection<ConfigurationSection> complex = ConfigurationUtils.getNodeList(parameters, "modify");
            for (ConfigurationSection section : complex) {
                ModifyProperty property = new ModifyProperty(section);
                modify.add(property);
            }
        }
        if (parameters instanceof SpellParameters) {
            extraParameters = (SpellParameters)parameters;
        }
    }

    @Override
    public SpellResult perform(CastContext context)
    {
        if (modify == null || extraParameters == null) {
            return SpellResult.FAIL;
        }
        CasterProperties properties = context.getTargetCasterProperties(modifyTarget);
        if (properties == null) {
            return SpellResult.NO_TARGET;
        }
        ConfigurationSection original = ConfigurationUtils.newConfigurationSection();
        ConfigurationSection changed = ConfigurationUtils.newConfigurationSection();
        for (ModifyProperty property : modify) {
            Object originalValue = properties.getProperty(property.path);
            Object newValue = property.value;
            if ((originalValue == null || originalValue instanceof Number) && property.value instanceof String) {
                // Allow using attributes and variables here
                double defaultValue = property.defaultValue == null ? 0 : property.defaultValue;
                double originalDouble = originalValue == null ? defaultValue : NumberConversions.toDouble(originalValue);

                Double transformedValue = SpellUtils.modifyProperty(originalDouble, (String)property.value, originalVariable, extraParameters);
                if (transformedValue != null) {
                    if (property.max != null) {
                        if (originalDouble >= property.max && transformedValue >= property.max) continue;
                        transformedValue = Math.min(transformedValue, property.max);
                    }
                    if (property.min != null) {
                        if (originalDouble <= property.min && transformedValue <= property.min) continue;
                        transformedValue = Math.max(transformedValue, property.min);
                    }
                    newValue = transformedValue;
                }
            }

            changed.set(property.path, newValue);
            original.set(property.path, originalValue);
        }

        if (changed.getKeys(false).isEmpty()) return SpellResult.NO_TARGET;
        if (upgrade)
            properties.upgrade(changed);
        else
            properties.configure(changed);
        if (!bypassUndo) {
            context.registerForUndo(new ModifyPropertyUndoAction(original, properties));
        }
        return SpellResult.CAST;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters)
    {
        super.getParameterNames(spell, parameters);
        parameters.add("modify");
        parameters.add("modify_target");
        parameters.add("original_variable");
        parameters.add("upgrade");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples)
    {
        if (parameterKey.equals("upgrade")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_BOOLEANS));
        } else  if (parameterKey.equals("modify_target")) {
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
}
