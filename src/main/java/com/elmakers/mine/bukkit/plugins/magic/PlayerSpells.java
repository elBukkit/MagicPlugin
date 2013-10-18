package com.elmakers.mine.bukkit.plugins.magic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.utilities.InventoryUtils;

public class PlayerSpells 
{
	protected Player player;
	protected Spells master;
	protected HashMap<String, Spell> spells = new HashMap<String, Spell>();
	private Inventory							storedInventory  			   = null;
	private final List<Spell>                   movementListeners              = new ArrayList<Spell>();
	private final List<Spell>                   quitListeners                  = new ArrayList<Spell>();
	private final List<Spell>                   deathListeners                 = new ArrayList<Spell>();
	private final List<Spell>                   damageListeners                = new ArrayList<Spell>();

	public boolean hasStoredInventory() {
		return storedInventory != null;
	}

	public Inventory getStoredInventory() {
		return storedInventory;
	}

	public void clearStoredInventory() {
		storedInventory = null;
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
		storedInventory.setContents(inventory.getContents());
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
			inventory.setItem(keepSlot,keepItem);
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

	public void cancel()
	{
		for (Spell spell : spells.values())
		{
			spell.cancel();
		}
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
		List<Spell> active = new ArrayList<Spell>();
		active.addAll(damageListeners);
		for (Spell listener : active)
		{
			listener.onPlayerDamage(event);
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
}
