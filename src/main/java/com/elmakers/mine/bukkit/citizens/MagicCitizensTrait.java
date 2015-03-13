package com.elmakers.mine.bukkit.citizens;

import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.integration.VaultController;
import com.elmakers.mine.bukkit.magic.MagicPlugin;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.milkbowl.vault.Vault;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class MagicCitizensTrait extends Trait {

    private String spellKey;
    private boolean npcCaster = true;
    private String[] parameters = null;
    private double cost = 0;
    private MagicAPI api;

	public MagicCitizensTrait() {
		super("magic");
	}

	public void load(DataKey data) {
        spellKey = data.getString("spell", null);
        npcCaster = data.getBoolean("caster", false);
        cost = data.getDouble("cost", 0);
        String parameterString = data.getString("parameters", null);
        parameters = null;
        if (parameterString != null && !parameterString.isEmpty()) {
            parameters = StringUtils.split(parameterString, " ");
            if (parameters.length == 0) {
                parameters = null;
            }
        }
	}

	public void save(DataKey data) {
        data.setString("spell", spellKey);
        data.setBoolean("caster", npcCaster);
        String parameterString = parameters != null && parameters.length > 0 ? StringUtils.join(parameters, " ") : null;
        data.setString("parameters", parameterString);
        data.setDouble("cost", cost);
	}

	@Override
	public void onRemove() {
	}

	@Override
	public void onAttach() {
        load(new net.citizensnpcs.api.util.MemoryDataKey());
        api = MagicPlugin.getAPI();
	}

    @EventHandler
    public void onClick(net.citizensnpcs.api.event.NPCRightClickEvent event){
        if (event.getNPC() != this.getNPC() || spellKey == null || spellKey.isEmpty()) return;

        CommandSender sender = event.getClicker();
        Player player = event.getClicker();
        Entity entity = event.getClicker();
        if (cost > 0) {
            if (!VaultController.hasEconomy()) {
                sender.sendMessage(api.getMessages().get("economy.missing"));
                return;
            }
            VaultController vault = VaultController.getInstance();
            if (!vault.has(player, cost)) {
                sender.sendMessage(api.getMessages().get("economy.insufficient").replace("$cost", vault.format(cost)));
                return;
            }
        }

        String[] parameters = this.parameters;
        if (npcCaster) {
            if (event.getNPC().isSpawned()) {
                entity = event.getNPC().getBukkitEntity();
                if (parameters == null) {
                    parameters = new String[2];
                } else {
                    parameters = new String[parameters.length + 2];
                    System.arraycopy(this.parameters, 0, parameters, 2, this.parameters.length);
                }
                parameters[0] = "player";
                parameters[1] = event.getClicker().getName();
            }
        }

        boolean result = api.cast(spellKey, parameters, sender, entity);
        if (result && cost > 0) {
            VaultController vault = VaultController.getInstance();
            sender.sendMessage(api.getMessages().get("economy.deducted").replace("$cost", vault.format(cost)));
            vault.withdrawPlayer(player, cost);
        }
    }

    public void describe(CommandSender sender)
    {
        sender.sendMessage(ChatColor.AQUA + "Magic NPC: " + ChatColor.GOLD + npc.getName() +
                ChatColor.WHITE + "(" + ChatColor.GRAY + npc.getId() + ChatColor.WHITE + ")");
        String spellDescription = spellKey == null ? (ChatColor.RED + "(None)") : (ChatColor.LIGHT_PURPLE + spellKey);
        sender.sendMessage(ChatColor.DARK_PURPLE + "Spell: " + spellDescription);
        String parameterDescription = parameters == null ? (ChatColor.GRAY + "(None)") : (ChatColor.LIGHT_PURPLE + StringUtils.join(parameters, " "));
        sender.sendMessage(ChatColor.DARK_PURPLE + "Parameters: " + parameterDescription);
        String casterDescription = npcCaster ? (ChatColor.GRAY + "NPC") : (ChatColor.LIGHT_PURPLE + "Player");
        sender.sendMessage(ChatColor.DARK_PURPLE + "Caster: " + casterDescription);
        if (VaultController.hasEconomy()) {
            VaultController vault = VaultController.getInstance();
            sender.sendMessage(ChatColor.DARK_PURPLE + "Cost: " + ChatColor.GOLD + vault.format(cost));
        }
    }

    public void configure(CommandSender sender, String key, String value)
    {
        if (key == null)
        {
            return;
        }
        if (key.equalsIgnoreCase("spell"))
        {
            if (value == null)
            {
                sender.sendMessage(ChatColor.RED + "Cleared spell");
            }
            else
            {
                spellKey = value;
                sender.sendMessage(ChatColor.DARK_PURPLE + "Set spell to: " + ChatColor.LIGHT_PURPLE + spellKey);
            }
        }
        else if (key.equalsIgnoreCase("parameters"))
        {
            if (value == null)
            {
                sender.sendMessage(ChatColor.RED + "Cleared parameters");
            }
            else
            {
                parameters = StringUtils.split(value, " ");
                sender.sendMessage(ChatColor.DARK_PURPLE + "Set parameters to: " + ChatColor.LIGHT_PURPLE + value);
            }
        }
        else if (key.equalsIgnoreCase("caster"))
        {
            if (value == null || !value.equalsIgnoreCase("true"))
            {
                sender.sendMessage(ChatColor.DARK_PURPLE + "Set caster as player");
                npcCaster = false;
            }
            else
            {
                npcCaster = true;
                sender.sendMessage(ChatColor.DARK_PURPLE + "Set caster as NPC");
            }
        }
        else if (key.equalsIgnoreCase("cost"))
        {
            if (value == null)
            {
                sender.sendMessage(ChatColor.DARK_PURPLE + "Cleared cost");
                cost = 0;
            }
            else
            {
                try {
                    cost = Double.parseDouble(value);
                    if (VaultController.hasEconomy()) {
                        VaultController vault = VaultController.getInstance();
                        sender.sendMessage(ChatColor.DARK_PURPLE + "Set cost to: " + ChatColor.GOLD + vault.format(cost));
                    } else {
                        sender.sendMessage(ChatColor.DARK_PURPLE + "Set cost to " + value);
                    }
                } catch (Exception ex) {
                    sender.sendMessage(ChatColor.RED + "Invalid cost: " + value);
                }
            }
        }
        else
        {
            sender.sendMessage(ChatColor.RED + "Expecting: spell, parameters or caster");
        }
    }
}