package com.elmakers.mine.bukkit.protection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;

public class NCPAPI implements Runnable {
    private Map<UUID, Long> flyExemptions = new HashMap<>();
    public static int CHECK_FREQUENCY = 20;

    public NCPAPI(Plugin plugin, Plugin ncpPlugin) {
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin,
                this, CHECK_FREQUENCY, CHECK_FREQUENCY);
    }

    @Override
    public void run() {
        List<UUID> uuids = new ArrayList<>(flyExemptions.keySet());
        long now = System.currentTimeMillis();
        for (UUID uuid : uuids)
        {
            long timeout = flyExemptions.get(uuid);
            if (now > timeout) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    NCPExemptionManager.unexempt(player, CheckType.MOVING_SURVIVALFLY);
                    NCPExemptionManager.unexempt(player, CheckType.MOVING_CREATIVEFLY);
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
            NCPExemptionManager.exemptPermanently(player, CheckType.MOVING_CREATIVEFLY);
            flyExemptions.put(id, newTimeout);
        }
    }

    public void addFlightExemption(Player player) {
        NCPExemptionManager.exemptPermanently(player, CheckType.MOVING_SURVIVALFLY);
        NCPExemptionManager.exemptPermanently(player, CheckType.MOVING_CREATIVEFLY);
    }

    public void removeFlightExemption(Player player) {
        NCPExemptionManager.unexempt(player, CheckType.MOVING_SURVIVALFLY);
        NCPExemptionManager.unexempt(player, CheckType.MOVING_CREATIVEFLY);
    }
}
