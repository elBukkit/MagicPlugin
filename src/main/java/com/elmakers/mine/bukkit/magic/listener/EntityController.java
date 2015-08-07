package com.elmakers.mine.bukkit.magic.listener;

import com.elmakers.mine.bukkit.action.ActionHandler;
import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.NMSUtils;
import com.elmakers.mine.bukkit.wand.Wand;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EntityController implements Listener {
    private final MagicController controller;
    private boolean preventMeleeDamage = false;
    private boolean keepWandsOnDeath = true;
    private boolean	disableItemSpawn = false;
    private boolean	preventWandMeleeDamage = true;
    private int ageDroppedItems	= 0;

    public EntityController(MagicController controller) {
        this.controller = controller;
    }

    public void setPreventMeleeDamage(boolean prevent) {
        preventMeleeDamage = prevent;
    }

    public void setKeepWandsOnDeath(boolean keep) {
        keepWandsOnDeath = keep;
    }

    public void setPreventWandMeleeDamage(boolean prevent) {
        preventWandMeleeDamage = prevent;
    }

    public void setDisableItemSpawn(boolean disable) {
        disableItemSpawn = disable;
    }

    public void setAgeDroppedItems(int age) {
        ageDroppedItems = age;
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        final Projectile projectile = event.getEntity();
        // This is delayed so that the EntityDamage version takes precedence
        if (ActionHandler.hasActions(projectile) || ActionHandler.hasEffects(projectile))
        {
            Bukkit.getScheduler().scheduleSyncDelayedTask(controller.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    ActionHandler.runActions(projectile, projectile.getLocation(), null);
                    ActionHandler.runEffects(projectile);
                }
            }, 1L);
        }
    }

    @EventHandler
    public void onEntityCombust(EntityCombustEvent event)
    {
        Entity entity = event.getEntity();
        Mage apiMage = controller.getRegisteredMage(entity);
        if (apiMage != null) {
            if (apiMage instanceof com.elmakers.mine.bukkit.magic.Mage) {
                com.elmakers.mine.bukkit.magic.Mage mage = (com.elmakers.mine.bukkit.magic.Mage) apiMage;
                mage.onPlayerCombust(event);
            }
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

        if (entity instanceof Projectile || entity instanceof TNTPrimed) return;

        Entity damager = event.getDamager();
        UndoList undoList = controller.getEntityUndo(damager);
        if (undoList != null) {
            // Prevent dropping items from frames,
            if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK || undoList.isScheduled()) {
                undoList.damage(entity, event.getDamage());
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
        if (damager instanceof Player ) {
            Mage damagerMage = controller.getRegisteredMage(damager);
            com.elmakers.mine.bukkit.api.wand.Wand activeWand = null;
            if (damagerMage != null) {
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
                ItemStack itemInHand = player.getItemInHand();
                boolean isMeleeWeapon = controller.isMeleeWeapon(itemInHand);
                boolean isMelee = event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK && !CompatibilityUtils.isDamaging;
                if (isMelee && hasWand && !isMeleeWeapon) {
                    event.setCancelled(true);
                    CompatibilityUtils.isDamaging = true;
                    activeWand.cast();
                    CompatibilityUtils.isDamaging = false;
                }
                else if (!hasWand && preventMeleeDamage && isMelee && !isMeleeWeapon) {
                    event.setCancelled(true);
                }
            }
        } else {
            ActionHandler.targetEffects(damager, entity);
            ActionHandler.runActions(damager, entity.getLocation(), entity);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDeath(EntityDeathEvent event)
    {
        Entity entity = event.getEntity();
        if (entity.hasMetadata("nodrops")) {
            event.setDroppedExp(0);
            event.getDrops().clear();
        }

        Mage apiMage = controller.getRegisteredMage(entity);
        if (apiMage == null) return;

        if (!(apiMage instanceof com.elmakers.mine.bukkit.magic.Mage)) return;
        com.elmakers.mine.bukkit.magic.Mage mage = (com.elmakers.mine.bukkit.magic.Mage)apiMage;

        mage.onPlayerDeath(event);
        mage.deactivateAllSpells();

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

        List<ItemStack> removeDrops = new ArrayList<ItemStack>();
        PlayerInventory inventory = player.getInventory();
        ItemStack[] contents = inventory.getContents();
        for (int index = 0; index < contents.length; index++)
        {
            ItemStack itemStack = contents[index];
            if (itemStack == null || itemStack.getType() == Material.AIR) continue;
            if (NMSUtils.isTemporary(itemStack) || Wand.isSkill(itemStack)) {
                removeDrops.add(itemStack);
                continue;
            }
            boolean keepItem = false;
            if (Wand.isWand(itemStack)) {
                keepItem = keepWandsOnDeath;
                if (!keepItem) {
                    Wand testWand = new Wand(controller, itemStack);
                    keepItem = testWand.keepOnDeath();
                }
            }
            if (keepItem)
            {
                mage.addToRespawnInventory(index, itemStack);
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
            boolean keepItem = false;
            if (Wand.isWand(itemStack)) {
                keepItem = keepWandsOnDeath;
                if (!keepItem) {
                    Wand testWand = new Wand(controller, itemStack);
                    keepItem = testWand.keepOnDeath();
                }
            }
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
        ActionHandler.runEffects(entity);
        ActionHandler.runActions(entity, entity.getLocation(), null);
        if (Wand.isWand(event.getEntity().getItemStack()))
        {
            Wand wand = new Wand(controller, entity.getItemStack());
            if (wand.isIndestructible()) {
                event.getEntity().setTicksLived(1);
                event.setCancelled(true);
            } else {
                controller.removeLostWand(wand.getLostId());
            }
        }
    }

    @EventHandler(priority=EventPriority.LOWEST)
    public void onItemSpawn(ItemSpawnEvent event)
    {
        if (disableItemSpawn)
        {
            event.setCancelled(true);
            return;
        }
        ItemStack spawnedItem = event.getEntity().getItemStack();
        if (Wand.isSkill(spawnedItem))
        {
            event.setCancelled(true);
            return;
        }
        if (Wand.isWand(spawnedItem))
        {
            Wand wand = new Wand(controller, event.getEntity().getItemStack());
            if (wand.isIndestructible()) {
                CompatibilityUtils.setInvulnerable(event.getEntity());

                // Don't show non-indestructible wands on dynmap
                controller.addLostWand(wand, event.getEntity().getLocation());
                Location dropLocation = event.getLocation();
                controller.info("Wand " + wand.getName() + ", id " + wand.getLostId() + " spawned at " + dropLocation.getBlockX() + " " + dropLocation.getBlockY() + " " + dropLocation.getBlockZ());
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
        try {
            Entity entity = event.getEntity();

            Mage apiMage = controller.getRegisteredMage(event.getEntity());
            if (apiMage != null)
            {
                if (!(apiMage instanceof com.elmakers.mine.bukkit.magic.Mage)) return;
                com.elmakers.mine.bukkit.magic.Mage mage = (com.elmakers.mine.bukkit.magic.Mage) apiMage;

                mage.onPlayerDamage(event);
            }
            else
            {
                Entity passenger = entity.getPassenger();
                Mage apiMountMage = controller.getRegisteredMage(passenger);
                if (apiMountMage != null) {
                    if (!(apiMountMage instanceof com.elmakers.mine.bukkit.magic.Mage)) return;
                    com.elmakers.mine.bukkit.magic.Mage mage = (com.elmakers.mine.bukkit.magic.Mage)apiMountMage;
                    mage.onPlayerDamage(event);
                }
            }
            if (entity instanceof Item)
            {
                Item item = (Item)entity;
                ItemStack itemStack = item.getItemStack();
                if (Wand.isWand(itemStack))
                {
                    Wand wand = new Wand(controller, item.getItemStack());
                    if (wand.isIndestructible()) {
                        event.setCancelled(true);
                    } else if (event.getDamage() >= itemStack.getDurability()) {
                        if (controller.removeLostWand(wand.getLostId())) {
                            controller.info("Wand " + wand.getName() + ", id " + wand.getLostId() + " destroyed");
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
