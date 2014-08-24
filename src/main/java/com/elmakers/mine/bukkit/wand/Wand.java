package com.elmakers.mine.bukkit.wand;

import java.util.*;
import java.util.regex.Matcher;

import com.elmakers.mine.bukkit.api.block.BrushMode;
import com.elmakers.mine.bukkit.api.spell.CastingCost;
import com.elmakers.mine.bukkit.api.spell.CostReducer;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.block.MaterialBrush;
import com.elmakers.mine.bukkit.effect.builtin.EffectRing;
import com.elmakers.mine.bukkit.magic.Mage;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.spell.BrushSpell;
import com.elmakers.mine.bukkit.spell.UndoableSpell;
import com.elmakers.mine.bukkit.utility.*;
import de.slikey.effectlib.util.ParticleEffect;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Wand implements CostReducer, com.elmakers.mine.bukkit.api.wand.Wand {
	public final static int INVENTORY_SIZE = 27;
	public final static int HOTBAR_SIZE = 9;
	public final static float DEFAULT_SPELL_COLOR_MIX_WEIGHT = 0.0001f;
	public final static float DEFAULT_WAND_COLOR_MIX_WEIGHT = 1.0f;

    public final static String[] EMPTY_PARAMETERS = new String[0];
	
	// REMEMBER! Each of these MUST have a corresponding class in .traders, else traders will
	// destroy the corresponding data.
	public final static String[] PROPERTY_KEYS = {
		"active_spell", "active_material",
        "path",
		"xp", "xp_regeneration", "xp_max",
		"bound", "uses", "upgrade", "indestructible", "undroppable",
		"cost_reduction", "cooldown_reduction", "effect_bubbles", "effect_color", 
		"effect_particle", "effect_particle_count", "effect_particle_data", "effect_particle_interval", 
		"effect_sound", "effect_sound_interval", "effect_sound_pitch", "effect_sound_volume",
		"haste", 
		"health_regeneration", "hunger_regeneration", 
		"icon", "mode", "keep", "locked", "quiet", "force", "randomize", "rename",
		"power", "overrides",
		"protection", "protection_physical", "protection_projectiles", 
		"protection_falling", "protection_fire", "protection_explosions",
		"materials", "spells"
	};
	public final static String[] HIDDEN_PROPERTY_KEYS = {
		"id", "owner", "owner_id", "name", "description", "template",
		"organize", "fill", "stored", "upgrade_icon"
	};
	public final static String[] ALL_PROPERTY_KEYS = (String[])ArrayUtils.addAll(PROPERTY_KEYS, HIDDEN_PROPERTY_KEYS);
	
	protected ItemStack item;
	protected MagicController controller;
	protected Mage mage;
	
	// Cached state
	private String id = "";
	private Inventory hotbar;
	private List<Inventory> inventories;
    private Set<String> spells = new HashSet<String>();
    private Set<String> brushes = new HashSet<String>();
	
	private String activeSpell = "";
	private String activeMaterial = "";
	protected String wandName = "";
	protected String description = "";
	private String owner = "";
    private String ownerId = "";
	private String template = "";
    private String path = "";
	private boolean bound = false;
	private boolean indestructible = false;
    private boolean undroppable = false;
	private boolean keep = false;
	private boolean autoOrganize = false;
	private boolean autoFill = false;
	private boolean isUpgrade = false;
    private boolean randomize = false;
    private boolean rename = false;
	
	private MaterialAndData icon = null;
    private MaterialAndData upgradeIcon = null;
	
	private float costReduction = 0;
	private float cooldownReduction = 0;
	private float damageReduction = 0;
	private float damageReductionPhysical = 0;
	private float damageReductionProjectiles = 0;
	private float damageReductionFalling = 0;
	private float damageReductionFire = 0;
	private float damageReductionExplosions = 0;
	private float power = 0;
	private boolean hasInventory = false;
	private boolean locked = false;
    private boolean forceUpgrade = false;
	private int uses = 0;
	private int xp = 0;
	
	private int xpRegeneration = 0;
	private int xpMax = 0;
	private float healthRegeneration = 0;
	private PotionEffect healthRegenEffect = null;
	private float hungerRegeneration = 0;
	private PotionEffect hungerRegenEffect = null;
	
	private ColorHD effectColor = null;
	private float effectColorSpellMixWeight = DEFAULT_SPELL_COLOR_MIX_WEIGHT;
	private float effectColorMixWeight = DEFAULT_WAND_COLOR_MIX_WEIGHT;	
	private ParticleEffect effectParticle = null;
	private float effectParticleData = 0;
	private int effectParticleCount = 0;
	private int effectParticleInterval = 0;
	private int effectParticleCounter = 0;
	private boolean effectBubbles = false;
	private EffectRing effectPlayer = null;
	
	private Sound effectSound = null;
	private int effectSoundInterval = 0;
	private int effectSoundCounter = 0;
	private float effectSoundVolume = 0;
	private float effectSoundPitch = 0;
	
	private float speedIncrease = 0;
	private PotionEffect hasteEffect = null;

    private int quietLevel = 0;
    private Map<String, String> castOverrides = null;
	
	private int storedXpLevel = 0;
	private float storedXpProgress = 0;
	
	// Inventory functionality
	
	private WandMode mode = null;
	private int openInventoryPage = 0;
	private boolean inventoryIsOpen = false;
	private Inventory displayInventory = null;
	
	// Kinda of a hacky initialization optimization :\
	private boolean suspendSave = false;

	// Wand configurations
	protected static Map<String, ConfigurationSection> wandTemplates = new HashMap<String, ConfigurationSection>();
	
	public static boolean displayManaAsBar = true;
    public static boolean displayManaAsDurability = true;
    public static boolean displayManaAsGlow = true;
	public static boolean retainLevelDisplay = true;
	public static Material DefaultUpgradeMaterial = Material.NETHER_STAR;
	public static Material DefaultWandMaterial = Material.BLAZE_ROD;
	public static Material EnchantableWandMaterial = null;
	public static boolean EnableGlow = true;

    private Inventory storedInventory = null;
    private Integer playerInventorySlot = null;

    private static final ItemStack[] itemTemplate = new ItemStack[0];

	public Wand(MagicController controller, ItemStack itemStack) {
		this.controller = controller;
		hotbar = CompatibilityUtils.createInventory(null, 9, "Wand");
		this.icon = new MaterialAndData(itemStack.getType(), (byte)itemStack.getDurability());
		inventories = new ArrayList<Inventory>();
        item = itemStack;
		indestructible = controller.getIndestructibleWands();
		loadState();
        updateName();
        updateLore();
	}
	
	public Wand(MagicController controller) {
		this(controller, DefaultWandMaterial, (short)0);
	}

    public Wand(MagicController controller, ConfigurationSection config) {
        this(controller, DefaultWandMaterial, (short)0);
        loadProperties(config);
        updateName();
        updateLore();
        saveState();
    }
	
	protected Wand(MagicController controller, String templateName) throws UnknownWandException {
		this(controller);
		suspendSave = true;
		String wandName = Messages.get("wand.default_name");
		String wandDescription = "";

		// Check for default wand
		if ((templateName == null || templateName.length() == 0) && wandTemplates.containsKey("default"))
		{
			templateName = "default";
		}
		
		// See if there is a template with this key
		if (templateName != null && templateName.length() > 0) {
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

			if (!wandTemplates.containsKey(templateName)) {
				throw new UnknownWandException(templateName);
			}
			ConfigurationSection wandConfig = wandTemplates.get(templateName);

			// Default to template names, override with localizations
            wandName = wandConfig.getString("name", wandName);
			wandName = Messages.get("wands." + templateName + ".name", wandName);
            wandDescription = wandConfig.getString("description", wandDescription);
			wandDescription = Messages.get("wands." + templateName + ".description", wandDescription);

			// Load all properties
			loadProperties(wandConfig);

            // Enchant, if an enchanting level was provided
            if (level > 0) {
                // Account for randomized locked wands
                boolean wasLocked = locked;
                locked = false;
                randomize(level, false, null);
                locked = wasLocked;
            }
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
		saveState();
	}
	
	public Wand(MagicController controller, Material icon, short iconData) {
		// This will make the Bukkit ItemStack into a real ItemStack with NBT data.
		this(controller, InventoryUtils.makeReal(new ItemStack(icon, 1, iconData)));
		wandName = Messages.get("wand.default_name");
        saveState();
		updateName();
	}
	
	public void unenchant() {
		item = new ItemStack(item.getType(), 1, (short)item.getDurability());
	}
	
	public void setIcon(Material material, byte data) {
		setIcon(material == null ? null : new MaterialAndData(material, data));
	}
	
	public void setIcon(MaterialAndData materialData) {
		icon = materialData;
		if (icon != null) {
			item.setType(icon.getMaterial());
			item.setDurability(icon.getData());
		}
	}
	
	public void makeUpgrade() {
        if (!isUpgrade) {
            isUpgrade = true;
            String oldName = wandName;
            wandName = Messages.get("wand.upgrade_name");
            wandName = wandName.replace("$name", oldName);
            description = Messages.get("wand.upgrade_default_description");
            if (template != null && template.length() > 0) {
                description = Messages.get("wands." + template + ".upgrade_description", description);
            }
            setIcon(DefaultUpgradeMaterial, (byte) 0);
            saveState();
            updateName(true);
            updateLore();
        }
	}
	
	protected void activateBrush(String materialKey) {
		setActiveBrush(materialKey);
		if (materialKey != null) {
			com.elmakers.mine.bukkit.api.block.MaterialBrush brush = mage.getBrush();
			if (brush != null) {
				brush.activate(mage.getLocation(), materialKey);

                if (mage != null) {
                    BrushMode mode = brush.getMode();
                    if (mode == BrushMode.CLONE) {
                        mage.sendMessage(Messages.get("wand.clone_material_activated"));
                    } else if (mode == BrushMode.REPLICATE) {
                        mage.sendMessage(Messages.get("wand.replicate_material_activated"));
                    }
                    if (brush.isEraseModifierActive()) {
                        mage.sendMessage(Messages.get("wand.erase_modifier_activated"));
                    }
                }

			}
		}
	}
	
	public void activateBrush(ItemStack itemStack) {
		if (!isBrush(itemStack)) return;
		activateBrush(getBrush(itemStack));
	}

    public String getLostId() { return id; }
    public void clearLostId() {
        if (id != null) {
            id = null;
            saveState();
        }
    }
	
	public int getXpRegeneration() {
		return xpRegeneration;
	}

	public int getXpMax() {
		return xpMax;
	}
	
	public int getExperience() {
		return xp;
	}
	
	public void removeExperience(int amount) {
		xp = Math.max(0,  xp - amount);
		updateMana();
	}

	public float getHealthRegeneration() {
		return healthRegeneration;
	}

	public float getHungerRegeneration() {
		return hungerRegeneration;
	}
	
	public boolean isModifiable() {
		return !locked;
	}
	
	public boolean isIndestructible() {
		return indestructible;
	}

    public boolean isUndroppable() {
        return undroppable;
    }
	
	public boolean isUpgrade() {
		return isUpgrade;
	}
	
	public boolean usesMana() {
		return xpMax > 0 && xpRegeneration > 0 && !isCostFree();
	}

	public float getCooldownReduction() {
		return controller.getCooldownReduction() + cooldownReduction * controller.getMaxCooldownReduction();
	}

	public float getCostReduction() {
		if (isCostFree()) return 1.0f;
		return controller.getCostReduction() + costReduction * controller.getMaxCostReduction();
	}
	
	public void setCooldownReduction(float reduction) {
		cooldownReduction = reduction;
	}
	
	public boolean getHasInventory() {
		return hasInventory;
	}

	public float getPower() {
		return power;
	}

    public float getHaste() {
        return speedIncrease;
    }
	
	public boolean isSuperProtected() {
		return damageReduction > 1;
	}
	
	public boolean isSuperPowered() {
		return power > 1;
	}
	
	public boolean isCostFree() {
		return costReduction > 1;
	}
	
	public boolean isCooldownFree() {
		return cooldownReduction > 1;
	}

	public float getDamageReduction() {
		return damageReduction * controller.getMaxDamageReduction();
	}

	public float getDamageReductionPhysical() {
		return damageReductionPhysical * controller.getMaxDamageReductionPhysical();
	}
	
	public float getDamageReductionProjectiles() {
		return damageReductionProjectiles * controller.getMaxDamageReductionProjectiles();
	}

	public float getDamageReductionFalling() {
		return damageReductionFalling * controller.getMaxDamageReductionFalling();
	}

	public float getDamageReductionFire() {
		return damageReductionFire * controller.getMaxDamageReductionFire();
	}

	public float getDamageReductionExplosions() {
		return damageReductionExplosions * controller.getMaxDamageReductionExplosions();
	}

	public int getUses() {
		return uses;
	}
	
	public String getName() {
		return wandName;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getOwner() {
		return owner;
	}

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

	public void setName(String name) {
		wandName = ChatColor.stripColor(name);
		updateName();
	}
	
	public void setTemplate(String templateName) {
		this.template = templateName;
	}
	
	public String getTemplate() {
		return this.template;
	}

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
	
	public void setDescription(String description) {
		this.description = description;
		updateLore();
	}
	
	public void tryToOwn(Player player) {
        if (ownerId == null || ownerId.length() == 0) {
            // Backwards-compatibility, don't overwrite unless the
            // name matches
            if (owner != null && !owner.equals(player.getName())) {
                return;
            }
            takeOwnership(player);
        }
	}

    protected void takeOwnership(Player player) {
        takeOwnership(player, controller != null && controller.bindWands(), controller != null && controller.keepWands());
    }

     public void takeOwnership(Player player, boolean setBound, boolean setKeep) {
		owner = player.getName();
        ownerId = player.getUniqueId().toString();
		if (setBound) {
			bound = true;
		}
		if (setKeep) {
			keep = true;
		}
		updateLore();
	}
	
	public ItemStack getItem() {
		return item;
	}
	
	protected List<Inventory> getAllInventories() {
		List<Inventory> allInventories = new ArrayList<Inventory>(inventories.size() + 1);
		allInventories.add(hotbar);
		allInventories.addAll(inventories);
		return allInventories;
	}
	
	public Set<String> getSpells() {
		return spells;
	}

    protected String getSpellString() {
		Set<String> spellNames = new TreeSet<String>();
		List<Inventory> allInventories = getAllInventories();
        int index = 0;
		for (Inventory inventory : allInventories) {
			ItemStack[] items = inventory.getContents();
			for (int i = 0; i < items.length; i++) {
				if (items[i] != null && isSpell(items[i])) {
                    String spellName = getSpell(items[i]) + "@" + index;
                    spellNames.add(spellName);
				}
                index++;
			}
		}
		return StringUtils.join(spellNames, ",");
	}

	public Set<String> getBrushes() {
		return brushes;
	}

    protected String getMaterialString() {
		Set<String> materialNames = new TreeSet<String>();
		List<Inventory> allInventories = new ArrayList<Inventory>(inventories.size() + 1);
		allInventories.add(hotbar);
		allInventories.addAll(inventories);
        int index = 0;
		for (Inventory inventory : allInventories) {
			ItemStack[] items = inventory.getContents();
			for (int i = 0; i < items.length; i++) {
                if (items[i] != null && isBrush(items[i])) {
                    String materialKey = getBrush(items[i]);
                    if (materialKey != null) {
                        materialKey += "@" + index;
                        materialNames.add(materialKey);
                    }
                }
                index++;
			}
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

		// Set the wand item
		Integer selectedItem = null;
        if (getMode() == WandMode.INVENTORY && mage != null && mage.getPlayer() != null && playerInventorySlot != null) {
            if (item == null || !isWand(item))
            {
                controller.getLogger().warning("Wand item isn't a wand");
                return;
            }
            selectedItem = playerInventorySlot;

            // Toss the item back into the wand inventory, it'll find a home somewhere.
            // We hope this doesn't recurse too badly! :\
            ItemStack existingHotbar = hotbar.getItem(selectedItem);

            // Set the wand item to occupy this spot
            hotbar.setItem(selectedItem, item);

            if (existingHotbar != null && existingHotbar.getType() != Material.AIR && !isWand(existingHotbar)) {
                addToInventory(existingHotbar);
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
		
		// Restore empty wand slot
		if (selectedItem != null) {
			hotbar.setItem(selectedItem, null);
		}
	}
	
	protected Inventory getDisplayInventory() {
		if (displayInventory == null) {
			displayInventory = CompatibilityUtils.createInventory(null, INVENTORY_SIZE + HOTBAR_SIZE, "Wand");
		}
		
		return displayInventory;
	}
	
	protected Inventory getInventoryByIndex(int inventoryIndex) {
		while (inventoryIndex >= inventories.size()) {
			inventories.add(CompatibilityUtils.createInventory(null, INVENTORY_SIZE, "Wand"));
		}
		return inventories.get(inventoryIndex);
	}
	
	protected Inventory getInventory(Integer slot) {
		Inventory inventory = hotbar;
		if (slot >= HOTBAR_SIZE) {
			int inventoryIndex = (slot - HOTBAR_SIZE) / INVENTORY_SIZE;
			inventory = getInventoryByIndex(inventoryIndex);
		}
		
		return inventory;
	}
	
	protected int getInventorySlot(Integer slot) {
		if (slot < HOTBAR_SIZE) {
			return slot;
		}
		
		return ((slot - HOTBAR_SIZE) % INVENTORY_SIZE);
	}
	
	protected void addToInventory(ItemStack itemStack, Integer slot) {
		if (slot == null) {
			addToInventory(itemStack);
			return;
		}
        Player player = mage != null ? mage.getPlayer() : null;
        if (player != null && slot == player.getInventory().getHeldItemSlot()) {
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
		hotbar.clear();
		inventories.clear();
        spells.clear();
        brushes.clear();

		// Support YML-List-As-String format and |-delimited format
		spellString = spellString.replaceAll("[\\]\\[]", "");
		String[] spellNames = StringUtils.split(spellString, "|,");
		for (String spellName : spellNames) {		
			String[] pieces = spellName.split("@");
			Integer slot = parseSlot(pieces);
			String spellKey = pieces[0].trim();
            spells.add(spellKey);
			ItemStack itemStack = createSpellIcon(spellKey);
			if (itemStack == null) {
				// controller.getPlugin().getLogger().warning("Unable to create spell icon for key " + spellKey);
				itemStack = new ItemStack(item.getType(), 1);
                CompatibilityUtils.setDisplayName(itemStack, spellKey);
                CompatibilityUtils.setMeta(itemStack, "spell", spellKey);
            }
			else if (activeSpell == null || activeSpell.length() == 0) activeSpell = spellKey;
			addToInventory(itemStack, slot);
		}
		materialString = materialString.replaceAll("[\\]\\[]", "");
		String[] materialNames = StringUtils.split(materialString, "|,");
		for (String materialName : materialNames) {
			String[] pieces = materialName.split("@");
			Integer slot = parseSlot(pieces);
			String materialKey = pieces[0].trim();
            brushes.add(materialKey);
			ItemStack itemStack = createBrushIcon(materialKey);
			if (itemStack == null) {
				controller.getPlugin().getLogger().warning("Unable to create material icon for key " + materialKey);
				continue;
			}
			if (activeMaterial == null || activeMaterial.length() == 0) activeMaterial = materialKey;
			addToInventory(itemStack, slot);
		}
		hasInventory = spellNames.length + materialNames.length > 1;
	}

    protected ItemStack createSpellIcon(SpellTemplate spell) {
        return createSpellItem(spell, controller, this, false);
    }

    public static ItemStack createSpellItem(String spellKey, MagicController controller, Wand wand, boolean isItem) {
        return createSpellItem(controller.getSpellTemplate(spellKey), controller, wand, isItem);
    }

    @SuppressWarnings("deprecation")
	public static ItemStack createSpellItem(SpellTemplate spell, MagicController controller, Wand wand, boolean isItem) {
		if (spell == null) return null;
		com.elmakers.mine.bukkit.api.block.MaterialAndData icon = spell.getIcon();
		if (icon == null) {
			controller.getPlugin().getLogger().warning("Unable to create spell icon for " + spell.getName() + ", missing material");	
			return null;
		}
		ItemStack itemStack = null;
		ItemStack originalItemStack = null;
		try {
			originalItemStack = new ItemStack(icon.getMaterial(), 1, (short)0, (byte)icon.getData());
			itemStack = InventoryUtils.makeReal(originalItemStack);
		} catch (Exception ex) {
			itemStack = null;
		}
		if (itemStack == null) {
			controller.getPlugin().getLogger().warning("Unable to create spell icon for " + spell.getKey() + " with material " + icon.getMaterial().name());
			return originalItemStack;
		}
		updateSpellItem(itemStack, spell, wand, wand == null ? null : wand.activeMaterial, isItem);
		return itemStack;
	}

	protected ItemStack createSpellIcon(String spellKey) {
		return createSpellItem(spellKey, controller, this, false);
	}
	
	private String getActiveWandName(String materialKey) {
        SpellTemplate spell = null;
        if (activeSpell != null && activeSpell.length() > 0) {
            spell = controller.getSpellTemplate(activeSpell);
        }

		return getActiveWandName(spell, materialKey);
	}
	
	protected ItemStack createBrushIcon(String materialKey) {
		return createBrushItem(materialKey, controller, this, false);
	}
	
	@SuppressWarnings("deprecation")
	public static ItemStack createBrushItem(String materialKey, MagicController controller, Wand wand, boolean isItem) {
		MaterialAndData brushData = MaterialBrush.parseMaterialKey(materialKey, false);
		if (brushData == null) return null;
		
		Material material = brushData.getMaterial();
		if (material == null || material == Material.AIR) {
			return null;
		}
		
		byte dataId = brushData.getData();
		ItemStack originalItemStack = new ItemStack(material, 1, (short)0, (byte)dataId);
		ItemStack itemStack = InventoryUtils.makeReal(originalItemStack);
		if (itemStack == null) {
			controller.getPlugin().getLogger().warning("Unable to create material icon for " + material.name() + ": " + materialKey);	
			return null;
		}
		List<String> lore = new ArrayList<String>();
		if (material != null) {
			lore.add(ChatColor.GRAY + Messages.get("wand.building_material_info").replace("$material", MaterialBrush.getMaterialName(materialKey)));
			if (material == MaterialBrush.EraseMaterial) {
				lore.add(Messages.get("wand.erase_material_description"));
			} else if (material == MaterialBrush.CopyMaterial) {
				lore.add(Messages.get("wand.copy_material_description"));
			} else if (material == MaterialBrush.CloneMaterial) {
				lore.add(Messages.get("wand.clone_material_description"));
			} else if (material == MaterialBrush.ReplicateMaterial) {
				lore.add(Messages.get("wand.replicate_material_description"));
			} else if (material == MaterialBrush.MapMaterial) {
				lore.add(Messages.get("wand.map_material_description"));
			} else if (material == MaterialBrush.SchematicMaterial) {
				lore.add(Messages.get("wand.schematic_material_description").replace("$schematic", brushData.getCustomName()));
			} else {
				lore.add(ChatColor.LIGHT_PURPLE + Messages.get("wand.building_material_description"));
			}
		}
		
		if (isItem) {
			lore.add(ChatColor.YELLOW + Messages.get("wand.brush_item_description"));
		}
        CompatibilityUtils.setLore(itemStack, lore);
		updateBrushItem(itemStack, materialKey, wand);
		return itemStack;
	}

	protected void saveState() {
		if (suspendSave) return;
        if (checkWandItem()) {
            updateName();
            updateLore();
            if (displayManaAsDurability && xpMax > 0 && xpRegeneration > 0) {
                updateDurability();
            }
        }

        ConfigurationSection stateNode = new MemoryConfiguration();
        saveProperties(stateNode);

        // Save legacy data as well until migration is settled
		Object wandNode = InventoryUtils.createNode(item, "wand");
		if (wandNode == null) {
			controller.getLogger().warning("Failed to save wand state for wand to : " + item + " in slot " + playerInventorySlot);
		} else {
            InventoryUtils.saveTagsToNBT(stateNode, wandNode, ALL_PROPERTY_KEYS);
        }
	}
	
	protected void loadState() {
		if (item == null) return;

        Object wandNode = InventoryUtils.getNode(item, "wand");
        if (wandNode == null) {
            return;
        }

        ConfigurationSection stateNode = new MemoryConfiguration();
        InventoryUtils.loadTagsFromNBT(stateNode, wandNode, ALL_PROPERTY_KEYS);

        loadProperties(stateNode);
	}

	public void saveProperties(ConfigurationSection node) {
        node.set("id", id);
        node.set("materials", getMaterialString());

		node.set("spells", getSpellString());
		
		node.set("active_spell", activeSpell);
		node.set("active_material", activeMaterial);
		node.set("name", wandName);
		node.set("description", description);
		node.set("owner", owner);
        node.set("owner_id", ownerId);
	
		node.set("cost_reduction", costReduction);
		node.set("cooldown_reduction", cooldownReduction);
		node.set("power", power);
		node.set("protection", damageReduction);
		node.set("protection_physical", damageReductionPhysical);
		node.set("protection_projectiles", damageReductionProjectiles);
		node.set("protection_falling", damageReductionFalling);
		node.set("protection_fire", damageReductionFire);
		node.set("protection_explosions", damageReductionExplosions);
		node.set("haste", speedIncrease);
		node.set("xp", xp);
		node.set("xp_regeneration", xpRegeneration);
		node.set("xp_max", xpMax);
		node.set("health_regeneration", healthRegeneration);
		node.set("hunger_regeneration", hungerRegeneration);
		node.set("uses", uses);
		node.set("locked", locked);
		node.set("effect_color", effectColor == null ? "none" : effectColor.toString());
		node.set("effect_bubbles", effectBubbles);
		node.set("effect_particle_data", Float.toString(effectParticleData));
		node.set("effect_particle_count", effectParticleCount);
		node.set("effect_particle_interval", effectParticleInterval);
		node.set("effect_sound_interval", effectSoundInterval);
		node.set("effect_sound_volume", Float.toString(effectSoundVolume));
		node.set("effect_sound_pitch", Float.toString(effectSoundPitch));
		node.set("quiet", quietLevel);
		node.set("keep", keep);
        node.set("randomize", randomize);
        node.set("rename", rename);
		node.set("bound", bound);
        node.set("force", forceUpgrade);
		node.set("indestructible", indestructible);
        node.set("undroppable", undroppable);
		node.set("fill", autoFill);
		node.set("upgrade", isUpgrade);
		node.set("organize", autoOrganize);
        if (castOverrides != null && castOverrides.size() > 0) {
            Collection<String> parameters = new ArrayList<String>();
            for (Map.Entry entry : castOverrides.entrySet()) {
                parameters.add(entry.getKey() + " " + entry.getValue());
            }
            node.set("overrides", StringUtils.join(parameters, ","));
        } else {
            node.set("overrides", null);
        }
		if (effectSound != null) {
			node.set("effect_sound", effectSound.name());
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
		if (icon != null) {
			String iconKey = MaterialBrush.getMaterialKey(icon);
			if (iconKey != null && iconKey.length() > 0) {
				node.set("icon", iconKey);
			} else {
				node.set("icon", null);
			}
		} else {
			node.set("icon", null);
		}
        if (upgradeIcon != null) {
            String iconKey = MaterialBrush.getMaterialKey(upgradeIcon);
            if (iconKey != null && iconKey.length() > 0) {
                node.set("upgrade_icon", iconKey);
            } else {
                node.set("upgrade_icon", null);
            }
        } else {
            node.set("upgrade_icon", null);
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

        if (storedInventory != null && controller.isInventoryBackupEnabled()) {
            YamlConfiguration inventoryConfig = new YamlConfiguration();
            ItemStack[] contents = storedInventory.getContents();
            inventoryConfig.set("contents", contents);
            String serialized = inventoryConfig.saveToString();
            node.set("stored", serialized);
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
	
	public void loadProperties(ConfigurationSection wandConfig, boolean safe) {
		locked = (boolean)wandConfig.getBoolean("locked", locked);
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
		int _xpRegeneration = wandConfig.getInt("xp_regeneration", xpRegeneration);
		xpRegeneration = safe ? Math.max(_xpRegeneration, xpRegeneration) : _xpRegeneration;
		int _xpMax = wandConfig.getInt("xp_max", xpMax);
		xpMax = safe ? Math.max(_xpMax, xpMax) : _xpMax;
		int _xp = wandConfig.getInt("xp", xp);
		xp = safe ? Math.max(_xp, xp) : _xp;
		float _healthRegeneration = (float)wandConfig.getDouble("health_regeneration", healthRegeneration);
		healthRegeneration = safe ? Math.max(_healthRegeneration, healthRegeneration) : _healthRegeneration;
		float _hungerRegeneration = (float)wandConfig.getDouble("hunger_regeneration", hungerRegeneration);
		hungerRegeneration = safe ? Math.max(_hungerRegeneration, hungerRegeneration) : _hungerRegeneration;
		int _uses = wandConfig.getInt("uses", uses);
		uses = safe ? Math.max(_uses, uses) : _uses;
		float _speedIncrease = (float)wandConfig.getDouble("haste", speedIncrease);
		speedIncrease = safe ? Math.max(_speedIncrease, speedIncrease) : _speedIncrease;
		
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
            indestructible = wandConfig.getBoolean("indestructible", indestructible);
            undroppable = wandConfig.getBoolean("undroppable", undroppable);
			bound = wandConfig.getBoolean("bound", bound);
            forceUpgrade = wandConfig.getBoolean("force", forceUpgrade);
            autoOrganize = wandConfig.getBoolean("organize", autoOrganize);
			autoFill = wandConfig.getBoolean("fill", autoFill);
            randomize = wandConfig.getBoolean("randomize", randomize);
            rename = wandConfig.getBoolean("rename", rename);

            if (wandConfig.contains("effect_particle")) {
				parseParticleEffect(wandConfig.getString("effect_particle"));
				effectParticleData = 0;
			}
			if (wandConfig.contains("effect_sound")) {
				parseSoundEffect(wandConfig.getString("effect_sound"));
			}
			effectParticleData = (float)wandConfig.getDouble("effect_particle_data", effectParticleData);
			effectParticleCount = wandConfig.getInt("effect_particle_count", effectParticleCount);
			effectParticleInterval = wandConfig.getInt("effect_particle_interval", effectParticleInterval);
			effectSoundInterval =  wandConfig.getInt("effect_sound_interval", effectSoundInterval);
			effectSoundVolume = (float)wandConfig.getDouble("effect_sound_volume", effectSoundVolume);
			effectSoundPitch = (float)wandConfig.getDouble("effect_sound_pitch", effectSoundPitch);
			
			setMode(parseWandMode(wandConfig.getString("mode"), mode));

			owner = wandConfig.getString("owner", owner);
            ownerId = wandConfig.getString("owner_id", ownerId);
			wandName = wandConfig.getString("name", wandName);			
			description = wandConfig.getString("description", description);
			template = wandConfig.getString("template", template);
            path = wandConfig.getString("path", path);
			
			activeSpell = wandConfig.getString("active_spell", activeSpell);
			activeMaterial = wandConfig.getString("active_material", activeMaterial);

			String wandMaterials = wandConfig.getString("materials", "");
			String wandSpells = wandConfig.getString("spells", "");

			if (wandMaterials.length() > 0 || wandSpells.length() > 0) {
				wandMaterials = wandMaterials.length() == 0 ? getMaterialString() : wandMaterials;
				wandSpells = wandSpells.length() == 0 ? getSpellString() : wandSpells;
				parseInventoryStrings(wandSpells, wandMaterials);
			}

            if (wandConfig.contains("randomize_icon")) {
                setIcon(ConfigurationUtils.toMaterialAndData(wandConfig.getString("randomize_icon")));
                randomize = true;
            } else if (!randomize && wandConfig.contains("icon")) {
                String iconKey = wandConfig.getString("icon");
                if (iconKey.contains(",")) {
                    Random r = new Random();
                    String[] keys = StringUtils.split(iconKey, ',');
                    iconKey = keys[r.nextInt(keys.length)];
                }
				setIcon(ConfigurationUtils.toMaterialAndData(iconKey));
			}

            if (wandConfig.contains("upgrade_icon")) {
                upgradeIcon = ConfigurationUtils.toMaterialAndData(wandConfig.getString("upgrade_icon"));
            }

            if (wandConfig.contains("overrides")) {
                castOverrides = null;
                String overrides = wandConfig.getString("overrides", null);
                if (overrides != null && !overrides.isEmpty()) {
                    castOverrides = new HashMap<String, String>();
                    String[] pairs = StringUtils.split(overrides, ',');
                    for (String pair : pairs) {
                        String[] keyValue = StringUtils.split(pair, " ");
                        if (keyValue.length > 0) {
                            String value = keyValue.length > 1 ? keyValue[1] : "";
                            castOverrides.put(keyValue[0], value);
                        }
                    }
                }
            }

            if (wandConfig.contains("stored")) {
                try {
                    YamlConfiguration inventoryConfig = new YamlConfiguration();
                    String serialized = wandConfig.getString("stored");
                    if (serialized.isEmpty()) {
                        storedInventory = null;
                    } else {
                        inventoryConfig.loadFromString(serialized);
                        Collection<ItemStack> collection = (Collection<ItemStack>) inventoryConfig.get("contents");
                        ItemStack[] contents = collection.toArray(itemTemplate);
                        storedInventory = CompatibilityUtils.createInventory(null, contents.length, "Stored Inventory");
                        storedInventory.setContents(contents);
                    }
                } catch (Exception ex) {
                    controller.getLogger().warning("Error loading stored wand inventory");
                    ex.printStackTrace();
                }
            }
		}
		
		// Some cleanup and sanity checks. In theory we don't need to store any non-zero value (as it is with the traders)
		// so try to keep defaults as 0/0.0/false.
		if (effectSound == null) {
			effectSoundInterval = 0;
			effectSoundVolume = 0;
			effectSoundPitch = 0;
		} else {
			effectSoundInterval = (effectSoundInterval == 0) ? 5 : effectSoundInterval;
			effectSoundVolume = (effectSoundVolume < 0.01f) ? 0.8f : effectSoundVolume;
			effectSoundPitch = (effectSoundPitch < 0.01f) ? 1.1f : effectSoundPitch;
		}
		
		if (effectParticle == null) {
			effectParticleInterval = 0;
		}
		
		if (xpRegeneration <= 0 || xpMax <= 0 || costReduction >= 1) {
			xpMax = 0;
			xpRegeneration = 0;
			xp = 0;
		}

		checkActiveMaterial();
	}

    protected void parseSoundEffect(String effectSoundName) {
        if (effectSoundName.length() > 0) {
            String soundName = effectSoundName.toUpperCase();
            try {
                effectSound = Sound.valueOf(soundName);
            } catch (Exception ex) {
                effectSound = null;
            }
        } else {
            effectSound = null;
        }
    }

    protected void parseParticleEffect(String effectParticleName) {
        if (effectParticleName.length() > 0) {
            String particleName = effectParticleName.toUpperCase();
            try {
                effectParticle = ParticleEffect.valueOf(particleName);
            } catch (Exception ex) {
                effectParticle = null;
            }
        } else {
            effectParticle = null;
        }
    }

	public void describe(CommandSender sender) {
		Object wandNode = InventoryUtils.getNode(item, "wand");
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
        if (storedInventory != null) {
            sender.sendMessage(ChatColor.RED + "Has a stored inventory");
        }
		
		for (String key : PROPERTY_KEYS) {
			String value = InventoryUtils.getMeta(wandNode, key);
			if (value != null && value.length() > 0) {
				sender.sendMessage(key + ": " + value);
			}
		}
	}

    private static String getBrushDisplayName(String materialKey) {
        String materialName = MaterialBrush.getMaterialName(materialKey);
        if (materialName == null) {
            materialName = "none";
        }
        return ChatColor.GRAY + materialName;
    }

    private static String getSpellDisplayName(SpellTemplate spell, String materialKey) {
		String name = "";
		if (spell != null) {
			if (materialKey != null && (spell instanceof BrushSpell) && !((BrushSpell)spell).hasBrushOverride()) {
				name = ChatColor.GOLD + spell.getName() + " " + getBrushDisplayName(materialKey) + ChatColor.WHITE;
			} else {
				name = ChatColor.GOLD + spell.getName() + ChatColor.WHITE;
			}
		}
		
		return name;
	}

	private String getActiveWandName(SpellTemplate spell, String materialKey) {
		// Build wand name
        int remaining = getRemainingUses();
		ChatColor wandColor = remaining > 0 ? ChatColor.DARK_RED : isModifiable()
                ? (bound ? ChatColor.DARK_AQUA : ChatColor.AQUA) :
                  (path != null && path.length() > 0 ? ChatColor.LIGHT_PURPLE : ChatColor.GOLD);
		String name = wandColor + getDisplayName();
        if (randomize) return name;

        Set<String> spells = getSpells();

        // Add active spell to description
        boolean showSpell = isModifiable() && hasPath();
        if (spell != null && (spells.size() > 1 || showSpell)) {
            name = getSpellDisplayName(spell, materialKey) + " (" + name + ChatColor.WHITE + ")";
        }

		if (remaining > 0) {
			String message = (remaining == 1) ? Messages.get("wand.uses_remaining_singular") : Messages.get("wand.uses_remaining_brief");
			name = name + " (" + ChatColor.RED + message.replace("$count", ((Integer)remaining).toString()) + ")";
		}
		return name;
	}
	
	private String getActiveWandName(SpellTemplate spell) {
		return getActiveWandName(spell, activeMaterial);
	}
	
	private String getActiveWandName() {
		SpellTemplate spell = null;
		if (activeSpell != null && activeSpell.length() > 0) {
			spell = controller.getSpellTemplate(activeSpell);
		}
		return getActiveWandName(spell);
	}

    protected String getDisplayName() {
        return randomize ? Messages.get("wand.randomized_name") : wandName;
    }

	public void updateName(boolean isActive) {
        CompatibilityUtils.setDisplayName(item, isActive && !isUpgrade ? getActiveWandName() : ChatColor.GOLD + getDisplayName());

		// Reset Enchantment glow
		if (EnableGlow) {
            CompatibilityUtils.addGlow(item);
		}

        // Make indestructible
        if (indestructible && !displayManaAsDurability) {
            CompatibilityUtils.makeUnbreakable(item);
        } else {
            CompatibilityUtils.removeUnbreakable(item);
        }
        CompatibilityUtils.hideFlags(item);
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
		Collection<String> lore = new ArrayList<String>();
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
			lore.add(ChatColor.LIGHT_PURPLE + "" + ChatColor.ITALIC + getLevelString("wand.mana_amount", xpMax, controller.getMaxMana()));
			lore.add(ChatColor.RESET + "" + ChatColor.LIGHT_PURPLE + getLevelString("wand.mana_regeneration", xpRegeneration, controller.getMaxManaRegeneration()));
		}
		if (costReduction > 0) lore.add(ChatColor.AQUA + getLevelString("wand.cost_reduction", costReduction));
		if (cooldownReduction > 0) lore.add(ChatColor.AQUA + getLevelString("wand.cooldown_reduction", cooldownReduction));
		if (power > 0) lore.add(ChatColor.AQUA + getLevelString("wand.power", power));
		if (speedIncrease > 0) lore.add(ChatColor.AQUA + getLevelString("wand.haste", speedIncrease));
		if (damageReduction > 0) lore.add(ChatColor.AQUA + getLevelString("wand.protection", damageReduction));
		if (damageReduction < 1) {
			if (damageReductionPhysical > 0) lore.add(ChatColor.AQUA + getLevelString("wand.protection_physical", damageReductionPhysical));
			if (damageReductionProjectiles > 0) lore.add(ChatColor.AQUA + getLevelString("wand.protection_projectile", damageReductionProjectiles));
			if (damageReductionFalling > 0) lore.add(ChatColor.AQUA + getLevelString("wand.protection_fall", damageReductionFalling));
			if (damageReductionFire > 0) lore.add(ChatColor.AQUA + getLevelString("wand.protection_fire", damageReductionFire));
			if (damageReductionExplosions > 0) lore.add(ChatColor.AQUA + getLevelString("wand.protection_blast", damageReductionExplosions));
		}
		if (healthRegeneration > 0) lore.add(ChatColor.AQUA + getLevelString("wand.health_regeneration", healthRegeneration));
		if (hungerRegeneration > 0) lore.add(ChatColor.AQUA + getLevelString("wand.hunger_regeneration", hungerRegeneration));
	}
	
	public static String getLevelString(String templateName, float amount)
	{
		return getLevelString(templateName, amount, 1);
	}
	
	public static String getLevelString(String templateName, float amount, float max)
	{
		String templateString = Messages.get(templateName);
		if (templateString.contains("$roman")) {
			templateString = templateString.replace("$roman", getRomanString(amount));
		}
		return templateString.replace("$amount", Integer.toString((int)amount));
	}

	private static String getRomanString(float amount) {
		String roman = "";

		if (amount > 1) {
			roman = Messages.get("wand.enchantment_level_max");
		} else if (amount > 0.8) {
			roman = Messages.get("wand.enchantment_level_5");
		} else if (amount > 0.6) {
			roman = Messages.get("wand.enchantment_level_4");
		} else if (amount > 0.4) {
			roman = Messages.get("wand.enchantment_level_3");
		} else if (amount > 0.2) {
			roman = Messages.get("wand.enchantment_level_2");
		} else {
			 roman = Messages.get("wand.enchantment_level_1");
		}
		return roman;
	}
	
	protected List<String> getLore(int spellCount, int materialCount) 
	{
		List<String> lore = new ArrayList<String>();

        if (description.length() > 0) {
            if (description.contains("$")) {
                String randomDescription = Messages.get("wand.randomized_lore");
                if (randomDescription.length() > 0) {
                    lore.add(ChatColor.ITALIC + "" + ChatColor.DARK_GREEN + randomDescription);
                }
            } else {
                lore.add(ChatColor.ITALIC + "" + ChatColor.GREEN + description);
            }
        }

        if (randomize) {
            return lore;
        }

		SpellTemplate spell = controller.getSpellTemplate(activeSpell);

        // This is here specifically for a wand that only has
        // one spell now, but may get more later. Since you
        // can't open the inventory in this state, you can not
        // otherwise see the spell lore.
		if (spell != null && spellCount == 1 && !hasInventory && !isUpgrade && hasPath()) {
            lore.add(getSpellDisplayName(spell, null));
            addSpellLore(spell, lore, this);
		}
        if (materialCount == 1 && activeMaterial != null && activeMaterial.length() > 0)
        {
            lore.add(getBrushDisplayName(activeMaterial));
        }
        if (!isUpgrade) {
            if (owner.length() > 0) {
                if (bound) {
                    String ownerDescription = Messages.get("wand.bound_description", "$name").replace("$name", owner);
                    lore.add(ChatColor.ITALIC + "" + ChatColor.DARK_AQUA + ownerDescription);
                } else {
                    String ownerDescription = Messages.get("wand.owner_description", "$name").replace("$name", owner);
                    lore.add(ChatColor.ITALIC + "" + ChatColor.DARK_GREEN + ownerDescription);
                }
            }
        }

        if (spellCount > 0) {
            if (isUpgrade) {
                lore.add(Messages.get("wand.upgrade_spell_count").replace("$count", ((Integer)spellCount).toString()));
            } else if (spellCount > 1) {
                lore.add(Messages.get("wand.spell_count").replace("$count", ((Integer)spellCount).toString()));
            }
        }
        if (materialCount > 0) {
            if (isUpgrade) {
                lore.add(Messages.get("wand.upgrade_material_count").replace("$count", ((Integer)materialCount).toString()));
            } else if (materialCount > 1) {
                lore.add(Messages.get("wand.material_count").replace("$count", ((Integer)materialCount).toString()));
            }
        }

		int remaining = getRemainingUses();
		if (remaining > 0) {
			if (isUpgrade) {
				String message = (remaining == 1) ? Messages.get("wand.upgrade_uses_singular") : Messages.get("wand.upgrade_uses");
				lore.add(ChatColor.RED + message.replace("$count", ((Integer)remaining).toString()));
			} else {
				String message = (remaining == 1) ? Messages.get("wand.uses_remaining_singular") : Messages.get("wand.uses_remaining_brief");
				lore.add(ChatColor.RED + message.replace("$count", ((Integer)remaining).toString()));
			}
		}
		addPropertyLore(lore);
		if (isUpgrade) {
			lore.add(ChatColor.YELLOW + Messages.get("wand.upgrade_item_description"));
		}
		return lore;
	}
	
	protected void updateLore() {
        CompatibilityUtils.setLore(item, getLore());

		if (EnableGlow) {
			CompatibilityUtils.addGlow(item);
		}
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
		ItemStack activeItem =  player.getInventory().getItemInHand();
		return isWand(activeItem);
	}
	
	public static Wand getActiveWand(MagicController spells, Player player) {
		ItemStack activeItem =  player.getInventory().getItemInHand();
		if (isWand(activeItem)) {
			return new Wand(spells, activeItem);
		}
		
		return null;
	}

	public static boolean isWand(ItemStack item) {
        return item != null && InventoryUtils.hasMeta(item, "wand") && !isUpgrade(item);
	}

    public static boolean isUpgrade(ItemStack item) {
        if (item == null) return false;
        Object wandNode = InventoryUtils.getNode(item, "wand");

        if (wandNode == null) return false;
        String upgradeData = InventoryUtils.getMeta(wandNode, "upgrade");

        return upgradeData != null && upgradeData.equals("true");
    }

	public static boolean isSpell(ItemStack item) {
        return item != null && InventoryUtils.hasMeta(item, "spell");
	}

	public static boolean isBrush(ItemStack item) {
        return item != null && InventoryUtils.hasMeta(item, "brush");
	}
	
	public static String getSpell(ItemStack item) {
        Object spellNode = InventoryUtils.getNode(item, "spell");
        if (spellNode == null) return null;
        return InventoryUtils.getMeta(spellNode, "key");
	}

    public static String getBrush(ItemStack item) {
        Object brushNode = InventoryUtils.getNode(item, "brush");
        if (brushNode == null) return null;
        return InventoryUtils.getMeta(brushNode, "key");
    }

	protected void updateInventoryName(ItemStack item, boolean activeName) {
		if (isSpell(item)) {
			Spell spell = mage.getSpell(getSpell(item));
			if (spell != null) {
				updateSpellItem(item, spell, activeName ? this : null, activeMaterial, false);
			}
		} else if (isBrush(item)) {
			updateBrushItem(item, getBrush(item), activeName ? this : null);
		}
	}
	
	public static void updateSpellItem(ItemStack itemStack, SpellTemplate spell, Wand wand, String activeMaterial, boolean isItem) {
		String displayName;
		if (wand != null) {
			displayName = wand.getActiveWandName(spell);
		} else {
			displayName = getSpellDisplayName(spell, activeMaterial);
		}
        CompatibilityUtils.setDisplayName(itemStack, displayName);
		List<String> lore = new ArrayList<String>();
		addSpellLore(spell, lore, wand);
		if (isItem) {
			lore.add(ChatColor.YELLOW + Messages.get("wand.spell_item_description"));
		}
        CompatibilityUtils.setLore(itemStack, lore);
        Object spellNode = CompatibilityUtils.createNode(itemStack, "spell");
		CompatibilityUtils.setMeta(spellNode, "key", spell.getKey());
        CompatibilityUtils.addGlow(itemStack);
	}
	
	public static void updateBrushItem(ItemStack itemStack, String materialKey, Wand wand) {
		String displayName = null;
		if (wand != null) {
			displayName = wand.getActiveWandName(materialKey);
		} else {
			displayName = MaterialBrush.getMaterialName(materialKey);
		}
        CompatibilityUtils.setDisplayName(itemStack, displayName);
        Object brushNode = CompatibilityUtils.createNode(itemStack, "brush");
        CompatibilityUtils.setMeta(brushNode, "key", materialKey);
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
            player.updateInventory();
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
            inventory.clear();
			updateHotbar(inventory);
			updateInventory(inventory, HOTBAR_SIZE, false);
			updateName();
			player.updateInventory();
		} else if (wandMode == WandMode.CHEST) {
			Inventory inventory = getDisplayInventory();
			inventory.clear();
			updateInventory(inventory, 0, true);
			player.updateInventory();
		}
	}
	
	private void updateHotbar(PlayerInventory playerInventory) {
		// Check for an item already in the player's held slot, which
		// we are about to replace with the wand.
		int currentSlot = playerInventory.getHeldItemSlot();
		ItemStack existingHotbar = hotbar.getItem(currentSlot);

        if (existingHotbar != null && existingHotbar.getType() != Material.AIR && !isWand(existingHotbar)) {
			// Toss the item back into the wand inventory, it'll find a home somewhere.
            hotbar.setItem(currentSlot, item);
			addToInventory(existingHotbar);
            hotbar.setItem(currentSlot, null);
		}

        // Set hotbar items from remaining list
		for (int hotbarSlot = 0; hotbarSlot < HOTBAR_SIZE; hotbarSlot++) {
			if (hotbarSlot != currentSlot) {
				ItemStack hotbarItem = hotbar.getItem(hotbarSlot);
				updateInventoryName(hotbarItem, true);
				playerInventory.setItem(hotbarSlot, hotbarItem);
			}
		}

        // Put the wand in the player's active slot.
        playerInventory.setItem(currentSlot, item);
        item = playerInventory.getItem(currentSlot);
    }
	
	private void updateInventory(Inventory targetInventory, int startOffset, boolean addHotbar) {
		// Set inventory from current page
		int currentOffset = startOffset;
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
		
		if (addHotbar) {
			for (int i = 0; i < HOTBAR_SIZE; i++) {
				ItemStack inventoryItem = hotbar.getItem(i);
				updateInventoryName(inventoryItem, false);
				targetInventory.setItem(currentOffset++, inventoryItem);
			}
		}
	}
	
	protected static void addSpellLore(SpellTemplate spell, List<String> lore, CostReducer reducer) {
		String description = spell.getDescription();
		String usage = spell.getUsage();
		if (description != null && description.length() > 0) {
			lore.add(description);
		}
		if (usage != null && usage.length() > 0) {
			lore.add(usage);
		}
		Collection<CastingCost> costs = spell.getCosts();
		if (costs != null) {
			for (CastingCost cost : costs) {
				if (cost.hasCosts(reducer)) {
					lore.add(ChatColor.YELLOW + Messages.get("wand.costs_description").replace("$description", cost.getFullDescription(reducer)));
				}
			}
		}
		Collection<CastingCost> activeCosts = spell.getActiveCosts();
		if (activeCosts != null) {
			for (CastingCost cost : activeCosts) {
				if (cost.hasCosts(reducer)) {
					lore.add(ChatColor.YELLOW + Messages.get("wand.active_costs_description").replace("$description", cost.getFullDescription(reducer)));
				}
			}
		}
		
		long duration = spell.getDuration();
		if (duration > 0) {
			long seconds = duration / 1000;
			if (seconds > 60 * 60 ) {
				long hours = seconds / (60 * 60);
				lore.add(Messages.get("duration.lasts_hours").replace("$hours", ((Long)hours).toString()));					
			} else if (seconds > 60) {
				long minutes = seconds / 60;
				lore.add(Messages.get("duration.lasts_minutes").replace("$minutes", ((Long)minutes).toString()));					
			} else {
				lore.add(Messages.get("duration.lasts_seconds").replace("$seconds", ((Long)seconds).toString()));
			}
		}
		
		if ((spell instanceof BrushSpell) && !((BrushSpell)spell).hasBrushOverride()) {
			String brushText = Messages.get("spell.brush");
            if (!brushText.isEmpty()) {
                lore.add(ChatColor.GOLD + brushText);
            }
		}
		if (spell instanceof UndoableSpell && ((UndoableSpell)spell).isUndoable()) {
            String undoableText = Messages.get("spell.undoable");
            if (!undoableText.isEmpty()) {
                lore.add(ChatColor.GRAY + undoableText);
            }
		}
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
		
		// Fill in the hotbar
		Player player = mage.getPlayer();
		PlayerInventory playerInventory = player.getInventory();
		for (int i = 0; i < HOTBAR_SIZE; i++) {
			ItemStack playerItem = playerInventory.getItem(i);
			if (isWand(playerItem)) {
				playerItem = null;
			}
			hotbar.setItem(i, playerItem);
		}
		
		// Fill in the active inventory page
		Inventory openInventory = getOpenInventory();
		for (int i = 0; i < openInventory.getSize(); i++) {
			openInventory.setItem(i, playerInventory.getItem(i + HOTBAR_SIZE));
		}
	}

    public int enchant(int totalLevels, com.elmakers.mine.bukkit.api.magic.Mage mage) {
        return randomize(totalLevels, true, mage);
    }

    public int enchant(int totalLevels) {
        return randomize(totalLevels, true, null);
    }

	protected int randomize(int totalLevels, boolean additive, com.elmakers.mine.bukkit.api.magic.Mage enchanter) {
        if (enchanter == null && mage != null) {
            enchanter = mage;
        }
        WandUpgradePath path = getPath();
		if (path == null) {
            if (enchanter != null) {
                enchanter.sendMessage(Messages.get("wand.no_path"));
            }
            return 0;
        }

        path.catchup(this, enchanter);

        int minLevel = path.getMinLevel();
        if (totalLevels < minLevel) {
            if (enchanter != null) {
                String levelMessage = Messages.get("wand.need_more_levels");
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
            WandLevel level = path.getLevel(addLevels);
            modified = level.randomizeWand(enchanter, this, additive);
			totalLevels -= maxLevel;
            if (modified) {
                if (enchanter != null) {
                    path.enchanted(enchanter);
                }
                levels += addLevels;
            } else if (path.hasUpgrade()) {
                if (path.checkUpgrade(enchanter, this)) {
                    WandUpgradePath newPath = path.getUpgrade();
                    if (newPath == null) {
                        enchanter.sendMessage("Configuration issue, please check logs");
                        controller.getLogger().warning("Invalid upgrade path: " + path.getUpgrade());
                    } else {
                        enchanter.sendMessage(Messages.get("wand.level_up").replace("$path", newPath.getName()));
                        path.upgraded(this, enchanter);
                        this.path = newPath.getKey();
                        levels += addLevels;
                    }
                }
            } else if (enchanter != null) {
                enchanter.sendMessage(Messages.get("wand.fully_enchanted"));
            }
			addLevels = Math.min(totalLevels, maxLevel);
			additive = true;
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
	
	protected void sendAddMessage(String messageKey, String nameParam) {
		if (mage == null) return;
		
		String message = Messages.get(messageKey).replace("$name", nameParam);
		mage.sendMessage(message);
	}
	
	public boolean add(Wand other) {
		if (!isModifiable()) {
            if (other.path == null || path == null || !other.path.equals(path)) {
                return false;
            }
        }

        if (other.path != null && !other.path.isEmpty() && (this.path == null || !this.path.equals(other.path))) {
            return false;
        }
		
		boolean modified = false;
		
		if (other.isForcedUpgrade() || other.costReduction > costReduction) { costReduction = other.costReduction; modified = true; if (costReduction > 0) sendAddMessage("wand.upgraded_property", getLevelString("wand.cost_reduction", costReduction)); }
		if (other.isForcedUpgrade() || other.power > power) { power = other.power; modified = true; if (power > 0) sendAddMessage("wand.upgraded_property", getLevelString("wand.power", power)); }
		if (other.isForcedUpgrade() || other.damageReduction > damageReduction) { damageReduction = other.damageReduction; modified = true; if (damageReduction > 0) sendAddMessage("wand.upgraded_property", getLevelString("wand.protection", damageReduction)); }
		if (other.isForcedUpgrade() || other.damageReductionPhysical > damageReductionPhysical) { damageReductionPhysical = other.damageReductionPhysical; modified = true; if (damageReductionPhysical > 0) sendAddMessage("wand.upgraded_property", getLevelString("wand.protection_physical", damageReductionPhysical)); }
		if (other.isForcedUpgrade() || other.damageReductionProjectiles > damageReductionProjectiles) { damageReductionProjectiles = other.damageReductionProjectiles; modified = true; if (damageReductionProjectiles > 0) sendAddMessage("wand.upgraded_property", getLevelString("wand.protection_projectile", damageReductionProjectiles)); }
		if (other.isForcedUpgrade() || other.damageReductionFalling > damageReductionFalling) { damageReductionFalling = other.damageReductionFalling; modified = true; if (damageReductionFalling > 0) sendAddMessage("wand.upgraded_property", getLevelString("wand.protection_fall", damageReductionFalling)); }
		if (other.isForcedUpgrade() || other.damageReductionFire > damageReductionFire) { damageReductionFire = other.damageReductionFire; modified = true; if (damageReductionFire > 0) sendAddMessage("wand.upgraded_property", getLevelString("wand.protection_fire", damageReductionFire)); }
		if (other.isForcedUpgrade() || other.damageReductionExplosions > damageReductionExplosions) { damageReductionExplosions = other.damageReductionExplosions; modified = true; if (damageReductionExplosions > 0) sendAddMessage("wand.upgraded_property", getLevelString("wand.protection_blast", damageReductionExplosions)); }
		if (other.isForcedUpgrade() || other.healthRegeneration > healthRegeneration) { healthRegeneration = other.healthRegeneration; modified = true; if (healthRegeneration > 0) sendAddMessage("wand.upgraded_property", getLevelString("wand.health_regeneration", healthRegeneration)); }
		if (other.isForcedUpgrade() || other.hungerRegeneration > hungerRegeneration) { hungerRegeneration = other.hungerRegeneration; modified = true; if (hungerRegeneration > 0) sendAddMessage("wand.upgraded_property", getLevelString("wand.hunger_regeneration", hungerRegeneration)); }
		if (other.isForcedUpgrade() || other.speedIncrease > speedIncrease) { speedIncrease = other.speedIncrease; modified = true; if (speedIncrease > 0) sendAddMessage("wand.upgraded_property", getLevelString("wand.haste", speedIncrease)); }
		
		// Mix colors
		if (other.effectColor != null) {
			if (this.effectColor == null || (other.isUpgrade() && other.effectColor != null)) {
				this.effectColor = other.effectColor;
			} else {
				this.effectColor = this.effectColor.mixColor(other.effectColor, other.effectColorMixWeight);
			}
			modified = true;
		}

        if (other.rename && other.template != null && other.template.length() > 0) {
            ConfigurationSection template = wandTemplates.get(other.template);

            wandName = template.getString("name", wandName);
            wandName = Messages.get("wands." + other.template + ".name", wandName);
            updateName();
        }

        // Kind of a hacky way to allow for quiet level overries
        if (other.quietLevel < 0) {
            int quiet = -other.quietLevel - 1;
            modified = quietLevel != quiet;
            quietLevel = quiet;
        }

		modified = modified | (!keep && other.keep);
		modified = modified | (!bound && other.bound);
		modified = modified | (!effectBubbles && other.effectBubbles);
        modified = modified | (!undroppable && other.undroppable);
        modified = modified | (!indestructible && other.indestructible);

		keep = keep || other.keep;
		bound = bound || other.bound;
        indestructible = indestructible || other.indestructible;
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
		}
		
		if (other.effectSound != null && (other.isUpgrade || effectSound == null)) {
			modified = modified | (effectSound != other.effectSound);
			effectSound = other.effectSound;
			modified = modified | (effectSoundInterval != other.effectSoundInterval);
			effectSoundInterval = other.effectSoundInterval;
			modified = modified | (effectSoundVolume != other.effectSoundVolume);
			effectSoundVolume = other.effectSoundVolume;
			modified = modified | (effectSoundPitch != other.effectSoundPitch);
			effectSoundPitch = other.effectSoundPitch;
		}
		
		if ((template == null || template.length() == 0) && (other.template != null && other.template.length() > 0)) {
			modified = true;
			template = other.template;
		}
		
		if (other.isUpgrade && other.mode != null) {
			modified = modified | (mode != other.mode);
			setMode(other.mode);
		}

        if (other.upgradeIcon != null && (this.icon == null
               || this.icon.getMaterial() != other.upgradeIcon.getMaterial()
               || this.icon.getData() != other.upgradeIcon.getData())) {
            modified = true;
            this.setIcon(other.upgradeIcon);
        }
		
		// Don't need mana if cost-free
		if (isCostFree()) {
			xpRegeneration = 0;
			xpMax = 0;
			xp = 0;
		} else {
			if (other.isForcedUpgrade() || other.xpRegeneration > xpRegeneration) { xpRegeneration = other.xpRegeneration; modified = true; sendAddMessage("wand.upgraded_property", getLevelString("wand.mana_regeneration", xpRegeneration, controller.getMaxManaRegeneration())); }
			if (other.isForcedUpgrade() || other.xpMax > xpMax) { xpMax = other.xpMax; modified = true; sendAddMessage("wand.upgraded_property", getLevelString("wand.mana_amount", xpMax, controller.getMaxMana())); }
			if (other.isForcedUpgrade() || other.xp > xp) { xp = other.xp; modified = true; }
		}
		
		// Eliminate limited-use wands
		if (uses == 0 || other.uses == 0) {
			modified = modified | (uses != 0);
			uses = 0;
		} else {
			// Otherwise add them
			modified = modified | (other.uses != 0);
			uses = uses + other.uses;
		}
		
		// Add spells
		Set<String> spells = other.getSpells();
		for (String spellKey : spells) {
			if (addSpell(spellKey)) {
				modified = true;
				String spellName = spellKey;
				SpellTemplate spell = controller.getSpellTemplate(spellKey);
				if (spell != null) spellName = spell.getName();
				if (mage != null) mage.sendMessage(Messages.get("wand.spell_added").replace("$name", spellName));
			}
		}

		// Add materials
		Set<String> materials = other.getBrushes();
		for (String materialKey : materials) {
			if (addBrush(materialKey)) {
				modified = true;
				if (mage != null) mage.sendMessage(Messages.get("wand.brush_added").replace("$name", MaterialBrush.getMaterialName(materialKey)));
			}
		}

        // Add cast overrides
        if (other.castOverrides != null && other.castOverrides.size() > 0) {
            if (castOverrides == null) {
                castOverrides = new HashMap<String, String>();
            }
            HashSet<String> upgradedSpells = new HashSet<String>();
            for (Map.Entry<String, String> entry : other.castOverrides.entrySet()) {
                String overrideKey = entry.getKey();
                String currentValue = castOverrides.get(overrideKey);
                String value = entry.getValue();
                if (currentValue != null && !other.isForcedUpgrade()) {
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
                        mage.sendMessage(Messages.get("wand.spell_upgraded").replace("$name", spellName));
                        upgradedSpells.add(spellKey);
                    }
                }
                castOverrides.put(entry.getKey(), entry.getValue());
            }
        }
		
		Player player = (mage == null) ? null : mage.getPlayer();
		if (other.autoFill && player != null) {
			this.fill(player);
			modified = true;
			if (mage != null) mage.sendMessage(Messages.get("wand.filled"));
		}
		
		if (other.autoOrganize && mage != null) {
			this.organizeInventory(mage);
			modified = true;
			if (mage != null) mage.sendMessage(Messages.get("wand.reorganized"));
		}

		saveState();
		updateName();
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
	
	public static void loadTemplates(ConfigurationSection properties) {
		wandTemplates.clear();
		
		Set<String> wandKeys = properties.getKeys(false);
		for (String key : wandKeys)
		{
			ConfigurationSection wandNode = properties.getConfigurationSection(key);
			wandNode.set("key", key);
			ConfigurationSection existing = wandTemplates.get(key);
			if (existing != null) {
				Set<String> overrideKeys = existing.getKeys(false);
				for (String propertyKey : overrideKeys) {
					existing.set(propertyKey, existing.get(key));
				}
			} else {
				wandTemplates.put(key,  wandNode);
			}
			if (!wandNode.getBoolean("enabled", true)) {
				wandTemplates.remove(key);
			}
		}
	}
	
	public static Collection<String> getWandKeys() {
		return wandTemplates.keySet();
	}
	
	public static Collection<ConfigurationSection> getWandTemplates() {
		return wandTemplates.values();
	}
	
	public static WandMode parseWandMode(String modeString, WandMode defaultValue) {
		for (WandMode testMode : WandMode.values()) {
			if (testMode.name().equalsIgnoreCase(modeString)) {
				return testMode;
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
	
	public void toggleInventory() {
		if (!hasInventory) {
			if (activeSpell == null || activeSpell.length() == 0) {
				Set<String> spells = getSpells();
				// Sanity check, so it'll switch to inventory next time
				if (spells.size() > 1) hasInventory = true;
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
				mage.playSound(Sound.CHEST_OPEN, 0.3f, 1.5f);
				mage.getPlayer().updateInventory();
			}
		}
	}

    public void cycleInventory() {
        cycleInventory(1);
    }
	
	@SuppressWarnings("deprecation")
	private void openInventory() {
		if (mage == null) return;
		
		WandMode wandMode = getMode();
		if (wandMode == WandMode.CHEST) {
			inventoryIsOpen = true;
			mage.playSound(Sound.CHEST_OPEN, 0.4f, 0.2f);
			updateInventory();
			mage.getPlayer().openInventory(getDisplayInventory());
		} else if (wandMode == WandMode.INVENTORY) {
			if (hasStoredInventory()) return;
            ItemStack debugStack = mage.getPlayer().getInventory().getItem(mage.getPlayer().getInventory().getHeldItemSlot());

            if (storeInventory()) {
				inventoryIsOpen = true;
				mage.playSound(Sound.CHEST_OPEN, 0.4f, 0.2f);
				updateInventory();
				mage.getPlayer().updateInventory();
			}
		}
	}
	
	public void closeInventory() {
		if (!isInventoryOpen()) return;
		saveInventory();
		if (mage != null) {
			mage.playSound(Sound.CHEST_CLOSE, 0.4f, 0.2f);
			if (getMode() == WandMode.INVENTORY) {
				restoreInventory();
			} else {
				mage.getPlayer().closeInventory();
			}
		}
        inventoryIsOpen = false;
	}
	
	public boolean fill(Player player) {
		Collection<SpellTemplate> allSpells = controller.getPlugin().getSpellTemplates();
		for (SpellTemplate spell : allSpells)
		{
			if (spell.hasCastPermission(player) && spell.hasIcon() && !spell.isHidden())
			{
				addSpell(spell.getKey());
			}
		}
		
		autoFill = false;
		saveState();
		
		return true;
	}

    protected boolean checkWandItem() {
        if (playerInventorySlot != null && mage != null && mage.isPlayer()) {
            Player player = mage.getPlayer();
            ItemStack currentItem = player.getInventory().getItem(playerInventorySlot);
            if (isWand(currentItem) &&
                NMSUtils.getHandle(currentItem) != NMSUtils.getHandle(item)) {
                item = currentItem;
                return true;
            }
        }

        return false;
    }

	public void activate(Mage mage, ItemStack wandItem, int slot) {
		if (mage == null || wandItem == null) return;
        id = null;

        if (!canUse(mage.getPlayer())) {
            mage.sendMessage(Messages.get("wand.bound").replace("$name", getOwner()));
            mage.setActiveWand(null);
            return;
        }

        if (this.isUpgrade) {
            controller.getLogger().warning("Activated an upgrade item- this shouldn't happen");
            return;
        }
        playerInventorySlot = slot;
		
		// Update held item, it may have been copied since this wand was created.
        boolean needsSave = NMSUtils.getHandle(this.item) != NMSUtils.getHandle(wandItem);
		this.item = wandItem;
		this.mage = mage;
		
		// Check for spell or other special icons in the player's inventory
		Player player = mage.getPlayer();
        Inventory inventory = player.getInventory();
		ItemStack[] items = inventory.getContents();
        boolean forceUpdate = false;
		for (int i = 0; i < items.length; i++) {
			ItemStack item = items[i];
			if (addItem(item)) {
                inventory.setItem(i, null);
                forceUpdate = true;
			}
		}
		
		// Check for an empty wand and auto-fill
		if (!isUpgrade && (controller.fillWands() || autoFill)) {
            fill(mage.getPlayer());
		}
		
		// Check for auto-organize
		if (autoOrganize && !isUpgrade) {
			organizeInventory(mage);
		}
		
		// Check for auto-bind
        // Don't do this for op'd players, effectively, so
        // they can create and give unbound wands.
		if (bound && (ownerId == null || ownerId.length() == 0) && !controller.hasPermission(player, "Magic.wand.override_bind", false)) {
            // Backwards-compatibility, don't overrwrite unless the
            // name matches
            if (owner == null || owner.length() == 0 || owner.equals(player.getName())) {
                takeOwnership(mage.getPlayer());
                needsSave = true;
            }
		}

        // Check for randomized wands
        if (randomize) {
            randomize();
            forceUpdate = true;
        }
		
		checkActiveMaterial();

		mage.setActiveWand(this);
		if (usesMana()) {
			storedXpLevel = player.getLevel();
			storedXpProgress = player.getExp();
			updateMana();
		}
        if (needsSave) {
            saveState();
        }
		updateActiveMaterial();
		updateName();
		updateLore();
		
		updateEffects();
        if (forceUpdate) {
            player.updateInventory();
        }
	}

    protected void randomize() {
        boolean modified = randomize;
        randomize = false;
        if (description.contains("$")) {
            Matcher matcher = Messages.PARAMETER_PATTERN.matcher(description);
            while (matcher.find()) {
                String key = matcher.group(1);
                if (key != null) {
                    modified = true;
                    description = description.replace("$" + key, Messages.getRandomized(key));
                }
            }

            updateLore();
            updateName();
        }

        if (template != null && template.length() > 0) {
            ConfigurationSection wandConfig = wandTemplates.get(template);
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
            saveState();
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
	
	public boolean addItem(ItemStack item) {
		if (isUpgrade) return false;

		if (isSpell(item)) {
			String spellKey = getSpell(item);
			Set<String> spells = getSpells();
			if (!spells.contains(spellKey) && addSpell(spellKey)) {
				SpellTemplate spell = controller.getSpellTemplate(spellKey);
				if (spell != null) {
					mage.sendMessage(Messages.get("wand.spell_added").replace("$name", spell.getName()));
					return true;
				}
			}
		} else if (isBrush(item)) {
			String materialKey = getBrush(item);
			Set<String> materials = getBrushes();
			if (!materials.contains(materialKey) && addBrush(materialKey)) {
				mage.sendMessage(Messages.get("wand.brush_added").replace("$name", MaterialBrush.getMaterialName(materialKey)));
				return true;
			}
		} else if (isUpgrade(item)) {
			Wand wand = new Wand(controller, item);
			return this.add(wand);
		}
		
		return false;
	}
	
	protected void updateEffects() {
		if (mage == null) return;
		Player player = mage.getPlayer();
		if (player == null) return;
		
		// Update Bubble effects effects
		if (effectBubbles) {
			CompatibilityUtils.addPotionEffect(player, effectColor.getColor());
		}
		
		Location location = mage.getLocation();
		
		if (effectParticle != null && location != null && effectParticleInterval > 0 && effectParticleCount > 0) {
			if ((effectParticleCounter++ % effectParticleInterval) == 0) {
				if (effectPlayer == null) {
					effectPlayer = new EffectRing(controller.getPlugin());
					effectPlayer.setParticleCount(2);
					effectPlayer.setIterations(2);
					effectPlayer.setRadius(2);
					effectPlayer.setSize(5);
					effectPlayer.setMaterial(location.getBlock().getRelative(BlockFace.DOWN));
				}
				effectPlayer.setParticleType(effectParticle);
				effectPlayer.setParticleData(effectParticleData);
				effectPlayer.setParticleCount(effectParticleCount);
				effectPlayer.start(player.getEyeLocation(), null);
			}
		}
		
		if (effectSound != null && location != null && controller.soundsEnabled()) {
			if ((effectSoundCounter++ % effectSoundInterval) == 0) {
				mage.getLocation().getWorld().playSound(location, effectSound, effectSoundVolume, effectSoundPitch);
			}
		}
	}

    protected void updateDurability() {
        int maxDurability = item.getType().getMaxDurability();
        if (maxDurability > 0) {
            int durability = (short)(xp * maxDurability / xpMax);
            durability = maxDurability - durability;
            if (durability >= maxDurability) {
                durability = maxDurability - 1;
            } else if (durability < 0) {
                durability = 0;
            }
            item.setDurability((short)durability);
        }
    }

	protected void updateMana() {
		if (mage != null && xpMax > 0 && xpRegeneration > 0) {
			Player player = mage.getPlayer();
            if (displayManaAsGlow) {
                if (xp == xpMax) {
                    CompatibilityUtils.addGlow(item);
                } else {
                    CompatibilityUtils.removeGlow(item);
                }
            }
            if (displayManaAsDurability) {
                updateDurability();
            }
			else {
                if (displayManaAsBar) {
                    if (!retainLevelDisplay) {
                        player.setLevel(0);
                    }
                    player.setExp((float)xp / (float)xpMax);
                } else {
                    player.setLevel(xp);
                    player.setExp(0);
                }
            }
		}
	}
	
	public boolean isInventoryOpen() {
		return mage != null && inventoryIsOpen;
	}
	
	public void deactivate() {
		if (mage == null) return;

        Player player = mage.getPlayer();
		if (effectBubbles && player != null) {
			CompatibilityUtils.removePotionEffect(player);
		}
		
		// This is a tying wands together with other spells, potentially
		// But with the way the mana system works, this seems like the safest route.
		mage.deactivateAllSpells();
		
		if (isInventoryOpen()) {
			closeInventory();
		}
        playerInventorySlot = null;
        storedInventory = null;
		
		if (usesMana() && player != null) {
            player.setExp(storedXpProgress);
            if (!retainLevelDisplay) {
                player.setLevel(storedXpLevel);
            }
			storedXpProgress = 0;
			storedXpLevel = 0;
		}
        saveState();
		mage.setActiveWand(null);
		mage = null;
	}
	
	public Spell getActiveSpell() {
		if (mage == null || activeSpell == null || activeSpell.length() == 0) return null;
		return mage.getSpell(activeSpell);
	}

    public String getActiveSpellKey() {
        return activeSpell;
    }

    public String getActiveBrushKey() {
        return activeMaterial;
    }
	
	public boolean cast() {
		Spell spell = getActiveSpell();
		if (spell != null) {
            use();
            Collection<String> castParameters = null;
            if (castOverrides != null && castOverrides.size() > 0) {
                castParameters = new ArrayList<String>();
                for (Map.Entry<String, String> entry : castOverrides.entrySet()) {
                    String[] key = StringUtils.split(entry.getKey(), ".");
                    if (key.length == 0) continue;
                    if (key.length == 2 && !key[0].equals("default") && !key[0].equals(spell.getKey())) {
                        continue;
                    }
                    castParameters.add(key.length == 2 ? key[1] : key[0]);
                    castParameters.add(entry.getValue());
                }
            }
			if (spell.cast(castParameters == null ? null : castParameters.toArray(EMPTY_PARAMETERS))) {
				Color spellColor = spell.getColor();
				if (spellColor != null && this.effectColor != null) {
					this.effectColor = this.effectColor.mixColor(spellColor, effectColorSpellMixWeight);
					// Note that we don't save this change.
					// The hope is that the wand will get saved at some point later
					// And we don't want to trigger NBT writes every spell cast.
					// And the effect color morphing isn't all that important if a few
					// casts get lost.
				}

				return true;
			}
		}
		
		return false;
	}
	
	@SuppressWarnings("deprecation")
	protected void use() {
		if (mage == null) return;
		if (uses > 0) {
			uses--;
			if (uses <= 0) {
                // Safety thing... ?
                uses = 1;

                Player player = mage.getPlayer();
                mage.playSound(Sound.ITEM_BREAK, 1.0f, 0.8f);

                deactivate();

				PlayerInventory playerInventory = player.getInventory();
				playerInventory.setItemInHand(new ItemStack(Material.AIR, 1));
				player.updateInventory();
			} else {
                saveState();
				updateName();
				updateLore();
			}
		}
	}
	
	public void onPlayerExpChange(PlayerExpChangeEvent event) {
		if (mage == null) return;

		if (addExperience(event.getAmount())) {
			event.setAmount(0);
		}
	}

    public boolean addExperience(int xp) {
        if (usesMana()) {
            Player player = mage == null ? null : mage.getPlayer();
            if (player != null) {
                player.setExp(storedXpProgress);
                if (!retainLevelDisplay) {
                    player.setLevel(storedXpLevel);
                }
                player.giveExp(xp);
                storedXpProgress = player.getExp();
                storedXpLevel = player.getLevel();
            }
            return true;
        }

        return false;
    }
	
	public void tick() {
		if (mage == null || item == null) return;
		
		Player player = mage.getPlayer();
		if (player == null) return;

        boolean modified = checkWandItem();
        int maxDurability = item.getType().getMaxDurability();

        // Auto-repair wands
        if (!displayManaAsDurability && maxDurability > 0) {
            item.setDurability((short)0);
        }

		if (speedIncrease > 0) {
			int hasteLevel = (int)(speedIncrease * controller.getMaxHaste());
			if (hasteEffect == null || hasteEffect.getAmplifier() != hasteLevel) {
				hasteEffect = new PotionEffect(PotionEffectType.SPEED, 80, hasteLevel, true);
			}
			
			CompatibilityUtils.applyPotionEffect(player, hasteEffect);
		}
		if (healthRegeneration > 0) {
			int regenLevel = (int)(healthRegeneration * controller.getMaxHealthRegeneration());
			if (healthRegenEffect == null || healthRegenEffect.getAmplifier() != regenLevel) {
				healthRegenEffect = new PotionEffect(PotionEffectType.REGENERATION, 80, regenLevel, true);
			}
			
			CompatibilityUtils.applyPotionEffect(player, healthRegenEffect);
		}
		if (hungerRegeneration > 0) {
			int regenLevel = (int)(hungerRegeneration * controller.getMaxHungerRegeneration());
			if (hungerRegenEffect == null || hungerRegenEffect.getAmplifier() != regenLevel) {
				hungerRegenEffect = new PotionEffect(PotionEffectType.SATURATION, 80, regenLevel, true);
			}
			
			CompatibilityUtils.applyPotionEffect(player, hungerRegenEffect);
		}
		if (usesMana()) {
			xp = Math.min(xpMax, xp + xpRegeneration);
			updateMana();
		}
		if (damageReductionFire > 0 && player.getFireTicks() > 0) {
			player.setFireTicks(0);
		}
		
		updateEffects();
        if (modified) {
            saveState();
        }
	}
	
	public MagicController getMaster() {
		return controller;
	}
	
	public void cycleSpells() {
		Set<String> spellsSet = getSpells();
		ArrayList<String> spells = new ArrayList<String>(spellsSet);
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
		
		spellIndex = (spellIndex + 1) % spells.size();
		setActiveSpell(spells.get(spellIndex).split("@")[0]);
	}
	
	public void cycleMaterials() {
		Set<String> materialsSet = getBrushes();
		ArrayList<String> materials = new ArrayList<String>(materialsSet);
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
		
		materialIndex = (materialIndex + 1) % materials.size();
		activateBrush(materials.get(materialIndex).split("@")[0]);
	}

	public Mage getActivePlayer() {
		return mage;
	}
	
	protected void clearInventories() {
		inventories.clear();
		hotbar.clear();
	}
	
	public Color getEffectColor() {
		return effectColor == null ? null : effectColor.getColor();
	}

    public ParticleEffect getEffectParticle() {
        return effectParticle;
    }

	public Inventory getHotbar() {
		return hotbar;
	}
	
	public WandMode getMode() {
		return mode != null ? mode : controller.getDefaultWandMode();
	}
	
	public void setMode(WandMode mode) {
		this.mode = mode;
	}
	
	public boolean showCastMessages() {
		return quietLevel == 0;
	}
	
	public boolean showMessages() {
		return quietLevel < 2;
	}
	
	/*
	 * Public API Implementation
	 */

    @Override
	public boolean isLost()
    {
		return this.id != null;
	}

    @Override
    public boolean isLost(com.elmakers.mine.bukkit.api.wand.LostWand lostWand) {
        return this.id != null && this.id.equals(lostWand.getId());
    }

    @Override
    public LostWand makeLost(Location location)
    {
        if (id == null || id.length() == 0) {
            id = UUID.randomUUID().toString();
            saveState();
        }
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
			activate((Mage)mage, player.getItemInHand(), player.getInventory().getHeldItemSlot());
		}
	}

	@Override
	public void organizeInventory(com.elmakers.mine.bukkit.api.magic.Mage mage) {
			WandOrganizer organizer = new WandOrganizer(this, mage);
			organizer.organize();
			openInventoryPage = 0;
			autoOrganize = false;
			saveState();
		}

	@Override
	public com.elmakers.mine.bukkit.api.wand.Wand duplicate() {
		ItemStack newItem = InventoryUtils.getCopy(item);
		Wand newWand = new Wand(controller, newItem);
		newWand.saveState();
		return newWand;
	}

	@Override
	public boolean configure(Map<String, Object> properties) {
		Map<Object, Object> convertedProperties = new HashMap<Object, Object>(properties);
		loadProperties(ConfigurationUtils.toNodeList(convertedProperties), false);
        saveState();
        updateName();
        updateLore();
		return true;
	}

	@Override
	public boolean upgrade(Map<String, Object> properties) {
		Map<Object, Object> convertedProperties = new HashMap<Object, Object>(properties);
		loadProperties(ConfigurationUtils.toNodeList(convertedProperties), true);
        saveState();
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

	@Override
	public boolean canUse(Player player) {
		if (!bound || owner == null || owner.length() == 0) return true;
		if (controller.hasPermission(player, "Magic.wand.override_bind", false)) return true;

        // Backwards-compatibility
        if (ownerId == null || ownerId.length() == 0) {
            return owner.equalsIgnoreCase(player.getName());
        }

		return ownerId.equalsIgnoreCase(player.getUniqueId().toString());
	}
	
	public boolean addSpell(String spellName) {
		if (!isModifiable()) return false;
		if (hasSpell(spellName)) return false;
		
		if (isInventoryOpen()) {
			saveInventory();
		}
        SpellTemplate template = controller.getSpellTemplate(spellName);
        if (template == null) {
            controller.getLogger().warning("Tried to add unknown spell to wand: " + spellName);
            return false;
        }
        if (hasSpell(template.getKey())) return false;

		ItemStack spellItem = createSpellIcon(template);
		if (spellItem == null) {
			return false;
		}
        spells.add(template.getKey());
		addToInventory(spellItem);
		updateInventory();
		hasInventory = getSpells().size() + getBrushes().size() > 1;
        saveState();
		updateLore();
		
		return true;
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
		return getSpells().contains(spellName);
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

        brushes.add(materialKey);
		addToInventory(itemStack);
		if (activeMaterial == null || activeMaterial.length() == 0) {
			setActiveBrush(materialKey);
		} else {
			updateInventory();
		}
        hasInventory = getSpells().size() + getBrushes().size() > 1;
        saveState();
		updateLore();

		return true;
	}
	
	@Override
	public void setActiveBrush(String materialKey) {
		this.activeMaterial = materialKey;
        saveState();
		updateName();
		updateActiveMaterial();
        updateHotbar();
	}

	@Override
	public void setActiveSpell(String activeSpell) {
		this.activeSpell = activeSpell;
        saveState();
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
        saveState();
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
        saveState();
		updateName();
		updateLore();
		
		return found;
	}

    @Override
    public Map<String, String> getOverrides()
    {
        return castOverrides == null ? new HashMap<String, String>() : new HashMap<String, String>(castOverrides);
    }

    @Override
    public void setOverrides(Map<String, String> overrides)
    {
        if (overrides == null) {
            this.castOverrides = null;
        } else {
            this.castOverrides = new HashMap<String, String>(overrides);
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
            castOverrides = new HashMap<String, String>();
        }
        if (value == null || value.length() == 0) {
            castOverrides.remove(key);
        } else {
            castOverrides.put(key, value);
        }
    }

    public void setStoredXpLevel(int level) {
        this.storedXpLevel = level;
    }

    public int getStoredXpLevel() {
        return storedXpLevel;
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
        storedInventory = CompatibilityUtils.createInventory(null, inventory.getSize(), "Stored Inventory");

        // Make sure we don't store any spells or magical materials, just in case
        ItemStack[] contents = inventory.getContents();
        for (int i = 0; i < contents.length; i++) {
            if (Wand.isSpell(contents[i])) {
                contents[i] = null;
            }
        }
        storedInventory.setContents(contents);
        inventory.clear();
        if (controller.isInventoryBackupEnabled()) {
            saveState();
        }

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
        inventory.setContents(storedInventory.getContents());
        storedInventory = null;
        saveState();

        player.updateInventory();

        return true;
    }

    public boolean isBound() {
        return bound;
    }
}
