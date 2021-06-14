package com.elmakers.mine.bukkit.boss;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Server;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;

public class BossBarTracker {
    private static final Random random = new Random();
    private final Mage mage;
    private BossBar bossBar;
    private final double radius;
    private final long interval;
    private final int intervalRandomization;
    private final Set<UUID> visibleTo = new HashSet<>();
    private final Set<UUID> processing = new HashSet<>();
    private long nextUpdate;

    public BossBarTracker(Mage mage, BossBarConfiguration config) {
        this.mage = mage;
        this.bossBar = config.createBossBar(mage);
        this.radius = config.getRadius();
        this.interval = config.getUpdateInterval();
        this.intervalRandomization = config.getUpdateIntervalRandomization();
        this.tick();
    }

    public void tick() {
        if (bossBar == null || !mage.isValid()) return;
        double progress = mage.getMaxHealth();
        if (progress > 0) {
            progress = mage.getHealth() / progress;
        }
        tick(progress);
    }

    public void tick(double progress) {
        if (bossBar == null || !mage.isValid()) return;
        this.bossBar.setProgress(Math.min(1, Math.max(0, progress)));
        if (System.currentTimeMillis() > nextUpdate) {
            updateVisibility();
        }
    }

    private void updateVisibility() {
        List<Entity> entities = CompatibilityLib.getCompatibilityUtils().getNearbyEntities(mage.getLocation(), radius, radius, radius);
        processing.addAll(visibleTo);
        visibleTo.clear();

        // Show to any newly nearby players
        for (Entity entity : entities) {
            if (entity instanceof Player) {
                Player player = (Player)entity;
                UUID id = player.getUniqueId();
                if (!processing.remove(id)) {
                    bossBar.addPlayer(player);
                }
                visibleTo.add(id);
            }
        }

        Plugin plugin = mage.getController().getPlugin();
        Server server = plugin.getServer();
        // Hide any players now out of range
        for (UUID leftArea : processing) {
            Player player = server.getPlayer(leftArea);
            if (player != null) {
                bossBar.removePlayer(player);
            }
        }
        processing.clear();

        nextUpdate = System.currentTimeMillis() + random.nextInt(intervalRandomization) + interval;
    }

    public void remove() {
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar.setVisible(false);
            bossBar = null;
            visibleTo.clear();
        }
    }
}
