package com.elmakers.mine.bukkit.magic.command;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.automata.Automaton;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import com.elmakers.mine.bukkit.utility.TextUtils;
import com.google.common.collect.ImmutableSet;

public class MagicAutomataCommandExecutor extends MagicTabExecutor {
    private final MagicController magicController;
    private final SelectedAutomata consoleSelection = new SelectedAutomata();
    private final Map<UUID, SelectedAutomata> selections = new HashMap<>();

    private static final ImmutableSet<String> PROPERTY_KEYS = ImmutableSet.of(
        "name", "interval", "effects",
        "spawn.mobs", "spawn.probability", "spawn.player_range", "spawn.min_players",
        "spawn.limit", "spawn.limit_range", "spawn.vertical_range", "spawn.radius",
        "spawn.vertical_radius", "spawn.retries", "min_players", "player_range",
        "min_time", "max_time", "min_moon_phase", "max_moon_phase", "moon_phase",
        "cast.spells", "cast.recast", "cast.undo_all", "spawn.count", "spawn.leash",
        "spawn.interval"
    );

    private static class SelectedAutomata {
        public Automaton selected;
        public List<Automaton> list;
    }

    public MagicAutomataCommandExecutor(MagicController controller) {
        super(controller.getAPI(), "mauto");
        this.magicController = controller;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!api.hasPermission(sender, getPermissionNode())) {
            sendNoPermission(sender);
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: mauto [add|select|remove|list|configure|describe]");
            return true;
        }

        String subCommand = args[0];
        args = Arrays.copyOfRange(args, 1, args.length);

        SelectedAutomata selection = consoleSelection;
        Player player = sender instanceof Player ? (Player)sender : null;
        if (player != null) {
            selection = selections.get(player.getUniqueId());
        }

        if (subCommand.equalsIgnoreCase("list")) {
            onListAutomata(sender, args);
            return true;
        }

        if (subCommand.equalsIgnoreCase("select")) {
            onSelectAutomata(sender, selection, args);
            return true;
        }

        if (subCommand.equalsIgnoreCase("remove")) {
            onRemoveAutomata(sender, selection);
            return true;
        }

        if (subCommand.equalsIgnoreCase("describe")) {
            onDescribeAutomata(sender, selection);
            return true;
        }

        if (subCommand.equalsIgnoreCase("configure")) {
            onConfigureAutomata(sender, selection, args);
            return true;
        }

        if (player == null) {

            sender.sendMessage(ChatColor.RED + "This command can only be used in-game");
            return true;
        }

        if (subCommand.equalsIgnoreCase("add")) {
            onAddAutomata(player, args);
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Usage: mauto [add|select|remove|list|configure|describe]");
        return true;
    }

    @Nonnull
    private SelectedAutomata getSelection(CommandSender sender) {
        SelectedAutomata selection = consoleSelection;
        Player player = sender instanceof Player ? (Player)sender : null;
        if (player != null) {
            selection = selections.get(player.getUniqueId());
            if (selection == null) {
                selection = new SelectedAutomata();
                selections.put(player.getUniqueId(), selection);
            }
        }
        return selection;
    }

    private void onListAutomata(CommandSender sender, String[] args) {
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
            location = player.getEyeLocation();
        } else if (sender instanceof Player) {
            location = ((Player) sender).getEyeLocation();
        }
        Collection<Automaton> allAutomata = magicController.getActiveAutomata();
        List<Automaton> automata;
        if (location != null) {
            automata = getSorted(allAutomata, location, range);
        } else {
            automata = new ArrayList<>(allAutomata);
        }
        getSelection(sender).list = automata;
        sender.sendMessage(ChatColor.AQUA + "Total active automata: " + ChatColor.DARK_AQUA + automata.size());
        boolean first = true;
        for (int i = 0; i < automata.size(); i++) {
            Automaton automaton = automata.get(i);
            Location automatonLocation = automaton.getLocation();
            String message = ChatColor.WHITE + Integer.toString(i + 1) + ChatColor.GRAY + ": "
                + ChatColor.LIGHT_PURPLE + automaton.getTemplateKey() + ChatColor.DARK_PURPLE
                + " at " + TextUtils.printLocation(automatonLocation, 0);

            String effectsKey = first ? "blockfindfirst" : "blockfind";
            String rangeMessage = playEffects(sender, automaton, effectsKey);
            if (rangeMessage != null) {
                first = false;
                message = message + rangeMessage;
            }

            sender.sendMessage(message);
        }
    }

    private List<Automaton> getSorted(Collection<Automaton> automata, Location location, int range) {
        int rangeSquared = range * range;
        List<Automaton> sorted = new ArrayList<>();
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
                if (!aInWorld) return 0;
                double aDistance = sortLocation.distanceSquared(a.getLocation());
                double bDistance = sortLocation.distanceSquared(b.getLocation());
                return (int) Math.round(aDistance - bDistance);
            }
        });
        return sorted;
    }

    private void onAddAutomata(Player player, String[] args) {
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
        Automaton automaton = new Automaton(magicController, location, key, player.getUniqueId().toString(), player.getName(), parameters);
        magicController.registerAutomaton(automaton);

        playEffects(player, automaton, "blockselect");
        getSelection(player).selected = automaton;

        player.sendMessage(ChatColor.AQUA + "Created automaton: " + ChatColor.LIGHT_PURPLE + automaton.getTemplateKey()
            + ChatColor.AQUA + " at " + TextUtils.printLocation(automaton.getLocation(), 0));
    }

    @Nullable
    private String playEffects(CommandSender sender, Automaton automaton, String effectsKey) {
        String rangeMessage = null;
        if (sender instanceof Player) {
            Location location = ((Player)sender).getLocation();
            Location automatonLocation = automaton.getLocation();
            if (location.getWorld().equals(automatonLocation.getWorld())) {
                double distance = location.distance(automatonLocation);
                rangeMessage = ChatColor.GRAY + " (" + ChatColor.WHITE + TextUtils.printNumber(distance, 1)
                    + ChatColor.BLUE + " blocks away" + ChatColor.GRAY + ")";

                if (distance < 64) {
                    controller.playEffects(effectsKey, location, automatonLocation);
                }
            }
        }

        return rangeMessage;
    }

    private void onRemoveAutomata(CommandSender sender, SelectedAutomata selection) {
        if (selection == null || selection.selected == null) {
            sender.sendMessage(ChatColor.RED + "No automata selected, use " + ChatColor.WHITE + "/mauto select");
            return;
        }

        Automaton automaton = selection.selected;
        if (!magicController.unregisterAutomaton(automaton)) {
            sender.sendMessage(ChatColor.RED + "Could not find automata at given position (something went wrong)");
            return;
        }

        Location location = automaton.getLocation();
        selection.selected = null;

        String rangeMessage = playEffects(sender, automaton, "blockremove");
        String message = ChatColor.YELLOW + "Removed " + ChatColor.LIGHT_PURPLE + automaton.getTemplateKey()
            + ChatColor.YELLOW + " at " + TextUtils.printLocation(location, 0);
        if (rangeMessage != null) {
            message += rangeMessage;
        }
        sender.sendMessage(message);
    }

    private void onDescribeAutomata(CommandSender sender, SelectedAutomata selection) {
        if (selection == null || selection.selected == null) {
            sender.sendMessage(ChatColor.RED + "No automata selected, use " + ChatColor.WHITE + "/mauto select");
            return;
        }

        Automaton automaton = selection.selected;
        Location location = automaton.getLocation();

        String rangeMessage = playEffects(sender, automaton, "blockselect");
        String message = ChatColor.LIGHT_PURPLE + automaton.getTemplateKey()
            + ChatColor.GREEN + " at " + TextUtils.printLocation(location, 0);
        if (rangeMessage != null) {
            message += rangeMessage;
        }
        sender.sendMessage(message);
        Collection<WeakReference<Entity>> spawned = automaton.getSpawned();
        if (spawned != null && !spawned.isEmpty()) {
            int limit = automaton.getSpawnLimit();
            sender.sendMessage(ChatColor.GOLD + "Has " + ChatColor.GREEN + spawned.size()
                + ChatColor.GRAY + "/" + ChatColor.DARK_GREEN + limit
                + ChatColor.GOLD + " active spawns");
        }
        if (automaton.hasSpawner()) {
            long timeToNextSpawn = automaton.getTimeToNextSpawn();
            sender.sendMessage(ChatColor.GOLD + "Time to next spawn: " + controller.getMessages().getTimeDescription(timeToNextSpawn, "description", "cooldown"));
        }

        String creatorName = automaton.getCreatorName();
        creatorName = (creatorName == null || creatorName.isEmpty()) ? ChatColor.YELLOW + "(Unknown)" : ChatColor.GREEN + creatorName;
        sender.sendMessage(ChatColor.DARK_GREEN + "  Created by: " + creatorName);
        ConfigurationSection parameters = automaton.getParameters();
        if (parameters == null) {
            sender.sendMessage(ChatColor.YELLOW + "(No Parameters)");
        } else {
            Set<String> keys = parameters.getKeys(true);
            sender.sendMessage(ChatColor.DARK_AQUA + "Has " + ChatColor.AQUA + Integer.toString(keys.size())
                + ChatColor.DARK_AQUA + " Parameters");
            for (String key : keys) {
                Object property = parameters.get(key);
                if (!(property instanceof ConfigurationSection)) {
                    sender.sendMessage(ChatColor.AQUA + key + ChatColor.GRAY + ": "
                        + ChatColor.DARK_AQUA + InventoryUtils.describeProperty(property));
                }
            }
        }
    }

    private void onConfigureAutomata(CommandSender sender, SelectedAutomata selection, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: " + ChatColor.WHITE + "/mauto configure <property> [value]");
            return;
        }

        if (selection == null || selection.selected == null) {
            sender.sendMessage(ChatColor.RED + "No automata selected, use " + ChatColor.WHITE + "/mauto select");
            return;
        }

        String key = args[0];
        Automaton automaton = selection.selected;
        ConfigurationSection parameters = automaton.getParameters();
        if (args.length == 1 && (parameters == null || !parameters.contains(key))) {
            sender.sendMessage(ChatColor.RED + "Automata does not have a " + ChatColor.WHITE + key + ChatColor.RED + "parameter");
            return;
        }

        Location location = automaton.getLocation();

        String rangeMessage = playEffects(sender, automaton, "blockselect");
        String message = ChatColor.GREEN + "Configured " + ChatColor.LIGHT_PURPLE + automaton.getTemplateKey()
            + ChatColor.GREEN + " at " + TextUtils.printLocation(location, 0);
        if (rangeMessage != null) {
            message += rangeMessage;
        }
        sender.sendMessage(message);

        boolean isActive = magicController.isActive(automaton);
        if (isActive) {
            automaton.pause();
        }
        if (args.length == 1) {
            parameters.set(key, null);
            sender.sendMessage(ChatColor.YELLOW + "Removed property: " + ChatColor.AQUA + key);
        } else {
            if (parameters == null) {
                parameters = new MemoryConfiguration();
            }
            ConfigurationUtils.set(parameters, key, args[1]);
            Object value = parameters.get(key);
            automaton.setParameters(parameters);
            sender.sendMessage(ChatColor.DARK_AQUA + "Set property: " + ChatColor.AQUA + key
                + ChatColor.DARK_AQUA + " to " + ChatColor.WHITE + InventoryUtils.describeProperty(value));
        }

        automaton.reload();
        if (isActive) {
            automaton.resume();
        }
    }

    private void onSelectAutomata(CommandSender sender, SelectedAutomata selection, String[] args) {
        if (selection == null || selection.list == null || selection.list.isEmpty()) {
            if (sender instanceof Player) {
                Location location = ((Player)sender).getLocation();
                List<Automaton> nearby = getSorted(magicController.getActiveAutomata(), location, 24);
                if (nearby != null && !nearby.isEmpty()) {
                    selection = getSelection(sender);
                    Automaton automaton = nearby.get(0);
                    selection.selected = automaton;

                    String rangeMessage = playEffects(sender, automaton, "blockselect");
                    String message = ChatColor.GREEN + "Selected nearby " + ChatColor.LIGHT_PURPLE + automaton.getTemplateKey()
                        + ChatColor.GREEN + " at " + TextUtils.printLocation(automaton.getLocation(), 0);
                    if (rangeMessage != null) {
                        message += rangeMessage;
                    }
                    sender.sendMessage(message);
                    return;
                }
            }

            sender.sendMessage(ChatColor.RED + "Nothing to select, Use " + ChatColor.WHITE + "/mauto list");
            return;
        }

        int index = 1;
        if (args.length > 0) {
            try {
                index = Integer.parseInt(args[0]);
            } catch (Exception ex) {
                sender.sendMessage(ChatColor.RED + "Invalid index: " + ChatColor.WHITE + args[0]);
                return;
            }
        }

        if (index <= 0 || index > selection.list.size()) {
            sender.sendMessage(ChatColor.RED + "Index out of range: " + ChatColor.WHITE + args[0]
                + ChatColor.GRAY + "/" + ChatColor.WHITE + selection.list.size());
            return;
        }

        Automaton automaton = selection.list.get(index - 1);
        selection.selected = automaton;
        Location location = automaton.getLocation();

        String rangeMessage = playEffects(sender, automaton, "blockselect");
        String message = ChatColor.GREEN + "Selected " + ChatColor.LIGHT_PURPLE + automaton.getTemplateKey()
            + ChatColor.GREEN + " at " + TextUtils.printLocation(location, 0);
        if (rangeMessage != null) {
            message += rangeMessage;
        }
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

        if (!sender.hasPermission("Magic.commands.mauto")) return options;
        if (args.length == 1) {
            options.add("add");
            options.add("list");
            options.add("remove");
            options.add("select");
            options.add("configure");
            options.add("describe");
        } else if (args.length == 2 && subCommand.equalsIgnoreCase("add")) {
            options.addAll(magicController.getAutomatonTemplateKeys());
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
