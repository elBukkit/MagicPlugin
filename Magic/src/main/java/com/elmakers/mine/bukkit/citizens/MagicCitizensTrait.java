package com.elmakers.mine.bukkit.citizens;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

import net.citizensnpcs.api.util.DataKey;

public class MagicCitizensTrait extends CitizensTrait {

    private String spellKey;
    private boolean npcCaster = false;
    private boolean targetPlayer = true;
    private boolean messagePlayer = false;
    private YamlConfiguration parameters = null;

    public MagicCitizensTrait() {
        super("magic");
    }

    @Override
    public void load(DataKey data) {
        super.load(data);
        spellKey = data.getString("spell", null);
        npcCaster = data.getBoolean("caster", false);
        targetPlayer = data.getBoolean("target_player", true);
        messagePlayer = data.getBoolean("message_player", false);
        String parameterString = data.getString("parameters", null);
        parameters = new YamlConfiguration();
        if (parameterString != null && !parameterString.isEmpty()) {
            if (!parameterString.contains(":")) {
                String[] simple = StringUtils.split(parameterString, ' ');
                if (simple.length > 0) {
                    ConfigurationUtils.addParameters(simple, parameters);
                }
            } else {
                try {
                    parameters.loadFromString(parameterString);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    @Override
    public void save(DataKey data) {
        super.save(data);
        data.setString("spell", spellKey);
        data.setBoolean("caster", npcCaster);
        data.setBoolean("target_player", targetPlayer);
        data.setBoolean("message_player", messagePlayer);
        String parameterString = parameters == null ? null : parameters.saveToString();
        data.setString("parameters", parameterString);
    }

    @Override
    public boolean perform(net.citizensnpcs.api.event.NPCRightClickEvent event) {
        if (spellKey == null || spellKey.isEmpty()) return false;

        CommandSender sender = event.getClicker();
        Entity entity = event.getClicker();
        ConfigurationSection config = this.parameters;
        if (npcCaster) {
            if (event.getNPC().isSpawned()) {
                entity = event.getNPC().getEntity();
                if (!messagePlayer) {
                    sender = null;
                }
                if (targetPlayer) {
                    config = new MemoryConfiguration();
                    ConfigurationUtils.addConfigurations(config, parameters);
                    config.set("player", event.getClicker().getName());
                }
            }
        }

        return api.cast(spellKey, config, sender, entity);
    }

    @Override
    public void describe(CommandSender sender)
    {
        super.describe(sender);
        String spellDescription = spellKey == null ? (ChatColor.RED + "(None)") : (ChatColor.LIGHT_PURPLE + spellKey);
        sender.sendMessage(ChatColor.DARK_PURPLE + "Spell: " + spellDescription);
        String casterDescription = npcCaster ? (ChatColor.GRAY + "NPC") : (ChatColor.LIGHT_PURPLE + "Player");
        sender.sendMessage(ChatColor.DARK_PURPLE + "Caster: " + casterDescription);
        if (npcCaster) {
            if (targetPlayer) {
                sender.sendMessage(" " + ChatColor.YELLOW + "Will auto-target player");
            }
            if (messagePlayer) {
                sender.sendMessage(" " + ChatColor.YELLOW + "Will relay cast messages to player");
            }
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

    @Override
    public void configure(CommandSender sender, String key, String value)
    {
        if (key == null)
        {
            return;
        }
        if (key.equalsIgnoreCase("spell") || key.equalsIgnoreCase("cast"))
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
        else if (key.equalsIgnoreCase("parameters") || key.equalsIgnoreCase("parameter"))
        {
            if (value == null)
            {
                sender.sendMessage(ChatColor.RED + "Cleared parameters");
                parameters = new YamlConfiguration();;
            }
            else
            {
                String[] params = StringUtils.split(value, ' ');
                if (params.length == 1) {
                    parameters.set(params[0], null);
                    sender.sendMessage(ChatColor.DARK_PURPLE + "Cleared " + ChatColor.LIGHT_PURPLE + params[0]
                            + ChatColor.DARK_PURPLE + ", parameters now: ");
                    describeParameters(sender);
                } else {
                    ConfigurationUtils.addParameters(params, parameters);
                    sender.sendMessage(ChatColor.DARK_PURPLE + "Set parameters to: ");
                    describeParameters(sender);
                }
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
        else if (key.equalsIgnoreCase("target_player"))
        {
            if (value == null || !value.equalsIgnoreCase("true"))
            {
                sender.sendMessage(ChatColor.DARK_PURPLE + "Will not auto-target the clicking player");
                targetPlayer = false;
            }
            else
            {
                targetPlayer = true;
                sender.sendMessage(ChatColor.DARK_PURPLE + "Will auto-target the clicking player");
            }
            if (!npcCaster) {
                sender.sendMessage(ChatColor.RED + "NOTE: " + ChatColor.YELLOW + "Has no effect unless you also set " + ChatColor.AQUA + "caster true");
            }
        }
        else if (key.equalsIgnoreCase("message_player"))
        {
            if (value == null || !value.equalsIgnoreCase("true"))
            {
                sender.sendMessage(ChatColor.DARK_PURPLE + "Will not relay messages to the clicking player");
                messagePlayer = false;
            }
            else
            {
                messagePlayer = true;
                sender.sendMessage(ChatColor.DARK_PURPLE + "Will relay messages to the clicking player");
            }
            if (!npcCaster) {
                sender.sendMessage(ChatColor.RED + "NOTE: " + ChatColor.YELLOW + "Has no effect unless you also set " + ChatColor.AQUA + "caster true");
            }
        }
        else if ((value == null || value.isEmpty()) && !baseParameters.contains(key))
        {
            spellKey = key;
            sender.sendMessage(ChatColor.DARK_PURPLE + "Set spell to: " + ChatColor.LIGHT_PURPLE + spellKey);
        }
        else
        {
            super.configure(sender, key, value);
        }
    }
}
