package com.elmakers.mine.bukkit.magic;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.elmakers.mine.bukkit.effect.HoloUtils;
import com.elmakers.mine.bukkit.effect.Hologram;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.block.BlockBatch;
import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.CostReducer;
import com.elmakers.mine.bukkit.api.spell.MageSpell;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellEventType;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.LostWand;
import com.elmakers.mine.bukkit.block.MaterialBrush;
import com.elmakers.mine.bukkit.block.UndoQueue;
import com.elmakers.mine.bukkit.block.batch.UndoBatch;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.wand.Wand;

public class Mage implements CostReducer, com.elmakers.mine.bukkit.api.magic.Mage
{
	protected static int 						AUTOMATA_ONLINE_TIMEOUT = 5000;

	final static private Set<Material> EMPTY_MATERIAL_SET = new HashSet<Material>();
	
	private static String defaultMageName = "Mage";
	
	protected final String						id;
	protected WeakReference<Player> 			_player;
    protected WeakReference<Entity> 			_entity;
	protected WeakReference<CommandSender>		_commandSender;
	protected String 							playerName;
	protected final MagicController				controller;
	protected HashMap<String, MageSpell> 		spells 						   	= new HashMap<String, MageSpell>();
	private Inventory							storedInventory  			   	= null;
	private Wand								activeWand					   	= null;
	private final Collection<Listener>          quitListeners                  	= new HashSet<Listener>();
	private final Collection<Listener>          deathListeners                 	= new HashSet<Listener>();
	private final Collection<Listener>          damageListeners                	= new HashSet<Listener>();
	private final Set<MageSpell>					activeSpells				= new HashSet<MageSpell>();
	private UndoQueue          					undoQueue               	   	= null;
	private LinkedList<BlockBatch>				pendingBatches					= new LinkedList<BlockBatch>();
	
	private Location				location;
	private float 					costReduction = 0;
	private float 					cooldownReduction = 0;
	private float 					powerMultiplier = 1;
	private long 					lastClick = 0;
	private long 					lastCast = 0;
	private long 					blockPlaceTimeout = 0;
	private Location 				lastDeathLocation = null;
	private final MaterialBrush		brush;
	
	private boolean 				isNewPlayer = true;

    private Hologram                hologram;
    private Integer                 hologramTaskId = null;
    private boolean                 hologramIsVisible = false;
	
	public Mage(String id, MagicController controller) {
		this.id = id;
		this.controller = controller;
		this.brush = new MaterialBrush(this, Material.DIRT, (byte)0);
		_player = new WeakReference<Player>(null);
        _entity = new WeakReference<Entity>(null);
		_commandSender = new WeakReference<CommandSender>(null);
	}
	
	// Taken from NMS Player
    public static int getExpToLevel(int expLevel) {
        return expLevel >= 30 ? 62 + (expLevel - 30) * 7 : (expLevel >= 15 ? 17 + (expLevel - 15) * 3 : 17);
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
		LivingEntity entity = getLivingEntity();
		if (entity != null && location != null) {
            entity.teleport(location);
			return;
		}
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
	
	public void setCooldownReduction(float reduction) {
		cooldownReduction = reduction;
	}
	
	public void setPowerMultiplier(float mutliplier) {
		powerMultiplier = mutliplier;
	}
	
	public boolean usesMana() {
		return activeWand == null ? false : activeWand.usesMana();
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
		storedInventory = CompatibilityUtils.createInventory(null, inventory.getSize(), "Stored Inventory");
		
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

	public boolean cancel()
	{
		boolean result = false;
		for (MageSpell spell : spells.values())
		{
			result = spell.cancel() || result;
		}
		return result;
	}

	public void onPlayerQuit(PlayerQuitEvent event)
	{
		Player player = getPlayer();
		if (player == null || player != event.getPlayer()) {
			return;
		}
		// Must allow listeners to remove themselves during the event!
		List<Listener> active = new ArrayList<Listener>(quitListeners);
		for (Listener listener : active)
		{
			callEvent(listener, event);
		}
	}

	public void onPlayerDeath(EntityDeathEvent event)
	{
		Player player = getPlayer();
		if (player == null || player != event.getEntity()) {
			return;
		}
		lastDeathLocation = player.getLocation();
		List<Listener> active = new ArrayList<Listener>(deathListeners);
		for (Listener listener : active)
		{
			callEvent(listener, event);
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
	
	protected void callEvent(Listener listener, Event event) {
		for (Method method : listener.getClass().getMethods()) {
			if (method.isAnnotationPresent(EventHandler.class)) {
				Class<? extends Object>[] parameters = method.getParameterTypes();
				if (parameters.length == 1 && parameters[0].isAssignableFrom(event.getClass())) {
					try {
						method.invoke(listener, event);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		}
	}
	
	public void onPlayerDamage(EntityDamageEvent event)
	{
		Player player = getPlayer();
		if (player == null) {
			return;
		}
		
		// Send on to any registered spells
		List<Listener> active = new ArrayList<Listener>(damageListeners);
		for (Listener listener : active)
		{
			callEvent(listener, event);
			if (event.isCancelled()) break;
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
	
	public void setActiveWand(Wand activeWand) {
		this.activeWand = activeWand;
		blockPlaceTimeout = System.currentTimeMillis() + 200;
	}
	
	public long getBlockPlaceTimeout() {
		return blockPlaceTimeout;
	}

	/**
	 * Send a message to this Mage when a spell is cast.
	 * 
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
	 * Send a message to this Mage. 
	 * 
	 * Use this to send messages to the player that are important.
	 * 
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
	
	public void playSound(Sound sound, float volume, float pitch) {
        if (!controller.soundsEnabled()) return;

		Player player = getPlayer();
		if (player != null) {
			player.playSound(player.getLocation(), sound, volume, pitch);
		} else {
            Entity entity = getEntity();
            entity.getLocation().getWorld().playSound(entity.getLocation(), sound, volume, pitch);
        }
	}
	
	public UndoQueue getUndoQueue() {
		if (undoQueue == null) {
			undoQueue = new UndoQueue(this);
			undoQueue.setMaxSize(controller.getUndoQueueDepth());
		}
		return undoQueue;
	}
	
	public UndoList getLastUndoList() {
		if (undoQueue == null || undoQueue.isEmpty()) return null;
		return undoQueue.getLast();
	}
	
	public boolean registerForUndo(com.elmakers.mine.bukkit.api.block.UndoList undoList) {
		if (undoList == null) return false;
		if (undoList.bypass()) return true;
		
		UndoQueue queue = getUndoQueue();
		int autoUndo = controller.getAutoUndoInterval();
		if (autoUndo > 0 && undoList.getScheduledUndo() == 0) {
			undoList.setScheduleUndo(autoUndo);
		}
		if (undoList.getScheduledUndo() > 0) {
			queue.scheduleCleanup(undoList);
		} else {
			queue.add(undoList);
		}
		controller.registerForUndo(this);
		
		return true;
	}

	protected void setPlayer(Player player)
	{
		if (player != null) {
			playerName = player.getName();
			this._player = new WeakReference<Player>(player);
            this._entity = new WeakReference<Entity>(player);
			this._commandSender = new WeakReference<CommandSender>(player);
		} else {
			this._player.clear();
            this._entity.clear();
			this._commandSender.clear();
		}
	}

    protected void setEntity(Entity entity)
    {
        if (entity != null) {
            playerName = entity.getType().name().toLowerCase().replace("_", " ");
            if (entity instanceof LivingEntity) {
                LivingEntity li = (LivingEntity)entity;
                String customName = li.getCustomName();
                if (customName != null && customName.length() > 0) {
                    playerName = customName;
                }
            }
            this._entity = new WeakReference<Entity>(entity);
        } else {
            this._entity.clear();
        }
    }
	
	protected void setCommandSender(CommandSender sender)
	{
		if (sender != null) {
			this._commandSender = new WeakReference<CommandSender>(sender);

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
			this._commandSender.clear();
			setLocation(null);
		}
	}
	
	protected void load(ConfigurationSection configNode)
	{
		try {
			if (configNode == null) return;

			isNewPlayer = false;
			playerName = configNode.getString("name", playerName);
			lastDeathLocation = ConfigurationUtils.getLocation(configNode, "last_death_location");
			location = ConfigurationUtils.getLocation(configNode, "location");
			lastCast = configNode.getLong("last_cast", lastCast);
			
			getUndoQueue().load(configNode);
			ConfigurationSection spellNode = configNode.getConfigurationSection("spells");
			if (spellNode != null) {
				Set<String> keys = spellNode.getKeys(false);
				for (String key : keys) {					
					Spell spell = getSpell(key);
					if (spell != null && spell instanceof MageSpell) {
						ConfigurationSection spellSection = spellNode.getConfigurationSection(key);
						((MageSpell)spell).load(spellSection);
					}
				}
			}
			
			if (configNode.contains("brush")) {
				brush.load(configNode.getConfigurationSection("brush"));
			}
		} catch (Exception ex) {
			controller.getPlugin().getLogger().warning("Failed to load player data for " + playerName + ": " + ex.getMessage());
		}		
	}
	
	protected void save(ConfigurationSection configNode)
	{
		try {
			configNode.set("name", playerName);
			configNode.set("last_cast", lastCast);
			configNode.set("last_death_location", ConfigurationUtils.fromLocation(lastDeathLocation));
			if (location != null) {
				configNode.set("location", ConfigurationUtils.fromLocation(location));
			}
			
			ConfigurationSection brushNode = configNode.createSection("brush");
			brush.save(brushNode);
			
			getUndoQueue().save(configNode);
			ConfigurationSection spellNode = configNode.createSection("spells");
			for (MageSpell spell : spells.values()) {
				ConfigurationSection section = spellNode.createSection(spell.getKey());
				section.set("active", spell.isActive());
				spell.save(section);
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
	
	// This gets called every second (or so - 20 ticks)
	protected void tick() {
		Player player = getPlayer();
		
		// We don't tick non-player or offline Mages
		if (player != null && player.isOnline()) {
			if (activeWand != null) {
				activeWand.tick();
			}
			
			// Copy this set since spells may get removed while iterating!
			List<MageSpell> active = new ArrayList<MageSpell>(activeSpells);
			for (MageSpell spell : active) {
				spell.tick();
				if (!spell.isActive()) {
					deactivateSpell(spell);
				}
			}
		}
	}
	
	public void processPendingBatches(int maxBlockUpdates) {
		if (pendingBatches.size() > 0) {
			int updated = 0;
			List<BlockBatch> processBatches = new ArrayList<BlockBatch>(pendingBatches);
			pendingBatches.clear();
			for (BlockBatch batch : processBatches) {
				if (updated < maxBlockUpdates) {;
					int batchUpdated = batch.process(maxBlockUpdates - updated);
					updated += batchUpdated;
				}
				if (!batch.isFinished()) {
					pendingBatches.add(batch);
				}
			}
		}
		
		if (pendingBatches.size() == 0) {
			controller.removePending(this);
		}
	}
	
	public void setLastHeldMapId(short mapId) {
		brush.setMapId(mapId);
	}
	
	protected void loadSpells(ConfigurationSection config)
	{
		if (config == null) return;
		
		// TODO: Handle disabled/removed spells?
		for (Spell spell : spells.values()) {
			String key = spell.getKey();
			if (config.contains(key)) {
				spell.loadTemplate(key, config.getConfigurationSection(key));
			}
		}
	}
	
	public boolean isNewPlayer()
	{
		return this.isNewPlayer;
	}

    public void clearNewPlayer() { this.isNewPlayer = false; }

	/*
	 * API Implementation
	 */

	@Override
	public Collection<com.elmakers.mine.bukkit.api.block.BlockBatch> getPendingBatches() 
	{
		Collection<com.elmakers.mine.bukkit.api.block.BlockBatch> pending = new ArrayList<com.elmakers.mine.bukkit.api.block.BlockBatch>();
		pending.addAll(pendingBatches);
		return pending;
	}
	
	@Override
	public String getName() {
		return playerName == null || playerName.length() == 0 ? defaultMageName : playerName;
	}

    public void setName(String name) {
        playerName = name;
    }

	@Override
	public String getId() {
		return id;
	}

	@Override
	public Location getLocation() {
		if (location != null) return location.clone();
		
		LivingEntity livingEntity = getLivingEntity();
		if (livingEntity == null) return null;
		return livingEntity.getLocation();
	}
	
	@Override
	public Location getEyeLocation() {
		LivingEntity entity = getLivingEntity();
		if (entity != null) return entity.getEyeLocation();

		Location location = getLocation();
		if (location != null) {
			location.setY(location.getY() + 1.5);
			return location;
		}
		return null;
	}
	
	@Override
	public Vector getDirection() {
		Location location = getLocation();
		if (location != null) {
			return location.getDirection();
		}
		return new Vector(0, 1, 0);
	}
	
	@Override
	public UndoList undo(Block target) 
	{
		return getUndoQueue().undo(target);
	}
	
	@Override
	public BlockBatch cancelPending() 
	{
		BlockBatch stoppedPending = null;
		if (pendingBatches.size() > 0) {
			List<BlockBatch> batches = new ArrayList<BlockBatch>();
			batches.addAll(pendingBatches);
			for (BlockBatch batch : batches) {
				if (!(batch instanceof UndoBatch)) {
					batch.finish();
					pendingBatches.remove(batch);
					stoppedPending = batch;
					break;
				}
			}
		}
		return stoppedPending;
	}
	
	@Override
	public UndoList undo() 
	{
		return getUndoQueue().undo();
	}
	
	@Override
	public boolean commit() 
	{
		return getUndoQueue().commit();
	}
	
	public boolean hasCastPermission(Spell spell)
	{
		return spell.hasCastPermission(getCommandSender());
	}
	
	@Override
	public Spell getSpell(String key)
	{
		MageSpell playerSpell = spells.get(key);
		if (playerSpell == null)
		{
			SpellTemplate spellTemplate = controller.getSpellTemplate(key);
			if (spellTemplate == null) return null;
			Spell newSpell = spellTemplate.createSpell();
			if (newSpell == null || !(newSpell instanceof MageSpell)) return null;
			playerSpell = (MageSpell)newSpell;
			spells.put(newSpell.getKey(), playerSpell);
		}
		playerSpell.setMage(this);

		return playerSpell;
	}
	
	@Override
	public Collection<Spell> getSpells() {
		List<Spell> export = new ArrayList<Spell>(spells.values());
		return export;
	}

	@Override
	public void activateSpell(Spell spell) {
		if (spell instanceof MageSpell) {
			MageSpell mageSpell = ((MageSpell)spell);
			activeSpells.add(mageSpell);
			
			// Call reactivate to avoid the Spell calling back to this method,
			// and to force activation if some state has gone wrong.
			mageSpell.reactivate();
		}
	}
	
	@Override
	public void deactivateSpell(Spell spell) {
		activeSpells.remove(spell);
		
		// If this was called by the Spell itself, the following
		// should do nothing as the spell is already marked as inactive.
		if (spell instanceof MageSpell) {
			((MageSpell)spell).deactivate();
		}
	}
	
	@Override
	public void deactivateAllSpells() {
		// Copy this set since spells will get removed while iterating!
		List<MageSpell> active = new ArrayList<MageSpell>(activeSpells);
		for (MageSpell spell : active) {
			spell.deactivate();
		}
		activeSpells.clear();
	}
	
	@Override
	public boolean isCostFree() {
		// Special case for command blocks and Automata
		if (getPlayer() == null) return true;
		return activeWand == null ? false : activeWand.isCostFree();
	}
	
	@Override
	public boolean isSuperProtected()
	{
		if (isPlayer() && controller.hasPermission(getPlayer(), "Magic.protected")) return true;

        // High damage resistance is treated as "protected"
        LivingEntity livingEntity = getLivingEntity();
        if (livingEntity != null && livingEntity.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) {
            Collection<PotionEffect> effects = livingEntity.getActivePotionEffects();
            for (PotionEffect effect : effects) {
                if (effect.getType().equals(PotionEffectType.DAMAGE_RESISTANCE)) {
                    if (effect.getAmplifier() >= 100) {
                        return true;
                    } else {
                        break;
                    }
                }
            }
        }
		return activeWand != null && activeWand.isSuperProtected();
	}
	
	@Override
	public boolean isSuperPowered()
	{
		if (isPlayer() && controller.hasPermission(getPlayer(), "Magic.powered")) return true;
		return activeWand != null && activeWand.isSuperPowered();
	}
	
	@Override
	public float getCostReduction() {
		return activeWand == null ? costReduction + controller.getCostReduction() : activeWand.getCostReduction() + costReduction;
	}
	
	@Override
	public float getCooldownReduction() {
		return activeWand == null ? cooldownReduction + controller.getCooldownReduction() : activeWand.getCooldownReduction() + cooldownReduction;
	}
	
	@Override
	public boolean isCooldownFree() {
		return activeWand == null ? false : activeWand.isCooldownFree();
	}
	
	@Override
	public Color getEffectColor() {
		if (activeWand == null) return null;
		return activeWand.getEffectColor();
	}
	
	@Override
	public void onCast(Spell spell, SpellResult result) {
		lastCast = System.currentTimeMillis();
		if (spell != null && result.isSuccess()) {
			// Notify controller of successful casts,
			// this if for dynmap display or other global-level processing.
			controller.onCast(this, spell, result);

            //TODO: Move this to BaseSpell?
            if (controller.getShowCastHoloText()) {
                showHoloText(getEyeLocation(), spell.getName(), 10000);
            }
		}
	}

	@Override
	public float getPower() {
		float power = Math.min(controller.getMaxPower(), activeWand == null ? 0 : activeWand.getPower());
		return power * powerMultiplier;
	}

	@Override
	public boolean isRestricted(Material material) 
	{
		if (isSuperPowered()) {
			return false;
		}
		return controller.isRestricted(material);	
	}
	
	@Override
	public MageController getController() {
		return controller;
	}
	
	@Override
	public Set<Material> getRestrictedMaterials()
	{
		if (isSuperPowered()) {
			return EMPTY_MATERIAL_SET;
		}
		return controller.getRestrictedMaterials();
	}

    @Override
    public boolean isPVPAllowed(Location location)
    {
        Player player = getPlayer();
        if (player != null) {
            return controller.isPVPAllowed(player, location);
        }

        return controller.isPVPAllowed(location == null ? getLocation() : location);
    }
	
	@Override
	public boolean hasBuildPermission(Block block) {
		return controller.hasBuildPermission(getPlayer(), block);
	}
	
	@Override
	public boolean isIndestructible(Block block) {
		return controller.isIndestructible(block);
	}
	
	@Override
	public boolean isDestructible(Block block) {
		return controller.isDestructible(block);
	}
	
	@Override
	public boolean isDead()
	{
		LivingEntity entity = getLivingEntity();
		if (entity != null) {
			return entity.isDead();
		}
		// Check for automata
		CommandSender sender = getCommandSender();
		if (sender == null || !(sender instanceof BlockCommandSender)) return true;
		BlockCommandSender commandBlock = (BlockCommandSender)sender;
		Block block = commandBlock.getBlock();
		if (!block.getChunk().isLoaded()) return true;
		return (block.getType() != Material.COMMAND);
	}
	
	@Override
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

	@Override
	public boolean isPlayer()
	{
		Player player = getPlayer();
		return player != null;
	}
	
	@Override
	public boolean hasLocation()
	{
		return getLocation() != null;
	}
	
	@Override
	public Inventory getInventory() {
        if (hasStoredInventory()) {
            return getStoredInventory();
        }

		Player player = getPlayer();
        if (player != null) {
            return player.getInventory();
        }
        // TODO: Maybe wrap EntityEquipment in an Inventory... ? Could be hacky.
        return null;
	}
	
	@Override
	public Wand getActiveWand() {
		return activeWand;
	}
	
	@Override
	public com.elmakers.mine.bukkit.api.block.MaterialBrush getBrush() {
		return brush;
	}
	
	@Override
	public float getDamageMultiplier() {
		float maxPowerMultiplier = controller.getMaxDamagePowerMultiplier() - 1;
		return 1 + (maxPowerMultiplier * getPower());
	}
	
	@Override
	public float getRangeMultiplier() {
		if (activeWand == null) return 1;
		
		float maxPowerMultiplier = controller.getMaxRangePowerMultiplier() - 1;
		float maxPowerMultiplierMax = controller.getMaxRangePowerMultiplierMax();
		float multiplier = 1 + (maxPowerMultiplier * getPower());
		return Math.min(multiplier, maxPowerMultiplierMax);
	}
	
	@Override
	public float getConstructionMultiplier() {
		float maxPowerMultiplier = controller.getMaxConstructionPowerMultiplier() - 1;
		return 1 + (maxPowerMultiplier * getPower());
	}
	
	@Override
	public float getRadiusMultiplier() {
		if (activeWand == null) return 1;
		
		float maxPowerMultiplier = controller.getMaxRadiusPowerMultiplier() - 1;
		float maxPowerMultiplierMax = controller.getMaxRadiusPowerMultiplierMax();
		float multiplier = 1 + (maxPowerMultiplier * getPower());
		return Math.min(multiplier, maxPowerMultiplierMax);
	}
	
	@Override
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
	
	@Override
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
	
	@Override
	public boolean addPendingBlockBatch(com.elmakers.mine.bukkit.api.block.BlockBatch batch) {
		if (pendingBatches.size() >= controller.getPendingQueueDepth()) {
			controller.getLogger().info("Rejected construction for " + getName() + ", already has " + pendingBatches.size()
					+ " pending, limit: " + controller.getPendingQueueDepth());
			
			return false;
		}
		pendingBatches.addLast(batch);
		controller.addPending(this);
		return true;
	}

	@Override
	public void registerEvent(SpellEventType type, Listener spell)
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

	@Override
	public void unregisterEvent(SpellEventType type, Listener spell)
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
	
	@Override
	public Player getPlayer()
	{
		return _player.get();
	}

    @Override
    public Entity getEntity()
    {
        return _entity.get();
    }

    @Override
    public LivingEntity getLivingEntity()
    {
        Entity entity = _entity.get();
        return (entity != null && entity instanceof LivingEntity) ? (LivingEntity)entity : null;
    }
	
	@Override
	public CommandSender getCommandSender()
	{
		return _commandSender.get();
	}
	
	@Override
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
	
	@Override
	public Location getLastDeathLocation()
	{
		return lastDeathLocation;
	}

    public void showHoloText(Location location, String text, int duration)
    {
        // TODO: Broadcast
        if (!isPlayer()) return;
        final Player player = getPlayer();

        if (hologram == null)
        {
            hologram = HoloUtils.createHoloText(location, text);
        }
        else
        {
            if (hologramIsVisible)
            {
                hologram.hide(player);
            }
            hologram.teleport(location);
            hologram.setLabel(text);
        }

        hologram.show(player);

        BukkitScheduler scheduler = Bukkit.getScheduler();
        if (hologramTaskId != null)
        {
            scheduler.cancelTask(hologramTaskId);
        }
        if (duration > 0)
        {
            scheduler.scheduleSyncDelayedTask(controller.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    hologram.hide(player);
                    hologramIsVisible = false;
                }
            }, duration);
        }
    }
}
