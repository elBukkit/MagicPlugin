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
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.block.BrushMode;
import com.elmakers.mine.bukkit.api.economy.Currency;
import com.elmakers.mine.bukkit.api.event.WandPreActivateEvent;
import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.magic.MageClassTemplate;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MagicProperties;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.api.spell.CostReducer;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellKey;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.WandAction;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.block.MaterialBrush;
import com.elmakers.mine.bukkit.effect.EffectPlayer;
import com.elmakers.mine.bukkit.effect.SoundEffect;
import com.elmakers.mine.bukkit.effect.WandEffectContext;
import com.elmakers.mine.bukkit.effect.builtin.EffectRing;
import com.elmakers.mine.bukkit.heroes.HeroesManager;
import com.elmakers.mine.bukkit.magic.BaseMagicConfigurable;
import com.elmakers.mine.bukkit.magic.Mage;
import com.elmakers.mine.bukkit.magic.MageClass;
import com.elmakers.mine.bukkit.magic.MageParameters;
import com.elmakers.mine.bukkit.magic.MagicAttribute;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.magic.MagicPropertyType;
import com.elmakers.mine.bukkit.utility.ColorHD;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import com.elmakers.mine.bukkit.utility.NMSUtils;

public class Wand extends WandProperties implements CostReducer, com.elmakers.mine.bukkit.api.wand.Wand {
    public static final int OFFHAND_SLOT = 40;
    public static final int INVENTORY_SIZE = 27;
    public static final int PLAYER_INVENTORY_SIZE = 36;
    public static final int INVENTORY_ORGANIZE_BUFFER = 4;
    public static final int HOTBAR_SIZE = 9;
    public static final int HOTBAR_INVENTORY_SIZE = HOTBAR_SIZE - 1;
    public static final float DEFAULT_SPELL_COLOR_MIX_WEIGHT = 0.0001f;
    public static boolean FILL_CREATOR = false;
    public static Vector DEFAULT_CAST_OFFSET = new Vector(0, 0, 0.5);
    public static String DEFAULT_WAND_TEMPLATE = "default";
    public static boolean CREATIVE_CHEST_MODE = false;

    private static final String[] EMPTY_PARAMETERS = new String[0];

    private static final Random random = new Random();

    /**
     * The item as it appears in the inventory of the player.
     */
    protected @Nullable ItemStack item;

    /**
     * The currently active mage.
     *
     * <p>Is only set when the wand is active or when the wand is
     * used for off-hand casting.
     */
    protected @Nullable Mage mage;
    protected @Nullable WandEffectContext effectContext;

    // Cached state
    private String id = "";
    private List<WandInventory> hotbars;
    private List<WandInventory> inventories;
    private Map<String, Integer> spellInventory = new HashMap<>();
    private Set<String> spells = new LinkedHashSet<>();
    private Map<String, Integer> spellLevels = new HashMap<>();
    private Map<String, Integer> brushInventory = new HashMap<>();
    private Set<String> brushes = new LinkedHashSet<>();

    private String activeSpell = "";
    private String alternateSpell = "";
    private String alternateSpell2 = "";
    private String activeBrush = "";
    protected String wandName = "";
    protected String description = "";
    private String owner = "";
    private String ownerId = "";
    private String template = "";
    private String path = "";
    private List<String> mageClassKeys = null;
    private boolean superProtected = false;
    private boolean superPowered = false;
    private boolean glow = false;
    private boolean bound = false;
    private boolean indestructible = false;
    private boolean undroppable = false;
    private boolean keep = false;
    private boolean passive = false;
    private boolean preuse = false;
    private boolean autoOrganize = false;
    private boolean autoAlphabetize = false;
    private boolean autoFill = false;
    private boolean isUpgrade = false;
    private boolean randomizeOnActivate = true;
    private boolean rename = false;
    private boolean renameDescription = false;
    private boolean quickCast = false;
    private boolean quickCastDisabled = false;
    private boolean manualQuickCastDisabled = false;
    private boolean isInOffhand = false;
    private boolean hasId = false;
    private boolean suspendUpdate = false;
    private int inventoryRows = 1;
    private Vector castLocation;

    private WandAction leftClickAction = WandAction.NONE;
    private WandAction rightClickAction = WandAction.NONE;
    private WandAction dropAction = WandAction.NONE;
    private WandAction swapAction = WandAction.NONE;

    private MaterialAndData icon = null;
    private MaterialAndData upgradeIcon = null;
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
    private boolean locked = false;
    private boolean lockedAllowUpgrades = false;
    private boolean forceUpgrade = false;
    private boolean isHeroes = false;
    private int uses = 0;
    private boolean hasUses = false;
    private boolean isSingleUse = false;
    private boolean limitSpellsToPath = false;
    private boolean limitBrushesToPath = false;
    private boolean resetManaOnActivate = false;
    private Currency currencyDisplay = null;

    private float manaPerDamage = 0;

    private ColorHD effectColor = null;
    private float effectColorSpellMixWeight = DEFAULT_SPELL_COLOR_MIX_WEIGHT;
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

    private Map<PotionEffectType, Integer> potionEffects = new HashMap<>();

    private SoundEffect effectSound = null;
    private int effectSoundInterval = 0;

    private int quietLevel = 0;
    private Map<String, String> castOverrides = null;

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
    public static WandManaMode currencyMode = WandManaMode.NUMBER;
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
    public static byte HIDE_FLAGS = 63;
    public static String brushSelectSpell = "";

    private Inventory storedInventory = null;
    private int heldSlot = 0;

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
            ConfigurationSection wandConfig = itemToConfig(item, new MemoryConfiguration());

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
            InventoryUtils.removeMeta(item, WAND_KEY);
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

    protected Wand(MagicController controller, String templateName) throws UnknownWandException {
        this(controller);

        // Default to "default" wand
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
        ConfigurationSection templateConfig = template.getConfiguration();

        if (templateConfig == null) {
            throw new UnknownWandException(templateName);
        }

        // Load all properties
        loadProperties();

        // Enchant, if an enchanting level was provided
        if (level > 0) {
            // Account for randomized locked wands
            boolean wasLocked = locked;
            locked = false;
            randomize(level, false, null, true);
            locked = wasLocked;
        }

        // Don't randomize now if set to randomize later
        // Otherwise, do this here so the description updates
        if (!randomizeOnActivate) {
            randomize();
        }

        updateName();
        updateLore();
        saveState();
    }

    public Wand(MagicController controller, Material icon, short iconData) {
        // This will make the Bukkit ItemStack into a real ItemStack with NBT data.
        this(controller, InventoryUtils.makeReal(new ItemStack(icon, 1, iconData)));
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
                    spellInventory = NMSUtils.getMap((ConfigurationSection)spellInventoryRaw);
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

    protected void updateHotbarCount() {
        int hotbarCount = Math.max(1, getInt("hotbar_count", 1));
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
            String templateIcon = template != null ? template.getProperty("icon", "") : null;
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
            item = InventoryUtils.makeReal(this.icon.getItemStack(1));
        }
        if (!icon.isValid()) {
            return;
        }

        Short durability = null;
        if (!indestructible && !isUpgrade && icon.getMaterial().getMaxDurability() > 0) {
            durability = item.getDurability();
        }

        try {
            if (inactiveIcon == null || (mage != null && getMode() == WandMode.INVENTORY && isInventoryOpen())) {
                icon.applyToItem(item);
            } else {
                inactiveIcon.applyToItem(item);
            }
        } catch (Exception ex) {
            controller.getLogger().log(Level.WARNING, "Unable to apply wand icon", ex);
            item.setType(DefaultWandMaterial);
        }

        if (durability != null) {
            item.setDurability(durability);
        }

        // Make indestructible
        // The isUpgrade checks here and above are for using custom icons in 1.9, this is a bit hacky.
        if ((indestructible || Unbreakable || isUpgrade) && !manaMode.useDurability()) {
            CompatibilityUtils.makeUnbreakable(item);
        } else {
            CompatibilityUtils.removeUnbreakable(item);
        }
        CompatibilityUtils.hideFlags(item, getProperty("hide_flags", HIDE_FLAGS));
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
            InventoryUtils.removeMeta(item, WAND_KEY);
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

    public boolean isModifiable() {
        return !locked;
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
        return item != null && InventoryUtils.hasMeta(item, UPGRADE_KEY);
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
            return passive ? reduction : stackPassiveProperty(reduction, costReduction * controller.getMaxCostReduction());
        }
        return costReduction;
    }

    @Override
    public float getCooldownReduction() {
        if (mage != null) {
            float reduction = mage.getCooldownReduction();
            return passive ? reduction : stackPassiveProperty(reduction, cooldownReduction * controller.getMaxCooldownReduction());
        }
        return cooldownReduction;
    }

    @Override
    public float getConsumeReduction() {
        if (mage != null) {
            float reduction = mage.getConsumeReduction();
            return passive ? reduction : stackPassiveProperty(reduction, consumeReduction);
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
        return ChatColor.translateAlternateColorCodes('&', wandName);
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
        long worth = 0;
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

    public void takeOwnership(Player player) {
        Mage mage = this.mage;
        if (mage == null) {
            mage = controller.getMage(player);
        }

        if ((ownerId == null || ownerId.length() == 0) && quietLevel < 2)
        {
            mage.sendMessage(getMessage("bound_instructions", "").replace("$wand", getName()));
            String spellKey = getActiveSpellKey();
            SpellTemplate spellTemplate = spellKey != null && !spellKey.isEmpty() ? controller.getSpellTemplate(spellKey) : null;
            if (spellTemplate != null)
            {
                String message = getMessage("spell_instructions", "").replace("$wand", getName());
                mage.sendMessage(message.replace("$spell", spellTemplate.getName()));
            }
            if (spells.size() > 1)
            {
                String controlKey = getControlKey(WandAction.TOGGLE);
                if (controlKey != null) {
                    controlKey = controller.getMessages().get("controls." + controlKey);
                    mage.sendMessage(getMessage("inventory_instructions", "")
                        .replace("$wand", getName()).replace("$toggle", controlKey));
                }
            }
            com.elmakers.mine.bukkit.api.wand.WandUpgradePath path = getPath();
            if (path != null)
            {
                String message = getMessage("enchant_instructions", "").replace("$wand", getName());
                mage.sendMessage(message);
            }
        }
        owner = ChatColor.stripColor(player.getDisplayName());
        ownerId = mage.getId();
        setProperty("owner", owner);
        setProperty("owner_id", ownerId);
        updateLore();
        saveState();
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

        WandMode mode = getMode();
        int fullSlot = 0;
        for (WandInventory inventory : checkInventories) {
            int inventorySize = inventory.getSize();
            Integer slot = null;
            int freeSpace = 0;
            for (int i = 0; i < inventorySize && freeSpace < INVENTORY_ORGANIZE_BUFFER; i++) {
                ItemStack existing = inventory.getItem(i);
                if (InventoryUtils.isEmpty(existing)) {
                    if (slot == null) {
                        slot = i;
                    }
                    freeSpace++;
                }
            }

            // Don't leave free space in hotbars
            if (slot != null && (freeSpace >= INVENTORY_ORGANIZE_BUFFER || inventorySize == HOTBAR_INVENTORY_SIZE || mode == WandMode.CHEST)) {
                added = true;
                inventory.setItem(slot, itemStack);
                fullSlot += slot;
                break;
            }
            fullSlot += inventory.getSize();
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
        WandMode brushMode = getBrushMode();
        for (String brushKey : brushes) {
            boolean addToInventory = brushMode == WandMode.INVENTORY || (MaterialBrush.isSpecialMaterialKey(brushKey) && !MaterialBrush.isSchematic(brushKey));
            if (addToInventory)
            {
                ItemStack itemStack = createBrushIcon(brushKey);
                if (itemStack == null) {
                    controller.getLogger().warning("Unable to create brush icon for key " + brushKey);
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
    }

    protected void parseSpells(String spellString) {
        // Support YML-List-As-String format
        // Maybe don't need this anymore since loading lists is now a separate path
        spellString = spellString.replaceAll("[\\]\\[]", "");
        String[] spellNames = StringUtils.split(spellString, ',');
        loadSpells(Arrays.asList(spellNames));
    }

    protected void clearSpells() {
        spellLevels.clear();
        spells.clear();
    }

    protected void loadSpells(Collection<String> spellKeys) {
        clearSpells();
        WandUpgradePath path = getPath();
        for (String spellName : spellKeys)
        {
            String[] pieces = StringUtils.split(spellName, '@');
            Integer slot = parseSlot(pieces);

            // Handle aliases and upgrades smoothly
            String loadedKey = pieces[0].trim();
            SpellKey spellKey = new SpellKey(loadedKey);
            SpellTemplate spell = controller.getSpellTemplate(loadedKey);

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
                if (slot != null) {
                    spellInventory.put(spellKey.getBaseKey(), slot);
                }
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
        if (spell == null) return null;
        String iconURL = spell.getIconURL();

        ItemStack itemStack = null;
        if (iconURL != null && (controller.isUrlIconsEnabled() || spell.getIcon() == null || !spell.getIcon().isValid() || spell.getIcon().getMaterial() == Material.AIR))
        {
            itemStack = controller.getURLSkull(iconURL);
        }

        if (itemStack == null)
        {
            ItemStack originalItemStack = null;
            com.elmakers.mine.bukkit.api.block.MaterialAndData icon = spell.getIcon();
            if (icon == null) {
                controller.getLogger().warning("Unable to create spell icon for " + spell.getName() + ", missing material");
                return null;
            }
            try {
                originalItemStack = new ItemStack(icon.getMaterial(), 1, icon.getData());
                itemStack = InventoryUtils.makeReal(originalItemStack);
            } catch (Exception ex) {
                itemStack = null;
            }

            if (itemStack == null) {
                if (icon.getMaterial() != Material.AIR) {
                    String iconName = icon.getName();
                    controller.getLogger().warning("Unable to create spell icon for " + spell.getKey() + " with material " + iconName);
                }
                return originalItemStack;
            }
        }
        InventoryUtils.makeUnbreakable(itemStack);
        InventoryUtils.hideFlags(itemStack, (byte)63);
        updateSpellItem(controller.getMessages(), itemStack, spell, args, mage, wand, wand == null ? null : wand.activeBrush, isItem);

        if (wand != null && wand.getMode() == WandMode.SKILLS && !isItem) {
            String mageClassKey = wand.getMageClassKey();
            ConfigurationSection skillsConfig = wand.getConfigurationSection("skills");
            InventoryUtils.configureSkillItem(itemStack, mageClassKey, skillsConfig);
        }

        return itemStack;
    }

    @Nullable
    protected ItemStack createBrushIcon(String materialKey) {
        return createBrushItem(materialKey, controller, this, false);
    }

    @Nullable
    public static ItemStack createBrushItem(String materialKey, com.elmakers.mine.bukkit.api.magic.MageController controller, Wand wand, boolean isItem) {
        MaterialBrush brushData = MaterialBrush.parseMaterialKey(materialKey);
        if (brushData == null) return null;

        ItemStack itemStack = brushData.getItem(controller, isItem);
        if (BrushGlow || (isItem && BrushItemGlow))
        {
            CompatibilityUtils.addGlow(itemStack);
        }
        InventoryUtils.makeUnbreakable(itemStack);
        InventoryUtils.hideFlags(itemStack, (byte)63);
        updateBrushItem(controller.getMessages(), itemStack, brushData, wand);
        return itemStack;
    }

    protected boolean findItem() {
        if (mage != null && item != null) {
            Player player = mage.getPlayer();
            if (player != null) {
                ItemStack itemInHand = player.getInventory().getItemInMainHand();
                if (itemInHand != null && !InventoryUtils.isSameInstance(itemInHand, item) && itemInHand.equals(item)) {
                    item = itemInHand;
                    isInOffhand = false;
                    return true;
                }
                itemInHand = player.getInventory().getItemInOffHand();
                if (itemInHand != null && !InventoryUtils.isSameInstance(itemInHand, item) && itemInHand.equals(item)) {
                    item = itemInHand;
                    isInOffhand = true;
                    return true;
                }

                itemInHand = player.getInventory().getItem(heldSlot);
                if (itemInHand != null && !InventoryUtils.isSameInstance(itemInHand, item) && itemInHand.equals(item)) {
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
            InventoryUtils.removeMeta(item, WAND_KEY);
        }

        Object wandNode = InventoryUtils.createNode(item, isUpgrade ? UPGRADE_KEY : WAND_KEY);
        if (wandNode == null) {
            controller.getLogger().warning("Failed to save wand state for wand to : " + item);
        } else {
            InventoryUtils.saveTagsToNBT(getConfiguration(), wandNode);
        }
    }

    @Nullable
    public static ConfigurationSection itemToConfig(ItemStack item, ConfigurationSection stateNode) {
        Object wandNode = InventoryUtils.getNode(item, WAND_KEY);

        if (wandNode == null) {
            wandNode = InventoryUtils.getNode(item, UPGRADE_KEY);
            if (wandNode == null) {
                return null;
            }
        }

        ConfigurationUtils.loadAllTagsFromNBT(stateNode, wandNode);

        return stateNode;
    }

    public static void configToItem(ConfigurationSection itemSection, ItemStack item) {
        ConfigurationSection stateNode = itemSection.getConfigurationSection("wand");
        Object wandNode = InventoryUtils.createNode(item, Wand.WAND_KEY);
        if (wandNode != null) {
            InventoryUtils.saveTagsToNBT(stateNode, wandNode);
        }
    }

    @Nullable
    protected String getPotionEffectString() {
        return getPotionEffectString(potionEffects);
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

    protected void updateBrushInventory(Map<String, Integer> updateBrushes) {
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

    protected void updateSpellInventory(Map<String, Integer> updateSpells) {
        for (Map.Entry<String, Integer> spellEntry : spellInventory.entrySet()) {
            String spellKey = spellEntry.getKey();
            Integer slot = updateSpells.get(spellKey);
            if (slot != null) {
                spellEntry.setValue(slot);
            }
        }
    }

    public void setEffectColor(String hexColor) {
        // Annoying config conversion issue :\
        if (hexColor.contains(".")) {
            hexColor = hexColor.substring(0, hexColor.indexOf('.'));
        }

        if (hexColor == null || hexColor.length() == 0 || hexColor.equals("none")) {
            effectColor = null;
            return;
        }
        effectColor = new ColorHD(hexColor);
        if (hexColor.equals("random")) {
            setProperty("effect_color", effectColor.toString());
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

        ItemData itemData = controller.getOrCreateItem(key);
        if (itemData == null) {
            return null;
        }

        com.elmakers.mine.bukkit.api.block.MaterialAndData materialData = itemData.getMaterialAndData();
        return materialData instanceof MaterialAndData ? (MaterialAndData)materialData : null;
    }

    @Override
    public void loadProperties() {
        super.loadProperties();
        locked = getBoolean("locked", locked);
        lockedAllowUpgrades = getBoolean("locked_allow_upgrades", false);
        consumeReduction = getFloat("consume_reduction");
        cooldownReduction = getFloat("cooldown_reduction");
        costReduction = getFloat("cost_reduction");
        power = getFloat("power");

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

        blockChance = getFloat("block_chance");
        blockReflectChance = getFloat("block_reflect_chance");
        blockFOV = getFloat("block_fov");
        blockMageCooldown = getInt("block_mage_cooldown");
        blockCooldown = getInt("block_cooldown");

        manaPerDamage = getFloat("mana_per_damage");
        earnMultiplier = getFloat("earn_multiplier", getFloat("sp_multiplier", 1));

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
            preuse = getBoolean("preuse", false);
        }

        // Convert some legacy properties to potion effects
        float healthRegeneration = getFloat("health_regeneration", 0);
        float hungerRegeneration = getFloat("hunger_regeneration", 0);
        float speedIncrease = getFloat("haste", 0);

        if (speedIncrease > 0) {
            potionEffects.put(PotionEffectType.SPEED, 1);
        }
        if (healthRegeneration > 0) {
            potionEffects.put(PotionEffectType.REGENERATION, 1);
        }
        if (hungerRegeneration > 0) {
            potionEffects.put(PotionEffectType.SATURATION, 1);
        }

        // This overrides the value loaded in CasterProperties
        if (!regenWhileInactive) {
            setProperty("mana_timestamp", System.currentTimeMillis());
        }

        if (hasProperty("effect_color")) {
            setEffectColor(getString("effect_color"));
        }

        id = getString("id");
        isUpgrade = getBoolean("upgrade");
        quietLevel = getInt("quiet");
        effectBubbles = getBoolean("effect_bubbles");
        keep = getBoolean("keep");
        passive = getBoolean("passive");
        indestructible = getBoolean("indestructible");
        superPowered = getBoolean("powered");
        superProtected = getBoolean("protected");
        glow = getBoolean("glow");
        undroppable = getBoolean("undroppable");
        isHeroes = getBoolean("heroes");
        bound = getBoolean("bound");
        forceUpgrade = getBoolean("force");
        autoOrganize = getBoolean("organize");
        autoAlphabetize = getBoolean("alphabetize");
        autoFill = getBoolean("fill");
        rename = getBoolean("rename");
        renameDescription = getBoolean("rename_description");
        enchantCount = getInt("enchant_count");
        maxEnchantCount = getInt("max_enchant_count");
        inventoryRows = getInt("inventory_rows", 5);
        resetManaOnActivate = getBoolean("reset_mana_on_activate", false);
        if (inventoryRows <= 0) inventoryRows = 1;

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
            castParameters = new MemoryConfiguration();
            ConfigurationUtils.addParameters(StringUtils.split(castParameterString, ' '), castParameters);
        } else {
            castParameters = null;
        }

        WandMode newMode = parseWandMode(getString("mode"), controller.getDefaultWandMode());
        if (newMode != mode) {
            if (isInventoryOpen()) {
                closeInventory();
            }
            mode = newMode;
        }

        brushMode = parseWandMode(getString("brush_mode"), controller.getDefaultBrushMode());
        currencyDisplay = controller.getCurrency(getString("currency_display", "sp"));

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
        leftClickAction = parseWandAction(getString("left_click"), leftClickAction);
        rightClickAction = parseWandAction(getString("right_click"), rightClickAction);
        dropAction = parseWandAction(getString("drop"), dropAction);
        swapAction = parseWandAction(getString("swap"), swapAction);

        owner = getString("owner");
        ownerId = getString("owner_id");
        template = getString("template");
        upgradeTemplate = getString("upgrade_template");
        path = getString("path");

        activeSpell = getString("active_spell");
        if (activeSpell != null && activeSpell.contains("|")) {
            SpellKey activeKey = new SpellKey(activeSpell);
            activeSpell = activeKey.getBaseKey();
            setProperty("active_spell", activeSpell);
        }
        alternateSpell = getString("alternate_spell");
        alternateSpell2 = getString("alternate_spell2");
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

        WandTemplate wandTemplate = getTemplate();

        if (hasProperty("icon_inactive")) {
            String iconKey = getString("icon_inactive");
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
        randomizeOnActivate = randomizeOnActivate && hasProperty("randomize_icon");
        if (randomizeOnActivate) {
            String randomizeIcon = getString("randomize_icon");
            setIcon(loadIcon(randomizeIcon));
            if (item == null) {
                controller.getLogger().warning("Invalid randomize_icon in wand '" + template + "' config: " + randomizeIcon);
            }
        } else if (hasProperty("icon")) {
            String iconKey = getString("icon");
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
                iconKey = templateConfig.getString("icon");
            }
            setIcon(loadIcon(iconKey));
            if (item == null) {
                controller.getLogger().warning("Invalid icon in wand '" + template + "' config: " + iconKey);
            }
            updateIcon();
        } else if (isUpgrade) {
            setIcon(new MaterialAndData(DefaultUpgradeMaterial));
        } else {
            setIcon(new MaterialAndData(DefaultWandMaterial));
        }

        if (hasProperty("upgrade_icon")) {
            upgradeIcon = loadIcon(getString("upgrade_icon"));
        }

        // Add vanilla attributes
        InventoryUtils.applyAttributes(item, getConfigurationSection("item_attributes"), getString("item_attribute_slot", getString("attribute_slot")));

        // Add unstashable and unmoveable tags
        if (getBoolean("unstashable") || (undroppable && Unstashable)) {
            InventoryUtils.setMetaBoolean(item, "unstashable", true);
        } else {
            InventoryUtils.removeMeta(item, "unstashable");
        }
        if (getBoolean("unmoveable")) {
            InventoryUtils.setMetaBoolean(item, "unmoveable", true);
        } else {
            InventoryUtils.removeMeta(item, "unmoveable");
        }
        if (undroppable) {
            InventoryUtils.setMetaBoolean(item, "undroppable", true);
        } else {
            InventoryUtils.removeMeta(item, "undroppable");
        }
        if (keep) {
            InventoryUtils.setMetaBoolean(item, "keep", true);
        } else {
            InventoryUtils.removeMeta(item, "keep");
        }

        // Add vanilla enchantments
        ConfigurationSection enchantments = getConfigurationSection("enchantments");
        InventoryUtils.applyEnchantments(item, enchantments);

        // Add enchantment glow
        if (enchantments == null || enchantments.getKeys(false).isEmpty()) {
            if (glow) {
                CompatibilityUtils.addGlow(item);
            } else {
                CompatibilityUtils.removeGlow(item);
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
                loadSpellLevels(NMSUtils.getMap((ConfigurationSection)spellLevelsRaw));
            }
        }
        checkActiveSpell();
        loadBrushes();

        Object brushInventoryRaw = getObject("brush_inventory");
        if (brushInventoryRaw != null) {
            // Not sure this will ever appear as a Map, but just in case
            if (brushInventoryRaw instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Integer> brushInventory = (Map<String, Integer>)brushInventoryRaw;
                loadBrushInventory(brushInventory);
            } else if (brushInventoryRaw instanceof ConfigurationSection) {
                loadBrushInventory(NMSUtils.getMap((ConfigurationSection)brushInventoryRaw));

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
                loadSpellInventory(NMSUtils.getMap((ConfigurationSection)spellInventoryRaw));
            }
        }
        else {
            // Spells may have contained an inventory from migration or templates with a spell@slot format.
            updateSpellInventory();
        }

        castOverrides = null;
        if (hasProperty("overrides")) {
            castOverrides = null;
            Object overridesGeneric = getObject("overrides");
            if (overridesGeneric != null) {
                castOverrides = new HashMap<>();
                if (overridesGeneric instanceof String) {
                    String overrides = (String) overridesGeneric;
                    if (!overrides.isEmpty()) {
                        // Support YML-List-As-String format
                        // May not really need this anymore.
                        overrides = overrides.replaceAll("[\\]\\[]", "");
                        String[] pairs = StringUtils.split(overrides, ',');
                        for (String override : pairs) {
                            parseOverride(override);
                        }
                    }
                } else if (overridesGeneric instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> overrideList = (List<String>)overridesGeneric;
                    for (String override : overrideList) {
                        parseOverride(override);
                    }
                } else if (overridesGeneric instanceof ConfigurationSection) {
                    ConfigurationSection overridesSection = (ConfigurationSection)overridesGeneric;
                    Set<String> keys = overridesSection.getKeys(true);
                    for (String key : keys) {
                        Object leaf = overridesSection.get(key);
                        if (!(leaf instanceof ConfigurationSection) && !(leaf instanceof Map)) {
                            castOverrides.put(key, leaf.toString());
                        }
                    }
                }
            }
        }

        potionEffects.clear();
        if (hasProperty("potion_effects")) {
            addPotionEffects(potionEffects, getString("potion_effects", null));
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

        checkActiveMaterial();
    }

    private void parseOverride(String override) {
        // Unescape commas
        override = override.replace("\\|", ",");
        String[] keyValue = StringUtils.split(override, ' ');
        if (keyValue.length > 0) {
            String value = keyValue.length > 1 ? keyValue[1] : "";
            castOverrides.put(keyValue[0], value);
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
        String brushPrefix = ChatColor.translateAlternateColorCodes('&', messages.get("wand.brush_prefix"));
        return brushPrefix + materialName;
    }

    private static String getSpellDisplayName(Messages messages, SpellTemplate spell, com.elmakers.mine.bukkit.api.block.MaterialBrush brush) {
        String name = "";
        if (spell != null) {
            String spellPrefix = ChatColor.translateAlternateColorCodes('&', messages.get("wand.spell_prefix"));
            if (brush != null && spell.usesBrush()) {
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

        return ChatColor.translateAlternateColorCodes('&', name);
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
        String name = ChatColor.translateAlternateColorCodes('&', getMessage(wandColorPrefix)) + getDisplayName();
        if (randomizeOnActivate) return name;

        Set<String> spells = getSpells();

        // Add active spell to description
        Messages messages = controller.getMessages();
        boolean showSpell = isModifiable() && hasSpellProgression();
        showSpell = !quickCast && (spells.size() > 1 || showSpell) && getMode() != WandMode.SKILLS;
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
        return ChatColor.translateAlternateColorCodes('&', randomizeOnActivate ? getMessage("randomized_name") : wandName);
    }

    public void updateName(boolean isActive) {
        if (isActive) {
            CompatibilityUtils.setDisplayName(item, !isUpgrade ? getActiveWandName() :
                    ChatColor.translateAlternateColorCodes('&', getMessage("upgrade_prefix")) + getDisplayName());
        } else {
            CompatibilityUtils.setDisplayName(item, ChatColor.stripColor(getDisplayName()));
        }
    }

    private void updateName() {
        updateName(true);
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

    protected void addPropertyLore(List<String> lore, boolean isSingleSpell)
    {
        if (usesMana() && effectiveManaMax > 0) {
            int manaMax = getManaMax();
            if (effectiveManaMax != manaMax) {
                String fullMessage = getLevelString("mana_amount_boosted", manaMax, controller.getMaxMana());
                ConfigurationUtils.addIfNotEmpty(fullMessage.replace("$mana", Integer.toString(effectiveManaMax)), lore);
            } else {
                ConfigurationUtils.addIfNotEmpty(getLevelString("mana_amount", manaMax, controller.getMaxMana()), lore);
            }
            int manaRegeneration = getManaRegeneration();
            if (manaRegeneration > 0 && effectiveManaRegeneration > 0) {
                if (effectiveManaRegeneration != manaRegeneration) {
                    String fullMessage = getLevelString("mana_regeneration_boosted", manaRegeneration, controller.getMaxManaRegeneration());
                    ConfigurationUtils.addIfNotEmpty(fullMessage.replace("$mana", Integer.toString(effectiveManaRegeneration)), lore);
                } else {
                    ConfigurationUtils.addIfNotEmpty(getLevelString("mana_regeneration", manaRegeneration, controller.getMaxManaRegeneration()), lore);
                }
            }
            if (manaPerDamage > 0) {
                ConfigurationUtils.addIfNotEmpty(getLevelString("mana_per_damage", manaPerDamage, controller.getMaxManaRegeneration()), lore);
            }
        }
        if (superPowered) {
            ConfigurationUtils.addIfNotEmpty(getMessage("super_powered"), lore);
        }
        if (blockReflectChance > 0) {
            ConfigurationUtils.addIfNotEmpty(getLevelString("reflect_chance", blockReflectChance), lore);
        } else if (blockChance != 0) {
            ConfigurationUtils.addIfNotEmpty(getLevelString("block_chance", blockChance), lore);
        }
        float manaMaxBoost = getManaMaxBoost();
        if (manaMaxBoost != 0) {
            ConfigurationUtils.addIfNotEmpty(getPropertyString("mana_boost", manaMaxBoost), lore);
        }
        float manaRegenerationBoost = getManaRegenerationBoost();
        if (manaRegenerationBoost != 0) {
            ConfigurationUtils.addIfNotEmpty(getPropertyString("mana_regeneration_boost", manaRegenerationBoost), lore);
        }

        if (castSpell != null) {
            SpellTemplate spell = controller.getSpellTemplate(castSpell);
            if (spell != null)
            {
                ConfigurationUtils.addIfNotEmpty(getMessage("spell_aura").replace("$spell", spell.getName()), lore);
            }
        }
        for (Map.Entry<PotionEffectType, Integer> effect : potionEffects.entrySet()) {
            ConfigurationUtils.addIfNotEmpty(describePotionEffect(effect.getKey(), effect.getValue()), lore);
        }

        // If this is a passive wand, then reduction properties stack onto the mage when worn.
        // In this case we should show it as such in the lore.
        if (passive) isSingleSpell = false;

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

        if (earnMultiplier > 1) {
            String earnDescription = getPropertyString("earn_multiplier", earnMultiplier - 1);
            earnDescription = earnDescription.replace("$type", "SP");
            ConfigurationUtils.addIfNotEmpty(earnDescription, lore);
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

                    label = getPropertyString("attributes", value, max).replace("$attribute", label);
                    lore.add(label);
                }
            }
        }
    }

    private String getPropertyString(String templateName, float value) {
        return getPropertyString(templateName, value, 1);
    }

    private String getPropertyString(String templateName, float value, float max) {
        String propertyTemplate = getBoolean("stack") ? "property_stack" : "property_value";
        if (value < 0) {
            propertyTemplate = propertyTemplate + "_negative";
        }
        return controller.getMessages().getPropertyString(getMessageKey(templateName), value, max, getMessageKey(propertyTemplate));
    }

    private String formatPropertyString(String template, float value) {
        return formatPropertyString(template, value, 1);
    }

    private String formatPropertyString(String template, float value, float max) {
        String propertyTemplate = getBoolean("stack") ? "property_stack" : "property_value";
        if (value < 0) {
            propertyTemplate = propertyTemplate + "_negative";
        }
        return controller.getMessages().formatPropertyString(template, value, max, getMessage(propertyTemplate));
    }

    private void addDamageTypeLore(String property, String propertyType, double amount, List<String> lore) {
        if (amount != 0) {
            String templateKey = getMessageKey(property + "." + propertyType);
            String template;
            if (controller.getMessages().containsKey(templateKey)) {
                template = controller.getMessages().get(templateKey);
            } else {
                templateKey = getMessageKey("protection.unknown");
                template = controller.getMessages().get(templateKey);
                String pretty = propertyType.substring(0, 1).toUpperCase() + propertyType.substring(1);
                template = template.replace("$type", pretty);
            }
            template = formatPropertyString(template, (float)amount);
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
            if (line.startsWith("$")) {
                switch (line) {
                    case "$description":
                        addDescriptionLore(lore);
                        break;
                    case "$path":
                        String pathTemplate = getMessage("path_lore", "");
                        String pathName = getPathName();
                        if (pathName != null && !pathTemplate.isEmpty()) {
                            lore.add(pathTemplate.replace("$path", pathName));
                        }
                        break;
                    case "$owner":
                        addOwnerDescription(lore);
                        break;
                    case "$spells":
                        int spellCount = getSpells().size();
                        if (spellCount > 0) {
                            ConfigurationUtils.addIfNotEmpty(getMessage("spell_count").replace("$count", Integer.toString(spellCount)), lore);
                        }
                        break;
                    case "$brushes":
                        int materialCount = getBrushes().size();
                        if (materialCount > 0) {
                            ConfigurationUtils.addIfNotEmpty(getMessage("material_count").replace("$count", Integer.toString(materialCount)), lore);
                        }
                        break;
                    case "$uses":
                        addUseLore(lore);
                        break;
                    case "$mana_max":
                        if (usesMana()) {
                            int manaMax = getManaMax();
                            if (effectiveManaMax != manaMax) {
                                String fullMessage = getLevelString("mana_amount_boosted", manaMax, controller.getMaxMana());
                                ConfigurationUtils.addIfNotEmpty(fullMessage.replace("$mana", Integer.toString(effectiveManaMax)), lore);
                            } else {
                                ConfigurationUtils.addIfNotEmpty(getLevelString("mana_amount", manaMax, controller.getMaxMana()), lore);
                            }
                        }
                        break;
                    case "$mana_regeneration":
                        if (usesMana()) {
                            int manaRegeneration = getManaRegeneration();
                            if (manaRegeneration > 0) {
                                if (effectiveManaRegeneration != manaRegeneration) {
                                    String fullMessage = getLevelString("mana_regeneration_boosted", manaRegeneration, controller.getMaxManaRegeneration());
                                    ConfigurationUtils.addIfNotEmpty(fullMessage.replace("$mana", Integer.toString(effectiveManaRegeneration)), lore);
                                } else {
                                    ConfigurationUtils.addIfNotEmpty(getLevelString("mana_regeneration", manaRegeneration, controller.getMaxManaRegeneration()), lore);
                                }
                            }
                        }
                        break;
                    default:
                        lore.add(ChatColor.translateAlternateColorCodes('&', line));
                }
            } else {
                lore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
        }
        return lore;
    }

    protected void addDescriptionLore(List<String> lore) {
        String descriptionTemplate = controller.getMessages().get(getMessageKey("description_lore"), "");
        if (!description.isEmpty() && !descriptionTemplate.isEmpty()) {
            if (description.contains("$path")) {
                String pathName = getPathName();
                String description = ChatColor.translateAlternateColorCodes('&', this.description);
                description = description.replace("$path", pathName == null ? "Unknown" : pathName);
                InventoryUtils.wrapText(descriptionTemplate.replace("$description", description), lore);
            } else {
                String description = ChatColor.translateAlternateColorCodes('&', this.description);
                InventoryUtils.wrapText(descriptionTemplate.replace("$description", description), lore);
            }
        }
    }

    @Nullable
    protected String getPathName() {
        String pathName = null;
        com.elmakers.mine.bukkit.api.wand.WandUpgradePath path = getPath();
        if (path != null) {
            pathName = path.getName();
        } else if (mageClassKeys != null && !mageClassKeys.isEmpty()) {
            MageClassTemplate classTemplate = controller.getMageClassTemplate(mageClassKeys.get(0));
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
            }
        }

        return pathName;
    }

    protected void addOwnerDescription(List<String> lore) {
        if (owner != null && owner.length() > 0) {
            if (bound) {
                String ownerDescription = getMessage("bound_description", "$name").replace("$name", owner);
                ConfigurationUtils.addIfNotEmpty(ownerDescription, lore);
            } else {
                String ownerDescription = getMessage("owner_description", "$name").replace("$name", owner);
                ConfigurationUtils.addIfNotEmpty(ownerDescription, lore);
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected List<String> getLore()
    {
        Object customLore = getProperty("lore");
        if (customLore != null && customLore instanceof Collection) {
            return getCustomLore((Collection<String>)customLore);
        }
        List<String> lore = new ArrayList<>();

        int spellCount = getSpells().size();
        int materialCount = getBrushes().size();

        String pathName = getPathName();
        if (description.length() > 0) {
            if (randomizeOnActivate) {
                String randomDescription = getMessage("randomized_lore");
                String randomTemplate = controller.getMessages().get(getMessageKey("randomized_description"), "");
                if (randomDescription.length() > 0 && !randomTemplate.isEmpty()) {
                    InventoryUtils.wrapText(randomTemplate.replace("$description", randomDescription), lore);
                    return lore;
                }
            }
            if (description.contains("$") && !description.contains("$path")) {
                String newDescription = controller.getMessages().escape(description);
                if (!newDescription.equals(description)) {
                    this.description = newDescription;
                    setProperty("description", description);
                }
            }
            String descriptionTemplate = controller.getMessages().get(getMessageKey("description_lore"), "");
            if (description.contains("$path") && !descriptionTemplate.isEmpty()) {
                String description = ChatColor.translateAlternateColorCodes('&', this.description);
                description = description.replace("$path", pathName == null ? "Unknown" : pathName);
                InventoryUtils.wrapText(descriptionTemplate.replace("$description", description), lore);
            } else if (description.contains("$")) {
                String randomDescription = getMessage("randomized_lore");
                String randomTemplate = controller.getMessages().get(getMessageKey("randomized_description"), "");
                if (randomDescription.length() > 0 && !randomTemplate.isEmpty()) {
                    randomDescription = ChatColor.translateAlternateColorCodes('&', randomDescription);
                    InventoryUtils.wrapText(randomTemplate.replace("$description", randomDescription), lore);
                    return lore;
                }
            } else if (!descriptionTemplate.isEmpty()) {
                String description = ChatColor.translateAlternateColorCodes('&', this.description);
                InventoryUtils.wrapText(descriptionTemplate.replace("$description", description), lore);
            }
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
        if (isUpgrade) {
            ConfigurationUtils.addIfNotEmpty(getMessage("upgrade_item_description"), lore);
        }
        return lore;
    }

    protected void addUseLore(List<String> lore) {
        int remaining = getRemainingUses();
        if (!isSingleUse && remaining > 0) {
            if (isUpgrade) {
                String message = (remaining == 1) ? getMessage("upgrade_uses_singular") : getMessage("upgrade_uses");
                ConfigurationUtils.addIfNotEmpty(message.replace("$count", Integer.toString(remaining)), lore);
            } else {
                String message = (remaining == 1) ? getMessage("uses_remaining_singular") : getMessage("uses_remaining_brief");
                ConfigurationUtils.addIfNotEmpty(message.replace("$count", Integer.toString(remaining)), lore);
            }
        }
    }

    protected void updateLore() {
        CompatibilityUtils.setLore(item, getLore());
    }

    public int getRemainingUses() {
        return uses;
    }

    public void makeEnchantable(boolean enchantable) {
        if (EnchantableWandMaterial == null) return;

        if (!enchantable) {
            item.setType(icon.getMaterial());
            item.setDurability(icon.getData());
        } else {
            MaterialSet enchantableMaterials = controller.getMaterialSetManager().getMaterialSetEmpty("enchantable");
            if (!enchantableMaterials.testItem(item)) {
                item.setType(EnchantableWandMaterial);
                item.setDurability((short) 0);
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
        return item != null && InventoryUtils.hasMeta(item, WAND_KEY);
    }

    public static boolean isWandOrUpgrade(ItemStack item) {
        return isWand(item) || isUpgrade(item);
    }

    public static boolean isSpecial(ItemStack item) {
        return isWand(item) || isUpgrade(item) || isSpell(item) || isBrush(item) || isSP(item) || isCurrency(item);
    }

    public static boolean isSelfDestructWand(ItemStack item) {
        return item != null && WAND_SELF_DESTRUCT_KEY != null && InventoryUtils.hasMeta(item, WAND_SELF_DESTRUCT_KEY);
    }

    public static boolean isSP(ItemStack item) {
        return InventoryUtils.hasMeta(item, "sp");
    }

    public static boolean isCurrency(ItemStack item) {
        return InventoryUtils.hasMeta(item, "currency");
    }

    @Nullable
    public static Integer getSP(ItemStack item) {
        if (InventoryUtils.isEmpty(item)) return null;
        String spNode = InventoryUtils.getMetaString(item, "sp");
        if (spNode == null) return null;
        Integer sp = null;
        try {
            sp = Integer.parseInt(spNode);
        } catch (Exception ex) {
            sp = null;
        }

        return sp;
    }

    @Nullable
    public static Double getCurrencyAmount(ItemStack item) {
        if (InventoryUtils.isEmpty(item)) return null;
        Object currencyNode = InventoryUtils.getNode(item, "currency");
        if (currencyNode == null) return null;
        return InventoryUtils.getMetaDouble(currencyNode, "amount");
    }

    @Nullable
    public static String getCurrencyType(ItemStack item) {
        if (InventoryUtils.isEmpty(item)) return null;
        Object currencyNode = InventoryUtils.getNode(item, "currency");
        if (currencyNode == null) return null;
        return InventoryUtils.getMetaString(currencyNode, "type");
    }

    public static boolean isSpell(ItemStack item) {
        return item != null && InventoryUtils.hasMeta(item, "spell");
    }

    public static boolean isSkill(ItemStack item) {
        return item != null && InventoryUtils.hasMeta(item, "skill");
    }

    public static boolean isBrush(ItemStack item) {
        return item != null && InventoryUtils.hasMeta(item, "brush");
    }

    @Nullable
    protected static Object getWandOrUpgradeNode(ItemStack item) {
        if (InventoryUtils.isEmpty(item)) return null;
        Object wandNode = InventoryUtils.getNode(item, WAND_KEY);
        if (wandNode == null) {
            wandNode = InventoryUtils.getNode(item, UPGRADE_KEY);
        }
        return wandNode;
    }

    @Nullable
    public static String getWandTemplate(ItemStack item) {
        Object wandNode = getWandOrUpgradeNode(item);
        if (wandNode == null) return null;
        return InventoryUtils.getMetaString(wandNode, "template");
    }

    @Nullable
    public static String getWandId(ItemStack item) {
        if (InventoryUtils.isEmpty(item)) return null;
        Object wandNode = InventoryUtils.getNode(item, WAND_KEY);
        if (wandNode == null) return null;
        return InventoryUtils.getMetaString(wandNode, "id");
    }

    @Nullable
    public static String getSpell(ItemStack item) {
        if (InventoryUtils.isEmpty(item)) return null;
        Object spellNode = InventoryUtils.getNode(item, "spell");
        if (spellNode == null) return null;
        return InventoryUtils.getMetaString(spellNode, "key");
    }

    @Nullable
    @Override
    public Spell getSpell(String spellKey, com.elmakers.mine.bukkit.api.magic.Mage mage) {
        if (mage == null) {
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
        if (InventoryUtils.isEmpty(item)) return null;
        Object spellNode = InventoryUtils.getNode(item, "spell");
        if (spellNode == null) return null;
        return InventoryUtils.getMetaString(spellNode, "class");
    }

    public static boolean isQuickCastSkill(ItemStack item) {
        if (InventoryUtils.isEmpty(item)) return false;
        Object spellNode = InventoryUtils.getNode(item, "spell");
        if (spellNode == null) return false;
        Boolean quickCast = InventoryUtils.containsNode(spellNode, "quick_cast") ? InventoryUtils.getMetaBoolean(spellNode, "quick_cast") : null;
        return quickCast == null ? true : quickCast;
    }

    @Nullable
    public static String getSpellArgs(ItemStack item) {
        if (InventoryUtils.isEmpty(item)) return null;
        Object spellNode = InventoryUtils.getNode(item, "spell");
        if (spellNode == null) return null;
        return InventoryUtils.getMetaString(spellNode, "args");
    }

    @Nullable
    public static String getBrush(ItemStack item) {
        if (InventoryUtils.isEmpty(item)) return null;
        Object brushNode = InventoryUtils.getNode(item, "brush");
        if (brushNode == null) return null;
        return InventoryUtils.getMetaString(brushNode, "key");
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

    public static void updateSpellItem(Messages messages, ItemStack itemStack, SpellTemplate spell, String args, Wand wand, String activeMaterial, boolean isItem) {
        updateSpellItem(messages, itemStack, spell, args, wand == null ? null : wand.getActiveMage(), wand, activeMaterial, isItem);
    }

    public static void updateSpellItem(Messages messages, ItemStack itemStack, SpellTemplate spell, String args, com.elmakers.mine.bukkit.api.magic.Mage mage, Wand wand, String activeMaterial, boolean isItem) {
        updateSpellName(messages, itemStack, spell, wand, activeMaterial);
        List<String> lore = new ArrayList<>();
        addSpellLore(messages, spell, lore, mage, wand);
        if (isItem) {
            ConfigurationUtils.addIfNotEmpty(messages.get("wand.spell_item_description"), lore);
        }
        CompatibilityUtils.setLore(itemStack, lore);
        Object spellNode = CompatibilityUtils.createNode(itemStack, "spell");
        CompatibilityUtils.setMeta(spellNode, "key", spell.getKey());
        CompatibilityUtils.setMeta(spellNode, "args", args);
        if (SpellGlow) {
            CompatibilityUtils.addGlow(itemStack);
        }
    }

    public static void updateSpellName(Messages messages, ItemStack itemStack, SpellTemplate spell, Wand wand, String activeMaterial) {
        String displayName;
        if (wand != null && !wand.isQuickCast()) {
            displayName = wand.getActiveWandName(spell);
        } else {
            displayName = getSpellDisplayName(messages, spell, MaterialBrush.parseMaterialKey(activeMaterial));
        }
        CompatibilityUtils.setDisplayName(itemStack, displayName);
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
        CompatibilityUtils.setDisplayName(itemStack, displayName);
    }

    public static void updateBrushItem(Messages messages, ItemStack itemStack, String materialKey, Wand wand) {
        updateBrushItem(messages, itemStack, MaterialBrush.parseMaterialKey(materialKey), wand);
    }

    public static void updateBrushItem(Messages messages, ItemStack itemStack, MaterialBrush brush, Wand wand) {
        updateBrushName(messages, itemStack, brush, wand);
        Object brushNode = CompatibilityUtils.createNode(itemStack, "brush");
        CompatibilityUtils.setMeta(brushNode, "key", brush.getKey());
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
            DeprecatedUtils.updateInventory(player);
        }
    }

    private boolean updateHotbar(PlayerInventory playerInventory) {
        if (getMode() != WandMode.INVENTORY) return false;
        WandInventory hotbar = getHotbar();
        if (hotbar == null) return false;

        // Make sure the wand is still in the held slot
        ItemStack currentItem = playerInventory.getItem(heldSlot);
        if (currentItem == null || !currentItem.getItemMeta().equals(item.getItemMeta())) {
            controller.getLogger().warning("Trying to update hotbar but the wand has gone missing");
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
        spell.addLore(messages, mage, wand, lore);
    }

    private String getInventoryTitle() {
        return getMessage("chest_inventory_title", "Wand");
    }

    protected WandInventory getOpenInventory() {
        while (openInventoryPage >= inventories.size()) {
            inventories.add(new WandInventory(getInventorySize()));
        }
        return inventories.get(openInventoryPage);
    }

    protected Inventory getDisplayInventory() {
        if (displayInventory == null || displayInventory.getSize() != getInventorySize()) {
            displayInventory = CompatibilityUtils.createInventory(null, getInventorySize(), getInventoryTitle());
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
                mage.giveItem(item);
                return false;
            }
        }

        return true;
    }

    @Override
    public int enchant(int totalLevels, com.elmakers.mine.bukkit.api.magic.Mage mage, boolean addSpells) {
        return randomize(totalLevels, true, mage, addSpells);
    }

    @Override
    public int enchant(int totalLevels, com.elmakers.mine.bukkit.api.magic.Mage mage) {
        return randomize(totalLevels, true, mage, true);
    }

    @Override
    public int enchant(int totalLevels) {
        return randomize(totalLevels, true, null, true);
    }

    protected int randomize(int totalLevels, boolean additive, com.elmakers.mine.bukkit.api.magic.Mage enchanter, boolean addSpells) {
        if (enchanter == null && mage != null) {
            enchanter = mage;
        }

        if (maxEnchantCount > 0 && enchantCount >= maxEnchantCount) {
            if (enchanter != null && addSpells) {
                enchanter.sendMessage(getMessage("max_enchanted").replace("$wand", getName()));
            }
            return 0;
        }

        WandUpgradePath path = getPath();
        if (path == null) {
            if (enchanter != null && addSpells) {
                enchanter.sendMessage(getMessage("no_path").replace("$wand", getName()));
            }
            return 0;
        }

        int minLevel = path.getMinLevel();
        if (totalLevels < minLevel) {
            if (enchanter != null && addSpells) {
                String levelMessage = getMessage("need_more_levels");
                levelMessage = levelMessage.replace("$levels", Integer.toString(minLevel));
                enchanter.sendMessage(levelMessage);
            }
            return 0;
        }

        // Just a hard-coded sanity check
        int maxLevel = path.getMaxLevel();
        totalLevels = Math.min(totalLevels, maxLevel * 50);

        int addLevels = Math.min(totalLevels, maxLevel);
        int levels = 0;
        boolean modified = true;
        while (addLevels >= minLevel && modified) {
            boolean hasUpgrade = path.hasUpgrade();
            WandLevel level = path.getLevel(addLevels);

            if (!path.canEnchant(this) && (path.hasSpells() || path.hasMaterials())) {
                // Check for level up
                WandUpgradePath nextPath = path.getUpgrade();
                if (nextPath != null) {
                    if (path.checkUpgradeRequirements(this, addSpells ? enchanter : null)) {
                        path.upgrade(this, enchanter);
                    }
                    break;
                } else {
                    if (enchanter != null && addSpells) {
                        enchanter.sendMessage(getMessage("fully_enchanted").replace("$wand", getName()));
                    }
                    break;
                }
            }

            modified = level.randomizeWand(enchanter, this, additive, hasUpgrade, addSpells);
            totalLevels -= maxLevel;
            if (modified) {
                if (enchanter != null) {
                    path.enchanted(enchanter);
                }
                levels += addLevels;

                // Check for level up
                WandUpgradePath nextPath = path.getUpgrade();
                if (nextPath != null && path.checkUpgradeRequirements(this, null) && !path.canEnchant(this)) {
                    path.upgrade(this, enchanter);
                    path = nextPath;
                }
            } else if (path.canEnchant(this)) {
                if (enchanter != null && levels == 0 && addSpells)
                {
                    String message = getMessage("require_more_levels");
                    enchanter.sendMessage(message);
                }
            } else if (hasUpgrade) {
                if (path.checkUpgradeRequirements(this, addSpells ? enchanter : null)) {
                    path.upgrade(this, enchanter);
                    levels += addLevels;
                }
            } else if (enchanter != null && addSpells) {
                enchanter.sendMessage(getMessage("fully_enchanted").replace("$wand", getName()));
            }
            addLevels = Math.min(totalLevels, maxLevel);
            additive = true;
        }

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
            ConfigurationSection wandConfig = controller.getWandTemplateConfiguration(template);
            if (wandConfig != null && wandConfig.contains("icon")) {
                String iconKey = wandConfig.getString("icon");
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
        if (controller == null) return null;

        Wand wand = null;
        try {
            wand = new Wand(controller, templateName);
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
            wand = controller.getWand(InventoryUtils.makeReal(itemStack.clone()));
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

    public boolean add(Wand other) {
        return add(other, this.mage);
    }

    @Override
    public boolean add(com.elmakers.mine.bukkit.api.wand.Wand other, com.elmakers.mine.bukkit.api.magic.Mage mage) {
        if (other instanceof Wand) {
            return add((Wand)other, mage);
        }

        return false;
    }

    public boolean add(Wand other, com.elmakers.mine.bukkit.api.magic.Mage mage) {
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
        upgradeConfig.set("id", null);
        upgradeConfig.set("indestructible", null);
        upgradeConfig.set("upgrade", null);
        upgradeConfig.set("icon", other.upgradeIcon == null ? null : other.upgradeIcon.getKey());
        upgradeConfig.set("upgrade_icon", null);
        upgradeConfig.set("template", other.upgradeTemplate);

        Messages messages = controller.getMessages();
        if (other.rename && templateConfig != null) {
            String newName = messages.get("wands." + other.template + ".name");
            newName = templateConfig.getString("name", newName);
            upgradeConfig.set("name", newName);
        } else {
            upgradeConfig.set("name", null);
        }

        if (other.renameDescription && templateConfig != null) {
            String newDescription = messages.get("wands." + other.template + ".description");
            newDescription = templateConfig.getString("description", newDescription);
            upgradeConfig.set("description", newDescription);
        } else {
            upgradeConfig.set("description", null);
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

    private void updateActiveMaterial() {
        if (mage == null) return;

        if (activeBrush == null) {
            mage.clearBuildingMaterial();
        } else {
            com.elmakers.mine.bukkit.api.block.MaterialBrush brush = mage.getBrush();
            brush.update(activeBrush);
        }
    }

    public void cycleActive(int direction) {
        Player player = mage != null ? mage.getPlayer() : null;
        if (player != null && player.isSneaking()) {
            com.elmakers.mine.bukkit.api.spell.Spell activeSpell = getActiveSpell();
            boolean cycleMaterials = false;
            if (activeSpell != null) {
                cycleMaterials = activeSpell.usesBrushSelection();
            }
            if (cycleMaterials) {
                cycleMaterials(direction);
            } else {
                cycleSpells(direction);
            }
        } else {
            cycleSpells(direction);
        }
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
                DeprecatedUtils.updateInventory(mage.getPlayer());
            }
        }
    }

    @Override
    public void cycleHotbar() {
        cycleHotbar(1);
    }

    public void cycleHotbar(int direction) {
        if (!hasInventory || getMode() != WandMode.INVENTORY) {
            return;
        }
        if (isInventoryOpen() && mage != null && hotbars.size() > 1) {
            saveInventory();
            int hotbarCount = hotbars.size();
            setCurrentHotbar(hotbarCount == 0 ? 0 : (currentHotbar + hotbarCount + direction) % hotbarCount);
            updateHotbar();
            if (!playPassiveEffects("cycle") && inventoryCycleSound != null) {
                mage.playSoundEffect(inventoryCycleSound);
            }
            sendMessage("hotbar_changed");
            updateHotbarStatus();
            DeprecatedUtils.updateInventory(mage.getPlayer());
        }
    }

    public void openInventory() {
        if (mage == null) return;
        if (System.currentTimeMillis() < mage.getWandDisableTime()) return;

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
                showActiveIcon(true);
                if (!playPassiveEffects("open") && inventoryOpenSound != null) {
                    mage.playSoundEffect(inventoryOpenSound);
                }
                updateInventory();
                updateHotbarStatus();
            }
        }
    }

    @Override
    public void closeInventory() {
        closeInventory(true);
    }

    public void closeInventory(boolean closePlayerInventory) {
        if (!isInventoryOpen()) return;
        controller.disableItemSpawn();
        inventoryWasOpen = true;
        WandMode mode = getMode();
        try {
            saveInventory();
            updateSpellInventory();
            updateBrushInventory();
            inventoryIsOpen = false;
            if (mage != null) {
                if (!playPassiveEffects("close") && inventoryCloseSound != null) {
                    mage.playSoundEffect(inventoryCloseSound);
                }
                if (mode == WandMode.INVENTORY) {
                    restoreInventory();
                    showActiveIcon(false);
                } else if (closePlayerInventory) {
                    mage.getPlayer().closeInventory();
                }

                // Check for items the player might've glitched onto their body...
                PlayerInventory inventory = mage.getPlayer().getInventory();
                ItemStack testItem = inventory.getHelmet();
                if (isSpell(testItem) || isBrush(testItem)) {
                    inventory.setHelmet(new ItemStack(Material.AIR));
                    DeprecatedUtils.updateInventory(mage.getPlayer());
                }
                testItem = inventory.getBoots();
                if (isSpell(testItem) || isBrush(testItem)) {
                    inventory.setBoots(new ItemStack(Material.AIR));
                    DeprecatedUtils.updateInventory(mage.getPlayer());
                }
                testItem = inventory.getLeggings();
                if (isSpell(testItem) || isBrush(testItem)) {
                    inventory.setLeggings(new ItemStack(Material.AIR));
                    DeprecatedUtils.updateInventory(mage.getPlayer());
                }
                testItem = inventory.getChestplate();
                if (isSpell(testItem) || isBrush(testItem)) {
                    inventory.setChestplate(new ItemStack(Material.AIR));
                    DeprecatedUtils.updateInventory(mage.getPlayer());
                }
                // This is kind of a hack :(
                testItem = inventory.getItemInOffHand();
                if ((isSpell(testItem) && !isSkill(testItem)) || isBrush(testItem)) {
                    inventory.setItemInOffHand(new ItemStack(Material.AIR));
                    DeprecatedUtils.updateInventory(mage.getPlayer());
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

        if (isModifiable() && isSpell(item) && !isSkill(item)) {
            String spell = getSpell(item);
            SpellKey spellKey = new SpellKey(spell);
            Integer currentLevel = spellLevels.get(spellKey.getBaseKey());
            if ((currentLevel == null || currentLevel < spellKey.getLevel()) && addSpell(spell)) {
                return true;
            }
        } else if (isModifiable() && isBrush(item)) {
            String materialKey = getBrush(item);
            Set<String> materials = getBrushes();
            if (!materials.contains(materialKey) && addBrush(materialKey)) {
                return true;
            }
        } else if (isUpgrade(item)) {
            Wand wand = controller.getWand(item);
            return this.add(wand);
        }
        if (mage != null && !mage.isAtMaxSkillPoints() && controller.skillPointItemsEnabled()) {
            Integer sp = getSP(item);
            if (sp != null) {
                int amount = (int)Math.floor(mage.getEarnMultiplier() * sp * item.getAmount());
                mage.addSkillPoints(amount);
                return true;
            }
        }

        return false;
    }

    protected void updateEffects() {
        updateEffects(mage);
    }

    public void updateEffects(Mage mage) {
        if (mage == null) return;
        Player player = mage.getPlayer();
        if (player == null) return;

        // Update Bubble effects effects
        if (effectBubbles && effectColor != null) {
            Location potionEffectLocation = player.getLocation();
            potionEffectLocation.setX(potionEffectLocation.getX() + random.nextDouble() - 0.5);
            potionEffectLocation.setY(potionEffectLocation.getY() + random.nextDouble() * player.getEyeHeight());
            potionEffectLocation.setZ(potionEffectLocation.getZ() + random.nextDouble() - 0.5);
            EffectPlayer.displayParticle(Particle.SPELL_MOB, potionEffectLocation, 0, 0, 0,
            0, 0, 1, effectColor.getColor(), null, (byte)0, 24);
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
                    effectPlayer.setColor(getEffectColor());
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
                        if (castParameters == null) {
                            castParameters = new MemoryConfiguration();
                        }
                        castParameters.set("passive", true);
                        mage.setCostFree(true);
                        mage.setQuiet(true);
                        try {
                            spell.cast(castParameters);
                        } catch (Exception ex) {
                            controller.getLogger().log(Level.WARNING, "Error casting aura spell " + spell.getKey(), ex);
                        }
                        mage.setCostFree(false);
                        mage.setQuiet(false);
                    }
                }
            }
        }

        if (playEffects && effectSound != null && controller.soundsEnabled() && effectSoundInterval > 0) {
            if (lastSoundEffect == 0 || now > lastSoundEffect + effectSoundInterval) {
                lastSoundEffect = now;
                effectSound.play(controller.getPlugin(), mage.getPlayer());
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
            item.setDurability((short)durability);
        }
    }

    public boolean usesXPBar()
    {
        return (usesCurrency() && currencyMode.useXP()) || (usesMana() && manaMode.useXP());
    }

    public boolean usesXPNumber()
    {
        return (usesCurrency() && currencyMode.useXPNumber()) || (usesMana() && manaMode.useXP());
    }

    public boolean hasSpellProgression()
    {
        return hasSpellProgression;
    }

    public boolean usesXPDisplay()
    {
        return usesXPBar() || usesXPNumber();
    }

    @Override
    public void updateMana() {
        Player player = mage == null ? null : mage.getPlayer();
        if (player == null) return;

        float mana = getMana();
        if (usesMana()) {
            if (manaMode.useGlow()) {
                if (mana == effectiveManaMax) {
                    CompatibilityUtils.addGlow(item);
                } else {
                    CompatibilityUtils.removeGlow(item);
                }
            }
            if (manaMode.useDurability()) {
                updateDurability();
            }
        }

        if (usesXPDisplay()) {
            int playerLevel = player.getLevel();
            float playerProgress = player.getExp();

            if (usesMana() && manaMode.useXPNumber())
            {
                playerLevel = (int) mana;
            }
            if (usesMana() && manaMode.useXPBar())
            {
                playerProgress = Math.min(Math.max(0, mana / effectiveManaMax), 1);
            }
            if (usesCurrency() && currencyMode.useXPNumber())
            {
                playerLevel = (int)Math.ceil(currencyDisplay.getBalance(mage, this));
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
        if (!bound) return;
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
        if (mage == null) return;

        // Play deactivate FX
        playPassiveEffects("deactivate");

        // Cancel effects
        if (effectContext != null) {
            int cancelDelay = getInt("cancel_effects_delay", 0);
            if (cancelDelay == 0) {
                effectContext.cancelEffects();
            } else {
                Plugin plugin = controller.getPlugin();
                final WandEffectContext context = effectContext;
                plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
                    @Override
                    public void run() {
                        context.cancelEffects();
                    }
                }, cancelDelay * 20 / 1000);
            }
        }

        Mage mage = this.mage;

        if (isInventoryOpen()) {
            closeInventory(closePlayerInventory);
        }
        showActiveIcon(false);
        storedInventory = null;
        if (usesXPNumber() || usesXPBar()) {
            mage.resetSentExperience();
        }
        saveState();
        mage.deactivateWand(this);
        this.mage = null;
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
    public Spell getAlternateSpell() {
        if (mage == null || alternateSpell == null || alternateSpell.length() == 0) return null;
        return mage.getSpell(alternateSpell);
    }

    @Nullable
    public Spell getAlternateSpell2() {
        if (mage == null || alternateSpell2 == null || alternateSpell2.length() == 0) return null;
        return mage.getSpell(alternateSpell2);
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

    public boolean alternateCast() {
        return cast(getAlternateSpell());
    }

    public boolean alternateCast2() {
        return cast(getAlternateSpell2());
    }

    @Override
    public boolean cast() {
        return cast(getActiveSpell(), null);
    }

    @Override
    public boolean cast(String[] parameters) {
        return cast(getActiveSpell(), parameters);
    }

    public boolean cast(Spell spell) {
        return cast(spell, null);
    }

    public boolean cast(Spell spell, String[] parameters) {
        if (preuse) {
            if (!use()) {
                return false;
            }
        }
        if (spell != null) {
            Collection<String> castParameters = null;
            if (castOverrides != null && castOverrides.size() > 0) {
                castParameters = new ArrayList<>();
                for (Map.Entry<String, String> entry : castOverrides.entrySet()) {
                    String[] key = StringUtils.split(entry.getKey(), '.');
                    if (key.length == 0) continue;
                    if (key.length == 2 && !key[0].equals("default") && !key[0].equals(spell.getSpellKey().getBaseKey()) && !key[0].equals(spell.getSpellKey().getKey())) {
                        continue;
                    }
                    castParameters.add(key.length == 2 ? key[1] : key[0]);
                    castParameters.add(entry.getValue());
                }
            }
            if (parameters != null) {
                if (castParameters == null) {
                    castParameters = new ArrayList<>();
                }
                for (String parameter : parameters) {
                    castParameters.add(parameter);
                }
            }
            if (spell.cast(castParameters == null ? null : castParameters.toArray(EMPTY_PARAMETERS))) {
                Color spellColor = spell.getColor();
                if (!preuse) {
                    use();
                }
                if (spellColor != null && this.effectColor != null) {
                    this.effectColor = this.effectColor.mixColor(spellColor, effectColorSpellMixWeight);
                    setProperty("effect_color", effectColor.toString());
                    // Note that we don't save this change.
                    // The hope is that the wand will get saved at some point later
                    // And we don't want to trigger NBT writes every spell cast.
                    // And the effect color morphing isn't all that important if a few
                    // casts get lost.
                }
                updateHotbarStatus();
                return true;
            }
        }

        return false;
    }

    protected boolean use() {
        boolean usesRemaining = true;
        if (hasUses) {
            findItem();
            ItemStack item = getItem();
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
                    DeprecatedUtils.updateInventory(player);
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
                effectiveManaMax = heroes.getMaxMana(player);
                effectiveManaRegeneration = heroes.getManaRegen(player);
                setManaMax(effectiveManaMax);
                setManaRegeneration(effectiveManaRegeneration);
                setMana(heroes.getMana(player));
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
        if (usesMana() && !isInOffhand) {
            updateMana();
        }

        if (player.isBlocking() && blockMageCooldown > 0) {
            mage.setRemainingCooldown(blockMageCooldown);
        }

        // Update hotbar glow
        if (!isInOffhand) {
            updateHotbarStatus();
        }

        if (!passive)
        {
            updateEffects();
        }
    }

    @Override
    public void armorUpdated() {
        updateMaxMana(true);
    }

    protected void updateMaxMana(boolean updateLore) {
        if (isHeroes) return;
        if (!hasOwnMana() && mageClass != null) {
            if (mageClass.updateMaxMana(mage) && updateLore) {
                updateLore();
            }

            effectiveManaMax = mageClass.getEffectiveManaMax();
            effectiveManaRegeneration = mageClass.getEffectiveManaRegeneration();
        } else if (super.updateMaxMana(mage) && updateLore) {
            updateLore();
        }
    }

    public void cycleSpells(int direction) {
        ArrayList<String> spells = new ArrayList<>(this.spells);
        if (spells.size() == 0) return;
        if (activeSpell == null) {
            setActiveSpell(spells.get(0));
            return;
        }

        int spellIndex = 0;
        for (int i = 0; i < spells.size(); i++) {
            if (spells.get(i).equals(activeSpell)) {
                spellIndex = i;
                break;
            }
        }

        spellIndex = (spellIndex + direction + spells.size()) % spells.size();
        setActiveSpell(spells.get(spellIndex));
    }

    public void cycleMaterials(int direction) {
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
    }

    @Nullable
    public Mage getActiveMage() {
        // TODO: Duplicate of #getMage()
        return mage;
    }

    public void setActiveMage(com.elmakers.mine.bukkit.api.magic.Mage mage) {
        if (mage instanceof Mage) {
            this.mage = (Mage)mage;
            armorUpdated();
        }
    }

    @Nullable
    @Override
    public Color getEffectColor() {
        return effectColor == null ? null : effectColor.getColor();
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
        return hotbars.size();
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

        // Handle the case of a path upgrade meaning there are suddenly more spells or brushes available
        boolean updateInventory = limitBrushesToPath || limitSpellsToPath;
        if (!oldPath.equals(path) && updateInventory) {
            closeInventory();
            if (limitSpellsToPath) {
                loadSpells();
            }
            if (limitBrushesToPath) {
                loadBrushes();
            }
            buildInventory();
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

    protected void showActiveIcon(boolean show) {
        if (this.icon == null || this.inactiveIcon == null || this.inactiveIcon.getMaterial() == Material.AIR || this.inactiveIcon.getMaterial() == null) return;
        if (this.icon.getMaterial() == Material.AIR || this.icon.getMaterial() == null) {
            this.icon.setMaterial(DefaultWandMaterial);
        }
        if (show) {
            if (inactiveIconDelay > 0) {
                Plugin plugin = controller.getPlugin();
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        findItem();
                        icon.applyToItem(item);
                    }
                }, inactiveIconDelay * 20 / 1000);
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
        Player player = mage.getPlayer();
        if (player == null) return false;

        if (!controller.hasWandPermission(player, this)) return false;

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
            mage.sendMessage(getMessage("bound").replace("$name", getOwner()));
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

        boolean needsSave = false;
        if (hasId) {
            needsSave = this.checkId() || needsSave;
        } else {
            setProperty("id", null);
        }

        this.mage = mage;
        this.isInOffhand = offhand;
        this.heldSlot = offhand ? OFFHAND_SLOT : player.getInventory().getHeldItemSlot();

        if (mageClassKeys != null && !mageClassKeys.isEmpty()) {
            MageClass mageClass = null;

            for (String mageClassKey : mageClassKeys) {
                mageClass = mage.getClass(mageClassKey);
                if (mageClass != null) break;
            }

            if (mageClass == null) {
                Integer lastSlot = mage.getLastActivatedSlot();
                if (!offhand && (lastSlot == null || lastSlot != player.getInventory().getHeldItemSlot())) {
                    mage.setLastActivatedSlot(player.getInventory().getHeldItemSlot());
                    mage.sendMessage(controller.getMessages().get("mage.no_class").replace("$name", getName()));
                }
                return false;
            }
            setMageClass(mageClass);
            if (!offhand) {
                mage.setActiveClass(mageClass.getKey());
            }
        }

        MageParameters wrapped = new MageParameters(mage, "Wand " + getTemplateKey());
        wrapped.wrap(configuration);
        load(wrapped);

        // This double-load here is not really ideal.
        // Seems hard to prevent without merging Wand construction and activation, though.
        loadProperties();

        mage.setLastActivatedSlot(player.getInventory().getHeldItemSlot());

        // Check for replacement template
        String replacementTemplate = getString("replace_on_activate", "");
        if (!replacementTemplate.isEmpty() && !replacementTemplate.equals(template)) {
            playEffects("replace");
            setTemplate(replacementTemplate);
            loadProperties();
            saveState();
            return activate(mage, offhand);
        }

        // Since these wands can't be opened we will just show them as open when held
        // We have to delay this 1 tick so it happens after the Mage has accepted the Wand
        if ((getMode() != WandMode.INVENTORY || offhand) && controller.isInitialized()) {
            Plugin plugin = controller.getPlugin();
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    showActiveIcon(true);
                    playPassiveEffects("open");
                }
            }, 1);
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
        if (resetManaOnActivate) {
            setMana(0.0f);
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
        if (checkInventoryForUpgrades()) {
            forceUpdate = true;
            needsSave = true;
        }

        // Check for auto-bind
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
            DeprecatedUtils.updateInventory(player);
        }

        return true;
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
        ItemStack newItem = InventoryUtils.getCopy(item);
        Wand newWand = controller.getWand(newItem);
        newWand.saveState();
        return newWand;
    }

    @Override
    @Deprecated
    public boolean configure(Map<String, Object> properties) {
        Map<Object, Object> convertedProperties = new HashMap<>(properties);
        configure(ConfigurationUtils.toConfigurationSection(convertedProperties));
        return true;
    }

    @Override
    public void updated() {
        if (suspendUpdate) return;
        loadProperties();
        if (mage != null) {
            buildInventory();
            if (isInventoryOpen()) {
                updateInventory();
            }
        }
        saveState();
        updateMaxMana(false);
        updateName();
        updateLore();
    }

    @Override
    public boolean isLocked() {
        return this.locked;
    }

    @Override
    public boolean upgradesAllowed() {
        return !this.locked || this.lockedAllowUpgrades;
    }

    @Override
    public void unlock() {
        locked = false;
        setProperty("locked", false);
    }

    public boolean isPassive() {
        return passive;
    }

    @Override
    public boolean canUse(Player player) {
        if (!bound || ownerId == null || ownerId.length() == 0) return true;
        if (controller.hasPermission(player, "Magic.wand.override_bind", false)) return true;

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
        return forceAddSpell(spellName);
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
        suspendUpdate = true;
        if (!super.addSpell(spellName)) {
            suspendUpdate = false;
            return false;
        }
        suspendUpdate = false;

        saveInventory();

        ItemStack spellItem = createSpellItem(spellKey.getKey());
        if (spellItem == null) {
            return false;
        }
        int level = spellKey.getLevel();
        int inventoryCount = inventories.size();
        int spellCount = spells.size();

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
                if (spellCount == 0)
                {
                    if (leftClickAction == WandAction.CAST) {
                        String message = getMessage("spell_instructions", "").replace("$wand", getName());
                        mage.sendMessage(message.replace("$spell", template.getName()));
                    }
                }
                else
                if (spellCount == 1)
                {
                    String controlKey = getControlKey(WandAction.TOGGLE);
                    String inventoryMessage = null;
                    switch (getMode()) {
                    case INVENTORY:
                        inventoryMessage = "inventory_instructions";
                        break;
                    case CHEST:
                        inventoryMessage = "chest_instructions";
                        break;
                    case SKILLS:
                        inventoryMessage = "skills_instructions";
                        break;
                    case CYCLE:
                        inventoryMessage = "cycle_instructions";
                        if (controlKey == null) {
                            controlKey = getControlKey(WandAction.CYCLE);
                        }
                        break;
                    case CAST:
                    case NONE:
                        // Ignore
                        break;
                    }
                    if (controlKey != null && inventoryMessage != null) {
                        controlKey = controller.getMessages().get("controls." + controlKey);
                        mage.sendMessage(getMessage(inventoryMessage, "")
                            .replace("$wand", getName()).replace("$toggle", controlKey).replace("$cycle", controlKey));
                    }
                }
                if (inventoryCount == 1 && inventories.size() > 1)
                {
                    mage.sendMessage(getMessage("page_instructions", "").replace("$wand", getName()));
                }
            }
        }

        return true;
    }

    /**
     * Covers the special case of a wand having spell levels and inventory slots that came from configs,
     * but now we've modified the spells list and need to figure out if we also need to pesist the levels and
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
        String wandKey = "wands." + template + "." + key;
        if (template != null && !template.isEmpty() && controller.getMessages().containsKey(wandKey)) {
            return wandKey;
        }
        return "wand." + key;
    }

    @Override
    protected String parameterizeMessage(String message) {
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

        suspendUpdate = true;
        if (!super.addBrush(materialKey)) {
            suspendUpdate = false;
            return false;
        }
        suspendUpdate = false;

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

        if (mage != null)
        {
            if (brushCount == 0)
            {
                String controlKey = getControlKey(WandAction.TOGGLE);
                if (controlKey != null) {
                    controlKey = controller.getMessages().get("controls." + controlKey);
                    mage.sendMessage(getMessage("brush_instructions")
                            .replace("$wand", getName()).replace("$toggle", controlKey));
                }
            }
            if (inventoryCount == 1 && inventories.size() > 1)
            {
                mage.sendMessage(getMessage("page_instructions").replace("$wand", getName()));
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

        suspendUpdate = true;
        if (!removeBrush(materialKey)) {
            suspendUpdate = false;
            return false;
        }
        suspendUpdate = false;

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

        suspendUpdate = true;
        if (!super.removeSpell(spellName)) {
            suspendUpdate = false;
            return false;
        }
        suspendUpdate = false;

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

    @Override
    public Map<String, String> getOverrides()
    {
        return castOverrides == null ? new HashMap<>() : new HashMap<>(castOverrides);
    }

    @Override
    public void setOverrides(Map<String, String> overrides)
    {
        if (overrides == null) {
            this.castOverrides = null;
        } else {
            this.castOverrides = new HashMap<>(overrides);
        }
        updateOverrides();
    }

    @Override
    public void removeOverride(String key)
    {
        if (castOverrides != null) {
            castOverrides.remove(key);
            updateOverrides();
        }
    }

    @Override
    public void setOverride(String key, String value)
    {
        if (castOverrides == null) {
            castOverrides = new HashMap<>();
        }
        if (value == null || value.length() == 0) {
            castOverrides.remove(key);
        } else {
            castOverrides.put(key, value);
        }
        updateOverrides();
    }

    @Override
    public boolean addOverride(String key, String value)
    {
        if (castOverrides == null) {
            castOverrides = new HashMap<>();
        }
        boolean modified = false;
        if (value == null || value.length() == 0) {
            modified = castOverrides.containsKey(key);
            castOverrides.remove(key);
        } else {
            String current = castOverrides.get(key);
            modified = current == null || !current.equals(value);
            castOverrides.put(key, value);
        }
        if (modified) {
            updateOverrides();
        }

        return modified;
    }

    protected void updateOverrides() {
        if (castOverrides != null && !castOverrides.isEmpty()) {
            setProperty("overrides", castOverrides);
        } else {
            setProperty("overrides", null);
        }
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

    public void setHeldSlot(int slot) {
        this.heldSlot = slot;
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
        storedInventory = CompatibilityUtils.createInventory(null, PLAYER_INVENTORY_SIZE, "Stored Inventory");
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
            if (i != heldSlot) {
                inventory.setItem(i, storedInventory.getItem(i));
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
        Object wandSection = InventoryUtils.getNode(item, WAND_KEY);
        if (wandSection == null) return false;

        String boundValue = InventoryUtils.getMetaString(wandSection, "owner_id");
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

    private void setSpellLevel(String spellKey, int level) {
        if (level <= 1) {
            spellLevels.remove(spellKey);
        } else {
            spellLevels.put(spellKey, level);
        }
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

    protected Map<String, Integer> getSpellInventory() {
        return new HashMap<>(spellInventory);
    }

    protected Map<String, Integer> getBrushInventory() {
        return new HashMap<>(brushInventory);
    }

    public Map<PotionEffectType, Integer> getPotionEffects() {
        return potionEffects;
    }

    @Override
    public float getHealthRegeneration() {
        Integer level = potionEffects.get(PotionEffectType.REGENERATION);
        return level != null && level > 0 ? (float)level : 0;
    }

    @Override
    public float getHungerRegeneration()  {
        Integer level = potionEffects.get(PotionEffectType.SATURATION);
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
        return dropAction;
    }

    @Override
    public WandAction getRightClickAction() {
        return rightClickAction;
    }

    @Override
    public WandAction getLeftClickAction() {
        return leftClickAction;
    }

    @Override
    public WandAction getSwapAction() {
        return swapAction;
    }

    @Override
    public boolean performAction(WandAction action) {
        WandMode mode = getMode();
        switch (action) {
            case CAST:
                cast();
                break;
            case ALT_CAST:
                alternateCast();
                break;
            case ALT_CAST2:
                alternateCast2();
                break;
            case TOGGLE:
                if (mode == WandMode.CYCLE) {
                    cycleActive(1);
                    return true;
                }
                if (mode != WandMode.CHEST && mode != WandMode.INVENTORY && mode != WandMode.SKILLS) return false;
                toggleInventory();
                break;
            case CYCLE:
                cycleActive(1);
                break;
            case CYCLE_REVERSE:
                cycleActive(-1);
                break;
            case CYCLE_HOTBAR:
                if (mode != WandMode.INVENTORY || !isInventoryOpen()) return false;
                if (getHotbarCount() > 1) {
                    cycleHotbar(1);
                } else {
                    closeInventory();
                }
                break;
            case CYCLE_HOTBAR_REVERSE:
                if (mode != WandMode.INVENTORY) return false;
                if (getHotbarCount() > 1) {
                    cycleHotbar(-1);
                } else if (isInventoryOpen()) {
                    closeInventory();
                } else {
                    return false;
                }
                break;
            default:
                return false;
        }

        return true;
    }

    @Override
    public boolean checkAndUpgrade(boolean quiet) {
        WandUpgradePath path = getPath();
        WandUpgradePath nextPath = path != null ? path.getUpgrade() : null;
        if (nextPath == null) {
            return true;
        }
        if (canProgress()) {
            return true;
        }
        if (!path.checkUpgradeRequirements(this, quiet ? null : mage)) {
            return false;
        }
        path.upgrade(this, mage);
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
        return upgrade(ConfigurationUtils.toConfigurationSection(convertedProperties));
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

    public void setCurrentHotbar(int hotbar) {
        this.currentHotbar = hotbar;
        setProperty("hotbar", currentHotbar);
    }

    public int getInventorySize() {
        WandMode mode = getMode();
        if (mode == WandMode.CHEST || mode == WandMode.SKILLS) {
            return 9 * inventoryRows;
        }
        return INVENTORY_SIZE;
    }

    public boolean usesCurrency() {
        if (currencyDisplay == null || !hasSpellProgression || earnMultiplier <= 0 || !currencyDisplay.isValid()) return false;
        if (currencyDisplay.getKey().equals("sp") && !controller.isSPEarnEnabled()) return false;
        return true;
    }

    public boolean usesCurrency(String type) {
        return usesCurrency() && currencyDisplay.getKey().equals(type);
    }

    public boolean usesSP() {
        return controller.isSPEarnEnabled() && usesCurrency("sp");
    }

    @Override
    public int getHeldSlot() {
        return heldSlot;
    }

    @Nullable
    @Override
    protected BaseMagicConfigurable getStorage(MagicPropertyType propertyType) {
        switch (propertyType) {
            case WAND: return this;
            case SUBCLASS: return mageClass;
            case CLASS:
                return mageClass == null ? null : mageClass.getRoot();
            case MAGE:
                return mage == null ? null : mage.getProperties();
        }
        return null;
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
    public WandEffectContext getEffectContext() {
        checkState(mage != null, "Mage is not available");

        if (effectContext == null || (effectContext.getMage() != mage)) {
            // Lazy load or mage has changed
            effectContext = new WandEffectContext(verifyNotNull(mage), this);
        }

        return verifyNotNull(effectContext);
    }

    @Override
    public Wand getWand() {
        return this;
    }

    @Override
    public boolean isInOffhand() {
        return isInOffhand;
    }
}
