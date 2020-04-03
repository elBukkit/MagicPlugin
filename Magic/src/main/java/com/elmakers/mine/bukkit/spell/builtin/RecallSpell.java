package com.elmakers.mine.bukkit.spell.builtin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.wand.LostWand;
import com.elmakers.mine.bukkit.spell.UndoableSpell;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.Target;

@Deprecated
public class RecallSpell extends UndoableSpell
{
    private static class UndoMarkerMove implements Runnable {
        private final Location location;
        private final RecallSpell spell;

        public UndoMarkerMove(RecallSpell spell, Location currentLocation) {
            this.location = currentLocation;
            this.spell = spell;
        }

        @Override
        public void run() {
            spell.markerLocation = this.location;
        }
    }

    public Location markerLocation;

    private static int MAX_RETRY_COUNT = 8;
    private static int RETRY_INTERVAL = 10;

    private int retryCount = 0;
    private boolean allowCrossWorld = true;
    private int selectedIndex = 0;
    private List<String> warps = new ArrayList<>();

    private RecallType selectedType = RecallType.MARKER;
    private int selectedTypeIndex = 0;
    private List<RecallType> enabledTypes = new ArrayList<>();

    private String castMessage;
    private String failMessage;

    private enum RecallType
    {
        MARKER,
        DEATH,
        SPAWN,
        HOME,
        WAND,
        WARP
        // FHOME,
    }

    private static class Waypoint
    {
        public final RecallType type;
        public  final int index;

        public Waypoint(RecallType type, int index) {
            this.type = type;
            this.index = index;
        }
    }

    @Override
    public SpellResult onCast(ConfigurationSection parameters)
    {
        boolean allowMarker = true;
        selectedTypeIndex = 0;
        int cycleRetries = 5;
        enabledTypes.clear();
        warps = null;

        Player player = mage.getPlayer();
        if (player == null) {
            return SpellResult.PLAYER_REQUIRED;
        }

        allowCrossWorld = parameters.getBoolean("cross_world", true);
        for (RecallType testType : RecallType.values()) {
            // Special-case for warps
            if (testType == RecallType.WARP) {
                if (parameters.contains("allow_warps")) {
                    warps = ConfigurationUtils.getStringList(parameters, "allow_warps");
                    enabledTypes.add(testType);
                    if (testType == selectedType) selectedTypeIndex = enabledTypes.size() - 1;
                }
            } else {
                if (parameters.getBoolean("allow_" + testType.name().toLowerCase(), true)) {
                    enabledTypes.add(testType);
                    if (testType == selectedType) selectedTypeIndex = enabledTypes.size() - 1;
                } else {
                    if (testType == RecallType.MARKER) allowMarker = false;
                }
            }
        }

        boolean reverseDirection = false;
        if (parameters.contains("warp")) {
            selectedType = RecallType.WARP;
            String warpName = parameters.getString("warp");
            castMessage = getMessage("cast_warp").replace("$name", warpName);
            Location location = controller.getWarp(warpName);
            if (tryTeleport(player, location)) {
                registerForUndo();
                return SpellResult.CAST;
            }
            return SpellResult.FAIL;
        }
        else if (parameters.contains("type")) {
            cycleRetries = 0;
            String typeString = parameters.getString("type", "");
            if (markerLocation != null && typeString.equalsIgnoreCase("remove")) {
                removeMarker();
                return SpellResult.TARGET_SELECTED;
            }
            RecallType newType = RecallType.valueOf(typeString.toUpperCase());

            selectedType = newType;
            Location location = getTargetLocation(player, selectedType, 0);
            if (tryTeleport(player, location)) {
                registerForUndo();
                return SpellResult.CAST;
            }
            return SpellResult.FAIL;
        }

        if (isLookingDown() && allowMarker)
        {
            if (placeMarker(getLocation().getBlock())) {
                registerForUndo();
                return SpellResult.CAST;
            }

            return SpellResult.FAIL;
        }
        else if (isLookingUp())
        {
            reverseDirection = false;
            cycleTarget(reverseDirection);
        }
        else
        {
            Location location = getLocation();
            List<Target> allWaypoints = new ArrayList<>();
            for (RecallType selectedType : enabledTypes) {
                if (selectedType == RecallType.WARP) {
                    for (int i = 0; i < warps.size(); i++) {
                        if (selectedType == this.selectedType && i == selectedIndex) continue;
                        Location targetLocation = getTargetLocation(player, selectedType, i);
                        if (targetLocation != null && targetLocation.getWorld().equals(location.getWorld())) {
                            Target target = new Target(location, targetLocation.getBlock(), 0, Math.PI);
                            target.setExtraData(new Waypoint(selectedType, i));
                            allWaypoints.add(target);
                        }
                    }
                } else if (selectedType == RecallType.WAND) {
                    List<LostWand> lostWands = mage.getLostWands();
                    for (int i = 0; i < lostWands.size(); i++) {
                        if (selectedType == this.selectedType && i == selectedIndex) continue;
                        Location targetLocation = getTargetLocation(player, selectedType, i);
                        if (targetLocation != null && targetLocation.getWorld().equals(location.getWorld())) {
                            Target target = new Target(location, targetLocation.getBlock(), 0, Math.PI);
                            target.setExtraData(new Waypoint(selectedType, i));
                            allWaypoints.add(target);
                        }
                    }
                } else {
                    if (selectedType == this.selectedType) continue;
                    Location targetLocation = getTargetLocation(player, selectedType, 0);
                    if (targetLocation != null && targetLocation.getWorld().equals(location.getWorld())) {
                        Target target = new Target(location, targetLocation.getBlock(), 0, Math.PI);
                        target.setExtraData(new Waypoint(selectedType, 0));
                        allWaypoints.add(target);
                    }
                }
            }
            if (allWaypoints.size() == 0) {
                return SpellResult.NO_TARGET;
            }

            Collections.sort(allWaypoints);
            Target targetWaypoint = allWaypoints.get(0);
            Waypoint waypoint = (Waypoint)targetWaypoint.getExtraData();
            selectedType = waypoint.type;
            selectedIndex = waypoint.index;
            if (tryCurrentType(player)) {
                registerForUndo();
                return SpellResult.CAST;
            }

            return SpellResult.FAIL;
        }

        if (selectedType == null) {
            if (enabledTypes.size() == 0) return SpellResult.FAIL;
            selectedType = enabledTypes.get(0);
        }

        boolean success = false;
        while (!success && cycleRetries >= 0) {
            success = tryCurrentType(player);
            if (!success && cycleRetries > 0) {
                cycleTarget(reverseDirection);
            }
            cycleRetries--;
        }

        if (!success) {
            sendMessage(failMessage);
            return SpellResult.FAIL;
        }
        registerForUndo();
        return SpellResult.CAST;
    }

    protected void cycleTargetType(boolean reverse) {
        if (reverse) selectedTypeIndex--;
        else selectedTypeIndex++;
        if (selectedTypeIndex < 0) selectedTypeIndex = enabledTypes.size() - 1;
        if (selectedTypeIndex >= enabledTypes.size()) selectedTypeIndex = 0;
        selectedType = enabledTypes.get(selectedTypeIndex);
        if (selectedType == RecallType.WARP) {
            if (reverse) selectedIndex = warps.size() - 1;
            else selectedIndex = 0;
        } else if (selectedType == RecallType.WAND) {
            if (reverse) {
                List<LostWand> lostWands = mage.getLostWands();
                selectedIndex = lostWands.size() - 1;
            }
            else selectedIndex = 0;
        } else {
            selectedIndex = 0;
        }
    }

    protected void cycleTarget(boolean reverse) {
        // Special-case for warps
        if (selectedType == RecallType.WARP) {
            if (reverse) {
                selectedIndex--;
                if (selectedIndex < 0) {
                    selectedIndex = warps.size() - 1;
                } else {
                    return;
                }
            }
            else {
                selectedIndex++;
                if (selectedIndex >= warps.size()) {
                    selectedIndex = 0;
                } else {
                    return;
                }
            }
        } else if (selectedType == RecallType.WAND) {
            List<LostWand> lostWands = mage.getLostWands();
            if (reverse) {
                selectedIndex--;
                if (selectedIndex < 0) {
                    selectedIndex = lostWands.size() - 1;
                } else {
                    return;
                }
            }
            else {
                selectedIndex++;
                if (selectedIndex >= lostWands.size()) {
                    selectedIndex = 0;
                } else {
                    return;
                }
            }
        }

        cycleTargetType(reverse);
    }

    @Nullable
    protected Location getTargetLocation(Player player, RecallType type, int index) {
        castMessage = "";
        failMessage = "";
        switch (type) {
        case MARKER:
            castMessage = getMessage("cast_marker");
            return markerLocation;
        case DEATH:
            castMessage = getMessage("cast_death");
            failMessage = getMessage("no_target_death");
            return mage.getLastDeathLocation();
        case SPAWN:
            castMessage = getMessage("cast_spawn");
            return getWorld().getSpawnLocation();
        case HOME:
            castMessage = getMessage("cast_home");
            failMessage = getMessage("no_target_home");
            return player == null ? null : player.getBedSpawnLocation();
        case WAND:
            List<LostWand> lostWands = mage.getLostWands();
            failMessage = getMessage("no_target_wand");
            castMessage = getMessage("cast_wand");
            if (lostWands == null || index < 0 || index >= lostWands.size()) return null;
            return lostWands.get(index).getLocation();
        case WARP:
            if (warps == null || index < 0 || index >= warps.size()) return null;
            String warpName = warps.get(index);
            castMessage = getMessage("cast_warp").replace("$name", warpName);
            return controller.getWarp(warpName);
        }

        return null;
    }

    protected boolean tryCurrentType(Player player) {
        Location location = getTargetLocation(player, selectedType, selectedIndex);
        if (location == null) {
            return false;
        }
        return tryTeleport(player, location);
    }

    protected boolean removeMarker()
    {
        if (markerLocation == null) return false;
        markerLocation = null;
        return true;
    }

    protected boolean tryTeleport(final Player player, final Location targetLocation) {
        if (targetLocation == null) {
            sendMessage(failMessage);
            return false;
        }
        if (!allowCrossWorld && !mage.getLocation().getWorld().equals(targetLocation.getWorld())) {
            sendMessage(getMessage("cross_world_disallowed"));
            return false;
        }

        Chunk chunk = targetLocation.getBlock().getChunk();
        if (!chunk.isLoaded()) {
            chunk.load(true);
            if (retryCount < MAX_RETRY_COUNT) {
                Plugin plugin = controller.getPlugin();
                final RecallSpell me = this;
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        me.tryTeleport(player, targetLocation);
                    }
                }, RETRY_INTERVAL);

                return true;
            }
        }

        registerMoved(player);
        Location playerLocation = player.getLocation();
        targetLocation.setYaw(playerLocation.getYaw());
        targetLocation.setPitch(playerLocation.getPitch());
        player.teleport(tryFindPlaceToStand(targetLocation));
        castMessage(castMessage);
        return true;
    }

    protected boolean placeMarker(Block target)
    {
        if (target == null)
        {
            return false;
        }

        registerForUndo(new UndoMarkerMove(this, markerLocation));
        if (markerLocation != null)
        {
            removeMarker();
            castMessage(getMessage("cast_marker_move"));
        }
        else
        {
            castMessage(getMessage("cast_marker_place"));
        }

        markerLocation = getLocation();
        markerLocation.setX(target.getX());
        markerLocation.setY(target.getY());
        markerLocation.setZ(target.getZ());

        return true;
    }

    @Override
    public void onLoad(ConfigurationSection node)
    {
        markerLocation = ConfigurationUtils.getLocation(node, "location");
    }

    @Override
    public void onSave(ConfigurationSection node)
    {
        node.set("location", ConfigurationUtils.fromLocation(markerLocation));
    }
}
