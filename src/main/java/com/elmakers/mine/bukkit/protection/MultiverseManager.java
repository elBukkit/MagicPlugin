package com.elmakers.mine.bukkit.protection;

import com.onarandombox.MultiverseCore.MultiverseCore;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

public class MultiverseManager {

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
            } catch (Throwable ex) {
            }

            if (mv != null) {
                plugin.getLogger().info("Multiverse-Core found, will respect PVP settings");
            }
        } else {
            mv = null;
        }
    }

    public boolean isPVPAllowed(World world) {
        if (!enabled || mv == null || world == null) return true;

        return mv.getMVWorldManager().getMVWorld(world).isPVPEnabled();
    }
}
