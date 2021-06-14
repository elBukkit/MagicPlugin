package com.elmakers.mine.bukkit.magic.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.item.ItemUpdatedCallback;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.item.ItemData;
import com.elmakers.mine.bukkit.utility.platform.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.platform.DeprecatedUtils;

public class MagicGiveCommandExecutor extends MagicTabExecutor {
    public MagicGiveCommandExecutor(MagicAPI api) {
        super(api, "mgive");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!api.hasPermission(sender, getPermissionNode()))
        {
            sendNoPermission(sender);
            return true;
        }

        if (args.length == 0 || args.length > 3)
        {
            sender.sendMessage("Usage: mgive [player] <item> [count]");
            return true;
        }

        String playerName = null;
        String itemName = null;
        String countString = null;

        if (args.length == 1) {
            itemName = args[0];
        } else if (args.length == 3) {
            playerName = args[0];
            itemName = args[1];
            countString = args[2];
        } else {
            playerName = args[0];
            Player testPlayer = DeprecatedUtils.getPlayer(playerName);
            if (testPlayer == null && !playerName.startsWith("@")) {
                itemName = args[0];
                countString = args[1];
                playerName = null;
            } else {
                itemName = args[1];
            }
        }

        int count = 1;
        if (countString != null) {
            try {
                count = Integer.parseInt(countString);
            } catch (Exception ex) {
                sender.sendMessage("Error parsing count: " + countString + ", should be an integer.");
                return true;
            }
        }

        if (!api.hasPermission(sender, "Magic.create." + itemName) && !api.hasPermission(sender, "Magic.create.*")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to create " + itemName);
            return true;
        }

        List<Player> players = new ArrayList<>();
        if (playerName != null && sender.hasPermission("Magic.commands.mgive.others")) {
            List<Entity> targets = CompatibilityUtils.selectEntities(sender, playerName);
            if (targets != null) {
                for (Entity entity : targets) {
                    if (entity instanceof Player) {
                        players.add((Player)entity);
                    }
                }
            } else {
                Player player = DeprecatedUtils.getPlayer(playerName);
                if (player == null) {
                    sender.sendMessage("No players matched: " + playerName);
                    return true;
                }
                players.add(player);
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Console usage: mgive <player> <item> [count]");
                return true;
            }
            players.add((Player)sender);
        }

        Set<String> customCosts = controller.getCurrencyKeys();
        for (Player player : players) {
            if (itemName.equalsIgnoreCase("xp")) {
                api.giveExperienceToPlayer(player, count);
                sender.sendMessage(ChatColor.AQUA + "Gave " + ChatColor.WHITE + count + ChatColor.AQUA + " experience to " + ChatColor.GOLD + player.getName());
            } else if (itemName.equalsIgnoreCase("sp")) {
                Mage mage = controller.getMage(player);
                mage.addSkillPoints(count);
                sender.sendMessage(ChatColor.AQUA + "Gave " + ChatColor.WHITE + count + ChatColor.AQUA + " skill points to " + ChatColor.GOLD + player.getName());
            } else if (customCosts.contains(itemName)) {
                Mage mage = controller.getMage(player);
                mage.addCurrency(itemName, count);
                sender.sendMessage(ChatColor.AQUA + "Gave " + ChatColor.WHITE + count + ChatColor.AQUA + " " + controller.getMessages().get("currency." + itemName + ".name", itemName)
                        + " to " + ChatColor.GOLD + player.getName());
            } else {
                final Mage mage = controller.getMage(player);
                final int itemCount = count;
                itemName = ItemData.cleanMinecraftItemName(itemName);
                final String itemKey = itemName;
                api.getController().createItem(itemName, mage, false, new ItemUpdatedCallback() {
                    @Override
                    public void updated(@Nullable ItemStack itemStack) {
                        if (itemStack == null) {
                            sender.sendMessage(ChatColor.RED + "Unknown item type " + ChatColor.DARK_RED + itemKey);
                            return;
                        }
                        itemStack.setAmount(itemCount);
                        String displayName = api.describeItem(itemStack);
                        sender.sendMessage(ChatColor.AQUA + "Gave " + ChatColor.WHITE + itemCount + " " + ChatColor.LIGHT_PURPLE + displayName + ChatColor.AQUA + " to " + ChatColor.GOLD + mage.getName());
                        mage.giveItem(itemStack, true, true);
                    }
                });
            }
        }

        return true;
    }

    @Override
    public Collection<String> onTabComplete(CommandSender sender, String commandName, String[] args) {
        Set<String> options = new HashSet<>();
        if (!sender.hasPermission("Magic.commands.mgive")) return options;

        if (args.length == 1 && sender.hasPermission("Magic.commands.mgive.others")) {
            options.addAll(api.getPlayerNames());
        }

        if (args.length == 1 || args.length == 2) {
            Collection<SpellTemplate> spellList = api.getSpellTemplates(sender.hasPermission("Magic.bypass_hidden"));
            for (SpellTemplate spell : spellList) {
                addIfPermissible(sender, options, "Magic.create.", spell.getKey());
            }
            Collection<String> allWands = api.getWandKeys();
            for (String wandKey : allWands) {
                addIfPermissible(sender, options, "Magic.create.", wandKey);
            }
            for (Material material : Material.values()) {
                addIfPermissible(sender, options, "Magic.create.", material.name().toLowerCase());
            }
            Collection<String> allItems = api.getController().getItemKeys();
            for (String itemKey : allItems) {
                addIfPermissible(sender, options, "Magic.create.", itemKey);
            }
            Collection<String> currencies = api.getController().getCurrencyKeys();
            for (String currency : currencies) {
                addIfPermissible(sender, options, "Magic.create.", currency);
            }
            Collection<String> recipes = controller.getRecipeKeys();
            for (String recipe : recipes) {
                addIfPermissible(sender, options, "Magic.create.", "recipe:" + recipe);
            }
            Collection<String> classKeys = controller.getMageClassKeys();
            for (String magicClass : classKeys) {
                addIfPermissible(sender, options, "Magic.create.", "recipes:" + magicClass);
            }
            Collection<String> mobKeys = controller.getMobKeys();
            for (String mobKey : mobKeys) {
                addIfPermissible(sender, options, "Magic.create.", "egg:" + mobKey);
            }
            for (EntityType entityType : EntityType.values()) {
                String mobKey = entityType.name().toLowerCase();
                addIfPermissible(sender, options, "Magic.create.", "egg:" + mobKey);
            }
            addIfPermissible(sender, options, "Magic.create.", "recipe:*");
            addIfPermissible(sender, options, "Magic.create.", "recipes:*");
            if (api.hasPermission(sender, "Magic.create.book")) {
                options.add("book:all");
                options.add("book:categories");
            }
        }
        return options;
    }
}
