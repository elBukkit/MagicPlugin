package com.elmakers.mine.bukkit.action.builtin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.util.NumberConversions;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

import de.slikey.effectlib.math.EquationStore;
import de.slikey.effectlib.math.EquationTransform;

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
    }

    @Override
    public SpellResult perform(CastContext context)
    {
        if (modify == null) {
            return SpellResult.FAIL;
        }
        Entity entity = context.getTargetEntity();
        Mage mage = entity == null
                ? null
                : context.getController().getRegisteredMage(entity);
        if (mage == null) {
            return SpellResult.NO_TARGET;
        }
        CasterProperties properties = null;
        if (modifyTarget.equals("wand")) {
            properties = context.checkWand();
        } else if (modifyTarget.equals("player")) {
            properties = mage.getProperties();
        } else {
            properties = mage.getClass(modifyTarget);
        }

        // I am now wishing I hadn't made a base class called "mage" :(
        if (properties == null && modifyTarget.equals("mage")) {
            properties = mage.getProperties();
        }

        if (properties == null) {
            return SpellResult.NO_TARGET;
        }
        ConfigurationSection original = new MemoryConfiguration();
        ConfigurationSection changed = new MemoryConfiguration();
        for (ModifyProperty property : modify) {
            Object originalValue = properties.getProperty(property.path);
            Object newValue = property.value;
            if ((originalValue == null || originalValue instanceof Number) && property.value instanceof String) {
                EquationTransform transform = EquationStore.getInstance().getTransform((String)property.value);
                originalValue = originalValue == null ? null : NumberConversions.toDouble(originalValue);
                double defaultValue = property.defaultValue == null ? 0 : property.defaultValue;
                if (transform.isValid()) {
                    if (originalValue == null) {
                        originalValue = defaultValue;
                    }
                    transform.setVariable("x", (Double)originalValue);
                    double transformedValue = transform.get();
                    if (!Double.isNaN(transformedValue)) {
                        if (property.max != null) {
                            transformedValue = Math.min(transformedValue, property.max);
                        }
                        if (property.min != null) {
                            transformedValue = Math.max(transformedValue, property.min);
                        }
                        newValue = transformedValue;
                    }
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
