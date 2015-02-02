package com.elmakers.mine.bukkit.spell;

import com.elmakers.mine.bukkit.api.action.SpellAction;
import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.effect.EffectPlayer;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.MageSpell;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Collection;

public abstract class BaseSpellAction implements SpellAction
{
    private Spell spell;
    private BaseSpell baseSpell;
    private BlockSpell blockSpell;
    private MageSpell mageSpell;
    private BrushSpell brushSpell;
    private TargetingSpell targetingSpell;
    private UndoableSpell undoSpell;
    private ActionSpell actionSpell;
    private ConfigurationSection parameters;

    public void registerForUndo(Runnable runnable)
    {
        if (undoSpell != null)
        {
            undoSpell.registerForUndo(runnable);
        }
    }

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

    public void registerForUndo(Block block)
    {
        if (undoSpell != null)
        {
            undoSpell.registerForUndo(block);
        }
    }

    public void updateBlock(Block block)
    {
        MageController controller = getController();
        if (controller != null)
        {
            controller.updateBlock(block);
        }
    }

    public void registerVelocity(Entity entity)
    {
        if (undoSpell != null)
        {
            undoSpell.registerVelocity(entity);
        }
    }

    public void registerMoved(Entity entity)
    {
        if (undoSpell != null)
        {
            undoSpell.registerMoved(entity);
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

    public Block getPreviousBlock()
    {
        return targetingSpell != null ? targetingSpell.getPreviousBlock() : null;
    }

    public Vector getDirection() {
        return baseSpell != null ? baseSpell.getDirection() : null;
    }

    public World getWorld() {
        return baseSpell != null ? baseSpell.getWorld() : null;
    }

    public Location getLocation() {
        return baseSpell != null ? baseSpell.getLocation() : null;
    }

    public Location getEyeLocation() {
        return baseSpell != null ? baseSpell.getEyeLocation() : null;
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
        if (spell instanceof TargetingSpell)
        {
            this.targetingSpell = (TargetingSpell)spell;
        }
        if (spell instanceof BlockSpell)
        {
            this.blockSpell = (BlockSpell)spell;
        }
        if (spell instanceof BrushSpell)
        {
            this.brushSpell = (BrushSpell)spell;
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
    public void prepare(ConfigurationSection parameters) {

    }

    @Override
    public void finish(ConfigurationSection parameters) {

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

    public Spell getSpell() {
        return spell;
    }

    @Override
    public void getParameterNames(Collection<String> parameters) {
        parameters.add("target_radius");
        parameters.add("target_count");
        parameters.add("target_self");
        parameters.add("target_all");
        parameters.add("target_all_worlds");
    }

    @Override
    public void getParameterOptions(Collection<String> examples, String parameterKey) {
        if (parameterKey.equals("target_radius") || parameterKey.equals("target_count")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_SIZES)));
        } else if (parameterKey.equals("target_self")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_BOOLEANS)));
        } else if (parameterKey.equals("target_all")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_BOOLEANS)));
        } else if (parameterKey.equals("target_all_worlds")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_BOOLEANS)));
        }
    }

    public String transformMessage(String message) {
        return message;
    }

    public String getMessage(String key) {
        return getMessage(key, key);
    }

    public String getMessage(String key, String def) {
        return baseSpell != null ? baseSpell.getMessage(key, def) : def;
    }

    public void addTargetEntity(Entity entity)
    {
        if (targetingSpell != null)
        {
            targetingSpell.addTargetEntity(entity);
        }
    }

    public Block findBlockUnder(Block block)
    {
        if (targetingSpell != null)
        {
            block = targetingSpell.findBlockUnder(block);
        }
        return block;
    }

    public boolean isTransparent(Material material)
    {
        if (targetingSpell != null)
        {
            return targetingSpell.isTransparent(material);
        }
        return material.isTransparent();
    }

    public MaterialBrush getBrush() {
        return brushSpell == null ? null : brushSpell.getBrush();
    }
}
