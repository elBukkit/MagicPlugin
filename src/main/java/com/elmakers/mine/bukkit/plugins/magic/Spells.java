package com.elmakers.mine.bukkit.plugins.magic;

import java.io.File;
import java.io.InputStream;
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
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
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
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitScheduler;

import com.elmakers.mine.bukkit.dao.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.blocks.BlockBatch;
import com.elmakers.mine.bukkit.utilities.CSVParser;
import com.elmakers.mine.bukkit.utilities.SetActiveItemSlotTask;
import com.elmakers.mine.bukkit.utilities.URLMap;
import com.elmakers.mine.bukkit.utilities.UndoQueue;
import com.elmakers.mine.bukkit.utilities.borrowed.Configuration;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class Spells implements Listener 
{
	/*
	 * Public API - Use for hooking up a plugin, or calling a spell
	 */

	public PlayerSpells getPlayerSpells(Player player)
	{
		PlayerSpells spells = playerSpells.get(player.getName());
		if (spells == null)
		{
			spells = new PlayerSpells(this, player);
			playerSpells.put(player.getName(), spells);
		}

		spells.setPlayer(player);

		return spells;
	}
	
	public PlayerSpells getPlayerSpells(String playerName) {
		if (!playerSpells.containsKey(playerName)) {
			playerSpells.put(playerName, new PlayerSpells(this, null));
		}
		
		return playerSpells.get(playerName);
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
		template.load(name, spellNode);

		addSpell(template);
	}

	public void addSpell(Spell variant)
	{
		Spell conflict = spells.get(variant.getKey());
		if (conflict != null)
		{
			log.log(Level.WARNING, "Duplicate spell name: '" + conflict.getKey() + "'");
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
	
	public float getMaxPowerMultiplier() {
		return maxPowerMultiplier;
	}
	
	/*
	 * Undo system
	 */

	public UndoQueue getUndoQueue(String playerName)
	{
		UndoQueue queue = playerUndoQueues.get(playerName);
		if (queue == null)
		{
			queue = new UndoQueue();
			queue.setMaxSize(undoQueueDepth);
			playerUndoQueues.put(playerName, queue);
		}
		return queue;
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

	public boolean undoAny(Player player, Block target)
	{
		for (String playerName : playerUndoQueues.keySet())
		{
			UndoQueue queue = playerUndoQueues.get(playerName);
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

	public void scheduleCleanup(BlockList blocks)
	{
		Server server = plugin.getServer();
		BukkitScheduler scheduler = server.getScheduler();

		// scheduler works in ticks- 20 ticks per second.
		long ticksToLive = blocks.getTimeToLive() * 20 / 1000;
		scheduler.scheduleSyncDelayedTask(plugin, new CleanupBlocksTask(this, blocks), ticksToLive);
	}

	/*
	 * Event registration- call to listen for events
	 */

	public void registerEvent(SpellEventType type, Spell spell)
	{
		PlayerSpells spells = getPlayerSpells(spell.getPlayer());
		spells.registerEvent(type, spell);
	}

	public void unregisterEvent(SpellEventType type, Spell spell)
	{
		PlayerSpells spells = getPlayerSpells(spell.getPlayer());
		spells.unregisterEvent(type, spell);
	}

	/*
	 * Random utility functions
	 */

	public boolean cancel(Player player)
	{
		PlayerSpells playerSpells = getPlayerSpells(player);
		return playerSpells.cancel();
	}

	public boolean isQuiet()
	{
		return quiet;
	}
	
	public int getMessageThrottle()
	{
		return messageThrottle;
	}

	public boolean isSilent()
	{
		return silent;
	}

	public boolean soundsEnabled()
	{
		return soundsEnabled;
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
	public Logger getLog()
	{
		return log;
	}

	public MagicPlugin getPlugin()
	{
		return plugin;
	}
	
	public boolean hasBuildPermission(Player player, Location location) {
		return hasBuildPermission(player, location.getBlock());
	}

	public boolean hasBuildPermission(Player player, Block block) {
		// First check the indestructible list
		if (indestructibleMaterials.contains(block.getType())) return false;

		// Now check the region manager.
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

	public void initialize(MagicPlugin plugin)
	{
		// Try to (dynamically) link to WorldGuard:
		try {
			regionManager = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
			Method canBuildMethod = regionManager.getClass().getMethod("canBuild", Player.class, Block.class);
			if (canBuildMethod != null) {
				log.info("WorldGuard found, will respect build permissions for construction spells");
			} else {
				regionManager = null;
			}
		} catch (Throwable ex) {
		}
		
		if (regionManager == null) {
			log.info("WorldGuard not found, not using a region manager.");
		}
		
		this.plugin = plugin;
		load();
		
		// Set up the PlayerSpells timer
		Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			public void run() {
				for (PlayerSpells spells : playerSpells.values()) {
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
	
	public void addPendingBlockBatch(BlockBatch batch) {
		pendingBatches.addLast(batch);
	}

	public void load()
	{
		File dataFolder = plugin.getDataFolder();
		dataFolder.mkdirs();

		File propertiesFile = new File(dataFolder, propertiesFileName);
		if (!propertiesFile.exists())
		{
			File oldDefaults = new File(dataFolder, propertiesFileNameDefaults);
			oldDefaults.delete();
			plugin.saveResource(propertiesFileNameDefaults, false);
			loadProperties(plugin.getResource(propertiesFileNameDefaults));
		} else {
			loadProperties(propertiesFile);
		}

		File spellsFile = new File(dataFolder, spellsFileName);
		if (!spellsFile.exists())
		{
			File oldDefaults = new File(dataFolder, spellsFileNameDefaults);
			oldDefaults.delete();
			plugin.saveResource(spellsFileNameDefaults, false);
			load(plugin.getResource(spellsFileNameDefaults));
		} else {
			load(spellsFile);
		}

		File playersFile = new File(dataFolder, playersFileName);
		if (playersFile.exists())
		{
			Configuration playerConfiguration = new Configuration(playersFile);
			playerConfiguration.load();
			List<String> playerNames = playerConfiguration.getKeys();
			for (String playerName : playerNames) {
				getPlayerSpells(playerName).load(playerConfiguration.getNode(playerName));
			}
		}
	
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				URLMap.resetAll();
				URLMap.load(plugin);
			}
		}, 20);
		
		Wand.load(plugin);

		log.info("Magic: Loaded " + spells.size() + " spells and " + Wand.getWandTemplates().size() + " wands");
	}
	
	public void save()
	{
		File dataFolder = plugin.getDataFolder();
		dataFolder.mkdirs();
		
		File playersFile = new File(dataFolder, playersFileName);
		Configuration playerConfiguration = new Configuration(playersFile);
		for (Entry<String, PlayerSpells> spellsEntry : playerSpells.entrySet()) {
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
				log.warning("Magic: Error loading spell " + key);
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
		silent = generalNode.getBoolean("silent", silent);
		quiet = generalNode.getBoolean("quiet", quiet);
		messageThrottle = generalNode.getInt("message_throttle", 0);
		maxBlockUpdates = generalNode.getInt("max_block_updates", 100);
		soundsEnabled = generalNode.getBoolean("sounds", soundsEnabled);
		maxPowerMultiplier = (float)generalNode.getDouble("max_power_multiplier", maxPowerMultiplier);
		castCommandCostReduction = (float)generalNode.getDouble("cast_command_cost_reduction", castCommandCostReduction);
		castCommandCooldownReduction = (float)generalNode.getDouble("cast_command_cooldown_reduction", castCommandCooldownReduction);
		blockPopulatorEnabled = generalNode.getBoolean("enable_block_populator", blockPopulatorEnabled);
		enchantingEnabled = generalNode.getBoolean("enable_enchanting", enchantingEnabled);
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
	}

	public void clear()
	{
		playerSpells.clear();
		spells.clear();
	}

	public void reset()
	{
		log.info("Magic: Resetting all spells to default");
		clear();

		File dataFolder = plugin.getDataFolder();
		dataFolder.mkdirs();

		File spellsFile = new File(dataFolder, spellsFileName);
		spellsFile.delete();

		File magicFile = new File(dataFolder, propertiesFileName);
		magicFile.delete();

		Wand.reset(plugin);
		
		load();
	}

	public List<Spell> getAllSpells()
	{
		List<Spell> allSpells = new ArrayList<Spell>();
		allSpells.addAll(spells.values());
		return allSpells;
	}
	
	public boolean allowPhysics(Block block)
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
	@EventHandler
	public void onPlayerEquip(PlayerItemHeldEvent event)
	{
		Player player = event.getPlayer();
		PlayerInventory inventory = player.getInventory();
		ItemStack next = inventory.getItem(event.getNewSlot());
		ItemStack previous = inventory.getItem(event.getPreviousSlot());

		PlayerSpells playerSpells = getPlayerSpells(player);
		Wand activeWand = playerSpells.getActiveWand();
		
		// Check for active Wand
		if (activeWand != null && Wand.isWand(previous)) {
			// If the wand inventory is open, we're going to let them select a spell or material
			if (activeWand.isInventoryOpen()) {
				// Update the wand item, Bukkit has probably made a copy
				activeWand.setItem(previous);
				
				// Check for spell or material selection
				if (next != null && next.getType() != Material.AIR) {
					Spell spell = playerSpells.getSpell(Wand.getSpell(next));
					if (spell != null) {
						playerSpells.cancel();
						activeWand.setActiveSpell(spell.getKey());
					} else {
						Material material = next.getType();
						if (buildingMaterials.contains(material) || material == Wand.EraseMaterial || material == Wand.CopyMaterial) {
							activeWand.setActiveMaterial(material, next.getData().getData());
						}
					}
				}
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
			newWand.activate(playerSpells);
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event)
	{
		PlayerSpells spells = getPlayerSpells(event.getPlayer());
		spells.onPlayerMove(event);
	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent event)
	{
		if (event.getEntityType() == EntityType.PLAYER && event.getEntity() instanceof Player) {
			onPlayerDeath((Player)event.getEntity(), event);
		}
	}

	public void onPlayerDeath(Player player, EntityDeathEvent event)
	{
		PlayerSpells playerSpells = getPlayerSpells(player);
		String rule = player.getWorld().getGameRuleValue("keepInventory");
		Wand wand = playerSpells.getActiveWand();
		if (wand != null  && !rule.equals("true")) {
			List<ItemStack> drops = event.getDrops();
			drops.clear();
			
			// Drop the held wand since it does not get stored
			drops.add(wand.getItem());
			
			// Retrieve stored inventory before deactiavting the wand
			if (playerSpells.hasStoredInventory()) {
				ItemStack[] stored = playerSpells.getStoredInventory().getContents();
				
				// Deactivate the wand.
				wand.deactivate();
	
				// Clear the inventory, which was just restored by the wand
				player.getInventory().clear();
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

		playerSpells.onPlayerDeath(event);
	}

	public void onPlayerDamage(Player player, EntityDamageEvent event)
	{
		PlayerSpells spells = getPlayerSpells(player);
		spells.onPlayerDamage(event);
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event)
	{
		if (Player.class.isInstance(event.getEntity()))
		{
			Player player = (Player)event.getEntity();
			onPlayerDamage(player, event);
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();		
		PlayerSpells playerSpells = getPlayerSpells(player);
		Wand wand = playerSpells.getActiveWand();
		
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
					|| material == Material.BED || material == Material.SIGN_POST || material == Material.COMMAND);
		}
		if (toggleInventory)
		{
			// Check for spell cancel first, e.g. fill or force
			if (!playerSpells.cancel()) {
				
				// Check for wand cycling
				if (wandCycling) {
					if (player.isSneaking()) {
						Spell activeSpell = wand.getActiveSpell();
						if (activeSpell != null && activeSpell.usesMaterial() && !activeSpell.hasMaterialOverride() && wand.getMaterials().length > 0) {
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
				playerSpells.playSound(Sound.NOTE_BASS, 1.0f, 0.7f);
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
		PlayerSpells playerSpells = getPlayerSpells(player);
		Wand wand = Wand.getActiveWand(this, player);
		if (wand != null) {
			wand.activate(playerSpells);
		}
	}
	
	@EventHandler
	public void onPlayerExpChange(PlayerExpChangeEvent event)
	{
		// We don't care about exp loss events
		if (event.getAmount() <= 0) return;
		
		Player player = event.getPlayer();
		PlayerSpells playerSpells = getPlayerSpells(player);
		Wand wand = playerSpells.getActiveWand();
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
		
		PlayerSpells playerSpells = getPlayerSpells(player);
		Wand wand = playerSpells.getActiveWand();
		if (wand != null) {
			wand.deactivate();
		}
		
		// Just in case...
		playerSpells.restoreInventory();
		
		playerSpells.onPlayerQuit(event);
		
		// Let the GC collect these
		// TODO: See how deep the rabbit-hole goes here. Probably need to clear spells too.
		playerSpells.setActiveWand(null);
		playerSpells.setPlayer(null);
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPluginDisable(PluginDisableEvent event)
	{
		for (PlayerSpells spells : playerSpells.values()) {
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
				PlayerSpells spells = getPlayerSpells(player);
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
		PlayerSpells spells = getPlayerSpells(player);
		
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
		PlayerSpells playerSpells = getPlayerSpells(player);
		Wand wand = playerSpells.getActiveWand();
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
		
		if (event.getInventory().getType() == InventoryType.ENCHANTING)
		{
			SlotType slotType = event.getSlotType();
			if (slotType == SlotType.CRAFTING) {
				ItemStack cursor = event.getCursor();
				ItemStack current = event.getCurrentItem();
				
				// Make wands into an enchantable item when placing
				if (Wand.isWand(cursor)) {
					Wand wand = new Wand(this, cursor);
					wand.makeEnchantable(true);
				}
				// And turn them back when taking out
				if (Wand.isWand(current)) {
					Wand wand = new Wand(this, current);
					wand.makeEnchantable(false);
				}
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
				
			}
			
			if (slotType == SlotType.RESULT) {
				// Check for wands in both slots
				// ...... arg. So close.. and yet, not.
				// I guess I need to wait for the long-awaited anvil API?
				ItemStack firstItem = anvilInventory.getItem(0);
				ItemStack secondItem = anvilInventory.getItem(1);
				if (Wand.isWand(firstItem) && Wand.isWand(secondItem)) 
				{
					Wand firstWand = new Wand(this, firstItem);
					Wand secondWand = new Wand(this, secondItem);
					Wand newWand = new Wand(this);
					newWand.setName(firstWand.getName());
					newWand.add(firstWand);
					newWand.add(secondWand);
					anvilInventory.setItem(0,  null);
					anvilInventory.setItem(1,  null);
					cursor.setType(Material.AIR);
					
					Player player = (Player)event.getWhoClicked();
					player.sendMessage("Combined wands for free. Hope that's what you wanted! (WIP, need Anvil API)");
					player.getInventory().addItem(newWand.getItem());
					
					// This seems to work in the debugger, but.. doesn't do anything.
					// InventoryUtils.setInventoryResults(anvilInventory, newWand.getItem());
				}
			}
			
			// Rename wand when taking from result slot
			if (slotType == SlotType.RESULT && Wand.isWand(current)) {
				ItemMeta meta = current.getItemMeta();
				String newName = meta.getDisplayName();
				Wand wand = new Wand(this, current);
				wand.setName(newName);
			}
		}
	}

	@EventHandler
	public void onInventoryClosed(InventoryCloseEvent event) {
		if (!(event.getPlayer() instanceof Player)) return;
			
		// Update the active wand, it may have changed around
		Player player = (Player)event.getPlayer();
		PlayerSpells playerSpells = getPlayerSpells(player);
		
		// Save the inventory state the the current wand if its spell inventory is open
		// This is just to make sure we don't lose changes made to the inventory
		Wand previousWand = playerSpells.getActiveWand();
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
				wand.activate(playerSpells);
			}
		}
		
		if (wand != null && !changedWands) {
			wand.updateInventoryNames(true);
		}
	}

	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event)
	{
		PlayerSpells spells = getPlayerSpells(event.getPlayer());
		if (spells.hasStoredInventory()) {
			event.setCancelled(true);   		
			if (spells.addToStoredInventory(event.getItem().getItemStack())) {
				event.getItem().remove();
			}
		} else {
			// Hackiness needed because we don't get an equip event for this!
			PlayerInventory inventory = event.getPlayer().getInventory();
			ItemStack inHand = inventory.getItemInHand();
			ItemStack pickup = event.getItem().getItemStack();
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
	public void onPlayerDropItem(PlayerDropItemEvent event)
	{
		Player player = event.getPlayer();
		PlayerSpells spells = getPlayerSpells(player);
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
	
	@EventHandler
	public void onWorldInit(WorldInitEvent event) {
		// Install our block populator if configured to do so.
		if (blockPopulatorEnabled && blockPopulatorConfig != null) {
			World world = event.getWorld();
			world.getPopulators().add(new WandChestPopulator(this, blockPopulatorConfig));
		}
	}
	
	public Spell getSpell(String name) {
		return spells.get(name);
	}
	
	public void info(String message) {
		log.info(message);
	}
	
	public void toggleCastCommandOverrides(PlayerSpells playerSpells, boolean override) {
		playerSpells.setCostReduction(override ? castCommandCostReduction : 0);
		playerSpells.setCooldownReduction(override ? castCommandCooldownReduction : 0);
	}

	/*
	 * Private data
	 */
	 private final String                        spellsFileName                 = "spells.yml";
	 private final String                        propertiesFileName             = "magic.yml";
	 private final String                        playersFileName                 = "players.yml";
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
	 private boolean                             silent                         = false;
	 private boolean                             quiet                          = true;
	 private boolean                             soundsEnabled                  = true;
	 private int								 messageThrottle				= 0;
	 private boolean							 blockPopulatorEnabled			= false;
	 private boolean							 enchantingEnabled				= false;
	 private float							 	 maxPowerMultiplier			    = 1.0f;
	 private float							 	 castCommandCostReduction	    = 1.0f;
	 private float							 	 castCommandCooldownReduction	    = 1.0f;
	 private ConfigurationNode					 blockPopulatorConfig			= null;
	 private HashMap<String, UndoQueue>          playerUndoQueues               = new HashMap<String, UndoQueue>();
	 private LinkedList<BlockBatch>				 pendingBatches					= new LinkedList<BlockBatch>();
	 private int								 maxBlockUpdates				= 100;
	 
	 private final Logger                        log                            = Logger.getLogger("Minecraft");
	 private final HashMap<String, Spell>        spells                         = new HashMap<String, Spell>();
	 private final HashMap<String, PlayerSpells> playerSpells                   = new HashMap<String, PlayerSpells>();

	 private Recipe								 wandRecipe						= null;
	 private Material							 wandRecipeUpperMaterial		= Material.DIAMOND;
	 private Material							 wandRecipeLowerMaterial		= Material.BLAZE_ROD;
	 private String								 recipeOutputTemplate			= "random(1)";
	 
	 private MagicPlugin                         plugin                         = null;
	 private Object								 regionManager					= null;
}
