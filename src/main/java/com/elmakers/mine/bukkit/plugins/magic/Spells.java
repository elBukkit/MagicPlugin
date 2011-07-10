package com.elmakers.mine.bukkit.plugins.magic;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitScheduler;

import com.elmakers.mine.bukkit.dao.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.spells.AbsorbSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.AlterSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.ArrowSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.BlastSpell;
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
import com.elmakers.mine.bukkit.plugins.magic.spells.ManifestSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.MapSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.MineSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.PeekSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.PillarSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.PortalSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.RecallSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.SignSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.StairsSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.TorchSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.TowerSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.TransmuteSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.TreeSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.TunnelSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.UndoSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.WeatherSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.WolfSpell;
import com.elmakers.mine.bukkit.utilities.PluginProperties;
import com.elmakers.mine.bukkit.utilities.UndoQueue;
import com.nijiko.permissions.PermissionHandler;

public class Spells
{
	/*
	 * Public API - Use for hooking up a plugin, or calling a spell
	 */
		
	public SpellVariant getSpell(Material material, Player player)
	{
		SpellVariant spell = spellsByMaterial.get(material);
		if (spell != null && !spell.hasSpellPermission(player)) return null;
		return spell;
	}

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
			Material m = variant.getMaterial();
			if (m != null && m != Material.AIR)
			{
				if (buildingMaterials.contains(m))
				{
					log.warning("Spell " + variant.getName() + " uses building material as icon: " + m.name().toLowerCase());
				}
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
		}
		
		spells.add(spell);
		spell.initialize(this);
	}
	
	/*
	 * Material use system
	 */
	
	public List<Material> getBuildingMaterials()
	{
		return buildingMaterials;
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
	
	public boolean undoAny(Player player, Block target)
	{
	    for (String playerName : playerUndoQueues.keySet())
	    {
	        UndoQueue queue = playerUndoQueues.get(playerName);
	        if (queue.undo(target))
	        {
	            if (!player.getName().equals(playerName))
	            {
	                player.sendMessage("Undid one of " + playerName +"'s spells");
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
		switch (type)
		{
			case PLAYER_MOVE:
				if (!movementListeners.contains(spell)) movementListeners.add(spell);
				break;
			case PLAYER_QUIT:
				if (!quitListeners.contains(spell)) quitListeners.add(spell);
				break;
			case PLAYER_DAMAGE:
				if (!damageListeners.contains(spell)) damageListeners.add(spell);
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
			case PLAYER_DAMAGE:
			    damageListeners.remove(spell);
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
	
	public int getWandTypeId()
	{
		return wandTypeId;
	}
	
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
		addBuiltinSpells();
		load();
	}
	
	public void load()
	{
		loadProperties();
	}

	protected void loadProperties()
	{
	    File dataFolder = plugin.getDataFolder();
	    dataFolder.mkdirs();
	    File pFile = new File(dataFolder, propertiesFile);
		PluginProperties properties = new PluginProperties(pFile.getAbsolutePath());
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
        wandTypeId = properties.getInteger("wand-type-id", wandTypeId);
		
		for (Spell spell : spells)
		{
			spell.onLoad(properties);
		}
		
		properties.save();
	}
	
	public void clear()
	{
		movementListeners.clear();
		damageListeners.clear();
		quitListeners.clear();
		spells.clear();
		spellVariants.clear();
		spellsByMaterial.clear();
	}
	
	/*
	 * Listeners / callbacks
	 */

	public void onPlayerQuit(PlayerQuitEvent event)
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
        List<Spell> active = new ArrayList<Spell>();
        active.addAll(damageListeners);
        for (Spell listener : active)
        {
            listener.onPlayerDamage(player, event);
        }
        
        Float amount = invincibleAmount(player);
    	if (amount != null && amount > 0)
    	{
    	    if (amount >= 1)
    	    {
    	        event.setCancelled(true);
    	    }
    	    else
    	    {
    	        int newDamage = (int)Math.floor((1.0f - amount) * event.getDamage());
    	        if (newDamage == 0) newDamage = 1;
    	        event.setDamage(newDamage);
    	    }
    	}
    }

    public List<SpellVariant> getAllSpells()
    {
    	List<SpellVariant> spells = new ArrayList<SpellVariant>();
    	spells.addAll(spellVariants.values());
    	return spells;
    }

    /**
    * Called when a player plays an animation, such as an arm swing
    * 
    * @param event Relevant event details
    */
   public void onPlayerAnimation(PlayerAnimationEvent event) 
   {
       Player player = event.getPlayer();
       if (event.getAnimationType() == PlayerAnimationType.ARM_SWING)
       {
           if (event.getPlayer().getInventory().getItemInHand().getTypeId() == getWandTypeId())
           {
               if (!hasWandPermission(player))
               {
                   return;
               }
               
               Inventory inventory = player.getInventory();
               ItemStack[] contents = inventory.getContents();
               
               SpellVariant spell = null;
               for (int i = 0; i < 9; i++)
               {
                   if (contents[i].getType() == Material.AIR || contents[i].getTypeId() == getWandTypeId())
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
                   castSpell(spell, player);
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
       
       if (firstMaterialSlot == 8) return false;
       
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
       
       for (int i = 0; i < 9; i++) { active[i] = contents[i]; }
       
       int maxSpellSlot = 0;
       int firstSpellSlot = -1;
       for (int i = 0; i < 9; i++)
       {
           boolean isEmpty = active[i] == null;
           Material activeType = isEmpty ? Material.AIR : active[i].getType();
           boolean isWand = activeType.getId() == getWandTypeId();
           boolean isSpell = false;
           if (activeType != Material.AIR)
           {
               SpellVariant spell = getSpell(activeType, player);
               isSpell = spell != null;
           }
           
           if (isSpell)
           {
               if (firstSpellSlot < 0) firstSpellSlot = i;
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
           Material contentsType = contents[i] == null ? Material.AIR : active[i].getType();
           if (contentsType.getId() != getWandTypeId())
           {
               for (int di = 1; di < numSpellSlots; di++)
               {
                   int dni = (ddi + di) % numSpellSlots;
                   int ni = dni + firstSpellSlot;
                   Material activeType = active[ni]== null ? Material.AIR : active[ni].getType();
                   if (activeType.getId() != getWandTypeId())
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
 
   /**
    * Called when a player uses an item
    * 
    * @param event Relevant event details
    */
   public void onPlayerInteract(PlayerInteractEvent event) 
   {
       if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
       {
           cancel(event.getPlayer());
           
           int materialId = event.getPlayer().getInventory().getItemInHand().getTypeId();
           Player player = event.getPlayer();
   
           if (!hasWandPermission(player))
           {
               return;
           }
           
           boolean cycleSpells = false;
   
           cycleSpells = player.isSneaking();
           if (materialId == getWandTypeId())
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
	
	protected void addBuiltinSpells()
	{
		addSpell(new HealSpell());
		addSpell(new BlinkSpell());
		addSpell(new TorchSpell());
		addSpell(new BoomSpell());
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
		addSpell(new WolfSpell());
        addSpell(new WeatherSpell());
        addSpell(new LightningSpell());
        addSpell(new GotoSpell());
        addSpell(new SignSpell());
        addSpell(new PortalSpell());
        addSpell(new GrenadeSpell());
        addSpell(new FireballSpell());
        addSpell(new FlingSpell());
        addSpell(new ForceSpell());
        addSpell(new MapSpell());
        addSpell(new LevitateSpell());
        addSpell(new InvisibilitySpell());
        
		// wip
		addSpell(new TowerSpell());
		// addSpell(new ExtendSpell());
		addSpell(new StairsSpell());
		
		log.info("Magic: Loaded " + spellVariants.size() + " spells.");
	}
	
	public Float invincibleAmount(Player player)
	{
		return invinciblePlayers.get(player.getName());
	}
	
	public void setInvincible(Player player, float invincible)
	{
	    if (invincible <= 0)
	    {
	        invinciblePlayers.remove(player.getName());
	    }
	    else
	    {
	        invinciblePlayers.put(player.getName(), invincible);
	    }
	}
	
	public boolean allowPhysics(Block block)
	{
	    if (physicsDisableTimeout == 0) return true;
	    if (System.currentTimeMillis() > physicsDisableTimeout) physicsDisableTimeout = 0;
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
	    PermissionHandler permissions = MagicPlugin.getPermissionHandler();
        if (permissions == null)
        {
            return defaultValue;
        }
        
        return permissions.has(player, pNode);
	}
	
    public boolean hasPermission(Player player, String pNode)
    {
        return hasPermission(player, pNode, true);
    }
    
	/*
	 * Private data
	 */
	private final String propertiesFile = "magic.properties";
	private int wandTypeId = 280;
	
	static final String		DEFAULT_BUILDING_MATERIALS	= "0,1,2,3,4,5,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,33,34,35,41,42,43,45,46,47,48,49,52,53,55,56,57,58,60,61,62,65,66,67,73,74,79,80,81,82,83,84,85,86,87,88,89,90,91,92,93,94,95,96";
	static final String		STICKY_MATERIALS = "37,38,39,50,51,55,59,63,64,65,66,68,70,71,72,75,76,77,78,83";
	static final String		STICKY_MATERIALS_DOUBLE_HEIGHT = "64,71,";
	
	private List<Material>	buildingMaterials	= new ArrayList<Material>();
	private List<Material>	stickyMaterials		= new ArrayList<Material>();
	private List<Material>	stickyMaterialsDoubleHeight		= new ArrayList<Material>();
	//private Material gravityFillMaterial = Material.DIRT;
	
	private long physicsDisableTimeout = 0;
	private int undoQueueDepth = 256;
	private boolean silent = false;
	private boolean quiet = true;
	private boolean allowCommands = true;
	private boolean	autoExpandUndo = true;
	private boolean autoPreventCaveIn = false;
	private int undoCaveInHeight = 32;
	private HashMap<String, UndoQueue> playerUndoQueues =  new HashMap<String, UndoQueue>();
	
	private final Logger log = Logger.getLogger("Minecraft");
	private final HashMap<String, SpellVariant> spellVariants = new HashMap<String, SpellVariant>();
	private final HashMap<Material, SpellVariant> spellsByMaterial = new HashMap<Material, SpellVariant>();
	private final List<Spell> spells = new ArrayList<Spell>();
	private final HashMap<String, PlayerSpells> playerSpells = new HashMap<String, PlayerSpells>();
	private final List<Spell> movementListeners = new ArrayList<Spell>();
	private final List<Spell> quitListeners = new ArrayList<Spell>();
	private final List<Spell> deathListeners = new ArrayList<Spell>();
    private final List<Spell> damageListeners = new ArrayList<Spell>();
	private final HashMap<String, Float> invinciblePlayers = new HashMap<String, Float>();

	private MagicPlugin plugin = null;
}
