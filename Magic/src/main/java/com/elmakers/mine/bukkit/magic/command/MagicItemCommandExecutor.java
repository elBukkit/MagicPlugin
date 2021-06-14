package com.elmakers.mine.bukkit.magic.command;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.integration.VaultController;
import com.elmakers.mine.bukkit.utility.Base64Coder;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.platform.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.platform.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.platform.ItemUtils;
import com.elmakers.mine.bukkit.utility.platform.NBTUtils;

public class MagicItemCommandExecutor extends MagicTabExecutor {

    public MagicItemCommandExecutor(MagicAPI api) {
        super(api, "mitem");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (args.length == 0) {
            return false;
        }
        if (args[0].equalsIgnoreCase("delete"))
        {
            if (!api.hasPermission(sender, "Magic.commands.mitem.delete")) {
                sendNoPermission(sender);
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage("Usage: /mitem delete <itemkey>");
                return true;
            }
            onItemDelete(sender, args[1]);
            return true;
        }

        if (args[0].equalsIgnoreCase("spawn"))
        {
            if (!api.hasPermission(sender, "Magic.commands.mitem.spawn")) {
                sendNoPermission(sender);
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage("Usage: /mitem spawn <itemkey>");
                return true;
            }
            String[] args2 = Arrays.copyOfRange(args, 1, args.length);
            onItemSpawn(sender, args2);
            return true;
        }

        String subCommand = args[0];
        String[] args2 = Arrays.copyOfRange(args, 1, args.length);
        Player player = sender instanceof Player ? (Player)sender : null;
        if (args2.length > 0) {
            Player findPlayer = DeprecatedUtils.getPlayer(args2[0]);
            if (findPlayer != null) {
                player = findPlayer;
                args2 = Arrays.copyOfRange(args2, 1, args2.length);
            }
        }

        // Everything beyond this point is is-game only and requires a sub-command
        if (player == null) {
            return false;
        }

        // All commands past here also require an item being held
        if (!checkItem(sender, player)) {
            return true;
        }
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        return processItemCommand(sender, player, itemInHand, subCommand, args2);
    }

    @Override
    public Collection<String> onTabComplete(CommandSender sender, String commandName, String[] args) {
        List<String> options = new ArrayList<>();

        if (args.length == 1)
        {
            addIfPermissible(sender, options, "Magic.commands.mitem.", "add");
            addIfPermissible(sender, options, "Magic.commands.mitem.", "remove");
            addIfPermissible(sender, options, "Magic.commands.mitem.", "name");
            addIfPermissible(sender, options, "Magic.commands.mitem.", "configure");
            addIfPermissible(sender, options, "Magic.commands.mitem.", "describe");
            addIfPermissible(sender, options, "Magic.commands.mitem.", "desc");
            addIfPermissible(sender, options, "Magic.commands.mitem.", "duplicate");
            addIfPermissible(sender, options, "Magic.commands.mitem.", "save");
            addIfPermissible(sender, options, "Magic.commands.mitem.", "delete");
            addIfPermissible(sender, options, "Magic.commands.mitem.", "destroy");
            addIfPermissible(sender, options, "Magic.commands.mitem.", "clean");
            addIfPermissible(sender, options, "Magic.commands.mitem.", "worth");
            addIfPermissible(sender, options, "Magic.commands.mitem.", "earns");
            addIfPermissible(sender, options, "Magic.commands.mitem.", "type");
            addIfPermissible(sender, options, "Magic.commands.mitem.", "damage");
            addIfPermissible(sender, options, "Magic.commands.mitem.", "skull");
            addIfPermissible(sender, options, "Magic.commands.mitem.", "spawn");
        }

        if (args.length == 2)
        {
            String subCommand = args[0];
            String subCommandPNode = "Magic.commands.mitem." + subCommand;

            if (!api.hasPermission(sender, subCommandPNode)) {
                return options;
            }

            if (subCommand.equalsIgnoreCase("add")) {
                options.add("enchant");
                options.add("attribute");
                options.add("lore");
                options.add("flag");
                options.add("unbreakable");
                options.add("unplaceable");
            }

            if (subCommand.equalsIgnoreCase("remove")) {
                options.add("enchant");
                options.add("attribute");
                options.add("lore");
                options.add("flag");
                options.add("unbreakable");
                options.add("unplaceable");
            }

            if (subCommand.equalsIgnoreCase("type")) {
                for (Material material : Material.values()) {
                    options.add(material.name().toLowerCase());
                }
            }

            if (subCommand.equalsIgnoreCase("spawn")) {
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
                addIfPermissible(sender, options, "Magic.create.", "sp");
            }

            if (subCommand.equalsIgnoreCase("damage")) {
                options.add("0");
                options.add("100");
            }

            if (subCommand.equalsIgnoreCase("delete")) {
                File itemFolder = new File(api.getController().getConfigFolder(), "items");
                if (itemFolder.exists()) {
                    File[] files = itemFolder.listFiles();
                    for (File file : files) {
                        if (file.getName().endsWith(".yml")) {
                            options.add(file.getName().replace(".yml", ""));
                        }
                    }
                }
            }
        }

        if (args.length == 3)
        {
            String subCommand = args[0];
            String subCommand2 = args[1];

            String commandPNode = "Magic.commands.mitem." + subCommand;

            if (!api.hasPermission(sender, commandPNode)) {
                return options;
            }
            boolean isAddRemove = subCommand.equalsIgnoreCase("remove") || subCommand.equalsIgnoreCase("add");
            if (isAddRemove && subCommand2.equalsIgnoreCase("enchant")) {
                for (Enchantment enchantment : Enchantment.values()) {
                    options.add(enchantment.getName().toLowerCase());
                }
            }
            if (isAddRemove && subCommand2.equalsIgnoreCase("attribute")) {
                for (Attribute attribute : Attribute.values()) {
                    options.add(attribute.name().toLowerCase());
                }
            }
            if (isAddRemove && subCommand2.equalsIgnoreCase("flag")) {
                for (ItemFlag flag : ItemFlag.values()) {
                    options.add(flag.name().toLowerCase());
                }
            }
            if (subCommand.equalsIgnoreCase("remove") && subCommand2.equalsIgnoreCase("lore")) {
                options.add("1");
                options.add("2");
                options.add("3");
            }
        }

        if (args.length == 5)
        {
            String subCommand = args[0];
            String subCommand2 = args[1];
            if (subCommand.equalsIgnoreCase("add") && subCommand2.equalsIgnoreCase("attribute")) {
                options.add("mainhand");
                options.add("offhand");
                options.add("feet");
                options.add("legs");
                options.add("chest");
                options.add("head");
            }
        }

        if (args.length == 6)
        {
            String subCommand = args[0];
            String subCommand2 = args[1];
            if (subCommand.equalsIgnoreCase("add") && subCommand2.equalsIgnoreCase("attribute")) {
                for (AttributeModifier.Operation operation : AttributeModifier.Operation.values()) {
                    options.add(operation.name().toLowerCase());
                }
            }
        }

        return options;
    }

    protected boolean processItemCommand(CommandSender sender, Player player, ItemStack item, String subCommand, String[] args)
    {
        if (!api.hasPermission(sender, "Magic.commands.mitem." + subCommand)) {
            sendNoPermission(sender);
            return true;
        }
        if (subCommand.equalsIgnoreCase("add"))
        {
            return onItemAdd(sender, player, item, args);
        }
        else if (subCommand.equalsIgnoreCase("remove"))
        {
            return onItemRemove(sender, player, item, args);
        }
        else if (subCommand.equalsIgnoreCase("destroy"))
        {
            return onItemDestroy(sender, player);
        }
        else if (subCommand.equalsIgnoreCase("clean"))
        {
            return onItemClean(sender, player);
        }
        else if (subCommand.equalsIgnoreCase("worth"))
        {
            return onItemWorth(sender, player, item);
        }
        else if (subCommand.equalsIgnoreCase("earns"))
        {
            return onItemEarns(sender, player, item);
        }
        else if (subCommand.equalsIgnoreCase("type"))
        {
            return onItemType(sender, player, item, args);
        }
        else if (subCommand.equalsIgnoreCase("damage"))
        {
            return onItemDurability(sender, player, item, args);
        }
        else if (subCommand.equalsIgnoreCase("duplicate"))
        {
            return onItemDuplicate(sender, player, item);
        }
        else if (subCommand.equalsIgnoreCase("save"))
        {
            return onItemSave(sender, player, item, args);
        }
        else if (subCommand.equalsIgnoreCase("describe") || subCommand.equalsIgnoreCase("desc"))
        {
            return onItemDescribe(sender, player, item, args);
        }
        else if (subCommand.equalsIgnoreCase("configure"))
        {
            return onItemConfigure(sender, player, item, args);
        }
        else if (subCommand.equalsIgnoreCase("name"))
        {
            return onItemName(sender, player, item, args);
        }
        else if (subCommand.equalsIgnoreCase("export"))
        {
            return onItemExport(sender, player, item, args);
        }
        else if (subCommand.equalsIgnoreCase("skull"))
        {
            return onItemSkull(sender, player, item);
        }

        return false;
    }

    public boolean onItemConfigure(CommandSender sender, Player player, ItemStack item, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /mitem configure <key> [value]");
            return true;
        }

        String tag = args[0];
        String[] path = StringUtils.split(tag, '.');
        ItemStack newItem = ItemUtils.makeReal(item);
        Object node = ItemUtils.getTag(item);
        if (args.length == 1) {
            int i = 0;
            while (node != null && i < path.length - 1) {
                node = NBTUtils.getNode(node, path[i]);
                i++;
            }
            if (node == null) {
                sender.sendMessage(ChatColor.RED + "Item does not have path: " + ChatColor.DARK_RED + tag);
                return true;
            }
            if (!NBTUtils.containsNode(node, path[path.length - 1])) {
                sender.sendMessage(ChatColor.RED + "Item does not have tag: " + ChatColor.DARK_RED + tag);
                return true;
            }
            NBTUtils.removeMeta(node, path[path.length - 1]);
            item.setItemMeta(newItem.getItemMeta());
            sender.sendMessage(ChatColor.GREEN + "Removed: " + ChatColor.DARK_GREEN + tag);
            return true;
        }
        String value = ChatColor.translateAlternateColorCodes('&', args[1]);
        for (int i = 0; i < path.length; i++) {
            String key = path[i];
            if (node == null) {
                sender.sendMessage(ChatColor.RED + "Failed to set item data");
                return true;
            }
            if (i < path.length - 1) {
                node = NBTUtils.createNode(node, key);
            } else {
                NBTUtils.setMetaTyped(node, key, value);
            }
        }
        item.setItemMeta(newItem.getItemMeta());
        sender.sendMessage(ChatColor.GREEN + "Set: " + ChatColor.DARK_GREEN + tag + " to " + ChatColor.AQUA + value);
        return true;
    }

    public boolean onItemDescribe(CommandSender sender, Player player, ItemStack item, String[] args) {
        MaterialAndData material = new MaterialAndData(item);
        sender.sendMessage(ChatColor.GOLD + material.getKey());
        YamlConfiguration configuration = new YamlConfiguration();
        if (ConfigurationUtils.loadAllTagsFromNBT(configuration, item)) {
            String itemString = null;
            if (args.length > 0) {
                Object target = configuration.get(args[0]);
                if (target == null) {
                    itemString = ChatColor.AQUA + args[0] + ChatColor.GRAY + ": " + ChatColor.RED + "(Not Set)";
                } else  {
                    configuration = new YamlConfiguration();
                    configuration.set(args[0], target);
                    itemString = configuration.saveToString().replace(ChatColor.COLOR_CHAR, '&');
                }
            } else {
                itemString = configuration.saveToString().replace(ChatColor.COLOR_CHAR, '&');
            }
            sender.sendMessage(itemString);
        }
        if (args.length == 0) {
            ItemData itemData = api.getController().getItem(item);
            if (itemData != null) {
                sender.sendMessage(ChatColor.AQUA + "Give with: " + ChatColor.GRAY + "/mgive " + ChatColor.YELLOW + itemData.getKey());
                double worth = itemData.getWorth();
                if (worth > 0) {
                    double earns = itemData.getEarns();
                    String message = ChatColor.AQUA + " Worth " + ChatColor.GREEN + worth;
                    if (earns != worth) {
                        message = message + " " + ChatColor.GRAY + "(" + ChatColor.DARK_GREEN + earns + ChatColor.DARK_AQUA + " when selling" + ChatColor.GRAY + ")";
                    }
                    sender.sendMessage(message);
                }
            }
        }
        return true;
    }

    public boolean onItemExport(CommandSender sender, Player player, ItemStack item, String[] parameters) {
        if (parameters.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /mitem export filename");
            return true;
        }
        PlayerInventory inventory = player.getInventory();
        int itemSlot = inventory.getHeldItemSlot();
        Map<String, MaterialAndData> items = new TreeMap<>();

        for (Material material : Material.values()) {
            ItemStack testItem = new ItemStack(material, 1);
            inventory.setItem(itemSlot, testItem);
            ItemStack setItem = inventory.getItem(itemSlot);
            if (setItem == null || setItem.getType() != testItem.getType()) {
                sender.sendMessage("Skipped: " + material.name());
                continue;
            }

            MaterialAndData mat = new MaterialAndData(material);
            items.put(mat.getKey(), mat);

            String baseName = mat.getName(controller.getMessages());
            for (short data = 1; data < 32; data++) {
                testItem = new ItemStack(material, 1, data);
                inventory.setItem(itemSlot, testItem);
                setItem = inventory.getItem(itemSlot);
                if (setItem == null || setItem.getType() != testItem.getType() || setItem.getDurability() != testItem.getDurability()) break;

                mat = new MaterialAndData(material, data);
                if (mat.getName().equals(baseName)) break;
                items.put(mat.getKey(), mat);
            }
        }

        File file = new File(api.getPlugin().getDataFolder(), parameters[0] + ".csv");
        try (Writer output = new OutputStreamWriter(new FileOutputStream(file), "UTF-8")) {
            output.append("Name,Key,Cost,Earns\n");

            for (MaterialAndData material : items.values()) {
                Double worth = api.getController().getWorth(material.getItemStack(1));
                String worthString = worth == null ? "" : worth.toString();
                Double earns = api.getController().getEarns(material.getItemStack(1));
                String earnsString = earns == null ? "" : earns.toString();
                output.append(material.getName() + "," + material.getKey() + "," + worthString + "," + earnsString + "\n");
            }
        } catch (Exception ex) {
            sender.sendMessage(ChatColor.RED + "Error exporting data: " + ex.getMessage());
            ex.printStackTrace();
        }
        inventory.setItem(itemSlot, item);
        return true;
    }

    public boolean onItemSerialize(CommandSender sender, Player player, ItemStack item) {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.set("item", item);
        String itemString = configuration.saveToString().replace("item:", "").replace(ChatColor.COLOR_CHAR, '&');
        sender.sendMessage(itemString);
        return true;
    }

    public boolean onItemWorth(CommandSender sender, Player player, ItemStack item) {
        MageController controller = api.getController();
        Double worth = controller.getWorth(item);
        if (worth == null) {
            sender.sendMessage(ChatColor.RED + "No worth defined for that item");
            return true;
        }
        String worthDescription = null;
        int amount = item.getAmount();
        double totalWorth = worth * amount;
        if (VaultController.hasEconomy()) {
            VaultController vault = VaultController.getInstance();
            worthDescription = vault.format(totalWorth);
            if (amount > 1) {
                worthDescription = worthDescription + ChatColor.WHITE
                        + " (" + ChatColor.GOLD + vault.format(worth) + ChatColor.WHITE + " each)";
            }
        } else {
            worthDescription = Double.toString(totalWorth);
            if (amount > 1) {
                worthDescription = worthDescription + ChatColor.WHITE
                        + " (" + ChatColor.GOLD + Double.toString(worth) + ChatColor.WHITE + " each)";
            }
        }

        sender.sendMessage("That item is worth " + ChatColor.GOLD + worthDescription);
        return true;
    }

    public boolean onItemEarns(CommandSender sender, Player player, ItemStack item) {
        MageController controller = api.getController();
        Double earns = controller.getEarns(item);
        if (earns == null) {
            sender.sendMessage(ChatColor.RED + "No earns defined for that item");
            return true;
        }
        String earnsDescription = null;
        int amount = item.getAmount();
        double totalWorth = earns * amount;
        if (VaultController.hasEconomy()) {
            VaultController vault = VaultController.getInstance();
            earnsDescription = vault.format(totalWorth);
            if (amount > 1) {
                earnsDescription = earnsDescription + ChatColor.WHITE
                        + " (" + ChatColor.GOLD + vault.format(earns) + ChatColor.WHITE + " each)";
            }
        } else {
            earnsDescription = Double.toString(totalWorth);
            if (amount > 1) {
                earnsDescription = earnsDescription + ChatColor.WHITE
                        + " (" + ChatColor.GOLD + Double.toString(earns) + ChatColor.WHITE + " each)";
            }
        }

        sender.sendMessage("That item can be sold for " + ChatColor.GOLD + earnsDescription);
        return true;
    }

    public boolean onItemDuplicate(CommandSender sender, Player player, ItemStack item)
    {
        ItemStack newItem = ItemUtils.getCopy(item);
        api.giveItemToPlayer(player, newItem);

        sender.sendMessage(api.getMessages().get("item.duplicated"));
        return true;
    }

    public boolean onItemSkull(CommandSender sender, Player player, ItemStack item)
    {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !(meta instanceof BookMeta)) {
            sender.sendMessage(api.getMessages().get("item.skull_no_book"));
            return true;
        }

        BookMeta bookMeta = (BookMeta)meta;
        List<String> pages = bookMeta.getPages();
        if (pages.isEmpty()) {
            sender.sendMessage(api.getMessages().get("item.skull_invalid_book"));
            return true;
        }

        ItemStack skullItem;
        String pageText = pages.get(0);
        try {
            String decoded = Base64Coder.decodeString(pageText);
            if (decoded == null || decoded.isEmpty()) {
                sender.sendMessage(api.getMessages().get("item.skull_invalid_book"));
                return true;
            }
            String url = decoded.replace("\"", "").replace("{textures:{SKIN:{url:", "").replace("}}}", "").trim();
            skullItem = controller.getURLSkull(url);
            if (ItemUtils.isEmpty(skullItem)) {
                sender.sendMessage(api.getMessages().get("item.skull_invalid_book"));
                return true;
            }
        } catch (Exception ex) {
            sender.sendMessage(api.getMessages().get("item.skull_invalid_book"));
            return true;
        }
        if (pages.size() > 1) {
            String secondPageText = pages.get(1);
            secondPageText = secondPageText.replace(ChatColor.COLOR_CHAR + "0", "");
            String[] pieces = StringUtils.split(secondPageText, '\n');
            if (pieces.length > 0) {
                ItemMeta skullMeta = skullItem.getItemMeta();
                skullMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', pieces[0]));
                if (pieces.length > 1) {
                    List<String> lore = new ArrayList<>();
                    for (int i = 1; i < pieces.length; i++) {
                        lore.add(ChatColor.translateAlternateColorCodes('&', pieces[i]));
                    }
                    skullMeta.setLore(lore);
                }
                skullItem.setItemMeta(skullMeta);
            }
        }
        sender.sendMessage(api.getMessages().get("item.skull"));
        player.getInventory().setItemInMainHand(skullItem);

        return true;
    }

    protected boolean checkItem(CommandSender sender, Player player)
    {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand == null || itemInHand.getType() == Material.AIR) {
            sender.sendMessage(api.getMessages().get("item.no_item"));
            return false;
        }
        return true;
    }

    public boolean onItemSpawn(CommandSender sender, String[] args) {
        if (!(sender instanceof Player) && args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: " + ChatColor.WHITE + "mitem spawn <item> <x> <y> <z> <world>");
            return true;
        }
        String itemType = args[0];
        ItemStack item = controller.createItem(itemType);
        if (item == null) {
            sender.sendMessage(ChatColor.RED + "Invalid item: " + ChatColor.WHITE + itemType);
            return true;
        }
        Location location = null;
        if (args.length == 2) {
            Entity target = CompatibilityUtils.getEntity(UUID.fromString(args[1]));
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Could not find entity with UUID: " + ChatColor.WHITE + args[1]);
                return true;
            }
            location = target.getLocation();
        } else {
            if (sender instanceof Player) {
                location = ((Player)sender).getLocation();
                if (args.length > 4) {
                    location.setWorld(Bukkit.getWorld(args[4]));
                }
            } else {
                location = new Location(Bukkit.getWorld(args[4]), 0.0f, 0.0f, 0.0f);
            }
            if (args.length > 4 && location.getWorld() == null) {
                sender.sendMessage(ChatColor.RED + "Invalid world: " + ChatColor.WHITE + args[4]);
                return true;
            }
            try {
                if (args.length > 1) {
                    location.setX(Double.parseDouble(args[1]));
                    if (args.length > 2) {
                        location.setY(Double.parseDouble(args[2]));
                        if (args.length > 3) {
                            location.setZ(Double.parseDouble(args[3]));
                        }
                    }
                }
            } catch (Exception ex) {
                sender.sendMessage(ChatColor.RED + "Usage: " + ChatColor.WHITE + "mitem spawn <item> <x> <y> <z> <world>");
                return true;
            }
        }

        location.getWorld().dropItem(location, item);
        return true;
    }

    public boolean onItemDelete(CommandSender sender, String itemKey) {
        MageController controller = api.getController();
        ItemData existing = controller.getItem(itemKey);
        if (existing == null) {
            sender.sendMessage(ChatColor.RED + "Unknown item: " + itemKey);
            return true;
        }
        boolean hasPermission = true;
        if (sender instanceof Player) {
            Player player = (Player)sender;
            if (!player.hasPermission("Magic.item.overwrite")) {
                if (player.hasPermission("Magic.item.overwrite_own")) {
                    String creatorId = existing.getCreatorId();
                    hasPermission = creatorId != null && creatorId.equalsIgnoreCase(player.getUniqueId().toString());
                } else {
                    hasPermission = false;
                }
            }
        }
        if (!hasPermission) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to delete " + itemKey);
            return true;
        }

        File itemFolder = new File(controller.getConfigFolder(), "items");
        File itemFile = new File(itemFolder, itemKey + ".yml");
        if (!itemFile.exists()) {
            sender.sendMessage(ChatColor.RED + "File doesn't exist: " + itemFile.getName());
            return true;
        }
        itemFile.delete();
        controller.unloadItemTemplate(itemKey);
        sender.sendMessage("Deleted item " + itemKey);
        return true;
    }

    public boolean onItemSave(CommandSender sender, Player player, ItemStack item, String[] parameters)
    {
        if (parameters.length < 1) {
            sender.sendMessage("Use: /mitem save <filename> [worth] [earns]");
            return true;
        }

        MageController controller = api.getController();
        String template = parameters[0];
        ItemData existing = controller.getItem(template);
        String creatorId = existing == null ? null : existing.getCreatorId();
        if (creatorId != null && !player.hasPermission("Magic.item.overwrite")) {
            boolean isCreator = creatorId.equalsIgnoreCase(player.getUniqueId().toString());
            if (!player.hasPermission("Magic.item.overwrite_own") || !isCreator) {
                sender.sendMessage(ChatColor.RED + "The " + template + " item already exists and you don't have permission (Magic.item.overwrite) to overwrite it.");
                return true;
            }
        }

        double worth = 0;
        if (parameters.length > 1) {
            try {
                worth = Double.parseDouble(parameters[1]);
            } catch (Exception ex) {
                sender.sendMessage("Invalid worth, expecting a number but got: " + parameters[1]);
                sender.sendMessage("Use: /mitem save <filename> [worth]");
                return true;
            }
        } else if (existing != null) {
            worth = existing.getWorth();
        }

        // We always save items with a quantity of 1!
        item.setAmount(1);

        YamlConfiguration itemConfig = new YamlConfiguration();
        ConfigurationSection itemSection = itemConfig.createSection(template);
        itemSection.set("creator_id", player.getUniqueId().toString());
        itemSection.set("creator", player.getName());
        itemSection.set("worth", worth);

        Double earns = null;
        if (parameters.length > 2) {
            try {
                earns = Double.parseDouble(parameters[2]);
                itemSection.set("earns", earns);
            } catch (Exception ex) {
                sender.sendMessage("Invalid earns, expecting a number but got: " + parameters[2]);
                sender.sendMessage("Use: /mitem save <filename> [worth] [earns]");
                return true;
            }
        } else if (existing != null && existing.hasCustomEarns()) {
            itemSection.set("earns", existing.getEarns());
        }

        itemSection.set("item", item);

        File itemFolder = new File(controller.getConfigFolder(), "items");
        File itemFile = new File(itemFolder, template + ".yml");
        itemFolder.mkdirs();
        try {
            itemConfig.save(itemFile);
        } catch (IOException ex) {
            ex.printStackTrace();
            sender.sendMessage(ChatColor.RED + "Can't write to file " + itemFile.getName());
            return true;
        }
        controller.loadItemTemplate(template, itemSection);
        String message = ChatColor.WHITE + "Item saved as " + ChatColor.GOLD + template + " worth " + ChatColor.GREEN + worth;
        if (earns != null) {
            message = message + " " + ChatColor.GRAY + "(" + ChatColor.DARK_GREEN + earns + ChatColor.DARK_AQUA + " when selling" + ChatColor.GRAY + ")";
        }
        sender.sendMessage(message);
        if (existing != null) {
            message = ChatColor.YELLOW + " Replaced Worth " + ChatColor.DARK_GREEN + existing.getWorth();
            if (existing.hasCustomEarns()) {
                message = message + " " + ChatColor.GRAY + "(" + ChatColor.DARK_GREEN + existing.getEarns() + ChatColor.DARK_AQUA + " when selling" + ChatColor.GRAY + ")";
            }
            sender.sendMessage(message);
        }
        return true;
    }

    public boolean onItemName(CommandSender sender, Player player, ItemStack item, String[] parameters)
    {
        String displayName = null;
        if (parameters.length < 1) {
            sender.sendMessage(api.getMessages().get("item.rename_clear"));
        } else {
            displayName = ChatColor.translateAlternateColorCodes('&', StringUtils.join(parameters, " "));
            sender.sendMessage(api.getMessages().get("item.renamed"));
        }

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        item.setItemMeta(meta);
        return true;
    }

    public boolean onItemAddFlag(CommandSender sender, Player player, ItemStack item, String flagName)
    {
        ItemFlag flag = null;
        try {
            flag = ItemFlag.valueOf(flagName.toUpperCase());
        } catch (Exception ex) {
            sender.sendMessage(ChatColor.RED + "Invalid flag: " + ChatColor.WHITE + flagName);
            return true;
        }

        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.addItemFlags(flag);
        item.setItemMeta(itemMeta);

        sender.sendMessage(api.getMessages().get("item.flag_added").replace("$flag", flag.name()));

        return true;
    }

    public boolean onItemRemoveFlag(CommandSender sender, Player player, ItemStack item, String flagName)
    {
        ItemFlag flag = null;
        ItemMeta itemMeta = item.getItemMeta();
        if (flagName == null) {
            Set<ItemFlag> flags = itemMeta.getItemFlags();
            if (flags == null || flags.size() == 0) {
                sender.sendMessage(api.getMessages().get("item.no_flags"));
                return true;
            }
            flag = flags.iterator().next();
        } else {
            try {
                flag = ItemFlag.valueOf(flagName.toUpperCase());
            } catch (Exception ex) {
                sender.sendMessage(ChatColor.RED + "Invalid flag: " + ChatColor.WHITE + flagName);
                return true;
            }
        }

        if (!itemMeta.hasItemFlag(flag)) {
            sender.sendMessage(api.getMessages().get("item.no_flag").replace("$flag", flag.name()));
        } else {
            itemMeta.removeItemFlags(flag);
            item.setItemMeta(itemMeta);
            sender.sendMessage(api.getMessages().get("item.flag_removed").replace("$flag", flag.name()));
        }

        return true;
    }

    public boolean onItemAddEnchant(CommandSender sender, Player player, ItemStack item, String enchantName, String enchantValue)
    {
        Enchantment enchantment = null;
        try {
            enchantment = Enchantment.getByName(enchantName.toUpperCase());
        } catch (Exception ex) {
            sender.sendMessage(ChatColor.RED + "Invalid enchantment: " + ChatColor.WHITE + enchantName);
            return true;
        }
        if (enchantment == null) {
            sender.sendMessage(ChatColor.RED + "Invalid enchantment: " + ChatColor.WHITE + enchantName);
            return true;
        }
        int level = 0;
        try {
            level = Integer.parseInt(enchantValue);
        } catch (Exception ex) {
            sender.sendMessage(ChatColor.RED + "Invalid enchantment level: " + ChatColor.WHITE + enchantValue);
            return true;
        }
        if (!player.hasPermission("Magic.item.enchant.extreme")) {
            if (level < 0 || level > 10) {
                sender.sendMessage(ChatColor.RED + "Invalid enchantment level: " + ChatColor.WHITE + enchantValue);
                return true;
            }
        }

        ItemMeta itemMeta = item.getItemMeta();
        boolean allowUnsafe = player.hasPermission("Magic.item.enchant.unsafe");
        if (itemMeta.addEnchant(enchantment, level, allowUnsafe)) {
            item.setItemMeta(itemMeta);
            sender.sendMessage(api.getMessages().get("item.enchant_added").replace("$enchant", enchantment.getName()));
        } else {
            if (!allowUnsafe && level > 5) {
                sender.sendMessage(api.getMessages().get("item.enchant_unsafe"));
            } else {
                sender.sendMessage(api.getMessages().get("item.enchant_not_added").replace("$enchant", enchantment.getName()));
            }
        }

        return true;
    }

    public boolean onItemRemoveEnchant(CommandSender sender, Player player, ItemStack item, String enchantName)
    {
        Enchantment enchantment = null;
        ItemMeta itemMeta = item.getItemMeta();
        if (enchantName == null) {
            Map<Enchantment, Integer> enchants = itemMeta.getEnchants();
            if (enchants == null || enchants.size() == 0) {
                sender.sendMessage(api.getMessages().get("item.no_enchants"));
                return true;
            }
            enchantment = enchants.keySet().iterator().next();
        } else {
            try {
                enchantment = Enchantment.getByName(enchantName.toUpperCase());
            } catch (Exception ex) {
                sender.sendMessage(ChatColor.RED + "Invalid enchantment: " + ChatColor.WHITE + enchantName);
                return true;
            }
            if (enchantment == null) {
                sender.sendMessage(ChatColor.RED + "Invalid enchantment: " + ChatColor.WHITE + enchantName);
                return true;
            }
        }

        if (!itemMeta.hasEnchant(enchantment)) {
            sender.sendMessage(api.getMessages().get("item.no_enchant").replace("$enchant", enchantment.getName()));
        } else {
            itemMeta.removeEnchant(enchantment);
            item.setItemMeta(itemMeta);
            sender.sendMessage(api.getMessages().get("item.enchant_removed").replace("$enchant", enchantment.getName()));
        }

        return true;
    }

    public boolean onItemAddAttribute(CommandSender sender, Player player, ItemStack item, String attributeName, String attributeValue, String attributeSlot, AttributeModifier.Operation operation)
    {
        Attribute attribute = null;
        if (attributeName == null) return false;

        try {
            attribute = Attribute.valueOf(attributeName.toUpperCase());
        } catch (Exception ex) {
            sender.sendMessage(ChatColor.RED + "Invalid attribute: " + ChatColor.WHITE + attributeName);
            return true;
        }

        double value = 0;
        try {
            value = Double.parseDouble(attributeValue);
        } catch (Exception ex) {
            sender.sendMessage(ChatColor.RED + "Invalid attribute value: " + ChatColor.WHITE + attributeValue);
            return true;
        }

        ItemStack newItem = ItemUtils.makeReal(item);
        CompatibilityUtils.removeItemAttribute(newItem, attribute);
        if (CompatibilityUtils.setItemAttribute(newItem, attribute, value, attributeSlot, operation.ordinal())) {
            if (attributeSlot == null) {
                attributeSlot = "(All Slots)";
            }

            item.setItemMeta(newItem.getItemMeta());
            sender.sendMessage(api.getMessages().get("item.attribute_added")
                    .replace("$attribute", attribute.name())
                    .replace("$value", Double.toString(value))
                    .replace("$slot", attributeSlot)
                    .replace("$operation", operation.name().toLowerCase()));
        } else {
            sender.sendMessage(api.getMessages().get("item.attribute_not_added").replace("$attribute", attribute.name()));
        }

        return true;
    }

    public boolean onItemRemoveAttribute(CommandSender sender, Player player, ItemStack item, String attributeName)
    {
        Attribute attribute = null;
        if (attributeName == null) return false;

        try {
            attribute = Attribute.valueOf(attributeName.toUpperCase());
        } catch (Exception ex) {
            sender.sendMessage(ChatColor.RED + "Invalid attribute: " + ChatColor.WHITE + attributeName);
            return true;
        }

        if (!CompatibilityUtils.removeItemAttribute(item, attribute)) {
            sender.sendMessage(api.getMessages().get("item.no_attribute").replace("$attribute", attribute.name()));
        } else {
            sender.sendMessage(api.getMessages().get("item.attribute_removed").replace("$attribute", attribute.name()));
        }

        return true;
    }

    public boolean onItemAddUnplaceable(CommandSender sender, Player player, ItemStack item)
    {
        if (ItemUtils.isUnplaceable(item)) {
            sender.sendMessage(api.getMessages().get("item.already_unplaceable"));
        } else {
            ItemStack newItem = ItemUtils.makeReal(item);
            ItemUtils.makeUnplaceable(newItem);
            item.setItemMeta(newItem.getItemMeta());
            sender.sendMessage(api.getMessages().get("item.add_unplaceable"));
        }

        return true;
    }

    public boolean onItemRemoveUnplaceable(CommandSender sender, Player player, ItemStack item)
    {
        if (!ItemUtils.isUnplaceable(item)) {
            sender.sendMessage(api.getMessages().get("item.not_unplaceable"));
        } else {
            ItemUtils.removeUnplaceable(item);
            sender.sendMessage(api.getMessages().get("item.remove_unplaceable"));
        }

        return true;
    }

    public boolean onItemAddUnbreakable(CommandSender sender, Player player, ItemStack item)
    {
        if (ItemUtils.isUnbreakable(item)) {
            sender.sendMessage(api.getMessages().get("item.already_unbreakable"));
        } else {
            // Need API!
            ItemStack newItem = ItemUtils.makeReal(item);
            ItemUtils.makeUnbreakable(newItem);
            item.setItemMeta(newItem.getItemMeta());
            sender.sendMessage(api.getMessages().get("item.add_unbreakable"));
        }

        return true;
    }

    public boolean onItemRemoveUnbreakable(CommandSender sender, Player player, ItemStack item)
    {
        if (!ItemUtils.isUnbreakable(item)) {
            sender.sendMessage(api.getMessages().get("item.not_unbreakable"));
        } else {
            ItemUtils.removeUnbreakable(item);
            sender.sendMessage(api.getMessages().get("item.remove_unbreakable"));
        }

        return true;
    }

    public boolean onItemAddLore(CommandSender sender, Player player, ItemStack item, String loreLine)
    {
        ItemMeta itemMeta = item.getItemMeta();
        List<String> lore = itemMeta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        lore.add(loreLine);
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);

        sender.sendMessage(api.getMessages().get("item.lore_added").replace("$lore", loreLine));
        return true;
    }

    public boolean onItemRemoveLore(CommandSender sender, Player player, ItemStack item, String loreIndex)
    {
        ItemMeta itemMeta = item.getItemMeta();
        List<String> lore = itemMeta.getLore();
        if (lore == null || lore.isEmpty()) {
            sender.sendMessage(api.getMessages().get("item.no_lore"));
            return true;
        }

        int index = 0;
        if (loreIndex != null) {
            try {
                index = Integer.parseInt(loreIndex);
            } catch (Exception ex) {
                sender.sendMessage(ChatColor.RED + "Invalid lore line: " + loreIndex);
                return true;
            }
        }

        if (index < 0 || index >= lore.size()) {
            sender.sendMessage(ChatColor.RED + "Invalid lore line: " + loreIndex);
            return true;
        }

        String line = lore.remove(index);
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        sender.sendMessage(api.getMessages().get("item.lore_removed").replace("$lore", line));
        return true;
    }

    public boolean onItemType(CommandSender sender, Player player, ItemStack item, String[] parameters)
    {
        if (parameters.length < 1) {
            return false;
        }
        String materialKey = parameters[0];
        MaterialAndData material = new MaterialAndData(materialKey);
        if (!material.isValid() || material.getMaterial() == Material.AIR) {
            sender.sendMessage(ChatColor.RED + "Invalid material key: " + ChatColor.DARK_RED + materialKey);
            return true;
        }
        material.applyToItem(item);
        return true;
    }

    public boolean onItemDurability(CommandSender sender, Player player, ItemStack item, String[] parameters)
    {
        if (parameters.length < 1) {
            return false;
        }
        short durability = 0;
        try {
            durability = (short)Integer.parseInt(parameters[0]);
        } catch (NumberFormatException ex) {
            sender.sendMessage("Invalid damage value: " + parameters[0]);
            return true;
        }
        item.setDurability(durability);
        return true;
    }

    public boolean onItemAdd(CommandSender sender, Player player, ItemStack item, String[] parameters)
    {
        if (parameters.length < 1) {
            return false;
        }
        String addCommand = parameters[0];
        if (addCommand.equalsIgnoreCase("unbreakable")) {
            return onItemAddUnbreakable(sender, player, item);
        }
        if (addCommand.equalsIgnoreCase("unplaceable")) {
            return onItemAddUnplaceable(sender, player, item);
        }
        if (parameters.length < 2) {
            return false;
        }
        if (addCommand.equalsIgnoreCase("flag")) {
            return onItemAddFlag(sender, player, item, parameters[1]);
        }

        if (addCommand.equalsIgnoreCase("lore")) {
            String[] loreLines = Arrays.copyOfRange(parameters, 1, parameters.length);
            String loreLine = ChatColor.translateAlternateColorCodes('&', StringUtils.join(loreLines, " "));
            return onItemAddLore(sender, player, item, loreLine);
        }
        if (addCommand.equalsIgnoreCase("enchant")) {
            return onItemAddEnchant(sender, player, item, parameters[1], parameters.length >= 3 ? parameters[2] : "1");
        }
        if (parameters.length < 3) {
            return false;
        }
        if (addCommand.equalsIgnoreCase("attribute")) {
            String slot = parameters.length > 3 ? parameters[3] : null;
            AttributeModifier.Operation operation = AttributeModifier.Operation.ADD_NUMBER;
            if (parameters.length > 4) {
                try {
                    operation = AttributeModifier.Operation.valueOf(parameters[4].toUpperCase());
                } catch (Exception ex) {
                    sender.sendMessage(ChatColor.RED + "Invalid operation: " + parameters[4]);
                }
            }
            return onItemAddAttribute(sender, player, item, parameters[1], parameters[2], slot, operation);
        }
        return false;
    }

    public boolean onItemDestroy(CommandSender sender, Player player)
    {
        player.getInventory().setItemInMainHand(null);
        sender.sendMessage(api.getMessages().get("item.destroyed"));
        return true;
    }

    public boolean onItemClean(CommandSender sender, Player player)
    {
        api.getController().cleanItem(player.getInventory().getItemInMainHand());
        sender.sendMessage(api.getMessages().get("item.cleaned"));
        return true;
    }

    public boolean onItemRemove(CommandSender sender, Player player, ItemStack item, String[] parameters)
    {
        if (parameters.length < 1) {
            return false;
        }
        String removeCommand = parameters[0];

        if (removeCommand.equalsIgnoreCase("unbreakable")) {
            return onItemRemoveUnbreakable(sender, player, item);
        }
        if (removeCommand.equalsIgnoreCase("unplaceable")) {
            return onItemRemoveUnplaceable(sender, player, item);
        }

        String firstParameter = parameters.length > 1 ? parameters[1] : null;
        if (removeCommand.equalsIgnoreCase("flag")) {
            return onItemRemoveFlag(sender, player, item, firstParameter);
        }
        if (removeCommand.equalsIgnoreCase("lore")) {
            return onItemRemoveLore(sender, player, item, firstParameter);
        }
        if (removeCommand.equalsIgnoreCase("enchant")) {
            return onItemRemoveEnchant(sender, player, item, firstParameter);
        }
        if (removeCommand.equalsIgnoreCase("attribute")) {
            return onItemRemoveAttribute(sender, player, item, firstParameter);
        }
        return false;
    }
}
