package com.elmakers.mine.bukkit.magic.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.automata.Automaton;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.TextUtils;

public class MagicAutomataCommandExecutor extends MagicTabExecutor {
    private final MagicController magicController;

    public MagicAutomataCommandExecutor(MagicController controller) {
        super(controller.getAPI());
        this.magicController = controller;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!api.hasPermission(sender, "Magic.commands.mauto")) {
            sendNoPermission(sender);
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: mauto [add|remove|list]");
            return true;
        }

        String subCommand = args[0];
        args = Arrays.copyOfRange(args, 1, args.length);

        if (subCommand.equalsIgnoreCase("list")) {
            onListAutomata(sender, args);
            return true;
        }

        if (!(sender instanceof Player)) {

            sender.sendMessage(ChatColor.RED + "This command can only be used in-game");
            return true;
        }

        Player player = (Player)sender;
        if (subCommand.equalsIgnoreCase("add")) {
            onAddAutomata(player, args);
            return true;
        }

        if (subCommand.equalsIgnoreCase("remove")) {
            onRemoveAutomata(player, args);
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Usage: mauto [add|remove|list]");
        return true;
    }

    protected void onListAutomata(CommandSender sender, String[] args) {
        int range = 0;
        Location location = null;
        if (args.length > 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Radius parameter can only be used in-game!");
                return;
            }
            try {
                range = Integer.parseInt(args[0]);
            } catch (Exception ex) {
                sender.sendMessage(ChatColor.RED + "Invalid radius: " + ChatColor.WHITE + args[0]);
                return;
            }

            Player player = (Player) sender;
            location = player.getLocation();
        } else if (sender instanceof Player) {
            location = ((Player) sender).getLocation();
        }
        Collection<Automaton> automata = magicController.getActiveAutomata();
        if (location != null) {
            automata = getSorted(automata, location, range);
        }
        sender.sendMessage(ChatColor.AQUA + "Total active automata: " + ChatColor.DARK_AQUA + automata.size());
        for (Automaton automaton : automata) {
            String message = ChatColor.LIGHT_PURPLE + automaton.getTemplateKey() + ChatColor.DARK_PURPLE
                + " at " + TextUtils.printLocation(automaton.getLocation(), 0);

            if (location != null && location.getWorld().equals(automaton.getLocation().getWorld())) {
                double distance = location.distance(automaton.getLocation());
                message = message + ChatColor.GRAY + " (" + ChatColor.WHITE + TextUtils.printNumber(distance, 1)
                    + ChatColor.BLUE + " blocks away" + ChatColor.GRAY + ")";
            }

            sender.sendMessage(message);
        }
    }

    private List<Automaton> getSorted(Collection<Automaton> automata, Location location, int range) {
        int rangeSquared = range * range;
        List<Automaton> sorted = new ArrayList();
        for (Automaton automaton : automata) {
            if (rangeSquared > 0) {
                if (!location.getWorld().equals(automaton.getLocation().getWorld())) continue;
                if (location.distanceSquared(automaton.getLocation()) > rangeSquared) continue;
            }

            sorted.add(automaton);
        }
        final Location sortLocation = location;
        Collections.sort(sorted, new Comparator<Automaton>() {
            @Override
            public int compare(Automaton a, Automaton b) {
                boolean aInWorld = sortLocation.getWorld().equals(a.getLocation().getWorld());
                boolean bInWorld = sortLocation.getWorld().equals(b.getLocation().getWorld());
                if (aInWorld && !bInWorld) return -1;
                if (!aInWorld && bInWorld) return 1;
                if (!aInWorld && !bInWorld) return 0;
                double aDistance = sortLocation.distanceSquared(a.getLocation());
                double bDistance = sortLocation.distanceSquared(b.getLocation());
                return (int) Math.round(aDistance - bDistance);
            }
        });
        return sorted;
    }

    protected void onAddAutomata(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: " + ChatColor.WHITE + "/mauto add <template>");
            return;
        }

        String key = args[0];
        if (!magicController.isAutomataTemplate(key)) {
            player.sendMessage(ChatColor.RED + "Invalid automata template: " + ChatColor.DARK_RED + key);
            return;
        }

        Location location = player.getLocation();
        Automaton existing = magicController.getAutomatonAt(location);
        if (existing != null) {
            player.sendMessage(ChatColor.RED + "Automata already exists: " + ChatColor.LIGHT_PURPLE + existing.getTemplateKey()
                + ChatColor.RED + " at " + TextUtils.printLocation(existing.getLocation(), 0));
            return;
        }

        ConfigurationSection parameters = null;
        if (args.length > 1) {
            String[] parameterArgs = Arrays.copyOfRange(args, 1, args.length);
            parameters = new MemoryConfiguration();
            ConfigurationUtils.addParameters(parameterArgs, parameters);
        }
        Automaton automaton = new Automaton(magicController, location.getBlock(), key, player.getUniqueId().toString(), parameters);
        magicController.registerAutomaton(automaton);

        player.sendMessage(ChatColor.AQUA + "Created automaton: " + ChatColor.LIGHT_PURPLE + automaton.getTemplateKey()
            + ChatColor.AQUA + " at " + TextUtils.printLocation(automaton.getLocation(), 0));
    }

    protected void onRemoveAutomata(Player player, String[] args) {
        int range = 16;
        if (args.length > 0) {
            try {
                range = Integer.parseInt(args[0]);
            } catch (Exception ex) {
                player.sendMessage(ChatColor.RED + "Invalid radius: " + ChatColor.WHITE + args[0]);
                return;
            }
        }

        List<Automaton> automata = getSorted(magicController.getActiveAutomata(), player.getLocation(), range);
        if (automata.size() == 0) {
            player.sendMessage(ChatColor.RED + "No automata within range, try adding a radius parameter, or check /mauto list");
            return;
        }

        Automaton automaton = automata.get(0);
        if (!magicController.unregisterAutomaton(automaton)) {
            player.sendMessage(ChatColor.RED + "Could not find automata at given position (something went wrong)");
            return;
        }
        player.sendMessage(ChatColor.YELLOW + "Removed " + ChatColor.LIGHT_PURPLE + automaton.getTemplateKey()
            + ChatColor.YELLOW + " at " + TextUtils.printLocation(automaton.getLocation(), 0));
    }

    @Override
    public Collection<String> onTabComplete(CommandSender sender, String commandName, String[] args) {
        List<String> options = new ArrayList<>();
        if (!sender.hasPermission("Magic.commands.mauto")) return options;
        if (args.length == 1) {
            options.add("add");
            options.add("list");
            options.add("remove");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("add")) {
            options.addAll(magicController.getAutomatonTemplateKeys());
        } else if (args.length >= 3 && args[0].equalsIgnoreCase("add") && args[args.length - 2].equals("spawn.mobs")) {
            options.addAll(api.getController().getMobKeys());
            for (EntityType entityType : EntityType.values()) {
                if (entityType.isAlive() && entityType.isSpawnable()) {
                    options.add(entityType.name().toLowerCase());
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("add")) {
            options.add("interval");
            options.add("spawn.mobs");
        }
        return options;
    }
}
