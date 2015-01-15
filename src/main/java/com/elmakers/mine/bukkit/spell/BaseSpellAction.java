package com.elmakers.mine.bukkit.spell;

import com.elmakers.mine.bukkit.api.action.SpellAction;
import com.elmakers.mine.bukkit.api.effect.EffectPlayer;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.MageSpell;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import java.util.Collection;

public abstract class BaseSpellAction implements SpellAction
{
    private Spell spell;
    private BaseSpell baseSpell;
    private BlockSpell blockSpell;
    private MageSpell mageSpell;
    private UndoableSpell undoSpell;
    private ActionSpell actionSpell;
    private ConfigurationSection parameters;

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

    public ActionHandler getActions(String key) {
        if (actionSpell != null)
        {
            return actionSpell.getActions(key);
        }
        return null;
    }

    public Vector getDirection() {
        return baseSpell != null ? baseSpell.getDirection() : null;
    }

    public Location getLocation() {
        return baseSpell != null ? baseSpell.getLocation() : null;
    }

    public Location getEyeLocation() {
        return baseSpell != null ? baseSpell.getLocation() : null;
    }

    public boolean isIndestructible(Block block) {
        return blockSpell != null ? blockSpell.isIndestructible(block) : true;
    }

    public boolean hasBuildPermission(Block block) {
        return blockSpell != null ? blockSpell.hasBuildPermission(block) : false;
    }

    public Collection<EffectPlayer> getEffects(String key) {
        return spell.getEffects(key);
    }

    public Collection<PotionEffect> getPotionEffects(ConfigurationSection parameters, Integer duration) {
        return baseSpell != null ? baseSpell.getPotionEffects(parameters, duration) : null;
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
        if (spell instanceof BaseSpell)
        {
            this.baseSpell = (BaseSpell)spell;
        }
        if (spell instanceof MageSpell)
        {
            this.mageSpell = (MageSpell)spell;
        }
        if (spell instanceof UndoableSpell)
        {
            this.undoSpell = (UndoableSpell)spell;
        }
        if (spell instanceof ActionSpell)
        {
            this.actionSpell = (ActionSpell)spell;
        }
        if (spell instanceof BlockSpell)
        {
            this.blockSpell = (BlockSpell)spell;
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
