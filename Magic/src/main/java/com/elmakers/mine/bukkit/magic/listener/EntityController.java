package com.elmakers.mine.bukkit.magic.listener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.projectiles.ProjectileSource;

import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.entity.EntityData;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.block.BlockData;
import com.elmakers.mine.bukkit.block.magic.MagicBlock;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.magic.MagicMetaKeys;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.Targeting;
import com.elmakers.mine.bukkit.utility.TextUtils;
import com.elmakers.mine.bukkit.wand.Wand;

public class EntityController implements Listener {
    private final MagicController controller;
    private double meleeDamageReduction = 0;
    private boolean preventMeleeDamage = false;
    private boolean keepWandsOnDeath = true;
    private boolean    disableItemSpawn = false;
    private boolean    forceSpawn = false;
    private boolean    preventWandMeleeDamage = true;
    private int ageDroppedItems    = 0;
    private Map<EntityType, Double> entityDamageReduction;

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

        if (CompatibilityLib.getEntityMetadataUtils().getBoolean(itemOne, MagicMetaKeys.TEMPORARY)
            || CompatibilityLib.getEntityMetadataUtils().getBoolean(itemTwo, MagicMetaKeys.TEMPORARY)) {
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
        Targeting.checkTracking(projectile, null, CompatibilityLib.getCompatibilityUtils().getHitBlock(event));
    }

    @EventHandler
    public void onEntityCombust(EntityCombustEvent event)
    {
        Entity entity = event.getEntity();
        if (controller.isMagicNPC(entity)) {
            event.setCancelled(true);
            return;
        }
        com.elmakers.mine.bukkit.magic.Mage mage = controller.getRegisteredMage(entity);
        if (mage != null) {
            mage.onCombust(event);
        }
        if (checkSuperProtection(entity)) {
            event.setCancelled(true);
            return;
        }
        com.elmakers.mine.bukkit.api.entity.EntityData entityData = controller.getMob(entity);
        if (entityData != null && !entityData.isCombustible()) {
            event.setCancelled(true);
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

    private boolean checkSuperProtection(Entity entity) {

        EntityData mob = controller.getMob(entity);
        if (mob != null && mob.isSuperProtected()) {
            if (entity.getFireTicks() > 0) {
                entity.setFireTicks(0);
            }
            return true;
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        Entity damager = event.getDamager();
        if (entity instanceof Projectile || entity instanceof TNTPrimed) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
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

        // Make sure to resolve the source after getting the undo list, since the undo
        // list is attached to the projectile.
        damager = controller.getDamageSource(damager);
        Mage mage = controller.getRegisteredMage(damager);
        if (mage != null && mage instanceof com.elmakers.mine.bukkit.magic.Mage) {
            ((com.elmakers.mine.bukkit.magic.Mage)mage).onDamageDealt(event);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityPreDamageByEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Projectile || entity instanceof TNTPrimed) return;

        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            Entity damager = event.getDamager();
            EntityData entityData = controller.getMob(damager);
            if (entityData != null && entityData.isPreventMelee()) {
                event.setCancelled(true);
                return;
            }
        }

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
        if (checkSuperProtection(entity)) {
            event.setCancelled(true);
            return;
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
            boolean isMelee = event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK && !CompatibilityLib.getCompatibilityUtils().isDamaging();

            if (isMelee && meleeDamageReduction > 0) {
                if (meleeDamageReduction >= 1) {
                    event.setCancelled(true);
                    return;
                }
                event.setDamage(event.getDamage() * (1 - meleeDamageReduction));
            }
            if (preventWandMeleeDamage)
            {
                boolean hasWand = activeWand != null;
                Player player = (Player) damager;
                ItemStack itemInHand = player.getInventory().getItemInMainHand();
                boolean isMeleeWeapon = controller.isMeleeWeapon(itemInHand);
                if (isMelee && hasWand && !isMeleeWeapon) {
                    event.setCancelled(true);
                } else if (!hasWand && preventMeleeDamage && isMelee && !isMeleeWeapon) {
                    event.setCancelled(true);
                }
            }
            if (!event.isCancelled()) {
                if (isMelee && damagerMage != null) {
                    activeWand = damagerMage.getActiveWand();
                    if (activeWand != null) {
                        activeWand.playEffects("hit_entity");
                        activeWand.damageDealt(event.getDamage(), entity);
                    }
                }
            }
        } else {
            Targeting.checkTracking(damager, entity, null);
        }
    }

    /**
     * This death handler fires right away to close the wand inventory before other plugin
     * see the drops.
     */
    public void handlePlayerDeath(Player player, com.elmakers.mine.bukkit.magic.Mage mage, List<ItemStack> drops, boolean isKeepInventory) {
        Wand wand = mage.getActiveWand();

        // First, deactivate the active wand.
        // If it had a spell inventory open, restore the survival inventory
        // If keepInventory is not set, add the survival inventory to drops
        if (wand != null) {
            // Retrieve stored inventory before deactivating the wand
            if (mage.hasStoredInventory()) {
                controller.info("** Wand inventory was open, clearing drops: " + drops.size(), 15);

                // Remove the wand inventory from drops
                drops.clear();

                // Deactivate the wand.
                wand.deactivate();

                // Add restored inventory back to drops
                if (!isKeepInventory) {
                    ItemStack[] stored = player.getInventory().getContents();
                    for (ItemStack stack : stored) {
                        if (stack != null) {
                            // Since armor is not stored in the wand inventory it will be removed from drops
                            // and added back in, hopefully that causes no issues
                            drops.add(stack);
                        }
                    }
                }
                controller.info("** Restored inventory added to drops: " + drops.size(), 15);
            } else {
                wand.deactivate();
            }
        }

        if (isKeepInventory) {
            controller.info("** Keep inventory is set,", 15);
            return;
        }
        // The Equip action and other temporary item-giving spells will have given items to the respawn inventory
        // on death. Let's take those items out and add them to drops
        int dropSize = drops.size();
        mage.addRespawnInventories(drops);
        mage.restoreRespawnInventories();
        dropSize = drops.size() - dropSize;
        controller.info("** Dropping " + dropSize + " items that were given on death, drops now: " + drops.size(), 15);

        // Now check for undroppable items.
        // Remove them from the inventory and drops list, and store them to give back on respawn
        // It should be OK if some plugin wants to come in after this and turn keep inventory back on,
        // it'll keep the inventory without any of the "keep" items (since we removed them), and hopefully
        // Things will merge back together properly in the end.
        PlayerInventory inventory = player.getInventory();
        ItemStack[] contents = inventory.getContents();
        for (int index = 0; index < contents.length; index++) {
            ItemStack itemStack = contents[index];
            if (itemStack == null || itemStack.getType() == Material.AIR) continue;

            // Remove temporary items from inventory and drops
            if (CompatibilityLib.getItemUtils().isTemporary(itemStack)) {
                ItemStack replacement = CompatibilityLib.getItemUtils().getReplacement(itemStack);
                if (!CompatibilityLib.getItemUtils().isEmpty(replacement)) {
                    drops.add(replacement);
                }
                drops.remove(itemStack);
                controller.info("** Removing temporary item from drops: " + TextUtils.nameItem(itemStack) + " (replaced with " + TextUtils.nameItem(itemStack) + ") drops now: " + drops.size(), 15);
                contents[index] = null;
                continue;
            }

            // Save "keep" items to return on respawn
            boolean keepItem = CompatibilityLib.getNBTUtils().getMetaBoolean(itemStack, "keep", false);
            if (!keepItem && keepWandsOnDeath && Wand.isWand(itemStack)) keepItem = true;
            if (keepItem) {
                mage.addToRespawnInventory(index, itemStack);
                contents[index] = null;
                drops.remove(itemStack);
                controller.info("** Removing keep item from drops: " + TextUtils.nameItem(itemStack) + "&r, drops now: " + drops.size(), 15);
            } else if (Wand.isSkill(itemStack)) {
                drops.remove(itemStack);
                contents[index] = null;
                controller.info("** Removing skill item from drops: " + TextUtils.nameItem(itemStack) + "&r, drops now: " + drops.size(), 15);
            }
        }
        inventory.setContents(contents);
        controller.info("** Done processing death with drops remaining: " + drops.size(), 15);
    }

    /**
     * This death handler is for mobs and players alike
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDeath(EntityDeathEvent event)
    {
        Entity entity = event.getEntity();
        boolean isPlayer = entity instanceof Player;
        if (isPlayer) {
            EntityDamageEvent.DamageCause cause = entity.getLastDamageCause() == null ? null : entity.getLastDamageCause().getCause();
            controller.info("* Processing death of " + entity.getName()
                + " from " + cause
                + " with drops: " + event.getDrops().size(), 15);
        }
        Long spawnerId = CompatibilityLib.getEntityMetadataUtils().getLong(entity, MagicMetaKeys.AUTOMATION);
        if (spawnerId != null) {
            MagicBlock magicBlock = controller.getActiveAutomaton(spawnerId);
            if (magicBlock != null) {
                magicBlock.onSpawnDeath();
            }
        }
        // Just don't ever clear player death drops, for real
        if (!isPlayer) {
            if (CompatibilityLib.getEntityMetadataUtils().getBoolean(entity, MagicMetaKeys.NO_DROPS)) {
                event.setDroppedExp(0);
                event.getDrops().clear();
            } else {
                UndoList pendingUndo = controller.getEntityUndo(entity);
                if (pendingUndo != null && pendingUndo.isUndoType(entity.getType())) {
                    event.getDrops().clear();
                }
            }
        } else {
            // Clean up metadata that shouldn't be on players
            CompatibilityLib.getEntityMetadataUtils().remove(entity, MagicMetaKeys.NO_DROPS);
        }
        EntityDamageEvent damageEvent = event.getEntity().getLastDamageCause();
        if (damageEvent instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent dbe = (EntityDamageByEntityEvent)damageEvent;
            Entity damager = dbe.getDamager();
            damager = controller.getDamageSource(damager);
            if (damager != null) {
                Mage damagerMage = controller.getRegisteredMage(damager);
                if (damagerMage != null) {
                    damagerMage.trigger("kill");
                }
            }
        }

        com.elmakers.mine.bukkit.magic.Mage mage = controller.getRegisteredMage(entity);
        if (mage == null) return;

        mage.deactivateAllSpells();
        mage.onDeath(event);
        if (isPlayer) {
            controller.info("* Mage class handled death, drops now: " + event.getDrops().size(), 15);
        }
        if (event instanceof PlayerDeathEvent) {
            PlayerDeathEvent playerDeath = (PlayerDeathEvent)event;
            handlePlayerDeath(playerDeath.getEntity(), mage, playerDeath.getDrops(), playerDeath.getKeepInventory());
        }
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
            controller.info("*** Trying to spawn item but skipping due to disableItemSpawn?" + disableItemSpawn
                    + " undoing?" + com.elmakers.mine.bukkit.block.BlockData.undoing
                    + ": " + TextUtils.nameItem(event.getEntity().getItemStack()), 18);
            event.setCancelled(true);
            return;
        }

        Item itemEntity = event.getEntity();
        ItemStack spawnedItem = itemEntity.getItemStack();
        if (CompatibilityLib.getItemUtils().isTemporary(spawnedItem)) {
            controller.info("*** Trying to drop a temporary item: " + TextUtils.nameItem(event.getEntity().getItemStack()), 18);
            event.setCancelled(true);
            return;
        }

        /*
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
        */
        if (Wand.isSkill(spawnedItem))
        {
            controller.info("*** Trying to drop a skill item: " + TextUtils.nameItem(event.getEntity().getItemStack()), 18);
            event.setCancelled(true);
            return;
        }
        if (Wand.isWand(spawnedItem))
        {
            boolean invulnerable = controller.getWandProperty(spawnedItem, "invulnerable", false);
            if (invulnerable) {
                CompatibilityLib.getCompatibilityUtils().setInvulnerable(event.getEntity());
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
                CompatibilityLib.getCompatibilityUtils().ageItem(item, ticks);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDamage(EntityDamageEvent event)
    {
        Entity entity = event.getEntity();
        if (entity == null) return;

        if (controller.isStaticMagicNPC(event.getEntity())) {
            event.setCancelled(true);
            return;
        }

        com.elmakers.mine.bukkit.magic.Mage mage = controller.getRegisteredMage(entity);
        if (mage != null)
        {
            mage.onDamage(event);
        }
        else
        {
            Entity passenger = CompatibilityLib.getDeprecatedUtils().getPassenger(entity);
            com.elmakers.mine.bukkit.magic.Mage mountMage = passenger == null ? null : controller.getRegisteredMage(passenger);
            if (mountMage != null) {
                mountMage.onDamage(event);
            }
        }
        if (checkSuperProtection(entity)) {
            event.setCancelled(true);
            return;
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
                } else if (event.getDamage() >= CompatibilityLib.getDeprecatedUtils().getItemDamage(itemStack)) {
                    String wandId = Wand.getWandId(itemStack);
                    controller.removeLostWand(wandId);
                }
            }
        }
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        Entity shooter = event.getEntity();
        com.elmakers.mine.bukkit.magic.Mage mage = controller.getRegisteredMage(shooter);
        if (mage == null || mage.isLaunchingProjectile()) return;
        mage.setLastBowPull(Math.max(0.0, Math.min(1.0, event.getForce())));
        mage.setLastBowUsed(event.getBow());
    }

    public void checkArrowLaunch(com.elmakers.mine.bukkit.magic.Mage mage, Projectile projectile, ProjectileLaunchEvent event) {
        if (!mage.isPlayer()) return;
        if (!CompatibilityLib.getCompatibilityUtils().isArrow(projectile)) return;
        Integer slot = mage.getArrowToLaunch();
        if (slot == null) return;
        ItemStack itemStack = mage.getItemInSlot(slot);
        mage.useArrow(itemStack, slot, event);
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.isCancelled()) return;

        Projectile projectile = event.getEntity();
        ProjectileSource shooter = projectile.getShooter();
        if (!(shooter instanceof Entity)) {
            return;
        }
        com.elmakers.mine.bukkit.magic.Mage mage = controller.getRegisteredMage((Entity)shooter);
        if (mage != null) {
            if (mage.isLaunchingProjectile()) return;
            mage.setLastProjectileType(projectile.getType());
            if (mage.trigger("launch")) {
                if (mage.isCancelLaunch()) {
                    event.setCancelled(true);
                }
            }
        }
        EntityData entityData = controller.getMob((Entity)shooter);
        if (entityData != null && entityData.isPreventProjectiles()) {
            event.setCancelled(true);
            return;
        }

        if (mage == null) {
            return;
        }
        Wand wand = mage.getActiveWand();
        if (wand == null) {
            checkArrowLaunch(mage, projectile, event);
            return;
        }

        Material wandIcon = wand.getIcon().getMaterial();
        if (wandIcon != Material.BOW && !wandIcon.name().equals("CROSSBOW")) return;
        double minPull = wand.getDouble("cast_min_bowpull");

        double pull = mage.getLastBowPull();
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

        try {
            wand.cast();
        } catch (Exception ex) {
            controller.getLogger().log(Level.SEVERE, "Error casting bow spell", ex);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeathNormal(PlayerDeathEvent event) {
        Entity entity = event.getEntity();
        EntityDamageEvent.DamageCause cause = entity.getLastDamageCause() == null ? null : entity.getLastDamageCause().getCause();
        controller.info("* NORMAL death of " + entity.getName()
                + " from " + cause
                + " with drops: " + event.getDrops().size()
                + " undoing? " + BlockData.undoing
                + " disable drops? " + disableItemSpawn
                + " keep inv? " + event.getKeepInventory(), 30);

        controller.info("** Registered entity death listeners: ", 40);
        HandlerList deathEventHandlers = EntityDeathEvent.getHandlerList();
        for (RegisteredListener listener : deathEventHandlers.getRegisteredListeners()) {
            controller.info("*** " + listener.getPlugin().getName() + " at " + listener.getPriority(), 40);
        }
        controller.info("** Registered player death listeners: ", 40);
        deathEventHandlers = PlayerDeathEvent.getHandlerList();
        for (RegisteredListener listener : deathEventHandlers.getRegisteredListeners()) {
            controller.info("*** " + listener.getPlugin().getName() + " at " + listener.getPriority(), 40);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeathHigh(PlayerDeathEvent event) {
        Entity entity = event.getEntity();
        EntityDamageEvent.DamageCause cause = entity.getLastDamageCause() == null ? null : entity.getLastDamageCause().getCause();
        controller.info("* HIGH death of " + entity.getName()
                + " from " + cause
                + " with drops: " + event.getDrops().size()
                + " undoing? " + BlockData.undoing
                + " disable drops? " + disableItemSpawn
                + " keep inv? " + event.getKeepInventory(), 30);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeathHighest(PlayerDeathEvent event) {
        Entity entity = event.getEntity();
        EntityDamageEvent.DamageCause cause = entity.getLastDamageCause() == null ? null : entity.getLastDamageCause().getCause();
        controller.info("* HIGHEST death of " + entity.getName()
                + " from " + cause
                + " with drops: " + event.getDrops().size()
                + " undoing? " + BlockData.undoing
                + " disable drops? " + disableItemSpawn
                + " keep inv? " + event.getKeepInventory(), 30);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeathMonitor(PlayerDeathEvent event) {
        Entity entity = event.getEntity();
        EntityDamageEvent.DamageCause cause = entity.getLastDamageCause() == null ? null : entity.getLastDamageCause().getCause();
        controller.info("* MONITOR death of " + entity.getName()
            + " from " + cause
            + " with drops: " + event.getDrops().size()
            + " undoing? " + BlockData.undoing
            + " disable drops? " + disableItemSpawn
            + " keep inv? " + event.getKeepInventory(), 30);
    }
}
