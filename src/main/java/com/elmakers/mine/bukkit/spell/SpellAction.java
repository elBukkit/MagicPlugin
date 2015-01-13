package com.elmakers.mine.bukkit.spell;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

public abstract class SpellAction
{
    protected ActionSpell spell;
    protected MageController controller;
    protected Mage mage;

    public void registerModified(Entity entity)
    {
        spell.registerModified(entity);
    }

    public void registerPotionEffects(Entity entity)
    {
        spell.registerPotionEffects(entity);
    }

    public void load(ActionSpell spell, ConfigurationSection configuration)
    {
        this.spell = spell;
        this.controller = spell.getController();
        this.mage = spell.getMage();
        this.loadTemplate(configuration);
    }

    public SpellResult perform(ConfigurationSection parameters)
    {
        return SpellResult.CAST;
    }

    public SpellResult perform(ConfigurationSection parameters, Entity targetEntity)
    {
        return SpellResult.CAST;
    }

    public SpellResult perform(ConfigurationSection parameters, Block targetBlock)
    {
        return SpellResult.CAST;
    }

    public void loadTemplate(ConfigurationSection configuration)
    {
    }
}
