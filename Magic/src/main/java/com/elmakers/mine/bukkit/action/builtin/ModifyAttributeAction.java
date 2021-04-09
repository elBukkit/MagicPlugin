package com.elmakers.mine.bukkit.action.builtin;

import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class ModifyAttributeAction extends BaseSpellAction
{
    private String attribute;
    private double value;
    private boolean bypassUndo;
    private String modifyTarget;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        modifyTarget = parameters.getString("modify_target", "player");
        bypassUndo = parameters.getBoolean("bypass_undo", false);
        attribute = parameters.getString("attribute");
        value = parameters.getDouble("value");
    }

    @Override
    public SpellResult perform(CastContext context)
    {
        if (attribute == null) {
            return SpellResult.FAIL;
        }
        Entity entity = context.getTargetEntity();
        Mage mage = entity == null
                ? null
                : context.getController().getRegisteredMage(entity);
        if (mage == null) {
            return SpellResult.NO_TARGET;
        }
        CasterProperties properties = context.getCasterProperties(modifyTarget);
        if (properties == null) {
            return SpellResult.NO_TARGET;
        }
        Double original = properties.getAttribute(attribute);
        if (original != null && original == value) {
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
