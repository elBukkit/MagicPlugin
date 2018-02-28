package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.magic.SingleParameterConfiguration;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Entity;

import java.util.Arrays;
import java.util.Collection;

public class ModifyPropertiesAction extends BaseSpellAction
{
    private SingleParameterConfiguration modify;
    private String modifyTarget;
    private boolean upgrade;

	private class ModifyPropertyUndoAction implements Runnable
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
        ConfigurationSection modifyProperties = parameters.getConfigurationSection("modify");
        if (modifyProperties != null) {
            modify = new SingleParameterConfiguration();
            modify.wrap(modifyProperties);
        }
        upgrade = parameters.getBoolean("upgrade", false);
    }

	@Override
	public SpellResult perform(CastContext context)
	{
        if (modify == null) {
            return SpellResult.FAIL;
        }
        Entity entity = context.getTargetEntity();
        Mage mage = context.getController().getRegisteredMage(entity);
        if (mage == null) {
            return SpellResult.NO_TARGET;
        }
        CasterProperties properties = null;
        if (modifyTarget.equals("wand")) {
            properties = mage.getActiveWand();
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
        for (String key : modify.getKeys(false)) {
            // TODO: instead of SingleParameterConfiguration here,
            // just fetch equations as needed.
            // Add min/max/default options to modify block
            Object originalValue = properties.getProperty(key);
            original.set(key, originalValue);
            if (originalValue instanceof Double) {
                modify.setValue((Double)originalValue);
                changed.set(key, modify.getDouble(key));
            } else if (originalValue instanceof Integer) {
                modify.setValue((Integer)originalValue);
                changed.set(key, modify.getInt(key));
            } else {
                changed.set(key, modify.get(key));
            }
        }
        if (upgrade)
            properties.upgrade(changed);
        else
            properties.configure(changed);
        context.registerForUndo(new ModifyPropertyUndoAction(original, properties));
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
            examples.add("mage");
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
