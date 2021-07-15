package com.elmakers.mine.bukkit.action.builtin;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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
import org.bukkit.entity.Entity;
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
import com.elmakers.mine.bukkit.api.magic.DeathLocation;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.protection.PlayerWarp;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.wand.LostWand;
import com.elmakers.mine.bukkit.api.warp.Warp;
import com.elmakers.mine.bukkit.block.DefaultMaterials;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.warp.MagicWarp;

public class RecallAction extends BaseTeleportAction implements GUIAction
{
    private static final Material DefaultWaypointMaterial = Material.BEACON;
    private boolean allowCrossWorld = true;
    private List<RecallType> enabledTypes = new ArrayList<>();
    private List<Waypoint> options = new ArrayList<>();
    private CastContext context;
    private ConfigurationSection parameters;
    private int protectionTime;
    private String markerKey = "recall_marker";
    private String unlockKey = "recall_warps";
    private String friendKey = "recall_friends";
    private int markerCount = 1;
    private int delay = 0;
    private boolean teleport = true;
    private boolean teleportTarget = false;
    private String titleKey = null;

    private boolean isActive = false;
    private long delayExpiration = 0;
    private Waypoint selectedWaypoint;
    private Location pendingTeleport = null;

    private static class UndoMarkerMove implements Runnable
    {
        private final Location location;
        private final WeakReference<Mage> mage;
        private final String markerKey;

        public UndoMarkerMove(Mage mage, Location currentLocation, String markerKey)
        {
            this.location = currentLocation;
            this.mage = new WeakReference<>(mage);
            this.markerKey = markerKey;
        }

        @Override
        public void run()
        {
            Mage mage = this.mage.get();
            if (mage != null) {
                mage.getData().set(markerKey, ConfigurationUtils.fromLocation(location));
            }
        }
    }

    private enum RecallType
    {
        REGIONS,
        ALL_REGIONS,
        DEATH,
        SOULS,
        SPAWN,
        TOWN,
        HOME,
        MARKER,
        WAND,
        COMMAND,
        WARP,
        FRIENDS,
        REMOVE_FRIENDS(false),

        PLACEHOLDER;

        public final boolean showByDefault;

        RecallType() {
            showByDefault = true;
        }

        RecallType(boolean show) {
            showByDefault = show;
        }
    }

    private static MaterialAndData defaultMaterial = new MaterialAndData(DefaultWaypointMaterial);

    private String getMarkerKey(int markerNumber) {
        String key = markerKey;
        if (markerNumber > 1) {
            key += markerNumber;
        }
        return key;
    }

    private class Waypoint implements Comparable<Waypoint>
    {
        public final RecallType type;
        public final String name;
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
        public final int markerNumber;
        public UUID friendId;
        public boolean unavailable = false;

        // Ok so I got sick of making these final with the zillion different constructors :|
        // These only work with the new-stype "options"
        public String description;
        public boolean showUnavailable;
        public String unavailableMessage;
        public MaterialAndData unavailableIcon;
        public boolean safe = true;
        public boolean locked = false;
        public String permission;

        public Waypoint(RecallType type, Location location, String name, String message, String failMessage, String description, MaterialAndData icon, boolean maintainDirection) {
            this.name = CompatibilityLib.getCompatibilityUtils().translateColors(name);
            this.type = type;
            this.location = location;
            this.message = message;
            this.description = description == null ? null : CompatibilityLib.getCompatibilityUtils().translateColors(description);
            this.failMessage = failMessage;
            this.icon = icon == null ? defaultMaterial : icon;
            this.iconURL = null;
            this.command = null;
            this.opPlayer = false;
            this.asConsole = false;
            this.maintainDirection = maintainDirection;
            this.markerNumber = 0;
            serverName = null;
            warpName = null;
        }

        public Waypoint(RecallType type, Location location, String name, String message, String failMessage, String description, MaterialAndData icon, String iconURL) {
            this.name = CompatibilityLib.getCompatibilityUtils().translateColors(name);
            this.type = type;
            this.location = location;
            this.message = message;
            this.description = description == null ? null : CompatibilityLib.getCompatibilityUtils().translateColors(description);
            this.failMessage = failMessage;
            this.icon = icon == null ? defaultMaterial : icon;
            this.iconURL = iconURL;
            this.command = null;
            this.opPlayer = false;
            this.asConsole = false;
            this.maintainDirection = false;
            this.markerNumber = 0;
            serverName = null;
            warpName = null;
        }

        public Waypoint(RecallType type, String warpName, String serverName, String name, String message, String failMessage, String description, MaterialAndData icon, String iconURL) {
            this.name = CompatibilityLib.getCompatibilityUtils().translateColors(name);
            this.type = type;
            this.location = null;
            this.warpName = warpName;
            this.serverName = serverName;
            this.message = message;
            this.description = description == null ? null : CompatibilityLib.getCompatibilityUtils().translateColors(description);;
            this.failMessage = failMessage;
            this.icon = icon == null ? defaultMaterial : icon;
            this.iconURL = iconURL;
            this.command = null;
            this.opPlayer = false;
            this.asConsole = false;
            this.maintainDirection = false;
            this.markerNumber = 0;
        }

        public Waypoint(RecallType type, String command, boolean opPlayer, boolean asConsole, String name, String message, String failMessage, String description, MaterialAndData icon, String iconURL) {
            this.name = CompatibilityLib.getCompatibilityUtils().translateColors(name);
            this.type = type;
            this.location = null;
            this.message = message;
            this.description = description == null ? null : CompatibilityLib.getCompatibilityUtils().translateColors(description);;
            this.failMessage = failMessage;
            this.icon = icon == null ? defaultMaterial : icon;
            this.iconURL = iconURL;
            this.command = command;
            this.opPlayer = opPlayer;
            this.asConsole = asConsole;
            this.maintainDirection = false;
            this.markerNumber = 0;
            serverName = null;
            warpName = null;
        }

        public Waypoint(CastContext context, ConfigurationSection configuration) {
            warpName = configuration.getString("warp", "");
            command = configuration.getString("command");
            if (command != null) {
                type = RecallType.COMMAND;
                serverName = null;
            } else if (!warpName.isEmpty()) {
                type = RecallType.WARP;
                serverName = configuration.getString("server");
            } else {
                RecallType parsedType;
                try {
                    parsedType = RecallType.valueOf(configuration.getString("type", "placeholder").toUpperCase());
                } catch (Exception ex) {
                    parsedType = RecallType.PLACEHOLDER;
                }
                type = parsedType;
                serverName = null;
            }

            // Determine default messages and icon based on type
            // Note that the default icon should not be set here, in case it is a placeholder.
            // The default icon will be used if no other icon matches, at inventory generation time
            Mage mage = context.getMage();
            Player player = mage.getPlayer();
            MaterialAndData defaultIcon = null;
            String defaultTitle = "";
            String defaultMessage = "";
            String defaultFailMessage = "";
            String defaultDescription = "";
            String defaultUnavailableMessage = context.getMessage("unavailable_description", "");
            MaterialAndData defaultUnavailableIcon = ConfigurationUtils.getMaterialAndData(parameters, "unavailable_icon");
            boolean defaultShowUnavailable = parameters.getBoolean("show_unavailable", false);
            Location location = null;
            int markerNumber = 0;
            boolean defaultMaintainDirection = false;
            boolean defaultSafe = true;

            switch (type) {
            case COMMAND:
                defaultTitle = context.getMessage("title_warp", "$name").replace("$name", command);
                defaultMessage = context.getMessage("cast_warp", "");
                defaultFailMessage = context.getMessage("no_target_warp", "");
                defaultUnavailableMessage = parameters.getString("unavailable_warp_description", "");
                break;
            case WARP:
                location = context.getController().getWarp(warpName);
                Warp magicWarp = context.getController().getMagicWarp(warpName);
                String warpLabel = warpName;
                if (magicWarp != null) {
                    warpLabel = magicWarp.getName();
                    String warpDescription = magicWarp.getDescription();
                    if (warpDescription != null) {
                        defaultDescription = warpDescription;
                    }
                    String iconKey = magicWarp.getIcon();
                    if (iconKey != null && !iconKey.isEmpty()) {
                        defaultIcon = new MaterialAndData(iconKey);
                    }
                }

                defaultTitle = context.getMessage("title_warp", "$name").replace("$name", warpLabel);
                defaultMessage = context.getMessage("cast_warp", "");
                defaultFailMessage = context.getMessage("no_target_warp", "");
                defaultUnavailableMessage = context.getMessage("unavailable_warp_description", "");
                break;
            case MARKER:
                markerNumber = configuration.getInt("marker", 1);
                location = ConfigurationUtils.getLocation(mage.getData(), getMarkerKey(markerNumber));
                defaultTitle = context.getMessage("title_marker", "Marker #$number").replace("$number", Integer.toString(markerNumber));
                defaultMessage = context.getMessage("cast_marker", "").replace("$number", Integer.toString(markerNumber));
                defaultFailMessage = context.getMessage("no_target_marker", "").replace("$number", Integer.toString(markerNumber));
                defaultDescription =  context.getMessage("description_marker", "").replace("$number", Integer.toString(markerNumber));
                defaultIcon = getIcon(context, parameters, "icon_marker");
                defaultMaintainDirection = true;
                break;
            case DEATH:
                location = mage.getLastDeathLocation();
                defaultTitle = context.getMessage("title_death", "Last Death");
                defaultMessage = context.getMessage("cast_death", "");
                defaultFailMessage = context.getMessage("no_target_death", "");
                defaultDescription =  context.getMessage("description_death", "");
                String iconString = parameters.getString("icon_death");
                if (iconString.equals("skull_item")) {
                    defaultIcon = DefaultMaterials.getSkeletonSkullItem();
                } else {
                    defaultIcon = getIcon(context, parameters, "icon_death");
                }
                defaultMaintainDirection = true;
                defaultSafe = false;
                break;
            case SPAWN:
                location = context.getWorld().getSpawnLocation();
                defaultTitle = context.getMessage("title_spawn", "Spawn");
                defaultMessage = context.getMessage("cast_spawn", "");
                defaultFailMessage = context.getMessage("no_target_spawn", "");
                defaultDescription =  context.getMessage("description_spawn", "");
                defaultIcon = getIcon(context, parameters, "icon_spawn");
                break;
            case TOWN:
                location = player == null ? null : context.getController().getTownLocation(player);
                defaultTitle = context.getMessage("title_town", "Town");
                defaultMessage = context.getMessage("cast_town", "");
                defaultFailMessage = context.getMessage("no_target_town", "");
                defaultDescription =  context.getMessage("description_town", "");
                defaultIcon = getIcon(context, parameters, "icon_town");
                break;
            case HOME:
                Location bedLocation = CompatibilityLib.getCompatibilityUtils().getBedSpawnLocation(player);
                if (bedLocation != null) {
                    bedLocation = bedLocation.clone();
                    bedLocation.setX(bedLocation.getX() + 0.5);
                    bedLocation.setZ(bedLocation.getZ() + 0.5);
                    bedLocation.setY(bedLocation.getY() + 1);
                }
                location = bedLocation;
                defaultTitle = context.getMessage("title_home", "Home");
                defaultMessage = context.getMessage("cast_home", "");
                defaultFailMessage = context.getMessage("no_target_home", "");
                defaultDescription =  context.getMessage("description_home", "");
                defaultIcon = getIcon(context, parameters, "icon_home");
                break;
            default:
                break;
            }

            String optionName = configuration.getString("name", defaultTitle);
            name = CompatibilityLib.getCompatibilityUtils().translateColors(optionName);
            message = configuration.getString("message",  defaultMessage).replace("$name", name);;
            description = CompatibilityLib.getCompatibilityUtils().translateColors(configuration.getString("description", defaultDescription));
            failMessage = configuration.getString("fail_message", defaultFailMessage).replace("$name", name);
            icon = ConfigurationUtils.getMaterialAndData(configuration, "icon", defaultIcon);
            iconURL = configuration.getString("icon_url");
            opPlayer = configuration.getBoolean("op");
            asConsole = configuration.getBoolean("console");
            maintainDirection = configuration.getBoolean("keep_direction", defaultMaintainDirection);
            safe = configuration.getBoolean("safe", defaultSafe);
            showUnavailable = configuration.getBoolean("show_unavailable", defaultShowUnavailable);
            unavailableIcon = ConfigurationUtils.getMaterialAndData(configuration, "icon_disabled", defaultUnavailableIcon);
            unavailableMessage = configuration.getString("unavailable_description", defaultUnavailableMessage);
            locked = configuration.getBoolean("locked", false);
            permission = configuration.getString("permission");
            this.location = location;
            this.markerNumber = markerNumber;
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
        // Check for waypoint items glitched into the player's inventory
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
        if (CompatibilityLib.getNBTUtils().hasMeta(item, "placeholder") || CompatibilityLib.getNBTUtils().hasMeta(item, "unavailable"))
        {
            context.getMage().deactivateGUI();
            return;
        }
        if (CompatibilityLib.getNBTUtils().hasMeta(item, "move_marker"))
        {
            int markerNumber = CompatibilityLib.getNBTUtils().getMetaInt(item, "move_marker", 1);
            if (placeMarker(context.getLocation().getBlock(), markerNumber))
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
            Waypoint waypoint = slot < 0 || slot >= options.size() ? null : options.get(slot);
            if (waypoint != null && !waypoint.unavailable)
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
        this.markerCount = parameters.getInt("marker_count", 1);
        this.teleport = parameters.getBoolean("teleport", true);
        this.teleportTarget = parameters.getBoolean("teleport_target", false);
        this.delay = parameters.getInt("delay", 0);
        this.delay = parameters.getInt("warmup", this.delay);
        titleKey = parameters.getString("title_key", "title");

        allowCrossWorld = parameters.getBoolean("allow_cross_world", true);
    }

    @Override
    public SpellResult perform(CastContext context) {
        if (pendingTeleport != null) {
            return doTeleport();
        }
        if (isActive) {
            if (context.getMage().getActiveGUI() != this) {
                if (context.getTargetLocation() == null) {
                    isActive = false;
                    return SpellResult.NO_TARGET;
                }
                if (delayExpiration > 0 && System.currentTimeMillis() < delayExpiration) {
                    return SpellResult.PENDING;
                }
                if (delayExpiration == 0 && delay > 0) {
                    context.playEffects("wait");
                    delayExpiration = System.currentTimeMillis() + delay;
                    return SpellResult.PENDING;
                }
                isActive = false;
                if (teleport) {
                    return doTeleport();
                }
                return SpellResult.CAST;
            }
            return SpellResult.PENDING;
        }
        this.context = context;
        enabledTypes.clear();
        options.clear();

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

        if (parameters.getBoolean("allow_magic_warps", true) && controller instanceof MagicController) {
            String group = parameters.getString("group", null);
            MagicController magic = (MagicController)controller;
            Collection<MagicWarp> warps = magic.getWarps().getMagicWarps();
            if (!warps.isEmpty() && warpConfig == null) {
                warpConfig = ConfigurationUtils.newConfigurationSection();
            }
            for (MagicWarp warp : warps) {
                String icon = warp.getIcon();
                if (icon == null || icon.isEmpty()) continue;
                String warpGroup = warp.getGroup();
                if (group != null && !group.isEmpty() && warpGroup != null && !warpGroup.isEmpty() && !warpGroup.equalsIgnoreCase(group)) continue;
                ConfigurationSection magicWarpConfig = ConfigurationUtils.newConfigurationSection();
                magicWarpConfig.set("name", warp.getName());
                magicWarpConfig.set("icon", icon);
                magicWarpConfig.set("description", warp.getDescription());
                magicWarpConfig.set("locked", warp.isLocked());
                warpConfig.set(warp.getKey(), magicWarpConfig);
            }
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
            warpName = CompatibilityLib.getCompatibilityUtils().translateColors(warpName);
            String unlockMessage = context.getMessage("unlock_warp").replace("$name", warpName);
            context.sendMessageKey("unlock_warp", unlockMessage);

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


            Player online = null;
            if (friendName.equals("target")) {
                Entity targetEntity = context.getTargetEntity();
                if (targetEntity != null && targetEntity instanceof Player) {
                    online = (Player)targetEntity;
                }
            } else if (friendName.equals("source")) {
                Entity sourceEntity = context.getEntity();
                if (sourceEntity != null && sourceEntity instanceof Player) {
                    online = (Player)sourceEntity;
                }
            } else {
               online = CompatibilityLib.getDeprecatedUtils().getPlayer(friendName);
            }
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
            context.sendMessageKey("add_friend", message);

            return SpellResult.CAST;
        }

        if (parameters.contains("removefriend"))
        {
            String friendName = parameters.getString("removefriend");
            Player online = CompatibilityLib.getDeprecatedUtils().getPlayer(friendName);
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
            context.sendMessageKey("remove_friend", message);

            return SpellResult.DEACTIVATE;
        }

        // Add configured options
        Location playerLocation = mage.getLocation();
        Set<RecallType> optionTypes = new HashSet<>();
        Collection<ConfigurationSection> optionConfiguration = ConfigurationUtils.getNodeList(parameters, "options");
        if (optionConfiguration != null) {
            for (ConfigurationSection optionConfig : optionConfiguration) {
                Waypoint newWaypoint = new Waypoint(context, optionConfig);
                options.add(newWaypoint);
                optionTypes.add(newWaypoint.type);
            }
        }

        // Automatically append enabled types if not defined in options
        boolean allowAll = parameters.getBoolean("allow_all", true);
        for (RecallType testType : RecallType.values()) {
            if (!optionTypes.contains(testType) && parameters.getBoolean("allow_" + testType.name().toLowerCase(), allowAll && testType.showByDefault)) {
                switch (testType) {
                    case FRIENDS:
                        for (String friendId : friends) {
                            Waypoint targetLocation = getFriend(friendId);
                            if (targetLocation != null && targetLocation.isValid(allowCrossWorld, playerLocation)) {
                                options.add(targetLocation);
                            }
                        }
                        break;
                    case REMOVE_FRIENDS:
                        for (String friendId : friends) {
                            Waypoint targetFriend = getRemoveFriend(friendId);
                            if (targetFriend != null) {
                                options.add(targetFriend);
                            }
                        }
                        break;
                    case WARP:
                        // Legacy warp config
                        if (warpConfig != null) {
                            Collection<String> warpKeys = warpConfig.getKeys(false);
                            for (String warpKey : warpKeys) {
                                ConfigurationSection config = warpConfig.getConfigurationSection(warpKey);
                                config.set("warp", warpKey);
                                Waypoint warp = new Waypoint(context, config);
                                options.add(warp);
                            }
                        }
                        break;
                    case COMMAND:
                        // Legacy command config
                        if (commandConfig != null) {
                            Collection<String> commandKeys = commandConfig.getKeys(false);
                            for (String commandKey : commandKeys) {
                                ConfigurationSection config = commandConfig.getConfigurationSection(commandKey);
                                Waypoint command = new Waypoint(context, config);
                                options.add(command);
                            }
                        }
                        break;
                    case WAND:
                        List<LostWand> lostWands = mage.getLostWands();
                        for (LostWand lostWand : lostWands) {
                            Waypoint targetLocation = getWaypoint(player, testType, lostWand.getLocation(), parameters, context);
                            if (targetLocation != null && targetLocation.isValid(allowCrossWorld, playerLocation)) {
                                options.add(targetLocation);
                            }
                        }
                        break;
                    case SOULS:
                        List<DeathLocation> deathLocations = controller.getDeathLocations(player);
                        if (deathLocations != null) {
                            for (DeathLocation death : deathLocations) {
                                Waypoint targetLocation = getWaypoint(player, testType, death.getLocation(), parameters, context);
                                if (targetLocation != null && targetLocation.isValid(allowCrossWorld, playerLocation)) {
                                    ItemStack[] items = death.getItems();
                                    int itemCount = items == null ? 0 : items.length;
                                    targetLocation.description = targetLocation.description
                                            .replace("$items", Integer.toString(itemCount))
                                            .replace("$xp", Integer.toString(death.getExperiencePoints()));
                                    options.add(targetLocation);
                                }
                            }
                        }
                        break;
                    case ALL_REGIONS:
                    case REGIONS:
                        boolean allRegions = testType == RecallType.ALL_REGIONS;
                        Set<String> warpProviders = controller.getPlayerWarpProviderKeys();
                        for (String key : warpProviders) {
                            String allowKey = allRegions ? "all_" : "";
                            boolean allowDefault = allRegions ? false : true;
                            if (parameters.getBoolean("allow_" + allowKey + key.toLowerCase(), allowDefault)) {
                                Collection<PlayerWarp> warps = allRegions ? controller.getAllPlayerWarps(key) : controller.getPlayerWarps(player, key);
                                if (warps == null) {
                                    break;
                                }
                                for (PlayerWarp warp : warps) {
                                    Location location = warp.getLocation();
                                    if (location == null) continue;
                                    String description = warp.getDescription();
                                    if (description == null) {
                                        description = context.getMessage("description_" + key, context.getMessage("description_regions"));
                                    }
                                    MaterialAndData icon = (MaterialAndData)warp.getIcon();
                                    if (icon == null) {
                                        icon = getIcon(context, parameters, "icon_" + key);
                                        if (icon == null) {
                                            icon = getIcon(context, parameters, "icon_regions");
                                        }
                                    }

                                    Waypoint waypoint = new Waypoint(RecallType.REGIONS, location,
                                        warp.getName(),
                                        context.getMessage("cast_" + key, context.getMessage("cast_regions")),
                                        context.getMessage("no_target_" + key, context.getMessage("no_target_regions")),
                                        description,
                                        icon,
                                        true);
                                    if (waypoint.isValid(allowCrossWorld, playerLocation)) {
                                        options.add(waypoint);
                                    }
                                }
                            }
                        }
                        break;
                    default:
                        Waypoint targetLocation = getWaypoint(player, testType, null, parameters, context);
                        if (targetLocation != null && targetLocation.isValid(allowCrossWorld, playerLocation)) {
                            options.add(targetLocation);
                        }
                        break;
                }
            }
        }

        // Process special commands
        if (parameters.contains("warp")) {
            String warpName = parameters.getString("warp");
            Waypoint waypoint = getWarp(warpName);
            if (tryTeleport(player, waypoint)) {
                if (teleport) {
                    return doTeleport();
                }
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
                    if (placeMarker(block, 1))
                    {
                        return SpellResult.TARGET_SELECTED;
                    }

                    return SpellResult.FAIL;
                }
            }
            RecallType recallType;
            try {
                recallType = RecallType.valueOf(typeString.toUpperCase());;
            } catch (Exception ex) {
                controller.getLogger().warning("Invalid recall type: " + typeString);
                return SpellResult.FAIL;
            }

            Waypoint location = getWaypoint(player, recallType, null, parameters, context);
            if (tryTeleport(player, location)) {
                if (teleport) {
                    return doTeleport();
                }
                return SpellResult.CAST;
            }
            return SpellResult.FAIL;
        }

        if (options.size() == 0) {
            return SpellResult.NO_TARGET;
        }

        String inventoryTitle = context.getMessage(titleKey, parameters.getString("title", "Recall"));
        int invSize = (int)Math.ceil(options.size() / 9.0f) * 9;
        Inventory displayInventory = CompatibilityLib.getCompatibilityUtils().createInventory(null, invSize, inventoryTitle);
        int index = 0;
        for (Waypoint waypoint : options)
        {
            if (waypoint.permission != null && !player.hasPermission(waypoint.permission)) continue;

            boolean isPlaceholder = waypoint.type == RecallType.PLACEHOLDER;
            boolean isValid = !isPlaceholder && waypoint.isValid(allowCrossWorld, playerLocation);

            boolean isUnavailable = false;
            if (!isPlaceholder && !parameters.getBoolean("allow_" + waypoint.type.name().toLowerCase(), true)) {
                isUnavailable = true;
            }
            if (!isUnavailable && waypoint.locked && (waypoint.warpName == null || !unlockedWarps.contains(waypoint.warpName))) {
                if (!waypoint.showUnavailable) {
                    continue;
                }
                isUnavailable = true;
            }
            if (!isValid) {
                isUnavailable = true;
            }
            if (isUnavailable && !waypoint.showUnavailable) {
                waypoint.unavailable = true;
                isPlaceholder = true;
            }

            ItemStack waypointItem = null;
            if (isPlaceholder) {
                String iconPlaceholderKey = parameters.getString("placeholder_icon", "air");
                waypointItem = controller.createItem(iconPlaceholderKey);
                if (waypointItem == null) {
                    waypointItem = new ItemStack(DefaultWaypointMaterial);
                }
            } else if (isUnavailable) {
                if (waypoint.unavailableIcon != null) {
                    waypointItem = waypoint.unavailableIcon.getItemStack(1);
                } else if (waypoint.iconURL != null && !waypoint.iconURL.isEmpty()) {
                    waypointItem = controller.getURLSkull(waypoint.iconURL);
                } else if (waypoint.icon != null) {
                    waypointItem = waypoint.icon.getItemStack(1);
                }
            } else {
                if (waypoint.iconURL != null && !waypoint.iconURL.isEmpty()) {
                    waypointItem = controller.getURLSkull(waypoint.iconURL);
                } else if (waypoint.icon != null) {
                    waypointItem = waypoint.icon.getItemStack(1);
                }
            }
            ItemMeta meta = waypointItem == null ? null : waypointItem.getItemMeta();
            if (meta == null && !isPlaceholder) {
                waypointItem = new ItemStack(DefaultWaypointMaterial);
                meta = waypointItem.getItemMeta();
                controller.getLogger().warning("Invalid waypoint icon for " + waypoint.name);
            }
            if (meta != null) {
                String name = waypoint.name;
                if (!isValid || isUnavailable || isPlaceholder) {
                    name = context.getMessage("unavailable_name").replace("$name", name);
                }
                meta.setDisplayName(name);
                if (waypoint.description != null && waypoint.description.length() > 0)
                {
                    List<String> lore = new ArrayList<>();
                    CompatibilityLib.getInventoryUtils().wrapText(waypoint.description, lore);
                    meta.setLore(lore);
                }
                String invalidMessage = context.getMessage("invalid_description");
                if (!isValid && invalidMessage != null) {
                    List<String> lore = meta.getLore();
                    if (lore == null) {
                        lore = new ArrayList<>();
                    }
                    CompatibilityLib.getInventoryUtils().wrapText(invalidMessage, lore);
                    meta.setLore(lore);
                } else if (isUnavailable && waypoint.unavailableMessage != null && waypoint.unavailableMessage.length() > 0) {
                    List<String> lore = meta.getLore();
                    if (lore == null) {
                        lore = new ArrayList<>();
                    }
                    CompatibilityLib.getInventoryUtils().wrapText(waypoint.unavailableMessage, lore);
                    meta.setLore(lore);
                }
                waypointItem.setItemMeta(meta);
                waypointItem = CompatibilityLib.getItemUtils().makeReal(waypointItem);
                CompatibilityLib.getItemUtils().hideFlags(waypointItem, 63);
                CompatibilityLib.getNBTUtils().setMeta(waypointItem, "waypoint", "true");
                CompatibilityLib.getItemUtils().makeUnbreakable(waypointItem);
                if (isPlaceholder) {
                    CompatibilityLib.getNBTUtils().setMetaBoolean(waypointItem, "placeholder", true);
                }
                if (isUnavailable) {
                    CompatibilityLib.getNBTUtils().setMetaBoolean(waypointItem, "unavailable", true);
                }
            }
            displayInventory.setItem(index, waypointItem);
            index++;

            // The inventory will limit its own size
            if (index >= displayInventory.getSize()) break;
        }
        context.playEffects("menu");
        mage.activateGUI(this, displayInventory);
        isActive = true;
        return SpellResult.PENDING;
    }

    protected void showMarkerConfirm(CastContext context)
    {
        options.clear();
        String inventoryTitle = context.getMessage("move_marker_title", "Move Marker");
        int invSize = (int)Math.ceil(markerCount / 9.0f) * 9;
        Inventory displayInventory = CompatibilityLib.getCompatibilityUtils().createInventory(null, invSize, inventoryTitle);
        MaterialAndData iconType = getIcon(context, parameters, "icon_move_marker");
        if (iconType == null) {
            iconType = new MaterialAndData(Material.STICK);
        }
        int startIndex = 0;
        if (markerCount < 8) {
            startIndex = (9 - markerCount) / 2;
        }
        for (int marker = 1; marker <= markerCount; marker++) {
            int inventoryIndex = startIndex + marker - 1;
            ItemStack markerItem = iconType.getItemStack(1);
            ItemMeta meta = markerItem.getItemMeta();
            meta.setDisplayName(context.getMessage("title_move_marker").replace("$number", Integer.toString(marker)));
            String description = context.getMessage("description_move_marker");
            if (description != null && description.length() > 0)
            {
                List<String> lore = new ArrayList<>();
                lore.add(description);
                meta.setLore(lore);
            }
            markerItem.setItemMeta(meta);
            markerItem = CompatibilityLib.getItemUtils().makeReal(markerItem);
            CompatibilityLib.getItemUtils().hideFlags(markerItem, 63);
            CompatibilityLib.getNBTUtils().setMetaInt(markerItem, "move_marker", marker);

            displayInventory.setItem(inventoryIndex, markerItem);
            context.getMage().activateGUI(this, displayInventory);
        }
    }

    @Nullable
    protected Waypoint getFriend(String uuid) {
        Player onlinePlayer = Bukkit.getPlayer(UUID.fromString(uuid));
        if (onlinePlayer == null) return null;

        String playerName = onlinePlayer.getDisplayName();
        String castMessage = context.getMessage("cast_friend").replace("$name", playerName);
        String failMessage = context.getMessage("no_target_friend").replace("$name", playerName);
        String title = context.getMessage("title_friend", "$name").replace("$name", playerName);
        String iconURL = CompatibilityLib.getSkinUtils().getOnlineSkinURL(onlinePlayer);

        return new Waypoint(RecallType.FRIENDS, onlinePlayer.getLocation(), title, castMessage, failMessage, "", null, iconURL);
    }

    @Nullable
    protected Waypoint getRemoveFriend(String uuid) {
        UUID playerId = UUID.fromString(uuid);
        Player onlinePlayer = Bukkit.getPlayer(playerId);
        if (onlinePlayer == null) return null;

        String playerName = onlinePlayer.getDisplayName();
        String castMessage = context.getMessage("cast_remove_friend").replace("$name", playerName);
        String failMessage = context.getMessage("no_target_remove_friend").replace("$name", playerName);
        String title = context.getMessage("title_remove_friend", "$name").replace("$name", playerName);
        String iconURL = CompatibilityLib.getSkinUtils().getOnlineSkinURL(onlinePlayer);

        Waypoint removeWaypoint = new Waypoint(RecallType.REMOVE_FRIENDS, onlinePlayer.getLocation(), title, castMessage, failMessage, "", null, iconURL);
        removeWaypoint.friendId = playerId;
        return removeWaypoint;
    }

    @Nullable
    protected Waypoint getWarp(String warpName) {
        for (Waypoint waypoint : options) {
            if (waypoint.type == RecallType.WARP && waypoint.warpName.equals(warpName)) {
                return waypoint;
            }
        }
        return null;
    }

    @Nullable
    protected Waypoint getWaypoint(Player player, RecallType type, Location location, ConfigurationSection parameters, CastContext context) {
        Mage mage = context.getMage();
        MageController controller = context.getController();
        switch (type) {
        case MARKER:
            location = ConfigurationUtils.getLocation(mage.getData(), markerKey);
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
            if (bedLocation == null || bedLocation.getWorld() == null) {
                return null;
            }
            bedLocation.setX(bedLocation.getX() + 0.5);
            bedLocation.setZ(bedLocation.getZ() + 0.5);
            bedLocation.setY(bedLocation.getY() + 1);
            return new Waypoint(type, bedLocation, context.getMessage("title_home", "Home"), context.getMessage("cast_home"), context.getMessage("no_target_home"), context.getMessage("description_home", ""), getIcon(context, parameters, "icon_home"), false);
        case WAND:
            return new Waypoint(type, location, context.getMessage("title_wand", "Lost Wand"), context.getMessage("cast_wand"), context.getMessage("no_target_wand"), context.getMessage("description_wand", ""), getIcon(context, parameters, "icon_wand"), true);
        case SOULS:
            return new Waypoint(type, location, context.getMessage("title_soul", "Lost Soul"), context.getMessage("cast_soul"), context.getMessage("no_target_soul"), context.getMessage("description_soul", ""), getIcon(context, parameters, "icon_soul"), false);
        default:
            return null;
        }
    }

    @Nullable
    protected static MaterialAndData getIcon(CastContext context, ConfigurationSection parameters, String key) {
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
        if (waypoint.type == RecallType.REMOVE_FRIENDS) {
            removeFriend(mage, waypoint.friendId);

            // Also remove you from the other players' list
            MageController controller = context.getController();
            Player online = controller.getPlugin().getServer().getPlayer(waypoint.friendId);
            Entity mageEntity = mage.getEntity();
            if (online != null && mageEntity != null) {
                Mage friendMage = controller.getMage(online);
                removeFriend(friendMage, mageEntity.getUniqueId());
            }

            return true;
        }

        Location targetLocation = waypoint.location;
        if (targetLocation == null) {
                String serverName = waypoint.serverName;
                String warpName = waypoint.warpName;
                if (warpName != null && serverName != null) {
                    context.getController().warpPlayerToServer(player, serverName, warpName);
                } else {
                    context.sendMessageKey("teleport_failed", waypoint.failMessage);
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
        if (!teleport) {
            mage.enableSuperProtection(protectionTime);
        }
        context.setTargetLocation(targetLocation);
        selectedWaypoint = waypoint;
        return true;
    }

    protected SpellResult doTeleport() {
        Entity targetEntity;
        Mage mage;
        if (teleportTarget) {
            targetEntity = context.getTargetEntity();
            mage = context.getController().getRegisteredMage(targetEntity);
        } else {
            mage = context.getMage();
            targetEntity = mage.getPlayer();
        }
        if (targetEntity == null) {
            return SpellResult.NO_TARGET;
        }
        Location targetLocation = context.getTargetLocation();
        if (!CompatibilityLib.getCompatibilityUtils().checkChunk(targetLocation)) {
            pendingTeleport = targetLocation;
            return SpellResult.PENDING;
        }
        pendingTeleport = null;
        if (selectedWaypoint == null) {
            context.getLogger().warning("Recall got to doTeleport with no waypoint selected");
            return SpellResult.FAIL;
        }
        context.playEffects("teleporting");
        if (context.teleport(targetEntity, targetLocation, verticalSearchDistance, selectedWaypoint.safe, selectedWaypoint.safe)) {
            context.castMessageKey("teleport", selectedWaypoint.message);
        } else {
            context.sendMessageKey("teleport_failed", selectedWaypoint.failMessage);
            return SpellResult.FAIL;
        }
        if (mage != null) {
            mage.enableSuperProtection(protectionTime);
        }
        return SpellResult.CAST;
    }

    protected boolean placeMarker(Block target, int markerNumber)
    {
        if (target == null)
        {
            return false;
        }

        Mage mage = context.getMage();
        ConfigurationSection mageData = mage.getData();
        String markerKey = getMarkerKey(markerNumber);
        Location location = ConfigurationUtils.getLocation(mageData, markerKey);

        context.registerForUndo(new UndoMarkerMove(mage, location, markerKey));
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

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("addfriend");
        parameters.add("removefriend");
        parameters.add("lock");
        parameters.add("unlock");
        parameters.add("warp");
        parameters.add("type");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("addfriend") || parameterKey.equals("removefriend")) {
            examples.addAll(spell.getController().getPlayerNames());
        }
    }

    @Override
    public void reset(CastContext context) {
        delayExpiration = 0;
        selectedWaypoint = null;
        isActive = false;
    }

    private void removeFriend(Mage mage, UUID playerId) {
        ConfigurationSection mageData = mage.getData();
        Set<String> friends = new HashSet<>();
        String friendString = mageData.getString(friendKey);
        if (friendString != null && !friendString.isEmpty())
        {
            friends.addAll(Arrays.asList(StringUtils.split(friendString, ',')));
        }
        friends.remove(playerId.toString());
        friendString = StringUtils.join(friends, ",");
        mageData.set(friendKey, friendString);
    }
}
