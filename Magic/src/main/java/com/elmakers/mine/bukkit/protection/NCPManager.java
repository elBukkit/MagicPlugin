package com.elmakers.mine.bukkit.protection;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class NCPManager {
    private boolean enabled = true;
    private NCPAPI ncp;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled && ncp != null;
    }

    public void initialize(Plugin plugin) {
        ncp = null;
        if (enabled) {
            Plugin ncpPlugin = plugin.getServer().getPluginManager().getPlugin("NoCheatPlus");
            if (ncpPlugin != null)
            {
                ncp = new NCPAPI(plugin, ncpPlugin);
                plugin.getLogger().info("NoCheatPlus found, adding exemption handlers.");
            } else {
                plugin.getLogger().info("NoCheatPlus not found, will not integrate.");
            }
        } else {
            plugin.getLogger().info("NoCheatPlus integration disabled, enable this in config.yml if you find Magic triggering NCP violations");
        }
    }

    public void addFlightExemption(Player player, int duration) {
        if (ncp != null) {
            ncp.addFlightExemption(player, duration);
        }
    }

    public void addFlightExemption(Player player) {
        if (ncp != null) {
            ncp.addFlightExemption(player);
        }
    }

    public void removeFlightExemption(Player player) {
        if (ncp != null) {
            ncp.removeFlightExemption(player);
        }
    }
}
