package com.elmakers.mine.bukkit.magic.listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.projectiles.ProjectileSource;

import com.elmakers.mine.bukkit.api.block.BlockData;
import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.entity.EntityData;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import com.elmakers.mine.bukkit.utility.NMSUtils;
import com.elmakers.mine.bukkit.utility.Targeting;
import com.elmakers.mine.bukkit.wand.Wand;

public class EntityController implements Listener {
    private static final double MAX_ARROW_SPEED = 3;
    private final MagicController controller;
    private double meleeDamageReduction = 0;
    private boolean preventMeleeDamage = false;
    private boolean keepWandsOnDeath = true;
    private boolean    disableItemSpawn = false;
    private boolean    forceSpawn = false;
    private boolean    preventWandMeleeDamage = true;
    private int ageDroppedItems    = 0;
    private Map<EntityType, Double> entityDamageReduction;
    private boolean launching = false;

    public void loadProperties(ConfigurationSection properties) {
        preventMeleeDamage = properties.getBoolean("prevent_melee_damage", false);
        meleeDamageReduction = properties.getDouble("melee_damage_reduction", 0);
        keepWandsOnDeath = properties.getBoolean("keep_wands_on_death", true);
        preventWandMeleeDamage = properties.getBoolean("prevent_wand_melee_damage", true);
        ageDroppedItems = properties.getInt("age_dropped_items", 0);
        ConfigurationSection entityReduction = properties.getConfigurationSection("entity_damage_reduction");
        if (entityReduction != null) {
            Set<String> keys = entityReduction.getKeys(false);
            entityDamageReduction = new HashMap<>();
            for (String key : keys) {
                try {
                    EntityType entityType = EntityType.valueOf(key.toUpperCase());
                    entityDamageReduction.put(entityType, entityReduction.getDouble(key));
                } catch (Exception ex) {
                    controller.getLogger().warning("Invalid entity type found in entity_damage_reduction: " + key);
                }
            }
        } else {
            entityDamageReduction = null;
        }
    }

    public EntityController(MagicController controller) {
        this.controller = controller;
    }

    public void setDisableItemSpawn(boolean disable) {
        disableItemSpawn = disable;
    }

    public void setForceSpawn(boolean forceSpawn) {
        this.forceSpawn = forceSpawn;
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGHEST)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (forceSpawn) {
            event.setCancelled(false);
        }
    }

    @EventHandler
    public void onItemMerge(ItemMergeEvent event) {
        Item itemOne = event.getEntity();
        Item itemTwo = event.getTarget();

        if (itemOne.hasMetadata("temporary") || itemTwo.hasMetadata("temporary")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        final Projectile projectile = event.getEntity();

        // This happens before EntityDamageEvent, so the hit target will
        // be assigned before the tracked projectile is checked.
        // This is here to register misses, mainly.

        // TODO: use event.getBlock in newer API.
        Targeting.checkTracking(controller.getPlugin(), projectile, null, CompatibilityUtils.getHitBlock(event));
    }

    @EventHandler
    public void onEntityCombust(EntityCombustEvent event)
    {
        Entity entity = event.getEntity();
        com.elmakers.mine.bukkit.magic.Mage mage = controller.getRegisteredMage(entity);
        if (mage != null) {
            mage.onCombust(event);
        }

        if (!event.isCancelled())
        {
            UndoList undoList = controller.getPendingUndo(entity.getLocation());
            if (undoList != null)
            {
                undoList.modify(entity);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof ItemFrame) || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;

        Entity damager = event.getDamager();

        UndoList undoList = controller.getEntityUndo(damager);
        if (undoList != null) {
            // Prevent dropping items from frames,
            if (undoList.isScheduled()) {
                undoList.damage(entity);
                if (!entity.isValid()) {
                    event.setCancelled(true);
                }
            } else {
                undoList.modify(entity);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityPreDamageByEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Projectile || entity instanceof TNTPrimed) return;
        Mage entityMage = controller.getRegisteredMage(entity);
        if (entityMage != null) {
            entityMage.damagedBy(event.getDamager(), event.getDamage());
            if (entity instanceof Player) {
                Player damaged = (Player)entity;
                if (damaged.isBlocking()) {
                    com.elmakers.mine.bukkit.api.wand.Wand damagedWand = entityMage.getActiveWand();
                    if (damagedWand != null) {
                        damagedWand.playEffects("hit_blocked");
                    }
                }
            }
            if (entityMage.isSuperProtected()) {
                event.setCancelled(true);
                return;
            }
        }
        Entity damager = event.getDamager();
        if (entityDamageReduction != null) {
            Double reduction = entityDamageReduction.get(damager.getType());
            if (reduction != null) {
                if (reduction >= 1) {
                    event.setCancelled(true);
                    return;
                }
                event.setDamage(event.getDamage() * (1 - reduction));
            }
        }
        if (damager instanceof Player) {
            Mage damagerMage = controller.getRegisteredMage(damager);
            com.elmakers.mine.bukkit.api.wand.Wand activeWand = null;
            boolean isMelee = event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK && !CompatibilityUtils.isDamaging;

            if (isMelee && meleeDamageReduction > 0) {
                if (meleeDamageReduction >= 1) {
                    event.setCancelled(true);
                    return;
                }
                event.setDamage(event.getDamage() * (1 - meleeDamageReduction));
            }

            if (isMelee && damagerMage != null) {
                activeWand = damagerMage.getActiveWand();
                if (activeWand != null) {
                    activeWand.playEffects("hit_entity");
                    activeWand.damageDealt(event.getDamage(), entity);
                }
            }
            if (preventWandMeleeDamage)
            {
                boolean hasWand = activeWand != null;
                Player player = (Player) damager;
                ItemStack itemInHand = player.getInventory().getItemInMainHand();
                boolean isMeleeWeapon = controller.isMeleeWeapon(itemInHand);
                if (isMelee && hasWand && !isMeleeWeapon) {
                    event.setCancelled(true);
                    CompatibilityUtils.isDamaging = true;
                    activeWand.performAction(activeWand.getLeftClickAction());
                    CompatibilityUtils.isDamaging = false;
                }
                else if (!hasWand && preventMeleeDamage && isMelee && !isMeleeWeapon) {
                    event.setCancelled(true);
                }
            }
        } else {
            Targeting.checkTracking(controller.getPlugin(), damager, entity, null);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDeath(EntityDeathEvent event)
    {
        Entity entity = event.getEntity();
        if (entity.hasMetadata("nodrops")) {
            event.setDroppedExp(0);
            event.getDrops().clear();
            entity.removeMetadata("nodrops", controller.getPlugin());
        }
        com.elmakers.mine.bukkit.magic.Mage mage = controller.getRegisteredMage(entity);
        if (mage == null) return;

        mage.deactivateAllSpells();
        mage.onDeath(event);

        if (!(entity instanceof Player)) {
            return;
        }
        final Player player = (Player)entity;

        String rule = entity.getWorld().getGameRuleValue("keepInventory");
        if (rule.equals("true")) return;

        List<ItemStack> drops = event.getDrops();
        Wand wand = mage.getActiveWand();
        if (wand != null) {
            // Retrieve stored inventory before deactivating the wand
            if (mage.hasStoredInventory()) {
                // Remove the wand inventory from drops
                drops.removeAll(Arrays.asList(player.getInventory().getContents()));

                // Deactivate the wand.
                wand.deactivate();

                // Add restored inventory back to drops
                ItemStack[] stored = player.getInventory().getContents();
                for (ItemStack stack : stored) {
                    if (stack != null) {
                        drops.add(stack);
                    }
                }
            } else {
                wand.deactivate();
            }
        }

        List<ItemStack> removeDrops = new ArrayList<>();
        PlayerInventory inventory = player.getInventory();
        ItemStack[] contents = inventory.getContents();
        for (int index = 0; index < contents.length; index++)
        {
            ItemStack itemStack = contents[index];
            if (itemStack == null || itemStack.getType() == Material.AIR) continue;
            if (NMSUtils.isTemporary(itemStack)) {
                removeDrops.add(itemStack);
                continue;
            }
            boolean keepItem = InventoryUtils.getMetaBoolean(itemStack, "keep", false);
            if (!keepItem && keepWandsOnDeath && Wand.isWand(itemStack)) keepItem = true;
            if (keepItem)
            {
                mage.addToRespawnInventory(index, itemStack);
                removeDrops.add(itemStack);
            } else if (Wand.isSkill(itemStack)) {
                removeDrops.add(itemStack);
            }
        }
        ItemStack[] armor = player.getInventory().getArmorContents();
        for (int index = 0; index < armor.length; index++)
        {
            ItemStack itemStack = armor[index];
            if (itemStack == null || itemStack.getType() == Material.AIR) continue;
            if (NMSUtils.isTemporary(itemStack) || Wand.isSkill(itemStack)) {
                removeDrops.add(itemStack);
                continue;
            }
            boolean keepItem = InventoryUtils.getMetaBoolean(itemStack, "keep", false);
            if (!keepItem && keepWandsOnDeath && Wand.isWand(itemStack)) keepItem = true;
            if (keepItem)
            {
                mage.addToRespawnArmor(index, itemStack);
                removeDrops.add(itemStack);
            }
        }

        drops.removeAll(removeDrops);
    }

    @EventHandler
    public void onItemDespawn(ItemDespawnEvent event)
    {
        Item entity = event.getEntity();
        ItemStack itemStack = entity.getItemStack();
        if (Wand.isWand(itemStack))
        {
            boolean immortal = controller.getWandProperty(itemStack, "immortal", false);
            if (immortal) {
                event.getEntity().setTicksLived(1);
                event.setCancelled(true);
            } else {
                controller.removeLostWand(Wand.getWandId(itemStack));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemSpawn(ItemSpawnEvent event)
    {
        if (disableItemSpawn || com.elmakers.mine.bukkit.block.BlockData.undoing)
        {
            event.setCancelled(true);
            return;
        }

        Item itemEntity = event.getEntity();
        ItemStack spawnedItem = itemEntity.getItemStack();
        Block block = itemEntity.getLocation().getBlock();
        BlockData undoData = com.elmakers.mine.bukkit.block.UndoList.getBlockData(block.getLocation());
        boolean isBreaking = block.getType() != Material.AIR;
        if (!isBreaking) {
            MaterialSet doubleAttachables = controller.getMaterialSetManager().getMaterialSetEmpty("attachable_double");
            isBreaking = doubleAttachables.testItem(spawnedItem);
        }
        if (undoData != null && isBreaking)
        {
            // if a block just broke via physics, it will not yet have its id changed to air
            // So we can catch this as a one-time event, for blocks we have recorded.
            if (undoData.getMaterial() != Material.AIR)
            {
                UndoList undoList = undoData.getUndoList();
                if (undoList != null) {
                    undoList.add(block);
                } else {
                    controller.getLogger().warning("Block broken into item under undo at " + block + ", but no undo list was assigned");
                }
                event.setCancelled(true);
                return;
            }

            // If this was a block we built magically, don't drop items if the item being dropped
            // matches the block type. This is messy, but avoid players losing all their items
            // when suffocating inside a Blob
            Collection<ItemStack> drops = block.getDrops();
            if (drops != null) {
                for (ItemStack drop : drops) {
                    if (drop.getType() == spawnedItem.getType()) {
                        com.elmakers.mine.bukkit.block.UndoList.commit(undoData);
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
        if (Wand.isSkill(spawnedItem))
        {
            event.setCancelled(true);
            return;
        }
        if (Wand.isWand(spawnedItem))
        {
            boolean invulnerable = controller.getWandProperty(spawnedItem, "invulnerable", false);
            if (invulnerable) {
                CompatibilityUtils.setInvulnerable(event.getEntity());
            }
            boolean trackWand = controller.getWandProperty(spawnedItem, "track", false);
            if (trackWand) {
                Wand wand = controller.getWand(spawnedItem);
                controller.addLostWand(wand, event.getEntity().getLocation());
            }
        } else  {
            // Don't do this, no way to differentiate between a dropped item from a broken block
            // versus a dead player
            // registerEntityForUndo(event.getEntity());
            if (ageDroppedItems > 0) {
                int ticks = ageDroppedItems * 20 / 1000;
                Item item = event.getEntity();
                CompatibilityUtils.ageItem(item, ticks);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event)
    {
        Entity entity = event.getEntity();
        if (entity == null) return;

        com.elmakers.mine.bukkit.magic.Mage mage = controller.getRegisteredMage(entity);
        if (mage != null)
        {
            mage.onDamage(event);
        }
        else
        {
            Entity passenger = DeprecatedUtils.getPassenger(entity);
            com.elmakers.mine.bukkit.magic.Mage mountMage = passenger == null ? null : controller.getRegisteredMage(passenger);
            if (mountMage != null) {
                mountMage.onDamage(event);
            }
        }
        if (entity instanceof Item)
        {
            Item item = (Item)entity;
            ItemStack itemStack = item.getItemStack();
            if (Wand.isWand(itemStack))
            {
                boolean invulnerable = controller.getWandProperty(itemStack, "invulnerable", false);
                if (invulnerable) {
                    event.setCancelled(true);
                } else if (event.getDamage() >= itemStack.getDurability()) {
                    String wandId = Wand.getWandId(itemStack);
                    controller.removeLostWand(wandId);
                }
            }
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetLivingEntityEvent event)
    {
        Entity target = event.getTarget();
        if (target == null) return;
        if (!(target instanceof Player)) return;
        Mage mage = controller.getRegisteredMage(target);
        if (mage == null) return;
        if (mage.isSuperProtected())
        {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (launching || event.isCancelled()) return;

        Projectile projectile = event.getEntity();
        ProjectileSource shooter = projectile.getShooter();
        if (!(shooter instanceof Entity)) {
            return;
        }
        com.elmakers.mine.bukkit.magic.Mage mage = controller.getRegisteredMage((Entity)shooter);
        if (mage == null) return;

        double pull = Math.min(1.0, projectile.getVelocity().length() / MAX_ARROW_SPEED);
        if (!(shooter instanceof Player)) {
            EntityData mobData = mage.getEntityData();
            if (mobData != null) {
                launching = true;
                try {
                    if (mobData.onLaunch(mage, pull)) {
                        event.setCancelled(true);
                    }
                } catch (Exception ex) {
                    controller.getLogger().log(Level.SEVERE, "Error casting bow spell from mob " + mobData.getName(), ex);
                }
                launching = false;
            }
            return;
        }

        Wand wand = mage.getActiveWand();
        if (wand == null) return;

        if (wand.getIcon().getMaterial() != Material.BOW) return;
        double minPull = wand.getDouble("cast_min_bowpull");

        if (minPull > 0 && pull < minPull) {
            if (wand.isInventoryOpen()) event.setCancelled(true);
            return;
        }

        Spell spell = wand.getActiveSpell();
        if (spell == null) {
            if (wand.isInventoryOpen()) event.setCancelled(true);
            return;
        }

        event.setCancelled(true);
        String[] parameters = {"bowpull", Double.toString(pull)};

        // prevent recursion!
        launching = true;
        try {
            wand.cast(parameters);
        } catch (Exception ex) {
            controller.getLogger().log(Level.SEVERE, "Error casting bow spell", ex);
        }
        launching = false;
    }
}
