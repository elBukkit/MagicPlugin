package com.elmakers.mine.bukkit.wand;

import java.util.*;
import java.util.regex.Matcher;

import com.elmakers.mine.bukkit.api.effect.ParticleType;
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
import com.elmakers.mine.bukkit.utility.ColorHD;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import com.elmakers.mine.bukkit.utility.Messages;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class Wand implements CostReducer, com.elmakers.mine.bukkit.api.wand.Wand {
    public static Plugin metadataProvider;

	public final static int INVENTORY_SIZE = 27;
	public final static int HOTBAR_SIZE = 9;
	public final static float DEFAULT_SPELL_COLOR_MIX_WEIGHT = 0.0001f;
	public final static float DEFAULT_WAND_COLOR_MIX_WEIGHT = 1.0f;
	
	// REMEMBER! Each of these MUST have a corresponding class in .traders, else traders will
	// destroy the corresponding data.
	public final static String[] PROPERTY_KEYS = {
		"active_spell", "active_material",
        "path",
		"xp", "xp_regeneration", "xp_max",
		"bound", "uses", "upgrade", "indestructible",
		"cost_reduction", "cooldown_reduction", "effect_bubbles", "effect_color", 
		"effect_particle", "effect_particle_count", "effect_particle_data", "effect_particle_interval", 
		"effect_sound", "effect_sound_interval", "effect_sound_pitch", "effect_sound_volume",
		"haste", 
		"health_regeneration", "hunger_regeneration", 
		"icon", "mode", "keep", "locked", "quiet", 
		"power", 
		"protection", "protection_physical", "protection_projectiles", 
		"protection_falling", "protection_fire", "protection_explosions",
		"materials", "spells"
	};
	public final static String[] HIDDEN_PROPERTY_KEYS = {
		"id", "owner", "owner_id", "name", "description", "template",
		"organize", "fill"
	};
	public final static String[] ALL_PROPERTY_KEYS = (String[])ArrayUtils.addAll(PROPERTY_KEYS, HIDDEN_PROPERTY_KEYS);
	
	protected ItemStack item;
	protected MagicController controller;
	protected Mage mage;
	
	// Cached state
	private String id = "";
	private Inventory hotbar;
	private List<Inventory> inventories;
	
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
	private boolean keep = false;
	private boolean autoOrganize = false;
	private boolean autoFill = false;
	private boolean isUpgrade = false;
	
	private MaterialAndData icon = null;
	
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
	private ParticleType effectParticle = null;
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
	
	private int storedXpLevel = 0;
	private int storedXp = 0;
	private float storedXpProgress = 0;
	
	private int quietLevel = 0;
	
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
	public static boolean retainLevelDisplay = true;
	public static Material DefaultUpgradeMaterial = Material.NETHER_STAR;
	public static Material DefaultWandMaterial = Material.BLAZE_ROD;
	public static Material EnchantableWandMaterial = null;
	public static boolean EnableGlow = true;

	public Wand(MagicController controller, ItemStack itemStack) {
		this.controller = controller;
		hotbar = CompatibilityUtils.createInventory(null, 9, "Wand");
		this.icon = new MaterialAndData(itemStack.getType(), (byte)itemStack.getDurability());
		inventories = new ArrayList<Inventory>();
		item = itemStack;
		indestructible = controller.getIndestructibleWands();
		loadState();
	}
	
	public Wand(MagicController controller) {
		this(controller, DefaultWandMaterial, (short)0);
	}
	
	protected Wand(MagicController controller, String templateName) throws IllegalArgumentException {
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
				throw new IllegalArgumentException("No template named " + templateName);
			}
			ConfigurationSection wandConfig = wandTemplates.get(templateName);

			// Default to template names, override with localizations
            wandName = wandConfig.getString("name", wandName);
			wandName = Messages.get("wands." + templateName + ".name", wandName);
            wandDescription = wandConfig.getString("description", wandDescription);
			wandDescription = Messages.get("wands." + templateName + ".description", wandDescription);

            if (wandDescription.contains("$")) {
                Matcher matcher = Messages.PARAMETER_PATTERN.matcher(wandDescription);
                while(matcher.find()) {
                    String key = matcher.group(1);
                    if (key != null) {
                        wandDescription = wandDescription.replace("$" + key, Messages.getRandomized(key));
                    }
                }
            }
			
			// Load all properties
			loadProperties(wandConfig);

            // Enchant, if an enchanting level was provided
            if (level > 0) {
                // Account for randomized locked wands
                boolean wasLocked = locked;
                locked = false;
                randomize(level, false);
                locked = wasLocked;
            }
		}

		setDescription(wandDescription);
		setName(wandName);
		setTemplate(templateName);
		suspendSave = false;
		saveState();
	}
	
	public Wand(MagicController controller, Material icon, short iconData) {
		// This will make the Bukkit ItemStack into a real ItemStack with NBT data.
		this(controller, InventoryUtils.getCopy(new ItemStack(icon, 1, iconData)));
		wandName = Messages.get("wand.default_name");
		updateName();
		if (EnableGlow) {
            CompatibilityUtils.addGlow(item);
		}
		saveState();
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
            wandName = Messages.get("wand.upgrade_name");
            description = Messages.get("wand.upgrade_default_description");
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
	
	public boolean isUpgrade() {
		return isUpgrade;
	}
	
	public boolean usesMana() {
		return xpMax > 0 && xpRegeneration > 0 && !isCostFree();
	}

	public float getCooldownReduction() {
		return controller.getCooldownReduction() + cooldownReduction * WandLevel.maxCooldownReduction;
	}

	public float getCostReduction() {
		if (isCostFree()) return 1.0f;
		return controller.getCostReduction() + costReduction * WandLevel.maxCostReduction;
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
		return damageReduction * WandLevel.maxDamageReduction;
	}

	public float getDamageReductionPhysical() {
		return damageReductionPhysical * WandLevel.maxDamageReductionPhysical;
	}
	
	public float getDamageReductionProjectiles() {
		return damageReductionProjectiles * WandLevel.maxDamageReductionProjectiles;
	}

	public float getDamageReductionFalling() {
		return damageReductionFalling * WandLevel.maxDamageReductionFalling;
	}

	public float getDamageReductionFire() {
		return damageReductionFire * WandLevel.maxDamageReductionFire;
	}

	public float getDamageReductionExplosions() {
		return damageReductionExplosions * WandLevel.maxDamageReductionExplosions;
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
		return getSpells(false);
	}
	
	protected Set<String> getSpells(boolean includePositions) {
		Set<String> spellNames = new TreeSet<String>();
		List<Inventory> allInventories = getAllInventories();
        int index = 0;
		for (Inventory inventory : allInventories) {
			ItemStack[] items = inventory.getContents();
			for (int i = 0; i < items.length; i++) {
				if (items[i] != null && isSpell(items[i])) {
                    String spellName = getSpell(items[i]);
                    if (includePositions) {
                        spellName += "@" + index;
                    }
                    spellNames.add(spellName);
				}
                index++;
			}
		}
		return spellNames;
	}
	
	protected String getSpellString() {
		return StringUtils.join(getSpells(true), ",");
	}

	public Set<String> getBrushes() {
		return getMaterialKeys(false);
	}
	
	protected Set<String> getMaterialKeys(boolean includePositions) {
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
                        if (includePositions) {
                            materialKey += "@" + index;
                        }
                        materialNames.add(materialKey);
                    }
                }
                index++;
			}
		}
		return materialNames;	
	}
	
	protected String getMaterialString() {
		return StringUtils.join(getMaterialKeys(true), ",");		
	}
	
	protected Integer parseSlot(String[] pieces) {
		Integer slot = null;
		if (pieces.length > 0) {
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
        if (getMode() == WandMode.INVENTORY && mage != null && mage.getPlayer() != null) {
            selectedItem = mage.getPlayer().getInventory().getHeldItemSlot();

            // Toss the item back into the wand inventory, it'll find a home somewhere.
            // We hope this doesn't recurse too badly! :\
            ItemStack existingHotbar = hotbar.getItem(selectedItem);
            if (existingHotbar != null && existingHotbar.getType() != Material.AIR && !isWand(existingHotbar)) {
                hotbar.setItem(selectedItem, item);
                addToInventory(existingHotbar);          }

            hotbar.setItem(selectedItem, item);
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
		// Support YML-List-As-String format and |-delimited format
		spellString = spellString.replaceAll("[\\]\\[]", "");
		String[] spellNames = StringUtils.split(spellString, "|,");
		for (String spellName : spellNames) {		
			String[] pieces = spellName.split("@");
			Integer slot = parseSlot(pieces);
			String spellKey = pieces[0].trim();
			ItemStack itemStack = createSpellIcon(spellKey);
			if (itemStack == null) {
				controller.getPlugin().getLogger().warning("Unable to create spell icon for key " + spellKey);
				continue;
			}
			if (activeSpell == null || activeSpell.length() == 0) activeSpell = spellKey;
			addToInventory(itemStack, slot);
		}
		materialString = materialString.replaceAll("[\\]\\[]", "");
		String[] materialNames = StringUtils.split(materialString, "|,");
		for (String materialName : materialNames) {
			String[] pieces = materialName.split("@");
			Integer slot = parseSlot(pieces);
			String materialKey = pieces[0].trim();
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

	@SuppressWarnings("deprecation")
	public static ItemStack createSpellItem(String spellKey, MagicController controller, Wand wand, boolean isItem) {
		SpellTemplate spell = controller.getSpellTemplate(spellKey);
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
			itemStack = InventoryUtils.getCopy(originalItemStack);
		} catch (Exception ex) {
			itemStack = null;
		}
		if (itemStack == null) {
			controller.getPlugin().getLogger().warning("Unable to create spell icon for " + spellKey + " with material " + icon.getMaterial().name());	
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
		ItemStack itemStack = InventoryUtils.getCopy(originalItemStack);
		if (itemStack == null) {
			controller.getPlugin().getLogger().warning("Unable to create material icon for " + material.name() + ": " + materialKey);	
			return null;
		}
		ItemMeta meta = itemStack.getItemMeta();
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
		meta.setLore(lore);
		itemStack.setItemMeta(meta);
		updateBrushItem(itemStack, materialKey, wand);
		return itemStack;
	}

	protected void saveState() {
		if (suspendSave || item == null) return;

        ConfigurationSection stateNode = new MemoryConfiguration();
        saveProperties(stateNode);

        // Save legacy data as well until migration is settled
		Object wandNode = InventoryUtils.createNode(item, "wand");
		if (wandNode == null) {
			controller.getLogger().warning("Failed to save legacy wand state for wand id " + id + " to : " + item + " of class " + item.getClass());
		} else {
            InventoryUtils.saveTagsToNBT(stateNode, wandNode, ALL_PROPERTY_KEYS);
        }

        // TODO: Re-implement using Metadata API in 4.0
        Object magicNode = CompatibilityUtils.createMetadataNode(item, metadataProvider, isUpgrade ? "upgrade" : "wand");
        if (magicNode == null) {
            controller.getLogger().warning("Failed to save wand state for wand id " + id + " to : " + item + " of class " + item.getClass());
            return;
        }

        // Clean up any extra data that might be on here
        if (isUpgrade) {
            CompatibilityUtils.removeMetadata(item, metadataProvider, "wand");
        } else {
            CompatibilityUtils.removeMetadata(item, metadataProvider, "upgrade");
        }
        CompatibilityUtils.removeMetadata(item, metadataProvider, "spell");
        CompatibilityUtils.removeMetadata(item, metadataProvider, "brush");

        InventoryUtils.saveTagsToNBT(stateNode, magicNode, ALL_PROPERTY_KEYS);
	}
	
	protected void loadState() {
		if (item == null) return;

        boolean isWand = CompatibilityUtils.hasMetadata(item, metadataProvider, "wand");
        boolean isUpgrade = !isWand && CompatibilityUtils.hasMetadata(item, metadataProvider, "upgrade");
        if (isWand || isUpgrade) {

            // TODO: Re-implement using Metadata API in 4.0
            Object magicNode = CompatibilityUtils.getMetadataNode(item, metadataProvider, isUpgrade ? "upgrade" : "wand");
            if (magicNode == null) {
                return;
            }

            ConfigurationSection stateNode = new MemoryConfiguration();
            InventoryUtils.loadTagsFromNBT(stateNode, magicNode, ALL_PROPERTY_KEYS);

            loadProperties(stateNode);
        } else {
            Object wandNode = InventoryUtils.getNode(item, "wand");
            if (wandNode == null) {
                return;
            }

            ConfigurationSection stateNode = new MemoryConfiguration();
            InventoryUtils.loadTagsFromNBT(stateNode, wandNode, ALL_PROPERTY_KEYS);

            loadProperties(stateNode);
        }
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
		node.set("bound", bound);
		node.set("indestructible", indestructible);
		node.set("fill", autoFill);
		node.set("upgrade", isUpgrade);
		node.set("organize", autoOrganize);
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
	}
	
	public void loadProperties(ConfigurationSection wandConfig) {
		loadProperties(wandConfig, false);
	}
	
	public void setEffectColor(String hexColor) {
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
			bound = wandConfig.getBoolean("bound", bound);
			autoOrganize = wandConfig.getBoolean("organize", autoOrganize);
			autoFill = wandConfig.getBoolean("fill", autoFill);
			
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
			
			if (wandConfig.contains("icon")) {
                String iconKey = wandConfig.getString("icon");
                if (iconKey.contains(",")) {
                    Random r = new Random();
                    String[] keys = StringUtils.split(iconKey, ',');
                    iconKey = keys[r.nextInt(keys.length)];
                }
				setIcon(ConfigurationUtils.toMaterialAndData(iconKey));
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
		} else {
			effectParticleInterval = (effectParticleInterval == 0) ? 2 : effectParticleInterval;
			effectParticleCount = (effectParticleCount == 0) ? 1 : effectParticleCount;
		}
		
		if (xpRegeneration <= 0 || xpMax <= 0 || costReduction >= 1) {
			xpMax = 0;
			xpRegeneration = 0;
			xp = 0;
		}
		
		checkActiveMaterial();
		
		saveState();
		updateName();
		updateLore();
	}

	protected void parseSoundEffect(String effectSoundName) {
		if (effectSoundName.length() > 0) {
			String testName = effectSoundName.toUpperCase().replace("_", "");
			try {
				for (Sound testType : Sound.values()) {
					String testTypeName = testType.name().replace("_", "");
					if (testTypeName.equals(testName)) {
						effectSound = testType;
						break;
					}
				}
			} catch (Exception ex) {
				effectSound = null;
			}
		} else {
			effectSound = null;
		}
	}

	protected void parseParticleEffect(String effectParticleName) {
		if (effectParticleName.length() > 0) {
			String testName = effectParticleName.toUpperCase().replace("_", "");
			try {
				for (ParticleType testType : ParticleType.values()) {
					String testTypeName = testType.name().replace("_", "");
					if (testTypeName.equals(testName)) {
						effectParticle = testType;
						break;
					}
				}
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
		if (owner.length() > 0) {
			sender.sendMessage(ChatColor.ITALIC + "" + ChatColor.WHITE + owner);
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
	
	private static String getSpellDisplayName(SpellTemplate spell, String materialKey) {
		String name = "";
		if (spell != null) {
			if (materialKey != null && (spell instanceof BrushSpell) && !((BrushSpell)spell).hasBrushOverride()) {
				String materialName = MaterialBrush.getMaterialName(materialKey);
				if (materialName == null) {
					materialName = "none";
				}
				name = ChatColor.GOLD + spell.getName() + ChatColor.GRAY + " " + materialName + ChatColor.WHITE;
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
                ? (bound ? ChatColor.DARK_AQUA : ChatColor.AQUA) : ChatColor.GOLD;
		String name = wandColor + wandName;

        Set<String> spells = getSpells();

        // Add active spell to description
        if (spell != null && (spells.size() > 1 || hasPath())) {
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
	
	public void updateName(boolean isActive) {
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(isActive ? getActiveWandName() : wandName);
		item.setItemMeta(meta);
		
		// Reset Enchantment glow
		if (EnableGlow) {
            CompatibilityUtils.addGlow(item);
		}

		// The all-important last step of restoring the meta state, something
		// the Anvil will blow away.
		saveState();
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
			lore.add(ChatColor.LIGHT_PURPLE + "" + ChatColor.ITALIC + getLevelString("wand.mana_amount", xpMax, WandLevel.maxMaxXp));
			lore.add(ChatColor.RESET + "" + ChatColor.LIGHT_PURPLE + getLevelString("wand.mana_regeneration", xpRegeneration, WandLevel.maxXpRegeneration));
		}
		if (costReduction > 0) lore.add(ChatColor.AQUA + getLevelString("wand.cost_reduction", costReduction));
		if (cooldownReduction > 0) lore.add(ChatColor.AQUA + getLevelString("wand.cooldown_reduction", cooldownReduction));
		if (power > 0) lore.add(ChatColor.AQUA + getLevelString("wand.power", power));
		if (speedIncrease > 0) lore.add(ChatColor.AQUA + getLevelString("wand.haste", speedIncrease));
		if (damageReduction > 0) lore.add(ChatColor.AQUA + getLevelString("wand.protection", damageReduction, WandLevel.maxDamageReduction));
		if (damageReduction < 1) {
			if (damageReductionPhysical > 0) lore.add(ChatColor.AQUA + getLevelString("wand.protection_physical", damageReductionPhysical, WandLevel.maxDamageReductionPhysical));
			if (damageReductionProjectiles > 0) lore.add(ChatColor.AQUA + getLevelString("wand.protection_projectile", damageReductionProjectiles, WandLevel.maxDamageReductionProjectiles));
			if (damageReductionFalling > 0) lore.add(ChatColor.AQUA + getLevelString("wand.protection_fall", damageReductionFalling, WandLevel.maxDamageReductionFalling));
			if (damageReductionFire > 0) lore.add(ChatColor.AQUA + getLevelString("wand.protection_fire", damageReductionFire, WandLevel.maxDamageReductionFire));
			if (damageReductionExplosions > 0) lore.add(ChatColor.AQUA + getLevelString("wand.protection_blast", damageReductionExplosions, WandLevel.maxDamageReductionExplosions));
		}
		if (healthRegeneration > 0) lore.add(ChatColor.AQUA + getLevelString("wand.health_regeneration", healthRegeneration));
		if (hungerRegeneration > 0) lore.add(ChatColor.AQUA + getLevelString("wand.hunger_regeneration", hungerRegeneration));
	}
	
	private String getLevelString(String templateName, float amount)
	{
		return getLevelString(templateName, amount, 1);
	}
	
	private static String getLevelString(String templateName, float amount, float max)
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
		
		SpellTemplate spell = controller.getSpellTemplate(activeSpell);
        if (activeSpell != null) {
            lore.add(getSpellDisplayName(spell, null));
        }
		if (spell != null && spellCount == 1 && materialCount <= 1 && !isUpgrade && !hasPath()) {
			addSpellLore(spell, lore, this);
		}
        if (description.length() > 0) {
            lore.add(ChatColor.ITALIC + "" + ChatColor.GREEN + description);
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
            } else {
                lore.add(Messages.get("wand.spell_count").replace("$count", ((Integer)spellCount).toString()));
            }
        }
        if (materialCount > 0) {
            if (isUpgrade) {
                lore.add(Messages.get("wand.material_count").replace("$count", ((Integer)materialCount).toString()));
            } else {
                lore.add(Messages.get("wand.upgrade_material_count").replace("$count", ((Integer)materialCount).toString()));
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
		ItemMeta meta = item.getItemMeta();
		List<String> lore = getLore();
		meta.setLore(lore);
		
		item.setItemMeta(meta);
		if (EnableGlow) {
			CompatibilityUtils.addGlow(item);
		}
		
		// Setting lore will reset wand data
		saveState();
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
		return item != null && (CompatibilityUtils.hasMetadata(item, metadataProvider, "wand") || isLegacyWand(item));
	}

    public static boolean isLegacyWand(ItemStack item) {
        return item != null && InventoryUtils.hasMeta(item, "wand") && !isLegacyUpgrade(item);
    }

	public static boolean isLegacyUpgrade(ItemStack item) {
		if (item == null) return false;
		Object wandNode = InventoryUtils.getNode(item, "wand");
		
		if (wandNode == null) return false;
		String upgradeData = InventoryUtils.getMeta(wandNode, "upgrade");
		
		return upgradeData != null && upgradeData.equals("true");
	}

    public static boolean isUpgrade(ItemStack item) {
        if (item == null) return false;
        if (isLegacyUpgrade(item)) return true;

        return item != null && CompatibilityUtils.hasMetadata(item, metadataProvider, "upgrade");
    }

    public static boolean isLegacySpell(ItemStack item) {
        return item != null && InventoryUtils.hasMeta(item, "spell");
    }

	public static boolean isSpell(ItemStack item) {
		return item != null && (CompatibilityUtils.hasMetadata(item, metadataProvider, "spell") || isLegacySpell(item));
	}

    public static boolean isLegacyBrush(ItemStack item) {
        return item != null && InventoryUtils.hasMeta(item, "brush");
    }

	public static boolean isBrush(ItemStack item) {
		return item != null && (CompatibilityUtils.hasMetadata(item, metadataProvider, "brush") || isLegacyBrush(item));
	}

    public static String getLegacySpell(ItemStack item) {
        if (!isLegacySpell(item)) return null;

        Object spellNode = InventoryUtils.getNode(item, "spell");
        return InventoryUtils.getMeta(spellNode, "key");
    }
	
	public static String getSpell(ItemStack item) {
		if (!isSpell(item)) {
            if (isLegacySpell(item)) {
                return getLegacySpell(item);
            }
            return null;
        }

        return CompatibilityUtils.getMetadata(item, metadataProvider, "spell");
	}

	public static String getLegacyBrush(ItemStack item) {
		if (!isLegacyBrush(item)) return null;
		
		Object brushNode = InventoryUtils.getNode(item, "brush");
		return InventoryUtils.getMeta(brushNode, "key");
	}

    public static String getBrush(ItemStack item) {
        if (!isBrush(item)) {
            if (isLegacyBrush(item)) {
                return getLegacyBrush(item);
            }
            return null;
        }

        return CompatibilityUtils.getMetadata(item, metadataProvider, "brush");
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
		ItemMeta meta = itemStack.getItemMeta();
		String displayName;
		if (wand != null) {
			displayName = wand.getActiveWandName(spell);
		} else {
			displayName = getSpellDisplayName(spell, activeMaterial);
		}
		meta.setDisplayName(displayName);
		List<String> lore = new ArrayList<String>();
		addSpellLore(spell, lore, wand);
		if (isItem) {
			lore.add(ChatColor.YELLOW + Messages.get("wand.spell_item_description"));
		}
		meta.setLore(lore);
		itemStack.setItemMeta(meta);
        CompatibilityUtils.addGlow(itemStack);
		CompatibilityUtils.setMetadata(itemStack, metadataProvider, "spell", spell.getKey());
	}
	
	public static void updateBrushItem(ItemStack itemStack, String materialKey, Wand wand) {
		ItemMeta meta = itemStack.getItemMeta();
		String displayName = null;
		if (wand != null) {
			displayName = wand.getActiveWandName(materialKey);
		} else {
			displayName = MaterialBrush.getMaterialName(materialKey);
		}
		meta.setDisplayName(displayName);
		itemStack.setItemMeta(meta);
        CompatibilityUtils.setMetadata(itemStack, metadataProvider, "brush", materialKey);
	}

	@SuppressWarnings("deprecation")
	private void updateInventory() {
		if (mage == null) return;
		if (!isInventoryOpen()) return;
		Player player = mage.getPlayer();
		if (player == null) return;
		
		WandMode wandMode = getMode();
		if (wandMode == WandMode.INVENTORY) {
			if (!mage.hasStoredInventory()) return;
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

		// Put the wand in the player's active slot.
		playerInventory.setItem(currentSlot, item);

		// Set hotbar items from remaining list
		for (int hotbarSlot = 0; hotbarSlot < HOTBAR_SIZE; hotbarSlot++) {
			if (hotbarSlot != currentSlot) {
				ItemStack hotbarItem = hotbar.getItem(hotbarSlot);
				updateInventoryName(hotbarItem, true);
				playerInventory.setItem(hotbarSlot, hotbarItem);
			}
		}
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
			lore.add(ChatColor.GOLD + Messages.get("spell.brush"));
		}
		if (spell instanceof UndoableSpell && ((UndoableSpell)spell).isUndoable()) {
			lore.add(ChatColor.GRAY + Messages.get("spell.undoable"));
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
		if (!mage.hasStoredInventory()) return;
		
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
		saveState();
	}

	public static boolean isActive(Player player) {
		ItemStack activeItem = player.getInventory().getItemInHand();
		return isWand(activeItem);
	}

    public boolean enchant(int totalLevels) {
        return randomize(totalLevels, true);
    }

	protected boolean randomize(int totalLevels, boolean additive) {
        WandUpgradePath path = getPath();
		if (path == null) return false;

		int maxLevel = path.getMaxLevel();

        // Just a hard-coded sanity check
        totalLevels = Math.min(totalLevels, maxLevel * 50);

		int addLevels = Math.min(totalLevels, maxLevel);
        boolean modified = true;
		while (addLevels > 0 && modified) {
            WandLevel level = path.getLevel(addLevels);
            modified = level.randomizeWand(this, additive);
			totalLevels -= maxLevel;
			addLevels = Math.min(totalLevels, maxLevel);
			additive = true;
		}

        return modified;
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
        } catch (IllegalArgumentException ignore) {
            // the Wand constructor throws an exception on an unknown tempalte
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
		if (!isModifiable() || !other.isModifiable()) return false;
		
		boolean modified = false;
		
		if (other.costReduction > costReduction) { costReduction = other.costReduction; modified = true; sendAddMessage("wand.upgraded_property", getLevelString("wand.cost_reduction", costReduction)); }
		if (other.power > power) { power = other.power; modified = true; sendAddMessage("wand.upgraded_property", getLevelString("wand.power", power)); }
		if (other.damageReduction > damageReduction) { damageReduction = other.damageReduction; modified = true; sendAddMessage("wand.upgraded_property", getLevelString("wand.protection", damageReduction)); }
		if (other.damageReductionPhysical > damageReductionPhysical) { damageReductionPhysical = other.damageReductionPhysical; modified = true; sendAddMessage("wand.upgraded_property", getLevelString("wand.protection_physical", damageReductionPhysical)); }
		if (other.damageReductionProjectiles > damageReductionProjectiles) { damageReductionProjectiles = other.damageReductionProjectiles; modified = true; sendAddMessage("wand.upgraded_property", getLevelString("wand.protection_projectile", damageReductionProjectiles)); }
		if (other.damageReductionFalling > damageReductionFalling) { damageReductionFalling = other.damageReductionFalling; modified = true; sendAddMessage("wand.upgraded_property", getLevelString("wand.protection_falling", damageReductionFalling)); }
		if (other.damageReductionFire > damageReductionFire) { damageReductionFire = other.damageReductionFire; modified = true; sendAddMessage("wand.upgraded_property", getLevelString("wand.protection_fire", damageReductionFire)); }
		if (other.damageReductionExplosions > damageReductionExplosions) { damageReductionExplosions = other.damageReductionExplosions; modified = true; sendAddMessage("wand.upgraded_property", getLevelString("wand.protection_explosions", damageReductionExplosions)); }
		if (other.healthRegeneration > healthRegeneration) { healthRegeneration = other.healthRegeneration; modified = true; sendAddMessage("wand.upgraded_property", getLevelString("wand.health_regeneration", healthRegeneration)); }
		if (other.hungerRegeneration > hungerRegeneration) { hungerRegeneration = other.hungerRegeneration; modified = true; sendAddMessage("wand.upgraded_property", getLevelString("wand.hunger_regeneration", hungerRegeneration)); }
		if (other.speedIncrease > speedIncrease) { speedIncrease = other.speedIncrease; modified = true; sendAddMessage("wand.upgraded_property", getLevelString("wand.haste", speedIncrease)); }
		
		// Mix colors
		if (other.effectColor != null) {
			if (this.effectColor == null) {
				this.effectColor = other.effectColor;
			} else {
				this.effectColor = this.effectColor.mixColor(other.effectColor, other.effectColorMixWeight);
			}
			modified = true;
		}
		
		modified = modified | (!keep && other.keep);
		modified = modified | (!bound && other.bound);
		modified = modified | (!effectBubbles && other.effectBubbles);

		keep = keep || other.keep;
		bound = bound || other.bound;
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
		
		// Don't need mana if cost-free
		if (isCostFree()) {
			xpRegeneration = 0;
			xpMax = 0;
			xp = 0;
		} else {
			if (other.xpRegeneration > xpRegeneration) { xpRegeneration = other.xpRegeneration; modified = true; sendAddMessage("wand.upgraded_property", getLevelString("wand.mana_regeneration", xpRegeneration, WandLevel.maxXpRegeneration)); }
			if (other.xpMax > xpMax) { xpMax = other.xpMax; modified = true; sendAddMessage("wand.upgraded_property", getLevelString("wand.mana_amount", xpMax, WandLevel.maxMaxXp)); }
			if (other.xp > xp) { xp = other.xp; modified = true; }
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
	public void cycleInventory() {
		if (!hasInventory) {
			return;
		}
		if (isInventoryOpen()) {
			saveInventory();
			int inventoryCount = inventories.size();
			openInventoryPage = inventoryCount == 0 ? 0 : (openInventoryPage + 1) % inventoryCount;
			updateInventory();
			if (mage != null && inventories.size() > 1) {
				mage.playSound(Sound.CHEST_OPEN, 0.3f, 1.5f);
				mage.getPlayer().updateInventory();
			}
		}
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
			if (mage.hasStoredInventory()) return;
			if (mage.storeInventory()) {
				inventoryIsOpen = true;
				mage.playSound(Sound.CHEST_OPEN, 0.4f, 0.2f);
				updateInventory();
				mage.getPlayer().updateInventory();
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	public void closeInventory() {
		if (!isInventoryOpen()) return;
		saveInventory();
		inventoryIsOpen = false;
		if (mage != null) {
			mage.playSound(Sound.CHEST_CLOSE, 0.4f, 0.2f);
			if (getMode() == WandMode.INVENTORY) {
				mage.restoreInventory();
				Player player = mage.getPlayer();
				player.setItemInHand(item);
				player.updateInventory();
			} else {
				mage.getPlayer().closeInventory();
			}
		}
		saveState();
	}
	
	public boolean fill(Player player) {
		Collection<SpellTemplate> allSpells = controller.getPlugin().getSpellTemplates();

		for (SpellTemplate spell : allSpells)
		{
			if (spell.hasCastPermission(player) && spell.getIcon().getMaterial() != Material.AIR)
			{
				addSpell(spell.getKey());
			}
		}
		
		autoFill = false;
		saveState();
		
		// TODO: Detect changes
		return true;
	}

	public void activate(Mage mage, ItemStack wandItem) {
		if (mage == null || wandItem == null) return;

        if (this.isUpgrade) {
            controller.getLogger().warning("Activated an upgrade item- this shouldn't happen");
            return;
        }
		
		// Update held item, it may have been copied since this wand was created.
		this.item = wandItem;
		this.mage = mage;
		
		// Check for spell or other special icons in the player's inventory
		Player player = mage.getPlayer();
		boolean modified = false;
		ItemStack[] items = player.getInventory().getContents();
		for (int i = 0; i < items.length; i++) {
			ItemStack item = items[i];
			if (addItem(item)) {
				modified = true;
				items[i] = null;
			}
		}
		if (modified) {
			player.getInventory().setContents(items);
		}
		
		// Check for an empty wand and auto-fill
		if (!isUpgrade && (controller.fillWands() || autoFill)) {
			if (getSpells().size() == 0) {
				fill(mage.getPlayer());
			}
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
            }
		}
		
		checkActiveMaterial();
		
		saveState();
		
		mage.setActiveWand(this);
		if (usesMana()) {
			storedXpLevel = player.getLevel();
			storedXpProgress = player.getExp();
			storedXp = 0;
			updateMana();
		}
		updateActiveMaterial();
		updateName();
		updateLore();
		
		updateEffects();
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
		
		if (effectParticle != null && location != null) {
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
	
	protected void updateMana() {
		if (mage != null && xpMax > 0 && xpRegeneration > 0) {
			Player player = mage.getPlayer();
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
	
	public boolean isInventoryOpen() {
		return mage != null && inventoryIsOpen;
	}
	
	public void deactivate() {
		if (mage == null) return;
		saveState();

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
		
		// Extra just-in-case
		mage.restoreInventory();
		
		if (usesMana() && player != null) {
            player.setExp(storedXpProgress);
            player.setLevel(storedXpLevel);
            player.giveExp(storedXp);
			storedXp = 0;
			storedXpProgress = 0;
			storedXpLevel = 0;
		}
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
			if (spell.cast()) {
				Color spellColor = spell.getColor();
				if (spellColor != null && this.effectColor != null) {
					this.effectColor = this.effectColor.mixColor(spellColor, effectColorSpellMixWeight);
					// Note that we don't save this change.
					// The hope is that the wand will get saved at some point later
					// And we don't want to trigger NBT writes every spell cast.
					// And the effect color morphing isn't all that important if a few
					// casts get lost.
				}
				
				use();
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
				Player player = mage.getPlayer();
				mage.playSound(Sound.ITEM_BREAK, 1.0f, 0.8f);
				PlayerInventory playerInventory = player.getInventory();
				playerInventory.setItemInHand(new ItemStack(Material.AIR, 1));
				player.updateInventory();
				deactivate();
			} else {
				updateName();
				updateLore();
				saveState();
			}
		}
	}
	
	public void onPlayerExpChange(PlayerExpChangeEvent event) {
		if (mage == null) return;
		
		if (usesMana()) {
			storedXp += event.getAmount();
			event.setAmount(0);
		}
	}
	
	public void tick() {
		if (mage == null) return;
		
		Player player = mage.getPlayer();
		if (player == null) return;
		
		if (speedIncrease > 0) {
			int hasteLevel = (int)(speedIncrease * WandLevel.maxHasteLevel);
			if (hasteEffect == null || hasteEffect.getAmplifier() != hasteLevel) {
				hasteEffect = new PotionEffect(PotionEffectType.SPEED, 80, hasteLevel, true);
			}
			
			CompatibilityUtils.applyPotionEffect(player, hasteEffect);
		}
		if (healthRegeneration > 0) {
			int regenLevel = (int)(healthRegeneration * WandLevel.maxHealthRegeneration);
			if (healthRegenEffect == null || healthRegenEffect.getAmplifier() != regenLevel) {
				healthRegenEffect = new PotionEffect(PotionEffectType.REGENERATION, 80, regenLevel, true);
			}
			
			CompatibilityUtils.applyPotionEffect(player, healthRegenEffect);
		}
		if (hungerRegeneration > 0) {
			int regenLevel = (int)(hungerRegeneration * WandLevel.maxHungerRegeneration);
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
	}
	
	public MagicController getMaster() {
		return controller;
	}
	
	public void cycleSpells(ItemStack newItem) {
		if (isWand(newItem)) item = newItem;
		
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
	
	public void cycleMaterials(ItemStack newItem) {
		if (isWand(newItem)) item = newItem;
		
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
	
	public boolean hasExperience() {
		return xpRegeneration > 0;
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
			try {
				throw new Exception("Wand activated without holding a wand!");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return;
		}
		
		if (!canUse(player)) {
			mage.sendMessage(Messages.get("wand.bound").replace("$name", owner));
			player.setItemInHand(null);
			Location location = player.getLocation();
			location.setY(location.getY() + 1);
            controller.addLostWand(makeLost(location));
            saveState();
			Item droppedItem = player.getWorld().dropItemNaturally(location, item);
			Vector velocity = droppedItem.getVelocity();
			velocity.setY(velocity.getY() * 2 + 1);
			droppedItem.setVelocity(velocity);
			return;
		}

        id = null;
		
		if (mage instanceof Mage) {
			activate((Mage)mage, player.getItemInHand());
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
		
		// TODO: Detect changes
		return true;
	}

	@Override
	public boolean upgrade(Map<String, Object> properties) {
		Map<Object, Object> convertedProperties = new HashMap<Object, Object>(properties);
		loadProperties(ConfigurationUtils.toNodeList(convertedProperties), true);
		// TODO: Detect changes
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
		ItemStack spellItem = createSpellIcon(spellName);
		if (spellItem == null) {
			controller.getPlugin().getLogger().warning("Unknown spell: " + spellName);
			return false;
		}
		addToInventory(spellItem);
		updateInventory();
		hasInventory = getSpells().size() + getBrushes().size() > 1;
		updateLore();
		saveState();
		
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
		
		ItemStack itemStack = createBrushIcon(materialKey);
		if (itemStack == null) return false;
		
		addToInventory(itemStack);
		if (activeMaterial == null || activeMaterial.length() == 0) {
			setActiveBrush(materialKey);
		} else {
			updateInventory();
		}
		updateLore();
		saveState();
		hasInventory = getSpells().size() + getBrushes().size() > 1;
		
		return true;
	}
	
	@Override
	public void setActiveBrush(String materialKey) {
		this.activeMaterial = materialKey;
		updateName();
		updateActiveMaterial();
		updateInventory();
		saveState();
	}

	@Override
	public void setActiveSpell(String activeSpell) {
		this.activeSpell = activeSpell;
		updateName();
		updateInventory();
		saveState();
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
		updateName();
		updateLore();
		saveState();
		if (isInventoryOpen()) {
			updateInventory();
		}
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
		updateName();
		updateLore();
		saveState();
		updateInventory();
		
		return found;
	}
}
