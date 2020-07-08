package com.elmakers.mine.bukkit.action.builtin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.elmakers.mine.bukkit.action.BaseTeleportAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.action.GUIAction;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.wand.LostWand;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import com.elmakers.mine.bukkit.utility.SkinUtils;

public class RecallAction extends BaseTeleportAction implements GUIAction
{
    private static final Material DefaultWaypointMaterial = Material.BEACON;

    private boolean allowCrossWorld = true;
    private Map<String, ConfigurationSection> warps = new HashMap<>();
    private Map<String, ConfigurationSection> commands = new HashMap<>();
    private List<RecallType> enabledTypes = new ArrayList<>();
    private Map<Integer, Waypoint> options = new HashMap<>();
    private CastContext context;
    private ConfigurationSection parameters;
    private int protectionTime;
    private String markerKey = "recall_marker";
    private String unlockKey = "recall_warps";
    private String friendKey = "recall_friends";

    private class UndoMarkerMove implements Runnable
    {
        private final Location location;
        private final Mage mage;

        public UndoMarkerMove(Mage mage, Location currentLocation)
        {
            this.location = currentLocation;
            this.mage = mage;
        }

        @Override
        public void run()
        {
            mage.getData().set(markerKey, ConfigurationUtils.fromLocation(location));
        }
    }

    private enum RecallType
    {
        COMMAND,
        WARP,
        DEATH,
        SPAWN,
        HOME,
        WAND,
        MARKER,
        TOWN,
        FIELDS,
        FRIENDS
    }

    private static MaterialAndData defaultMaterial = new MaterialAndData(DefaultWaypointMaterial);

    private static class Waypoint implements Comparable<Waypoint>
    {
        public final RecallType type;
        public final String name;
        public final String description;
        public final Location location;
        public final String message;
        public final String failMessage;
        public final MaterialAndData icon;
        public final String iconURL;
        public final String command;
        public final boolean opPlayer;
        public final boolean asConsole;
        public final boolean maintainDirection;
        public final String warpName;
        public final String serverName;

        public boolean safe = true;

        public Waypoint(RecallType type, Location location, String name, String message, String failMessage, String description, MaterialAndData icon, boolean maintainDirection) {
            this.name = ChatColor.translateAlternateColorCodes('&', name);
            this.type = type;
            this.location = location;
            this.message = message;
            this.description = description == null ? null : ChatColor.translateAlternateColorCodes('&', description);
            this.failMessage = failMessage;
            this.icon = icon == null ? defaultMaterial : icon;
            this.iconURL = null;
            this.command = null;
            this.opPlayer = false;
            this.asConsole = false;
            this.maintainDirection = maintainDirection;
            serverName = null;
            warpName = null;
        }

        public Waypoint(RecallType type, Location location, String name, String message, String failMessage, String description, MaterialAndData icon, String iconURL) {
            this.name = ChatColor.translateAlternateColorCodes('&', name);
            this.type = type;
            this.location = location;
            this.message = message;
            this.description = description == null ? null : ChatColor.translateAlternateColorCodes('&', description);
            this.failMessage = failMessage;
            this.icon = icon == null ? defaultMaterial : icon;
            this.iconURL = iconURL;
            this.command = null;
            this.opPlayer = false;
            this.asConsole = false;
            this.maintainDirection = false;
            serverName = null;
            warpName = null;
        }

        public Waypoint(RecallType type, String warpName, String serverName, String name, String message, String failMessage, String description, MaterialAndData icon, String iconURL) {
            this.name = ChatColor.translateAlternateColorCodes('&', name);
            this.type = type;
            this.location = null;
            this.warpName = warpName;
            this.serverName = serverName;
            this.message = message;
            this.description = description == null ? null : ChatColor.translateAlternateColorCodes('&', description);;
            this.failMessage = failMessage;
            this.icon = icon == null ? defaultMaterial : icon;
            this.iconURL = iconURL;
            this.command = null;
            this.opPlayer = false;
            this.asConsole = false;
            this.maintainDirection = false;
        }

        public Waypoint(RecallType type, String command, boolean opPlayer, boolean asConsole, String name, String message, String failMessage, String description, MaterialAndData icon, String iconURL) {
            this.name = ChatColor.translateAlternateColorCodes('&', name);
            this.type = type;
            this.location = null;
            this.message = message;
            this.description = description == null ? null : ChatColor.translateAlternateColorCodes('&', description);;
            this.failMessage = failMessage;
            this.icon = icon == null ? defaultMaterial : icon;
            this.iconURL = iconURL;
            this.command = command;
            this.opPlayer = opPlayer;
            this.asConsole = asConsole;
            this.maintainDirection = false;
            serverName = null;
            warpName = null;
        }

        @Override
        public int compareTo(Waypoint o) {
            if (type != o.type)
            {
                if (type == RecallType.COMMAND) return -1;
                if (o.type == RecallType.COMMAND) return 1;
                if (type == RecallType.WARP) return -1;
                if (o.type == RecallType.WARP) return 1;
            }
            return name.compareTo(o.name);
        }

        public boolean isValid(boolean crossWorld, Location source)
        {
            if (isCommand())
            {
                return true;
            }
            if (location == null || location.getWorld() == null)
            {
                if (serverName != null && warpName != null)
                {
                    return true;
                }
                return false;
            }
            return crossWorld || source.getWorld().equals(location.getWorld());
        }

        public boolean isCommand() {
            return command != null;
        }
    }

    @Override
    public boolean isUndoable() {
        return true;
    }

    @Override
    public void deactivated() {
        // Check for shop items glitched into the player's inventory
        if (context != null) {
            context.getMage().removeItemsWithTag("waypoint");
        }
    }

    @Override
    public void dragged(InventoryDragEvent event) {
        event.setCancelled(true);
    }

    @Override
    public void clicked(InventoryClickEvent event)
    {
        event.setCancelled(true);
        if (context == null) {
            event.getWhoClicked().closeInventory();
            return;
        }
        ItemStack item = event.getCurrentItem();
        if (InventoryUtils.hasMeta(item, "move_marker"))
        {
            if (placeMarker(context.getLocation().getBlock()))
            {
                context.sendMessageKey("target_selected");
            }
            context.getMage().deactivateGUI();
            return;
        }
        if (item == null || item.getType() == Material.AIR)
        {
            context.getMage().deactivateGUI();
        }
        int slot = event.getRawSlot();
        if (event.getSlotType() == InventoryType.SlotType.CONTAINER)
        {
            Waypoint waypoint = options.get(slot);
            if (waypoint != null)
            {
                Mage mage = context.getMage();
                Player player = mage.getPlayer();
                mage.deactivateGUI();
                tryTeleport(player, waypoint);
            }
        }
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        this.parameters = parameters;
        this.context = context;
        this.markerKey = parameters.getString("marker_key", "recall_marker");
        this.unlockKey = parameters.getString("unlock_key", "recall_warps");
        this.friendKey = parameters.getString("friend_key", "recall_friends");
        this.protectionTime = parameters.getInt("protection_duration", 0);

        allowCrossWorld = parameters.getBoolean("allow_cross_world", true);
    }

    @Override
    public SpellResult perform(CastContext context) {
        this.context = context;
        enabledTypes.clear();
        warps.clear();
        commands.clear();

        Mage mage = context.getMage();
        MageController controller = context.getController();
        Player player = mage.getPlayer();
        if (player == null) {
            return SpellResult.PLAYER_REQUIRED;
        }

        Set<String> unlockedWarps = new HashSet<>();
        ConfigurationSection mageData = mage.getData();

        String unlockedString = mageData.getString(unlockKey);
        if (unlockedString != null && !unlockedString.isEmpty())
        {
            unlockedWarps.addAll(Arrays.asList(StringUtils.split(unlockedString, ',')));
        }

        Set<String> friends = new HashSet<>();
        String friendString = mageData.getString(friendKey);
        if (friendString != null && !friendString.isEmpty())
        {
            friends.addAll(Arrays.asList(StringUtils.split(friendString, ',')));
        }

        ConfigurationSection warpConfig = null;
        if (parameters.contains("warps"))
        {
            warpConfig = ConfigurationUtils.getConfigurationSection(parameters, "warps");
        }

        ConfigurationSection commandConfig = null;
        if (parameters.contains("commands"))
        {
            commandConfig = ConfigurationUtils.getConfigurationSection(parameters, "commands");
        }

        if (parameters.contains("unlock"))
        {
            String unlockWarp = parameters.getString("unlock");
            if (unlockWarp == null || unlockWarp.isEmpty() || unlockedWarps.contains(unlockWarp))
            {
                return SpellResult.NO_ACTION;
            }

            if (warpConfig == null && commandConfig == null)
            {
                return SpellResult.FAIL;
            }

            unlockedWarps.add(unlockWarp);
            unlockedString = StringUtils.join(unlockedWarps, ",");
            mageData.set(unlockKey, unlockedString);

            String warpName = unlockWarp;
            ConfigurationSection config = warpConfig == null ? null : warpConfig.getConfigurationSection(unlockWarp);
            if (config != null)
            {
                warpName = config.getString("name", warpName);
            }
            else
            {
                config = commandConfig == null ? null : commandConfig.getConfigurationSection(unlockWarp);
                if (config != null)
                {
                    warpName = config.getString("name", warpName);
                }
            }
            warpName = ChatColor.translateAlternateColorCodes('&', warpName);
            String unlockMessage = context.getMessage("unlock_warp").replace("$name", warpName);
            context.sendMessage(unlockMessage);

            return SpellResult.CAST;
        }

        if (parameters.contains("lock"))
        {
            String lockWarpString = parameters.getString("lock");
            String[] lockWarps = StringUtils.split(lockWarpString, ',');
            boolean locked = false;
            for (String lockWarp : lockWarps)
            {
                if (unlockedWarps.contains(lockWarp))
                {
                    locked = true;
                    unlockedWarps.remove(lockWarp);
                }
            }

            if (locked) {
                unlockedString = StringUtils.join(unlockedWarps, ",");
                mageData.set(unlockKey, unlockedString);
            }

            return locked ? SpellResult.DEACTIVATE : SpellResult.NO_ACTION;
        }

        if (parameters.contains("addfriend"))
        {
            String friendName = parameters.getString("addfriend");
            if (friendName == null || friendName.isEmpty())
            {
                return SpellResult.NO_ACTION;
            }

            Player online = DeprecatedUtils.getPlayer(friendName);
            if (online == null)
            {
                return SpellResult.FAIL;
            }

            String uuid = online.getUniqueId().toString();
            if (friends.contains(uuid))
            {
                return SpellResult.NO_ACTION;
            }

            friends.add(uuid);
            friendString = StringUtils.join(friends, ",");
            mageData.set(friendKey, friendString);

            String message = context.getMessage("add_friend").replace("$name", online.getDisplayName());
            context.sendMessage(message);

            return SpellResult.CAST;
        }

        if (parameters.contains("removefriend"))
        {
            String friendName = parameters.getString("removefriend");
            Player online = DeprecatedUtils.getPlayer(friendName);
            if (online == null)
            {
                return SpellResult.FAIL;
            }

            String uuid = online.getUniqueId().toString();
            if (!friends.contains(uuid))
            {
                return SpellResult.NO_ACTION;
            }

            friends.remove(uuid);

            friendString = StringUtils.join(friends, ",");
            mageData.set(friendKey, friendString);

            String message = context.getMessage("remove_friend").replace("$name", online.getDisplayName());
            context.sendMessage(message);

            return SpellResult.DEACTIVATE;
        }

        Location playerLocation = mage.getLocation();
        for (RecallType testType : RecallType.values())
        {
            // Special-case for warps
            if (testType == RecallType.WARP)
            {
                if (warpConfig != null)
                {
                    Collection<String> warpKeys = warpConfig.getKeys(false);
                    for (String warpKey : warpKeys)
                    {
                        ConfigurationSection config = warpConfig.getConfigurationSection(warpKey);
                        boolean isLocked = config.getBoolean("locked", false);
                        isLocked = isLocked && !unlockedWarps.contains(warpKey);
                        String permission = config.getString("permission");
                        boolean hasPermission = permission == null || player.hasPermission(permission);
                        if (!isLocked && hasPermission)
                        {
                            warps.put(warpKey, config);
                        }
                    }
                }
            }
            // Special-case for commands
            else if (testType == RecallType.COMMAND)
            {
                if (commandConfig != null)
                {
                    Collection<String> commandKeys = commandConfig.getKeys(false);
                    for (String commandKey : commandKeys)
                    {
                        ConfigurationSection config = commandConfig.getConfigurationSection(commandKey);
                        boolean isLocked = config.getBoolean("locked", false);
                        isLocked = isLocked && !unlockedWarps.contains(commandKey);
                        String permission = config.getString("permission");
                        boolean hasPermission = permission == null || player.hasPermission(permission);
                        if (!isLocked && hasPermission)
                        {
                            commands.put(commandKey, config);
                        }
                    }
                }
            }
            else
            {
                if (parameters.getBoolean("allow_" + testType.name().toLowerCase(), true))
                {
                    enabledTypes.add(testType);
                }
            }
        }

        if (warps.size() > 0)
        {
            enabledTypes.add(RecallType.WARP);
        }

        if (commands.size() > 0)
        {
            enabledTypes.add(RecallType.COMMAND);
        }

        if (parameters.contains("warp"))
        {
            String warpName = parameters.getString("warp");
            Waypoint waypoint = getWarp(warpName);
            if (tryTeleport(player, waypoint)) {
                return SpellResult.CAST;
            }
            return SpellResult.FAIL;
        }
        else
        if (parameters.contains("command"))
        {
            String commandName = parameters.getString("command");
            Waypoint waypoint = getCommand(context, commandName);
            if (tryTeleport(player, waypoint)) {
                return SpellResult.CAST;
            }
            return SpellResult.FAIL;
        }
        else if (parameters.contains("type"))
        {
            String typeString = parameters.getString("type", "");
            if (parameters.getBoolean("allow_marker", true))
            {
                if (typeString.equalsIgnoreCase("remove"))
                {
                    if (removeMarker())
                    {
                        return SpellResult.TARGET_SELECTED;
                    }
                    return SpellResult.FAIL;
                }

                if (typeString.equalsIgnoreCase("place"))
                {
                    Block block = context.getLocation().getBlock();
                    if (parameters.getBoolean("marker_requires_build", true) && !context.hasBuildPermission(block))
                    {
                        return SpellResult.NO_TARGET;
                    }
                    if (hasMarker() && parameters.getBoolean("confirm_marker", true))
                    {
                        showMarkerConfirm(context);
                        return SpellResult.CAST;
                    }
                    if (placeMarker(block))
                    {
                        return SpellResult.TARGET_SELECTED;
                    }

                    return SpellResult.FAIL;
                }
            }

            RecallType recallType = RecallType.valueOf(typeString.toUpperCase());

            Waypoint location = getWaypoint(player, recallType, 0, parameters, context);
            if (tryTeleport(player, location)) {
                return SpellResult.CAST;
            }
            return SpellResult.FAIL;
        }

        List<Waypoint> allWaypoints = new ArrayList<>();
        for (RecallType selectedType : enabledTypes) {
            if (selectedType == RecallType.FRIENDS) {
                for (String friendId : friends) {
                    Waypoint targetLocation = getFriend(friendId);
                    if (targetLocation != null && targetLocation.isValid(allowCrossWorld, playerLocation)) {
                        allWaypoints.add(targetLocation);
                    }
                }
            } else if (selectedType == RecallType.WARP) {
                for (String warpKey : warps.keySet()) {
                    Waypoint targetLocation = getWarp(warpKey);
                    if (targetLocation != null && targetLocation.isValid(allowCrossWorld, playerLocation)) {
                        allWaypoints.add(targetLocation);
                    }
                }
            } else if (selectedType == RecallType.COMMAND) {
                for (String commandKey : commands.keySet()) {
                    Waypoint targetLocation = getCommand(context, commandKey);
                    if (targetLocation != null && targetLocation.isValid(allowCrossWorld, playerLocation)) {
                        allWaypoints.add(targetLocation);
                    }
                }
            } else if (selectedType == RecallType.WAND) {
                List<LostWand> lostWands = mage.getLostWands();
                for (int i = 0; i < lostWands.size(); i++) {
                    Waypoint targetLocation = getWaypoint(player, selectedType, i, parameters, context);
                    if (targetLocation != null && targetLocation.isValid(allowCrossWorld, playerLocation)) {
                        allWaypoints.add(targetLocation);
                    }
                }
            }
            else if (selectedType == RecallType.FIELDS)
            {
                Map<String, Location> fields = controller.getHomeLocations(player);
                if (fields != null) {
                    for (Map.Entry<String, Location> fieldEntry : fields.entrySet()) {
                        Location location = fieldEntry.getValue().clone();
                        location.setX(location.getX() + 0.5);
                        location.setZ(location.getZ() + 0.5);
                        allWaypoints.add(new Waypoint(RecallType.FIELDS, location,
                                fieldEntry.getKey(),
                                context.getMessage("cast_field"),
                                context.getMessage("no_target_field"),
                                context.getMessage("description_field", ""),
                                getIcon(context, parameters, "icon_field"),
                                true));
                    }
                }
            } else {
                Waypoint targetLocation = getWaypoint(player, selectedType, 0, parameters, context);
                if (targetLocation != null && targetLocation.isValid(allowCrossWorld, playerLocation)) {
                    allWaypoints.add(targetLocation);
                }
            }
        }
        if (allWaypoints.size() == 0) {
            return SpellResult.NO_TARGET;
        }

        options.clear();
        Collections.sort(allWaypoints);
        String inventoryTitle = context.getMessage("title", "Recall");
        int invSize = (int)Math.ceil(allWaypoints.size() / 9.0f) * 9;
        Inventory displayInventory = CompatibilityUtils.createInventory(null, invSize, inventoryTitle);
        int index = 0;
        for (Waypoint waypoint : allWaypoints)
        {
            ItemStack waypointItem;
            if (waypoint.iconURL != null && !waypoint.iconURL.isEmpty()) {
                waypointItem = controller.getURLSkull(waypoint.iconURL);
            } else {
                waypointItem = waypoint.icon.getItemStack(1);
            }
            ItemMeta meta = waypointItem == null ? null : waypointItem.getItemMeta();
            if (meta == null) {
                waypointItem = new ItemStack(DefaultWaypointMaterial);
                meta = waypointItem.getItemMeta();
                controller.getLogger().warning("Invalid waypoint icon for " + waypoint.name);
            }
            meta.setDisplayName(waypoint.name);
            if (waypoint.description != null && waypoint.description.length() > 0)
            {
                List<String> lore = new ArrayList<>();
                InventoryUtils.wrapText(waypoint.description, lore);
                meta.setLore(lore);
            }
            waypointItem.setItemMeta(meta);
            waypointItem = InventoryUtils.makeReal(waypointItem);
            InventoryUtils.hideFlags(waypointItem, 63);
            InventoryUtils.setMeta(waypointItem, "waypoint", "true");
            CompatibilityUtils.makeUnbreakable(waypointItem);
            displayInventory.setItem(index, waypointItem);
            options.put(index, waypoint);
            index++;
        }
        mage.activateGUI(this, displayInventory);

        return SpellResult.CAST;
    }

    @Nullable
    protected Waypoint getUnknownWarp(String warpKey) {
        MageController controller = context.getController();
        Location warpLocation = controller.getWarp(warpKey);
        if (warpLocation == null || warpLocation.getWorld() == null) {
            return null;
        }

        String castMessage = context.getMessage("cast_warp").replace("$name", warpKey);
        String failMessage = context.getMessage("no_target_warp").replace("$name", warpKey);
        return new Waypoint(RecallType.WARP, warpLocation, warpKey, castMessage, failMessage, "", null, null);
    }

    protected void showMarkerConfirm(CastContext context)
    {
        options.clear();
        String inventoryTitle = context.getMessage("move_marker_title", "Move Marker");
        Inventory displayInventory = CompatibilityUtils.createInventory(null, 9, inventoryTitle);
        MaterialAndData iconType = getIcon(context, parameters, "icon_move_marker");
        ItemStack markerItem = iconType.getItemStack(1);

        ItemMeta meta = markerItem.getItemMeta();
        meta.setDisplayName(context.getMessage("title_move_marker"));
        String description = context.getMessage("description_move_marker");
        if (description != null && description.length() > 0)
        {
            List<String> lore = new ArrayList<>();
            lore.add(description);
            meta.setLore(lore);
        }
        markerItem.setItemMeta(meta);
        markerItem = InventoryUtils.makeReal(markerItem);
        InventoryUtils.hideFlags(markerItem, 63);
        InventoryUtils.setMeta(markerItem, "move_marker", "true");

        displayInventory.setItem(4, markerItem);
        context.getMage().activateGUI(this, displayInventory);
    }

    @Nullable
    protected Waypoint getFriend(String uuid) {
        Player onlinePlayer = Bukkit.getPlayer(UUID.fromString(uuid));
        if (onlinePlayer == null) return null;

        String playerName = onlinePlayer.getDisplayName();
        String castMessage = context.getMessage("cast_friend").replace("$name", playerName);
        String failMessage = context.getMessage("no_target_friend").replace("$name", playerName);
        String title = context.getMessage("title_friend", "$name").replace("$name", playerName);
        String iconURL = SkinUtils.getOnlineSkinURL(onlinePlayer);

        return new Waypoint(RecallType.WARP, onlinePlayer.getLocation(), title, castMessage, failMessage, "", null, iconURL);
    }

    @Nullable
    protected Waypoint getWarp(String warpKey) {
        if (warps == null) return getUnknownWarp(warpKey);
        ConfigurationSection config = warps.get(warpKey);
        if (config == null) return getUnknownWarp(warpKey);

        MageController controller = context.getController();
        String warpName = config.getString("name", warpKey);
        String castMessage = context.getMessage("cast_warp").replace("$name", warpName);
        String failMessage = context.getMessage("no_target_warp").replace("$name", warpName);
        String title = context.getMessage("title_warp", "$name").replace("$name", warpName);
        String description = config.getString("description");
        String iconURL = config.getString("icon_url");
        MaterialAndData icon = getIcon(context, config, "icon");

        Location warpLocation = controller.getWarp(warpKey);
        if (warpLocation == null || warpLocation.getWorld() == null) {
            String serverName = config.getString("server", null);
            if (serverName != null) {
                return new Waypoint(RecallType.WARP, warpKey, serverName, title, castMessage, failMessage, description, icon, iconURL);
            }

            return null;
        }

        return new Waypoint(RecallType.WARP, warpLocation, title, castMessage, failMessage, description, icon, iconURL);
    }

    @Nullable
    protected Waypoint getCommand(CastContext context, String commandKey) {
        if (commands == null) return null;
        ConfigurationSection config = commands.get(commandKey);
        if (config == null) return null;

        String commandName = config.getString("name", commandKey);
        String castMessage = context.getMessage("cast_warp").replace("$name", commandName);
        String failMessage = context.getMessage("no_target_warp").replace("$name", commandName);
        String title = context.getMessage("title_warp").replace("$name", commandName);
        String description = config.getString("description");
        String iconURL = config.getString("icon_url");
        String command = context.parameterize(config.getString("command"));
        boolean op = config.getBoolean("op", false);
        boolean console = config.getBoolean("console", false);
        MaterialAndData icon = getIcon(context, config, "icon");
        return new Waypoint(RecallType.COMMAND, command, op, console, title, castMessage, failMessage, description, icon, iconURL);
    }

    @Nullable
    protected Waypoint getWaypoint(Player player, RecallType type, int index, ConfigurationSection parameters, CastContext context) {
        Mage mage = context.getMage();
        MageController controller = context.getController();
        switch (type) {
        case MARKER:
            Location location = ConfigurationUtils.getLocation(mage.getData(), markerKey);
            return new Waypoint(type, location, context.getMessage("title_marker"), context.getMessage("cast_marker", "Market"), context.getMessage("no_target_marker"), context.getMessage("description_marker", ""), getIcon(context, parameters, "icon_marker"), true);
        case DEATH:
            Waypoint death = new Waypoint(type, mage.getLastDeathLocation(), "Last Death", context.getMessage("cast_death", "Last Death"), context.getMessage("no_target_death"), context.getMessage("description_death", ""), getIcon(context, parameters, "icon_death"), true);
            death.safe = false;
            return death;
        case SPAWN:
            return new Waypoint(type, context.getWorld().getSpawnLocation(), context.getMessage("title_spawn", "Spawn"), context.getMessage("cast_spawn"), context.getMessage("no_target_spawn"), context.getMessage("description_spawn", ""), getIcon(context, parameters, "icon_spawn"), false);
        case TOWN:
            return new Waypoint(type, controller.getTownLocation(player), context.getMessage("title_town", "Town"), context.getMessage("cast_town"), context.getMessage("no_target_town"), context.getMessage("description_town", ""), getIcon(context, parameters, "icon_town"), false);
        case HOME:
            Location bedLocation = player == null ? null : player.getBedSpawnLocation();
            if (bedLocation != null) {
                bedLocation.setX(bedLocation.getX() + 0.5);
                bedLocation.setZ(bedLocation.getZ() + 0.5);
                bedLocation.setY(bedLocation.getY() + 1);
            }
            return new Waypoint(type, bedLocation, context.getMessage("title_home", "Home"), context.getMessage("cast_home"), context.getMessage("no_target_home"), context.getMessage("description_home", ""), getIcon(context, parameters, "icon_home"), false);
        case WAND:
            List<LostWand> lostWands = mage.getLostWands();
            if (lostWands == null || index < 0 || index >= lostWands.size()) return null;
            return new Waypoint(type, lostWands.get(index).getLocation(), context.getMessage("title_wand", "Lost Wand"), context.getMessage("cast_wand"), context.getMessage("no_target_wand"), context.getMessage("description_wand", ""), getIcon(context, parameters, "icon_wand"), true);
        default:
            return null;
        }
    }

    @Nullable
    protected MaterialAndData getIcon(CastContext context, ConfigurationSection parameters, String key) {
        String iconKey = parameters.getString(key);
        if (iconKey == null || iconKey.isEmpty()) return null;

        MaterialAndData material = ConfigurationUtils.getMaterialAndData(parameters, key);
        if (material == null || !material.isValid() || material.getMaterial() == null)
        {
            context.getLogger().warning("Invalid material specified for " + context.getSpell().getKey() + " " + key + ": " + iconKey);
            return null;
        }
        return material;
    }

    protected boolean removeMarker()
    {
        Mage mage = context.getMage();
        ConfigurationSection mageData = mage.getData();
        Location location = ConfigurationUtils.getLocation(mageData, markerKey);
        if (location == null) return false;
        mageData.set(markerKey, null);
        return true;
    }

    protected boolean hasMarker()
    {
        Mage mage = context.getMage();
        ConfigurationSection mageData = mage.getData();
        Location location = ConfigurationUtils.getLocation(mageData, markerKey);
        return location != null;
    }

    protected boolean tryTeleport(final Player player, final Waypoint waypoint) {
        Mage mage = context.getMage();
        if (waypoint == null) return false;
        if (waypoint.isCommand()) {
            if (waypoint.asConsole) {
                try {
                    player.getServer().dispatchCommand(Bukkit.getConsoleSender(), waypoint.command);
                } catch (Exception ex) {
                    context.getLogger().log(Level.WARNING, "Error running command as console " + waypoint.command, ex);
                }
            } else {
                CommandSender sender = mage.getCommandSender();
                boolean isOp = sender.isOp();
                if (waypoint.opPlayer && !isOp) {
                    sender.setOp(true);
                }
                try {
                    player.getServer().dispatchCommand(sender, waypoint.command);
                } catch (Exception ex) {
                    context.getLogger().log(Level.WARNING, "Error running command " + waypoint.command, ex);
                }
                if (waypoint.opPlayer && !isOp) {
                    sender.setOp(false);
                }
            }
            mage.enableSuperProtection(protectionTime);
            return true;
        }

        Location targetLocation = waypoint.location;
        if (targetLocation == null) {
                String serverName = waypoint.serverName;
                String warpName = waypoint.warpName;
                if (warpName != null && serverName != null) {
                    context.getController().warpPlayerToServer(player, serverName, warpName);
                } else {
                    context.sendMessage(waypoint.failMessage);
                }
            return false;
        }
        if (!allowCrossWorld && !mage.getLocation().getWorld().equals(targetLocation.getWorld())) {
            context.sendMessageKey("cross_world_disallowed");
            return false;
        }

        if (waypoint.maintainDirection)
        {
            Location playerLocation = player.getLocation();
            targetLocation.setYaw(playerLocation.getYaw());
            targetLocation.setPitch(playerLocation.getPitch());
        }
        mage.enableSuperProtection(protectionTime);
        if (context.teleport(player, targetLocation, verticalSearchDistance, waypoint.safe, waypoint.safe)) {
            context.castMessage(waypoint.message);
        } else {
            context.castMessage(waypoint.failMessage);
        }
        return true;
    }

    protected boolean placeMarker(Block target)
    {
        if (target == null)
        {
            return false;
        }

        Mage mage = context.getMage();
        ConfigurationSection mageData = mage.getData();
        Location location = ConfigurationUtils.getLocation(mageData, markerKey);

        context.registerForUndo(new UndoMarkerMove(mage, location));
        if (location != null)
        {
            context.sendMessageKey("cast_marker_move");
        }
        else
        {
            context.sendMessageKey("cast_marker_place");
        }

        location = context.getLocation();
        location.setX(target.getX() + 0.5);
        location.setY(target.getY());
        location.setZ(target.getZ() + 0.5);

        mageData.set(markerKey, ConfigurationUtils.fromLocation(location));
        return true;
    }
}
