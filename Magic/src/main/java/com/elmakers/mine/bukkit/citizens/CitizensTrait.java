package com.elmakers.mine.bukkit.citizens;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.elmakers.mine.bukkit.api.entity.EntityData;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.integration.VaultController;
import com.elmakers.mine.bukkit.magic.MagicPlugin;

import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

public abstract class CitizensTrait extends Trait {
    private String permissionNode;
    private boolean invisible = false;
    private double cost = 0;
    private String mobKey;
    private ItemStack requireItem = null;
    private ItemStack hatItem;
    protected MagicAPI api;

    private static String[] _baseParameters = new String[] { "permission", "invisible", "cost", "mob", "requires", "hat" };
    protected static Set<String> baseParameters = new HashSet<String>(Arrays.asList(_baseParameters));

    protected CitizensTrait(String name) {
        super(name);
    }

    @Override
    public void load(DataKey data) {
        permissionNode = data.getString("permission", null);
        invisible = data.getBoolean("invisible", false);
        cost = data.getDouble("cost", 0);
        mobKey = data.getString("mob");
        String itemKey = data.getString("requires");
        if (itemKey != null && itemKey.length() > 0) {
            requireItem = api.createItem(itemKey);
        }
        String hatKey = data.getString("hat");
        if (hatKey != null && hatKey.length() > 0) {
            hatItem = api.createItem(hatKey);
        }
    }

    @Override
    public void save(DataKey data) {
        data.setString("permission", permissionNode);
        data.setBoolean("invisible", invisible);
        data.setDouble("cost", cost);
        if (requireItem != null) {
            data.setString("require", api.getItemKey(requireItem));
        }
        if (hatItem != null) {
            data.setString("hat", api.getItemKey(hatItem));
        }
        data.setString("mob", mobKey);
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
        updateEntity();
    }

    protected void updateEntity() {
        Entity entity = null;
        try {
            entity = npc.getEntity();
        } catch (Exception ignored) {
        }

        LivingEntity li = entity instanceof LivingEntity ? (LivingEntity)entity : null;
        if (li != null) {
            if (invisible) {
                li.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
            } else {
                li.removePotionEffect(PotionEffectType.INVISIBILITY);
            }
            if (hatItem != null) {
                li.getEquipment().setHelmet(hatItem);
            }
        }
        if (entity != null && mobKey != null) {
            MageController controller = api.getController();
            EntityData customEntity = controller.getMob(mobKey);
            if (customEntity != null) {
                customEntity.attach(controller, entity);
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

        if (requireItem != null && !api.hasItem(player, requireItem)) {
            sender.sendMessage(api.getMessages().get("economy.requires").replace("$cost", api.describeItem(requireItem)));
            return;
        }

        boolean result = perform(event);
        if (result && cost > 0) {
            VaultController vault = VaultController.getInstance();
            sender.sendMessage(api.getMessages().get("economy.deducted").replace("$cost", vault.format(cost)));
            vault.withdrawPlayer(player, cost);
        }
        if (result && requireItem != null) {
            sender.sendMessage(api.getMessages().get("economy.deducted").replace("$cost", api.describeItem(requireItem)));
            api.takeItem(player, requireItem);
        }
    }

    public void describe(CommandSender sender)
    {
        sender.sendMessage(ChatColor.AQUA + "Magic NPC: " + ChatColor.GOLD + npc.getName()
                + ChatColor.WHITE + "(" + ChatColor.GRAY + npc.getId() + ChatColor.WHITE + ")");
        String permissionDescription = permissionNode == null ? (ChatColor.GRAY + "(None)") : (ChatColor.LIGHT_PURPLE + permissionNode);
        sender.sendMessage(ChatColor.DARK_PURPLE + "Permission: " + permissionDescription);
        String invisibleDescription = invisible ? (ChatColor.GREEN + "YES") : (ChatColor.GRAY + "NO");
        sender.sendMessage(ChatColor.DARK_PURPLE + "Invisible: " + invisibleDescription);
        if (VaultController.hasEconomy()) {
            VaultController vault = VaultController.getInstance();
            sender.sendMessage(ChatColor.DARK_PURPLE + "Cost: " + ChatColor.GOLD + vault.format(cost));
        }
        if (requireItem != null) {
            sender.sendMessage(ChatColor.DARK_PURPLE + "Requires: " + ChatColor.GOLD + api.describeItem(requireItem));
        }
        if (hatItem != null) {
            sender.sendMessage(ChatColor.DARK_PURPLE + "Wearing Hat: " + ChatColor.GOLD + api.describeItem(hatItem));
        }
        if (mobKey != null) {
            sender.sendMessage(ChatColor.DARK_PURPLE + "Using mob profile: " + ChatColor.GOLD + mobKey);
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
            updateEntity();
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
        else if (key.equalsIgnoreCase("requires"))
        {
            if (value == null)
            {
                sender.sendMessage(ChatColor.DARK_PURPLE + "Cleared item requirement");
                cost = 0;
            }
            else
            {
                try {
                    requireItem = api.createItem(value);
                    sender.sendMessage(ChatColor.DARK_PURPLE + "Set item requirement to " + api.describeItem(requireItem));
                } catch (Exception ex) {
                    sender.sendMessage(ChatColor.RED + "Invalid item: " + value);
                }
            }
        }
        else if (key.equalsIgnoreCase("mob"))
        {
            MageController controller = api.getController();
            if (mobKey != null) {
                try {
                    Entity entity = npc.getEntity();
                    if (entity != null) {
                        Mage mage = controller.getRegisteredMage(entity);
                        if (mage != null) {
                            controller.removeMage(mage);
                        }
                    }
                } catch (Exception ignored) {
                }
            }
            if (value == null)
            {
                sender.sendMessage(ChatColor.DARK_PURPLE + "Cleared mob profile");
                mobKey = null;
            }
            else
            {
                EntityData customMob = controller.getMob(value);
                if (customMob != null) {
                    mobKey = value;
                    updateEntity();
                    sender.sendMessage(ChatColor.DARK_PURPLE + "Set mob to " + mobKey);
                } else {
                    sender.sendMessage(ChatColor.RED + "Invalid mob: " + value);
                }
            }
        }
        else if (key.equalsIgnoreCase("hat"))
        {
            hatItem = api.createItem(value);
            if (hatItem == null)
            {
                sender.sendMessage(ChatColor.DARK_PURPLE + "removed hat");
                Entity entity = null;
                try {
                    entity = npc.getEntity();
                } catch (Exception ignored) {
                }

                LivingEntity li = entity instanceof LivingEntity ? (LivingEntity)entity : null;
                if (li != null) {
                    li.getEquipment().setHelmet(hatItem);
                }
            }
            else
            {
                try {
                    hatItem = api.createItem(value);
                    sender.sendMessage(ChatColor.DARK_PURPLE + "Set hat to " + api.describeItem(hatItem));
                    updateEntity();
                } catch (Exception ex) {
                    sender.sendMessage(ChatColor.RED + "Invalid item: " + value);
                }
            }
        }
        else
        {
            sender.sendMessage(ChatColor.RED + "Not a valid configuration option- use the <tab> key for help");
        }
    }
}
