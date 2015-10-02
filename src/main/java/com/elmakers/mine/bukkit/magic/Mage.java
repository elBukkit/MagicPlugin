package com.elmakers.mine.bukkit.magic;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import com.elmakers.mine.bukkit.action.TeleportTask;
import com.elmakers.mine.bukkit.api.action.GUIAction;
import com.elmakers.mine.bukkit.api.batch.SpellBatch;
import com.elmakers.mine.bukkit.api.data.BrushData;
import com.elmakers.mine.bukkit.api.data.MageData;
import com.elmakers.mine.bukkit.api.data.SpellData;
import com.elmakers.mine.bukkit.api.data.UndoData;
import com.elmakers.mine.bukkit.api.effect.SoundEffect;
import com.elmakers.mine.bukkit.api.spell.SpellKey;
import com.elmakers.mine.bukkit.effect.HoloUtils;
import com.elmakers.mine.bukkit.effect.Hologram;
import com.elmakers.mine.bukkit.spell.ActionSpell;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import com.elmakers.mine.bukkit.utility.NMSUtils;
import de.slikey.effectlib.util.ParticleEffect;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.batch.Batch;
import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.CostReducer;
import com.elmakers.mine.bukkit.api.spell.MageSpell;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellEventType;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.LostWand;
import com.elmakers.mine.bukkit.block.MaterialBrush;
import com.elmakers.mine.bukkit.block.UndoQueue;
import com.elmakers.mine.bukkit.batch.UndoBatch;
import com.elmakers.mine.bukkit.wand.Wand;

public class Mage implements CostReducer, com.elmakers.mine.bukkit.api.magic.Mage {
    protected static int AUTOMATA_ONLINE_TIMEOUT = 5000;
    public static double WAND_LOCATION_OFFSET = 0.5;
    public static double WAND_LOCATION_VERTICAL_OFFSET = 0;
    public static int JUMP_EFFECT_FLIGHT_EXEMPTION_DURATION = 0;
    final static private Set<Material> EMPTY_MATERIAL_SET = new HashSet<Material>();
    private static String defaultMageName = "Mage";

    protected final String id;
    protected ConfigurationSection data = new MemoryConfiguration();
    protected WeakReference<Player> _player;
    protected WeakReference<Entity> _entity;
    protected WeakReference<CommandSender> _commandSender;
    protected boolean hasEntity;
    protected String playerName;
    protected final MagicController controller;
    protected CommandSender debugger;
    protected HashMap<String, MageSpell> spells = new HashMap<String, MageSpell>();
    private Wand activeWand = null;
    private Wand soulWand = null;
    private Map<String, Wand> boundWands = new HashMap<String, Wand>();
    private final Collection<Listener> quitListeners = new HashSet<Listener>();
    private final Collection<Listener> deathListeners = new HashSet<Listener>();
    private final Collection<Listener> damageListeners = new HashSet<Listener>();
    private final Set<MageSpell> activeSpells = new HashSet<MageSpell>();
    private UndoQueue undoQueue = null;
    private LinkedList<Batch> pendingBatches = new LinkedList<Batch>();
    private boolean loading = false;
    private int debugLevel = 0;
    private boolean trackSpellCasts = true;
    private boolean quiet = false;

    private Map<PotionEffectType, Integer> effectivePotionEffects = new HashMap<PotionEffectType, Integer>();
    protected float damageReduction = 0;
    protected float damageReductionPhysical = 0;
    protected float damageReductionProjectiles = 0;
    protected float damageReductionFalling = 0;
    protected float damageReductionFire = 0;
    protected float damageReductionExplosions = 0;

    private Map<Integer, Wand> activeArmor = new HashMap<Integer, Wand>();

    private Location location;
    private float costReduction = 0;
    private float cooldownReduction = 0;
    private long cooldownExpiration = 0;
    private float powerMultiplier = 1;
    private long lastClick = 0;
    private long lastCast = 0;
    private long blockPlaceTimeout = 0;
    private Location lastDeathLocation = null;
    private final MaterialBrush brush;
    private long fallProtection = 0;
    private long fallProtectionCount = 1;
    private BaseSpell fallingSpell = null;

    private boolean isNewPlayer = true;

    private GUIAction gui = null;

    private Hologram hologram;
    private Integer hologramTaskId = null;
    private boolean hologramIsVisible = false;

    private Map<Integer, ItemStack> respawnInventory;
    private Map<Integer, ItemStack> respawnArmor;
    private List<ItemStack> restoreInventory;

    private String destinationWarp;

    public Mage(String id, MagicController controller) {
        this.id = id;
        this.controller = controller;
        this.brush = new MaterialBrush(this, Material.DIRT, (byte) 0);
        _player = new WeakReference<Player>(null);
        _entity = new WeakReference<Entity>(null);
        _commandSender = new WeakReference<CommandSender>(null);
        hasEntity = false;
    }

    public void setCostReduction(float reduction) {
        costReduction = reduction;
    }

    public boolean hasStoredInventory() {
        return activeWand != null && activeWand.hasStoredInventory();
    }

    @Override
    public Set<Spell> getActiveSpells() {
        return new HashSet<Spell>(activeSpells);
    }

    public Inventory getStoredInventory() {
        return activeWand != null ? activeWand.getStoredInventory() : null;
    }

    public void setLocation(Location location) {
        LivingEntity entity = getLivingEntity();
        if (entity != null && location != null) {
            entity.teleport(location);
            return;
        }
        this.location = location;
    }

    public void setLocation(Location location, boolean direction) {
        if (!direction) {
            if (this.location == null) {
                this.location = location;
            } else {
                this.location.setX(location.getX());
                this.location.setY(location.getY());
                this.location.setZ(location.getZ());
            }
        } else {
            this.location = location;
        }
    }

    public void clearCache() {
        if (brush != null) {
            brush.clearSchematic();
        }
    }

    public void setCooldownReduction(float reduction) {
        cooldownReduction = reduction;
    }

    public void setPowerMultiplier(float mutliplier) {
        powerMultiplier = mutliplier;
    }

    public boolean usesMana() {
        return activeWand == null ? false : activeWand.usesMana();
    }

    public boolean addToStoredInventory(ItemStack item) {
        return (activeWand == null ? false : activeWand.addToStoredInventory(item));
    }

    public boolean cancel() {
        boolean result = false;
        for (MageSpell spell : spells.values()) {
            result = spell.cancel() || result;
        }
        return result;
    }

    public void onPlayerQuit(PlayerEvent event) {
        Player player = getPlayer();
        if (player == null || player != event.getPlayer()) {
            return;
        }
        // Must allow listeners to remove themselves during the event!
        List<Listener> active = new ArrayList<Listener>(quitListeners);
        for (Listener listener : active) {
            callEvent(listener, event);
        }
    }

    public void onPlayerDeath(EntityDeathEvent event) {
        Player player = getPlayer();
        if (player == null || player != event.getEntity()) {
            return;
        }
        lastDeathLocation = player.getLocation();
        List<Listener> active = new ArrayList<Listener>(deathListeners);
        for (Listener listener : active) {
            callEvent(listener, event);
        }
    }

    public void onPlayerCombust(EntityCombustEvent event) {
        if (activeWand != null && activeWand.getDamageReductionFire() > 0) {
            event.getEntity().setFireTicks(0);
            event.setCancelled(true);
        }
    }

    protected void callEvent(Listener listener, Event event) {
        for (Method method : listener.getClass().getMethods()) {
            if (method.isAnnotationPresent(EventHandler.class)) {
                Class<? extends Object>[] parameters = method.getParameterTypes();
                if (parameters.length == 1 && parameters[0].isAssignableFrom(event.getClass())) {
                    try {
                        method.invoke(listener, event);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    public void onPlayerDamage(EntityDamageEvent event) {
        Player player = getPlayer();
        if (player == null) {
            return;
        }

        // Send on to any registered spells
        List<Listener> active = new ArrayList<Listener>(damageListeners);
        for (Listener listener : active) {
            callEvent(listener, event);
            if (event.isCancelled()) break;
        }

        EntityDamageEvent.DamageCause cause = event.getCause();
        if (cause == EntityDamageEvent.DamageCause.FALL) {
            if (fallProtectionCount > 0 && fallProtection > 0 && fallProtection > System.currentTimeMillis()) {
                event.setCancelled(true);
                fallProtectionCount--;
                if (fallingSpell != null) {
                    double scale = 1;
                    LivingEntity li = getLivingEntity();
                    if (li != null) {
                        scale = event.getDamage() / li.getMaxHealth();
                    }
                    fallingSpell.playEffects("land", (float)scale);
                }
                if (fallProtectionCount <= 0) {
                    fallProtection = 0;
                    fallingSpell = null;
                }
                return;
            } else {
                fallingSpell = null;
            }
        }

        if (isSuperProtected()) {
            event.setCancelled(true);
            if (player.getFireTicks() > 0) {
                player.setFireTicks(0);
            }
            return;
        }

        if (event.isCancelled()) return;

        // First check for damage reduction
        float reduction = 0;
        reduction = damageReduction * controller.getMaxDamageReduction();
        switch (cause) {
            case CONTACT:
            case ENTITY_ATTACK:
                reduction += damageReductionPhysical * controller.getMaxDamageReductionPhysical();
                break;
            case PROJECTILE:
                reduction += damageReductionProjectiles * controller.getMaxDamageReductionProjectiles();
                break;
            case FALL:
                reduction += damageReductionFalling * controller.getMaxDamageReductionFalling();
                break;
            case FIRE:
            case FIRE_TICK:
            case LAVA:
                // Also put out fire if they have fire protection of any kind.
                if (damageReductionFire > 0 && player.getFireTicks() > 0) {
                    player.setFireTicks(0);
                }
                reduction += damageReductionFire * controller.getMaxDamageReductionFire();
                break;
            case BLOCK_EXPLOSION:
            case ENTITY_EXPLOSION:
                reduction += damageReductionExplosions * controller.getMaxDamageReductionExplosions();
            default:
                break;
        }
        if (reduction >= 1) {
            event.setCancelled(true);
            return;
        }

        if (reduction > 0) {
            double newDamage = (1.0f - reduction) * event.getDamage();
            if (newDamage <= 0) newDamage = 0.1;
            event.setDamage(newDamage);
        }
    }

    public void setActiveWand(Wand activeWand) {
        // Avoid deactivating a wand by mistake, and avoid infinite recursion on null!
        if (this.activeWand == activeWand) return;
        this.activeWand = activeWand;
        if (activeWand != null && activeWand.isBound() && activeWand.canUse(getPlayer())) {
            String template = activeWand.getTemplateKey();
            if (template != null && !template.isEmpty()) {
                boundWands.put(template, activeWand);
            }
        }
        blockPlaceTimeout = System.currentTimeMillis() + 200;
        updateEquipmentEffects();
    }

    public long getBlockPlaceTimeout() {
        return blockPlaceTimeout;
    }

    /**
     * Send a message to this Mage when a spell is cast.
     *
     * @param message The message to send
     */
    public void castMessage(String message) {
        if (message == null || message.length() == 0 || quiet) return;

        CommandSender sender = getCommandSender();
        if (sender != null && controller.showCastMessages() && controller.showMessages()) {
            sender.sendMessage(controller.getCastMessagePrefix() + message);
        }
    }

    /**
     * Send a message to this Mage.
     * <p/>
     * Use this to send messages to the player that are important.
     *
     * @param message The message to send
     */
    public void sendMessage(String message) {
        if (message == null || message.length() == 0 || quiet) return;

        CommandSender sender = getCommandSender();
        if (sender != null && controller.showMessages()) {
            sender.sendMessage(controller.getMessagePrefix() + message);
        }
    }

    public void clearBuildingMaterial() {
        brush.setMaterial(controller.getDefaultMaterial(), (byte) 1);
    }

    @Override
    public void playSoundEffect(SoundEffect soundEffect) {
        if (!controller.soundsEnabled() || soundEffect == null) return;

        soundEffect.play(controller.getPlugin(), getEntity());
    }

    public UndoQueue getUndoQueue() {
        if (undoQueue == null) {
            undoQueue = new UndoQueue(this);
            undoQueue.setMaxSize(controller.getUndoQueueDepth());
        }
        return undoQueue;
    }

    @Override
    public UndoList getLastUndoList() {
        if (undoQueue == null || undoQueue.isEmpty()) return null;
        return undoQueue.getLast();
    }

    public boolean prepareForUndo(com.elmakers.mine.bukkit.api.block.UndoList undoList) {
        if (undoList == null) return false;
        if (undoList.bypass()) return true;
        UndoQueue queue = getUndoQueue();
        queue.add(undoList);
        return true;
    }

    public boolean registerForUndo(com.elmakers.mine.bukkit.api.block.UndoList undoList) {
        if (!prepareForUndo(undoList)) return false;

        int autoUndo = controller.getAutoUndoInterval();
        if (autoUndo > 0 && undoList.getScheduledUndo() == 0) {
            undoList.setScheduleUndo(autoUndo);
        } else {
            undoList.updateScheduledUndo();
        }

        if (undoList.isScheduled())
        {
            controller.scheduleUndo(undoList);
        }

        return true;
    }

    @Override
    public void addUndoBatch(com.elmakers.mine.bukkit.api.batch.UndoBatch batch) {
        pendingBatches.addLast(batch);
        controller.addPending(this);
    }

    protected void setPlayer(Player player) {
        if (player != null) {
            playerName = player.getName();
            this._player = new WeakReference<Player>(player);
            this._entity = new WeakReference<Entity>(player);
            this._commandSender = new WeakReference<CommandSender>(player);
            hasEntity = true;
        } else {
            this._player.clear();
            this._entity.clear();
            this._commandSender.clear();
            hasEntity = false;
        }
    }

    protected void setEntity(Entity entity) {
        if (entity != null) {
            playerName = entity.getType().name().toLowerCase().replace("_", " ");
            if (entity instanceof LivingEntity) {
                LivingEntity li = (LivingEntity) entity;
                String customName = li.getCustomName();
                if (customName != null && customName.length() > 0) {
                    playerName = customName;
                }
            }
            this._entity = new WeakReference<Entity>(entity);
            hasEntity = true;
        } else {
            this._entity.clear();
            hasEntity = false;
        }
    }

    protected void setCommandSender(CommandSender sender) {
        if (sender != null) {
            this._commandSender = new WeakReference<CommandSender>(sender);

            if (sender instanceof BlockCommandSender) {
                BlockCommandSender commandBlock = (BlockCommandSender) sender;
                playerName = commandBlock.getName();
                Location location = getLocation();
                if (location == null) {
                    location = commandBlock.getBlock().getLocation();
                } else {
                    Location blockLocation = commandBlock.getBlock().getLocation();
                    location.setX(blockLocation.getX());
                    location.setY(blockLocation.getY());
                    location.setZ(blockLocation.getZ());
                }
                setLocation(location, false);
            } else {
                setLocation(null);
            }
        } else {
            this._commandSender.clear();
            setLocation(null);
        }
    }

    protected void onLoad(MageData data) {
        loading = false;
        Player player = getPlayer();
        if (player != null) {
            if (restoreInventory != null) {
                controller.getLogger().info("Restoring saved inventory for player " + player.getName() + " - did the server not shut down properly?");
                if (activeWand != null) {
                    activeWand.deactivate();
                }
                Inventory inventory = player.getInventory();
                for (int slot = 0; slot < restoreInventory.size(); slot++) {
                    Object item = restoreInventory.get(slot);
                    if (item instanceof ItemStack) {
                        inventory.setItem(slot, (ItemStack)item);
                    } else {
                        inventory.setItem(slot, null);
                    }
                }
                restoreInventory = null;
            }

            Collection<SpellData> spellDataList = data == null ? null : data.getSpellData();
            if (spellDataList != null) {
                for (SpellData spellData : spellDataList) {
                    Spell spell = spells.get(spellData.getKey());
                    if (spell != null && spell instanceof MageSpell) {
                        ((MageSpell)spell).load(spellData);
                    }
                }
            }

            if (activeWand == null) {
                String welcomeWand = controller.getWelcomeWand();
                Wand wand = Wand.getActiveWand(controller, player);
                if (wand != null) {
                    wand.activate(this);
                } else if (isNewPlayer && welcomeWand.length() > 0) {
                    isNewPlayer = false;
                    wand = Wand.createWand(controller, welcomeWand);
                    if (wand != null) {
                        wand.takeOwnership(player, false, false);
                        giveItem(wand.getItem());
                        controller.getLogger().info("Gave welcome wand " + wand.getName() + " to " + player.getName());
                    } else {
                        controller.getLogger().warning("Unable to give welcome wand '" + welcomeWand + "' to " + player.getName());
                    }
                }
            }
        }
    }

    protected void finishLoad(MageData data) {
        MageLoadTask loadTask = new MageLoadTask(this, data);
        Bukkit.getScheduler().scheduleSyncDelayedTask(controller.getPlugin(), loadTask, 1);
    }

    protected boolean load(MageData data) {
        try {
            if (data == null) {
                finishLoad(data);
                return true;
            }

            boundWands.clear();
            Map<String, ItemStack> boundWandItems = data.getBoundWands();
            if (boundWandItems != null) {
                for (ItemStack boundWandItem : boundWandItems.values()) {
                    Wand boundWand = new Wand(controller, boundWandItem);
                    boundWands.put(boundWand.getTemplateKey(), boundWand);
                }
            }
            this.data = data.getExtraData();
            com.elmakers.mine.bukkit.api.wand.Wand apiWand = data.getSoulWand();
            if (apiWand instanceof Wand) {
                this.soulWand = (Wand)apiWand;
            }

            cooldownExpiration = data.getCooldownExpiration();
            fallProtectionCount = data.getFallProtectionCount();
            fallProtection = data.getFallProtectionDuration();
            if (fallProtectionCount > 0 && fallProtection > 0) {
                fallProtection = System.currentTimeMillis() + fallProtection;
            }

            isNewPlayer = false;
            playerName = data.getName();
            lastDeathLocation = data.getLastDeathLocation();
            location = data.getLocation();
            lastCast = data.getLastCast();
            destinationWarp = data.getDestinationWarp();
            if (destinationWarp != null) {
                if (!destinationWarp.isEmpty()) {
                    Location destination = controller.getWarp(destinationWarp);
                    if (destination != null) {
                        Plugin plugin = controller.getPlugin();
                        controller.info("Warping " + getEntity().getName() + " to " + destinationWarp + " on login");
                        TeleportTask task = new TeleportTask(getController(), getEntity(), destination, 4, null);
                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, task, 1);
                    } else {
                        controller.info("Failed to warp " + getEntity().getName() + " to " + destinationWarp + " on login, warp doesn't exist");
                    }
                }
                destinationWarp = null;
            }

            getUndoQueue().load(data.getUndoData());

            Collection<SpellData> spellData = data.getSpellData();
            if (spellData != null) {
                for (SpellData spell : spellData) {
                    // Spell data is loaded in onLoad to ensure it happens in the main thread
                    createSpell(spell.getKey());
                }
            }

            respawnInventory = data.getRespawnInventory();
            respawnArmor = data.getRespawnArmor();

            BrushData brushData = data.getBrushData();
            if (brushData != null) {
                brush.load(brushData);
            }

            restoreInventory = data.getStoredInventory();
        } catch (Exception ex) {
            controller.getLogger().warning("Failed to load player data for " + playerName + ": " + ex.getMessage());
            return false;
        }

        finishLoad(data);
        return true;
    }

    @Override
    public boolean save(MageData data) {
        try {
            data.setName(getName());
            data.setId(getId());
            data.setLastCast(lastCast);
            data.setLastDeathLocation(lastDeathLocation);
            data.setLocation(location);
            data.setDestinationWarp(destinationWarp);
            data.setCooldownExpiration(cooldownExpiration);
            long now = System.currentTimeMillis();

            if (fallProtectionCount > 0 && fallProtection > now) {
                data.setFallProtectionCount(fallProtectionCount);
                data.setFallProtectionDuration(fallProtection - now);
            } else {
                data.setFallProtectionCount(0);
                data.setFallProtectionDuration(0);
            }

            BrushData brushData = new BrushData();
            brush.save(brushData);
            data.setBrushData(brushData);
            UndoData undoData = new UndoData();
            getUndoQueue().save(undoData);
            data.setUndoData(undoData);

            List<SpellData> spellDataList = new ArrayList<SpellData>();
            for (MageSpell spell : spells.values()) {
                SpellData spellData = new SpellData(spell.getKey());
                spell.save(spellData);
                spellDataList.add(spellData);
            }
            data.setSpellData(spellDataList);

            if (boundWands.size() > 0) {
                Map<String, ItemStack> wandItems = new HashMap<String, ItemStack>();
                for (Map.Entry<String, Wand> wandEntry : boundWands.entrySet()) {
                    wandItems.put(wandEntry.getKey(), wandEntry.getValue().getItem());
                }
                data.setBoundWands(wandItems);
            }
            data.setSoulWand(soulWand);
            data.setRespawnArmor(respawnArmor);
            data.setRespawnInventory(respawnInventory);
            if (activeWand != null && activeWand.hasStoredInventory()) {
                data.setStoredInventory(Arrays.asList(activeWand.getStoredInventory().getContents()));
            }
            data.setExtraData(this.data);
        } catch (Exception ex) {
            controller.getPlugin().getLogger().log(Level.WARNING, "Failed to save player data for " + playerName, ex);
            return false;
        }
        return true;
    }

    public boolean checkLastClick(long maxInterval) {
        long now = System.currentTimeMillis();
        long previous = lastClick;
        lastClick = now;
        return (previous <= 0 || previous + maxInterval < now);
    }

    protected void removeActiveEffects() {
        LivingEntity entity = getLivingEntity();
        if (entity == null) return;

        Collection<PotionEffect> activeEffects = entity.getActivePotionEffects();
        for (PotionEffect effect : activeEffects)
        {
            if (effect.getDuration() > Integer.MAX_VALUE / 2)
            {
                entity.removePotionEffect(effect.getType());
            }
        }
    }

    public Wand checkWand(ItemStack itemInHand) {
        Player player = getPlayer();
        if (isLoading() || player == null) return null;

        String itemId = Wand.getWandId(itemInHand);
        String activeId = activeWand != null ? activeWand.getId() : null;

        if ((itemId != null && activeId == null)
                || (activeId != null && itemId == null)
                || (itemId != null && activeId != null && !itemId.equals(activeId))
                )
        {
            if (activeWand != null) {
                activeWand.deactivate();
            }
            if (itemInHand != null && itemId != null) {
                Wand newActiveWand = new Wand(controller, itemInHand);
                newActiveWand.activate(this);
            }
        }

        return activeWand;
    }

    @Override
    public Wand checkWand() {
        Player player = getPlayer();
        if (isLoading() || player == null) return null;
        return checkWand(player.getItemInHand());
    }

    // This gets called every second (or so - 20 ticks)
    protected void tick() {
        Player player = getPlayer();

        // We don't tick non-player or offline Mages
        if (player != null && player.isOnline()) {
            checkWand();
            if (activeWand != null) {
                activeWand.tick();
            }

            // Avoid getting kicked for large jump effects
            // It'd be nice to filter this by amplitude, but as
            // it turns out that is not easy to check efficiently.
            if (JUMP_EFFECT_FLIGHT_EXEMPTION_DURATION > 0 && player.hasPotionEffect(PotionEffectType.JUMP))
            {
                controller.addFlightExemption(player, JUMP_EFFECT_FLIGHT_EXEMPTION_DURATION);
            }

            for (Wand armorWand : activeArmor.values())
            {
                armorWand.updateEffects(this);
            }

            // Copy this set since spells may get removed while iterating!
            List<MageSpell> active = new ArrayList<MageSpell>(activeSpells);
            for (MageSpell spell : active) {
                spell.tick();
                if (!spell.isActive()) {
                    deactivateSpell(spell);
                }
            }
        }
    }

    public int processPendingBatches(int maxBlockUpdates) {
        int updated = 0;
        if (pendingBatches.size() > 0) {
            List<Batch> processBatches = new ArrayList<Batch>(pendingBatches);
            pendingBatches.clear();
            for (Batch batch : processBatches) {
                if (updated < maxBlockUpdates) {
                    int batchUpdated = batch.process(maxBlockUpdates - updated);
                    updated += batchUpdated;
                }
                if (!batch.isFinished()) {
                    pendingBatches.add(batch);
                }
            }
        }

        if (pendingBatches.size() == 0) {
            controller.removePending(this);
        }
        return updated;
    }

    public void setLastHeldMapId(int mapId) {
        brush.setMapId(mapId);
    }

    protected void loadSpells(Map<String, ConfigurationSection> spellConfiguration) {
        if (spellConfiguration == null) return;

        Collection<MageSpell> currentSpells = new ArrayList<MageSpell>(spells.values());
        for (MageSpell spell : currentSpells) {
            String key = spell.getKey();
            if (spellConfiguration.containsKey(key)) {
                ConfigurationSection template = spellConfiguration.get(key);
                String className = template.getString("class");
                if (className == null)
                {
                    className = ActionSpell.class.getName();
                }
                // Check for spells that have changed class
                if (!spell.getClass().getName().contains(className)) {
                    SpellData spellData = new SpellData(key);
                    spell.save(spellData);
                    spells.remove(key);
                    Spell newSpell = getSpell(key);
                    if (newSpell != null && newSpell instanceof MageSpell) {
                        ((MageSpell)newSpell).load(spellData);
                    }
                } else {
                    spell.loadTemplate(key, template);
                }
            } else {
                spells.remove(key);
            }
        }
    }

    public boolean isNewPlayer() {
        return this.isNewPlayer;
    }

    public void clearNewPlayer() {
        this.isNewPlayer = false;
    }

	/*
	 * API Implementation
	 */

    @Override
    public Collection<Batch> getPendingBatches() {
        Collection<Batch> pending = new ArrayList<Batch>();
        pending.addAll(pendingBatches);
        return pending;
    }

    @Override
    public String getName() {
        return playerName == null || playerName.length() == 0 ? defaultMageName : playerName;
    }

    public String getDisplayName() {
        Entity entity = getEntity();
        if (entity == null) {
            return getName();
        }

        if (entity instanceof Player) {
            return ((Player)entity).getDisplayName();
        }

        return controller.getEntityDisplayName(entity);
    }

    public void setName(String name) {
        playerName = name;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Location getLocation() {
        if (location != null) return location.clone();

        LivingEntity livingEntity = getLivingEntity();
        if (livingEntity == null) return null;
        return livingEntity.getLocation();
    }

    @Override
    public Location getEyeLocation() {
        Entity entity = getEntity();
        if (entity != null) {
            return CompatibilityUtils.getEyeLocation(entity);
        }

        return getLocation();
    }

    @Override
    public Location getWandLocation() {
        Location wandLocation = getEyeLocation();
        if (wandLocation == null) {
            return null;
        }
        Location toTheRight = wandLocation.clone();
        toTheRight.setYaw(toTheRight.getYaw() + 90);
        Vector wandDirection = toTheRight.getDirection();
        wandLocation = wandLocation.clone();
        wandLocation.add(wandDirection.multiply(WAND_LOCATION_OFFSET));
        wandLocation.setY(wandLocation.getY() + WAND_LOCATION_VERTICAL_OFFSET);
        return wandLocation;
    }

    @Override
    public Vector getDirection() {
        Location location = getLocation();
        if (location != null) {
            return location.getDirection();
        }
        return new Vector(0, 1, 0);
    }

    @Override
    public UndoList undo(Block target) {
        return getUndoQueue().undo(target);
    }

    @Override
    public Batch cancelPending() {
        return cancelPending(null, true);
    }

    @Override
    public Batch cancelPending(String spellKey) {
        return cancelPending(spellKey, true);
    }

    @Override
    public Batch cancelPending(boolean force) {
        return cancelPending(null, force);
    }

    public Batch cancelPending(String spellKey, boolean force) {
        Batch stoppedPending = null;
        if (pendingBatches.size() > 0) {
            List<Batch> batches = new ArrayList<Batch>();
            batches.addAll(pendingBatches);
            for (Batch batch : batches) {
                if (spellKey != null || !force) {
                    if (!(batch instanceof SpellBatch)) {
                        continue;
                    }
                    SpellBatch spellBatch = (SpellBatch)batch;
                    Spell spell = spellBatch.getSpell();
                    if (spell == null) {
                        continue;
                    }
                    if (!force && !spell.isCancellable()) {
                        continue;
                    }
                    if (spellKey != null && !spell.getSpellKey().getBaseKey().equalsIgnoreCase(spellKey)) {
                        continue;
                    }
                }
                if (!(batch instanceof UndoBatch)) {
                    batch.finish();
                    pendingBatches.remove(batch);
                    stoppedPending = batch;
                }
            }
        }
        return stoppedPending;
    }

    @Override
    public UndoList undo() {
        return getUndoQueue().undo();
    }

    @Override
    public boolean commit() {
        return getUndoQueue().commit();
    }

    public boolean hasCastPermission(Spell spell) {
        return spell.hasCastPermission(getCommandSender());
    }

    @Override
    public MageSpell getSpell(String key) {
        if (loading) return null;

        MageSpell playerSpell = spells.get(key);
        if (playerSpell == null) {
            playerSpell = createSpell(key);
        } else {
            playerSpell.setMage(this);
        }

        return playerSpell;
    }

    protected MageSpell createSpell(String key) {
        MageSpell playerSpell = spells.get(key);
        if (playerSpell != null) {
            playerSpell.setMage(this);
            return playerSpell;
        }
        SpellTemplate spellTemplate = controller.getSpellTemplate(key);
        if (spellTemplate == null) return null;
        Spell newSpell = spellTemplate.createSpell();
        if (newSpell == null || !(newSpell instanceof MageSpell)) return null;
        playerSpell = (MageSpell)newSpell;
        spells.put(newSpell.getKey(), playerSpell);

        SpellKey baseKey = newSpell.getSpellKey();
        SpellKey upgradeKey = new SpellKey(baseKey.getBaseKey(), baseKey.getLevel() + 1);
        Spell upgradeSpell = createSpell(upgradeKey.getKey());
        if (upgradeSpell instanceof MageSpell)
        {
            playerSpell.setUpgrade((MageSpell)upgradeSpell);
        }
        playerSpell.setMage(this);
        return playerSpell;
    }

    @Override
    public Collection<Spell> getSpells() {
        List<Spell> export = new ArrayList<Spell>(spells.values());
        return export;
    }

    @Override
    public void activateSpell(Spell spell) {
        if (spell instanceof MageSpell) {
            MageSpell mageSpell = ((MageSpell) spell);
            activeSpells.add(mageSpell);
            mageSpell.setActive(true);
        }
    }

    @Override
    public void deactivateSpell(Spell spell) {
        activeSpells.remove(spell);

        // If this was called by the Spell itself, the following
        // should do nothing as the spell is already marked as inactive.
        if (spell instanceof MageSpell) {
            ((MageSpell) spell).deactivate();
        }
    }

    @Override
    public void deactivateAllSpells() {
        deactivateAllSpells(false, false);
    }

    @Override
    public void deactivateAllSpells(boolean force, boolean quiet) {
        // Copy this set since spells will get removed while iterating!
        List<MageSpell> active = new ArrayList<MageSpell>(activeSpells);
        for (MageSpell spell : active) {
            if (spell.deactivate(force, quiet)) {
                activeSpells.remove(spell);
            }
        }

        // This is mainly here to prevent multi-wand spamming and for
        // Disarm to be more powerful.. because Disarm needs to be more
        // powerful :|
        cancelPending(false);
    }

    @Override
    public boolean isCostFree() {
        // Special case for command blocks and Automata
        if (getPlayer() == null) return true;
        return activeWand == null ? false : activeWand.isCostFree();
    }

    @Override
    public boolean isSuperProtected() {
        return activeWand != null && activeWand.isSuperProtected();
    }

    @Override
    public boolean isSuperPowered() {
        return activeWand != null && activeWand.isSuperPowered();
    }

    @Override
    public float getCostReduction() {
        return activeWand == null ? costReduction + controller.getCostReduction() : activeWand.getCostReduction() + costReduction;
    }

    @Override
    public float getCostScale() {
        return 1;
    }

    @Override
    public float getCooldownReduction() {
        return activeWand == null ? cooldownReduction + controller.getCooldownReduction() : activeWand.getCooldownReduction() + cooldownReduction;
    }

    @Override
    public boolean isCooldownFree() {
        return activeWand == null ? false : activeWand.isCooldownFree();
    }

    @Override
    public long getRemainingCooldown() {
        long remaining = 0;
        if (cooldownExpiration > 0)
        {
            long now = System.currentTimeMillis();
            if (cooldownExpiration > now) {
                remaining = cooldownExpiration - now;
            } else {
                cooldownExpiration = 0;
            }
        }

        return remaining;
    }

    @Override
    public void clearCooldown() {
        cooldownExpiration = 0;
    }

    @Override
    public void setRemainingCooldown(long ms) {
        cooldownExpiration = Math.max(ms + System.currentTimeMillis(), cooldownExpiration);
    }

    @Override
    public Color getEffectColor() {
        if (activeWand == null) return null;
        return activeWand.getEffectColor();
    }

    @Override
    public String getEffectParticleName() {
        if (activeWand == null) return null;
        ParticleEffect particle = activeWand.getEffectParticle();
        return particle == null ? null : particle.name();
    }

    @Override
    public void onCast(Spell spell, SpellResult result) {
        lastCast = System.currentTimeMillis();
        if (spell != null) {
            // Notify controller of successful casts,
            // this if for dynmap display or other global-level processing.
            controller.onCast(this, spell, result);
        }
    }

    @Override
    public float getPower() {
        float power = Math.min(controller.getMaxPower(), activeWand == null ? 0 : activeWand.getPower());
        return power * powerMultiplier;
    }

    @Override
    public boolean isRestricted(Material material) {
        Player player = getPlayer();
        if (player != null && player.hasPermission("Magic.bypass_restricted")) return false;
        return controller.isRestricted(material);
    }

    @Override
    public MageController getController() {
        return controller;
    }

    @Override
    public Set<Material> getRestrictedMaterials() {
        if (isSuperPowered()) {
            return EMPTY_MATERIAL_SET;
        }
        return controller.getRestrictedMaterials();
    }

    @Override
    public boolean isPVPAllowed(Location location) {
        return controller.isPVPAllowed(getPlayer(), location == null ? getLocation() : location);
    }

    @Override
    public boolean hasBuildPermission(Block block) {
        return controller.hasBuildPermission(getPlayer(), block);
    }

    @Override
    public boolean hasBreakPermission(Block block) {
        return controller.hasBreakPermission(getPlayer(), block);
    }

    @Override
    public boolean isIndestructible(Block block) {
        return controller.isIndestructible(block);
    }

    @Override
    public boolean isDestructible(Block block) {
        return controller.isDestructible(block);
    }

    @Override
    public boolean isDead() {
        LivingEntity entity = getLivingEntity();
        if (entity != null) {
            return entity.isDead();
        }
        // Check for automata
        CommandSender sender = getCommandSender();
        if (sender == null || !(sender instanceof BlockCommandSender)) return true;
        BlockCommandSender commandBlock = (BlockCommandSender) sender;
        Block block = commandBlock.getBlock();
        if (!block.getChunk().isLoaded()) return true;
        return (block.getType() != Material.COMMAND);
    }

    @Override
    public boolean isOnline() {
        Player player = getPlayer();
        if (player != null) {
            return player.isOnline();
        }
        // Check for automata
        CommandSender sender = getCommandSender();
        if (sender == null || !(sender instanceof BlockCommandSender)) return true;
        return lastCast > System.currentTimeMillis() - AUTOMATA_ONLINE_TIMEOUT;
    }

    @Override
    public boolean isPlayer() {
        Player player = getPlayer();
        return player != null;
    }

    @Override
    public boolean hasLocation() {
        return getLocation() != null;
    }

    @Override
    public Inventory getInventory() {
        if (hasStoredInventory()) {
            return getStoredInventory();
        }

        Player player = getPlayer();
        if (player != null) {
            return player.getInventory();
        }
        // TODO: Maybe wrap EntityEquipment in an Inventory... ? Could be hacky.
        return null;
    }

    @Override
    public Wand getSoulWand() {
        if (soulWand == null) {
            soulWand = new Wand(controller);
        }
        return soulWand;
    }

    @Override
    public Wand getActiveWand() {
        return activeWand;
    }

    @Override
    public com.elmakers.mine.bukkit.api.block.MaterialBrush getBrush() {
        return brush;
    }

    @Override
    public float getDamageMultiplier() {
        float maxPowerMultiplier = controller.getMaxDamagePowerMultiplier() - 1;
        return 1 + (maxPowerMultiplier * getPower());
    }

    @Override
    public float getRangeMultiplier() {
        if (activeWand == null) return 1;

        float maxPowerMultiplier = controller.getMaxRangePowerMultiplier() - 1;
        float maxPowerMultiplierMax = controller.getMaxRangePowerMultiplierMax();
        float multiplier = 1 + (maxPowerMultiplier * getPower());
        return Math.min(multiplier, maxPowerMultiplierMax);
    }

    @Override
    public float getConstructionMultiplier() {
        float maxPowerMultiplier = controller.getMaxConstructionPowerMultiplier() - 1;
        return 1 + (maxPowerMultiplier * getPower());
    }

    @Override
    public float getRadiusMultiplier() {
        if (activeWand == null) return 1;

        float maxPowerMultiplier = controller.getMaxRadiusPowerMultiplier() - 1;
        float maxPowerMultiplierMax = controller.getMaxRadiusPowerMultiplierMax();
        float multiplier = 1 + (maxPowerMultiplier * getPower());
        return Math.min(multiplier, maxPowerMultiplierMax);
    }

    @Override
    public float getMana() {
        return activeWand == null ? 0 : activeWand.getMana();
    }

    @Override
    public void removeMana(float mana) {
        if (activeWand != null) {
            activeWand.removeMana(mana);
        }
    }

    @Override
    public void removeExperience(int xp) {
        Player player = getPlayer();
        if (player == null) return;

        float expProgress = player.getExp();
        int expLevel = player.getLevel();
        while ((expProgress > 0 || expLevel > 0) && xp > 0) {
            if (expProgress > 0) {
                int expAtLevel = (int) (expProgress * (player.getExpToLevel()));
                if (expAtLevel > xp) {
                    expAtLevel -= xp;
                    xp = 0;
                    expProgress = (float) expAtLevel / (float)Wand.getExpToLevel(expLevel);
                } else {
                    expProgress = 0;
                    xp -= expAtLevel;
                }
            } else {
                xp -= Wand.getExpToLevel(expLevel - 1);
                expLevel--;
                if (xp < 0) {
                    expProgress = (float) (-xp) / Wand.getExpToLevel(expLevel);
                    xp = 0;
                }
            }
        }

        player.setExp(Math.max(0, Math.min(1.0f, expProgress)));
        player.setLevel(Math.max(0, expLevel));

        if (activeWand != null) {
            activeWand.updateExperience();
        }
    }

    @Override
    public int getLevel() {
        if (activeWand != null && activeWand.usesMana() && activeWand.displayManaAsXp() && !Wand.retainLevelDisplay) {
            return activeWand.getStoredXpLevel();
        }

        Player player = getPlayer();
        if (player != null) {
            return player.getLevel();
        }

        return 0;
    }

    @Override
    public void setLevel(int level) {
        Player player = getPlayer();
        if (player != null) {
            player.setLevel(level);
        }
        if (activeWand != null && activeWand.usesMana() && activeWand.displayManaAsXp()) {
            activeWand.setStoredXpLevel(level);
        }
    }

    @Override
    public int getExperience() {
        Player player = getPlayer();
        if (player == null) return 0;

        int xp = 0;
        float expProgress = player.getExp();
        int expLevel = player.getLevel();

        if (activeWand != null && activeWand.usesMana() && activeWand.displayManaAsXp()) {
            expLevel = activeWand.getStoredXpLevel();
            expProgress = activeWand.getStoredXpProgress();
        }

        return Wand.getExperience(expLevel, expProgress);
    }

    @Override
    public void giveExperience(int xp) {
        if (activeWand != null && activeWand.usesMana() && activeWand.displayManaAsXp()) {
            activeWand.addExperience(xp);
        }

        Player player = getPlayer();
        if (player != null) {
            player.giveExp(xp);
        }
    }

    @Override
    public boolean addBatch(Batch batch) {
        if (pendingBatches.size() >= controller.getPendingQueueDepth()) {
            controller.getLogger().info("Rejected spell batch for " + getName() + ", already has " + pendingBatches.size()
                    + " pending, limit: " + controller.getPendingQueueDepth());

            return false;
        }
        pendingBatches.addLast(batch);
        controller.addPending(this);
        return true;
    }

    @Override
    public void registerEvent(SpellEventType type, Listener spell) {
        switch (type) {
            case PLAYER_QUIT:
                if (!quitListeners.contains(spell))
                    quitListeners.add(spell);
                break;
            case PLAYER_DAMAGE:
                if (!damageListeners.contains(spell))
                    damageListeners.add(spell);
                break;
            case PLAYER_DEATH:
                if (!deathListeners.contains(spell))
                    deathListeners.add(spell);
                break;
        }
    }

    @Override
    public void unregisterEvent(SpellEventType type, Listener spell) {
        switch (type) {
            case PLAYER_DAMAGE:
                damageListeners.remove(spell);
                break;
            case PLAYER_QUIT:
                quitListeners.remove(spell);
                break;
            case PLAYER_DEATH:
                deathListeners.remove(spell);
                break;
        }
    }

    @Override
    public Player getPlayer() {
        return _player.get();
    }

    @Override
    public Entity getEntity() {
        return _entity.get();
    }

    @Override
    public LivingEntity getLivingEntity() {
        Entity entity = _entity.get();
        return (entity != null && entity instanceof LivingEntity) ? (LivingEntity) entity : null;
    }

    @Override
    public CommandSender getCommandSender() {
        return _commandSender.get();
    }

    @Override
    public List<LostWand> getLostWands() {
        Entity entity = getEntity();
        Collection<LostWand> allWands = controller.getLostWands();
        List<LostWand> mageWands = new ArrayList<LostWand>();

        if (entity == null) {
            return mageWands;
        }

        String playerId = entity.getUniqueId().toString();
        for (LostWand lostWand : allWands) {
            String owner = lostWand.getOwnerId();
            if (owner != null && owner.equals(playerId)) {
                mageWands.add(lostWand);
            }
        }
        return mageWands;
    }

    @Override
    public Location getLastDeathLocation() {
        return lastDeathLocation;
    }

    public void showHoloText(Location location, String text, int duration) {
        // TODO: Broadcast
        if (!isPlayer()) return;
        final Player player = getPlayer();

        if (hologram == null) {
            hologram = HoloUtils.createHoloText(location, text);
        } else {
            if (hologramIsVisible) {
                hologram.hide(player);
            }
            hologram.teleport(location);
            hologram.setLabel(text);
        }

        hologram.show(player);

        BukkitScheduler scheduler = Bukkit.getScheduler();
        if (hologramTaskId != null) {
            scheduler.cancelTask(hologramTaskId);
        }
        if (duration > 0) {
            scheduler.scheduleSyncDelayedTask(controller.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    hologram.hide(player);
                    hologramIsVisible = false;
                }
            }, duration);
        }
    }

    public void enableFallProtection(int ms, Spell protector)
    {
        enableFallProtection(ms, 1, protector);
    }

    public void enableFallProtection(int ms, int count, Spell protector)
    {
        if (ms <= 0 || count <= 0) return;
        if (protector != null && protector instanceof BaseSpell) {
            this.fallingSpell = (BaseSpell)protector;
        }

        long nextTime = System.currentTimeMillis() + ms;
        if (nextTime > fallProtection) {
            fallProtection = nextTime;
        }
        if (count > fallProtectionCount) {
            fallProtectionCount = count;
        }
    }

    public void enableFallProtection(int ms)
    {
        enableFallProtection(ms, null);
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    public boolean isLoading() {
        return loading;
    }

    public boolean isValid() {
        if (!hasEntity) return true;
        Entity entity = getEntity();

        if (entity == null) return false;
        if (controller.isNPC(entity)) return true;

        if (entity instanceof Player) {
            Player player = (Player)entity;
            return player.isOnline();
        }

        if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)entity;
            return !living.isDead();
        }

        // Automata theoretically handle themselves by sticking around for a while
        // And forcing themselves to be forgotten
        // but maybe some extra safety here would be good?
        return true;
    }

    @Override
    public boolean restoreWand() {
        if (boundWands.size() == 0) return false;
        Player player = getPlayer();
        if (player == null) return false;
        Set<String> foundTemplates = new HashSet<String>();
        ItemStack[] inventory = getInventory().getContents();
        for (ItemStack item : inventory) {
            if (Wand.isWand(item)) {
                Wand tempWand = new Wand(controller, item);
                String template = tempWand.getTemplateKey();
                if (template != null) {
                    foundTemplates.add(template);
                }
            }
        }

        int givenWands = 0;
        for (Map.Entry<String, Wand> wandEntry : boundWands.entrySet()) {
            if (foundTemplates.contains(wandEntry.getKey())) continue;

            givenWands++;
            ItemStack wandItem = wandEntry.getValue().duplicate().getItem();
            wandItem.setAmount(1);
            giveItem(wandItem);
        }
        return givenWands > 0;
    }

    @Override
    public boolean isStealth() {
        if (isSneaking()) return true;
        if (activeWand != null && activeWand.isStealth()) return true;
        return false;
    }

    @Override
    public boolean isSneaking() {
        Player player = getPlayer();
        return (player != null && player.isSneaking());
    }

    @Override
    public ConfigurationSection getData() {
        if (loading) {
            return new MemoryConfiguration();
        }
        return data;
    }

    @Override
    public void activateGUI(GUIAction action, Inventory inventory)
    {
        GUIAction previousGUI = gui;
        Player player = getPlayer();
        gui = action;
        if (player != null)
        {
            controller.disableItemSpawn();
            try {
                player.closeInventory();

                // We have to do this a lot..
                // here it will get cleared on close inventory, potentially
                gui = action;

                if (inventory != null) {
                    player.openInventory(inventory);
                }
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
            controller.enableItemSpawn();
        }
        // Reset this as it may have gotten cleared while closing the previous
        // inventory!
        gui = action;

        if (previousGUI != null)
        {
            previousGUI.deactivated();
        }
    }

    @Override
    public void deactivateGUI()
    {
        activateGUI(null, null);
    }

    @Override
    public GUIAction getActiveGUI()
    {
        return gui;
    }

    @Override
    public int getDebugLevel() {
        return debugLevel;
    }

    @Override
    public void setDebugger(CommandSender sender) {
        this.debugger = sender;
    }

    @Override
    public CommandSender getDebugger() {
        return debugger;
    }

    @Override
    public void setDebugLevel(int debugLevel) {
        this.debugLevel = debugLevel;
    }

    @Override
    public void sendDebugMessage(String message) {
        sendDebugMessage(message, 1);
    }

    @Override
    public void sendDebugMessage(String message, int level) {
        if (debugLevel >= level && message != null && !message.isEmpty()) {
            CommandSender sender = debugger;
            if (sender == null) {
                sender = getCommandSender();
            }
            if (sender != null) {
                sender.sendMessage(controller.getMessagePrefix() + message);
            }
        }
    }

    public void clearRespawnInventories() {
        respawnArmor = null;
        respawnInventory = null;
    }

    public void restoreRespawnInventories() {
        Player player = getPlayer();
        if (player == null) {
            return;
        }
        PlayerInventory inventory = player.getInventory();
        if (respawnArmor != null) {
            ItemStack[] armor = inventory.getArmorContents();
            for (Map.Entry<Integer, ItemStack> entry : respawnArmor.entrySet()) {
                armor[entry.getKey()] = entry.getValue();
            }
            player.getInventory().setArmorContents(armor);
            armorUpdated();
        }
        if (respawnInventory != null) {
            for (Map.Entry<Integer, ItemStack> entry : respawnInventory.entrySet()) {
                inventory.setItem(entry.getKey(), entry.getValue());
            }
        }
        clearRespawnInventories();
        armorUpdated();
    }

    public void addToRespawnInventory(int slot, ItemStack item) {
        if (respawnInventory == null) {
            respawnInventory = new HashMap<Integer, ItemStack>();
        }
        respawnInventory.put(slot, item);
    }

    public void addToRespawnArmor(int slot, ItemStack item) {
        if (respawnArmor == null) {
            respawnArmor = new HashMap<Integer, ItemStack>();
        }
        respawnArmor.put(slot, item);
    }

    public void giveItem(ItemStack itemStack) {

        if (hasStoredInventory()) {
            addToStoredInventory(itemStack);
            return;
        }

        // Check for wand upgrades if appropriate
        Wand activeWand = getActiveWand();
        if (activeWand != null) {
            if (activeWand.addItem(itemStack)) {
                return;
            }
        }

        // Place directly in hand if possible
        Player player = getPlayer();
        if (player == null) return;

        PlayerInventory inventory = player.getInventory();
        ItemStack inHand = inventory.getItemInHand();
        if (inHand == null || inHand.getType() == Material.AIR) {
            inventory.setItemInHand(itemStack);
            // Get the new item reference -
            // it might change when added to an Inventory! :|
            itemStack = inventory.getItemInHand();
            if (Wand.isWand(itemStack)) {
                Wand wand = new Wand(controller, itemStack);
                wand.activate(this);
            } else {
                if (itemStack.getType() == Material.MAP) {
                    setLastHeldMapId(itemStack.getDurability());
                }
            }
        } else {
            HashMap<Integer, ItemStack> returned = player.getInventory().addItem(itemStack);
            if (returned.size() > 0) {
                player.getWorld().dropItem(player.getLocation(), itemStack);
            }
        }
    }

    public void armorUpdated() {
        activeArmor.clear();
        Player player = getPlayer();
        if (player != null)
        {
            ItemStack[] armor = player.getInventory().getArmorContents();
            for (int index = 0; index < armor.length; index++) {
                ItemStack armorItem = armor[index];
                if (Wand.isWand(armorItem)) {
                    activeArmor.put(index, new Wand(controller, armorItem));
                }
            }
        }

        if (activeWand != null) {
            activeWand.armorUpdated();
        }
        updateEquipmentEffects();
    }

    protected void updateEquipmentEffects() {
        damageReduction = 0;
        damageReductionPhysical = 0;
        damageReductionProjectiles = 0;
        damageReductionFalling = 0;
        damageReductionFire = 0;
        damageReductionExplosions = 0;
        effectivePotionEffects.clear();
        if (activeWand != null && !activeWand.isPassive())
        {
            damageReduction += activeWand.getDamageReduction();
            damageReductionPhysical += activeWand.getDamageReductionPhysical();
            damageReductionProjectiles += activeWand.getDamageReductionProjectiles();
            damageReductionFalling += activeWand.getDamageReductionFalling();
            damageReductionFire += activeWand.getDamageReductionFire();
            damageReductionExplosions += activeWand.getDamageReductionExplosions();
            effectivePotionEffects.putAll(activeWand.getPotionEffects());
        }
        for (Wand armorWand : activeArmor.values())
        {
            if (armorWand != null) {
                damageReduction += armorWand.getDamageReduction();
                damageReductionPhysical += armorWand.getDamageReductionPhysical();
                damageReductionProjectiles += armorWand.getDamageReductionProjectiles();
                damageReductionFalling += armorWand.getDamageReductionFalling();
                damageReductionFire += armorWand.getDamageReductionFire();
                damageReductionExplosions += armorWand.getDamageReductionExplosions();
                effectivePotionEffects.putAll(armorWand.getPotionEffects());
            }
        }
        damageReduction = Math.min(damageReduction, 1);
        damageReductionPhysical = Math.min(damageReductionPhysical, 1);
        damageReductionProjectiles = Math.min(damageReductionProjectiles, 1);
        damageReductionFalling = Math.min(damageReductionFalling, 1);
        damageReductionFire = Math.min(damageReductionFire, 1);
        damageReductionExplosions = Math.min(damageReductionExplosions, 1);


        LivingEntity entity = getLivingEntity();
        if (entity != null)
        {
            Collection<PotionEffect> activeEffects = entity.getActivePotionEffects();
            for (PotionEffect effect : activeEffects)
            {
                if (!effectivePotionEffects.containsKey(effect.getType()) && effect.getDuration() > Integer.MAX_VALUE / 4)
                {
                    entity.removePotionEffect(effect.getType());
                }
            }
            for (Map.Entry<PotionEffectType, Integer> effects : effectivePotionEffects.entrySet()) {
                PotionEffect effect = new PotionEffect(effects.getKey(), Integer.MAX_VALUE, effects.getValue(), true);
                CompatibilityUtils.applyPotionEffect(entity, effect);
            }
        }
    }

    public Collection<Wand> getActiveArmor()
    {
        return activeArmor.values();
    }

    @Override
    public void deactivate() {
        // Close the wand inventory to make sure the player's normal inventory gets saved
        if (activeWand != null) {
            activeWand.deactivate();
        }
        deactivateAllSpells(true, true);
        removeActiveEffects();
    }

    @Override
    public void removeItemsWithTag(String tag) {
        Player player = getPlayer();
        if (player == null) return;

        PlayerInventory inventory = player.getInventory();
        ItemStack[] contents = inventory.getContents();
        for (int index = 0; index < contents.length; index++)
        {
            ItemStack item = contents[index];
            if (item != null && item.getType() != Material.AIR && InventoryUtils.hasMeta(item, tag))
            {
                inventory.setItem(index, null);
            }
        }

        boolean modified = false;
        ItemStack[] armor = inventory.getArmorContents();
        for (int index = 0; index < armor.length; index++)
        {
            ItemStack item = armor[index];
            if (item != null && item.getType() != Material.AIR && InventoryUtils.hasMeta(item, tag))
            {
                modified = true;
                armor[index] = null;
            }
        }
        if (modified)
        {
            inventory.setArmorContents(armor);
        }
    }

    @Override
    public void setTrackCasts(boolean track) {
        this.trackSpellCasts = track;
    }

    @Override
    public boolean getTrackCasts() {
        return trackSpellCasts;
    }

    @Override
    public void setQuiet(boolean quiet) {
        this.quiet = quiet;
    }

    @Override
    public boolean isQuiet() {
        return quiet;
    }

    public void setDestinationWarp(String warp) {
        destinationWarp = warp;
    }
}

