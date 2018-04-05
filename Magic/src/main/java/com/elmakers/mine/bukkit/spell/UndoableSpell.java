package com.elmakers.mine.bukkit.spell;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.TargetType;
import com.elmakers.mine.bukkit.block.UndoList;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class UndoableSpell extends TargetingSpell {
    private UndoList         modifiedBlocks             = null;
    private boolean         undoEntityEffects        = false;
    private Set<EntityType> undoEntityTypes         = null;
    private boolean         bypassUndo                = false;
    private int                 autoUndo                = 0;
    private double          undoSpeed               = 0;

    @Override
    public void processParameters(ConfigurationSection parameters)
    {
        super.processParameters(parameters);
        undoEntityEffects = parameters.getBoolean("entity_undo", false);
        bypassUndo = parameters.getBoolean("bypass_undo", false);
        bypassUndo = parameters.getBoolean("bu", bypassUndo);
        autoUndo = parameters.getInt("undo", 0);
        autoUndo = parameters.getInt("u", autoUndo);
        bypassUndo = parameters.getBoolean("bypass_undo", false);
        undoSpeed = parameters.getDouble("undo_speed", 0);
        if (parameters.contains("entity_undo_types"))
        {
            undoEntityTypes = new HashSet<>();
            Collection<String> typeStrings = ConfigurationUtils.getStringList(parameters, "entity_undo_types");
            for (String typeString : typeStrings)
            {
                try {
                    undoEntityTypes.add(EntityType.valueOf(typeString.toUpperCase()));
                } catch (Exception ex) {
                    controller.getLogger().warning("Unknown entity type: " + typeString);
                }
            }
        }
        else
        {
            undoEntityTypes = controller.getUndoEntityTypes();
        }

        configureUndoList();
    }

    @Override
    protected void loadTemplate(ConfigurationSection node)
    {
        super.loadTemplate(node);

        // Also load this here so it is available from templates, prior to casting
        ConfigurationSection parameters = node.getConfigurationSection("parameters");
        if (parameters != null)
        {
            bypassUndo = parameters.getBoolean("bypass_undo", false);
            bypassUndo = parameters.getBoolean("bu", bypassUndo);
            autoUndo = parameters.getInt("undo", 0);
            autoUndo = parameters.getInt("u", autoUndo);
        }
    }

    @Override
    protected void reset()
    {
        super.reset();
        if (!isActive())
        {
            modifiedBlocks = null;
        }
    }

    public int getModifiedCount()
    {
        return modifiedBlocks == null ? 0 : modifiedBlocks.size();
    }

    public void prepareForUndo()
    {
        UndoList list = getUndoList();
        mage.prepareForUndo(list);
    }

    public void registerForUndo() {
        // Must add empty lists here since they may get added to later!
        UndoList list = getUndoList();
        if (!list.isScheduled())
        {
            controller.update(list);
        }
        mage.registerForUndo(list);
    }

    public void registerForUndo(Block block) {
        getUndoList().add(block);
    }

    public void registerForUndo(Runnable runnable)
    {
        getUndoList().add(runnable);
    }

    public void registerForUndo(Entity entity)
    {
        getUndoList().add(entity);
    }

    public void registerModified(Entity entity) {
        getUndoList().modify(entity);
    }

    public void registerPotionEffects(Entity entity)
    {
        getUndoList().addPotionEffects(entity);
    }

    public void registerMoved(Entity entity) {
        getUndoList().move(entity);
    }

    public void registerVelocity(Entity entity)
    {
        getUndoList().modifyVelocity(entity);
    }

    public UndoList getUndoList()
    {
        if (modifiedBlocks == null) {
            modifiedBlocks = new UndoList(mage, this.getName());
            modifiedBlocks.setSpell(this);
            configureUndoList();
        }
        return modifiedBlocks;
    }

    protected void configureUndoList() {
        if (modifiedBlocks != null) {
            modifiedBlocks.setEntityUndo(undoEntityEffects);
            modifiedBlocks.setEntityUndoTypes(undoEntityTypes);
            modifiedBlocks.setBypass(bypassUndo);
            modifiedBlocks.setScheduleUndo(autoUndo);
            modifiedBlocks.setUndoSpeed(undoSpeed);
        }
    }

    public boolean contains(Block block)
    {
        return modifiedBlocks.contains(block);
    }

    public int getScheduledUndo()
    {
        return autoUndo;
    }

    @Override
    public boolean isUndoable()
    {
        return !bypassUndo;
    }

    public void applyPotionEffects(Location location, int radius, Collection<PotionEffect> potionEffects) {
        if (potionEffects == null || radius <= 0 || potionEffects.size() == 0) return;

        int radiusSquared = radius * 2;
        Collection<Entity> entities = CompatibilityUtils.getNearbyEntities(location, radius, radius, radius);
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity) {
                Mage targetMage = null;
                if (controller.isMage(entity)) {
                    targetMage = controller.getMage(entity);
                }

                boolean isSourcePlayer = entity == mage.getEntity();
                if (isSourcePlayer && getTargetType() != TargetType.ANY && getTargetType() != TargetType.SELF) {
                    continue;
                }

                // Check for protected players
                if (targetMage != null && isSuperProtected(targetMage) && !isSourcePlayer) {
                    continue;
                }

                if (!canTarget(entity)) continue;

                if (entity.getLocation().distanceSquared(location) < radiusSquared) {
                    registerPotionEffects(entity);
                    CompatibilityUtils.applyPotionEffects((LivingEntity)entity, potionEffects);

                    if (targetMage != null) {
                        String playerMessage = getMessage("cast_player_message");
                        if (playerMessage.length() > 0) {
                            playerMessage = playerMessage.replace("$spell", getName());
                            targetMage.sendMessage(playerMessage);
                        }
                    }
                }
            }
        }
    }

    @Override
    public long getDuration()
    {
        if (!bypassUndo && autoUndo != 0)
        {
            return autoUndo;
        }
        return super.getDuration();
    }
}
