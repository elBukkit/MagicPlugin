package com.elmakers.mine.bukkit.plugins.magic;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitScheduler;

import com.elmakers.mine.bukkit.dao.BlockList;
import com.elmakers.mine.bukkit.utilities.CSVParser;
import com.elmakers.mine.bukkit.utilities.UndoQueue;
import com.elmakers.mine.bukkit.utilities.UpdateInventoryTask;
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
		Material m = variant.getMaterial();
		if (m != null && m != Material.AIR)
		{
			/*
            if (buildingMaterials.contains(m))
            {
                log.warning("Spell " + variant.getName() + " uses building material as icon: " + m.name().toLowerCase());
            }
			 */
			conflict = spellsByMaterial.get(m);
			if (conflict != null)
			{
				log.log(Level.WARNING, "Duplicate spell material: " + m.name() + " for " + conflict.getKey() + " and " + variant.getKey());
			}
			else
			{
				spellsByMaterial.put(variant.getMaterial(), variant);
			}
		}
	}

	/*
	 * Material use system
	 */

	public List<Material> getBuildingMaterials()
	{
		return buildingMaterials;
	}

	public List<Material> getTargetThroughMaterials()
	{
		return targetThroughMaterials;
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
		UndoQueue queue = getUndoQueue(player.getName());

		queue.add(blocks);
	}

	public boolean undoAny(Player player, Block target)
	{
		for (String playerName : playerUndoQueues.keySet())
		{
			UndoQueue queue = playerUndoQueues.get(playerName);
			if (queue.undo(target))
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
		return queue.undo();
	}

	public boolean undo(String playerName, Block target)
	{
		UndoQueue queue = getUndoQueue(playerName);
		return queue.undo(target);
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
		scheduler.scheduleSyncDelayedTask(plugin, new CleanupBlocksTask(blocks), ticksToLive);
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

	public void cancel(Player player)
	{
		PlayerSpells playerSpells = getPlayerSpells(player);
		playerSpells.cancel();
	}

	public boolean isQuiet()
	{
		return quiet;
	}

	public boolean isSilent()
	{
		return silent;
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

	/*
	 * Internal functions - don't call these, or really anything below here.
	 */

	/*
	 * Saving and loading
	 */

	public void initialize(MagicPlugin plugin)
	{
		this.plugin = plugin;
		load();
		
		// Set up the Wand-tracking timer
		Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			public void run() {
				for (PlayerSpells spells : playerSpells.values()) {
					Player player = spells.getPlayer();
					if (player.isOnline()) { 
						int xpRegeneration = spells.getXpRegeneration();
						int xpMax = spells.getXPMax();
						if (xpRegeneration > 0 && player.getTotalExperience() < xpMax) {
							player.giveExp(xpRegeneration);
						}
						int healthRegeneration = spells.getHealthRegeneration();
						if (healthRegeneration > 0 && player.getHealth() < 20) {
							player.setHealth(Math.min(20, player.getHealth() + healthRegeneration));
						}
						int hungerRegeneration = spells.getHungerRegeneration();
						if (hungerRegeneration > 0 && player.getFoodLevel() < 20) {
							player.setExhaustion(0);
							player.setFoodLevel(Math.min(20, player.getFoodLevel() + hungerRegeneration));
						}
					}
				}
			}
		}, 0, 20);
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
		
		Wand.load(plugin);

		log.info("Magic: Loaded " + spells.size() + " spells and " + Wand.getWandTemplates().size() + " wands");
	}

	protected void save(File spellsFile)
	{
		Configuration config = new Configuration(spellsFile);
		ConfigurationNode spellsNode = config.createChild("spells");

		for (Spell spell : spells.values())
		{
			ConfigurationNode spellNode = spellsNode.createChild(spell.getKey());
			spell.save(spellNode);
		}

		config.save();
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
		silent = generalNode.getBoolean("silent", silent);
		quiet = generalNode.getBoolean("quiet", quiet);

		buildingMaterials = generalNode.getMaterials("building", DEFAULT_BUILDING_MATERIALS);
		targetThroughMaterials = generalNode.getMaterials("target_through", DEFAULT_TARGET_THROUGH_MATERIALS);

		CSVParser csv = new CSVParser();
		stickyMaterials = csv.parseMaterials(STICKY_MATERIALS);
		stickyMaterialsDoubleHeight = csv.parseMaterials(STICKY_MATERIALS_DOUBLE_HEIGHT);

		properties.save();
	}

	public void clear()
	{
		playerSpells.clear();
		spells.clear();
		spellsByMaterial.clear();
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
	
	protected Spell getActiveSpell(Player player) {
		Inventory inventory = player.getInventory();
		ItemStack[] contents = inventory.getContents();
		PlayerSpells playerSpells = getPlayerSpells(player);
		Spell spell = null;
		for (int i = 0; i < 9; i++)
		{
			if (contents[i] == null || contents[i].getType() == Material.AIR || Wand.isWand(contents[i]))
			{
				continue;
			}
			spell = playerSpells.getSpell(contents[i].getType());
			if (spell != null)
			{
				break;
			}
		}
		
		return spell;
	}
	
	protected void cast(Player player)
	{
		Wand wand = Wand.getActiveWand(player);
		if (wand != null)
		{
			if (!hasWandPermission(player))
			{
				return;
			}

			Spell spell = getActiveSpell(player);
			PlayerSpells playerSpells = getPlayerSpells(player);
			if (spell != null)
			{
				if (spell.cast()) {
					wand.use(playerSpells);
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	public boolean cycleMaterials(Player player)
	{
		List<Material> buildingMaterials = getBuildingMaterials();
		PlayerInventory inventory = player.getInventory();
		ItemStack[] contents = inventory.getContents();
		int firstMaterialSlot = 8;
		int lastMaterialSlot = 8;
		boolean foundAir = false;

		for (int i = 8; i >= 0; i--)
		{
			Material mat = contents[i] == null ? Material.AIR : contents[i].getType();
			if (mat == Material.AIR)
			{
				if (foundAir)
				{
					break;
				}
				else
				{
					foundAir = true;
					firstMaterialSlot = i;
					continue;
				}
			}
			else
			{
				if (!Wand.isSpell(contents[i]) && !Wand.isWand(contents[i]) && (mat == Wand.EraseMaterial || buildingMaterials.contains(mat)))
				{
					firstMaterialSlot = i;
					continue;
				}
				else if (Wand.isWand(contents[i])) 
				{
					lastMaterialSlot = i - 1;
					continue;
				} else 
				{
					break;
				}
			}
		}

		if (firstMaterialSlot >= lastMaterialSlot)
			return false;

		ItemStack lastSlot = contents[lastMaterialSlot];
		for (int i = lastMaterialSlot - 1; i >= firstMaterialSlot; i--)
		{
			contents[i + 1] = contents[i];
		}
		contents[firstMaterialSlot] = lastSlot;

		inventory.setContents(contents);
		
		// Some hackery to try and get a tooltip to show up on item switch
		Wand wand = Wand.getActiveWand(player);
		PlayerSpells playerSpells = getPlayerSpells(player);
		wand.updateName(playerSpells);
		
		player.updateInventory();

		return true;
	}

	@SuppressWarnings("deprecation")
	public void cycleSpells(Player player)
	{
		Inventory inventory = player.getInventory();
		ItemStack[] contents = inventory.getContents();
		ItemStack[] active = new ItemStack[9];

		for (int i = 0; i < 9; i++)
		{
			active[i] = contents[i];
		}

		int maxSpellSlot = 0;
		int firstSpellSlot = -1;
		PlayerSpells playerSpells = getPlayerSpells(player);
		for (int i = 0; i < 9; i++)
		{
			boolean isEmpty = active[i] == null;
			Material activeType = isEmpty ? Material.AIR : active[i].getType();
			boolean isWand = isEmpty ? false : Wand.isWand(active[i]);
			boolean isSpell = false;
			if (activeType != Material.AIR && Wand.isSpell(active[i]))
			{
				Spell spell = playerSpells.getSpell(activeType);
				isSpell = spell != null;
			}

			if (isSpell)
			{
				if (firstSpellSlot < 0)
					firstSpellSlot = i;
				maxSpellSlot = i;
			}
			else
			{
				if (!isWand && firstSpellSlot >= 0)
				{
					break;
				}
			}

		}

		int numSpellSlots = firstSpellSlot < 0 ? 0 : maxSpellSlot - firstSpellSlot + 1;

		if (numSpellSlots < 2)
		{
			return;
		}

		for (int ddi = 0; ddi < numSpellSlots; ddi++)
		{
			int i = ddi + firstSpellSlot;
			boolean isEmpty = contents[i] == null;
			boolean isWand = isEmpty ? false : Wand.isWand(active[i]);

			if (!isWand)
			{
				for (int di = 1; di < numSpellSlots; di++)
				{
					int dni = (ddi + di) % numSpellSlots;
					int ni = dni + firstSpellSlot;
					isEmpty = active[ni] == null;
					isWand = isEmpty ? false : Wand.isWand(active[ni]);
					if (!isWand)
					{
						contents[i] = active[ni];
						break;
					}
				}
			}
		}

		inventory.setContents(contents);
		// Some hackery to try and get a tooltip to show up on item switch
		Wand wand = Wand.getActiveWand(player);
		wand.updateName(playerSpells);
		player.updateInventory();
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
	
	@EventHandler
	public void onPlayerEquip(PlayerItemHeldEvent event)
	{
		Player player = event.getPlayer();
		PlayerInventory inventory = player.getInventory();
		ItemStack previous = inventory.getItem(event.getPreviousSlot());
		ItemStack next = inventory.getItem(event.getNewSlot());

		boolean wasWand = previous != null && Wand.isWand(previous);
		boolean isWand = next != null && Wand.isWand(next);

		// If we're not dealing with wands, we don't care
		// And you should never be switching directly from one wand to another!
		if (wasWand == isWand) return;
		
		PlayerSpells playerSpells = getPlayerSpells(player);

		// If we're switching to a wand, save the inventory.
		if (isWand) {
			Wand newWand = new Wand(next);
			if (playerSpells.storeInventory(event.getNewSlot(), next)) {
				newWand.activate(playerSpells, event.getNewSlot());
			}
		} else if (wasWand) {
			Wand oldWand = new Wand(previous);
			oldWand.deactivate(playerSpells);

			// Restore inventory
			playerSpells.restoreInventory(event.getPreviousSlot(), previous);

			// Check for new wand selection, after restoring inventory
			next = inventory.getItem(event.getNewSlot());
			if (Wand.isWand(next)) {
				Wand newWand = new Wand(next);
				if (playerSpells.storeInventory(event.getNewSlot(), next)) {
					newWand.activate(playerSpells, event.getNewSlot());
				}
			}
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
		PlayerSpells spells = getPlayerSpells(player);
		String rule = player.getWorld().getGameRuleValue("keepInventory");
		if (spells.hasStoredInventory() && !rule.equals("true")) {
			List<ItemStack> drops = event.getDrops();
			drops.clear();

			// Drop the held wand, since that's not in the stored inventory
			ItemStack wand = player.getInventory().getItemInHand();
			if (Wand.isWand(wand)) {
				drops.add(wand);
			}

			player.getInventory().clear();
			ItemStack[] stored = spells.getStoredInventory().getContents();
			spells.clearStoredInventory();
			for (ItemStack stack : stored) {
				if (stack != null) {
					drops.add(stack);
				}
			}
		}

		spells.onPlayerDeath(event);
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
		if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)
		{
			cast(event.getPlayer());
		}
		else
			if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
			{
				cancel(event.getPlayer());
				Player player = event.getPlayer();

				if (!hasWandPermission(player))
				{
					return;
				}

				boolean cycleSpells = false;

				cycleSpells = player.isSneaking();
				if (Wand.isActive(player))
				{
					if (cycleSpells)
					{
						if (!cycleMaterials(event.getPlayer()))
						{
							cycleSpells(event.getPlayer());
						}
					}
					else
					{
						cycleSpells(event.getPlayer());
					}
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
		if (Wand.isActive(player)) {
			// Save inventory
			PlayerSpells spells = getPlayerSpells(player);
			if (spells.storeInventory()) {
				// Create spell inventory
				Wand wand = new Wand(player.getItemInHand());
				wand.activate(spells);
			}
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		PlayerSpells spells = getPlayerSpells(event.getPlayer());
		spells.onPlayerQuit(event);
		spells.restoreInventory();
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPluginDisable(PluginDisableEvent event)
	{
		for (PlayerSpells spells : playerSpells.values()) {
			Player player = spells.getPlayer();
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
			Wand wand = Wand.getActiveWand(player);
			if (wand != null) {
				PlayerSpells spells = getPlayerSpells(player);
				wand.activate(spells);
				player.updateInventory();
			}
		}
	}

	@EventHandler
	public void onPlayerCraftItem(CraftItemEvent event)
	{
		if (!(event.getWhoClicked() instanceof Player)) return;
		Player player = (Player)event.getWhoClicked();
		PlayerSpells spells = getPlayerSpells(player);
		if (spells.hasStoredInventory()) {
			event.setCancelled(true); 
		}
	}

	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent event) {
		if (!(event.getPlayer() instanceof Player)) return;
		Player player = (Player)event.getPlayer();
		PlayerSpells playerSpells = getPlayerSpells(player);
		if (playerSpells.hasStoredInventory()) {
			if (Wand.isActive(player)) {
				Wand wand = new Wand(player.getItemInHand());
				wand.deactivate(playerSpells);
			}
			playerSpells.restoreInventory();
		}
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if (!(event.getPlayer() instanceof Player)) return;
		Player player = (Player)event.getPlayer();
		PlayerSpells spells = getPlayerSpells(player);
		if (!spells.hasStoredInventory() && Wand.isActive(player)) {
			if (spells.storeInventory()) {
				Wand wand = new Wand(player.getItemInHand());
				wand.activate(spells);
				
				// Need an extra update here, probably something happens after inventory close.
				new UpdateInventoryTask(player).runTaskLater(this.plugin, 2);
			}
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
				Wand wand = new Wand(pickup);
				event.setCancelled(true);
				event.getItem().remove();
				inventory.setItem(inventory.getHeldItemSlot(), pickup);
				if (spells.storeInventory()) {
					// Create spell inventory
					wand.activate(spells);
				}
			} 
		}
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event)
	{
		PlayerSpells spells = getPlayerSpells(event.getPlayer());
		if (spells.hasStoredInventory()) {
			ItemStack inHand = event.getPlayer().getInventory().getItemInHand();
			if (Wand.isWand(event.getItemDrop().getItemStack()) && (inHand == null || inHand.getType() == Material.AIR)) {
				spells.restoreInventory(0,  null);
			} else {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player)) return;
		if (event.getView().getType() != InventoryType.CRAFTING) return;

		Player player = (Player)event.getWhoClicked();
		PlayerInventory inventory = player.getInventory();
		PlayerSpells spells = getPlayerSpells(player);
		if (spells.hasStoredInventory()) {
			if (event.getSlot() == inventory.getHeldItemSlot()) {
				event.setCancelled(true);
			}
		}
	}

	public Spell getSpell(Material material) {
		return spellsByMaterial.get(material);
	}
	
	public Spell getSpell(String name) {
		return spells.get(name);
	}

	/*
	 * Private data
	 */
	 private final String                        spellsFileName                 = "spells.yml";
	 private final String                        propertiesFileName             = "magic.yml";
	 private final String                        spellsFileNameDefaults         = "spells.defaults.yml";
	 private final String                        propertiesFileNameDefaults     = "magic.defaults.yml";

	 static final String                         DEFAULT_BUILDING_MATERIALS     = "0,1,2,3,4,5,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,33,34,35,41,42,43,45,46,47,48,49,52,53,55,56,57,58,60,61,62,65,66,67,73,74,79,80,81,82,83,84,85,86,87,88,89,90,91,92,93,94,95,96,97,98,99,100,101,102,103,104,105,106,107,108,109";
	 static final String                         DEFAULT_TARGET_THROUGH_MATERIALS = "0";
	 
	 static final String                         STICKY_MATERIALS               = "37,38,39,50,51,55,59,63,64,65,66,68,70,71,72,75,76,77,78,83";
	 static final String                         STICKY_MATERIALS_DOUBLE_HEIGHT = "64,71,";

	 private List<Material>                      buildingMaterials              = new ArrayList<Material>();
	 private List<Material>                      stickyMaterials                = new ArrayList<Material>();
	 private List<Material>                      stickyMaterialsDoubleHeight    = new ArrayList<Material>();
	 private List<Material>                      targetThroughMaterials  		= new ArrayList<Material>();

	 private long                                physicsDisableTimeout          = 0;
	 private int                                 undoQueueDepth                 = 256;
	 private boolean                             silent                         = false;
	 private boolean                             quiet                          = true;
	 private HashMap<String, UndoQueue>          playerUndoQueues               = new HashMap<String, UndoQueue>();

	 private final Logger                        log                            = Logger.getLogger("Minecraft");
	 private final HashMap<String, Spell>        spells                         = new HashMap<String, Spell>();
	 private final HashMap<Material, Spell>      spellsByMaterial               = new HashMap<Material, Spell>();
	 private final HashMap<String, PlayerSpells> playerSpells                   = new HashMap<String, PlayerSpells>();

	 private MagicPlugin                         plugin                         = null;
}
