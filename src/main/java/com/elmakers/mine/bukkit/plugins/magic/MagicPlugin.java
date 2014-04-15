package com.elmakers.mine.bukkit.plugins.magic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.elmakers.mine.bukkit.api.magic.Automaton;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.wand.LostWand;
import com.elmakers.mine.bukkit.block.BlockData;
import com.elmakers.mine.bukkit.plugins.magic.commands.CastCommandExecutor;
import com.elmakers.mine.bukkit.plugins.magic.commands.MagicCommandExecutor;
import com.elmakers.mine.bukkit.plugins.magic.commands.SpellsCommandExecutor;
import com.elmakers.mine.bukkit.plugins.magic.commands.WandCommandExecutor;
import com.elmakers.mine.bukkit.plugins.magic.wand.Wand;
import com.elmakers.mine.bukkit.utilities.URLMap;

public class MagicPlugin extends JavaPlugin implements MagicAPI
{	
	/*
	 * Private data
	 */	
	private MagicController controller = null;

	/*
	 * Plugin interface
	 */

	public void onEnable() 
	{
		if (controller == null) {
			controller = new MagicController(this);
		}
		initialize();

		BlockData.setServer(getServer());
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(controller, this);
		
		TabExecutor magicCommand = new MagicCommandExecutor(this);
		getCommand("magic").setExecutor(magicCommand);
		getCommand("magic").setTabCompleter(magicCommand);
		TabExecutor castCommand = new CastCommandExecutor(this);
		getCommand("cast").setExecutor(castCommand);
		getCommand("cast").setTabCompleter(castCommand);
		getCommand("castp").setExecutor(castCommand);
		getCommand("castp").setTabCompleter(castCommand);
		TabExecutor wandCommand = new WandCommandExecutor(this);
		getCommand("wand").setExecutor(wandCommand);
		getCommand("wand").setTabCompleter(wandCommand);
		getCommand("wandp").setExecutor(wandCommand);
		getCommand("wandp").setTabCompleter(wandCommand);
		TabExecutor spellsCommand = new SpellsCommandExecutor(this);
		getCommand("spells").setExecutor(spellsCommand);
	}

	protected void initialize()
	{
		controller.initialize();
	}

	/* 
	 * Help commands
	 */

	public void onDisable() 
	{
		controller.save();
		controller.clear();
	}

	/*
	 * API Implementation
	 */
	
	@Override
	public Plugin getPlugin() {
		return this;
	}

	@Override
	public boolean hasPermission(CommandSender sender, String pNode) {
		return controller.hasPermission(sender, pNode);
	}

	@Override
	public boolean hasPermission(CommandSender sender, String pNode, boolean defaultPermission) {
		return controller.hasPermission(sender, pNode, defaultPermission);
	}

	@Override
	public void save() {
		controller.save();
		URLMap.save();
	}

	@Override
	public void reload() {
		controller.loadConfiguration();
		URLMap.loadConfiguration();
	}

	@Override
	public void clearCache() {
		controller.clearCache();
		URLMap.clearCache();
	}
	
	@Override
	public boolean commit() {
		return controller.commitAll();
	}
	
	@Override
	public Collection<com.elmakers.mine.bukkit.api.magic.Mage> getMages() {
		Collection<com.elmakers.mine.bukkit.api.magic.Mage> mages = new ArrayList<com.elmakers.mine.bukkit.api.magic.Mage>();
		Collection<Mage> internal = controller.getMages();
		for (Mage mage : internal) {
			mages.add(mage);
		}
		return mages;
	}
	
	@Override
	public Collection<com.elmakers.mine.bukkit.api.magic.Mage> getMagesWithPendingBatches() {
		Collection<com.elmakers.mine.bukkit.api.magic.Mage> mages = new ArrayList<com.elmakers.mine.bukkit.api.magic.Mage>();
		Collection<Mage> internal = controller.getPending();
		mages.addAll(internal);
		return mages;
	}
	
	@Override
	public Collection<LostWand> getLostWands() {
		Collection<LostWand> lostWands = new ArrayList<LostWand>();
		lostWands.addAll(controller.getLostWands());
		return lostWands;
	}
	
	@Override
	public Collection<Automaton> getAutomata() {
		Collection<Automaton> automata = new ArrayList<Automaton>();
		automata.addAll(controller.getAutomata());
		return automata;
	}

	@Override
	public void removeLostWand(String id) {
		controller.removeLostWand(id);
	}

	@Override
	public com.elmakers.mine.bukkit.api.wand.Wand getWand(ItemStack itemStack) {
		return new Wand(controller, itemStack);
	}
	
	public boolean isWand(ItemStack item) {
		return Wand.isWand(item);
	}

	@Override
	public void giveItemToPlayer(Player player, ItemStack itemStack) {
		// Place directly in hand if possible
		PlayerInventory inventory = player.getInventory();
		ItemStack inHand = inventory.getItemInHand();
		if (inHand == null || inHand.getType() == Material.AIR) {
			inventory.setItem(inventory.getHeldItemSlot(), itemStack);
			if (Wand.isWand(itemStack)) {
				Wand wand = new Wand(controller, itemStack);
				wand.activate(controller.getMage(player));
			}
		} else {
			HashMap<Integer, ItemStack> returned = player.getInventory().addItem(itemStack);
			if (returned.size() > 0) {
				player.getWorld().dropItem(player.getLocation(), itemStack);
			}
		}
	}

	@Override
	public com.elmakers.mine.bukkit.api.magic.Mage getMage(CommandSender sender) {
		return controller.getMage(sender);
	}

	@Override
	public com.elmakers.mine.bukkit.api.wand.Wand createWand(String wandKey) {
		return Wand.createWand(controller, wandKey);
	}

	@Override
	public ItemStack createSpellItem(String spellKey) {
		return Wand.createSpellItem(spellKey, controller, null, true);
	}

	@Override
	public ItemStack createBrushItem(String brushKey) {
		return Wand.createMaterialItem(brushKey, controller, null, true);
	}

	@Override
	public void cast(String spellName, String[] parameters) {
		controller.cast(null, spellName, parameters, null, null);
	}

	@Override
	public Collection<com.elmakers.mine.bukkit.api.spell.Spell> getSpells() {
		Collection<com.elmakers.mine.bukkit.api.spell.Spell> spells = new ArrayList<com.elmakers.mine.bukkit.api.spell.Spell>();
		spells.addAll(controller.getAllSpells());
		return spells;
	}

	@Override
	public Collection<String> getWandKeys() {
		return Wand.getWandKeys();
	}

	@Override
	public void cast(String spellName, String[] parameters, CommandSender sender, Player player) {
		controller.cast(null, spellName, parameters, sender, player);
	}

	@Override
	public Collection<String> getPlayerNames() {
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
	public com.elmakers.mine.bukkit.api.wand.Wand createWand(Material iconMaterial, short iconData) {
		return new Wand(controller, iconMaterial, iconData);
	}

	@Override
	public Spell getSpell(String key) {
		return controller.getSpell(key);
	}
}
