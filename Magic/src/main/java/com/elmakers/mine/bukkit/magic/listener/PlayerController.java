package com.elmakers.mine.bukkit.magic.listener;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
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
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.economy.Currency;
import com.elmakers.mine.bukkit.api.entity.EntityData;
import com.elmakers.mine.bukkit.api.item.Cost;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.wand.WandAction;
import com.elmakers.mine.bukkit.block.DefaultMaterials;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.magic.Mage;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.magic.MagicMetaKeys;
import com.elmakers.mine.bukkit.magic.SpellBlock;
import com.elmakers.mine.bukkit.tasks.DropActionTask;
import com.elmakers.mine.bukkit.tasks.GiveItemTask;
import com.elmakers.mine.bukkit.tasks.PlayerQuitTask;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.CurrencyAmount;
import com.elmakers.mine.bukkit.utility.TextUtils;
import com.elmakers.mine.bukkit.wand.Wand;

public class PlayerController implements Listener {
    private static final int DEBUG_LEVEL = 199;

    private final MagicController controller;
    private final Map<Material, SpellBlock> spellBlocks = new HashMap<>();
    private int clickCooldown = 150;
    private boolean cancelInteractOnLeftClick = true;
    private boolean cancelInteractOnRightClick = false;
    private boolean allowOffhandCasting = true;
    private boolean autoAbsorbSP = true;
    private long lastDropWarn = 0;
    private int logoutDelay = 0;

    public PlayerController(MagicController controller) {
        this.controller = controller;
    }

    public void loadProperties(ConfigurationSection properties) {
        logoutDelay = properties.getInt("logout_delay", 0);
        clickCooldown = properties.getInt("click_cooldown", 0);
        String enchantBlockKey = properties.getString("enchant_block", "enchantment_table");
        if (!enchantBlockKey.isEmpty()) {
            MaterialAndData enchantBlockMaterial = new MaterialAndData(enchantBlockKey);
            if (enchantBlockMaterial.isValid()) {
                SpellBlock enchantBlock = new SpellBlock(
                        properties.getString("enchant_click"),
                        properties.getString("enchant_sneak_click"),
                        properties.getBoolean("enchant_click_requires_wand", false)
                );
                spellBlocks.put(enchantBlockMaterial.getMaterial(), enchantBlock);
            }
        }
        ConfigurationSection spellBlocks = properties.getConfigurationSection("spell_blocks");
        if (spellBlocks != null) {
            Collection<String> keys = spellBlocks.getKeys(false);
            for (String key : keys) {
                MaterialAndData blockMaterial = new MaterialAndData(key);
                ConfigurationSection spellBlockConfiguration = spellBlocks.getConfigurationSection(key);
                if (blockMaterial.isValid() && spellBlockConfiguration != null) {
                    SpellBlock enchantBlock = new SpellBlock(spellBlockConfiguration);
                    this.spellBlocks.put(blockMaterial.getMaterial(), enchantBlock);
                }
            }
        }
        cancelInteractOnLeftClick = properties.getBoolean("cancel_interact_on_left_click", true);
        cancelInteractOnRightClick = properties.getBoolean("cancel_interact_on_right_click", false);
        allowOffhandCasting = properties.getBoolean("allow_offhand_casting", true);
        autoAbsorbSP = properties.getBoolean("auto_absorb_sp", true);
    }

    private void trigger(Player player, String trigger) {
        Mage mage = controller.getRegisteredMage(player);
        if (mage != null) {
            mage.trigger(trigger);
        }
    }

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        trigger(event.getPlayer(), event.isSneaking() ? "sneak" : "stop_sneak");
    }

    @EventHandler
    public void onPlayerSprint(PlayerToggleSprintEvent event) {
        trigger(event.getPlayer(), event.isSprinting() ? "sprint" : "stop_sprint");
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
    public void onPlayerToggleGlide(EntityToggleGlideEvent event) {
        Entity entity = event.getEntity();
        Mage mage = controller.getRegisteredMage(entity);
        if (mage != null && mage.isGlidingAllowed() && !event.isGliding() && !entity.isOnGround()) {
            event.setCancelled(true);
            Player player = mage.getPlayer();
            if (player != null) {
                controller.addFlightExemption(player, 5000);
            }
        } else if (mage != null) {
            mage.trigger(event.isGliding() ? "glide" : "stop_glide");
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
        if (mage.getDebugLevel() >= DEBUG_LEVEL) {
            mage.sendDebugMessage("EQUIP " + event.getNewSlot() + " from " + event.getPreviousSlot(), DEBUG_LEVEL);
        }
        controller.updateOnEquip(next);

        // Check for self-destructing and temporary items
        if (Wand.isSelfDestructWand(next)) {
            mage.sendMessageKey("wand.self_destruct");
            inventory.setItem(event.getNewSlot(), null);
            mage.checkWand();
            return;
        }

        // Immovable items don't disappear when equipped, this is to match with click behavior
        // Also allows temporary items to be held, like with the broom handle
        if (CompatibilityLib.getItemUtils().isTemporary(next) && !CompatibilityLib.getNBTUtils().getMetaBoolean(next, "unmoveable", false)) {
            ItemStack replacement = CompatibilityLib.getItemUtils().getReplacement(next);
            inventory.setItem(event.getNewSlot(), replacement);
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

        // Check for auto wand
        boolean isWand = Wand.isWand(next);
        if (!isWand) {
            ItemStack autoWand = controller.getAutoWand(next);
            if (autoWand != null) {
                next = autoWand;
                inventory.setItem(event.getNewSlot(), next);
                isWand = true;
            }
        }

        // Check for active Wand
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
            // Clear the last activated slot so we show invalid wand messages again
            mage.setLastActivatedSlot(event.getPreviousSlot());
            // Check to see if we've switched to/from a wand
            mage.checkWandNextTick();
        }

        // Check for map selection if no wand is active
        activeWand = mage.getActiveWand();
        if (activeWand == null && next != null) {
            if (DefaultMaterials.isFilledMap(next.getType())) {
                mage.setLastHeldMapId(CompatibilityLib.getInventoryUtils().getMapId(next));
            }
        }
    }

    @EventHandler
    public void onPlayerSwapItem(PlayerSwapHandItemsEvent event) {
        final Player player = event.getPlayer();
        Mage mage = controller.getRegisteredMage(player);
        if (mage == null) return;
        ItemStack main = event.getMainHandItem();
        ItemStack offhand = event.getOffHandItem();
        if (mage.getDebugLevel() >= DEBUG_LEVEL) {
            mage.sendDebugMessage("SWAP ITEM: " + (main == null ? "(Nothing)" : main.getType().name())
                + " with " + (offhand == null ? "(Nothing)" : offhand.getType().name()), DEBUG_LEVEL);
        }
        if (CompatibilityLib.getNBTUtils().getMetaBoolean(offhand, "unswappable", false) || CompatibilityLib.getNBTUtils().getMetaBoolean(main, "unswappable", false)) {
            event.setCancelled(true);
        }
        mage.trigger("swap");
        final Wand activeWand = mage.getActiveWand();
        final Wand offhandWand = mage.getOffhandWand();

        if (activeWand == null && offhandWand == null) return;

        if (activeWand != null && activeWand.performAction(activeWand.getSwapAction())) {
            event.setCancelled(true);
        } else if (activeWand != null && activeWand.isInventoryOpen()) {
            activeWand.closeInventory();
            event.setCancelled(true);
        } else if (activeWand != null || offhandWand != null || Wand.isWand(event.getMainHandItem()) || Wand.isWand(event.getOffHandItem())) {
            // Make sure to save changes to the active and offhand wands
            boolean checkWand = false;
            if (activeWand != null && Wand.isWand(event.getOffHandItem())) {
                ItemStack activeItem = activeWand.getItem();
                ItemMeta activeMeta = activeItem.getItemMeta();
                ItemMeta offhandMeta = event.getOffHandItem().getItemMeta();
                if (offhandMeta != null && activeMeta != null && activeMeta.equals(offhandMeta)) {
                    activeWand.setItem(event.getOffHandItem());
                    activeWand.saveState();
                }
                checkWand = true;
            }
            if (offhandWand != null && Wand.isWand(event.getMainHandItem())) {
                ItemStack offhandItem = offhandWand.getItem();
                ItemMeta offhandMeta = offhandItem.getItemMeta();
                ItemMeta mainHandMeta = event.getMainHandItem().getItemMeta();
                if (mainHandMeta != null && offhandMeta != null && offhandMeta.equals(mainHandMeta)) {
                    offhandWand.setItem(event.getMainHandItem());
                    offhandWand.saveState();
                }
                checkWand = true;
            }

            if (checkWand) {
                mage.checkWandNextTick();
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event)
    {
        final Player player = event.getPlayer();
        Mage mage = controller.getRegisteredMage(player);
        if (mage == null) return;
        if (mage.getDebugLevel() >= DEBUG_LEVEL) {
            Item item = event.getItemDrop();
            ItemStack itemStack = item == null ? null : item.getItemStack();
            mage.sendDebugMessage("DROP ITEM: " + (itemStack == null ? "(Nothing)" : itemStack.getType().name()) + " at " + System.currentTimeMillis(), DEBUG_LEVEL);
        }

        // As of 1.15 we will get an animation event right after the drop event.
        // We want to ignore this.
        mage.checkLastClick(50);

        // Catch lag-related glitches dropping items from GUIs
        if (mage.getActiveGUI() != null) {
            event.setCancelled(true);
            return;
        }
        mage.trigger("drop");

        final Wand activeWand = mage.getActiveWand();
        final ItemStack droppedItem = event.getItemDrop().getItemStack();

        boolean cancelEvent = false;
        ItemStack activeItem = activeWand == null ? null : activeWand.getItem();
        // It seems like Spigot sets the original item to air before dropping
        // We will be satisfied to only compare the metadata.
        ItemMeta activeMeta = activeItem == null ? null : activeItem.getItemMeta();
        ItemMeta droppedMeta = droppedItem.getItemMeta();
        final boolean droppedSpell = Wand.isSpell(droppedItem) || Wand.isBrush(droppedItem);
        final boolean droppedWand = droppedMeta != null && activeMeta != null && activeMeta.equals(droppedMeta);
        boolean inSpellInventory = activeWand != null && activeWand.isInventoryOpen();
        if (droppedWand && activeWand.isUndroppable()) {
            // Postpone cycling until after this event unwinds
            Bukkit.getScheduler().scheduleSyncDelayedTask(controller.getPlugin(), new DropActionTask(activeWand));
            cancelEvent = true;
        } else if (activeWand != null) {
            if (droppedWand) {
                ItemStack mainhandItem = player.getInventory().getItemInMainHand();
                activeWand.setItem(droppedItem);
                activeWand.deactivate();
                ItemStack restoredItem = player.getInventory().getItemInMainHand();
                ItemMeta restoredMeta = restoredItem == null ? null : restoredItem.getItemMeta();
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
            } else if (inSpellInventory && droppedSpell) {
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
            cancelEvent = CompatibilityLib.getNBTUtils().getMetaBoolean(droppedItem, "undroppable", false);
        }
        if (cancelEvent) {
            // Work around a Spigot bug that would make the item disappear if the player's inventory is full
            if (!inSpellInventory || !droppedSpell) {
                boolean isFull = true;
                ItemStack[] items = player.getInventory().getStorageContents();
                for (int i = items.length - 1; i >= 0; i--) {
                    if (CompatibilityLib.getItemUtils().isEmpty(items[i])) {
                        isFull = false;
                        break;
                    }
                }
                if (isFull) return;
            }

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
        mage.onRespawn();
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerInteractArmorStand(PlayerArmorStandManipulateEvent event)
    {
        Player player = event.getPlayer();
        Mage mage = controller.getRegisteredMage(player);
        if (mage == null) return;
        if (mage.getDebugLevel() >= DEBUG_LEVEL) {
            ItemStack playerItem = event.getPlayerItem();
            mage.sendDebugMessage("ENTITY ARMOR STAND with: " + event.getHand() + " at " + event.getRightClicked() + " with " + (playerItem == null ? "(Nothing)" : playerItem.getType().name()), DEBUG_LEVEL);
        }
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
            if (CompatibilityLib.getNBTUtils().getMetaBoolean(mainHand, "undroppable", false)
                || (CompatibilityLib.getItemUtils().isEmpty(mainHand) && CompatibilityLib.getNBTUtils().getMetaBoolean(offhand, "undroppable", false))) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event)
    {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Entity entity = event.getRightClicked();
        Mage mobMage = controller.getRegisteredMage(entity);
        if (mobMage != null) {
            mobMage.trigger("interact");
        }

        EntityData mob = controller.getMob(entity);
        if (mob == null) return;
        String interactSpell = mob.getInteractSpell();
        interactSpell = interactSpell != null && interactSpell.isEmpty() ? null : interactSpell;
        List<String> interactCommands = mob.getInteractCommands();
        interactCommands = interactCommands != null && interactCommands.isEmpty() ? null : interactCommands;

        if (interactSpell == null && interactCommands == null) return;

        // This will be processed as a left-click via the animate event if we don't cancel it and set a cooldown
        event.setCancelled(true);
        Player player = event.getPlayer();
        Mage playerMage = controller.getMage(player);
        playerMage.checkLastClick(0);

        if (playerMage.getDebugLevel() >= DEBUG_LEVEL) {
            playerMage.sendDebugMessage("ENTITY AT INTERACT with: " + event.getHand() + " at " + entity + " : " + TextUtils.printVector(event.getClickedPosition()), DEBUG_LEVEL);
        }
        String permission = mob.getInteractPermission();
        if (permission != null && !permission.isEmpty() && !player.hasPermission(permission)) {
            String message = controller.getMessages().get("npc.no_permission");
            playerMage.sendMessage(message);
            return;
        }
        boolean requiresOwner = mob.getInteractRequiresOwner();
        if (requiresOwner) {
            if (!(entity instanceof Tameable)) {
                return;
            }
            Tameable tamed = (Tameable)entity;
            AnimalTamer tamer = tamed.getOwner();
            if (!tamer.getUniqueId().equals(player.getUniqueId())) {
                return;
            }
        }

        Collection<Cost> costs = mob.getInteractCosts();
        if (costs != null) {
            for (Cost cost : costs) {
                if (!cost.has(playerMage, playerMage.getActiveWand(), null)) {
                    String baseMessage = controller.getMessages().get("npc.insufficient");
                    String costDescription = cost.getFullDescription(controller.getMessages(), null);
                    costDescription = baseMessage.replace("$cost", costDescription);
                    playerMage.sendMessage(costDescription);
                    return;
                }
            }
        }

        boolean success = false;
        if (interactSpell != null) {
            ConfigurationSection parameters = mob.getInteractSpellParameters();
            parameters = parameters == null ? ConfigurationUtils.newConfigurationSection() : ConfigurationUtils.cloneConfiguration(parameters);
            Entity sourceEntity = player;
            switch (mob.getInteractSpellSource()) {
                case PLAYER:
                    sourceEntity = player;
                    break;
                case MOB:
                    sourceEntity = entity;
                    break;
                case CONSOLE:
                    controller.getLogger().info("Invalid spell source on " + mob.getKey() + ": CONSOLE, using MOB instead");
                    sourceEntity = entity;
                    break;
                case OPPED_PLAYER:
                    controller.getLogger().info("Invalid spell source on " + mob.getKey() + ": OPPED_PLAYER, will not op player for spell cast");
                    sourceEntity = player;
                    break;
            }
            Mage mage = controller.getMage(sourceEntity);
            switch (mob.getInteractSpellTarget()) {
                case PLAYER:
                    parameters.set("player", player.getName());
                    break;
                case MOB:
                    parameters.set("entity", entity.getUniqueId().toString());
                    break;
                case NONE:
                    break;
            }
            if (!interactSpell.contains("|")) {
                int level = mage.getActiveProperties().getSpellLevel(interactSpell);
                if (level > 1) {
                    interactSpell += "|" + level;
                }
            }

            success = controller.cast(mage, interactSpell, parameters, player, sourceEntity);
        }
        if (interactCommands != null) {
            CommandSender executor = player;
            boolean opPlayer = false;
            switch (mob.getInteractCommandSource()) {
                case CONSOLE:
                    executor = Bukkit.getConsoleSender();
                    break;
                case PLAYER:
                    executor = player;
                    break;
                case OPPED_PLAYER:
                    executor = player;
                    opPlayer = !player.isOp();
                    break;
                case MOB:
                    controller.getLogger().info("Invalid spell source on " + mob.getKey() + ": MOB, will use CONSOLE instead");
                    executor = Bukkit.getConsoleSender();
                    break;
            }
            Location location = entity.getLocation();
            for (String command : interactCommands) {
                try {
                    if (opPlayer) {
                        executor.setOp(true);
                    }
                    String converted = TextUtils.parameterize(command, location, player);
                    controller.getPlugin().getServer().dispatchCommand(executor, converted);
                    success = true;
                } catch (Exception ex) {
                    controller.getLogger().log(Level.WARNING, "Error running command: " + command, ex);
                } finally {
                    if (opPlayer) {
                        executor.setOp(false);
                    }
                }
            }
        }

        if (costs != null && success) {
            String baseMessage = controller.getMessages().get("npc.deducted");
            for (Cost cost : costs) {
                cost.deduct(playerMage, playerMage.getActiveWand(), null);
                String costDescription = cost.getFullDescription(controller.getMessages(), null);
                costDescription = baseMessage.replace("$cost", costDescription);
                playerMage.sendMessage(costDescription);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Entity clickedEntity = event.getRightClicked();
        // Check for this event being sent in addition to "at entity". We'll let the other handler
        // perform the actions, but we need to cancel this one too otherwise villager interaction
        // interferes with NPC interaction.
        EntityData mob = controller.getMob(clickedEntity);
        if (mob != null && mob.hasInteract()) {
            event.setCancelled(true);
            return;
        }

        Player player = event.getPlayer();
        Mage mage = controller.getRegisteredMage(player);
        if (mage == null) return;
        Wand wand = mage.checkWand();

        // Check for a player placing a wand in an item frame

        if (mage.getDebugLevel() >= DEBUG_LEVEL) {
            mage.sendDebugMessage("ENTITY INTERACT with: " + event.getHand() + " at " + clickedEntity, DEBUG_LEVEL);
        }

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
                if (CompatibilityLib.getNBTUtils().getMetaBoolean(mainHand, "undroppable", false)
                    || (CompatibilityLib.getItemUtils().isEmpty(mainHand) && CompatibilityLib.getNBTUtils().getMetaBoolean(offhand, "undroppable", false))) {
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
        if (event.getAnimationType() != PlayerAnimationType.ARM_SWING) {
            return;
        }
        if (!controller.useAnimationEvents(player)) {
            return;
        }

        Mage mage = controller.getMage(player);
        if (mage.getDebugLevel() >= DEBUG_LEVEL) {
            mage.sendDebugMessage("ANIMATE: " + event.getAnimationType() + " at " + System.currentTimeMillis(), DEBUG_LEVEL);
        }

        if (!mage.checkLastClick(clickCooldown)) {
            return;
        }
        mage.trigger("left_click");

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

        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        Mage mage = controller.getMage(player);
        if (mage.getDebugLevel() >= DEBUG_LEVEL) {
            ItemStack item = event.getItem();
            Block block = event.getClickedBlock();
            mage.sendDebugMessage("INTERACT " + event.getAction()  + " with " + event.getHand() + " using: " + (item == null ? "(Nothing)" : item.getType().name())
                + ", block: " + (block == null ? "(Nothing)" : block.getType().name()) + " at " + System.currentTimeMillis(), DEBUG_LEVEL);
        }

        // Check for locked items
        if (!mage.canUse(itemInHand)) {
            mage.sendMessage(controller.getMessages().get("mage.no_class").replace("$name", controller.describeItem(itemInHand)));
            event.setCancelled(true);
            return;
        }

        Wand wand = mage.checkWand();

        // Check for wand permission if a wand is being held
        if (wand != null) {
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
        }

        // If wand needs to be closed then always allow a right-click
        boolean closingWand = isRightClick && wand != null && wand.getRightClickAction() == WandAction.TOGGLE && wand.isInventoryOpen();
        if (!closingWand) {
            closingWand = isLeftClick && wand != null && wand.getLeftClickAction() == WandAction.TOGGLE && wand.isInventoryOpen();
        }
        // Check for offhand casting
        if (!closingWand && isRightClick && allowOffhandCasting && mage.offhandCast()) {
            // Kind of weird but the intention is to avoid normal "left click" actions,
            // which in the offhand case are right-click actions.
            if (cancelInteractOnLeftClick) {
                event.setCancelled(true);
            }
            return;
        }

        // Don't allow interacting while holding spells, brushes or upgrades
        boolean isSkill = Wand.isSkill(itemInHand);
        boolean isSpell = !isSkill && Wand.isSpell(itemInHand);
        if (!closingWand && (isSpell || Wand.isBrush(itemInHand) || Wand.isUpgrade(itemInHand))) {
            event.setCancelled(true);
            return;
        }

        boolean isOffhandSkill = false;
        ItemStack itemInOffhand = player.getInventory().getItemInOffHand();
        if (isRightClick && !closingWand) {
            isOffhandSkill = Wand.isSkill(itemInOffhand);
            boolean isOffhandSpell = !isOffhandSkill && Wand.isSpell(itemInOffhand);
            if (isOffhandSpell || Wand.isBrush(itemInOffhand) || Wand.isUpgrade(itemInOffhand)) {
                event.setCancelled(true);
                return;
            }
        }

        // Check for right-clicking SP or currency items
        if (isRightClick && !closingWand) {
            CurrencyAmount currencyAmount = CompatibilityLib.getInventoryUtils().getCurrencyAmount(itemInHand);
            Currency currency = currencyAmount == null ? null : controller.getCurrency(currencyAmount.getType());
            if (currency != null) {
                Messages messages = controller.getMessages();
                currencyAmount.scale(itemInHand.getAmount());
                if (mage.isAtMaxCurrency(currencyAmount.getType())) {
                    String limitMessage = messages.get("currency." + currencyAmount.getType() + ".limit", messages.get("currency.default.limit"));
                    limitMessage = limitMessage.replace("$amount", currency.formatAmount(currency.getMaxValue(), messages));
                    limitMessage = limitMessage.replace("$type", currency.getName(messages));
                    mage.sendMessage(limitMessage);
                } else {
                    mage.addCurrency(currencyAmount.getType(), currencyAmount.getAmount(), true);
                    player.getInventory().setItemInMainHand(null);
                    String balanceMessage = messages.get("currency." + currencyAmount.getType() + ".deposited", messages.get("currency.default.deposited"));
                    balanceMessage = balanceMessage.replace("$amount",  currency.formatAmount(currencyAmount.getAmount(), messages));
                    balanceMessage = balanceMessage.replace("$balance", currency.formatAmount(currency.getBalance(mage), messages));
                    balanceMessage = balanceMessage.replace("$type", currency.getName(messages));
                    mage.sendMessage(balanceMessage);
                }
                event.setCancelled(true);
                return;
            }
        }

        if (action == Action.RIGHT_CLICK_BLOCK) {
            Material material = event.getClickedBlock().getType();
            boolean isInteractible = wand != null ? wand.isInteractible(event.getClickedBlock()) : controller.isInteractible(event.getClickedBlock());
            isRightClick = !isInteractible;

            // This is to prevent Essentials signs from giving you an item in your wand inventory.
            if (wand != null && DefaultMaterials.isSign(material)) {
                wand.closeInventory();
            }
        }
        if (!isLeftClick && !mage.checkLastClick(clickCooldown)) {
            return;
        }
        if (isRightClick) {
            mage.trigger("right_click");
        }

        // Prefer wand right-click if wand is active
        if (isOffhandSkill && wand != null) {
            if (wand.getRightClickAction() != WandAction.NONE) {
                isOffhandSkill = false;
            }
        }

        if (isRightClick && (isOffhandSkill || isSkill) && !closingWand) {
            if (isSkill) {
                mage.useSkill(itemInHand);
            } else {
                mage.useSkill(itemInOffhand);
            }
            event.setCancelled(true);
            return;
        }

        // Check for wearing via right-click
        // Special-case here for skulls, which actually are not wearable via right-click.
        // Wearable wands are handled below.
        if (wand == null && !closingWand && itemInHand != null && isRightClick && controller.isWearable(itemInHand) && !DefaultMaterials.isSkull(itemInHand.getType()))
        {
            controller.onArmorUpdated(mage);
            return;
        }

        // Check for enchantment table click
        Block clickedBlock = event.getClickedBlock();
        SpellBlock spellBlock = clickedBlock == null ? null : spellBlocks.get(clickedBlock.getType());
        if (spellBlock != null
            && !closingWand
            && (wand != null || !spellBlock.requiresWand())
            && (!spellBlock.requiresSpellProgression() || (wand != null && wand.hasSpellProgression()))
        ) {
            String spellKey = null;
            if (player.isSneaking()) {
                spellKey = spellBlock.getRightClickSneakSpell();
            } else {
                spellKey = spellBlock.getRightClickSpell();
            }

            Spell spell = spellKey != null ? mage.getSpell(spellKey) : null;;
            if (spell != null) {
                boolean result = spell.cast();
                if (spellBlock.isCancelClick() && result) {
                    event.setCancelled(true);
                }
            }
            return;
        }

        // Check for offhand items
        ItemStack offhandItem = player.getInventory().getItemInOffHand();
        if (controller.isOffhandMaterial(offhandItem) && !closingWand) {
            return;
        }

        if (wand == null) return;

        if (isRightClick && wand.performAction(wand.getRightClickAction()))
        {
            if (cancelInteractOnRightClick) {
                event.setCancelled(true);
            } else {
                // This prevents glitches when using block-based consumable wands
                event.setUseInteractedBlock(Event.Result.DENY);
            }
        } else if (isRightClick) {
            if (wand.tryToWear(mage)) {
                event.setCancelled(true);
                player.getInventory().setItemInMainHand(null);
                mage.checkWand();
            } else {
                controller.onArmorUpdated(mage);
            }
        }

        if (isLeftClick) {
            if (!controller.useAnimationEvents(player)) {
                wand.playEffects("swing");
                if (!wand.isUpgrade()) {
                    if (wand.performAction(wand.getLeftClickAction()) && cancelInteractOnLeftClick) {
                        event.setCancelled(true);
                    }
                }
            } else if (!wand.isUpgrade() && wand.getLeftClickAction() != WandAction.NONE && cancelInteractOnLeftClick) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            return;
        }

        controller.onPreLogin(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        // Automatically re-activate mages.
        Player player = event.getPlayer();
        Mage mage = controller.getMage(player);
        // In case of rapid relog, this mage may have been marked for removal already
        mage.setUnloading(false);
        controller.checkVanished(player);
        if (player.hasPermission("Magic.migrate")) {
           controller.checkForMigration(player);
        }
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

            if (logoutDelay > 0) {
                Bukkit.getScheduler().runTaskLater(controller.getPlugin(),
                    new PlayerQuitTask(controller, mage),
                    (int)Math.ceil((double)logoutDelay * 20 / 1000));
            } else {
                controller.playerQuit(mage);
            }
        }

        // Make sure they don't keep any temporary items that may be waiting for undo
        ItemStack[] armor = player.getInventory().getArmorContents();
        for (int i = 0; i < armor.length; i++) {
            ItemStack currentItem = armor[i];
            if (CompatibilityLib.getItemUtils().isTemporary(currentItem)) {
                ItemStack replacement = CompatibilityLib.getItemUtils().getReplacement(currentItem);
                armor[i] = replacement;
                player.getInventory().setArmorContents(armor);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerTeleport(PlayerTeleportEvent event)
    {
        Player player = event.getPlayer();
        if (controller.isNPC(player)) return;
        Mage mage = controller.getRegisteredMage(player);
        if (mage != null) {
            mage.onTeleport(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event)
    {
        Player player = event.getPlayer();
        if (controller.isNPC(player)) return;
        Mage mage = controller.getRegisteredMage(player);
        if (mage != null) {
            mage.onChangeWorld();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event)
    {
        Player player = event.getPlayer();
        if (!player.isOnline()) return;
        Mage mage = controller.getRegisteredMage(player);
        if (mage != null) {
            mage.onGameModeChange(player.getGameMode(), event.getNewGameMode());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    @SuppressWarnings("deprecation")
    public void onPlayerPickupItem(org.bukkit.event.player.PlayerPickupItemEvent event)
    {
        // TODO: Change to EntityPickupItemEvent
        Player player = event.getPlayer();
        if (player.isDead()) {
            controller.info("Player picking up item while dead? " + player.getName() + ", cancelling", 5);
            event.setCancelled(true);
            return;
        }
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
    @SuppressWarnings("deprecation")
    public void onPlayerPrePickupItem(org.bukkit.event.player.PlayerPickupItemEvent event)
    {
        // TODO: Change to EntityPickupItemEvent
        Item item = event.getItem();
        ItemStack pickup = item.getItemStack();
        if (CompatibilityLib.getItemUtils().isTemporary(pickup) || CompatibilityLib.getEntityMetadataUtils().getBoolean(item, MagicMetaKeys.TEMPORARY))
        {
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

        // Auto-absorb SP items
        if (autoAbsorbSP && Wand.isSP(pickup)) {
            if (mage.getActiveProperties().addItem(pickup)) {
                event.getItem().remove();
                event.setCancelled(true);
                return;
            }
        }

        // Wands will absorb spells and upgrade items
        Wand activeWand = mage.getActiveWand();
        if (activeWand != null
                && activeWand.isAutoAbsorb()
                && activeWand.isModifiable()
                && (Wand.isSpell(pickup) || Wand.isBrush(pickup) || Wand.isUpgrade(pickup))
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
        Player player = event.getPlayer();
        Mage mage = controller.getMage(player);
        mage.trigger("consume");

        ItemStack originalItem = event.getItem();
        if (!originalItem.hasItemMeta()) return;

        // The item we get passed in this event is a shallow bukkit copy.
        ItemStack item = CompatibilityLib.getItemUtils().makeReal(originalItem);

        String consumeSpell = controller.getWandProperty(item, "consume_spell", "");
        if (!consumeSpell.isEmpty()) {
            Spell spell = mage.getSpell(consumeSpell);
            if (spell != null) {
                boolean success;
                if (Wand.isWand(item)) {
                    Wand wand = Wand.createWand(controller, item);
                    success = wand.cast(spell);
                } else {
                    success = spell.cast();
                }
                if (!success) {
                    event.setCancelled(true);
                } else {
                    // So unfortunately cancelling the event prevents us from being
                    // able to hold right-click to continue consuming more food.
                    // We will instead change the item in the event to air, which will
                    // prevent the default food behavior from applying
                    event.setItem(new ItemStack(Material.AIR));
                    item = originalItem;

                    // However, this also means we have to deal with consuming the item ourselves.
                    // The event will remove it completely since we set it to air,
                    // we need to put it back after the event logic has finished.
                    boolean creative = player.getGameMode() == GameMode.CREATIVE;
                    if (item.getAmount() > 1 || creative) {
                        ItemStack offhandItem = player.getInventory().getItemInOffHand();
                        boolean offhand = item.equals(offhandItem);
                        if (!creative) {
                            item.setAmount(item.getAmount() - 1);
                        }
                        Plugin plugin = controller.getPlugin();
                        plugin.getServer().getScheduler().runTask(plugin, new GiveItemTask(player, item, offhand));
                    }
                }
            }
        }
    }
}
