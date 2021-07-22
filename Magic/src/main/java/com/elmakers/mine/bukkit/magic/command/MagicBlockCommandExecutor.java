package com.elmakers.mine.bukkit.magic.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.block.magic.MagicBlock;
import com.elmakers.mine.bukkit.block.magic.MagicBlockTemplate;
import com.elmakers.mine.bukkit.block.magic.Nearby;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.TextUtils;
import com.google.common.collect.ImmutableSet;

public class MagicBlockCommandExecutor extends MagicTabExecutor {
    private final MagicController magicController;
    private final MagicBlockSelectionManager selections;

    private static final ImmutableSet<String> PROPERTY_KEYS = ImmutableSet.of(
        "name", "interval", "effects",
        "spawn.mobs", "spawn.probability", "spawn.player_range", "spawn.min_players",
        "spawn.limit", "spawn.limit_range", "spawn.vertical_range", "spawn.radius",
        "spawn.vertical_radius", "spawn.retries", "min_players", "player_range",
        "min_time", "max_time", "min_moon_phase", "max_moon_phase", "moon_phase",
        "cast.spells", "cast.recast", "cast.undo_all", "spawn.count", "spawn.leash",
        "spawn.interval", "spawn.parameters", "spawn.check_radius", "spawn.check_floor",
        "spawn.vertical_check_radius"
    );
    private static final ImmutableSet<String> IGNORE_PROPERTIES = ImmutableSet.of("name", "description");

    public MagicBlockCommandExecutor(MagicController controller) {
        super(controller.getAPI(), "mblock");
        selections = new MagicBlockSelectionManager(controller);
        this.magicController = controller;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!api.hasPermission(sender, getPermissionNode())) {
            sendNoPermission(sender);
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: mblock [add|select|remove|list|configure|describe|name]");
            return true;
        }

        String subCommand = args[0];
        args = Arrays.copyOfRange(args, 1, args.length);
        MagicBlock selection = selections.getSelected(sender);

        if (subCommand.equalsIgnoreCase("list")) {
            onListBlocks(sender, args);
            return true;
        }

        if (subCommand.equalsIgnoreCase("select")) {
            onSelectBlock(sender, args);
            return true;
        }

        if (subCommand.equalsIgnoreCase("remove")) {
            onRemoveBlock(sender, selection);
            return true;
        }

        if (subCommand.equalsIgnoreCase("debug")) {
            onDebugBlock(sender, selection, args);
            return true;
        }

        if (subCommand.equalsIgnoreCase("enable")) {
            onEnableBlock(sender, selection);
            return true;
        }

        if (subCommand.equalsIgnoreCase("disable")) {
            onDisableBlock(sender, selection);
            return true;
        }

        if (subCommand.equalsIgnoreCase("tp")) {
            onTeleportBlock(sender, selection);
            return true;
        }

        if (subCommand.equalsIgnoreCase("move") || subCommand.equalsIgnoreCase("tphere")) {
            onMoveBlock(sender, selection);
            return true;
        }

        if (subCommand.equalsIgnoreCase("describe") || subCommand.equalsIgnoreCase("desc")) {
            onDescribeBlock(sender, selection);
            return true;
        }

        if (subCommand.equalsIgnoreCase("configure")) {
            onConfigureBlock(sender, selection, args);
            return true;
        }

        if (subCommand.equalsIgnoreCase("name")) {
            onNameBlock(sender, selection, args);
            return true;
        }

        Player player = (sender instanceof Player) ? (Player)sender : null;
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "This command can only be used in-game");
            return true;
        }

        if (subCommand.equalsIgnoreCase("add")) {
            onMagicBlock(player, args);
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Usage: mblock [add|select|remove|list|configure|describe]");
        return true;
    }

    private void onListBlocks(CommandSender sender, String[] args) {
        selections.list(sender, args);
    }

    private void onMagicBlock(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: " + ChatColor.WHITE + "/mblock add <template>");
            return;
        }

        String key = args[0];
        if (!magicController.isMagicBlockTemplate(key)) {
            player.sendMessage(ChatColor.RED + "Invalid block template: " + ChatColor.DARK_RED + key);
            return;
        }

        Location location = player.getLocation();
        MagicBlock existing = magicController.getMagicBlockAt(location);
        if (existing != null) {
            player.sendMessage(ChatColor.RED + "Magic Block already exists: " + ChatColor.LIGHT_PURPLE + existing.getName()
                + ChatColor.RED + " at " + TextUtils.printLocation(existing.getLocation(), 0));
            return;
        }

        ConfigurationSection parameters = null;
        if (args.length > 1) {
            String[] parameterArgs = Arrays.copyOfRange(args, 1, args.length);
            parameters = ConfigurationUtils.newConfigurationSection();
            ConfigurationUtils.addParameters(parameterArgs, parameters);
        }
        MagicBlock magicBlock = new MagicBlock(magicController, location, key, player.getUniqueId().toString(), player.getName(), parameters);
        magicController.registerMagicBlock(magicBlock);

        playEffects(player, magicBlock, "blockselect");
        selections.setSelection(player, magicBlock);

        player.sendMessage(ChatColor.AQUA + "Created Magic Block: " + ChatColor.LIGHT_PURPLE + magicBlock.getName()
            + ChatColor.AQUA + " at " + TextUtils.printLocation(magicBlock.getLocation(), 0));
    }

    private void playEffects(CommandSender sender, MagicBlock magicBlock, String effectsKey) {
        selections.playEffects(sender, magicBlock, effectsKey);
    }

    private void onRemoveBlock(CommandSender sender, MagicBlock magicBlock) {
        if (magicBlock == null) {
            sender.sendMessage(ChatColor.RED + "No magic block selected, use " + ChatColor.WHITE + "/mblock select");
            return;
        }
        if (!magicController.unregisterMagicBlock(magicBlock)) {
            sender.sendMessage(ChatColor.RED + "Could not find magic block at given position (something went wrong)");
            return;
        }

        Location location = magicBlock.getLocation();
        selections.clearSelection(sender);

        playEffects(sender, magicBlock, "blockremove");
        String rangeMessage = selections.getDistanceMessage(sender, magicBlock);
        String message = ChatColor.YELLOW + "Removed " + ChatColor.LIGHT_PURPLE + magicBlock.getName()
            + ChatColor.YELLOW + " at " + TextUtils.printLocation(location, 0);
        if (rangeMessage != null) {
            message += rangeMessage;
        }
        sender.sendMessage(message);
    }

    private void onDebugBlock(CommandSender sender, MagicBlock magicBlock, String[] args) {
        if (magicBlock == null) {
            sender.sendMessage(ChatColor.RED + "No magic block selected, use " + ChatColor.WHITE + "/mblock select");
            return;
        }
        int level = 1;
        if (args.length > 0) {
            try {
                level = Integer.parseInt(args[0]);
            } catch (Exception ex) {
                sender.sendMessage(ChatColor.RED + "Expected debug level, got: " + ChatColor.WHITE + args[0]);
                return;
            }
        }

        Mage mage = magicBlock.getMage();

        if (level == 0 || (mage.getDebugLevel() > 0 && level == 1)) {
            mage.setDebugLevel(0);
            mage.setDebugger(null);
            sender.sendMessage(ChatColor.GOLD + "Disabling debug for " + ChatColor.AQUA + magicBlock.getName());
            return;
        }

        mage.setDebugLevel(level);
        mage.setDebugger(sender);

        Location location = magicBlock.getLocation();
        playEffects(sender, magicBlock, "blockdebug");
        String rangeMessage = selections.getDistanceMessage(sender, magicBlock);
        String message = ChatColor.YELLOW + "Debugging " + ChatColor.LIGHT_PURPLE + magicBlock.getName()
            + ChatColor.YELLOW + " at " + TextUtils.printLocation(location, 0);
        if (rangeMessage != null) {
            message += rangeMessage;
        }
        sender.sendMessage(message);
    }

    private void onEnableBlock(CommandSender sender, MagicBlock magicBlock) {
        if (magicBlock == null) {
            sender.sendMessage(ChatColor.RED + "No magic block selected, use " + ChatColor.WHITE + "/mblock select");
            return;
        }
        if (magicBlock.isEnabled()) {
            sender.sendMessage(ChatColor.RED + "Block is already enabled");
            return;
        }
        magicBlock.enable();
        Location location = magicBlock.getLocation();
        playEffects(sender, magicBlock, "blockselect");
        String rangeMessage = selections.getDistanceMessage(sender, magicBlock);
        String message = ChatColor.YELLOW + "Enabled " + ChatColor.LIGHT_PURPLE + magicBlock.getName()
            + ChatColor.YELLOW + " at " + TextUtils.printLocation(location, 0);
        if (rangeMessage != null) {
            message += rangeMessage;
        }
        sender.sendMessage(message);
    }

    private void onDisableBlock(CommandSender sender, MagicBlock magicBlock) {
        if (magicBlock == null) {
            sender.sendMessage(ChatColor.RED + "No magic block selected, use " + ChatColor.WHITE + "/mblock select");
            return;
        }
        if (!magicBlock.isEnabled()) {
            sender.sendMessage(ChatColor.RED + "Block is already disabled");
            return;
        }
        magicBlock.disable();
        Location location = magicBlock.getLocation();
        playEffects(sender, magicBlock, "blockremove");
        String rangeMessage = selections.getDistanceMessage(sender, magicBlock);
        String message = ChatColor.YELLOW + "Disabled " + ChatColor.LIGHT_PURPLE + magicBlock.getName()
            + ChatColor.YELLOW + " at " + TextUtils.printLocation(location, 0);
        if (rangeMessage != null) {
            message += rangeMessage;
        }
        sender.sendMessage(message);
    }

    private void onTeleportBlock(CommandSender sender, MagicBlock magicBlock) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command may only be used in-game");
            return;
        }
        if (magicBlock == null) {
            sender.sendMessage(ChatColor.RED + "No magic block selected, use " + ChatColor.WHITE + "/mblock select");
            return;
        }

        Player player = (Player)sender;
        Location location = magicBlock.getLocation();
        player.teleport(magicBlock.getLocation());
        String message = ChatColor.YELLOW + "Teleported you to " + ChatColor.LIGHT_PURPLE + magicBlock.getName()
            + ChatColor.YELLOW + " at " + TextUtils.printLocation(location, 0);
        sender.sendMessage(message);
    }

    private void onMoveBlock(CommandSender sender, MagicBlock magicBlock) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command may only be used in-game");
            return;
        }
        if (magicBlock == null) {
            sender.sendMessage(ChatColor.RED + "No magic block selected, use " + ChatColor.WHITE + "/mblock select");
            return;
        }

        Player player = (Player)sender;
        Location location = player.getLocation();
        MagicBlock existing = magicController.getMagicBlockAt(location);
        if (existing != null) {
            player.sendMessage(ChatColor.RED + "Magic Block already exists: " + ChatColor.LIGHT_PURPLE + existing.getName()
                + ChatColor.RED + " at " + TextUtils.printLocation(existing.getLocation(), 0));
            return;
        }

        magicController.moveMagicBlock(magicBlock, location);
        String message = ChatColor.YELLOW + "Moved " + ChatColor.LIGHT_PURPLE + magicBlock.getName()
            + ChatColor.YELLOW + " to " + TextUtils.printLocation(location, 0);
        sender.sendMessage(message);
    }

    private void onDescribeBlock(CommandSender sender, MagicBlock magicBlock) {
        if (magicBlock == null) {
            sender.sendMessage(ChatColor.RED + "No magic block selected, use " + ChatColor.WHITE + "/mblock select");
            return;
        }

        Location location = magicBlock.getLocation();

        playEffects(sender, magicBlock, "blockselect");
        String message = ChatColor.LIGHT_PURPLE + magicBlock.getName()
            + ChatColor.GREEN + " at " + TextUtils.printLocation(location, 0)
            + selections.getDistanceMessage(sender, magicBlock);
        sender.sendMessage(message);
        String description = magicBlock.getDescription();
        if (description != null) {
            sender.sendMessage(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + description);
        }
        if (magicBlock.hasSpawner()) {
            Nearby nearby = magicBlock.getNearby();
            if (nearby != null) {
                int limit = magicBlock.getSpawnLimit();
                sender.sendMessage(ChatColor.GOLD + "Has " + ChatColor.GREEN + nearby.mobs
                    + ChatColor.GRAY + "/" + ChatColor.DARK_GREEN + limit
                    + ChatColor.GOLD + " active mobs");
                int minPlayers = magicBlock.getSpawnMinPlayers();
                sender.sendMessage(ChatColor.GOLD + "Has " + ChatColor.GREEN + nearby.players
                    + ChatColor.GRAY + "/" + ChatColor.DARK_GREEN + minPlayers
                    + ChatColor.GOLD + " nearby players");
            }

            long timeToNextSpawn = magicBlock.getTimeToNextSpawn();
            sender.sendMessage(ChatColor.GOLD + "Time to next spawn: " + controller.getMessages().getTimeDescription(timeToNextSpawn, "description", "cooldown"));
        }

        String creatorName = magicBlock.getCreatorName();
        if (creatorName != null && !creatorName.isEmpty()) {
            sender.sendMessage(ChatColor.DARK_GREEN + "  Created by: " + ChatColor.GREEN + creatorName);
        }
        ConfigurationSection parameters = magicBlock.getParameters();
        Set<String> parameterKeys = null;
        if (parameters == null) {
            sender.sendMessage(ChatColor.YELLOW + "(No Parameters)");
        } else {
            parameterKeys = parameters.getKeys(true);
            sender.sendMessage(ChatColor.DARK_AQUA + "Has " + ChatColor.AQUA + parameterKeys.size()
                + ChatColor.DARK_AQUA + " Parameters");
            for (String key : parameterKeys) {
                if (IGNORE_PROPERTIES.contains(key)) continue;
                Object property = parameters.get(key);
                if (!(property instanceof ConfigurationSection)) {
                    sender.sendMessage(ChatColor.AQUA + key + ChatColor.GRAY + ": "
                        + ChatColor.DARK_AQUA + CompatibilityLib.getInventoryUtils().describeProperty(property));
                }
            }
        }
        MagicBlockTemplate template = magicBlock.getTemplate();
        if (template != null) {
            ConfigurationSection templateParameters = template.getConfiguration();
            Set<String> keys = templateParameters.getKeys(true);
            for (String key : keys) {
                if (IGNORE_PROPERTIES.contains(key)) continue;
                if (parameterKeys != null && parameterKeys.contains(key)) continue;
                Object property = templateParameters.get(key);
                if (!(property instanceof ConfigurationSection)) {
                    sender.sendMessage(ChatColor.GRAY + key + ChatColor.DARK_GRAY + ": "
                        + ChatColor.DARK_AQUA + CompatibilityLib.getInventoryUtils().describeProperty(property));
                }
            }
        }
    }

    private void onNameBlock(CommandSender sender, MagicBlock magicBlock, String[] args) {
        if (magicBlock == null) {
            sender.sendMessage(ChatColor.RED + "No magic block selected, use " + ChatColor.WHITE + "/mblock select");
            return;
        }

        if (args.length == 0) {
            magicBlock.setName(null);
            sender.sendMessage(ChatColor.GREEN + "Cleared custom name for " + ChatColor.WHITE + magicBlock.getName());
            return;
        }

        String currentName = magicBlock.getName();
        magicBlock.setName(StringUtils.join(args, " "));
        sender.sendMessage(ChatColor.GREEN + "Renamed " + ChatColor.WHITE + currentName + ChatColor.GRAY + " to " + ChatColor.AQUA + magicBlock.getName());
    }

    private void onConfigureBlock(CommandSender sender, MagicBlock magicBlock, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: " + ChatColor.WHITE + "/mblock configure <property> [value]");
            return;
        }

        if (magicBlock == null) {
            sender.sendMessage(ChatColor.RED + "No magic block selected, use " + ChatColor.WHITE + "/mblock select");
            return;
        }

        String key = args[0];
        ConfigurationSection parameters = magicBlock.getParameters();
        if (args.length == 1 && (parameters == null || !parameters.contains(key))) {
            sender.sendMessage(ChatColor.RED + "Magic block does not have a " + ChatColor.WHITE + key + ChatColor.RED + " parameter");
            return;
        }

        Location location = magicBlock.getLocation();

        selections.playEffects(sender, magicBlock, "blockselect");
        String message = ChatColor.GREEN + "Configured " + ChatColor.LIGHT_PURPLE + magicBlock.getName()
            + ChatColor.GREEN + " at " + TextUtils.printLocation(location, 0)
            + selections.getDistanceMessage(sender, magicBlock);
        sender.sendMessage(message);

        boolean isActive = magicController.isActive(magicBlock);
        if (isActive) {
            magicBlock.pause();
        }
        if (args.length == 1) {
            parameters.set(key, null);
            sender.sendMessage(ChatColor.YELLOW + "Removed property: " + ChatColor.AQUA + key);
        } else {
            if (parameters == null) {
                parameters = ConfigurationUtils.newConfigurationSection();
            }
            String configValue = StringUtils.join(Arrays.copyOfRange(args, 1, args.length), " ");
            ConfigurationUtils.set(parameters, key, configValue);
            Object value = parameters.get(key);
            magicBlock.setParameters(parameters);
            sender.sendMessage(ChatColor.DARK_AQUA + "Set property: " + ChatColor.AQUA + key
                + ChatColor.DARK_AQUA + " to " + ChatColor.WHITE + CompatibilityLib.getInventoryUtils().describeProperty(value));
        }

        magicBlock.reload();
        if (isActive) {
            magicBlock.resume();
        }
    }

    private void onSelectBlock(CommandSender sender, String[] args) {
        List<MagicBlock> list = selections.getList(sender);
        if (list == null || args.length == 0) {
            if (sender instanceof Player) {
                list = selections.updateList(sender).getList();
                if (args.length == 0) {
                    if (list.isEmpty()) {
                        sender.sendMessage(ChatColor.RED + "Could not find any magic blocks");
                    } else {
                        MagicBlock magicBlock = list.get(0);
                        selections.playEffects(sender, magicBlock, "blockselect");
                        String message = ChatColor.GREEN + "Selected nearby " + ChatColor.LIGHT_PURPLE + magicBlock.getName()
                            + ChatColor.GREEN + " at " + TextUtils.printLocation(magicBlock.getLocation(), 0)
                            + selections.getDistanceMessage(sender, magicBlock);
                        sender.sendMessage(message);
                        selections.setSelection(sender, magicBlock);
                    }
                    return;
                }
            } else {
                if (args.length == 0) {
                    sender.sendMessage(ChatColor.RED + "Nothing to select, Use " + ChatColor.WHITE + "/mblock list");
                    return;
                } else {
                    list = selections.updateList(sender).getList();
                }
            }
        }

        MagicBlock magicBlock = null;
        String name = args[0];
        if (args.length > 0) {
            try {
                int index = Integer.parseInt(name);
                if (index <= 0 || index > list.size()) {
                    sender.sendMessage(ChatColor.RED + "Index out of range: " + ChatColor.WHITE + args[0]
                        + ChatColor.GRAY + "/" + ChatColor.WHITE + list.size());
                    return;
                }
                magicBlock = list.get(index - 1);
            } catch (NumberFormatException ignore) {
            }
        }
        if (magicBlock == null) {
            for (MagicBlock candidate : list) {
                if (candidate.getName().equalsIgnoreCase(name) || candidate.getTemplateKey().equalsIgnoreCase(name)) {
                    magicBlock = candidate;
                    break;
                }
            }
        }
        if (magicBlock == null) {
            sender.sendMessage(ChatColor.RED + "Could not find: " + ChatColor.WHITE + name);
            return;
        }

        selections.setSelection(sender, magicBlock);
        Location location = magicBlock.getLocation();

        selections.playEffects(sender, magicBlock, "blockselect");
        String message = ChatColor.GREEN + "Selected " + ChatColor.LIGHT_PURPLE + magicBlock.getName()
            + ChatColor.GREEN + " at " + TextUtils.printLocation(location, 0)
            + selections.getDistanceMessage(sender, magicBlock);
        sender.sendMessage(message);
    }

    @Override
    public Collection<String> onTabComplete(CommandSender sender, String commandName, String[] args) {
        List<String> options = new ArrayList<>();
        String subCommand = args[0];
        String property = "";
        boolean isConfigure = args.length >= 3 && subCommand.equalsIgnoreCase("add");
        if (subCommand.equalsIgnoreCase("configure") && args.length >= 2) {
            isConfigure = true;
        }
        if (isConfigure) {
            property = args[args.length - 2];
        }

        if (!sender.hasPermission("Magic.commands.mblock")) return options;
        if (args.length == 1) {
            options.add("add");
            options.add("list");
            options.add("remove");
            options.add("select");
            options.add("configure");
            options.add("describe");
            options.add("desc");
            options.add("name");
            options.add("tp");
            options.add("move");
            options.add("debug");
            options.add("enable");
            options.add("disable");
        } else if (args.length == 2 && subCommand.equalsIgnoreCase("add")) {
            options.addAll(magicController.getMagicBlockTemplateKeys());
        } else if (isConfigure) {
            switch (property) {
                case "spawn.mobs":
                    options.addAll(api.getController().getMobKeys());
                    for (EntityType entityType : EntityType.values()) {
                        if (entityType.isAlive() && entityType.isSpawnable()) {
                            options.add(entityType.name().toLowerCase());
                        }
                    }
                    break;
                case "cast.spells":
                    Collection<SpellTemplate> spells = api.getController().getSpellTemplates();
                    for (SpellTemplate spell : spells) {
                        options.add(spell.getKey());
                    }
                    break;
                case "cast.undo_all":
                case "cast.leash":
                case "cast.recast":
                case "spawn.check_floor":
                    options.addAll(Arrays.asList(BaseSpell.EXAMPLE_BOOLEANS));
                    break;
                case "spawn.interval":
                case "interval":
                    options.addAll(Arrays.asList(BaseSpell.EXAMPLE_DURATIONS));
                    break;
                case "effects":
                    options.addAll(magicController.getEffectKeys());
                    break;
                case "max_moon_phase":
                case "min_moon_phase":
                case "moon_phase":
                    options.add("full");
                    options.add("new");
                    for (int i = 0; i < 8; i++) {
                        options.add(Integer.toString(i));
                    }
                    break;
                case "max_time":
                case "min_time":
                    options.add("dawn");
                    options.add("day");
                    options.add("noon");
                    options.add("dusk");
                    options.add("night");
                    options.add("midnight");
                    options.add("0");
                    options.add("6000");
                    options.add("12000");
                    options.add("18000");
                    break;
                case "spawn.radius":
                case "spawn.vertical_radius":
                case "spawn.vertical_range":
                case "spawn.limit":
                case "spawn.count":
                case "spawn.limit_range":
                case "spawn.player_range":
                case "spawn.retries":
                case "spawn.min_players":
                case "spawn.check_radius":
                case "spawn.vertical_check_radius":
                case "player_range":
                case "min_players":
                    options.addAll(Arrays.asList(BaseSpell.EXAMPLE_SIZES));
                    break;
                case "spawn.probability":
                    options.addAll(Arrays.asList(BaseSpell.EXAMPLE_PERCENTAGES));
                    break;
                default:
                    if (!PROPERTY_KEYS.contains(property)) {
                        options.addAll(PROPERTY_KEYS);
                    }
            }
        }
        return options;
    }
}
