package com.elmakers.mine.bukkit.plugins.magic;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitScheduler;

import com.elmakers.mine.bukkit.dao.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.spells.AbsorbSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.AlterSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.ArrowSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.BlinkSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.BoomSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.BridgeSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.ConstructSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.CushionSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.DisintegrateSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.FamiliarSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.FillSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.FireSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.FireballSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.FlingSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.ForceSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.FrostSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.GillsSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.GotoSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.GrenadeSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.HealSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.InvincibleSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.InvisibilitySpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.LavaSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.LevitateSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.LightningSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.MineSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.PillarSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.PortalSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.RecallSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.SignSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.TorchSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.TreeSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.UndoSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.WeatherSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.WolfSpell;
import com.elmakers.mine.bukkit.utilities.CSVParser;
import com.elmakers.mine.bukkit.utilities.InventoryUtils;
import com.elmakers.mine.bukkit.utilities.UndoQueue;
import com.elmakers.mine.bukkit.utilities.UpdateInventoryTask;
import com.elmakers.mine.bukkit.utilities.borrowed.Configuration;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class Spells implements Listener 
{
	public static Enchantment MagicEnchantment = Enchantment.ARROW_INFINITE;

	/*
	 * Public API - Use for hooking up a plugin, or calling a spell
	 */

	public Spell getSpell(Material material, Player player)
	{
		Spell spell = spellsByMaterial.get(material);
		if (spell == null || !spell.hasSpellPermission(player))
			return null;

		return getSpell(spell.getName(), player);
	}

	public Spell getSpell(String name, Player player)
	{
		Spell spell = spells.get(name);
		if (spell == null || !spell.hasSpellPermission(player))
			return null;

		PlayerSpells playerSpells = getPlayerSpells(player);
		Spell playerSpell = playerSpells.getSpell(spell.getName());
		if (playerSpell == null)
		{
			playerSpell = (Spell)spell.clone();
			playerSpells.addSpell(playerSpell);
		}

		playerSpell.setPlayer(player);

		return playerSpell;
	}

	public PlayerSpells getPlayerSpells(Player player)
	{
		PlayerSpells spells = playerSpells.get(player.getName());
		if (spells == null)
		{
			spells = new PlayerSpells(player);
			playerSpells.put(player.getName(), spells);
		}

		spells.setPlayer(player);

		return spells;
	}

	protected void createDefaultSpells()
	{
		createSpell(new AbsorbSpell(), "absorb", Material.BUCKET, "Absorb some of the target", "construction", "");
		createSpell(new AlterSpell(), "alter", Material.REDSTONE_TORCH_ON, "Alter certain objects", "construction", "");
		createSpell(new ArrowSpell(), "arrow", Material.ARROW, "Fire a magic arrow", "combat", "");
		createSpell(new ArrowSpell(), "arrowrain", Material.BOW, "Fire a volley of arrows", "combat", "count 4");
		createSpell(new BlinkSpell(), "blink", Material.FEATHER, "Teleport to your target", "psychic", "");
		createSpell(new BlinkSpell(), "ascend", Material.RED_MUSHROOM, "Go up to the nearest safe spot", "psychic", "type ascend");
		createSpell(new BlinkSpell(), "descend", Material.BROWN_MUSHROOM, "Travel underground", "psychic", "type descend");
		createSpell(new BlinkSpell(), "tesseract", Material.WEB, "Blink a short distance", "psychic", "", "range 8 allow_passthrough false allow_ascend false allow_descend false");
		createSpell(new BoomSpell(), "boom", Material.RED_ROSE, "Create an explosion", "combat", "");
		createSpell(new BoomSpell(), "kaboom", Material.REDSTONE_WIRE, "Create a big explosion", "combat", "size 6");
		createSpell(new BoomSpell(), "kamikazee", Material.DEAD_BUSH, "Kill yourself with an explosion", "combat", "size 8 target here");
		createSpell(new BoomSpell(), "nuke", Material.BED, "Create a huge explosino", "combat", "size 20");
		createSpell(new BridgeSpell(), "bridge", Material.GOLD_HOE, "Extend the ground underneath you", "construction", "");
		createSpell(new ConstructSpell(), "blob", Material.CLAY_BALL, "Create a solid blob", "construction", "type sphere radius 3", "allow_max_range true");
		createSpell(new ConstructSpell(), "shell", Material.BOWL, "Create a large spherical shell", "construction", "type sphere fill hollow radius 10");
		createSpell(new ConstructSpell(), "box", Material.GOLD_HELMET, "Create a large hollow box", "construction", "type cuboid fill hollow radius 6");
		createSpell(new ConstructSpell(), "superblob", Material.CLAY_BRICK, "Create a large solid sphere", "construction", "type sphere radius 8", "allow_max_range true");
		createSpell(new ConstructSpell(), "sandblast", Material.SANDSTONE, "Drop a big block of sand", "combat", "type cuboid radius 4 material sand", "allow_max_range true");
		createSpell(new ConstructSpell(), "blast", Material.SULPHUR, "Mine out a large area", "mining", "type sphere material air", "destructible 1,2,3,4,10,11,12,13,87,88");
		createSpell(new ConstructSpell(), "superblast", Material.SLIME_BALL, "Mine out a very large area", "mining", "type sphere radius 16 material air", "destructible 1,2,3,4,10,11,12,13,87,88");
		createSpell(new ConstructSpell(), "peek", Material.SUGAR_CANE, "Temporarily glass the target surface", "alchemy", "material glass type sphere radius 3", "undo 4000 destructible 1,2,3,4,8,9,10,11,12,13,87,88");
		createSpell(new ConstructSpell(), "breach", Material.SEEDS, "Temporarily destroy the target surface", "alchemy", "material air type sphere radius 4", "undo 10000 destructible 1,2,3,4,8,9,10,11,12,13,87,88");
		createSpell(new CushionSpell(), "cushion", Material.SOUL_SAND, "Create a safety bubble", "alchemy", "");
		createSpell(new DisintegrateSpell(), "disintegrate", Material.BONE, "Damage your target", "combat", "");
		createSpell(new FamiliarSpell(), "familiar", Material.EGG, "Create an animal familiar", "summoner", "");
		createSpell(new FamiliarSpell(), "monster", Material.PUMPKIN, "Call a monster to your side", "summoner", "type monster");
		createSpell(new FamiliarSpell(), "mob", Material.JACK_O_LANTERN, "Summon a mob of monsters", "summoner", "type mob count 20");
		createSpell(new FamiliarSpell(), "ender", Material.ENDER_PEARL, "Summon an enderman", "summoner", "type enderman");
		createSpell(new FamiliarSpell(), "farm", Material.WHEAT, "Create a herd", "farming", "30");
		createSpell(new FillSpell(), "fill", Material.GOLD_SPADE, "Fill a selected area (cast twice)", "construction", "");
		createSpell(new FillSpell(), "paint", Material.PAINTING, "Fill a single block", "alchemy", "type single");
		createSpell(new FillSpell(), "recurse", Material.WOOD_SPADE, "Recursively fill blocks", "alchemy", "type recurse");
		createSpell(new FireballSpell(), "fireball", Material.NETHERRACK, "Cast an exploding fireball", "combat", "");
		createSpell(new FireballSpell(), "icbm", Material.ROTTEN_FLESH, "Cast an exploding fireball", "combat", "size 10");
		createSpell(new FireSpell(), "fire", Material.FLINT_AND_STEEL, "Light fires from a distance", "elemental", "");
		createSpell(new FireSpell(), "inferno", Material.FIRE, "Burn a wide area", "master", "6");
		createSpell(new FlingSpell(), "fling", Material.LEATHER_BOOTS, "Sends you flying in the target direction", "psychic", "size 5");
		createSpell(new ForceSpell(), "force", Material.STRING, "Use telekinesis", "psychic", "", "allow_max_range true range 48");
		createSpell(new ForceSpell(), "pull", Material.FISHING_ROD, "Pull things toward you", "psychic", "type pull range 32");
		createSpell(new ForceSpell(), "push", Material.RAILS, "Push things away from you", "psychic", "type push range 16");
		createSpell(new FrostSpell(), "frost", Material.SNOW_BALL, "Freeze water and create snow", "alchemy", "", "range 24");
		createSpell(new GillsSpell(), "gills", Material.RAW_FISH, "Restores health while moving underwater", "medic", "");
		createSpell(new GotoSpell(), "gather", Material.GLOWSTONE_DUST, "Gather groups of players together", "master", "");     
		createSpell(new GrenadeSpell(), "grenade", Material.TNT, "Place a primed grenade", "combat", "", "", "tnt 1");
		createSpell(new HealSpell(), "heal", Material.BREAD, "Heal yourself or others", "medic", "", "cooldown 5000", "apple 1 wheat 1");
		createSpell(new InvincibleSpell(), "invincible", Material.GOLDEN_APPLE, "Make yourself impervious to damage", "master", "");
		createSpell(new InvincibleSpell(), "ironskin", Material.IRON_CHESTPLATE, "Protect you from damage", "master", "amount 99");
		createSpell(new InvincibleSpell(), "leatherskin", Material.LEATHER_CHESTPLATE, "Protect you from some damage", "combat", "amount 50");
		createSpell(new InvisibilitySpell(), "cloak", Material.CHAINMAIL_CHESTPLATE, "Make yourself invisible while still", "psychic", "");
		createSpell(new LavaSpell(), "lava", Material.LAVA, "Fire a stream of lava", "combat", "");
		createSpell(new LevitateSpell(), "levitate", Material.GOLD_BOOTS, "Levitate yourself up into the air", "psychic", "");
		createSpell(new LightningSpell(), "lightning", Material.COOKED_FISH, "Strike lighting at your target", "combat", "");
		createSpell(new LightningSpell(), "storm", Material.GRILLED_PORK, "Start a lightning storm", "elemental", "radius 10", "cooldown 500");
		createSpell(new MineSpell(), "mine", Material.GOLD_PICKAXE, "Mines and drops the targeted resources", "mining", "");
		createSpell(new PillarSpell(), "pillar", Material.GOLD_AXE, "Raises a pillar up", "construction", "");
		createSpell(new PillarSpell(), "stalactite", Material.WOOD_AXE, "Create a downward pillar", "construction", "type down");
		createSpell(new PortalSpell(), "portal", Material.PORTAL, "Create two connected portals", "psychic", "");
		createSpell(new RecallSpell(), "recall", Material.COMPASS, "Marks locations for return", "exploration", "");
		createSpell(new RecallSpell(), "spawn", Material.YELLOW_FLOWER, "Take yourself back home", "exploration", "type spawn");
		createSpell(new SignSpell(), "sign", Material.SIGN_POST, "Give yourself some signs", "master", "");
		createSpell(new SignSpell(), "tag", Material.SIGN, "Leave a sign with your name", "exploration", "tag", "cooldown 10000");
		createSpell(new TorchSpell(), "torch", Material.TORCH, "Shed some light", "exploration", "");
		createSpell(new TorchSpell(), "day", Material.FLINT, "Change time time to day", "elemental", "time day");
		createSpell(new TorchSpell(), "night", Material.COAL, "Change time time to night", "elemental", "time night");
		createSpell(new TreeSpell(), "tree", Material.SAPLING, "Instantly grow a tree", "farming", "");
		createSpell(new UndoSpell(), "rewind", Material.WATCH, "Undo your last action", "alchemy", "");
		createSpell(new UndoSpell(), "erase", Material.LEVER, "Undo your target construction", "alchemy", "");
		createSpell(new WeatherSpell(), "weather", Material.WATER, "Change the weather", "elemental", "");
		createSpell(new WolfSpell(), "wolf", Material.PORK, "Create a wolf familiar to follow you around", "summoner", "", "cooldown 500");  
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

		template.load(name, spellNode);

		addSpell(template);
	}

	public void addSpell(Spell variant)
	{
		Spell conflict = spells.get(variant.getName());
		if (conflict != null)
		{
			log.log(Level.WARNING, "Duplicate spell name: '" + conflict.getName() + "'");
		}
		else
		{
			spells.put(variant.getName(), variant);
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
				log.log(Level.WARNING, "Duplicate spell material: " + m.name() + " for " + conflict.getName() + " and " + variant.getName());
			}
			else
			{
				spellsByMaterial.put(variant.getMaterial(), variant);
			}
		}

		variant.initialize(this);
	}

	/*
	 * Material use system
	 */

	public List<Material> getBuildingMaterials()
	{
		return buildingMaterials;
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
	}

	public void load()
	{
		File dataFolder = plugin.getDataFolder();
		dataFolder.mkdirs();

		File propertiesFile = new File(dataFolder, propertiesFileName);
		loadProperties(propertiesFile);

		File spellsFile = new File(dataFolder, spellsFileName);

		if (!spellsFile.exists())
		{
			createDefaultSpells();
			save(spellsFile);
		}
		else
		{
			load(spellsFile);
		}

		log.info("Magic: Loaded " + spells.size() + " spells.");
	}

	protected void save(File spellsFile)
	{
		Configuration config = new Configuration(spellsFile);
		ConfigurationNode spellsNode = config.createChild("spells");

		for (Spell spell : spells.values())
		{
			ConfigurationNode spellNode = spellsNode.createChild(spell.getName());
			spell.save(spellNode);
		}

		config.save();
	}

	protected void load(File spellsFile)
	{
		Configuration config = new Configuration(spellsFile);
		config.load();

		ConfigurationNode spellsNode = config.getNode("spells");
		if (spellsNode == null) return;

		List<String> spellKeys = spellsNode.getKeys();
		for (String key : spellKeys)
		{
			ConfigurationNode spellNode = spellsNode.getNode(key);
			Spell newSpell = Spell.loadSpell(key, spellNode);
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
		Configuration properties = new Configuration(propertiesFile);
		properties.load();

		ConfigurationNode generalNode = properties.createChild("general");
		undoQueueDepth = generalNode.getInteger("undo_depth", undoQueueDepth);
		silent = generalNode.getBoolean("silent", silent);
		quiet = generalNode.getBoolean("quiet", quiet);

		buildingMaterials = generalNode.getMaterials("building", DEFAULT_BUILDING_MATERIALS);

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

		load();
	}

	public void updateWandInventory(Player player) {
		updateWandInventory(player, player.getInventory().getHeldItemSlot(), player.getInventory().getItemInHand());
	}

	@SuppressWarnings("deprecation")
	protected void updateWandInventory(Player player, int itemSlot, ItemStack wand) {
		if (!isWand(wand)) return;

		Inventory inventory = player.getInventory();
		inventory.clear();
		inventory.setItem(itemSlot, wand);
		String spellString = InventoryUtils.getMeta(wand, "magic_spells");
		String[] spells = StringUtils.split(spellString, "|");

		int currentIndex = 0;
		for (int i = 0; i < spells.length; i++) {
			if (currentIndex == itemSlot) currentIndex++;
			Spell spell = getSpell(spells[i], player);
			if (spell != null) {
				ItemStack itemStack = new ItemStack(spell.getMaterial(), 1);
				itemStack.addUnsafeEnchantment(Spells.MagicEnchantment, 1);
				ItemMeta meta = itemStack.getItemMeta();
				meta.setDisplayName("Spell: " + spell.getName());
				List<String> lore = new ArrayList<String>();
				lore.add(spell.getCategory());
				lore.add(spell.getDescription());
				meta.setLore(lore);
				itemStack.setItemMeta(meta);
				inventory.setItem(currentIndex, itemStack);
			}

			currentIndex++;
		}

		player.updateInventory();
	}


	public static ItemStack setWandSpells(ItemStack wand, Collection<String> spellNames) {
		String spellString = StringUtils.join(spellNames, "|");

		// Update wand lore - do this BEFORE setMeta, else Bukkit will squash our spell list.
		updateWand(wand, spellNames.size());

		// Set new spells string, which creates a copy of the wand.
		wand = InventoryUtils.setMeta(wand,  "magic_spells", spellString);

		return wand;
	}


	public static void updateWand(ItemStack wand, int spellCount) {
		updateWand(wand, spellCount, null);
	}

	public static void updateWand(ItemStack wand, int spellCount, String name) {
		ItemMeta meta = wand.getItemMeta();
		if (name != null) {
			meta.setDisplayName(name);
		}
		List<String> lore = new ArrayList<String>();
		lore.add("Knows " + spellCount +" Spells");
		lore.add("Left-click to cast active spell");
		lore.add("Right-click to cycle spells");
		meta.setLore(lore);
		wand.setItemMeta(meta);
	}

	public List<Spell> getAllSpells()
	{
		List<Spell> allSpells = new ArrayList<Spell>();
		allSpells.addAll(spells.values());
		return allSpells;
	}

	protected void cast(Player player)
	{
		if (isWandActive(player))
		{
			if (!hasWandPermission(player))
			{
				return;
			}

			Inventory inventory = player.getInventory();
			ItemStack[] contents = inventory.getContents();

			Spell spell = null;
			for (int i = 0; i < 9; i++)
			{
				if (contents[i] == null || contents[i].getType() == Material.AIR || isWand(contents[i]))
				{
					continue;
				}
				spell = getSpell(contents[i].getType(), player);
				if (spell != null)
				{
					break;
				}
			}

			if (spell != null)
			{
				spell.cast();
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
				if (buildingMaterials.contains(mat))
				{
					firstMaterialSlot = i;
					continue;
				}
				else
				{
					break;
				}
			}
		}

		if (firstMaterialSlot == 8)
			return false;

		ItemStack lastSlot = contents[8];
		for (int i = 7; i >= firstMaterialSlot; i--)
		{
			contents[i + 1] = contents[i];
		}
		contents[firstMaterialSlot] = lastSlot;

		inventory.setContents(contents);
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
		for (int i = 0; i < 9; i++)
		{
			boolean isEmpty = active[i] == null;
			Material activeType = isEmpty ? Material.AIR : active[i].getType();
			boolean isWand = isEmpty ? false : isWand(active[i]);
			boolean isSpell = false;
			if (activeType != Material.AIR && active[i].hasItemMeta() && active[i].getItemMeta().hasEnchant(MagicEnchantment))
			{
				Spell spell = getSpell(activeType, player);
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
			boolean isWand = isEmpty ? false : isWand(active[i]);

			if (!isWand)
			{
				for (int di = 1; di < numSpellSlots; di++)
				{
					int dni = (ddi + di) % numSpellSlots;
					int ni = dni + firstSpellSlot;
					isEmpty = active[ni] == null;
					isWand = isEmpty ? false : isWand(active[ni]);
					if (!isWand)
					{
						contents[i] = active[ni];
						break;
					}
				}
			}
		}

		inventory.setContents(contents);
		player.updateInventory();
	}

	public static boolean isWandActive(Player player) {
		ItemStack activeItem =  player.getInventory().getItemInHand();
		return isWand(activeItem);
	}

	public static boolean isWand(ItemStack item) {
		return item != null && item.getType() == Material.STICK && item.hasItemMeta() && item.getItemMeta().hasEnchant(MagicEnchantment);
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
		return hasPermission(player, "Magic.wand.use");
	}

	public boolean hasPermission(Player player, String pNode, boolean defaultValue)
	{
		// TODO: What happened to built-in bukkit permissions?
		return defaultValue;
	}

	public boolean hasPermission(Player player, String pNode)
	{
		return hasPermission(player, pNode, true);
	}

	/*
	 * Listeners / callbacks
	 */
	@EventHandler
	public void onPlayerEquip(PlayerItemHeldEvent event)
	{
		Player player = event.getPlayer();
		PlayerInventory inventory = player.getInventory();
		ItemStack previous = inventory.getItem(event.getPreviousSlot());
		ItemStack next = inventory.getItem(event.getNewSlot());

		boolean wasWand = previous != null && isWand(previous);
		boolean isWand = next != null && isWand(next);

		// If we're not dealing with wands, we don't care
		// And you should never be switching directly from one wand to another!
		if (wasWand == isWand) return;

		// If we're switching to a wand, save the inventory.
		if (isWand) {
			PlayerSpells spells = getPlayerSpells(player);
			if (spells.storeInventory(event.getNewSlot(), next)) {
				updateWandInventory(player, event.getNewSlot(), next);
			}
		} else if (wasWand) {
			// Rebuild spell inventory, save in wand.
			ItemStack[] items = inventory.getContents();
			List<String> spellNames = new ArrayList<String>();
			for (int i = 0; i < items.length; i++) {
				if (items[i] == null) continue;
				if (!items[i].hasItemMeta() || !items[i].getItemMeta().hasEnchant(MagicEnchantment)) continue;

				Spell spell = getSpell(items[i].getType(), player);
				if (spell == null) continue;
				spellNames.add(spell.getName());
			}
			previous = setWandSpells(previous, spellNames);

			// Restore inventory
			PlayerSpells spells = getPlayerSpells(player);
			spells.restoreInventory(event.getPreviousSlot(), previous);

			// Check for new wand selection
			if (isWand(inventory.getItem(event.getNewSlot()))) {
				if (spells.storeInventory(event.getNewSlot(), inventory.getItem(event.getNewSlot()))) {
					updateWandInventory(player, event.getNewSlot(), inventory.getItem(event.getNewSlot()));
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

	@EventHandler
	public void onPlayerDeath(Player player, EntityDeathEvent event)
	{
		PlayerSpells spells = getPlayerSpells(player);
		String rule = player.getWorld().getGameRuleValue("keepInventory");
		if (spells.hasStoredInventory() && !rule.equals("true")) {
			List<ItemStack> drops = event.getDrops();
			drops.clear();

			// Drop the held wand, since that's not in the stored inventory
			ItemStack wand = player.getInventory().getItemInHand();
			if (isWand(wand)) {
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
				if (isWandActive(player))
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
				if (isWandActive(player)) {
					// Save inventory
					PlayerSpells spells = getPlayerSpells(player);
					if (spells.storeInventory()) {
						// Create spell inventory
						updateWandInventory(player);
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

	@EventHandler
	public void onPluginDisable(PluginDisableEvent event)
	{
		for (PlayerSpells spells : playerSpells.values()) {
			spells.restoreInventory();
		}
	}

	@EventHandler
	public void onPluginEnable(PluginEnableEvent event)
	{

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
		PlayerSpells spells = getPlayerSpells((Player)event.getPlayer());
		if (spells.hasStoredInventory()) {
			spells.restoreInventory();
		}
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if (!(event.getPlayer() instanceof Player)) return;
		if (event.getView().getType() == InventoryType.CRAFTING) return;
		Player player = (Player)event.getPlayer();
		PlayerSpells spells = getPlayerSpells(player);
		if (!spells.hasStoredInventory() && isWandActive(player)) {
			if (spells.storeInventory()) {
				updateWandInventory(player);
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
			if (isWand(pickup) && (inHand == null || inHand.getType() == Material.AIR)) {
				event.setCancelled(true);
				event.getItem().remove();
				inventory.setItem(inventory.getHeldItemSlot(), pickup);
				if (spells.storeInventory()) {
					// Create spell inventory
					updateWandInventory(event.getPlayer());
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
			if (isWand(event.getItemDrop().getItemStack()) && (inHand == null || inHand.getType() == Material.AIR)) {
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

	/*
	 * Private data
	 */
	 private final String                        spellsFileName                 = "spells.yml";
	 private final String                        propertiesFileName             = "magic.yml";

	 static final String                         DEFAULT_BUILDING_MATERIALS     = "0,1,2,3,4,5,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,33,34,35,41,42,43,45,46,47,48,49,52,53,55,56,57,58,60,61,62,65,66,67,73,74,79,80,81,82,83,84,85,86,87,88,89,90,91,92,93,94,95,96,97,98,99,100,101,102,103,104,105,106,107,108,109";
	 static final String                         STICKY_MATERIALS               = "37,38,39,50,51,55,59,63,64,65,66,68,70,71,72,75,76,77,78,83";
	 static final String                         STICKY_MATERIALS_DOUBLE_HEIGHT = "64,71,";

	 private List<Material>                      buildingMaterials              = new ArrayList<Material>();
	 private List<Material>                      stickyMaterials                = new ArrayList<Material>();
	 private List<Material>                      stickyMaterialsDoubleHeight    = new ArrayList<Material>();

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
