package com.elmakers.mine.bukkit.protection;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class NCPAPI implements Runnable {
    private Map<UUID, Long> flyExemptions = new HashMap<UUID, Long>();
    public static int CHECK_FREQUENCY = 20;

    public NCPAPI(Plugin plugin, Plugin ncpPlugin) {
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin,
                this, CHECK_FREQUENCY, CHECK_FREQUENCY);
    }

    public void run() {
        List<UUID> uuids = new ArrayList<UUID>(flyExemptions.keySet());
        long now = System.currentTimeMillis();
        for (UUID uuid : uuids)
        {
            long timeout = flyExemptions.get(uuid);
            if (now > timeout) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    NCPExemptionManager.unexempt(player, CheckType.MOVING_SURVIVALFLY);
                }
                flyExemptions.remove(uuid);
            }
        }
    }

    public void addFlightExemption(Player player, long duration) {
        UUID id = player.getUniqueId();
        long newTimeout = System.currentTimeMillis() + duration;
        Long timeout = flyExemptions.get(id);
        if (timeout == null || timeout < newTimeout) {
            NCPExemptionManager.exemptPermanently(player, CheckType.MOVING_SURVIVALFLY);
            flyExemptions.put(id, newTimeout);
        }
    }
}
