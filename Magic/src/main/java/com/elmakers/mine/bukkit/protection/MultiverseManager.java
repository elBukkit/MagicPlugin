package com.elmakers.mine.bukkit.protection;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.protection.PVPManager;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;

public class MultiverseManager implements PVPManager {

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
                    mv = (MultiverseCore)mvPlugin;
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
        MVWorldManager manager = mv.getMVWorldManager();
        if (manager == null) return true;
        MultiverseWorld mvWorld = manager.getMVWorld(world);
        if (mvWorld == null) return true;
;       return mvWorld.isPVPEnabled();
    }
}
