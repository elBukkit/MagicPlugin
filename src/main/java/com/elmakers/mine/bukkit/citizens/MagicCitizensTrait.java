package com.elmakers.mine.bukkit.citizens;

import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.magic.MagicPlugin;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;

public class MagicCitizensTrait extends Trait {

    private String spellKey;
    private boolean npcCaster = true;
    private String[] parameters = null;
    private MagicAPI api;

	public MagicCitizensTrait() {
		super("magic");
	}

	public void load(DataKey data) {
        spellKey = data.getString("spell", null);
        npcCaster = data.getBoolean("caster", false);
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

        String[] parameters = this.parameters;
        Entity entity = null;
        CommandSender sender = null;
        if (npcCaster) {
            if (event.getNPC().isSpawned()) {
                entity = event.getNPC().getBukkitEntity();
                if (parameters == null) {
                    parameters = new String[2];
                } else {
                    parameters = new String[parameters.length + 2];
                    System.arraycopy(this.parameters, 0, parameters, 2, parameters.length);
                }
                parameters[0] = "player";
                parameters[1] = event.getClicker().getName();
            }
        } else {
            entity = event.getClicker();
            sender = event.getClicker();
        }

        api.cast(spellKey, parameters, sender, entity);
    }

    public void describe(CommandSender sender)
    {
        sender.sendMessage(ChatColor.AQUA + "Magic NPC: " + ChatColor.GOLD + npc.getName() +
                ChatColor.WHITE + "(" + ChatColor.GRAY + npc.getId() + ChatColor.WHITE + ")");
        String spellDescription = spellKey == null ? (ChatColor.RED + "(None)") : (ChatColor.LIGHT_PURPLE + spellKey);
        sender.sendMessage(ChatColor.DARK_PURPLE + "Spell: " + spellDescription);
        String parameterDescription = parameters == null ? (ChatColor.GRAY + "(None)") : (ChatColor.LIGHT_PURPLE + StringUtils.join(parameters, ""));
        sender.sendMessage(ChatColor.DARK_PURPLE + "Parameters: " + parameterDescription);
        String casterDescription = npcCaster ? (ChatColor.GRAY + "NPC") : (ChatColor.LIGHT_PURPLE + "Player");
        sender.sendMessage(ChatColor.DARK_PURPLE + "Caster: " + casterDescription);
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
        else
        {
            sender.sendMessage(ChatColor.RED + "Expecting: spell, parameters or caster");
        }
    }
}