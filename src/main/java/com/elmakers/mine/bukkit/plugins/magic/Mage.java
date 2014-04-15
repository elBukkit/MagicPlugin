package com.elmakers.mine.bukkit.plugins.magic;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.spell.CostReducer;
import com.elmakers.mine.bukkit.block.BlockBatch;
import com.elmakers.mine.bukkit.block.BlockList;
import com.elmakers.mine.bukkit.block.MaterialBrush;
import com.elmakers.mine.bukkit.block.UndoBatch;
import com.elmakers.mine.bukkit.block.UndoQueue;
import com.elmakers.mine.bukkit.plugins.magic.wand.LostWand;
import com.elmakers.mine.bukkit.plugins.magic.wand.Wand;
import com.elmakers.mine.bukkit.utilities.InventoryUtils;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class Mage implements CostReducer, com.elmakers.mine.bukkit.api.magic.Mage
{
	protected static int 						AUTOMATA_ONLINE_TIMEOUT = 5000;
	
	protected final String						id;
	protected WeakReference<Player> 			player;
	protected WeakReference<CommandSender>		commandSender;
	protected String 							playerName;
	protected final MagicController				controller;
	protected HashMap<String, Spell> 			spells 						  = new HashMap<String, Spell>();
	private Inventory							storedInventory  			   = null;
	private Wand								activeWand					   = null;
	private final List<Spell>                   quitListeners                  = new ArrayList<Spell>();
	private final List<Spell>                   deathListeners                 = new ArrayList<Spell>();
	private final List<Spell>                   damageListeners                = new ArrayList<Spell>();
	private final Set<Spell>					activeSpells				   = new HashSet<Spell>();
	private UndoQueue          					undoQueue               	   = null;
	private LinkedList<BlockBatch>				pendingBatches					= new LinkedList<BlockBatch>();
	
	private Location			location;
	private float 				costReduction = 0;
	private float 				cooldownReduction = 0;
	private float 				powerMultiplier = 1;
	private long 				lastClick = 0;
	private long 				lastCast = 0;
	private long 				blockPlaceTimeout = 0;
	private Location 			lastDeathLocation = null;
	private final MaterialBrush		brush;
	
	private boolean isNewPlayer = true;
	
	public Mage(String id, MagicController controller) {
		this.id = id;
		this.controller = controller;
		this.brush = new MaterialBrush(this, Material.DIRT, (byte)0);
		player = new WeakReference<Player>(null);
		commandSender = new WeakReference<CommandSender>(null);
	}
	
	private static String defaultMageName = "Automaton";
	
	protected void processResult(SpellResult result) {
		lastCast = System.currentTimeMillis();
	}
	
	public void removeExperience(int xp) {
		
		if (activeWand != null && activeWand.hasExperience()) {
			activeWand.removeExperience(xp);
			return;
		}
		
		Player player = getPlayer();
		if (player == null) return;
		
		float expProgress = player.getExp();
		int expLevel = player.getLevel();
		while ((expProgress > 0 || expLevel > 0) && xp > 0) {
			if (expProgress > 0) {
				int expAtLevel = (int)(expProgress * (player.getExpToLevel()));
				if (expAtLevel > xp) {
					expAtLevel -= xp;
					xp = 0;
					expProgress = (float)expAtLevel / (float)getExpToLevel(expLevel);
				} else {
					expProgress = 0;
					xp -= expAtLevel;
				}
			} else {
				xp -= player.getExpToLevel();
				expLevel--;
				if (xp < 0) {
					expProgress = (float)(-xp) / getExpToLevel(expLevel);
					xp = 0;
				}
			}
		}
		
		player.setExp(expProgress);
		player.setLevel(expLevel);
	}
	
	// Taken from NMS Player
    public static int getExpToLevel(int expLevel) {
        return expLevel >= 30 ? 62 + (expLevel - 30) * 7 : (expLevel >= 15 ? 17 + (expLevel - 15) * 3 : 17);
    }
	
	public int getExperience() {
		if (activeWand != null && activeWand.hasExperience()) {
			return activeWand.getExperience();
		}
		
		Player player = getPlayer();
		if (player == null) return 0;
		
		int xp = 0;
		float expProgress = player.getExp();
		int expLevel = player.getLevel();
		for (int level = 0; level < expLevel; level++) {
			xp += getExpToLevel(level);
		}
		return xp + (int)(expProgress * getExpToLevel(expLevel));
	}
	
	public void setCostReduction(float reduction) {
		costReduction = reduction;
	}
	
	public boolean hasStoredInventory() {
		return storedInventory != null;
	}

	public Inventory getStoredInventory() {
		return storedInventory;
	}
	
	public void setLocation(Location location) {
		this.location = location;
	}
	
	public void setLocation(Location location, boolean direction) {
		if (!direction) {
			if (this.location == null) {
				this.location = location;
			} else {
				this.location.setX(location.getX());
				this.location.setY(location.getY());
				this.location.setZ(location.getZ());
			}
		} else {
			this.location = location;
		}
	}
	
	public void clearCache() {
		if (brush != null) {
			brush.clearSchematic();
		}
	}
	
	public float getCostReduction() {
		return activeWand == null ? costReduction + controller.getCostReduction() : activeWand.getCostReduction() + costReduction;
	}
	
	public float getCooldownReduction() {
		return activeWand == null ? cooldownReduction + controller.getCooldownReduction() : activeWand.getCooldownReduction() + cooldownReduction;
	}
	
	public void setCooldownReduction(float reduction) {
		cooldownReduction = reduction;
	}
	
	public void setPowerMultiplier(float mutliplier) {
		powerMultiplier = mutliplier;
	}
	
	public boolean usesMana() {
		return activeWand == null ? false : activeWand.usesMana();
	}
	
	protected float getWandPower() {
		float power = Math.min(controller.getMaxPower(), activeWand == null ? 0 : activeWand.getPower());
		return power * powerMultiplier;
	}
	
	public float getDamageMultiplier() {
		float maxPowerMultiplier = controller.getMaxDamagePowerMultiplier() - 1;
		return 1 + (maxPowerMultiplier * getWandPower());
	}
	
	public float getRangeMultiplier() {
		if (activeWand == null) return 1;
		
		float maxPowerMultiplier = controller.getMaxRangePowerMultiplier() - 1;
		float maxPowerMultiplierMax = controller.getMaxRangePowerMultiplierMax();
		float multiplier = 1 + (maxPowerMultiplier * getWandPower());
		return Math.min(multiplier, maxPowerMultiplierMax);
	}
	
	public float getConstructionMultiplier() {
		float maxPowerMultiplier = controller.getMaxConstructionPowerMultiplier() - 1;
		return 1 + (maxPowerMultiplier * getWandPower());
	}
	
	public float getRadiusMultiplier() {
		if (activeWand == null) return 1;
		
		float maxPowerMultiplier = controller.getMaxRadiusPowerMultiplier() - 1;
		float maxPowerMultiplierMax = controller.getMaxRadiusPowerMultiplierMax();
		float multiplier = 1 + (maxPowerMultiplier * getWandPower());
		return Math.min(multiplier, maxPowerMultiplierMax);
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
			return false;
		}

		Player player = getPlayer();
		if (player == null) {
			return false;
		}
		Inventory inventory = player.getInventory();
		storedInventory = InventoryUtils.createInventory(null, inventory.getSize(), "Stored Inventory");
		
		// Make sure we don't store any spells or magical materials, just in case
		ItemStack[] contents = inventory.getContents();
		for (int i = 0; i < contents.length; i++) {
			if (Wand.isSpell(contents[i])) {
				contents[i] = null;
			}
		}
		storedInventory.setContents(contents);
		inventory.clear();

		return true;
	}

	public boolean restoreInventory() {
		if (storedInventory == null) {
			return false;
		}
		Player player = getPlayer();
		if (player == null) {
			return false;
		}
		Inventory inventory = player.getInventory();
		inventory.setContents(storedInventory.getContents());
		storedInventory = null;

		return true;
	}

	public void registerEvent(SpellEventType type, Spell spell)
	{
		switch (type)
		{
		case PLAYER_QUIT:
			if (!quitListeners.contains(spell))
				quitListeners.add(spell);
			break;
		case PLAYER_DAMAGE:
			if (!damageListeners.contains(spell))
				damageListeners.add(spell);
			break;
		case PLAYER_DEATH:
			if (!deathListeners.contains(spell))
				deathListeners.add(spell);
			break;
		}
	}

	public void unregisterEvent(SpellEventType type, Spell spell)
	{
		switch (type)
		{
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
	
	public Player getPlayer()
	{
		return player.get();
	}
	
	public CommandSender getCommandSender()
	{
		return commandSender.get();
	}

	public boolean cancel()
	{
		boolean result = false;
		for (Spell spell : spells.values())
		{
			result = result || spell.cancel();
		}
		return result;
	}

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

	public void onPlayerDeath(EntityDeathEvent event)
	{
		Player player = getPlayer();
		if (player == null) {
			return;
		}
		lastDeathLocation = player.getLocation();
		List<Spell> active = new ArrayList<Spell>();
		active.addAll(deathListeners);
		for (Spell listener : active)
		{
			if (player == listener.getPlayer())
			{
				listener.onPlayerDeath(event);
			}
		}
	}
	
	public void onPlayerCombust(EntityCombustEvent event)
	{
		if (activeWand != null && activeWand.getDamageReductionFire() > 0)
		{
			event.getEntity().setFireTicks(0);
			event.setCancelled(true);
		}
	}
	
	public boolean isSuperProtected()
	{
		if (controller.hasPermission(getPlayer(), "Magic.protected")) return true;
		return activeWand != null && activeWand.isSuperProtected();
	}
	
	public boolean isSuperPowered()
	{
		if (controller.hasPermission(getPlayer(), "Magic.powered")) return true;		
		return activeWand != null && activeWand.isSuperPowered();
	}
	
	public void onPlayerDamage(EntityDamageEvent event)
	{
		Player player = getPlayer();
		if (player == null) {
			return;
		}
		
		// Send on to any registered spells
		List<Spell> active = new ArrayList<Spell>();
		active.addAll(damageListeners);
		for (Spell listener : active)
		{
			listener.onPlayerDamage(event);
		}
		
		if (isSuperProtected()) {
			event.setCancelled(true);
			if (player.getFireTicks() > 0) {
				player.setFireTicks(0);
			}
			return;
		}
		
		if (event.isCancelled()) return;
		
		// First check for damage reduction
		float reduction = 0;
		if (activeWand != null) {
			reduction = activeWand.getDamageReduction();
			float damageReductionFire = activeWand.getDamageReductionFire();
			switch (event.getCause()) {
				case CONTACT:
				case ENTITY_ATTACK:
					reduction += activeWand.getDamageReductionPhysical();
					break;
				case PROJECTILE:
					reduction += activeWand.getDamageReductionProjectiles();
					break;
				case FALL:
					reduction += activeWand.getDamageReductionFalling();
					break;
				case FIRE:
				case FIRE_TICK:
				case LAVA:
					// Also put out fire if they have fire protection of any kind.
					if (damageReductionFire > 0 && player.getFireTicks() > 0) {
						player.setFireTicks(0);
					}
					reduction += damageReductionFire;
					break;
				case BLOCK_EXPLOSION:
				case ENTITY_EXPLOSION:
					reduction += activeWand.getDamageReductionExplosions();
				default:
					break;
			}
		}
		
		if (reduction > 1) {
			event.setCancelled(true);
			return;
		}
		
		if (reduction > 0) {
			double newDamage = (1.0f - reduction) * event.getDamage();
			if (newDamage <= 0) newDamage = 0.1;
			event.setDamage(newDamage);
		}
	}
	
	public MagicController getController() {
		return controller;
	}
	
	public Inventory getInventory() {
		Player player = getPlayer();
		return hasStoredInventory() ? getStoredInventory() : (player == null ? null : player.getInventory());
	}
	
	public Wand getActiveWand() {
		return activeWand;
	}
	
	public void setActiveWand(Wand activeWand) {
		this.activeWand = activeWand;
		blockPlaceTimeout = System.currentTimeMillis() + 200;
	}
	
	public long getBlockPlaceTimeout() {
		return blockPlaceTimeout;
	}
	
	public MaterialBrush getBrush() {
		return brush;
	}

	/**
	 * Send a message to a player when a spell is cast.
	 * 
	 * @param player The player to send a message to 
	 * @param message The message to send
	 */
	public void castMessage(String message)
	{
		if (message == null || message.length() == 0) return;
		
		// First check wand
		if (activeWand != null && !activeWand.showCastMessages()) return;
		
		CommandSender sender = getCommandSender();
		if (sender != null && controller.showCastMessages() && controller.showMessages())
		{
			sender.sendMessage(controller.getCastMessagePrefix() + message);
		}
	}

	/**
	 * Send a message to a player. 
	 * 
	 * Use this to send messages to the player that are important.
	 * 
	 * @param player The player to send the message to
	 * @param message The message to send
	 */
	public void sendMessage(String message)
	{
		if (message == null || message.length() == 0) return;
		
		// First check wand
		if (activeWand != null && !activeWand.showMessages()) return;
				
		CommandSender sender = getCommandSender();
		if (sender != null && controller.showMessages())
		{
			sender.sendMessage(controller.getMessagePrefix() + message);
		}
	}
	
	public void clearBuildingMaterial() {
		brush.setMaterial(controller.getDefaultMaterial(), (byte)1);
	}
	
	public boolean hasBuildPermission(Block block) {
		return controller.hasBuildPermission(getPlayer(), block);
	}
	
	public boolean isIndestructible(Block block) {
		return controller.isIndestructible(block);
	}
	
	public boolean isDestructible(Block block) {
		return controller.isDestructible(block);
	}
	
	public void playSound(Sound sound, float volume, float pitch) {
		Player player = getPlayer();
		if (player != null && controller.soundsEnabled()) {
			player.playSound(player.getLocation(), sound, volume, pitch);
		}
	}
	
	public UndoQueue getUndoQueue() {
		if (undoQueue == null) {
			undoQueue = new UndoQueue();
			undoQueue.setMaxSize(controller.getUndoQueueDepth());
		}
		return undoQueue;
	}
	
	public void registerForUndo(BlockList blockList) {
		UndoQueue queue = getUndoQueue();
		int autoUndo = controller.getAutoUndoInterval();
		if (autoUndo > 0 && blockList.getTimeToLive() == 0) {
			blockList.setTimeToLive(autoUndo);
		}
		if (blockList.getTimeToLive() > 0) {
			queue.scheduleCleanup(this, blockList);
		} else {
			queue.add(blockList);
		}
	}
	
	public Color getEffectColor() {
		if (activeWand == null) return null;
		return Color.fromRGB(activeWand.getEffectColor());
	}
	
	public FireworkEffect getFireworkEffect() {
		return getFireworkEffect(null, null, null, null, null);
	}
	
	public FireworkEffect getFireworkEffect(Color color1, Color color2, org.bukkit.FireworkEffect.Type fireworkType) {
			return getFireworkEffect(color1, color2, fireworkType, null, null);
	}

	public FireworkEffect getFireworkEffect(Color color1, Color color2, org.bukkit.FireworkEffect.Type fireworkType, Boolean flicker, Boolean trail) {
		Color wandColor = getEffectColor();
		Random rand = new Random();
		if (wandColor != null) {
			color1 = wandColor;
			color2 = wandColor.mixColors(color1, Color.WHITE);
		} else {
			if (color1 == null) {
				color1 = Color.fromRGB(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
			}
			if (color2 == null) {
				color2 = Color.fromRGB(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
			}
		}
		if (fireworkType == null) {
			fireworkType = org.bukkit.FireworkEffect.Type.values()[rand.nextInt(org.bukkit.FireworkEffect.Type.values().length)];
		}
		if (flicker == null) {
			flicker = rand.nextBoolean();
		}
		if (trail == null) {
			trail = rand.nextBoolean();
		}
		
		return FireworkEffect.builder().flicker(flicker).withColor(color1).withFade(color2).with(fireworkType).trail(trail).build();
	}
	
	public Location getLastDeathLocation()
	{
		return lastDeathLocation;
	}

	protected void setPlayer(Player player)
	{
		if (player != null) {
			playerName = player.getName();
			this.player = new WeakReference<Player>(player);
			this.commandSender = new WeakReference<CommandSender>(player);
		} else {
			this.player.clear();
			this.commandSender.clear();
		}
	}
	
	protected void setCommandSender(CommandSender sender)
	{
		if (sender != null) {
			this.commandSender = new WeakReference<CommandSender>(sender);

			if (sender instanceof BlockCommandSender) {
				BlockCommandSender commandBlock = (BlockCommandSender)sender;
				playerName = commandBlock.getName();
				Location location = getLocation();
				if (location == null) {
					location = commandBlock.getBlock().getLocation();
				} else {
					Location blockLocation = commandBlock.getBlock().getLocation();
					location.setX(blockLocation.getX());
					location.setY(blockLocation.getY());
					location.setZ(blockLocation.getZ());
				}
				setLocation(location, false);
			} else {
				setLocation(null);
			}
		} else {
			this.commandSender.clear();
			setLocation(null);
		}
	}
	
	protected void load(ConfigurationNode configNode)
	{
		try {
			if (configNode == null) return;

			isNewPlayer = false;
			playerName = configNode.getString("name", playerName);
			lastDeathLocation = configNode.getLocation("last_death_location");
			location = configNode.getLocation("location");
			lastCast = configNode.getLong("last_cast", lastCast);
			
			getUndoQueue().load(this, configNode);
			ConfigurationNode spellNode = configNode.getNode("spells");
			if (spellNode != null) {
				List<String> keys = spellNode.getKeys();
				for (String key : keys) {
					Spell spell = getSpell(key, getPlayer());
					if (spell != null) {
						spell.load(spellNode.getNode(key));
					}
				}
			}
			
			if (configNode.containsKey("brush")) {
				brush.load(configNode.getNode("brush"));
			}
		} catch (Exception ex) {
			controller.getPlugin().getLogger().warning("Failed to load player data for " + playerName + ": " + ex.getMessage());
		}		
	}
	
	protected void save(ConfigurationNode configNode)
	{
		try {
			configNode.setProperty("name", playerName);
			configNode.setProperty("last_cast", lastCast);
			configNode.setProperty("last_death_location", lastDeathLocation);
			if (location != null) {
				configNode.setProperty("location", location);
			}
			
			ConfigurationNode brushNode = configNode.createChild("brush");
			brush.save(brushNode);
			
			getUndoQueue().save(this, configNode);
			ConfigurationNode spellNode = configNode.createChild("spells");
			for (Spell spell : spells.values()) {
				spell.save(spellNode.createChild(spell.getKey()));
			}
		} catch (Exception ex) {
			controller.getPlugin().getLogger().warning("Failed to save player data for " + playerName + ": " + ex.getMessage());
		}	
	}
	
	protected boolean checkLastClick(long maxInterval)
	{
		long now = System.currentTimeMillis();
		long previous = lastClick;
		lastClick = now;
		return (previous <= 0 || previous + maxInterval < now);
	}
	
	protected void activateSpell(Spell spell) {
		activeSpells.add(spell);
	}
	
	protected void deactivateSpell(Spell spell) {
		activeSpells.remove(spell);
	}
	
	public void deactivateAllSpells() {
		// Copy this set since spells will get removed while iterating!
		List<Spell> active = new ArrayList<Spell>(activeSpells);
		for (Spell spell : active) {
			spell.deactivate();
		}
	}
	
	// This gets called every second (or so - 20 ticks)
	protected void tick() {
		// TODO: Won't need this online check once we're cleaning up on logout, I think.
		// Also this theoretically should never happen since we deactive wands on logout. Shrug.
		Player player = getPlayer();
		if (activeWand != null && player != null && player.isOnline()) {
			activeWand.tick();
		}
		
		// Copy this set since spells may get removed while iterating!
		List<Spell> active = new ArrayList<Spell>(activeSpells);
		for (Spell spell : active) {
			spell.checkActiveDuration();
			spell.checkActiveCosts();
		}
	}
	
	public boolean addPendingBlockBatch(BlockBatch batch) {
		if (pendingBatches.size() >= controller.getPendingQueueDepth()) {
			return false;
		}
		pendingBatches.addLast(batch);
		controller.addPending(this);
		return true;
	}
	
	public void processPendingBatches(int maxBlockUpdates) {
		if (pendingBatches.size() > 0) {
			int updated = 0;
			List<BlockBatch> processBatches = new ArrayList<BlockBatch>(pendingBatches);
			pendingBatches.clear();
			for (BlockBatch batch : processBatches) {
				int batchUpdated = batch.process(maxBlockUpdates - updated);
				updated += batchUpdated;
				if (!batch.isFinished()) {
					pendingBatches.add(batch);
				}
				
				if (updated >= maxBlockUpdates) break;
			}
		}
		
		if (pendingBatches.size() == 0) {
			controller.removePending(this);
		}
	}
	
	public List<LostWand> getLostWands() {
		Collection<LostWand> allWands = controller.getLostWands();
		List<LostWand> mageWands = new ArrayList<LostWand>();
		for (LostWand lostWand : allWands) {
			String owner = lostWand.getOwner();
			if (owner != null && owner.equals(playerName)) {
				mageWands.add(lostWand);
			}
		}
		return mageWands;
	}
	
	public void setLastHeldMapId(short mapId) {
		brush.setMapId(mapId);
	}
	
	protected void loadSpells(ConfigurationNode config)
	{
		if (config == null) return;
		
		// TODO: Handle disabled/removed spells?
		for (Spell spell : spells.values()) {
			String key = spell.getKey();
			if (config.containsKey(key)) {
				spell.loadTemplate(config.getNode(key));
			}
		}
	}

	final static private Set<Material> EMPTY_MATERIAL_SET = new HashSet<Material>();
	
	public Set<Material> getRestrictedMaterials()
	{
		if (isSuperPowered()) {
			return EMPTY_MATERIAL_SET;
		}
		return controller.getRestrictedMaterials();
	}

	public boolean isRestricted(Material material) 
	{
		if (isSuperPowered()) {
			return false;
		}
		return controller.isRestricted(material);	
	}
	
	public boolean isPlayer()
	{
		Player player = getPlayer();
		return player != null;
	}
	
	public boolean isDead()
	{
		Player player = getPlayer();
		if (player != null) {
			return player.isDead();
		}
		// Check for automata
		CommandSender sender = getCommandSender();
		if (sender == null || !(sender instanceof BlockCommandSender)) return true;
		BlockCommandSender commandBlock = (BlockCommandSender)sender;
		Block block = commandBlock.getBlock();
		if (!block.getChunk().isLoaded()) return true;
		return (block.getType() != Material.COMMAND);
	}
	
	public boolean isOnline()
	{
		Player player = getPlayer();
		if (player != null) {
			return player.isOnline();
		}
		// Check for automata
		CommandSender sender = getCommandSender();
		if (sender == null || !(sender instanceof BlockCommandSender)) return true;
		return lastCast > System.currentTimeMillis() - AUTOMATA_ONLINE_TIMEOUT;
	}
	
	public boolean hasLocation()
	{
		return getLocation() != null;
	}
	
	public boolean isNewPlayer()
	{
		return this.isNewPlayer;
	}
	
	/*
	 * API Implementation(non-Javadoc)
	 */

	public Collection<com.elmakers.mine.bukkit.block.BlockBatch> getPendingBatches() 
	{
		Collection<com.elmakers.mine.bukkit.block.BlockBatch> pending = new ArrayList<com.elmakers.mine.bukkit.block.BlockBatch>();
		pending.addAll(pendingBatches);
		return pending;
	}
	
	public String getName() {
		return playerName == null || playerName.length() == 0 ? defaultMageName : playerName;
	}
	
	public String getId() {
		return id;
	}

	public Location getLocation() {
		if (location != null) return location.clone();
		
		Player player = getPlayer();
		if (player == null) return null;
		return player.getLocation();
	}
	
	public Location getEyeLocation() {
		Player player = getPlayer();
		if (player != null) return player.getEyeLocation();
		Location location = getLocation();
		if (location != null) {
			location.setY(location.getY() + 1.5);
			return location;
		}
		return null;
	}
	
	public Vector getDirection() {
		Location location = getLocation();
		if (location != null) {
			return location.getDirection();
		}
		return new Vector(0, 1, 0);
	}
	
	public boolean undo(Block target) {
		return getUndoQueue().undo(this, target);
	}
	
	public boolean cancelPending() {
		boolean stoppedPending = false;
		if (pendingBatches.size() > 0) {
			List<BlockBatch> batches = new ArrayList<BlockBatch>();
			batches.addAll(pendingBatches);
			for (BlockBatch batch : batches) {
				if (!(batch instanceof UndoBatch)) {
					batch.finish();
					pendingBatches.remove(batch);
					stoppedPending = true;
				}
			}
		}
		return stoppedPending;
	}
	
	public boolean undo() {
		
		if (cancelPending()) return true;
		return getUndoQueue().undo(this);
	}
	
	public boolean commit() {
		return getUndoQueue().commit();
	}

	public com.elmakers.mine.bukkit.api.spell.Spell getSpell(String name)
	{
		return getSpell(name, getPlayer());
	}
	
	public Spell getSpell(String name, Player usePermissions)
	{
		Spell spell = controller.getSpell(name);
		if (spell == null || !spell.hasSpellPermission(usePermissions))
			return null;

		Spell playerSpell = spells.get(spell.getKey());
		if (playerSpell == null)
		{
			playerSpell = (Spell)spell.clone();
			spells.put(spell.getKey(), playerSpell);
		}
		playerSpell.setMage(this);

		return playerSpell;
	}
	
}
