package com.elmakers.mine.bukkit.spell;

import java.util.Collection;
import java.util.List;

import com.elmakers.mine.bukkit.utility.Target;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.TargetType;
import com.elmakers.mine.bukkit.block.UndoList;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;

public abstract class UndoableSpell extends TargetingSpell {
    private UndoList 		modifiedBlocks 			= null;
    private boolean 		undoEntityEffects		= true;
    private boolean 		bypassUndo				= false;
    private double 		    targetBreakables	    = 0;
    private int	 			autoUndo				= 0;

    @Override
    protected void processParameters(ConfigurationSection parameters)
    {
        super.processParameters(parameters);
        undoEntityEffects = parameters.getBoolean("entity_undo", true);
        bypassUndo = parameters.getBoolean("bypass_undo", false);
        bypassUndo = parameters.getBoolean("bu", bypassUndo);
        autoUndo = parameters.getInt("undo", 0);
        autoUndo = parameters.getInt("u", autoUndo);
        targetBreakables = parameters.getDouble("target_breakables", 0);
        bypassUndo = parameters.getBoolean("bypass_undo", false);
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
    protected void preCast()
    {
        super.preCast();
        modifiedBlocks = null;
    }

    public int getModifiedCount()
    {
        return modifiedBlocks == null ? 0 : modifiedBlocks.size();
    }

    public void registerForUndo()
    {
        // Must add empty lists here since they may get added to later!
        UndoList list = getUndoList();
        controller.update(list);
        mage.registerForUndo(list);
    }

    public void registerForUndo(Block block)
    {
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

    public void registerModified(Entity entity)
    {
        getUndoList().modify(entity);
    }

    public void registerPotionEffects(Entity entity)
    {
        getUndoList().addPotionEffects(entity);
    }

    public void registerMoved(Entity entity)
    {
        getUndoList().move(entity);
    }

    public void registerVelocity(Entity entity)
    {
        getUndoList().modifyVelocity(entity);
    }

    public void watch(Entity entity)
    {
        if (entity == null) return;
        getUndoList().watch(entity);
    }

    public UndoList getUndoList()
    {
        if (modifiedBlocks == null) {
            modifiedBlocks = new UndoList(mage, this, this.getName());
            modifiedBlocks.setEntityUndo(undoEntityEffects);
            modifiedBlocks.setBypass(bypassUndo);
            modifiedBlocks.setScheduleUndo(autoUndo);
        }
        return modifiedBlocks;
    }

    public boolean contains(Block block)
    {
        return modifiedBlocks.contains(block);
    }

    public int getScheduledUndo()
    {
        return autoUndo;
    }

    public boolean isUndoable()
    {
        return !bypassUndo;
    }

    protected void applyPotionEffects(Location location, int radius, Collection<PotionEffect> potionEffects) {
        if (potionEffects == null || radius <= 0 || potionEffects.size() == 0) return;

        int radiusSquared = radius * 2;
        List<Entity> entities = CompatibilityUtils.getNearbyEntities(location, radius, radius, radius);
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

    protected void breakBlock(Block block, int recursion) {
        if (!block.hasMetadata("breakable")) return;

        Location effectLocation = block.getLocation().add(0.5, 0.5, 0.5);
        effectLocation.getWorld().playEffect(effectLocation, Effect.STEP_SOUND, block.getType().getId());
        UndoList undoList = getUndoList();
        if (undoList != null) {
            undoList.add(block);
        }
        block.removeMetadata("breakable", mage.getController().getPlugin());
        block.removeMetadata("backfire", mage.getController().getPlugin());
        block.setType(Material.AIR);

        if (--recursion > 0) {
            breakBlock(block.getRelative(BlockFace.UP), recursion);
            breakBlock(block.getRelative(BlockFace.DOWN), recursion);
            breakBlock(block.getRelative(BlockFace.EAST), recursion);
            breakBlock(block.getRelative(BlockFace.WEST), recursion);
            breakBlock(block.getRelative(BlockFace.NORTH), recursion);
            breakBlock(block.getRelative(BlockFace.SOUTH), recursion);
        }
    }

    @Override
    protected Target getTarget()
    {
        Target target = super.getTarget();
        if (targetBreakables > 0 && target.isValid()) {
            // The Target has already been re-routed to the Player's location
            // if this has been reflected- but we want the original block
            // that was targeted.
            Block block = getCurBlock();
            if (block != null && block.hasMetadata("breakable")) {
                int breakable = (int)(targetBreakables > 1 ? targetBreakables :
                        (random.nextDouble() < targetBreakables ? 1 : 0));
                if (breakable > 0) {
                    List<MetadataValue> metadata = block.getMetadata("breakable");
                    for (MetadataValue value : metadata) {
                        if (value.getOwningPlugin().equals(controller.getPlugin())) {
                            breakBlock(block, value.asInt() + breakable - 1);
                            break;
                        }
                    }
                }
            }
        }
        return target;
    }
}
