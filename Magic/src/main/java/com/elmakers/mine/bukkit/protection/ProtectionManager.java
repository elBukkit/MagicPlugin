package com.elmakers.mine.bukkit.protection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

import com.elmakers.mine.bukkit.api.protection.BlockBreakManager;
import com.elmakers.mine.bukkit.api.protection.BlockBuildManager;

public class ProtectionManager implements BlockBreakManager, BlockBuildManager {
    private Plugin owningPlugin;
    private final Set<Plugin> plugins = new HashSet<>();
    private final List<String> pluginNames = new ArrayList<>();
    private boolean checkedListeners;
    private final List<RegisteredListener> breakListeners = new ArrayList<>();
    private final List<RegisteredListener> buildListeners = new ArrayList<>();

    public boolean isEnabled() {
        return plugins.size() > 0;
    }

    public void initialize(Plugin owner, List<String> pluginNames) {
        owningPlugin = owner;
        plugins.clear();
        checkedListeners = false;
        this.pluginNames.clear();
        this.pluginNames.addAll(pluginNames);
    }

    public void check() {
        for (String pluginName : pluginNames) {
            Plugin plugin = owningPlugin.getServer().getPluginManager().getPlugin(pluginName);
            if (plugin != null) {
                plugins.add(plugin);
                owningPlugin.getLogger().info("Integrating with " + pluginName + " using fake break/build events");
            }
        }
    }

    private void checkListeners() {
        if (checkedListeners) return;
        checkBreakListeners();
        checkBuildListeners();
        checkedListeners = true;
    }

    private void checkBreakListeners() {
        breakListeners.clear();
        HandlerList handlers = BlockBreakEvent.getHandlerList();
        for (RegisteredListener listener : handlers.getRegisteredListeners()) {
            if (plugins.contains(listener.getPlugin())) {
                breakListeners.add(listener);
            }
        }
    }

    private void checkBuildListeners() {
        buildListeners.clear();
        HandlerList handlers = BlockPlaceEvent.getHandlerList();
        for (RegisteredListener listener : handlers.getRegisteredListeners()) {
            if (plugins.contains(listener.getPlugin())) {
                buildListeners.add(listener);
            }
        }
    }

    @Override
    public boolean hasBuildPermission(Player player, Block block) {
        if (player == null || block == null) return true;

        checkListeners();
        BlockPlaceEvent placeEvent = new BlockPlaceEvent(block, block.getState(), block.getRelative(BlockFace.DOWN), player.getInventory().getItemInMainHand(), player, true, EquipmentSlot.HAND);
        for (RegisteredListener listener : buildListeners) {
            try {
                listener.callEvent(placeEvent);
                if (placeEvent.isCancelled()) {
                    return false;
                }
            } catch (Exception ex) {
                owningPlugin.getLogger().log(Level.WARNING, "An error occurred sending a BlockPlaceEvent to " + listener.getPlugin().getName(), ex);
            }
        }
        return true;
    }

    @Override
    public boolean hasBreakPermission(Player player, Block block) {
        if (player == null || block == null) return true;

        checkListeners();
        BlockBreakEvent breakEvent = new BlockBreakEvent(block, player);
        for (RegisteredListener listener : breakListeners) {
            try {
                listener.callEvent(breakEvent);
                if (breakEvent.isCancelled()) {
                    return false;
                }
            } catch (Exception ex) {
                owningPlugin.getLogger().log(Level.WARNING, "An error occurred sending a BlockBreakEvent to " + listener.getPlugin().getName(), ex);
            }
        }
        return true;
    }
}
