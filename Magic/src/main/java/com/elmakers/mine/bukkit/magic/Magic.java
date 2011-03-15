package com.elmakers.mine.bukkit.magic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import com.elmakers.mine.bukkit.magic.dao.SpellVariant;
import com.elmakers.mine.bukkit.persistence.Persistence;
import com.elmakers.mine.bukkit.persistence.dao.BlockList;
import com.elmakers.mine.bukkit.utilities.PluginUtilities;
import com.elmakers.mine.bukkit.utilities.UndoQueue;

public class Magic
{
	/*
	 * Public API - Use for hooking up a plugin, or calling a spell
	 */
		
	public SpellVariant getSpell(String name, Player player)
	{
		SpellVariant spell = spellVariants.get(name);
		if (spell != null && !spell.hasSpellPermission(player)) return null;
		return spell;
	}
	
	public PlayerSpells getPlayerSpells(Player player)
	{
		PlayerSpells spells = playerSpells.get(player.getName());
		if (spells == null)
		{
			spells = new PlayerSpells();
			playerSpells.put(player.getName(), spells);
		}
		return spells;
	}
	
	public boolean castSpell(SpellVariant spell, Player player)
	{
		return castSpell(spell, new String[0], player);
	}
	
	public boolean castSpell(SpellVariant spell, String[] parameters, Player player)
	{
		return spell.cast(parameters, player);
	}
	
	public void addSpell(Spell spell)
	{
		List<SpellVariant> variants = spell.getVariants();
		for (SpellVariant variant : variants)
		{
			SpellVariant conflict = spellVariants.get(variant.getName());
			if (conflict != null)
			{
				log.log(Level.WARNING, "Duplicate spell name: '" + conflict.getName() + "'");
			}
			else
			{
				spellVariants.put(variant.getName(), variant);
			}
		}
		
		spells.add(spell);
		spell.initialize(this, utilities, persistence);
	}
	
	/*
	 * Material use system
	 */
	
	public List<Material> getBuildingMaterials()
	{
		return buildingMaterials;
	}
	
	public void setCurrentMaterialType(Player player, Material material, byte data)
	{
		PlayerSpells spells = getPlayerSpells(player);
		if 
		(
				spells.isUsingMaterial() 
		&& 		(spells.getMaterial() != material || spells.getData() != data)
		)
		{
			spells.setMaterial(material);
			spells.setData(data);
			player.sendMessage("Now using " + material.name().toLowerCase());
			// Must allow listeners to remove themselves during the event!
			List<Spell> active = new ArrayList<Spell>();
			active.addAll(materialListeners);
			for (Spell listener : active)
			{
				listener.onMaterialChoose(player);
			}
		}

	}
	
	public void startMaterialUse(Player player, Material material, byte data)
	{
		PlayerSpells spells = getPlayerSpells(player);
		spells.startMaterialUse(material, data);
	}
	
	public Material finishMaterialUse(Player player)
	{
		PlayerSpells spells = getPlayerSpells(player);
		return spells.finishMaterialUse();
	}
	
	public byte getMaterialData(Player player)
	{
		PlayerSpells spells = getPlayerSpells(player);
		return spells.getData();
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
		
		/* TODO: Get this working again!
		if (autoExpandUndo)
		{
			BlockList expandedBlocks = new BlockList(blocks);
			for (UndoableBlock undoBlock : blocks.getBlocks())
			{
				Block block = undoBlock.getBlock();
				Material newType = block.getType();
				if (newType == undoBlock.getOriginalMaterial() || isSolid(newType))
				{
					continue;
				}
				
				for (int side = 0; side < 4; side++)
				{
					BlockFace sideFace = UndoableBlock.SIDES[side];
					Block sideBlock = block.getFace(sideFace);
					if (blocks.contains(sideBlock)) continue;
					
					if (isSticky(undoBlock.getOriginalSideMaterial(side)))
					{
						UndoableBlock stickyBlock = expandedBlocks.addBlock(sideBlock);
						stickyBlock.setFromSide(undoBlock, side);
					}
				}
				
				Material topMaterial = undoBlock.getOriginalTopMaterial();
				Block topBlock = block.getFace(BlockFace.UP);
				if (!blocks.contains(topBlock))
				{  
					if (isAffectedByGravity(topMaterial))
					{
						expandedBlocks.addBlock(topBlock);
						if (autoPreventCaveIn)
						{
							topBlock.setType(gravityFillMaterial);
						}
						else
						{
							for (int dy = 0; dy < undoCaveInHeight; dy++)
							{
								topBlock = topBlock.getFace(BlockFace.UP);
								if (isAffectedByGravity(topBlock.getType()))
								{
									expandedBlocks.addBlock(topBlock);
								}
								else
								{
									break;
								}
							}
						}
					}
					else
					if (isStickyAndTall(topMaterial))
					{
						UndoableBlock stickyBlock = expandedBlocks.addBlock(topBlock);
						stickyBlock.setFromBottom(undoBlock);
						stickyBlock = expandedBlocks.addBlock(topBlock.getFace(BlockFace.UP));
						stickyBlock.setFromBottom(undoBlock);
					}
					else
					if (isSticky(topMaterial))
					{
						UndoableBlock stickyBlock = expandedBlocks.addBlock(topBlock);
						stickyBlock.setFromBottom(undoBlock);
					}
				}
			}
			blocks = expandedBlocks;
		}
		*/
		queue.add(blocks);
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
	
	public void cleanup()
	{
		synchronized(cleanupLock)
		{
			if (cleanupBlocks.size() == 0) return;
			
			List<BlockList> tempList = new ArrayList<BlockList>();
			tempList.addAll(cleanupBlocks);
			long timePassed = System.currentTimeMillis() - lastCleanupTime;
			for (BlockList blocks : tempList)
			{
				boolean undoSuccess = false;
				if (blocks.age((int)timePassed))
				{
					undoSuccess = blocks.undo();
				}
				if (undoSuccess && blocks.isExpired())
				{
					cleanupBlocks.remove(blocks);
				}
			}
			lastCleanupTime = System.currentTimeMillis();
		}
	}
	
	public void forceCleanup()
	{
		for (BlockList blocks : cleanupBlocks)
		{
			blocks.undo();
		}
		cleanupBlocks.clear();
	}
	
	public void scheduleCleanup(BlockList blocks)
	{
		synchronized(cleanupLock)
		{
			if (cleanupBlocks.size() == 0)
			{
				lastCleanupTime = System.currentTimeMillis();
			}
			cleanupBlocks.add(blocks);
		}
	}
	
	/*
	 * Event registration- call to listen for events
	 */
	
	public void registerEvent(SpellEventType type, Spell spell)
	{
		switch (type)
		{
			case PLAYER_MOVE:
				if (!movementListeners.contains(spell)) movementListeners.add(spell);
				break;
			case MATERIAL_CHANGE:
				if (!materialListeners.contains(spell)) materialListeners.add(spell);
				break;
			case PLAYER_QUIT:
				if (!quitListeners.contains(spell)) quitListeners.add(spell);
				break;
			case PLAYER_DEATH:
				if (!deathListeners.contains(spell)) deathListeners.add(spell);
				break;
		}
	}
	
	public void unregisterEvent(SpellEventType type, Spell spell)
	{
		switch (type)
		{
			case PLAYER_MOVE:
				movementListeners.remove(spell);
				break;
			case MATERIAL_CHANGE:
				materialListeners.remove(spell);
				break;
			case PLAYER_QUIT:
				quitListeners.remove(spell);
				break;
			case PLAYER_DEATH:
				deathListeners.remove(spell);
				break;
		}
	}

	/*
	 * Random utility functions
	 */
	
	public void cancel(Player player)
	{
		for (Spell spell : spells)
		{
			spell.cancel(this, player);
		}
	}
	
	public boolean allowCommandUse()
	{
		return allowCommands;
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
		// DOORS are on this list, it's a bit of a hack, but if you consider them
		// as two separate blocks, the top one of which "breaks" when the bottom one does,
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
	
	/*
	 * Internal functions - don't call these, or really anything below here.
	 */
	
	
	/*
	 * Saving and loading
	 */
	
	public void initialize(Server server, Persistence persistence, PluginUtilities utilities)
	{
		this.server = server;
		this.persistence = persistence;
		this.utilities = utilities;
		findSpells();
	}

	protected void loadProperties()
	{
		/*
		PluginProperties properties = new PluginProperties(propertiesFile);
		properties.load();
		
		undoQueueDepth = properties.getInteger("spells-general-undo-depth", undoQueueDepth);
		silent = properties.getBoolean("spells-general-silent", silent);
		quiet = properties.getBoolean("spells-general-quiet", quiet);
		autoExpandUndo = properties.getBoolean("spells-general-expand-undo", autoExpandUndo);
		allowCommands = properties.getBoolean("spells-general-allow-commands", allowCommands);
		stickyMaterials = PluginProperties.parseMaterials(STICKY_MATERIALS);
		stickyMaterialsDoubleHeight = PluginProperties.parseMaterials(STICKY_MATERIALS_DOUBLE_HEIGHT);
		autoPreventCaveIn = properties.getBoolean("spells-general-prevent-cavein", autoPreventCaveIn);
		undoCaveInHeight = properties.getInteger("spells-general-undo-cavein-height", undoCaveInHeight);
		
		//buildingMaterials = properties.getMaterials("spells-general-building", DEFAULT_BUILDING_MATERIALS);
		buildingMaterials = PluginProperties.parseMaterials(DEFAULT_BUILDING_MATERIALS);
		
		for (Spell spell : spells)
		{
			spell.onLoad(properties);
		}
		
		properties.save();
		*/
	}
	
	public void clear()
	{
		forceCleanup();
		movementListeners.clear();
		materialListeners.clear();
		quitListeners.clear();
		spells.clear();
		spellVariants.clear();
		spellsByMaterial.clear();
	}
	
	/*
	 * Listeners / callbacks
	 */

	public void onPlayerQuit(PlayerEvent event)
	{
		// Must allow listeners to remove themselves during the event!
		List<Spell> active = new ArrayList<Spell>();
		active.addAll(quitListeners);
		for (Spell listener : active)
		{
			listener.onPlayerQuit(event);
		}
	}
	
	public void onPlayerMove(PlayerMoveEvent event)
	{
		// Used as a refresh timer for now.. :(
		cleanup();
		
		// Must allow listeners to remove themselves during the event!
		List<Spell> active = new ArrayList<Spell>();
		active.addAll(movementListeners);
		for (Spell listener : active)
		{
			listener.onPlayerMove(event);
		}
	}	
	
	public void onPlayerDeath(Player player, EntityDeathEvent event)
	{
		// Must allow listeners to remove themselves during the event!
		/* Disabled for now- multi-world issues
		List<Spell> active = new ArrayList<Spell>();
		active.addAll(deathListeners);
		for (Spell listener : active)
		{
			listener.onPlayerDeath(player, event);
		}
		*/
	}
	  
    public void onPlayerDamage(Player player, EntityDamageEvent event)
    {
    	if (isInvincible(player))
    	{
    		event.setCancelled(true);
    	}
    }

    public List<SpellVariant> getAllSpells()
    {
    	List<SpellVariant> spells = new ArrayList<SpellVariant>();
    	spells.addAll(spellVariants.values());
    	return spells;
    }
    
    /*
	public void setNether(NetherManager nether)
	{
		this.nether = nether;
	}
	*/
	
	protected void findSpells()
	{
		/*
		addSpell(new HealSpell());
		addSpell(new BlinkSpell());
		addSpell(new TorchSpell());
		addSpell(new FireballSpell());
		addSpell(new PillarSpell());
		addSpell(new BridgeSpell());
		addSpell(new AbsorbSpell());
		addSpell(new FillSpell());
		addSpell(new CushionSpell());
		addSpell(new UndoSpell());
		addSpell(new AlterSpell());
		addSpell(new BlastSpell());
		addSpell(new MineSpell());
		addSpell(new TreeSpell());
		addSpell(new ArrowSpell());
		addSpell(new FrostSpell());
		addSpell(new GillsSpell());
		addSpell(new FamiliarSpell());
		addSpell(new ConstructSpell());
		addSpell(new TransmuteSpell());
		addSpell(new RecallSpell());
		addSpell(new DisintegrateSpell());
		addSpell(new ManifestSpell());
		addSpell(new PeekSpell());
		addSpell(new FireSpell());
		addSpell(new LavaSpell());
		addSpell(new InvincibleSpell());
		addSpell(new TunnelSpell());
		
		// wip
		// addSpell(new TowerSpell());
		// addSpell(new ExtendSpell());
		// addSpell(new StairsSpell());
		
		// NetherGate spells
		if (nether != null)
		{
			addSpell(new PortalSpell(nether));
			addSpell(new PhaseSpell(nether));
			addSpell(new WindowSpell(nether));
		}
		*/
	}
	
	public boolean isInvincible(Player player)
	{
		Boolean isInvincible = invinciblePlayers.get(player.getName());
		if (isInvincible == null)
		{
			return false;
		}
		
		return isInvincible;
	}
	
	public void setInvincible(Player player, boolean invincible)
	{
		invinciblePlayers.put(player.getName(), invincible);
	}
	
	/*
	 * Private data
	 */
	
	static final String		DEFAULT_BUILDING_MATERIALS	= "1,2,3,4,5,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,24,25,35,41,42,43,45,46,47,48,49,56,57,60,65,66,73,74,79,80,81,82,83,85,86,87,88,89,90,91";
	static final String		STICKY_MATERIALS = "37,38,39,50,51,55,59,63,64,65,66,68,70,71,72,75,76,77,78,83";
	static final String		STICKY_MATERIALS_DOUBLE_HEIGHT = "64,71,";
	
	private List<Material>	buildingMaterials	= new ArrayList<Material>();
	private List<Material>	stickyMaterials		= new ArrayList<Material>();
	private List<Material>	stickyMaterialsDoubleHeight		= new ArrayList<Material>();
	//private Material gravityFillMaterial = Material.DIRT;
	
	private final List<BlockList> cleanupBlocks = new ArrayList<BlockList>();
	private final Object cleanupLock = new Object();
	private long lastCleanupTime = 0;
	
	private int undoQueueDepth = 256;
	private boolean silent = false;
	private boolean quiet = true;
	private boolean allowCommands = true;
	/*
	private boolean	autoExpandUndo = true;
	private boolean autoPreventCaveIn = false;
	private int undoCaveInHeight = 32;
	*/
	private HashMap<String, UndoQueue> playerUndoQueues =  new HashMap<String, UndoQueue>();
	
	private final Logger log = Logger.getLogger("Minecraft");
	private final HashMap<String, SpellVariant> spellVariants = new HashMap<String, SpellVariant>();
	private final HashMap<Material, SpellVariant> spellsByMaterial = new HashMap<Material, SpellVariant>();
	private final List<Spell> spells = new ArrayList<Spell>();
	private final HashMap<String, PlayerSpells> playerSpells = new HashMap<String, PlayerSpells>();
	private final List<Spell> movementListeners = new ArrayList<Spell>();
	private final List<Spell> materialListeners = new ArrayList<Spell>();
	private final List<Spell> quitListeners = new ArrayList<Spell>();
	private final List<Spell> deathListeners = new ArrayList<Spell>();
	private final HashMap<String, Boolean> invinciblePlayers = new HashMap<String, Boolean>();

	//private NetherManager nether = null;
	
	protected Persistence persistence = null;
	protected PluginUtilities utilities = null;
	protected Server server = null;
}
