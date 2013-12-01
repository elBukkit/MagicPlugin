package com.elmakers.mine.bukkit.plugins.magic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.utilities.InventoryUtils;

public class PlayerSpells implements CostReducer
{
	protected Player player;
	protected Spells master;
	protected HashMap<String, Spell> spells = new HashMap<String, Spell>();
	private Inventory							storedInventory  			   = null;
	private Wand								activeWand					   = null;
	private final List<Spell>                   movementListeners              = new ArrayList<Spell>();
	private final List<Spell>                   quitListeners                  = new ArrayList<Spell>();
	private final List<Spell>                   deathListeners                 = new ArrayList<Spell>();
	private final List<Spell>                   damageListeners                = new ArrayList<Spell>();
	
	private float costReduction = 0;
	private float cooldownReduction = 0;
	private ItemStack buildingMaterial = null;

	public void removeExperience(int xp) {
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
	
	// Taken from mc Player
    public static int getExpToLevel(int expLevel) {
        return expLevel >= 30 ? 62 + (expLevel - 30) * 7 : (expLevel >= 15 ? 17 + (expLevel - 15) * 3 : 17);
    }
	
	public int getExperience() {
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
	
	public float getCostReduction() {
		return activeWand == null ? costReduction : activeWand.getCostReduction() + costReduction;
	}
	
	public float getPowerMultiplier() {
		float maxPowerMultiplier = master.getMaxPowerMultiplier() - 1;
		return activeWand == null ? 1 : 1 + (maxPowerMultiplier * activeWand.getPower());
	}
	
	public float getCooldownReduction() {
		return activeWand == null ? cooldownReduction : activeWand.getCooldownReduction() + cooldownReduction;
	}
	
	public void setCooldownReduction(float reduction) {
		cooldownReduction = reduction;
	}

	public boolean addToStoredInventory(ItemStack item) {
		if (storedInventory == null) {
			return false;
		}

		HashMap<Integer, ItemStack> remainder = storedInventory.addItem(item);
		for (ItemStack remains : remainder.values()) {
			player.getWorld().dropItemNaturally(player.getLocation(), remains);
		}

		return true;
	}

	public boolean storeInventory(int keepSlot, ItemStack keepItem) {
		Inventory inventory = player.getInventory();
		if (storedInventory != null) {
			return false;
		}

		if (keepItem != null) {
			inventory.clear(keepSlot);
		}

		storedInventory = InventoryUtils.createInventory(null, inventory.getSize(), "Magic.Wand.StoredInventory");
		
		// Make sure we don't store any spells or magical materials, just in case
		ItemStack[] contents = inventory.getContents();
		for (int i = 0; i < contents.length; i++) {
			if (Wand.isSpell(contents[i])) {
				contents[i] = null;
			}
		}
		storedInventory.setContents(contents);
		inventory.clear();

		if (keepItem != null) {
			inventory.setItem(keepSlot, keepItem);
		}

		return true;
	}

	public boolean storeInventory() {
		return storeInventory(player.getInventory().getHeldItemSlot(), player.getInventory().getItemInHand());
	}

	public boolean restoreInventory() {
		return restoreInventory(player.getInventory().getHeldItemSlot(), player.getInventory().getItemInHand());
	}

	public boolean restoreInventory(int keepSlot, ItemStack keepItem) {
		if (storedInventory == null) {
			return false;
		}
		Inventory inventory = player.getInventory();
		inventory.setContents(storedInventory.getContents());
		storedInventory = null;

		if (keepItem != null) {
			ItemStack occupied = inventory.getItem(keepSlot);
			inventory.setItem(keepSlot, keepItem);
			if (occupied != null) {
				HashMap<Integer, ItemStack> remainder = inventory.addItem(occupied);
				for (ItemStack remains : remainder.values()) {
					player.getWorld().dropItemNaturally(player.getLocation(), remains);
				}
			}
		}

		return true;
	}

	public void registerEvent(SpellEventType type, Spell spell)
	{
		switch (type)
		{
		case PLAYER_MOVE:
			if (!movementListeners.contains(spell))
				movementListeners.add(spell);
			break;
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

	public void setPlayer(Player player)
	{
		this.player = player;
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

	public PlayerSpells(Spells master, Player player)
	{
		this.master = master;
		this.player = player;
	}
	
	public Player getPlayer()
	{
		return player;
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

	public void onPlayerDeath(EntityDeathEvent event)
	{
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
	
	public void onPlayerDamage(EntityDamageEvent event)
	{
		// Send on to any registered spells
		List<Spell> active = new ArrayList<Spell>();
		active.addAll(damageListeners);
		for (Spell listener : active)
		{
			listener.onPlayerDamage(event);
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
		
		if (reduction >= 1) {
			event.setCancelled(true);
			return;
		}
		
		if (reduction > 0) {
			int newDamage = (int)Math.floor((1.0f - reduction) * event.getDamage());
			if (newDamage == 0) newDamage = 1;
			event.setDamage(newDamage);
		}
	}


	public Spell getSpell(Material material)
	{
		Spell spell = master.getSpell(material);
		if (spell == null || !spell.hasSpellPermission(player))
			return null;

		return getSpell(spell.getKey());
	}

	public Spell getSpell(String name)
	{
		Spell spell = master.getSpell(name);
		if (spell == null || !spell.hasSpellPermission(player))
			return null;

		Spell playerSpell = spells.get(spell.getKey());
		if (playerSpell == null)
		{
			playerSpell = (Spell)spell.clone();
			spells.put(spell.getKey(), playerSpell);
		}

		playerSpell.setPlayer(player);

		return playerSpell;
	}
	
	public Spells getMaster() {
		return master;
	}
	
	public Inventory getInventory() {
		return hasStoredInventory() ? getStoredInventory() : player.getInventory();
	}
	
	public Wand getActiveWand() {
		if (activeWand != null) {
			ItemStack currentItem = player.getItemInHand();
			if (Wand.isWand(currentItem)) {
				activeWand.setItem(currentItem);
			}
		}
		return activeWand;
	}
	
	public void setActiveWand(Wand activeWand) {
		this.activeWand = activeWand;
	}
	
	@SuppressWarnings("deprecation")
	public void setBuildingMaterial(Material material, byte data) {
		if (material == Wand.CopyMaterial) {
			buildingMaterial = null;
			return;
		}
		if (material == Wand.EraseMaterial) {
			material = Material.AIR;
		}
		buildingMaterial = new ItemStack(material, 1, (short)0, data);
	}
	
	public void clearBuildingMaterial() {
		buildingMaterial = null;
	}
	
	public ItemStack getBuildingMaterial() {
		return buildingMaterial;
	}
	
	public boolean hasBuildPermission(Location location) {
		return master.hasBuildPermission(player, location);
	}
	
	public boolean hasBuildPermission(Block block) {
		return master.hasBuildPermission(player, block);
	}
	
	public void onCast(SpellResult result) {
		switch(result) {
			case SUCCESS:
				// No sound on success
				break;
			case INSUFFICIENT_RESOURCES:
				// player.playEffect(player.getLocation(), Effect.SMOKE,  null);
				playSound(Sound.NOTE_BASS, 1, 1);
				break;
			case COOLDOWN:
				// player.playEffect(player.getLocation(), Effect.SMOKE,  null);
				playSound(Sound.NOTE_SNARE_DRUM, 1, 1);
				break;
			case NO_TARGET:
				playSound(Sound.NOTE_STICKS, 1, 1);
				break;
			case COST_FREE:
				break;
			default:
				// player.playEffect(player.getLocation(), Effect.EXTINGUISH,  null);
				playSound(Sound.NOTE_BASS_DRUM, 1, 1);
		}
	}
	
	public void playSound(Sound sound, float volume, float pitch) {
		if (master.soundsEnabled()) {
			player.playSound(player.getLocation(), sound, volume, pitch);
		}
	}
}
