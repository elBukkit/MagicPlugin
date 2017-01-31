package com.elmakers.mine.bukkit.wand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.elmakers.mine.bukkit.api.block.BrushMode;
import com.elmakers.mine.bukkit.api.event.AddSpellEvent;
import com.elmakers.mine.bukkit.api.event.SpellUpgradeEvent;
import com.elmakers.mine.bukkit.api.event.WandActivatedEvent;
import com.elmakers.mine.bukkit.api.event.WandPreActivateEvent;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.api.spell.CastingCost;
import com.elmakers.mine.bukkit.api.spell.CostReducer;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellKey;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.WandTemplate;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.block.MaterialBrush;
import com.elmakers.mine.bukkit.effect.builtin.EffectRing;
import com.elmakers.mine.bukkit.heroes.HeroesManager;
import com.elmakers.mine.bukkit.magic.Mage;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.ColorHD;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.elmakers.mine.bukkit.effect.SoundEffect;
import de.slikey.effectlib.util.ParticleEffect;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
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

public class Wand implements CostReducer, com.elmakers.mine.bukkit.api.wand.Wand {
	public final static int INVENTORY_SIZE = 27;
	public final static int PLAYER_INVENTORY_SIZE = 36;
	public final static int HOTBAR_SIZE = 9;
	public final static int HOTBAR_INVENTORY_SIZE = HOTBAR_SIZE - 1;
	public final static float DEFAULT_SPELL_COLOR_MIX_WEIGHT = 0.0001f;
	public final static float DEFAULT_WAND_COLOR_MIX_WEIGHT = 1.0f;
	public static int MAX_LORE_LENGTH = 24;
	public static String DEFAULT_WAND_TEMPLATE = "default";

    public final static String[] EMPTY_PARAMETERS = new String[0];

    public final static Set<String> PROPERTY_KEYS = ImmutableSet.of(
            "active_spell", "active_material",
            "path", "template", "passive",
            "mana", "mana_regeneration", "mana_max", "mana_max_boost",
            "mana_regeneration_boost",
            "mana_per_damage",
            "bound", "soul", "has_uses", "uses", "upgrade", "indestructible",
            "undroppable",
            "consume_reduction", "cost_reduction", "cooldown_reduction",
            "effect_bubbles", "effect_color",
            "effect_particle", "effect_particle_count", "effect_particle_data",
            "effect_particle_interval",
            "effect_particle_min_velocity",
            "effect_particle_radius", "effect_particle_offset",
            "effect_sound", "effect_sound_interval", "effect_sound_pitch",
            "effect_sound_volume",
            "cast_spell", "cast_parameters", "cast_interval",
            "cast_min_velocity", "cast_velocity_direction",
            "hotbar_count", "hotbar",
            "icon", "icon_inactive", "icon_inactive_delay", "mode",
            "brush_mode",
            "keep", "locked", "quiet", "force", "randomize", "rename",
            "rename_description",
            "power", "overrides",
            "protection", "protection_physical", "protection_projectiles",
            "protection_falling", "protection_fire", "protection_explosions",
            "potion_effects",
            "materials", "spells", "powered", "protected", "heroes",
            "enchant_count", "max_enchant_count",
            "quick_cast", "left_click", "right_click", "drop", "swap"
    );

    private final static Set<String> HIDDEN_PROPERTY_KEYS = ImmutableSet.of(
            "id", "owner", "owner_id", "name", "description",
            "organize", "alphabetize", "fill", "stored", "upgrade_icon",
            "mana_timestamp", "upgrade_template", "custom_properties",
            // For legacy wands
            "haste",
            "health_regeneration", "hunger_regeneration",
            "xp", "xp_regeneration", "xp_max", "xp_max_boost",
            "xp_regeneration_boost",
            "mode_cast", "mode_drop"
    );

    /**
     * Set of properties that should be stored as NBT tags on a wand.
     */
    private final static Set<String> ALL_PROPERTY_KEYS_SET = Sets.union(
            PROPERTY_KEYS, HIDDEN_PROPERTY_KEYS);

    protected @Nonnull ItemStack item;
    protected final @Nonnull MagicController controller;

    /**
     * The currently active mage.
     *
     * Is only set when the wand is active of when the wand is
     * used for off-hand casting.
     */
    protected @Nullable Mage mage;
	
	// Cached state
	private String id = "";
	private List<Inventory> hotbars;
	private List<Inventory> inventories;
    private Map<String, Integer> spells = new HashMap<>();
    private Map<String, Integer> spellLevels = new HashMap<>();
    private Map<String, Integer> brushes = new HashMap<>();

    /**
     * Custom properties that 3rd party systems can set on a wand for their
     * internal use.
     */
    private Map<String, String> customProperties = new HashMap<>();
	
	private String activeSpell = "";
	private String activeMaterial = "";
	protected String wandName = "";
	protected String description = "";
	private String owner = "";
    private String ownerId = "";
	private String template = "";
    private String path = "";
    private boolean superProtected = false;
    private boolean superPowered = false;
    private boolean glow = false;
	private boolean soul = false;
    private boolean bound = false;
	private boolean indestructible = false;
    private boolean undroppable = false;
	private boolean keep = false;
    private boolean passive = false;
	private boolean autoOrganize = false;
    private boolean autoAlphabetize = false;
	private boolean autoFill = false;
	private boolean isUpgrade = false;
    private boolean randomize = false;
    private boolean rename = false;
    private boolean renameDescription = false;
    private boolean quickCast = false;
    private boolean quickCastDisabled = false;
    private boolean manualQuickCastDisabled = false;
	
	private WandAction leftClickAction = WandAction.CAST;
	private WandAction rightClickAction = WandAction.TOGGLE;
	private WandAction dropAction = WandAction.CYCLE_HOTBAR;
	private WandAction swapAction = WandAction.NONE;

	private MaterialAndData icon = null;
    private MaterialAndData upgradeIcon = null;
    private MaterialAndData inactiveIcon = null;
    private int inactiveIconDelay = 0;
    private String upgradeTemplate = null;
	
	protected float costReduction = 0;
	protected float consumeReduction = 0;
    protected float cooldownReduction = 0;
    protected float damageReduction = 0;
    protected float damageReductionPhysical = 0;
    protected float damageReductionProjectiles = 0;
    protected float damageReductionFalling = 0;
    protected float damageReductionFire = 0;
    protected float damageReductionExplosions = 0;
    private float power = 0;

    private int maxEnchantCount = 0;
    private int enchantCount = 0;

	private boolean hasInventory = false;
	private boolean locked = false;
    private boolean forceUpgrade = false;
    private boolean isHeroes = false;
	private int uses = 0;
    private boolean hasUses = false;
    private boolean isSingleUse = false;
	private float mana = 0;

    private float manaMaxBoost = 0;
    private float manaRegenerationBoost = 0;
	private int manaRegeneration = 0;
	private int manaMax = 0;
    private long lastManaRegeneration = 0;
    private float manaPerDamage = 0;
    private int effectiveManaMax = 0;
    private int effectiveManaRegeneration = 0;
	
	private ColorHD effectColor = null;
	private float effectColorSpellMixWeight = DEFAULT_SPELL_COLOR_MIX_WEIGHT;
	private float effectColorMixWeight = DEFAULT_WAND_COLOR_MIX_WEIGHT;	
	private ParticleEffect effectParticle = null;
	private float effectParticleData = 0;
	private int effectParticleCount = 0;
	private int effectParticleInterval = 0;
    private double effectParticleMinVelocity = 0;
    private double effectParticleRadius = 0.2;
    private double effectParticleOffset = 0.3;
	private boolean effectBubbles = false;
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

    private boolean effectBubblesApplied = false;
    private boolean hasSpellProgression = false;

    private long lastLocationTime;
    private Vector lastLocation;

    private long lastSoundEffect;
    private long lastParticleEffect;
    private long lastSpellCast;
	
	// Inventory functionality
	
	private WandMode mode = null;
    private WandMode brushMode = null;
	private int openInventoryPage = 0;
	private boolean inventoryIsOpen = false;
	private Inventory displayInventory = null;
	private int currentHotbar = 0;
	
	// Kinda of a hacky initialization optimization :\
	private boolean suspendSave = false;
	
    public static WandManaMode manaMode = WandManaMode.BAR;
    public static WandManaMode spMode = WandManaMode.NUMBER;
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
    public static boolean Unbreakable = false;
	public static boolean Undroppable = true;
    public static SoundEffect inventoryOpenSound = null;
    public static SoundEffect inventoryCloseSound = null;
    public static SoundEffect inventoryCycleSound = null;
	public static SoundEffect noActionSound = null;
    public static String WAND_KEY = "wand";
    public static String WAND_SELF_DESTRUCT_KEY = null;
    public static byte HIDE_FLAGS = 60;
	public static String brushSelectSpell = "";

    private Inventory storedInventory = null;
    private int storedSlot;

    /**
     * @deprecated Use {@link MagicController#getWand(ItemStack)}.
     */
    @Deprecated
    public Wand(MagicController controller, ItemStack itemStack) {
        Preconditions.checkNotNull(controller);
        Preconditions.checkNotNull(itemStack);

        this.controller = controller;
        wandName = controller.getMessages().get("wand.default_name");
        hotbars = new ArrayList<>();
		setHotbarCount(1);
		if (itemStack.getType() == Material.AIR) {
			itemStack.setType(DefaultWandMaterial);
		}
		this.icon = new MaterialAndData(itemStack);
		inventories = new ArrayList<>();
        item = itemStack;
		loadState();
        updateName();
        updateLore();
	}

	protected void setHotbarCount(int count) {
		hotbars.clear();
		while (hotbars.size() < count) {
			hotbars.add(CompatibilityUtils.createInventory(null, HOTBAR_INVENTORY_SIZE, "Wand"));
		}
		while (hotbars.size() > count) {
			hotbars.remove(0);
		}
	}
	
	public Wand(MagicController controller) {
		this(controller, DefaultWandMaterial, (short) 0);
	}

    public Wand(MagicController controller, ConfigurationSection config) {
        this(controller, DefaultWandMaterial, (short)0);
        loadProperties(config);
        updateName();
        updateLore();
        saveItemState();
    }
	
	protected Wand(MagicController controller, String templateName) throws UnknownWandException {
		this(controller);
		suspendSave = true;
		String wandName = controller.getMessages().get("wand.default_name");
		String wandDescription = "";

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
		ConfigurationSection wandConfig = template.getConfiguration();

		if (wandConfig == null) {
			throw new UnknownWandException(templateName);
		}

		// Default to template names, override with localizations
		wandName = wandConfig.getString("name", wandName);
		wandName = controller.getMessages().get("wands." + templateName + ".name", wandName);
		wandDescription = wandConfig.getString("description", wandDescription);
		wandDescription = controller.getMessages().get("wands." + templateName + ".description", wandDescription);

		// Load all properties
		loadProperties(wandConfig);

		// Add vanilla enchantments
		InventoryUtils.applyEnchantments(item, wandConfig.getConfigurationSection("enchantments"));

		// Enchant, if an enchanting level was provided
		if (level > 0) {
			// Account for randomized locked wands
			boolean wasLocked = locked;
			locked = false;
			randomize(level, false, null, true);
			locked = wasLocked;
		}
		setDescription(wandDescription);
		setName(wandName);

        // Don't randomize now if set to randomize later
        // Otherwise, do this here so the description updates
        if (!randomize) {
            randomize();
        }

        setTemplate(templateName);
		suspendSave = false;
		saveItemState();
	}
	
	public Wand(MagicController controller, Material icon, short iconData) {
		// This will make the Bukkit ItemStack into a real ItemStack with NBT data.
		this(controller, InventoryUtils.makeReal(new ItemStack(icon, 1, iconData)));
        saveItemState();
		updateName();
	}
	
	@Override
    public void unenchant() {
		item = new ItemStack(item.getType(), 1, item.getDurability());
	}
	
	public void setIcon(Material material, byte data) {
		setIcon(material == null ? null : new MaterialAndData(material, data));
	}

	public void updateIcon() {
		setIcon(icon);
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
		updateIcon();
	}
	
	@Override
	public void setIcon(com.elmakers.mine.bukkit.api.block.MaterialAndData materialData) {
		if (materialData instanceof MaterialAndData) {
			setIcon((MaterialAndData)materialData);
		} else {
			setIcon(new MaterialAndData(materialData));
		}
	}
	
	public void setIcon(MaterialAndData materialData) {
        if (materialData == null || !materialData.isValid()) return;
		if (materialData.getMaterial() == Material.AIR || materialData.getMaterial() == null) {
			materialData.setMaterial(DefaultWandMaterial);
		}
		icon = materialData;

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
		CompatibilityUtils.hideFlags(item, HIDE_FLAGS);
	}
	
	@Override
    public void makeUpgrade() {
        if (!isUpgrade) {
            isUpgrade = true;
            String oldName = wandName;
            wandName = controller.getMessages().get("wand.upgrade_name");
            wandName = wandName.replace("$name", oldName);
            description = controller.getMessages().get("wand.upgrade_default_description");
            if (template != null && template.length() > 0) {
                description = controller.getMessages().get("wands." + template + ".upgrade_description", description);
            }
            setIcon(DefaultUpgradeMaterial, (byte) 0);
            saveItemState();
            updateName(true);
            updateLore();
        }
	}

    public void newId() {
        if (!this.isUpgrade && !this.isSingleUse) {
            id = UUID.randomUUID().toString();
        } else {
            id = null;
        }
    }

    public void checkId() {
        if ((id == null || id.length() == 0) && !this.isUpgrade) {
            newId();
            saveItemState();
        }
    }

    @Override
    public String getId() {
        return isSingleUse ? template : id;
    }

    public float getManaRegenerationBoost() {
        return manaRegenerationBoost;
    }

    public float getManaMaxBoost() {
        return manaMaxBoost;
    }
	
	@Override
    public int getManaRegeneration() {
		return manaRegeneration;
	}

	@Override
	public int getManaMax() {
		return manaMax;
	}

    @Override
    public void setMana(float mana) {
        this.mana = Math.max(0, mana);
    }

    @Override
    public void setManaMax(int manaMax) {
        this.manaMax = Math.max(0, manaMax);
    }

	@Override
	public float getMana() {
		return mana;
	}

	@Override
	public void removeMana(float amount) {
        if (isHeroes && mage != null) {
            HeroesManager heroes = controller.getHeroes();
            if (heroes != null) {
                heroes.removeMana(mage.getPlayer(), (int)Math.ceil(amount));
            }
        }
		mana = Math.max(0,  mana - amount);
		updateMana();
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
		// Don't allow dropping wands while the inventory is open.
        return undroppable || isInventoryOpen();
    }
	
	public boolean isUpgrade() {
		return isUpgrade;
	}
	
	public boolean usesMana() {
        if (isCostFree()) return false;
		return manaMax > 0 || isHeroes;
	}

	@Override
    public float getCooldownReduction() {
		return controller.getCooldownReduction() + cooldownReduction * controller.getMaxCooldownReduction();
	}

    @Override
	public float getCostReduction() {
		if (isCostFree()) return 1.0f;
		return controller.getCostReduction() + costReduction * controller.getMaxCostReduction();
	}

	@Override
	public float getConsumeReduction() {
		return consumeReduction;
	}

    @Override
    public float getCostScale() {
        return 1;
    }
	
	public void setCooldownReduction(float reduction) {
		cooldownReduction = reduction;
	}
	
	public boolean getHasInventory() {
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
    public boolean isCostFree() {
		return costReduction > 1;
	}

	@Override
    public boolean isConsumeFree() {
		return consumeReduction >= 1;
	}
	
	@Override
    public boolean isCooldownFree() {
		return cooldownReduction > 1;
	}

	public float getDamageReduction() {
		return damageReduction;
	}

	public float getDamageReductionPhysical() {
		return damageReductionPhysical;
	}
	
	public float getDamageReductionProjectiles() {
		return damageReductionProjectiles;
	}

	public float getDamageReductionFalling() {
		return damageReductionFalling;
	}

	public float getDamageReductionFire() {
		return damageReductionFire;
	}

	public float getDamageReductionExplosions() {
		return damageReductionExplosions;
	}
	
	@Override
    public String getName() {
		return wandName;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getOwner() {
		return owner;
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
                worth += spell.getWorth();
            }
        }
        return worth;
    }

	@Override
    public void setName(String name) {
		wandName = ChatColor.stripColor(name);
		updateName();
	}
	
	public void setTemplate(String templateName) {
		this.template = templateName;
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
    public com.elmakers.mine.bukkit.api.wand.WandUpgradePath getPath() {
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
		updateLore();
	}
	
	public boolean tryToOwn(Player player) {
        if (ownerId == null || ownerId.length() == 0) {
            takeOwnership(player);
			return true;
        }
		
		return false;
	}

    protected void takeOwnership(Player player) {
        takeOwnership(player, controller.bindWands(), controller.keepWands());
    }

	public void takeOwnership(Player player, boolean setBound, boolean setKeep) {
        if (mage != null && (ownerId == null || ownerId.length() == 0) && quietLevel < 2)
        {
            mage.sendMessage(getMessage("bound_instructions", "").replace("$wand", getName()));
            Spell spell = getActiveSpell();
            if (spell != null)
            {
                String message = getMessage("spell_instructions", "").replace("$wand", getName());
                mage.sendMessage(message.replace("$spell", spell.getName()));
            }
            if (spells.size() > 1)
            {
                mage.sendMessage(getMessage("inventory_instructions", "").replace("$wand", getName()));
            }
            com.elmakers.mine.bukkit.api.wand.WandUpgradePath path = getPath();
            if (path != null)
            {
                String message = getMessage("enchant_instructions", "").replace("$wand", getName());
                mage.sendMessage(message);
            }
        }
        owner = ChatColor.stripColor(player.getDisplayName());
        ownerId = player.getUniqueId().toString();
		if (setBound) {
			bound = true;
		}
		if (setKeep) {
			keep = true;
		}
		updateLore();
	}
	
	@Override
    public ItemStack getItem() {
		return item;
	}
	
	@Override
	public com.elmakers.mine.bukkit.api.block.MaterialAndData getIcon() {
		return icon;
	}

	@Override
	public com.elmakers.mine.bukkit.api.block.MaterialAndData getInactiveIcon() {
		return inactiveIcon;
	}
	
	protected List<Inventory> getAllInventories() {
		List<Inventory> allInventories = new ArrayList<>(inventories.size() + hotbars.size());
        allInventories.addAll(hotbars);
		allInventories.addAll(inventories);
		return allInventories;
	}
	
	@Override
    public Set<String> getSpells() {
		return spells.keySet();
	}

    protected String getSpellString() {
		Set<String> spellNames = new TreeSet<>();
        for (Map.Entry<String, Integer> spellSlot : spells.entrySet()) {
            Integer slot = spellSlot.getValue();
            String spellKey = spellSlot.getKey();
            if (slot != null) {
                spellKey += "@" + slot;
            }
            spellNames.add(spellKey);
        }
		return StringUtils.join(spellNames, ",");
	}

	@Override
    public Set<String> getBrushes() {
		return brushes.keySet();
	}

    protected String getMaterialString() {
		Set<String> materialNames = new TreeSet<>();
        for (Map.Entry<String, Integer> brushSlot : brushes.entrySet()) {
            Integer slot = brushSlot.getValue();
            String materialKey = brushSlot.getKey();
            if (slot != null) {
                materialKey += "@" + slot;
            }
            materialNames.add(materialKey);
        }
		return StringUtils.join(materialNames, ",");
	}
	
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
	
	protected void addToInventory(ItemStack itemStack) {
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
		List<Inventory> checkInventories = getAllInventories();
		boolean added = false;

		for (Inventory inventory : checkInventories) {
			HashMap<Integer, ItemStack> returned = inventory.addItem(itemStack);

			if (returned.size() == 0) {
				added = true;
				break;
			}
		}
		if (!added) {
			Inventory newInventory = CompatibilityUtils.createInventory(null, INVENTORY_SIZE, "Wand");
			newInventory.addItem(itemStack);
			inventories.add(newInventory);
		}
	}
	
	protected Inventory getDisplayInventory() {
		if (displayInventory == null) {
            int inventorySize = INVENTORY_SIZE + HOTBAR_SIZE;
			displayInventory = CompatibilityUtils.createInventory(null, inventorySize, "Wand");
		}
		
		return displayInventory;
	}

    protected @Nonnull Inventory getInventoryByIndex(int inventoryIndex) {
        // Auto create
        while (inventoryIndex >= inventories.size()) {
            inventories.add(CompatibilityUtils.createInventory(null, INVENTORY_SIZE, "Wand"));
        }

        return inventories.get(inventoryIndex);
    }

	protected int getHotbarSize() {
		return hotbars.size() * HOTBAR_INVENTORY_SIZE;
	}

    protected @Nonnull Inventory getInventory(int slot) {
        int hotbarSize = getHotbarSize();

        if (slot < hotbarSize) {
            return hotbars.get(slot / HOTBAR_INVENTORY_SIZE);
        }

        int inventoryIndex = (slot - hotbarSize) / INVENTORY_SIZE;
        return getInventoryByIndex(inventoryIndex);
    }

    protected int getInventorySlot(int slot) {
        int hotbarSize = getHotbarSize();

        if (slot < hotbarSize) {
            return slot % HOTBAR_INVENTORY_SIZE;
        }

        return ((slot - hotbarSize) % INVENTORY_SIZE);
    }

	protected void addToInventory(ItemStack itemStack, Integer slot) {
		if (slot == null) {
			addToInventory(itemStack);
			return;
		}
		Inventory inventory = getInventory(slot);
		slot = getInventorySlot(slot);
		
		ItemStack existing = inventory.getItem(slot);
		inventory.setItem(slot, itemStack);
		
		if (existing != null && existing.getType() != Material.AIR) {
			addToInventory(existing);
		}
	}
	
	protected void parseInventoryStrings(String spellString, String materialString) {
        // Force an update of the display inventory since chest mode is a different size
        displayInventory = null;

		for (Inventory hotbar : hotbars) {
			hotbar.clear();
		}
		inventories.clear();
        spells.clear();
        spellLevels.clear();
        brushes.clear();

		// Support YML-List-As-String format
		spellString = spellString.replaceAll("[\\]\\[]", "");
		String[] spellNames = StringUtils.split(spellString, ",");
		for (String spellName : spellNames)
        {
			String[] pieces = spellName.split("@");
			Integer slot = parseSlot(pieces);

            // Handle aliases and upgrades smoothly
            String loadedKey = pieces[0].trim();
            SpellKey spellKey = new SpellKey(loadedKey);
            SpellTemplate spell = controller.getSpellTemplate(loadedKey);
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
                if (currentLevel == null || currentLevel < spellKey.getLevel()) {
                    spellLevels.put(spellKey.getBaseKey(), spellKey.getLevel());
                    spells.put(spellKey.getKey(), slot);
                    if (currentLevel != null)
                    {
                        SpellKey oldKey = new SpellKey(spellKey.getBaseKey(), currentLevel);
                        spells.remove(oldKey.getKey());
                    }
                }
                if (activeSpell == null || activeSpell.length() == 0)
                {
                    activeSpell = spellKey.getKey();
                }
            }
            ItemStack itemStack = createSpellItem(spell, "", controller, getActivePlayer(), this, false);
			if (itemStack != null)
            {
				addToInventory(itemStack, slot);
            }
		}
		materialString = materialString.replaceAll("[\\]\\[]", "");
		String[] materialNames = StringUtils.split(materialString, ",");
        WandMode brushMode = getBrushMode();
		for (String materialName : materialNames) {
			String[] pieces = materialName.split("@");
			Integer slot = parseSlot(pieces);
			String materialKey = pieces[0].trim();
            brushes.put(materialKey, slot);
            boolean addToInventory = brushMode == WandMode.INVENTORY || (MaterialBrush.isSpecialMaterialKey(materialKey) && !MaterialBrush.isSchematic(materialKey));
            if (addToInventory)
            {
                ItemStack itemStack = createBrushIcon(materialKey);
                if (itemStack == null) {
                    controller.getPlugin().getLogger().warning("Unable to create material icon for key " + materialKey);
                    continue;
                }
                if (activeMaterial == null || activeMaterial.length() == 0) activeMaterial = materialKey;
                addToInventory(itemStack, slot);
            }
		}
		updateHasInventory();
        if (openInventoryPage >= inventories.size()) {
            openInventoryPage = 0;
        }
	}

    protected ItemStack createSpellIcon(SpellTemplate spell) {
        return createSpellItem(spell, "", controller, getActivePlayer(), this, false);
    }

    public static ItemStack createSpellItem(String spellKey, MagicController controller, Wand wand, boolean isItem) {
        String[] split = spellKey.split(" ", 2);
        return createSpellItem(controller.getSpellTemplate(split[0]), split.length > 1 ? split[1] : "", controller, wand == null ? null : wand.getActivePlayer(), wand, isItem);
    }

    public static ItemStack createSpellItem(String spellKey, MagicController controller, com.elmakers.mine.bukkit.api.magic.Mage mage, Wand wand, boolean isItem) {
        String[] split = spellKey.split(" ", 2);
        return createSpellItem(controller.getSpellTemplate(split[0]), split.length > 1 ? split[1] : "", controller, mage, wand, isItem);
    }

	public static ItemStack createSpellItem(SpellTemplate spell, String args, MagicController controller, com.elmakers.mine.bukkit.api.magic.Mage mage, Wand wand, boolean isItem) {
		if (spell == null) return null;
        String iconURL = spell.getIconURL();

        ItemStack itemStack = null;
        if (iconURL != null && controller.isUrlIconsEnabled())
        {
            itemStack = InventoryUtils.getURLSkull(iconURL);
        }

        if (itemStack == null)
        {
            ItemStack originalItemStack = null;
            com.elmakers.mine.bukkit.api.block.MaterialAndData icon = spell.getIcon();
            if (icon == null) {
                controller.getPlugin().getLogger().warning("Unable to create spell icon for " + spell.getName() + ", missing material");
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
					controller.getPlugin().getLogger().warning("Unable to create spell icon for " + spell.getKey() + " with material " + iconName);
				}
                return originalItemStack;
            }
        }
		InventoryUtils.makeUnbreakable(itemStack);
        InventoryUtils.hideFlags(itemStack, (byte)63);
		updateSpellItem(controller.getMessages(), itemStack, spell, args, mage, wand, wand == null ? null : wand.activeMaterial, isItem);
		return itemStack;
	}
	
	protected ItemStack createBrushIcon(String materialKey) {
		return createBrushItem(materialKey, controller, this, false);
	}
	
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
	
	public void checkItem(ItemStack newItem) {
		if (newItem.getAmount() > item.getAmount()) {
			item.setAmount(newItem.getAmount());
		}
	}

	@Override
	public void saveState() {
		saveInventory();
		saveItemState();
	}

    public void saveItemState() {
		if (suspendSave) return;

        // Make sure we're on the current item instance
        if (mage != null) {
            Player player = mage.getPlayer();
            if (player != null) {
                ItemStack itemInHand = player.getInventory().getItemInMainHand();
                String itemId = getWandId(itemInHand);
                if (itemId != null && itemId.equals(id)) {
                    item = itemInHand;
					updateIcon();
                    updateName();
                    updateLore();
                }
            }
        }

        if (item.getType() == Material.AIR) return;

        ConfigurationSection stateNode = new MemoryConfiguration();
        saveProperties(stateNode);

		Object wandNode = InventoryUtils.createNode(item, WAND_KEY);
		if (wandNode == null) {
			controller.getLogger().warning("Failed to save wand state for wand to : " + item);
            Thread.dumpStack();
		} else {
            // Save custom keys
            if(!customProperties.isEmpty()) {
                InventoryUtils.saveTagsToNBT(stateNode, wandNode, customProperties.keySet());
            }

            InventoryUtils.saveTagsToNBT(stateNode, wandNode, ALL_PROPERTY_KEYS_SET);
        }

		if (mage != null) {
			Player player = mage.getPlayer();
			if (player != null) {
				// Update the stored item, too- in case we save while the
				// inventory is open.
				if (storedInventory != null) {
					int currentSlot = player.getInventory().getHeldItemSlot();
					ItemStack storedItem = storedInventory.getItem(currentSlot);
					String storedId = getWandId(storedItem);
					if (storedId != null && storedId.equals(id)) {
						storedInventory.setItem(currentSlot, item);
					}
				}
			}
		}
	}

    protected void loadState() {
        ConfigurationSection stateNode = itemToConfig(item, new MemoryConfiguration());

        if(stateNode != null) {
            loadProperties(stateNode);
        }
    }

    public static ConfigurationSection itemToConfig(ItemStack item, ConfigurationSection stateNode) {
        Object wandNode = InventoryUtils.getNode(item, WAND_KEY);

        if (wandNode == null) {
            return null;
        }

        InventoryUtils.loadTagsFromNBT(stateNode, wandNode, ALL_PROPERTY_KEYS_SET);

        // Do a second pass when there are custom properties
        String[] propertyKeys = customPropertiesFromConfig(stateNode);
        if(propertyKeys.length > 0) {
            InventoryUtils.loadTagsFromNBT(stateNode, wandNode, Arrays.asList(propertyKeys));
        }

        return stateNode;
    }

    public static String[] customPropertiesFromConfig(ConfigurationSection stateNode) {
        return StringUtils.split(stateNode.getString("custom_properties", ""), ",");
    }

    public static void configToItem(ConfigurationSection itemSection, ItemStack item) {
        ConfigurationSection stateNode = itemSection.getConfigurationSection("wand");
        Object wandNode = InventoryUtils.createNode(item, Wand.WAND_KEY);
        if (wandNode != null) {
            String[] propertyKeys = customPropertiesFromConfig(stateNode);
            if(propertyKeys.length > 0) {
                InventoryUtils.saveTagsToNBT(stateNode, wandNode, Arrays.asList(propertyKeys));
            }

            InventoryUtils.saveTagsToNBT(stateNode, wandNode, Wand.ALL_PROPERTY_KEYS_SET);
        }
    }

    protected String getPotionEffectString() {
        if (potionEffects.size() == 0) return null;
        Collection<String> effectStrings = new ArrayList<>();
        for (Map.Entry<PotionEffectType, Integer> entry : potionEffects.entrySet()) {
            String effectString = entry.getKey().getName();
            if (entry.getValue() > 0) {
                effectString += ":" + entry.getValue();
            }
            effectStrings.add(effectString);
        }
        return StringUtils.join(effectStrings, ",");
    }

    @Override
    public void save(ConfigurationSection node, boolean filtered) {
        saveProperties(node);

        // Filter out some fields
        if (filtered) {
            node.set("id", null);
            node.set("owner_id", null);
            node.set("owner", null);
            node.set("template", null);

            // This is super hacky, copyied from InventoryUtils.saveTagsToNBT
            // But it generally reproduces what you'd have to do in the config
            // to recreate this wand, sooooo
            Collection<String> keys = node.getKeys(false);
            for (String key : keys) {
                String value = node.getString(key);
                if (value == null || value.length() == 0 || value.equals("0") || value.equals("0.0") || value.equals("false")) {
                    node.set(key, null);
                }
            }
        }

        // Change some CSV to lists
        if (node.contains("spells")) {
            node.set("spells", StringUtils.split(node.getString("spells"), ","));
        }
        if (node.contains("materials")) {
            node.set("materials", StringUtils.split(node.getString("materials"), ","));
        }
        if (node.contains("overrides")) {
            node.set("overrides", StringUtils.split(node.getString("overrides"), ","));
        }
        if (node.contains("potion_effects")) {
            node.set("potion_effects", StringUtils.split(node.getString("potion_effects"), ","));
        }
    }

	public void saveProperties(ConfigurationSection node) {
		node.set("id", id);
		node.set("materials", getMaterialString());
		node.set("spells", getSpellString());
        node.set("potion_effects", getPotionEffectString());
		node.set("hotbar_count", hotbars.size());
		node.set("hotbar", currentHotbar);
		node.set("active_spell", activeSpell);
		node.set("active_material", activeMaterial);
		node.set("name", wandName);
		node.set("description", description);
		node.set("owner", owner);
        node.set("owner_id", ownerId);

		node.set("cost_reduction", costReduction);
		node.set("consume_reduction", consumeReduction);
		node.set("cooldown_reduction", cooldownReduction);
		node.set("power", power);
        node.set("enchant_count", enchantCount);
        node.set("max_enchant_count", maxEnchantCount);
		node.set("protection", damageReduction);
		node.set("protection_physical", damageReductionPhysical);
		node.set("protection_projectiles", damageReductionProjectiles);
		node.set("protection_falling", damageReductionFalling);
		node.set("protection_fire", damageReductionFire);
		node.set("protection_explosions", damageReductionExplosions);
		node.set("mana", mana);
		node.set("mana_regeneration", manaRegeneration);
		node.set("mana_max", manaMax);
        node.set("mana_max_boost", manaMaxBoost);
        node.set("mana_regeneration_boost", manaRegenerationBoost);
        node.set("mana_timestamp", lastManaRegeneration);
        node.set("mana_per_damage", manaPerDamage);
		node.set("uses", uses);
        node.set("has_uses", hasUses);
		node.set("locked", locked);
		node.set("effect_color", effectColor == null ? "none" : effectColor.toString());
		node.set("effect_bubbles", effectBubbles);
		node.set("effect_particle_data", Float.toString(effectParticleData));
		node.set("effect_particle_count", effectParticleCount);
        node.set("effect_particle_min_velocity", effectParticleMinVelocity);
		node.set("effect_particle_interval", effectParticleInterval);
        node.set("effect_particle_radius", effectParticleRadius);
        node.set("effect_particle_offset", effectParticleOffset);
		node.set("effect_sound_interval", effectSoundInterval);

        node.set("cast_interval", castInterval);
        node.set("cast_min_velocity", castMinVelocity);
        String directionString = null;
        if (castVelocityDirection != null) {
            directionString = ConfigurationUtils.fromVector(castVelocityDirection);
        }
        node.set("cast_velocity_direction", directionString);
        node.set("cast_spell", castSpell);
        String castParameterString = null;
        if (castParameters != null) {
            castParameterString = ConfigurationUtils.getParameters(castParameters);
        }
        node.set("cast_parameters", castParameterString);

		node.set("quiet", quietLevel);
        node.set("passive", passive);
		node.set("keep", keep);
        node.set("randomize", randomize);
        node.set("rename", rename);
        node.set("rename_description", renameDescription);
		node.set("bound", bound);
        node.set("soul", soul);
        node.set("force", forceUpgrade);
		node.set("indestructible", indestructible);
        node.set("protected", superProtected);
        node.set("powered", superPowered);
        node.set("glow", glow);
        node.set("undroppable", undroppable);
        node.set("heroes", isHeroes);
		node.set("fill", autoFill);
		node.set("upgrade", isUpgrade);
		node.set("organize", autoOrganize);
        node.set("alphabetize", autoAlphabetize);
        if (castOverrides != null && castOverrides.size() > 0) {
            Collection<String> parameters = new ArrayList<>();
            for (Map.Entry<String, String> entry : castOverrides.entrySet()) {
                String value = entry.getValue();
                parameters.add(entry.getKey() + " " + value.replace(",", "\\,"));
            }
            node.set("overrides", StringUtils.join(parameters, ","));
        } else {
            node.set("overrides", null);
        }
		if (effectSound != null) {
			node.set("effect_sound", effectSound.toString());
		} else {
			node.set("effectSound", null);
		}
		if (effectParticle != null) {
			node.set("effect_particle", effectParticle.name());
		} else {
			node.set("effect_particle", null);
		}
		if (mode != null) {
			node.set("mode", mode.name());
		} else {
			node.set("mode", null);
		}
		
		node.set("left_click", leftClickAction.name());
		node.set("right_click", rightClickAction.name());
		node.set("drop", dropAction.name());
		node.set("swap", swapAction.name());
		
        if (quickCast) {
            node.set("quick_cast", "true");
        } else if (quickCastDisabled) {
            if (manualQuickCastDisabled) {
                node.set("quick_cast", "disable");
            } else {
                node.set("quick_cast", "manual");
            }
        } else {
            node.set("quick_cast", null);
        }
        if (brushMode != null) {
            node.set("brush_mode", brushMode.name());
        } else {
            node.set("brush_mode", null);
        }
		if (icon != null && icon.getMaterial() != null && icon.getMaterial() != Material.AIR) {
			String iconKey = icon.getKey();
			if (iconKey != null && iconKey.length() > 0) {
				node.set("icon", iconKey);
			} else {
				node.set("icon", null);
			}
		} else {
			node.set("icon", null);
		}
        if (inactiveIcon != null && inactiveIcon.getMaterial() != null && inactiveIcon.getMaterial() != Material.AIR) {
            String iconKey = inactiveIcon.getKey();
            if (iconKey != null && iconKey.length() > 0) {
                node.set("icon_inactive", iconKey);
            } else {
                node.set("icon_inactive", null);
            }
        } else {
            node.set("icon_inactive", null);
        }
        node.set("icon_inactive_delay", inactiveIconDelay);
        if (upgradeIcon != null) {
            String iconKey = upgradeIcon.getKey();
            if (iconKey != null && iconKey.length() > 0) {
                node.set("upgrade_icon", iconKey);
            } else {
                node.set("upgrade_icon", null);
            }
        } else {
            node.set("upgrade_icon", null);
        }
        if (upgradeTemplate != null && upgradeTemplate.length() > 0) {
            node.set("upgrade_template", upgradeTemplate);
        } else {
            node.set("upgrade_template", null);
        }
		if (template != null && template.length() > 0) {
			node.set("template", template);
		} else {
			node.set("template", null);
		}
        if (path != null && path.length() > 0) {
            node.set("path", path);
        } else {
            node.set("path", null);
        }

        // Make sure to put this at the end of saveProperties so it cannot
        // override anything
        Set<String> customAdded = new HashSet<>(customProperties.size());
        for(Map.Entry<String, String> entry : customProperties.entrySet()) {
            if(!node.isSet(entry.getKey())) {
                node.set(entry.getKey(), entry.getValue());
                customAdded.add(entry.getKey());
            }
        }

        if(!customAdded.isEmpty()) {
            node.set("custom_properties", StringUtils.join(customAdded, ","));
        }
    }
	
	public void loadProperties(ConfigurationSection wandConfig) {
		loadProperties(wandConfig, false);
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
	}

    public static void addPotionEffects(Map<PotionEffectType, Integer> effects, ItemStack item) {
        addPotionEffects(effects, getWandString(item, "potion_effects"));
    }

    protected static void addPotionEffects(Map<PotionEffectType, Integer> effects, String effectsString) {
        if (effectsString == null || effectsString.isEmpty()) {
            return;
        }
        effectsString = effectsString.replaceAll("[\\]\\[]", "");
        String[] effectStrings = StringUtils.split(effectsString, ",");
        for (String effectString : effectStrings) {
            try {
                effectString = effectString.trim();
                PotionEffectType type;
                int power = 0;
                if (effectString.contains(":")) {
                    String[] pieces = effectString.split(":");
                    type = PotionEffectType.getByName(pieces[0].toUpperCase());
                    power = Integer.parseInt(pieces[1]);
                } else {
                    type = PotionEffectType.getByName(effectString.toUpperCase());
                }
                if (type == null) {
                    throw new Exception("Invalid potion effect");
                }

                Integer existing = effects.get(type);
                if (existing == null || existing < power) {
                    effects.put(type, power);
                }
            } catch (Exception ex) {
                Bukkit.getLogger().log(Level.WARNING, "Invalid potion effect: " + effectString);
            }
        }
    }
	
	public void loadProperties(ConfigurationSection wandConfig, boolean safe) {
		locked = wandConfig.getBoolean("locked", locked);
		float _consumeReduction = (float)wandConfig.getDouble("consume_reduction", consumeReduction);
		consumeReduction = safe ? Math.max(_consumeReduction, consumeReduction) : _consumeReduction;
		float _costReduction = (float)wandConfig.getDouble("cost_reduction", costReduction);
		costReduction = safe ? Math.max(_costReduction, costReduction) : _costReduction;
		float _cooldownReduction = (float)wandConfig.getDouble("cooldown_reduction", cooldownReduction);
		cooldownReduction = safe ? Math.max(_cooldownReduction, cooldownReduction) : _cooldownReduction;
		float _power = (float)wandConfig.getDouble("power", power);
		power = safe ? Math.max(_power, power) : _power;
		float _damageReduction = (float)wandConfig.getDouble("protection", damageReduction);
		damageReduction = safe ? Math.max(_damageReduction, damageReduction) : _damageReduction;
		float _damageReductionPhysical = (float)wandConfig.getDouble("protection_physical", damageReductionPhysical);
		damageReductionPhysical = safe ? Math.max(_damageReductionPhysical, damageReductionPhysical) : _damageReductionPhysical;
		float _damageReductionProjectiles = (float)wandConfig.getDouble("protection_projectiles", damageReductionProjectiles);
		damageReductionProjectiles = safe ? Math.max(_damageReductionProjectiles, damageReductionPhysical) : _damageReductionProjectiles;
		float _damageReductionFalling = (float)wandConfig.getDouble("protection_falling", damageReductionFalling);
		damageReductionFalling = safe ? Math.max(_damageReductionFalling, damageReductionFalling) : _damageReductionFalling;
		float _damageReductionFire = (float)wandConfig.getDouble("protection_fire", damageReductionFire);
		damageReductionFire = safe ? Math.max(_damageReductionFire, damageReductionFire) : _damageReductionFire;
		float _damageReductionExplosions = (float)wandConfig.getDouble("protection_explosions", damageReductionExplosions);
		damageReductionExplosions = safe ? Math.max(_damageReductionExplosions, damageReductionExplosions) : _damageReductionExplosions;
		
		int _manaRegeneration = wandConfig.getInt("mana_regeneration", wandConfig.getInt("xp_regeneration", manaRegeneration));
		manaRegeneration = safe ? Math.max(_manaRegeneration, manaRegeneration) : _manaRegeneration;
		int _manaMax = wandConfig.getInt("mana_max", wandConfig.getInt("xp_max", manaMax));
		manaMax = safe ? Math.max(_manaMax, manaMax) : _manaMax;
		int _mana = wandConfig.getInt("mana", wandConfig.getInt("xp", (int) mana));
		mana = safe ? Math.max(_mana, mana) : _mana;
        float _manaMaxBoost = (float)wandConfig.getDouble("mana_max_boost", wandConfig.getDouble("xp_max_boost", manaMaxBoost));
        manaMaxBoost = safe ? Math.max(_manaMaxBoost, manaMaxBoost) : _manaMaxBoost;
        float _manaRegenerationBoost = (float)wandConfig.getDouble("mana_regeneration_boost", wandConfig.getDouble("xp_regeneration_boost", manaRegenerationBoost));
        manaRegenerationBoost = safe ? Math.max(_manaRegenerationBoost, manaRegenerationBoost) : _manaRegenerationBoost;
        int _uses = wandConfig.getInt("uses", uses);
        uses = safe ? Math.max(_uses, uses) : _uses;
        String tempId = wandConfig.getString("id", id);
        isSingleUse = (tempId == null || tempId.isEmpty()) && uses == 1;
        hasUses = wandConfig.getBoolean("has_uses", hasUses) || uses > 0;

        float _manaPerDamage = (float)wandConfig.getDouble("mana_per_damage", manaPerDamage);
        manaPerDamage = safe ? Math.max(_manaPerDamage, manaPerDamage) : _manaPerDamage;

        // Convert some legacy properties to potion effects
        float healthRegeneration = (float)wandConfig.getDouble("health_regeneration", 0);
		float hungerRegeneration = (float)wandConfig.getDouble("hunger_regeneration", 0);
		float speedIncrease = (float)wandConfig.getDouble("haste", 0);

        if (speedIncrease > 0) {
            potionEffects.put(PotionEffectType.SPEED, 1);
        }
        if (healthRegeneration > 0) {
            potionEffects.put(PotionEffectType.REGENERATION, 1);
        }
        if (hungerRegeneration > 0) {
            potionEffects.put(PotionEffectType.SATURATION, 1);
        }

        if (regenWhileInactive) {
            lastManaRegeneration = wandConfig.getLong("mana_timestamp");
        } else {
            lastManaRegeneration = System.currentTimeMillis();
        }

		if (wandConfig.contains("effect_color") && !safe) {
			setEffectColor(wandConfig.getString("effect_color"));
		}
		
		// Don't change any of this stuff in safe mode
		if (!safe) {
			id = wandConfig.getString("id", id);
            isUpgrade = wandConfig.getBoolean("upgrade", isUpgrade);
            quietLevel = wandConfig.getInt("quiet", quietLevel);
			effectBubbles = wandConfig.getBoolean("effect_bubbles", effectBubbles);
			keep = wandConfig.getBoolean("keep", keep);
            passive = wandConfig.getBoolean("passive", passive);
            indestructible = wandConfig.getBoolean("indestructible", indestructible);
            superPowered = wandConfig.getBoolean("powered", superPowered);
            superProtected = wandConfig.getBoolean("protected", superProtected);
            glow = wandConfig.getBoolean("glow", glow);
            undroppable = wandConfig.getBoolean("undroppable", undroppable);
            isHeroes = wandConfig.getBoolean("heroes", isHeroes);
			bound = wandConfig.getBoolean("bound", bound);
            soul = wandConfig.getBoolean("soul", soul);
            forceUpgrade = wandConfig.getBoolean("force", forceUpgrade);
            autoOrganize = wandConfig.getBoolean("organize", autoOrganize);
            autoAlphabetize = wandConfig.getBoolean("alphabetize", autoAlphabetize);
			autoFill = wandConfig.getBoolean("fill", autoFill);
            randomize = wandConfig.getBoolean("randomize", randomize);
            rename = wandConfig.getBoolean("rename", rename);
            renameDescription = wandConfig.getBoolean("rename_description", renameDescription);
            enchantCount = wandConfig.getInt("enchant_count", enchantCount);
            maxEnchantCount = wandConfig.getInt("max_enchant_count", maxEnchantCount);

            if (wandConfig.contains("effect_particle")) {
                effectParticle = ConfigurationUtils.toParticleEffect(wandConfig.getString("effect_particle"));
				effectParticleData = 0;
			}
			if (wandConfig.contains("effect_sound")) {
                effectSound = ConfigurationUtils.toSoundEffect(wandConfig.getString("effect_sound"));
			}
			effectParticleData = (float)wandConfig.getDouble("effect_particle_data", effectParticleData);
			effectParticleCount = wandConfig.getInt("effect_particle_count", effectParticleCount);
            effectParticleRadius = wandConfig.getDouble("effect_particle_radius", effectParticleRadius);
            effectParticleOffset = wandConfig.getDouble("effect_particle_offset", effectParticleOffset);
			effectParticleInterval = wandConfig.getInt("effect_particle_interval", effectParticleInterval);
            effectParticleMinVelocity = wandConfig.getDouble("effect_particle_min_velocity", effectParticleMinVelocity);
			effectSoundInterval =  wandConfig.getInt("effect_sound_interval", effectSoundInterval);

            castInterval = wandConfig.getInt("cast_interval", castInterval);
            castMinVelocity = wandConfig.getDouble("cast_min_velocity", castMinVelocity);
            castVelocityDirection = ConfigurationUtils.getVector(wandConfig, "cast_velocity_direction", castVelocityDirection);
            castSpell = wandConfig.getString("cast_spell", castSpell);
            String castParameterString = wandConfig.getString("cast_parameters", null);
            if (castParameterString != null && !castParameterString.isEmpty()) {
                castParameters = new MemoryConfiguration();
                ConfigurationUtils.addParameters(StringUtils.split(castParameterString, " "), castParameters);
            }

            boolean needsInventoryUpdate = false;
            if (wandConfig.contains("mode")) {
                WandMode newMode = parseWandMode(wandConfig.getString("mode"), mode);
                if (newMode != mode) {
                    setMode(newMode);
                    needsInventoryUpdate = true;
                }
            }

            if (wandConfig.contains("brush_mode")) {
                setBrushMode(parseWandMode(wandConfig.getString("brush_mode"), brushMode));
            }
            String quickCastType = wandConfig.getString("quick_cast", wandConfig.getString("mode_cast"));
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
			
			// Backwards compatibility
			if (wandConfig.getBoolean("mode_drop", false)) {
				dropAction = WandAction.TOGGLE;
				swapAction = WandAction.CYCLE_HOTBAR;
				rightClickAction = WandAction.NONE;
			} else if (mode == WandMode.CAST) {
				leftClickAction = WandAction.CAST;
				rightClickAction = WandAction.CAST;
				swapAction = WandAction.NONE;
				dropAction = WandAction.NONE;
			}
			leftClickAction = parseWandAction(wandConfig.getString("left_click"), leftClickAction);
			rightClickAction = parseWandAction(wandConfig.getString("right_click"), rightClickAction);
			dropAction = parseWandAction(wandConfig.getString("drop"), dropAction);
			swapAction = parseWandAction(wandConfig.getString("swap"), swapAction);

			owner = wandConfig.getString("owner", owner);
            ownerId = wandConfig.getString("owner_id", ownerId);
			wandName = wandConfig.getString("name", wandName);			
			description = wandConfig.getString("description", description);
			template = wandConfig.getString("template", template);
            upgradeTemplate = wandConfig.getString("upgrade_template", upgradeTemplate);
            path = wandConfig.getString("path", path);

			activeSpell = wandConfig.getString("active_spell", activeSpell);
			activeMaterial = wandConfig.getString("active_material", activeMaterial);

            String wandMaterials = "";
            String wandSpells = "";
            if (wandConfig.contains("hotbar_count")) {
                int newCount = Math.max(1, wandConfig.getInt("hotbar_count"));
                if ((!safe && newCount != hotbars.size()) || newCount > hotbars.size()) {
                    if (isInventoryOpen()) {
                        closeInventory();
                    }
                    needsInventoryUpdate = true;
                    setHotbarCount(newCount);
                }
            }

            if (wandConfig.contains("hotbar")) {
                int hotbar = wandConfig.getInt("hotbar");
                currentHotbar = hotbar < 0 || hotbar >= hotbars.size() ? 0 : hotbar;
            }

            if (needsInventoryUpdate) {
                // Force a re-parse of materials and spells
                wandSpells = getSpellString();
                wandMaterials = getMaterialString();
            }

			wandMaterials = wandConfig.getString("materials", wandMaterials);
			wandSpells = wandConfig.getString("spells", wandSpells);

			if (wandMaterials.length() > 0 || wandSpells.length() > 0) {
				wandMaterials = wandMaterials.length() == 0 ? getMaterialString() : wandMaterials;
				wandSpells = wandSpells.length() == 0 ? getSpellString() : wandSpells;
				parseInventoryStrings(wandSpells, wandMaterials);
			}
			
			// Check for migration information in the template config
			ConfigurationSection templateConfig = null;
			if (template != null && !template.isEmpty()) {
				templateConfig = controller.getWandTemplateConfiguration(template);
			}
			String migrateTemplate = templateConfig == null ? null : templateConfig.getString("migrate_to");
			if (migrateTemplate != null) {
				template = migrateTemplate;
				templateConfig = controller.getWandTemplateConfiguration(template);
			}
            WandTemplate wandTemplate = getTemplate();
			
			if (templateConfig != null) {
				// Add vanilla attributes
				InventoryUtils.applyAttributes(item, templateConfig.getConfigurationSection("attributes"), templateConfig.getString("attribute_slot", null));
			}

            if (wandConfig.contains("icon_inactive")) {
                String iconKey = wandConfig.getString("icon_inactive");
                if (wandTemplate != null) {
                    iconKey = wandTemplate.migrateIcon(iconKey);
                }
                if (iconKey != null) {
                    inactiveIcon = new MaterialAndData(iconKey);
                }
            }
            if (inactiveIcon != null && (inactiveIcon.getMaterial() == null || inactiveIcon.getMaterial() == Material.AIR))
            {
                inactiveIcon = null;
            }
            inactiveIconDelay = wandConfig.getInt("icon_inactive_delay", inactiveIconDelay);

            if (wandConfig.contains("randomize_icon")) {
                setIcon(new MaterialAndData(wandConfig.getString("randomize_icon")));
                randomize = true;
            } else if (!randomize && wandConfig.contains("icon")) {
                String iconKey = wandConfig.getString("icon");
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
                setIcon(new MaterialAndData(iconKey));
			} else if (isUpgrade) {
                setIcon(new MaterialAndData(DefaultUpgradeMaterial));
            }

            if (wandConfig.contains("upgrade_icon")) {
                upgradeIcon = new MaterialAndData(wandConfig.getString("upgrade_icon"));
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

            if (wandConfig.contains("overrides")) {
                castOverrides = null;
                String overrides = wandConfig.getString("overrides", null);
                if (overrides != null && !overrides.isEmpty()) {
                    // Support YML-List-As-String format
                    overrides = overrides.replaceAll("[\\]\\[]", "");
                    // Unescape commas
                    overrides = overrides.replace("\\,", ",");

                    char split = ',';
                    // Check for | delimited format
                    if (overrides.charAt(0) == '|') {
                        split = '|';
                        overrides = overrides.substring(1);
                    }
                    castOverrides = new HashMap<>();
                    String[] pairs = StringUtils.split(overrides, split);
                    for (String pair : pairs) {
                        String[] keyValue = StringUtils.split(pair, " ");
                        if (keyValue.length > 0) {
                            String value = keyValue.length > 1 ? keyValue[1] : "";
                            castOverrides.put(keyValue[0], value);
                        }
                    }
                }
            }

            if (wandConfig.contains("potion_effects")) {
                potionEffects.clear();
                addPotionEffects(potionEffects, wandConfig.getString("potion_effects", null));
            }
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

        String customKeysStr = wandConfig.getString("custom_properties", "");
        String[] customKeys = StringUtils.split(customKeysStr, ",");
        for(String key : customKeys ) {
            customProperties.put(key, wandConfig.getString(key));
        }

        updateMaxMana(false);
        checkActiveMaterial();
        checkId();
    }

	@Override
    public void describe(CommandSender sender) {
		Object wandNode = InventoryUtils.getNode(item, WAND_KEY);
		if (wandNode == null) {
			sender.sendMessage("Found a wand with missing NBT data. This may be an old wand, or something may have wiped its data");
            return;
		}
		ChatColor wandColor = isModifiable() ? ChatColor.AQUA : ChatColor.RED;
		sender.sendMessage(wandColor + wandName);
		if (description.length() > 0) {
			sender.sendMessage(ChatColor.ITALIC + "" + ChatColor.GREEN + description);
		} else {
			sender.sendMessage(ChatColor.ITALIC + "" + ChatColor.GREEN + "(No Description)");
		}
		if (owner.length() > 0 || ownerId.length() > 0) {
			sender.sendMessage(ChatColor.ITALIC + "" + ChatColor.WHITE + owner + " (" + ChatColor.GRAY + ownerId + ChatColor.WHITE + ")");
		} else {
			sender.sendMessage(ChatColor.ITALIC + "" + ChatColor.WHITE + "(No Owner)");
		}
		
		for (String key : PROPERTY_KEYS) {
			String value = InventoryUtils.getMeta(wandNode, key);
			if (value != null && value.length() > 0) {
				sender.sendMessage(key + ": " + value);
			}
		}
	}

    private static String getBrushDisplayName(Messages messages, MaterialBrush brush) {
        String materialName = brush == null ? null : brush.getName(messages);
        if (materialName == null) {
            materialName = "none";
        }
        return ChatColor.GRAY + materialName;
    }

    private static String getSpellDisplayName(Messages messages, SpellTemplate spell, MaterialBrush brush) {
		String name = "";
		if (spell != null) {
			if (brush != null && spell.usesBrush()) {
				name = ChatColor.GOLD + spell.getName() + " " + getBrushDisplayName(messages, brush) + ChatColor.WHITE;
			} else {
				name = ChatColor.GOLD + spell.getName() + ChatColor.WHITE;
			}
		}
		
		return name;
	}

	private String getActiveWandName(SpellTemplate spell, MaterialBrush brush) {
		// Build wand name
        int remaining = getRemainingUses();
		ChatColor wandColor = (hasUses && remaining <= 1) ? ChatColor.DARK_RED : isModifiable()
                ? (bound ? ChatColor.DARK_AQUA : ChatColor.AQUA) :
                  (path != null && path.length() > 0 ? ChatColor.LIGHT_PURPLE : ChatColor.GOLD);
		String name = wandColor + getDisplayName();
        if (randomize) return name;

        Set<String> spells = getSpells();

        // Add active spell to description
        Messages messages = controller.getMessages();
        boolean showSpell = isModifiable() && hasPath();
        if (spell != null && !quickCast && (spells.size() > 1 || showSpell)) {
            name = getSpellDisplayName(messages, spell, brush) + " (" + name + ChatColor.WHITE + ")";
        }

		if (remaining > 1) {
			String message = getMessage("uses_remaining_brief");
			name = name + ChatColor.DARK_RED + " (" + ChatColor.RED + message.replace("$count", ((Integer)remaining).toString()) + ChatColor.DARK_RED + ")";
		}
		return name;
	}
	
	private String getActiveWandName(SpellTemplate spell) {
		return getActiveWandName(spell, MaterialBrush.parseMaterialKey(activeMaterial));
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
        return ChatColor.translateAlternateColorCodes('&', randomize ? getMessage("randomized_name") : wandName);
    }

	public void updateName(boolean isActive) {
		if (isActive) {
			CompatibilityUtils.setDisplayName(item, !isUpgrade ? getActiveWandName() : ChatColor.GOLD + getDisplayName());
		} else {
			CompatibilityUtils.setDisplayName(item, ChatColor.stripColor(getDisplayName()));
		}

		// Reset Enchantment glow
		if (glow) {
            CompatibilityUtils.addGlow(item);
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

	protected List<String> getLore() {
		return getLore(getSpells().size(), getBrushes().size());
	}
	
	protected void addPropertyLore(List<String> lore)
	{
		if (usesMana()) {
            if (effectiveManaMax != manaMax) {
                String fullMessage = getLevelString(controller.getMessages(), "wand.mana_amount_boosted", manaMax, controller.getMaxMana());
                lore.add(ChatColor.LIGHT_PURPLE + "" + ChatColor.ITALIC + fullMessage.replace("$mana", Integer.toString(effectiveManaMax)));
            } else {
                lore.add(ChatColor.LIGHT_PURPLE + "" + ChatColor.ITALIC + getLevelString(controller.getMessages(), "wand.mana_amount", manaMax, controller.getMaxMana()));
            }
            if (manaRegeneration > 0) {
                if (effectiveManaRegeneration != manaRegeneration) {
                    String fullMessage = getLevelString(controller.getMessages(), "wand.mana_regeneration_boosted", manaRegeneration, controller.getMaxManaRegeneration());
                    lore.add(ChatColor.LIGHT_PURPLE + "" + ChatColor.ITALIC + fullMessage.replace("$mana", Integer.toString(effectiveManaRegeneration)));
                } else {
                    lore.add(ChatColor.RESET + "" + ChatColor.LIGHT_PURPLE + getLevelString(controller.getMessages(), "wand.mana_regeneration", manaRegeneration, controller.getMaxManaRegeneration()));
                }
            }
            if (manaPerDamage > 0) {
                lore.add(ChatColor.DARK_RED + "" + ChatColor.ITALIC + getLevelString(controller.getMessages(), "wand.mana_per_damage", manaPerDamage, controller.getMaxManaRegeneration()));
            }
		}
        if (superPowered) {
            lore.add(ChatColor.DARK_AQUA + getMessage("super_powered"));
        }
        if (manaMaxBoost != 0) {
            lore.add(ChatColor.LIGHT_PURPLE + "" + ChatColor.ITALIC + getPercentageString(controller.getMessages(), "wand.mana_boost", manaMaxBoost));
        }
        if (manaRegenerationBoost != 0) {
            lore.add(ChatColor.LIGHT_PURPLE + "" + ChatColor.ITALIC + getPercentageString(controller.getMessages(), "wand.mana_regeneration_boost", manaRegenerationBoost));
        }
        if (castSpell != null) {
            SpellTemplate spell = controller.getSpellTemplate(castSpell);
            if (spell != null)
            {
                lore.add(ChatColor.AQUA + getMessage("spell_aura").replace("$spell", spell.getName()));
            }
        }
        for (Map.Entry<PotionEffectType, Integer> effect : potionEffects.entrySet()) {
            String effectName = effect.getKey().getName();
            String effectFirst = effectName.substring(0, 1);
            effectName = effectName.substring(1).toLowerCase().replace("_", " ");
            effectName = effectFirst + effectName;
            lore.add(ChatColor.AQUA + getLevelString(controller.getMessages(), "wand.potion_effect", effect.getValue(), 5).replace("$effect", effectName));
        }
		if (consumeReduction > 0) lore.add(ChatColor.AQUA + getLevelString(controller.getMessages(), "wand.consume_reduction", consumeReduction));
		if (costReduction > 0) lore.add(ChatColor.AQUA + getLevelString(controller.getMessages(), "wand.cost_reduction", costReduction));
		if (cooldownReduction > 0) lore.add(ChatColor.AQUA + getLevelString(controller.getMessages(), "wand.cooldown_reduction", cooldownReduction));
		if (power > 0) lore.add(ChatColor.AQUA + getLevelString(controller.getMessages(), "wand.power", power));
        if (superProtected) {
            lore.add(ChatColor.DARK_AQUA + getMessage("super_protected"));
        } else {
            if (damageReduction > 0) lore.add(ChatColor.AQUA + getLevelString(controller.getMessages(), "wand.protection", damageReduction));
            if (damageReductionPhysical > 0) lore.add(ChatColor.AQUA + getLevelString(controller.getMessages(), "wand.protection_physical", damageReductionPhysical));
            if (damageReductionProjectiles > 0) lore.add(ChatColor.AQUA + getLevelString(controller.getMessages(), "wand.protection_projectile", damageReductionProjectiles));
            if (damageReductionFalling > 0) lore.add(ChatColor.AQUA + getLevelString(controller.getMessages(), "wand.protection_fall", damageReductionFalling));
            if (damageReductionFire > 0) lore.add(ChatColor.AQUA + getLevelString(controller.getMessages(), "wand.protection_fire", damageReductionFire));
            if (damageReductionExplosions > 0) lore.add(ChatColor.AQUA + getLevelString(controller.getMessages(), "wand.protection_blast", damageReductionExplosions));
        }
	}
	
	public static String getLevelString(Messages messages, String templateName, float amount)
	{
		return getLevelString(messages, templateName, amount, 1);
	}
	
	public static String getLevelString(Messages messages, String templateName, float amount, float max)
	{
		String templateString = messages.get(templateName);
		if (templateString.contains("$roman")) {
            if (max != 1) {
                amount = amount / max;
            }
			templateString = templateString.replace("$roman", getRomanString(messages, amount));
		}
		return templateString.replace("$amount", Integer.toString((int) amount));
	}

    public static String getPercentageString(Messages messages, String templateName, float amount)
    {
        String templateString = messages.get(templateName);
        return templateString.replace("$amount", Integer.toString((int)(amount * 100)));
    }

	private static String getRomanString(Messages messages, float amount) {
		String roman = "";

		if (amount > 1) {
			roman = messages.get("wand.enchantment_level_max");
		} else if (amount > 0.8) {
			roman = messages.get("wand.enchantment_level_5");
		} else if (amount > 0.6) {
			roman = messages.get("wand.enchantment_level_4");
		} else if (amount > 0.4) {
			roman = messages.get("wand.enchantment_level_3");
		} else if (amount > 0.2) {
			roman = messages.get("wand.enchantment_level_2");
		} else {
			 roman = messages.get("wand.enchantment_level_1");
		}
		return roman;
	}
	
	protected List<String> getLore(int spellCount, int materialCount) 
	{
		List<String> lore = new ArrayList<>();

        if (description.length() > 0) {
            if (description.contains("$path")) {
                String pathName = "Unknown";
                com.elmakers.mine.bukkit.api.wand.WandUpgradePath path = getPath();
                if (path != null) {
                    pathName = path.getName();
                }
                String description = this.description;
                description = description.replace("$path", pathName);
				InventoryUtils.wrapText(ChatColor.ITALIC + "" + ChatColor.GREEN, description, MAX_LORE_LENGTH, lore);
            }
            else if (description.contains("$")) {
                String randomDescription = getMessage("randomized_lore");
                if (randomDescription.length() > 0) {
					InventoryUtils.wrapText(ChatColor.ITALIC + "" + ChatColor.DARK_GREEN, randomDescription, MAX_LORE_LENGTH, lore);
                }
            } else {
				InventoryUtils.wrapText(ChatColor.ITALIC + "" + ChatColor.GREEN, description, MAX_LORE_LENGTH, lore);
            }
        }

        if (randomize) {
            return lore;
        }

		SpellTemplate spell = controller.getSpellTemplate(activeSpell);
        Messages messages = controller.getMessages();

        // This is here specifically for a wand that only has
        // one spell now, but may get more later. Since you
        // can't open the inventory in this state, you can not
        // otherwise see the spell lore.
		if (spell != null && spellCount == 1 && !hasInventory && !isUpgrade && hasPath() && !spell.isHidden())
        {
            addSpellLore(messages, spell, lore, getActivePlayer(), this);
		}
		// This is here mostly for broomsticks..
		else if (spell != null && spellCount == 1 && !isUpgrade && hasPath())
		{
			String cooldownDescription = spell.getCooldownDescription();
			if (cooldownDescription != null && !cooldownDescription.isEmpty()) {
				lore.add(messages.get("cooldown.description").replace("$time", cooldownDescription));
			}
			long effectiveDuration = spell.getDuration();
			if (effectiveDuration > 0) {
				long seconds = effectiveDuration / 1000;
				if (seconds > 60 * 60 ) {
					long hours = seconds / (60 * 60);
					lore.add(ChatColor.GRAY + messages.get("duration.lasts_hours").replace("$hours", ((Long)hours).toString()));
				} else if (seconds > 60) {
					long minutes = seconds / 60;
					lore.add(ChatColor.GRAY + messages.get("duration.lasts_minutes").replace("$minutes", ((Long)minutes).toString()));
				} else {
					lore.add(ChatColor.GRAY + messages.get("duration.lasts_seconds").replace("$seconds", ((Long)seconds).toString()));
				}
			}
		}
        if (materialCount == 1 && activeMaterial != null && activeMaterial.length() > 0)
        {
            lore.add(getBrushDisplayName(messages, MaterialBrush.parseMaterialKey(activeMaterial)));
        }
        if (!isUpgrade) {
            if (owner.length() > 0) {
                if (bound) {
					if (soul) {
						String ownerDescription = getMessage("soulbound_description", "$name").replace("$name", owner);
						lore.add(ChatColor.ITALIC + "" + ChatColor.DARK_AQUA + ownerDescription);
					} else {
						String ownerDescription = getMessage("bound_description", "$name").replace("$name", owner);
						lore.add(ChatColor.ITALIC + "" + ChatColor.DARK_AQUA + ownerDescription);
					}
                } else {
                    String ownerDescription = getMessage("owner_description", "$name").replace("$name", owner);
                    lore.add(ChatColor.ITALIC + "" + ChatColor.DARK_GREEN + ownerDescription);
                }
            }
        }

        if (spellCount > 0) {
            if (isUpgrade) {
                lore.add(getMessage("upgrade_spell_count").replace("$count", ((Integer)spellCount).toString()));
            } else if (spellCount > 1) {
                lore.add(getMessage("spell_count").replace("$count", ((Integer)spellCount).toString()));
            }
        }
        if (materialCount > 0) {
            if (isUpgrade) {
                lore.add(getMessage("upgrade_material_count").replace("$count", ((Integer)materialCount).toString()));
            } else if (materialCount > 1) {
                lore.add(getMessage("material_count").replace("$count", ((Integer)materialCount).toString()));
            }
        }

		int remaining = getRemainingUses();
		if (remaining > 0) {
			if (isUpgrade) {
				String message = (remaining == 1) ? getMessage("upgrade_uses_singular") : getMessage("upgrade_uses");
				lore.add(ChatColor.RED + message.replace("$count", ((Integer)remaining).toString()));
			} else {
				String message = (remaining == 1) ? getMessage("uses_remaining_singular") : getMessage("uses_remaining_brief");
				lore.add(ChatColor.RED + message.replace("$count", ((Integer)remaining).toString()));
			}
		}
		addPropertyLore(lore);
		if (isUpgrade) {
			lore.add(ChatColor.YELLOW + getMessage("upgrade_item_description"));
		}
		return lore;
	}
	
	protected void updateLore() {
        CompatibilityUtils.setLore(item, getLore());

		if (glow) {
			CompatibilityUtils.addGlow(item);
		}
	}

    public void save() {
        saveItemState();
        updateName();
        updateLore();
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
			Set<Material> enchantableMaterials = controller.getMaterialSet("enchantable");
			if (!enchantableMaterials.contains(item.getType())) {
				item.setType(EnchantableWandMaterial);
				item.setDurability((short)0);
			}
		}
		updateName();
	}
	
	public static boolean hasActiveWand(Player player) {
		if (player == null) return false;
		ItemStack activeItem =  player.getInventory().getItemInMainHand();
		return isWand(activeItem);
	}
	
	public static Wand getActiveWand(MagicController controller, Player player) {
		ItemStack activeItem =  player.getInventory().getItemInMainHand();
		if (isWand(activeItem)) {
			return controller.getWand(activeItem);
		}
		
		return null;
	}

	public static boolean isWand(ItemStack item) {
        return item != null && InventoryUtils.hasMeta(item, WAND_KEY) && !isUpgrade(item);
	}

	public static boolean isWandOrUpgrade(ItemStack item) {
		return item != null && InventoryUtils.hasMeta(item, WAND_KEY);
	}

	public static boolean isBound(ItemStack item) {
		Object wandSection = InventoryUtils.getNode(item, WAND_KEY);
		if (wandSection == null) return false;
		
		String boundValue = InventoryUtils.getMeta(wandSection, "bound");
		return boundValue != null && boundValue.equalsIgnoreCase("true");
	}

    public static boolean isSelfDestructWand(ItemStack item) {
        return item != null && WAND_SELF_DESTRUCT_KEY != null && InventoryUtils.hasMeta(item, WAND_SELF_DESTRUCT_KEY);
    }

	public static boolean isSP(ItemStack item) {
		return InventoryUtils.hasMeta(item, "sp");
	}

	public static Integer getSP(ItemStack item) {
		if (InventoryUtils.isEmpty(item)) return null;
		String spNode = InventoryUtils.getMeta(item, "sp");
		if (spNode == null) return null;
		Integer sp = null;
		try {
			sp = Integer.parseInt(spNode);
		} catch (Exception ex) {
			sp = null;
		}

		return sp;
	}

    public static boolean isSingleUse(ItemStack item) {
		if (InventoryUtils.isEmpty(item)) return false;
        Object wandNode = InventoryUtils.getNode(item, WAND_KEY);

        if (wandNode == null) return false;
        String useCount = InventoryUtils.getMeta(wandNode, "uses");
        String wandId = InventoryUtils.getMeta(wandNode, "id");

        return useCount != null && useCount.equals("1") && (wandId == null || wandId.isEmpty());
    }

    public static boolean isUpgrade(ItemStack item) {
		if (InventoryUtils.isEmpty(item)) return false;
        Object wandNode = InventoryUtils.getNode(item, WAND_KEY);

        if (wandNode == null) return false;
        String upgradeData = InventoryUtils.getMeta(wandNode, "upgrade");

        return upgradeData != null && upgradeData.equals("true");
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

    public static String getWandTemplate(ItemStack item) {
		if (InventoryUtils.isEmpty(item)) return null;
        Object wandNode = InventoryUtils.getNode(item, WAND_KEY);
        if (wandNode == null) return null;
        return InventoryUtils.getMeta(wandNode, "template");
    }

    public static String getWandId(ItemStack item) {
		if (InventoryUtils.isEmpty(item)) return null;
        Object wandNode = InventoryUtils.getNode(item, WAND_KEY);
        if (wandNode == null || isUpgrade(item)) return null;
        if (isSingleUse(item)) return getWandTemplate(item);
        return InventoryUtils.getMeta(wandNode, "id");
    }

	public static String getSpell(ItemStack item) {
		if (InventoryUtils.isEmpty(item)) return null;
        Object spellNode = InventoryUtils.getNode(item, "spell");
        if (spellNode == null) return null;
        return InventoryUtils.getMeta(spellNode, "key");
	}

    public static String getSpellArgs(ItemStack item) {
		if (InventoryUtils.isEmpty(item)) return null;
        Object spellNode = InventoryUtils.getNode(item, "spell");
        if (spellNode == null) return null;
        return InventoryUtils.getMeta(spellNode, "args");
    }

    public static String getBrush(ItemStack item) {
		if (InventoryUtils.isEmpty(item)) return null;
        Object brushNode = InventoryUtils.getNode(item, "brush");
        if (brushNode == null) return null;
        return InventoryUtils.getMeta(brushNode, "key");
    }

	protected void updateInventoryName(ItemStack item, boolean activeName) {
		if (isSpell(item)) {
			Spell spell = mage.getSpell(getSpell(item));
			if (spell != null) {
				updateSpellItem(controller.getMessages(), item, spell, "", getActivePlayer(), activeName ? this : null, activeMaterial, false);
			}
		} else if (isBrush(item)) {
			updateBrushItem(controller.getMessages(), item, getBrush(item), activeName ? this : null);
		}
	}

    public static void updateSpellItem(Messages messages, ItemStack itemStack, SpellTemplate spell, String args, Wand wand, String activeMaterial, boolean isItem) {
        updateSpellItem(messages, itemStack, spell, args, wand == null ? null : wand.getActivePlayer(), wand, activeMaterial, isItem);
    }

    public static void updateSpellItem(Messages messages, ItemStack itemStack, SpellTemplate spell, String args, com.elmakers.mine.bukkit.api.magic.Mage mage, Wand wand, String activeMaterial, boolean isItem) {
        String displayName;
		if (wand != null && !wand.isQuickCast()) {
			displayName = wand.getActiveWandName(spell);
		} else {
			displayName = getSpellDisplayName(messages, spell, MaterialBrush.parseMaterialKey(activeMaterial));
		}
        CompatibilityUtils.setDisplayName(itemStack, displayName);
		List<String> lore = new ArrayList<>();
		addSpellLore(messages, spell, lore, mage, wand);
		if (isItem) {
			lore.add(ChatColor.YELLOW + messages.get("wand.spell_item_description"));
		}
        CompatibilityUtils.setLore(itemStack, lore);
        Object spellNode = CompatibilityUtils.createNode(itemStack, "spell");
		CompatibilityUtils.setMeta(spellNode, "key", spell.getKey());
        CompatibilityUtils.setMeta(spellNode, "args", args);
        if (SpellGlow) {
            CompatibilityUtils.addGlow(itemStack);
        }
	}

    public static void updateBrushItem(Messages messages, ItemStack itemStack, String materialKey, Wand wand) {
        updateBrushItem(messages, itemStack, MaterialBrush.parseMaterialKey(materialKey), wand);
    }
	
	public static void updateBrushItem(Messages messages, ItemStack itemStack, MaterialBrush brush, Wand wand) {
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

	@SuppressWarnings("deprecation")
	private void updateInventory() {
		if (mage == null) return;
		if (!isInventoryOpen()) return;
		Player player = mage.getPlayer();
		if (player == null) return;
		
		WandMode wandMode = getMode();
		if (wandMode == WandMode.INVENTORY) {
			if (!hasStoredInventory()) return;
			PlayerInventory inventory = player.getInventory();
            for (int i = 0; i < PLAYER_INVENTORY_SIZE; i++) {
				inventory.setItem(i, null);
			}
			updateHotbar(inventory);
			updateInventory(inventory, false);
			updateName();
			player.updateInventory();
		} else if (wandMode == WandMode.CHEST) {
			Inventory inventory = getDisplayInventory();
			inventory.clear();
			updateInventory(inventory, true);
			player.updateInventory();
		}
	}
	
	private void updateHotbar(PlayerInventory playerInventory) {
        if (getMode() != WandMode.INVENTORY) return;
		Inventory hotbar = getHotbar();
        if (hotbar == null) return;

        // Check for an item already in the player's held slot, which
        // we are about to replace with the wand.
		int currentSlot = playerInventory.getHeldItemSlot();
        ItemStack currentItem = playerInventory.getItem(currentSlot);
        String currentId = getWandId(currentItem);
        if (currentId != null && !currentId.equals(id)) {
            return;
        }

        // Reset the held item, just in case Bukkit made a copy or something.
        playerInventory.setItemInMainHand(this.item);

        // Set hotbar items from remaining list
		int targetOffset = 0;
		for (int hotbarSlot = 0; hotbarSlot < HOTBAR_INVENTORY_SIZE; hotbarSlot++)
		{
			if (hotbarSlot == currentSlot)
			{
				targetOffset = 1;
			}

			ItemStack hotbarItem = hotbar.getItem(hotbarSlot);
			updateInventoryName(hotbarItem, true);
			playerInventory.setItem(hotbarSlot + targetOffset, hotbarItem);
		}
    }
	
	private void updateInventory(Inventory targetInventory, boolean addHotbars) {
		// Set inventory from current page
		int currentOffset = addHotbars ? 0 : HOTBAR_SIZE;
        List<Inventory> inventories = this.inventories;
		if (openInventoryPage < inventories.size()) {
            Inventory inventory = inventories.get(openInventoryPage);
            ItemStack[] contents = inventory.getContents();
            for (int i = 0; i < contents.length; i++) {
                ItemStack inventoryItem = contents[i];
                updateInventoryName(inventoryItem, false);
                targetInventory.setItem(currentOffset, inventoryItem);
                currentOffset++;
            }
        }
        if (addHotbars && openInventoryPage < hotbars.size()) {
            Inventory inventory = hotbars.get(openInventoryPage);
            ItemStack[] contents = inventory.getContents();
            for (int i = 0; i < contents.length; i++) {
                ItemStack inventoryItem = contents[i];
                updateInventoryName(inventoryItem, false);
                targetInventory.setItem(currentOffset, inventoryItem);
                currentOffset++;
            }
        }
	}
	
	protected static void addSpellLore(Messages messages, SpellTemplate spell, List<String> lore, com.elmakers.mine.bukkit.api.magic.Mage mage, Wand wand) {
        spell.addLore(messages, mage, wand, lore);
	}
	
	protected Inventory getOpenInventory() {
		while (openInventoryPage >= inventories.size()) {
			inventories.add(CompatibilityUtils.createInventory(null, INVENTORY_SIZE, "Wand"));
		}
		return inventories.get(openInventoryPage);
	}
	
	public void saveInventory() {
		if (mage == null) return;
		if (!isInventoryOpen()) return;
		if (mage.getPlayer() == null) return;
		if (getMode() != WandMode.INVENTORY) return;
		if (!hasStoredInventory()) return;

        // Work-around glitches that happen if you're dragging an item on death
        if (mage.isDead()) return;

		// Fill in the hotbar
		Player player = mage.getPlayer();
		PlayerInventory playerInventory = player.getInventory();
		Inventory hotbar = getHotbar();
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
		Inventory openInventory = getOpenInventory();
		for (int i = 0; i < openInventory.getSize(); i++) {
            ItemStack playerItem = playerInventory.getItem(i + HOTBAR_SIZE);
            if (!updateSlot(i + hotbarOffset + openInventoryPage * INVENTORY_SIZE, playerItem)) {
                playerItem = new ItemStack(Material.AIR);
                playerInventory.setItem(i + HOTBAR_SIZE, playerItem);
            }
			openInventory.setItem(i, playerItem);
		}
	}

    protected boolean updateSlot(int slot, ItemStack item) {
        String spellKey = getSpell(item);
        if (spellKey != null) {
            spells.put(spellKey, slot);
        } else {
            String brushKey = getBrush(item);
            if (brushKey != null) {
                brushes.put(brushKey, slot);
            } else if (mage != null && item != null && item.getType() != Material.AIR) {
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
            if (enchanter != null) {
                enchanter.sendMessage(getMessage("max_enchanted").replace("$wand", getName()));
            }
            return 0;
        }

        WandUpgradePath path = (WandUpgradePath)getPath();
		if (path == null) {
            if (enchanter != null) {
                enchanter.sendMessage(getMessage("no_path").replace("$wand", getName()));
            }
            return 0;
        }

        int minLevel = path.getMinLevel();
        if (totalLevels < minLevel) {
            if (enchanter != null) {
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
                    if (path.checkUpgradeRequirements(this, enchanter)) {
                        path.upgrade(this, enchanter);
                    }
                    break;
                } else {
                    enchanter.sendMessage(getMessage("fully_enchanted").replace("$wand", getName()));
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
                if (path.checkUpgradeRequirements(this, enchanter)) {
                    path.upgrade(this, enchanter);
                    levels += addLevels;
                }
            } else if (enchanter != null) {
                enchanter.sendMessage(getMessage("fully_enchanted").replace("$wand", getName()));
            }
			addLevels = Math.min(totalLevels, maxLevel);
			additive = true;
		}

        if (levels > 0) {
            enchantCount++;
        }

        saveItemState();
        updateName();
        updateLore();
        return levels;
	}

    public static ItemStack createItem(MagicController controller, String templateName) {
        ItemStack item = createSpellItem(templateName, controller, null, true);
        if (item == null) {
            item = createBrushItem(templateName, controller, null, true);
            if (item == null) {
                Wand wand = createWand(controller, templateName);
                if (wand != null) {
                    item = wand.getItem();
                }
            }
        }

        return item;
    }
	
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

    public static Wand createWand(MagicController controller, ItemStack itemStack) {
        if (controller == null) return null;

        Wand wand = null;
        try {
            wand = controller.getWand(InventoryUtils.makeReal(itemStack));
            wand.saveItemState();
            wand.updateName();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return wand;
    }
	
	protected void sendAddMessage(com.elmakers.mine.bukkit.api.magic.Mage mage, String messageKey, String nameParam) {
		if (mage == null) return;
		String message = getMessage(messageKey).replace("$name", nameParam).replace("$wand", getName());
		mage.sendMessage(message);
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

		// Check for forced upgrades
		if (other.isForcedUpgrade()) {
			String template = other.getTemplateKey();
			ConfigurationSection templateConfig = controller.getWandTemplateConfiguration(template);
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
			templateConfig.set("key", null);

			this.loadProperties(templateConfig, false);
			this.saveItemState();
			return true;
		}

		// Don't allow upgrades from an item on a different path
		if (other.isUpgrade() && other.path != null && !other.path.isEmpty() && (this.path == null || !this.path.equals(other.path))) {
			return false;
		}

		boolean modified = false;

        Messages messages = controller.getMessages();
		if (other.costReduction > costReduction) { costReduction = other.costReduction; modified = true; if (costReduction > 0) sendAddMessage(mage, "upgraded_property", getLevelString(messages, "wand.cost_reduction", costReduction)); }
		if (other.consumeReduction > consumeReduction) { consumeReduction = other.consumeReduction; modified = true; if (consumeReduction > 0) sendAddMessage(mage, "upgraded_property", getLevelString(messages, "wand.consume_reduction", consumeReduction)); }
		if (other.power > power) { power = other.power; modified = true; if (power > 0) sendAddMessage(mage, "upgraded_property", getLevelString(messages, "wand.power", power)); }
		if (other.damageReduction > damageReduction) { damageReduction = other.damageReduction; modified = true; if (damageReduction > 0) sendAddMessage(mage, "upgraded_property", getLevelString(messages, "wand.protection", damageReduction)); }
		if (other.damageReductionPhysical > damageReductionPhysical) { damageReductionPhysical = other.damageReductionPhysical; modified = true; if (damageReductionPhysical > 0) sendAddMessage(mage, "upgraded_property", getLevelString(messages, "wand.protection_physical", damageReductionPhysical)); }
		if (other.damageReductionProjectiles > damageReductionProjectiles) { damageReductionProjectiles = other.damageReductionProjectiles; modified = true; if (damageReductionProjectiles > 0) sendAddMessage(mage, "upgraded_property", getLevelString(messages, "wand.protection_projectile", damageReductionProjectiles)); }
		if (other.damageReductionFalling > damageReductionFalling) { damageReductionFalling = other.damageReductionFalling; modified = true; if (damageReductionFalling > 0) sendAddMessage(mage, "upgraded_property", getLevelString(messages, "wand.protection_fall", damageReductionFalling)); }
		if (other.damageReductionFire > damageReductionFire) { damageReductionFire = other.damageReductionFire; modified = true; if (damageReductionFire > 0) sendAddMessage(mage, "upgraded_property", getLevelString(messages, "wand.protection_fire", damageReductionFire)); }
		if (other.damageReductionExplosions > damageReductionExplosions) { damageReductionExplosions = other.damageReductionExplosions; modified = true; if (damageReductionExplosions > 0) sendAddMessage(mage, "upgraded_property", getLevelString(messages, "wand.protection_blast", damageReductionExplosions)); }
        if (other.manaRegenerationBoost > manaRegenerationBoost) { manaRegenerationBoost = other.manaRegenerationBoost; modified = true; if (manaRegenerationBoost > 0) sendAddMessage(mage, "upgraded_property", getLevelString(messages, "wand.mana_regeneration_boost", manaRegenerationBoost)); }
        if (other.manaMaxBoost > manaMaxBoost) { manaMaxBoost = other.manaMaxBoost; modified = true; if (manaMaxBoost > 0) sendAddMessage(mage, "upgraded_property", getLevelString(messages, "wand.mana_boost", manaMaxBoost)); }

        boolean needsInventoryUpdate = false;
		if (other.hotbars.size() > hotbars.size()) {
			int newCount = Math.max(1, other.hotbars.size());
			if (newCount != hotbars.size()) {
				if (isInventoryOpen()) {
					closeInventory();
				}
                setHotbarCount(newCount);
                needsInventoryUpdate = true;
				modified = true;
                if (getMode() == WandMode.INVENTORY) {
					sendAddMessage(mage, "hotbar_added", Integer.toString(newCount));
                }
			}
		}

		// Mix colors
		if (other.effectColor != null) {
			if (this.effectColor == null || other.isUpgrade()) {
				this.effectColor = other.effectColor;
			} else {
				this.effectColor = this.effectColor.mixColor(other.effectColor, other.effectColorMixWeight);
			}
			modified = true;
		}

        if (other.rename && other.template != null && other.template.length() > 0) {
            ConfigurationSection template = controller.getWandTemplateConfiguration(other.template);

            wandName = messages.get("wands." + other.template + ".name", wandName);
            wandName = template.getString("name", wandName);
            updateName();
        }

        if (other.renameDescription && other.template != null && other.template.length() > 0) {
            ConfigurationSection template = controller.getWandTemplateConfiguration(other.template);

            description = messages.get("wands." + other.template + ".description", description);
            description = template.getString("description", description);
            updateLore();
        }

        // Kind of a hacky way to allow for quiet level overries
        if (other.quietLevel < 0) {
            int quiet = -other.quietLevel - 1;
            modified = quietLevel != quiet;
            quietLevel = quiet;
        }

        for (Map.Entry<PotionEffectType, Integer> otherEffects : other.potionEffects.entrySet()) {
            Integer current = potionEffects.get(otherEffects.getKey());
            if (current == null || current < otherEffects.getValue()) {
                potionEffects.put(otherEffects.getKey(), otherEffects.getValue());
                modified = true;
            }
        }

		modified = modified | (!keep && other.keep);
		modified = modified | (!bound && other.bound);
		modified = modified | (!effectBubbles && other.effectBubbles);
        modified = modified | (!undroppable && other.undroppable);
        modified = modified | (!indestructible && other.indestructible);
        modified = modified | (!superPowered && other.superPowered);
        modified = modified | (!superProtected && other.superProtected);
        modified = modified | (!glow && other.glow);

		keep = keep || other.keep;
		bound = bound || other.bound;
        indestructible = indestructible || other.indestructible;
        superPowered = superPowered || other.superPowered;
        superProtected = superProtected || other.superProtected;
        glow = glow || other.glow;
        undroppable = undroppable || other.undroppable;
        effectBubbles = effectBubbles || other.effectBubbles;
		if (other.effectParticle != null && (other.isUpgrade || effectParticle == null)) {
			modified = modified | (effectParticle != other.effectParticle);
			effectParticle = other.effectParticle;
			modified = modified | (effectParticleData != other.effectParticleData);
			effectParticleData = other.effectParticleData;
			modified = modified | (effectParticleCount != other.effectParticleCount);
			effectParticleCount = other.effectParticleCount;
			modified = modified | (effectParticleInterval != other.effectParticleInterval);
			effectParticleInterval = other.effectParticleInterval;
            modified = modified | (effectParticleRadius != other.effectParticleRadius);
            effectParticleRadius = other.effectParticleRadius;
            modified = modified | (effectParticleOffset != other.effectParticleOffset);
            effectParticleOffset = other.effectParticleOffset;
            modified = modified | (effectParticleMinVelocity < other.effectParticleOffset);
            effectParticleMinVelocity = Math.max(effectParticleMinVelocity, other.effectParticleMinVelocity);
		}

        if (other.castSpell != null && (other.isUpgrade || castSpell == null || !castSpell.equals(other.castSpell))) {
            modified = true;
            castSpell = other.castSpell;
            castParameters = ConfigurationUtils.addConfigurations(castParameters, other.castParameters);
            castInterval = other.castInterval;
            castVelocityDirection = other.castVelocityDirection;
            castMinVelocity = other.castMinVelocity;
        }
		
		if (other.effectSound != null && (other.isUpgrade || effectSound == null)) {
			modified = modified | (effectSound == null || !effectSound.equals(other.effectSound));
            effectSound = other.effectSound;
			modified = modified | (effectSoundInterval != other.effectSoundInterval);
			effectSoundInterval = other.effectSoundInterval;
		}
		
		if ((template == null || template.length() == 0) && (other.template != null && other.template.length() > 0)) {
			modified = true;
			template = other.template;
		}

        if (other.isUpgrade && other.upgradeTemplate != null && (this.template == null || !this.template.equals(other.template))) {
            this.template = other.upgradeTemplate;
            modified = true;
        }
		
		if (other.isUpgrade && other.mode != null) {
            if (mode != other.mode) {
                if (isInventoryOpen()) {
                    closeInventory();
                }
                needsInventoryUpdate = true;
                modified = true;
                setMode(other.mode);
            }
		}

        if (needsInventoryUpdate) {
            String wandSpells = getSpellString();
            String wandMaterials = getMaterialString();
            if (wandMaterials.length() > 0 || wandSpells.length() > 0) {
                parseInventoryStrings(wandSpells, wandMaterials);
            }
        }

        if (other.isUpgrade && other.brushMode != null) {
            modified = modified | (brushMode != other.brushMode);
            setBrushMode(other.brushMode);
        }

        if (other.upgradeIcon != null && (this.icon == null
               || this.icon.getMaterial() != other.upgradeIcon.getMaterial()
               || !Objects.equal(this.icon.getData(), other.upgradeIcon.getData()))) {
            modified = true;
            this.setIcon(other.upgradeIcon);
        }

        if (other.isUpgrade && other.inactiveIcon != null
				&& other.inactiveIcon.getMaterial() != Material.AIR && other.inactiveIcon.getMaterial() != null &&
                (this.inactiveIcon == null
                        || this.inactiveIcon.getMaterial() != other.inactiveIcon.getMaterial()
                        || !Objects.equal(this.inactiveIcon.getData(), other.inactiveIcon.getData())
                ))
        {
            this.inactiveIcon = other.inactiveIcon;
            inactiveIcon.applyToItem(item);
            modified = true;
        }
		
		// Don't need mana if cost-free
		if (isCostFree()) {
			manaRegeneration = 0;
			manaMax = 0;
			mana = 0;
		} else {
			if (other.manaRegeneration > manaRegeneration) { manaRegeneration = other.manaRegeneration; modified = true; sendAddMessage(mage, "upgraded_property", getLevelString(messages, "wand.mana_regeneration", manaRegeneration, controller.getMaxManaRegeneration())); }
            if (other.manaPerDamage > manaPerDamage) { manaPerDamage = other.manaPerDamage; modified = true; sendAddMessage(mage, "upgraded_property", getLevelString(messages, "wand.mana_per_damage", manaPerDamage, controller.getMaxManaRegeneration())); }
			if (other.manaMax > manaMax) { manaMax = other.manaMax; modified = true; sendAddMessage(mage, "upgraded_property", getLevelString(messages, "wand.mana_amount", manaMax, controller.getMaxMana())); }
			if (other.mana > mana) {
                float previousMana = mana;
                mana = Math.min(manaMax, other.mana);
                if (mana > previousMana) {
                    if (mage != null)
                    {
                        String message = getMessage("mana_added").replace("$value", Integer.toString((int) mana)).replace("$wand", getName());
                        mage.sendMessage(message);
                    }
                    modified = true;
                }
            }
		}
		
		// Add spells
		Set<String> spells = other.getSpells();
		for (String spellKey : spells) {
            SpellTemplate currentSpell = getBaseSpell(spellKey);
			if (addSpell(spellKey)) {
				modified = true;
				String spellName = spellKey;
				SpellTemplate spell = controller.getSpellTemplate(spellKey);
				if (spell != null) spellName = spell.getName();

                if (mage != null) {
                    if (currentSpell != null) {
                        String levelDescription = spell.getLevelDescription();
                        if (levelDescription == null || levelDescription.isEmpty()) {
                            levelDescription = spellName;
                        }
                        mage.sendMessage(messages.get("wand.spell_upgraded").replace("$name", currentSpell.getName()).replace("$level", levelDescription).replace("$wand", getName()));
                        mage.sendMessage(spell.getUpgradeDescription().replace("$name", currentSpell.getName()));

						SpellUpgradeEvent upgradeEvent = new SpellUpgradeEvent(mage, this, currentSpell, spell);
						Bukkit.getPluginManager().callEvent(upgradeEvent);
                    } else {
                        mage.sendMessage(messages.get("wand.spell_added").replace("$name", spellName).replace("$wand", getName()));

						AddSpellEvent addEvent = new AddSpellEvent(mage, this, spell);
						Bukkit.getPluginManager().callEvent(addEvent);
                    }
                }
			}
		}

		// Add materials
		Set<String> materials = other.getBrushes();
		for (String materialKey : materials) {
			if (addBrush(materialKey)) {
				modified = true;
				if (mage != null) mage.sendMessage(messages.get("wand.brush_added").replace("$wand", getName()).replace("$name", MaterialBrush.getMaterialName(messages, materialKey)));
			}
		}

        // Add cast overrides
        if (other.castOverrides != null && other.castOverrides.size() > 0) {
            if (castOverrides == null) {
                castOverrides = new HashMap<>();
            }
            HashSet<String> upgradedSpells = new HashSet<>();
            for (Map.Entry<String, String> entry : other.castOverrides.entrySet()) {
                String overrideKey = entry.getKey();
                String currentValue = castOverrides.get(overrideKey);
                String value = entry.getValue();
                if (currentValue != null) {
                    try {
                        double currentDouble = Double.parseDouble(currentValue);
                        double newDouble = Double.parseDouble(value);
                        if (newDouble < currentDouble) {
                            value = currentValue;
                        }
                    } catch (Exception ex) {
                    }
                }

                boolean addOverride = currentValue == null || !value.equals(currentValue);
                modified = modified || addOverride;
                if (addOverride && mage != null && overrideKey.contains(".")) {
                    String[] pieces = StringUtils.split(overrideKey, '.');
                    String spellKey = pieces[0];
                    String spellName = spellKey;
                    if (!upgradedSpells.contains(spellKey)) {
                        SpellTemplate spell = controller.getSpellTemplate(spellKey);
                        if (spell != null) spellName = spell.getName();
                        mage.sendMessage(messages.get("wand.spell_override_upgraded").replace("$name", spellName));
                        upgradedSpells.add(spellKey);
                    }
                }
                castOverrides.put(entry.getKey(), entry.getValue());
            }
        }
		
		Player player = (mage == null) ? null : mage.getPlayer();
		if (other.autoFill && player != null) {
			this.fill(player, controller.getMaxWandFillLevel());
			modified = true;
			if (mage != null) mage.sendMessage(getMessage("filled").replace("$wand", getName()));
		}
		
		if (other.autoOrganize && mage != null) {
			this.organizeInventory(mage);
			modified = true;
			mage.sendMessage(getMessage("reorganized").replace("$wand", getName()));
		}

        if (other.autoAlphabetize) {
            this.alphabetizeInventory();
            modified = true;
            if (mage != null) mage.sendMessage(getMessage("alphabetized").replace("$wand", getName()));
        }

		saveItemState();
		updateName();
		updateMaxMana(false);
		updateLore();

		return modified;
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
			} catch(Exception ex) {
				
			}
		}
		
		return defaultValue;
	}
	
	public static WandAction parseWandAction(String actionString, WandAction defaultValue) {
		if (actionString != null && !actionString.isEmpty()) {
			try {
				defaultValue = WandAction.valueOf(actionString.toUpperCase());
			} catch(Exception ex) {

			}
		}

		return defaultValue;
	}
	
	private void updateActiveMaterial() {
		if (mage == null) return;
		
		if (activeMaterial == null) {
			mage.clearBuildingMaterial();
		} else {
			com.elmakers.mine.bukkit.api.block.MaterialBrush brush = mage.getBrush();
			brush.update(activeMaterial);
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
		if (mage != null && mage.cancel()) {
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
				Set<String> spells = getSpells();
				// Sanity check, so it'll switch to inventory next time
				updateHasInventory();
				if (spells.size() > 0) {
					activeSpell = spells.iterator().next();
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
		hasInventory = inventorySize > 1 || (inventorySize == 1 && hasPath());
	}
	
	@SuppressWarnings("deprecation")
	public void cycleInventory(int direction) {
		if (!hasInventory) {
			return;
		}
		if (isInventoryOpen()) {
			saveInventory();
			int inventoryCount = inventories.size();
			openInventoryPage = inventoryCount == 0 ? 0 : (openInventoryPage + inventoryCount + direction) % inventoryCount;
			updateInventory();
			if (mage != null && inventories.size() > 1) {
                if (!playEffects("cycle") && inventoryCycleSound != null) {
                    mage.playSoundEffect(inventoryCycleSound);
                }
				mage.getPlayer().updateInventory();
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
			currentHotbar = hotbarCount == 0 ? 0 : (currentHotbar + hotbarCount + direction) % hotbarCount;
			updateHotbar();
			if (!playEffects("cycle") && inventoryCycleSound != null) {
				mage.playSoundEffect(inventoryCycleSound);
			}
            updateHotbarStatus();
			DeprecatedUtils.updateInventory(mage.getPlayer());
		}
	}

    public void cycleInventory() {
        cycleInventory(1);
    }
	
	@SuppressWarnings("deprecation")
	public void openInventory() {
		if (mage == null) return;
		
		WandMode wandMode = getMode();
		if (wandMode == WandMode.CHEST) {
			inventoryIsOpen = true;
            if (!playEffects("open") && inventoryOpenSound != null) {
                mage.playSoundEffect(inventoryOpenSound);
            }
            updateInventory();
			mage.getPlayer().openInventory(getDisplayInventory());
		} else if (wandMode == WandMode.INVENTORY) {
			if (hasStoredInventory()) return;
            if (storeInventory()) {
				inventoryIsOpen = true;
                showActiveIcon(true);
                if (!playEffects("open") && inventoryOpenSound != null) {
					mage.playSoundEffect(inventoryOpenSound);
				}
				updateInventory();
                updateHotbarStatus();
				mage.getPlayer().updateInventory();
			}
		}
	}
	
	@Override
    public void closeInventory() {
		if (!isInventoryOpen()) return;
        controller.disableItemSpawn();
        WandMode mode = getMode();
        try {
            saveInventory();
            inventoryIsOpen = false;
            if (mage != null) {
                if (!playEffects("close") && inventoryCloseSound != null) {
                    mage.playSoundEffect(inventoryCloseSound);
                }
                if (mode == WandMode.INVENTORY) {
                    restoreInventory();
                    showActiveIcon(false);
                } else {
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
				if (isSpell(testItem) || isBrush(testItem)) {
					inventory.setItemInOffHand(new ItemStack(Material.AIR));
					DeprecatedUtils.updateInventory(mage.getPlayer());
				}
            }
        } catch (Throwable ex) {
            restoreInventory();
        }

        if (mode == WandMode.INVENTORY && mage != null) {
            try {
                mage.getPlayer().closeInventory();
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
        controller.enableItemSpawn();
	}

    @Override
    public boolean fill(Player player) {
        return fill(player, 0);
    }

    @Override
	public boolean fill(Player player, int maxLevel) {
        Collection<String> currentSpells = new ArrayList<>(getSpells());
        for (String spellKey : currentSpells) {
            SpellTemplate spell = controller.getSpellTemplate(spellKey);
            if (!spell.hasCastPermission(player))
            {
                removeSpell(spellKey);
            }
        }

		Collection<SpellTemplate> allSpells = controller.getPlugin().getSpellTemplates();
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
			if (spell.hasCastPermission(player) && spell.hasIcon() && !spell.isHidden())
			{
				addSpell(key);
			}
		}
		
		autoFill = false;
		saveItemState();
		
		return true;
	}

    protected void randomize() {
        boolean modified = randomize;
        randomize = false;
        if (description.contains("$")) {
            String newDescription = controller.getMessages().escape(description);
            if (!newDescription.equals(description)) {
                description = newDescription;
                modified = true;
                updateLore();
                updateName();
            }
        }

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
                modified = true;
            }
        }

        if (modified) {
            saveItemState();
        }
    }
	
	protected void checkActiveMaterial() {
		if (activeMaterial == null || activeMaterial.length() == 0) {
			Set<String> materials = getBrushes();
			if (materials.size() > 0) {
				activeMaterial = materials.iterator().next();
			}
		}
	}

    @Override
	public boolean addItem(ItemStack item) {
		if (isUpgrade) return false;

		if (isModifiable() && isSpell(item) && !isSkill(item)) {
			String spellKey = getSpell(item);
            SpellTemplate currentSpell = getBaseSpell(spellKey);
			Set<String> spells = getSpells();
			if (!spells.contains(spellKey) && addSpell(spellKey)) {
				SpellTemplate spell = controller.getSpellTemplate(spellKey);
				if (spell != null) {
                    if (mage != null) {
                        if (currentSpell != null) {
                            String levelDescription = spell.getLevelDescription();
                            if (levelDescription == null || levelDescription.isEmpty()) {
                                levelDescription = spell.getName();
                            }
                            mage.sendMessage(getMessage("spell_upgraded").replace("$wand", getName()).replace("$name", currentSpell.getName()).replace("$level", levelDescription));
                            mage.sendMessage(spell.getUpgradeDescription().replace("$name", currentSpell.getName()));

							SpellUpgradeEvent upgradeEvent = new SpellUpgradeEvent(mage, this, currentSpell, spell);
							Bukkit.getPluginManager().callEvent(upgradeEvent);
                        } else {
                            mage.sendMessage(getMessage("spell_added").replace("$wand", getName()).replace("$name", spell.getName()));
                            activeSpell = spell.getKey();

							AddSpellEvent addEvent = new AddSpellEvent(mage, this, spell);
							Bukkit.getPluginManager().callEvent(addEvent);
                        }
                    }
                    return true;
				}
			}
		} else if (isModifiable() && isBrush(item)) {
			String materialKey = getBrush(item);
			Set<String> materials = getBrushes();
			if (!materials.contains(materialKey) && addBrush(materialKey)) {
                if (mage != null) {
                    Messages messages = controller.getMessages();
                    mage.sendMessage(messages.get("wand.brush_added").replace("$wand", getName()).replace("$name", MaterialBrush.getMaterialName(messages, materialKey)));
                }
                return true;
			}
		} else if (isUpgrade(item)) {
			Wand wand = controller.getWand(item);
			return this.add(wand);
		}
		if (mage != null && !mage.isAtMaxSkillPoints()) {
			Integer sp = getSP(item);
			if (sp != null) {
				mage.addSkillPoints(sp * item.getAmount());
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
			CompatibilityUtils.addPotionEffect(player, effectColor.getColor());
            effectBubblesApplied = true;
		} else if (effectBubblesApplied) {
            effectBubblesApplied = false;
            CompatibilityUtils.removePotionEffect(player);
        }
		
		Location location = mage.getLocation();
		long now = System.currentTimeMillis();
        Vector mageLocation = location.toVector();
		if (effectParticle != null && effectParticleInterval > 0 && effectParticleCount > 0) {
            boolean velocityCheck = true;
            if (effectParticleMinVelocity > 0) {
                if (lastLocation != null && lastLocationTime != 0) {
                    double velocitySquared = effectParticleMinVelocity * effectParticleMinVelocity;
                    Vector velocity = lastLocation.subtract(mageLocation);
                    velocity.setY(0);
                    double speedSquared = velocity.lengthSquared() * 1000 / (now - lastLocationTime);
                    velocityCheck = (speedSquared > velocitySquared);
                } else {
                    velocityCheck = false;
                }
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
            boolean velocityCheck = true;
            if (castMinVelocity > 0) {
                if (lastLocation != null && lastLocationTime != 0) {
                    double velocitySquared = castMinVelocity * castMinVelocity;
                    Vector velocity = lastLocation.subtract(mageLocation).multiply(-1);
                    if (castVelocityDirection != null) {
                        velocity = velocity.multiply(castVelocityDirection);

                        // This is kind of a hack to make jump-detection work.
                        if (castVelocityDirection.getY() < 0) {
                            velocityCheck = velocity.getY() < 0;
                        } else {
                            velocityCheck = velocity.getY() > 0;
                        }
                    }
                    if (velocityCheck)
                    {
                        double speedSquared = velocity.lengthSquared() * 1000 / (now - lastLocationTime);
                        velocityCheck = (speedSquared > velocitySquared);
                    }
                } else {
                    velocityCheck = false;
                }
            }
            if (velocityCheck && (lastSpellCast == 0 || now > lastSpellCast + castInterval)) {
                lastSpellCast = now;
                Spell spell = mage.getSpell(castSpell);
                if (spell != null) {
					if (castParameters == null) {
						castParameters = new MemoryConfiguration();
					}
                    castParameters.set("track_casts", false);
                    mage.setCostReduction(100);
                    mage.setQuiet(true);
                    try {
                        spell.cast(castParameters);
                    } catch (Exception ex) {
                        controller.getLogger().log(Level.WARNING, "Error casting aura spell " + spell.getKey(), ex);
                    }
                    mage.setQuiet(false);
                    mage.setCostReduction(0);
                }
            }
        }
		
		if (effectSound != null && controller.soundsEnabled() && effectSoundInterval > 0) {
			if (lastSoundEffect == 0 || now > lastSoundEffect + effectSoundInterval) {
                lastSoundEffect = now;
				mage.getLocation().getWorld().playSound(location, effectSound.getSound(), effectSound.getVolume(), effectSound.getPitch());
			}
		}

        lastLocation = mageLocation;
        lastLocationTime = now;
	}

    protected void updateDurability() {
        int maxDurability = item.getType().getMaxDurability();
        if (maxDurability > 0 && effectiveManaMax > 0) {
            int durability = (short)(mana * maxDurability / effectiveManaMax);
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
        return (hasSpellProgression && spMode.useXP()) || (usesMana() && manaMode.useXP());
    }

    public boolean usesXPNumber()
    {
        return (hasSpellProgression && spMode.useXPNumber() && controller.isSPEnabled()) || (usesMana() && manaMode.useXP());
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
                playerProgress = Math.max(0, mana / effectiveManaMax);
            }
            if (controller.isSPEnabled() && spMode.useXPNumber() && hasSpellProgression)
            {
                playerLevel = mage.getSkillPoints();
            }
			
			mage.sendExperience(playerProgress, playerLevel);
        }
	}

	@Override
	public boolean isInventoryOpen() {
		return mage != null && inventoryIsOpen;
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
		saveState();
	}
	
	@Override
	public void bind() {
		if (bound) return;
		
		Mage holdingMage = mage;
		deactivate();
		bound = true;
		saveState();
		
		if (holdingMage != null) {
			holdingMage.checkWand();
		}
	}
	
	@Override
    public void deactivate() {
		if (mage == null) return;

        // Play deactivate FX
        playEffects("deactivate");

        Mage mage = this.mage;
        Player player = mage.getPlayer();
		if (effectBubblesApplied && player != null) {
			CompatibilityUtils.removePotionEffect(player);
            effectBubblesApplied = false;
		}
		
		if (isInventoryOpen()) {
			closeInventory();
		}
		showActiveIcon(false);
        storedInventory = null;
		if (usesXPNumber() || usesXPBar()) {
			mage.resetSentExperience();
		}
        saveItemState();
		mage.setActiveWand(null);
		this.mage = null;
		updateMaxMana(true);
	}
	
	@Override
    public Spell getActiveSpell() {
		if (mage == null || activeSpell == null || activeSpell.length() == 0) return null;
		return mage.getSpell(activeSpell);
	}

    @Override
    public SpellTemplate getBaseSpell(String spellName) {
        SpellKey key = new SpellKey(spellName);
        Integer spellLevel = spellLevels.get(key.getBaseKey());
        if (spellLevel == null) return null;

        String spellKey = key.getBaseKey();
        if (key.isVariant()) {
            spellKey += "|" + key.getLevel();
        }
        return controller.getSpellTemplate(spellKey);
    }

    @Override
    public String getActiveSpellKey() {
        return activeSpell;
    }

    @Override
    public String getActiveBrushKey() {
        return activeMaterial;
    }

    @Override
    public void damageDealt(double damage, Entity target) {
        if (effectiveManaMax == 0 && manaMax > 0) {
            effectiveManaMax = manaMax;
        }
        if (manaPerDamage > 0 && effectiveManaMax > 0 && mana < effectiveManaMax) {
            mana = Math.min(effectiveManaMax, mana + (float)damage * manaPerDamage);
            updateMana();
        }
    }

    @Override
    public boolean cast() {
        return cast(getActiveSpell());
    }

	public boolean cast(Spell spell) {
		if (spell != null) {
            Collection<String> castParameters = null;
            if (castOverrides != null && castOverrides.size() > 0) {
                castParameters = new ArrayList<>();
                for (Map.Entry<String, String> entry : castOverrides.entrySet()) {
                    String[] key = StringUtils.split(entry.getKey(), ".");
                    if (key.length == 0) continue;
                    if (key.length == 2 && !key[0].equals("default") && !key[0].equals(spell.getSpellKey().getBaseKey()) && !key[0].equals(spell.getSpellKey().getKey())) {
                        continue;
                    }
                    castParameters.add(key.length == 2 ? key[1] : key[0]);
                    castParameters.add(entry.getValue());
                }
            }
			if (spell.cast(castParameters == null ? null : castParameters.toArray(EMPTY_PARAMETERS))) {
				Color spellColor = spell.getColor();
                use();
				if (spellColor != null && this.effectColor != null) {
					this.effectColor = this.effectColor.mixColor(spellColor, effectColorSpellMixWeight);
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
	
	@SuppressWarnings("deprecation")
	protected void use() {
		if (mage == null) return;
		if (hasUses) {
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
                if (uses <= 0) {
                    Player player = mage.getPlayer();

                    deactivate();

                    PlayerInventory playerInventory = player.getInventory();
                    item = player.getItemInHand();
                    if (item.getAmount() > 1) {
                        item.setAmount(item.getAmount() - 1);
                    } else {
                        playerInventory.setItemInHand(new ItemStack(Material.AIR, 1));
                    }
                    player.updateInventory();
                } else {
                    saveItemState();
                    updateName();
                    updateLore();
                }
            }
		}
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
	
	public boolean tickMana(Player player) {
		boolean updated = false;
		if (usesMana()) {
			long now = System.currentTimeMillis();
			if (isHeroes)
			{
				HeroesManager heroes = controller.getHeroes();
				if (heroes != null)
				{
					effectiveManaMax = heroes.getMaxMana(player);
					effectiveManaRegeneration = heroes.getManaRegen(player);
					manaMax = effectiveManaMax;
					manaRegeneration = effectiveManaRegeneration;
					mana = heroes.getMana(player);
					updated = true;
				}
			}
			else if (manaRegeneration > 0 && lastManaRegeneration > 0 && effectiveManaRegeneration > 0)
			{
				long delta = now - lastManaRegeneration;
				if (effectiveManaMax == 0 && manaMax > 0) {
					effectiveManaMax = manaMax;
				}
				mana = Math.min(effectiveManaMax, mana + (float) effectiveManaRegeneration * (float)delta / 1000);
				updated = true;
			}
			lastManaRegeneration = now;
		}
		
		return updated;
	}
	
	public void tick() {
		if (mage == null) return;
		
		Player player = mage.getPlayer();
		if (player == null) return;

		if (tickMana(player)) {
			updateMana();
		}

        // Update hotbar glow
        updateHotbarStatus();

        if (!passive)
        {
            if (damageReductionFire > 0 && player.getFireTicks() > 0) {
                player.setFireTicks(0);
            }

            updateEffects();
        }
    }

    public void armorUpdated() {
        updateMaxMana(true);
    }

    protected void updateMaxMana(boolean updateLore) {
        if (isHeroes) return;

		int currentMana = effectiveManaMax;
		int currentManaRegen = effectiveManaRegeneration;

        float effectiveBoost = manaMaxBoost;
        float effectiveRegenBoost = manaRegenerationBoost;
        if (mage != null)
        {
            Collection<Wand> activeArmor = mage.getActiveArmor();
            for (Wand armorWand : activeArmor) {
                effectiveBoost += armorWand.getManaMaxBoost();
                effectiveRegenBoost += armorWand.getManaRegenerationBoost();
            }
        }
        effectiveManaMax = manaMax;
        if (effectiveBoost != 0) {
            effectiveManaMax = (int)Math.ceil(effectiveManaMax + effectiveBoost * effectiveManaMax);
        }
        effectiveManaRegeneration = manaRegeneration;
        if (effectiveRegenBoost != 0) {
            effectiveManaRegeneration = (int)Math.ceil(effectiveManaRegeneration + effectiveRegenBoost * effectiveManaRegeneration);
        }

		if (updateLore && (currentMana != effectiveManaMax || effectiveManaRegeneration != currentManaRegen)) {
			updateLore();
		}
    }

    public static Float getWandFloat(ItemStack item, String key) {
        try {
            Object wandNode = InventoryUtils.getNode(item, WAND_KEY);
            if (wandNode != null) {
                String value = InventoryUtils.getMeta(wandNode, key);
                if (value != null && !value.isEmpty()) {
                    return Float.parseFloat(value);
                }
            }
        } catch (Exception ex) {

        }
        return null;
    }

    public static String getWandString(ItemStack item, String key) {
        try {
            Object wandNode = InventoryUtils.getNode(item, WAND_KEY);
            if (wandNode != null) {
                return InventoryUtils.getMeta(wandNode, key);
            }
        } catch (Exception ex) {

        }
        return null;
    }
	
	public MagicController getMaster() {
		return controller;
	}
	
	public void cycleSpells(int direction) {
		Set<String> spellsSet = getSpells();
		ArrayList<String> spells = new ArrayList<>(spellsSet);
		if (spells.size() == 0) return;
		if (activeSpell == null) {
			activeSpell = spells.get(0).split("@")[0];
			return;
		}
		
		int spellIndex = 0;
		for (int i = 0; i < spells.size(); i++) {
			if (spells.get(i).split("@")[0].equals(activeSpell)) {
				spellIndex = i;
				break;
			}
		}
		
		spellIndex = (spellIndex + direction) % spells.size();
		setActiveSpell(spells.get(spellIndex).split("@")[0]);
	}
	
	public void cycleMaterials(int direction) {
		Set<String> materialsSet = getBrushes();
		ArrayList<String> materials = new ArrayList<>(materialsSet);
		if (materials.size() == 0) return;
		if (activeMaterial == null) {
			activeMaterial = materials.get(0).split("@")[0];
			return;
		}
		
		int materialIndex = 0;
		for (int i = 0; i < materials.size(); i++) {
			if (materials.get(i).split("@")[0].equals(activeMaterial)) {
				materialIndex = i;
				break;
			}
		}
		
		materialIndex = (materialIndex + direction) % materials.size();
		setActiveBrush(materials.get(materialIndex).split("@")[0]);
	}

	public Mage getActivePlayer() {
		return mage;
	}
	
	public Color getEffectColor() {
		return effectColor == null ? null : effectColor.getColor();
	}

    public ParticleEffect getEffectParticle() {
        return effectParticle;
    }

	public Inventory getHotbar() {
        if (this.hotbars.size() == 0) return null;

		if (currentHotbar < 0 || currentHotbar >= this.hotbars.size())
		{
			currentHotbar = 0;
		}
		return this.hotbars.get(currentHotbar);
	}

    public int getHotbarCount() {
        if (getMode() != WandMode.INVENTORY) return 0;
        return hotbars.size();
    }

	public List<Inventory> getHotbars() {
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
        WandMode wandMode = mode != null ? mode : controller.getDefaultWandMode();
        Player player = mage == null ? null : mage.getPlayer();
        if (wandMode == WandMode.INVENTORY && player != null && player.getGameMode() == GameMode.CREATIVE) {
            wandMode = WandMode.CHEST;
        }
		return wandMode;
	}

    public WandMode getBrushMode() {
        return brushMode != null ? brushMode : controller.getDefaultBrushMode();
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
        this.path = path;
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
        return new LostWand(this, location);
    }

	@Override
	public void activate(com.elmakers.mine.bukkit.api.magic.Mage mage) {
		Player player = mage.getPlayer();
		if (!Wand.hasActiveWand(player)) {
			controller.getLogger().warning("Wand activated without holding a wand!");
			return;
		}
		
		if (mage instanceof Mage) {
			activate((Mage)mage);
		}
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
                        updateIcon();
						// Needed because if the inventory has opened the item reference may be stale (?)
						if (mage != null) {
							Player player = mage.getPlayer();
							if (player != null) {
								player.getInventory().setItem(storedSlot, item);
							}
						}
                    }
                }, inactiveIconDelay * 20 / 1000);
            } else {
                icon.applyToItem(item);
            }
        } else {
            inactiveIcon.applyToItem(this.item);
        }
    }
	
	public void setMage(Mage mage) {
		this.mage = mage;
	}

    public void activate(Mage mage) {
        if (mage == null) return;
        Player player = mage.getPlayer();
        if (player != null) {
			if (!controller.hasWandPermission(player, this)) return;
			InventoryView openInventory = player.getOpenInventory();
			InventoryType inventoryType = openInventory.getType();
			if (inventoryType == InventoryType.ENCHANTING ||
				inventoryType == InventoryType.ANVIL) return;
        }

        if (!canUse(player)) {
            mage.sendMessage(getMessage("bound").replace("$name", getOwner()));
            mage.setActiveWand(null);
            return;
        }

		WandTemplate template = getTemplate();
		Wand soulWand = mage.getSoulWand();
		if (template != null && template.isSoul() && soulWand != this) {
			if (!soul) {
				// Migrate old wands
				this.add(soulWand);
				MemoryConfiguration soulConfiguration = new MemoryConfiguration();
				saveProperties(soulConfiguration);
				soulWand.loadProperties(soulConfiguration);
				soul = true;
				saveState();
			}
			soulWand.soul = true;
			soulWand.item = this.item;
			soulWand.activate(mage);
			return;
		}

		this.newId();

		if (getMode() != WandMode.INVENTORY) {
			showActiveIcon(true);
		}

        if (this.isUpgrade) {
            controller.getLogger().warning("Activated an upgrade item- this shouldn't happen");
            return;
        }

        WandPreActivateEvent preActivateEvent = new WandPreActivateEvent(mage, this);
        Bukkit.getPluginManager().callEvent(preActivateEvent);
        if (preActivateEvent.isCancelled()) {
            return;
        }

        // Update held item, it may have been copied since this wand was created.
        this.mage = mage;
        boolean forceUpdate = false;

        // Check for an empty wand and auto-fill
        if (!isUpgrade && (controller.fillWands() || autoFill)) {
            fill(mage.getPlayer(), controller.getMaxWandFillLevel());
        }

        if (isHeroes && player != null) {
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
                    if (!spells.containsKey(heroesKey))
                    {
                        addSpell(heroesKey);
                    }
                }
                this.mage = mage;
            }
        }

        // Check for auto-organize
        if (autoOrganize && !isUpgrade) {
            organizeInventory(mage);
        }

        // Check for auto-alphabetize
        if (autoAlphabetize && !isUpgrade) {
            alphabetizeInventory();
        }

        // Check for spell or other special icons in the player's inventory
        Inventory inventory = player.getInventory();
        ItemStack[] items = inventory.getContents();
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (addItem(item)) {
                inventory.setItem(i, null);
                forceUpdate = true;
            }
        }

        // Check for auto-bind
        if (bound)
        {
            String mageName = ChatColor.stripColor(mage.getPlayer().getDisplayName());
            String mageId = mage.getPlayer().getUniqueId().toString();
            boolean ownerRenamed = owner != null && ownerId != null && ownerId.equals(mageId) && !owner.equals(mageName);

            if (ownerId == null || ownerId.length() == 0 || owner == null || ownerRenamed)
            {
                takeOwnership(mage.getPlayer());
            }
        }

        // Check for randomized wands
        if (randomize) {
            randomize();
            forceUpdate = true;
        }

        checkActiveMaterial();

        mage.setActiveWand(this);
        tick();
        saveItemState();

		updateMaxMana(false);
        updateActiveMaterial();
        updateName();
        updateLore();

        // Play activate FX
        playEffects("activate");

        lastSoundEffect = 0;
        lastParticleEffect = 0;
        lastSpellCast = 0;
        lastLocationTime = 0;
        lastLocation = null;
        if (forceUpdate) {
            DeprecatedUtils.updateInventory(player);
        }

        WandActivatedEvent activatedEvent = new WandActivatedEvent(mage, this);
        Bukkit.getPluginManager().callEvent(activatedEvent);
    }

	@Override
	public void organizeInventory(com.elmakers.mine.bukkit.api.magic.Mage mage) {
        WandOrganizer organizer = new WandOrganizer(this, mage);
        organizer.organize();
        openInventoryPage = 0;
		currentHotbar = 0;
        autoOrganize = false;
        autoAlphabetize = false;

		// Force inventory re-load
		saveItemState();
		loadState();
		updateInventory();
    }

    @Override
    public void alphabetizeInventory() {
        WandOrganizer organizer = new WandOrganizer(this);
        organizer.alphabetize();
        openInventoryPage = 0;
		currentHotbar = 0;
        autoOrganize = false;
        autoAlphabetize = false;

		// Force inventory re-load
		saveItemState();
		loadState();
		updateInventory();
    }

	@Override
	public com.elmakers.mine.bukkit.api.wand.Wand duplicate() {
		ItemStack newItem = InventoryUtils.getCopy(item);
		Wand newWand = controller.getWand(newItem);
		newWand.saveItemState();
		return newWand;
	}

	@Override
	public boolean configure(Map<String, Object> properties) {
		Map<Object, Object> convertedProperties = new HashMap<Object, Object>(properties);
		loadProperties(ConfigurationUtils.toConfigurationSection(convertedProperties), false);
        saveItemState();
        updateName();
        updateLore();
		return true;
	}

	@Override
	public boolean upgrade(Map<String, Object> properties) {
		Map<Object, Object> convertedProperties = new HashMap<Object, Object>(properties);
		loadProperties(ConfigurationUtils.toConfigurationSection(convertedProperties), true);
        saveItemState();
        updateName();
        updateLore();
		return true;
	}

	@Override
	public boolean isLocked() {
		return this.locked;
	}

    @Override
    public void unlock() {
        locked = false;
    }

    public boolean isPassive() {
        return passive;
    }

	@Override
	public boolean canUse(Player player) {
		if (!bound || ownerId == null || ownerId.length() == 0) return true;
		if (controller.hasPermission(player, "Magic.wand.override_bind", false)) return true;

		return ownerId.equalsIgnoreCase(player.getUniqueId().toString());
	}
	
	@Override
    public boolean addSpell(String spellName) {
		if (!isModifiable()) return false;

        SpellKey spellKey = new SpellKey(spellName);
        if (hasSpell(spellKey)) {
            return false;
        }

        if (isInventoryOpen()) {
			saveInventory();
		}
        SpellTemplate template = controller.getSpellTemplate(spellName);
        if (template == null) {
            controller.getLogger().warning("Tried to add unknown spell to wand: " + spellName);
            return false;
        }

        // This handles adding via an alias
        if (hasSpell(template.getKey())) return false;

		ItemStack spellItem = createSpellIcon(template);
		if (spellItem == null) {
			return false;
		}
        spellKey = template.getSpellKey();
        int level = spellKey.getLevel();
        int inventoryCount = inventories.size();
        int spellCount = spells.size();

        // Special handling for spell upgrades and spells to remove
        Integer inventorySlot = null;
        Integer currentLevel = spellLevels.get(spellKey.getBaseKey());
        if (currentLevel != null) {
            if (activeSpell != null && !activeSpell.isEmpty()) {
                SpellKey currentKey = new SpellKey(activeSpell);
                if (currentKey.getBaseKey().equals(spellKey.getBaseKey())) {
                    activeSpell = spellKey.getKey();
                }
            }
        }
        List<SpellKey> spellsToRemove = new ArrayList<>(template.getSpellsToRemove().size());
        for (SpellKey key : template.getSpellsToRemove()) {
            if (spellLevels.get(key.getBaseKey()) != null) {
                spellsToRemove.add(key);
            }
        }
        if (currentLevel != null || !spellsToRemove.isEmpty()) {
            List<Inventory> allInventories = getAllInventories();
            int currentSlot = 0;
            for (Inventory inventory : allInventories) {
                ItemStack[] items = inventory.getContents();
                for (int index = 0; index < items.length; index++) {
                    ItemStack itemStack = items[index];
                    if (isSpell(itemStack)) {
                        SpellKey checkKey = new SpellKey(getSpell(itemStack));
                        if (checkKey.getBaseKey().equals(spellKey.getBaseKey())) {
                            inventorySlot = currentSlot;
                            inventory.setItem(index, null);
                            spells.remove(checkKey.getKey());
                        } else {
                            for (SpellKey key : spellsToRemove) {
                                if (checkKey.getBaseKey().equals(key.getBaseKey())) {
                                    inventory.setItem(index, null);
                                    spells.remove(key.getKey());
                                    spellLevels.remove(key.getBaseKey());
                                }
                            }
                        }
                    }
                    currentSlot++;
                }
            }
        }
        if (activeSpell == null || activeSpell.isEmpty()) {
            activeSpell = spellKey.getKey();
        }

        spellLevels.put(spellKey.getBaseKey(), level);
        spells.put(template.getKey(), inventorySlot);
		addToInventory(spellItem, inventorySlot);
		updateInventory();
		updateHasInventory();
        saveItemState();
		updateLore();

        if (mage != null && spells.size() != spellCount)
        {
            if (spellCount == 0)
            {
                String message = getMessage("spell_instructions", "").replace("$wand", getName());
                mage.sendMessage(message.replace("$spell", template.getName()));
            }
            else
            if (spellCount == 1)
            {
                mage.sendMessage(getMessage("inventory_instructions", "").replace("$wand", getName()));
            }
            if (inventoryCount == 1 && inventories.size() > 1)
            {
                mage.sendMessage(getMessage("page_instructions", "").replace("$wand", getName()));
            }
        }
		
		return true;
	}

    public String getMessage(String key) {
        return getMessage(key, "");
    }

    public String getMessage(String key, String defaultValue) {
        String message = controller.getMessages().get("wand." + key, defaultValue);
        if (template != null && !template.isEmpty()) {
            message = controller.getMessages().get("wands." + template + "." + key, message);
        }
        return message;
    }

	@Override
	public boolean add(com.elmakers.mine.bukkit.api.wand.Wand other) {
		if (other instanceof Wand) {
			return add((Wand)other);
		}
		
		return false;
	}

    @Override
	public boolean hasBrush(String materialKey) {
		return getBrushes().contains(materialKey);
	}
	
	@Override
	public boolean hasSpell(String spellName) {
		return hasSpell(new SpellKey(spellName));
	}

    public boolean hasSpell(SpellKey spellKey) {
        Integer level = spellLevels.get(spellKey.getBaseKey());
        return (level != null && level >= spellKey.getLevel());
    }
	
	@Override
	public boolean addBrush(String materialKey) {
		if (!isModifiable()) return false;
		if (hasBrush(materialKey)) return false;

        if (isInventoryOpen()) {
            saveInventory();
        }
		
		ItemStack itemStack = createBrushIcon(materialKey);
		if (itemStack == null) return false;

        int inventoryCount = inventories.size();
        int brushCount = brushes.size();

        brushes.put(materialKey, null);
		addToInventory(itemStack);
		if (activeMaterial == null || activeMaterial.length() == 0) {
			activateBrush(materialKey);
		} else {
			updateInventory();
		}
		updateHasInventory();
        saveItemState();
		updateLore();

        if (mage != null)
        {
            if (brushCount == 0)
            {
                mage.sendMessage(getMessage("brush_instructions").replace("$wand", getName()));
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

        if (mage != null) {
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
    }

    public void setActiveBrush(ItemStack itemStack) {
        if (!isBrush(itemStack)) return;
        setActiveBrush(getBrush(itemStack));
    }

	public void activateBrush(String materialKey) {
		this.activeMaterial = materialKey;
        saveItemState();
		updateName();
		updateActiveMaterial();
        updateHotbar();
	}

	@Override
	public void setActiveSpell(String activeSpell) {
        SpellKey spellKey = new SpellKey(activeSpell);
        activeSpell = spellKey.getBaseKey();
        if (!spellLevels.containsKey(activeSpell))
        {
            return;
        }
        spellKey = new SpellKey(spellKey.getBaseKey(), spellLevels.get(activeSpell));
		this.activeSpell = spellKey.getKey();
        saveItemState();
		updateName();
	}

	@Override
	public boolean removeBrush(String materialKey) {
		if (!isModifiable() || materialKey == null) return false;
		
		if (isInventoryOpen()) {
			saveInventory();
		}
		if (materialKey.equals(activeMaterial)) {
			activeMaterial = null;
		}
        brushes.remove(materialKey);
		List<Inventory> allInventories = getAllInventories();
		boolean found = false;
		for (Inventory inventory : allInventories) {
			ItemStack[] items = inventory.getContents();
			for (int index = 0; index < items.length; index++) {
				ItemStack itemStack = items[index];
				if (itemStack != null && isBrush(itemStack)) {
					String itemKey = getBrush(itemStack);
					if (itemKey.equals(materialKey)) {
						found = true;
						inventory.setItem(index, null);
					} else if (activeMaterial == null) {
						activeMaterial = materialKey;
					}
					if (found && activeMaterial != null) {
						break;
					}
				}
			}
		}
		updateActiveMaterial();
		updateInventory();
        saveItemState();
		updateName();
		updateLore();
		return found;
	}
	
	@Override
	public boolean removeSpell(String spellName) {
		if (!isModifiable()) return false;
		
		if (isInventoryOpen()) {
			saveInventory();
		}
		if (spellName.equals(activeSpell)) {
			activeSpell = null;
		}
        spells.remove(spellName);
        SpellKey spellKey = new SpellKey(spellName);
        spellLevels.remove(spellKey.getBaseKey());
		
		List<Inventory> allInventories = getAllInventories();
		boolean found = false;
		for (Inventory inventory : allInventories) {
			ItemStack[] items = inventory.getContents();
			for (int index = 0; index < items.length; index++) {
				ItemStack itemStack = items[index];
				if (itemStack != null && itemStack.getType() != Material.AIR && isSpell(itemStack)) {
					if (getSpell(itemStack).equals(spellName)) {
						found = true;
						inventory.setItem(index, null);
					} else if (activeSpell == null) {
						activeSpell = getSpell(itemStack);
					}
					if (found && activeSpell != null) {
						break;
					}
				}
			}
		}
        updateInventory();
		updateHasInventory();
        saveItemState();
        updateName();
        updateLore();

		return found;
	}

    @Override
    public Map<String, String> getOverrides()
    {
        return castOverrides == null ? new HashMap<String, String>() : new HashMap<>(castOverrides);
    }

    @Override
    public void setOverrides(Map<String, String> overrides)
    {
        if (overrides == null) {
            this.castOverrides = null;
        } else {
            this.castOverrides = new HashMap<>(overrides);
        }
    }

    @Override
    public void removeOverride(String key)
    {
        if (castOverrides != null) {
            castOverrides.remove(key);
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

    public void setStoredSlot(int slot) {
        this.storedSlot = slot;
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
			// Make sure we don't store any spells or magical materials, just in case
			ItemStack item = inventory.getItem(i);
			if (!Wand.isSpell(item) || Wand.isSkill(item)) {
				storedInventory.setItem(i, item);
			}
			inventory.setItem(i, null);
		}
        storedSlot = inventory.getHeldItemSlot();
		inventory.setItem(storedSlot, item);

        return true;
    }

    @SuppressWarnings("deprecation")
    public boolean restoreInventory() {
        if (storedInventory == null) {
            return false;
        }
        Player player = mage.getPlayer();
        if (player == null) {
            return false;
        }

		PlayerInventory inventory = player.getInventory();
		// Check for the wand having been removed somehow, we don't want to put it back
		// if that happened.
		// This fixes dupe issues with armor stands, among other things
		// We do need to account for the wand not being the active slot anymore
		ItemStack storedItem = storedInventory.getItem(storedSlot);
		ItemStack currentItem = inventory.getItem(storedSlot);
		String currentId = getWandId(currentItem);
		String storedId = getWandId(storedItem);
		if (storedId != null && storedId.equals(id) && !Objects.equal(currentId, id)) {
			// Hacky special-case to avoid glitching spells out of the inventory
			// via the offhand slot.
			if (isSpell(currentItem)) {
				currentItem = null;
			}
			storedInventory.setItem(storedSlot, currentItem);
			controller.info("Cleared wand on inv close for player " + player.getName());
		}

		for (int i = 0; i < storedInventory.getSize(); i++) {
			inventory.setItem(i, storedInventory.getItem(i));
		}
        storedInventory = null;
        saveItemState();

        inventory.setHeldItemSlot(storedSlot);

        player.updateInventory();

        return true;
    }

    @Override
    public boolean isSoul() {
        return soul;
    }

    @Override
    public boolean isBound() {
        return bound;
    }
	
	@Override
	public Spell getSpell(String spellKey, com.elmakers.mine.bukkit.api.magic.Mage mage) {
		if (mage == null) {
			return null;
		}
		SpellKey key = new SpellKey(spellKey);
		String baseKey = key.getBaseKey();
		Integer level = spellLevels.get(baseKey);
		if (level == null) {
			return null;
		}
		SpellKey levelKey = new SpellKey(baseKey, level);
		return mage.getSpell(levelKey.getKey());
	}
	
	@Override
	public SpellTemplate getSpellTemplate(String spellKey) {
		SpellKey key = new SpellKey(spellKey);
		String baseKey = key.getBaseKey();
		Integer level = spellLevels.get(baseKey);
		if (level == null) {
			return null;
		}
		SpellKey levelKey = new SpellKey(baseKey, level);
		return controller.getSpellTemplate(levelKey.getKey());
	}
	
	@Override
    public Spell getSpell(String spellKey) {
		return getSpell(spellKey, mage);
    }

    @Override
    public int getSpellLevel(String spellKey) {
        SpellKey key = new SpellKey(spellKey);
        Integer level = spellLevels.get(key.getBaseKey());
        return level == null ? 0 : level;
    }

    @Override
    public MageController getController() {
        return controller;
    }

    protected Map<String, Integer> getSpellInventory() {
        return new HashMap<>(spells);
    }

    protected Map<String, Integer> getBrushInventory() {
        return new HashMap<>(brushes);
    }

    protected void updateSpellInventory(Map<String, Integer> updateSpells) {
        for (Map.Entry<String, Integer> spellEntry : spells.entrySet()) {
            String spellKey = spellEntry.getKey();
            Integer slot = updateSpells.get(spellKey);
            if (slot != null) {
                spellEntry.setValue(slot);
            }
        }
    }

    protected void updateBrushInventory(Map<String, Integer> updateBrushes) {
        for (Map.Entry<String, Integer> brushEntry : brushes.entrySet()) {
            String brushKey = brushEntry.getKey();
            Integer slot = updateBrushes.get(brushKey);
            if (slot != null) {
                brushEntry.setValue(slot);
            }
        }
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

	@Override
	public WandTemplate getTemplate() {
		if (template == null || template.isEmpty()) return null;
		return controller.getWandTemplate(template);
	}

    @Override
    public boolean playEffects(String effects) {
        WandTemplate wandTemplate = getTemplate();
        if (wandTemplate != null && mage != null) {
            return wandTemplate.playEffects(mage, effects);
        }
        return false;
    }
	
	public WandAction getDropAction() {
		return dropAction;
	}
	
	public WandAction getRightClickAction() {
		return rightClickAction;
	}

	public WandAction getLeftClickAction() {
		return leftClickAction;
	}

	public WandAction getSwapAction() {
		return swapAction;
	}
	
	public boolean performAction(WandAction action) {
		WandMode mode = getMode();
		switch (action) {
			case CAST:
				cast();
				break;
			case TOGGLE:
				if (mode != WandMode.CHEST && mode != WandMode.INVENTORY) return false;
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
        com.elmakers.mine.bukkit.api.wand.WandUpgradePath path = getPath();
        com.elmakers.mine.bukkit.api.wand.WandUpgradePath nextPath = path != null ? path.getUpgrade(): null;
		if (nextPath == null || path.canEnchant(this)) {
			return true;
		}
		if (!path.checkUpgradeRequirements(this, quiet ? null : mage)) {
			return false;
		}
		path.upgrade(this, mage);
        return true;
    }

    /**
     * Gets a custom property from the wand.
     *
     * @param key The key of the property.
     * @return
     *     Null if no value with the provided key exists, otherwise the value.
     */
    public @Nullable String getCustomProperty(String key) {
        Preconditions.checkNotNull(key, "key");
        return customProperties.get(key);
    }

    /**
     * Sets a custom property on the wand.
     *
     * @param key The key of the property.
     * @param value
     *     The associated value. If this is null, the property will be removed.
     * @throws IllegalStateException In case the wand is not modifiable.
     */
    public void setCustomProperty(@Nonnull String key, String value) {
        Preconditions.checkNotNull(key, "key");
        Preconditions.checkState(isModifiable(), "Wand is not modifiable");

        if(value == null) {
            customProperties.remove(key);
        } else {
            customProperties.put(key, value);
        }

        saveItemState();
    }
    
    public int getEffectiveManaMax() {
		return effectiveManaMax;
	}
	
	public int getEffectiveManaRegeneration() {
		return effectiveManaRegeneration;
	}
}
