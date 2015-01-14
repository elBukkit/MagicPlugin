package com.elmakers.mine.bukkit.spell;

import com.elmakers.mine.bukkit.api.action.SpellAction;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Entity;

public abstract class BaseSpellAction implements SpellAction
{
    protected ActionSpell spell;
    protected ConfigurationSection parameters;

    public void registerModified(Entity entity)
    {
        spell.registerModified(entity);
    }

    public void registerPotionEffects(Entity entity)
    {
        spell.registerPotionEffects(entity);
    }

    public Mage getMage() {
        return spell.getMage();
    }

    public MageController getController() {
        return spell.getMage().getController();
    }

    @Override
    public void load(Spell spell, ConfigurationSection parameters)
    {
        if (spell instanceof ActionSpell)
        {
            this.spell = (ActionSpell)spell;
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
}
