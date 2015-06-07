package com.elmakers.mine.bukkit.citizens;

import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.integration.VaultController;
import com.elmakers.mine.bukkit.magic.MagicPlugin;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public abstract class CitizensTrait extends Trait {
    private String permissionNode;
    private boolean invisible = false;
    private double cost = 0;
    protected MagicAPI api;

    protected CitizensTrait(String name) {
        super(name);
    }

    public void load(DataKey data) {
        permissionNode = data.getString("permission", null);
        invisible = data.getBoolean("invisible", false);
        cost = data.getDouble("cost", 0);
    }

    public void save(DataKey data) {
        data.setString("permission", permissionNode);
        data.setBoolean("invisible", invisible);
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

    @Override
    public void onSpawn() {
        updatePotionEffects();
    }

    protected void updatePotionEffects() {
        Entity entity = null;
        try {
            entity = npc.getEntity();
        } catch (Exception ex) {

        }
        LivingEntity li = entity instanceof LivingEntity ? (LivingEntity)entity : null;
        if (li != null) {
            if (invisible) {
                li.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
            } else {
                li.removePotionEffect(PotionEffectType.INVISIBILITY);
            }
        }
    }

    public abstract boolean perform(net.citizensnpcs.api.event.NPCRightClickEvent event);

    @EventHandler
    public void onClick(net.citizensnpcs.api.event.NPCRightClickEvent event) {
        if (event.getNPC() != this.getNPC()) return;

        CommandSender sender = event.getClicker();
        Player player = event.getClicker();
        if (permissionNode != null && !player.hasPermission(permissionNode)) {
            return;
        }
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

        boolean result = perform(event);
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
        String permissionDescription = permissionNode == null ? (ChatColor.GRAY + "(None)") : (ChatColor.LIGHT_PURPLE + permissionNode);
        sender.sendMessage(ChatColor.DARK_PURPLE + "Permission: " + permissionDescription);
        String invisibleDescription = invisible ? (ChatColor.GREEN + "YES") : (ChatColor.GRAY + "NO");
        sender.sendMessage(ChatColor.DARK_PURPLE + "Invisible: " + invisibleDescription);
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
        else if (key.equalsIgnoreCase("invisible"))
        {
            if (value == null || !value.equalsIgnoreCase("true"))
            {
                sender.sendMessage(ChatColor.DARK_PURPLE + "Set NPC visible");
                invisible = false;
            }
            else
            {
                invisible = true;
                sender.sendMessage(ChatColor.DARK_PURPLE + "Set NPC invisible");
            }
            updatePotionEffects();
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
            sender.sendMessage(ChatColor.RED + "Not a valid configuration option- use the <tab> key for help");
        }
    }
}
