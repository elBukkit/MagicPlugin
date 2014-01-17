package com.elmakers.mine.bukkit.plugins.magic;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

import com.elmakers.mine.bukkit.essentials.MagicItemDb;
import com.elmakers.mine.bukkit.essentials.Mailer;
import com.elmakers.mine.bukkit.plugins.magic.blocks.BlockBatch;
import com.elmakers.mine.bukkit.plugins.magic.blocks.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.populator.WandChestPopulator;
import com.elmakers.mine.bukkit.plugins.magic.wand.Wand;
import com.elmakers.mine.bukkit.plugins.magic.wand.WandLevel;
import com.elmakers.mine.bukkit.utilities.CSVParser;
import com.elmakers.mine.bukkit.utilities.InventoryUtils;
import com.elmakers.mine.bukkit.utilities.Messages;
import com.elmakers.mine.bukkit.utilities.SetActiveItemSlotTask;
import com.elmakers.mine.bukkit.utilities.URLMap;
import com.elmakers.mine.bukkit.utilities.UndoQueue;
import com.elmakers.mine.bukkit.utilities.borrowed.Configuration;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class MagicController implements Listener 
{
	public MagicController(final MagicPlugin plugin)
	{
		this.plugin = plugin;
	}
	
	/*
	 * Public API - Use for hooking up a plugin, or calling a spell
	 */

	public Mage getMage(Player player)
	{
		Mage mage = mages.get(player.getName());
		if (mage == null)
		{
			mage = new Mage(this, player);
			mages.put(player.getName(), mage);
		}

		mage.setPlayer(player);

		return mage;
	}
	
	public Mage getMage(String playerName) 
	{
		if (!mages.containsKey(playerName)) 
		{
			mages.put(playerName, new Mage(this, null));
		}
		
		return mages.get(playerName);
	}

	public void createSpell(Spell template, String name, Material icon, String description, String category, String parameterString)
	{
		createSpell(template, name, icon, description, category, parameterString, null, null);
	}

	public void createSpell(Spell template, String name, Material icon, String description, String category, String parameterString, String propertiesString)
	{
		createSpell(template, name, icon, description, category, parameterString, propertiesString, null);    
	}

	public void createSpell(Spell template, String name, Material icon, String description, String category, String parameterString, String propertiesString, String costsString)
	{
		ConfigurationNode spellNode = new ConfigurationNode();
		ConfigurationNode parameterNode = spellNode.createChild("parameters");
		ConfigurationNode propertiesNode = spellNode.createChild("properties");

		if (parameterString != null && parameterString.length() > 0)
		{
			String[] parameters = parameterString.split(" ");
			Spell.addParameters(parameters, parameterNode);
		}

		if (propertiesString != null && propertiesString.length() > 0)
		{
			String[] properties = propertiesString.split(" ");
			Spell.addParameters(properties, propertiesNode);
		}

		if (costsString != null && costsString.length() > 0)
		{
			List< Map<String, Object> > costs = new ArrayList< Map<String, Object> >();
			String[] costPairs = costsString.split(" ");
			for (int i = 0; i < costPairs.length - 1; i += 2)
			{
				try
				{
					int amount = Integer.parseInt(costPairs[i + 1]);
					Map<String, Object> cost = new HashMap<String, Object>();
					cost.put("material", costPairs[i]);
					cost.put("amount", amount);
					costs.add(cost);
				}
				catch(Exception ex)
				{

				}
			}

			spellNode.setProperty("costs", costs);
		}

		spellNode.setProperty("description", description);
		spellNode.setProperty("icon", icon);
		spellNode.setProperty("category", category);

		template.initialize(this);
		template.loadTemplate(name, spellNode);

		addSpell(template);
	}

	public void addSpell(Spell variant)
	{
		Spell conflict = spells.get(variant.getKey());
		if (conflict != null)
		{
			getLogger().log(Level.WARNING, "Duplicate spell name: '" + conflict.getKey() + "'");
		}
		else
		{
			spells.put(variant.getKey(), variant);
		}
	}

	/*
	 * Material use system
	 */

	public Set<Material> getBuildingMaterials()
	{
		return buildingMaterials;
	}

	public Set<Material> getDestructibleMaterials()
	{
		return destructibleMaterials;
	}

	public Set<Material> getTargetThroughMaterials()
	{
		return targetThroughMaterials;
	}
	
	public float getMaxDamagePowerMultiplier() {
		return maxDamagePowerMultiplier;
	}
	
	public float getMaxConstructionPowerMultiplier() {
		return maxConstructionPowerMultiplier;
	}
	
	public float getMaxRadiusPowerMultiplier() {
		return maxRadiusPowerMultiplier;
	}
	
	public float getMaxRangePowerMultiplier() {
		return maxRangePowerMultiplier;
	}
	
	/*
	 * Undo system
	 */

	public UndoQueue getUndoQueue(String playerName)
	{
		return getMage(playerName).getUndoQueue();
	}
	
	public int getUndoQueueDepth() {
		return undoQueueDepth;
	}

	public void addToUndoQueue(Player player, BlockList blocks)
	{
		addToUndoQueue(player.getName(), blocks);
	}

	public void addToUndoQueue(String playerName, BlockList blocks)
	{
		UndoQueue queue = getUndoQueue(playerName);
		queue.add(blocks);
	}

	public void scheduleCleanup(String playerName, BlockList blocks)
	{
		UndoQueue queue = getUndoQueue(playerName);
		queue.scheduleCleanup(this, blocks);
	}

	public boolean undoAny(Player player, Block target)
	{
		for (String playerName : mages.keySet())
		{
			UndoQueue queue = getMage(playerName).getUndoQueue();
			if (queue.undo(this, target))
			{
				if (!player.getName().equals(playerName))
				{
					player.sendMessage("Undid one of " + playerName + "'s spells");
				}
				return true;
			}
		}

		return false;
	}

	public boolean undo(String playerName)
	{
		UndoQueue queue = getUndoQueue(playerName);
		return queue.undo(this);
	}

	public boolean commit(String playerName)
	{
		UndoQueue queue = getUndoQueue(playerName);
		if (queue.getSize() == 0) return false;
		queue.commit();
		return true;
	}

	public boolean commitAll()
	{
		boolean undid = false;
		for (Mage mage : mages.values()) {
			UndoQueue queue = mage.getUndoQueue();
			if (queue.getSize() == 0) {
				undid = true;
				queue.commit();
			}
		}
		return undid;
	}

	public boolean undo(String playerName, Block target)
	{
		UndoQueue queue = getUndoQueue(playerName);
		return queue.undo(this, target);
	}

	public BlockList getLastBlockList(String playerName, Block target)
	{
		UndoQueue queue = getUndoQueue(playerName);
		return queue.getLast(target);
	}

	public BlockList getLastBlockList(String playerName)
	{
		UndoQueue queue = getUndoQueue(playerName);
		return queue.getLast();
	}

	/*
	 * Event registration- call to listen for events
	 */

	public void registerEvent(SpellEventType type, Spell spell)
	{
		Mage spells = getMage(spell.getPlayer());
		spells.registerEvent(type, spell);
	}

	public void unregisterEvent(SpellEventType type, Spell spell)
	{
		Mage spells = getMage(spell.getPlayer());
		spells.unregisterEvent(type, spell);
	}

	/*
	 * Random utility functions
	 */

	public boolean cancel(Player player)
	{
		Mage mage = getMage(player);
		return mage.cancel();
	}

	public String getMessagePrefix()
	{
		return messagePrefix;
	}

	public String getCastMessagePrefix()
	{
		return castMessagePrefix;
	}
	
	public boolean showCastMessages()
	{
		return showCastMessages;
	}

	public boolean showMessages()
	{
		return showMessages;
	}
	
	public int getMessageThrottle()
	{
		return messageThrottle;
	}

	public boolean soundsEnabled()
	{
		return soundsEnabled;
	}

	public boolean fillWands()
	{
		return fillWands;
	}

	public boolean isSolid(Material mat)
	{
		return (mat != Material.AIR && mat != Material.WATER && mat != Material.STATIONARY_WATER && mat != Material.LAVA && mat != Material.STATIONARY_LAVA);
	}

	public boolean isSticky(Material mat)
	{
		return stickyMaterials.contains(mat);
	}

	public boolean isStickyAndTall(Material mat)
	{
		return stickyMaterialsDoubleHeight.contains(mat);
	}

	public boolean isAffectedByGravity(Material mat)
	{
		// DOORS are on this list, it's a bit of a hack, but if you consider
		// them
		// as two separate blocks, the top one of which "breaks" when the bottom
		// one does,
		// it applies- but only really in the context of the auto-undo system,
		// so this should probably be its own mat list, ultimately.
		return (mat == Material.GRAVEL || mat == Material.SAND || mat == Material.WOOD_DOOR || mat == Material.IRON_DOOR);
	}

	/*
	 * Get the log, if you need to debug or log errors.
	 */
	public Logger getLogger()
	{
		return plugin.getLogger();
	}

	public MagicPlugin getPlugin()
	{
		return plugin;
	}

	public boolean isIndestructible(Player player, Location location) 
	{
		return isIndestructible(player, location.getBlock());
	}

	public boolean isIndestructible(Player player, Block block) 
	{
		// TODO: Player/wand-based overrides?
		return (indestructibleMaterials.contains(block.getType()));		
	}
	
	public boolean hasBuildPermission(Player player, Location location) 
	{
		return hasBuildPermission(player, location.getBlock());
	}

	public boolean hasBuildPermission(Player player, Block block) 
	{
		// Check the region manager.
		// TODO: We need to be able to do WG permission checks while a player is offline.
		if (regionManager == null || player == null) return true;
		
		try {
			Method canBuildMethod = regionManager.getClass().getMethod("canBuild", Player.class, Block.class);
			if (canBuildMethod != null) {
				return (Boolean)canBuildMethod.invoke(regionManager, player, block);
			}
		} catch (Throwable ex) {
		}
		
		return true;
	}
	
	/*
	 * Internal functions - don't call these, or really anything below here.
	 */
	
	/*
	 * Saving and loading
	 */

	public void initialize()
	{
		// Try to (dynamically) link to WorldGuard:
		try {
			regionManager = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
			Method canBuildMethod = regionManager.getClass().getMethod("canBuild", Player.class, Block.class);
			if (canBuildMethod != null) {
				getLogger().info("WorldGuard found, will respect build permissions for construction spells");
			} else {
				regionManager = null;
			}
		} catch (Throwable ex) {
		}
		
		if (regionManager == null) {
			getLogger().info("WorldGuard not found, not using a region manager.");
		}
		
		// Try to (dynamically) link to dynmap:
		try {
			Plugin dynmapPlugin = plugin.getServer().getPluginManager().getPlugin("dynmap");
			if (!(dynmapPlugin instanceof DynmapCommonAPI)) {
				throw new Exception("Dynmap plugin found, but class is not DynmapCommonAPI");
			}
			dynmap = (DynmapCommonAPI)dynmapPlugin;
		} catch (Throwable ex) {
			plugin.getLogger().warning(ex.getMessage());
		}
		
		if (regionManager == null) {
			getLogger().info("WorldGuard not found, not using a region manager.");
		}

		load();
		
		// Set up the PlayerSpells timer
		Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			public void run() {
				for (Mage spells : mages.values()) {
					spells.tick();
				}
			}
		}, 0, 20);

		// Set up the Block update timer
		Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			public void run() {
				int updated = 0;
				while (updated < maxBlockUpdates && pendingBatches.size() > 0) {
					BlockBatch batch = pendingBatches.getFirst();
					int batchUpdated = batch.process(maxBlockUpdates);
					updated += batchUpdated;
					if (batch.isFinished()) {
						pendingBatches.removeFirst();
					}
				}
			}
		}, 0, 1);
	}
	
	public void updateBlock(Block block)
	{
		updateBlock(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
	}
	
	public void updateBlock(String worldName, int x, int y, int z)
	{
		if (dynmap != null && dynmapUpdate)
		{
			dynmap.triggerRenderOfBlock(worldName, x, y, z);
		}
	}
	
	public void updateVolume(String worldName, int minx, int miny, int minz, int maxx, int maxy, int maxz)
	{
		if (dynmap != null && dynmapUpdate)
		{
			dynmap.triggerRenderOfVolume(worldName, minx, miny, minz, maxx, maxy, maxz);
		}
	}
	
	public boolean removeMarker(String id, String group)
	{
		boolean removed = false;
		if (dynmap != null && dynmapShowWands && dynmap.markerAPIInitialized()) 
		{
			MarkerAPI markers = dynmap.getMarkerAPI();
			MarkerSet markerSet = markers.getMarkerSet(group);
			if (markerSet != null) {
				Marker marker = markerSet.findMarker(id);
				if (marker != null) {
					removed = true;
					marker.deleteMarker();
				}
			}
		}
		
		return removed;
	}
	
	public boolean addMarker(String id, String group, String title, String world, int x, int y, int z, String description)
	{
		boolean created = false;
		if (dynmap != null && dynmapShowWands && dynmap.markerAPIInitialized())
		{
			MarkerAPI markers = dynmap.getMarkerAPI();
			MarkerSet markerSet = markers.getMarkerSet(group);
			if (markerSet == null) {
				markerSet = markers.createMarkerSet(group, "Wands", null, false);
			}
			MarkerIcon wandIcon = markers.getMarkerIcon("wand");
			if (wandIcon == null) {
				wandIcon = markers.createMarkerIcon("wand", "Wand", plugin.getResource("wand_icon32.png"));
			}
			
			Marker marker = markerSet.findMarker(id);
			if (marker == null) {
				created = true;
				marker = markerSet.createMarker(id, title, world, x, y, z, wandIcon, false);
			} else {
				marker.setLocation(world, x, y, z);
				marker.setLabel(title);
			}
			marker.setDescription(description);
		}
		
		return created;
	}
	
	public void addPendingBlockBatch(BlockBatch batch) {
		pendingBatches.addLast(batch);
	}

	public void load()
	{
		final File dataFolder = plugin.getDataFolder();
		dataFolder.mkdirs();
		
		// Load localizations
		Messages.reset();
		Messages.load(plugin);
		
		// Load main configuration
		File oldDefaults = new File(dataFolder, propertiesFileNameDefaults);
		oldDefaults.delete();
		getLogger().info("Overwriting file " + propertiesFileNameDefaults);
		plugin.saveResource(propertiesFileNameDefaults, false);
		File propertiesFile = new File(dataFolder, propertiesFileName);
		if (!propertiesFile.exists())
		{
			getLogger().info("Loading defaults from: " + propertiesFileNameDefaults);
			loadProperties(plugin.getResource(propertiesFileNameDefaults));
		} else {
			getLogger().info("Loading customizations from: " + propertiesFile.getName());
			loadProperties(propertiesFile);
		}

		// Load spells
		oldDefaults = new File(dataFolder, spellsFileNameDefaults);
		oldDefaults.delete();
		getLogger().info("Overwriting file " + spellsFileNameDefaults);
		plugin.saveResource(spellsFileNameDefaults, false);
		File spellsFile = new File(dataFolder, spellsFileName);
		if (!spellsFile.exists())
		{
			getLogger().info("Loading default spells from: " + spellsFileNameDefaults);
			load(plugin.getResource(spellsFileNameDefaults));
		} else {
			getLogger().info("Loading spells from: " + spellsFile.getName());
			load(spellsFile);
		}

		// Load player data
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				File playersFile = new File(dataFolder, playersFileName);
				if (playersFile.exists())
				{
					getLogger().info("Loading player data from file " + playersFile.getName());
					Configuration playerConfiguration = new Configuration(playersFile);
					playerConfiguration.load();
					List<String> playerNames = playerConfiguration.getKeys();
					for (String playerName : playerNames) {
						getMage(playerName).load(playerConfiguration.getNode(playerName));
					}
				}
			}
		}, 5);
	
		// Load URL map data
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				URLMap.resetAll();
				URLMap.load(plugin);
			}
		}, 20);
		
		// Load wand templates
		Wand.load(plugin);

		getLogger().info("Magic: Loaded " + spells.size() + " spells and " + Wand.getWandTemplates().size() + " wands");
	}
	
	public void save()
	{
		File dataFolder = plugin.getDataFolder();
		dataFolder.mkdirs();
		
		File playersFile = new File(dataFolder, playersFileName);
		Configuration playerConfiguration = new Configuration(playersFile);
		for (Entry<String, Mage> spellsEntry : mages.entrySet()) {
			ConfigurationNode playerNode = playerConfiguration.createChild(spellsEntry.getKey());
			spellsEntry.getValue().save(playerNode);
		}
		playerConfiguration.save();
	}

	protected void load(File spellsFile)
	{
		load(new Configuration(spellsFile));
	}

	protected void load(InputStream spellsConfig)
	{
		load(new Configuration(spellsConfig));
	}
	
	protected void load(Configuration config)
	{
		config.load();

		ConfigurationNode spellsNode = config.getNode("spells");
		if (spellsNode == null) return;

		List<String> spellKeys = spellsNode.getKeys();
		for (String key : spellKeys)
		{
			ConfigurationNode spellNode = spellsNode.getNode(key);
			Spell newSpell = Spell.loadSpell(key, spellNode, this);
			if (newSpell == null)
			{
				getLogger().warning("Magic: Error loading spell " + key);
				continue;
			}
			addSpell(newSpell);
		}
	}

	protected void loadProperties(File propertiesFile)
	{
		loadProperties(new Configuration(propertiesFile));
	}
	
	protected void loadProperties(InputStream properties)
	{
		loadProperties(new Configuration(properties));
	}
	
	protected void loadProperties(Configuration properties)
	{
		properties.load();

		ConfigurationNode generalNode = properties.getNode("general");
		undoQueueDepth = generalNode.getInteger("undo_depth", undoQueueDepth);
		wandCycling = generalNode.getBoolean("right_click_cycles", wandCycling);
		showMessages = generalNode.getBoolean("show_messages", showMessages);
		showCastMessages = generalNode.getBoolean("show_cast_messages", showCastMessages);
		messagePrefix = generalNode.getString("message_prefix", messagePrefix);
		castMessagePrefix = generalNode.getString("cast_message_prefix", castMessagePrefix);
		clickCooldown = generalNode.getInt("click_cooldown", clickCooldown);
		messageThrottle = generalNode.getInt("message_throttle", 0);
		maxBlockUpdates = generalNode.getInt("max_block_updates", maxBlockUpdates);
		soundsEnabled = generalNode.getBoolean("sounds", soundsEnabled);
		fillWands = generalNode.getBoolean("fill_wands", fillWands);
		indestructibleWands = generalNode.getBoolean("indestructible_wands", indestructibleWands);
		keepWandsOnDeath = generalNode.getBoolean("keep_wands_on_death", keepWandsOnDeath);
		maxDamagePowerMultiplier = (float)generalNode.getDouble("max_power_damage_multiplier", maxDamagePowerMultiplier);
		maxConstructionPowerMultiplier = (float)generalNode.getDouble("max_power_construction_multiplier", maxConstructionPowerMultiplier);
		maxRangePowerMultiplier = (float)generalNode.getDouble("max_power_range_multiplier", maxRangePowerMultiplier);
		maxRadiusPowerMultiplier = (float)generalNode.getDouble("max_power_radius_multiplier", maxRadiusPowerMultiplier);
		castCommandCostReduction = (float)generalNode.getDouble("cast_command_cost_reduction", castCommandCostReduction);
		castCommandCooldownReduction = (float)generalNode.getDouble("cast_command_cooldown_reduction", castCommandCooldownReduction);
		blockPopulatorEnabled = generalNode.getBoolean("enable_block_populator", blockPopulatorEnabled);
		enchantingEnabled = generalNode.getBoolean("enable_enchanting", enchantingEnabled);
		combiningEnabled = generalNode.getBoolean("enable_combining", combiningEnabled);
		organizingEnabled = generalNode.getBoolean("enable_organizing", organizingEnabled);
		dynmapShowWands = generalNode.getBoolean("dynamp_show_wands", dynmapShowWands);
		dynmapUpdate = generalNode.getBoolean("dynmap_update", dynmapUpdate);
		blockPopulatorConfig = generalNode.getNode("populate_chests");

		buildingMaterials = generalNode.getMaterials("building", DEFAULT_BUILDING_MATERIALS);
		indestructibleMaterials = generalNode.getMaterials("indestructible", DEFAULT_INDESTRUCTIBLE_MATERIALS);
		destructibleMaterials = generalNode.getMaterials("destructible", DEFAULT_DESTRUCTIBLE_MATERIALS);
		targetThroughMaterials = generalNode.getMaterials("target_through", DEFAULT_TARGET_THROUGH_MATERIALS);

		CSVParser csv = new CSVParser();
		stickyMaterials = csv.parseMaterials(STICKY_MATERIALS);
		stickyMaterialsDoubleHeight = csv.parseMaterials(STICKY_MATERIALS_DOUBLE_HEIGHT);
		
		// Parse wand settings
		Wand.WandMaterial = generalNode.getMaterial("wand_item", Wand.WandMaterial);
		Wand.CopyMaterial = generalNode.getMaterial("copy_item", Wand.CopyMaterial);
		Wand.EraseMaterial = generalNode.getMaterial("erase_item", Wand.EraseMaterial);
		Wand.CloneMaterial = generalNode.getMaterial("clone_item", Wand.CloneMaterial);
		Wand.EnchantableWandMaterial = generalNode.getMaterial("wand_item_enchantable", Wand.EnchantableWandMaterial);

		// Parse crafting recipe settings
		boolean craftingEnabled = generalNode.getBoolean("enable_crafting", false);
		if (craftingEnabled) {
			recipeOutputTemplate = generalNode.getString("crafting_output", recipeOutputTemplate);
			wandRecipeUpperMaterial = generalNode.getMaterial("crafting_material_upper", Material.DIAMOND);
			wandRecipeLowerMaterial = generalNode.getMaterial("crafting_material_lower", Material.BLAZE_ROD);
			Wand wand = new Wand(this);
			ShapedRecipe recipe = new ShapedRecipe(wand.getItem());
			recipe.shape("o", "i").
					setIngredient('o', wandRecipeUpperMaterial).
					setIngredient('i', wandRecipeLowerMaterial);
			wandRecipe = recipe;
		}
		
		// Try to link to Essentials:
		Object essentials = plugin.getServer().getPluginManager().getPlugin("Essentials");
		if (essentials != null) {
			mailer = new Mailer(essentials);
		}
		
		if (generalNode.getBoolean("enable_essentials_signs", false)) {
			final MagicController me = this;
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					try {
						Object essentials = me.plugin.getServer().getPluginManager().getPlugin("Essentials");
						if (essentials != null) {
							Class<?> essentialsClass = essentials.getClass();
							Field itemDbField = essentialsClass.getDeclaredField("itemDb");
							itemDbField.setAccessible(true);
							Object oldEntry = itemDbField.get(essentials);
							if (oldEntry instanceof MagicItemDb) {
								getLogger().info("Essentials integration already set up, skipping");
								return;
							}
							if (!oldEntry.getClass().getName().equals("com.earth2me.essentials.ItemDb")){
								getLogger().info("Essentials Item DB class unexepcted: " + oldEntry.getClass().getName() + ", skipping integration");
								return;
							}
							Object newEntry = new MagicItemDb(me, essentials);
							itemDbField.set(essentials, newEntry);
							Field confListField = essentialsClass.getDeclaredField("confList");
							confListField.setAccessible(true);
							@SuppressWarnings("unchecked")
							List<Object> confList = (List<Object>)confListField.get(essentials);
							confList.remove(oldEntry);
							confList.add(newEntry);
							getLogger().info("Essentials found, hooked up custom item handler");
						}
					} catch (Throwable ex) {
						ex.printStackTrace();
					}
				}
			}, 5);
		}
	}

	protected void clear()
	{
		mages.clear();
		spells.clear();
	}

	public List<Spell> getAllSpells()
	{
		List<Spell> allSpells = new ArrayList<Spell>();
		allSpells.addAll(spells.values());
		return allSpells;
	}
	
	protected boolean allowPhysics(Block block)
	{
		if (physicsDisableTimeout == 0)
			return true;
		if (System.currentTimeMillis() > physicsDisableTimeout)
			physicsDisableTimeout = 0;
		return false;
	}

	public void disablePhysics(int interval)
	{
		physicsDisableTimeout = System.currentTimeMillis() + interval;
	}

	public boolean hasWandPermission(Player player)
	{
		return hasPermission(player, "Magic.wand.use", true);
	}

	public boolean hasPermission(Player player, String pNode, boolean defaultValue)
	{
		boolean isSet = player.isPermissionSet(pNode);
		return isSet ? player.hasPermission(pNode) : defaultValue;
	}

	public boolean hasPermission(Player player, String pNode)
	{
		return hasPermission(player, pNode, false);
	}
	
	public boolean hasPermission(CommandSender sender, String pNode)
	{
		if (!(sender instanceof Player)) return true;
		return hasPermission((Player)sender, pNode, false);
	}
	
	public boolean hasPermission(CommandSender sender, String pNode, boolean defaultValue)
	{
		if (!(sender instanceof Player)) return true;
		return hasPermission((Player)sender, pNode, defaultValue);
	}

	/*
	 * Listeners / callbacks
	 */
	@EventHandler
	public void onContainerClick(InventoryDragEvent event) {
		// this is a huge hack! :\
		// I apologize for any weird behavior this causes.
		// Bukkit, unfortunately, will blow away NBT data for anything you drag
		// Which will nuke a wand or spell.
		// To make matters worse, Bukkit passes a copy of the item in the event, so we can't 
		// even check for metadata and only cancel the event if it involves one of our special items.
		// The best I can do is look for metadata at all, since Bukkit will retain the name and lore.
		ItemStack oldStack = event.getOldCursor();
		if (oldStack != null && oldStack.hasItemMeta()) {
			event.setCancelled(true);
			return;
		}
	}
	
	@SuppressWarnings("deprecation")
	protected void onPlayerActivateIcon(Mage mage, Wand activeWand, ItemStack icon)
	{
		// Check for spell or material selection
		if (icon != null && icon.getType() != Material.AIR) {
			Spell spell = mage.getSpell(Wand.getSpell(icon));
			if (spell != null) {
				mage.cancel();
				activeWand.setActiveSpell(spell.getKey());
			} else {
				Material material = icon.getType();
				if (buildingMaterials.contains(material) || material == Wand.EraseMaterial || material == Wand.CopyMaterial) {
					activeWand.setActiveMaterial(material, icon.getData().getData());
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerEquip(PlayerItemHeldEvent event)
	{
		Player player = event.getPlayer();
		PlayerInventory inventory = player.getInventory();
		ItemStack next = inventory.getItem(event.getNewSlot());
		ItemStack previous = inventory.getItem(event.getPreviousSlot());

		Mage mage = getMage(player);
		Wand activeWand = mage.getActiveWand();
		
		// Check for active Wand
		if (activeWand != null && Wand.isWand(previous)) {
			// If the wand inventory is open, we're going to let them select a spell or material
			if (activeWand.isInventoryOpen()) {
				// Update the wand item, Bukkit has probably made a copy
				activeWand.setItem(previous);
				
				// Check for spell or material selection
				onPlayerActivateIcon(mage, activeWand, next);
				
				// Cancelling the event causes some name bouncing. Trying out just resetting the item slot in a tick.
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new SetActiveItemSlotTask(player, event.getPreviousSlot()), 1);
				return;	
			} else {
				// Otherwise, we're switching away from the wand, so deactivate it.
				activeWand.deactivate();
			}
		}
		
		// If we're switching to a wand, activate it.
		if (next != null && Wand.isWand(next)) {
			Wand newWand = new Wand(this, next);
			newWand.activate(mage);
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event)
	{
		Mage spells = getMage(event.getPlayer());
		spells.onPlayerMove(event);
	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent event)
	{
		if (event.getEntityType() == EntityType.PLAYER && event.getEntity() instanceof Player) {
			onPlayerDeath((Player)event.getEntity(), event);
		}
	}

	protected void onPlayerDeath(final Player player, EntityDeathEvent event)
	{
		String rule = player.getWorld().getGameRuleValue("keepInventory");
		if (rule.equals("true")) return;
		
		Mage mage = getMage(player);
		List<ItemStack> drops = event.getDrops();
		Wand wand = mage.getActiveWand();
		if (wand != null) {
			// Retrieve stored inventory before deactiavting the wand
			if (mage.hasStoredInventory()) {
				drops.clear();

				ItemStack[] stored = mage.getStoredInventory().getContents();
				
				// Deactivate the wand.
				wand.deactivate();
	
				for (ItemStack stack : stored) {
					if (stack != null) {
						drops.add(stack);
					}
				}
				
				// Drop armor also
				ItemStack[] armor = player.getInventory().getArmorContents();
				for (ItemStack stack : armor) {
					if (stack != null) {
						drops.add(stack);
					}
				}
			} else {
				wand.deactivate();
			}
		}
		
		if (keepWandsOnDeath)
		{
			List<ItemStack> oldDrops = new ArrayList<ItemStack>(drops);
			final List<ItemStack> droppedWands = new ArrayList<ItemStack>();
			drops.clear();
			for (ItemStack itemStack : oldDrops)
			{
				if (Wand.isWand(itemStack))
				{
					droppedWands.add(itemStack);
				}
				else
				{
					drops.add(itemStack);
				}
			}
			if (droppedWands.size() > 0)
			{
				Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
					public void run() {
						for (ItemStack itemStack : droppedWands)
							player.getInventory().addItem(itemStack);
						}
					}
				, 5);
			}
		}

		mage.onPlayerDeath(event);
	}

	public void onPlayerDamage(Player player, EntityDamageEvent event)
	{
		Mage spells = getMage(player);
		spells.onPlayerDamage(event);
	}
	
	@EventHandler
	public void onEntityCombust(EntityCombustEvent event)
	{
		if (!(event.getEntity() instanceof Player)) return;
		Mage spells = getMage((Player)event.getEntity());
		spells.onPlayerCombust(event);
	}
	
	@EventHandler
	public void onItemDespawn(ItemDespawnEvent event)
	{
		if ((indestructibleWands || dynmapShowWands) && Wand.isWand(event.getEntity().getItemStack()))
		{
			if (indestructibleWands) {
				event.getEntity().setTicksLived(1);
				event.setCancelled(true);
			} else if (dynmapShowWands) {
				Wand wand = new Wand(this, event.getEntity().getItemStack());
				if (removeMarker("wand-" + wand.getId(), "Wands")) {
					getLogger().info("Wand despawned, removed from map");
				}
			}
		}
	}
	
	@EventHandler
	public void onItemSpawn(ItemSpawnEvent event)
	{
		if ((indestructibleWands || dynmapShowWands) && Wand.isWand(event.getEntity().getItemStack()))
		{
			if (indestructibleWands) {
				InventoryUtils.setInvulnerable(event.getEntity());
			}
			if (dynmapShowWands) {
				Wand wand = new Wand(this, event.getEntity().getItemStack());
				if (wand != null) {
					addWandMarker(wand, event.getEntity().getLocation());
				}
			}
		}
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event)
	{
		Entity entity = event.getEntity();
		if (entity instanceof Player)
		{
			Player player = (Player)event.getEntity();
			onPlayerDamage(player, event);
		}
        if (entity instanceof Item && (indestructibleWands || dynmapShowWands))
        {
   		 	Item item = (Item)entity;
   		 	ItemStack itemStack = item.getItemStack();
            if (Wand.isWand(itemStack))
            {
            	if (indestructibleWands) {
                     event.setCancelled(true);
            	} else if(dynmapShowWands && event.getDamage() >= itemStack.getDurability()) {
                	Wand wand = new Wand(this, item.getItemStack());
                	if (removeMarker("wand-" + wand.getId(), "Wands")) {
                		plugin.getLogger().info("Wand destroyed, removed from map");
                	}
                }
			}  
        }
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();		
		Mage mage = getMage(player);
		if (!mage.checkLastClick(clickCooldown)) {
			return;
		}
		
		Wand wand = mage.getActiveWand();
		
		if (wand == null && Wand.hasActiveWand(player)) {
			if (mage.hasStoredInventory()) {
				mage.restoreInventory();
			}
			wand = Wand.getActiveWand(this, player);
			wand.activate(mage);
		}
		
		// Another hacky double-check for wands getting accidentally deactivated?
		if (wand == null && mage.hasStoredInventory())
		{
			mage.restoreInventory();
			return;
		}
		
		if (wand == null || !hasWandPermission(player))
		{
			return;
		}
		
		// An extra double-check for a bug that is hard to reproduce, where the wand is no
		// longer the active item, but we never deactivated it.
		if (!Wand.hasActiveWand(player)) 
		{
			wand.deactivate();
		}
		
		if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)
		{
			wand.cast();
		}
		boolean toggleInventory = (event.getAction() == Action.RIGHT_CLICK_AIR);
		if (!toggleInventory && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Material material = event.getClickedBlock().getType();
			toggleInventory = !(material == Material.CHEST || material == Material.WOODEN_DOOR 
					|| material == Material.IRON_DOOR_BLOCK || material == Material.ENDER_CHEST
					|| material == Material.ANVIL || material == Material.BREWING_STAND || material == Material.ENCHANTMENT_TABLE
					|| material == Material.STONE_BUTTON || material == Material.LEVER || material == Material.FURNACE
					|| material == Material.BED || material == Material.SIGN_POST || material == Material.COMMAND || material == Material.WALL_SIGN);
			
			// This is to prevent Essentials signs from giving you an item in your wand inventory.
			if (material== Material.SIGN_POST || material == Material.WALL_SIGN) {
				wand.closeInventory();
			}
		}
		if (toggleInventory)
		{
			// Check for spell cancel first, e.g. fill or force
			if (!mage.cancel()) {
				
				// Check for wand cycling
				if (wandCycling) {
					if (player.isSneaking()) {
						Spell activeSpell = wand.getActiveSpell();
						if (activeSpell != null && activeSpell.usesMaterial() && !activeSpell.hasMaterialOverride() && wand.getMaterialNames().size() > 0) {
							wand.cycleMaterials();
						} else {
							wand.cycleSpells();
						}
					} else { 
						wand.cycleSpells();
					}
				} else {
					if (wand.getHasInventory()) {
						wand.toggleInventory();
					}
				}
			} else {
				mage.playSound(Sound.NOTE_BASS, 1.0f, 0.7f);
			}
		}
	}

	@EventHandler
	public void onBlockPhysics(BlockPhysicsEvent event)
	{
		if (!allowPhysics(event.getBlock()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		// Check for wand re-activation.
		Player player = event.getPlayer();
		Mage mage = getMage(player);
		Wand wand = Wand.getActiveWand(this, player);
		if (wand != null) {
			wand.activate(mage);
		}
	}
	
	@EventHandler
	public void onPlayerExpChange(PlayerExpChangeEvent event)
	{
		// We don't care about exp loss events
		if (event.getAmount() <= 0) return;
		
		Player player = event.getPlayer();
		Mage mage = getMage(player);
		Wand wand = mage.getActiveWand();
		if (wand != null) {
			wand.onPlayerExpChange(event);
		}
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();
		
		// Make sure they get their portraits back right away on relogin.
		URLMap.resend(player.getName());
		
		Mage mage = getMage(player);
		Wand wand = mage.getActiveWand();
		if (wand != null) {
			wand.deactivate();
		}
		
		// Just in case...
		mage.restoreInventory();
		
		mage.onPlayerQuit(event);
		
		// Let the GC collect these
		mage.setActiveWand(null);
		mage.setPlayer(null);
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPluginDisable(PluginDisableEvent event)
	{
		for (Mage spells : mages.values()) {
			Player player = spells.getPlayer();
			if (player == null) continue;
			
			Wand wand = spells.getActiveWand();
			if (wand != null) {
				wand.deactivate();
			}
			spells.restoreInventory();
			player.updateInventory();
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPluginEnable(PluginEnableEvent event)
	{
		Player[] players = plugin.getServer().getOnlinePlayers();
		for (Player player : players) {
			Wand wand = Wand.getActiveWand(this, player);
			if (wand != null) {
				Mage spells = getMage(player);
				wand.activate(spells);
				player.updateInventory();
			}
		}
		
		// Add our custom recipe if crafting is enabled
		if (wandRecipe != null) {
			plugin.getServer().addRecipe(wandRecipe);
		}
	}
	
	@EventHandler
	public void onPrepareCraftItem(PrepareItemCraftEvent event) 
	{
		Recipe recipe = event.getRecipe();
		if (wandRecipe != null && recipe.getResult().getType() == Wand.WandMaterial) {
			// Verify that this was our recipe
			// Just in case something else can craft our base material (e.g. stick)
			Inventory inventory = event.getInventory();
			if (!inventory.contains(wandRecipeLowerMaterial) || !inventory.contains(wandRecipeUpperMaterial)) {
				return;
			}
			
			Wand wand = Wand.createWand(this, recipeOutputTemplate);
			if (wand == null) {
				wand = new Wand(this);
			}
			event.getInventory().setResult(wand.getItem());
		}
	}
	
	@EventHandler
	public void onCraftItem(CraftItemEvent event) {
		if (!(event.getWhoClicked() instanceof Player)) return;
		
		Player player = (Player)event.getWhoClicked();
		Mage spells = getMage(player);
		
		// Don't allow crafting in the wand inventory.
		if (spells.hasStoredInventory()) {
			event.setCancelled(true); 
			return;
		}
	}

	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent event) {
		if (!(event.getPlayer() instanceof Player)) return;
		
		Player player = (Player)event.getPlayer();
		Mage mage = getMage(player);
		Wand wand = mage.getActiveWand();
		if (wand != null) {
			// NOTE: This never actually happens, unfortunately opening the player's inventory is client-side.
			if (event.getView().getType() == InventoryType.CRAFTING) {
				wand.updateInventoryNames(false);
			} else {
				wand.deactivate();
			}
		}
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player)) return;
		
		// log.info("CLICK: " + event.getAction() + " on " + event.getSlotType() + " in "+ event.getInventory().getType());
	
		if (event.getInventory().getType() == InventoryType.ENCHANTING)
		{
			SlotType slotType = event.getSlotType();
			if (slotType == SlotType.CRAFTING) {
				ItemStack cursor = event.getCursor();
				ItemStack current = event.getCurrentItem();
				
				// Make wands into an enchantable item when placing
				if (Wand.isWand(cursor)) {
					Wand wand = new Wand(this, cursor);
					if (wand.isModifiable()) {
						wand.makeEnchantable(true);
					}
				}
				// And turn them back when taking out
				if (Wand.isWand(current)) {
					Wand wand = new Wand(this, current);
					if (wand.isModifiable()) {
						wand.makeEnchantable(false);
					}
				}
				return;
			}
		}
		if (event.getInventory().getType() == InventoryType.ANVIL)
		{
			SlotType slotType = event.getSlotType();
			ItemStack cursor = event.getCursor();
			ItemStack current = event.getCurrentItem();
			Inventory anvilInventory = event.getInventory();
			
			// Set/unset active names when starting to craft
			if (slotType == SlotType.CRAFTING) {
				// Putting a wand into the anvil's crafting slot
				if (Wand.isWand(cursor)) {
					Wand wand = new Wand(this, cursor);
					wand.updateName(false);
				} 
				// Taking a wand out of the anvil's crafting slot
				if (Wand.isWand(current)) {
					Wand wand = new Wand(this, current);
					wand.updateName(true);
				}
				
				return;
			}
			
			// Rename wand when taking from result slot
			if (slotType == SlotType.RESULT && Wand.isWand(current)) {
				ItemMeta meta = current.getItemMeta();
				String newName = meta.getDisplayName();
				Wand wand = new Wand(this, current);
				Player player = (Player)event.getWhoClicked();
				wand.takeOwnership(player, newName, true);
				wand.organizeInventory();
				return;
			}

			if (combiningEnabled && slotType == SlotType.RESULT) {
				// Check for wands in both slots
				// ...... arg. So close.. and yet, not.
				// I guess I need to wait for the long-awaited anvil API?
				ItemStack firstItem = anvilInventory.getItem(0);
				ItemStack secondItem = anvilInventory.getItem(1);
				if (Wand.isWand(firstItem) && Wand.isWand(secondItem)) 
				{
					Wand firstWand = new Wand(this, firstItem);
					Wand secondWand = new Wand(this, secondItem);
					Player player = (Player)event.getWhoClicked();
					if (!firstWand.isModifiable() || !secondWand.isModifiable()) {
						player.sendMessage("One of your wands can not be combined");
						return;
					}
					// TODO: Can't get the anvil's text from here.
					firstWand.takeOwnership(player, firstWand.getName(), true);
					firstWand.add(secondWand);
					anvilInventory.setItem(0,  null);
					anvilInventory.setItem(1,  null);
					cursor.setType(Material.AIR);

					firstWand.organizeInventory();
					player.getInventory().addItem(firstWand.getItem());
					player.sendMessage("Your wands have been combined!");
					
					// This seems to work in the debugger, but.. doesn't do anything.
					// InventoryUtils.setInventoryResults(anvilInventory, newWand.getItem());
				} else if (Wand.isWand(firstItem)) {
					Wand firstWand = new Wand(this, firstItem);
					Player player = (Player)event.getWhoClicked();
					// TODO: Can't get the anvil's text from here.
					anvilInventory.setItem(0,  null);
					anvilInventory.setItem(1,  null);
					cursor.setType(Material.AIR);

					firstWand.organizeInventory();
					player.getInventory().addItem(firstWand.getItem());
					player.sendMessage("Your wand has been organized!");
				}
				
				return;
			}
		}
		
		// Check for wand cycling with active inventory
		if (event.getInventory().getType() == InventoryType.CRAFTING) {
			Player player = (Player)event.getWhoClicked();
			Mage mage = getMage(player);
			Wand wand = mage.getActiveWand();
			if (wand != null && wand.isInventoryOpen()) {
				if (event.getAction() == InventoryAction.PICKUP_HALF || event.getAction() == InventoryAction.NOTHING) {
					wand.cycleInventory();
					event.setCancelled(true);
					return;
				}
				
				if (event.getSlotType() == SlotType.ARMOR) {
					event.setCancelled(true);
					return;
				}
				
				if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
					ItemStack clickedItem = event.getCurrentItem();
					if (clickedItem != null) {
						boolean quickActivate = event.getSlot() >= Wand.hotbarSize;
						
						// Kind of hacky, but prevents players stealing armor
						Material material = clickedItem.getType();
						if (material == Material.SKULL_ITEM || material == Material.LEATHER_BOOTS || material == Material.LEATHER_LEGGINGS  || material == Material.LEATHER_CHESTPLATE || material == Material.LEATHER_HELMET 
							|| material == Material.LEATHER_BOOTS || material == Material.LEATHER_LEGGINGS  || material == Material.LEATHER_CHESTPLATE || material == Material.LEATHER_HELMET
							|| material == Material.GOLD_BOOTS || material == Material.GOLD_LEGGINGS  || material == Material.GOLD_CHESTPLATE || material == Material.GOLD_HELMET
							|| material == Material.IRON_BOOTS || material == Material.IRON_LEGGINGS  || material == Material.IRON_CHESTPLATE || material == Material.IRON_HELMET
							|| material == Material.DIAMOND_BOOTS || material == Material.DIAMOND_LEGGINGS  || material == Material.DIAMOND_CHESTPLATE || material == Material.DIAMOND_HELMET
						) {
							quickActivate = true;
						}
						
						if (quickActivate) {
							onPlayerActivateIcon(mage, wand, clickedItem);
							player.closeInventory();
							event.setCancelled(true);
						}
						return;
					}
				}
			}
			
			return;
		}
	}

	@EventHandler
	public void onInventoryClosed(InventoryCloseEvent event) {
		if (!(event.getPlayer() instanceof Player)) return;

		// Update the active wand, it may have changed around
		Player player = (Player)event.getPlayer();
		Mage mage = getMage(player);
		
		// Save the inventory state the the current wand if its spell inventory is open
		// This is just to make sure we don't lose changes made to the inventory
		Wand previousWand = mage.getActiveWand();
		if (previousWand != null && previousWand.isInventoryOpen()) {
			previousWand.saveInventory();
		}
		
		Wand wand = Wand.getActiveWand(this, player);
		boolean changedWands = false;
		if (previousWand != null && wand == null) changedWands = true;
		if (previousWand == null && wand != null) changedWands = true;
		if (previousWand != null && wand != null && !previousWand.equals(wand)) changedWands = true;
		if (changedWands) {
			if (previousWand != null) {
				previousWand.deactivate();
			}
			if (wand != null) {
				wand.activate(mage);
			}
		}
		
		if (wand != null && !changedWands) {
			wand.updateInventoryNames(true);
		}
	}

	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event)
	{
		Mage spells = getMage(event.getPlayer());
		ItemStack pickup = event.getItem().getItemStack();
		if (dynmapShowWands && Wand.isWand(pickup)) {
			Wand wand = new Wand(this, pickup);
			removeMarker("wand-" + wand.getId(), "Wands");
		}
		if (spells.hasStoredInventory()) {
			event.setCancelled(true);   		
			if (spells.addToStoredInventory(event.getItem().getItemStack())) {
				event.getItem().remove();
			}
		} else {
			// Hackiness needed because we don't get an equip event for this!
			PlayerInventory inventory = event.getPlayer().getInventory();
			ItemStack inHand = inventory.getItemInHand();
			if (Wand.isWand(pickup) && (inHand == null || inHand.getType() == Material.AIR)) {
				Wand wand = new Wand(this, pickup);
				event.setCancelled(true);
				event.getItem().remove();
				inventory.setItem(inventory.getHeldItemSlot(), pickup);
				wand.activate(spells);
			} 
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event)
	{
		Player player = event.getPlayer();
		Mage spells = getMage(player);
		if (spells.hasStoredInventory() || spells.getBlockPlaceTimeout() > System.currentTimeMillis()) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event)
	{
		Player player = event.getPlayer();
		Mage spells = getMage(player);
		Wand activeWand = spells.getActiveWand();
		if (activeWand != null) {
			ItemStack inHand = event.getPlayer().getInventory().getItemInHand();
			// Kind of a hack- check if we just dropped a wand, and now have an empty hand
			if (Wand.isWand(event.getItemDrop().getItemStack()) && (inHand == null || inHand.getType() == Material.AIR)) {
				activeWand.deactivate();
				// Clear after inventory restore, since that will put the wand back
				player.setItemInHand(new ItemStack(Material.AIR, 1));
			} else {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onEnchantItem(EnchantItemEvent event) {
		if (enchantingEnabled && Wand.isWand(event.getItem())) {
			event.getEnchantsToAdd().clear();
			int level = event.getExpLevelCost();
			Wand wand = new Wand(this, event.getItem());
			WandLevel.randomizeWand(wand, true, level);
		}
	}
	
	@EventHandler
	public void onPrepareEnchantItem(PrepareItemEnchantEvent event) {
		if (Wand.isWand(event.getItem())) {
			Wand wandItem = new Wand(this, event.getItem());
			if (!wandItem.isModifiable()) return;
			
			Set<Integer> levelSet = WandLevel.getLevels();
			ArrayList<Integer> levels = new ArrayList<Integer>();
			levels.addAll(levelSet);
			int[] offered = event.getExpLevelCostsOffered();
			// bonusLevels caps at 20
			int bonusLevels = event.getEnchantmentBonus();
			int maxLevel = levels.get(levels.size() - 1) - 20 + bonusLevels;
			
			for (int i = 0; i < offered.length - 1; i++) {
				int levelIndex = (int)((float)i * levels.size() / (float)offered.length);
				levelIndex += (float)bonusLevels * ((i + 1) / offered.length);
				levelIndex = Math.min(levelIndex, levels.size() - 1);
				offered[i] = levels.get(levelIndex);
			}
			offered[offered.length - 1] = maxLevel;
			event.setCancelled(false);
		}
	}
	
	public WandChestPopulator getWandChestPopulator() {
		return new WandChestPopulator(this, blockPopulatorConfig);
	}
	
	@EventHandler
	public void onWorldInit(WorldInitEvent event) {
		// Install our block populator if configured to do so.
		if (blockPopulatorEnabled && blockPopulatorConfig != null) {
			World world = event.getWorld();
			world.getPopulators().add(getWandChestPopulator());
		}
	}
	
	protected boolean addWandMarker(Wand wand, Location location) {
		String description = wand.getHTMLDescription();
		return addMarker("wand-" + wand.getId(), "Wands", wand.getName(), location.getWorld().getName(),
			location.getBlockX(), location.getBlockY(), location.getBlockZ(),
			description
		);
	}
	
	protected void checkForWands(final Entity[] entities, final int retries) {
		if (dynmapShowWands && dynmap != null) {
			if (!dynmap.markerAPIInitialized()) {
				if (retries > 0) {
					final MagicController me = this;
					Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
						public void run() {
							me.checkForWands(entities, retries + 1);
						}
					}, 40);
				}
				return;
			}
			int wandCount = 0;
			for (Entity entity : entities) {
				if (!(entity instanceof Item)) continue;
				Item item = (Item)entity;
				ItemStack itemStack = item.getItemStack();
				if (Wand.isWand(itemStack)) {
					Wand wand = new Wand(this, itemStack);
					wandCount += addWandMarker(wand, item.getLocation()) ? 1 : 0;
				}
			}
			
			if (wandCount > 0) {
				getLogger().info("Found " + wandCount + " wands, added to map");
			}
		}
	}

	@EventHandler
	public void onChunkLoad(ChunkLoadEvent e) {
		// Look for wands in the chnk
		final MagicController me = this;
		final ChunkLoadEvent event = e;
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				me.checkForWands(event.getChunk().getEntities(), 10);
			}
		}, 40);
	}
	
	public Spell getSpell(String name) {
		return spells.get(name);
	}
	
	public void toggleCastCommandOverrides(Mage mage, boolean override) {
		mage.setCostReduction(override ? castCommandCostReduction : 0);
		mage.setCooldownReduction(override ? castCommandCooldownReduction : 0);
	}
	
	public static List<String> getPlayerNames() {
		List<String> playerNames = new ArrayList<String>();
		List<World> worlds = Bukkit.getWorlds();
		for (World world : worlds) {
			List<Player> players = world.getPlayers();
			for (Player player : players) {
				playerNames.add(player.getName());
			}
		}
		return playerNames;
	}
	
	public boolean sendMail(CommandSender sender, String fromPlayer, String toPlayer, String message) {
		if (mailer != null) {
			return mailer.sendMail(sender, fromPlayer, toPlayer, message);
		}
		
		return false;
	}

	/*
	 * Private data
	 */
	 private final String                        spellsFileName                 = "spells.yml";
	 private final String                        propertiesFileName             = "magic.yml";
	 private final String                        playersFileName                = "players.yml";
	 private final String                        spellsFileNameDefaults         = "spells.defaults.yml";
	 private final String                        propertiesFileNameDefaults     = "magic.defaults.yml";

	 static final String                         DEFAULT_BUILDING_MATERIALS     = "0,1,2,3,4,5,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,33,34,35,41,42,43,45,46,47,48,49,52,53,55,56,57,58,60,61,62,65,66,67,73,74,79,80,81,82,83,84,85,86,87,88,89,90,91,92,93,94,95,96,97,98,99,100,101,102,103,104,105,106,107,108,109";
	 static final String                         DEFAULT_INDESTRUCTIBLE_MATERIALS = "7,54,130";
     static final String						 DEFAULT_DESTRUCTIBLE_MATERIALS	= "0,1,2,3,4,8,9,10,11,12,13,87,88";
	 static final String                         DEFAULT_TARGET_THROUGH_MATERIALS = "0";
	 
	 static final String                         STICKY_MATERIALS               = "37,38,39,50,51,55,59,63,64,65,66,68,70,71,72,75,76,77,78,83";
	 static final String                         STICKY_MATERIALS_DOUBLE_HEIGHT = "64,71,";

	 private Set<Material>                      buildingMaterials              = new TreeSet<Material>();
	 private Set<Material>                      indestructibleMaterials        = new TreeSet<Material>();
	 private Set<Material>                      destructibleMaterials        = new TreeSet<Material>();
	 private Set<Material>                      stickyMaterials                = new TreeSet<Material>();
	 private Set<Material>                      stickyMaterialsDoubleHeight    = new TreeSet<Material>();
	 private Set<Material>                      targetThroughMaterials  	   = new TreeSet<Material>();

	 private long                                physicsDisableTimeout          = 0;
	 private int                                 undoQueueDepth                 = 256;
	 private boolean							 wandCycling					= false;
	 private boolean                             showMessages                   = true;
	 private boolean                             showCastMessages               = false;
	 private String								 messagePrefix					= "";
	 private String								 castMessagePrefix				= "";
	 private boolean                             soundsEnabled                  = true;
	 private boolean                             fillWands                      = false;
	 private boolean                             indestructibleWands            = true;
	 private boolean                             keepWandsOnDeath	            = true;
	 private int								 messageThrottle				= 0;
	 private int								 clickCooldown					= 150;
	 private boolean							 blockPopulatorEnabled			= false;
	 private boolean							 enchantingEnabled				= false;
	 private boolean							 combiningEnabled				= false;
	 private boolean							 organizingEnabled				= false;
	 private boolean							 dynmapUpdate					= true;
	 private boolean							 dynmapShowWands				= true;
	 private float							 	 maxDamagePowerMultiplier	    = 2.0f;
	 private float								 maxConstructionPowerMultiplier = 5.0f;
	 private float								 maxRadiusPowerMultiplier 		= 1.5f;
	 private float								 maxRangePowerMultiplier 		= 3.0f;
	 private float							 	 castCommandCostReduction	    = 1.0f;
	 private float							 	 castCommandCooldownReduction	= 1.0f;
	 private ConfigurationNode					 blockPopulatorConfig			= null;
	 private LinkedList<BlockBatch>				 pendingBatches					= new LinkedList<BlockBatch>();
	 private int								 maxBlockUpdates				= 100;
	 
	 private final HashMap<String, Spell>        spells                         = new HashMap<String, Spell>();
	 private final HashMap<String, Mage> 		 mages                  		= new HashMap<String, Mage>();

	 private Recipe								 wandRecipe						= null;
	 private Material							 wandRecipeUpperMaterial		= Material.DIAMOND;
	 private Material							 wandRecipeLowerMaterial		= Material.BLAZE_ROD;
	 private String								 recipeOutputTemplate			= "random(1)";
	 
	 private MagicPlugin                         plugin                         = null;
	 private Object								 regionManager					= null;
	 private DynmapCommonAPI					 dynmap							= null;
	 private Mailer								 mailer							= null;
}
