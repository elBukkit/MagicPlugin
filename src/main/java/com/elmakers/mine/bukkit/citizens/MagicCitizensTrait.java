package com.elmakers.mine.bukkit.citizens;

import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.integration.VaultController;
import com.elmakers.mine.bukkit.magic.MagicPlugin;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.milkbowl.vault.Vault;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import javax.security.auth.login.Configuration;
import java.util.Collection;

public class MagicCitizensTrait extends Trait {

    private String spellKey;
    private String permissionNode;
    private boolean npcCaster = true;
    private YamlConfiguration parameters = null;
    private double cost = 0;
    private MagicAPI api;

	public MagicCitizensTrait() {
		super("magic");
	}

	public void load(DataKey data) {
        spellKey = data.getString("spell", null);
        permissionNode = data.getString("permission", null);
        npcCaster = data.getBoolean("caster", false);
        cost = data.getDouble("cost", 0);
        String parameterString = data.getString("parameters", null);
        parameters = new YamlConfiguration();
        if (parameterString != null && !parameterString.isEmpty()) {
            if (!parameterString.contains(":")) {
                String[] simple = StringUtils.split(parameterString, " ");
                if (simple.length > 0) {
                    ConfigurationUtils.addParameters(simple, parameters);
                }
            } else {
                try {
                    parameters.loadFromString(parameterString);
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
	}

	public void save(DataKey data) {
        data.setString("spell", spellKey);
        data.setString("permission", permissionNode);
        data.setBoolean("caster", npcCaster);
        String parameterString = parameters.saveToString();
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
        if (permissionNode != null && !player.hasPermission(permissionNode)) {
            return;
        }
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

        ConfigurationSection config = this.parameters;
        if (npcCaster) {
            if (event.getNPC().isSpawned()) {
                entity = event.getNPC().getBukkitEntity();
                sender = null;
                config = new MemoryConfiguration();
                ConfigurationUtils.addConfigurations(config, parameters);
                config.set("player", event.getClicker().getName());
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
        String permissionDescription = permissionNode == null ? (ChatColor.GRAY + "(None)") : (ChatColor.LIGHT_PURPLE + permissionNode);
        sender.sendMessage(ChatColor.DARK_PURPLE + "Permission: " + permissionDescription);
        String casterDescription = npcCaster ? (ChatColor.GRAY + "NPC") : (ChatColor.LIGHT_PURPLE + "Player");
        sender.sendMessage(ChatColor.DARK_PURPLE + "Caster: " + casterDescription);
        if (VaultController.hasEconomy()) {
            VaultController vault = VaultController.getInstance();
            sender.sendMessage(ChatColor.DARK_PURPLE + "Cost: " + ChatColor.GOLD + vault.format(cost));
        }
        sender.sendMessage(ChatColor.DARK_PURPLE + "Parameters: ");
        describeParameters(sender);
    }

    protected void describeParameters(CommandSender sender) {
        Collection<String> keys = parameters.getKeys(false);
        if (keys.size() == 0) {
            sender.sendMessage(ChatColor.GRAY + " (None)");
        }
        for (String key : keys) {
            String value = null;
            if (parameters.isConfigurationSection(key)) {
                ConfigurationSection child = parameters.getConfigurationSection(key);
                value = "(" + child.getKeys(false).size() + " values)";
            } else {
                value = parameters.getString(key);
            }
            sender.sendMessage(ChatColor.LIGHT_PURPLE + " " + key + ": " + value);
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
            spellKey = value;
            if (value == null)
            {
                sender.sendMessage(ChatColor.RED + "Cleared spell");
            }
            else
            {
                sender.sendMessage(ChatColor.DARK_PURPLE + "Set spell to: " + ChatColor.LIGHT_PURPLE + spellKey);
            }
        }
        if (key.equalsIgnoreCase("permission"))
        {
            permissionNode = value;
            if (value == null)
            {
                sender.sendMessage(ChatColor.RED + "Cleared permission node");
            }
            else
            {
                sender.sendMessage(ChatColor.DARK_PURPLE + "Set required permission to: " + ChatColor.LIGHT_PURPLE + permissionNode);
            }
        }
        else if (key.equalsIgnoreCase("parameters"))
        {
            if (value == null)
            {
                sender.sendMessage(ChatColor.RED + "Cleared parameters");
                parameters = null;
            }
            else
            {
                String[] params = StringUtils.split(value, " ");
                parameters = new YamlConfiguration();
                ConfigurationUtils.addParameters(params, parameters);
                sender.sendMessage(ChatColor.DARK_PURPLE + "Set parameters to: ");
                describeParameters(sender);
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