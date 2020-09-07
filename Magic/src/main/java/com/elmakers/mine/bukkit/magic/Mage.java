package com.elmakers.mine.bukkit.magic;

import static com.google.common.base.Verify.verifyNotNull;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MainHand;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.action.TeleportTask;
import com.elmakers.mine.bukkit.api.action.GUIAction;
import com.elmakers.mine.bukkit.api.attributes.AttributeProvider;
import com.elmakers.mine.bukkit.api.batch.Batch;
import com.elmakers.mine.bukkit.api.batch.SpellBatch;
import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.data.BrushData;
import com.elmakers.mine.bukkit.api.data.MageData;
import com.elmakers.mine.bukkit.api.data.SpellData;
import com.elmakers.mine.bukkit.api.data.UndoData;
import com.elmakers.mine.bukkit.api.economy.Currency;
import com.elmakers.mine.bukkit.api.effect.SoundEffect;
import com.elmakers.mine.bukkit.api.event.WandActivatedEvent;
import com.elmakers.mine.bukkit.api.event.WandDeactivatedEvent;
import com.elmakers.mine.bukkit.api.magic.CastSourceLocation;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MagicAttribute;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.api.magic.Trigger;
import com.elmakers.mine.bukkit.api.spell.CastingCost;
import com.elmakers.mine.bukkit.api.spell.CostReducer;
import com.elmakers.mine.bukkit.api.spell.MageSpell;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellEventType;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.LostWand;
import com.elmakers.mine.bukkit.api.wand.WandAction;
import com.elmakers.mine.bukkit.api.wand.WandTemplate;
import com.elmakers.mine.bukkit.api.wand.WandUpgradePath;
import com.elmakers.mine.bukkit.batch.UndoBatch;
import com.elmakers.mine.bukkit.block.DefaultMaterials;
import com.elmakers.mine.bukkit.block.MaterialBrush;
import com.elmakers.mine.bukkit.block.UndoQueue;
import com.elmakers.mine.bukkit.effect.HoloUtils;
import com.elmakers.mine.bukkit.effect.Hologram;
import com.elmakers.mine.bukkit.effect.MageEffectContext;
import com.elmakers.mine.bukkit.entity.EntityData;
import com.elmakers.mine.bukkit.heroes.HeroesManager;
import com.elmakers.mine.bukkit.integration.VaultController;
import com.elmakers.mine.bukkit.spell.ActionSpell;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.spell.TriggeredSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import com.elmakers.mine.bukkit.wand.Wand;
import com.elmakers.mine.bukkit.wand.WandManaMode;
import com.elmakers.mine.bukkit.wand.WandMode;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

import de.slikey.effectlib.util.VectorUtils;

public class Mage implements CostReducer, com.elmakers.mine.bukkit.api.magic.Mage {
    protected static int AUTOMATA_ONLINE_TIMEOUT = 5000;
    public static int CHANGE_WORLD_EQUIP_COOLDOWN = 1000;
    public static int JUMP_EFFECT_FLIGHT_EXEMPTION_DURATION = 0;
    public static int OFFHAND_CAST_RANGE = 32;
    public static int OFFHAND_CAST_COOLDOWN = 500;
    public static boolean DEACTIVATE_WAND_ON_WORLD_CHANGE = false;
    public static int DEFAULT_SP = 0;
    public static String DEFAULT_CLASS = "";
    private static String defaultMageName = "Mage";
    private static String SKILL_POINT_KEY = "sp";

    public static CastSourceLocation DEFAULT_CAST_LOCATION = CastSourceLocation.MAINHAND;
    public static Vector DEFAULT_CAST_OFFSET = new Vector(0.5, -0.5, 0);
    public static double SNEAKING_CAST_OFFSET = -0.2;

    private static class DamagedBy {
        private WeakReference<Player> player;
        public double damage;

        public DamagedBy(Player player, double damage) {
            this.player = new WeakReference<>(player);
            this.damage = damage;
        }

        public Entity getEntity() {
            return player.get();
        }
    }

    protected final String id;
    private final @Nonnull MageProperties properties;
    private final Map<String, MageClass> classes = new HashMap<>();
    private final Map<String, MageModifier> modifiers = new HashMap<>();
    private final Set<MageModifier> removeModifiers = new HashSet<>();
    private final Map<String, Double> attributes = new HashMap<>();
    private ConfigurationSection variables;
    private final Map<String, List<TriggeredSpell>> triggers = new HashMap<>();
    private final Set<String> triggeredSpells = new HashSet<>();
    private final Set<String> triggeringSpells = new HashSet<>();
    private final List<TriggeredSpell> processingTriggers = new ArrayList<>();
    private final Map<String, Long> lastTriggers = new HashMap<>();
    protected ConfigurationSection data = new MemoryConfiguration();
    protected Map<String, SpellData> spellData = new HashMap<>();
    protected WeakReference<Player> playerRef;
    protected WeakReference<Entity> entityRef;
    protected WeakReference<CommandSender> commandSenderRef;
    protected boolean hasEntity;
    protected String playerName;
    protected final MagicController controller;
    protected CommandSender debugger;
    protected HashMap<String, MageSpell> spells = new HashMap<>();
    private Wand activeWand = null;
    private Wand offhandWand = null;
    private MageClass activeClass = null;
    private boolean offhandCast = false;
    private Map<String, Wand> boundWands = new HashMap<>();
    private final Collection<Listener> quitListeners = new HashSet<>();
    private final Collection<Listener> deathListeners = new HashSet<>();
    private final Collection<Listener> damageListeners = new HashSet<>();
    private final Set<MageSpell> activeSpells = new HashSet<>();
    private UndoQueue undoQueue = null;
    private Map<String, UndoData> externalUndoData = null;
    private Deque<Batch> pendingBatches = new ConcurrentLinkedDeque<>();
    private boolean loading = false;
    private boolean unloading = false;
    private int debugLevel = 0;
    private boolean quiet = false;
    private EntityData entityData;
    private long lastTick;
    private Location lastLocation;
    private Vector velocity = new Vector();
    private long lastBlockTime;
    private long ignoreItemActivationUntil = 0;
    private boolean forget = false;
    private long disableWandOpenUntil = 0;
    private long created;
    private MageEffectContext effectContext = null;

    private DamagedBy topDamager;
    private DamagedBy lastDamager;
    private Map<UUID, DamagedBy> damagedBy;
    private WeakReference<Entity> lastDamageTarget;
    private Map<Player, MageConversation> conversations = new WeakHashMap<>();

    private Map<PotionEffectType, Integer> effectivePotionEffects = new HashMap<>();
    private Map<String, Double> protection = new HashMap<>();
    private Map<String, Double> weakness = new HashMap<>();
    private Map<String, Double> strength = new HashMap<>();
    private float costReduction = 0;
    private float cooldownReduction = 0;
    private float consumeReduction = 0;
    private float powerMultiplier = 1;
    private float earnMultiplier = 1;

    private boolean costFree = false;
    private boolean cooldownFree = false;

    protected boolean isVanished = false;
    protected long superProtectionExpiration = 0;

    private Map<Integer, Wand> activeArmor = new HashMap<>();

    private Location location;
    private long cooldownExpiration = 0;
    private float magePowerBonus = 0;
    private long lastClick = 0;
    private long lastCast = 0;
    private long lastOffhandCast = 0;
    private long blockPlaceTimeout = 0;
    private Location lastDeathLocation = null;
    private final MaterialBrush brush;
    private long fallProtection = 0;
    private long fallProtectionCount = 1;
    private BaseSpell fallingSpell = null;
    private boolean isAutomaton = false;

    private boolean gaveWelcomeWand = false;

    private GUIAction gui = null;

    private Hologram hologram;
    private boolean hologramIsVisible = false;
    private boolean isNPC = false;

    private Map<Integer, ItemStack> respawnInventory;
    private Map<Integer, ItemStack> respawnArmor;
    private List<ItemStack> restoreInventory;
    private boolean restoreOpenWand;
    private Float restoreExperience;
    private Integer restoreLevel;
    private boolean virtualExperience = false;
    private float virtualExperienceProgress = 0.0f;
    private int virtualExperienceLevel = 0;
    private boolean glidingAllowed = false;
    private Set<String> tags = new HashSet<>();

    private String destinationWarp;
    private Integer lastActivatedSlot;
    private String currentDamageType;
    private String lastDamageType;
    private String currentDamageDealtType;
    private String lastDamageDealtType;

    private boolean launchingProjectile;
    private double lastDamage;
    private double lastDamageDealt;
    private double lastBowPull;
    private ItemStack lastBowUsed;
    private boolean cancelLaunch = false;
    private EntityType lastProjectileType;
    private boolean bypassEnabled;

    public Mage(String id, MagicController controller) {
        this.id = id;
        this.controller = controller;
        this.brush = new MaterialBrush(this, Material.DIRT, (byte) 0);
        this.properties = new MageProperties(this);
        playerRef = new WeakReference<>(null);
        entityRef = new WeakReference<>(null);
        commandSenderRef = new WeakReference<>(null);
        hasEntity = false;
        this.created = System.currentTimeMillis();
    }

    @Override
    public boolean hasStoredInventory() {
        return activeWand != null && activeWand.hasStoredInventory();
    }

    @Override
    public Set<Spell> getActiveSpells() {
        return new HashSet<>(activeSpells);
    }

    @Nullable
    public Inventory getStoredInventory() {
        return activeWand != null ? activeWand.getStoredInventory() : null;
    }

    @Override
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

    @Override
    public void setCostFree(boolean free) {
        costFree = free;
    }

    @Override
    public void setCooldownFree(boolean free) {
        cooldownFree = free;
    }

    @Override
    public void setPowerMultiplier(float multiplier) {
        powerMultiplier = multiplier;
    }

    @Override
    public float getPowerMultiplier() {
        return powerMultiplier;
    }

    public boolean usesMana() {
        return activeWand == null ? false : activeWand.usesMana();
    }

    @Override
    public boolean addToStoredInventory(ItemStack item) {
        return (activeWand == null ? false : activeWand.addToStoredInventory(item));
    }

    public boolean cancelSelection() {
        boolean result = false;
        if (!activeSpells.isEmpty()) {
            List<MageSpell> active = new ArrayList<>(activeSpells);
            for (MageSpell spell : active) {
                result = spell.cancelSelection() || result;
            }
        }
        return result;
    }

    public void onPlayerQuit(PlayerEvent event) {
        Player player = getPlayer();
        if (player == null || player != event.getPlayer()) {
            return;
        }
        // Must allow listeners to remove themselves during the event!
        List<Listener> active = new ArrayList<>(quitListeners);
        for (Listener listener : active) {
            callEvent(listener, event);
        }
    }

    public void onPlayerDeath(EntityDeathEvent event) {
        Player player = getPlayer();

        if (!player.hasMetadata("arena")) {
            lastDeathLocation = player.getLocation();
        }
        List<Listener> active = new ArrayList<>(deathListeners);
        for (Listener listener : active) {
            callEvent(listener, event);
        }
    }

    public void onDeath(EntityDeathEvent event) {
        for (Iterator<Batch> iterator = pendingBatches.iterator(); iterator.hasNext();) {
            Batch batch = iterator.next();
            if (!(batch instanceof SpellBatch)) continue;
            SpellBatch spellBatch = (SpellBatch)batch;
            Spell spell = spellBatch.getSpell();
            if (spell.cancelOnDeath()) {
                spell.cancel();
                batch.finish();
                iterator.remove();
            }
        }

        if (getEntity() == event.getEntity()) {
            trigger("death");
        }

        Player player = getPlayer();
        if (player != null && player == event.getEntity()) {
            onPlayerDeath(event);
            return;
        }

        // TODO: This is mainly here for mobs, but if this is ever used for players we need a better place to reset.
        if (effectContext != null) {
            effectContext.cancelEffects();
            effectContext = null;
        }
    }

    public void onCombust(EntityCombustEvent event) {
        if (getProtection("fire") >= 1) {
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

    private void setTarget(Entity target) {
        if (entityData != null && !entityData.shouldFocusOnDamager()) return;

        LivingEntity li = getLivingEntity();
        if (li != null && li instanceof Creature && target instanceof LivingEntity) {
            Creature creature = ((Creature)li);
            if (creature.getTarget() != target) {
                sendDebugMessage("Now targeting " + target.getName(), 6);
            }

            creature.setTarget((LivingEntity)target);
        }
    }

    @Override
    public @Nullable Collection<Entity> getDamagers() {
        if (damagedBy == null) return null;
        Collection<Entity> damagers = new ArrayList<>();
        for (DamagedBy damager : damagedBy.values()) {
            Entity entity = damager.getEntity();
            if (entity != null) {
                damagers.add(entity);
            }
        }
        return damagers;
    }

    @Override
    public @Nullable Entity getLastDamager() {
        return lastDamager == null ? null : lastDamager.getEntity();
    }

    @Override
    public @Nullable Entity getLastDamageTarget() {
        return lastDamageTarget == null ? null : lastDamageTarget.get();
    }

    @Override
    public @Nullable Entity getTopDamager() {
        if (topDamager == null) return null;
        Entity topEntity = topDamager.getEntity();
        if (topEntity == null && damagedBy != null) {
            double topDamage = 0;
            for (Iterator<Map.Entry<UUID, DamagedBy>> it = damagedBy.entrySet().iterator(); it.hasNext();) {
                Map.Entry<UUID, DamagedBy> entry = it.next();
                DamagedBy damaged = entry.getValue();
                Entity entity = damaged.getEntity();
                if (entity != null && entity.isValid() && !entity.isDead()) {
                    boolean withinRange = withinRange(entity);
                    if (withinRange && damaged.damage > topDamage) {
                        topEntity = entity;
                        topDamage = damaged.damage;
                        topDamager = damaged;
                    }
                } else {
                    it.remove();
                }
            }
        }

        return topEntity;
    }

    private boolean withinRange(Entity entity) {
        boolean withinRange = getLocation().getWorld().getName().equals(entity.getLocation().getWorld().getName());
        double rangeSquared = entityData == null ? 0 : entityData.getTrackRadiusSquared();
        if (rangeSquared > 0 && withinRange) {
            withinRange = getLocation().distanceSquared(entity.getLocation()) <= rangeSquared;
        }
        return withinRange;
    }

    @Override
    public void damagedBy(@Nonnull Entity damager, double damage) {
        lastDamage = damage;
        if (damagedBy == null) return;
        damager = CompatibilityUtils.getSource(damager);

        // Only tracking players for now.
        if (!(damager instanceof Player)) return;
        Player damagingPlayer = (Player)damager;

        lastDamager = damagedBy.get(damagingPlayer.getUniqueId());
        if (lastDamager == null) {
            lastDamager = new DamagedBy(damagingPlayer, damage);
            damagedBy.put(damagingPlayer.getUniqueId(), lastDamager);
        } else {
            lastDamager.damage += damage;
        }

        if (topDamager != null) {
            if (topDamager.getEntity() == null || topDamager.damage < lastDamager.damage || !withinRange(topDamager.getEntity())) {
                topDamager = lastDamager;
                setTarget(damagingPlayer);
            }
        } else {
            topDamager = lastDamager;
            setTarget(damagingPlayer);
        }
    }

    public void onDamageDealt(EntityDamageEvent event) {
        String damageType = currentDamageDealtType;
        lastDamageTarget = new WeakReference<>(event.getEntity());
        lastDamageDealt = event.getDamage();
        currentDamageDealtType = null;
        lastDamageDealtType = getDamageType(damageType, event.getCause());
        trigger("damage_dealt");
    }

    private String getDamageType(String damageType, EntityDamageEvent.DamageCause cause) {
        if (damageType == null) {
            switch (cause) {
                case CONTACT:
                case ENTITY_ATTACK:
                    damageType = "physical";
                    break;
                case FIRE:
                case FIRE_TICK:
                case LAVA:
                    damageType = "fire";
                    break;
                case BLOCK_EXPLOSION:
                case ENTITY_EXPLOSION:
                    damageType = "explosion";
                    break;
                default:
                    damageType = cause.name().toLowerCase();
                    break;
            }
        }
        return damageType;
    }

    public void onDamage(EntityDamageEvent event) {
        String damageType = currentDamageType;
        currentDamageType = null;
        lastDamage = event.getDamage();
        LivingEntity entity = getLivingEntity();
        if (entity == null) {
            return;
        }

        // Send on to any registered spells
        List<Listener> active = new ArrayList<>(damageListeners);
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
                        scale = event.getDamage() / CompatibilityUtils.getMaxHealth(li);
                    }
                    fallingSpell.playEffects("land", (float)scale, getLocation().getBlock().getRelative(BlockFace.DOWN));
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
            if (entity.getFireTicks() > 0) {
                entity.setFireTicks(0);
            }
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        // First check for damage reduction
        double reduction = 0;
        Double overallProtection = protection.get("overall");
        if (overallProtection != null) {
            reduction = overallProtection * controller.getMaxDamageReduction("overall");
        }

        // Apply weaknesses
        double multiplier = 1;
        Double overallWeakness = weakness.get("overall");
        if (overallWeakness != null && overallWeakness > 0) {
            double defendMultiplier = controller.getMaxDefendMultiplier("overall");
            if (defendMultiplier > 1) {
                defendMultiplier = 1 + (defendMultiplier - 1) * overallWeakness;
                multiplier *= defendMultiplier;
            }
        }

        if (cause == EntityDamageEvent.DamageCause.FIRE_TICK) {
            // Also put out fire if they have maxed out fire protection.
            double damageReductionFire = getProtection("fire");
            if (damageReductionFire >= 1 && entity.getFireTicks() > 0) {
                entity.setFireTicks(0);
            }
        }
        lastDamageType = getDamageType(damageType, cause);
        damageType = lastDamageType;

        // Process triggers
        trigger("damage");

        double protection = getProtection(damageType);
        double maxReduction = controller.getMaxDamageReduction(damageType);
        reduction += protection * maxReduction;

        if (reduction >= 1) {
            event.setCancelled(true);
            sendDebugMessage(ChatColor.RED + "Damage nullified by " + ChatColor.BLUE + damageType + " (" + cause + ")", 8);
            return;
        }

        double damage = event.getDamage();
        sendDebugMessage(ChatColor.RED + "Damaged by " + ChatColor.BLUE + (damageType == null ? "generic" : damageType) + " (" + cause + ")" + ChatColor.RED + " for "
                + ChatColor.DARK_RED + damage, 10);
        if (reduction > 0) {
            damage = (1.0 - reduction) * damage;
            sendDebugMessage(ChatColor.DARK_RED + "Damage type " + ChatColor.BLUE + damageType
                    + " reduced by " + ChatColor.AQUA + reduction + ChatColor.DARK_RED + " to " + ChatColor.RED + damage, 9);
            event.setDamage(damage);
        }

        double weakness = getWeakness(damageType);
        double maxMultiplier = controller.getMaxDefendMultiplier(damageType);
        if (maxMultiplier > 1 && weakness > 0) {
            weakness = 1 + (maxMultiplier - 1) * weakness;
            multiplier *= weakness;
        }

        if (multiplier > 1) {
            damage = multiplier * damage;
            sendDebugMessage(ChatColor.DARK_RED + "Damage type " + ChatColor.BLUE + damageType
                    + " multiplied by " + ChatColor.AQUA + multiplier + ChatColor.DARK_RED + " to " + ChatColor.RED + damage, 9);
            event.setDamage(damage);
        }

        if (damage > 0) {
            for (Iterator<Batch> iterator = pendingBatches.iterator(); iterator.hasNext();) {
                Batch batch = iterator.next();
                if (!(batch instanceof SpellBatch)) continue;
                SpellBatch spellBatch = (SpellBatch)batch;
                Spell spell = spellBatch.getSpell();
                double cancelOnDamage = spell.cancelOnDamage();
                if (cancelOnDamage > 0 && cancelOnDamage < damage)
                {
                    spell.cancel();
                    batch.finish();
                    iterator.remove();
                }
            }
        }
    }

    public double getProtection(String damageType) {
        Double value = protection.get(damageType);
        return value == null ? 0 : value;
    }

    public double getWeakness(String damageType) {
        Double value = weakness.get(damageType);
        return value == null ? 0 : value;
    }

    @Override
    public void unbindAll() {
        boundWands.clear();
    }

    @Override
    public void unbind(com.elmakers.mine.bukkit.api.wand.Wand wand) {
        unbind(wand.getTemplateKey());
    }

    @Override
    public boolean unbind(String template) {
        if (template != null) {
            return boundWands.remove(template) != null;
        }
        return false;
    }

    public void checkActiveSpells(Wand wand) {
        // This handles any spells that are running now
        for (Iterator<Batch> iterator = pendingBatches.iterator(); iterator.hasNext();) {
            Batch batch = iterator.next();
            if (!(batch instanceof SpellBatch)) continue;
            SpellBatch spellBatch = (SpellBatch)batch;
            Spell spell = spellBatch.getSpell();
            if (spell.cancelOnNoWand() && spell.getCurrentCast().getWand() == wand)
            {
                spell.cancel();
                batch.finish();
                iterator.remove();
            }
        }

        // This handles toggleable spells, which may or may not have an active batch running.
        if (activeSpells.isEmpty()) return;

        ArrayList<MageSpell> active = new ArrayList<>(activeSpells);
        for (MageSpell spell : active) {
            if (spell.cancelOnNoWand() && spell.getCurrentCast().getWand() == wand) {
                spell.deactivate();
            }
        }
    }

    protected void deactivateWand() {
        // Close the wand inventory to make sure the player's normal inventory gets saved
        if (activeWand != null) {
            activeWand.deactivate();
        }
    }

    public void deactivateWand(Wand wand) {
        if (wand == null) return;

        if (wand == activeWand) {
            checkActiveSpells(activeWand);
            setActiveWand(null);
        }
        if (wand == offhandWand) {
            checkActiveSpells(activeWand);
            setOffhandWand(null);
        }

        WandDeactivatedEvent event = new WandDeactivatedEvent(this, wand);
        Bukkit.getPluginManager().callEvent(event);
    }

    public void onTeleport(PlayerTeleportEvent event) {
        if (DEACTIVATE_WAND_ON_WORLD_CHANGE) {
            Location from = event.getFrom();
            Location to = event.getTo();

            if (from.getWorld().equals(to.getWorld())) {
                return;
            }
            deactivateWand();
        }
    }

    public void onChangeWorld() {
        checkWandNextTick(true);
        if (CHANGE_WORLD_EQUIP_COOLDOWN > 0) {
            ignoreItemActivationUntil = System.currentTimeMillis() + CHANGE_WORLD_EQUIP_COOLDOWN;
        }
    }

    public void activateIcon(Wand activeWand, ItemStack icon)
    {
        if (System.currentTimeMillis() < ignoreItemActivationUntil) {
            return;
        }
        // Check for spell or material selection
        if (icon != null && icon.getType() != Material.AIR) {
            com.elmakers.mine.bukkit.api.spell.Spell spell = getSpell(Wand.getSpell(icon));
            if (spell != null) {
                boolean isQuickCast = spell.isQuickCast() && !activeWand.isQuickCastDisabled();
                isQuickCast = isQuickCast || (activeWand.getMode() == WandMode.CHEST && activeWand.isQuickCast());
                if (spell.isPassive()) {
                    toggleSpellEnabled(spell);
                } else if (isQuickCast) {
                    activeWand.cast(spell);
                } else {
                    activeWand.setActiveSpell(spell.getKey());
                }
            } else if (Wand.isBrush(icon)) {
                activeWand.setActiveBrush(icon);
            }
        } else {
            activeWand.setActiveSpell("");
        }
        DeprecatedUtils.updateInventory(getPlayer());
    }

    public void setActiveWand(Wand activeWand) {
        // Avoid deactivating a wand by mistake, and avoid infinite recursion on null!
        if (this.activeWand == activeWand) return;
        this.activeWand = activeWand;
        if (activeWand != null && activeWand.isBound() && activeWand.canUse(getPlayer())) {
            addBound(activeWand);
        }
        blockPlaceTimeout = System.currentTimeMillis() + 200;
        updatePassiveEffects();

        if (activeWand != null && !isLoading()) {
            WandActivatedEvent activatedEvent = new WandActivatedEvent(this, activeWand);
            Bukkit.getPluginManager().callEvent(activatedEvent);
        }
    }

    public void setOffhandWand(Wand offhandWand) {
        // Avoid deactivating a wand by mistake, and avoid infinite recursion on null!
        if (this.offhandWand == offhandWand) return;
        this.offhandWand = offhandWand;
        if (offhandWand != null && offhandWand.isBound() && offhandWand.canUse(getPlayer())) {
            addBound(offhandWand);
        }
        blockPlaceTimeout = System.currentTimeMillis() + 200;
        updatePassiveEffects();

        if (offhandWand != null && !isLoading()) {
            WandActivatedEvent activatedEvent = new WandActivatedEvent(this, offhandWand);
            Bukkit.getPluginManager().callEvent(activatedEvent);
        }
    }

    @Override
    public boolean tryToOwn(com.elmakers.mine.bukkit.api.wand.Wand wand) {
        if (isPlayer() && wand instanceof Wand && ((Wand)wand).tryToOwn(getPlayer())) {
            addBound((Wand)wand);
            return true;
        }

        return false;
    }

    protected void addBound(Wand wand) {
        WandTemplate template = wand.getTemplate();
        if (template != null && template.isRestorable()) {
            String templateKey = template.getKey();
            if (templateKey != null && !templateKey.isEmpty()) {
                boundWands.put(templateKey, wand);
            }
        }
    }

    public long getBlockPlaceTimeout() {
        return blockPlaceTimeout;
    }

    /**
     * Send a message to this Mage when a spell is cast.
     *
     * @param message The message to send
     */
    @Override
    public void castMessage(String message) {
        if (!controller.showCastMessages()) return;
        sendMessage(controller.getCastMessagePrefix(), message);
    }

    /**
     * Send a message to this Mage.
     * <p/>
     * Use this to send messages to the player that are important.
     *
     * @param message The message to send
     */
    @Override
    public void sendMessage(String message) {
        sendMessage(controller.getMessagePrefix(), message);
    }

    public void sendMessage(String prefix, String message) {
        if (message == null || message.length() == 0 || quiet || !controller.showMessages()) return;
        sendMessage(getCommandSender(), getPlayer(), prefix, message);
    }

    public static void sendMessage(CommandSender sender, Player player, String prefix, String message) {
        if (message == null || message.length() == 0 || sender == null) return;
        boolean isTitle = false;
        boolean isActionBar = false;
        if (prefix.startsWith("a:")) {
            isActionBar = true;
            prefix = prefix.substring(2);
        } else if (prefix.startsWith("t:")) {
            isTitle = true;
            prefix = prefix.substring(2);
        }

        if (message.startsWith("t:")) {
            isTitle = true;
            // message overrides prefix
            isActionBar = false;
            message = message.substring(2);
        }

        String[] messages = StringUtils.split(message, "\n");
        if (messages.length == 0) {
            return;
        }

        if (isTitle && player != null) {
            String fullMessage = prefix + messages[0];
            String subtitle = messages.length > 1 ? prefix + messages[1] : null;
            CompatibilityUtils.sendTitle(player, fullMessage, subtitle, -1, -1, -1);
            if (messages.length > 2) {
                messages = Arrays.copyOfRange(messages, 2, messages.length);
            } else {
                return;
            }
        }

        for (String line : messages) {
            boolean lineIsActionBar = isActionBar;
            if (line.startsWith("a:")) {
                lineIsActionBar = true;
                line = line.substring(2);
            }

            String fullMessage = prefix + line;
            if (lineIsActionBar && player != null) {
                CompatibilityUtils.sendActionBar(player, fullMessage);
            } else {
                sender.sendMessage(fullMessage);
            }
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

    @Override
    public UndoQueue getUndoQueue() {
        if (undoQueue == null) {
            undoQueue = new UndoQueue(this);
            undoQueue.setMaxSize(controller.getUndoQueueDepth());
        }
        return undoQueue;
    }

    @Nullable
    @Override
    public UndoList getLastUndoList() {
        if (undoQueue == null || undoQueue.isEmpty()) return null;
        return undoQueue.getLast();
    }

    @Override
    public boolean prepareForUndo(com.elmakers.mine.bukkit.api.block.UndoList undoList) {
        if (undoList == null) return false;
        if (undoList.bypass()) return false;
        UndoQueue queue = getUndoQueue();
        queue.add(undoList);
        return true;
    }

    @Override
    public boolean registerForUndo(com.elmakers.mine.bukkit.api.block.UndoList undoList) {
        if (!prepareForUndo(undoList)) return false;

        int autoUndo = controller.getAutoUndoInterval();
        if (autoUndo > 0 && undoList.getScheduledUndo() == 0) {
            undoList.setScheduleUndo(autoUndo);
        } else {
            undoList.updateScheduledUndo();
        }

        if (!undoList.hasBeenScheduled() && undoList.isScheduled())
        {
            if (undoList.hasChanges()) {
                controller.scheduleUndo(undoList);
            } else {
                undoQueue.skippedUndo(undoList);
            }
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
            this.playerRef = new WeakReference<>(player);
            this.entityRef = new WeakReference<>(player);
            this.commandSenderRef = new WeakReference<>(player);
            hasEntity = true;
        } else {
            this.playerRef.clear();
            this.entityRef.clear();
            this.commandSenderRef.clear();
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
            this.entityRef = new WeakReference<>(entity);
            hasEntity = true;
            isNPC = controller.isNPC(entity);
        } else {
            this.entityRef.clear();
            hasEntity = false;
            isNPC = false;
        }
    }

    protected void setCommandSender(CommandSender sender) {
        if (sender != null) {
            this.commandSenderRef = new WeakReference<>(sender);

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
            this.commandSenderRef.clear();
            setLocation(null);
        }
    }

    protected void onLoad(MageData data) {
        try {
            Collection<SpellData> spellDataList = data == null ? null : data.getSpellData();
            if (spellDataList != null) {
                for (SpellData spellData : spellDataList) {
                    this.spellData.put(spellData.getKey().getKey(), spellData);
                }
            }

            // Load player-specific data
            Player player = getPlayer();
            if (player != null) {
                if (controller.isInventoryBackupEnabled()) {
                    if (restoreInventory != null) {
                        controller.getLogger().info("Restoring saved inventory for player " + player.getName() + " - did the server not shut down properly?");
                        if (activeWand != null) {
                            activeWand.deactivate();
                        }
                        Inventory inventory = player.getInventory();
                        for (int slot = 0; slot < restoreInventory.size(); slot++) {
                            Object item = restoreInventory.get(slot);
                            if (item instanceof ItemStack) {
                                inventory.setItem(slot, (ItemStack) item);
                            } else {
                                inventory.setItem(slot, null);
                            }
                        }
                        restoreInventory = null;
                    }
                    if (restoreExperience != null) {
                        player.setExp(restoreExperience);
                        restoreExperience = null;
                    }
                    if (restoreLevel != null) {
                        player.setLevel(restoreLevel);
                        restoreLevel = null;
                    }
                }

                if (activeWand == null) {
                    String welcomeWand = controller.getWelcomeWand();
                    if (!gaveWelcomeWand && welcomeWand.length() > 0) {
                        gaveWelcomeWand = true;
                        Wand wand = Wand.createWand(controller, welcomeWand);
                        if (wand != null) {
                            wand.takeOwnership(player);
                            giveItem(wand.getItem());
                            controller.getLogger().info("Gave welcome wand " + wand.getName() + " to " + player.getName());
                        } else {
                            controller.getLogger().warning("Unable to give welcome wand '" + welcomeWand + "' to " + player.getName());
                        }
                    }
                }

                if (activeWand != null && restoreOpenWand && !activeWand.isInventoryOpen())
                {
                    activeWand.openInventory();
                }
                restoreOpenWand = false;
            }

            loading = false;
            // Force-add default class
            getActiveClass();
            armorUpdated();
            trigger("join");
        } catch (Exception ex) {
            controller.getLogger().warning("Error finalizing player data for " + playerName + ": " + ex.getMessage());
        }
    }

    protected void finishLoad(MageData data) {
        MageLoadTask loadTask = new MageLoadTask(this, data);
        Bukkit.getScheduler().scheduleSyncDelayedTask(controller.getPlugin(), loadTask, 1);
    }

    @Override
    public boolean load(MageData data) {
        try {
            if (data == null) {
                finishLoad(data);
                return true;
            }

            this.data = data.getExtraData();
            this.properties.load(data.getProperties());
            this.properties.loadProperties();
            this.variables = data.getVariables();

            boundWands.clear();
            Map<String, ItemStack> boundWandItems = data.getBoundWands();
            if (boundWandItems != null) {
                for (ItemStack boundWandItem : boundWandItems.values()) {
                    try {
                        Wand boundWand = controller.getWand(boundWandItem);
                        String templateKey = boundWand.getTemplateKey();
                        if (templateKey != null && !templateKey.isEmpty()) {
                            boundWands.put(templateKey, boundWand);
                        }
                    } catch (Exception ex) {
                        controller.getLogger().log(Level.WARNING, "Failed to load bound wand for " + playerName + ": " + boundWandItem, ex);
                    }
                }
            }

            this.classes.clear();
            Map<String, ConfigurationSection> classProperties = data.getClassProperties();
            for (Map.Entry<String, ConfigurationSection> entry : classProperties.entrySet()) {
                // ... what to do if missing templates? Don't want to lose data. Will need to think about this.
                String mageClassKey = entry.getKey();
                MageClassTemplate classTemplate = controller.getMageClass(mageClassKey);
                if (classTemplate != null) {
                    MageClass newClass = new MageClass(this, classTemplate);
                    newClass.load(entry.getValue());
                    classes.put(mageClassKey, newClass);
                }
            }

            this.modifiers.clear();
            Map<String, ConfigurationSection> modifierProperties = data.getModifierProperties();
            for (Map.Entry<String, ConfigurationSection> entry : modifierProperties.entrySet()) {
                String modifierKey = entry.getKey();
                ModifierTemplate template = controller.getModifierTemplate(modifierKey);
                if (template != null) {
                    MageModifier newModifier = new MageModifier(this, template);
                    newModifier.load(entry.getValue());
                    modifiers.put(modifierKey, newModifier);
                }
            }

            // Link up parents. This may cause additional classes to be added or created, if the player did not
            // have that class loaded.
            // So we need to continue to assign parents until no new classes are added.
            Set<String> assignedClasses = new HashSet<>();
            boolean allAssigned = false;
            while (!allAssigned) {
                List<MageClass> mageClasses = new ArrayList<>(classes.values());
                for (MageClass mageClass : mageClasses) {
                    if (!assignedClasses.contains(mageClass.getKey())) {
                        assignedClasses.add(mageClass.getKey());
                        assignParent(mageClass);
                    }
                }
                allAssigned = assignedClasses.containsAll(classes.keySet());
            }

            // Load activeClass
            setActiveClass(data.getActiveClass());

            // Re-activate unlocked classes
            activateClasses();
            activateModifiers();

            // Restore saved health, which may have gotten lowered when we deactivated classes
            double health = data.getHealth();
            LivingEntity li = getLivingEntity();
            if (health > 0 && li != null) {
                health = Math.min(health, CompatibilityUtils.getMaxHealth(li));
                li.setHealth(health);
            }

            cooldownExpiration = data.getCooldownExpiration();
            fallProtectionCount = data.getFallProtectionCount();
            fallProtection = data.getFallProtectionDuration();
            if (fallProtectionCount > 0 && fallProtection > 0) {
                fallProtection = System.currentTimeMillis() + fallProtection;
            }

            gaveWelcomeWand = data.getGaveWelcomeWand();
            playerName = data.getName();
            lastDeathLocation = data.getLastDeathLocation();
            lastCast = data.getLastCast();
            created = data.getCreatedTime();
            destinationWarp = data.getDestinationWarp();
            if (destinationWarp != null) {
                if (!destinationWarp.isEmpty()) {
                    Location destination = controller.getWarp(destinationWarp);
                    if (destination != null) {
                        Plugin plugin = controller.getPlugin();
                        controller.info("Warping " + getEntity().getName() + " to " + destinationWarp + " on login");
                        TeleportTask task = new TeleportTask(getController(), getEntity(), destination, 4, true, true, null);
                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, task, 1);
                    } else {
                        controller.info("Failed to warp " + getEntity().getName() + " to " + destinationWarp + " on login, warp doesn't exist");
                    }
                }
                destinationWarp = null;
            }

            getUndoQueue().load(data.getUndoData());
            externalUndoData = data.getExternalUndoData();

            respawnInventory = data.getRespawnInventory();
            respawnArmor = data.getRespawnArmor();
            restoreOpenWand = data.isOpenWand();

            BrushData brushData = data.getBrushData();
            if (brushData != null) {
                brush.load(brushData);
            }

            if (controller.isInventoryBackupEnabled()) {
                restoreInventory = data.getStoredInventory();
                restoreLevel = data.getStoredLevel();
                restoreExperience = data.getStoredExperience();
            }
        } catch (Exception ex) {
            controller.getLogger().log(Level.WARNING, "Failed to load player data for " + playerName, ex);
            return false;
        }

        finishLoad(data);
        return true;
    }

    @Override
    public @Nullable MageClass getActiveClass() {
        if (activeClass == null && !DEFAULT_CLASS.isEmpty()) {
            setActiveClass(DEFAULT_CLASS);
        }
        return activeClass;
    }

    @Override
    public boolean setActiveClass(String classKey) {
        if (classKey == null) {
            activeClass = null;
            return true;
        }
        MageClass targetClass = getClass(classKey);
        if (targetClass == null) {
            return false;
        }
        activeClass = targetClass;

        // TODO: Is this the best place to do this?
        activeClass.loadProperties();
        updatePassiveEffects();
        return true;
    }

    @Override
    public boolean removeClass(String classKey) {
        if (!classes.containsKey(classKey)) {
            return false;
        }
        classes.get(classKey).onRemoved();
        classes.remove(classKey);
        if (activeClass != null && activeClass.getTemplate().getKey().equals(classKey)) {
            activeClass = null;
        }
        return true;
    }

    public boolean canUse(ItemStack itemStack) {
        String lockKey = controller.getLockKey(itemStack);
        if (lockKey == null) {
            return true;
        }
        for (MageClass mageClass : classes.values()) {
            if (!mageClass.isLocked() && mageClass.canUse(lockKey)) return true;
        }
        return false;
    }

    public boolean useArrow(ItemStack itemStack, int slot, ProjectileLaunchEvent event) {
        String spellKey = Wand.getArrowSpell(itemStack);
        if (spellKey == null) return false;

        Spell spell = getSpell(spellKey);
        if (spell == null) {
            return false;
        }
        event.setCancelled(true);
        String skillClass = Wand.getArrowSpellClass(itemStack);
        if (skillClass != null && !skillClass.isEmpty()) {
            if (!setActiveClass(skillClass)) {
                sendMessage(controller.getMessages().get("mage.no_class").replace("$name", spell.getName()));
                return false;
            }
        }
        if (!canUse(itemStack)) {
            sendMessage(controller.getMessages().get("mage.no_class").replace("$name", spell.getName()));
            return false;
        }

        if (!isCostFree()) {
            if (itemStack.getAmount() <= 1) {
                clearSlot(slot);
            } else {
                itemStack.setAmount(itemStack.getAmount() - 1);
            }
        }
        try {
            spell.cast();
        } catch (Exception ex) {
            controller.getLogger().log(Level.SEVERE, "Error casting arrow spell", ex);
        }
        return true;
    }

    public boolean useSkill(ItemStack skillItem) {
        Spell spell = getSpell(Wand.getSpell(skillItem));
        if (spell == null) return false;
        if (spell.isPassive()) {
            if (spell.isToggleable()) {
                spell.setEnabled(!spell.isEnabled());
                Wand.updateSpellItem(controller.getMessages(), skillItem, spell, "", null, null, false);
            }
            return true;
        }
        boolean canUse = true;
        String skillClass = Wand.getSpellClass(skillItem);
        if (skillClass != null && !skillClass.isEmpty()) {
            if (!setActiveClass(skillClass)) {
                canUse = false;
                sendMessage(controller.getMessages().get("mage.no_class").replace("$name", spell.getName()));
            } else {
                if (!activeClass.hasSpell(spell.getKey())) {
                    canUse = false;
                    sendMessage(controller.getMessages().get("mage.no_spell").replace("$name", spell.getName()));
                }
            }
        }
        if (canUse) {
            // TODO: Maybe find a better way to handle this.
            // There is also an issue of an active wand taking over the hotbar display.
            Wand activeWand = this.activeWand;
            this.activeWand = null;
            spell.cast();
            this.activeWand = activeWand;
        }
        return canUse;
    }

    @Override
    @Nullable
    public MageClass unlockClass(@Nonnull String key) {
        return getClass(key, true);
    }

    @Override
    public boolean lockClass(@Nonnull String key) {
        MageClass mageClass = getClass(key);
        if (mageClass == null) {
            return false;
        }
        mageClass.lock();
        if (activeClass != null && activeClass.isLocked()) {
            setActiveClass(null);
        }
        checkWand();
        checkOffhandWand();
        return true;
    }

    @Override
    public @Nonnull Collection<com.elmakers.mine.bukkit.api.magic.MageClass> getClasses() {
       List<com.elmakers.mine.bukkit.api.magic.MageClass> mageClasses = new ArrayList<>();
       mageClasses.addAll(classes.values());
       return mageClasses;
    }

    @Override
    public @Nonnull Collection<String> getClassKeys() {
        return classes.keySet();
    }

    @Override
    public @Nullable MageClass getClass(@Nonnull String key) {
        return getClass(key, false);
    }

    private @Nullable MageClass getClass(@Nonnull String key, boolean unlock) {
        MageClass mageClass = classes.get(key);
        if (mageClass == null) {
            MageClassTemplate template = controller.getMageClass(key);
            if (template != null && (unlock || !template.isLocked())) {
                mageClass = new MageClass(this, template);
                assignParent(mageClass);
                classes.put(key, mageClass);
                mageClass.onUnlocked();
            }
        }
        if (mageClass != null && mageClass.isLocked()) {
            if (unlock) {
                mageClass.unlock();
            } else {
                mageClass = null;
            }
        }
        return mageClass;
    }

    @Override
    public boolean hasClassUnlocked(@Nonnull String key) {
        MageClass mageClass = classes.get(key);
        return mageClass != null && !mageClass.isLocked();
    }

    private void assignParent(MageClass mageClass) {
        TemplateProperties template = mageClass.getTemplate();
        TemplateProperties parentTemplate = template.getParent();
        if (parentTemplate != null) {
            // Having a sub-class means having the parent class.
            MageClass parentClass = getClass(parentTemplate.getKey(), true);

            // Should never be null, check is here to silence the compiler.
            if (parentClass != null) {
                mageClass.setParent(parentClass);
            }
        }
    }

    @Override
    public boolean save(MageData data) {
        if (loading) return false;
        try {
            data.setName(getName());
            data.setId(getId());
            data.setCreatedTime(created);
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
            data.setExternalUndoData(externalUndoData);

            data.setSpellData(this.spellData.values());

            if (boundWands.size() > 0) {
                Map<String, ItemStack> wandItems = new HashMap<>();
                for (Map.Entry<String, Wand> wandEntry : boundWands.entrySet()) {
                    wandItems.put(wandEntry.getKey(), wandEntry.getValue().getItem());
                }
                data.setBoundWands(wandItems);
            }
            data.setRespawnArmor(respawnArmor);
            data.setRespawnInventory(respawnInventory);
            data.setOpenWand(false);
            if (activeWand != null) {
                if (activeWand.hasStoredInventory()) {
                    data.setStoredInventory(Arrays.asList(activeWand.getStoredInventory().getContents()));
                }
                if (activeWand.isInventoryOpen()) {
                    data.setOpenWand(true);
                }
            }
            data.setGaveWelcomeWand(gaveWelcomeWand);
            data.setExtraData(this.data);
            data.setProperties(properties.getConfiguration());
            data.setVariables(variables);

            Map<String, ConfigurationSection> classProperties = new HashMap<>();
            for (Map.Entry<String, MageClass> entry : classes.entrySet()) {
                classProperties.put(entry.getKey(), entry.getValue().getConfiguration());
            }
            data.setClassProperties(classProperties);

            Map<String, ConfigurationSection> modifierProperties = new HashMap<>();
            for (Map.Entry<String, MageModifier> entry : modifiers.entrySet()) {
                modifierProperties.put(entry.getKey(), entry.getValue().getConfiguration());
            }
            data.setModifierProperties(modifierProperties);

            String activeClassKey = activeClass == null ? null : activeClass.getTemplate().getKey();
            data.setActiveClass(activeClassKey);
            LivingEntity li = getLivingEntity();
            if (li != null) {
                data.setHealth(li.getHealth());
            }
        } catch (Exception ex) {
            controller.getLogger().log(Level.WARNING, "Failed to save player data for " + playerName, ex);
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

    public void sendMessageKey(String key) {
        sendMessage(controller.getMessages().get(key, key));
    }

    @Nullable
    private Wand checkWand(ItemStack itemInHand) {
        Player player = getPlayer();
        if (isLoading() || player == null) return null;

        ItemStack activeWandItem = activeWand != null ? activeWand.getItem() : null;
        if (InventoryUtils.isSameInstance(activeWandItem, itemInHand)) return activeWand;

        if (!Wand.isWand(itemInHand)) itemInHand = null;

        if ((itemInHand != null && activeWandItem == null)
                || (activeWandItem != null && itemInHand == null)
                || (activeWandItem != null && itemInHand != null && !itemInHand.equals(activeWandItem))
                )
        {
            if (activeWand != null) {
                activeWand.deactivate();
            }
            if (itemInHand != null && controller.hasWandPermission(player)) {
                Wand newActiveWand = controller.getWand(itemInHand);
                if (!newActiveWand.activate(this)) {
                    setActiveWand(null);
                }
            }
        }

        return activeWand;
    }

    @Nullable
    @Override
    public Wand checkWand() {
        Player player = getPlayer();
        if (isLoading() || player == null) return null;
        checkOffhandWand();
        return checkWand(player.getInventory().getItemInMainHand());
    }

    public boolean offhandCast() {
        long now = System.currentTimeMillis();
        if (lastOffhandCast > 0 && now < lastOffhandCast + OFFHAND_CAST_COOLDOWN) {
            return false;
        }
        lastOffhandCast = now;

        Player player = getPlayer();
        if (isLoading() || player == null) return false;

        ItemStack itemInOffhand = player.getInventory().getItemInOffHand();
        if (Wand.isWand(itemInOffhand)) {
            if (offhandWand != null && (offhandWand.getLeftClickAction() == WandAction.CAST || offhandWand.getRightClickAction() == WandAction.CAST)) {
                offhandCast = true;
                Wand castingWand = offhandWand;
                boolean castResult = false;
                try {
                    castingWand.tickMana();
                    castingWand.setActiveMage(this);
                    castResult = castingWand.cast();

                    // Don't swing arm is cast is from right-click
                    if (castingWand.getRightClickAction() != WandAction.CAST) {
                        CompatibilityUtils.swingOffhand(player, OFFHAND_CAST_RANGE);
                    }
                } catch (Exception ex) {
                    controller.getLogger().log(Level.WARNING, "Error casting from offhand wand", ex);
                }
                offhandCast = false;
                return castResult;
            }
        }

        return false;
    }

    public boolean setOffhandActive(boolean active) {
        boolean wasActive = offhandCast;
        this.offhandCast = active;
        return wasActive;
    }

    @Nullable
    private Wand checkOffhandWand() {
        Player player = getPlayer();
        if (player == null) {
            return null;
        }
        return checkOffhandWand(player.getInventory().getItemInOffHand());
    }

    @Nullable
    private Wand checkOffhandWand(ItemStack itemInHand) {
        Player player = getPlayer();
        if (isLoading() || player == null) return null;

        ItemStack offhandWandItem = offhandWand != null ? offhandWand.getItem() : null;
        if (InventoryUtils.isSameInstance(offhandWandItem, itemInHand)) return offhandWand;

        if (!Wand.isWand(itemInHand)) itemInHand = null;

        if ((itemInHand != null && offhandWandItem == null)
        || (offhandWandItem != null && itemInHand == null)
        || (itemInHand != null && offhandWandItem != null && !itemInHand.equals(offhandWandItem))
        )
        {
            if (offhandWand != null) {
                offhandWand.deactivate();
            }
            if (itemInHand != null && controller.hasWandPermission(player)) {
                Wand newActiveWand = controller.getWand(itemInHand);
                if (!newActiveWand.activateOffhand(this)) {
                    setOffhandWand(null);
                }
            }
        }
        return offhandWand;
    }

    public void checkWandNextTick(boolean checkAll) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(controller.getPlugin(), new CheckWandTask(this, checkAll));
    }

    public void checkWandNextTick() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(controller.getPlugin(), new CheckWandTask(this));
    }

    @Override
    public Vector getVelocity() {
        return velocity;
    }

    protected void updateVelocity() {
        if (lastLocation != null) {
            Location currentLocation = getLocation();
            if (currentLocation.getWorld().equals(lastLocation.getWorld())) {
                long interval = System.currentTimeMillis() - lastTick;
                velocity.setX((currentLocation.getX() - lastLocation.getX()) * 1000 / interval);
                velocity.setY((currentLocation.getY() - lastLocation.getY()) * 1000 / interval);
                velocity.setZ((currentLocation.getZ() - lastLocation.getZ()) * 1000 / interval);
            } else {
                velocity.setX(0);
                velocity.setY(0);
                velocity.setZ(0);
            }
        }
        lastLocation = getLocation();
    }

    @Override
    public void tick() {
        triggeringSpells.clear();
        long now = System.currentTimeMillis();
        if (entityData != null) {
            if (lastTick != 0) {
                long tickInterval = entityData.getTickInterval();
                if (tickInterval > 0 && now - lastTick > tickInterval) {
                    updateVelocity();
                    entityData.tick(this);
                    lastTick = now;
                }
            } else {
                lastTick = now;
            }
        } else {
            trigger("interval");
            updateVelocity();
            lastTick = now;
        }

        // Check for expired modifiers
        for (MageModifier modifier : modifiers.values()) {
            if (modifier.hasDuration() && modifier.getTimeRemaining() <= 0) {
                removeModifiers.add(modifier);
            }
        }
        for (MageModifier modifier : removeModifiers) {
            modifiers.remove(modifier.getKey());
            modifier.onRemoved();
        }
        if (!removeModifiers.isEmpty()) {
            removeModifiers.clear();
            updatePassiveEffects();
        }

        if (isNPC) return;

        // We don't tick non-player or offline Mages, except
        // above where entityData is ticked if present.
        Player player = getPlayer();
        if (player != null && player.isOnline()) {
            checkWand();
            if (activeWand != null) {
                activeWand.tick();
            } else if (virtualExperience) {
                resetSentExperience();
            }
            if (offhandWand != null) {
                offhandWand.tick();
            }
            if (activeClass != null) {
                activeClass.tick();
            }
            properties.tick();

            if (Wand.LiveHotbarSkills && (activeWand == null || !activeWand.isInventoryOpen())) {
                updateHotbarStatus();
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
            List<MageSpell> active = new ArrayList<>(activeSpells);
            for (MageSpell spell : active) {
                spell.tick();
                if (!spell.isActive()) {
                    deactivateSpell(spell);
                }
            }
        }
    }

    public int processPendingBatches(int maxWorldAllowed) {
        int updated = 0;
        for (Iterator<Batch> iterator = pendingBatches.iterator(); iterator.hasNext();) {
            Batch batch = iterator.next();
            int batchUpdated = batch.process(Math.max(1, maxWorldAllowed - updated));
            updated += batchUpdated;
            if (batch.isFinished()) {
                iterator.remove();
            }
        }
        return updated;
    }

    public boolean hasPendingBatches() {
        return !pendingBatches.isEmpty();
    }

    public void setLastHeldMapId(int mapId) {
        brush.setMapId(mapId);
    }

    @Override
    public int getLastHeldMapId() {
        return brush.getMapId();
    }

    protected void reloadClasses() {
        for (Iterator<Map.Entry<String, MageClass>> it = classes.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, MageClass> entry = it.next();
            String templateKey = entry.getKey();
            MageClass mageClass = entry.getValue();
            MageClassTemplate template = controller.getMageClassTemplate(templateKey);
            if (template == null) {
                mageClass.onRemoved();
                if (activeClass == mageClass) {
                    setActiveClass(null);
                }
                it.remove();
                continue;
            }

            if (!mageClass.isLocked()) {
                mageClass.deactivateAttributes();
            }
            mageClass.setTemplate(template);
            if (!mageClass.isLocked()) {
                mageClass.activateAttributes();
            }
        }
    }

    protected void reloadModifiers() {
        for (Iterator<Map.Entry<String, MageModifier>> it = modifiers.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, MageModifier> entry = it.next();
            String templateKey = entry.getKey();
            MageModifier modifier = entry.getValue();
            ModifierTemplate template = controller.getModifierTemplate(templateKey);
            if (template == null) {
                modifier.onRemoved();
                it.remove();
                continue;
            }
            modifier.deactivateAttributes();
            modifier.setTemplate(template);
            modifier.activateAttributes();
        }
    }

    protected void reloadAttributes() {
        for (Iterator<Map.Entry<String, MageClass>> it = classes.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, MageClass> entry = it.next();
            MageClass mageClass = entry.getValue();
            if (!mageClass.isLocked()) {
                mageClass.deactivateAttributes();
                mageClass.activateAttributes();
            }
        }

        for (Iterator<Map.Entry<String, MageModifier>> it = modifiers.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, MageModifier> entry = it.next();
            MageModifier modifier = entry.getValue();
            modifier.deactivateAttributes();
            modifier.activateAttributes();
        }
    }

    protected void loadSpells(ConfigurationSection spellConfiguration) {
        if (spellConfiguration == null) return;

        Collection<MageSpell> currentSpells = new ArrayList<>(spells.values());
        for (MageSpell spell : currentSpells) {
            String key = spell.getKey();
            if (spellConfiguration.contains(key)) {
                ConfigurationSection template = spellConfiguration.getConfigurationSection(key);
                String className = template.getString("class");
                if (className == null)
                {
                    className = ActionSpell.class.getName();
                }
                // Check for spells that have changed class
                // TODO: Still unsure if this is right.
                if (!spell.getClass().getName().contains(className)) {
                    spells.remove(key);
                    this.spellData.put(key, spell.getSpellData());
                } else {
                    spell.loadTemplate(key, template);
                    spell.loadPrerequisites(template);
                }
            } else {
                spells.remove(key);
            }
        }
    }

    /*
     * API Implementation
     */

    @Override
    public Collection<Batch> getPendingBatches() {
        Collection<Batch> pending = new ArrayList<>();
        pending.addAll(pendingBatches);
        return pending;
    }

    @Override
    public String getName() {
        return playerName == null || playerName.length() == 0 ? defaultMageName : playerName;
    }

    @Override
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

    @Nullable
    @Override
    public Location getLocation() {
        if (location != null) return location.clone();

        LivingEntity livingEntity = getLivingEntity();
        if (livingEntity == null) return null;
        return livingEntity.getLocation();
    }

    @Nullable
    @Override
    public Location getEyeLocation() {
        Entity entity = getEntity();
        if (entity != null) {
            return CompatibilityUtils.getEyeLocation(entity);
        }

        return getLocation();
    }

    @Nullable
    @Override
    public Location getCastLocation() {
        Location castLocation = getEyeLocation();
        if (activeWand != null && !offhandCast) {
            castLocation = activeWand.getLocation();
        } else if (offhandWand != null && offhandCast) {
            castLocation = offhandWand.getLocation();
        } else if (DEFAULT_CAST_LOCATION == CastSourceLocation.MAINHAND) {
            castLocation = getOffsetLocation(castLocation, false, DEFAULT_CAST_OFFSET);
        } else if (DEFAULT_CAST_LOCATION == CastSourceLocation.OFFHAND) {
            castLocation = getOffsetLocation(castLocation, true, DEFAULT_CAST_OFFSET);
        }

        return castLocation;
    }

    @Nullable
    @Override
    public Location getWandLocation() {
        return getCastLocation();
    }

    public Location getOffsetLocation(Location baseLocation, boolean isInOffhand, Vector offset) {
        Entity entity = getEntity();
        if (entity == null) return baseLocation;

        boolean leftHand = isInOffhand;
        if (entity instanceof HumanEntity) {
            HumanEntity human = (HumanEntity)entity;
            if (human.getMainHand() == MainHand.LEFT) {
                leftHand = !leftHand;
            }
        }
        double sneakOffset = 0;
        if (entity instanceof Player && ((Player)entity).isSneaking()) {
            sneakOffset = SNEAKING_CAST_OFFSET;
        }

        if (leftHand) {
            offset = new Vector(offset.getX(), offset.getY() + sneakOffset, -offset.getZ());
        } else if (sneakOffset != 0) {
            offset = new Vector(offset.getX(), offset.getY() + sneakOffset, offset.getZ());
        }

        baseLocation.add(VectorUtils.rotateVector(offset, baseLocation));
        return baseLocation;
    }

    @Nullable
    @Override
    public Location getOffhandWandLocation() {
        Location wandLocation = getEyeLocation();
        if (offhandWand != null) {
            wandLocation = offhandWand.getLocation();
        }
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

    @Nullable
    @Override
    public UndoList undo(Block target) {
        return getUndoQueue().undo(target);
    }

    @Nullable
    @Override
    public UndoList undo() {
        return getUndoQueue().undo();
    }

    @Nullable
    @Override
    public Batch cancelPending() {
        return cancelPending(null, true);
    }

    @Nullable
    @Override
    public Batch cancelPending(String spellKey) {
        return cancelPending(spellKey, true);
    }

    @Nullable
    @Override
    public Batch cancelPending(boolean force) {
        return cancelPending(null, force);
    }

    @Nullable
    @Override
    public Batch cancelPending(String spellKey, boolean force) {
        return cancelPending(spellKey, force, true);
    }


    @Nullable
    @Override
    public Batch cancelPending(String spellKey, boolean force, boolean nonBatched) {
        return cancelPending(spellKey, force, nonBatched, null);
    }

    @Nullable
    public Batch cancelPending(String spellKey, boolean force, boolean nonBatched, String exceptSpellKey) {
        Batch stoppedPending = null;
        if (!pendingBatches.isEmpty()) {
            List<Batch> batches = new ArrayList<>();
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
                if (exceptSpellKey != null) {
                    if (batch instanceof SpellBatch) {
                        SpellBatch spellBatch = (SpellBatch)batch;
                        Spell spell = spellBatch.getSpell();
                        if (spell != null && spell.getSpellKey().getBaseKey().equalsIgnoreCase(exceptSpellKey)) {
                            continue;
                        }
                    }
                }

                if (!(batch instanceof UndoBatch)) {
                    if (batch instanceof SpellBatch)
                    {
                        SpellBatch spellBatch = (SpellBatch)batch;
                        Spell spell = spellBatch.getSpell();
                        if (spell != null)
                        {
                            spell.cancel();
                        }
                    }

                    batch.finish();
                    pendingBatches.remove(batch);
                    stoppedPending = batch;
                }
            }
        }
        if (nonBatched && stoppedPending == null && spellKey != null && !spellKey.isEmpty()) {
            Spell cancelSpell = getSpell(spellKey);
            if (cancelSpell != null) {
                cancelSpell.cancel();
            }
        }
        return stoppedPending;
    }

    @Override
    public int finishPendingUndo() {
        int finished = 0;
        if (!pendingBatches.isEmpty()) {
            List<Batch> batches = new ArrayList<>();
            batches.addAll(pendingBatches);
            for (Batch batch : batches) {
                if (batch instanceof UndoBatch) {
                    ((UndoBatch)batch).complete();
                    pendingBatches.remove(batch);
                    finished++;
                }
            }
        }

        return finished;
    }

    @Override
    public boolean commit() {
        return getUndoQueue().commit();
    }

    @Override
    public boolean hasCastPermission(Spell spell) {
        return spell.hasCastPermission(getCommandSender());
    }

    @Override
    public boolean hasSpell(String key) {
        return spells.containsKey(key);
    }

    @Nullable
    @Override
    public MageSpell getSpell(String key) {
        if (loading) return null;

        MageSpell playerSpell = spells.get(key);
        if (playerSpell == null) {
            playerSpell = createSpell(key);
            if (playerSpell != null) {
                SpellData spellData = this.spellData.get(key);
                if (spellData == null) {
                    spellData = new SpellData(key);
                    this.spellData.put(key, spellData);
                }
                playerSpell.setSpellData(spellData);
            }
        } else {
            playerSpell.setMage(this);
        }

        return playerSpell;
    }

    @Nullable
    protected MageSpell createSpell(String key) {
        MageSpell playerSpell = spells.get(key);
        if (playerSpell != null) {
            playerSpell.setMage(this);
            return playerSpell;
        }
        SpellTemplate spellTemplate = controller.getSpellTemplate(key);
        if (spellTemplate == null) return null;
        playerSpell = spellTemplate.createMageSpell(this);
        if (playerSpell == null) return null;
        spells.put(playerSpell.getKey(), playerSpell);
        return playerSpell;
    }

    @Override
    public Collection<Spell> getSpells() {
        List<Spell> export = new ArrayList<>(spells.values());
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
        deactivateAllSpells(force, quiet, null);
    }

    @Override
    public void deactivateAllSpells(boolean force, boolean quiet, String exceptSpellKey) {
        // Copy this set since spells will get removed while iterating!
        List<MageSpell> active = new ArrayList<>(activeSpells);
        for (MageSpell spell : active) {
            if (exceptSpellKey != null && spell.getSpellKey().getBaseKey().equalsIgnoreCase(exceptSpellKey)) continue;
            if (spell.deactivate(force, quiet)) {
                activeSpells.remove(spell);
            }
        }

        // This is mainly here to prevent multi-wand spamming and for
        // Disarm to be more powerful.. because Disarm needs to be more
        // powerful :|
        cancelPending(null, false, true, exceptSpellKey);
    }

    @Override
    public boolean isCostFree() {
        // Special case for command blocks, Automata and magic mobs
        if (costFree || getPlayer() == null) return true;
        return getCostReduction() > 1;
    }

    @Override
    public boolean isConsumeFree() {
        return activeWand != null && activeWand.isConsumeFree();
    }

    @Override
    public boolean isSuperProtected() {
        if (superProtectionExpiration != 0) {
            if (System.currentTimeMillis() > superProtectionExpiration) {
                superProtectionExpiration = 0;
            } else {
                return true;
            }
        }
        MageClass activeClass = getActiveClass();
        if (activeClass != null && activeClass.getBoolean("protected")) {
            return true;
        }
        if (offhandCast && offhandWand != null) {
            return offhandWand.isSuperProtected();
        }
        return activeWand != null && activeWand.isSuperProtected();
    }

    @Override
    public boolean isSuperPowered() {
        if (activeClass != null && activeClass.getBoolean("powered")) {
            return true;
        }
        if (offhandCast && offhandWand != null) {
            return offhandWand.isSuperPowered();
        }
        return activeWand != null && activeWand.isSuperPowered();
    }

    @Override
    public float getCostReduction() {
        if (costFree) {
            return 2;
        }
        return costReduction * controller.getMaxCostReduction() + controller.getCostReduction();
    }

    @Override
    public float getConsumeReduction() {
        return consumeReduction;
    }

    @Override
    public float getCostScale() {
        return 1;
    }

    @Override
    public float getCooldownReduction() {
        if (cooldownFree) {
            return 2;
        }
        return cooldownReduction * controller.getMaxCooldownReduction() + controller.getCooldownReduction();
    }

    @Override
    public boolean isCooldownFree() {
        return cooldownFree || getCooldownReduction() > 1;
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
        HeroesManager heroes = controller.getHeroes();
        Player player = getPlayer();
        if (heroes != null && player != null) {
            heroes.clearCooldown(player);
        }
    }

    @Override
    public void setRemainingCooldown(long ms) {
        cooldownExpiration = Math.max(ms + System.currentTimeMillis(), cooldownExpiration);
        HeroesManager heroes = controller.getHeroes();
        Player player = getPlayer();
        if (heroes != null && player != null) {
            heroes.setCooldown(player, ms);
        }
    }

    @Override
    public void reduceRemainingCooldown(long ms) {
        cooldownExpiration = Math.max(0, cooldownExpiration - ms);
        HeroesManager heroes = controller.getHeroes();
        Player player = getPlayer();
        if (heroes != null && player != null) {
            heroes.reduceCooldown(player, ms);
        }
    }

    @Nullable
    @Override
    public Color getEffectColor() {
        if (offhandCast && offhandWand != null) {
            return offhandWand.getEffectColor();
        }
        if (activeWand == null) {
            return properties.getEffectColor();
        }
        return activeWand.getEffectColor();
    }

    @Nullable
    @Override
    public String getEffectParticleName() {
        if (offhandCast && offhandWand != null) {
            return offhandWand.getEffectParticleName();
        }
        if (activeWand == null) return null;
        return activeWand.getEffectParticleName();
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
        if (offhandCast && offhandWand != null) {
            float power = Math.min(controller.getMaxPower(), offhandWand.getPower() + getMagePowerBonus());
            return power * powerMultiplier;
        }
        float power = Math.min(controller.getMaxPower(), activeWand == null ? getMagePowerBonus() : activeWand.getPower() + getMagePowerBonus());
        return power * powerMultiplier;
    }

    @Override
    public float getMagePowerBonus() {
        return magePowerBonus;
    }

    @Override
    public void setMagePowerBonus(float magePowerBonus) {
        this.magePowerBonus = magePowerBonus;
    }

    @Override
    @Deprecated
    public boolean isRestricted(Material material) {
        Player player = getPlayer();
        if (controller.hasBypassPermission(player)) return false;
        if (player != null && player.hasPermission("Magic.bypass_restricted"))
            return false;
        return controller.isRestricted(material);
    }

    @Override
    public boolean isRestricted(Material material, Short data) {
        Player player = getPlayer();
        if (controller.hasBypassPermission(player)) return false;
        if (player != null && player.hasPermission("Magic.bypass_restricted"))
            return false;
        return controller.isRestricted(material, data);
    }

    @Override
    public MageController getController() {
        return controller;
    }

    @Nullable
    @Override
    @Deprecated
    public Set<Material> getRestrictedMaterials() {
        return MaterialSets.toLegacy(getRestrictedMaterialSet());
    }

    @Override
    public MaterialSet getRestrictedMaterialSet() {
        if (isSuperPowered()) {
            return MaterialSets.empty();
        }
        return controller.getRestrictedMaterialSet();
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
        if (isAutomaton) {
            return !isValid();
        }
        return false;
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

    @Nullable
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
    public int removeItem(ItemStack itemStack, boolean allowVariants) {
        if (!isPlayer()) return 0;
        Integer sp = Wand.getSP(itemStack);
        if (sp != null) {
            int currentSP = getSkillPoints();
            int newSP = currentSP - sp;
            if (currentSP < sp) {
                sp = sp - currentSP;
            } else {
                sp = 0;
            }
            setSkillPoints(newSP);
            return sp;
        }
        int amount = itemStack == null ? 0 : itemStack.getAmount();
        Inventory inventory = getInventory();
        ItemStack[] contents = inventory.getContents();
        for (int index = 0; amount > 0 && index < contents.length; index++) {
            ItemStack item = contents[index];
            if (isMatch(itemStack, item, allowVariants)) {
                if (amount >= item.getAmount()) {
                    amount -= item.getAmount();
                    inventory.setItem(index, null);
                } else {
                    item.setAmount(item.getAmount() - amount);
                    amount = 0;
                }
            }
        }

        PlayerInventory playerInventory = getPlayer().getInventory();
        if (amount > 0) {
            ItemStack[] extra = playerInventory.getExtraContents();
            for (int index = 0; amount > 0 && index < extra.length; index++) {
                ItemStack item = extra[index];
                boolean modified = false;
                if (isMatch(itemStack, item, allowVariants)) {
                    if (amount >= item.getAmount()) {
                        amount -= item.getAmount();
                        extra[index] = null;
                        modified = true;
                    } else {
                        item.setAmount(item.getAmount() - amount);
                        amount = 0;
                        modified = true;
                    }
                }
                if (modified) {
                    playerInventory.setExtraContents(extra);
                }
            }
        }

        if (amount > 0) {
            ItemStack[] armor = playerInventory.getArmorContents();
            for (int index = 0; amount > 0 && index < armor.length; index++) {
                ItemStack item = armor[index];
                boolean modified = false;
                if (isMatch(itemStack, item, allowVariants)) {
                    if (amount >= item.getAmount()) {
                        amount -= item.getAmount();
                        armor[index] = null;
                        modified = true;
                    } else {
                        item.setAmount(item.getAmount() - amount);
                        amount = 0;
                        modified = true;
                    }
                }
                if (modified) {
                    playerInventory.setArmorContents(armor);
                }
            }
        }

        return amount;
    }

    @Override
    public int removeItem(ItemStack itemStack) {
        return removeItem(itemStack, false);
    }

    protected boolean isMatch(ItemStack itemStack, ItemStack candidate, boolean allowVariants) {
        if (candidate == null || itemStack == null) {
            return false;
        }
        if (!allowVariants) {
            return controller.itemsAreEqual(itemStack, candidate);
        }
        if (itemStack.getType() != candidate.getType()) {
            return false;
        }
        // Display name still needs to match, if present
        if (itemStack.hasItemMeta() != candidate.hasItemMeta()) {
            return false;
        }
        if (itemStack.hasItemMeta()) {
            ItemMeta meta = itemStack.getItemMeta();
            ItemMeta candidateMeta = candidate.getItemMeta();
            if (meta.hasDisplayName() != candidateMeta.hasDisplayName()) {
                return false;
            }
            if (meta.hasDisplayName() && !meta.getDisplayName().equals(candidateMeta.getDisplayName())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean hasItem(ItemStack itemStack, boolean allowVariants) {
        if (!isPlayer()) return false;
        Integer sp = Wand.getSP(itemStack);
        if (sp != null) {
            return getSkillPoints() >= sp;
        }

        int amount = itemStack == null ? 0 : itemStack.getAmount();
        if (amount <= 0) {
            return true;
        }
        Inventory inventory = getInventory();
        ItemStack[] contents = inventory.getContents();
        for (ItemStack item : contents) {
            if (isMatch(itemStack, item, allowVariants) && (amount -= item.getAmount()) <= 0) {
                return true;
            }
        }

        PlayerInventory playerInventory = getPlayer().getInventory();
        ItemStack[] armor = playerInventory.getArmorContents();
        for (ItemStack item : armor) {
            if (isMatch(itemStack, item, allowVariants) && (amount -= item.getAmount()) <= 0) {
                return true;
            }
        }
        ItemStack[] extra = playerInventory.getExtraContents();
        for (ItemStack item : extra) {
            if (isMatch(itemStack, item, allowVariants) && (amount -= item.getAmount()) <= 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasItem(ItemStack itemStack) {
        return hasItem(itemStack, false);
    }

    @Override
    public int getItemCount(ItemStack itemStack, boolean allowDamaged) {
        if (!isPlayer()) return 0;
        Integer sp = Wand.getSP(itemStack);
        if (sp != null) {
            return getSkillPoints();
        }

        int amount = 0;
        Inventory inventory = getInventory();
        ItemStack[] contents = inventory.getContents();
        for (ItemStack item : contents) {
            if (isMatch(itemStack, item, allowDamaged)) {
                amount += item.getAmount();
            }
        }

        PlayerInventory playerInventory = getPlayer().getInventory();
        ItemStack[] armor = playerInventory.getArmorContents();
        for (ItemStack item : armor) {
            if (isMatch(itemStack, item, allowDamaged)) {
                amount += item.getAmount();
            }
        }
        ItemStack[] extra = playerInventory.getExtraContents();
        for (ItemStack item : extra) {
            if (isMatch(itemStack, item, allowDamaged)) {
                amount += item.getAmount();
            }
        }
        return amount;
    }

    @Override
    public int getItemCount(ItemStack itemStack) {
        return getItemCount(itemStack, false);
    }

    @Nullable
    @Override
    @Deprecated
    public Wand getSoulWand() {
        return null;
    }

    @Override
    public Wand getActiveWand() {
        if (offhandCast && offhandWand != null) {
            return offhandWand;
        }
        return activeWand;
    }

    @Override
    public Wand getOffhandWand() {
        return offhandWand;
    }

    @Override
    public com.elmakers.mine.bukkit.api.block.MaterialBrush getBrush() {
        return brush;
    }

    @Override
    public float getDamageMultiplier() {
        float multiplier = 1.0f;
        float maxPowerMultiplier = controller.getMaxDamagePowerMultiplier() - 1;
        if (maxPowerMultiplier > 0) {
            multiplier = 1.0f + (maxPowerMultiplier * getPower());
        }
        Double overallMultiplier = strength.get("overall");
        if (overallMultiplier != null && overallMultiplier != 0) {
            double attackMultiplier = controller.getMaxAttackMultiplier("overall");
            if (attackMultiplier > 1) {
                attackMultiplier = 1.0 + (attackMultiplier - 1.0) * overallMultiplier;
                multiplier = (float)(multiplier * attackMultiplier);
            }
        }
        return Math.max(0, multiplier);
    }

    @Override
    public double getDamageMultiplier(String damageType) {
        double overallMultiplier = getDamageMultiplier();
        Double typeMultiplier = damageType == null || damageType.isEmpty() ? null : strength.get(damageType);

        if (typeMultiplier != null && typeMultiplier > 0) {
            double attackMultiplier = controller.getMaxAttackMultiplier(damageType);
            if (attackMultiplier > 1) {
                attackMultiplier = 1.0 + (attackMultiplier - 1.0) * typeMultiplier;
                overallMultiplier *= attackMultiplier;
            }
        }
        return Math.max(0, overallMultiplier);
    }

    @Override
    public float getRangeMultiplier() {
        if (activeWand == null) return 1;

        float maxPowerMultiplier = controller.getMaxRangePowerMultiplier() - 1;
        float maxPowerMultiplierMax = controller.getMaxRangePowerMultiplierMax();
        float multiplier = 1.0f + (maxPowerMultiplier * getPower());
        return Math.min(multiplier, maxPowerMultiplierMax);
    }

    @Override
    public float getConstructionMultiplier() {
        float maxPowerMultiplier = controller.getMaxConstructionPowerMultiplier() - 1;
        return 1.0f + (maxPowerMultiplier * getPower());
    }

    @Override
    public float getRadiusMultiplier() {
        if (activeWand == null) return 1;

        float maxPowerMultiplier = controller.getMaxRadiusPowerMultiplier() - 1;
        float maxPowerMultiplierMax = controller.getMaxRadiusPowerMultiplierMax();
        float multiplier = 1.0f + (maxPowerMultiplier * getPower());
        return Math.min(multiplier, maxPowerMultiplierMax);
    }

    @Override
    @Nonnull
    public CasterProperties getActiveProperties() {
        if (offhandCast && offhandWand != null) {
            return offhandWand;
        }
        if (activeWand != null) {
            return activeWand;
        }
        if (activeClass != null) {
            return activeClass;
        }
        return properties;
    }

    @Override
    public float getMana() {
        return getActiveProperties().getMana();
    }

    @Override
    public int getManaMax() {
        return getActiveProperties().getManaMax();
    }

    @Override
    public int getManaRegeneration() {
        return getActiveProperties().getManaRegeneration();
    }

    @Override
    public void setMana(float mana) {
        getActiveProperties().setMana(mana);
    }

    @Override
    public void updateMana() {
        getActiveProperties().updateMana();
    }

    @Override
    public int getEffectiveManaMax() {
        return getActiveProperties().getEffectiveManaMax();
    }

    @Override
    public int getEffectiveManaRegeneration() {
        return getActiveProperties().getEffectiveManaRegeneration();
    }

    @Override
    public void removeMana(float mana) {
        getActiveProperties().removeMana(mana);
    }

    @Override
    public void removeExperience(int xp) {
        Player player = getPlayer();
        if (player == null) return;

        float expProgress = player.getExp();
        int expLevel = player.getLevel();

        while ((expProgress > 0 || expLevel > 0) && xp > 0) {
            if (expProgress > 0) {
                float expToLevel = Wand.getExpToLevel(expLevel);
                int expAtLevel = (int)(expProgress * expToLevel);
                if (expAtLevel > xp) {
                    expAtLevel -= xp;
                    xp = 0;
                    expProgress = expAtLevel / expToLevel;
                } else {
                    expProgress = 0;
                    xp -= expAtLevel;
                }
            } else {
                xp -= Wand.getExpToLevel(expLevel - 1);
                expLevel--;
                if (xp < 0) {
                    expProgress = (float)-xp / Wand.getExpToLevel(expLevel);
                    xp = 0;
                }
            }
        }

        player.setExp(Math.max(0, Math.min(1.0f, expProgress)));
        player.setLevel(Math.max(0, expLevel));
    }

    @Override
    public int getLevel() {
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
    }

    @Override
    public int getExperience() {
        Player player = getPlayer();
        if (player == null) return 0;

        float expProgress = player.getExp();
        int expLevel = player.getLevel();

        return Wand.getExperience(expLevel, expProgress);
    }

    @Override
    public void giveExperience(int xp) {
        Player player = getPlayer();
        if (player != null) {
            player.giveExp(xp);
        }
    }

    public void sendExperience(float exp, int level) {
        if (virtualExperience && exp == virtualExperienceProgress && level == virtualExperienceLevel) return;
        Player player = getPlayer();
        if (player != null) {
            CompatibilityUtils.sendExperienceUpdate(player, exp, level);
            virtualExperience = true;
            virtualExperienceProgress = exp;
            virtualExperienceLevel = level;
        }
    }

    public void resetSentExperience() {
        Player player = getPlayer();
        if (player != null) {
            CompatibilityUtils.sendExperienceUpdate(player, player.getExp(), player.getLevel());
        }
        virtualExperience = false;
    }

    public void experienceChanged() {
        virtualExperience = false;
    }

    @Override
    public boolean addBatch(Batch batch) {
        if (pendingBatches.size() >= controller.getPendingQueueDepth()) {
            controller.getLogger().info("Rejected spell cast for " + getName() + ", already has " + pendingBatches.size()
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

    @Nullable
    @Override
    public Player getPlayer() {
        return isNPC ? null : playerRef.get();
    }

    @Override
    public Entity getEntity() {
        return entityRef.get();
    }

    @Override
    public EntityData getEntityData() {
        return entityData;
    }

    @Nullable
    @Override
    public LivingEntity getLivingEntity() {
        Entity entity = entityRef.get();
        return (entity != null && entity instanceof LivingEntity) ? (LivingEntity) entity : null;
    }

    @Override
    public CommandSender getCommandSender() {
        return commandSenderRef.get();
    }

    @Override
    public List<LostWand> getLostWands() {
        Entity entity = getEntity();
        Collection<LostWand> allWands = controller.getLostWands();
        List<LostWand> mageWands = new ArrayList<>();

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

    @Override
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

    @Override
    public void enableFallProtection(int ms) {
        enableFallProtection(ms, null);
    }

    @Override
    public void enableFallProtection(int ms, @Nullable Spell protector) {
        enableFallProtection(ms, 1, protector);
    }

    @Override
    public void enableFallProtection(int ms, int count, @Nullable Spell protector) {
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

    @Override
    public void clearFallProtection() {
        fallingSpell = null;
        fallProtection = 0;
        fallProtectionCount = 0;
    }

    @Override
    public void enableSuperProtection(int ms) {
        if (ms <= 0) return;

        long nextTime = System.currentTimeMillis() + ms;
        if (nextTime > superProtectionExpiration) {
            superProtectionExpiration = nextTime;
        }
    }

    @Override
    public void clearSuperProtection() {
        superProtectionExpiration = 0;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    @Override
    public void disable() {
        // Kind of a hack, but the loading flag will prevent the Mage from doing anything further
        this.loading = true;
    }

    @Override
    public boolean isLoading() {
        return loading;
    }

    public void setUnloading(boolean unloading) {
        this.unloading = unloading;
    }

    public boolean isUnloading() {
        return unloading;
    }

    @Override
    public boolean hasPending() {
        if (undoQueue != null && undoQueue.hasScheduled()) return true;
        if (hasPendingBatches()) return true;

        return false;
    }

    @Override
    public boolean isValid() {
        if (forget) return false;
        if (!hasEntity) return true;
        Entity entity = getEntity();

        if (entity == null) return false;
        if (!isNPC && entity instanceof Player) {
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
        return entity.isValid();
    }

    @Override
    public boolean restoreWand() {
        if (boundWands.size() == 0) return false;
        Player player = getPlayer();
        if (player == null) return false;
        Set<String> foundTemplates = new HashSet<>();
        ItemStack[] inventory = getInventory().getContents();
        for (ItemStack item : inventory) {
            if (Wand.isWand(item)) {
                Wand tempWand = controller.getWand(item);
                String template = tempWand.getTemplateKey();
                if (template != null) {
                    foundTemplates.add(template);
                }
            }
        }
        inventory = player.getEnderChest().getContents();
        for (ItemStack item : inventory) {
            if (Wand.isWand(item)) {
                Wand tempWand = controller.getWand(item);
                String template = tempWand.getTemplateKey();
                if (template != null) {
                    foundTemplates.add(template);
                }
            }
        }

        int givenWands = 0;
        for (Map.Entry<String, Wand> wandEntry : boundWands.entrySet()) {
            String templateKey = wandEntry.getKey();
            if (foundTemplates.contains(templateKey)) continue;
            WandTemplate template = controller.getWandTemplate(templateKey);
            Wand wand = wandEntry.getValue();
            if (template == null || !wand.isBound()) continue;

            givenWands++;
            ItemStack wandItem = wand.duplicate().getItem();
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
    public boolean isJumping() {
        Entity entity = getEntity();
        return (entity != null && !entity.isOnGround());
    }

    @Override
    public ConfigurationSection getData() {
        if (loading) {
            return new MemoryConfiguration();
        }
        return data;
    }

    public void onGUIDeactivate()
    {
        GUIAction previousGUI = gui;
        gui = null;
        Player player = getPlayer();
        if (player != null) {
            DeprecatedUtils.updateInventory(player);
        }

        if (previousGUI != null)
        {
            previousGUI.deactivated();
        }
    }

    @Override
    public void activateGUI(GUIAction action, Inventory inventory)
    {
        Player player = getPlayer();
        if (player != null)
        {
            controller.disableItemSpawn();
            try {
                player.closeInventory();
                if (inventory != null) {
                    gui = action;
                    player.openInventory(inventory);
                }
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
            controller.enableItemSpawn();
        }
        gui = action;
    }

    @Override
    public void continueGUI(GUIAction action, Inventory inventory)
    {
        Player player = getPlayer();
        if (player != null)
        {
            controller.disableItemSpawn();
            try {
                if (inventory != null) {
                    gui = action;
                    player.openInventory(inventory);
                }
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
            controller.enableItemSpawn();
        }
        gui = action;
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
    public void debugPermissions(CommandSender sender, Spell spell) {
        com.elmakers.mine.bukkit.api.wand.Wand wand = getActiveWand();
        Location location = getLocation();
        if (spell == null && wand != null) {
            spell = wand.getActiveSpell();
        }
        sender.sendMessage(ChatColor.GOLD + "Permission check for " + ChatColor.AQUA + getDisplayName());
        sender.sendMessage(ChatColor.GOLD + "  id " + ChatColor.DARK_AQUA + getId());
        sender.sendMessage(ChatColor.GOLD + " at " + ChatColor.AQUA
                + ChatColor.BLUE + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ()
                + " " + ChatColor.DARK_BLUE + location.getWorld().getName());

        Player player = getPlayer();
        boolean hasBypass = false;
        boolean hasPVPBypass = false;
        boolean hasBuildBypass = false;
        boolean hasBreakBypass = false;
        if (player != null) {
            hasBypass = controller.hasBypassPermission(player);
            hasPVPBypass = player.hasPermission("Magic.bypass_pvp");
            hasBuildBypass = player.hasPermission("Magic.bypass_build");
            sender.sendMessage(ChatColor.AQUA + " Has bypass: " + formatBoolean(hasBypass, true, null));
            sender.sendMessage(ChatColor.AQUA + " Has PVP bypass: " + formatBoolean(hasPVPBypass, true, null));
            sender.sendMessage(ChatColor.AQUA + " Has Build bypass: " + formatBoolean(hasBuildBypass, true, null));
            sender.sendMessage(ChatColor.AQUA + " Has Break bypass: " + formatBoolean(hasBreakBypass, true, null));
        }

        boolean buildPermissionRequired = spell == null ? false : spell.requiresBuildPermission();
        boolean breakPermissionRequired = spell == null ? false : spell.requiresBreakPermission();
        boolean pvpRestricted = spell == null ? false : spell.isPvpRestricted();
        sender.sendMessage(ChatColor.AQUA + " Can build: " + formatBoolean(hasBuildPermission(location.getBlock()), hasBuildBypass || !buildPermissionRequired ? null : true));
        sender.sendMessage(ChatColor.AQUA + " Can break: " + formatBoolean(hasBreakPermission(location.getBlock()), hasBreakBypass || !breakPermissionRequired ? null : true));
        sender.sendMessage(ChatColor.AQUA + " Can pvp: " + formatBoolean(isPVPAllowed(location), hasPVPBypass || !pvpRestricted ? null : true));
        boolean isPlayer = player != null;
        boolean spellDisguiseRestricted = (spell == null) ? false : spell.isDisguiseRestricted();
        sender.sendMessage(ChatColor.AQUA + " Is disguised: " + formatBoolean(controller.isDisguised(getEntity()), null, isPlayer && spellDisguiseRestricted ? true : null));
        WorldBorder border = location.getWorld().getWorldBorder();
        double borderSize = border.getSize();

        // Kind of a hack, meant to prevent this from showing up when there's no border defined
        if (borderSize < 50000000)
        {
            borderSize = borderSize / 2 - border.getWarningDistance();
            Location offset = location.subtract(border.getCenter());
            boolean isOutsideBorder = (offset.getX() < -borderSize || offset.getX() > borderSize || offset.getZ() < -borderSize || offset.getZ() > borderSize);
            sender.sendMessage(ChatColor.AQUA + " Is in world border (" + ChatColor.GRAY + borderSize + ChatColor.AQUA + "): " + formatBoolean(!isOutsideBorder, true, false));
        }

        if (spell != null)
        {
            sender.sendMessage(ChatColor.AQUA + " Has pnode " + ChatColor.GOLD + spell.getPermissionNode() + ChatColor.AQUA + ": " + formatBoolean(spell.hasCastPermission(player), hasBypass ? null : true));
            sender.sendMessage(ChatColor.AQUA + " Region override: " + formatBoolean(controller.getRegionCastPermission(player, spell, location), hasBypass ? null : true));
            sender.sendMessage(ChatColor.AQUA + " Field override: " + formatBoolean(controller.getPersonalCastPermission(player, spell, location), hasBypass ? null : true));
            com.elmakers.mine.bukkit.api.block.MaterialBrush brush = spell.getBrush();
            if (brush != null) {
                sender.sendMessage(ChatColor.GOLD + " " + spell.getName() + ChatColor.AQUA + " is erase: " + formatBoolean(brush.isErase(), null));
            }
            sender.sendMessage(ChatColor.GOLD + " " + spell.getName() + ChatColor.AQUA + " requires build: " + formatBoolean(spell.requiresBuildPermission(), null, true, true));
            sender.sendMessage(ChatColor.GOLD + " " + spell.getName() + ChatColor.AQUA + " requires break: " + formatBoolean(spell.requiresBreakPermission(), null, true, true));
            sender.sendMessage(ChatColor.GOLD + " " + spell.getName() + ChatColor.AQUA + " requires pvp: " + formatBoolean(spell.isPvpRestricted(), null, true, true));
            sender.sendMessage(ChatColor.GOLD + " " + spell.getName() + ChatColor.AQUA + " allowed while disguised: " + formatBoolean(!spell.isDisguiseRestricted(), null, false, true));
            if (spell instanceof BaseSpell)
            {
                boolean buildPermission = ((BaseSpell)spell).hasBuildPermission(location.getBlock());
                sender.sendMessage(ChatColor.GOLD + " " + spell.getName() + ChatColor.AQUA + " has build: " + formatBoolean(buildPermission, hasBuildBypass || !spell.requiresBuildPermission() ? null : true));
                boolean breakPermission = ((BaseSpell)spell).hasBreakPermission(location.getBlock());
                sender.sendMessage(ChatColor.GOLD + " " + spell.getName() + ChatColor.AQUA + " has break: " + formatBoolean(breakPermission, hasBreakBypass || !spell.requiresBreakPermission() ? null : true));
            }
            sender.sendMessage(ChatColor.AQUA + " Can cast " + ChatColor.GOLD + spell.getName() + ChatColor.AQUA + ": " + formatBoolean(spell.canCast(location)));
        }
    }

    public static String formatBoolean(Boolean flag, Boolean greenState)
    {
        return formatBoolean(flag, greenState, greenState == null ? null : !greenState, false);
    }

    public static String formatBoolean(Boolean flag)
    {
        return formatBoolean(flag, true, false, false);
    }

    public static String formatBoolean(Boolean flag, Boolean greenState, Boolean redState)
    {
        return formatBoolean(flag, greenState, redState, false);
    }

    public static String formatBoolean(Boolean flag, Boolean greenState, Boolean redState, boolean dark)
    {
        if (flag == null) {
            return ChatColor.GRAY + "none";
        }
        String text = flag ? "true" : "false";
        if (greenState != null && Objects.equal(flag, greenState)) {
            return (dark ? ChatColor.DARK_GREEN : ChatColor.GREEN) + text;
        } else if (redState != null && Objects.equal(flag, redState)) {
            return (dark ? ChatColor.DARK_RED : ChatColor.RED) + text;
        }
        return ChatColor.GRAY + text;
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
        }
        if (respawnInventory != null) {
            List<ItemStack> addToInventory = null;
            for (Map.Entry<Integer, ItemStack> entry : respawnInventory.entrySet()) {
                int slot = entry.getKey();
                ItemStack item = entry.getValue();
                if (slot >= 0) {
                    inventory.setItem(entry.getKey(), item);
                } else {
                    if (addToInventory == null) {
                        addToInventory = new ArrayList<>();
                    }
                    addToInventory.add(item);
                }
            }
            if (addToInventory != null) {
                for (ItemStack item : addToInventory) {
                    Map<Integer, ItemStack> returned = inventory.addItem(item);
                    if (!returned.isEmpty()) {
                        player.getWorld().dropItem(player.getLocation(), item);
                    }
                }
            }
        }
        clearRespawnInventories();
        controller.getPlugin().getServer().getScheduler().runTaskLater(controller.getPlugin(), new Runnable() {
            @Override
            public void run() {
                armorUpdated();
            }
        }, 1);
    }


    public void addToRespawnInventory(ItemStack item) {
        addToRespawnInventory(-1, item);
    }

    public void addToRespawnInventory(int slot, ItemStack item) {
        if (respawnInventory == null) {
            respawnInventory = new HashMap<>();
        }
        respawnInventory.put(slot, item);
    }

    public void addToRespawnArmor(int slot, ItemStack item) {
        if (respawnArmor == null) {
            respawnArmor = new HashMap<>();
        }
        respawnArmor.put(slot, item);
    }

    @Override
    public void setArmorItem(int armorSlot, ItemStack itemStack) {
        Player player = getPlayer();
        if (player == null) return;
        if (player.isDead()) {
            addToRespawnArmor(armorSlot, itemStack);
            return;
        }

        ItemStack[] armor = player.getInventory().getArmorContents();
        armor[armorSlot] = itemStack;
        player.getInventory().setArmorContents(armor);
    }

    @Override
    public void giveItem(ItemStack itemStack, boolean putInHand) {
        if (!tryGiveItem(itemStack, putInHand)) {
            Entity entity = getEntity();
            if (entity != null) {
                entity.getWorld().dropItem(entity.getLocation(), itemStack);
            }
        }
    }

    @Override
    public void giveItem(ItemStack itemStack) {
        if (!tryGiveItem(itemStack)) {
            Entity entity = getEntity();
            if (entity != null) {
                entity.getWorld().dropItem(entity.getLocation(), itemStack);
            }
        }
    }

    @Override
    public boolean tryGiveItem(ItemStack itemStack, boolean putInHand) {
        if (putInHand) {
            return tryGiveItem(itemStack);
        } else {
            Player player = getPlayer();
            // See note in other version of tryGiveItem, not sure this is "right" but is probably "best".
            if (player == null) return true;

            if (player.isDead()) {
                addToRespawnInventory(itemStack);
                return true;
            }

            PlayerInventory inventory = player.getInventory();
            ItemStack inHand = inventory.getItemInMainHand();
            Integer freeSlot = null;
            if (InventoryUtils.isEmpty(inHand)) {
                for (int i = 0; i < inventory.getSize() && freeSlot == null; i++) {
                    if (i != inventory.getHeldItemSlot() && InventoryUtils.isEmpty(inventory.getItem(i))) {
                        freeSlot = i;
                    }
                }
            }
            if (freeSlot == null) {
                return tryGiveItem(itemStack);
            } else {
                inventory.setItem(freeSlot, itemStack);
            }
        }
        return true;
    }

    @Override
    public boolean tryGiveItem(ItemStack itemStack) {
        if (InventoryUtils.isEmpty(itemStack)) return true;

        // Check for wand upgrades if appropriate
        Wand activeWand = getActiveWand();
        if (activeWand != null) {
            if (activeWand.addItem(itemStack)) {
                 return true;
            }
        }

        Player player = getPlayer();
        // Should this return false ?
        // That'd be a change from previous behavior, giving items to mobs now means dropping items on the ground,
        // which we probably don't want.
        if (player == null) return true;

        // Bind item if configured to do so
        if (controller.isBindOnGive() && Wand.isWand(itemStack)) {
            Wand wand = controller.getWand(itemStack);
            if (wand.isBound()) {
                wand.tryToOwn(player);
                itemStack = wand.getItem();
            }
        }

        if (player.isDead()) {
            addToRespawnInventory(itemStack);
            return true;
        }

        if (hasStoredInventory()) {
            return addToStoredInventory(itemStack);
        }

        // Place directly in hand if possible
        PlayerInventory inventory = player.getInventory();
        ItemStack inHand = inventory.getItemInMainHand();
        if (inHand == null || inHand.getType() == Material.AIR) {
            inventory.setItemInMainHand(itemStack);
            // Get the new item reference -
            // it might change when added to an Inventory! :|
            itemStack = inventory.getItemInMainHand();
            if (Wand.isWand(itemStack)) {
                checkWand();
            } else {
                if (DefaultMaterials.isFilledMap(itemStack.getType())) {
                    setLastHeldMapId(InventoryUtils.getMapId(itemStack));
                }
            }
        } else {
            HashMap<Integer, ItemStack> returned = player.getInventory().addItem(itemStack);
            return returned.isEmpty();
        }
        return true;
    }

    public void armorUpdated() {
        activeArmor.clear();
        Player player = getPlayer();
        if (player != null)
        {
            boolean changed = false;
            ItemStack[] armor = player.getInventory().getArmorContents();
            for (int index = 0; index < armor.length; index++) {
                ItemStack armorItem = armor[index];
                if (InventoryUtils.isEmpty(armorItem)) continue;

                // Check for locked items
                if (!canUse(armorItem)) {
                    sendMessage(controller.getMessages().get("mage.no_class").replace("$name", controller.describeItem(armorItem)));
                    changed = true;
                    armor[index] = null;
                    giveItem(armorItem);
                    continue;
                }

                if (Wand.isWand(armorItem)) {
                    activeArmor.put(index, controller.getWand(armorItem));
                }
            }

            if (changed) {
                player.getInventory().setArmorContents(armor);
            }
        }

        if (activeWand != null) {
            activeWand.armorUpdated();
        } else if (activeClass != null) {
            activeClass.armorUpdated();
        }
        updatePassiveEffects();
    }

    protected void addPassiveEffectsGroup(Map<String, Double> properties, CasterProperties addProperties, String section, boolean stack, Double maxValue) {
       ConfigurationSection addSection = addProperties.getConfigurationSection(section);
       if (addSection != null) {
           Set<String> sectionTypes = addSection.getKeys(false);
            for (String sectionType : sectionTypes) {
                Double existing = properties.get(sectionType);
                if (existing == null) {
                    existing = 0.0;
                }
                double addValue = addSection.getDouble(sectionType);
                if (stack) {
                    if (maxValue != null) {
                        existing = Math.min(1, existing + addValue);
                    } else {
                        existing = existing + addValue;
                    }
                } else {
                    existing = Math.max(existing, addValue);
                }
                properties.put(sectionType, existing);
            }
       }
    }

    protected void addPassiveEffects(CasterProperties properties, boolean activeReduction) {
        earnMultiplier = (float) (earnMultiplier * properties.getDouble("earn_multiplier", properties.getDouble("sp_multiplier", 1.0)));

        boolean stack = properties.getBoolean("stack", false);
        addPassiveEffectsGroup(protection, properties, "protection", stack, 1.0);
        addPassiveEffectsGroup(weakness, properties, "weakness", stack, 1.0);
        addPassiveEffectsGroup(strength, properties, "strength", stack, 1.0);
        addPassiveEffectsGroup(attributes, properties, "attributes", stack, null);

        if (activeReduction || properties.getBoolean("passive") || stack) {
            if (stack) {
                cooldownReduction = stackValue(cooldownReduction, properties.getFloat("cooldown_reduction", 0));
                costReduction = stackValue(costReduction, properties.getFloat("cost_reduction", 0));
                consumeReduction = stackValue(consumeReduction, properties.getFloat("consume_reduction", 0));
            } else {
                cooldownReduction = Math.max(cooldownReduction, properties.getFloat("cooldown_reduction", 0));
                costReduction = Math.max(costReduction, properties.getFloat("cost_reduction", 0));
                consumeReduction = Math.max(consumeReduction, properties.getFloat("consume_reduction", 0));
            }
        }

        // Iterate over all spells and compile triggers
        for (String spellKey : properties.getSpells()) {
            if (triggeredSpells.contains(spellKey)) continue;
            Spell spell = getSpell(spellKey);
            if (spell == null) continue;
            triggeredSpells.add(spellKey);
            Collection<Trigger> spellTriggers = spell.getTriggers();
            for (Trigger trigger : spellTriggers) {
                String triggerType = trigger.getTrigger();
                List<TriggeredSpell> typeTriggers = triggers.get(triggerType);
                if (typeTriggers == null) {
                    typeTriggers = new ArrayList<>();
                    triggers.put(triggerType, typeTriggers);
                }
                typeTriggers.add(new TriggeredSpell(spellKey, trigger));
            }
        }
    }

    protected float stackValue(float currentValue, float stackValue) {
        return Math.min(1, stackValue + currentValue);
    }

    @Override
    public void updatePassiveEffects() {
        protection.clear();
        strength.clear();
        weakness.clear();
        attributes.clear();

        // Try to avoid constantly re-creating these, don't clear the whole map
        for (List<TriggeredSpell> triggerList : triggers.values()) {
            triggerList.clear();
        }
        triggeredSpells.clear();

        earnMultiplier = 1;
        cooldownReduction = 0;
        costReduction = 0;
        consumeReduction = 0;

        List<PotionEffectType> currentEffects = new ArrayList<>(effectivePotionEffects.keySet());
        LivingEntity entity = getLivingEntity();
        effectivePotionEffects.clear();

        addPassiveEffects(properties, true);
        if (activeClass != null)
        {
            addPassiveEffects(activeClass, true);
        }
        for (MageClass mageClass : classes.values()) {
            if (mageClass != activeClass && !mageClass.isLocked() && mageClass.getBoolean("passive")) {
                addPassiveEffects(mageClass, true);
            }
        }
        for (MageModifier modifier : modifiers.values()) {
            addPassiveEffects(modifier, true);
        }

        if (activeWand != null && !activeWand.isPassive())
        {
            addPassiveEffects(activeWand, false);
            effectivePotionEffects.putAll(activeWand.getPotionEffects());
        }
        // Don't add these together so things stay balanced!
        if (offhandWand != null && !offhandWand.isPassive())
        {
            addPassiveEffects(offhandWand, false);
            effectivePotionEffects.putAll(offhandWand.getPotionEffects());
        }
        for (Wand armorWand : activeArmor.values())
        {
            if (armorWand != null) {
                addPassiveEffects(armorWand, false);
                effectivePotionEffects.putAll(armorWand.getPotionEffects());
            }
        }

        if (entity != null)
        {
            for (PotionEffectType effectType : currentEffects) {
                if (!effectivePotionEffects.containsKey(effectType)) {
                    entity.removePotionEffect(effectType);
                }
            }
            for (Map.Entry<PotionEffectType, Integer> effects : effectivePotionEffects.entrySet()) {
                PotionEffect effect = new PotionEffect(effects.getKey(), Integer.MAX_VALUE, effects.getValue(), true, false);
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
        deactivateWand();
        deactivateAllSpells(true, true);
        removeActiveEffects();
    }

    @Override
    public void undoScheduled() {
        // Immediately rollback any auto-undo spells
        if (undoQueue != null) {
            int undid = undoQueue.undoScheduled();
            if (undid != 0) {
                controller.info("Player " + getName() + " logging out, auto-undid " + undid + " spells");
            }
        }

        int finished = finishPendingUndo();
        if (finished != 0) {
            controller.info("Player " + getName() + " logging out, fast-forwarded undo for " + finished + " spells");
        }

        if (undoQueue != null) {
            if (!undoQueue.isEmpty()) {
                if (controller.commitOnQuit()) {
                    controller.info("Player logging out, committing constructions: " + getName());
                    undoQueue.commit();
                } else {
                    controller.info("Player " + getName() + " logging out with " + undoQueue.getSize() + " spells in their undo queue");
                }
            }
        }
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

    @Override
    public boolean isAtMaxSkillPoints() {
        return getSkillPoints() >= controller.getSPMaximum();
    }

    @Nullable
    private Currency initCurrency(String type) {
        Currency currency = controller.getCurrency(type);
        if (!data.contains(type)) {
            data.set(type, currency == null ? 0.0 : currency.getDefaultValue());
        }
        return currency;
    }

    @Override
    public double getCurrency(String type) {
        Currency currency = controller.getCurrency(type);
        return data.getDouble(type, currency == null ? 0.0 : currency.getDefaultValue());
    }

    @Override
    public void addCurrency(String type, double delta) {
        boolean firstEarn = !data.contains(type);
        Currency currency = initCurrency(type);
        double newValue = data.getDouble(type) + delta;
        if (currency != null && currency.hasMaxValue()) {
            newValue = Math.min(newValue, currency.getMaxValue());
        }

        Messages messages = controller.getMessages();
        String earnMessage = messages.get("currency." + type + ".earned", messages.get("currency.earned"));
        if (delta > 0 && earnMessage != null && !earnMessage.isEmpty()) {
            String amountString = messages.get("currency." + type + ".amount", type).replace("$amount",Integer.toString((int)Math.ceil(delta)));
            sendMessage(earnMessage.replace("$earned", amountString));
        }

        setCurrency(type, newValue);
        if (activeWand != null && Wand.currencyMode != WandManaMode.NONE && activeWand.usesCurrency(type)) {
            if (firstEarn && currency != null) {
                sendMessage(activeWand.getMessage("earn_instructions").replace("$currency", currency.getName(controller.getMessages())));
            }

            activeWand.updateMana();
        }
    }

    @Override
    public void removeCurrency(String type, double delta) {
        initCurrency(type);
        double current = data.getDouble(type);
        data.set(type, current - delta);
    }

    @Override
    public void setCurrency(String type, double amount) {
        data.set(type, amount);
    }

    @Override
    public boolean isAtMaxCurrency(String type) {
        Currency currency = controller.getCurrency(type);
        if (currency == null || !currency.hasMaxValue()) {
            return false;
        }
        double value = getCurrency(type);
        return value >= currency.getMaxValue();
    }

    @Override
    public int getSkillPoints() {
        if (!data.contains(SKILL_POINT_KEY)) {
            data.set(SKILL_POINT_KEY, DEFAULT_SP);
        }
        // .. I thought Configuration section would auto-convert? I guess not!
        if (data.isString(SKILL_POINT_KEY)) {
            try {
                data.set(SKILL_POINT_KEY, Integer.parseInt(data.getString(SKILL_POINT_KEY)));
            } catch (Exception ex) {
                data.set(SKILL_POINT_KEY, 0);
            }
        }
        return data.getInt(SKILL_POINT_KEY);
    }

    @Override
    public void addSkillPoints(int delta) {
        int current = getSkillPoints();
        if (isPlayer()) {
            String earnMessage = controller.getMessages().get("mage.earned_sp");
            if (delta > 0 && earnMessage != null && !earnMessage.isEmpty()) {
                sendMessage(earnMessage.replace("$amount", Integer.toString(delta)));
            }
        }
        setSkillPoints(current + delta);
    }

    @Override
    public void setSkillPoints(int amount) {
        // We don't allow negative skill points.
        boolean firstEarn = !data.contains(SKILL_POINT_KEY);
        amount = Math.max(amount, 0);
        int limit = controller.getSPMaximum();
        if (limit > 0) {
            amount = Math.min(amount, limit);
        }
        data.set(SKILL_POINT_KEY, amount);

        if (activeWand != null && Wand.currencyMode != WandManaMode.NONE && activeWand.usesSP()) {
            if (firstEarn) {
                sendMessage(activeWand.getMessage("sp_instructions"));
            }
            activeWand.updateMana();
        }
    }

    @Override
    public Wand getBoundWand(String template) {
        return boundWands.get(template);
    }

    @Nullable
    @Override
    public WandUpgradePath getBoundWandPath(String templateKey) {
        com.elmakers.mine.bukkit.api.wand.Wand boundWand = boundWands.get(templateKey);
        if (boundWand != null) {
            return boundWand.getPath();
        }

        return null;
    }

    public void setEntityData(EntityData entityData) {
        this.entityData = entityData;
        controller.registerMagicMob(this);

        ConfigurationSection mageProperties = entityData.getMageProperties();
        if (mageProperties != null) {
            ConfigurationUtils.addConfigurations(properties.getConfiguration(), mageProperties);
            properties.loadProperties();
            updatePassiveEffects();
        }

        // Only Magic Mobs tracks damage by player, for now
        damagedBy = new HashMap<>();
    }

    @Override
    public List<Wand> getBoundWands() {
        return ImmutableList.copyOf(boundWands.values());
    }

    public void updateHotbarStatus() {
        Player player = getPlayer();
        if (player != null) {
            Location location = getLocation();
            boolean isWandInventory = hasStoredInventory();
            for (int i = 0; i < Wand.HOTBAR_SIZE; i++) {
                ItemStack spellItem = player.getInventory().getItem(i);
                String spellKey = Wand.getSpell(spellItem);
                String classKey = Wand.getSpellClass(spellItem);
                boolean isSkill = Wand.isSkill(spellItem);
                if (spellKey != null && (isSkill || isWandInventory)) {
                    Spell spell = getSpell(spellKey);
                    if (spell != null) {
                        int targetAmount = 1;
                        long remainingCooldown = 0;
                        CastingCost requiredCost = null;
                        boolean canCastSpell = false;
                        if (classKey != null && !classKey.isEmpty()) {
                            MageClass mageClass = getClass(classKey);
                            if (mageClass != null && spell instanceof BaseSpell) {
                                BaseSpell baseSpell = (BaseSpell)spell;
                                baseSpell.setMageClass(mageClass);
                                remainingCooldown = spell.getRemainingCooldown();
                                requiredCost = spell.getRequiredCost();
                                canCastSpell = spell.canCast(location);
                                baseSpell.setMageClass(null);
                            }
                        } else {
                            remainingCooldown = spell.getRemainingCooldown();
                            requiredCost = spell.getRequiredCost();
                            canCastSpell = spell.canCast(location);
                        }

                        boolean canCast = canCastSpell;
                        if (canCastSpell && remainingCooldown == 0 && requiredCost == null) {
                            targetAmount = 1;
                        } else if (canCastSpell) {
                            canCast = remainingCooldown == 0;
                            targetAmount = Wand.LiveHotbarCooldown ? (int)Math.min(Math.ceil((double)remainingCooldown / 1000), 99) : 99;
                            if (Wand.LiveHotbarCooldown && Wand.LiveHotbarMana && requiredCost != null) {
                                int mana = requiredCost.getMana();
                                if (mana > 0) {
                                    if (mana <= getEffectiveManaMax() && getEffectiveManaRegeneration() > 0) {
                                        float remainingMana = mana - getMana();
                                        canCast = canCast && remainingMana <= 0;
                                        int targetManaTime = (int)Math.min(Math.ceil(remainingMana / getEffectiveManaRegeneration()), 99);
                                        targetAmount = Math.max(targetManaTime, targetAmount);
                                    } else {
                                        canCastSpell = false;
                                        canCast = false;
                                    }
                                }
                            }
                        }
                        if (targetAmount == 0) targetAmount = 1;
                        boolean setAmount = false;

                        MaterialAndData disabledIcon = spell.getDisabledIcon();
                        MaterialAndData spellIcon = spell.getIcon();
                        String urlIcon = spell.getIconURL();
                        String disabledUrlIcon = spell.getDisabledIconURL();
                        boolean usingURLIcon = (controller.isUrlIconsEnabled() || spellIcon == null || spellIcon.getMaterial() == Material.AIR) && urlIcon != null && !urlIcon.isEmpty();
                        if (disabledIcon != null && spellIcon != null && !usingURLIcon) {
                            if (!canCast || !spell.isEnabled()) {
                                if (disabledIcon.isValid() && (disabledIcon.getMaterial() != spellItem.getType() || disabledIcon.getData() != spellItem.getDurability())) {
                                    disabledIcon.applyToItem(spellItem);
                                }
                                if (!canCastSpell) {
                                    if (spellItem.getAmount() != 1) {
                                        spellItem.setAmount(1);
                                    }
                                    setAmount = true;
                                }
                            } else {
                                if (spellIcon.isValid() && (spellIcon.getMaterial() != spellItem.getType() || spellIcon.getData() != spellItem.getDurability())) {
                                    spellIcon.applyToItem(spellItem);
                                }
                            }
                        } else if (usingURLIcon && disabledUrlIcon != null && !disabledUrlIcon.isEmpty() && DefaultMaterials.isSkull(spellItem.getType())) {
                            String currentURL = InventoryUtils.getSkullURL(spellItem);
                            if (!canCast) {
                                if (!disabledUrlIcon.equals(currentURL)) {
                                    spellItem = InventoryUtils.setSkullURL(spellItem, disabledUrlIcon);
                                    player.getInventory().setItem(i, spellItem);
                                }
                                if (!canCastSpell) {
                                    if (spellItem.getAmount() != 1) {
                                        spellItem.setAmount(1);
                                    }
                                    setAmount = true;
                                }
                            } else {
                                if (!urlIcon.equals(currentURL)) {
                                    spellItem = InventoryUtils.setSkullURL(spellItem, urlIcon);
                                    player.getInventory().setItem(i, spellItem);
                                }
                            }
                        }

                        if (!setAmount && spellItem.getAmount() != targetAmount) {
                            spellItem.setAmount(targetAmount);
                        }
                    }
                }
            }
        }
    }

    public long getLastBlockTime() {
        return lastBlockTime;
    }

    public void setLastBlockTime(long ms) {
        lastBlockTime = ms;
    }

    @Override
    public boolean isReflected(double angle) {
        if (activeWand != null && activeWand.isReflected(angle)) {
            return true;
        }
        if (offhandWand != null && offhandWand.isReflected(angle)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isBlocked(double angle) {
        if (activeWand != null && activeWand.isBlocked(angle)) {
            return true;
        }
        if (offhandWand != null && offhandWand.isBlocked(angle)) {
            return true;
        }
        return false;
    }

    @Override
    @Deprecated
    public float getSPMultiplier() {
        return earnMultiplier;
    }

    @Override
    public float getEarnMultiplier() {
        return earnMultiplier;
    }

    @Override
    public @Nonnull MageProperties getProperties() {
        return properties;
    }

    @Override
    public double getVehicleMovementDirection() {
        LivingEntity li = getLivingEntity();
        if (li == null) return 0.0f;
        return CompatibilityUtils.getForwardMovement(li);
    }

    @Override
    public double getVehicleStrafeDirection() {
        LivingEntity li = getLivingEntity();
        if (li == null) return 0.0f;
        return CompatibilityUtils.getStrafeMovement(li);
    }

    @Override
    public boolean isVehicleJumping() {
        LivingEntity li = getLivingEntity();
        if (li == null) return false;
        return CompatibilityUtils.isJumping(li);
    }

    @Override
    public void setVanished(boolean vanished) {
        Player thisPlayer = getPlayer();
        if (thisPlayer != null && isVanished != vanished) {
            controller.setVanished(this, vanished);
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (vanished) {
                    DeprecatedUtils.hidePlayer(controller.getPlugin(), player, thisPlayer);
                } else {
                    DeprecatedUtils.showPlayer(controller.getPlugin(), player, thisPlayer);
                }
            }
        }
        isVanished = vanished;
    }

    @Override
    public boolean isVanished() {
        return isVanished;
    }

    @Override
    public void setGlidingAllowed(boolean allow) {
        glidingAllowed = allow;
        Player player = getPlayer();
        if (player != null) {
            controller.addFlightExemption(player, 5000);
        }
    }

    @Override
    public boolean isGlidingAllowed() {
        return glidingAllowed;
    }

    public boolean isForget() {
        return forget;
    }

    public void setForget(boolean forget) {
        this.forget = forget;
    }

    @Override
    public double getVaultBalance() {
        if (!VaultController.hasEconomy() || !isPlayer()) return 0;
        return VaultController.getInstance().getBalance(getPlayer());
    }

    @Override
    public boolean addVaultCurrency(double delta) {
        if (!VaultController.hasEconomy() || !isPlayer()) return false;
        VaultController.getInstance().depositPlayer(getPlayer(), delta);
        return true;
    }

    @Override
    public boolean removeVaultCurrency(double delta) {
        if (!VaultController.hasEconomy() || !isPlayer()) return false;
        VaultController.getInstance().withdrawPlayer(getPlayer(), delta);
        return true;
    }

    public void setIsAutomaton(boolean automaton) {
        this.isAutomaton = automaton;
    }

    @Override
    public boolean isAutomaton() {
        return isAutomaton;
    }

    @Override
    public void addTag(String tag) {
        tags.add(tag);
    }

    @Override
    public boolean hasTag(String tag) {
        return tags.contains(tag);
    }

    public void setOpenCooldown(long openCooldown) {
        if (openCooldown > 0) {
            disableWandOpenUntil = System.currentTimeMillis() + openCooldown;
        }
    }

    public long getWandDisableTime() {
        return disableWandOpenUntil;
    }

    public Integer getLastActivatedSlot() {
        return lastActivatedSlot;
    }

    public void setLastActivatedSlot(int slot) {
        this.lastActivatedSlot = slot;
    }

    @Nullable
    @Override
    public Double getAttribute(String attributeKey) {
        Double attribute = attributes.get(attributeKey);
        if (attribute == null) {
            MagicAttribute defaultSetting = controller.getAttribute(attributeKey);
            if (defaultSetting != null) {
                attribute = defaultSetting.getDefault();
            }
        }
        if (attribute == null) {
            Player player = getPlayer();
            if (player != null) {
                List<AttributeProvider> providers = controller.getAttributeProviders();
                for (AttributeProvider provider : providers) {
                    Double value = provider.getAttributeValue(attributeKey, player);
                    if (value != null) {
                        attribute = value;
                        break;
                    }
                }
            }
        }

        if (attribute == null) {
            attribute = getBuiltinAttribute(attributeKey);
        }

        return attribute;
    }

    @Nullable
    private Double getBuiltinAttribute(String attributeKey) {
        switch (attributeKey) {
            case "health": {
                LivingEntity living = getLivingEntity();
                return living == null ? null : living.getHealth();
            }
            case "health_max": {
                LivingEntity living = getLivingEntity();
                return living == null ? null : CompatibilityUtils.getMaxHealth(living);
            }
            case "mana": {
                return (double)getMana();
            }
            case "mana_max": {
                return (double)getEffectiveManaMax();
            }
            case "xp": {
                return (double)getExperience();
            }
            case "level": {
                return (double)getLevel();
            }
            case "time": {
                Location location = getLocation();
                return location == null ? null : (double)location.getWorld().getTime();
            }
            case "moon": {
                Location location = getLocation();
                return location == null ? null : (double)(int)((location.getWorld().getFullTime() / 24000) % 8);
            }
            case "location_x": {
                Location location = getLocation();
                return location == null ? null : location.getX();
            }
            case "location_y": {
                Location location = getLocation();
                return location == null ? null : location.getY();
            }
            case "location_z": {
                Location location = getLocation();
                return location == null ? null : location.getZ();
            }
            case "damage": {
                return getLastDamage();
            }
            case "damage_dealt": {
                return getLastDamageDealt();
            }
            case "bowpull": {
                return getLastBowPull();
            }
            case "bowpower": {
                return (double)getLastBowPower();
            }

            default:
                return null;
        }
    }

    @Override
    public void setLastDamageType(String damageType) {
        currentDamageType = damageType;
        lastDamageType = damageType;
    }

    @Override
    public String getLastDamageType() {
        return lastDamageType;
    }

    @Override
    public void setLastDamageDealtType(String damageType) {
        currentDamageDealtType = damageType;
        lastDamageDealtType = damageType;
    }

    @Override
    public String getLastDamageDealtType() {
        return lastDamageDealtType;
    }

    public boolean isManaRegenerationDisabled() {
        for (MageSpell spell : activeSpells) {
            if (spell.disableManaRegenerationWhenActive()) {
                return true;
            }
        }
        return false;
    }

    @Override
    @Nullable
    public Wand findWand(@Nonnull String template) {
        Player player = getPlayer();
        if (player == null) return null;
        Inventory inventory = player.getInventory();
        for (ItemStack itemStack : inventory.getContents()) {
            String itemTemplate = Wand.getWandTemplate(itemStack);
            if (itemTemplate != null && itemTemplate.equals(template)) {
                return controller.getWand(itemStack);
            }
        }
        return null;
    }

    @Override
    @Nonnull
    public MageEffectContext getEffectContext() {
        if (effectContext == null) {
            // Lazy load or mage has changed
            effectContext = new MageEffectContext(this);
        }

        return verifyNotNull(effectContext);
    }

    @Override
    public long getCreatedTime() {
        return created;
    }

    @Override
    public boolean trigger(String trigger) {
        lastTriggers.put(trigger, System.currentTimeMillis());
        if (entityData != null) {
            cancelLaunch = true;
            return entityData.trigger(this, trigger);
        }

        List<TriggeredSpell> spells = triggers.get(trigger.toLowerCase());
        if (spells == null || spells.isEmpty()) {
            return false;
        }
        if (!trigger.equals("interval")) {
            sendDebugMessage("Processing trigger: " + trigger, 5);
        }

        // Copy the trigger list since spells can modify it
        processingTriggers.clear();
        processingTriggers.addAll(spells);

        boolean activated = false;
        cancelLaunch = false;
        for (TriggeredSpell triggered : processingTriggers) {
            if (triggered.getTrigger().isValid(this)) {
                Spell spell = getSpell(triggered.getSpellKey());
                if (spell != null && spell.isEnabled() && !triggeringSpells.contains(spell.getKey())) {
                    triggeringSpells.add(spell.getKey());
                    cancelLaunch = cancelLaunch || triggered.getTrigger().isCancelLaunch();
                    activated = spell.cast() || activated;
                    triggered.getTrigger().triggered();
                }
            }
        }

        return activated;
    }

    @Override
    public void attributesUpdated() {
        reloadAttributes();
        updatePassiveEffects();

        // Reload spell parameter so lore updates
        for (MageSpell spell : spells.values()) {
            spell.updateTemplateParameters();
        }

        // Reload active wand
        if (activeWand != null) {
            activeWand.deactivate();
            checkWand();
        }
    }

    @Override
    public void deactivateClasses() {
        for (MageClass mageClass : classes.values()) {
            if (!mageClass.isLocked()) {
                mageClass.deactivateAttributes();
            }
        }
    }

    @Override
    public void activateClasses() {
        for (MageClass mageClass : classes.values()) {
            if (!mageClass.isLocked()) {
                mageClass.activateAttributes();
            }
        }
    }

    @Override
    public void deactivateModifiers() {
        for (MageModifier modifier : modifiers.values()) {
            modifier.deactivateAttributes();
        }
    }

    @Override
    public void activateModifiers() {
        for (MageModifier modifier : modifiers.values()) {
            modifier.activateAttributes();
        }
    }

    @Override
    public void setLaunchingProjectile(boolean launching) {
        // I thought of making this a reference counter, but decided that was too likely to go wrong
        // if a caller misses the unset.
        launchingProjectile = launching;
    }

    @Override
    public boolean isLaunchingProjectile() {
        return launchingProjectile;
    }

    @Override
    public double getLastDamage() {
        return lastDamage;
    }

    public void setLastDamage(double lastDamage) {
        this.lastDamage = lastDamage;
    }

    @Override
    public double getLastDamageDealt() {
        return lastDamageDealt;
    }

    @Override
    public double getLastBowPull() {
        return lastBowPull;
    }

    public void setLastBowPull(double lastBowPull) {
        this.lastBowPull = lastBowPull;
    }

    public int getLastBowPower() {
        if (lastBowUsed == null || !lastBowUsed.hasItemMeta()) {
            return 0;
        }
        ItemMeta meta = lastBowUsed.getItemMeta();
        return meta.getEnchantLevel(Enchantment.ARROW_DAMAGE);
    }

    public void setLastBowUsed(ItemStack itemStack) {
        this.lastBowUsed = itemStack;
    }

    public void setLastProjectileType(EntityType t) {
        lastProjectileType = t;
    }

    @Override
    public EntityType getLastProjectileType() {
        return lastProjectileType;
    }

    @Override
    public double getHealth() {
        LivingEntity li = getLivingEntity();
        return li == null ? 0 : li.getHealth();
    }

    @Override
    public double getMaxHealth() {
        LivingEntity li = getLivingEntity();
        return li == null ? 0 : CompatibilityUtils.getMaxHealth(li);
    }

    public boolean isCancelLaunch() {
        if (entityData != null) {
            return entityData.isCancelLaunch();
        }
        return cancelLaunch;
    }

    @Override
    @Nullable
    public Long getLastTrigger(String trigger) {
        return lastTriggers.get(trigger);
    }

    @Override
    public boolean toggleSpellEnabled(String spellKey) {
        return toggleSpellEnabled(getSpell(spellKey));
    }

    public boolean toggleSpellEnabled(Spell spell) {
        if (spell != null && spell.isToggleable()) {
            spell.setEnabled(!spell.isEnabled());
            if (activeWand != null) {
                activeWand.updateSpellItem(spell);
            }
            return true;
        }
        return false;
    }

    @Override
    @Nonnull
    public ConfigurationSection getVariables() {
        if (variables == null) {
            variables = new MemoryConfiguration();
        }
        return variables;
    }

    @Override
    public boolean addModifier(@Nonnull String key) {
        return addModifier(key, 0);
    }

    @Override
    public boolean addModifier(@Nonnull String key, @Nullable ConfigurationSection properties) {
        int duration = properties == null ? 0 : properties.getInt("duration");
        return addModifier(key, duration, properties);
    }

    @Override
    public boolean addModifier(@Nonnull String key, int duration) {
        return addModifier(key, duration, null);
    }

    @Override
    public boolean addModifier(@Nonnull String key, int duration, @Nullable ConfigurationSection properties) {
        MageModifier modifier = modifiers.get(key);
        // TODO: Property diff/stacking?
        if (modifier != null) {
            if (!modifier.hasDuration()) {
                return false;
            }
            if (duration > 0 && modifier.getTimeRemaining() > duration) {
                return false;
            }
            modifier.reset(duration);
            return true;
        }
        ModifierTemplate template = controller.getModifierTemplate(key);
        if (template == null) {
            controller.getLogger().warning("Invalid modifier key: " + key);
            return false;
        }

        template = template.getMageTemplate(this);
        modifier = new MageModifier(this, template);
        modifiers.put(key, modifier);
        modifier.onAdd(duration);
        updatePassiveEffects();
        return true;
    }

    @Override
    public MageModifier removeModifier(@Nonnull String key) {
        MageModifier modifier = modifiers.remove(key);
        if (modifier != null) {
            modifier.onRemoved();
            updatePassiveEffects();
        }
        return modifier;
    }

    @Override
    @Nonnull
    public Set<String> getModifierKeys() {
        return modifiers.keySet();
    }

    @Override
    @Nullable
    public MageModifier getModifier(String key) {
        return modifiers.get(key);
    }

    /**
     * Get the item slot of the arrow that will be fired from a bow.
     * -1 : Main hand
     * -2 : Offhand
     * >=0 : Inventory slot
     * @return null if the player is not holding an arrow
     */
    @Nullable
    public Integer getArrowToLaunch() {
        Player player = getPlayer();
        PlayerInventory inventory = player == null ? null : player.getInventory();
        if (inventory == null) {
            return null;
        }
        ItemStack itemStack = inventory.getItemInMainHand();
        if (itemStack.getType() == Material.ARROW) {
            return -1;
        }
        itemStack = inventory.getItemInOffHand();
        if (itemStack.getType() == Material.ARROW) {
            return -2;
        }
        ItemStack[] contents = inventory.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() == Material.ARROW) {
                return i;
            }
        }
        return null;
    }

    @Nullable
    public ItemStack getItemInSlot(int slot) {
        Player player = getPlayer();
        PlayerInventory inventory = player == null ? null : player.getInventory();
        if (inventory == null) {
            return null;
        }
        if (slot == -1) {
            return inventory.getItemInMainHand();
        }
        if (slot == -2) {
            return inventory.getItemInOffHand();
        }
        return inventory.getItem(slot);
    }

    public void clearSlot(int slot) {
        Player player = getPlayer();
        PlayerInventory inventory = player == null ? null : player.getInventory();
        if (inventory == null) {
            return;
        }
        if (slot == -1) {
            inventory.setItemInMainHand(null);
        } else if (slot == -2) {
            inventory.setItemInOffHand(null);
        } else {
            inventory.setItem(slot, null);
        }
    }

    /**
     * This isa non-API method that returns the live version of the conversation map
     */
    @Nonnull
    public Map<Player, MageConversation> getConversations() {
        return conversations;
    }

    @Override
    public boolean isBypassEnabled() {
        return bypassEnabled;
    }

    @Override
    public void setBypassEnabled(boolean enable) {
        bypassEnabled = enable;
    }
}
