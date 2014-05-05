package com.elmakers.mine.bukkit.magic;

import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.CodeSource;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.potion.PotionEffectType;
import org.mcstats.Metrics;
import org.mcstats.Metrics.Graph;

import com.elmakers.mine.bukkit.api.block.BoundingBox;
import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.MageSpell;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.block.Automaton;
import com.elmakers.mine.bukkit.block.BlockData;
import com.elmakers.mine.bukkit.block.MaterialBrush;
import com.elmakers.mine.bukkit.block.UndoQueue;
import com.elmakers.mine.bukkit.block.WorldEditSchematic;
import com.elmakers.mine.bukkit.dynmap.DynmapController;
import com.elmakers.mine.bukkit.effect.EffectPlayer;
import com.elmakers.mine.bukkit.elementals.ElementalsController;
import com.elmakers.mine.bukkit.essentials.MagicItemDb;
import com.elmakers.mine.bukkit.essentials.Mailer;
import com.elmakers.mine.bukkit.magic.command.MagicTabExecutor;
import com.elmakers.mine.bukkit.magic.listener.AnvilController;
import com.elmakers.mine.bukkit.magic.listener.CraftingController;
import com.elmakers.mine.bukkit.magic.listener.EnchantingController;
import com.elmakers.mine.bukkit.metrics.CategoryCastPlotter;
import com.elmakers.mine.bukkit.metrics.DeltaPlotter;
import com.elmakers.mine.bukkit.metrics.SpellCastPlotter;
import com.elmakers.mine.bukkit.protection.FactionsManager;
import com.elmakers.mine.bukkit.protection.WorldGuardManager;
import com.elmakers.mine.bukkit.spell.SpellCategory;
import com.elmakers.mine.bukkit.traders.TradersController;
import com.elmakers.mine.bukkit.utilities.CompleteDragTask;
import com.elmakers.mine.bukkit.utilities.DataStore;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import com.elmakers.mine.bukkit.utility.Messages;
import com.elmakers.mine.bukkit.utility.NMSUtils;
import com.elmakers.mine.bukkit.utility.URLMap;
import com.elmakers.mine.bukkit.wand.LostWand;
import com.elmakers.mine.bukkit.wand.Wand;
import com.elmakers.mine.bukkit.wand.WandLevel;
import com.elmakers.mine.bukkit.wand.WandMode;
import com.elmakers.mine.bukkit.warp.WarpController;

public class MagicController implements Listener, MageController
{
	public MagicController(final MagicPlugin plugin)
	{
		this.plugin = plugin;
		
		configFolder = plugin.getDataFolder();
		configFolder.mkdirs();

		dataFolder = new File(configFolder, "data");
		dataFolder.mkdirs();

		schematicFolder = new File(configFolder, "schematics");
		schematicFolder.mkdirs();
		
		playerDataFolder = new File(dataFolder, "players");
		playerDataFolder.mkdirs();

		defaultsFolder = new File(configFolder, "defaults");
		defaultsFolder.mkdirs();
	}
	
	public Mage getMage(String mageId, CommandSender commandSender)
	{
		Mage mage = null;
		if (!mages.containsKey(mageId)) 
		{
			mage = new Mage(mageId, this);
			mage.setCommandSender(commandSender);
			if (commandSender instanceof Player) {
				mage.setPlayer((Player)commandSender);
			}
			
			// Check for existing data file
			File playerFile = new File(playerDataFolder, mageId + ".dat");
			if (playerFile.exists()) 
			{
				getLogger().info("Loading player data from file " + playerFile.getName());
				try {
					Configuration playerData = YamlConfiguration.loadConfiguration(playerFile);
					mage.load(playerData);
				} catch (Exception ex) {
					getLogger().warning("Failed to load player data from file " + playerFile.getName());
					ex.printStackTrace();
				}
			}
			
			mages.put(mageId, mage);
		} else {
			mage = mages.get(mageId);
			
			// Re-set command sender to update position
			mage.setCommandSender(commandSender);
			if (commandSender instanceof Player) {
				mage.setPlayer((Player)commandSender);
			}
		}
		return mage;
	}
	
	protected void loadMage(String playerId, ConfigurationSection node)
	{
		Mage mage = getMage(playerId);
		try {
			mage.load(node);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	protected Mage getMage(String mageId)
	{
		return getMage(mageId, null);
	}

	public void addSpell(Spell variant)
	{
		SpellTemplate conflict = spells.get(variant.getKey());
		if (conflict != null)
		{
			getLogger().log(Level.WARNING, "Duplicate spell key: '" + conflict.getKey() + "'");
		}
		else
		{
			spells.put(variant.getKey(), variant);
		}
	}
	
	public float getMaxDamagePowerMultiplier() 
	{
		return maxDamagePowerMultiplier;
	}
	
	public float getMaxConstructionPowerMultiplier() 
	{
		return maxConstructionPowerMultiplier;
	}
	
	public float getMaxRadiusPowerMultiplier() 
	{
		return maxRadiusPowerMultiplier;
	}
	
	public float getMaxRadiusPowerMultiplierMax() 
	{
		return maxRadiusPowerMultiplierMax;
	}
	
	public float getMaxRangePowerMultiplier() 
	{
		return maxRangePowerMultiplier;
	}
	
	public float getMaxRangePowerMultiplierMax() 
	{
		return maxRangePowerMultiplierMax;
	}
	
	public int getAutoUndoInterval() 
	{
		return autoUndo;
	}
	
	public float getMaxPower() 
	{
		return maxPower;
	}
	
	/*
	 * Undo system
	 */

	public int getUndoQueueDepth() 
	{
		return undoQueueDepth;
	}
	
	public int getPendingQueueDepth() 
	{
		return pendingQueueDepth;
	}

	/*
	 * Random utility functions
	 */

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

	public boolean soundsEnabled()
	{
		return soundsEnabled;
	}

	public boolean fillWands()
	{
		return fillingEnabled;
	}

	public boolean bindWands()
	{
		return bindingEnabled;
	}

	public boolean keepWands()
	{
		return keepingEnabled;
	}

	/*
	 * Get the log, if you need to debug or log errors.
	 */
	public Logger getLogger()
	{
		return plugin.getLogger();
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

	protected boolean isRestricted(Material material) 
	{
		return restrictedMaterials.contains(material);		
	}
	
	public boolean hasBuildPermission(Player player, Location location) 
	{
		return hasBuildPermission(player, location.getBlock());
	}

	public boolean hasBuildPermission(Player player, Block block) 
	{
		// Check the region manager, or Factions
		boolean allowed = true;		
		if (bypassBuildPermissions) return true;
		
		allowed = allowed && worldGuardManager.hasBuildPermission(player, block);
		allowed = allowed && factionsManager.hasBuildPermission(player, block);
		
		return allowed;
	}
	
	public boolean schematicsEnabled() {
		return cuboidClipboardClass != null;
	}
	
	public void clearCache() {
		// Only delete schematics that we have builtins for.
		String[] schematicFiles = schematicFolder.list();
		for (String schematicFilename : schematicFiles) {
			if (!schematicFilename.endsWith(".schematic")) continue;
			InputStream builtin = plugin.getResource("schematics/" + schematicFilename);
			if (builtin == null) continue;
			File schematicFile = new File(schematicFolder, schematicFilename);
			schematicFile.delete();
			plugin.getLogger().info("Deleted file " + schematicFile.getAbsolutePath());
		}
		
		schematics.clear();
		for (Mage mage : mages.values()) {
			mage.clearCache();
		}
	}
	
	public WorldEditSchematic loadSchematic(String schematicName) {
		if (schematicName == null || schematicName.length() == 0 || !schematicsEnabled()) return null;
		
		if (schematics.containsKey(schematicName)) {
			WeakReference<WorldEditSchematic> schematic = schematics.get(schematicName);
			if (schematic != null) {
				WorldEditSchematic cached = schematic.get();
				if (cached != null) {
					return cached;
				}
			}
		}

		String fileName = schematicName + ".schematic";
		File schematicFile = new File(schematicFolder, fileName);
		if (!schematicFile.exists()) {
			try {
				// Check extra path first
				File extraSchematicFile = null;
				if (extraSchematicFilePath != null && extraSchematicFilePath.length() > 0) {
					File schematicFolder = new File(configFolder, "../" + extraSchematicFilePath);
					extraSchematicFile = new File(schematicFolder, schematicName + ".schematic");
					getLogger().info("Checking for external schematic: " + extraSchematicFile.getAbsolutePath());
				}
				
				if (extraSchematicFile != null && extraSchematicFile.exists()) {
					schematicFile = extraSchematicFile;
					getLogger().info("Loading file: " + extraSchematicFile.getAbsolutePath());
				}  else {
					plugin.saveResource("schematics/" + fileName, true);
					getLogger().info("Adding builtin schematic: schematics/" + fileName);
				}
			} catch (Exception ex) {
				
			}
		}

		if (!schematicFile.exists()) {
			getLogger().warning("Could not load file: " + schematicFile.getAbsolutePath());
			return null;
		}
				
		try {
			Method loadSchematicMethod = cuboidClipboardClass.getMethod("loadSchematic", File.class);
			getLogger().info("Loading schematic file: " + schematicFile.getAbsolutePath());
			WorldEditSchematic schematic = new WorldEditSchematic(loadSchematicMethod.invoke(null, schematicFile));
			schematics.put(schematicName, new WeakReference<WorldEditSchematic>(schematic));
			return schematic;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return null;
	}

	@Override
	public Collection<String> getBrushKeys() {
		List<String> names = new ArrayList<String>();
		Material[] materials = Material.values();
		for (Material material : materials) {
			// Only show blocks
			if (material.isBlock()) {
				names.add(material.name().toLowerCase());
			}
		}
		
		// Add special materials
		for (String brushName : MaterialBrush.SPECIAL_MATERIAL_KEYS) {
			names.add(brushName.toLowerCase());
		}
		
		// Add schematics
		Collection<String> schematics = getSchematicNames();
		for (String schematic : schematics) {
			names.add("schematic:" + schematic);
		}
		
		return names;
	}

	public Collection<String> getSchematicNames() {
		Collection<String> schematicNames = new ArrayList<String>();
		if (!MaterialBrush.SchematicsEnabled) return schematicNames;
		
		// Load internal schematics.. this may be a bit expensive.
		try {
			CodeSource codeSource = MagicTabExecutor.class.getProtectionDomain().getCodeSource();
			if (codeSource != null) {
				URL jar = codeSource.getLocation();
				ZipInputStream zip = new ZipInputStream(jar.openStream());
				ZipEntry entry = zip.getNextEntry();
				while (entry != null) {
					String name = entry.getName();
					if (name.startsWith("schematics/") && name.endsWith(".schematic")) {
				    	String schematicName = name.replace(".schematic", "").replace("schematics/", "");
				    	schematicNames.add(schematicName);
					}
					entry = zip.getNextEntry();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		// Load external schematics
		try {
			// Check extra path first
			if (extraSchematicFilePath != null && extraSchematicFilePath.length() > 0) {
				File schematicFolder = new File(configFolder, "../" + extraSchematicFilePath);
				for (File schematicFile : schematicFolder.listFiles()) {
					if (schematicFile.getName().endsWith(".schematic")) {
						String schematicName = schematicFile.getName().replace(".schematic", "");
				    	schematicNames.add(schematicName);
					}
				}
			}
		} catch (Exception ex) {
			
		}
		
		return schematicNames;
	}
	
	/*
	 * Internal functions - don't call these, or really anything below here.
	 */
	
	/*
	 * Saving and loading
	 */
	public void initialize()
	{
		crafting = new CraftingController(this);
		enchanting = new EnchantingController(this);
		anvil = new AnvilController(this);
		load();
		
		// Try to link to Essentials:
		Object essentials = plugin.getServer().getPluginManager().getPlugin("Essentials");
		hasEssentials = essentials != null;
		if (hasEssentials) {
			try {
				mailer = new Mailer(essentials);
			} catch (Exception ex) {
				getLogger().warning("Essentials found, but failed to hook up to Mailer");
				mailer = null;
			}
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

		// Check for dtlTraders
		tradersController = null;
		try {
			Plugin tradersPlugin = plugin.getServer().getPluginManager().getPlugin("dtlTraders");
			if (tradersPlugin != null) {
				tradersController = new TradersController();
				tradersController.initialize(this, tradersPlugin);
				getLogger().info("dtlTraders found, integrating for selling Wands, Spells, Brushes and Upgrades");
			}
		} catch (Throwable ex) {
			ex.printStackTrace();
			tradersController = null;
		}
		
		if (tradersController == null) {
			getLogger().info("dtlTraders not found, will not integrate.");
		}
		
		// Try to link to WorldEdit
		// TODO: Make wrapper class to avoid this reflection.
		try {
			cuboidClipboardClass = Class.forName("com.sk89q.worldedit.CuboidClipboard");
			Method loadSchematicMethod = cuboidClipboardClass.getMethod("loadSchematic", File.class);
			if (loadSchematicMethod != null) {
				getLogger().info("WorldEdit found, schematic brushes enabled.");
				MaterialBrush.SchematicsEnabled = true;
				hasWorldEdit = true;
			} else {
				cuboidClipboardClass = null;
			}
		} catch (Throwable ex) {
		}
		
		// Try to link to CommandBook
		hasCommandBook = false;
		try {
			Plugin commandBookPlugin = plugin.getServer().getPluginManager().getPlugin("CommandBook");
			if (commandBookPlugin != null) {
				warpController = new WarpController();
				if (warpController.setCommandBook(commandBookPlugin)) {
					getLogger().info("CommandBook found, integrating for Recall warps");
					hasCommandBook = true;
				} else {
					getLogger().warning("CommandBook integration failed");
				}
			}
		} catch (Throwable ex) {
			
		}
		
		if (cuboidClipboardClass == null) {
			getLogger().info("WorldEdit not found, schematic brushes will not work.");
			MaterialBrush.SchematicsEnabled = false;
			hasWorldEdit = false;
		}
		
		// Link to factions
		factionsManager.initialize(plugin);
		
		// Try to (dynamically) link to WorldGuard:
		worldGuardManager.initialize(plugin);
		
		// Try to link to dynmap:
		try {
			Plugin dynmapPlugin = plugin.getServer().getPluginManager().getPlugin("dynmap");
			if (dynmapPlugin != null) {
				dynmap = new DynmapController(plugin, dynmapPlugin);
			} else {
				dynmap = null;
			}
		} catch (Throwable ex) {
			plugin.getLogger().warning(ex.getMessage());
		}
		
		if (dynmap == null) {
			getLogger().info("dynmap not found, not integrating.");
		} else {
			getLogger().info("dynmap found, integrating.");
		}
		
		// Try to link to Elementals:
		try {
			Plugin elementalsPlugin = plugin.getServer().getPluginManager().getPlugin("Splateds_Elementals");
			if (elementalsPlugin != null) {
				elementals = new ElementalsController(elementalsPlugin);
			} else {
				elementals = null;
			}
		} catch (Throwable ex) {
			plugin.getLogger().warning(ex.getMessage());
		}
		
		if (elementals != null) {
			getLogger().info("Elementals found, integrating.");
		}
		
		// Activate Metrics
		activateMetrics();
		
		// Set up the PlayerSpells timer
		Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			public void run() {
				long threshold = System.currentTimeMillis() - MAGE_FORGET_THRESHOLD;
				for (Entry<String, Long> mageEntry : forgetMages.entrySet()) {
					if (mageEntry.getValue() < threshold)
					mages.remove(mageEntry.getKey());
				}
				forgetMages.clear();
				
				
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
		
		registerListeners();
	}
	
	protected void activateMetrics()
	{
		// Activate Metrics
		final MagicController controller = this;
		metrics = null;
		if (metricsLevel > 0) {
			try {
			    metrics = new Metrics(plugin);
			   
			    if (metricsLevel > 1) {
			    	Graph integrationGraph = metrics.createGraph("Plugin Integration");
			    	integrationGraph.addPlotter(new Metrics.Plotter("Essentials") {						
						@Override public int getValue() { return controller.hasEssentials ? 1 : 0; }
					});
			    	integrationGraph.addPlotter(new Metrics.Plotter("WorldEdit") {						
						@Override public int getValue() { return controller.hasWorldEdit ? 1 : 0; }
					});
			    	integrationGraph.addPlotter(new Metrics.Plotter("Dynmap") {						
						@Override public int getValue() { return controller.hasDynmap ? 1 : 0; }
					});
			    	integrationGraph.addPlotter(new Metrics.Plotter("Factions") {						
						@Override public int getValue() { return controller.factionsManager.isEnabled() ? 1 : 0; }
					});
			    	integrationGraph.addPlotter(new Metrics.Plotter("WorldGuard") {						
						@Override public int getValue() { return controller.worldGuardManager.isEnabled() ? 1 : 0; }
					});
			    	integrationGraph.addPlotter(new Metrics.Plotter("Elementals") {						
						@Override public int getValue() { return controller.elementalsEnabled() ? 1 : 0; }
					});
			    	integrationGraph.addPlotter(new Metrics.Plotter("Traders") {						
						@Override public int getValue() { return controller.tradersController != null ? 1 : 0; }
					});
			    	integrationGraph.addPlotter(new Metrics.Plotter("CommandBook") {						
						@Override public int getValue() { return controller.hasCommandBook ? 1 : 0; }
					});
			    	
			    	Graph featuresGraph = metrics.createGraph("Features Enabled");
			    	featuresGraph.addPlotter(new Metrics.Plotter("Crafting") {						
						@Override public int getValue() { return controller.crafting.isEnabled() ? 1 : 0; }
					});
			    	featuresGraph.addPlotter(new Metrics.Plotter("Enchanting") {						
						@Override public int getValue() { return controller.enchanting.isEnabled() ? 1 : 0; }
					});
			    	featuresGraph.addPlotter(new Metrics.Plotter("Anvil Combining") {						
						@Override public int getValue() { return controller.anvil.isCombiningEnabled() ? 1 : 0; }
					});
			    	featuresGraph.addPlotter(new Metrics.Plotter("Anvil Organizing") {						
						@Override public int getValue() { return controller.anvil.isOrganizingEnabled() ? 1 : 0; }
					});
			    	featuresGraph.addPlotter(new Metrics.Plotter("Anvil Binding") {						
						@Override public int getValue() { return controller.bindingEnabled ? 1 : 0; }
					});
			    	featuresGraph.addPlotter(new Metrics.Plotter("Anvil Keeping") {						
						@Override public int getValue() { return controller.keepingEnabled ? 1 : 0; }
					});
			    }
			    
			    if (metricsLevel > 2) {
			    	Graph categoryGraph = metrics.createGraph("Casts by Category");
			    	for (final SpellCategory category : categories.values()) {
			    		categoryGraph.addPlotter(new DeltaPlotter(new CategoryCastPlotter(category)));
			    	}
			    	
			    	Graph totalCategoryGraph = metrics.createGraph("Total Casts by Category");
			    	for (final SpellCategory category : categories.values()) {
			    		totalCategoryGraph.addPlotter(new CategoryCastPlotter(category));
			    	}
			    }

			    if (metricsLevel > 3) {
			    	Graph spellGraph = metrics.createGraph("Casts");
			    	for (final SpellTemplate spell : spells.values()) {
			    		if (!(spell instanceof Spell)) continue;
			    		spellGraph.addPlotter(new DeltaPlotter(new SpellCastPlotter((Spell)spell)));
			    	}
			    	
			    	Graph totalCastGraph = metrics.createGraph("Total Casts");
			    	for (final SpellTemplate spell : spells.values()) {
			    		if (!(spell instanceof Spell)) continue;
			    		totalCastGraph.addPlotter(new SpellCastPlotter((Spell)spell));
			    	}
			    }
			    
			    metrics.start();
			    plugin.getLogger().info("Activated MCStats");
			} catch (Exception ex) {
			    plugin.getLogger().warning("Failed to load MCStats: " + ex.getMessage());
			}
		}
	}
	
	protected void registerListeners() {
		PluginManager pm = plugin.getServer().getPluginManager();
		pm.registerEvents(this, plugin);
		pm.registerEvents(crafting, plugin);
		pm.registerEvents(enchanting, plugin);
		pm.registerEvents(anvil, plugin);
	}
	
	public Collection<Mage> getPending() {
		return pendingConstruction.values();
	}
	
	protected void addPending(Mage mage) {
		pendingConstruction.put(mage.getName(), mage);
	}
	
	protected void removePending(Mage mage) {
		pendingConstruction.remove(mage.getName());
	}
	
	public boolean removeMarker(String id, String group)
	{
		boolean removed = false;
		if (dynmap != null && dynmapShowWands) 
		{
			return dynmap.removeMarker(id, group);
		}
		
		return removed;
	}
	
	public boolean addMarker(String id, String group, String title, String world, int x, int y, int z, String description)
	{
		boolean created = false;
		if (dynmap != null && dynmapShowWands)
		{
			created = dynmap.addMarker(id, group, title, world, x, y, z, description);
		}
		
		return created;
	}
	
	protected File getDataFile(String fileName)
	{
		return new File(dataFolder, fileName + ".yml");
	}
	
	protected ConfigurationSection loadDataFile(String fileName)
	{
		File dataFile = getDataFile(fileName);
		if (!dataFile.exists()) {
			return null;
		}
		Configuration configuration = YamlConfiguration.loadConfiguration(dataFile);
		return configuration;
	}
	
	protected DataStore createDataFile(String fileName)
	{
		File dataFile = new File(dataFolder, fileName + ".yml");
		DataStore configuration = new DataStore(getLogger(), dataFile);
		return configuration;
	}

	protected ConfigurationSection loadConfigFile(String fileName, boolean loadDefaults)
	{
		String configFileName = fileName + ".yml";
		File configFile = new File(configFolder, configFileName);
		if (!configFile.exists()) {
			getLogger().info("Saving template " + configFileName + ", edit to customize configuration.");
			plugin.saveResource(configFileName, false);
		}

		String defaultsFileName = "defaults/" + fileName + ".defaults.yml";
		plugin.saveResource(defaultsFileName, true);

		getLogger().info("Loading " + configFile.getName());
		Configuration config = YamlConfiguration.loadConfiguration(configFile);
		
		if (!loadDefaults) {
			return config;
		}
		
		Configuration defaultConfig = YamlConfiguration.loadConfiguration(plugin.getResource(defaultsFileName));
		ConfigurationUtils.addConfigurations(defaultConfig, config);
		return defaultConfig;
	}
	
	public void loadConfiguration()
	{
		// Clear some cache stuff... mainly this is for debuggin/testing.
		schematics.clear();
		
		// Load main configuration
		try {
			loadProperties(loadConfigFile(CONFIG_FILE, true));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		// Load localizations
		try {
			Messages.reset();
			Messages.load(loadConfigFile(MESSAGES_FILE, true));
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// Load materials configuration
		try {
			loadMaterials(loadConfigFile(MATERIALS_FILE, true));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		// Load spells
		try {
			loadSpells(loadConfigFile(SPELLS_FILE, loadDefaultSpells));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		// Load wand templates
		try {
			Wand.loadTemplates(loadConfigFile(WANDS_FILE, loadDefaultWands));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		getLogger().info("Magic: Loaded " + spells.size() + " spells and " + Wand.getWandTemplates().size() + " wands");
	}
	
	protected void loadSpellData()
	{
		try {
			ConfigurationSection configNode = loadDataFile(SPELLS_DATA_FILE);
			if (configNode == null) return;

			Set<String> keys = configNode.getKeys(false);
			for (String key : keys) {					
				SpellTemplate spell = getSpellTemplate(key);
				
				// Bit hacky to use the Spell load method on a SpellTemplate, but... meh!
				if (spell != null && spell instanceof MageSpell) {
					ConfigurationSection spellSection = configNode.getConfigurationSection(key);
					((MageSpell)spell).load(spellSection);
				}
			}
		} catch (Exception ex) {
			getLogger().warning("Failed to load spell metrics");
		}
	}
	
	public void load()
	{
		loadConfiguration();
		loadSpellData();
		
		File[] playerFiles = playerDataFolder.listFiles(new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return name.toLowerCase().endsWith(".dat");
		    }
		});
		
		for (File playerFile : playerFiles)
		{
			// Skip if older than 2 days
			if (playerDataThreshold > 0 && playerFile.lastModified() < System.currentTimeMillis() - playerDataThreshold) continue;
			
			Configuration playerData = YamlConfiguration.loadConfiguration(playerFile);
			if (playerData.contains("scheduled") && playerData.getList("scheduled").size() > 0) {
				String playerId = playerFile.getName().replaceFirst("[.][^.]+$", "");
				loadMage(playerId, playerData);
			}
		}
		
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			public void run() {
				// Load lost wands
				getLogger().info("Loading lost wand data");
				loadLostWands();
				
				// Load toggle-on-load blocks
				getLogger().info("Loading automata data");
				loadAutomata();
				
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
				
				getLogger().info("Finished loading data.");
			}
		}, 10);
	}

	protected void loadLostWands()
	{
		try {
			ConfigurationSection lostWandConfiguration = loadDataFile(LOST_WANDS_FILE);
			if (lostWandConfiguration != null)
			{
				Set<String> wandIds = lostWandConfiguration.getKeys(false);
				for (String wandId : wandIds) {
					LostWand lostWand = new LostWand(wandId, lostWandConfiguration.getConfigurationSection(wandId));
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
		
		getLogger().info("Loaded " + lostWands.size() + " lost wands");
	}
	
	protected void saveSpellData()
	{
		String lastKey = "";
		try {
			DataStore spellsDataFile = createDataFile(SPELLS_DATA_FILE);
			for (SpellTemplate spell : spells.values()) {
				lastKey = spell.getKey();
				ConfigurationSection spellNode = spellsDataFile.createSection(lastKey);
				if (spellNode == null) {
					getLogger().warning("Error saving spell data for " + lastKey);
					continue;
				}
				// Hackily re-using save
				if (spell != null && spell instanceof MageSpell) {
					((MageSpell)spell).save(spellNode);
				}
			}
			spellsDataFile.save();
		} catch (Throwable ex) {
			getLogger().warning("Error saving spell data for " + lastKey);
			ex.printStackTrace();
		}
	}
	
	protected void saveLostWands() {
		String lastKey = "";
		try {
			DataStore lostWandsConfiguration = createDataFile(LOST_WANDS_FILE);
			for (Entry<String, LostWand> wandEntry : lostWands.entrySet()) {
				lastKey = wandEntry.getKey();
				ConfigurationSection wandNode = lostWandsConfiguration.createSection(lastKey);
				if (wandNode == null) {
					getLogger().warning("Error saving lost wand data for " + lastKey);
					continue;
				}
				if (!wandEntry.getValue().isValid()) {
					getLogger().warning("Invalid lost and data for " + lastKey);
					continue;
				}
				wandEntry.getValue().save(wandNode);
			}
			lostWandsConfiguration.save();
		} catch (Throwable ex) {
			getLogger().warning("Error saving lost wand data for " + lastKey);
			ex.printStackTrace();
		}
	}

	protected void loadAutomata()
	{
		int automataCount = 0;
		try {
			ConfigurationSection toggleBlockData = loadDataFile(AUTOMATA_FILE);
			if (toggleBlockData != null)
			{
				Set<String> chunkIds = toggleBlockData.getKeys(false);
				for (String chunkId : chunkIds) {
					ConfigurationSection chunkNode = toggleBlockData.getConfigurationSection(chunkId);
					Map<Long, Automaton> restoreChunk = new HashMap<Long, Automaton>();
					automata.put(chunkId, restoreChunk);
					Set<String> blockIds = chunkNode.getKeys(false);
					for (String blockId : blockIds) {
						ConfigurationSection toggleConfig = chunkNode.getConfigurationSection(blockId);
						Automaton toggle = new Automaton(toggleConfig);
						restoreChunk.put(toggle.getId(), toggle);
						automataCount++;
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		getLogger().info("Loaded " + automataCount + " automata");
	}
	
	protected void saveAutomata()
	{
		try {
			DataStore automataData = createDataFile(AUTOMATA_FILE);
			for (Entry<String, Map<Long, Automaton>> toggleEntry : automata.entrySet()) {
				Collection<Automaton> blocks = toggleEntry.getValue().values();
				if (blocks.size() > 0) {
					ConfigurationSection chunkNode = automataData.createSection(toggleEntry.getKey());
					for (Automaton block : blocks) {
						ConfigurationSection node = chunkNode.createSection(Long.toString(block.getId()));
						block.save(node);
					}
				}
			}
			automataData.save();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
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
		if (!wand.hasId()) return false;
		
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
	
	public WandMode getDefaultWandMode() {
		return defaultWandMode;
	}
	
	protected void savePlayerData() {
		try {
			for (Entry<String, Mage> mageEntry : mages.entrySet()) {
				File playerData = new File(playerDataFolder, mageEntry.getKey() + ".dat");
				DataStore playerConfig = new DataStore(getLogger(), playerData);
				Mage mage = mageEntry.getValue();
				mage.save(playerConfig);
				playerConfig.save();
				
				// Check for players we can forget
				Player player = mage.getPlayer();
				if (player != null && !player.isOnline() && !player.hasMetadata("NPC")) {
					UndoQueue undoQueue = mage.getUndoQueue();
					if (undoQueue == null || undoQueue.isEmpty()) {
						getLogger().info("Offline player " + player.getName() + " has no pending undo actions, forgetting");
						forgetMages.put(mageEntry.getKey(), 0l);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		// Forget players we don't need to keep in memory
		for (String forgetId : forgetMages.keySet()) {
			mages.remove(forgetId);
		}
		forgetMages.clear();
	}
	
	public void save()
	{
		getLogger().info("Saving player data");
		savePlayerData();
		
		getLogger().info("Saving spell data");
		saveSpellData();

		getLogger().info("Saving lost wands data");
		saveLostWands();

		getLogger().info("Saving image map data");
		URLMap.save();

		getLogger().info("Saving automata data");
		saveAutomata();
	}
	
	protected void loadSpells(ConfigurationSection config)
	{
		if (config == null) return;
		
		// Reset existing spells.
		spells.clear();
		
		Set<String> spellKeys = config.getKeys(false);
		for (String key : spellKeys)
		{
			ConfigurationSection spellNode = config.getConfigurationSection(key);
			if (!spellNode.getBoolean("enabled", true)) {
				continue;
			}
			
			Spell newSpell = loadSpell(key, spellNode, this);
			if (newSpell == null)
			{
				getLogger().warning("Magic: Error loading spell " + key);
				continue;
			}
			addSpell(newSpell);
		}
		
		// Update registered mages so their spells are current
		for (Mage mage : mages.values()) {
			mage.loadSpells(config);
		}
	}

	public static Spell loadSpell(String name, ConfigurationSection node, MageController controller)
	{
		String className = node.getString("class");
		if (className == null) return null;

		if (className.indexOf('.') <= 0)
		{
			className = BUILTIN_SPELL_CLASSPATH + "." + className;
		}

		Class<?> spellClass = null;
		try
		{
			spellClass = Class.forName(className);
		}
		catch (Throwable ex)
		{
			controller.getLogger().warning("Error loading spell: " + className);
			ex.printStackTrace();
			return null;
		}

		Object newObject;
		try
		{
			newObject = spellClass.newInstance();
		}
		catch (Throwable ex)
		{

			controller.getLogger().warning("Error loading spell: " + className);
			ex.printStackTrace();
			return null;
		}

		if (newObject == null || !(newObject instanceof MageSpell))
		{
			controller.getLogger().warning("Error loading spell: " + className + ", does it implement MageSpell?");
			return null;
		}

		MageSpell newSpell = (MageSpell)newObject;
		newSpell.initialize(controller);
		newSpell.loadTemplate(name, node);
		com.elmakers.mine.bukkit.api.spell.SpellCategory category = newSpell.getCategory();
		if (category instanceof SpellCategory) {
			((SpellCategory)category).addSpellTemplate(newSpell);
		}
		return newSpell;
	}
	
	protected void loadMaterials(ConfigurationSection materialNode)
	{
		if (materialNode == null) return;
		
		Set<String> keys = materialNode.getKeys(false);
		for (String key : keys) {
			materialSets.put(key, ConfigurationUtils.getMaterials(materialNode, key));
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
	
	protected void loadProperties(ConfigurationSection properties)
	{
		if (properties == null) return;

		// Cancel any pending configurable tasks
		if (autoSaveTaskId > 0) {
			Bukkit.getScheduler().cancelTask(autoSaveTaskId);
			autoSaveTaskId = 0;
		}
		
		loadDefaultSpells = properties.getBoolean("load_default_spells", loadDefaultSpells);
		loadDefaultWands = properties.getBoolean("load_default_wands", loadDefaultWands);
		maxTNTPerChunk = properties.getInt("max_tnt_per_chunk", maxTNTPerChunk);
		undoQueueDepth = properties.getInt("undo_depth", undoQueueDepth);
		pendingQueueDepth = properties.getInt("pending_depth", pendingQueueDepth);
		undoMaxPersistSize = properties.getInt("undo_max_persist_size", undoMaxPersistSize);
		commitOnQuit = properties.getBoolean("commit_on_quit", commitOnQuit);
		playerDataThreshold = (long)(properties.getDouble("undo_max_persist_size", 0) * 1000 * 24 * 3600);
		defaultWandMode = Wand.parseWandMode(properties.getString("default_wand_mode", ""), defaultWandMode);
		showMessages = properties.getBoolean("show_messages", showMessages);
		showCastMessages = properties.getBoolean("show_cast_messages", showCastMessages);
		clickCooldown = properties.getInt("click_cooldown", clickCooldown);
		messageThrottle = properties.getInt("message_throttle", 0);
		maxBlockUpdates = properties.getInt("max_block_updates", maxBlockUpdates);
		ageDroppedItems = properties.getInt("age_dropped_items", ageDroppedItems);
		enableItemHacks = properties.getBoolean("enable_custom_item_hacks", enableItemHacks);
		soundsEnabled = properties.getBoolean("sounds", soundsEnabled);
		fillingEnabled = properties.getBoolean("fill_wands", fillingEnabled);
		indestructibleWands = properties.getBoolean("indestructible_wands", indestructibleWands);
		keepWandsOnDeath = properties.getBoolean("keep_wands_on_death", keepWandsOnDeath);
		welcomeWand = properties.getString("welcome_wand", "");
		maxDamagePowerMultiplier = (float)properties.getDouble("max_power_damage_multiplier", maxDamagePowerMultiplier);
		maxConstructionPowerMultiplier = (float)properties.getDouble("max_power_construction_multiplier", maxConstructionPowerMultiplier);
		maxRangePowerMultiplier = (float)properties.getDouble("max_power_range_multiplier", maxRangePowerMultiplier);
		maxRangePowerMultiplierMax = (float)properties.getDouble("max_power_range_multiplier_max", maxRangePowerMultiplierMax);
		maxRadiusPowerMultiplier = (float)properties.getDouble("max_power_radius_multiplier", maxRadiusPowerMultiplier);
		maxRadiusPowerMultiplierMax = (float)properties.getDouble("max_power_radius_multiplier_max", maxRadiusPowerMultiplierMax);
		maxPower = (float)properties.getDouble("max_power", maxPower);
		costReduction = (float)properties.getDouble("cost_reduction", costReduction);
		cooldownReduction = (float)properties.getDouble("cooldown_reduction", cooldownReduction);
		castCommandCostReduction = (float)properties.getDouble("cast_command_cost_reduction", castCommandCostReduction);
		castCommandCooldownReduction = (float)properties.getDouble("cast_command_cooldown_reduction", castCommandCooldownReduction);
		castCommandPowerMultiplier = (float)properties.getDouble("cast_command_power_multiplier", castCommandPowerMultiplier);
		autoUndo = properties.getInt("auto_undo", autoUndo);
		bindingEnabled = properties.getBoolean("enable_binding", bindingEnabled);
		keepingEnabled = properties.getBoolean("enable_keeping", keepingEnabled);
		essentialsSignsEnabled = properties.getBoolean("enable_essentials_signs", essentialsSignsEnabled);
		dynmapShowWands = properties.getBoolean("dynmap_show_wands", dynmapShowWands);
		dynmapShowSpells = properties.getBoolean("dynmap_show_spells", dynmapShowSpells);
		dynmapUpdate = properties.getBoolean("dynmap_update", dynmapUpdate);
		bypassBuildPermissions = properties.getBoolean("bypass_build", bypassBuildPermissions);
		bypassPvpPermissions = properties.getBoolean("bypass_pvp", bypassPvpPermissions);
		extraSchematicFilePath = properties.getString("schematic_files", extraSchematicFilePath);
		createWorldsEnabled = properties.getBoolean("enable_world_creation", createWorldsEnabled);

		messagePrefix = properties.getString("message_prefix", messagePrefix);
		castMessagePrefix = properties.getString("cast_message_prefix", castMessagePrefix);

		messagePrefix = ChatColor.translateAlternateColorCodes('&', messagePrefix);
		castMessagePrefix = ChatColor.translateAlternateColorCodes('&', castMessagePrefix);
		
		worldGuardManager.setEnabled(properties.getBoolean("region_manager_enabled", factionsManager.isEnabled()));
		factionsManager.setEnabled(properties.getBoolean("factions_enabled", factionsManager.isEnabled()));
		
		metricsLevel = properties.getInt("metrics_level", metricsLevel);
		
		if (properties.contains("mana_display")) {
			Wand.retainLevelDisplay = properties.getString("mana_display").equals("hybrid");
			Wand.displayManaAsBar = !properties.getString("mana_display").equals("number");
		}
		
		// Parse wand settings
		Wand.DefaultUpgradeMaterial = ConfigurationUtils.getMaterial(properties, "wand_upgrade_item", Wand.DefaultUpgradeMaterial);
		Wand.EnableGlow = properties.getBoolean("enable_glow", Wand.EnableGlow);
		MaterialBrush.CopyMaterial = ConfigurationUtils.getMaterial(properties, "copy_item", MaterialBrush.CopyMaterial);
		MaterialBrush.EraseMaterial = ConfigurationUtils.getMaterial(properties, "erase_item", MaterialBrush.EraseMaterial);
		MaterialBrush.CloneMaterial = ConfigurationUtils.getMaterial(properties, "clone_item", MaterialBrush.CloneMaterial);
		MaterialBrush.ReplicateMaterial = ConfigurationUtils.getMaterial(properties, "replicate_item", MaterialBrush.ReplicateMaterial);
		MaterialBrush.SchematicMaterial = ConfigurationUtils.getMaterial(properties, "schematic_item", MaterialBrush.SchematicMaterial);
		MaterialBrush.MapMaterial = ConfigurationUtils.getMaterial(properties, "map_item", MaterialBrush.MapMaterial);
		
		// Set up other systems
		EffectPlayer.SOUNDS_ENABLED = soundsEnabled;
		
		// Set up auto-save timer
		final MagicController saveController = this;
		int autoSaveIntervalTicks = properties.getInt("auto_save", 0) * 20 / 1000;;
		if (autoSaveIntervalTicks > 1) {
			autoSaveTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, 
					new Runnable() {
						public void run() {
							saveController.getLogger().info("Auto-saving Magic data");
							saveController.save();
							saveController.getLogger().info("... Done auto-saving.");
						}
					}, 
					autoSaveIntervalTicks, autoSaveIntervalTicks);
		}
		
		// Set up WandLevel limits
		WandLevel.load(properties);
		
		// Load sub-controllers
		crafting.load(properties);
		enchanting.load(properties);
		anvil.load(properties);
	}

	protected void clear()
	{
		mages.clear();
		pendingConstruction.clear();
		spells.clear();
	}
	
	protected void unregisterPhysicsHandler(Listener listener)
	{
		BlockPhysicsEvent.getHandlerList().unregister(listener);
		physicsHandler = null;
	}
	
	protected void registerForUndo(Mage mage)
	{
		pendingUndo.add(mage.getId());
	}

	public boolean hasWandPermission(Player player)
	{
		return hasPermission(player, "Magic.wand.use", true);
	}

	public boolean hasPermission(Player player, String pNode, boolean defaultValue)
	{
		// Should this return defaultValue? Can't give perms to console.
		if (player == null) return true;
		
		// Seems like the GM should handle this, but no?
		// I mean.. really? It Essentials GM doesn't handle wildcards? Holy cow...
		if (pNode.contains(".")) {
			String parentNode = pNode.substring(0, pNode.lastIndexOf('.') + 1) + "*";
			boolean isParentSet = player.isPermissionSet(parentNode);
			if (isParentSet) {
				defaultValue = player.hasPermission(parentNode);
			}
		}
		
		boolean isSet = player.isPermissionSet(pNode);
		if (defaultValue) {
			return isSet ? player.hasPermission(pNode) : defaultValue;
		}
		return player.hasPermission(pNode);
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
	public void onEntityChangeBlockEvent(EntityChangeBlockEvent event) {
		Entity entity = event.getEntity();

		if (entity instanceof FallingBlock) {
			if (event.getEntity().hasMetadata("MagicBlockList")) {
				List<MetadataValue> values = entity.getMetadata("MagicBlockList");  
				for (MetadataValue value : values) {
					if (value.getOwningPlugin() == plugin) {
						UndoList blockList = (UndoList)value.value();
						blockList.convert(entity, event.getBlock());
					}
				}
			} else {
				registerFallingBlock(entity, event.getBlock());
			}
		}
	}
	
	protected void registerFallingBlock(Entity fallingBlock, Block block) {
		long now = System.currentTimeMillis();
		Collection<String> keys = new ArrayList<String>(pendingUndo);
		
		for (String key : keys) {
			if (mages.containsKey(key)) {
				Mage mage = mages.get(key);
				UndoList lastUndo = mage.getLastUndoList();
				if (lastUndo == null || lastUndo.getModifiedTime() < now - undoTimeWindow) {
					pendingUndo.remove(key);
				} else if (lastUndo.contains(fallingBlock.getLocation(), undoBlockBorderSize)) {
					lastUndo.fall(fallingBlock, block);
				}
			} else {
				pendingUndo.remove(key);
			}
		}
	}
	
	@EventHandler
	public void onInventoryDrag(InventoryDragEvent event) {
		if (!enableItemHacks || event.isCancelled()) return;
		
		// this is a huge hack! :\
		// I apologize for any weird behavior this causes.
		// Bukkit, unfortunately, will blow away NBT data for anything you drag
		// Which will nuke a wand or spell.
		// To make matters worse, Bukkit passes a copy of the item in the event, so we can't 
		// even check for metadata and only cancel the event if it involves one of our special items.
		// The best I can do is look for metadata at all, since Bukkit will retain the name and lore.
		
		// I have now decided to copy over the CB default handler for this, and cancel the event.
		// The only change I have made is that *real* ItemStack copies are made, instead of shallow Bukkit ones.
		ItemStack oldStack = event.getOldCursor();
		HumanEntity entity = event.getWhoClicked();
		if (oldStack != null && oldStack.hasItemMeta() && entity instanceof Player) {
			// Only do this if we're only dragging one item, since I don't 
			// really know what happens or how you would drag more than one.
			Map<Integer, ItemStack> draggedSlots = event.getNewItems();
			if (draggedSlots.size() != 1) return;
			
			event.setCancelled(true);
			
			// Cancelling the event will put the item back on the cursor,
			// and skip updating the inventory.
			
			// So we will wait one tick and then fix this up using the original item.
			InventoryView view = event.getView();
			for (Integer dslot : draggedSlots.keySet()) {
				CompleteDragTask completeDrag = new CompleteDragTask((Player)entity, view, dslot);
				completeDrag.runTaskLater(plugin, 1);
            }
			
			return;
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (event.isCancelled()) return;
		Entity entity = event.getEntity();
		if (entity instanceof Projectile || entity instanceof TNTPrimed) return;
		
		Entity damager = event.getDamager();
		UndoList undoList = getEntityUndo(damager);
		if (undoList != null) {
			undoList.modify(entity);
		}
	}
	
	protected UndoList getEntityUndo(Entity entity) {
		UndoList blockList = null;
		if (entity == null) return null;
		if (entity instanceof Player) {
			Mage mage = getMage((Player)entity);
			
			// Hm, kinda hacky.
			Object lastUndo = mage.getLastUndoList();
			if (lastUndo instanceof UndoList) {
				blockList = (UndoList)lastUndo;
			}
		} else if (entity.hasMetadata("MagicBlockList")) {
			List<MetadataValue> values = entity.getMetadata("MagicBlockList");  
			for (MetadataValue value : values) {
				if (value.getOwningPlugin() == plugin) {
					blockList = (UndoList)value.value();
				}
			}
		}
		
		return blockList;
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onEntityExplode(EntityExplodeEvent event) {
		Entity explodingEntity = event.getEntity();
		if (explodingEntity == null) return;
		
		UndoList blockList = getEntityUndo(explodingEntity);
		
		if (event.isCancelled()) {
			if (blockList != null) blockList.cancelExplosion(explodingEntity);
		}
		else if (maxTNTPerChunk > 0 && explodingEntity.getType() == EntityType.PRIMED_TNT) {
			Chunk chunk = explodingEntity.getLocation().getChunk();
			if (chunk == null || !chunk.isLoaded()) return;
			
			int tntCount = 0;
			Entity[] entities = chunk.getEntities();
			for (Entity entity : entities) {
				if (entity != null && entity.getType() == EntityType.PRIMED_TNT) {
					tntCount++;
				}
			}
			if (tntCount > maxTNTPerChunk) {
				event.setCancelled(true);
				if (blockList != null) blockList.cancelExplosion(explodingEntity);
			} else {
				if (blockList != null) blockList.explode(explodingEntity, event.blockList());
			}
		} 
		else if (blockList != null) {
			blockList.explode(explodingEntity, event.blockList());
		}
	}
	
	protected void onPlayerActivateIcon(Mage mage, Wand activeWand, ItemStack icon)
	{
		// Check for spell or material selection
		if (icon != null && icon.getType() != Material.AIR) {
			com.elmakers.mine.bukkit.api.spell.Spell spell = mage.getSpell(Wand.getSpell(icon));
			if (spell != null) {
				activeWand.saveInventory();
				activeWand.setActiveSpell(spell.getKey());
				
				// Reset the held item, Bukkit may have replaced it (?)
				mage.getPlayer().setItemInHand(activeWand.getItem());
			} else if (Wand.isBrush(icon)){
				activeWand.saveInventory();
				activeWand.activateBrush(icon);
				
				// Reset the held item, Bukkit may have replaced it (?)
				mage.getPlayer().setItemInHand(activeWand.getItem());
			}
		} else {
			activeWand.setActiveSpell("");
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
				// Check for spell or material selection
				onPlayerActivateIcon(mage, activeWand, next);
				
				event.setCancelled(true);
				return;
			} else {
				// Otherwise, we're switching away from the wand, so deactivate it.
				activeWand.deactivate();
			}
		}
		
		// If we're switching to a wand, activate it.
		if (next != null && Wand.isWand(next)) {
			Wand newWand = new Wand(this, next);
			newWand.activate(mage, next);			
		}
		
		// Check for map selection if no wand is active
		activeWand = mage.getActiveWand();
		if (activeWand == null && next != null) {
			if (next.getType() == Material.MAP) {
				mage.setLastHeldMapId(next.getDurability());
			}
		}
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event)
	{
		Player player = event.getPlayer();
		Mage mage = getMage(player);
		final Wand activeWand = mage.getActiveWand();
		if (activeWand != null) {
			ItemStack droppedItem = event.getItemDrop().getItemStack();
			ItemStack inHand = event.getPlayer().getInventory().getItemInHand();
			// Kind of a hack- check if we just dropped a wand, and now have an empty hand
			if (Wand.isWand(droppedItem) && (inHand == null || inHand.getType() == Material.AIR)) {
				activeWand.deactivate();
				// Clear after inventory restore (potentially with deactivate), since that will put the wand back
				if (Wand.hasActiveWand(player)) {
					player.setItemInHand(new ItemStack(Material.AIR, 1));
				}
			} else if (activeWand.isInventoryOpen()) {
				// The item is already removed from the wand's inventory, but that should be ok
				removeItemFromWand(activeWand, droppedItem);
				
				// Cancelling the event causes some really strange behavior, including the item
				// being put back in the inventory.
				// So instead of cancelling, we'll try and update the returned item in place.
				
			}
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
			// Retrieve stored inventory before deactivating the wand
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
		
		List<ItemStack> oldDrops = new ArrayList<ItemStack>(drops);
		final List<ItemStack> keepWands = new ArrayList<ItemStack>();
		drops.clear();
		for (ItemStack itemStack : oldDrops)
		{
			boolean keepItem = false;
			if (Wand.isWand(itemStack)) {
				keepItem = keepWandsOnDeath;	
				if (!keepItem) {
					Wand testWand = new Wand(this, itemStack);
					keepItem = testWand.keepOnDeath();
				}
			}
			if (keepItem)
			{
				keepWands.add(itemStack);
			}
			else
			{
				drops.add(itemStack);
			}
		}
		if (keepWands.size() > 0)
		{
			Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
				public void run() {
					for (ItemStack itemStack : keepWands)
						player.getInventory().addItem(itemStack);
					}
				}
			, 5);
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
		if (Wand.isWand(event.getEntity().getItemStack()))
		{
			Wand wand = new Wand(this, event.getEntity().getItemStack());			
			if (wand.isIndestructible()) {
				event.getEntity().setTicksLived(1);
				event.setCancelled(true);
			} else if (dynmapShowWands) {
				removeLostWand(wand);
			}
		}
	}
	
	@EventHandler
	public void onItemSpawn(ItemSpawnEvent event)
	{
		if (Wand.isWand(event.getEntity().getItemStack()))
		{
			Wand wand = new Wand(this, event.getEntity().getItemStack());
			if (wand != null && wand.isIndestructible()) {
				InventoryUtils.setInvulnerable(event.getEntity());

				// Don't show non-indestructible wands on dynmap
				addLostWand(wand, event.getEntity().getLocation());		
				Location dropLocation = event.getLocation();
				getLogger().info("Wand " + wand.getName() + ", id " + wand.getId() + " spawned at " + dropLocation.getBlockX() + " " + dropLocation.getBlockY() + " " + dropLocation.getBlockZ());
			}
		} else  {
			registerEntityForUndo(event.getEntity());
			if (ageDroppedItems > 0) {
				int ticks = ageDroppedItems * 20 / 1000;
				Item item = event.getEntity();
				ageItem(item, ticks);
			}
		}
	}

	protected void registerEntityForUndo(Entity entity) {
		long now = System.currentTimeMillis();
		Collection<String> keys = new ArrayList<String>(pendingUndo);
		
		for (String key : keys) {
			if (mages.containsKey(key)) {
				Mage mage = mages.get(key);
				UndoList lastUndo = mage.getLastUndoList();
				if (lastUndo == null || lastUndo.getModifiedTime() < now - undoTimeWindow) {
					pendingUndo.remove(key);
				} else if (lastUndo.contains(entity.getLocation(), undoBlockBorderSize)) {
					lastUndo.add(entity);
				}
			} else {
				pendingUndo.remove(key);
			}
		}
	}
	
	protected void ageItem(Item item, int ticksToAge)
	{
		try {
			Class<?> itemClass = NMSUtils.getBukkitClass("net.minecraft.server.EntityItem");
			Object handle = NMSUtils.getHandle(item);
			Field ageField = itemClass.getDeclaredField("age");
			ageField.setAccessible(true);
			ageField.set(handle, ticksToAge);
		} catch (Exception ex) {
			ex.printStackTrace();
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
	        if (entity instanceof Item)
	        {
	   		 	Item item = (Item)entity;
	   		 	ItemStack itemStack = item.getItemStack();
	            if (Wand.isWand(itemStack))
	            {
                	Wand wand = new Wand(this, item.getItemStack());
	            	if (wand.isIndestructible()) {
	                     event.setCancelled(true);
	            	} else if (event.getDamage() >= itemStack.getDurability()) {
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
	
	@EventHandler(priority=EventPriority.LOW)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.isCancelled())
            return;
        
        // Check for clicking on a Citizens NPC, in case
        // this hasn't been cancelled yet
        if (event.getRightClicked().hasMetadata("NPC")) {
        	Player player = event.getPlayer();		
    		Mage mage = getMage(player);
        	Wand wand = mage.getActiveWand();
        	if (wand != null) {
        		wand.closeInventory();
        	}
        	
        	// Don't let it re-open right away
        	mage.checkLastClick(0);
        }
    }

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		// Note that an interact on air event will arrive pre-cancelled
		// So this is kind of useless. :\
		//if (event.isCancelled()) return;
		
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
		
		if (wand == null) return;
		
		if (!hasWandPermission(player))
		{
			// Check for self-destruct
			if (hasPermission(player, "Magic.wand.destruct", false)) {
				wand.deactivate();
				PlayerInventory inventory = player.getInventory();
				ItemStack[] items = inventory.getContents();
				for (int i = 0; i < items.length; i++) {
					ItemStack item = items[i];
					if (Wand.isWand(item) || Wand.isSpell(item) || Wand.isBrush(item)) {
						items[i] = null;
					}
				}
				inventory.setContents(items);
				mage.sendMessage(Messages.get("wand.self_destruct"));
			}
			return;
		}
		
		if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK && !wand.isUpgrade())
		{
			// Don't allow casting if the player is confused
			if (!mage.isSuperPowered() && !mage.isSuperProtected() && player.hasPotionEffect(PotionEffectType.CONFUSION)) {
				if (soundsEnabled) {
					player.playEffect(EntityEffect.HURT);
				}
				return;
			}
			wand.cast();
			event.setCancelled(true);
			return;
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
				if (wand.getMode() == WandMode.CYCLE) {
					if (player.isSneaking()) {
						com.elmakers.mine.bukkit.api.spell.Spell activeSpell = wand.getActiveSpell();
						boolean cycleMaterials = false;
						if (activeSpell != null) {
							cycleMaterials = activeSpell.hasBrushOverride() && wand.getBrushes().size() > 0;
						}
						if (cycleMaterials) {
							wand.cycleMaterials(player.getItemInHand());
						} else {
							wand.cycleSpells(player.getItemInHand());
						}
					} else { 
						wand.cycleSpells(player.getItemInHand());
					}
				} else {
					wand.toggleInventory();
				}
				event.setCancelled(true);
			} else {
				mage.playSound(Sound.NOTE_BASS, 1.0f, 0.7f);
			}
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
		} else if (mage.isNewPlayer() && welcomeWand.length() > 0) {
			wand = Wand.createWand(this, welcomeWand);
			if (wand != null) {
				giveItemToPlayer(player, wand.getItem());
				getLogger().info("Gave welcome wand " + wand.getName() + " to " + player.getName());
			} else {
				getLogger().warning("Unable to give welcome wand '" + welcomeWand + "' to " + player.getName());
			}
		}
		
		Collection<Spell> spells = mage.getSpells();
		for (Spell spell : spells) {
			// Reactivate spells that were active on logout.
			if (spell.isActive()) {
				mage.sendMessage(Messages.get("spell.reactivate").replace("$name", spell.getName()));
				mage.activateSpell(spell);
			}
		}
	}
	
	@Override
	public void giveItemToPlayer(Player player, ItemStack itemStack) {
		// Place directly in hand if possible
		PlayerInventory inventory = player.getInventory();
		ItemStack inHand = inventory.getItemInHand();
		if (inHand == null || inHand.getType() == Material.AIR) {
			inventory.setItem(inventory.getHeldItemSlot(), itemStack);
			if (Wand.isWand(itemStack)) {
				Wand wand = new Wand(this, itemStack);
				wand.activate(this.getMage((CommandSender)player));
			}
		} else {
			HashMap<Integer, ItemStack> returned = player.getInventory().addItem(itemStack);
			if (returned.size() > 0) {
				player.getWorld().dropItem(player.getLocation(), itemStack);
			}
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
		
		// Make sure they get their portraits re-rendered on relogin.
		URLMap.resend(player.getName());
		
		Mage mage = getMage(player);
		mage.onPlayerQuit(event);
		UndoQueue undoQueue = mage.getUndoQueue();
		
		if (commitOnQuit && undoQueue != null && !undoQueue.isEmpty()) {
			getLogger().info("Player logged out, committing constructions: " + mage.getName());
			undoQueue.commit();
			undoQueue.undoScheduled(mage);
		}
		
		try {
			File playerData = new File(playerDataFolder, player.getUniqueId().toString() + ".dat");
			getLogger().info("Player logged out, saving data to " + playerData.getName());
			DataStore playerConfig = new DataStore(getLogger(), playerData);
			mage.save(playerConfig);
			playerConfig.save();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		// Close the wand inventory to make sure the player's normal inventory gets saved
		Wand wand = mage.getActiveWand();
		if (wand != null) {
			wand.closeInventory();
		}
		
		// Just in case...
		mage.restoreInventory();
		
		// Let the GC collect the mage, unless they have some pending undo batches
		// or an undo queue (for rewind)
		if (undoQueue == null || undoQueue.isEmpty()) {
			getLogger().info("Player has no pending undo actions, forgetting: " + mage.getName());
			mages.remove(player.getUniqueId().toString());
		}
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
		crafting.enable(plugin);
	}

	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent event) {
		if (!(event.getPlayer() instanceof Player)) return;
		
		Player player = (Player)event.getPlayer();
		Mage mage = getMage(player);
		Wand wand = mage.getActiveWand();
		if (wand != null) {
			// NOTE: The type will never actually be CRAFTING, at least for now.
			// But we can hope for server-side player inventory open notification one day, right?
			// Anyway, check for opening another inventory and close the wand.
			if (event.getView().getType() != InventoryType.CRAFTING) {
				if (wand.getMode() == WandMode.INVENTORY || !wand.isInventoryOpen()) {
					wand.deactivate();
				}
			}
		}
	}
	
	protected ItemStack removeItemFromWand(Wand wand, ItemStack droppedItem) {
		if (wand == null || droppedItem == null || Wand.isWand(droppedItem)) {
			return null;
		}
		
		if (Wand.isSpell(droppedItem)) {
			String spellKey = Wand.getSpell(droppedItem);
			wand.removeSpell(spellKey);
			wand.saveInventory();
			
			// Update the item for proper naming and lore
			SpellTemplate spell = getSpellTemplate(spellKey);
			if (spell != null) {
				Wand.updateSpellItem(droppedItem, spell, null, null, true);
			}
		} else if (Wand.isBrush(droppedItem)) {
			String brushKey = Wand.getBrush(droppedItem);
			wand.removeBrush(brushKey);
			wand.saveInventory();
			
			// Update the item for proper naming and lore
			Wand.updateBrushItem(droppedItem, brushKey, null);
		}
		return droppedItem;
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		// getLogger().info("CLICK: " + event.getAction() + " on " + event.getSlotType() + " in "+ event.getInventory().getType() + " slots: " + event.getSlot() + ":" + event.getRawSlot());

		if (event.isCancelled()) return;
		if (!(event.getWhoClicked() instanceof Player)) return;

		Player player = (Player)event.getWhoClicked();
		Mage mage = getMage(player);
		
		// Check for temporary items
		ItemStack clickedItem = event.getCurrentItem();
		
		if (clickedItem != null && NMSUtils.isTemporary(clickedItem)) {
			String message = NMSUtils.getTemporaryMessage(clickedItem);
			if (message != null && message.length() > 1) {
				mage.sendMessage(message);
			}
			event.setCurrentItem(null);
			event.setCancelled(true);
			return;
		}
		Wand activeWand = mage.getActiveWand();
	
		InventoryType inventoryType = event.getInventory().getType();
		
		// Check for dropping items out of a wand's inventory
		if (event.getAction() == InventoryAction.DROP_ONE_SLOT && activeWand != null && activeWand.isInventoryOpen())
		{
			ItemStack droppedItem = clickedItem;
			ItemStack newDrop = removeItemFromWand(activeWand, droppedItem);
			
			if (newDrop != null) 
			{
				Location location = player.getLocation();
				Item item = location.getWorld().dropItem(location, newDrop);
				item.setVelocity(location.getDirection().normalize());
			}
			else 
			{
				event.setCancelled(true);
			}
			return;
		}
		
		// Check for wand cycling with active inventory
		if (activeWand != null) {
			WandMode wandMode = activeWand.getMode();
			if ((wandMode == WandMode.INVENTORY && inventoryType == InventoryType.CRAFTING) || 
			    (wandMode == WandMode.CHEST && inventoryType == InventoryType.CHEST)) {
				if (activeWand != null && activeWand.isInventoryOpen()) {
					if (event.getAction() == InventoryAction.PICKUP_HALF || event.getAction() == InventoryAction.NOTHING) {
						activeWand.cycleInventory();
						event.setCancelled(true);
						return;
					}
					
					if (event.getSlotType() == SlotType.ARMOR) {
						event.setCancelled(true);
						return;
					}
					
					// Chest mode falls back to selection from here.
					if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY || wandMode == WandMode.CHEST) {
						onPlayerActivateIcon(mage, activeWand, clickedItem);
						player.closeInventory();
						event.setCancelled(true);
						return;
					}
					
					// Prevent wand duplication
					if (Wand.isWand(event.getCursor()) || Wand.isWand(clickedItem)) {
						event.setCancelled(true);
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
		
		Wand previousWand = mage.getActiveWand();
		
		// Save the inventory state the the current wand if its spell inventory is open
		// This is just to make sure we don't lose changes made to the inventory
		if (previousWand != null && previousWand.isInventoryOpen()) {
			if (previousWand.getMode() == WandMode.INVENTORY) {
				previousWand.saveInventory();
			} else if (previousWand.getMode() == WandMode.CHEST) {
				// First check for chest inventory mode, we may just be closing a display inventory.
				previousWand.closeInventory();
				return;
			}
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
	}
	
	@EventHandler
	public void onPlayerGameModeChange(PlayerGameModeChangeEvent event)
	{
		if (event.getNewGameMode() == GameMode.CREATIVE && enableItemHacks) {
			boolean ejected = false;
			Player player = event.getPlayer();
			Mage mage = getMage(player);
			Wand activeWand = mage.getActiveWand();
			if (activeWand != null) {
				activeWand.deactivate();
			}
			Inventory inventory = player.getInventory();
			ItemStack[] contents = inventory.getContents();
			for (int i = 0; i < contents.length; i++) {
				ItemStack item = contents[i];
				if (Wand.isWand(item)) {
					ejected = true;
					inventory.setItem(i, null);
					player.getWorld().dropItemNaturally(player.getLocation(), item);
				}
			}
			if (ejected) {
				mage.sendMessage("Ejecting wands, creative mode will destroy them!");
				
			}
		}
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerPickupItem(PlayerPickupItemEvent event)
	{
		if (event.isCancelled()) return;
		
		Mage mage = getMage(event.getPlayer());
		ItemStack pickup = event.getItem().getItemStack();
		boolean isWand = Wand.isWand(pickup);
		
		// Creative mode inventory hacky work-around :\
		if (event.getPlayer().getGameMode() == GameMode.CREATIVE && isWand && enableItemHacks) {
			event.setCancelled(true);
			return;
		}
		
		if (dynmapShowWands && isWand) {
			Wand wand = new Wand(this, pickup);
			plugin.getLogger().info("Player " + mage.getName() + " picked up wand " + wand.getName() + ", id " + wand.getId());
			removeLostWand(wand);
		}
		
		Wand activeWand = mage.getActiveWand();
		if (activeWand != null && (!Wand.isWand(pickup) || Wand.isWandUpgrade(pickup))
			&& activeWand.isModifiable() && activeWand.addItem(pickup)) {
			event.getItem().remove();
			event.setCancelled(true);   
			return;
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
		
		ItemStack itemStack = event.getItemInHand();
		if (Wand.isWand(itemStack) || Wand.isBrush(itemStack) || Wand.isSpell(itemStack)) {
			event.setCancelled(true);
		}
	}
	
	protected boolean addLostWandMarker(LostWand lostWand) {
		Location location = lostWand.getLocation();
		if (!lostWand.isIndestructible()) {
			return true;
		}
		return addMarker("wand-" + lostWand.getId(), "Wands", lostWand.getName(), location.getWorld().getName(),
			location.getBlockX(), location.getBlockY(), location.getBlockZ(), lostWand.getDescription()
		);
	}
	
	protected void checkForWands(final Chunk chunk, final int retries) {
		if (dynmapShowWands && dynmap != null) {
			if (!dynmap.isReady()) {
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
		
		// Also check for any blocks we need to toggle.
		triggerBlockToggle(e.getChunk());
	}
	
	public void toggleCastCommandOverrides(com.elmakers.mine.bukkit.api.magic.Mage _mage, boolean override) {
		// Reach into internals a bit here.
		if (_mage instanceof Mage) {
			Mage mage = (Mage)_mage;
			mage.setCostReduction(override ? castCommandCostReduction : 0);
			mage.setCooldownReduction(override ? castCommandCooldownReduction : 0);
			mage.setPowerMultiplier(override ? castCommandPowerMultiplier : 1);	
		}
	}
	
	public float getCooldownReduction() {
		return cooldownReduction;
	}
	
	public float getCostReduction() {
		return costReduction;
	}
	
	public Material getDefaultMaterial() {
		return defaultMaterial;
	}
	
	public Collection<com.elmakers.mine.bukkit.api.wand.LostWand> getLostWands() {
		return new ArrayList<com.elmakers.mine.bukkit.api.wand.LostWand>(lostWands.values());
	}
	
	public Collection<Automaton> getAutomata() {
		Collection<Automaton> all = new ArrayList<Automaton>();
		for (Map<Long, Automaton> chunkList : automata.values()) {
			all.addAll(chunkList.values());
		}
		return all;
	}
	
	public boolean cast(com.elmakers.mine.bukkit.api.magic.Mage mage, String spellName, String[] parameters, CommandSender sender, Player player)
	{
		Player usePermissions = (sender == player) ? player : (sender instanceof Player ? (Player)sender : null);
		Location targetLocation = null;
		if (mage == null) {
			CommandSender mageController = player == null ? sender : player;
			if (sender instanceof BlockCommandSender) {
				targetLocation = ((BlockCommandSender)sender).getBlock().getLocation();
			}
			if (sender instanceof Player) {
				targetLocation = ((Player)player).getLocation();
			}
			mage = getMage(mageController);
		}
		
		SpellTemplate template = getSpellTemplate(spellName);
		if (template == null || !template.hasCastPermission(usePermissions))
		{
			if (sender != null) {
				sender.sendMessage("Spell " + spellName + " unknown");
			}
			return false;
		}
		com.elmakers.mine.bukkit.api.spell.Spell spell = mage.getSpell(spellName);
		if (spell == null)
		{
			if (sender != null) {
				sender.sendMessage("Spell " + spellName + " unknown");
			}
			return false;
		}

		// Make it free and skip cooldowns, if configured to do so.
		toggleCastCommandOverrides(mage, true);
		spell.cast(parameters, targetLocation);
		toggleCastCommandOverrides(mage, false);
		if (sender != player && sender != null) {
			String castMessage = "Cast " + spellName;
			if (player != null) {
				castMessage += " on " + player.getName();
			}
			sender.sendMessage(castMessage);
		}

		return true;
	}
	
	public void onCast(Mage mage, com.elmakers.mine.bukkit.api.spell.Spell spell, SpellResult result) {
		if (dynmapShowSpells && dynmap != null) {
			dynmap.showCastMarker(mage, spell, result);
		}
	}
	
	protected void triggerBlockToggle(final Chunk chunk) {
		String chunkKey = getChunkKey(chunk);
		Map<Long, Automaton> chunkData = automata.get(chunkKey);
		if (chunkData != null) {
			final List<Automaton> restored = new ArrayList<Automaton>();
			Collection<Long> blockKeys = new ArrayList<Long>(chunkData.keySet());
			long timeThreshold = System.currentTimeMillis() - toggleCooldown;
			for (Long blockKey : blockKeys) {
				Automaton toggleBlock = chunkData.get(blockKey);
				
				// Skip it for now if the chunk was recently loaded
				if (toggleBlock.getCreatedTime() < timeThreshold) {
					Block current = toggleBlock.getBlock();
					// Don't toggle the block if it has changed to something else.
					if (current.getType() == toggleBlock.getMaterial()) {
						current.setType(Material.AIR);
						restored.add(toggleBlock);
					}
					
					chunkData.remove(blockKey);
				}
			}
			if (restored.size() > 0) {
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, 
					new Runnable() {
						public void run() {
							for (Automaton restoreBlock : restored) {
								getLogger().info("Resuming block at " + restoreBlock.getPosition() + ": " + restoreBlock.getName());
								restoreBlock.restore();
								sendToMages(restoreBlock.getMessage(), restoreBlock.getPosition().toLocation(restoreBlock.getWorld()));	
							}
						}
				}, 5);
			}
			if (chunkData.size() == 0) {
				automata.remove(chunkKey);
			}
		}
	}
	
	public void sendToMages(String message, Location location, int range) {
		int rangeSquared = range * range;
		if (message != null && message.length() > 0) {
			for (Mage mage : mages.values())
			{
				if (!mage.isPlayer() || mage.isDead() || !mage.isOnline() || !mage.hasLocation()) continue;
				if (!mage.getLocation().getWorld().equals(location.getWorld())) continue;
				if (mage.getLocation().toVector().distanceSquared(location.toVector()) < rangeSquared) {
					mage.sendMessage(message);
				}
			}
		}
	}
	
	public boolean getIndestructibleWands() {
		return indestructibleWands;
	}
	
	public void forgetMage(com.elmakers.mine.bukkit.api.magic.Mage mage) {
		forgetMages.put(mage.getId(), System.currentTimeMillis());
	}
	
	/*
	 * API Implementation
	 */
	
	@Override
	public boolean isAutomata(Block block) {
		String chunkId = getChunkKey(block.getChunk());
		Map<Long, Automaton> toReload = automata.get(chunkId);
		if (toReload != null) {
			return toReload.containsKey(BlockData.getBlockId(block));
		}
		return false;
	}
	
	@Override
	public void updateBlock(Block block)
	{
		updateBlock(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
	}
	
	@Override
	public void updateBlock(String worldName, int x, int y, int z)
	{
		if (dynmap != null && dynmapUpdate)
		{
			dynmap.triggerRenderOfBlock(worldName, x, y, z);
		}
	}
	
	@Override
	public void updateVolume(String worldName, int minx, int miny, int minz, int maxx, int maxy, int maxz)
	{
		if (dynmap != null && dynmapUpdate && worldName != null && worldName.length() > 0)
		{
			dynmap.triggerRenderOfVolume(worldName, minx, miny, minz, maxx, maxy, maxz);
		}
	}
	
	public void update(String worldName, BoundingBox area)
	{
		if (dynmap != null && dynmapUpdate && area != null && worldName != null && worldName.length() > 0)
		{
			dynmap.triggerRenderOfVolume(worldName, 
				area.getMin().getBlockX(), area.getMin().getBlockY(), area.getMin().getBlockZ(), 
				area.getMax().getBlockX(), area.getMax().getBlockY(), area.getMax().getBlockZ());
		}
	}
	
	@Override
	public void update(com.elmakers.mine.bukkit.api.block.BlockList blockList)
	{
		if (blockList != null) {
			if (blockList.size() > VOLUME_UPDATE_THRESHOLD) {
				update(blockList.getWorldName(), blockList.getArea());
			} else {
				for (com.elmakers.mine.bukkit.api.block.BlockData blockData : blockList) {
					updateBlock(blockData.getWorldName(), blockData.getPosition().getBlockX(), blockData.getPosition().getBlockY(), blockData.getPosition().getBlockZ());
				}
			}
		}
	}
	
	@Override
	public boolean canCreateWorlds()
	{
		return createWorldsEnabled;
	}

	@Override
	public Set<Material> getMaterialSet(String name)
	{
		if (name.contains(",")) {
			return ConfigurationUtils.parseMaterials(name);
		}
		if (!materialSets.containsKey(name)) {
			return ConfigurationUtils.parseMaterials(name);
		}
		return materialSets.get(name);
	}
	
	@Override public int getMaxY() {
		return MAX_Y;
	}
	
	@Override
	public void sendToMages(String message, Location location) {
		sendToMages(message, location, toggleMessageRange);
	}
	
	@Override
	public void registerAutomata(Block block, String name, String message) {
		String chunkId = getChunkKey(block.getChunk());
		Map<Long, Automaton> toReload = automata.get(chunkId);
		if (toReload == null) {
			toReload = new HashMap<Long, Automaton>();
			automata.put(chunkId, toReload);
		}
		Automaton data = new Automaton(block, name, message);
		toReload.put(data.getId(), data);
	}

	@Override
	public boolean unregisterAutomata(Block block) {
		// Note that we currently don't clean up an empty entry,
		// purposefully, to prevent thrashing the main map and adding lots
		// of HashMap creation.
		String chunkId = getChunkKey(block.getChunk());
		Map<Long, Automaton> toReload = automata.get(chunkId);
		if (toReload != null) {
			toReload.remove(BlockData.getBlockId(block));
		}
		
		return toReload != null;
	}
	
	@Override
	public int getMaxUndoPersistSize() {
		return undoMaxPersistSize;
	}

	@Override
	public MagicPlugin getPlugin()
	{
		return plugin;
	}
	
	@Override
	public Collection<com.elmakers.mine.bukkit.api.magic.Mage> getMages()
	{
		Collection<com.elmakers.mine.bukkit.api.magic.Mage> mageInterfaces = new ArrayList<com.elmakers.mine.bukkit.api.magic.Mage>(mages.values());
		return mageInterfaces;
	}

	@Override
	public Set<Material> getBuildingMaterials()
	{
		return buildingMaterials;
	}

	@Override
	public Set<Material> getDestructibleMaterials()
	{
		return destructibleMaterials;
	}

	@Override
	public Set<Material> getRestrictedMaterials()
	{
		return restrictedMaterials;
	}
	
	@Override
	public int getMessageThrottle()
	{
		return messageThrottle;
	}

	protected Mage getMage(Player player)
	{
		if (player == null) return null;
		String id = player.getUniqueId().toString();
		
		// Check for Citizens NPC!
		if (player.hasMetadata("NPC")) {
			id = "NPC-" + player.getUniqueId();
		}
		return getMage(id, player);
	}
	
	@Override
	public com.elmakers.mine.bukkit.api.magic.Mage getMage(CommandSender commandSender)
	{
		String mageId = "COMMAND";
		if (commandSender instanceof ConsoleCommandSender) {
			mageId = "CONSOLE";
		} else if (commandSender instanceof Player) {
			return getMage((Player)commandSender);
		} else if (commandSender instanceof BlockCommandSender) {
			BlockCommandSender commandBlock = (BlockCommandSender)commandSender;
			String commandName = commandBlock.getName();
			if (commandName != null && commandName.length() > 0) {
				mageId = "COMMAND-" + commandBlock.getName();
			}
		}
		
		return getMage(mageId, commandSender);
	}
	
	@Override
	public Collection<String> getMaterialSets()
	{
		return materialSets.keySet();
	}
	
	@Override
	public Collection<String> getPlayerNames() 
	{
		List<String> playerNames = new ArrayList<String>();
		List<World> worlds = Bukkit.getWorlds();
		for (World world : worlds) {
			List<Player> players = world.getPlayers();
			for (Player player : players) {
				if (player.hasMetadata("NPC")) continue;
				playerNames.add(player.getName());
			}
		}
		return playerNames;
	}

	@Override
	public void disablePhysics(int interval)
	{
		if (physicsHandler == null && interval > 0) {
			physicsHandler = new PhysicsHandler(this, interval);
			Bukkit.getPluginManager().registerEvents(physicsHandler, plugin);
		}
	}
	
	@Override
	public boolean commitAll()
	{
		boolean undid = false;
		for (Mage mage : mages.values()) {
			undid = mage.commit() || undid;
		}
		return undid;
	}
	
	@Override
	public boolean isPVPAllowed(Location location)
	{
		if (bypassPvpPermissions) return true;
		return worldGuardManager.isPVPAllowed(location);
	}
	
	@Override
	public Location getWarp(String warpName) {
		if (warpController == null) return null;
		return warpController.getWarp(warpName);
	}
	
	@Override
	public boolean sendMail(CommandSender sender, String fromPlayer, String toPlayer, String message) {
		if (mailer != null) {
			return mailer.sendMail(sender, fromPlayer, toPlayer, message);
		}
		
		return false;
	}

	@Override
	public UndoList undoAny(Block target)
	{
		for (Mage mage : mages.values())
		{
			UndoList undid = mage.undo(target);
			if (undid != null)
			{
				return undid;
			}
		}

		return null;
	}

	@Override
	public com.elmakers.mine.bukkit.api.wand.Wand createWand(String wandKey) 
	{
		return Wand.createWand(this, wandKey);
	}

	@Override
	public boolean elementalsEnabled() 
	{
		return (elementals != null);
	}

	@Override
	public boolean createElemental(Location location, String templateName, CommandSender creator) 
	{
		return elementals.createElemental(location, templateName, creator);
	}

	@Override
	public boolean isElemental(Entity entity) 
	{
		if (elementals == null || entity.getType() != EntityType.FALLING_BLOCK) return false;
		return elementals.isElemental(entity);
	}

	@Override
	public boolean damageElemental(Entity entity, double damage, int fireTicks, CommandSender attacker) 
	{
		if (elementals == null) return false;
		return elementals.damageElemental(entity, damage, fireTicks, attacker);
	}

	@Override
	public boolean setElementalScale(Entity entity, double scale) 
	{
		if (elementals == null) return false;
		return elementals.setElementalScale(entity, scale);
	}

	@Override
	public double getElementalScale(Entity entity) 
	{
		if (elementals == null) return 0;
		return elementals.getElementalScale(entity);
	}

	@Override
	public com.elmakers.mine.bukkit.api.spell.SpellCategory getCategory(String key) 
	{
		SpellCategory category = categories.get(key);
		if (category == null) {
			category = new com.elmakers.mine.bukkit.spell.SpellCategory(key, this);
			categories.put(key, category);
		}
		return category;
	}

	@Override
	public Collection<com.elmakers.mine.bukkit.api.spell.SpellCategory> getCategories()
	{
		List<com.elmakers.mine.bukkit.api.spell.SpellCategory> allCategories = new ArrayList<com.elmakers.mine.bukkit.api.spell.SpellCategory>();
		allCategories.addAll(categories.values());
		return allCategories;
	}

	@Override
	public Collection<SpellTemplate> getSpellTemplates()
	{
		List<SpellTemplate> allSpells = new ArrayList<SpellTemplate>();
		allSpells.addAll(spells.values());
		return allSpells;
	}
	
	public SpellTemplate getSpellTemplate(String name) 
	{
		if (name == null || name.length() == 0) return null;
		return spells.get(name);
	}

	/*
	 * Private data
	 */
	
	 private final static int MAX_Y = 255;	
	 private static final String BUILTIN_SPELL_CLASSPATH = "com.elmakers.mine.bukkit.spell.builtin";
	 private static int VOLUME_UPDATE_THRESHOLD = 32;	
	 private static int MAGE_FORGET_THRESHOLD = 30000;

	 private final String                        SPELLS_FILE                 	= "spells";
	 private final String                        CONFIG_FILE             		= "config";
	 private final String                        WANDS_FILE             		= "wands";
	 private final String                        MESSAGES_FILE             		= "messages";
	 private final String                        MATERIALS_FILE             	= "materials";
	 private final String						 LOST_WANDS_FILE				= "lostwands";
	 private final String						 SPELLS_DATA_FILE				= "spells";
	 private final String						 AUTOMATA_FILE					= "automata";
	 private final String						 URL_MAPS_FILE					= "imagemaps";
	 
	 private boolean 							loadDefaultSpells				= true;
	 private boolean 							loadDefaultWands				= true;

	 static final String                         STICKY_MATERIALS               = "37,38,39,50,51,55,59,63,64,65,66,68,70,71,72,75,76,77,78,83";
	 static final String                         STICKY_MATERIALS_DOUBLE_HEIGHT = "64,71,";

	 private Set<Material>                      buildingMaterials              = new HashSet<Material>();
	 private Set<Material>                      indestructibleMaterials        = new HashSet<Material>();
	 private Set<Material>                      restrictedMaterials	 	       = new HashSet<Material>();
	 private Set<Material>                      destructibleMaterials          = new HashSet<Material>();
	 private Map<String, Set<Material>>			materialSets				   = new HashMap<String, Set<Material>>();
	 
	 private int								 undoTimeWindow					= 6000;
	 private int								 undoBlockBorderSize			= 2;
	 private int								 maxTNTPerChunk					= 0;
	 private int                                 undoQueueDepth                 = 256;
	 private int								 pendingQueueDepth				= 16;
	 private int                                 undoMaxPersistSize             = 0;
	 private boolean                             commitOnQuit             		= false;
	 private long                                playerDataThreshold            = 0;
	 private WandMode							 defaultWandMode				= WandMode.INVENTORY;
	 private boolean                             showMessages                   = true;
	 private boolean                             showCastMessages               = false;
	 private String								 messagePrefix					= "";
	 private String								 castMessagePrefix				= "";
	 private boolean                             soundsEnabled                  = true;
	 private boolean                             indestructibleWands            = true;
	 private boolean                             keepWandsOnDeath	            = true;
	 private String								 welcomeWand					= "";
	 private int								 messageThrottle				= 0;
	 private int								 clickCooldown					= 150;
	 private boolean							 bindingEnabled					= false;
	 private boolean							 keepingEnabled					= false;
	 private boolean                             fillingEnabled                 = false;
	 private boolean							 essentialsSignsEnabled			= false;
	 private boolean							 dynmapUpdate					= true;
	 private boolean							 dynmapShowWands				= true;
	 private boolean							 dynmapShowSpells				= true;
	 private boolean							 createWorldsEnabled			= true;
	 private float							 	 maxDamagePowerMultiplier	    = 2.0f;
	 private float								 maxConstructionPowerMultiplier = 5.0f;
	 private float								 maxRadiusPowerMultiplier 		= 2.5f;
	 private float								 maxRadiusPowerMultiplierMax    = 4.0f;
	 private float								 maxRangePowerMultiplier 		= 3.0f;
	 private float								 maxRangePowerMultiplierMax 	= 5.0f;
	 private float								 maxPower						= 1.0f;
	 private float							 	 castCommandCostReduction	    = 1.0f;
	 private float							 	 castCommandCooldownReduction	= 1.0f;
	 private float								 castCommandPowerMultiplier     = 0.0f;
	 private float							 	 costReduction	    			= 0.0f;
	 private float							 	 cooldownReduction				= 0.0f;
	 private int								 maxBlockUpdates				= 100;
	 private int								 ageDroppedItems				= 0;
	 private int								 autoUndo						= 0;
	 private int								 autoSaveTaskId					= 0;
	 private WarpController						 warpController					= null;
	 
	 private final Map<String, SpellTemplate>   spells              		= new HashMap<String, SpellTemplate>();
	 private final Map<String, SpellCategory>   categories              	= new HashMap<String, SpellCategory>();
	 private final Map<String, Mage> 		 	mages                  		= new HashMap<String, Mage>();
	 private final Map<String, Long>			forgetMages					= new HashMap<String, Long>();
	 private final Map<String, Mage>		 	pendingConstruction			= new HashMap<String, Mage>();
	 private final Set<String>  	 			pendingUndo					= new HashSet<String>();
	 private final Map<String, WeakReference<WorldEditSchematic>>	 schematics	= new HashMap<String, WeakReference<WorldEditSchematic>>();
 
	 private MagicPlugin                         plugin                         = null;
	 private final File							 configFolder;
	 private final File							 dataFolder;
	 private final File							 schematicFolder;
	 private final File							 defaultsFolder;
	 private final File							 playerDataFolder;
	 private boolean							 enableItemHacks			 	= true;

	 private int								 toggleCooldown					= 1000;
	 private int								 toggleMessageRange				= 1024;
	 
	 private boolean							 bypassBuildPermissions         = false;
	 private boolean							 bypassPvpPermissions           = false;
	 private FactionsManager					 factionsManager				= new FactionsManager();
	 private WorldGuardManager					 worldGuardManager				= new WorldGuardManager();
	 
	 private TradersController					 tradersController				= null;
	 private String								 extraSchematicFilePath			= null;
	 private Class<?>							 cuboidClipboardClass           = null;
	 private DynmapController					 dynmap							= null;
	 private ElementalsController				 elementals						= null;
	 private Mailer								 mailer							= null;
	 private Material							 defaultMaterial				= Material.DIRT;
	 
	 private PhysicsHandler						 physicsHandler					= null;

	 private Map<String, Map<Long, Automaton>> 	 automata			    		= new HashMap<String, Map<Long, Automaton>>();
	 private Map<String, LostWand>				 lostWands						= new HashMap<String, LostWand>();
	 private Map<String, Set<String>>		 	 lostWandChunks					= new HashMap<String, Set<String>>();
	 
	 private int								 metricsLevel					= 5;
	 private Metrics							 metrics						= null;
	 private boolean							 hasDynmap						= false;
	 private boolean							 hasEssentials					= false;
	 private boolean							 hasCommandBook					= false;
	 private boolean							 hasWorldEdit					= false;
	 
	 // Sub-Controllers
	 private CraftingController					 crafting						= null;
	 private EnchantingController				 enchanting						= null;
	 private AnvilController					 anvil							= null;
}
