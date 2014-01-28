package com.elmakers.mine.bukkit.plugins.magic;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Color;
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
import org.bukkit.event.entity.EntityExplodeEvent;
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
import org.bukkit.util.Vector;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.markers.CircleMarker;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;
import org.dynmap.markers.PolyLineMarker;

import com.elmakers.mine.bukkit.essentials.MagicItemDb;
import com.elmakers.mine.bukkit.essentials.Mailer;
import com.elmakers.mine.bukkit.plugins.magic.populator.WandChestPopulator;
import com.elmakers.mine.bukkit.plugins.magic.wand.LostWand;
import com.elmakers.mine.bukkit.plugins.magic.wand.Wand;
import com.elmakers.mine.bukkit.plugins.magic.wand.WandLevel;
import com.elmakers.mine.bukkit.utilities.InventoryUtils;
import com.elmakers.mine.bukkit.utilities.Messages;
import com.elmakers.mine.bukkit.utilities.NMSUtils;
import com.elmakers.mine.bukkit.utilities.SetActiveItemSlotTask;
import com.elmakers.mine.bukkit.utilities.URLMap;
import com.elmakers.mine.bukkit.utilities.borrowed.Configuration;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class MagicController implements Listener 
{
	public MagicController(final MagicPlugin plugin)
	{
		this.plugin = plugin;
		
		configFolder = plugin.getDataFolder();
		configFolder.mkdirs();

		dataFolder = new File(configFolder, "data");
		dataFolder.mkdirs();
		
		playerDataFolder = new File(dataFolder, "players");
		playerDataFolder.mkdirs();

		defaultsFolder = new File(configFolder, "defaults");
		defaultsFolder.mkdirs();	
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

	public Set<Material> getRestrictedMaterials()
	{
		return restrictedMaterials;
	}

	public Set<Material> getMaterialSet(String name)
	{
		if (!materialSets.containsKey(name)) {
			return new HashSet<Material>();
		}
		return materialSets.get(name);
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
	
	public float getMaxRadiusPowerMultiplierMax() {
		return maxRadiusPowerMultiplierMax;
	}
	
	public float getMaxRangePowerMultiplier() {
		return maxRangePowerMultiplier;
	}
	
	public float getMaxRangePowerMultiplierMax() {
		return maxRangePowerMultiplierMax;
	}
	
	public int getAutoUndoInterval() {
		return autoUndo;
	}
	
	/*
	 * Undo system
	 */

	public int getUndoQueueDepth() {
		return undoQueueDepth;
	}

	public String undoAny(Block target)
	{
		for (String playerName : mages.keySet())
		{
			Mage mage = getMage(playerName);
			if (mage.undo(target))
			{
				return playerName;
			}
		}

		return null;
	}

	public boolean undo(String playerName)
	{
		Mage mage = getMage(playerName);
		return mage.undo();
	}

	public boolean commitAll()
	{
		boolean undid = false;
		for (Mage mage : mages.values()) {
			undid = mage.commit() || undid;
		}
		return undid;
	}

	/*
	 * Event registration- call to listen for events
	 */

	public void registerEvent(SpellEventType type, Spell spell)
	{
		Mage mage = getMage(spell.getPlayer());
		mage.registerEvent(type, spell);
	}

	public void unregisterEvent(SpellEventType type, Spell spell)
	{
		Mage mage = getMage(spell.getPlayer());
		mage.unregisterEvent(type, spell);
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

	public boolean isIndestructible(Location location) 
	{
		return isIndestructible(location.getBlock());
	}

	public boolean isIndestructible(Block block) 
	{
		return indestructibleMaterials.contains(block.getType());
	}

	public boolean isDestructible(Block block) 
	{
		return destructibleMaterials.contains(block.getType());		
	}

	public boolean isRestricted(Material material) 
	{
		return restrictedMaterials.contains(material);		
	}
	
	public boolean hasBuildPermission(Player player, Location location) 
	{
		return hasBuildPermission(player, location.getBlock());
	}

	public boolean hasBuildPermission(Player player, Block block) 
	{
		// Check the region manager.
		// TODO: We need to be able to do WG permission checks while a player is offline.
		if (!regionManagerEnabled || regionManager == null || player == null) return true;
		
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
		load();
		
		if (craftingEnabled) {
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
		
		if (essentialsSignsEnabled) {
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
		
		// Try to (dynamically) link to WorldGuard:
		if (regionManagerEnabled) {
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
		} else {
			getLogger().info("Region manager disabled");
		}
		
		// Try to (dynamically) link to dynmap:
		try {
			Plugin dynmapPlugin = plugin.getServer().getPluginManager().getPlugin("dynmap");
			if (dynmapPlugin != null && !(dynmapPlugin instanceof DynmapCommonAPI)) {
				throw new Exception("Dynmap plugin found, but class is not DynmapCommonAPI");
			}
			dynmap = (DynmapCommonAPI)dynmapPlugin;
		} catch (Throwable ex) {
			plugin.getLogger().warning(ex.getMessage());
		}
		
		if (dynmap == null) {
			getLogger().info("dynmap not found, not integrating.");
		} else {
			getLogger().info("dynmap found, integrating.");
		}
		
		// Set up the PlayerSpells timer
		Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			public void run() {
				for (Mage mage : mages.values()) {
					mage.tick();
				}
			}
		}, 0, 20);

		// Set up the Block update timer
		Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			public void run() {
				List<Mage> pending = new ArrayList<Mage>();
				pending.addAll(pendingConstruction.values());
				for (Mage mage : pending) {
					mage.processPendingBatches(maxBlockUpdates);
				}
			}
		}, 0, 1);
	}
	
	protected void addPending(Mage mage) {
		pendingConstruction.put(mage.getName(), mage);
	}
	
	protected void removePending(Mage mage) {
		pendingConstruction.remove(mage.getName());
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
				markerSet = markers.createMarkerSet(group, group, null, false);
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
			if (description != null) {
				marker.setDescription(description);
			}
		}
		
		return created;
	}
	
	protected File getDataFile(String fileName)
	{
		File dataFile = new File(dataFolder, fileName + ".yml");

		// Migration - TODO: Remove
		File legacyFile = new File(configFolder, fileName + ".yml");
		// Extra-special hacky migration!
		if (fileName.equals(URL_MAPS_FILE)) {
			legacyFile = new File(configFolder,"urlmaps.yml");
		}
		if (legacyFile.exists() && !dataFile.exists()) {
			getLogger().info("MIGRATING " + legacyFile.getName() + ", you should only see this once.");
			legacyFile.renameTo(dataFile);
		}
		
		return dataFile;
	}
	
	protected ConfigurationNode loadDataFile(String fileName)
	{
		File dataFile = getDataFile(fileName);
		if (!dataFile.exists()) {
			return null;
		}
		Configuration configuration = new Configuration(dataFile);
		configuration.load();
		return configuration;
	}
	
	protected Configuration createDataFile(String fileName)
	{
		File dataFile = new File(dataFolder, fileName + ".yml");
		Configuration configuration = new Configuration(dataFile);
		return configuration;
	}

	protected ConfigurationNode loadConfigFile(String fileName)
	{
		String configFileName = fileName + ".yml";
		String defaultsFileName = "defaults/" + fileName + ".defaults.yml";
		File configFile = new File(configFolder, configFileName);
		if (!configFile.exists()) {
			getLogger().info("Saving template " + configFileName + ", edit to customize configuration.");
			plugin.saveResource(configFileName, false);
		}
		Configuration config = new Configuration(configFile);
		getLogger().info("Loading " + configFile.getName());
		config.load();
		
		plugin.saveResource(defaultsFileName, true);
		Configuration defaultConfig = new Configuration(plugin.getResource(defaultsFileName));
		defaultConfig.load();
		defaultConfig.add(config);
		
		return defaultConfig;
	}
	
	public void loadConfiguration()
	{
		// Load main configuration
		try {
			loadProperties(loadConfigFile(CONFIG_FILE));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		// Load localizations
		try {
			Messages.reset();
			Messages.load(loadConfigFile(MESSAGES_FILE));
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// Load materials configuration
		try {
			loadMaterials(loadConfigFile(MATERIALS_FILE));
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// Load block populator configuration
		try {
			loadPopulator(loadConfigFile(BLOCK_POPULATOR_FILE));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		// Load spells
		try {
			loadSpells(loadConfigFile(SPELLS_FILE));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		// Load wand templates
		try {
			Wand.loadTemplates(loadConfigFile(WANDS_FILE));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		getLogger().info("Magic: Loaded " + spells.size() + " spells and " + Wand.getWandTemplates().size() + " wands");
	}
	
	public void load()
	{
		loadConfiguration();
		
		// Delay some loading, in particular world lookups by name seem to fail at onEnable time
		// I'm guessing this is because I force Magic to run prior to inialization
		// This is pretty hacky, but I'd hope everything is OK on the next time anyway.
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				
				// Load Player Data

				// Legacy migration
				File playersFile = new File(configFolder, "players.yml");
				if (playersFile.exists()) {
					getLogger().info("MIGRATING player data from file " + playersFile.getName());
					Configuration playerConfiguration = new Configuration(playersFile);
					playerConfiguration.load();
					List<String> playerNames = playerConfiguration.getKeys();
					for (String playerName : playerNames) {
						getMage(playerName).load(playerConfiguration.getNode(playerName));
					}
					
					playersFile.renameTo(new File("players.yml.bak"));
					getLogger().info("Migration complete, you should not see this message again.");
				} else {
					// TODO: Remove the above, make this the only path.
					File[] playerFiles = playerDataFolder.listFiles(new FilenameFilter() {
					    public boolean accept(File dir, String name) {
					        return name.toLowerCase().endsWith(".yml");
					    }
					});
					
					for (File playerFile : playerFiles)
					{
						Configuration playerData = new Configuration(playerFile);
						getLogger().info("Loading player data from file " + playerFile.getName());
						playerData.load();
						String playerName = playerFile.getName().replaceFirst("[.][^.]+$", "");
						getMage(playerName).load(playerData);
					}
				}
				
				// Load lost wands
				try {
					getLogger().info("Loading lost wand data");
					ConfigurationNode lostWandConfiguration = loadDataFile(LOST_WANDS_FILE);
					if (lostWandConfiguration != null)
					{
						List<String> wandIds = lostWandConfiguration.getKeys();
						for (String wandId : wandIds) {
							LostWand lostWand = new LostWand(wandId, lostWandConfiguration.getNode(wandId));
							if (!lostWand.isValid()) {
								getLogger().info("Skipped invalid entry in lostwands.yml file, entry will be deleted. The wand is really lost now!");
								continue;
							}
							addLostWand(lostWand);
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				
				// Load URL Map Data
				try {
					URLMap.resetAll();
					File urlMapFile = getDataFile(URL_MAPS_FILE);
					File imageCache = new File(dataFolder, "imagemapcache");
					imageCache.mkdirs();
					URLMap.load(plugin, urlMapFile, imageCache);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}, 10);
	}
	
	protected String getChunkKey(Chunk chunk) {
		return chunk.getWorld().getName() + "|" + chunk.getX() + "," + chunk.getZ();
	}
	
	protected boolean addLostWand(LostWand lostWand) {
		if (lostWands.containsKey(lostWand.getId())) {
			updateLostWand(lostWand);
			
			return false;
		}
		lostWands.put(lostWand.getId(), lostWand);
		String chunkKey = getChunkKey(lostWand.getLocation().getChunk());
		Set<String> chunkWands = lostWandChunks.get(chunkKey);
		if (chunkWands == null) {
			chunkWands = new HashSet<String>();
			lostWandChunks.put(chunkKey, chunkWands);
		}
		chunkWands.add(lostWand.getId());
		
		if (dynmapShowWands) {
			addLostWandMarker(lostWand);
		}
		
		return true;
	}
	
	protected void updateLostWand(Wand wand, Location dropLocation) {
		LostWand lostWand = lostWands.get(wand.getId());
		lostWand.update(wand, dropLocation);
		addLostWandMarker(lostWand);
	}

	protected void updateLostWand(LostWand newLost) {
		LostWand currentLostWand = lostWands.get(newLost.getId());
		currentLostWand.update(newLost);
		
		if (dynmapShowWands) {
			addLostWandMarker(currentLostWand);
		}
	}
	
	public boolean addLostWand(Wand wand, Location dropLocation) {
		if (lostWands.containsKey(wand.getId())) {
			updateLostWand(wand, dropLocation);
			return false;
		}
		LostWand lostWand = new LostWand(wand, dropLocation);
		addLostWand(lostWand);
		
		return true;
	}

	public boolean removeLostWand(String wandId) {
		if (!lostWands.containsKey(wandId)) return false;
		
		LostWand lostWand = lostWands.get(wandId);
		lostWands.remove(wandId);
		String chunkKey = getChunkKey(lostWand.getLocation().getChunk());
		Set<String> chunkWands = lostWandChunks.get(chunkKey);
		if (chunkWands != null) {
			chunkWands.remove(wandId);
			if (chunkWands.size() == 0) {
				lostWandChunks.remove(chunkKey);
			}
		}
		
		if (dynmapShowWands) {
			if (removeMarker("wand-" + wandId, "Wands")) {
				getLogger().info("Wand removed from map");
			}
		}
		
		return true;
	}
	
	public boolean removeLostWand(Wand wand) {
		return removeLostWand(wand.getId());
	}
	
	public void save()
	{
		getLogger().info("Saving player data");
		for (Entry<String, Mage> mageEntry : mages.entrySet()) {
			File playerData = new File(playerDataFolder, mageEntry.getKey() + ".yml");
			Configuration playerConfig = new Configuration(playerData);
			mageEntry.getValue().save(playerConfig);
			playerConfig.save();
		}

		getLogger().info("Saving lost wands data");
		Configuration lostWandsConfiguration = createDataFile(LOST_WANDS_FILE);
		for (Entry<String, LostWand> wandEntry : lostWands.entrySet()) {
			ConfigurationNode wandNode = lostWandsConfiguration.createChild(wandEntry.getKey());
			wandEntry.getValue().save(wandNode);
		}
		lostWandsConfiguration.save();

		getLogger().info("Saving image map data");
		URLMap.save();
	}
	
	protected void loadSpells(ConfigurationNode config)
	{
		if (config == null) return;
		
		// Reset existing spells.
		// Existing Mages might have references to these, though.
		// Not sure how to handle that.
		spells.clear();
		
		List<String> spellKeys = config.getKeys();
		for (String key : spellKeys)
		{
			ConfigurationNode spellNode = config.getNode(key);
			if (!spellNode.getBoolean("enabled", true)) {
				continue;
			}
			
			Spell newSpell = Spell.loadSpell(key, spellNode, this);
			if (newSpell == null)
			{
				getLogger().warning("Magic: Error loading spell " + key);
				continue;
			}
			addSpell(newSpell);
		}
	}
	
	protected void loadMaterials(ConfigurationNode materialNode)
	{
		if (materialNode == null) return;
		
		List<String> keys = materialNode.getKeys();
		for (String key : keys) {
			materialSets.put(key,  materialNode.getMaterials(key));
		}
		if (materialSets.containsKey("building")) {
			buildingMaterials = materialSets.get("building");
		}
		if (materialSets.containsKey("indestructible")) {
			indestructibleMaterials = materialSets.get("indestructible");
		}
		if (materialSets.containsKey("restricted")) {
			restrictedMaterials = materialSets.get("restricted");
		}
		if (materialSets.containsKey("destructible")) {
			destructibleMaterials = materialSets.get("destructible");
		}
	}
	
	protected void loadProperties(ConfigurationNode properties)
	{
		if (properties == null) return;
		
		maxTNTPerChunk = properties.getInteger("max_tnt_per_chunk", maxTNTPerChunk);
		undoQueueDepth = properties.getInteger("undo_depth", undoQueueDepth);
		wandCycling = properties.getBoolean("right_click_cycles", wandCycling);
		showMessages = properties.getBoolean("show_messages", showMessages);
		showCastMessages = properties.getBoolean("show_cast_messages", showCastMessages);
		messagePrefix = properties.getString("message_prefix", messagePrefix);
		castMessagePrefix = properties.getString("cast_message_prefix", castMessagePrefix);
		clickCooldown = properties.getInt("click_cooldown", clickCooldown);
		messageThrottle = properties.getInt("message_throttle", 0);
		maxBlockUpdates = properties.getInt("max_block_updates", maxBlockUpdates);
		ageDroppedItems = properties.getInt("age_dropped_items", ageDroppedItems);
		soundsEnabled = properties.getBoolean("sounds", soundsEnabled);
		fillWands = properties.getBoolean("fill_wands", fillWands);
		indestructibleWands = properties.getBoolean("indestructible_wands", indestructibleWands);
		keepWandsOnDeath = properties.getBoolean("keep_wands_on_death", keepWandsOnDeath);
		maxDamagePowerMultiplier = (float)properties.getDouble("max_power_damage_multiplier", maxDamagePowerMultiplier);
		maxConstructionPowerMultiplier = (float)properties.getDouble("max_power_construction_multiplier", maxConstructionPowerMultiplier);
		maxRangePowerMultiplier = (float)properties.getDouble("max_power_range_multiplier", maxRangePowerMultiplier);
		maxRangePowerMultiplierMax = (float)properties.getDouble("max_power_range_multiplier_max", maxRangePowerMultiplierMax);
		maxRadiusPowerMultiplier = (float)properties.getDouble("max_power_radius_multiplier", maxRadiusPowerMultiplier);
		maxRadiusPowerMultiplierMax = (float)properties.getDouble("max_power_radius_multiplier_max", maxRadiusPowerMultiplierMax);
		castCommandCostReduction = (float)properties.getDouble("cast_command_cost_reduction", castCommandCostReduction);
		castCommandCooldownReduction = (float)properties.getDouble("cast_command_cooldown_reduction", castCommandCooldownReduction);
		autoUndo = properties.getInteger("auto_undo", autoUndo);
		blockPopulatorEnabled = properties.getBoolean("enable_block_populator", blockPopulatorEnabled);
		enchantingEnabled = properties.getBoolean("enable_enchanting", enchantingEnabled);
		combiningEnabled = properties.getBoolean("enable_combining", combiningEnabled);
		organizingEnabled = properties.getBoolean("enable_organizing", organizingEnabled);
		essentialsSignsEnabled = properties.getBoolean("enable_essentials_signs", essentialsSignsEnabled);
		dynmapShowWands = properties.getBoolean("dynmap_show_wands", dynmapShowWands);
		dynmapShowSpells = properties.getBoolean("dynmap_show_spells", dynmapShowSpells);
		dynmapUpdate = properties.getBoolean("dynmap_update", dynmapUpdate);
		regionManagerEnabled = properties.getBoolean("region_manager_enabled", regionManagerEnabled);
		
		// Parse wand settings
		Wand.WandMaterial = properties.getMaterial("wand_item", Wand.WandMaterial);
		Wand.CopyMaterial = properties.getMaterial("copy_item", Wand.CopyMaterial);
		Wand.EraseMaterial = properties.getMaterial("erase_item", Wand.EraseMaterial);
		Wand.CloneMaterial = properties.getMaterial("clone_item", Wand.CloneMaterial);
		Wand.ReplicateMaterial = properties.getMaterial("replicate_item", Wand.ReplicateMaterial);
		Wand.EnchantableWandMaterial = properties.getMaterial("wand_item_enchantable", Wand.EnchantableWandMaterial);

		// Parse crafting recipe settings
		craftingEnabled = properties.getBoolean("enable_crafting", craftingEnabled);
		if (craftingEnabled) {
			recipeOutputTemplate = properties.getString("crafting_output", recipeOutputTemplate);
			wandRecipeUpperMaterial = properties.getMaterial("crafting_material_upper", wandRecipeUpperMaterial);
			wandRecipeLowerMaterial = properties.getMaterial("crafting_material_lower", wandRecipeLowerMaterial);
		}
	}
	
	protected void loadPopulator(ConfigurationNode properties)
	{		
		if (properties == null) return;
	
		blockPopulatorConfig = properties.getNode("populate_chests");
	}

	protected void clear()
	{
		mages.clear();
		pendingConstruction.clear();
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
	public void onInventoryDrag(InventoryDragEvent event) {
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
	
	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
		if (maxTNTPerChunk > 0 && event.getEntity().getType() == EntityType.PRIMED_TNT) {
			Chunk chunk = event.getEntity().getLocation().getChunk();
			int tntCount = 0;
			Entity[] entities = chunk.getEntities();
			for (Entity entity : entities) {
				if (entity.getType() == EntityType.PRIMED_TNT) {
					tntCount++;
				}
			}
			if (tntCount > maxTNTPerChunk) {
				event.setCancelled(true);
			}
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
				if (material.isBlock() || 
					material == Wand.EraseMaterial || material == Wand.CopyMaterial || 
					material == Wand.CloneMaterial || material == Wand.ReplicateMaterial) {
					activeWand.activateMaterial(material, icon.getData().getData());
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
		Mage mage = getMage(player);
		mage.onPlayerDamage(event);
	}
	
	@EventHandler
	public void onEntityCombust(EntityCombustEvent event)
	{
		if (!(event.getEntity() instanceof Player)) return;
		Mage mage = getMage((Player)event.getEntity());
		mage.onPlayerCombust(event);
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
				removeLostWand(wand);
			}
		}
	}
	
	@EventHandler
	public void onItemSpawn(ItemSpawnEvent event)
	{
		if (Wand.isWand(event.getEntity().getItemStack()))
		{
			if (indestructibleWands) {
				InventoryUtils.setInvulnerable(event.getEntity());
			}
			Wand wand = new Wand(this, event.getEntity().getItemStack());
			if (wand != null) {
				addLostWand(wand, event.getEntity().getLocation());		
				Location dropLocation = event.getLocation();
				getLogger().info("Wand " + wand.getName() + ", id " + wand.getId() + " spawned at " + dropLocation.getBlockX() + " " + dropLocation.getBlockY() + " " + dropLocation.getBlockZ());

			}
		} else if (ageDroppedItems > 0) {
			try {
				Class<?> itemClass = NMSUtils.getBukkitClass("net.minecraft.server.EntityItem");
				Item item = event.getEntity();
				Object handle = NMSUtils.getHandle(item);
				Field ageField = itemClass.getDeclaredField("age");
				ageField.setAccessible(true);
				ageField.set(handle, ageDroppedItems);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event)
	{
		try {
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
	            	} else if (event.getDamage() >= itemStack.getDurability()) {
	                	Wand wand = new Wand(this, item.getItemStack());
	                	if (removeLostWand(wand)) {
	                		plugin.getLogger().info("Wand " + wand.getName() + ", id " + wand.getId() + " destroyed");
	                	}
	                }
				}  
	        }
		} catch (Exception ex) {
			// TODO: Trying to track down a stacktrace-less NPE that seemed to come from here:
			// [06:22:34] [Server thread/ERROR]: Could not pass event EntityDamageEvent to Magic v2.9.0
			// Caused by: java.lang.NullPointerException
			ex.printStackTrace();
		}
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		// Block block = event.getClickedBlock();
		// getLogger().info("INTERACT: " + event.getAction() + " on " + (block == null ? "NOTHING" : block.getType()));
		
		Player player = event.getPlayer();		
		Mage mage = getMage(player);
		if (!mage.checkLastClick(clickCooldown)) {
			return;
		}
		
		Wand wand = mage.getActiveWand();
		
		// Hacky check for immediately activating a wand if for some reason it was
		// not active
		if (wand == null && Wand.hasActiveWand(player)) {
			if (mage.hasStoredInventory()) {
				mage.restoreInventory();
			}
			wand = Wand.getActiveWand(this, player);
			wand.activate(mage);
			getLogger().warning("Player was holding an inactive wand on interact- activating.");			
		}
		
		// Safety check, we don't want to lose the player's inventory.
		// In theory, this should never happen though!
		if (wand == null && mage.hasStoredInventory())
		{
			getLogger().warning("Player had no active wand, but a stored inventory- restoring.");
			mage.restoreInventory();
			return;
		}
		
		if (wand == null || !hasWandPermission(player))
		{
			return;
		}
		
		// An extra double-check for a bug that is hard to reproduce, where the wand is no
		// longer the active item, but we never deactivated it.
		// Don't do this- it may happen semi-normally while lagging.
		/*
		if (!Wand.hasActiveWand(player)) 
		{
			wand.deactivate();
		}
		*/
		
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
						boolean cycleMaterials = false;
						if (activeSpell != null && activeSpell instanceof BrushSpell) {
							BrushSpell brushSpell = (BrushSpell)activeSpell;
							cycleMaterials = brushSpell.hasBrushOverride() && wand.getMaterialKeys().size() > 0;
						}
						if (cycleMaterials) {
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
		for (Mage mage : mages.values()) {
			Player player = mage.getPlayer();
			if (player == null) continue;
			
			Wand wand = mage.getActiveWand();
			if (wand != null) {
				wand.deactivate();
			}
			mage.restoreInventory();
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
				Mage mage = getMage(player);
				wand.activate(mage);
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
		if (craftingEnabled && wandRecipe != null && recipe.getResult().getType() == Wand.WandMaterial) {
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
		Mage mage = getMage(player);
		
		// Don't allow crafting in the wand inventory.
		if (mage.hasStoredInventory()) {
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
	
		if (enchantingEnabled && event.getInventory().getType() == InventoryType.ENCHANTING)
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
				if (organizingEnabled) {
					wand.organizeInventory();
				}
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

					if (organizingEnabled) {
						firstWand.organizeInventory();
					}
					player.getInventory().addItem(firstWand.getItem());
					player.sendMessage("Your wands have been combined!");
					
					// This seems to work in the debugger, but.. doesn't do anything.
					// InventoryUtils.setInventoryResults(anvilInventory, newWand.getItem());
				} else if (organizingEnabled && Wand.isWand(firstItem)) {
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
						onPlayerActivateIcon(mage, wand, clickedItem);
						player.closeInventory();
						event.setCancelled(true);
						return;
					}
				}
				
				// Prevent wand duplication
				if (Wand.isWand(event.getCursor()) || Wand.isWand(event.getCurrentItem())) {
					event.setCancelled(true);
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
		
		if (previousWand != null && !changedWands && previousWand.isInventoryOpen()) {
			previousWand.updateInventoryNames(true);
		}
	}

	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event)
	{
		Mage mage = getMage(event.getPlayer());
		ItemStack pickup = event.getItem().getItemStack();
		if (dynmapShowWands && Wand.isWand(pickup)) {
			Wand wand = new Wand(this, pickup);
			plugin.getLogger().info("Player " + mage.getName() + " picked up wand " + wand.getName() + ", id " + wand.getId());
			removeLostWand(wand);
		}
		if (mage.hasStoredInventory()) {
			event.setCancelled(true);   		
			if (mage.addToStoredInventory(event.getItem().getItemStack())) {
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
				wand.activate(mage);
			} 
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event)
	{
		Player player = event.getPlayer();
		Mage mage = getMage(player);
		if (mage.hasStoredInventory() || mage.getBlockPlaceTimeout() > System.currentTimeMillis()) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event)
	{
		Player player = event.getPlayer();
		Mage mage = getMage(player);
		final Wand activeWand = mage.getActiveWand();
		if (activeWand != null) {
			ItemStack inHand = event.getPlayer().getInventory().getItemInHand();
			// Kind of a hack- check if we just dropped a wand, and now have an empty hand
			if (Wand.isWand(event.getItemDrop().getItemStack()) && (inHand == null || inHand.getType() == Material.AIR)) {
				activeWand.deactivate();
				// Clear after inventory restore (potentially with deactivate), since that will put the wand back
				player.setItemInHand(new ItemStack(Material.AIR, 1));
			} else if (activeWand.isInventoryOpen()) {
				// Don't allow dropping anything out of the wand inventory, 
				// but this will close the inventory.
				Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
					public void run() {
						activeWand.closeInventory();
					}
				}, 1);
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
		if (enchantingEnabled && Wand.isWand(event.getItem())) {
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
		if (blockPopulatorEnabled && blockPopulatorConfig == null) {
			plugin.getLogger().warning("Block populator is enabled, but missing config");
		}
		if (blockPopulatorEnabled && blockPopulatorConfig != null) {
			World world = event.getWorld();
			world.getPopulators().add(getWandChestPopulator());
			plugin.getLogger().info("Installing chest populator in " + world.getName());
		}
	}
	
	protected boolean addLostWandMarker(LostWand lostWand) {
		Location location = lostWand.getLocation();
		return addMarker("wand-" + lostWand.getId(), "Wands", lostWand.getName(), location.getWorld().getName(),
			location.getBlockX(), location.getBlockY(), location.getBlockZ(), lostWand.getDescription()
		);
	}
	
	protected void checkForWands(final Chunk chunk, final int retries) {
		if (dynmapShowWands && dynmap != null) {
			if (!dynmap.markerAPIInitialized()) {
				if (retries > 0) {
					final MagicController me = this;
					Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
						public void run() {
							me.checkForWands(chunk, retries + 1);
						}
					}, 10);
				}
				return;
			}
			Entity[] entities = chunk.getEntities();
			Set<String> presentWandIds = new HashSet<String>();
			for (Entity entity : entities) {
				if (!(entity instanceof Item)) continue;
				Item item = (Item)entity;
				ItemStack itemStack = item.getItemStack();
				if (Wand.isWand(itemStack)) {
					Wand wand = new Wand(this, itemStack);
					addLostWand(wand, item.getLocation());
					presentWandIds.add(wand.getId());
				}
			}
			
			// Remove missing lost wands
			String chunkKey = getChunkKey(chunk);
			Set<String> chunkWands = lostWandChunks.get(chunkKey);
			if (chunkWands != null) {
				List<String> iterateWands = new ArrayList<String>(chunkWands);
				for (String wandId : iterateWands) {
					if (!presentWandIds.contains(wandId)) {
						LostWand lostWand = lostWands.get(wandId);
						String name = null;
						String owner = null;
						if (lostWand != null) {
							name = lostWand.getName();
							owner = lostWand.getOwner();
						}
						name = name == null ? "(Unknown)" : name;
						owner = owner == null ? "(Unknown)" : owner;
						plugin.getLogger().info("Wand " + wandId + ": " + name + "@" + owner + ", not found in chunk, presumed lost");
						removeLostWand(wandId);
					}
				}
			}
		}
	}

	@EventHandler
	public void onChunkLoad(ChunkLoadEvent e) {
		// Look for wands in the chunk
		final MagicController me = this;
		final ChunkLoadEvent event = e;
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				me.checkForWands(event.getChunk(), 10);
			}
		}, 5);
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
	
	public Material getDefaultMaterial() {
		return defaultMaterial;
	}
	
	public Collection<LostWand> getLostWands() {
		return lostWands.values();
	}
	
	public void onCast(Mage mage, Spell spell) {
		if (dynmapShowSpells && dynmap != null && dynmap.markerAPIInitialized()) {
			MarkerAPI markers = dynmap.getMarkerAPI();
			MarkerSet spellSet = markers.getMarkerSet("Spells");
			if (spellSet == null) {
				spellSet = markers.createMarkerSet("Spells", "Spell Casts", null, false);
			}
			final String markerId = "Spell-" + mage.getName();
			final String targetId = "SpellTarget-" + mage.getName();
			
			int range = 32;
			double radius = 3.0 * mage.getDamageMultiplier();
			int width = (int)(2.0 * mage.getDamageMultiplier());
			width = Math.min(8, width);
			final Location location = mage.getLocation();
			Color color = mage.getEffectColor();
			color = color == null ? Color.PURPLE : color;
			final String worldName = location.getWorld().getName();
			String label = spell.getName() + " : " + mage.getName();
			
			// Create a circular disc for a spell cast
			CircleMarker marker = spellSet.findCircleMarker(markerId);
			if (marker != null) {
				marker.setCenter(worldName, location.getX(), location.getY(), location.getZ());
				marker.setLabel(label);
			} else {
				marker = spellSet.createCircleMarker(markerId, label, false, worldName, location.getX(), location.getY(), location.getZ(), radius, radius, false);
			}
			marker.setRadius(radius, radius);
			marker.setLineStyle(width, 0.9, color.asRGB());
			marker.setFillStyle(0.5, color.asRGB());
			
			// Create a targeting indicator line
			Location target = null;
			Target spellTarget = spell.getTarget();
			if (spellTarget != null) {
				target = spellTarget.getLocation();
			}
			
			if (target == null) {
				target = location.clone();
				Vector direction = location.getDirection();
				direction.normalize().multiply(range);
				target.add(direction);
			}
						
			PolyLineMarker targetMarker = spellSet.findPolyLineMarker(targetId);
			if (targetMarker != null) {
				targetMarker.setCornerLocation(0, location.getX(), location.getY(), location.getZ());
				targetMarker.setCornerLocation(1, target.getX(), target.getY(), target.getZ());
				targetMarker.setLabel(label);
			} else {
				double[] x = {location.getX(), target.getX()};
				double[] y = {location.getY(), target.getY()};
				double[] z = {location.getZ(), target.getZ()};
				
				targetMarker = spellSet.createPolyLineMarker(targetId, label, false, worldName, x, y, z, false);
			}
			targetMarker.setLineStyle(width, 0.8, color.asRGB());
			
			/*
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					marker.deleteMarker();
					// deleteMarker does not seem to work. :\
					double[] x = {location.getX(), location.getX()};
					double[] y = {location.getY(), location.getY()};
					double[] z = {location.getZ(), location.getZ()};
					markerSet.createPolyLineMarker(markerId, "(None)", false, location.getWorld().getName(), x, y, z, false);
				}
			}, 20 * 5);
			*/
		}
	}

	/*
	 * Private data
	 */
	 private final String                        SPELLS_FILE                 	= "spells";
	 private final String                        CONFIG_FILE             		= "config";
	 private final String                        WANDS_FILE             		= "wands";
	 private final String                        MESSAGES_FILE             		= "messages";
	 private final String                        MATERIALS_FILE             	= "materials";
	 private final String                        BLOCK_POPULATOR_FILE           = "populator";
	 private final String						 LOST_WANDS_FILE				= "lostwands";
	 private final String						 URL_MAPS_FILE					= "imagemaps";

	 static final String                         STICKY_MATERIALS               = "37,38,39,50,51,55,59,63,64,65,66,68,70,71,72,75,76,77,78,83";
	 static final String                         STICKY_MATERIALS_DOUBLE_HEIGHT = "64,71,";

	 private Set<Material>                      buildingMaterials              = new HashSet<Material>();
	 private Set<Material>                      indestructibleMaterials        = new HashSet<Material>();
	 private Set<Material>                      restrictedMaterials	 	       = new HashSet<Material>();
	 private Set<Material>                      destructibleMaterials          = new HashSet<Material>();
	 private Map<String, Set<Material>>			materialSets				   = new HashMap<String, Set<Material>>();
	 
	 private long                                physicsDisableTimeout          = 0;
	 private int								 maxTNTPerChunk					= 0;
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
	 private boolean							 craftingEnabled				= false;
	 private boolean							 enchantingEnabled				= false;
	 private boolean							 combiningEnabled				= false;
	 private boolean							 organizingEnabled				= false;
	 private boolean							 essentialsSignsEnabled			= false;
	 private boolean							 dynmapUpdate					= true;
	 private boolean							 dynmapShowWands				= true;
	 private boolean							 dynmapShowSpells				= true;
	 private float							 	 maxDamagePowerMultiplier	    = 2.0f;
	 private float								 maxConstructionPowerMultiplier = 5.0f;
	 private float								 maxRadiusPowerMultiplier 		= 2.5f;
	 private float								 maxRadiusPowerMultiplierMax    = 4.0f;
	 private float								 maxRangePowerMultiplier 		= 3.0f;
	 private float								 maxRangePowerMultiplierMax 	= 5.0f;
	 private float							 	 castCommandCostReduction	    = 1.0f;
	 private float							 	 castCommandCooldownReduction	= 1.0f;
	 private ConfigurationNode					 blockPopulatorConfig			= null;
	 private int								 maxBlockUpdates				= 100;
	 private int								 ageDroppedItems				= 0;
	 private int								 autoUndo						= 0;
	 
	 private final HashMap<String, Spell>        spells                         = new HashMap<String, Spell>();
	 private final HashMap<String, Mage> 		 mages                  		= new HashMap<String, Mage>();
	 private final HashMap<String, Mage>		 pendingConstruction			= new HashMap<String, Mage>();

	 private Recipe								 wandRecipe						= null;
	 private Material							 wandRecipeUpperMaterial		= Material.DIAMOND;
	 private Material							 wandRecipeLowerMaterial		= Material.BLAZE_ROD;
	 private String								 recipeOutputTemplate			= "random(1)";
	 
	 private MagicPlugin                         plugin                         = null;
	 private final File							 configFolder;
	 private final File							 dataFolder;
	 private final File							 defaultsFolder;
	 private final File							 playerDataFolder;
	 
	 private boolean							 regionManagerEnabled           = true;
	 private Object								 regionManager					= null;
	 private DynmapCommonAPI					 dynmap							= null;
	 private Mailer								 mailer							= null;
	 private Material							 defaultMaterial				= Material.DIRT;
	 
	 private Map<String, LostWand>				 lostWands						= new HashMap<String, LostWand>();
	 private Map<String, Set<String>>		 	 lostWandChunks					= new HashMap<String, Set<String>>();
}
