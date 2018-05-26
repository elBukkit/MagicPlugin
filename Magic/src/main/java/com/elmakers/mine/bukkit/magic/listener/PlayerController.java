package com.elmakers.mine.bukkit.magic.listener;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.economy.Currency;
import com.elmakers.mine.bukkit.api.entity.EntityData;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.wand.WandAction;
import com.elmakers.mine.bukkit.block.DefaultMaterials;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.magic.DropActionTask;
import com.elmakers.mine.bukkit.magic.Mage;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import com.elmakers.mine.bukkit.utility.NMSUtils;
import com.elmakers.mine.bukkit.wand.Wand;

public class PlayerController implements Listener {
    private final MagicController controller;
    private int clickCooldown = 150;
    private MaterialAndData enchantBlockMaterial;
    private String enchantClickSpell = "spellshop";
    private String enchantSneakClickSpell = "upgrades";
    private boolean cancelInteractOnLeftClick = true;
    private boolean cancelInteractOnRightClick = false;
    private boolean allowOffhandCasting = true;
    private long lastDropWarn = 0;

    public PlayerController(MagicController controller) {
        this.controller = controller;
    }

    public void loadProperties(ConfigurationSection properties) {
        clickCooldown = properties.getInt("click_cooldown", 0);
        enchantBlockMaterial = new MaterialAndData(properties.getString("enchant_block", "enchantment_table"));
        enchantClickSpell = properties.getString("enchant_click");
        enchantSneakClickSpell = properties.getString("enchant_sneak_click");
        cancelInteractOnLeftClick = properties.getBoolean("cancel_interact_on_left_click", true);
        cancelInteractOnRightClick = properties.getBoolean("cancel_interact_on_right_click", false);
        allowOffhandCasting = properties.getBoolean("allow_offhand_casting", true);
    }

    @EventHandler
    public void onPlayerExpChange(PlayerExpChangeEvent event)
    {
        Player player = event.getPlayer();
        Mage mage = controller.getRegisteredMage(player);
        if (mage != null) {
            mage.experienceChanged();
        }
    }

    @EventHandler
    public void onPlayerToggleGlide(EntityToggleGlideEvent event)
    {
        Entity entity = event.getEntity();
        Mage mage = controller.getRegisteredMage(entity);
        if (mage != null && mage.isGlidingAllowed() && !event.isGliding() && !entity.isOnGround()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerEquip(PlayerItemHeldEvent event)
    {
        if (!controller.isLoaded()) return;

        Player player = event.getPlayer();
        PlayerInventory inventory = player.getInventory();
        ItemStack next = inventory.getItem(event.getNewSlot());

        Mage mage = controller.getMage(player);

        // Check for self-destructing and temporary items
        if (Wand.isSelfDestructWand(next)) {
            mage.sendMessageKey("wand.self_destruct");
            inventory.setItem(event.getNewSlot(), null);
            mage.checkWand();
            return;
        }

        if (NMSUtils.isTemporary(next)) {
            inventory.setItem(event.getNewSlot(), null);
            mage.checkWand();
            return;
        }

        Wand activeWand = mage.getActiveWand();
        boolean isSkill = Wand.isSkill(next);
        if (isSkill && Wand.isQuickCastSkill(next))
        {
            mage.useSkill(next);
            event.setCancelled(true);
            return;
        }

        boolean isQuickCast = activeWand != null && activeWand.isQuickCast() && activeWand.isInventoryOpen();
        if (isQuickCast)
        {
            Spell spell = mage.getSpell(Wand.getSpell(next));
            if (spell != null) {
                activeWand.cast(spell);
            }
            event.setCancelled(true);
            return;
        }

        // Check for active Wand
        boolean isWand = Wand.isWand(next);
        if (activeWand != null && activeWand.isInventoryOpen()) {
            // If the wand inventory is open, we're going to let them select a spell or material
            if (!isWand) {
                mage.activateIcon(activeWand, next);

                // Make sure we have the current wand item in the player's inventory so the
                // item text gets updated on hotbar item selection "bounce"
                int previousSlot = event.getPreviousSlot();
                ItemStack previous = inventory.getItem(previousSlot);
                if (previous != null && previous.equals(activeWand.getItem())) {
                    player.getInventory().setItem(previousSlot, activeWand.getItem());
                }
            }

            event.setCancelled(true);
        } else if (isWand || activeWand != null) {
            // Check to see if we've switched to/from a wand
            mage.checkWandNextTick();
        }

        // Check for map selection if no wand is active
        activeWand = mage.getActiveWand();
        if (activeWand == null && next != null) {
            if (next.getType() == Material.MAP) {
                mage.setLastHeldMapId(next.getDurability());
            }
        }
    }

    @EventHandler
    public void onPlayerSwapItem(PlayerSwapHandItemsEvent event) {

        final Player player = event.getPlayer();
        Mage mage = controller.getRegisteredMage(player);
        if (mage == null) return;

        final Wand activeWand = mage.getActiveWand();
        final Wand offhandWand = mage.getOffhandWand();

        if (activeWand == null && offhandWand == null) return;

        if (activeWand != null && activeWand.performAction(activeWand.getSwapAction())) {
            event.setCancelled(true);
        } else if (activeWand != null && activeWand.isInventoryOpen()) {
            activeWand.closeInventory();
            event.setCancelled(true);
        } else if (activeWand != null || offhandWand != null || Wand.isWand(event.getMainHandItem()) || Wand.isWand(event.getOffHandItem())) {
            mage.checkWandNextTick();
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event)
    {
        final Player player = event.getPlayer();
        Mage mage = controller.getRegisteredMage(player);
        if (mage == null) return;

        // Catch lag-related glitches dropping items from GUIs
        if (mage.getActiveGUI() != null) {
            event.setCancelled(true);
            return;
        }

        final Wand activeWand = mage.getActiveWand();
        final ItemStack droppedItem = event.getItemDrop().getItemStack();

        boolean cancelEvent = false;
        ItemStack activeItem = activeWand == null ? null : activeWand.getItem();
        // It seems like Spigot sets the original item to air before dropping
        // We will be satisfied to only compare the metadata.
        ItemMeta activeMeta = activeItem == null ? null : activeItem.getItemMeta();
        ItemMeta droppedMeta = droppedItem.getItemMeta();
        boolean droppedWand = droppedMeta != null && activeMeta != null && activeItem.getItemMeta().equals(droppedItem.getItemMeta());
        if (droppedWand && activeWand.isUndroppable()) {
            // Postpone cycling until after this event unwinds
            Bukkit.getScheduler().scheduleSyncDelayedTask(controller.getPlugin(), new DropActionTask(activeWand));
            cancelEvent = true;
        } else if (activeWand != null) {
            if (droppedWand) {
                ItemStack mainhandItem = player.getInventory().getItemInMainHand();
                activeWand.deactivate();
                ItemStack restoredItem = player.getInventory().getItemInMainHand();
                ItemMeta restoredMeta = restoredItem == null ? null : restoredItem.getItemMeta();
                activeMeta = activeWand.getItem().getItemMeta();
                // Might have just saved some changes
                droppedItem.setItemMeta(activeMeta);
                // Clear after inventory restore (potentially with deactivate), since that will put the wand back
                if (Wand.hasActiveWand(player) && restoredItem.getType() != Material.AIR
                        && restoredMeta != null && activeMeta.equals(restoredMeta)) {
                    ItemStack newItem = player.getInventory().getItemInMainHand();
                    if (mainhandItem.getAmount() > 0) {
                        newItem.setAmount(mainhandItem.getAmount());
                        player.getInventory().setItemInMainHand(newItem);
                    } else {
                        player.getInventory().setItemInMainHand(new ItemStack(Material.AIR, 1));
                    }
                }
            } else if (activeWand.isInventoryOpen()) {
                if (!controller.isSpellDroppingEnabled()) {
                    cancelEvent = true;
                    // This will happen if you graph an item, change pages, and then drop the item- it would disappear
                    // from the inventory until reload.
                    // Check for this state and prevent it.
                    boolean isInventoryFull = true;
                    PlayerInventory playerInventory = player.getInventory();
                    for (int i = 0; i < Wand.PLAYER_INVENTORY_SIZE; i++) {
                        ItemStack item = playerInventory.getItem(i);
                        if (item == null || item.getType() == Material.AIR) {
                            isInventoryFull = false;
                            break;
                        }
                    }
                    if (isInventoryFull) {
                        activeWand.addToInventory(droppedItem);
                    }
                } else {
                    // The item is already removed from the wand's inventory, but that should be ok
                    controller.removeItemFromWand(activeWand, droppedItem);
                }
            }
        }
        if (!cancelEvent) {
            cancelEvent = InventoryUtils.getMetaBoolean(droppedItem, "undroppable", false);
        }
        if (cancelEvent) {
            if (droppedWand) {
                activeWand.setItem(droppedItem);
            }
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Mage mage = controller.getRegisteredMage(event.getPlayer());
        if (mage == null) return;
        mage.restoreRespawnInventories();
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerInteractArmorStand(PlayerArmorStandManipulateEvent event)
    {
        Player player = event.getPlayer();
        Mage mage = controller.getRegisteredMage(player);
        if (mage == null) return;
        com.elmakers.mine.bukkit.api.wand.Wand wand = mage.checkWand();
        if (wand != null) {
            if (wand.isUndroppable()) {
                event.setCancelled(true);
                return;
            } else {
                wand.deactivate();
            }
        } else {
            ItemStack mainHand = player.getInventory().getItemInMainHand();
            ItemStack offhand = player.getInventory().getItemInOffHand();
            if (InventoryUtils.getMetaBoolean(mainHand, "undroppable", false)
                || (InventoryUtils.isEmpty(mainHand) && InventoryUtils.getMetaBoolean(offhand, "undroppable", false))) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event)
    {
        Entity entity = event.getRightClicked();

        EntityData mob = controller.getMobByName(entity.getCustomName());
        if (mob == null) return;
        String interactSpell = mob.getInteractSpell();
        if (interactSpell == null || interactSpell.isEmpty()) return;

        Player player = event.getPlayer();
        Mage mage = controller.getMage(player);
        event.setCancelled(true);
        ConfigurationSection config = new MemoryConfiguration();
        config.set("entity", entity.getUniqueId().toString());
        controller.cast(mage, interactSpell, config, player, player);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Mage mage = controller.getRegisteredMage(player);
        if (mage == null) return;
        Wand wand = mage.checkWand();

        // Check for a player placing a wand in an item frame
        Entity clickedEntity = event.getRightClicked();

        // Don't think this ever fires for ArmorStand - see above
        boolean isPlaceable = clickedEntity instanceof ItemFrame || clickedEntity instanceof ArmorStand;
        if (isPlaceable) {
            if (wand != null) {
                if (wand.isUndroppable()) {
                    event.setCancelled(true);
                    return;
                } else {
                    wand.deactivate();
                }
            } else {
                ItemStack mainHand = player.getInventory().getItemInMainHand();
                ItemStack offhand = player.getInventory().getItemInOffHand();
                if (InventoryUtils.getMetaBoolean(mainHand, "undroppable", false)
                    || (InventoryUtils.isEmpty(mainHand) && InventoryUtils.getMetaBoolean(offhand, "undroppable", false))) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        // Check for clicking on a Citizens NPC, in case
        // this hasn't been cancelled yet
        if (controller.isNPC(event.getRightClicked())) {
            if (wand != null) {
                wand.closeInventory();
            }

            // Don't let it re-open right away
            mage.checkLastClick(0);
        } else {
            // Don't allow interacting while holding spells, brushes or upgrades
            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            if (Wand.isSpell(itemInHand) || Wand.isBrush(itemInHand) || Wand.isUpgrade(itemInHand)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerAnimate(PlayerAnimationEvent event)
    {
        Player player = event.getPlayer();
        if (event.getAnimationType() != PlayerAnimationType.ARM_SWING)
        {
            return;
        }

        Mage mage = controller.getMage(player);

        Wand wand = mage.checkWand();
        if (wand == null) return;

        Messages messages = controller.getMessages();
        if (!controller.hasWandPermission(player))
        {
            return;
        }

        // Check for region or wand-specific permissions
        if (!controller.hasWandPermission(player, wand))
        {
            wand.deactivate();
            mage.sendMessage(messages.get("wand.no_permission").replace("$wand", wand.getName()));
            return;
        }

        if (!mage.checkLastClick(clickCooldown)) {
            return;
        }

        if (wand.isUpgrade()) return;

        wand.playEffects("swing");

        wand.performAction(wand.getLeftClickAction());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (!controller.isLoaded()) return;

        Action action = event.getAction();
        boolean isLeftClick = action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK;
        boolean isRightClick = (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK);

        // We only care about left click and right click.
        if (!isLeftClick && !isRightClick) return;

        // Note that an interact on air event will arrive pre-cancelled
        // So this is kind of useless. :\
        //if (event.isCancelled()) return;

        // Block block = event.getClickedBlock();
        // controller.getLogger().info("INTERACT: " + event.getAction() + " on " + (block == null ? "NOTHING" : block.getType()) + " cancelled: " + event.isCancelled());

        Player player = event.getPlayer();

        // Don't allow interacting while holding spells, brushes or upgrades
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        boolean isSkill = Wand.isSkill(itemInHand);
        boolean isSpell = !isSkill && Wand.isSpell(itemInHand);
        if (isSpell || Wand.isBrush(itemInHand) || Wand.isUpgrade(itemInHand)) {
            event.setCancelled(true);
            return;
        }

        boolean isOffhandSkill = false;
        ItemStack itemInOffhand = player.getInventory().getItemInOffHand();
        if (isRightClick) {
            isOffhandSkill = Wand.isSkill(itemInOffhand);
            boolean isOffhandSpell = !isOffhandSkill && Wand.isSpell(itemInOffhand);
            if (isOffhandSpell || Wand.isBrush(itemInOffhand) || Wand.isUpgrade(itemInOffhand)) {
                event.setCancelled(true);
                return;
            }
        }

        Mage mage = controller.getMage(player);

        // Check for right-clicking SP or currency items
        if (isRightClick) {
            Integer sp = Wand.getSP(itemInHand);
            if (sp != null) {
                if (mage.isAtMaxSkillPoints()) {
                    String limitMessage = controller.getMessages().get("sp.limit");
                    limitMessage = limitMessage.replace("$amount", Integer.toString(controller.getSPMaximum()));
                    mage.sendMessage(limitMessage);
                } else {
                    mage.addSkillPoints(sp);
                    String balanceMessage = controller.getMessages().get("sp.deposited");
                    balanceMessage = balanceMessage.replace("$amount", Integer.toString(sp));
                    balanceMessage = balanceMessage.replace("$balance", Integer.toString(mage.getSkillPoints()));
                    mage.sendMessage(balanceMessage);
                    player.getInventory().setItemInMainHand(null);
                }
                event.setCancelled(true);
                return;
            }
            Double value = Wand.getCurrencyAmount(itemInHand);
            if (value != null) {
                String currencyKey = Wand.getCurrencyType(itemInHand);
                if (mage.isAtMaxCurrency(currencyKey)) {
                    Currency currency = controller.getCurrency(currencyKey);
                    String limitMessage = controller.getMessages().get("currency." + currencyKey + ".limit", controller.getMessages().get("currency.limit"));
                    limitMessage = limitMessage.replace("$amount", Integer.toString((int)Math.ceil(currency.getMaxValue())));
                    limitMessage = limitMessage.replace("$type", controller.getMessages().get("currency." + currencyKey + ".name", currencyKey));
                    mage.sendMessage(limitMessage);
                } else {
                    mage.addCurrency(currencyKey, value);
                    player.getInventory().setItemInMainHand(null);

                    String balanceMessage = controller.getMessages().get("currency." + currencyKey + ".deposited", controller.getMessages().get("currency.deposited"));
                    balanceMessage = balanceMessage.replace("$amount", Integer.toString((int)Math.ceil(value)));
                    balanceMessage = balanceMessage.replace("$balance", Integer.toString((int)Math.ceil(mage.getCurrency(currencyKey))));
                    balanceMessage = balanceMessage.replace("$type", controller.getMessages().get("currency." + currencyKey + ".name", currencyKey));
                    mage.sendMessage(balanceMessage);
                }
                event.setCancelled(true);
                return;
            }
        }

        Wand wand = mage.checkWand();
        if (action == Action.RIGHT_CLICK_BLOCK) {
            Material material = event.getClickedBlock().getType();
            isRightClick = !controller.isInteractable(event.getClickedBlock());

            // This is to prevent Essentials signs from giving you an item in your wand inventory.
            if (wand != null && DefaultMaterials.isSign(material)) {
                wand.closeInventory();
            }
        }
        if (!isLeftClick && !mage.checkLastClick(clickCooldown)) {
            return;
        }

        // Prefer wand right-click if wand is active
        if (isOffhandSkill && wand != null) {
            if (wand.getRightClickAction() != WandAction.NONE) {
                isOffhandSkill = false;
            }
        }
        if (isRightClick && (isOffhandSkill || isSkill)) {
            if (isSkill) {
                mage.useSkill(itemInHand);
            } else {
                mage.useSkill(itemInOffhand);
            }
            event.setCancelled(true);
            return;
        }

        // Check for offhand casting
        if (isRightClick)
        {
            if (allowOffhandCasting && mage.offhandCast()) {
                // Kind of weird but the intention is to avoid normal "left click" actions,
                // which in the offhand case are right-click actions.
                if (cancelInteractOnLeftClick) {
                    event.setCancelled(true);
                }
                return;
            }
        }

        // Check for wearing via right-click
        // Special-case here for skulls, which actually are not wearable via right-click.
        if (itemInHand != null && isRightClick && controller.isWearable(itemInHand) && !DefaultMaterials.isSkull(itemInHand.getType()))
        {
            if (wand != null)
            {
                wand.deactivate();
            }
            controller.onArmorUpdated(mage);
            return;
        }

        if (wand == null) return;

        Messages messages = controller.getMessages();
        if (!controller.hasWandPermission(player))
        {
            return;
        }

        // Check for region or wand-specific permissions
        if (!controller.hasWandPermission(player, wand))
        {
            wand.deactivate();
            mage.sendMessage(messages.get("wand.no_permission").replace("$wand", wand.getName()));
            return;
        }

        // Check for enchantment table click
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock != null && clickedBlock.getType() != Material.AIR && enchantBlockMaterial != null && enchantBlockMaterial.is(clickedBlock))
        {
            Spell spell = null;
            if (player.isSneaking())
            {
                spell = enchantSneakClickSpell != null ? mage.getSpell(enchantSneakClickSpell) : null;
            }
            else
            {
                spell = enchantClickSpell != null ? mage.getSpell(enchantClickSpell) : null;
            }
            if (spell != null)
            {
                spell.cast();
                event.setCancelled(true);
                return;
            }
        }

        if (isLeftClick && !wand.isUpgrade() && wand.getLeftClickAction() != WandAction.NONE && cancelInteractOnLeftClick)
        {
            event.setCancelled(true);
        }

        if (isRightClick && wand.performAction(wand.getRightClickAction()) && cancelInteractOnRightClick)
        {
           event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        // Automatically re-activate mages.
        Player player = event.getPlayer();
        controller.getMage(player);
        controller.checkVanished(player);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerKick(PlayerKickEvent event)
    {
        handlePlayerQuitEvent(event);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        handlePlayerQuitEvent(event);
    }

    protected void handlePlayerQuitEvent(PlayerEvent event) {
        Player player = event.getPlayer();
        Mage mage = controller.getRegisteredMage(player);
        if (mage != null) {
            mage.onPlayerQuit(event);
            controller.playerQuit(mage);
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event)
    {
        Player player = event.getPlayer();
        if (controller.isNPC(player)) return;
        Mage mage = controller.getMage(player);
        mage.onTeleport(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event)
    {
        Player player = event.getPlayer();
        if (controller.isNPC(player)) return;
        Mage mage = controller.getMage(player);
        mage.onChangeWorld();
    }

    @EventHandler
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event)
    {
        if (event.getNewGameMode() == GameMode.CREATIVE) {
            Player player = event.getPlayer();
            if (!player.isOnline()) return;

            Mage mage = controller.getMage(player);
            com.elmakers.mine.bukkit.api.wand.Wand activeWand = mage.getActiveWand();
            if (activeWand != null) {
                activeWand.closeInventory();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerPickupItem(org.bukkit.event.player.PlayerPickupItemEvent event)
    {
        Player player = event.getPlayer();
        Mage mage = controller.getMage(player);

        // If a wand's inventory is active, add the item there
        if (mage.hasStoredInventory()) {
            event.setCancelled(true);
            if (mage.addToStoredInventory(event.getItem().getItemStack())) {
                event.getItem().remove();
                mage.playSoundEffect(Wand.itemPickupSound);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerPrePickupItem(org.bukkit.event.player.PlayerPickupItemEvent event)
    {
        Item item = event.getItem();
        ItemStack pickup = item.getItemStack();
        if (NMSUtils.isTemporary(pickup) || item.hasMetadata("temporary"))
        {
            item.removeMetadata("temporary", controller.getPlugin());
            item.remove();
            event.setCancelled(true);
            return;
        }

        boolean isWand = Wand.isWand(pickup);

        // Check to see if this is an item we might undo, and remove it from undo
        UndoList undoList = controller.getEntityUndo(item);
        if (undoList != null) {
            undoList.remove(item);
        }

        Player player = event.getPlayer();
        Mage mage = controller.getMage(player);
        // Remove lost wands from records
        Messages messages = controller.getMessages();
        if (isWand) {
            Wand wand = controller.getWand(pickup);
            if (!wand.canUse(player)) {
                if (lastDropWarn == 0 || System.currentTimeMillis() - lastDropWarn > 10000) {
                    mage.sendMessage(messages.get("wand.bound").replace("$name", wand.getOwner()));
                }
                lastDropWarn = System.currentTimeMillis();
                event.setCancelled(true);
                return;
            }

            controller.removeLostWand(wand.getId());
        }

        // Wands will absorb spells and upgrade items
        Wand activeWand = mage.getActiveWand();
        if (activeWand != null
                && activeWand.isModifiable()
                && (Wand.isSpell(pickup) || Wand.isBrush(pickup) || Wand.isUpgrade(pickup) || Wand.isSP(pickup))
                && activeWand.addItem(pickup)) {
            event.getItem().remove();
            event.setCancelled(true);
            return;
        }

        if (!mage.hasStoredInventory() && isWand) {
            mage.checkWandNextTick();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (!item.hasItemMeta()) return;

        // The item we get passed in this event is a shallow bukkit copy.
        item = CompatibilityUtils.makeReal(item);

        String consumeSpell = controller.getWandProperty(item, "consume_spell", "");
        if (!consumeSpell.isEmpty()) {
            Mage mage = controller.getMage(event.getPlayer());
            Spell spell = mage.getSpell(consumeSpell);
            if (spell != null) {
                spell.cast();
            }
        }
    }
}
