package com.elmakers.mine.bukkit.wand;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verifyNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MainHand;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.block.BrushMode;
import com.elmakers.mine.bukkit.api.economy.Currency;
import com.elmakers.mine.bukkit.api.event.SpellInventoryEvent;
import com.elmakers.mine.bukkit.api.event.WandPreActivateEvent;
import com.elmakers.mine.bukkit.api.item.Icon;
import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.magic.MageClassTemplate;
import com.elmakers.mine.bukkit.api.magic.MageContext;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MagicAttribute;
import com.elmakers.mine.bukkit.api.magic.MagicProperties;
import com.elmakers.mine.bukkit.api.magic.MagicPropertyType;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.api.magic.ProgressionPath;
import com.elmakers.mine.bukkit.api.spell.CastingCost;
import com.elmakers.mine.bukkit.api.spell.CostReducer;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellKey;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.WandAction;
import com.elmakers.mine.bukkit.api.wand.WandUseMode;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.block.MaterialBrush;
import com.elmakers.mine.bukkit.boss.BossBarConfiguration;
import com.elmakers.mine.bukkit.configuration.MageParameters;
import com.elmakers.mine.bukkit.effect.EffectPlayer;
import com.elmakers.mine.bukkit.effect.SoundEffect;
import com.elmakers.mine.bukkit.effect.WandContext;
import com.elmakers.mine.bukkit.effect.builtin.EffectRing;
import com.elmakers.mine.bukkit.heroes.HeroesManager;
import com.elmakers.mine.bukkit.item.InventorySlot;
import com.elmakers.mine.bukkit.magic.BaseMagicConfigurable;
import com.elmakers.mine.bukkit.magic.Mage;
import com.elmakers.mine.bukkit.magic.MageClass;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.tasks.ApplyWandIconTask;
import com.elmakers.mine.bukkit.tasks.CancelEffectsContextTask;
import com.elmakers.mine.bukkit.tasks.OpenWandTask;
import com.elmakers.mine.bukkit.tasks.RestoreSpellIconTask;
import com.elmakers.mine.bukkit.utility.BukkitMetadataUtils;
import com.elmakers.mine.bukkit.utility.CompatibilityConstants;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.CurrencyAmount;
import com.elmakers.mine.bukkit.utility.Replacer;
import com.elmakers.mine.bukkit.utility.TextUtils;
import com.elmakers.mine.bukkit.utility.platform.CompatibilityUtils;

public class Wand extends WandProperties implements CostReducer, com.elmakers.mine.bukkit.api.wand.Wand, Replacer {
    public static final int OFFHAND_SLOT = 40;
    public static final int INVENTORY_SIZE = 27;
    public static final int CHEST_ITEMS_PER_ROW = 9;
    public static final int PLAYER_INVENTORY_SIZE = 36;
    public static final int INVENTORY_ORGANIZE_BUFFER = 4;
    public static final int HOTBAR_SIZE = 9;
    public static final int HOTBAR_INVENTORY_SIZE = HOTBAR_SIZE - 1;
    public static boolean FILL_CREATOR = false;
    public static Vector DEFAULT_CAST_OFFSET = new Vector(0, 0, 0.5);
    public static String DEFAULT_WAND_TEMPLATE = "default";
    public static boolean CREATIVE_CHEST_MODE = false;
    public static boolean OLD_WAND_LOCKED = false;

    private static final Random random = new Random();

    /**
     * The item as it appears in the inventory of the player.
     */
    protected @Nullable ItemStack item;
    private int hideFlags;

    /**
     * The currently active mage.
     *
     * <p>Is only set when the wand is active or when the wand is
     * used for off-hand casting.
     */
    protected @Nullable Mage mage;
    protected @Nullable WandContext wandContext;

    // Cached state
    private String id = "";
    private List<WandInventory> hotbars;
    private List<WandInventory> inventories;
    private Map<String, Integer> spellInventory = new HashMap<>();
    private Set<String> spells = new LinkedHashSet<>();
    private Map<String, Integer> spellLevels = new HashMap<>();
    private Map<String, Integer> brushInventory = new HashMap<>();
    private Set<String> brushes = new LinkedHashSet<>();
    private MaterialSet interactibleMaterials = null;

    private String activeSpell = "";
    private String[] alternateSpells = new String[7];
    private String activeBrush = "";
    protected String wandName = "";
    protected String description = "";
    private String owner = "";
    private String ownerId = "";
    private String template = "";
    private String path = "";
    private String inventoryOpenLore = "";
    private List<String> mageClassKeys = null;
    private boolean superProtected = false;
    private boolean superPowered = false;
    private boolean glow = false;
    private boolean spellGlow = false;
    private boolean bound = false;
    private boolean boundDisplayName = true;
    private boolean indestructible = false;
    private boolean undroppable = false;
    private boolean keep = false;
    private boolean worn = false;
    private boolean swappable = true;
    private WandUseMode useMode = WandUseMode.SUCCESS;
    private boolean autoOrganize = false;
    private boolean autoAlphabetize = false;
    private boolean autoFill = false;
    private boolean isUpgrade = false;
    private boolean isCostFree = false;
    private boolean randomizeOnActivate = true;
    private boolean rename = false;
    private boolean renameDescription = false;
    private boolean quickCast = false;
    private boolean quickCastDisabled = false;
    private boolean manualQuickCastDisabled = false;
    private boolean isInOffhand = false;
    private boolean hasId = false;
    private boolean showCycleModeLore = true;
    private boolean alwaysUseActiveName = false;
    private boolean neverUseActiveName = false;
    private boolean instructions = true;
    private boolean instructionsLore = true;
    private int inventoryRows = 1;
    private Vector castLocation;

    private WandAction leftClickAction = WandAction.NONE;
    private WandAction rightClickAction = WandAction.NONE;
    private WandAction dropAction = WandAction.NONE;
    private WandAction swapAction = WandAction.NONE;
    private WandAction noBowpullAction = WandAction.NONE;
    private WandAction leftClickSneakAction = WandAction.NONE;
    private WandAction rightClickSneakAction = WandAction.NONE;
    private WandAction dropSneakAction = WandAction.NONE;
    private WandAction swapSneakAction = WandAction.NONE;
    private WandAction noBowpullSneakAction = WandAction.NONE;

    private MaterialAndData icon = null;
    private MaterialAndData inactiveIcon = null;
    private int inactiveIconDelay = 0;
    private String upgradeTemplate = null;

    protected float consumeReduction = 0;
    protected float cooldownReduction = 0;
    protected float costReduction = 0;
    protected Map<String, Double> protection;
    private float power = 0;
    private float earnMultiplier = 1;

    private float blockFOV = 0;
    private float blockChance = 0;
    private float blockReflectChance = 0;
    private int blockMageCooldown = 0;
    private int blockCooldown = 0;

    private int maxEnchantCount = 0;
    private int enchantCount = 0;

    private boolean hasInventory = false;
    private boolean modifiable = true;
    private boolean locked = true;
    private boolean autoAbsorb = false;
    private boolean forceUpgrade = false;
    private boolean isHeroes = false;
    private int uses = 0;
    private boolean hasUses = false;
    private boolean isSingleUse = false;
    private boolean limitSpellsToPath = false;
    private boolean levelSpells = false;
    private String levelSpellsToPath = null;
    private boolean limitBrushesToPath = false;
    private Double resetManaOnActivate = null;

    private float manaPerDamage = 0;

    private Particle effectParticle = null;
    private float effectParticleData = 0;
    private int effectParticleCount = 0;
    private int effectParticleInterval = 0;
    private double effectParticleMinVelocity = 0;
    private double effectParticleRadius = 0;
    private double effectParticleOffset = 0;
    private boolean effectBubbles = false;
    private boolean activeEffectsOnly = false;
    private EffectRing effectPlayer = null;

    private int castInterval = 0;
    private double castMinVelocity = 0;
    private Vector castVelocityDirection = null;
    private String castSpell = null;
    private ConfigurationSection castParameters = null;

    private SoundEffect effectSound = null;
    private int effectSoundInterval = 0;

    private int quietLevel = 0;

    // Slot system
    private List<String> unslotted = null;
    private List<WandUpgradeSlot> slots = null;
    private ConfigurationSection slottedConfiguration = null;

    // Other property overrides
    private ConfigurationSection requirementConfiguration = null;
    private List<RequirementProperties> requirementProperties = null;

    // Transient state

    private boolean hasSpellProgression = false;

    private long lastSoundEffect;
    private long lastParticleEffect;
    private long lastSpellCast;

    // Inventory functionality

    private WandMode mode = null;
    private WandMode brushMode = null;
    private int openInventoryPage = 0;
    private boolean inventoryIsOpen = false;
    private boolean inventoryWasOpen = false;
    private Inventory displayInventory = null;
    private int currentHotbar = 0;

    public static WandManaMode manaMode = WandManaMode.BAR;
    public static boolean regenWhileInactive = true;
    public static Material DefaultUpgradeMaterial = Material.NETHER_STAR;
    public static Material DefaultWandMaterial = Material.BLAZE_ROD;
    public static Material EnchantableWandMaterial = null;
    public static boolean SpellGlow = false;
    public static boolean BrushGlow = false;
    public static boolean BrushItemGlow = true;
    public static boolean LiveHotbar = true;
    public static boolean LiveHotbarSkills = false;
    public static boolean LiveHotbarCooldown = true;
    public static boolean LiveHotbarCharges = true;
    public static boolean LiveHotbarMana = true;
    public static boolean Unbreakable = false;
    public static boolean Unstashable = true;
    public static SoundEffect inventoryOpenSound = null;
    public static SoundEffect inventoryCloseSound = null;
    public static SoundEffect inventoryCycleSound = null;
    public static SoundEffect noActionSound = null;
    public static SoundEffect itemPickupSound = null;
    public static String WAND_KEY = "wand";
    public static String UPGRADE_KEY = "wand_upgrade";
    public static String WAND_SELF_DESTRUCT_KEY = null;
    public static int HIDE_FLAGS = 127;
    public static String brushSelectSpell = "";

    private Inventory storedInventory = null;
    private int heldSlot = 0;
    private boolean isActive = false;
    private long activationTimestamp;

    // Glyph hotbar
    private final GlyphHotbar glyphHotbar = new GlyphHotbar(this);

    // XP bar
    protected WandDisplayMode xpBarDisplayMode = WandDisplayMode.MANA;

    // Level
    protected WandDisplayMode levelDisplayMode = WandDisplayMode.SP;

    // Boss bar
    protected BossBar bossBar;
    protected BossBarConfiguration bossBarConfiguration;
    protected WandDisplayMode bossBarDisplayMode = WandDisplayMode.COOLDOWN;

    // Action bar
    protected String lastActionBarMessage;
    protected String actionBarMessage;
    protected String actionBarOpenMessage;
    protected String actionBarFont;
    protected int actionBarInterval;
    protected int actionBarDelay;
    protected long lastActionBar;
    protected boolean actionBarMana;
    protected boolean lastActionBarFullMana;

    public Wand(MagicController controller) {
        super(controller);

        hotbars = new ArrayList<>();
        inventories = new ArrayList<>();
    }

    /**
     * @deprecated Use {@link MagicController#getWand(ItemStack)}.
     */
    @Deprecated
    public Wand(MagicController controller, ItemStack itemStack) {
        this(controller);
        checkNotNull(itemStack);

        if (itemStack.getType() == Material.AIR) {
            itemStack.setType(DefaultWandMaterial);
        }
        this.icon = new MaterialAndData(itemStack);
        item = itemStack;
        boolean needsSave = false;
        boolean isWand = isWand(item);
        boolean isUpgradeItem = isUpgrade(item);
        if (isWand || isUpgradeItem) {
            ConfigurationSection wandConfig = itemToConfig(item, ConfigurationUtils.newConfigurationSection());

            // Check for template migration
            WandTemplate wandTemplate = controller.getWandTemplate(wandConfig.getString("template"));
            WandTemplate migrateTemplate = wandTemplate == null ? null : wandTemplate.getMigrateTemplate();
            if (migrateTemplate != null) {
                wandConfig.set("template", migrateTemplate.getKey());
            }

            // Check for wand data migration
            int version = wandConfig.getInt("version", 0);
            if (version < CURRENT_VERSION) {
                // Migration will be handled by CasterProperties, this is just here
                // So that we save the data after to avoid re-migrating.
                needsSave = true;
            }
            randomizeOnActivate = !wandConfig.contains("icon");
            load(wandConfig);
        } else {
            updateIcon();
            needsSave = true;
        }
        loadProperties();

        // Migrate old upgrade items
        if ((isUpgrade || isUpgradeItem) && isWand) {
            needsSave = true;
            CompatibilityLib.getNBTUtils().removeMeta(item, WAND_KEY);
        }
        if (needsSave) {
            saveState();
            updateName();
            updateLore();
        }
    }

    public Wand(MagicController controller, ConfigurationSection config) {
        this(controller, DefaultWandMaterial, (short)0);
        load(config);
        loadProperties();
        updateName();
        updateLore();
        saveState();
    }

    protected Wand(MagicController controller, String templateName, Mage mage) throws UnknownWandException {
        this(controller);

        // Default to "default" wand
        this.mage = mage;
        if (templateName == null || templateName.length() == 0)
        {
            templateName = DEFAULT_WAND_TEMPLATE;
        }

        // Check for randomized/pre-enchanted wands
        int level = 0;
        if (templateName.contains("(")) {
            String levelString = templateName.substring(templateName.indexOf('(') + 1, templateName.length() - 1);
            try {
                level = Integer.parseInt(levelString);
            } catch (Exception ex) {
                throw new IllegalArgumentException(ex);
            }
            templateName = templateName.substring(0, templateName.indexOf('('));
        }

        WandTemplate template = controller.getWandTemplate(templateName);
        if (template == null) {
            throw new UnknownWandException(templateName);
        }
        WandTemplate migrateTemplate = template.getMigrateTemplate();
        if (migrateTemplate != null) {
            template = migrateTemplate;
            templateName = migrateTemplate.getKey();
        }

        setTemplate(templateName);
        setProperty("version", CURRENT_VERSION);

        // Load all properties
        loadProperties();

        // Enchant, if an enchanting level was provided
        if (level > 0) {
            // Account for randomized unmodifiable wands
            boolean wasModifiable = modifiable;
            modifiable = true;
            randomize(level, null, true);
            modifiable = wasModifiable;
        }

        // Don't randomize now if set to randomize later
        // Otherwise, do this here so the description updates
        if (!randomizeOnActivate) {
            randomize();
        }

        updateItem();
        updateName();
        updateLore();
        saveState();
        this.mage = null;
    }

    public Wand(MagicController controller, Material icon, short iconData) {
        // This will make the Bukkit ItemStack into a real ItemStack with NBT data.
        this(controller, CompatibilityLib.getItemUtils().makeReal(
                CompatibilityLib.getDeprecatedUtils().createItemStack(icon, 1, iconData)
        ));
        saveState();
        updateName();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void migrate(int version, ConfigurationSection wandConfig) {
        // First migration, clean out wand data that matches template
        // We've done this twice now, the second time to handle removing hard-coded defaults that
        // were not present in the template configs.
        if (version <= 1) {
            ConfigurationSection templateConfig = controller.getWandTemplateConfiguration(wandConfig.getString("template"));
            if (templateConfig != null) {
                // This is an unfortunate special case for wands waiting to be randomized
                String randomizeIcon = templateConfig.getString("randomize_icon");
                String currentIcon = wandConfig.getString("icon");
                if (randomizeIcon != null && currentIcon != null && randomizeIcon.equals(currentIcon)) {
                    wandConfig.set("icon", null);
                }
                // This was a potentially leftover property from randomized wands we can ditch
                wandConfig.set("randomize", null);
                Set<String> keys = templateConfig.getKeys(false);
                for (String key : keys) {
                    Object templateData = templateConfig.get(key);
                    Object wandData = wandConfig.get(key);
                    if (wandData == null) continue;
                    String templateString = templateData.toString();
                    String wandString = wandData.toString();
                    if (templateData instanceof List) {
                        templateString = templateString.substring(1, templateString.length() - 1);
                        templateString = templateString.replace(", ", ",");
                        templateData = templateString;
                    }
                    if (wandString.equalsIgnoreCase(templateString)) {
                        wandConfig.set(key, null);
                        continue;
                    }

                    try {
                        double numericValue = Double.parseDouble(wandString);
                        double numericTemplate = Double.parseDouble(templateString);

                        if (numericValue == numericTemplate) {
                            wandConfig.set(key, null);
                            continue;
                        }
                    } catch (NumberFormatException ignored) {

                    }
                    if (wandData.equals(templateData)) {
                        wandConfig.set(key, null);
                    }
                }
            }
        }

        // Remove icon if matches template
        if (version <= 3) {
            ConfigurationSection templateConfig = controller.getWandTemplateConfiguration(wandConfig.getString("template"));
            String templateIcon = templateConfig == null ? null : templateConfig.getString("icon");
            if (templateIcon != null && templateIcon.equals(wandConfig.getString("icon", ""))) {
                wandConfig.set("icon", null);
            }
        }

        // Migration: remove level from spell inventory
        if (version <= 4) {
            Object spellInventoryRaw = wandConfig.get("spell_inventory");
            if (spellInventoryRaw != null) {
                Map<String, ? extends Object> spellInventory = null;
                Map<String, Integer> newSpellInventory = new HashMap<>();
                if (spellInventoryRaw instanceof Map) {
                    spellInventory = (Map<String, ? extends Object>)spellInventoryRaw;
                } else if (spellInventoryRaw instanceof ConfigurationSection) {
                    spellInventory = CompatibilityLib.getCompatibilityUtils().getMap((ConfigurationSection)spellInventoryRaw);
                }
                if (spellInventory != null) {
                    for (Map.Entry<String, ? extends Object> spellEntry : spellInventory.entrySet()) {
                        Object slot = spellEntry.getValue();
                        if (slot != null && slot instanceof Integer) {
                            SpellKey spellKey = new SpellKey(spellEntry.getKey());
                            // Prefer to use the base spell if present since that is what we'd be
                            // using on load.
                            Object testSlot = spellInventory.get(spellKey.getBaseKey());
                            if (testSlot != null) {
                                slot = testSlot;
                            }
                            newSpellInventory.put(spellKey.getBaseKey(), (Integer)slot);
                        }
                    }
                    wandConfig.set("spell_inventory", newSpellInventory);
                }
            }
        }

        // Migration: move attributes to item_attributes
        if (version <= 5) {
            ConfigurationSection attributes = wandConfig.getConfigurationSection("attributes");
            wandConfig.set("attributes", null);
            wandConfig.set("item_attributes", attributes);
        }

        super.migrate(version, wandConfig);
    }

    @Override
    public void load(ConfigurationSection configuration) {
        if (configuration != null) {
            setTemplate(configuration.getString("template"));
        }
        super.load(configuration);
    }

    // Update the hotbars inventory list to match the most recently configured value
    // This will be followed with checkHotbarCount, after the inventories have been built
    // This catches the case of the hotbar count having changed so we can preserve the location
    // of spells in the main inventories.
    protected void updateHotbarCount() {
        int hotbarCount = 0;
        if (hasProperty("hotbar_inventory_count")) {
            hotbarCount = Math.max(1, getInt("hotbar_inventory_count", 1));
        } else {
            hotbarCount = getHotbarCount();
        }
        if (hotbarCount != hotbars.size()) {
            if (isInventoryOpen()) {
                closeInventory();
            }
            hotbars.clear();
            while (hotbars.size() < hotbarCount) {
                hotbars.add(new WandInventory(HOTBAR_INVENTORY_SIZE));
            }
            while (hotbars.size() > hotbarCount) {
                hotbars.remove(0);
            }
        }
    }

    // This catches the hotbar_count having changed since the last time the inventory was built
    // in which case we want to add a new hotbar inventory without re-arranging the main inventories
    // newly added hotbars will be empty, spells in removed hotbars will be added to the end of the inventories.
    protected void checkHotbarCount() {
        if (!hasInventory || getHotbarCount() == 0) return;
        int hotbarCount = Math.max(1, getInt("hotbar_count", 1));
        if (hotbarCount != hotbars.size()) {
            while (hotbars.size() < hotbarCount) {
                hotbars.add(new WandInventory(HOTBAR_INVENTORY_SIZE));
            }
            while (hotbars.size() > hotbarCount) {
                hotbars.remove(0);
            }

            List<WandInventory> pages = new ArrayList<>(inventories);
            int slotOffset = getInt("hotbar_count") * HOTBAR_INVENTORY_SIZE;
            int index = 0;
            for (WandInventory inventory : pages) {
                for (ItemStack itemStack : inventory.items) {
                    updateSlot(index + slotOffset, itemStack);
                    index++;
                }
            }
            updateSpellInventory();
            updateBrushInventory();
        }
        setProperty("hotbar_inventory_count", hotbarCount);
    }

    @Override
    public void unenchant() {
        controller.cleanItem(item);
        clear();
    }

    public void updateItemIcon() {
        setIcon(icon);
    }

    protected void updateIcon() {
        if (icon != null && icon.getMaterial() != null && icon.getMaterial() != Material.AIR) {
            String iconKey = icon.getKey();
            if (iconKey != null && iconKey.isEmpty()) {
                iconKey = null;
            }
            WandTemplate template = getTemplate();
            String templateIcon = template != null
                    ? template.getIcon(controller.isLegacyIconsEnabled())
                    : null;
            if (templateIcon == null || !templateIcon.equals(iconKey)) {
                setProperty("icon", iconKey);
            }
        }
    }

    @Override
    public void setInactiveIcon(com.elmakers.mine.bukkit.api.block.MaterialAndData materialData) {
        if (materialData == null) {
            inactiveIcon = null;
        } else if (materialData instanceof MaterialAndData) {
            inactiveIcon = ((MaterialAndData)materialData);
        } else {
            inactiveIcon = new MaterialAndData(materialData);
        }

        String inactiveIconKey = null;
        if (inactiveIcon != null && inactiveIcon.getMaterial() != null && inactiveIcon.getMaterial() != Material.AIR) {
            inactiveIconKey = inactiveIcon.getKey();
            if (inactiveIconKey != null && inactiveIconKey.isEmpty()) {
                inactiveIconKey = null;
            }
        }
        setProperty("inactive_icon", inactiveIconKey);
        updateItemIcon();
    }

    public void setIcon(Material material, byte data) {
        setIcon(material == null ? null : new MaterialAndData(material, data));
        updateIcon();
    }

    @Override
    public void setIcon(com.elmakers.mine.bukkit.api.block.MaterialAndData materialData) {
        if (materialData instanceof MaterialAndData) {
            setIcon((MaterialAndData)materialData);
        } else {
            setIcon(new MaterialAndData(materialData));
        }
        updateIcon();
    }

    public void setIcon(MaterialAndData materialData) {
        if (materialData == null || !materialData.isValid()) return;
        if (materialData.getMaterial() == Material.AIR || materialData.getMaterial() == null) {
            materialData.setMaterial(DefaultWandMaterial);
        }
        icon = materialData;
        if (item == null) {
            item = CompatibilityLib.getItemUtils().makeReal(this.icon.getItemStack(1));
        }
        if (!icon.isValid()) {
            return;
        }

        Short durability = null;
        if (!indestructible && !isUpgrade && icon.getMaterial().getMaxDurability() > 0) {
            durability = CompatibilityLib.getDeprecatedUtils().getItemDamage(item);
        }
        try {
            if (inactiveIcon == null || useActiveIcon()) {
                if (inactiveIcon != null && inactiveIconDelay > 0) {
                    inactiveIcon.applyToItem(item);
                    Plugin plugin = controller.getPlugin();
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new ApplyWandIconTask(this), inactiveIconDelay * 20 / 1000);
                } else {
                    icon.applyToItem(item);
                }
            } else {
                inactiveIcon.applyToItem(item);
            }
        } catch (Exception ex) {
            controller.getLogger().log(Level.WARNING, "Unable to apply wand icon", ex);
            item.setType(DefaultWandMaterial);
        }

        if (durability != null) {
            CompatibilityLib.getDeprecatedUtils().setItemDamage(item, durability);
        }

        // Make indestructible
        // The isUpgrade checks here and above are for using custom icons in 1.9, this is a bit hacky.
        if ((indestructible || Unbreakable || isUpgrade) && !manaMode.useDurability()) {
            CompatibilityLib.getItemUtils().makeUnbreakable(item);
        } else {
            CompatibilityLib.getItemUtils().removeUnbreakable(item);
        }
        CompatibilityLib.getItemUtils().hideFlags(item, hideFlags);
    }

    private boolean useActiveIcon() {
        boolean useActiveIcon = mage != null;
        if (useActiveIcon && getMode() == WandMode.INVENTORY) {
            useActiveIcon = isInventoryOpen();
        }
        return useActiveIcon;
    }

    @Override
    public void makeUpgrade() {
        if (!isUpgrade) {
            isUpgrade = true;
            String oldName = wandName;
            String newName = getMessage("upgrade_name");
            newName = newName.replace("$name", oldName);
            String newDescription = controller.getMessages().get("wand.upgrade_default_description");
            if (template != null && template.length() > 0) {
                newDescription = controller.getMessages().get("wands." + template + ".upgrade_description", description);
            }
            setIcon(DefaultUpgradeMaterial, (byte) 0);
            setName(newName);
            setDescription(newDescription);
            CompatibilityLib.getNBTUtils().removeMeta(item, WAND_KEY);
            saveState();
            updateName(true);
            updateLore();
        }
    }

    public void newId() {
        id = UUID.randomUUID().toString();
        setProperty("id", id);
    }

    public boolean checkId() {
        if (id == null || id.length() == 0) {
            newId();
            return true;
        }
        return false;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isModifiable() {
        return modifiable;
    }

    public boolean isAutoAbsorb() {
        return autoAbsorb;
    }

    @Override
    public boolean isIndestructible() {
        return indestructible;
    }

    @Override
    public boolean isUndroppable() {
        return undroppable;
    }

    public boolean isUpgrade() {
        return isUpgrade;
    }

    public static boolean isUpgrade(ItemStack item) {
        return item != null && CompatibilityLib.getNBTUtils().containsTag(item, UPGRADE_KEY);
    }

    @Override
    public boolean isCostFree() {
        if (isCostFree) {
            return true;
        }
        return super.isCostFree();
    }

    @Override
    public boolean usesMana() {
        if (isCostFree()) return false;
        return getManaMax() > 0 || (isHeroes && mage != null);
    }

    @Override
    public void removeMana(float amount) {
        if (isHeroes && mage != null) {
            HeroesManager heroes = controller.getHeroes();
            if (heroes != null) {
                heroes.removeMana(mage.getPlayer(), (int)Math.ceil(amount));
            }
        }
        super.removeMana(amount);
        updateMana();
    }

    @Override
    public float getCostReduction() {
        if (mage != null) {
            float reduction = mage.getCostReduction();
            return worn ? reduction : stackPassiveProperty(reduction, costReduction * controller.getMaxCostReduction());
        }
        return costReduction;
    }

    @Override
    public float getCooldownReduction() {
        if (mage != null) {
            float reduction = mage.getCooldownReduction();
            return worn ? reduction : stackPassiveProperty(reduction, cooldownReduction * controller.getMaxCooldownReduction());
        }
        return cooldownReduction;
    }

    @Override
    public float getConsumeReduction() {
        if (mage != null) {
            float reduction = mage.getConsumeReduction();
            return worn ? reduction : stackPassiveProperty(reduction, consumeReduction);
        }
        return consumeReduction;
    }

    @Override
    public float getCostScale() {
        return 1;
    }

    @Override
    public boolean hasInventory() {
        return hasInventory;
    }

    @Override
    public float getPower() {
        return power;
    }

    @Override
    public boolean isSuperProtected() {
        return superProtected;
    }

    @Override
    public boolean isSuperPowered() {
        return superPowered;
    }

    @Override
    public boolean isConsumeFree() {
        return consumeReduction >= 1;
    }

    @Override
    public boolean isCooldownFree() {
        return cooldownReduction > 1;
    }

    @Override
    public String getName() {
        return CompatibilityLib.getCompatibilityUtils().translateColors(wandName);
    }

    public String getDescription() {
        return description;
    }

    public String getOwner() {
        return owner == null ? "" : owner;
    }

    public String getOwnerId() {
        return ownerId;
    }

    @Override
    public long getWorth() {
        long worth = getInt("worth");
        if (worth > 0) {
            return worth;
        }
        // TODO: Item properties, brushes, etc
        Set<String> spells = getSpells();
        for (String spellKey : spells) {
            SpellTemplate spell = controller.getSpellTemplate(spellKey);
            if (spell != null) {
                worth = (long)(worth + spell.getWorth());
            }
        }
        return worth;
    }

    @Override
    public void setName(String name) {
        wandName = ChatColor.stripColor(name);
        setProperty("name", wandName);
        updateName();
    }

    public void setTemplate(String templateName) {
        this.template = templateName;
        WandTemplate wandTemplate = controller.getWandTemplate(templateName);
        if (wandTemplate != null) {
            setWandTemplate(wandTemplate);
        }
        setProperty("template", template);
    }

    @Override
    public String getTemplateKey() {
        return this.template;
    }

    @Override
    public boolean hasTag(String tag) {
        WandTemplate template = getTemplate();
        return template != null && template.hasTag(tag);
    }

    @Override
    public boolean hasSlot(String slotType) {
        if (slots == null || slotType == null) {
            return false;
        }
        for (WandUpgradeSlot slot : slots) {
            if (slot.getType().equals(slotType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public WandUpgradePath getPath() {
        String pathKey = path;
        if (pathKey == null || pathKey.length() == 0) {
            pathKey = controller.getDefaultWandPath();
        }
        return WandUpgradePath.getPath(pathKey);
    }

    public boolean hasPath() {
        return path != null && path.length() > 0;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
        setProperty("description", description);
        updateLore();
    }

    public boolean tryToOwn(Player player) {
        if (ownerId == null || ownerId.length() == 0) {
            takeOwnership(player);
            return true;
        }

        return false;
    }

    @Override
    public void showInstructions() {
        showInstructions(false);
    }

    public void showInstructions(boolean forceBound) {
        if (mage == null || !instructions) return;
        startWandInstructions();
        if (forceBound) {
            doShowBoundInstructions();
        } else {
            showBoundInstructions();
        }
        showSpellInstructions();
        showBrushInstructions();
        showHotbarInstructions();
        showPageInstructions();
        showManaInstructions();
        showPathInstructions();
        endWandInstructions();
    }

    private void showBoundInstructions() {
        if (ownerId != null) {
            doShowBoundInstructions();
        }
    }

    private void doShowBoundInstructions() {
        mage.sendMessage(getMessage("bound_instructions", "").replace("$wand", getName()));
    }

    private boolean showCycleActiveInstructions(int spellCount) {
        if (spellCount > 1) {
            if (rightClickAction == WandAction.CYCLE_ACTIVE_HOTBAR
                    || leftClickAction == WandAction.CYCLE_ACTIVE_HOTBAR
                    || dropAction == WandAction.CYCLE_ACTIVE_HOTBAR
                    || swapAction == WandAction.CYCLE_ACTIVE_HOTBAR) {

                String cycleMessage = getMessage("cycle_active_hotbar_instructions");
                String controlKey = getControlKey(WandAction.CYCLE_ACTIVE_HOTBAR);
                controlKey = controller.getMessages().get("controls." + controlKey);
                cycleMessage = cycleMessage.replace("$button", controlKey);
                mage.sendMessage(cycleMessage);
                return true;
            }
        }
        return false;
    }

    private void showSecondSpellInstructions(int spellCount) {
        String headerMessage = getMessage("spell_count_instructions");
        if (headerMessage.isEmpty()) return;
        mage.sendMessage(headerMessage);

        if (!showCycleActiveInstructions(spellCount)) {
            showModeControlsInstructions(spellCount);
        }
    }

    private void showNinthSpellInstructions(int spellCount) {
        String headerMessage = getMessage("spell_count_instructions_9");
        if (headerMessage.isEmpty()) return;
        mage.sendMessage(headerMessage);
        showModeControlsInstructions(spellCount);
    }

    private void showModeControlsInstructions(int spellCount) {
        String controlKey = getInventoryKey();
        String inventoryMessage = getControlMessage(spellCount);
        if (controlKey != null && inventoryMessage != null) {
            controlKey = controller.getMessages().get("controls." + controlKey);
            mage.sendMessage(getMessage(inventoryMessage, "")
                    .replace("$wand", getName()).replace("$toggle", controlKey).replace("$cycle", controlKey));
        }

        if (slots != null) {
            for (WandUpgradeSlot slot : slots) {
                slot.showControlInstructions(mage, controller.getMessages());
            }
        }
    }

    private void showSpellInstructions() {
        String spellKey = getActiveSpellKey();
        SpellTemplate spellTemplate = spellKey != null && !spellKey.isEmpty() ? controller.getSpellTemplate(spellKey) : null;
        if (spellTemplate != null) {
            int spellCount = spells.size();
            if (spellCount > 0) {
                String toggleControlKey = getControlKey(WandAction.TOGGLE);
                toggleControlKey = controller.getMessages().get("controls." + toggleControlKey);
                String castControlKey = getControlKey(WandAction.CAST);
                castControlKey = controller.getMessages().get("controls." + castControlKey);
                String message = getMessage("spell_instructions", "")
                        .replace("$wand", getName())
                        .replace("$toggle", toggleControlKey)
                        .replace("$cast", castControlKey)
                        .replace("$spell", spellTemplate.getName());
                mage.sendMessage(message);
                showCycleActiveInstructions(spellCount);
                showModeControlsInstructions(spellCount);
            }
        }
    }

    private void showBrushInstructions() {
        int brushCount = brushes.size();
        if (brushCount != 0) {
            String controlKey = getControlKey(WandAction.TOGGLE);
            if (controlKey != null) {
                controlKey = controller.getMessages().get("controls." + controlKey);
                mage.sendMessage(getMessage("brush_instructions")
                        .replace("$wand", getName()).replace("$toggle", controlKey));
            }
        }
    }

    private void showPageInstructions() {
        if (inventories.size() > 1) {
            mage.sendMessage(getMessage("page_instructions", "").replace("$wand", getName()));
        }
    }

    private void showHotbarInstructions() {
        if (getInt("hotbar_count") > 1) {
            String controlKey = getControlKey(WandAction.CYCLE_HOTBAR);
            if (controlKey != null) {
                controlKey = controller.getMessages().get("controls." + controlKey);
                mage.sendMessage(getMessage("hotbar_instructions", "")
                    .replace("$wand", getName())
                    .replace("$cycle_hotbar", controlKey));
            }
        }
    }

    private void showManaInstructions() {
        String spellKey = getActiveSpellKey();
        SpellTemplate spellTemplate = spellKey != null && !spellKey.isEmpty() ? controller.getSpellTemplate(spellKey) : null;
        if (usesMana() && spellTemplate != null) {
            String message = getMessage("mana_instructions", "")
                .replace("$wand", getName())
                .replace("$spell", spellTemplate.getName());
            mage.sendMessage(message);
        }
    }

    private void showPathInstructions() {
        com.elmakers.mine.bukkit.api.wand.WandUpgradePath path = getPath();
        if (path != null) {
            if (usesSP()) {
                String message = getMessage("enchant_instructions", "").replace("$wand", getName());
                mage.sendMessage(message);
            }
            ProgressionPath nextPath = path.getNextPath();
            if (nextPath != null && path.isAutomaticProgression()) {
                String message = getMessage("path_instructions", "").replace("$path", path.getName()).replace("$nextpath", nextPath.getName());
                mage.sendMessage(message);
            }
        }
    }

    public void takeOwnership(Player player) {
        boolean setMage = false;
        if (mage == null) {
            setMage(controller.getMage(player), false, null);
            setMage = true;
        }

        if ((ownerId == null || ownerId.length() == 0) && quietLevel < 2) {
            showInstructions(true);
        }
        String ownerName = boundDisplayName ? player.getDisplayName() : player.getName();
        owner = ChatColor.stripColor(ownerName);
        ownerId = mage.getId();
        if (setMage) {
            mage = null;
        }
        setProperty("owner", owner);
        setProperty("owner_id", ownerId);
        updateLore();
        saveState();
    }

    private void startWandInstructions() {
        if (mage == null) return;
        String message = getMessage("wand_instructions_header", "").replace("$wand", getName());
        mage.sendMessage(message);
    }

    private void endWandInstructions() {
        if (mage == null) return;
        String message = getMessage("wand_instructions_footer", "").replace("$wand", getName());
        mage.sendMessage(message);
    }

    @Nullable
    public String getControlKey(WandAction action) {
        String controlKey = null;
        if (rightClickAction == action) {
            controlKey = "right_click";
        } else if (dropAction == action) {
            controlKey = "drop";
        } else if (leftClickAction == action) {
            controlKey = "left_click";
        } else if (swapAction == action) {
            controlKey = "swap";
        } else if (noBowpullAction == action) {
            controlKey = "no_bowpull";
        }

        return controlKey;
    }

    @Nullable
    @Override
    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    @Override
    public com.elmakers.mine.bukkit.api.block.MaterialAndData getIcon() {
        return icon;
    }

    @Override
    public com.elmakers.mine.bukkit.api.block.MaterialAndData getInactiveIcon() {
        return inactiveIcon;
    }

    protected List<WandInventory> getAllInventories() {
        int hotbarCount = getHotbarCount();
        List<WandInventory> allInventories = new ArrayList<>(inventories.size() + hotbarCount);
        if (hotbarCount > 0) {
            allInventories.addAll(hotbars);
        }
        allInventories.addAll(inventories);
        return allInventories;
    }

    @Override
    public Set<String> getBaseSpells() {
        return spells;
    }

    @Override
    protected @Nonnull Map<String, Integer> getSpellLevels() {
        return spellLevels;
    }

    @Override
    public Set<String> getSpells() {
        Set<String> spellSet = new HashSet<>();
        for (String key : spells) {
            Integer level = spellLevels.get(key);
            if (level != null) {
                spellSet.add(new SpellKey(key, level).getKey());
            } else {
                spellSet.add(key);
            }
        }
        return spellSet;
    }

    @Override
    public Set<String> getBrushes() {
        return brushes;
    }

    @Nullable
    protected Integer parseSlot(String[] pieces) {
        Integer slot = null;
        if (pieces.length > 1) {
            try {
                slot = Integer.parseInt(pieces[1]);
            } catch (Exception ex) {
                slot = null;
            }
            if (slot != null && slot < 0) {
                slot = null;
            }
        }
        return slot;
    }


    protected void addToInventory(ItemStack itemStack, Integer slot) {
        if (slot == null) {
            addToInventory(itemStack);
            return;
        }
        WandInventory inventory = getInventory(slot);
        slot = getInventorySlot(slot);

        ItemStack existing = inventory.getItem(slot);
        inventory.setItem(slot, itemStack);

        if (existing != null && existing.getType() != Material.AIR) {
            addToInventory(existing);
        }
    }

    public void addToInventory(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return;
        }
        if (getBrushMode() != WandMode.INVENTORY && isBrush(itemStack)) {
            String brushKey = getBrush(itemStack);
            if (!MaterialBrush.isSpecialMaterialKey(brushKey) || MaterialBrush.isSchematic(brushKey))
            {
                return;
            }
        }
        List<WandInventory> checkInventories = getAllInventories();
        boolean added = false;

        int organizeBuffer = getOrganizeBuffer();
        WandMode mode = getMode();
        int fullSlot = 0;
        for (WandInventory inventory : checkInventories) {
            int inventorySize = inventory.getSize();
            Integer slot = null;
            int freeSpace = 0;
            for (int i = 0; i < inventorySize && freeSpace < organizeBuffer; i++) {
                ItemStack existing = inventory.getItem(i);
                if (CompatibilityLib.getItemUtils().isEmpty(existing)) {
                    if (slot == null) {
                        slot = i;
                    }
                    freeSpace++;
                }
            }

            // Don't leave free space in hotbars
            if (slot != null && (freeSpace >= organizeBuffer || inventorySize == HOTBAR_INVENTORY_SIZE || mode == WandMode.CHEST)) {
                added = true;
                inventory.setItem(slot, itemStack);
                fullSlot += slot;
                break;
            }
            fullSlot += inventorySize;
        }
        if (!added) {
            fullSlot = getHotbarSize() + getInventorySize() * inventories.size();
            WandInventory newInventory = new WandInventory(getInventorySize());
            newInventory.addItem(itemStack);
            inventories.add(newInventory);
        }
        updateSlot(fullSlot, itemStack);
    }

    protected @Nonnull WandInventory getInventoryByIndex(int inventoryIndex) {
        // Auto create
        while (inventoryIndex >= inventories.size()) {
            inventories.add(new WandInventory(getInventorySize()));
        }

        return inventories.get(inventoryIndex);
    }

    protected int getHotbarSize() {
        if (getMode() != WandMode.INVENTORY) return 0;
        return hotbars.size() * HOTBAR_INVENTORY_SIZE;
    }

    protected @Nonnull WandInventory getInventory(int slot) {
        int hotbarSize = getHotbarSize();

        if (slot < hotbarSize) {
            return hotbars.get(slot / HOTBAR_INVENTORY_SIZE);
        }

        int inventoryIndex = (slot - hotbarSize) / getInventorySize();
        return getInventoryByIndex(inventoryIndex);
    }

    protected int getInventorySlot(int slot) {
        int hotbarSize = getHotbarSize();

        if (slot < hotbarSize) {
            return slot % HOTBAR_INVENTORY_SIZE;
        }

        return ((slot - hotbarSize) % getInventorySize());
    }

    protected void buildInventory() {
        // Force an update of the display inventory since chest mode is a different size
        displayInventory = null;

        updateHotbarCount();
        for (WandInventory hotbar : hotbars) {
            hotbar.clear();
        }
        inventories.clear();

        List<ItemStack> unsorted = new ArrayList<>();
        for (String key : spells) {
            int spellLevel = getSpellLevel(key);
            SpellKey spellKey = new SpellKey(key, spellLevel);
            SpellTemplate spell = mage == null ? controller.getSpellTemplate(spellKey.getKey()) : mage.getSpell(spellKey.getKey());
            ItemStack itemStack = createSpellItem(spellKey.getKey(), "", false);
            if (itemStack != null)
            {
                Integer slot = spellInventory.get(spell.getSpellKey().getBaseKey());
                if (slot == null) {
                    unsorted.add(itemStack);
                } else {
                    addToInventory(itemStack, slot);
                }
            }
        }
        WandMode wandMode = getMode();
        WandMode brushMode = getBrushMode();
        for (String brushKey : brushes) {
            boolean addToInventory = brushMode == WandMode.INVENTORY || (wandMode == WandMode.INVENTORY && MaterialBrush.isSpecialMaterialKey(brushKey) && !MaterialBrush.isSchematic(brushKey));
            if (addToInventory)
            {
                ItemStack itemStack = createBrushIcon(brushKey);
                if (itemStack == null) {
                    controller.info("Unable to create brush icon for key " + brushKey, 5);
                    continue;
                }
                Integer slot = brushInventory.get(brushKey);
                if (activeBrush == null || activeBrush.length() == 0) activeBrush = brushKey;
                addToInventory(itemStack, slot);
            }
        }
        for (ItemStack unsortedItem : unsorted) {
            addToInventory(unsortedItem);
        }

        updateHasInventory();
        if (openInventoryPage >= inventories.size() && openInventoryPage != 0 && hasInventory) {
            setOpenInventoryPage(0);
        }

        checkHotbarCount();
    }

    protected void parseSpells(String spellString) {
        // Support YML-List-As-String format
        // Maybe don't need this anymore since loading lists is now a separate path
        spellString = spellString.replaceAll("[\\]\\[]", "");
        String[] spellNames = StringUtils.split(spellString, ',');
        loadSpells(Arrays.asList(spellNames));
    }

    protected void clearSpells() {
        // The configuration just wraps this map, so clearing it also clears the configuration.
        // We will not do that, since we'll be trying to load from the config right after this.
        spellLevels = new HashMap<>();
        spells.clear();
    }

    private int getNextFreeSlot(int minSlot) {
        int hotbarCount = getHotbarCount();
        int pageStart = hotbarCount * HOTBAR_INVENTORY_SIZE;
        int inventorySize = getInventorySize();
        Set<Integer> used = new HashSet<>();
        used.addAll(brushInventory.values());
        used.addAll(spellInventory.values());
        int slot = minSlot;
        while (true) {
            if (slot > pageStart && (slot - pageStart) % inventorySize > inventorySize - getOrganizeBuffer()) {
                slot = (((slot - pageStart) / inventorySize) + 1) * inventorySize + pageStart;
            }
            if (!used.contains(slot)) return slot;
            slot++;
        }
    }

    private int getMaxSlot() {
        int maxSlot = -1;
        for (Map.Entry<String, Integer> brush : brushInventory.entrySet()) {
            maxSlot = Math.max(maxSlot, brush.getValue());
        }
        for (Map.Entry<String, Integer> spell : spellInventory.entrySet()) {
            maxSlot = Math.max(maxSlot, spell.getValue());
        }
        return maxSlot;
    }

    protected void loadSpells(Collection<String> spellKeys) {
        clearSpells();
        WandUpgradePath path = getPath();
        int maxSlot = -1;
        int nextFreeSlot = 0;
        int hotbarCount = getHotbarCount();
        int pageStart = hotbarCount * HOTBAR_INVENTORY_SIZE;
        int inventorySize = getInventorySize();
        for (String spellName : spellKeys)
        {
            if (spellName.equalsIgnoreCase("none")) {
                if (maxSlot < 0) {
                    maxSlot = getMaxSlot();
                }
                maxSlot++;
                continue;
            }
            if (spellName.equalsIgnoreCase("newpage")) {
                if (maxSlot < 0) {
                    maxSlot = getMaxSlot();
                }
                if (maxSlot < pageStart) {
                    maxSlot = (((maxSlot + 1) / HOTBAR_INVENTORY_SIZE) + 1) * HOTBAR_INVENTORY_SIZE - 1;
                } else {
                    maxSlot = ((((maxSlot + 1) - pageStart) / inventorySize) + 1) * inventorySize + pageStart - 1;
                }
                continue;
            }
            String[] pieces = StringUtils.split(spellName, '@');
            Integer slot = parseSlot(pieces);

            // Handle aliases and upgrades smoothly
            SpellKey spellKey = new SpellKey(pieces[0].trim());
            spellKey = controller.unalias(spellKey);
            if (levelSpells) {
                int maxLevel = controller.getMaxLevel(spellKey.getBaseKey());
                int targetLevel = maxLevel;
                if (levelSpellsToPath != null) {
                    ProgressionPath levelPath = controller.getPath(levelSpellsToPath);
                    if (levelPath != null) {
                        SpellKey testLevelKey = new SpellKey(spellKey.getBaseKey(), targetLevel);
                        SpellTemplate testLevel = controller.getSpellTemplate(testLevelKey.getKey());
                        String targetPath = testLevel != null ? testLevel.getRequiredUpgradePath() : null;
                        while (targetLevel > 1 && targetPath != null && !levelPath.hasPath(targetPath)) {
                            targetLevel--;
                            testLevelKey = new SpellKey(spellKey.getBaseKey(), targetLevel);
                            testLevel = controller.getSpellTemplate(testLevelKey.getKey());
                            targetPath = testLevel != null ? testLevel.getRequiredUpgradePath() : null;
                        }

                        // The above logic will give us
                        // the first version we could level up *from*, so we want to go one level later
                        targetLevel = Math.min(targetLevel + 1, maxLevel);
                    } else {
                        targetLevel = 0;
                    }
                }
                if (targetLevel > spellKey.getLevel()) {
                    spellKey = new SpellKey(spellKey.getBaseKey(), targetLevel);
                }
            }
            SpellTemplate spell = controller.getSpellTemplate(spellKey.getKey());

            if (limitSpellsToPath && path != null && !path.containsSpell(spellKey.getBaseKey())) continue;

            // Downgrade spells if higher levels have gone missing
            while (spell == null && spellKey.getLevel() > 0)
            {
                spellKey = new SpellKey(spellKey.getBaseKey(), spellKey.getLevel() - 1);
                spell = controller.getSpellTemplate(spellKey.getKey());
            }
            if (spell != null)
            {
                spellKey = spell.getSpellKey();
                Integer currentLevel = spellLevels.get(spellKey.getBaseKey());
                if (spellKey.getLevel() > 1 && (currentLevel == null || currentLevel < spellKey.getLevel())) {
                    setSpellLevel(spellKey.getBaseKey(), spellKey.getLevel());
                }
                if (slot == null) {
                    slot = spellInventory.get(spellKey.getBaseKey());
                }
                if (slot == null) {
                    if (maxSlot > nextFreeSlot) {
                        nextFreeSlot = maxSlot + 1;
                    }
                    slot = getNextFreeSlot(nextFreeSlot);
                    nextFreeSlot = slot + 1;
                }
                spellInventory.put(spellKey.getBaseKey(), slot);
                spells.add(spellKey.getBaseKey());
                if (activeSpell == null || activeSpell.length() == 0)
                {
                    activeSpell = spellKey.getBaseKey();
                }
            }
        }
    }

    private void loadSpells() {
        Object wandSpells = getObject("spells");
        if (wandSpells != null) {
            if (wandSpells instanceof String) {
                parseSpells((String)wandSpells);
            } else if (wandSpells instanceof Collection) {
                @SuppressWarnings("unchecked")
                Collection<String> spellList = (Collection<String>)wandSpells;
                loadSpells(spellList);
            } else {
                clearSpells();
            }
        } else {
            clearSpells();
        }
    }

    protected void parseBrushes(String brushString) {
        // Support YML-List-As-String format
        // Maybe don't need this anymore since loading lists is now a separate path
        brushString = brushString.replaceAll("[\\]\\[]", "");
        String[] brushNames = StringUtils.split(brushString, ',');
        loadBrushes(Arrays.asList(brushNames));
    }

    protected void clearBrushes() {
        brushes.clear();
    }

    protected void loadBrushes(Collection<String> brushKeys) {
        WandUpgradePath path = getPath();
        clearBrushes();
        for (String materialName : brushKeys) {
            String[] pieces = StringUtils.split(materialName, '@');
            Integer slot = parseSlot(pieces);
            String materialKey = pieces[0].trim();
            if (limitBrushesToPath && path != null && !path.containsBrush(materialKey)) continue;
            if (slot != null) {
                brushInventory.put(materialKey, slot);
            }
            brushes.add(materialKey);
        }
    }

    private void loadBrushes() {
        Object wandBrushes = getObject("brushes", getObject("materials"));
        if (wandBrushes != null) {
            if (wandBrushes instanceof String) {
                parseBrushes((String)wandBrushes);
            } else if (wandBrushes instanceof Collection) {
                @SuppressWarnings("unchecked")
                Collection<String> brushList = (Collection<String>)wandBrushes;
                loadBrushes(brushList);
            } else {
                clearBrushes();
            }
        } else {
            clearBrushes();
        }
    }

    protected void loadBrushInventory(Map<String, ? extends Object> inventory) {
        if (inventory == null) return;
        WandUpgradePath path = getPath();
        for (Map.Entry<String, ?> brushEntry : inventory.entrySet()) {
            Object slot = brushEntry.getValue();
            String brushKey = brushEntry.getKey();
            if (limitBrushesToPath && path != null && !path.containsBrush(brushKey)) continue;
            if (slot != null && slot instanceof Integer) {
                brushInventory.put(brushKey, (Integer)slot);
            }
        }
    }

    protected void loadSpellInventory(Map<String, ? extends Object> inventory) {
        if (inventory == null) return;
        WandUpgradePath path = getPath();
        for (Map.Entry<String, ? extends Object> spellEntry : inventory.entrySet()) {
            String spellKey = spellEntry.getKey();
            if (limitSpellsToPath && path != null && !path.containsSpell(spellKey)) continue;
            Object slot = spellEntry.getValue();
            if (slot != null && slot instanceof Integer) {
                spellInventory.put(spellKey, (Integer)slot);
            }
        }
    }

    protected void loadSpellLevels(Map<String, ? extends Object> levels) {
        if (levels == null) return;
        for (Map.Entry<String, ? extends Object> spellEntry : levels.entrySet()) {
            Object level = spellEntry.getValue();
            if (level != null && level instanceof Integer) {
                SpellKey spellKey = new SpellKey(spellEntry.getKey(), (Integer)level);
                SpellTemplate spell = controller.getSpellTemplate(spellKey.getKey());

                // Downgrade spells if higher levels have gone missing
                while (spell == null && spellKey.getLevel() > 0)
                {
                    spellKey = new SpellKey(spellKey.getBaseKey(), spellKey.getLevel() - 1);
                    spell = controller.getSpellTemplate(spellKey.getKey());
                }

                setSpellLevel(spellKey.getBaseKey(), spellKey.getLevel());
            }
        }
    }

    @Nullable
    public static ItemStack createSpellItem(String spellKey, MagicController controller, Wand wand, boolean isItem) {
        String[] split = spellKey.split(" ", 2);
        return createSpellItem(controller.getSpellTemplate(split[0]), split.length > 1 ? split[1] : "", controller, wand == null ? null : wand.getActiveMage(), wand, isItem);
    }

    @Nullable
    public static ItemStack createSpellItem(String spellKey, MagicController controller, com.elmakers.mine.bukkit.api.magic.Mage mage, Wand wand, boolean isItem) {
        String[] split = spellKey.split(" ", 2);
        return createSpellItem(controller.getSpellTemplate(split[0]), split.length > 1 ? split[1] : "", controller, mage, wand, isItem);
    }

    @Nullable
    public ItemStack createSpellItem(String spellKey) {
        return createSpellItem(spellKey, "", false);
    }

    @Nullable
    public ItemStack createSpellItem(String spellKey, String args, boolean isItem) {
        SpellTemplate spell = mage == null ? controller.getSpellTemplate(spellKey) : mage.getSpell(spellKey);
        return createSpellItem(spell, args, controller, mage, this, isItem);
    }

    @Nullable
    public static ItemStack createSpellItem(SpellTemplate spell, String args, MagicController controller, com.elmakers.mine.bukkit.api.magic.Mage mage, Wand wand, boolean isItem) {
        if (spell == null || !(spell instanceof BaseSpell)) return null;
        ItemStack itemStack = ((BaseSpell)spell).createItem(mage);
        if (itemStack == null) {
            return null;
        }
        CompatibilityLib.getItemUtils().makeUnbreakable(itemStack);
        CompatibilityLib.getItemUtils().hideFlags(itemStack, HIDE_FLAGS);
        updateSpellItem(controller.getMessages(), itemStack, spell, args, mage, wand, wand == null ? null : wand.activeBrush, isItem);

        if (wand != null && wand.getMode() == WandMode.SKILLS && !isItem) {
            String mageClassKey = wand.getMageClassKey();
            ConfigurationSection skillsConfig = wand.getConfigurationSection("skills");
            CompatibilityLib.getInventoryUtils().configureSkillItem(itemStack, mageClassKey, skillsConfig);
        }

        return itemStack;
    }

    @Nullable
    protected ItemStack createBrushIcon(String materialKey) {
        return createBrushItem(materialKey, controller, this, false);
    }


    @Nullable
    public static ItemStack createBrushItem(String materialKey, com.elmakers.mine.bukkit.api.magic.MageController controller, Wand wand, boolean isItem) {
        return createBrushItem(materialKey, controller, wand, isItem, true);
    }

    @Nullable
    public static ItemStack createBrushItem(String materialKey, com.elmakers.mine.bukkit.api.magic.MageController controller, Wand wand, boolean isItem, boolean useWandName) {
        MaterialBrush brushData = MaterialBrush.parseMaterialKey(materialKey);
        if (brushData == null) return null;

        ItemStack itemStack = brushData.getItem(controller, isItem);
        if (BrushGlow || (isItem && BrushItemGlow))
        {
            CompatibilityLib.getItemUtils().addGlow(itemStack);
        }
        CompatibilityLib.getItemUtils().makeUnbreakable(itemStack);
        CompatibilityLib.getItemUtils().hideFlags(itemStack, HIDE_FLAGS);
        updateBrushItem(controller.getMessages(), itemStack, brushData, wand, useWandName);
        List<String> lore = itemStack.getItemMeta().getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        String keyMessage = wand != null ? wand.getMessage("brush.key") : controller.getMessages().get("brush.key");
        if (keyMessage != null && !keyMessage.isEmpty()) {
            CompatibilityLib.getInventoryUtils().wrapText(keyMessage.replace("$key", materialKey), lore);
        }
        boolean consumeFree = wand == null ? false : wand.isConsumeFree();
        if (brushData.getMode() == BrushMode.MATERIAL && !consumeFree) {
            Currency currency = controller.getBlockExchangeCurrency();
            if (currency != null) {
                ItemStack worthItem = brushData.getItemStack(1);
                Double itemWorth = controller.getWorth(worthItem, currency.getKey());
                if (itemWorth != null && itemWorth > 0) {
                    String message = wand != null ? wand.getMessage("brush.consumes", controller.getMessages().get("brush.consumes")) : controller.getMessages().get("brush.consumes");
                    if (message != null && !message.isEmpty()) {
                        lore.add(message.replace("$description", currency.formatAmount(itemWorth, controller.getMessages())));
                    }
                }
            }
        }
        CompatibilityLib.getCompatibilityUtils().setLore(itemStack, lore);
        return itemStack;
    }

    protected boolean findItem() {
        if (mage != null && item != null) {
            Player player = mage.getPlayer();
            if (player != null) {
                ItemStack itemInHand = player.getInventory().getItemInMainHand();
                if (itemInHand != null && !CompatibilityLib.getInventoryUtils().isSameInstance(itemInHand, item)
                    && controller.isSameItem(itemInHand, item)) {
                    item = itemInHand;
                    isInOffhand = false;
                    return true;
                }
                itemInHand = player.getInventory().getItemInOffHand();
                if (itemInHand != null && !CompatibilityLib.getInventoryUtils().isSameInstance(itemInHand, item)
                    && controller.isSameItem(itemInHand, item)) {
                    item = itemInHand;
                    isInOffhand = true;
                    return true;
                }

                itemInHand = player.getInventory().getItem(heldSlot);
                if (itemInHand != null && !CompatibilityLib.getInventoryUtils().isSameInstance(itemInHand, item)
                    && controller.isSameItem(itemInHand, item)) {
                    item = itemInHand;
                    isInOffhand = true;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void saveState() {
        // Make sure we're on the current item instance
        if (findItem()) {
            updateItemIcon();
            updateName();
            updateLore();
        }

        if (item == null || item.getType() == Material.AIR) return;

        // Check for upgrades that still have wand data
        if (isUpgrade && isWand(item)) {
            CompatibilityLib.getNBTUtils().removeMeta(item, WAND_KEY);
        }

        Object wandNode = CompatibilityLib.getNBTUtils().createTag(item, isUpgrade ? UPGRADE_KEY : WAND_KEY);
        if (wandNode == null) {
            String ownerMessage = "";
            if (mage != null) {
                ownerMessage = " for player " + mage.getName();
            }
            controller.getLogger().warning("Failed to save wand state for wand to : " + item + ownerMessage);
        } else {
            CompatibilityLib.getInventoryUtils().saveTagsToNBT(getConfiguration(), wandNode);
        }
    }

    @Nullable
    public static ConfigurationSection itemToConfig(ItemStack item, ConfigurationSection stateNode) {
        Object wandNode = CompatibilityLib.getNBTUtils().getTag(item, WAND_KEY);

        if (wandNode == null) {
            wandNode = CompatibilityLib.getNBTUtils().getTag(item, UPGRADE_KEY);
            if (wandNode == null) {
                return null;
            }
        }

        ConfigurationUtils.loadAllTagsFromNBT(stateNode, wandNode);

        return stateNode;
    }

    public static void configToItem(ConfigurationSection itemSection, ItemStack item) {
        ConfigurationSection stateNode = itemSection.getConfigurationSection("wand");
        Object wandNode = CompatibilityLib.getNBTUtils().createTag(item, Wand.WAND_KEY);
        if (wandNode != null) {
            CompatibilityLib.getInventoryUtils().saveTagsToNBT(stateNode, wandNode);
        }
    }

    @Override
    public void save(ConfigurationSection node, boolean filtered) {
        ConfigurationUtils.addConfigurations(node, getConfiguration());

        // Filter out some fields
        if (filtered) {
            node.set("id", null);
            node.set("owner_id", null);
            node.set("owner", null);
            node.set("template", null);
            node.set("mana_timestamp", null);
            node.set("enchant_count", null);
        }

        if (isUpgrade) {
            node.set("upgrade", true);
        }

        if (template != null && !template.isEmpty()) {
            node.set("template", null);
            node.set("inherit", template);
        }
    }

    public void save() {
        saveState();
        updateName();
        updateLore();
    }

    public void updateBrushInventory() {
        if (brushInventory.isEmpty()) {
            setProperty("brush_inventory", null);
        } else {
            setProperty("brush_inventory", new HashMap<>(brushInventory));
        }
    }

    @Override
    public void updateBrushInventory(Map<String, Integer> updateBrushes) {
        for (Map.Entry<String, Integer> brushEntry : brushInventory.entrySet()) {
            String brushKey = brushEntry.getKey();
            Integer slot = updateBrushes.get(brushKey);
            if (slot != null) {
                brushEntry.setValue(slot);
            }
        }
    }

    public void updateSpellInventory() {
        if (spellInventory.isEmpty()) {
            setProperty("spell_inventory", null);
        } else {
            setProperty("spell_inventory", new HashMap<>(spellInventory));
        }
    }

    @Override
    public void updateSpellInventory(Map<String, Integer> updateSpells) {
        for (Map.Entry<String, Integer> spellEntry : spellInventory.entrySet()) {
            String spellKey = spellEntry.getKey();
            Integer slot = updateSpells.get(spellKey);
            if (slot != null) {
                spellEntry.setValue(slot);
            }
        }
    }

    private void migrateProtection(String legacy, String migrateTo) {
        if (hasProperty(legacy)) {
            double protection = getDouble(legacy);
            clearProperty(legacy);
            setProperty("protection." + migrateTo, protection);
        }
    }

    @Nullable
    private MaterialAndData loadIcon(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }

        Icon icon = controller.getIcon(key);
        String iconType = icon == null ? null : icon.getType();
        if (iconType != null && (iconType.equals("upgrade") || iconType.equals("wand"))) {
            com.elmakers.mine.bukkit.api.block.MaterialAndData iconMaterial = icon.getItemMaterial(controller.isLegacyIconsEnabled());
            if (iconMaterial != null && iconMaterial instanceof MaterialAndData) {
                return (MaterialAndData)iconMaterial;
            }
        }

        // This lets us use spell books as wands, which is a fancy way to make
        // books that can teach you about a spell
        if (key.startsWith("book:")) {
            ItemStack bookItem = controller.createItem(key);
            if (bookItem != null) {
                return new MaterialAndData(bookItem);
            }
        }

        com.elmakers.mine.bukkit.api.block.MaterialAndData materialData = null;
        ItemData itemData = controller.getItem(key);
        if (itemData != null) {
            materialData = itemData.getMaterialAndData();
        } else {
            materialData = new MaterialAndData(key);
            if (!materialData.isValid()) {
                ItemStack iconItem = controller.createItem(key, mage);
                if (!CompatibilityLib.getItemUtils().isEmpty(iconItem)) {
                    materialData = new MaterialAndData(iconItem);
                }
            }

        }

        // try icon again if we still are invalid
        if ((materialData == null || !materialData.isValid()) && icon != null) {
            com.elmakers.mine.bukkit.api.block.MaterialAndData iconMaterial = icon.getItemMaterial(controller.isLegacyIconsEnabled());
            if (iconMaterial != null && iconMaterial instanceof MaterialAndData) {
                return (MaterialAndData)iconMaterial;
            }
        }
        return materialData instanceof MaterialAndData ? (MaterialAndData)materialData : null;
    }

    protected void loadParameters() {
        // These should only be parameters to are OK and safe (performance-wise in particular)
        // to change on the fly without fully reloading the wand
        consumeReduction = getFloat("consume_reduction");
        cooldownReduction = getFloat("cooldown_reduction");
        costReduction = getFloat("cost_reduction");
        power = getFloat("power");
        blockChance = getFloat("block_chance");
        blockReflectChance = getFloat("block_reflect_chance");
        blockFOV = getFloat("block_fov");
        blockMageCooldown = getInt("block_mage_cooldown");
        blockCooldown = getInt("block_cooldown");

        manaPerDamage = getFloat("mana_per_damage");
        earnMultiplier = getFloat("earn_multiplier", getFloat("sp_multiplier", 1));
        quietLevel = getInt("quiet");
        if (quietLevel == 0 && getBoolean("quiet")) {
            quietLevel = 1;
        }
        effectBubbles = getBoolean("effect_bubbles");
        superPowered = getBoolean("powered");
        superProtected = getBoolean("protected");
        glow = getBoolean("glow");
        spellGlow = getBoolean("spell_glow");
        undroppable = getBoolean("undroppable");
        swappable = getBoolean("swappable", true);
        maxEnchantCount = getInt("max_enchant_count");
        showCycleModeLore = getBoolean("show_cycle_lore", true);

        alwaysUseActiveName = false;
        neverUseActiveName = false;
        if (hasProperty("use_active_name")) {
            if (getBoolean("use_active_name")) {
                alwaysUseActiveName = true;
            } else {
                neverUseActiveName = true;
            }
        }

        activeEffectsOnly = getBoolean("active_effects");
        effectParticleData = getFloat("effect_particle_data");
        effectParticleCount = getInt("effect_particle_count");
        effectParticleRadius = getDouble("effect_particle_radius");
        effectParticleOffset = getDouble("effect_particle_offset");
        effectParticleInterval = getInt("effect_particle_interval");
        effectParticleMinVelocity = getDouble("effect_particle_min_velocity");
        effectSoundInterval =  getInt("effect_sound_interval");
        castLocation = getVector("cast_location");

        castInterval = getInt("cast_interval");
        castMinVelocity = getDouble("cast_min_velocity");
        castVelocityDirection = getVector("cast_velocity_direction");
        castSpell = getString("cast_spell");
        String castParameterString = getString("cast_parameters", null);
        if (castParameterString != null && !castParameterString.isEmpty()) {
            castParameters = ConfigurationUtils.newConfigurationSection();
            ConfigurationUtils.addParameters(StringUtils.split(castParameterString, ' '), castParameters);
        } else {
            castParameters = null;
        }

        leftClickAction = parseWandAction(getString("left_click"), leftClickAction);
        rightClickAction = parseWandAction(getString("right_click"), rightClickAction);
        dropAction = parseWandAction(getString("drop"), dropAction);
        swapAction = parseWandAction(getString("swap"), swapAction);
        noBowpullAction = parseWandAction(getString("no_bowpull"), noBowpullAction);
        leftClickSneakAction = parseWandAction(getString("left_click_sneak"), leftClickSneakAction);
        rightClickSneakAction = parseWandAction(getString("right_click_sneak"), rightClickSneakAction);
        dropSneakAction = parseWandAction(getString("drop_sneak"), dropSneakAction);
        swapSneakAction = parseWandAction(getString("swap_sneak"), swapSneakAction);
        noBowpullSneakAction = parseWandAction(getString("no_bowpull_sneak"), noBowpullSneakAction);

        // Update glyph bar configuration
        glyphHotbar.load(getConfigurationSection("glyph_hotbar"));

        // Boss bar, can be a simple boolean or a config
        bossBarConfiguration = null;
        if (getBoolean("boss_bar", false)) {
            bossBarConfiguration = new BossBarConfiguration(controller, ConfigurationUtils.newConfigurationSection());
        } else {
            ConfigurationSection config = getConfigurationSection("boss_bar");
            if (config != null) {
                bossBarDisplayMode = parseDisplayMode(config, WandDisplayMode.COOLDOWN);
                if (bossBarDisplayMode != WandDisplayMode.NONE) {
                    bossBarConfiguration = new BossBarConfiguration(controller, config, "$wand");
                }
            } else {
                String bossBarMode = getString("boss_bar");
                bossBarDisplayMode = parseDisplayMode(bossBarMode, WandDisplayMode.NONE);
                if (bossBarDisplayMode != WandDisplayMode.NONE) {
                    bossBarConfiguration = new BossBarConfiguration(controller, ConfigurationUtils.newConfigurationSection());
                }
            }
        }
        if (bossBarConfiguration == null && bossBar != null) {
            removeBossBar();
        }

        WandDisplayMode previousXP = xpBarDisplayMode;
        WandDisplayMode previousLevel = levelDisplayMode;
        ConfigurationSection config = getConfigurationSection("xp_display");
        if (config != null) {
            xpBarDisplayMode = parseDisplayMode(config, WandDisplayMode.MANA);
        } else {
            String displayMode = getString("xp_display");
            xpBarDisplayMode = parseDisplayMode(displayMode, WandDisplayMode.MANA);
        }

        config = getConfigurationSection("level_display");
        if (config != null) {
            levelDisplayMode = parseDisplayMode(config, WandDisplayMode.SP);
        } else {
            // Backwards-compatibility
            String currencyDisplay = getString("currency_display", "sp");
            if (!currencyDisplay.equals("sp")) {
                if (currencyDisplay.isEmpty()) {
                    levelDisplayMode = WandDisplayMode.NONE;
                } else {
                    levelDisplayMode = WandDisplayMode.getCurrency(currencyDisplay);
                }
            } else {
                String displayMode = getString("level_display");
                levelDisplayMode = parseDisplayMode(displayMode, WandDisplayMode.SP);
            }
        }

        if ((previousXP != xpBarDisplayMode && xpBarDisplayMode == WandDisplayMode.NONE)
            || (previousLevel != levelDisplayMode && levelDisplayMode == WandDisplayMode.NONE)) {
            resetXPDisplay();
        }

        config = getConfigurationSection("action_bar");
        if (config != null) {
            actionBarMessage = config.getString("message");
            actionBarOpenMessage = config.getString("open_message", actionBarMessage);
            if (actionBarMessage.isEmpty()) {
                actionBarMessage = null;
            } else {
                actionBarInterval = config.getInt("interval", 1000);
                actionBarDelay = config.getInt("delay", 0);
                actionBarMana = config.getBoolean("uses_mana");
                actionBarFont = config.getString("font");
            }
            lastActionBar = 0;
        } else {
            actionBarMessage = getString("action_bar");
            actionBarOpenMessage = actionBarMessage;
        }
        if (actionBarMessage == null) {
            actionBarMana = false;
        }

        // Some cleanup and sanity checks. In theory we don't need to store any non-zero value (as it is with the traders)
        // so try to keep defaults as 0/0.0/false.
        if (effectSound == null) {
            effectSoundInterval = 0;
        } else {
            effectSoundInterval = (effectSoundInterval == 0) ? 5 : effectSoundInterval;
        }

        if (effectParticle == null) {
            effectParticleInterval = 0;
        }
    }

    @Override
    public void loadProperties() {
        super.loadProperties();
        // Slotted upgrades can override anything else
        slots = null;
        List<String> slotKeys = getStringList("slots");
        if (slotKeys != null && !slotKeys.isEmpty()) {
            slots = new ArrayList<>();
            for (String slotKey : slotKeys) {
                slots.add(new WandUpgradeSlot(controller, slotKey));
            }
        } else {
            ConfigurationSection slotConfig = getConfigurationSection("slots");
            if (slotConfig != null) {
                Set<String> keys = slotConfig.getKeys(false);
                if (!keys.isEmpty()) {
                    slots = new ArrayList<>();
                    for (String key : keys) {
                        slots.add(new WandUpgradeSlot(controller, key, slotConfig.getConfigurationSection(key)));
                    }
                }
            }
        }
        unslotted = null;
        List<String> slottedKeys = getStringList("slotted");
        if (slottedKeys != null && !slottedKeys.isEmpty()) {
            for (String slottedKey : slottedKeys) {
                Wand slottedWand = controller.createWand(slottedKey);
                if (slottedWand == null || !addSlotted(slottedWand)) {
                    if (unslotted == null) {
                        unslotted = new ArrayList<>();
                    }
                    unslotted.add(slottedKey);
                }
            }
        }
        updateSlotted();

        // Requirements upgrades come next
        if (hasProperty("requirement_properties")) {
            List<ConfigurationSection> requirementList = getSectionList("requirement_properties");
            if (requirementList.isEmpty()) {
                ConfigurationSection singleRequirement = getConfigurationSection("requirement_properties");
                if (singleRequirement != null) {
                    requirementList.add(singleRequirement);
                }
            }
            if (!requirementList.isEmpty()) {
                requirementProperties = new ArrayList<>();
                for (ConfigurationSection requirementConfig : requirementList) {
                    RequirementProperties requirement = new RequirementProperties(requirementConfig);
                    if (!requirement.isEmpty()) {
                        requirementProperties.add(requirement);
                    }
                }
                if (mage != null) {
                    updateRequirementConfiguration();
                }
            } else {
                requirementProperties = null;
                requirementConfiguration = null;
            }
        } else {
            requirementProperties = null;
            requirementConfiguration = null;
        }

        // Read path first since it can be used to override any other property
        path = getString("path");

        // Reload base properties, this reloading is unfortunate but we need CasterProperties
        // to be aware of slotted upgrades and requirements
        super.loadProperties();

        if (OLD_WAND_LOCKED) {
            // Can't support locked wands this way
            locked = false;
            modifiable = getBoolean("locked", false);
        } else {
            locked = getBoolean("locked", false);
            modifiable = getBoolean("modifiable", true);
        }
        autoAbsorb = getBoolean("auto_absorb", false);
        inventoryOpenLore = getMessage("inventory_open", "");

        ConfigurationSection protectionConfig = getConfigurationSection("protection");
        if (protectionConfig == null && hasProperty("protection")) {
            migrateProtection("protection", "overall");
            migrateProtection("protection_physical", "physical");
            migrateProtection("protection_projectiles", "projectile");
            migrateProtection("protection_falling", "fall");
            migrateProtection("protection_fire", "fire");
            migrateProtection("protection_explosions", "explosion");
            protectionConfig = getConfigurationSection("protection");
        }

        if (protectionConfig != null) {
            protection = new HashMap<>();
            for (String protectionKey : protectionConfig.getKeys(false)) {
                protection.put(protectionKey, protectionConfig.getDouble(protectionKey));
            }
        }

        hasId = getBoolean("unique", false);
        String interactibleMaterialKey = getString("interactible");
        if (interactibleMaterialKey != null) {
            interactibleMaterials = controller.getMaterialSetManager().fromConfigEmpty(interactibleMaterialKey);
        } else {
            interactibleMaterials = null;
        }

        String singleClass = getString("class");
        if (singleClass != null && !singleClass.isEmpty()) {
            mageClassKeys = new ArrayList<>();
            mageClassKeys.add(singleClass);
        } else {
            mageClassKeys = getStringList("classes");
        }

        // Check for single-use wands
        uses = getInt("uses");
        hasUses = uses > 0;
        if (hasUses) {
            // Backwards-compatibility
            boolean preuse = getBoolean("preuse", false);
            useMode = parseUseMode(getString("use_mode"), preuse ? WandUseMode.PRECAST : WandUseMode.SUCCESS);
        }

        // This overrides the value loaded in CasterProperties
        if (!usesMana()) {
            setProperty("mana_timestamp", null);
        } else if (!regenWhileInactive) {
            setProperty("mana_timestamp", System.currentTimeMillis());
        }

        id = getString("id");
        isUpgrade = getBoolean("upgrade");
        keep = getBoolean("keep");
        worn = getBoolean("worn", getBoolean("passive"));
        indestructible = getBoolean("indestructible");
        isHeroes = getBoolean("heroes");
        bound = getBoolean("bound");
        boundDisplayName = getString("bound_name", "display").equals("display");
        forceUpgrade = getBoolean("force");
        autoOrganize = getBoolean("organize");
        autoAlphabetize = getBoolean("alphabetize");
        autoFill = getBoolean("fill");
        rename = getBoolean("rename");
        renameDescription = getBoolean("rename_description");
        enchantCount = getInt("enchant_count");
        inventoryRows = getInt("inventory_rows", 5);
        resetManaOnActivate = null;
        if (hasProperty("reset_mana_on_activate")) {
            String asString = getString("reset_mana_on_activate");
            if (asString.equalsIgnoreCase("true")) {
                resetManaOnActivate = 0.0;
            } else if (!asString.equalsIgnoreCase("false")) {
                resetManaOnActivate = getDouble("reset_mana_on_activate", 0);
            }
        }

        if (hasProperty("effect_particle")) {
            effectParticle = ConfigurationUtils.toParticleEffect(getString("effect_particle"));
            effectParticleData = 0;
        } else {
            effectParticle = null;
        }
        if (hasProperty("effect_sound")) {
            effectSound = ConfigurationUtils.toSoundEffect(getString("effect_sound"));
        } else {
            effectSound = null;
        }

        WandMode newMode = parseWandMode(getString("mode"), controller.getDefaultWandMode());
        if (newMode != mode) {
            if (isInventoryOpen()) {
                closeInventory();
            }
            mode = newMode;
        }

        brushMode = parseWandMode(getString("brush_mode"), controller.getDefaultBrushMode());

        // Backwards compatibility
        if (getBoolean("mode_drop", false)) {
            dropAction = WandAction.TOGGLE;
            swapAction = WandAction.CYCLE_HOTBAR;
            rightClickAction = WandAction.NONE;
            quickCast = true;
            // This is to turn the redundant spell lore off
            quickCastDisabled = true;
            manualQuickCastDisabled = false;
        } else if (mode == WandMode.CAST) {
            leftClickAction = WandAction.CAST;
            rightClickAction = WandAction.CAST;
            swapAction = WandAction.NONE;
            dropAction = WandAction.NONE;
        } else if (mode == WandMode.CYCLE) {
            leftClickAction = WandAction.CAST;
            rightClickAction = WandAction.NONE;
            swapAction = WandAction.NONE;
            dropAction = WandAction.CYCLE;
        } else {
            leftClickAction = WandAction.NONE;
            rightClickAction = WandAction.NONE;
            dropAction = WandAction.NONE;
            swapAction = WandAction.NONE;
            quickCast = false;
            quickCastDisabled = false;
            manualQuickCastDisabled = false;
        }

        String quickCastType = getString("quick_cast", getString("mode_cast"));
        if (quickCastType != null) {
            if (quickCastType.equalsIgnoreCase("true")) {
                quickCast = true;
                // This is to turn the redundant spell lore off
                quickCastDisabled = true;
                manualQuickCastDisabled = false;
            } else if (quickCastType.equalsIgnoreCase("manual")) {
                quickCast = false;
                quickCastDisabled = true;
                manualQuickCastDisabled = false;
            } else if (quickCastType.equalsIgnoreCase("disable")) {
                quickCast = false;
                quickCastDisabled = true;
                manualQuickCastDisabled = true;
            } else {
                quickCast = false;
                quickCastDisabled = false;
                manualQuickCastDisabled = false;
            }
        }

        owner = getString("owner");
        ownerId = getString("owner_id");
        template = getString("template");
        upgradeTemplate = getString("upgrade_template");

        activeSpell = getString("active_spell");
        if (activeSpell != null && activeSpell.contains("|")) {
            SpellKey activeKey = new SpellKey(activeSpell);
            activeSpell = activeKey.getBaseKey();
            setProperty("active_spell", activeSpell);
        }
        for (int i = 0; i < alternateSpells.length; i++) {
            String key = "alternate_spell";
            if (i > 0) {
                key = key + (i + 1);
            }
            alternateSpells[i] = getString(key);
        }
        activeBrush = getString("active_brush", getString("active_material"));

        if (hasProperty("hotbar")) {
            currentHotbar = getInt("hotbar");
        }

        if (hasProperty("page")) {
            int page = getInt("page");
            if (page != openInventoryPage) {
                openInventoryPage = page;
            }
        }

        // Default to template names, override with localizations and finally with wand data
        wandName = controller.getMessages().get("wand.default_name");
        description = "";

        // Check for migration information in the template config
        ConfigurationSection templateConfig = null;
        if (template != null && !template.isEmpty()) {
            templateConfig = controller.getWandTemplateConfiguration(template);
            if (templateConfig != null) {
                wandName = templateConfig.getString("name", wandName);
                description = templateConfig.getString("description", description);

                int templateUses = templateConfig.getInt("uses");
                isSingleUse = templateUses == 1;
                hasUses = hasUses || templateUses > 0;
            }
            wandName = controller.getMessages().get("wands." + template + ".name", wandName);
            description = controller.getMessages().get("wands." + template + ".description", description);
        }
        wandName = getString("name", wandName);
        description = getString("description", description);

        List<String> flagList = getStringList("item_flags");
        if (flagList != null) {
            hideFlags = 0;
            for (String flagKey : flagList) {
                try {
                    ItemFlag flag = ItemFlag.valueOf(flagKey.toUpperCase());
                    // Making some assumptions here, but should be ok..
                    hideFlags |= (1 << flag.ordinal());
                } catch (Exception ex) {
                    controller.getLogger().warning("Invalid ItemFlag: " + flagKey);
                }
            }
        } else {
            hideFlags = getProperty("hide_flags", HIDE_FLAGS);
        }

        WandTemplate wandTemplate = getTemplate();

        boolean legacyIcons = controller.isLegacyIconsEnabled();
        if (hasIcon(legacyIcons, "icon_inactive")) {
            String iconKey = getIcon(legacyIcons, "icon_inactive");
            if (wandTemplate != null) {
                iconKey = wandTemplate.migrateIcon(iconKey);
            }
            if (iconKey != null) {
                inactiveIcon = loadIcon(iconKey);
            }
        } else {
            inactiveIcon = null;
        }
        if (inactiveIcon != null && (inactiveIcon.getMaterial() == null || inactiveIcon.getMaterial() == Material.AIR))
        {
            inactiveIcon = null;
        }
        inactiveIconDelay = getInt("icon_inactive_delay");
        randomizeOnActivate = randomizeOnActivate && hasIcon(legacyIcons, "randomize_icon");
        if (randomizeOnActivate) {
            String randomizeIcon = getIcon(legacyIcons, "randomize_icon");
            setIcon(loadIcon(randomizeIcon));
            if (item == null) {
                controller.getLogger().warning("Invalid randomize_icon in wand '" + template + "' config: " + randomizeIcon);
                setIcon(new MaterialAndData(DefaultWandMaterial));
            }
        } else if (hasIcon(legacyIcons)) {
            String iconKey = getIcon(legacyIcons);
            if (wandTemplate != null) {
                iconKey = wandTemplate.migrateIcon(iconKey);
            }
            if (iconKey.contains(",")) {
                Random r = new Random();
                String[] keys = StringUtils.split(iconKey, ',');
                iconKey = keys[r.nextInt(keys.length)];
            }
            // Port old custom wand icons
            if (templateConfig != null && iconKey.contains("i.imgur.com")) {
                iconKey = ConfigurationUtils.getIcon(templateConfig, legacyIcons);
            }
            setIcon(loadIcon(iconKey));
            if (item == null) {
                controller.getLogger().warning("Invalid icon in wand '" + template + "' config: " + iconKey);
                setIcon(new MaterialAndData(DefaultWandMaterial));
            }
            updateIcon();
        } else if (isUpgrade) {
            setIcon(new MaterialAndData(DefaultUpgradeMaterial));
        } else {
            setIcon(new MaterialAndData(DefaultWandMaterial));
        }

        isCostFree = false;
        if (getBoolean("infinity_cost_free", false)) {
            ItemMeta itemMeta = item.getItemMeta();
            if (itemMeta != null && itemMeta.hasEnchant(Enchantment.ARROW_INFINITE)) {
                isCostFree = true;
            }
        }

        // Check for path-based migration, may update icons
        com.elmakers.mine.bukkit.api.wand.WandUpgradePath upgradePath = getPath();
        if (upgradePath != null) {
            hasSpellProgression = upgradePath.getSpells().size() > 0
                    || upgradePath.getExtraSpells().size() > 0
                    || upgradePath.getRequiredSpells().size() > 0;
            upgradePath.checkMigration(this);
        } else {
            hasSpellProgression = false;
        }
        if (isHeroes) {
            hasSpellProgression = true;
        }

        brushInventory.clear();
        spellInventory.clear();
        limitSpellsToPath = getBoolean("limit_spells_to_path");
        limitBrushesToPath = getBoolean("limit_brushes_to_path");
        levelSpellsToPath = getString("level_spells_to_path");
        levelSpells = getBoolean("level_spells", levelSpellsToPath != null && !levelSpellsToPath.isEmpty());

        Object brushInventoryRaw = getObject("brush_inventory");
        if (brushInventoryRaw != null) {
            // Not sure this will ever appear as a Map, but just in case
            if (brushInventoryRaw instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Integer> brushInventory = (Map<String, Integer>)brushInventoryRaw;
                loadBrushInventory(brushInventory);
            } else if (brushInventoryRaw instanceof ConfigurationSection) {
                loadBrushInventory(CompatibilityLib.getCompatibilityUtils().getMap((ConfigurationSection)brushInventoryRaw));
            }
        }

        Object spellInventoryRaw = getObject("spell_inventory");
        if (spellInventoryRaw != null) {
            // Not sure this will ever appear as a Map, but just in case
            if (spellInventoryRaw instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Integer> spellInventory = (Map<String, Integer>)spellInventoryRaw;
                loadSpellInventory(spellInventory);
            } else if (spellInventoryRaw instanceof ConfigurationSection) {
                loadSpellInventory(CompatibilityLib.getCompatibilityUtils().getMap((ConfigurationSection)spellInventoryRaw));
            }
        }

        loadSpells();

        // Load spell levels
        Object spellLevelsRaw = getObject("spell_levels");
        if (spellLevelsRaw != null) {
            // Not sure this will ever appear as a Map, but just in case
            if (spellLevelsRaw instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Integer> spellLevels = (Map<String, Integer>)spellLevelsRaw;
                loadSpellLevels(spellLevels);
            } else if (spellLevelsRaw instanceof ConfigurationSection) {
                loadSpellLevels(CompatibilityLib.getCompatibilityUtils().getMap((ConfigurationSection)spellLevelsRaw));
            }
        }
        checkActiveSpell();
        loadBrushes();

        // Spells may have contained an inventory from migration or templates with a spell@slot format.
        if (spellInventoryRaw == null) {
            updateSpellInventory();
        }

        checkActiveMaterial();
        loadParameters();

        // Automatically decide if instruction messages and lore should be used
        if (inventoryRows <= 0) inventoryRows = 1;

        instructions = bound || usesSP() || usesMana() || hasInventory;
        instructionsLore = hasInventory;
        instructionsLore = instructionsLore || needsInstructions(rightClickAction);
        instructionsLore = instructionsLore || needsInstructions(leftClickAction);
        instructionsLore = instructionsLore || needsInstructions(dropAction);
        instructionsLore = instructionsLore || needsInstructions(swapAction);

        instructions = getBoolean("instructions", instructions);
        instructionsLore = getBoolean("lore_instructions", instructionsLore);
    }

    private boolean needsInstructions(WandAction action) {
        switch (action) {
            case TOGGLE:
            case CYCLE:
            case CYCLE_HOTBAR:
            case CYCLE_ACTIVE_HOTBAR:
                return true;
            default:
                return false;
        }
    }

    private WandDisplayMode parseDisplayMode(String displayMode, WandDisplayMode defaultMode) {
        WandDisplayMode mode = null;
        try {
            mode = WandDisplayMode.parse(displayMode);
        } catch (Exception ex) {
            controller.getLogger().warning("Invalid display mode: " + ex.getMessage());
        }
        return mode == null ? defaultMode : mode;
    }

    private WandDisplayMode parseDisplayMode(ConfigurationSection config, WandDisplayMode defaultMode) {
        WandDisplayMode mode = defaultMode;
        try {
            mode = WandDisplayMode.parse(controller, config, "mode");
        } catch (Exception ex) {
            controller.getLogger().warning("Invalid display mode: " + ex.getMessage());
        }
        return mode == null ? defaultMode : mode;
    }

    private void checkBossBar() {
        if (mage == null) return;
        Player player = mage.getPlayer();
        if (player == null || bossBarConfiguration == null) return;
        if (!bossBarDisplayMode.isEnabled(this)) return;
        if (bossBar == null) {
            bossBar = bossBarConfiguration.createBossBar(this);
            bossBar.addPlayer(player);
        }
        double progress = bossBarDisplayMode.getProgress(this);
        bossBar.setProgress(Math.min(1, Math.max(0, progress)));
        bossBarConfiguration.updateTitle(bossBar, this);
    }

    private void removeBossBar() {
        if (bossBar != null) {
            bossBar.setVisible(false);
            bossBar.removeAll();
            bossBar = null;
        }
    }

    @Override
    public void describe(CommandSender sender, @Nullable Set<String> ignoreProperties) {
        ChatColor wandColor = isModifiable() ? ChatColor.AQUA : ChatColor.RED;
        sender.sendMessage(wandColor + getName());
        if (isUpgrade) {
            sender.sendMessage(ChatColor.YELLOW + "(Upgrade)");
        }
        if (description.length() > 0) {
            sender.sendMessage(ChatColor.ITALIC + "" + ChatColor.GREEN + description);
        } else {
            sender.sendMessage(ChatColor.ITALIC + "" + ChatColor.GREEN + "(No Description)");
        }
        if (owner != null && owner.length() > 0 && ownerId != null && ownerId.length() > 0) {
            sender.sendMessage(ChatColor.ITALIC + "" + ChatColor.WHITE + owner + " (" + ChatColor.GRAY + ownerId + ChatColor.WHITE + ")");
        } else {
            sender.sendMessage(ChatColor.ITALIC + "" + ChatColor.WHITE + "(No Owner)");
        }

        super.describe(sender, ignoreProperties);

        WandTemplate template = getTemplate();
        if (template != null) {
            sender.sendMessage("" + ChatColor.BOLD + ChatColor.GREEN + "Template Configuration:");
            ConfigurationSection itemConfig = getConfiguration();
            Set<String> ownKeys = itemConfig.getKeys(false);
            template.describe(sender, ignoreProperties, ownKeys);
        }
    }

    private static String getBrushDisplayName(Messages messages, com.elmakers.mine.bukkit.api.block.MaterialBrush brush) {
        String materialName = brush == null || !brush.isValid() ? null : brush.getName(messages);
        if (materialName == null) {
            materialName = "none";
        }
        String brushPrefix = CompatibilityLib.getCompatibilityUtils().translateColors(messages.get("wand.brush_prefix"));
        return brushPrefix + materialName;
    }

    private static String getSpellDisplayName(Messages messages, SpellTemplate spell, com.elmakers.mine.bukkit.api.block.MaterialBrush brush) {
        String name = "";
        if (spell != null) {
            String spellPrefix = CompatibilityLib.getCompatibilityUtils().translateColors(messages.get("wand.spell_prefix"));
            if (brush != null && spell.usesBrush() && spell.showBrush()) {
                name = spellPrefix + spell.getName() + " " + getBrushDisplayName(messages, brush) + ChatColor.WHITE;
            } else {
                name = spellPrefix + spell.getName() + ChatColor.WHITE;
            }
        }

        return name;
    }

    private String getCustomName(String displayName, SpellTemplate spell, com.elmakers.mine.bukkit.api.block.MaterialBrush brush) {
        String name = displayName;

        // $name
        name = name.replace("$name", wandName);

        // $path
        String pathName = getPathName();
        if (pathName != null) {
            name = name.replace("$path", pathName);
        }

        // $spell
        String spellName = spell == null ? "" : spell.getName();
        name = name.replace("$spell", spellName);

        // $brush
        String brushName = brush == null ? "" : brush.getName();
        name = name.replace("$brush", brushName);

        // $uses
        name = name.replace("$uses", Integer.toString(getRemainingUses()));

        return CompatibilityLib.getCompatibilityUtils().translateColors(name);
    }

    private String getActiveWandName(SpellTemplate spell, com.elmakers.mine.bukkit.api.block.MaterialBrush brush) {
        String customName = getString("display_name");
        if (customName != null && !customName.isEmpty()) {
            return getCustomName(customName, spell, brush);
        }

        // Build wand name
        int remaining = getRemainingUses();
        String wandColorPrefix = (hasUses && remaining <= 1) ? "single_use_prefix" : isModifiable()
                ? (bound ? "bound_prefix" : "unbound_prefix") :
                  (path != null && path.length() > 0 ? "has_path_prefix" : "unmodifiable_prefix");
        String name = CompatibilityLib.getCompatibilityUtils().translateColors(getMessage(wandColorPrefix)) + getDisplayName();
        if (randomizeOnActivate) return name;

        Set<String> spells = getSpells();

        // Add active spell to description
        Messages messages = controller.getMessages();
        boolean showSpell = isModifiable() && hasSpellProgression();
        showSpell = !quickCast && (spells.size() > 1 || showSpell) && getMode() != WandMode.SKILLS;
        showSpell = showSpell || alwaysUseActiveName;
        if (spell != null && showSpell) {
            name = getSpellDisplayName(messages, spell, brush) + " (" + name + ChatColor.WHITE + ")";
        }

        if (remaining > 1) {
            String message = getMessage("uses_remaining_brief");
            name = name + ChatColor.DARK_RED + " (" + message.replace("$count", Integer.toString(remaining)) + ChatColor.DARK_RED + ")";
        }
        return name;
    }

    private String getActiveWandName(SpellTemplate spell) {
        return getActiveWandName(spell, mage == null ? MaterialBrush.parseMaterialKey(activeBrush) : mage.getBrush());
    }

    private String getActiveWandName(MaterialBrush brush) {
        SpellTemplate spell = null;
        if (activeSpell != null && activeSpell.length() > 0) {
            spell = controller.getSpellTemplate(activeSpell);
        }
        return getActiveWandName(spell, brush);
    }

    private String getActiveWandName() {
        SpellTemplate spell = null;
        if (activeSpell != null && activeSpell.length() > 0) {
            spell = controller.getSpellTemplate(activeSpell);
        }
        return getActiveWandName(spell);
    }

    protected String getDisplayName() {
        return CompatibilityLib.getCompatibilityUtils().translateColors(randomizeOnActivate ? getMessage("randomized_name") : wandName);
    }

    public void updateName(boolean isActive) {
        updateName(isActive, false);
    }

    public void updateName(boolean isActive, boolean stripColors) {
        String name;
        findItem();
        if (!neverUseActiveName && (isActive || alwaysUseActiveName)) {
            name = !isUpgrade ? getActiveWandName() :
                    CompatibilityLib.getCompatibilityUtils().translateColors(getMessage("upgrade_prefix")) + getDisplayName();
        } else {
            name = stripColors ? getDisplayName() : getActiveWandName(null, null);
        }

        if (stripColors) {
            name = ChatColor.stripColor(name);
        }
        CompatibilityLib.getCompatibilityUtils().setDisplayName(item, name);

        // This is a bit of a hack to make anvil+book combining show enchantments
        if (!stripColors) {
            CompatibilityLib.getItemUtils().hideFlags(item, hideFlags);
        } else if ((hideFlags & 1) == 1) {
            CompatibilityLib.getItemUtils().hideFlags(item, hideFlags & ~1);
        }
    }

    private void updateName() {
        updateName(isActive);
    }

    protected static String convertToHTML(String line) {
        int tagCount = 1;
        line = "<span style=\"color:white\">" + line;
        for (ChatColor c : ChatColor.values()) {
            tagCount += StringUtils.countMatches(line, c.toString());
            String replaceStyle = "";
            if (c == ChatColor.ITALIC) {
                replaceStyle = "font-style: italic";
            } else if (c == ChatColor.BOLD) {
                replaceStyle = "font-weight: bold";
            } else if (c == ChatColor.UNDERLINE) {
                replaceStyle = "text-decoration: underline";
            } else {
                String color = c.name().toLowerCase().replace("_", "");
                if (c == ChatColor.LIGHT_PURPLE) {
                    color = "mediumpurple";
                }
                replaceStyle = "color:" + color;
            }
            line = line.replace(c.toString(), "<span style=\"" + replaceStyle + "\">");
        }
        for (int i = 0; i < tagCount; i++) {
            line += "</span>";
        }

        return line;
    }

    public String getHTMLDescription() {
        Collection<String> rawLore = getLore();
        Collection<String> lore = new ArrayList<>();
        lore.add("<h2>" + convertToHTML(getActiveWandName()) + "</h2>");
        for (String line : rawLore) {
            lore.add(convertToHTML(line));
        }

        return "<div style=\"background-color: black; margin: 8px; padding: 8px\">" + StringUtils.join(lore, "<br/>") + "</div>";
    }

    private String getPropertyString(String templateName, float value) {
        return getPropertyString(templateName, value, 1, false);
    }

    private String getPropertyString(String templateName, float value, float max, boolean defaultStack) {
        String propertyTemplate = getBoolean("stack", defaultStack) ? "property_stack" : "property_value";
        if (value < 0) {
            propertyTemplate = propertyTemplate + "_negative";
        }
        return controller.getMessages().getPropertyString(getMessageKey(templateName), value, max, getMessageKey(propertyTemplate));
    }

    private String formatPropertyString(String message, float value, float max) {
        String propertyTemplate = getBoolean("stack") ? "property_stack" : "property_value";
        if (value < 0) {
            propertyTemplate = propertyTemplate + "_negative";
        }
        return controller.getMessages().formatPropertyString(message, value, max, getMessage(propertyTemplate));
    }

    private void addDamageTypeLore(String property, String propertyType, double amount, List<String> lore) {
        addDamageTypeLore(property, propertyType, amount, 1, lore);
    }

    private void addDamageTypeLore(String property, String propertyType, double amount, double max, List<String> lore) {
        addDamageTypeLore(property, propertyType, amount, max, lore, null);
    }

    private void addDamageTypeLore(String property, String propertyType, double amount, double max, List<String> lore, String unknownDefault) {
        if (amount != 0) {
            String prefix = getMessageKey("prefixes." + property);
            prefix = controller.getMessages().get(prefix, "");
            String templateKey = getMessageKey(property + "." + propertyType);
            String template;
            if (controller.getMessages().containsKey(templateKey)) {
                template = controller.getMessages().get(templateKey);
            } else {
                templateKey = getMessageKey(property + ".unknown");
                template = controller.getMessages().get(templateKey);
                String pretty = propertyType.substring(0, 1).toUpperCase() + propertyType.substring(1);
                template = template.replace("$type", pretty);
                if (unknownDefault != null && !unknownDefault.isEmpty()) {
                    // This is some special-case hackery, currently only used for enchantments
                    unknownDefault = WordUtils.capitalize(unknownDefault.toLowerCase().replace("_", " "));
                    template = template.replace("$name", unknownDefault);
                }
            }
            template = formatPropertyString(prefix + template, (float)amount, (float)max);
            ConfigurationUtils.addIfNotEmpty(template, lore);
        }
    }

    public String getLevelString(String templateName, float amount)
    {
        return controller.getMessages().getLevelString(getMessageKey(templateName), amount);
    }

    public String getLevelString(String templateName, float amount, float max)
    {
        return controller.getMessages().getLevelString(getMessageKey(templateName), amount, max);
    }

    protected List<String> getCustomLore(Collection<String> loreTemplate) {
        List<String> lore = new ArrayList<>();
        for (String line : loreTemplate) {
            if (line == null || line.isEmpty()) {
                lore.add("");
                continue;
            }
            line = parameterize(line);
            line = CompatibilityLib.getCompatibilityUtils().translateColors(line);
            CompatibilityLib.getInventoryUtils().wrapText(line, lore);
        }
        return lore;
    }

    protected String getAndUpdateDescription() {
        String updatedDescription = description;
        if (description.contains("$") && !description.contains("$path")) {
            String newDescription = controller.getMessages().escape(description);
            if (!newDescription.equals(description)) {
                this.description = newDescription;
                setProperty("description", description);
                updatedDescription = newDescription;
            }
        }
        String descriptionTemplate = controller.getMessages().get(getMessageKey("description_lore"), "");
        if (description.contains("$path") && !descriptionTemplate.isEmpty()) {
            String pathName = getPathName();
            updatedDescription = updatedDescription.replace("$path", pathName == null ? "Unknown" : pathName);
        } else if (description.contains("$")) {
            String randomDescription = getMessage("randomized_lore");
            String randomTemplate = controller.getMessages().get(getMessageKey("randomized_description"), "");
            if (randomDescription.length() > 0 && !randomTemplate.isEmpty()) {
                updatedDescription = randomDescription;
            }
        } else if (!descriptionTemplate.isEmpty()) {
            updatedDescription = descriptionTemplate.replace("$description", description);
        }
        updatedDescription = CompatibilityLib.getCompatibilityUtils().translateColors(updatedDescription);
        return updatedDescription;
    }

    @Nullable
    protected String getPathName() {
        String pathName = null;
        com.elmakers.mine.bukkit.api.wand.WandUpgradePath path = getPath();
        if (path != null) {
            pathName = path.getName();
        } else if (mageClassKeys != null && !mageClassKeys.isEmpty()) {
            for (String classKey : mageClassKeys) {
                MageClassTemplate classTemplate = controller.getMageClassTemplate(classKey);
                if (classTemplate != null) {
                    String pathKey = classTemplate.getProperty("path", "");
                    if (!pathKey.isEmpty()) {
                        path = controller.getPath(pathKey);
                    }
                    if (path != null) {
                        pathName = path.getName();
                    } else {
                        pathName = classTemplate.getName();
                    }
                    break;
                }
            }
        }

        return pathName;
    }

    @Nonnull
    protected String getOwnerDescription() {
        String ownerDescription = "";
        if (owner != null && owner.length() > 0) {
            if (bound) {
                ownerDescription = getMessage("bound_description", "$name").replace("$name", owner);
            } else {
                ownerDescription = getMessage("owner_description", "$name").replace("$name", owner);
            }
        }
        return ownerDescription;
    }

    protected void addOwnerDescription(List<String> lore) {
        ConfigurationUtils.addIfNotEmpty(getOwnerDescription(), lore);
    }

    @SuppressWarnings("unchecked")
    protected List<String> getLore()
    {
        List<String> messagesLore = controller.getMessages().getAll(getMessageKey("lore"));
        if (messagesLore != null) {
            return getCustomLore(messagesLore);
        }
        Object customLore = getProperty("lore");
        if (customLore != null && customLore instanceof Collection) {
            return getCustomLore((Collection<String>)customLore);
        }
        List<String> lore = new ArrayList<>();

        int spellCount = getSpells().size();
        int materialCount = getBrushes().size();

        if (isUpgrade) {
            String slot = getString("slot");
            if (slot != null && !slot.isEmpty()) {
                WandUpgradeSlotTemplate wandSlot = controller.getWandSlotTemplate(slot);
                if (wandSlot == null || !wandSlot.isHidden()) {
                    String slotName = controller.getMessages().get("wand_slots." + slot + ".name", slot);
                    ConfigurationUtils.addIfNotEmpty(getMessage("upgrade_slot").replace("$slot", slotName), lore);
                }
            }
        }

        String pathName = getPathName();
        if (description.length() > 0) {
            if (randomizeOnActivate) {
                String randomDescription = getMessage("randomized_lore");
                String randomTemplate = controller.getMessages().get(getMessageKey("randomized_description"), "");
                if (randomDescription.length() > 0 && !randomTemplate.isEmpty()) {
                    CompatibilityLib.getInventoryUtils().wrapText(randomTemplate.replace("$description", randomDescription), lore);
                    return lore;
                }
            }
            String description = getAndUpdateDescription();
            CompatibilityLib.getInventoryUtils().wrapText(description, lore);
        }
        String pathTemplate = getMessage("path_lore", "");
        if (pathName != null && !pathTemplate.isEmpty()) {
            lore.add(pathTemplate.replace("$path", pathName));
        }

        if (!isUpgrade) {
            addOwnerDescription(lore);
        }

        SpellTemplate spell = mage == null ? controller.getSpellTemplate(getActiveSpellKey()) : mage.getSpell(getActiveSpellKey());
        Messages messages = controller.getMessages();

        // This is here specifically for a wand that only has
        // one spell now, but may get more later. Since you
        // can't open the inventory in this state, you can not
        // otherwise see the spell lore.
        boolean isSingleSpell = spell != null && spellCount == 1 && !hasInventory && !isUpgrade;
        if (showCycleModeLore && getMode() == WandMode.CYCLE && spell != null) {
            isSingleSpell = true;
        }
        if (isSingleSpell)
        {
            addSpellLore(messages, spell, lore, getActiveMage(), this);
        }
        if (materialCount == 1 && activeBrush != null && activeBrush.length() > 0)
        {
            lore.add(getBrushDisplayName(messages, MaterialBrush.parseMaterialKey(activeBrush)));
        }

        if (spellCount > 0) {
            if (isUpgrade) {
                ConfigurationUtils.addIfNotEmpty(getMessage("upgrade_spell_count").replace("$count", Integer.toString(spellCount)), lore);
            } else if (spellCount > 1) {
                ConfigurationUtils.addIfNotEmpty(getMessage("spell_count").replace("$count", Integer.toString(spellCount)), lore);
            }
        }
        if (materialCount > 0) {
            if (isUpgrade) {
                ConfigurationUtils.addIfNotEmpty(getMessage("upgrade_material_count").replace("$count", Integer.toString(materialCount)), lore);
            } else if (materialCount > 1) {
                ConfigurationUtils.addIfNotEmpty(getMessage("material_count").replace("$count", Integer.toString(materialCount)), lore);
            }
        }

        addUseLore(lore);
        addPropertyLore(lore, isSingleSpell);
        if (!isUpgrade && slots != null) {
            boolean printedHeader = false;
            for (WandUpgradeSlot slot : slots) {
                if (slot.isHidden()) {
                    continue;
                }
                if (!printedHeader) {
                    ConfigurationUtils.addIfNotEmpty(getMessage("slots_header").replace("$count", Integer.toString(slots.size())), lore);
                    printedHeader = true;
                }
                Wand slotted = slot.getSlotted();
                if (slotted == null) {
                    String slotName = controller.getMessages().get("wand_slots." + slot.getType() + ".name", slot.getType());
                    ConfigurationUtils.addIfNotEmpty(getMessage("empty_slot").replace("$slot", slotName), lore);
                } else {
                    ConfigurationUtils.addIfNotEmpty(getMessage("slotted").replace("$slotted", slotted.getName()), lore);
                }
            }
        }

        String slotType = getSlot();
        if (slotType != null && !slotType.isEmpty()) {
            WandUpgradeSlotTemplate template = controller.getWandSlotTemplate(slotType);
            String defaultSlotted = template == null ? null : template.getDefaultSlottedKey();
            if (defaultSlotted != null && defaultSlotted.equals(getTemplateKey())) {
                String slotName = controller.getMessages().get("slots." + slotType + ".name", slotType);
                ConfigurationUtils.addIfNotEmpty(getMessage("default_slotted").replace("$slot", slotName), lore);
            }
        }


        if (instructionsLore && spellCount > 1) {
            String header = getMessage("lore_instructions_header", "");
            CompatibilityLib.getInventoryUtils().wrapText(header, lore);
            if (isInventoryOpen() && inventoryOpenLore != null && !inventoryOpenLore.isEmpty()) {
                CompatibilityLib.getInventoryUtils().wrapText(inventoryOpenLore, lore);

                String cycleMessage = getMessage("inventory_open_cycle", "");
                if (!cycleMessage.isEmpty() &&  inventories.size() > 1) {
                    CompatibilityLib.getInventoryUtils().wrapText(cycleMessage, lore);
                }

                cycleMessage = getMessage("inventory_open_cycle_hotbar", "");
                if (!cycleMessage.isEmpty() &&  hotbars.size() > 1) {
                    CompatibilityLib.getInventoryUtils().wrapText(cycleMessage, lore);
                }
            }
            addInstructionLore("left_click", leftClickAction, lore);
            addInstructionLore("right_click", rightClickAction, lore);
            addInstructionLore("drop", dropAction, lore);
            addInstructionLore("swap", swapAction, lore);
            addInstructionLore("no_bowpull", noBowpullAction, lore);
            if (slots != null) {
                for (WandUpgradeSlot slot : slots) {
                    slot.addInstructionLore(lore, controller.getMessages());
                }
            }
        }

        // This should always be last
        // Mainly for extremly hacky reasons, the shop removes this line when displaying upgrades.
        // TODO: Make this less hacky, add some way to disable this lore explicitly when creating a wand icon
        if (isUpgrade) {
            ConfigurationUtils.addIfNotEmpty(getMessage("upgrade_item_description"), lore);
        }

        return lore;
    }

    private void addInstructionLore(String key, WandAction action, List<String> lore) {
        if (action == WandAction.NONE) return;
        String line = getMessage("lore_" + action.name().toLowerCase() + "_instructions", "");
        if (isInventoryOpen()) {
            line = getMessage("inventory_open_" + action.name().toLowerCase() + "_instructions", line);
        }
        if (line.isEmpty()) {
            return;
        }
        String controlKey = controller.getMessages().get("controls." + key);
        if (controlKey.isEmpty()) {
            return;
        }
        line = line.replace("$button", controlKey);
        line = line.replace("$wand", getName());
        Spell spell = getActiveSpell();
        String spellName = spell == null ? "" : spell.getName();
        line = line.replace("$spell", spellName);
        CompatibilityLib.getInventoryUtils().wrapText(line, lore);
    }

    public String getSlot() {
        return getString("slot");
    }

    protected void addUseLore(List<String> lore) {
        String message = getUseLore();
        ConfigurationUtils.addIfNotEmpty(message, lore);
    }

    @Nonnull
    protected String getUseLore() {
        String message = "";
        int remaining = getRemainingUses();
        if (!isSingleUse && remaining > 0) {
            if (isUpgrade) {
                message = (remaining == 1) ? getMessage("upgrade_uses_singular") : getMessage("upgrade_uses");
                message = message.replace("$count", Integer.toString(remaining));
            } else {
                message = (remaining == 1) ? getMessage("uses_remaining_singular") : getMessage("uses_remaining_brief");
                message = message.replace("$count", Integer.toString(remaining));
            }
        }
        return message;
    }


    protected void updateLore() {
        findItem();
        CompatibilityLib.getCompatibilityUtils().setLore(item, getLore());
    }

    public int getRemainingUses() {
        return uses;
    }

    public void makeEnchantable(boolean enchantable) {
        if (EnchantableWandMaterial == null) return;

        if (!enchantable) {
            item.setType(icon.getMaterial());
            CompatibilityLib.getDeprecatedUtils().setItemDamage(item, icon.getData());
        } else {
            MaterialSet enchantableMaterials = controller.getMaterialSetManager().getMaterialSetEmpty("enchantable");
            if (!enchantableMaterials.testItem(item)) {
                item.setType(EnchantableWandMaterial);
                CompatibilityLib.getDeprecatedUtils().setItemDamage(item, (short)0);
            }
        }
        updateName();
    }

    public static boolean hasActiveWand(Player player) {
        if (player == null) return false;
        ItemStack activeItem =  player.getInventory().getItemInMainHand();
        return isWand(activeItem);
    }

    @Nullable
    public static Wand getActiveWand(MagicController controller, Player player) {
        ItemStack activeItem =  player.getInventory().getItemInMainHand();
        if (isWand(activeItem)) {
            return controller.getWand(activeItem);
        }

        return null;
    }

    public static boolean isWand(ItemStack item) {
        return item != null && CompatibilityLib.getNBTUtils().containsTag(item, WAND_KEY);
    }

    public static boolean isWandOrUpgrade(ItemStack item) {
        return isWand(item) || isUpgrade(item);
    }

    public static boolean isSpecial(ItemStack item) {
        return isWand(item) || isUpgrade(item) || isSpell(item) || isBrush(item) || isSP(item) || isCurrency(item);
    }

    public static boolean isSelfDestructWand(ItemStack item) {
        return item != null && WAND_SELF_DESTRUCT_KEY != null && CompatibilityLib.getNBTUtils().containsTag(item, WAND_SELF_DESTRUCT_KEY);
    }

    public static boolean isSP(ItemStack item) {
        return CompatibilityLib.getNBTUtils().containsTag(item, "sp");
    }

    public static boolean isCurrency(ItemStack item) {
        return CompatibilityLib.getNBTUtils().containsTag(item, "currency");
    }

    protected void addPropertyLore(List<String> lore, boolean isSingleSpell)
    {
        if (usesMana() && effectiveManaMax > 0) {
            int manaMax = getManaMax();
            if (effectiveManaMax != manaMax) {
                String fullMessage = getLevelString("mana_amount_boosted", manaMax, controller.getMaxMana());
                ConfigurationUtils.addIfNotEmpty(fullMessage.replace("$mana", Integer.toString((int)Math.ceil(effectiveManaMax))), lore);
            } else {
                ConfigurationUtils.addIfNotEmpty(getLevelString("mana_amount", manaMax, controller.getMaxMana()), lore);
            }
            int manaRegeneration = getManaRegeneration();
            if (manaRegeneration > 0 && effectiveManaRegeneration > 0) {
                if (effectiveManaRegeneration != manaRegeneration) {
                    String fullMessage = getLevelString("mana_regeneration_boosted", manaRegeneration, controller.getMaxManaRegeneration());
                    ConfigurationUtils.addIfNotEmpty(fullMessage.replace("$mana", Integer.toString((int)Math.ceil(effectiveManaRegeneration))), lore);
                } else {
                    ConfigurationUtils.addIfNotEmpty(getLevelString("mana_regeneration", manaRegeneration, controller.getMaxManaRegeneration()), lore);
                }
            }
            if (manaPerDamage > 0) {
                ConfigurationUtils.addIfNotEmpty(getLevelString("mana_per_damage", manaPerDamage, controller.getMaxManaRegeneration()), lore);
            }
        }
        if (blockReflectChance > 0) {
            ConfigurationUtils.addIfNotEmpty(getLevelString("reflect_chance", blockReflectChance), lore);
        } else if (blockChance != 0) {
            ConfigurationUtils.addIfNotEmpty(getLevelString("block_chance", blockChance), lore);
        }
        float manaMaxBoost = getManaMaxBoost();
        if (manaMaxBoost != 0) {
            ConfigurationUtils.addIfNotEmpty(getPropertyString("mana_boost", manaMaxBoost, 1, true), lore);
        }
        float manaRegenerationBoost = getManaRegenerationBoost();
        if (manaRegenerationBoost != 0) {
            ConfigurationUtils.addIfNotEmpty(getPropertyString("mana_regeneration_boost", manaRegenerationBoost, 1, true), lore);
        }

        if (earnMultiplier > 1) {
            String earnDescription = getPropertyString("earn_multiplier", earnMultiplier - 1);
            String earnType = getController().getMessages().get("currency.sp.name_short", "SP");
            earnDescription = earnDescription.replace("$type", earnType);
            ConfigurationUtils.addIfNotEmpty(earnDescription, lore);
        }

        if (castSpell != null) {
            SpellTemplate spell = controller.getSpellTemplate(castSpell);
            if (spell != null)
            {
                ConfigurationUtils.addIfNotEmpty(getMessage("spell_aura").replace("$spell", spell.getName()), lore);
            }
        }
        for (Map.Entry<PotionEffectType, Integer> effect : getPotionEffects().entrySet()) {
            ConfigurationUtils.addIfNotEmpty(describePotionEffect(effect.getKey(), effect.getValue()), lore);
        }

        // If this is a passive wand, then reduction properties stack onto the mage when worn.
        // In this case we should show it as such in the lore.
        if (worn) isSingleSpell = false;

        if (consumeReduction != 0 && !isSingleSpell) ConfigurationUtils.addIfNotEmpty(getPropertyString("consume_reduction", consumeReduction), lore);

        if (costReduction != 0 && !isSingleSpell) ConfigurationUtils.addIfNotEmpty(getPropertyString("cost_reduction", costReduction), lore);
        if (cooldownReduction != 0 && !isSingleSpell) ConfigurationUtils.addIfNotEmpty(getPropertyString("cooldown_reduction", cooldownReduction), lore);
        if (power > 0) ConfigurationUtils.addIfNotEmpty(getLevelString("power", power), lore);
        if (superProtected) {
            ConfigurationUtils.addIfNotEmpty(getMessage("super_protected"), lore);
        } else if (protection != null) {
            for (Map.Entry<String, Double> entry : protection.entrySet()) {
                String protectionType = entry.getKey();
                double amount = entry.getValue();
                addDamageTypeLore("protection", protectionType, amount, lore);
            }
        }
        if (superPowered) {
            ConfigurationUtils.addIfNotEmpty(getMessage("super_powered"), lore);
        }

        if (isEnchantable()) {
            int hideFlags = getProperty("hide_flags", HIDE_FLAGS);
            ConfigurationSection enchantments = getConfigurationSection("enchantments");
            if ((hideFlags & 1) == 1 && enchantments != null) {
                Set<String> enchantmentKeys = enchantments.getKeys(false);
                for (String enchantmentKey : enchantmentKeys) {
                    int level = enchantments.getInt(enchantmentKey);
                    String[] pieces = StringUtils.split(enchantmentKey, ":");
                    enchantmentKey = pieces[pieces.length - 1];
                    addDamageTypeLore("enchantment", enchantmentKey, level, 0, lore, enchantmentKey);
                }
            }
        }

        ConfigurationSection weaknessConfig = getConfigurationSection("weakness");
        if (weaknessConfig != null) {
            Set<String> keys = weaknessConfig.getKeys(false);
            for (String key : keys) {
                addDamageTypeLore("weakness", key, weaknessConfig.getDouble(key), lore);
            }
        }

        ConfigurationSection strengthConfig = getConfigurationSection("strength");
        if (strengthConfig != null) {
            Set<String> keys = strengthConfig.getKeys(false);
            for (String key : keys) {
                addDamageTypeLore("strength", key, strengthConfig.getDouble(key), lore);
            }
        }
        ConfigurationSection attributes = getConfigurationSection("attributes");
        if (attributes != null) {
            // Don't bother with the lore at all if the template has been blanked out
            String template = getMessage("attributes");
            if (!template.isEmpty()) {
                Set<String> keys = attributes.getKeys(false);
                for (String key : keys) {
                    String label = controller.getMessages().get("attributes." + key + ".name", key);

                    // We are only display attributes as integers for now
                    int value = attributes.getInt(key);
                    if (value == 0) continue;

                    float max = 1;
                    MagicAttribute attribute = controller.getAttribute(key);
                    if (attribute != null) {
                        Double maxValue = attribute.getMax();
                        if (maxValue != null) {
                            max = (float)(double)maxValue;
                        }
                    }

                    label = getPropertyString("attributes", value, max, true).replace("$attribute", label);
                    lore.add(label);
                }
            }
        }
    }

    public static boolean isSpell(ItemStack item) {
        return item != null && CompatibilityLib.getNBTUtils().containsTag(item, "spell");
    }

    public static boolean isSkill(ItemStack item) {
        return item != null && CompatibilityLib.getNBTUtils().containsTag(item, "skill");
    }

    public static boolean isBrush(ItemStack item) {
        return item != null && CompatibilityLib.getNBTUtils().containsTag(item, "brush");
    }

    @Nullable
    protected static Object getWandOrUpgradeNode(ItemStack item) {
        if (CompatibilityLib.getItemUtils().isEmpty(item)) return null;
        Object wandNode = CompatibilityLib.getNBTUtils().getTag(item, WAND_KEY);
        if (wandNode == null) {
            wandNode = CompatibilityLib.getNBTUtils().getTag(item, UPGRADE_KEY);
        }
        return wandNode;
    }

    @Nullable
    public static String getWandTemplate(ItemStack item) {
        Object wandNode = getWandOrUpgradeNode(item);
        if (wandNode == null) return null;
        return CompatibilityLib.getNBTUtils().getString(wandNode, "template");
    }

    @Nullable
    public static String getWandId(ItemStack item) {
        if (CompatibilityLib.getItemUtils().isEmpty(item)) return null;
        Object wandNode = CompatibilityLib.getNBTUtils().getTag(item, WAND_KEY);
        if (wandNode == null) return null;
        return CompatibilityLib.getNBTUtils().getString(wandNode, "id");
    }

    @Nullable
    public static String getArrowSpell(ItemStack item) {
        if (CompatibilityLib.getItemUtils().isEmpty(item)) return null;
        Object arrowNode = CompatibilityLib.getNBTUtils().getTag(item, "arrow");
        if (arrowNode == null) return null;
        Object spellNode = CompatibilityLib.getNBTUtils().getTag(arrowNode, "spell");
        if (spellNode == null) return null;
        return CompatibilityLib.getNBTUtils().getString(spellNode, "key");
    }

    @Nullable
    public static String getArrowSpellClass(ItemStack item) {
        if (CompatibilityLib.getItemUtils().isEmpty(item)) return null;
        Object arrowNode = CompatibilityLib.getNBTUtils().getTag(item, "arrow");
        if (arrowNode == null) return null;
        Object spellNode = CompatibilityLib.getNBTUtils().getTag(arrowNode, "spell");
        if (spellNode == null) return null;
        return CompatibilityLib.getNBTUtils().getString(spellNode, "class");
    }

    @Nullable
    public static String getSpellBaseKey(ItemStack item) {
        String spellKey = getSpell(item);
        if (spellKey != null) {
            SpellKey key = new SpellKey(spellKey);
            spellKey = key.getBaseKey();
        }
        return spellKey;
    }

    @Nullable
    public static String getSpell(ItemStack item) {
        if (CompatibilityLib.getItemUtils().isEmpty(item)) return null;
        Object spellNode = CompatibilityLib.getNBTUtils().getTag(item, "spell");
        if (spellNode == null) return null;
        return CompatibilityLib.getNBTUtils().getString(spellNode, "key");
    }

    @Nullable
    @Override
    public Spell getSpell(String spellKey, com.elmakers.mine.bukkit.api.magic.Mage mage) {
        if (mage == null || spellKey == null) {
            return null;
        }
        if (!hasSpell(spellKey)) return null;
        SpellKey key = new SpellKey(spellKey);
        spellKey = key.getBaseKey();
        Integer level = spellLevels.get(spellKey);
        if (level != null) {
            spellKey = new SpellKey(spellKey, level).getKey();
        }
        return mage.getSpell(spellKey);
    }

    @Nullable
    @Override
    public Spell getSpell(String spellKey) {
        return getSpell(spellKey, mage);
    }

    @Nullable
    public static String getSpellClass(ItemStack item) {
        if (CompatibilityLib.getItemUtils().isEmpty(item)) return null;
        Object spellNode = CompatibilityLib.getNBTUtils().getTag(item, "spell");
        if (spellNode == null) return null;
        return CompatibilityLib.getNBTUtils().getString(spellNode, "class");
    }

    public static boolean isQuickCastSkill(ItemStack item) {
        if (CompatibilityLib.getItemUtils().isEmpty(item)) return false;
        Object spellNode = CompatibilityLib.getNBTUtils().getTag(item, "spell");
        if (spellNode == null) return false;
        Boolean quickCast = CompatibilityLib.getNBTUtils().contains(spellNode, "quick_cast") ? CompatibilityLib.getNBTUtils().getOptionalBoolean(spellNode, "quick_cast") : null;
        return quickCast == null ? true : quickCast;
    }

    @Nullable
    public static String getSpellArgs(ItemStack item) {
        if (CompatibilityLib.getItemUtils().isEmpty(item)) return null;
        Object spellNode = CompatibilityLib.getNBTUtils().getTag(item, "spell");
        if (spellNode == null) return null;
        return CompatibilityLib.getNBTUtils().getString(spellNode, "args");
    }

    @Nullable
    public static String getBrush(ItemStack item) {
        if (CompatibilityLib.getItemUtils().isEmpty(item)) return null;
        Object brushNode = CompatibilityLib.getNBTUtils().getTag(item, "brush");
        if (brushNode == null) return null;
        return CompatibilityLib.getNBTUtils().getString(brushNode, "key");
    }

    protected void updateInventoryName(ItemStack item, boolean activeName) {
        if (isSpell(item)) {
            Spell spell = mage.getSpell(getSpell(item));
            if (spell != null) {
                updateSpellName(controller.getMessages(), item, spell, activeName ? this : null, activeBrush);
            }
        } else if (isBrush(item)) {
            updateBrushName(controller.getMessages(), item, getBrush(item), activeName ? this : null);
        }
    }

    private boolean updateIfMatch(ItemStack itemStack, Spell spell) {
        String spellKey = getSpell(itemStack);
        if (spellKey != null && spellKey.equals(spell.getKey())) {
            updateSpellItem(controller.getMessages(), itemStack, spell, "", this, activeBrush, false);
            return true;
        }
        return false;
    }

    public void updateSpellItem(Spell spell) {
        Player player = mage == null ? null : mage.getPlayer();
        if (player != null) {
            for (ItemStack itemStack : player.getInventory()) {
                if (updateIfMatch(itemStack, spell)) {
                    break;
                }
            }
        }
        if (inventories.size() > openInventoryPage) {
            WandInventory inventory = inventories.get(openInventoryPage);
            for (ItemStack itemStack : inventory.getContents()) {
                if (updateIfMatch(itemStack, spell)) {
                    return;
                }
            }
        }
        if (hotbars.size() > currentHotbar) {
            WandInventory inventory = hotbars.get(currentHotbar);
            for (ItemStack itemStack : inventory.getContents()) {
                if (updateIfMatch(itemStack, spell)) {
                    return;
                }
            }
        }
    }

    public static void updateSpellItem(Messages messages, ItemStack itemStack, SpellTemplate spell, String args, Wand wand, String activeMaterial, boolean isItem) {
        updateSpellItem(messages, itemStack, spell, args, wand == null ? null : wand.getActiveMage(), wand, activeMaterial, isItem);
    }

    public static void updateSpellItem(Messages messages, ItemStack itemStack, SpellTemplate spell, String args, com.elmakers.mine.bukkit.api.magic.Mage mage, Wand wand, String activeMaterial, boolean isItem) {
        // Just act like there's no wand if this is going to be an item
        if (isItem) {
            wand = null;
        }

        updateSpellName(messages, itemStack, spell, wand, activeMaterial);
        List<String> lore = new ArrayList<>();
        addSpellLore(messages, spell, lore, mage, wand);
        if (isItem) {
            ConfigurationUtils.addIfNotEmpty(messages.get("wand.spell_item_description"), lore);
        }
        CompatibilityLib.getCompatibilityUtils().setLore(itemStack, lore);
        Object spellNode = CompatibilityLib.getNBTUtils().createTag(itemStack, "spell");
        CompatibilityLib.getNBTUtils().setString(spellNode, "key", spell.getKey());
        CompatibilityLib.getNBTUtils().setString(spellNode, "args", args);
        if (SpellGlow || (wand != null && wand.spellGlow)) {
            CompatibilityLib.getItemUtils().addGlow(itemStack);
        }
    }

    public static void updateSpellName(Messages messages, ItemStack itemStack, SpellTemplate spell, Wand wand, String activeMaterial) {
        String displayName;
        if (wand != null && !wand.isQuickCast()) {
            displayName = wand.getActiveWandName(spell);
        } else {
            displayName = getSpellDisplayName(messages, spell, MaterialBrush.parseMaterialKey(activeMaterial));
        }
        CompatibilityLib.getCompatibilityUtils().setDisplayName(itemStack, displayName);
    }

    public static void updateBrushName(Messages messages, ItemStack itemStack, String materialKey, Wand wand) {
        updateBrushName(messages, itemStack, MaterialBrush.parseMaterialKey(materialKey), wand);
    }

    public static void updateBrushName(Messages messages, ItemStack itemStack, MaterialBrush brush, Wand wand) {
        String displayName;
        if (wand != null) {
            Spell activeSpell = wand.getActiveSpell();
            if (activeSpell != null && activeSpell.usesBrush()) {
                displayName = wand.getActiveWandName(brush);
            } else {
                displayName = ChatColor.RED + brush.getName(messages);
            }
        } else {
            displayName = brush.getName(messages);
        }
        CompatibilityLib.getCompatibilityUtils().setDisplayName(itemStack, displayName);
    }

    public static void updateBrushItem(Messages messages, ItemStack itemStack, String materialKey, Wand wand) {
        updateBrushItem(messages, itemStack, MaterialBrush.parseMaterialKey(materialKey), wand, true);
    }

    public static void updateBrushItem(Messages messages, ItemStack itemStack, MaterialBrush brush, Wand wand, boolean useWandName) {
        if (useWandName) {
            updateBrushName(messages, itemStack, brush, wand);
        }
        Object brushNode = CompatibilityLib.getNBTUtils().createTag(itemStack, "brush");
        CompatibilityLib.getNBTUtils().setString(brushNode, "key", brush.getKey());
    }

    public void updateHotbar() {
        if (mage == null) return;
        if (!isInventoryOpen()) return;
        Player player = mage.getPlayer();
        if (player == null) return;
        if (!hasStoredInventory()) return;

        WandMode wandMode = getMode();
        if (wandMode == WandMode.INVENTORY) {
            PlayerInventory inventory = player.getInventory();
            updateHotbar(inventory);
            CompatibilityLib.getDeprecatedUtils().updateInventory(player);
        }
    }

    private boolean updateHotbar(PlayerInventory playerInventory) {
        if (getMode() != WandMode.INVENTORY) return false;
        WandInventory hotbar = getHotbar();
        if (hotbar == null) return false;

        // Make sure the wand is still in the held slot
        ItemStack currentItem = playerInventory.getItem(heldSlot);
        if (currentItem == null || !currentItem.getItemMeta().equals(item.getItemMeta())) {
            if (mage != null) {
                mage.sendDebugMessage("Trying to update hotbar but the wand has gone missing");
            }
            return false;
        }

        // Set hotbar items from remaining list
        int targetOffset = 0;
        for (int hotbarSlot = 0; hotbarSlot < HOTBAR_INVENTORY_SIZE; hotbarSlot++)
        {
            if (hotbarSlot == heldSlot)
            {
                targetOffset = 1;
            }

            ItemStack hotbarItem = hotbar.getItem(hotbarSlot);
            updateInventoryName(hotbarItem, true);
            playerInventory.setItem(hotbarSlot + targetOffset, hotbarItem);
        }
        return true;
    }

    private void updateInventory() {
        if (mage == null) return;
        if (!isInventoryOpen()) return;
        Player player = mage.getPlayer();
        if (player == null) return;

        WandMode wandMode = getMode();
        if (wandMode == WandMode.INVENTORY) {
            if (!hasStoredInventory()) return;
            PlayerInventory inventory = player.getInventory();
            if (!updateHotbar(inventory)) {
                for (int i = 0; i < HOTBAR_SIZE; i++) {
                    if (i != inventory.getHeldItemSlot()) {
                        inventory.setItem(i, null);
                    }
                }
            }
            updateInventory(inventory);
            updateName();
        } else if (wandMode == WandMode.CHEST || wandMode == WandMode.SKILLS) {
            Inventory inventory = getDisplayInventory();
            inventory.clear();
            updateInventory(inventory);
        }
    }

    private void updateInventory(Inventory targetInventory) {
        // Set inventory from current page, taking into account hotbar offset
        int currentOffset = getHotbarSize() > 0 ? HOTBAR_SIZE : 0;
        List<WandInventory> inventories = this.inventories;
        if (openInventoryPage < inventories.size()) {
            WandInventory inventory = inventories.get(openInventoryPage);
            ItemStack[] contents = inventory.getContents();
            for (int i = 0; i < contents.length; i++) {
                if (currentOffset >= PLAYER_INVENTORY_SIZE && getMode() == WandMode.INVENTORY) break;
                ItemStack inventoryItem = contents[i];
                updateInventoryName(inventoryItem, false);
                targetInventory.setItem(currentOffset, inventoryItem);
                currentOffset++;
            }
        }
        for (;currentOffset < targetInventory.getSize() && currentOffset < PLAYER_INVENTORY_SIZE; currentOffset++) {
            targetInventory.setItem(currentOffset,  null);
        }
    }

    protected static void addSpellLore(Messages messages, SpellTemplate spell, List<String> lore, com.elmakers.mine.bukkit.api.magic.Mage mage, Wand wand) {
        if (wand != null && spell instanceof BaseSpell) {
            BaseSpell baseSpell = (BaseSpell)spell;
            Map<String, String> overrides = wand.getOverrides();
            ConfigurationSection parameters = baseSpell.getWorkingParameters();
            if (parameters != null && overrides != null && !overrides.isEmpty()) {
                parameters = ConfigurationUtils.cloneConfiguration(parameters);
                for (Map.Entry<String, String> entry : overrides.entrySet()) {
                    parameters.set(entry.getKey(), entry.getValue());
                }
                baseSpell.processTemplateParameters(parameters);
            }
        }
        spell.addLore(messages, mage, wand, lore);
    }

    private String getInventoryTitle() {
        return getMessage("chest_inventory_title", "$wand").replace("$wand", getName());
    }

    protected WandInventory getOpenInventory() {
        while (openInventoryPage >= inventories.size()) {
            inventories.add(new WandInventory(getInventorySize()));
        }
        return inventories.get(openInventoryPage);
    }

    protected Inventory getDisplayInventory() {
        if (displayInventory == null || displayInventory.getSize() != getInventorySize()) {
            displayInventory = CompatibilityLib.getCompatibilityUtils().createInventory(null, getInventorySize(), getInventoryTitle());
        }

        return displayInventory;
    }

    public void saveChestInventory() {
        if (displayInventory == null) return;

        WandInventory openInventory = getOpenInventory();
        Map<String, Integer> previousSlots = new HashMap<>();
        Set<String> addedBack = new HashSet<>();
        for (int i = 0; i < displayInventory.getSize(); i++) {
            ItemStack playerItem = displayInventory.getItem(i);
            String itemSpellKey = getSpell(playerItem);
            if (!updateSlot(i + openInventoryPage * getInventorySize(), playerItem)) {
                playerItem = new ItemStack(Material.AIR);
                displayInventory.setItem(i, playerItem);
            } else if (itemSpellKey != null) {
                addedBack.add(itemSpellKey);
            }

            // We don't want to clear items that were taken out, so save them to check later
            ItemStack current = openInventory.getItem(i);
            String spellKey = getSpell(current);
            if (spellKey != null) {
                previousSlots.put(spellKey, i);
            }
            openInventory.setItem(i, playerItem);
        }

        // Put back any items that were taken out
        for (Map.Entry<String, Integer> entry : previousSlots.entrySet()) {
            if (!addedBack.contains(entry.getKey())) {
                ItemStack current = openInventory.getItem(entry.getValue());
                ItemStack itemStack = createSpellItem(entry.getKey(), "", false);
                if (current == null || current.getType() == Material.AIR) {
                    openInventory.setItem(entry.getValue(), itemStack);
                } else {
                    openInventory.addItem(itemStack);
                }
            }
        }
    }

    public void saveInventory() {
        if (mage == null) return;
        if (getMode() == WandMode.SKILLS) {
            saveChestInventory();
            return;
        }
        if (!isInventoryOpen()) return;
        if (mage.getPlayer() == null) return;
        if (getMode() != WandMode.INVENTORY) return;
        if (!hasStoredInventory()) return;

        // Work-around glitches that happen if you're dragging an item on death
        if (mage.isDead()) return;

        // Fill in the hotbar
        Player player = mage.getPlayer();
        PlayerInventory playerInventory = player.getInventory();
        WandInventory hotbar = getHotbar();
        if (hotbar != null)
        {
            int saveOffset = 0;
            for (int i = 0; i < HOTBAR_SIZE; i++) {
                ItemStack playerItem = playerInventory.getItem(i);
                if (isWand(playerItem)) {
                    saveOffset = -1;
                    continue;
                }
                int hotbarOffset = i + saveOffset;
                if (hotbarOffset >= hotbar.getSize()) {
                    // This can happen if there is somehow no wand in the wand inventory.
                    break;
                }
                if (!updateSlot(i + saveOffset + currentHotbar * HOTBAR_INVENTORY_SIZE, playerItem)) {
                    playerItem = new ItemStack(Material.AIR);
                    playerInventory.setItem(i, playerItem);
                }
                hotbar.setItem(i + saveOffset, playerItem);
            }
        }

        // Fill in the active inventory page
        int hotbarOffset = getHotbarSize();
        WandInventory openInventory = getOpenInventory();
        for (int i = 0; i < openInventory.getSize(); i++) {
            ItemStack playerItem = playerInventory.getItem(i + HOTBAR_SIZE);
            if (!updateSlot(i + hotbarOffset + openInventoryPage * getInventorySize(), playerItem)) {
                playerItem = new ItemStack(Material.AIR);
                playerInventory.setItem(i + HOTBAR_SIZE, playerItem);
            }
            openInventory.setItem(i, playerItem);
        }
    }

    protected boolean updateSlot(int slot, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return true;
        String spellKey = getSpell(item);
        if (spellKey != null) {
            SpellKey key = new SpellKey(spellKey);
            spellInventory.put(key.getBaseKey(), slot);
        } else {
            String brushKey = getBrush(item);
            if (brushKey != null) {
                brushInventory.put(brushKey, slot);
            } else if (mage != null) {
                // Must have been an item inserted directly into player's inventory?
                // Make sure it's not the wand item, that can happen due to glitchiness with the pick block
                // button.
                mage.sendDebugMessage(ChatColor.RED + "  updating slot with a non-brush/spell item: " + item.getType(), 30);
                if (!item.equals(this.item)) {
                    mage.giveItem(item);
                    mage.sendDebugMessage(ChatColor.RED + "    Giving to mage: " + item.getType(), 100);
                }
                return false;
            }
        }

        return true;
    }

    @Override
    public int enchant(int totalLevels, com.elmakers.mine.bukkit.api.magic.Mage mage, boolean addSpells) {
        return randomize(totalLevels, mage, addSpells);
    }

    @Override
    public int enchant(int totalLevels, com.elmakers.mine.bukkit.api.magic.Mage mage) {
        return randomize(totalLevels, mage, true);
    }

    @Override
    public int enchant(int totalLevels) {
        return randomize(totalLevels, null, true);
    }

    protected int randomize(int totalLevels, com.elmakers.mine.bukkit.api.magic.Mage enchanter, boolean addSpells) {
        Mage activeMage = this.mage;
        if (enchanter instanceof Mage && this.mage == null) {
            this.mage = (Mage)enchanter;
        }
        int levels = randomize(totalLevels, addSpells);
        this.mage = activeMage;
        return levels;
    }

    @Override
    public int randomize(int totalLevels, boolean addSpells) {
        if (maxEnchantCount > 0 && enchantCount >= maxEnchantCount) {
            if (mage != null && addSpells) {
                mage.sendMessage(getMessage("max_enchanted").replace("$wand", getName()));
            }
            return 0;
        }

        int levels = super.randomize(totalLevels, addSpells);

        if (levels > 0) {
            enchantCount++;
            setProperty("enchant_count", enchantCount);
        }

        saveState();
        updateName();
        updateLore();
        return levels;
    }

    protected void randomize() {
        if (template != null && template.length() > 0) {
            WandTemplate wandConfig = controller.getWandTemplate(template);
            boolean legacyIcons = controller.isLegacyIconsEnabled();
            if (wandConfig != null && wandConfig.hasIcon(legacyIcons)) {
                String iconKey = wandConfig.getIcon(legacyIcons);
                if (iconKey.contains(",")) {
                    Random r = new Random();
                    String[] keys = StringUtils.split(iconKey, ',');
                    iconKey = keys[r.nextInt(keys.length)];
                }
                setIcon(ConfigurationUtils.toMaterialAndData(iconKey));
                updateIcon();
                playEffects("randomize");
            }
        }
    }

    @Nullable
    public static Wand createWand(MagicController controller, String templateName) {
        return createWand(controller, templateName, null);
    }

    @Nullable
    public static Wand createWand(MagicController controller, String templateName, Mage mage) {
        if (controller == null) return null;

        Wand wand = null;
        try {
            wand = new Wand(controller, templateName, mage);
        } catch (UnknownWandException ignore) {
            // the Wand constructor throws an exception on an unknown template
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return wand;
    }

    @Nonnull
    public static Wand createWand(@Nonnull MagicController controller, @Nonnull ItemStack itemStack) {
        checkNotNull(controller);
        checkNotNull(itemStack);
        Wand wand = null;
        try {
            wand = controller.getWand(CompatibilityLib.getItemUtils().makeReal(itemStack.clone()));
            wand.saveState();
            wand.updateName();
        } catch (Exception ex) {
            ex.printStackTrace();
            wand = new Wand(controller);
        }
        return wand;
    }

    @Override
    public boolean add(com.elmakers.mine.bukkit.api.wand.Wand other) {
        if (other instanceof Wand) {
            return add((Wand)other);
        }

        return false;
    }

    @Override
    @Deprecated
    public boolean add(com.elmakers.mine.bukkit.api.wand.Wand other, com.elmakers.mine.bukkit.api.magic.Mage mage) {
        if (other instanceof Wand) {
            return add((Wand)other);
        }

        return false;
    }

    public boolean add(Wand other) {
        if (!isModifiable()) {
            // Only allow upgrading a modifiable wand via an upgrade item
            // and only if the paths match.
            if (!other.isUpgrade() || other.path == null || path == null || other.path.isEmpty() || path.isEmpty() || !other.path.equals(path)) {
                return false;
            }
        }

        // Can't combine limited-use wands
        if (hasUses || other.hasUses)
        {
            return false;
        }
        if (isHeroes || other.isHeroes) {
            return false;
        }

        ConfigurationSection templateConfig = controller.getWandTemplateConfiguration(other.getTemplateKey());

        // Check for forced upgrades
        if (other.isForcedUpgrade()) {
            if (templateConfig == null) {
                return false;
            }
            templateConfig = ConfigurationUtils.cloneConfiguration(templateConfig);
            templateConfig.set("name", templateConfig.getString("upgrade_name"));
            templateConfig.set("description", templateConfig.getString("upgrade_description"));
            templateConfig.set("force", null);
            templateConfig.set("upgrade", null);
            templateConfig.set("legacy_icon", templateConfig.getString("legacy_upgrade_icon"));
            templateConfig.set("icon", templateConfig.getString("upgrade_icon"));
            templateConfig.set("indestructible", null);
            templateConfig.set("upgrade_icon", null);

            configure(templateConfig);
            return true;
        }

        // Don't allow upgrades from an item on a different path
        if (other.isUpgrade() && other.path != null && !other.path.isEmpty() && (this.path == null || !this.path.equals(other.path))) {
            return false;
        }

        ConfigurationSection upgradeConfig = ConfigurationUtils.cloneConfiguration(other.getEffectiveConfiguration());
        String upgradeIcon = ConfigurationUtils.getIcon(upgradeConfig, controller.isLegacyIconsEnabled(), "upgrade_icon");
        cleanUpgradeConfig(upgradeConfig);
        upgradeConfig.set("icon", upgradeIcon);
        upgradeConfig.set("template", other.upgradeTemplate);

        Messages messages = controller.getMessages();
        if (other.rename && templateConfig != null) {
            String newName = messages.get("wands." + other.template + ".name");
            newName = templateConfig.getString("name", newName);
            upgradeConfig.set("name", newName);
        }

        if (other.renameDescription && templateConfig != null) {
            String newDescription = messages.get("wands." + other.template + ".description");
            newDescription = templateConfig.getString("description", newDescription);
            upgradeConfig.set("description", newDescription);
        }
        return upgrade(upgradeConfig);
    }

    public boolean isForcedUpgrade()
    {
        return isUpgrade && forceUpgrade;
    }

    public boolean keepOnDeath() {
        return keep;
    }

    public static WandMode parseWandMode(String modeString, WandMode defaultValue) {
        if (modeString != null && !modeString.isEmpty()) {
            try {
                defaultValue = WandMode.valueOf(modeString.toUpperCase());
            } catch (Exception ignored) {
            }
        }

        return defaultValue;
    }

    public static WandAction parseWandAction(String actionString, WandAction defaultValue) {
        if (actionString != null && !actionString.isEmpty()) {
            try {
                defaultValue = WandAction.valueOf(actionString.toUpperCase());
            } catch (Exception ignored) {
            }
        }

        return defaultValue;
    }

    public WandUseMode parseUseMode(String useString, WandUseMode defaultValue) {
        if (useString != null && !useString.isEmpty()) {
            try {
                return WandUseMode.valueOf(useString.toUpperCase());
            } catch (Exception ex) {
                controller.getLogger().warning("Invalid use mode: " + useString);
            }
        }

        return defaultValue;
    }

    private void updateActiveMaterial() {
        if (mage == null) return;

        if (activeBrush == null) {
            mage.clearBuildingMaterial();
        } else {
            com.elmakers.mine.bukkit.api.block.MaterialBrush brush = mage.getBrush();
            brush.update(activeBrush);
        }
    }

    private int getInventoryItemCount() {
        int itemCount = 0;
        for (WandInventory inventory : inventories) {
            itemCount += inventory.getSize();
        }
        return itemCount;
    }

    private ItemStack getInventoryItem(int index) {
        if (inventories.isEmpty()) return null;
        int inventoryIndex = 0;
        WandInventory inventory = inventories.get(inventoryIndex);
        while (index >= inventory.getSize()) {
            index -= inventory.getSize();
            inventoryIndex++;
            if (inventoryIndex >= inventories.size()) return null;
            inventory = inventories.get(inventoryIndex);
        }
        return inventory.getItem(index);
    }

    public boolean cycleSpells(int direction) {
        if (inventories.isEmpty()) return false;
        int itemCount = getInventoryItemCount();
        int spellIndex = 0;
        for (int i = 0; i < itemCount; i++) {
            ItemStack item = getInventoryItem(i);
            String spellKey = getSpellBaseKey(item);
            if (spellKey.equals(activeSpell)) {
                spellIndex = i;
                break;
            }
        }

        int tryIndex = (spellIndex + direction + itemCount) % itemCount;
        // Try all slots, including the one we were already on in case we're stuck with one spell in the hotbar
        for (int offset = 1; offset <= itemCount; offset++) {
            ItemStack item = getInventoryItem(tryIndex);
            if (activateIcon(item)) {
                currentHotbar = tryIndex / CHEST_ITEMS_PER_ROW;
                updateActionBar();
                playPassiveEffects("cycle_spell");
                if (showCycleModeLore) {
                    updateLore();
                }
                return true;
            }
            tryIndex = (tryIndex + direction + itemCount) % itemCount;
        }
        return false;
    }

    public void cycleBrushes(int direction) {
        Set<String> materialsSet = getBrushes();
        ArrayList<String> materials = new ArrayList<>(materialsSet);
        if (materials.size() == 0) return;
        if (activeBrush == null) {
            setActiveBrush(StringUtils.split(materials.get(0), '@')[0]);
            return;
        }

        int materialIndex = 0;
        for (int i = 0; i < materials.size(); i++) {
            if (StringUtils.split(materials.get(i),'@')[0].equals(activeBrush)) {
                materialIndex = i;
                break;
            }
        }

        materialIndex = (materialIndex + direction) % materials.size();
        setActiveBrush(StringUtils.split(materials.get(materialIndex), '@')[0]);
        playPassiveEffects("cycle_spell");
    }

    public boolean cycleActiveHotbar(int direction) {
        WandInventory hotbar = getActiveHotbar();
        if (hotbar == null || mage == null) return false;

        int spellIndex = 0;
        if (activeSpell != null) {
            for (int i = 0; i < hotbar.getSize(); i++) {
                ItemStack hotbarItem = hotbar.getItem(i);
                String hotbarSpellKey = getSpellBaseKey(hotbarItem);
                if (hotbarSpellKey != null && hotbarSpellKey.equals(activeSpell)) {
                    spellIndex = i;
                    break;
                }
            }
        }

        int tryIndex = (spellIndex + direction + hotbar.getSize()) % hotbar.getSize();
        // Try all slots, including the one we were already on in case we're stuck with one spell in the hotbar
        for (int offset = 1; offset <= hotbar.getSize(); offset++) {
            ItemStack hotbarItem = hotbar.getItem(tryIndex);
            if (activateIcon(hotbarItem)) {
                updateActionBar();
                playPassiveEffects("cycle_spell");
                Player player = mage.getPlayer();
                if (isInventoryOpen() && player != null) {
                    // Make the selected spell item bounce
                    int hotbarIndex = tryIndex >= heldSlot ? tryIndex + 1 : tryIndex;
                    ItemStack itemStack = player.getInventory().getItem(hotbarIndex);
                    if (!CompatibilityLib.getItemUtils().isEmpty(itemStack)) {
                        player.getInventory().setItem(hotbarIndex, null);
                        Plugin plugin = controller.getPlugin();
                        plugin.getServer().getScheduler().runTaskLater(plugin, new RestoreSpellIconTask(player, hotbarIndex, itemStack, this), 1);
                    }
                }
                return true;
            }
            tryIndex = (tryIndex + direction + hotbar.getSize()) % hotbar.getSize();
        }
        return false;
    }

    public void toggleInventory() {
        if (mage != null && mage.cancelSelection()) {
            mage.playSoundEffect(noActionSound);
            return;
        }
        Player player = mage == null ? null : mage.getPlayer();
        boolean isSneaking = player != null && player.isSneaking();
        Spell currentSpell = getActiveSpell();
        if (getBrushMode() == WandMode.CHEST && brushSelectSpell != null && !brushSelectSpell.isEmpty() && isSneaking && currentSpell != null && currentSpell.usesBrushSelection())
        {
            Spell brushSelect = mage.getSpell(brushSelectSpell);
            if (brushSelect != null)
            {
                brushSelect.cast();
                return;
            }
        }

        if (!hasInventory) {
            if (activeSpell == null || activeSpell.length() == 0) {
                // Sanity check, so it'll switch to inventory next time
                updateHasInventory();
                if (spells.size() > 0) {
                    setActiveSpell(spells.iterator().next());
                }
            }
            updateName();
            return;
        }
        if (!isInventoryOpen()) {
            openInventory();
        } else {
            closeInventory();
        }
    }

    public void updateHasInventory() {
        int inventorySize = getSpells().size() + getBrushes().size();
        hasInventory = inventorySize > 1 || (inventorySize == 1 && hasSpellProgression) || autoFill;
    }

    public void cycleInventory() {
        cycleInventory(1);
    }

    public void cycleInventory(int direction) {
        if (!hasInventory) {
            return;
        }
        if (isInventoryOpen()) {
            saveInventory();
            int inventoryCount = inventories.size();
            setOpenInventoryPage(inventoryCount == 0 ? 0 : (openInventoryPage + inventoryCount + direction) % inventoryCount);
            updateInventory();
            if (mage != null && inventories.size() > 1) {
                if (!playPassiveEffects("cycle") && inventoryCycleSound != null) {
                    mage.playSoundEffect(inventoryCycleSound);
                }
                CompatibilityLib.getDeprecatedUtils().updateInventory(mage.getPlayer());
            }
        }
    }

    @Override
    public void cycleHotbar() {
        cycleHotbar(1);
    }

    public boolean cycleHotbar(int direction) {
        if (mage == null) {
            return false;
        }
        WandMode mode = getMode();
        switch (mode) {
            case INVENTORY:
                return cycleHotbarInventory(direction);
            case CYCLE:
            case CHEST:
                return cycleHotbarChest(direction);
            default:
                break;
        }
        return false;
    }

    private boolean cycleHotbarChest(int direction) {
        if (inventories.isEmpty()) return false;
        int maxIndex = getInventoryItemCount() - 1;
        for (; maxIndex >= 0; maxIndex--) {
            ItemStack item = getInventoryItem(maxIndex);
            if (!CompatibilityLib.getItemUtils().isEmpty(item)) {
                break;
            }
        }
        int hotbarCount = maxIndex / CHEST_ITEMS_PER_ROW + 1;
        WandInventory previous = getActiveHotbar();
        setCurrentHotbar((currentHotbar + direction + hotbarCount) % hotbarCount);
        updateActiveSpellFromHotbarSwitch(previous, getActiveHotbar());
        return true;
    }

    private void updateActiveSpellFromHotbarSwitch(WandInventory previousHotbar, WandInventory currentHotbar) {
        if (previousHotbar == null || currentHotbar == null) return;
        ItemStack newActiveSpell = null;
        ItemStack fallbackSpell = null;
        if (activeSpell != null) {
            for (int slot = 0; slot < previousHotbar.getSize(); slot++) {
                ItemStack hotbarItem = previousHotbar.getItem(slot);
                String spellKey = getSpellBaseKey(hotbarItem);
                if (spellKey != null && spellKey.equals(activeSpell)) {
                    newActiveSpell = currentHotbar.getItem(slot);
                    if (newActiveSpell != null || fallbackSpell != null) {
                        break;
                    }
                } else if (fallbackSpell == null) {
                    // Fall back to the first non-empty icon on the hotbar
                    fallbackSpell = currentHotbar.getItem(slot);
                }
            }
        }
        if (newActiveSpell == null && fallbackSpell != null) {
            newActiveSpell = fallbackSpell;
        }
        if (newActiveSpell != null) {
            activateIcon(newActiveSpell);
        }
    }

    private boolean cycleHotbarInventory(int direction) {
        if (!hasInventory || hotbars.isEmpty()) {
            return false;
        }
        boolean isInventoryOpen = isInventoryOpen();
        if (isInventoryOpen) {
            saveInventory();
        }
        int hotbarCount = hotbars.size();
        int previousHotbar = currentHotbar;
        setCurrentHotbar(hotbarCount == 0 ? 0 : (currentHotbar + hotbarCount + direction) % hotbarCount);
        if (isInventoryOpen) {
            updateHotbar();
        }
        if (!playPassiveEffects("cycle_hotbar") && inventoryCycleSound != null) {
            mage.playSoundEffect(inventoryCycleSound);
        }
        sendMessage("hotbar_changed");
        if (isInventoryOpen) {
            updateHotbarStatus();
            CompatibilityLib.getDeprecatedUtils().updateInventory(mage.getPlayer());
        } else {
            WandInventory previous = hotbars.get(previousHotbar);
            WandInventory current = hotbars.get(currentHotbar);
            updateActiveSpellFromHotbarSwitch(previous, current);
        }
        return true;
    }

    private boolean activateIcon(ItemStack item) {
        if (CompatibilityLib.getItemUtils().isEmpty(item)) return false;

        String hotbarSpellKey = getSpellBaseKey(item);
        if (hotbarSpellKey == null) {
            return false;
        }
        // I feel like supporting brushes here would be annoying
        setActiveSpell(hotbarSpellKey);
        return true;
    }

    public void openInventory() {
        if (mage == null) return;
        if (System.currentTimeMillis() < mage.getWandDisableTime()) return;

        SpellInventoryEvent event = new SpellInventoryEvent(mage, true);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        WandMode wandMode = getMode();
        if (wandMode == WandMode.CHEST || wandMode == WandMode.SKILLS) {
            inventoryIsOpen = true;
            if (!playPassiveEffects("open") && inventoryOpenSound != null) {
                mage.playSoundEffect(inventoryOpenSound);
            }
            updateInventory();
            mage.getPlayer().openInventory(getDisplayInventory());
        } else if (wandMode == WandMode.INVENTORY) {
            if (hasStoredInventory()) return;
            if (storeInventory()) {
                inventoryIsOpen = true;
                updateRequirements();
                showActiveIcon(true);
                if (!playPassiveEffects("open") && inventoryOpenSound != null) {
                    mage.playSoundEffect(inventoryOpenSound);
                }
                updateInventory();
                updateHotbarStatus();
                if (inventoryOpenLore != null && !inventoryOpenLore.isEmpty()) {
                    updateLore();
                }
                updateName();
                updateActionBar();
            }
        }
    }

    @Override
    public void closeInventory() {
        closeInventory(true);
    }

    public void closeInventory(boolean closePlayerInventory) {
        if (!isInventoryOpen()) return;

        SpellInventoryEvent event = new SpellInventoryEvent(mage, false);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        controller.disableItemSpawn();
        inventoryWasOpen = true;
        WandMode mode = getMode();
        try {
            saveInventory();
            updateSpellInventory();
            updateBrushInventory();
            inventoryIsOpen = false;
            updateRequirements();
            updateName();
            updateActionBar();
            if (mage != null) {
                if (!playPassiveEffects("close") && inventoryCloseSound != null) {
                    mage.playSoundEffect(inventoryCloseSound);
                }
                if (mode == WandMode.INVENTORY) {
                    restoreInventory();
                    showActiveIcon(false);
                    if (inventoryOpenLore != null && !inventoryOpenLore.isEmpty()) {
                        updateLore();
                    }
                } else if (closePlayerInventory) {
                    mage.getPlayer().closeInventory();
                }

                // Check for items the player might've glitched onto their body...
                PlayerInventory inventory = mage.getPlayer().getInventory();
                ItemStack testItem = inventory.getHelmet();
                if (isSpell(testItem) || isBrush(testItem)) {
                    inventory.setHelmet(new ItemStack(Material.AIR));
                    CompatibilityLib.getDeprecatedUtils().updateInventory(mage.getPlayer());
                }
                testItem = inventory.getBoots();
                if (isSpell(testItem) || isBrush(testItem)) {
                    inventory.setBoots(new ItemStack(Material.AIR));
                    CompatibilityLib.getDeprecatedUtils().updateInventory(mage.getPlayer());
                }
                testItem = inventory.getLeggings();
                if (isSpell(testItem) || isBrush(testItem)) {
                    inventory.setLeggings(new ItemStack(Material.AIR));
                    CompatibilityLib.getDeprecatedUtils().updateInventory(mage.getPlayer());
                }
                testItem = inventory.getChestplate();
                if (isSpell(testItem) || isBrush(testItem)) {
                    inventory.setChestplate(new ItemStack(Material.AIR));
                    CompatibilityLib.getDeprecatedUtils().updateInventory(mage.getPlayer());
                }
                // This is kind of a hack :(
                testItem = inventory.getItemInOffHand();
                if ((isSpell(testItem) && !isSkill(testItem)) || isBrush(testItem)) {
                    inventory.setItemInOffHand(new ItemStack(Material.AIR));
                    CompatibilityLib.getDeprecatedUtils().updateInventory(mage.getPlayer());
                }
            }
        } catch (Throwable ex) {
            restoreInventory();
        }

        if (mode == WandMode.INVENTORY && mage != null && closePlayerInventory) {
            try {
                mage.getPlayer().closeInventory();
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
        controller.enableItemSpawn();
        inventoryWasOpen = false;
    }

    @Override
    public boolean fill(Player player) {
        return fill(player, 0);
    }

    @Override
    public boolean fill(Player player, int maxLevel) {
        // This is for the editor, it saves using player logins and is *not*
        // directly related to mage ids. This has to use player id.
        String playerId = player.getUniqueId().toString();
        closeInventory();

        // Update the inventory to make sure we don't overwrite slots of current spells
        if (this.mage != null) {
            buildInventory();
        }

        Collection<String> currentSpells = new ArrayList<>(getSpells());
        for (String spellKey : currentSpells) {
            SpellTemplate spell = controller.getSpellTemplate(spellKey);
            boolean removeSpell = !spell.hasCastPermission(player);
            String creatorId = spell.getCreatorId();
            removeSpell = removeSpell || (FILL_CREATOR && (creatorId == null || !playerId.equals(creatorId)));
            if (removeSpell)
            {
                removeSpell(spellKey);
            }
        }

        Collection<SpellTemplate> allSpells = controller.getPlugin().getSpellTemplates();

        // Hack to prevent messaging
        Mage mage = this.mage;
        this.mage = null;
        for (SpellTemplate spell : allSpells)
        {
            String key = spell.getKey();
            if (maxLevel > 0 && spell.getSpellKey().getLevel() > maxLevel)
            {
                continue;
            }
            if (key.startsWith("heroes*"))
            {
                continue;
            }
            String creatorId = spell.getCreatorId();
            if (FILL_CREATOR && (creatorId == null || !playerId.equals(creatorId)))
            {
                continue;
            }
            if (spell.hasCastPermission(player) && spell.hasIcon() && !spell.isHidden())
            {
                addSpell(key);
            }
        }
        this.mage = mage;
        updateSpellInventory();
        updateBrushInventory();
        if (this.mage != null) {
            buildInventory();
        }

        if (!FILL_CREATOR) {
            if (autoFill) setProperty("fill", false);
            autoFill = false;
        }
        saveState();

        return true;
    }

    protected void checkActiveMaterial() {
        if (activeBrush == null || activeBrush.length() == 0) {
            Set<String> materials = getBrushes();
            if (materials.size() > 0) {
                activeBrush = materials.iterator().next();
            }
        }
    }

    @Override
    public boolean addItem(ItemStack item) {
        if (isUpgrade) return false;
        // This prevents issues with class wands that don't own their own properties, though this is
        // not really an idea solution.
        if (mageClassKeys != null && !mageClassKeys.isEmpty() && mage == null) {
            return false;
        }
        CurrencyAmount currency = CompatibilityLib.getInventoryUtils().getCurrencyAmount(item);
        boolean isUpgrade = isUpgrade(item);
        if (!isModifiable() && !isUpgrade && currency == null) return false;
        if (isUpgrade) {
            Wand upgradeWand = controller.createWand(item);
            String slot = upgradeWand.getSlot();
            // Make sure to not allow super.addItem to process this if it has a slot
            // Even if we can't add it via addSloted, it would be absorbed as an upgrade and permanently
            // change this wand.
            // This could probably be improved with some re-arranging.
            if (slot != null && !slot.isEmpty()) {
                boolean added = addSlotted(upgradeWand);
                if (added) {
                    saveSlotted();
                    updateSlotted();
                    updated();
                }
                return added;
            }
        }

        return super.addItem(item);
    }

    public boolean addSlotted(Wand upgradeWand) {
        if (slots == null || slots.isEmpty()) {
            return false;
        }
        for (WandUpgradeSlot slot : slots) {
            if (slot.addSlotted(upgradeWand, mage)) {
                return true;
            }
        }
        return false;
    }

    private void saveSlotted() {
        List<String> slottedKeys = null;
        if (unslotted != null || slots != null) {
            slottedKeys = new ArrayList<>();
            if (slots != null) {
                for (WandUpgradeSlot slot : slots) {
                    if (slot.hasDefaultSlotted()) {
                        continue;
                    }
                    Wand slotted = slot.getSlotted();
                    if (slotted != null) {
                        slottedKeys.add(slotted.getKey());
                    }
                }
            }
            if (unslotted != null) {
                slottedKeys.addAll(unslotted);
            }
        }
        setProperty("slotted", slottedKeys);
    }

    protected void updateSlotted() {
        if (slots == null || slots.isEmpty()) {
            slottedConfiguration = null;
            return;
        }
        slottedConfiguration = ConfigurationUtils.newConfigurationSection();
        for (WandUpgradeSlot slot : slots) {
            Wand slotted = slot.getSlotted();
            if (slotted != null) {
                ConfigurationSection upgradeConfig = ConfigurationUtils.cloneConfiguration(slotted.getEffectiveConfiguration());
                cleanSlottedUpgradeConfig(upgradeConfig);
                ConfigurationUtils.addConfigurations(slottedConfiguration, upgradeConfig);
            }
        }
    }

    protected void updateEffects() {
        updateEffects(mage);
    }

    public void updateEffects(Mage mage) {
        if (mage == null) return;
        Player player = mage.getPlayer();
        if (player == null) return;

        // Update Bubble effects effects
        Color effectColor = getEffectColor();
        if (effectBubbles && effectColor != null) {
            Location potionEffectLocation = player.getLocation();
            potionEffectLocation.setX(potionEffectLocation.getX() + random.nextDouble() - 0.5);
            potionEffectLocation.setY(potionEffectLocation.getY() + random.nextDouble() * player.getEyeHeight());
            potionEffectLocation.setZ(potionEffectLocation.getZ() + random.nextDouble() - 0.5);
            EffectPlayer.displayParticle(Particle.SPELL_MOB, potionEffectLocation, 0, 0, 0,
            0, 0, 1, effectColor, null, (byte)0, 24);
        }

        Location location = mage.getLocation();
        long now = System.currentTimeMillis();
        boolean playEffects = !activeEffectsOnly || inventoryIsOpen || isInOffhand;
        if (playEffects && effectParticle != null && effectParticleInterval > 0 && effectParticleCount > 0) {
            boolean velocityCheck = true;
            if (effectParticleMinVelocity > 0) {
                double velocitySquared = effectParticleMinVelocity * effectParticleMinVelocity;
                Vector velocity = mage.getVelocity().clone();
                velocity.setY(0);
                double speedSquared = velocity.lengthSquared();
                velocityCheck = (speedSquared > velocitySquared);
            }
            if (velocityCheck && (lastParticleEffect == 0 || now > lastParticleEffect + effectParticleInterval)) {
                lastParticleEffect = now;
                Location effectLocation = player.getLocation();
                Location eyeLocation = player.getEyeLocation();
                effectLocation.setY(eyeLocation.getY() + effectParticleOffset);
                if (effectPlayer == null) {
                    effectPlayer = new EffectRing(controller.getPlugin());
                    effectPlayer.setParticleCount(1);
                    effectPlayer.setIterations(1);
                    effectPlayer.setParticleOffset(0, 0, 0);
                }
                effectPlayer.setMaterial(location.getBlock().getRelative(BlockFace.DOWN));
                if (effectParticleData == 0)
                {
                    effectPlayer.setColor(effectColor);
                }
                else
                {
                    effectPlayer.setColor(null);
                }
                effectPlayer.setParticleType(effectParticle);
                effectPlayer.setParticleData(effectParticleData);
                effectPlayer.setSize(effectParticleCount);
                effectPlayer.setRadius((float)effectParticleRadius);
                effectPlayer.start(effectLocation, null);
            }
        }

        if (castSpell != null && castInterval > 0) {
            if (lastSpellCast == 0 || now > lastSpellCast + castInterval) {
                boolean velocityCheck = true;
                if (castMinVelocity > 0) {
                    double velocitySquared = castMinVelocity * castMinVelocity;
                    Vector velocity = mage.getVelocity();
                    if (castVelocityDirection != null) {
                        velocity = velocity.clone().multiply(castVelocityDirection);

                        // This is kind of a hack to make jump-detection work.
                        if (castVelocityDirection.getY() < 0) {
                            velocityCheck = velocity.getY() < 0;
                        } else {
                            velocityCheck = velocity.getY() > 0;
                        }
                    }
                    if (velocityCheck)
                    {
                        double speedSquared = velocity.lengthSquared();
                        velocityCheck = (speedSquared > velocitySquared);
                    }
                }
                if (velocityCheck) {
                    lastSpellCast = now;
                    Spell spell = mage.getSpell(castSpell);
                    if (spell != null) {
                        boolean costFree = getBoolean("cast_interval_cost_free", false);
                        if (castParameters == null) {
                            castParameters = ConfigurationUtils.newConfigurationSection();
                        }
                        castParameters.set("aura", true);
                        if (costFree) {
                            mage.setCostFree(true);
                        }
                        mage.setQuiet(true);
                        try {
                            doCast(spell, castParameters);
                        } catch (Exception ex) {
                            controller.getLogger().log(Level.WARNING, "Error casting aura spell " + spell.getKey(), ex);
                        }
                        if (costFree) {
                            mage.setCostFree(false);
                        }
                        mage.setQuiet(false);
                    }
                }
            }
        }

        if (playEffects && effectSound != null && controller.soundsEnabled() && effectSoundInterval > 0) {
            if (lastSoundEffect == 0 || now > lastSoundEffect + effectSoundInterval) {
                lastSoundEffect = now;
                effectSound.play(controller.getPlugin(), controller.getLogger(), mage.getPlayer());
            }
        }
    }

    protected void updateDurability() {
        int maxDurability = item.getType().getMaxDurability();
        if (maxDurability > 0 && effectiveManaMax > 0) {
            int durability = (short)(getMana() * maxDurability / effectiveManaMax);
            durability = maxDurability - durability;
            if (durability >= maxDurability) {
                durability = maxDurability - 1;
            } else if (durability < 0) {
                durability = 0;
            }
            CompatibilityLib.getDeprecatedUtils().setItemDamage(item, (short)durability);
        }
    }

    public boolean usesXPBar()
    {
        return xpBarDisplayMode.isEnabled(this);
    }

    public boolean usesXPNumber()
    {
        return levelDisplayMode.isEnabled(this);
    }

    public boolean usesXPDisplay()
    {
        return usesXPBar() || usesXPNumber();
    }

    public boolean usesCurrency(String type) {
        if (type.equals("sp") && !usesSP()) return false;
        return xpBarDisplayMode.usesCurrency(type) || levelDisplayMode.usesCurrency(type) || bossBarDisplayMode.usesCurrency(type);
    }

    public boolean usesSP() {
        return controller.isSPEarnEnabled() && hasSpellProgression && earnMultiplier > 0;
    }

    public boolean hasSpellProgression()
    {
        return hasSpellProgression;
    }

    public boolean usesInstructions() {
        return instructions;
    }

    @Override
    public void updateMana() {
        if (isInOffhand) {
            return;
        }

        Player player = mage == null ? null : mage.getPlayer();
        if (player == null) return;
        updateXPBar();

        float mana = getMana();
        if (usesMana()) {
            if (actionBarMana) {
                updateActionBar();
            }
            if (manaMode.useGlow()) {
                if (mana == effectiveManaMax) {
                    CompatibilityLib.getItemUtils().addGlow(item);
                } else {
                    CompatibilityLib.getItemUtils().removeGlow(item);
                }
            }
            if (manaMode.useDurability()) {
                updateDurability();
            }
        }
    }

    protected void clearActionBar() {
        Player player = mage == null ? null : mage.getPlayer();
        if (player == null || actionBarMessage == null || actionBarMana) {
            return;
        }
        CompatibilityLib.getCompatibilityUtils().sendActionBar(player, "");
    }

    protected void updateActionBar() {
        Player player = mage == null ? null : mage.getPlayer();
        if (player == null) {
            return;
        }
        String useMessage = getActionBarMessage();
        if (useMessage == null || useMessage.isEmpty()) {
            // Clear immediately if we just turned off the message
            if (lastActionBarMessage != null && !lastActionBarMessage.isEmpty()) {
                clearActionBar();
                lastActionBarMessage = useMessage;
            }
            return;
        }
        if (actionBarDelay > 0 && System.currentTimeMillis() < activationTimestamp + actionBarDelay) {
            return;
        }
        if (actionBarMana) {
            double mana = mage.getMana();
            double manaMax = mage.getEffectiveManaMax();
            boolean fullMana = mana == manaMax;
            if (fullMana && lastActionBarFullMana) {
                return;
            }
            lastActionBarFullMana = fullMana;
        }
        String message = parameterize(useMessage);
        CompatibilityLib.getCompatibilityUtils().sendActionBar(player, message, actionBarFont);
        lastActionBarMessage = message;
    }

    protected String getActionBarMessage() {
        return inventoryIsOpen ? actionBarOpenMessage : actionBarMessage;
    }

    protected boolean isActionBarActive() {
        String message = getActionBarMessage();
        return message != null && !message.isEmpty();
    }

    public boolean handleActionBar(String message) {
        String actionBarMessage = getActionBarMessage();
        if (actionBarMessage == null || !actionBarMessage.contains("$extra")) {
            return false;
        }
        return glyphHotbar.handleActionBar(message);
    }

    public boolean handleInsufficientResources(Spell spell, CastingCost cost) {
        if (cost.getMana() == 0) {
            return false;
        }
        return glyphHotbar.handleInsufficientResources(spell, cost);
    }

    public boolean handleCooldown(Spell spell) {
        String actionBarMessage = getActionBarMessage();
        if (actionBarMessage == null || !actionBarMessage.contains("$hotbar")) {
            return false;
        }
        return glyphHotbar.handleCooldown(spell);
    }

    public boolean handleInsufficientCharges(Spell spell) {
        String actionBarMessage = getActionBarMessage();
        if (actionBarMessage == null || !actionBarMessage.contains("$hotbar")) {
            return false;
        }
        return glyphHotbar.handleInsufficientCharges(spell);
    }

    @Override
    public String parameterize(String message) {
        if (message == null || message.isEmpty()) return "";
        message = TextUtils.parameterize(message, this);
        if (mage != null) {
            message = mage.parameterize(message);
        }
        return message;
    }

    protected WandInventory getActiveHotbar() {
        WandMode mode = getMode();
        switch (mode) {
            case CYCLE:
            case CHEST:
                if (inventories.isEmpty()) return null;
                int inventoryIndex = currentHotbar / inventoryRows;
                if (inventoryIndex >= inventories.size()) {
                    inventoryIndex = 0;
                }
                int inventoryRow = currentHotbar % inventoryRows;
                return inventories.get(inventoryIndex).getRow(inventoryRow, CHEST_ITEMS_PER_ROW);
            default:
                break;
        }
        if (hotbars.isEmpty()) return null;
        return hotbars.get(currentHotbar);
    }

    private String getHotbarGlyphs() {
        return glyphHotbar.getGlyphs();
    }

    @Override
    public String getReplacement(String line, boolean integerValues) {
        switch (line) {
            case "extra":
                return glyphHotbar.getExtraMessage();
            case "hotbar":
                return getHotbarGlyphs();
            case "description":
                return getAndUpdateDescription();
            case "path":
                String pathTemplate = getMessage("path_lore", "");
                String pathName = getPathName();
                if (pathName != null && !pathTemplate.isEmpty()) {
                    return pathTemplate.replace("$path", pathName);
                }
                return "";
            case "owner":
                return getOwnerDescription();
            case "spells":
                int spellCount = getSpells().size();
                if (spellCount > 0) {
                    return getMessage("spell_count").replace("$count", Integer.toString(spellCount));
                }
                return "";
            case "brushes":
                int materialCount = getBrushes().size();
                if (materialCount > 0) {
                    return getMessage("material_count").replace("$count", Integer.toString(materialCount));
                }
                return "";
            case "uses":
                return getUseLore();
            case "mana_max":
                if (usesMana()) {
                    float manaMax = getManaMax();
                    if (effectiveManaMax != manaMax) {
                        String fullMessage = getLevelString("mana_amount_boosted", manaMax, controller.getMaxMana());
                        return fullMessage.replace("$mana", Integer.toString((int)Math.ceil(effectiveManaMax)));
                    } else {
                        return getLevelString("mana_amount", manaMax, controller.getMaxMana());
                    }
                }
                return "";
            case "mana_regeneration":
                if (usesMana()) {
                    double manaRegeneration = getManaRegeneration();
                    if (manaRegeneration > 0) {
                        if (effectiveManaRegeneration != manaRegeneration) {
                            String fullMessage = getLevelString("mana_regeneration_boosted", (int)Math.ceil(manaRegeneration), controller.getMaxManaRegeneration());
                            return fullMessage.replace("$mana", Integer.toString((int)Math.ceil(effectiveManaRegeneration)));
                        } else {
                            return getLevelString("mana_regeneration", (int)Math.ceil(manaRegeneration), controller.getMaxManaRegeneration());
                        }
                    }
                }
                return "";
        }
        return null;
    }

    protected void resetXPDisplay() {
        Player player = mage == null ? null : mage.getPlayer();
        if (player == null) return;
        mage.sendExperience(player.getExp(), player.getLevel());
    }

    public void updateXPBar() {
        Player player = mage == null ? null : mage.getPlayer();
        if (player == null) return;

        boolean usesXPDisplay = xpBarDisplayMode.isEnabled(this);
        // backwards-compatibility
        if (usesXPDisplay && xpBarDisplayMode.usesMana() && manaMode == WandManaMode.NONE) {
            usesXPDisplay = false;
        }
        boolean usesLevelDisplay = levelDisplayMode.isEnabled(this);
        if (usesXPDisplay || usesLevelDisplay) {
            int playerLevel = player.getLevel();
            float playerProgress = player.getExp();

            if (usesXPDisplay) {
                playerProgress = (float)Math.min(Math.max(0, xpBarDisplayMode.getProgress(this)), 1);
            }
            if (usesLevelDisplay) {
                playerLevel = (int)levelDisplayMode.getValue(this);
            }

            mage.sendExperience(playerProgress, playerLevel);
        }
    }

    @Override
    public boolean isInventoryOpen() {
        return mage != null && inventoryIsOpen;
    }

    // Somewhat hacky method to handle inventory close event knowing that this was a wand inventory that just closed.
    public boolean wasInventoryOpen() {
        return inventoryWasOpen;
    }

    @Override
    public void unbind() {
        com.elmakers.mine.bukkit.api.magic.Mage owningMage = this.mage;
        deactivate();

        if (ownerId != null) {
            if (owningMage == null || !owningMage.getId().equals(ownerId)) {
                owningMage = controller.getRegisteredMage(ownerId);
            }

            if (owningMage != null) {
                owningMage.unbind(this);
            }
            ownerId = null;
        } else {
            owningMage.unbind(this);
        }
        bound = false;
        owner = null;
        setProperty("bound", false);
        setProperty("owner", null);
        setProperty("owner_id", null);
        saveState();
        updateLore();
        updateName();
    }

    @Override
    public void bind() {
        if (bound) return;

        Mage holdingMage = mage;
        deactivate();
        bound = true;
        setProperty("bound", true);
        saveState();

        if (holdingMage != null) {
            holdingMage.checkWand();
        }
    }

    @Override
    public void deactivate() {
        deactivate(true);
    }

    public void deactivate(boolean closePlayerInventory) {
        if (mage == null || !isActive) return;
        isActive = false;
        mage.sendDebugMessage(ChatColor.YELLOW + " Deactivating wand", 50);

        // Remove boss bar if we have one
        removeBossBar();

        // Clear action bar immediately
        clearActionBar();

        // Play deactivate FX
        playPassiveEffects("deactivate");

        // Cancel effects
        if (wandContext != null) {
            int cancelDelay = getInt("cancel_effects_delay", 0);
            if (cancelDelay == 0) {
                wandContext.cancelEffects();
            } else {
                Plugin plugin = controller.getPlugin();
                plugin.getServer().getScheduler().runTaskLater(plugin, new CancelEffectsContextTask(wandContext), cancelDelay * 20 / 1000);
            }
        }

        Mage mage = this.mage;

        if (isInventoryOpen()) {
            closeInventory(closePlayerInventory);
        }
        storedInventory = null;
        if (usesXPNumber() || usesXPBar()) {
            mage.resetSentExperience();
        }
        saveState();
        showActiveIcon(false);
        mage.deactivateWand(this);
        this.mage = null;
        // Update any item attributes or enchants that rely on player attributes
        setTemplate(template);
        updateItem();
        updateMaxMana(true);
    }

    @Nullable
    @Override
    public Spell getActiveSpell() {
        if (mage == null) return null;
        String activeSpellKey = getActiveSpellKey();
        if (activeSpellKey == null || activeSpellKey.length() == 0) return null;
        return mage.getSpell(activeSpellKey);
    }

    @Nullable
    public Spell getAlternateSpell(int index) {
        String key = alternateSpells[index];
        if (mage == null || key == null || key.length() == 0) return null;
        return mage.getSpell(key);
    }

    @Nullable
    @Override
    public SpellTemplate getBaseSpell(String spellName) {
        return getBaseSpell(new SpellKey(spellName));
    }

    @Nullable
    public SpellTemplate getBaseSpell(SpellKey key) {
        if (!spells.contains(key.getBaseKey())) return null;
        SpellKey baseKey = new SpellKey(key.getBaseKey(), getSpellLevel(key.getBaseKey()));
        return controller.getSpellTemplate(baseKey.getKey());
    }

    public String getBaseActiveSpell() {
        return activeSpell;
    }

    @Override
    public String getActiveSpellKey() {
        String activeSpellKey = activeSpell;
        Integer level = spellLevels.get(activeSpellKey);
        if (level != null) {
            activeSpellKey = new SpellKey(activeSpellKey, level).getKey();
        }
        return activeSpellKey;
    }

    @Override
    public String getActiveBrushKey() {
        return activeBrush;
    }

    @Override
    public void damageDealt(double damage, Entity target) {
        if (manaPerDamage > 0) {
            int manaMax = getEffectiveManaMax();
            float mana = getMana();
            if (manaMax > 0 && mana < manaMax) {
                setMana(Math.min(manaMax, mana + (float)damage * manaPerDamage));
                updateMana();
            }
        }
    }

    public boolean alternateCast(int index) {
        return cast(getAlternateSpell(index));
    }

    @Override
    public boolean cast() {
        return cast(getActiveSpell(), null);
    }

    @Override
    public boolean cast(String[] parameters) {
        return cast(getActiveSpell(), parameters);
    }

    @Override
    public boolean cast(Spell spell) {
        return cast(spell, null);
    }

    public boolean cast(Spell spell, String[] parameterArguments) {
        ConfigurationSection parameters = null;
        if (parameterArguments != null && parameterArguments.length > 0) {
            parameters = ConfigurationUtils.newConfigurationSection();
            ConfigurationUtils.addParameters(parameterArguments, parameters);
        }
        return doCast(spell, parameters);
    }

    public boolean doCast(Spell spell, ConfigurationSection parameters) {
        if (spell == null) {
            return false;
        }
        if (spell.isPassive()) {
            if (spell.isToggleable()) {
                spell.setEnabled(!spell.isEnabled());
                updateSpellItem(spell);
            }
            return true;
        }
        if (useMode == WandUseMode.PRECAST) {
            use();
        }
        ConfigurationSection castParameters = null;
        Map<String, String> castOverrides = this.getOverrides();
        if (castOverrides != null && castOverrides.size() > 0) {
            castParameters = ConfigurationUtils.newConfigurationSection();
            for (Map.Entry<String, String> entry : castOverrides.entrySet()) {
                String[] key = StringUtils.split(entry.getKey(), ".", 2);
                if (key.length == 0) continue;
                if (key.length == 2 && !key[0].equals("default") && !key[0].equals(spell.getSpellKey().getBaseKey()) && !key[0].equals(spell.getSpellKey().getKey())) {
                    continue;
                }
                castParameters.set(key.length == 2 ? key[1] : key[0], entry.getValue());
            }
        }
        if (parameters != null) {
            if (castParameters == null) {
                castParameters = ConfigurationUtils.newConfigurationSection();
            }
            ConfigurationUtils.addConfigurations(castParameters, parameters, true);
        }
        if (spell.cast(this, castParameters)) {
            if (useMode != WandUseMode.PRECAST) {
                use();
            }
            onCast(spell);
            updateHotbarStatus();
            return true;
        }

        if (useMode == WandUseMode.ALWAYS) {
            use();
        }

        return false;
    }

    protected boolean use() {
        boolean usesRemaining = true;
        if (hasUses) {
            findItem();
            ItemStack item = getItem();
            if (mage != null) {
                Player player = mage.getPlayer();
                if (player != null && player.getGameMode() == GameMode.CREATIVE) {
                    return true;
                }
            }
            if (item.getAmount() > 1)
            {
                item.setAmount(item.getAmount() - 1);
            }
            else
            {
                if (uses > 0)
                {
                    uses--;
                }
                if (uses <= 0 && mage != null) {
                    // If the wand is not currently active it will be destroyed on next activate
                    Player player = mage.getPlayer();
                    Mage theMage = mage;
                    playEffects("break");

                    deactivate();

                    PlayerInventory playerInventory = player.getInventory();
                    if (item.getAmount() > 1) {
                        item.setAmount(item.getAmount() - 1);
                    } else {
                        if (isInOffhand) {
                            playerInventory.setItemInOffHand(new ItemStack(Material.AIR, 1));
                        } else {
                            playerInventory.setItemInMainHand(new ItemStack(Material.AIR, 1));
                        }
                        usesRemaining = false;
                        theMage.sendMessage(getMessage("used"));
                    }
                    CompatibilityLib.getDeprecatedUtils().updateInventory(player);
                }
                setProperty("uses", uses);
                saveState();
                updateName();
                updateLore();
            }
        }

        return usesRemaining;
    }

    // Taken from NMS HumanEntity
    public static int getExpToLevel(int expLevel) {
        return expLevel >= 30 ? 112 + (expLevel - 30) * 9 : (expLevel >= 15 ? 37 + (expLevel - 15) * 5 : 7 + expLevel * 2);
    }

    public static int getExperience(int expLevel, float expProgress) {
        int xp = 0;
        for (int level = 0; level < expLevel; level++) {
            xp += Wand.getExpToLevel(level);
        }
        return xp + (int) (expProgress * Wand.getExpToLevel(expLevel));
    }

    protected void updateHotbarStatus() {
        Player player = mage == null ? null : mage.getPlayer();
        if (player != null && LiveHotbar && getMode() == WandMode.INVENTORY && isInventoryOpen()) {
            mage.updateHotbarStatus();
        }
    }

    @Override
    public boolean tickMana() {
        if (isHeroes)
        {
            HeroesManager heroes = controller.getHeroes();
            if (heroes != null && mage != null && mage.isPlayer())
            {
                Player player = mage.getPlayer();
                effectiveManaMax = (float)heroes.getMaxMana(player);
                effectiveManaRegeneration = (float)heroes.getManaRegen(player);
                setManaMax(effectiveManaMax);
                setManaRegeneration(effectiveManaRegeneration);
                setMana((float)heroes.getMana(player));
                return true;
            }

            return false;
        }
        return super.tickMana();
    }

    @Override
    public void tick() {
        if (mage == null) return;
        Player player = mage.getPlayer();
        if (player == null) return;

        super.tick();

        updateRequirements();

        // Update UIs, if not in offhand
        if (!isInOffhand) {
            updateXPBar();
            long now = System.currentTimeMillis();
            // Always tick action bar while animating
            if (isActionBarActive()
                    && (now > lastActionBar + actionBarInterval || glyphHotbar.isAnimating())) {
                lastActionBar = now;
                updateActionBar();
            }
            updateHotbarStatus();
            checkBossBar();
        }

        // Check for blocking cooldown
        if (player.isBlocking() && blockMageCooldown > 0) {
            mage.setRemainingCooldown(blockMageCooldown);
        }

        if (!worn) {
            updateEffects();
        }
    }

    protected boolean updateRequirementConfiguration() {
        if (mage == null) {
            requirementConfiguration = null;
            return false;
        }
        MageContext context = getContext();
        boolean changed = false;
        for (RequirementProperties properties : requirementProperties) {
            if (properties.hasChanged(context)) {
                changed = true;
            }
        }
        if (changed) {
            requirementConfiguration = null;
            for (RequirementProperties properties : requirementProperties) {
                if (properties.isAllowed()) {
                    if (requirementConfiguration == null) {
                        requirementConfiguration = ConfigurationUtils.newConfigurationSection();
                    }
                    ConfigurationUtils.addConfigurations(requirementConfiguration, properties.getProperties(), true);
                }
            }
        }
        return changed;
    }

    protected void updateRequirements() {
        if (requirementProperties == null) return;
        if (updateRequirementConfiguration()) {
            loadParameters();
            updateName();
            updateLore();
        }
    }

    @Override
    public void passiveEffectsUpdated() {
        updateMaxMana(true);
    }

    protected void updateMaxMana(boolean updateLore) {
        if (isHeroes) return;
        if (!hasOwnMana() && mageClass != null) {
            updateLore = mageClass.updateMaxMana(mage) && updateLore;

            effectiveManaMax = mageClass.getEffectiveManaMax();
            effectiveManaRegeneration = mageClass.getEffectiveManaRegeneration();
            if (updateLore) {
                updateLore();
            }
        } else if (super.updateMaxMana(mage) && updateLore) {
            updateLore();
        }
    }

    @Nullable
    public Mage getActiveMage() {
        // TODO: Duplicate of #getMage()
        return mage;
    }

    public void setActiveMage(com.elmakers.mine.bukkit.api.magic.Mage mage) {
        if (mage instanceof Mage) {
            this.mage = (Mage)mage;
            passiveEffectsUpdated();
        }
    }

    public Particle getEffectParticle() {
        return effectParticle;
    }

    @Nullable
    @Override
    public String getEffectParticleName() {
        return effectParticle == null ? null : effectParticle.name();
    }

    @Nullable
    public WandInventory getHotbar() {
        if (this.hotbars.size() == 0) return null;

        if (currentHotbar < 0 || currentHotbar >= this.hotbars.size())
        {
            setCurrentHotbar(0);
        }
        return this.hotbars.get(currentHotbar);
    }

    public int getHotbarCount() {
        if (getMode() != WandMode.INVENTORY) return 0;
        return Math.max(1, getInt("hotbar_count", 1));
    }

    public List<WandInventory> getHotbars() {
        return hotbars;
    }

    @Override
    public boolean isQuickCastDisabled() {
        return quickCastDisabled;
    }

    public boolean isManualQuickCastDisabled() {
        return manualQuickCastDisabled;
    }

    @Override
    public boolean isQuickCast() {
        return quickCast;
    }

    public WandMode getMode() {
        WandMode wandMode = mode;
        Player player = mage == null ? null : mage.getPlayer();
        if (CREATIVE_CHEST_MODE && wandMode == WandMode.INVENTORY && player != null && player.getGameMode() == GameMode.CREATIVE) {
            wandMode = WandMode.CHEST;
        }
        return wandMode;
    }

    public WandMode getBrushMode() {
        return brushMode;
    }

    public void setMode(WandMode mode) {
        this.mode = mode;
    }

    public void setBrushMode(WandMode mode) {
        this.brushMode = mode;
    }

    @Override
    public boolean showCastMessages() {
        return quietLevel == 0;
    }

    @Override
    public boolean showMessages() {
        return quietLevel < 2;
    }

    public boolean isStealth() {
        return quietLevel > 2;
    }

    @Override
    public void setPath(String path) {
        String oldPath = this.path;
        this.path = path;
        setProperty("path", path);
        if (!oldPath.equals(path)) {
            updated();
        }
    }

    /*
     * Public API Implementation
     */

    @Override
    public boolean isLost(com.elmakers.mine.bukkit.api.wand.LostWand lostWand) {
        return this.id != null && this.id.equals(lostWand.getId());
    }

    @Override
    public LostWand makeLost(Location location)
    {
        checkId();
        saveState();
        return new LostWand(this, location);
    }

    public void applyIcon() {
        if (!useActiveIcon()) return;
        findItem();
        icon.applyToItem(item);
    }

    public void showActiveIcon(boolean show) {
        if (this.icon == null || this.inactiveIcon == null || this.inactiveIcon.getMaterial() == Material.AIR || this.inactiveIcon.getMaterial() == null) return;
        if (this.icon.getMaterial() == Material.AIR || this.icon.getMaterial() == null) {
            this.icon.setMaterial(DefaultWandMaterial);
        }
        if (show) {
            if (inactiveIconDelay > 0) {
                Plugin plugin = controller.getPlugin();
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new ApplyWandIconTask(this), inactiveIconDelay * 20 / 1000);
            } else {
                findItem();
                icon.applyToItem(item);
            }
        } else {
            findItem();
            inactiveIcon.applyToItem(this.item);
        }
    }

    public boolean activateOffhand(Mage mage) {
        return activate(mage, true);
    }

    @Override
    @Deprecated
    public void activate(com.elmakers.mine.bukkit.api.magic.Mage mage) {
        if (mage instanceof Mage) {
            activate((Mage)mage);
        }
    }

    public boolean activate(Mage mage) {
        return activate(mage, false);
    }

    public boolean activate(Mage mage, boolean offhand) {
        if (mage == null) return false;
        mage.sendDebugMessage(ChatColor.YELLOW + "   Activating wand", 50);
        Player player = mage.getPlayer();
        if (player == null) return false;

        if (!controller.hasWandPermission(player, this)) return false;

        if (getBoolean("self_destruct")) {
            mage.sendMessageKey("wand.self_destruct");
            if (offhand) {
                player.getInventory().setItemInOffHand(null);
            } else {
                player.getInventory().setItemInMainHand(null);
            }
            return false;
        }

        InventoryView openInventory = player.getOpenInventory();
        InventoryType inventoryType = openInventory.getType();
        if (inventoryType == InventoryType.ENCHANTING
                || inventoryType == InventoryType.ANVIL) return false;

        if (hasUses && uses <= 0) {
            if (offhand) {
                player.getInventory().setItemInOffHand(new ItemStack(Material.AIR, 1));
            } else {
                player.getInventory().setItemInMainHand(new ItemStack(Material.AIR, 1));
            }
            return false;
        }

        if (!canUse(player)) {
            mage.messageNoUse(this);
            return false;
        }

        if (this.isUpgrade) {
            controller.getLogger().warning("Activated an upgrade item- this shouldn't happen");
            return false;
        }

        WandPreActivateEvent preActivateEvent = new WandPreActivateEvent(mage, this);
        Bukkit.getPluginManager().callEvent(preActivateEvent);
        if (preActivateEvent.isCancelled()) {
            return false;
        }

        if (getMode() != WandMode.INVENTORY) {
            showActiveIcon(true);
        }

        boolean needsSave = false;
        if (hasId) {
            needsSave = this.checkId() || needsSave;
        } else {
            setProperty("id", null);
        }

        if (!setMage(mage, offhand, player)) {
            return false;
        }
        isActive = true;
        activationTimestamp = System.currentTimeMillis();
        this.isInOffhand = offhand;
        this.heldSlot = offhand ? OFFHAND_SLOT : player.getInventory().getHeldItemSlot();

        discoverRecipes("discover_recipes");

        mage.setLastActivatedSlot(player.getInventory().getHeldItemSlot());

        // Check for replacement template
        String replacementTemplate = getString("replace_on_activate", "");
        if (!replacementTemplate.isEmpty() && !replacementTemplate.equals(template)) {
            replaceTemplate(replacementTemplate);
            return activate(mage, offhand);
        }

        // Since these wands can't be opened we will just show them as open when held
        // We have to delay this 1 tick so it happens after the Mage has accepted the Wand
        if ((getMode() != WandMode.INVENTORY || offhand) && controller.isLoaded()) {
            Plugin plugin = controller.getPlugin();
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new OpenWandTask(this), 1);
        }

        // Check for an empty wand and auto-fill
        if (!isUpgrade && (controller.fillWands() || autoFill)) {
            fill(mage.getPlayer(), controller.getMaxWandFillLevel());
            needsSave = true;
        }

        if (isHeroes) {
            HeroesManager heroes = controller.getHeroes();
            if (heroes != null) {
                Set<String> skills = heroes.getSkills(player);
                Collection<String> currentSpells = new ArrayList<>(getSpells());
                for (String spellKey : currentSpells) {
                    if (spellKey.startsWith("heroes*") && !skills.contains(spellKey.substring(7)))
                    {
                        removeSpell(spellKey);
                    }
                }

                // Hack to prevent messaging
                this.mage = null;
                for (String skillKey : skills)
                {
                    String heroesKey = "heroes*" + skillKey;
                    if (!spells.contains(heroesKey))
                    {
                        addSpell(heroesKey);
                    }
                }
                this.mage = mage;
            }
        }

        // Check for mana reset
        if (resetManaOnActivate != null && usesMana()) {
            float newMana = (float)(double)resetManaOnActivate;
            if (newMana < 1) {
                newMana *= getManaMax();
            }
            if (newMana < getMana()) {
                setMana(newMana);
            }
            setProperty("mana_timestamp", System.currentTimeMillis());
        }

        // Check for auto-organize
        if (autoOrganize && !isUpgrade) {
            organizeInventory(mage);
            needsSave = true;
        }

        // Check for auto-alphabetize
        if (autoAlphabetize && !isUpgrade) {
            alphabetizeInventory();
            needsSave = true;
        }

        boolean forceUpdate = false;
        if (autoAbsorb && checkInventoryForUpgrades()) {
            forceUpdate = true;
            needsSave = true;
        }

        // Check for randomized wands
        if (randomizeOnActivate) {
            randomize();
            randomizeOnActivate = false;
            forceUpdate = true;
            needsSave = true;
        }

        // Don't build the inventory until activated so we can take Mage boosts into account
        if (offhand) {
            mage.setOffhandWand(this);
        } else {
            mage.setActiveWand(this);
        }
        buildInventory();

        updateMaxMana(false);
        tick();
        if (!isInOffhand) {
            updateMana();
        }

        checkActiveMaterial();

        // Check for auto-bind
        // Do this after above initialization so the wand instructions have a clear picture of this wand's config
        if (bound)
        {
            String mageName = ChatColor.stripColor(mage.getPlayer().getDisplayName());
            String mageId = mage.getId();
            boolean ownerRenamed = owner != null && ownerId != null && ownerId.equals(mageId) && !owner.equals(mageName);

            if (ownerId == null || ownerId.length() == 0 || owner == null || ownerRenamed)
            {
                takeOwnership(mage.getPlayer());
                needsSave = true;
            }
        }

        if (needsSave) {
            saveState();
        }
        updateActiveMaterial();
        updateName();
        updateLore();

        // Play activate FX
        playPassiveEffects("activate");

        lastSoundEffect = 0;
        lastParticleEffect = 0;
        lastSpellCast = 0;
        if (forceUpdate) {
            CompatibilityLib.getDeprecatedUtils().updateInventory(player);
        }

        return true;
    }

    // This should be used sparingly, if at all... currently only
    // used when applying an upgrade to a wand while not held
    public void setMage(Mage mage) {
        this.mage = mage;
    }

    private boolean setMage(Mage mage, boolean offhand, Player player) {
        this.mage = mage;
        if (mageClassKeys != null && !mageClassKeys.isEmpty()) {
            MageClass mageClass = null;

            for (String mageClassKey : mageClassKeys) {
                mageClass = mage.getClass(mageClassKey);
                if (mageClass != null) break;
            }

            if (mageClass == null) {
                if (player != null) {
                    Integer lastSlot = mage.getLastActivatedSlot();
                    if (!offhand && (lastSlot == null || lastSlot != player.getInventory().getHeldItemSlot())) {
                        mage.setLastActivatedSlot(player.getInventory().getHeldItemSlot());
                        mage.sendMessage(controller.getMessages().get("mage.no_class").replace("$name", getName()));
                    }
                }
                return false;
            }
            setMageClass(mageClass);
            if (!offhand && player != null) {
                mage.setActiveClass(mageClass.getKey());
            }
        }

        MageParameters wrapped = new MageParameters(mage, "Wand " + getTemplateKey());
        wrapped.wrap(configuration);
        load(wrapped);

        // This double-load here is not really ideal.
        // Seems hard to prevent without merging Wand construction and activation, though.
        loadProperties();
        updateItem();
        return true;
    }

    public void updateItem() {
        // Add vanilla attributes
        CompatibilityLib.getInventoryUtils().applyAttributes(item, getConfigurationSection("item_attributes"), getString("item_attribute_slot", getString("attribute_slot")));

        // Add unstashable, unmoveable, etc tags
        if (getBoolean("unswappable")) {
            CompatibilityLib.getNBTUtils().setBoolean(item, "unswappable", true);
        } else {
            CompatibilityLib.getNBTUtils().removeMeta(item, "unswappable");
        }
        if (getBoolean("unstashable") || (undroppable && Unstashable)) {
            CompatibilityLib.getNBTUtils().setBoolean(item, "unstashable", true);
        } else {
            CompatibilityLib.getNBTUtils().removeMeta(item, "unstashable");
        }

        if (getBoolean("craftable")) {
            CompatibilityLib.getNBTUtils().setBoolean(item, "craftable", true);
        } else {
            CompatibilityLib.getNBTUtils().removeMeta(item, "craftable");
        }
        if (getBoolean("unmoveable")) {
            CompatibilityLib.getNBTUtils().setBoolean(item, "unmoveable", true);
        } else {
            CompatibilityLib.getNBTUtils().removeMeta(item, "unmoveable");
        }
        if (undroppable) {
            CompatibilityLib.getNBTUtils().setBoolean(item, "undroppable", true);
        } else {
            CompatibilityLib.getNBTUtils().removeMeta(item, "undroppable");
        }
        if (keep) {
            CompatibilityLib.getNBTUtils().setBoolean(item, "keep", true);
        } else {
            CompatibilityLib.getNBTUtils().removeMeta(item, "keep");
        }

        // Add vanilla enchantments
        ConfigurationSection enchantments = getConfigurationSection("enchantments");
        if (enchantments == null) {
            List<String> enchantmentList = getStringList("enchantments");
            if (enchantmentList != null && !enchantmentList.isEmpty()) {
                enchantments = ConfigurationUtils.newConfigurationSection();
                for (String enchantKey : enchantmentList) {
                    int level = 1;
                    String[] pieces = StringUtils.split(enchantKey, ":");
                    if (pieces.length > 1) {
                        try {
                            level = Integer.parseInt(pieces[1]);
                            enchantKey = pieces[0];
                        } catch (Exception ex) {
                            controller.getLogger().warning("Invalid enchantment level: " + enchantKey);
                            continue;
                        }
                    }
                    enchantments.set(enchantKey, level);
                }
            }
        }
        CompatibilityLib.getInventoryUtils().applyEnchantments(item, enchantments);

        // Add enchantment glow
        if (enchantments == null || enchantments.getKeys(false).isEmpty()) {
            if (glow) {
                CompatibilityLib.getItemUtils().addGlow(item);
            } else {
                CompatibilityLib.getItemUtils().removeGlow(item);
            }
        }
    }

    private void replaceTemplate(String newTemplate) {
        playEffects("replace");
        setTemplate(newTemplate);
        clearProperty("icon");
        loadProperties();
        saveState();
    }

    public boolean checkInventoryForUpgrades() {
        boolean updated = false;
        Player player = mage == null ? null : mage.getPlayer();
        if (player == null || mage.hasStoredInventory()) return false;

        // Check for spell or other special icons in the player's inventory
        Inventory inventory = player.getInventory();
        ItemStack[] items = inventory.getContents();
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (addItem(item)) {
                inventory.setItem(i, null);
                updated = true;
            }
        }

        return updated;
    }

    private void setOpenInventoryPage(int page) {
        this.openInventoryPage = page;
        this.setProperty("page", page);
    }

    @Override
    public boolean organizeInventory() {
        if (mage != null) {
            return organizeInventory(mage);
        }
        return false;
    }

    @Override
    public boolean organizeInventory(com.elmakers.mine.bukkit.api.magic.Mage mage) {
        WandOrganizer organizer = new WandOrganizer(this, mage);
        closeInventory();
        organizer.organize();
        setOpenInventoryPage(0);
        setCurrentHotbar(currentHotbar);
        if (autoOrganize) setProperty("organize", false);
        autoOrganize = false;
        updateSpellInventory();
        updateBrushInventory();
        if (this.mage != null) {
            buildInventory();
        }
        return true;
    }

    @Override
    public boolean alphabetizeInventory() {
        WandOrganizer organizer = new WandOrganizer(this);
        closeInventory();
        organizer.alphabetize();
        setOpenInventoryPage(0);
        setCurrentHotbar(0);
        if (autoAlphabetize) setProperty("alphabetize", false);
        autoAlphabetize = false;
        updateSpellInventory();
        updateBrushInventory();
        if (mage != null) {
            buildInventory();
        }
        return true;
    }

    @Override
    public com.elmakers.mine.bukkit.api.wand.Wand duplicate() {
        ItemStack newItem = CompatibilityLib.getItemUtils().getCopy(item);
        Wand newWand = controller.getWand(newItem);
        newWand.saveState();
        return newWand;
    }

    @Override
    @Deprecated
    public boolean configure(Map<String, Object> properties) {
        Map<Object, Object> convertedProperties = new HashMap<>(properties);
        configure(ConfigurationUtils.convertConfigurationSection(convertedProperties));
        return true;
    }

    @Override
    public void preUpdate() {
        updateSpellInventory();
        updateBrushInventory();
    }

    @Override
    public void updated() {
        if (wandContext != null) {
            wandContext.cancelEffects();
            wandContext = null;
        }

        if (isInventoryOpen()) {
            saveInventory();
            updateSpellInventory();
        }
        saveState();

        // This will reload properties from the item, so make sure to save state first
        super.updated();

        if (mage != null) {
            buildInventory();
            if (isInventoryOpen()) {
                updateInventory();
            }
        }

        updateMaxMana(false);
        updateName();
        updateLore();
        if (mage != null) {
            playPassiveEffects("activate");
        }
    }

    @Override
    public boolean isLocked() {
        return this.locked;
    }

    @Override
    public boolean upgradesAllowed() {
        return this.modifiable;
    }

    @Override
    @Deprecated
    public void unlock() {
        makeModifiable();
    }

    @Override
    public void makeModifiable() {
        modifiable = true;
        setProperty("modifiable", true);
    }

    public boolean isWorn() {
        return worn;
    }

    @Override
    public boolean canUse(Player player) {
        if (locked) {
            Mage mage = controller.getMage(player);
            if (!mage.canUse(getTemplateKey())) {
                return false;
            }
        }
        if (!bound || ownerId == null || ownerId.length() == 0) return true;
        if (controller.hasPermission(player, "magic.wand.override_bind")) return true;

        String playerId = controller.getMageIdentifier().fromEntity(player);
        if (ownerId.equalsIgnoreCase(playerId)) {
            return true;
        }

        // Fall back to checking the UUID rather than the mage ID
        // This can be removed when all AMC wands have been migrated
        return ownerId.equals(player.getUniqueId().toString());
    }

    @Override
    public boolean addSpell(String spellName) {
        if (!isModifiable()) return false;
        return super.addSpell(spellName);
    }

    @Override
    public boolean forceAddSpell(String spellName) {
        SpellTemplate template = controller.getSpellTemplate(spellName);
        if (template == null) {
            return false;
        }
        SpellKey spellKey = template.getSpellKey();
        if (limitSpellsToPath) {
            WandUpgradePath path = getPath();
            if (path != null && !path.containsSpell(spellKey.getBaseKey())) return false;
        }
        checkSpellLevelsAndInventory();
        int inventoryCount = inventories.size();
        int spellCount = spells.size();
        if (!super.forceAddSpell(spellName)) {
            return false;
        }
        saveInventory();

        ItemStack spellItem = createSpellItem(spellKey.getKey());
        if (spellItem == null) {
            return false;
        }
        int level = spellKey.getLevel();

        // Look for existing spells for spell upgrades
        Integer inventorySlot = spellInventory.get(spellKey.getBaseKey());
        clearSlot(inventorySlot);

        setSpellLevel(spellKey.getBaseKey(), level);
        spells.add(spellKey.getBaseKey());

        if (activeSpell == null || activeSpell.isEmpty()) {
            setActiveSpell(spellKey.getBaseKey());
        }

        addToInventory(spellItem, inventorySlot);
        checkSpellLevelsAndInventory();
        updateInventory();
        updateHasInventory();
        saveState();
        updateLore();

        if (mage != null)
        {
            if (spells.size() != spellCount) {
                // Did we get another page?
                if (inventoryCount == 1 && inventories.size() > 1) {
                    startWandInstructions();
                    showSpellInstructions();
                    showPageInstructions();
                    endWandInstructions();
                } else if (spellCount == 1) {
                    // We got a second spell
                    startWandInstructions();
                    showSecondSpellInstructions(spells.size());
                    endWandInstructions();
                } else if (spellCount == 8) {
                    // We got a second hotbar of spells
                    startWandInstructions();
                    showNinthSpellInstructions(spells.size());
                    endWandInstructions();
                }
            }
        }

        return true;
    }

    @Nonnull
    public String getInventoryKey(String def) {
        String key = getInventoryKey();
        return key == null ? def : key;
    }

    @Nullable
    private String getInventoryKey() {
        switch (getMode()) {
            case CYCLE:
                return getControlKey(WandAction.CYCLE);
            default:
                return getControlKey(WandAction.TOGGLE);
        }
    }

    @Nullable
    private String getControlMessage(int spellCount) {
        String inventoryMessage = null;
        switch (getMode()) {
        case INVENTORY:
            if (spellCount < 2) return null;
            inventoryMessage = "inventory_instructions";
            break;
        case CHEST:
            if (spellCount < 2) return null;
            inventoryMessage = "chest_instructions";
            break;
        case SKILLS:
            inventoryMessage = "skills_instructions";
            break;
        case CYCLE:
            if (spellCount < 2) return null;
            inventoryMessage = "cycle_instructions";
            break;
        case CAST:
        case NONE:
            // Ignore
            break;
        }
        return inventoryMessage;
    }

    /**
     * Covers the special case of a wand having spell levels and inventory slots that came from configs,
     * but now we've modified the spells list and need to figure out if we also need to persist the levels and
     * slots separately.
     *
     * <p>This should all be moved to CasterProperties at some point to handle the same sort of issues with mage class
     * configs.
     */
    private void checkSpellLevelsAndInventory() {
        if (!spellLevels.isEmpty()) {
            MagicProperties storage = getStorage("spell_levels");
            if (storage == null || storage == this) {
                if (!configuration.contains("spell_levels")) {
                    configuration.set("spell_levels", spellLevels);
                }
            }
        }
        if (!spellInventory.isEmpty()) {
            MagicProperties storage = getStorage("spell_inventory");
            if (storage == null || storage == this) {
                if (!configuration.contains("spell_inventory")) {
                    configuration.set("spell_inventory", spellInventory);
                }
            }
        }
    }

    private void clearSlot(Integer slot) {
        if (slot != null) {
            WandInventory inventory = getInventory(slot);
            slot = getInventorySlot(slot);
            inventory.setItem(slot, null);
        }
    }

    @Override
    public String getMessage(String messageKey, String defaultValue) {
        String message = super.getMessage(messageKey, defaultValue);

        // Some special-casing here, not sure how to avoid.
        if (messageKey.equals("hotbar_count_usage")) {
            String controlKey = getControlKey(WandAction.CYCLE_HOTBAR);
            if (controlKey != null) {
                controlKey = controller.getMessages().get("controls." + controlKey);
                message = message.replace("$cycle_hotbar", controlKey);
            } else {
                return "";
            }
        }
        return message;
    }

    @Override
    protected String getMessageKey(String key) {
        WandTemplate template = getTemplate();
        String templateKey = template == null ? null : template.getMessageKey(key, controller);
        if (templateKey != null) {
            return templateKey;
        }
        // For performance reasons we will only look one level up
        template = template == null ? null : template.getParent();
        templateKey = template == null ? null : template.getMessageKey(key, controller);
        if (templateKey != null) {
            return templateKey;
        }

        return "wand." + key;
    }

    @Override
    protected String parameterizeMessage(String message) {
        // TODO: Should this route to paramterize() ?
        return message.replace("$wand", getName());
    }

    @Override
    public boolean hasBrush(String materialKey) {
        if (limitBrushesToPath) {
            WandUpgradePath path = getPath();
            if (path != null && !path.containsBrush(materialKey)) return false;
        }
        return getBrushes().contains(materialKey);
    }

    @Override
    public boolean hasSpell(String spellName) {
        return hasSpell(new SpellKey(spellName));
    }

    @Override
    public boolean hasSpell(SpellKey spellKey) {
        if (!spells.contains(spellKey.getBaseKey())) return false;
        if (limitSpellsToPath) {
            WandUpgradePath path = getPath();
            if (path != null && !path.containsSpell(spellKey.getBaseKey())) return false;
        }
        int level = getSpellLevel(spellKey.getBaseKey());
        return (level >= spellKey.getLevel());
    }

    @Override
    public boolean addBrush(String materialKey) {
        if (!isModifiable()) return false;

        if (limitBrushesToPath) {
            WandUpgradePath path = getPath();
            if (path != null && !path.containsBrush(materialKey)) return false;
        }

        if (!super.addBrush(materialKey)) {
            return false;
        }

        saveInventory();

        ItemStack itemStack = createBrushIcon(materialKey);
        if (itemStack == null) return false;

        int inventoryCount = inventories.size();
        int brushCount = brushes.size();

        brushInventory.put(materialKey, null);
        brushes.add(materialKey);
        addToInventory(itemStack);
        if (activeBrush == null || activeBrush.length() == 0) {
            activateBrush(materialKey);
        } else {
            updateInventory();
        }
        updateHasInventory();
        saveState();
        updateLore();

        if (mage != null) {
            // This is a little hackily tied to the send method logic
            // This will also cause the spell instructions to get shown again when you get a new page
            // But I think that's an OK reminder
            boolean sendInstructions = (brushCount == 0) || (inventoryCount == 1 && inventories.size() > 1);
            if (sendInstructions) {
                startWandInstructions();
                showBrushInstructions();
                showPageInstructions();
                endWandInstructions();
            }
        }

        return true;
    }

    @Override
    public void setActiveBrush(String materialKey) {
        activateBrush(materialKey);
        if (materialKey == null || mage == null) {
            return;
        }

        com.elmakers.mine.bukkit.api.block.MaterialBrush brush = mage.getBrush();
        if (brush == null) {
            return;
        }

        boolean eraseWasActive = brush.isEraseModifierActive();
        brush.activate(mage.getLocation(), materialKey);

        BrushMode mode = brush.getMode();
        if (mode == BrushMode.CLONE) {
            mage.sendMessage(getMessage("clone_material_activated"));
        } else if (mode == BrushMode.REPLICATE) {
            mage.sendMessage(getMessage("replicate_material_activated"));
        }
        if (!eraseWasActive && brush.isEraseModifierActive()) {
            mage.sendMessage(getMessage("erase_modifier_activated"));
        }
    }

    public void setActiveBrush(ItemStack itemStack) {
        if (!isBrush(itemStack)) return;
        setActiveBrush(getBrush(itemStack));
    }

    public void activateBrush(String materialKey) {
        this.activeBrush = materialKey;
        setProperty("active_brush", this.activeBrush);
        saveState();
        updateActiveMaterial();
        updateName();
        updateHotbar();
    }

    @Override
    public void setActiveSpell(String activeSpell) {
        if (activeSpell != null) {
            SpellKey spellKey = new SpellKey(activeSpell);
            this.activeSpell = spellKey.getBaseKey();
        } else {
            this.activeSpell = null;
        }
        checkActiveSpell();
        setProperty("active_spell", this.activeSpell);
        saveState();
        updateName();
    }

    protected void checkActiveSpell() {
        // Support wands with just an active spell and no spells list
        if (activeSpell != null && !spells.isEmpty() && !spells.contains(activeSpell)) {
            activeSpell = null;
        }
    }

    @Override
    public boolean removeBrush(String materialKey) {
        if (!isModifiable() || materialKey == null) return false;
        if (limitBrushesToPath) {
            WandUpgradePath path = getPath();
            if (path != null && !path.containsBrush(materialKey)) return false;
        }

        if (!super.removeBrush(materialKey)) {
            return false;
        }

        saveInventory();
        if (materialKey.equals(activeBrush)) {
            activeBrush = null;
        }
        clearSlot(brushInventory.get(materialKey));
        brushInventory.remove(materialKey);
        boolean found = brushes.remove(materialKey);
        if (activeBrush == null && brushes.size() > 0) {
            activeBrush = brushes.iterator().next();
        }
        updateActiveMaterial();
        updateInventory();
        updateBrushInventory();
        saveState();
        updateName();
        updateLore();
        return found;
    }

    @Override
    public boolean removeSpell(String spellName) {
        if (!isModifiable()) return false;

        SpellKey spellKey = new SpellKey(spellName);
        if (limitSpellsToPath) {
            WandUpgradePath path = getPath();
            if (path != null && !path.containsSpell(spellKey.getBaseKey())) return false;
        }

        if (!super.removeSpell(spellName)) {
            return false;
        }

        saveInventory();
        if (activeSpell != null) {
            SpellKey activeKey = new SpellKey(activeSpell);
            if (spellKey.getBaseKey().equals(activeKey.getBaseKey())) {
                setActiveSpell(null);
            }
        }
        clearSlot(spellInventory.get(spellKey.getBaseKey()));
        spells.remove(spellKey.getBaseKey());
        spellLevels.remove(spellKey.getBaseKey());
        spellInventory.remove(spellKey.getBaseKey());
        if (activeSpell == null && spells.size() > 0) {
            setActiveSpell(spells.iterator().next());
        }

        checkSpellLevelsAndInventory();
        updateInventory();
        updateHasInventory();
        updateSpellInventory();
        saveState();
        updateName();
        updateLore();

        return true;
    }

    public boolean hasStoredInventory() {
        return storedInventory != null;
    }

    public Inventory getStoredInventory() {
        return storedInventory;
    }

    public boolean addToStoredInventory(ItemStack item) {
        if (storedInventory == null) {
            return false;
        }

        HashMap<Integer, ItemStack> remainder = storedInventory.addItem(item);
        return remainder.size() == 0;
    }

    public boolean storeInventory() {
        if (storedInventory != null) {
            if (mage != null) {
                mage.sendMessage("Your wand contains a previously stored inventory and will not activate, let go of it to clear.");
            }
            controller.getLogger().warning("Tried to store an inventory with one already present: " + (mage == null ? "?" : mage.getName()));
            return false;
        }

        Player player = mage.getPlayer();
        if (player == null) {
            return false;
        }
        PlayerInventory inventory = player.getInventory();
        storedInventory = CompatibilityLib.getCompatibilityUtils().createInventory(null, PLAYER_INVENTORY_SIZE, "Stored Inventory");
        for (int i = 0; i < PLAYER_INVENTORY_SIZE; i++) {
            ItemStack item = inventory.getItem(i);
            storedInventory.setItem(i, item);
            if (i != heldSlot) {
                inventory.setItem(i, null);
            }
        }

        return true;
    }

    public boolean restoreInventory() {
        if (storedInventory == null) {
            return false;
        }
        Player player = mage.getPlayer();
        if (player == null) {
            return false;
        }

        PlayerInventory inventory = player.getInventory();
        for (int i = 0; i < storedInventory.getSize(); i++) {
            ItemStack storedItem = storedInventory.getItem(i);
            // This works around a pick block exploit by replacing the block (which ends up being a spell) that was
            // picked with the wand item.
            // Otherwise normally we don't replace the wand item itself in case it was modified since being stored
            if (i != heldSlot || isSpell(inventory.getItem(i))) {
                inventory.setItem(i, storedItem);
            }
        }
        storedInventory = null;
        inventory.setHeldItemSlot(heldSlot);

        return true;
    }

    @Override
    @Deprecated
    public boolean isSoul() {
        return false;
    }

    public static boolean isBound(ItemStack item) {
        Object wandSection = CompatibilityLib.getNBTUtils().getTag(item, WAND_KEY);
        if (wandSection == null) return false;

        String boundValue = CompatibilityLib.getNBTUtils().getString(wandSection, "owner_id");
        return boundValue != null;
    }

    @Override
    public boolean isBound() {
        return bound;
    }

    @Nullable
    @Override
    public SpellTemplate getSpellTemplate(String spellKey) {
        SpellKey key = new SpellKey(spellKey);
        spellKey = key.getBaseKey();
        if (!spells.contains(spellKey)) return null;
        Integer level = spellLevels.get(spellKey);
        if (level != null) {
            spellKey = new SpellKey(spellKey, level).getKey();
        }
        return controller.getSpellTemplate(spellKey);
    }

    @Override
    public boolean setSpellLevel(String spellKey, int level) {
        boolean modified;
        if (level <= 1) {
            modified = spellLevels.containsKey(spellKey);
            spellLevels.remove(spellKey);
        } else {
            modified = !spellLevels.containsKey(spellKey) || spellLevels.get(spellKey) != level;
            spellLevels.put(spellKey, level);
        }
        return modified;
    }

    @Override
    public int getSpellLevel(String spellKey) {
        Integer level = spellLevels.get(spellKey);
        return level == null ? 1 : level;
    }

    @Override
    public MageController getController() {
        return controller;
    }

    @Override
    public Map<String, Integer> getSpellInventory() {
        return new HashMap<>(spellInventory);
    }

    @Override
    public Map<String, Integer> getBrushInventory() {
        return new HashMap<>(brushInventory);
    }

    @Override
    public float getHealthRegeneration() {
        Integer level = getPotionEffects().get(PotionEffectType.REGENERATION);
        return level != null && level > 0 ? (float)level : 0;
    }

    @Override
    public float getHungerRegeneration()  {
        Integer level = getPotionEffects().get(PotionEffectType.SATURATION);
        return level != null && level > 0 ? (float)level : 0;
    }

    @Nullable
    @Override
    public WandTemplate getTemplate() {
        if (template == null || template.isEmpty()) return null;
        return controller.getWandTemplate(template);
    }

    public boolean playPassiveEffects(String effects) {
        WandTemplate wandTemplate = getTemplate();
        if (wandTemplate != null && mage != null) {
            boolean offhandActive = mage.setOffhandActive(isInOffhand);
            boolean result = false;
            try {
                result = wandTemplate.playEffects(this, effects);
            } catch (Exception ex) {
                result = false;
                controller.getLogger().log(Level.WARNING, "Error playing effects " + effects + " from wand " + template, ex);
            }
            mage.setOffhandActive(offhandActive);
            return result;
        }
        return false;
    }

    @Override
    public boolean playEffects(String effects) {
        if (activeEffectsOnly && !inventoryIsOpen) {
             return false;
        }
        return playPassiveEffects(effects);
    }

    @Override
    public WandAction getDropAction() {
        return dropSneakAction != WandAction.NONE && mage != null && mage.isSneaking() ? dropSneakAction : dropAction;
    }

    @Override
    public WandAction getRightClickAction() {
        return rightClickSneakAction != WandAction.NONE && mage != null && mage.isSneaking() ? rightClickSneakAction : rightClickAction;
    }

    @Override
    public WandAction getLeftClickAction() {
        return leftClickSneakAction != WandAction.NONE && mage != null && mage.isSneaking() ? leftClickSneakAction : leftClickAction;
    }

    @Override
    public WandAction getSwapAction() {
        return swapSneakAction != WandAction.NONE && mage != null && mage.isSneaking() ? swapSneakAction : swapAction;
    }

    public WandAction getNoBowpullAction() {
        return noBowpullSneakAction != WandAction.NONE && mage != null && mage.isSneaking() ? noBowpullSneakAction : noBowpullAction;
    }

    @Override
    public boolean performAction(WandAction action) {
        WandMode mode = getMode();
        switch (action) {
            case CAST:
                cast();
                break;
            case ALT_CAST:
                alternateCast(0);
                break;
            case ALT_CAST2:
                alternateCast(1);
                break;
            case ALT_CAST3:
                alternateCast(2);
                break;
            case ALT_CAST4:
                alternateCast(3);
                break;
            case ALT_CAST5:
                alternateCast(4);
                break;
            case ALT_CAST6:
                alternateCast(5);
                break;
            case ALT_CAST7:
                alternateCast(6);
                break;
            case TOGGLE:
                if (mode == WandMode.CYCLE) {
                    // TODO: Config-drive this special casing?
                    if (mage != null && mage.isSneaking()) {
                        cycleSpells(-1);
                    } else {
                        cycleSpells(1);
                    }
                    return true;
                }
                if (mode != WandMode.CHEST && mode != WandMode.INVENTORY && mode != WandMode.SKILLS) return false;
                toggleInventory();
                break;
            case CYCLE:
                cycleSpells(1);
                break;
            case CYCLE_REVERSE:
                cycleSpells(-1);
                break;
            case CYCLE_BRUSH:
                cycleBrushes(1);
                break;
            case CYCLE_BRUSH_REVERSE:
                cycleBrushes(-1);
                break;
            case CYCLE_ACTIVE_HOTBAR:
                return cycleActiveHotbar(1);
            case CYCLE_ACTIVE_HOTBAR_REVERSE:
                return cycleActiveHotbar(-1);
            case CYCLE_HOTBAR:
                return cycleHotbar(1);
            case CYCLE_HOTBAR_REVERSE:
                return cycleHotbar(-1);
            case REPLACE:
                // Check for replacement template
                String replacementTemplate = getString("replacement", "");
                if (replacementTemplate.isEmpty() || replacementTemplate.equals(template)) {
                    return false;
                }
                replaceTemplate(replacementTemplate);
                if (mage != null) {
                    Mage mage = this.mage;
                    deactivate();
                    mage.checkWandNextTick();
                }
                break;
            case CANCEL:
                // Just here to return true
                break;
            default:
                return false;
        }

        return true;
    }

    @Override
    public boolean hasUpgrade() {
        WandUpgradePath path = getPath();
        return path != null && path.hasUpgrade();
    }

    @Override
    public boolean checkUpgrade(boolean quiet) {
        WandUpgradePath path = getPath();
        return path == null || !path.hasUpgrade() ? false : path.checkUpgradeRequirements(this, quiet ? null : mage);
    }

    @Override
    @Deprecated
    public boolean upgrade(Map<String, Object> properties) {
        Map<Object, Object> convertedProperties = new HashMap<>(properties);
        return upgrade(ConfigurationUtils.convertConfigurationSection(convertedProperties));
    }

    @Override
    public boolean upgrade(boolean quiet) {
        WandUpgradePath path = getPath();
        if (path == null) return false;
        path.upgrade(this, quiet ? null : mage);
        return true;
    }

    @Override
    public boolean isBlocked(double angle) {
        if (mage == null) return false;
        if (blockChance == 0) return false;
        if (blockFOV > 0 && angle > blockFOV) return false;
        long lastBlock = mage.getLastBlockTime();
        if (blockCooldown > 0 && lastBlock > 0 && lastBlock + blockCooldown > System.currentTimeMillis()) return false;
        boolean isBlocked = Math.random() <= blockChance;
        if (isBlocked) {
            playEffects("spell_blocked");
            mage.setLastBlockTime(System.currentTimeMillis());
        }
        return isBlocked;
    }

    @Override
    public boolean isReflected(double angle) {
        if (mage == null) return false;
        if (blockReflectChance == 0) return false;
        if (blockFOV > 0 && angle > blockFOV) return false;
        long lastBlock = mage.getLastBlockTime();
        if (blockCooldown > 0 && lastBlock > 0 && lastBlock + blockCooldown > System.currentTimeMillis()) return false;
        boolean isReflected = Math.random() <= blockReflectChance;
        if (isReflected) {
            playEffects("spell_reflected");
            if (mage != null) mage.setLastBlockTime(System.currentTimeMillis());
        }
        return isReflected;
    }

    @Nullable
    @Override
    public Location getLocation() {
        if (mage == null) {
            return null;
        }
        // Vive support
        Player player = mage.getPlayer();
        if (player != null && CompatibilityConstants.USE_METADATA_LOCATIONS) {
            boolean leftHand = isInOffhand;
            if (player.getMainHand() == MainHand.LEFT) {
                leftHand = !leftHand;
            }
            Location handLocation = BukkitMetadataUtils.getLocation(player, leftHand ? "lefthand.pos" : "righthand.pos");
            if (handLocation != null) {
                Vector offset = castLocation == null ? DEFAULT_CAST_OFFSET : castLocation;
                // This is a little hacky, but we are going to only use the y-component of the configured offset,
                // accounting for it normally being about a half a block higher than hands, based at eye location
                handLocation = handLocation.clone();
                handLocation.setY(handLocation.getY() + offset.getY() + 0.5);
                return handLocation;
            }
        }
        Location wandLocation = mage.getEyeLocation();
        wandLocation = mage.getOffsetLocation(wandLocation, isInOffhand, castLocation == null ? DEFAULT_CAST_OFFSET : castLocation);
        return wandLocation;
    }

    @Nullable
    @Override
    public Mage getMage() {
        return mage;
    }

    @Override
    public @Nullable MageClass getMageClass() {
        return mageClass;
    }

    @Override
    public @Nullable String getMageClassKey() {
        if (mageClass != null) {
            return mageClass.getKey();
        }
        return mageClassKeys == null || mageClassKeys.isEmpty() ? null : mageClassKeys.get(0);
    }

    @Override
    public void setCurrentHotbar(int hotbar) {
        this.currentHotbar = hotbar;
        setProperty("hotbar", currentHotbar);
    }

    @Override
    public int getCurrentHotbar() {
        return currentHotbar;
    }

    public int getInventorySize() {
        WandMode mode = getMode();
        if (mode == WandMode.CHEST || mode == WandMode.SKILLS) {
            return CHEST_ITEMS_PER_ROW * inventoryRows;
        }
        return INVENTORY_SIZE;
    }

    @Override
    public int getHeldSlot() {
        return heldSlot;
    }

    @Override
    public void setHeldSlot(int slot) {
        this.heldSlot = slot;
    }

    @Nullable
    @Override
    public BaseMagicConfigurable getStorage(MagicPropertyType propertyType) {
        switch (propertyType) {
            case WAND: return this;
            case SUBCLASS: return mageClass;
            case CLASS:
                return mageClass == null ? null : mageClass.getRoot();
            case MAGE:
                return mage == null ? null : mage.getProperties();
            default:
                return null;
        }
    }

    @Override
    public boolean isPlayer() {
        return mage == null ? false : mage.isPlayer();
    }

    @Nullable
    @Override
    public Player getPlayer() {
        return mage == null ? null : mage.getPlayer();
    }

    @Override
    @Nonnull
    public MageContext getContext() {
        checkState(mage != null, "Mage is not available");

        if (wandContext == null || (wandContext.getMage() != mage)) {
            // Lazy load or mage has changed
            wandContext = new WandContext(verifyNotNull(mage), this);
        }

        return verifyNotNull(wandContext);
    }

    @Override
    public Wand getWand() {
        return this;
    }

    @Override
    public boolean isInOffhand() {
        return isInOffhand;
    }

    public boolean hasWearable() {
        return hasProperty("wearable");
    }

    public boolean isWearableInSlot(int slotNumber) {
        if (isBoolean("wearable") && getBoolean("wearable")) return true;
        Collection<String> slots = getStringList("wearable");
        if (slots != null) {
            for (String slotKey : slots) {
                InventorySlot slot = InventorySlot.parse(slotKey);
                if (slot != null) {
                    if (slot.getSlot() == slotNumber) {
                        return true;
                    }
                } else {
                    controller.getLogger().warning("Invalid wearable slot: " + slotKey);
                }
            }
        }
        return false;
    }

    @Nullable
    public Collection<Integer> getWearableSlots() {
        List<Integer> slots = null;
        if (isBoolean("wearable") && getBoolean("wearable")) {
            slots = new ArrayList<>();
            for (InventorySlot slot : InventorySlot.values()) {
                if (slot.getSlot() >= 0) {
                    slots.add(slot.getSlot());
                }
            }
            return slots;
        }
        Collection<String> slotList = getStringList("wearable");
        if (slotList != null) {
            slots = new ArrayList<>();
            for (String slotKey : slotList) {
                InventorySlot slot = InventorySlot.parse(slotKey);
                if (slot != null) {
                    slots.add(slot.getSlot());
                } else {
                    controller.getLogger().warning("Invalid wearable slot: " + slotKey);
                }
            }
        }
        return slots;
    }

    public boolean tryToWear(Mage mage) {
        Player player = mage.getPlayer();
        if (player == null) return false;
        Collection<Integer> slots = getWearableSlots();
        if (slots != null) {
            for (int slot : slots) {
                ItemStack existing = player.getInventory().getItem(slot);
                if (CompatibilityLib.getItemUtils().isEmpty(existing)) {
                    deactivate();
                    player.getInventory().setItem(slot, getItem());
                    controller.onArmorUpdated(mage);
                    return true;
                }
            }
        }
        return false;
    }

    public static void addParameterKeys(MageController controller, Collection<String> options) {
        for (String key : PROPERTY_KEYS) {
            options.add(key);
        }

        for (String damageType : controller.getDamageTypes()) {
            options.add("protection." + damageType);
            options.add("strength." + damageType);
            options.add("weakness." + damageType);
        }
    }

    public static void addParameterValues(MageController controller, String key, Collection<String> options) {
        if (key.equals("effect_sound")) {
            Sound[] sounds = Sound.values();
            for (Sound sound : sounds) {
                options.add(sound.name().toLowerCase());
            }
        } else if (key.equals("effect_particle")) {
            for (Particle particleType : Particle.values()) {
                options.add(particleType.name().toLowerCase());
            }
        } else if (key.equals("mode")) {
            for (WandMode mode : WandMode.values()) {
                options.add(mode.name().toLowerCase());
            }
        } else if (key.equals("left_click") || key.equals("right_click")
                || key.equals("drop") || key.equals("swap")
                || key.equals("left_click_sneak") || key.equals("right_click_sneak")
                || key.equals("drop_sneak") || key.equals("swap_sneak")
                || key.equals("no_bowpull") || key.equals("no_bowpull_sneak")) {
            for (WandAction action : WandAction.values()) {
                options.add(action.name().toLowerCase());
            }
        } else if (key.equals("item_flags")) {
            for (ItemFlag flag : ItemFlag.values()) {
                options.add(flag.name().toLowerCase());
            }
        } else if (key.equals("boss_bar") || key.equals("xp_display") || key.equals("level_display")) {
            WandDisplayMode.addOptions(options);
        } else if (key.equals("action_bar")) {
            options.add("&cHP&7: &c@health&7/&c@health_max &f &f &f &f &f &f &bMP&7: &b@mana&7/&b@mana_max");
        }
    }

    public int getOrganizeBuffer() {
        return getInt("page_free_space", INVENTORY_ORGANIZE_BUFFER);
    }

    @Override
    public boolean isEnchantable() {
        return getBoolean("enchantable");
    }

    @Override
    public void setEnchantments(Map<Enchantment, Integer> enchants) {
        if (enchants == null) {
            setProperty("enchantments", null);
            if (item != null) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.getEnchants().clear();
                    item.setItemMeta(meta);
                }
            }
        } else {
            CompatibilityUtils compatibilityUtils = CompatibilityLib.getCompatibilityUtils();
            ConfigurationSection enchantments = ConfigurationUtils.newConfigurationSection();
            for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                enchantments.set(compatibilityUtils.getEnchantmentKey(entry.getKey()), entry.getValue());
            }
            setProperty("enchantments", enchantments);
            if (item != null) {
                CompatibilityLib.getInventoryUtils().applyEnchantments(item, enchantments);
            }
            saveState();
            updateLore();
        }
    }

    @Override
    public boolean addEnchantments(Map<Enchantment, Integer> enchants) {
        if (enchants == null || item == null) {
            return false;
        }

        CompatibilityUtils compatibilityUtils = CompatibilityLib.getCompatibilityUtils();
        List<String> enchantmentsAllowed = getStringList("allowed_enchantments");
        Set<Enchantment> allowed = null;
        if (enchantmentsAllowed != null && !enchantmentsAllowed.isEmpty()) {
            allowed = new HashSet<>();
            for (String enchantmentKey : enchantmentsAllowed) {
                Enchantment enchantment = compatibilityUtils.getEnchantmentByKey(enchantmentKey);
                if (enchantment != null) {
                    allowed.add(enchantment);
                }
            }
        }

        ConfigurationSection enchantments = ConfigurationUtils.newConfigurationSection();
        for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
            Enchantment enchantment = entry.getKey();
            if (allowed != null && !allowed.contains(enchantment)) continue;
            enchantments.set(compatibilityUtils.getEnchantmentKey(enchantment), entry.getValue());
        }
        if (enchantments.getKeys(false).isEmpty()) {
            return false;
        }
        if (CompatibilityLib.getInventoryUtils().addEnchantments(item, enchantments)) {
            enchantments = ConfigurationUtils.newConfigurationSection();
            Map<Enchantment, Integer> newEnchants = item.getItemMeta().getEnchants();
            for (Map.Entry<Enchantment, Integer> entry : newEnchants.entrySet()) {
                enchantments.set(compatibilityUtils.getEnchantmentKey(entry.getKey()), entry.getValue());
            }
            setProperty("enchantments", enchantments);
            saveState();
            updateLore();
            return true;
        }
        return false;
    }

    @Override
    @Nonnull
    public Map<Enchantment, Integer> getEnchantments() {
        Map<Enchantment, Integer> enchantMap = new HashMap<>();
        ConfigurationSection enchantConfig = getConfigurationSection("enchantments");
        if (enchantConfig != null) {
            CompatibilityUtils compatibilityUtils = CompatibilityLib.getCompatibilityUtils();
            Collection<String> enchantKeys = enchantConfig.getKeys(false);
            for (String enchantKey : enchantKeys) {
                try {
                    Enchantment enchantment = compatibilityUtils.getEnchantmentByKey(enchantKey);
                    if (enchantment == null) {
                        controller.getLogger().warning("Invalid enchantment in wand " + getTemplateKey() + ": " + enchantKey);
                    } else {
                        enchantMap.put(enchantment, enchantConfig.getInt(enchantKey));
                    }
                } catch (Exception ex) {
                    controller.getLogger().log(Level.SEVERE, "Could not add enchantment: " + enchantKey + " to wand " + getTemplateKey(), ex);
                }
            }
        }
        return enchantMap;
    }

    @Override
    public boolean hasProperty(String key) {
        if (slottedConfiguration != null && slottedConfiguration.contains(key)) {
            return true;
        }
        if (requirementConfiguration != null && requirementConfiguration.contains(key)) {
            return true;
        }
        return hasOwnProperty(key);
    }

    @Nullable
    @Override
    public ConfigurationSection getConfigurationSection(String key) {
        // Special override to handle merging slotted configurations
        ConfigurationSection base = super.getConfigurationSection(key);
        ConfigurationSection slotted = slottedConfiguration != null ? slottedConfiguration.getConfigurationSection(key) : null;
        ConfigurationSection requirement = requirementConfiguration != null ? requirementConfiguration.getConfigurationSection(key) : null;

        ConfigurationSection merged;
        if (slotted != null) {
            merged = slotted;
        } else if (requirement != null) {
            merged = requirement;
        } else {
            // Just return the base if we have no overrides
            return base;
        }

        // If we have both slotted and requirement, then we start with slotted and layer over requirement, not overriding
        if (slotted != null && requirement != null) {
            ConfigurationUtils.overlayConfigurations(merged, requirement);
        }
        // Finally if we have a base, that goes on last (again because not overridding)
        if (base != null) {
            ConfigurationUtils.overlayConfigurations(merged, base);
        }
        return merged;
    }

    @Override
    @Nullable
    public Object getProperty(String key) {
        return getPropertyConfiguration(key).get(key);
    }

    @Override
    @Nonnull
    public ConfigurationSection getPropertyConfiguration(String key) {
        // ConfigurationSections get merged in getConfigurationSection, we always need to return
        // the base in that case.
        if (requirementConfiguration != null && requirementConfiguration.contains(key) && !requirementConfiguration.isConfigurationSection(key)) {
            return requirementConfiguration;
        }
        if (slottedConfiguration != null && slottedConfiguration.contains(key) && !slottedConfiguration.isConfigurationSection(key)) {
            return slottedConfiguration;
        }
        return super.getPropertyConfiguration(key);
    }

    public boolean isInteractible(Block block) {
        if (block == null) {
            return false;
        }
        if (interactibleMaterials != null) {
            return interactibleMaterials.testBlock(block);
        }
        return controller.isInteractible(block);
    }

    public boolean isPlaceable() {
        return getBoolean("placeable");
    }

    public boolean isSwappable() {
        return swappable;
    }
}
