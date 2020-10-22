package com.elmakers.mine.bukkit.magic.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.TextUtils;
import com.elmakers.mine.bukkit.warp.MagicWarp;

public class MagicWarpCommandExecutor extends MagicTabExecutor {
    private final MagicController magicController;
    private static final int warpsPerPage = 8;

    public MagicWarpCommandExecutor(MagicController controller) {
        super(controller.getAPI(), "mwarp");
        this.magicController = controller;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!api.hasPermission(sender, getPermissionNode())) {
            sendNoPermission(sender);
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: mwarp [add|replace|remove|go|list|import|configure]");
            return true;
        }

        String subCommand = args[0];

        if (!api.hasPermission(sender, "Magic.commands.mwarp." + subCommand)) {
            sendNoPermission(sender);
            return true;
        }

        if (subCommand.equalsIgnoreCase("import")) {
            onImportWarps(sender);
            return true;
        }

        if (subCommand.equalsIgnoreCase("list")) {
            int pageNumber = 1;
            if (args.length > 1) {
                try {
                    pageNumber = Integer.parseInt(args[1]);
                } catch (NumberFormatException ex) {
                    sender.sendMessage(ChatColor.RED + "Invalid page number: " + pageNumber);
                    return true;
                }
            }
            onListWarps(sender, pageNumber);
            return true;
        }

        if (args.length == 1) {
            sender.sendMessage(ChatColor.RED + "Usage: mwarp [add|replace|remove|go] <warpname>");
            return true;
        }

        String warpName = args[1];
        String[] parameters = Arrays.copyOfRange(args, 2, args.length);

        if (subCommand.equalsIgnoreCase("remove")) {
            onRemoveWarp(sender, warpName);
            return true;
        }

        if (subCommand.equalsIgnoreCase("configure")) {
            onConfigureWarp(sender, warpName, parameters);
            return true;
        }

        if (subCommand.equalsIgnoreCase("send")) {
            if (args.length < 3) {
                sender.sendMessage(ChatColor.RED + "Usage: mwarp send <player> <warp>");
                return true;
            }
            Player sendPlayer = DeprecatedUtils.getPlayer(args[1]);
            if (sendPlayer == null) {
                sender.sendMessage(ChatColor.RED + "Can't find player: " + args[1]);
                return true;
            }
            onSendWarp(sender, sendPlayer, args[2]);
            return true;
        }

        Player player = sender instanceof Player ? (Player)sender : null;
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "This command may only be used from in-game.");
            return true;
        }

        if (subCommand.equalsIgnoreCase("replace")) {
            onAddWarp(player, warpName, true);
            return true;
        }

        if (subCommand.equalsIgnoreCase("add")) {
            onAddWarp(player, warpName, false);
            return true;
        }

        if (subCommand.equalsIgnoreCase("go")) {
            onGoWarp(player, warpName);
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Usage: mwarp [add|replace|remove|go|import]");
        return true;
    }

    private void onGoWarp(Player player, String warpName) {
        Location location = magicController.getWarp(warpName);
        if (location == null) {
            player.sendMessage(ChatColor.RED + "Unknown warp: " + ChatColor.DARK_RED + warpName);
            return;
        }
        player.teleport(location);
    }

    private void onSendWarp(CommandSender sender, Player player, String warpName) {
        Location location = magicController.getWarp(warpName);
        if (location == null) {
            sender.sendMessage(ChatColor.RED + "Unknown warp: " + ChatColor.DARK_RED + warpName);
            return;
        }
        player.teleport(location);
    }

    private void onAddWarp(Player player, String warpName, boolean overwrite) {
        if (!overwrite && magicController.getWarps().hasCustomWarp(warpName)) {
            player.sendMessage(ChatColor.RED + "Warp: " + ChatColor.DARK_RED + warpName + ChatColor.RED + " already exists!");
            player.sendMessage(ChatColor.YELLOW + "Use " + ChatColor.GOLD + "/mwarp replace " + warpName + ChatColor.YELLOW + " to replace.");
            return;
        }

        magicController.getWarps().setWarp(warpName, player.getLocation());
        player.sendMessage(ChatColor.AQUA + "Set warp: " + ChatColor.DARK_AQUA + warpName + ChatColor.AQUA + " to " + TextUtils.printLocation(player.getLocation()));
    }

    private void onRemoveWarp(CommandSender sender, String warpName) {
        if (magicController.getWarps().removeWarp(warpName)) {
            sender.sendMessage(ChatColor.AQUA + "Removed warp: " + ChatColor.DARK_AQUA + warpName);
        } else {
            sender.sendMessage(ChatColor.RED + "Unknown warp: " + ChatColor.DARK_RED + warpName);
        }
    }

    private void onConfigureWarp(CommandSender sender, String warpName, String[] parameters) {
        MagicWarp warp = magicController.getWarps().getMagicWarp(warpName);
        if (warp == null) {
            sender.sendMessage(ChatColor.RED + "Unknown warp: " + ChatColor.DARK_RED + warpName);
            return;
        }
        if (parameters.length == 0) {
            sender.sendMessage(ChatColor.RED + "Missing parameter name");
            return;
        }
        String parameterKey = parameters[0];
        String value = "";
        MagicController magic = (MagicController)controller;
        if (parameters.length > 0) {
            value = StringUtils.join(Arrays.copyOfRange(parameters, 1, parameters.length));
        }
        if (parameterKey.equalsIgnoreCase("marker_icon")) {
            if (value.isEmpty()) {
                warp.removeMarker(magic);
                warp.setMarkerIcon(null);
                sender.sendMessage(ChatColor.AQUA + "Configured warp: "
                    + ChatColor.DARK_AQUA + warpName + ChatColor.RED + ", cleared "
                    + ChatColor.YELLOW + "marker icon");
            } else {
                warp.setMarkerIcon(value);
                sender.sendMessage(ChatColor.AQUA + "Configured warp: "
                    + ChatColor.DARK_AQUA + warpName + ChatColor.AQUA + ", set "
                    + ChatColor.YELLOW + "marker icon" + ChatColor.AQUA + " to "
                    + ChatColor.GOLD + value);
            }
        } else if (parameterKey.equalsIgnoreCase("name")) {
            if (value.isEmpty()) {
                warp.setName(null);
                sender.sendMessage(ChatColor.AQUA + "Configured warp: "
                    + ChatColor.DARK_AQUA + warpName + ChatColor.RED + ", cleared "
                    + ChatColor.YELLOW + "name");
            } else {
                warp.setName(value);
                sender.sendMessage(ChatColor.AQUA + "Configured warp: "
                    + ChatColor.DARK_AQUA + warpName + ChatColor.AQUA + ", set "
                    + ChatColor.YELLOW + "name" + ChatColor.AQUA + " to "
                    + ChatColor.GOLD + value);
            }
        } else if (parameterKey.equalsIgnoreCase("description")) {
            if (value.isEmpty()) {
                warp.setDescription(null);
                sender.sendMessage(ChatColor.AQUA + "Configured warp: "
                    + ChatColor.DARK_AQUA + warpName + ChatColor.RED + ", cleared "
                    + ChatColor.YELLOW + "description");
            } else {
                warp.setDescription(value);
                sender.sendMessage(ChatColor.AQUA + "Configured warp: "
                    + ChatColor.DARK_AQUA + warpName + ChatColor.AQUA + ", set "
                    + ChatColor.YELLOW + "description" + ChatColor.AQUA + " to "
                    + ChatColor.GOLD + value);
            }
        } else if (parameterKey.equalsIgnoreCase("marker_set")) {
            warp.removeMarker(magic);
            if (value.isEmpty()) {
                warp.setMarkerSet(null);
                sender.sendMessage(ChatColor.AQUA + "Configured warp: "
                    + ChatColor.DARK_AQUA + warpName + ChatColor.RED + ", cleared "
                    + ChatColor.YELLOW + "marker set");
            } else {
                warp.setMarkerSet(value);
                sender.sendMessage(ChatColor.AQUA + "Configured warp: "
                    + ChatColor.DARK_AQUA + warpName + ChatColor.AQUA + ", set "
                    + ChatColor.YELLOW + "marker set" + ChatColor.AQUA + " to "
                    + ChatColor.GOLD + value);
            }
        } else if (parameterKey.equalsIgnoreCase("icon")) {
            if (value.isEmpty()) {
                warp.setIcon(null);
                sender.sendMessage(ChatColor.AQUA + "Configured warp: "
                    + ChatColor.DARK_AQUA + warpName + ChatColor.RED + ", cleared "
                    + ChatColor.YELLOW + "icon");
            } else {
                warp.setIcon(value);
                sender.sendMessage(ChatColor.AQUA + "Configured warp: "
                    + ChatColor.DARK_AQUA + warpName + ChatColor.AQUA + ", set "
                    + ChatColor.YELLOW + "icon" + ChatColor.AQUA + " to "
                    + ChatColor.GOLD + value);
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Unknown warp parameter: " + ChatColor.YELLOW + parameterKey);
            return;
        }
        warp.checkMarker(magic);
    }

    private void onListWarps(CommandSender sender, int pageNumber) {
        int startIndex = (pageNumber - 1) * warpsPerPage;
        List<String> warps = magicController.getWarps().getWarps();
        for (int i = startIndex; i < startIndex + warpsPerPage && i < warps.size(); i++) {
            String warp = warps.get(i);
            sender.sendMessage(ChatColor.YELLOW + Integer.toString(i) + ChatColor.GRAY + ": " + ChatColor.GOLD + warp);
        }
        if (warps.size() > warpsPerPage) {
            int pages = (warps.size() / warpsPerPage) + 1;
            sender.sendMessage("  " + ChatColor.GRAY + "Page " + ChatColor.YELLOW
                + pageNumber + ChatColor.GRAY + "/" + ChatColor.GOLD + pages);
        }
    }

    private void onImportWarps(CommandSender sender) {
        int imported = magicController.getWarps().importWarps();
        sender.sendMessage(ChatColor.AQUA + "Imported " + ChatColor.DARK_AQUA + imported + ChatColor.AQUA + " warps");
    }

    @Override
    public Collection<String> onTabComplete(CommandSender sender, String commandName, String[] args) {
        List<String> options = new ArrayList<>();
        if (!sender.hasPermission("Magic.commands.mwarp")) return options;
        if (args.length == 1) {
            addIfPermissible(sender, options, "Magic.commands.mwarp.", "add");
            addIfPermissible(sender, options, "Magic.commands.mwarp.", "remove");
            addIfPermissible(sender, options, "Magic.commands.mwarp.", "replace");
            addIfPermissible(sender, options, "Magic.commands.mwarp.", "go");
            addIfPermissible(sender, options, "Magic.commands.mwarp.", "send");
            addIfPermissible(sender, options, "Magic.commands.mwarp.", "import");
            addIfPermissible(sender, options, "Magic.commands.mwarp.", "list");
            addIfPermissible(sender, options, "Magic.commands.mwarp.", "configure");
        } else if (args.length == 2) {
            String subCommand = args[0];
            if (subCommand.equals("remove") || subCommand.equals("go") || subCommand.equals("replace") || subCommand.equals("configure")) {
                options.addAll(magicController.getWarps().getCustomWarps());
            } else if (subCommand.equals("send")) {
                options.addAll(api.getPlayerNames());
            }
        } else if (args.length == 3) {
            String subCommand = args[0];
            if (subCommand.equals("send")) {
                options.addAll(magicController.getWarps().getCustomWarps());
            } else if (subCommand.equals("configure")) {
                options.add("name");
                options.add("description");
                options.add("icon");
                options.add("marker_icon");
                options.add("marker_set");
            }
        } else if (args.length == 4) {
            String subCommand = args[0];
            if (subCommand.equals("configure")) {
                String parameterKey = args[2];
                if (parameterKey.equals("marker_icon")) {
                    options.addAll(((MagicController)controller).getMarkerIcons());
                } else if (parameterKey.equals("icon")) {
                    Collection<String> allItems = api.getController().getItemKeys();
                    for (String itemKey : allItems) {
                        options.add(itemKey);
                    }
                } else if (parameterKey.equals("name")) {
                    options.addAll(((MagicController)controller).getMarkerIcons());
                }
            }
        }
        return options;
    }
}
