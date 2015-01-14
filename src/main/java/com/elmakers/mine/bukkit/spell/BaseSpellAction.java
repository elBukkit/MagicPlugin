package com.elmakers.mine.bukkit.spell;

import com.elmakers.mine.bukkit.api.action.SpellAction;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.MageSpell;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Entity;

public abstract class BaseSpellAction implements SpellAction
{
    protected Spell spell;
    protected MageSpell mageSpell;
    protected UndoableSpell undoSpell;
    protected ConfigurationSection parameters;

    public void registerModified(Entity entity)
    {
        if (undoSpell != null)
        {
            undoSpell.registerModified(entity);
        }
    }

    public void registerForUndo(Entity entity)
    {
        if (undoSpell != null)
        {
            undoSpell.registerForUndo(entity);
        }
    }

    public void registerPotionEffects(Entity entity)
    {
        if (undoSpell != null)
        {
            undoSpell.registerPotionEffects(entity);
        }
    }

    public Mage getMage()
    {
        return mageSpell == null ? null : mageSpell.getMage();
    }

    public MageController getController() {
        Mage mage = getMage();
        return mage == null ? null : mage.getController();
    }

    @Override
    public void load(Spell spell, ConfigurationSection parameters)
    {
        this.spell = spell;
        if (spell instanceof MageSpell)
        {
            this.mageSpell = (MageSpell)spell;
        }
        if (spell instanceof UndoableSpell)
        {
            this.undoSpell = (UndoableSpell)spell;
        }
        this.parameters = parameters;
    }

    @Override
    public ConfigurationSection getParameters(ConfigurationSection baseParameters)
    {
        if (parameters == null)
        {
            return baseParameters;
        }
        ConfigurationSection combined = new MemoryConfiguration();
        ConfigurationUtils.addConfigurations(combined, baseParameters);
        ConfigurationUtils.addConfigurations(combined, parameters);
        return combined;
    }

    @Override
    public boolean usesBrush()
    {
        return false;
    }

    @Override
    public boolean isUndoable()
    {
        return false;
    }
}
