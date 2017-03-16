package com.elmakers.mine.bukkit.wand;

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

import com.elmakers.mine.bukkit.api.block.BrushMode;
import com.elmakers.mine.bukkit.api.event.AddSpellEvent;
import com.elmakers.mine.bukkit.api.event.SpellUpgradeEvent;
import com.elmakers.mine.bukkit.api.event.WandPreActivateEvent;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.Messages;
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
import com.elmakers.mine.bukkit.utility.NMSUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.elmakers.mine.bukkit.effect.SoundEffect;
import de.slikey.effectlib.util.ParticleEffect;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
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

public class Wand extends WandProperties implements CostReducer, com.elmakers.mine.bukkit.api.wand.Wand {
	public final static int INVENTORY_SIZE = 27;
	public final static int PLAYER_INVENTORY_SIZE = 36;
	public final static int INVENTORY_ORGANIZE_BUFFER = 4;
	public final static int HOTBAR_SIZE = 9;
	public final static int HOTBAR_INVENTORY_SIZE = HOTBAR_SIZE - 1;
	public final static float DEFAULT_SPELL_COLOR_MIX_WEIGHT = 0.0001f;
	public static Vector DEFAULT_CAST_OFFSET = new Vector(0.5, 0, 0);
	public static int MAX_LORE_LENGTH = 24;
	public static String DEFAULT_WAND_TEMPLATE = "default";
	private static int WAND_VERSION = 4;

    private final static String[] EMPTY_PARAMETERS = new String[0];

    private final static Random random = new Random();

    /**
     * The item as it appears in the inventory of the player.
     */
    protected @Nullable ItemStack item;

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
    private Map<String, Integer> spellInventory = new HashMap<>();
    private Set<String> spells = new LinkedHashSet<>();
    private Map<String, Integer> spellLevels = new HashMap<>();
    private Map<String, Integer> brushInventory = new HashMap<>();
	private Set<String> brushes = new LinkedHashSet<>();
	
	private String activeSpell = "";
	private String activeBrush = "";
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
    private boolean randomizeOnActivate = true;
    private boolean rename = false;
    private boolean renameDescription = false;
    private boolean quickCast = false;
    private boolean quickCastDisabled = false;
    private boolean manualQuickCastDisabled = false;
    private boolean isInOffhand = false;
	private boolean hasId = false;
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
    private float spMultiplier = 1;
	
	private float blockFOV = 0;
	private float blockChance = 0;
	private float blockReflectChance = 0;
    private int blockMageCooldown = 0;
    private int blockCooldown = 0;

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
	private ParticleEffect effectParticle = null;
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
	public static String UPGRADE_KEY = "wand_upgrade";
    public static String WAND_SELF_DESTRUCT_KEY = null;
    public static byte HIDE_FLAGS = 63;
	public static String brushSelectSpell = "";

    private Inventory storedInventory = null;
    private int storedSlot;

    public Wand(MagicController controller) {
		super(controller);

		hotbars = new ArrayList<>();
		setHotbarCount(1);
		inventories = new ArrayList<>();
	}

    /**
     * @deprecated Use {@link MagicController#getWand(ItemStack)}.
     */
    @Deprecated
    public Wand(MagicController controller, ItemStack itemStack) {
    	this(controller);
        Preconditions.checkNotNull(itemStack);

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

			int version = wandConfig.getInt("version", 0);
			if (version < WAND_VERSION) {
				migrate(version, wandConfig);
				needsSave = true;
			}
			randomizeOnActivate = !wandConfig.contains("icon");
			load(wandConfig);
		} else {
        	updateIcon();
		}
		loadProperties();

        // Migrate old upgrade items
        if ((isUpgrade || isUpgradeItem) && isWand) {
            needsSave = true;
            InventoryUtils.removeMeta(item, WAND_KEY);
        }
        if (needsSave) {
            saveState();
        }
        updateName();
        updateLore();
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
		setProperty("version", WAND_VERSION);
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
					} catch (NumberFormatException ex) {

					}
					if (wandData.equals(templateData)) {
						wandConfig.set(key, null);
					}
				}
			}
		}

		// Migration: remove level from spell inventory
		if (version <= 2) {
			Object spellInventoryRaw = wandConfig.get("spell_inventory");
			if (spellInventoryRaw != null) {
				Map<String, ? extends Object> spellInventory = null;
				Map<String, Integer> newSpellInventory = new HashMap<>();
				if (spellInventoryRaw instanceof Map) {
					org.bukkit.Bukkit.getLogger().info("MAP");
					spellInventory = (Map<String, ? extends Object>)spellInventoryRaw;
				} else if (spellInventoryRaw instanceof ConfigurationSection) {
					spellInventory = NMSUtils.getMap((ConfigurationSection)spellInventoryRaw);
				}
				if (spellInventory != null) {
					for (Map.Entry<String, ? extends Object> spellEntry : spellInventory.entrySet()) {
						Object slot = spellEntry.getValue();
						if (slot != null && slot instanceof Integer) {
							SpellKey spellKey = new SpellKey(spellEntry.getKey());
							newSpellInventory.put(spellKey.getBaseKey(), (Integer)slot);
						}
					}
					wandConfig.set("spell_inventory", newSpellInventory);
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

    	wandConfig.set("version", WAND_VERSION);
	}
	
	@Override
	public void load(ConfigurationSection configuration) {
		if (configuration != null) {
			setTemplate(configuration.getString("template"));
		}
		super.load(configuration);
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
	
	@Override
    public void unenchant() {
    	controller.cleanItem(item);
		clear();
	}
	
	public void setIcon(Material material, byte data) {
		setIcon(material == null ? null : new MaterialAndData(material, data));
        updateIcon();
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
			String newName = controller.getMessages().get("wand.upgrade_name");
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

    public void checkId() {
        if (id == null || id.length() == 0) {
            newId();
        }
    }

    @Override
    public String getId() {
        return id;
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
    	if (isCostFree()) {
			setProperty("mana", null);
		} else {
			this.mana = Math.max(0, mana);
			setProperty("mana", this.mana);
		}
    }

    @Override
    public void setManaMax(int manaMax) {
        this.manaMax = Math.max(0, manaMax);
		setProperty("mana_max", this.manaMax);
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
		setMana(mana - amount);
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
        return undroppable;
    }
	
	public boolean isUpgrade() {
		return isUpgrade;
	}
	
	public boolean usesMana() {
        if (isCostFree()) return false;
		return manaMax > 0 || (isHeroes && mage != null);
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
		setProperty("cooldown_reduction", cooldownReduction);
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
                worth += spell.getWorth();
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
		if (wandTemplate instanceof WandTemplateProperties) {
			setWandTemplate((WandTemplateProperties)wandTemplate);
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
		setProperty("owner", owner);
		setProperty("owner_id", ownerId);
		updateLore();
	}
	
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
	
	protected List<Inventory> getAllInventories() {
    	int hotbarCount = getHotbarCount();
		List<Inventory> allInventories = new ArrayList<>(inventories.size() + hotbarCount);
        if (hotbarCount > 0) {
        	allInventories.addAll(hotbars);
		}
		allInventories.addAll(inventories);
		return allInventories;
	}
	
	@Override
    public Set<String> getSpells() {
		return spells;
	}

	@Override
    public Set<String> getBrushes() {
		return brushes;
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
		List<Inventory> checkInventories = getAllInventories();
		boolean added = false;

		WandMode mode = getMode();
		for (Inventory inventory : checkInventories) {
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
				break;
			}
		}
		if (!added) {
			Inventory newInventory = CompatibilityUtils.createInventory(null, getInventorySize(), "Wand");
			newInventory.addItem(itemStack);
			inventories.add(newInventory);
		}
	}

    protected @Nonnull Inventory getInventoryByIndex(int inventoryIndex) {
        // Auto create
        while (inventoryIndex >= inventories.size()) {
            inventories.add(CompatibilityUtils.createInventory(null, getInventorySize(), "Wand"));
        }

        return inventories.get(inventoryIndex);
    }

	protected int getHotbarSize() {
    	if (getMode() != WandMode.INVENTORY) return 0;
		return hotbars.size() * HOTBAR_INVENTORY_SIZE;
	}

    protected @Nonnull Inventory getInventory(int slot) {
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

	protected void buildInventory() {
		// Force an update of the display inventory since chest mode is a different size
		displayInventory = null;

		for (Inventory hotbar : hotbars) {
			hotbar.clear();
		}
		inventories.clear();

		List<ItemStack> unsorted = new ArrayList<>();
		for (String spellKey : spells) {
			SpellTemplate spell = controller.getSpellTemplate(spellKey);
			ItemStack itemStack = createSpellItem(spell, "", controller, getActiveMage(), this, false);
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
					controller.getPlugin().getLogger().warning("Unable to create brush icon for key " + brushKey);
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
		if (openInventoryPage >= inventories.size() && openInventoryPage != 0) {
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
		for (String spellName : spellKeys)
		{
			String[] pieces = StringUtils.split(spellName, '@');
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
					if (slot != null) {
						spellInventory.put(spellKey.getBaseKey(), slot);
					}
					spells.add(spellKey.getKey());
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
    	clearBrushes();
		for (String materialName : brushKeys) {
			String[] pieces = StringUtils.split(materialName, '@');
			Integer slot = parseSlot(pieces);
			String materialKey = pieces[0].trim();
			if (slot != null) {
				brushInventory.put(materialKey, slot);
			}
			brushes.add(materialKey);
		}
	}

	protected void loadBrushInventory(Map<String, ? extends Object> inventory) {
    	if (inventory == null) return;
    	for (Map.Entry<String, ?> brushEntry : inventory.entrySet()) {
    		Object slot = brushEntry.getValue();
    		if (slot != null && slot instanceof Integer) {
    			brushInventory.put(brushEntry.getKey(), (Integer)slot);
			}
		}
	}

	protected void loadSpellInventory(Map<String, ? extends Object> inventory) {
		if (inventory == null) return;
		for (Map.Entry<String, ? extends Object> spellEntry : inventory.entrySet()) {
			Object slot = spellEntry.getValue();
			if (slot != null && slot instanceof Integer) {
				spellInventory.put(spellEntry.getKey(), (Integer)slot);
			}
		}
	}

    protected ItemStack createSpellIcon(SpellTemplate spell) {
        return createSpellItem(spell, "", controller, getActiveMage(), this, false);
    }

    public static ItemStack createSpellItem(String spellKey, MagicController controller, Wand wand, boolean isItem) {
        String[] split = spellKey.split(" ", 2);
        return createSpellItem(controller.getSpellTemplate(split[0]), split.length > 1 ? split[1] : "", controller, wand == null ? null : wand.getActiveMage(), wand, isItem);
    }

    public static ItemStack createSpellItem(String spellKey, MagicController controller, com.elmakers.mine.bukkit.api.magic.Mage mage, Wand wand, boolean isItem) {
        String[] split = spellKey.split(" ", 2);
        return createSpellItem(controller.getSpellTemplate(split[0]), split.length > 1 ? split[1] : "", controller, mage, wand, isItem);
    }

	public static ItemStack createSpellItem(SpellTemplate spell, String args, MagicController controller, com.elmakers.mine.bukkit.api.magic.Mage mage, Wand wand, boolean isItem) {
		if (spell == null) return null;
        String iconURL = spell.getIconURL();

        ItemStack itemStack = null;
        if (iconURL != null && (controller.isUrlIconsEnabled() || spell.getIcon() == null || !spell.getIcon().isValid() || spell.getIcon().getMaterial() == Material.AIR))
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

		if (wand != null && wand.getMode() == WandMode.SKILLS && !isItem) {
			InventoryUtils.setMeta(itemStack, "skill", "true");
		}
		InventoryUtils.makeUnbreakable(itemStack);
        InventoryUtils.hideFlags(itemStack, (byte)63);
		updateSpellItem(controller.getMessages(), itemStack, spell, args, mage, wand, wand == null ? null : wand.activeBrush, isItem);
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

	protected boolean findItem() {
		if (mage != null && item != null) {
			Player player = mage.getPlayer();
			if (player != null) {
				ItemStack itemInHand = player.getInventory().getItemInMainHand();
				if (itemInHand != null && itemInHand != item && itemInHand.equals(item)) {
					item = itemInHand;
					isInOffhand = false;
					return true;
				}
				itemInHand = player.getInventory().getItemInOffHand();
				if (itemInHand != null && itemInHand != item && itemInHand.equals(item)) {
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
            Thread.dumpStack();
		} else {
            InventoryUtils.saveTagsToNBT(getConfiguration(), wandNode);
        }
	}

    public static ConfigurationSection itemToConfig(ItemStack item, ConfigurationSection stateNode) {
        Object wandNode = InventoryUtils.getNode(item, WAND_KEY);

        if (wandNode == null) {
			wandNode = InventoryUtils.getNode(item, UPGRADE_KEY);
			if (wandNode == null) {
				return null;
			}
        }

        InventoryUtils.loadAllTagsFromNBT(stateNode, wandNode);

        return stateNode;
    }

    public static void configToItem(ConfigurationSection itemSection, ItemStack item) {
        ConfigurationSection stateNode = itemSection.getConfigurationSection("wand");
        Object wandNode = InventoryUtils.createNode(item, Wand.WAND_KEY);
        if (wandNode != null) {
            InventoryUtils.saveTagsToNBT(stateNode, wandNode);
        }
    }

    protected String getPotionEffectString() {
        return getPotionEffectString(potionEffects);
    }

    @Override
    public void save(ConfigurationSection node, boolean filtered) {
		ConfigurationUtils.addConfigurations(node, getEffectiveConfiguration());

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
    }

    public void updateBrushes() {
		if (brushes.isEmpty()) {
			setProperty("brushes", null);
		} else {
			setProperty("brushes", new ArrayList<>(brushes));
		}
	}

	public void updateSpells() {
		if (spells.isEmpty()) {
			setProperty("spells", null);
		} else {
			setProperty("spells", new ArrayList<>(spells));
		}
	}

	public void updateBrushInventory() {
    	if (brushInventory.isEmpty()) {
			setProperty("brush_inventory", null);
		} else {
			setProperty("brush_inventory", brushInventory);
		}
	}

	public void updateSpellInventory() {
		if (spellInventory.isEmpty()) {
			setProperty("spell_inventory", null);
		} else {
			setProperty("spell_inventory", spellInventory);
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

	public boolean loadProperties() {
		return loadProperties(getEffectiveConfiguration());
	}
	
	public boolean loadProperties(ConfigurationSection wandConfig) {
		locked = wandConfig.getBoolean("locked", locked);
		consumeReduction = (float)wandConfig.getDouble("consume_reduction");
		costReduction = (float)wandConfig.getDouble("cost_reduction");
		cooldownReduction = (float)wandConfig.getDouble("cooldown_reduction");
		power = (float)wandConfig.getDouble("power");
		damageReduction = (float)wandConfig.getDouble("protection");
		damageReductionPhysical = (float)wandConfig.getDouble("protection_physical");
		damageReductionProjectiles = (float)wandConfig.getDouble("protection_projectiles");
		damageReductionFalling = (float)wandConfig.getDouble("protection_falling");
		damageReductionFire = (float)wandConfig.getDouble("protection_fire");
		damageReductionExplosions = (float)wandConfig.getDouble("protection_explosions");

		hasId = wandConfig.getBoolean("unique", false);

		blockChance = (float)wandConfig.getDouble("block_chance");
		blockReflectChance = (float)wandConfig.getDouble("block_reflect_chance");
		blockFOV = (float)wandConfig.getDouble("block_fov");
        blockMageCooldown = wandConfig.getInt("block_mage_cooldown");
        blockCooldown = wandConfig.getInt("block_cooldown");

		manaRegeneration = wandConfig.getInt("mana_regeneration", wandConfig.getInt("xp_regeneration"));
		manaMax = wandConfig.getInt("mana_max", wandConfig.getInt("xp_max"));
		mana = wandConfig.getInt("mana", wandConfig.getInt("xp"));
        manaMaxBoost = (float)wandConfig.getDouble("mana_max_boost", wandConfig.getDouble("xp_max_boost"));
        manaRegenerationBoost = (float)wandConfig.getDouble("mana_regeneration_boost", wandConfig.getDouble("xp_regeneration_boost"));
        manaPerDamage = (float)wandConfig.getDouble("mana_per_damage");
		spMultiplier = (float)wandConfig.getDouble("sp_multiplier", 1);

        // Check for single-use wands
		uses = wandConfig.getInt("uses");
		hasUses = uses > 0;

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

		if (wandConfig.contains("effect_color")) {
			setEffectColor(wandConfig.getString("effect_color"));
		}

		id = wandConfig.getString("id");
		isUpgrade = wandConfig.getBoolean("upgrade");
		quietLevel = wandConfig.getInt("quiet");
		effectBubbles = wandConfig.getBoolean("effect_bubbles");
		keep = wandConfig.getBoolean("keep");
		passive = wandConfig.getBoolean("passive");
		indestructible = wandConfig.getBoolean("indestructible");
		superPowered = wandConfig.getBoolean("powered");
		superProtected = wandConfig.getBoolean("protected");
		glow = wandConfig.getBoolean("glow");
		undroppable = wandConfig.getBoolean("undroppable");
		isHeroes = wandConfig.getBoolean("heroes");
		bound = wandConfig.getBoolean("bound");
		soul = wandConfig.getBoolean("soul");
		forceUpgrade = wandConfig.getBoolean("force");
		autoOrganize = wandConfig.getBoolean("organize");
		autoAlphabetize = wandConfig.getBoolean("alphabetize");
		autoFill = wandConfig.getBoolean("fill");
		rename = wandConfig.getBoolean("rename");
		renameDescription = wandConfig.getBoolean("rename_description");
		enchantCount = wandConfig.getInt("enchant_count");
		maxEnchantCount = wandConfig.getInt("max_enchant_count");
		inventoryRows = wandConfig.getInt("inventory_rows", 5);
		if (inventoryRows <= 0) inventoryRows = 1;

		if (wandConfig.contains("effect_particle")) {
			effectParticle = ConfigurationUtils.toParticleEffect(wandConfig.getString("effect_particle"));
			effectParticleData = 0;
		} else {
			effectParticle = null;
		}
		if (wandConfig.contains("effect_sound")) {
			effectSound = ConfigurationUtils.toSoundEffect(wandConfig.getString("effect_sound"));
		} else {
			effectSound = null;
		}
		activeEffectsOnly = wandConfig.getBoolean("active_effects");
		effectParticleData = (float)wandConfig.getDouble("effect_particle_data");
		effectParticleCount = wandConfig.getInt("effect_particle_count");
		effectParticleRadius = wandConfig.getDouble("effect_particle_radius");
		effectParticleOffset = wandConfig.getDouble("effect_particle_offset");
		effectParticleInterval = wandConfig.getInt("effect_particle_interval");
		effectParticleMinVelocity = wandConfig.getDouble("effect_particle_min_velocity");
		effectSoundInterval =  wandConfig.getInt("effect_sound_interval");
		castLocation = ConfigurationUtils.getVector(wandConfig, "cast_location");

		castInterval = wandConfig.getInt("cast_interval");
		castMinVelocity = wandConfig.getDouble("cast_min_velocity");
		castVelocityDirection = ConfigurationUtils.getVector(wandConfig, "cast_velocity_direction");
		castSpell = wandConfig.getString("cast_spell");
		String castParameterString = wandConfig.getString("cast_parameters", null);
		if (castParameterString != null && !castParameterString.isEmpty()) {
			castParameters = new MemoryConfiguration();
			ConfigurationUtils.addParameters(StringUtils.split(castParameterString, ' '), castParameters);
		} else {
			castParameters = null;
		}

		boolean needsInventoryUpdate = false;
		WandMode newMode = parseWandMode(wandConfig.getString("mode"), controller.getDefaultWandMode());
		if (newMode != mode) {
			if (isInventoryOpen()) {
				closeInventory();
			}
			mode = newMode;
			needsInventoryUpdate = true;
		}

		brushMode = parseWandMode(wandConfig.getString("brush_mode"), controller.getDefaultBrushMode());

		// Backwards compatibility
		if (wandConfig.getBoolean("mode_drop", false)) {
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
		leftClickAction = parseWandAction(wandConfig.getString("left_click"), leftClickAction);
		rightClickAction = parseWandAction(wandConfig.getString("right_click"), rightClickAction);
		dropAction = parseWandAction(wandConfig.getString("drop"), dropAction);
		swapAction = parseWandAction(wandConfig.getString("swap"), swapAction);

		owner = wandConfig.getString("owner");
		ownerId = wandConfig.getString("owner_id");
		template = wandConfig.getString("template");
		upgradeTemplate = wandConfig.getString("upgrade_template");
		path = wandConfig.getString("path");

		activeSpell = wandConfig.getString("active_spell");
		activeBrush = wandConfig.getString("active_brush", wandConfig.getString("active_material"));

		if (wandConfig.contains("hotbar_count")) {
			int newCount = Math.max(1, wandConfig.getInt("hotbar_count"));
			if (newCount != hotbars.size() || newCount > hotbars.size()) {
				if (isInventoryOpen()) {
					closeInventory();
				}
				needsInventoryUpdate = true;
				setHotbarCount(newCount);
			}
		}

		if (wandConfig.contains("hotbar")) {
			int hotbar = wandConfig.getInt("hotbar");
			if (hotbar != currentHotbar) {
				needsInventoryUpdate = true;
				setCurrentHotbar(hotbar < 0 || hotbar >= hotbars.size() ? 0 : hotbar);
			}
		}

		if (wandConfig.contains("page")) {
			int page = wandConfig.getInt("page");
			if (page != openInventoryPage) {
				needsInventoryUpdate = true;
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
		wandName = wandConfig.getString("name", wandName);
		description = wandConfig.getString("description", description);

		WandTemplate wandTemplate = getTemplate();
		WandTemplate migrateTemplate = wandTemplate == null ? null : wandTemplate.getMigrateTemplate();
		if (migrateTemplate != null) {
			template = migrateTemplate.getKey();
			templateConfig = migrateTemplate.getConfiguration();
			wandTemplate = migrateTemplate;
		}

		// Add vanilla attributes
		InventoryUtils.applyAttributes(item, wandConfig.getConfigurationSection("attributes"), wandConfig.getString("attribute_slot"));

		// Add vanilla enchantments
		ConfigurationSection enchantments = wandConfig.getConfigurationSection("enchantments");
		InventoryUtils.applyEnchantments(item, enchantments);

		// Add enchantment glow
		if (enchantments == null || enchantments.getKeys(false).isEmpty()) {
			if (glow) {
				CompatibilityUtils.addGlow(item);
			} else {
				CompatibilityUtils.removeGlow(item);
			}
		}

		if (wandConfig.contains("icon_inactive")) {
			String iconKey = wandConfig.getString("icon_inactive");
			if (wandTemplate != null) {
				iconKey = wandTemplate.migrateIcon(iconKey);
			}
			if (iconKey != null) {
				inactiveIcon = new MaterialAndData(iconKey);
			}
		} else {
			inactiveIcon = null;
		}
		if (inactiveIcon != null && (inactiveIcon.getMaterial() == null || inactiveIcon.getMaterial() == Material.AIR))
		{
			inactiveIcon = null;
		}
		inactiveIconDelay = wandConfig.getInt("icon_inactive_delay");
		randomizeOnActivate = randomizeOnActivate && wandConfig.contains("randomize_icon");
		if (randomizeOnActivate) {
			setIcon(new MaterialAndData(wandConfig.getString("randomize_icon")));
		} else if (wandConfig.contains("icon")) {
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
			updateIcon();
		} else if (isUpgrade) {
			setIcon(new MaterialAndData(DefaultUpgradeMaterial));
		} else {
			setIcon(new MaterialAndData(DefaultWandMaterial));
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
		if (isHeroes) {
			hasSpellProgression = true;
		}

		brushInventory.clear();
		spellInventory.clear();
		Object wandSpells = wandConfig.get("spells");
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

		Object wandBrushes = wandConfig.get("brushes", wandConfig.get("materials"));
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

		Object brushInventoryRaw = wandConfig.get("brush_inventory");
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

		Object spellInventoryRaw = wandConfig.get("spell_inventory");
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
		buildInventory();

		castOverrides = null;
		if (wandConfig.contains("overrides")) {
			castOverrides = null;
			Object overridesGeneric = wandConfig.get("overrides");
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
		if (wandConfig.contains("potion_effects")) {
			addPotionEffects(potionEffects, wandConfig.getString("potion_effects", null));
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

        updateMaxMana(false);
        checkActiveMaterial();

        return needsInventoryUpdate;
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
    public void describe(CommandSender sender) {
		ChatColor wandColor = isModifiable() ? ChatColor.AQUA : ChatColor.RED;
		sender.sendMessage(wandColor + wandName);
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

		super.describe(sender, HIDDEN_PROPERTY_KEYS);

		WandTemplate template = getTemplate();
		if (template != null) {
			sender.sendMessage("" + ChatColor.BOLD + ChatColor.GREEN + "Template Configuration:");
			ConfigurationSection itemConfig = getConfiguration();
			Set<String> ownKeys = itemConfig.getKeys(false);
			ownKeys.addAll(HIDDEN_PROPERTY_KEYS);
			template.describe(sender, ownKeys);
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
        if (randomizeOnActivate) return name;

        Set<String> spells = getSpells();

        // Add active spell to description
        Messages messages = controller.getMessages();
        boolean showSpell = isModifiable() && hasSpellProgression();
        showSpell = !quickCast && (spells.size() > 1 || showSpell);
        if (spell != null && showSpell) {
            name = getSpellDisplayName(messages, spell, brush) + " (" + name + ChatColor.WHITE + ")";
        }

		if (remaining > 1) {
			String message = getMessage("uses_remaining_brief");
			name = name + ChatColor.DARK_RED + " (" + ChatColor.RED + message.replace("$count", ((Integer)remaining).toString()) + ChatColor.DARK_RED + ")";
		}
		return name;
	}
	
	private String getActiveWandName(SpellTemplate spell) {
		return getActiveWandName(spell, MaterialBrush.parseMaterialKey(activeBrush));
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
			CompatibilityUtils.setDisplayName(item, !isUpgrade ? getActiveWandName() : ChatColor.GOLD + getDisplayName());
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

	protected List<String> getLore() {
		return getLore(getSpells().size(), getBrushes().size());
	}
	
	protected void addPropertyLore(List<String> lore, boolean isSingleSpell)
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
        if (blockReflectChance > 0) {
			lore.add(ChatColor.AQUA + getLevelString(controller.getMessages(), "wand.reflect_chance", blockReflectChance));
		} else if (blockChance != 0) {
			lore.add(ChatColor.AQUA + getLevelString(controller.getMessages(), "wand.block_chance", blockChance));
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
            lore.add(ChatColor.AQUA + describePotionEffect(effect.getKey(), effect.getValue()));
        }
		if (consumeReduction > 0 && !isSingleSpell) lore.add(ChatColor.AQUA + getLevelString(controller.getMessages(), "wand.consume_reduction", consumeReduction));
		if (costReduction > 0 && !isSingleSpell) lore.add(ChatColor.AQUA + getLevelString(controller.getMessages(), "wand.cost_reduction", costReduction));
		if (cooldownReduction > 0 && !isSingleSpell) lore.add(ChatColor.AQUA + getLevelString(controller.getMessages(), "wand.cooldown_reduction", cooldownReduction));
		if (power > 0) lore.add(ChatColor.AQUA + getLevelString(controller.getMessages(), "wand.power", power));
        if (superProtected) {
            lore.add(ChatColor.DARK_AQUA + getMessage("super_protected"));
        } else {
            if (damageReduction > 0) lore.add(ChatColor.AQUA + getLevelString(controller.getMessages(), "wand.protection", damageReduction));
            if (damageReductionPhysical > 0) lore.add(ChatColor.AQUA + getLevelString(controller.getMessages(), "wand.protection_physical", damageReductionPhysical));
            if (damageReductionProjectiles > 0) lore.add(ChatColor.AQUA + getLevelString(controller.getMessages(), "wand.protection_projectile", damageReductionProjectiles));
            if (damageReductionFalling > 0) lore.add(ChatColor.AQUA + getLevelString(controller.getMessages(), "wand.protection_falling", damageReductionFalling));
            if (damageReductionFire > 0) lore.add(ChatColor.AQUA + getLevelString(controller.getMessages(), "wand.protection_fire", damageReductionFire));
            if (damageReductionExplosions > 0) lore.add(ChatColor.AQUA + getLevelString(controller.getMessages(), "wand.protection_explosions", damageReductionExplosions));
        }
        if (spMultiplier > 1) {
			lore.add(ChatColor.AQUA + getPercentageString(controller.getMessages(), "wand.sp_multiplier", spMultiplier - 1));
		}
	}
	
	public static String getLevelString(Messages messages, String templateName, float amount)
	{
		return messages.getLevelString(templateName, amount);
	}
	
	public static String getLevelString(Messages messages, String templateName, float amount, float max)
	{
		return messages.getLevelString(templateName, amount, max);
	}

    public static String getPercentageString(Messages messages, String templateName, float amount)
    {
        return messages.getPercentageString(templateName, amount);
    }
	
	protected List<String> getLore(int spellCount, int materialCount) 
	{
		List<String> lore = new ArrayList<>();

        if (description.length() > 0) {
			if (randomizeOnActivate) {
				String randomDescription = getMessage("randomized_lore");
				if (randomDescription.length() > 0) {
					InventoryUtils.wrapText(ChatColor.ITALIC + "" + ChatColor.DARK_GREEN, randomDescription, MAX_LORE_LENGTH, lore);
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

		if (!isUpgrade) {
			if (owner != null && owner.length() > 0) {
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

		SpellTemplate spell = controller.getSpellTemplate(activeSpell);
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
		if (!isSingleUse && remaining > 0) {
			if (isUpgrade) {
				String message = (remaining == 1) ? getMessage("upgrade_uses_singular") : getMessage("upgrade_uses");
				lore.add(ChatColor.RED + message.replace("$count", ((Integer)remaining).toString()));
			} else {
				String message = (remaining == 1) ? getMessage("uses_remaining_singular") : getMessage("uses_remaining_brief");
				lore.add(ChatColor.RED + message.replace("$count", ((Integer)remaining).toString()));
			}
		}
		addPropertyLore(lore, isSingleSpell);
		if (isUpgrade) {
			lore.add(ChatColor.YELLOW + getMessage("upgrade_item_description"));
		}
		return lore;
	}
	
	protected void updateLore() {
        CompatibilityUtils.setLore(item, getLore());
	}

    public void save() {
        saveState();
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
        return item != null && InventoryUtils.hasMeta(item, WAND_KEY);
	}

	public static boolean isWandOrUpgrade(ItemStack item) {
		return isWand(item) || isUpgrade(item);
	}

	public static boolean isSpecial(ItemStack item) {
		return isWand(item) || isUpgrade(item) || isSpell(item) || isBrush(item) || isSP(item);
	}

	public static boolean isBound(ItemStack item) {
		Object wandSection = InventoryUtils.getNode(item, WAND_KEY);
		if (wandSection == null) return false;
		
		String boundValue = InventoryUtils.getMetaString(wandSection, "bound");
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

    public static boolean isUpgrade(ItemStack item) {
    	return item != null && InventoryUtils.hasMeta(item, UPGRADE_KEY);
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

	protected static Object getWandOrUpgradeNode(ItemStack item) {
		if (InventoryUtils.isEmpty(item)) return null;
		Object wandNode = InventoryUtils.getNode(item, WAND_KEY);
		if (wandNode == null) {
			wandNode = InventoryUtils.getNode(item, UPGRADE_KEY);
		}
		return wandNode;
	}

    public static String getWandTemplate(ItemStack item) {
        Object wandNode = getWandOrUpgradeNode(item);
        if (wandNode == null) return null;
        return InventoryUtils.getMetaString(wandNode, "template");
    }

    public static String getWandId(ItemStack item) {
		if (InventoryUtils.isEmpty(item)) return null;
        Object wandNode = InventoryUtils.getNode(item, WAND_KEY);
        if (wandNode == null) return null;
        return InventoryUtils.getMetaString(wandNode, "id");
    }

	public static String getSpell(ItemStack item) {
		if (InventoryUtils.isEmpty(item)) return null;
        Object spellNode = InventoryUtils.getNode(item, "spell");
        if (spellNode == null) return null;
        return InventoryUtils.getMetaString(spellNode, "key");
	}

    public static String getSpellArgs(ItemStack item) {
		if (InventoryUtils.isEmpty(item)) return null;
        Object spellNode = InventoryUtils.getNode(item, "spell");
        if (spellNode == null) return null;
        return InventoryUtils.getMetaString(spellNode, "args");
    }

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

	public static void updateSpellName(Messages messages, ItemStack itemStack, SpellTemplate spell, Wand wand, String activeMaterial) {
		String displayName;
		if (wand != null && !wand.isQuickCast()) {
			displayName = wand.getActiveWandName(spell);
		} else {
			displayName = getSpellDisplayName(messages, spell, MaterialBrush.parseMaterialKey(activeMaterial));
		}
		CompatibilityUtils.setDisplayName(itemStack, displayName);
	}

	public static void updateSpellItem(Messages messages, ItemStack itemStack, SpellTemplate spell, String args, com.elmakers.mine.bukkit.api.magic.Mage mage, Wand wand, String activeMaterial, boolean isItem) {
        updateSpellName(messages, itemStack, spell, wand, activeMaterial);
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
			DeprecatedUtils.updateInventory(player);
		} else if (wandMode == WandMode.CHEST || wandMode == WandMode.SKILLS) {
			Inventory inventory = getDisplayInventory();
			inventory.clear();
			updateInventory(inventory);
		}
	}
	
	private boolean updateHotbar(PlayerInventory playerInventory) {
        if (getMode() != WandMode.INVENTORY) return false;
		Inventory hotbar = getHotbar();
        if (hotbar == null) return false;

        // Make sure the wand is still in the held slot
		int currentSlot = playerInventory.getHeldItemSlot();
        ItemStack currentItem = playerInventory.getItem(currentSlot);
        if (currentItem == null || !currentItem.getItemMeta().equals(item.getItemMeta())) {
        	controller.getLogger().warning("Trying to update hotbar but the wand has gone missing");
            return false;
        }

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
		return true;
    }
	
	private void updateInventory(Inventory targetInventory) {
		// Set inventory from current page, taking into account hotbar offset
		int currentOffset = getHotbarSize() > 0 ? HOTBAR_SIZE : 0;
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
        for (;currentOffset < targetInventory.getSize() && currentOffset < PLAYER_INVENTORY_SIZE; currentOffset++) {
			targetInventory.setItem(currentOffset,  null);
		}
	}
	
	protected static void addSpellLore(Messages messages, SpellTemplate spell, List<String> lore, com.elmakers.mine.bukkit.api.magic.Mage mage, Wand wand) {
        spell.addLore(messages, mage, wand, lore);
	}
	
	protected Inventory getOpenInventory() {
		while (openInventoryPage >= inventories.size()) {
			inventories.add(CompatibilityUtils.createInventory(null, getInventorySize(), "Wand"));
		}
		return inventories.get(openInventoryPage);
	}

	protected Inventory getDisplayInventory() {
		if (displayInventory == null || displayInventory.getSize() != getInventorySize()) {
			displayInventory = CompatibilityUtils.createInventory(null, getInventorySize(), "Wand");
		}

		return displayInventory;
	}

	public void saveChestInventory() {
		if (displayInventory == null) return;

		Inventory openInventory = getOpenInventory();
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
				ItemStack itemStack = createSpellItem(controller.getSpellTemplate(entry.getKey()), "", controller, getActiveMage(), this, false);
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
            wand.saveState();
            wand.updateName();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return wand;
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
				Set<String> spells = getSpells();
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
		hasInventory = inventorySize > 1 || (inventorySize == 1 && hasSpellProgression);
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

    public void cycleInventory() {
        cycleInventory(1);
    }

	public void openInventory() {
		if (mage == null) return;
		
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
				DeprecatedUtils.updateInventory(mage.getPlayer());
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

		saveState();
	}

    @Override
    public boolean fill(Player player) {
        return fill(player, 0);
    }

    @Override
	public boolean fill(Player player, int maxLevel) {
    	closeInventory();
        Collection<String> currentSpells = new ArrayList<>(getSpells());
        for (String spellKey : currentSpells) {
            SpellTemplate spell = controller.getSpellTemplate(spellKey);
            if (!spell.hasCastPermission(player))
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
			if (spell.hasCastPermission(player) && spell.hasIcon() && !spell.isHidden())
			{
				addSpell(key);
			}
		}
		this.mage = mage;

		if (autoFill) setProperty("fill", false);
		autoFill = false;
		updateSpells();
		saveState();
		
		return true;
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
					setIcon(ConfigurationUtils.toMaterialAndData(iconKey));
					updateIcon();
					playEffects("randomize");
                }
            }
        }
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
			String spellKey = getSpell(item);
			Set<String> spells = getSpells();
			if (!spells.contains(spellKey) && addSpell(spellKey)) {
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
			Location potionEffectLocation = player.getLocation();
			potionEffectLocation.setX(potionEffectLocation.getX() + random.nextDouble() - 0.5);
			potionEffectLocation.setY(potionEffectLocation.getY() + random.nextDouble() * player.getEyeHeight());
			potionEffectLocation.setZ(potionEffectLocation.getZ() + random.nextDouble() - 0.5);
			ParticleEffect.SPELL_MOB.display(potionEffectLocation, effectColor.getColor(), 24);
		}
		
		Location location = mage.getLocation();
		long now = System.currentTimeMillis();
        Vector mageLocation = location.toVector();
		boolean playEffects = !activeEffectsOnly || inventoryIsOpen || isInOffhand;
		if (playEffects && effectParticle != null && effectParticleInterval > 0 && effectParticleCount > 0) {
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
                    castParameters.set("passive", true);
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
		
		if (playEffects && effectSound != null && controller.soundsEnabled() && effectSoundInterval > 0) {
			if (lastSoundEffect == 0 || now > lastSoundEffect + effectSoundInterval) {
                lastSoundEffect = now;
                effectSound.play(controller.getPlugin(), mage.getPlayer());
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
        return (usesSP() && spMode.useXP()) || (usesMana() && manaMode.useXP());
    }

    public boolean usesXPNumber()
    {
        return (usesSP() && spMode.useXPNumber() && controller.isSPEnabled()) || (usesMana() && manaMode.useXP());
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
            if (usesSP() && spMode.useXPNumber())
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
		if (mage == null) return;

        // Play deactivate FX
		playPassiveEffects("deactivate");

        Mage mage = this.mage;
		
		if (isInventoryOpen()) {
			closeInventory();
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
	
	@Override
    public Spell getActiveSpell() {
		if (mage == null || activeSpell == null || activeSpell.length() == 0) return null;
		return mage.getSpell(activeSpell);
	}

    @Override
    public SpellTemplate getBaseSpell(String spellName) {
        return getBaseSpell(new SpellKey(spellName));
    }

    public SpellTemplate getBaseSpell(SpellKey key) {
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
        return activeBrush;
    }

    @Override
    public void damageDealt(double damage, Entity target) {
        if (effectiveManaMax == 0 && manaMax > 0) {
            effectiveManaMax = manaMax;
        }
        if (manaPerDamage > 0 && effectiveManaMax > 0 && mana < effectiveManaMax) {
            setMana(Math.min(effectiveManaMax, mana + (float)damage * manaPerDamage));
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
                    String[] key = StringUtils.split(entry.getKey(), '.');
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
	
	protected void use() {
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
					}
					DeprecatedUtils.updateInventory(player);
                }
				setProperty("uses", uses);
				saveState();
				updateName();
				updateLore();
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
					setMana(heroes.getMana(player));
					updated = true;
				}
			}
			else if (manaRegeneration > 0 && lastManaRegeneration > 0 && effectiveManaRegeneration > 0)
			{
				long delta = now - lastManaRegeneration;
				if (effectiveManaMax == 0 && manaMax > 0) {
					effectiveManaMax = manaMax;
				}
				setMana(Math.min(effectiveManaMax, mana + (float) effectiveManaRegeneration * (float)delta / 1000));
				updated = true;
			}
			lastManaRegeneration = now;
			setProperty("mana_timestamp", lastManaRegeneration);
		}
		
		return updated;
	}
	
	public void tick() {
		if (mage == null) return;
		
		Player player = mage.getPlayer();
		if (player == null) return;

		if (tickMana(player) && !isInOffhand) {
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
            if (isInOffhand) {
				Wand activeWand = mage.getActiveWand();
				if (activeWand != null && !activeWand.isPassive()) {
					effectiveBoost += activeWand.getManaMaxBoost();
					effectiveRegenBoost += activeWand.getManaRegenerationBoost();
				}
            } else {
				Wand offhandWand = mage.getOffhandWand();
				if (offhandWand != null && !offhandWand.isPassive()) {
					effectiveBoost += offhandWand.getManaMaxBoost();
					effectiveRegenBoost += offhandWand.getManaRegenerationBoost();
				}
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
	
	public void cycleSpells(int direction) {
		Set<String> spellsSet = getSpells();
		ArrayList<String> spells = new ArrayList<>(spellsSet);
		if (spells.size() == 0) return;
		if (activeSpell == null) {
			setActiveSpell(StringUtils.split(spells.get(0),'@')[0]);
			return;
		}
		
		int spellIndex = 0;
		for (int i = 0; i < spells.size(); i++) {
			if (StringUtils.split(spells.get(i), '@')[0].equals(activeSpell)) {
				spellIndex = i;
				break;
			}
		}
		
		spellIndex = (spellIndex + direction) % spells.size();
		setActiveSpell(StringUtils.split(spells.get(spellIndex), '@')[0]);
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

	public Mage getActiveMage() {
		return mage;
	}

	public void setActiveMage(com.elmakers.mine.bukkit.api.magic.Mage mage) {
    	if (mage instanceof Mage) {
			this.mage = (Mage)mage;
		}
	}

	@Override
	public Color getEffectColor() {
		return effectColor == null ? null : effectColor.getColor();
	}

    public ParticleEffect getEffectParticle() {
        return effectParticle;
    }

	@Override
	public String getEffectParticleName() {
		return effectParticle == null ? null : effectParticle.name();
	}

	public Inventory getHotbar() {
        if (this.hotbars.size() == 0) return null;

		if (currentHotbar < 0 || currentHotbar >= this.hotbars.size())
		{
			setCurrentHotbar(currentHotbar);
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
        return mode;
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
        this.path = path;
		setProperty("path", path);
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

	@Override
	@Deprecated
	public void activate(com.elmakers.mine.bukkit.api.magic.Mage mage) {
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

	public boolean activate(Mage mage) {
    	return activate(mage, false);
	}

	public boolean activateOffhand(Mage mage) {
		return activate(mage, true);
	}

    public boolean activate(Mage mage, boolean offhand) {
        if (mage == null) return false;
        Player player = mage.getPlayer();
        if (player == null) return false;

		if (!controller.hasWandPermission(player, this)) return false;
		InventoryView openInventory = player.getOpenInventory();
		InventoryType inventoryType = openInventory.getType();
		if (inventoryType == InventoryType.ENCHANTING ||
			inventoryType == InventoryType.ANVIL) return false;

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

		if (hasId) {
			this.checkId();
		} else {
			setProperty("id", null);
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

        this.mage = mage;
        this.isInOffhand = offhand;

		// Check for replacement template
		String replacementTemplate = getProperty("replace_on_activate", "");
		if (!replacementTemplate.isEmpty() && !replacementTemplate.equals(template)) {
			playEffects("replace");
			setTemplate(replacementTemplate);
			loadProperties();
		}

		// Since these wands can't be opened we will just show them as open when held
		// We have to delay this 1 tick so it happens after the Mage has accepted the Wand
		if (getMode() != WandMode.INVENTORY || offhand) {
			Plugin plugin = controller.getPlugin();
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				@Override
				public void run() {
					showActiveIcon(true);
					playPassiveEffects("open");
				}
			}, 1);
		}

        boolean forceUpdate = false;

        // Check for an empty wand and auto-fill
        if (!isUpgrade && (controller.fillWands() || autoFill)) {
            fill(mage.getPlayer(), controller.getMaxWandFillLevel());
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
        if (randomizeOnActivate) {
            randomize();
			randomizeOnActivate = false;
            forceUpdate = true;
        }

		updateMaxMana(false);
		tick();
		if (!isInOffhand) {
			updateMana();
		}

		checkActiveMaterial();
		saveState();
        updateActiveMaterial();
        updateName();
        updateLore();

        // Play activate FX
		playPassiveEffects("activate");

        lastSoundEffect = 0;
        lastParticleEffect = 0;
        lastSpellCast = 0;
        lastLocationTime = 0;
        lastLocation = null;
        if (forceUpdate) {
            DeprecatedUtils.updateInventory(player);
        }

        return true;
    }

    @Override
	public boolean organizeInventory() {
    	if (mage != null) {
    		return organizeInventory(mage);
		}
		return false;
	}

	private void setOpenInventoryPage(int page) {
    	this.openInventoryPage = page;
    	this.setProperty("page", page);
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
		if (mage != null) {
			saveState();
			loadProperties();
			updateInventory();
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
			saveState();
			loadProperties();
			updateInventory();
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
	public boolean configure(Map<String, Object> properties) {
		Map<Object, Object> convertedProperties = new HashMap<Object, Object>(properties);
		configure(ConfigurationUtils.toConfigurationSection(convertedProperties));
		return true;
	}

	@Override
	public boolean upgrade(Map<String, Object> properties) {
		Map<Object, Object> convertedProperties = new HashMap<Object, Object>(properties);
		return upgrade(ConfigurationUtils.toConfigurationSection(convertedProperties));
	}

	@Override
	protected void updated() {
		loadProperties();
		saveState();
		updateName();
		updateLore();
	}

	@Override
	public boolean isLocked() {
		return this.locked;
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

		return ownerId.equalsIgnoreCase(player.getUniqueId().toString());
	}
	
	@Override
    public boolean addSpell(String spellName) {
		if (!isModifiable()) return false;

        SpellKey spellKey = new SpellKey(spellName);
        if (hasSpell(spellKey)) {
            return false;
        }

		saveInventory();
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
        SpellTemplate currentSpell = getBaseSpell(spellKey);
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

        spellLevels.put(spellKey.getBaseKey(), level);
		spells.add(template.getKey());

		if (currentLevel != null) {
			if (activeSpell != null && !activeSpell.isEmpty()) {
				SpellKey currentKey = new SpellKey(activeSpell);
				if (currentKey.getBaseKey().equals(spellKey.getBaseKey())) {
					setActiveSpell(spellKey.getKey());
				}
			}
		}
		if (activeSpell == null || activeSpell.isEmpty()) {
			setActiveSpell(spellKey.getKey());
		}

		addToInventory(spellItem, inventorySlot);
		updateInventory();
		updateHasInventory();
		updateSpells();
        saveState();
		updateLore();

        if (mage != null)
        {
            if (currentSpell != null) {
                String levelDescription = template.getLevelDescription();
                if (levelDescription == null || levelDescription.isEmpty()) {
                    levelDescription = template.getName();
                }
                sendLevelMessage("spell_upgraded", currentSpell.getName(), levelDescription);

                mage.sendMessage(template.getUpgradeDescription().replace("$name", currentSpell.getName()));

                SpellUpgradeEvent upgradeEvent = new SpellUpgradeEvent(mage, this, currentSpell, template);
                Bukkit.getPluginManager().callEvent(upgradeEvent);
            } else {
                sendAddMessage("spell_added", template.getName());

                AddSpellEvent addEvent = new AddSpellEvent(mage, this, template);
                Bukkit.getPluginManager().callEvent(addEvent);
            }

            if (spells.size() != spellCount) {
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
        }
		
		return true;
	}

	@Override
	protected void sendAddMessage(String messageKey, String nameParam) {
		if (mage == null || nameParam == null || nameParam.isEmpty()) return;
		String message = getMessage(messageKey).replace("$name", nameParam).replace("$wand", getName());
		mage.sendMessage(message);
	}

    protected void sendLevelMessage(String messageKey, String nameParam, String level) {
        if (mage == null || nameParam == null || nameParam.isEmpty()) return;
        String message = getMessage(messageKey).replace("$name", nameParam).replace("$wand", getName()).replace("$level", level);
        mage.sendMessage(message);
    }

	@Override
	protected void sendMessage(String messageKey) {
		if (mage == null || messageKey == null || messageKey.isEmpty()) return;
		String message = getMessage(messageKey).replace("$wand", getName());
		mage.sendMessage(message);
	}

	@Override
    public String getMessage(String key, String defaultValue) {
        String message = controller.getMessages().get("wand." + key, defaultValue);
        if (template != null && !template.isEmpty()) {
            message = controller.getMessages().get("wands." + template + "." + key, message);
        }
        return message;
    }


	@Override
	protected void sendDebug(String debugMessage) {
		if (mage != null) {
			mage.sendDebugMessage(debugMessage);
		}
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

    @Override
    public boolean hasSpell(SpellKey spellKey) {
        Integer level = spellLevels.get(spellKey.getBaseKey());
        return (level != null && level >= spellKey.getLevel());
    }
	
	@Override
	public boolean addBrush(String materialKey) {
		if (!isModifiable()) return false;
		if (hasBrush(materialKey)) return false;

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
		updateBrushes();
        saveState();
		updateLore();

        if (mage != null)
        {
			Messages messages = controller.getMessages();
			String materialName = MaterialBrush.getMaterialName(messages, materialKey);
			if (materialName == null)
			{
				mage.getController().getLogger().warning("Invalid material: " + materialKey);
				materialName = materialKey;
			}

			sendAddMessage("brush_added", materialName);

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
		this.activeBrush = materialKey;
		setProperty("active_brush", this.activeBrush);
        saveState();
		updateName();
		updateActiveMaterial();
        updateHotbar();
	}

	@Override
	public void setActiveSpell(String activeSpell) {
    	if (activeSpell != null) {
			SpellKey spellKey = new SpellKey(activeSpell);
			activeSpell = spellKey.getBaseKey();
			if (!spellLevels.containsKey(activeSpell))
			{
				return;
			}
			spellKey = new SpellKey(spellKey.getBaseKey(), spellLevels.get(activeSpell));
			this.activeSpell = spellKey.getKey();
		} else {
    		this.activeSpell = null;
		}
		setProperty("active_spell", this.activeSpell);
        saveState();
		updateName();
	}

	@Override
	public boolean removeBrush(String materialKey) {
		if (!isModifiable() || materialKey == null) return false;
		
		saveInventory();
		if (materialKey.equals(activeBrush)) {
			activeBrush = null;
		}
        brushInventory.remove(materialKey);
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
					} else if (activeBrush == null) {
						activeBrush = materialKey;
					}
					if (found && activeBrush != null) {
						break;
					}
				}
			}
		}
		updateActiveMaterial();
		updateInventory();
		updateBrushes();
		updateBrushInventory();
        saveState();
		updateName();
		updateLore();
		return found;
	}
	
	@Override
	public boolean removeSpell(String spellName) {
		if (!isModifiable()) return false;
		
		saveInventory();
		if (spellName.equals(activeSpell)) {
			setActiveSpell(null);
		}
        SpellKey spellKey = new SpellKey(spellName);
		Integer level = spellLevels.get(spellKey.getBaseKey());
		if (level != null && level > 1) {
			spellKey = new SpellKey(spellKey.getBaseKey(), level);
			spellName = spellKey.getKey();
		}
		spells.remove(spellName);
        spellLevels.remove(spellKey.getBaseKey());
		spellInventory.remove(spellKey.getBaseKey());
		
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
						setActiveSpell(getSpell(itemStack));
					}
					if (found && activeSpell != null) {
						break;
					}
				}
			}
		}
        updateInventory();
		updateHasInventory();
		updateSpells();
		updateSpellInventory();
        saveState();
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

    public boolean restoreInventory() {
        if (storedInventory == null) {
            return false;
        }
        Player player = mage.getPlayer();
        if (player == null) {
            return false;
        }

		PlayerInventory inventory = player.getInventory();

		// Reset the wand item
		storedInventory.setItem(storedSlot, item);

		for (int i = 0; i < storedInventory.getSize(); i++) {
			inventory.setItem(i, storedInventory.getItem(i));
		}
        storedInventory = null;
        inventory.setHeldItemSlot(storedSlot);
		DeprecatedUtils.updateInventory(player);

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
        return new HashMap<>(spellInventory);
    }

    protected Map<String, Integer> getBrushInventory() {
        return new HashMap<>(brushInventory);
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

    protected void updateBrushInventory(Map<String, Integer> updateBrushes) {
        for (Map.Entry<String, Integer> brushEntry : brushInventory.entrySet()) {
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
		WandUpgradePath nextPath = path != null ? path.getUpgrade(): null;
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
	public boolean canProgress() {
		WandUpgradePath path = getPath();
		return (path != null && path.canEnchant(this));
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
	public boolean upgrade(boolean quiet) {
		WandUpgradePath path = getPath();
		if (path == null) return false;
		path.upgrade(this, quiet ? null : mage);
		return true;
	}
    
    public int getEffectiveManaMax() {
		return effectiveManaMax;
	}
	
	public int getEffectiveManaRegeneration() {
		return effectiveManaRegeneration;
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

	@Override
	public Location getLocation() {
		if (mage == null) {
			return null;
		}
		Location wandLocation = mage.getEyeLocation();
		wandLocation = mage.getOffsetLocation(wandLocation, isInOffhand, castLocation == null ? DEFAULT_CAST_OFFSET : castLocation);
		return wandLocation;
	}

	@Override
	public Mage getMage() {
    	return mage;
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

	public float getSPMultiplier() {
    	return spMultiplier;
	}

	public boolean usesSP() {
    	return hasSpellProgression && controller.isSPEnabled() && controller.isSPEarnEnabled() && spMultiplier > 0;
	}
}
