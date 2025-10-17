package com.elmakers.mine.bukkit.protection;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.mvplugins.multiverse.core.MultiverseCore;
import org.mvplugins.multiverse.core.MultiverseCoreApi;
import org.mvplugins.multiverse.core.world.MultiverseWorld;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.external.vavr.control.Option;

import com.elmakers.mine.bukkit.api.protection.PVPManager;

public class Multiverse5Manager implements PVPManager {
    private boolean enabled = false;
    private MultiverseCore mv = null;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled && mv != null;
    }

    public void initialize(Plugin plugin) {
        if (enabled) {
            try {
                Plugin mvPlugin = plugin.getServer().getPluginManager().getPlugin("Multiverse-Core");
                if (mvPlugin instanceof MultiverseCore) {
                    mv = (MultiverseCore) mvPlugin;
                }
            } catch (Throwable ignored) {
            }

            if (mv != null) {
                plugin.getLogger().info("Multiverse-Core found, will respect PVP settings");
            }
        } else {
            mv = null;
        }
    }

    @Override
    public boolean isPVPAllowed(Player player, Location location) {
        if (!enabled || mv == null || location == null) return true;
        World world = location.getWorld();
        if (world == null) return true;
        MultiverseCoreApi api = MultiverseCoreApi.get();
        WorldManager worldManager = api.getWorldManager();
        if (worldManager == null) return true;
        Option<MultiverseWorld> mvWorldOption = worldManager.getWorld(world);
        if (mvWorldOption.isEmpty()) return true;
        MultiverseWorld mvWorld = mvWorldOption.get();
        return mvWorld.getPvp();
    }
}
