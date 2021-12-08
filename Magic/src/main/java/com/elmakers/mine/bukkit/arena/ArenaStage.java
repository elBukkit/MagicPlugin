package com.elmakers.mine.bukkit.arena;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.WeakHashMap;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.entity.EntityData;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;

public class ArenaStage extends ArenaStageTemplate {
    private static Random random = new Random();
    private int index;
    private String name;

    private Map<Entity, ArenaPlayer> spawned = new WeakHashMap<>();
    private long started;
    private long lastTick;

    public ArenaStage(Arena arena, int index) {
        super(arena);
        this.index = index;
    }

    public ArenaStage(Arena arena, int index, ConfigurationSection configuration) {
        super(arena, configuration);
        this.index = index;
    }

    @Override
    protected void load() {
        ConfigurationSection effectiveConfiguration = ConfigurationUtils.cloneConfiguration(configuration);
        DefaultStage defaultStage = arena.getDefaultStage();
        ConfigurationUtils.addConfigurations(effectiveConfiguration, defaultStage.configuration, false);
        load(effectiveConfiguration);
    }

    @Override
    protected void load(ConfigurationSection configuration) {
        super.load(configuration);
        name = configuration.getString("name");
    }

    @Override
    public void describe(CommandSender sender) {
        sender.sendMessage(ChatColor.AQUA + getName() + ChatColor.GRAY + " (" + ChatColor.DARK_AQUA + getNumber() + ChatColor.GRAY + ")");
        super.describe(sender);
    }

    public String getMessage(String key) {
        return arena.getMessage("stage." + key).replace("$name", getName());
    }

    public void start() {
        started = System.currentTimeMillis();
        lastTick = started;
        if (!mobs.isEmpty()) {
            arena.messageInGamePlayers(getMessage("start_mobs"));
            MageController magic = arena.getController().getMagic();
            magic.setForceSpawn(true);
            List<ArenaPlayer> players = new ArrayList<>(arena.getLivingParticipants());
            if (players.isEmpty()) {
                arena.getController().getMagic().getLogger().warning("Arena stage " + getNumber() + " of " + arena.getKey() + " starting without any living players");
                return;
            }
            try {
                List<Location> spawns = getMobSpawns();
                int num = 0;
                for (ArenaMobSpawner mobSpawner : mobs) {
                    EntityData mobType = mobSpawner.getEntity();
                    if (mobType == null) continue;
                    for (int i = 0; i < mobSpawner.getCount(); i++) {
                        Location spawn = spawns.get(num);
                        if (randomizeMobSpawn != null) {
                            spawn = spawn.clone();
                            spawn.add(
                                (2 * random.nextDouble() - 1) * randomizeMobSpawn.getX(),
                                (2 * random.nextDouble() - 1) * randomizeMobSpawn.getY(),
                                (2 * random.nextDouble() - 1) * randomizeMobSpawn.getZ()
                            );
                        }
                        num = (num + 1) % spawns.size();
                        Entity spawnedEntity = mobType.spawn(spawn);
                        if (spawnedEntity != null) {
                            arena.getController().register(spawnedEntity, arena);
                            if (!defaultDrops) {
                                magic.disableDrops(spawnedEntity);
                            }
                            ArenaPlayer targetPlayer = null;
                            if (forceTarget && spawnedEntity instanceof Creature) {
                                targetPlayer = RandomUtils.getRandom(players);
                                Player player = targetPlayer.getPlayer();
                                ((Creature)spawnedEntity).setTarget(player);
                                CompatibilityLib.getMobUtils().setPathfinderTarget(spawnedEntity, player, 0);
                            }
                            spawned.put(spawnedEntity, targetPlayer);
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            magic.setForceSpawn(false);
        }

        if (startSpell != null && !startSpell.isEmpty()) {
            Mage arenaMage = arena.getMage();
            arenaMage.setLocation(arena.getCenter());
            Spell spell = arenaMage.getSpell(startSpell);
            if (spell != null) {
               spell.cast();
            }
        }
    }

public void checkAggro(Entity mob) {
        Set<ArenaPlayer> currentPlayers = arena.getLivingParticipants();
        if (!currentPlayers.isEmpty()) {
            ArenaPlayer target = spawned.get(mob);
            if (target != null && !currentPlayers.contains(target)) {
                target = null;
            }
            if (target == null) {
                target = RandomUtils.getRandom(new ArrayList<>(currentPlayers));
                spawned.put(mob, target);
            }
            if (mob instanceof Creature) {
                ((Creature)mob).setTarget(target.getPlayer());
            }
            CompatibilityLib.getMobUtils().setPathfinderTarget(mob, target.getPlayer(), 1);
        }
    }

    public void mobDied(Entity entity) {
        arena.getController().unregister(entity);
        spawned.remove(entity);
    }

    public void completed() {
        arena.messageInGamePlayers(getMessage("win"));

        Collection<ArenaPlayer> players = arena.getParticipants();
        for (ArenaPlayer player : players) {
            Mage mage = player.getMage();
            if (winXP > 0) {
                mage.sendMessage(ChatColor.AQUA + "You have been awarded " + ChatColor.DARK_AQUA + Integer.toString(winXP) + ChatColor.AQUA + " experience!");
                mage.giveExperience(winXP);
            }
            if (winSP > 0) {
                mage.sendMessage(ChatColor.AQUA + "You have been awarded " + ChatColor.DARK_AQUA + Integer.toString(winSP) + ChatColor.AQUA + " spell points!");
                mage.addSkillPoints(winSP);
            }
            if (winMoney > 0) {
                mage.sendMessage(ChatColor.AQUA + "You have been awarded $" + ChatColor.DARK_AQUA + Integer.toString(winMoney) + ChatColor.AQUA + "!");
                mage.addVaultCurrency(winMoney);
            }
        }

        finish();
    }

    public void finish() {
        if (endSpell != null && !endSpell.isEmpty()) {
            Mage arenaMage = arena.getMage();
            arenaMage.setLocation(arena.getCenter());
            Spell spell = arenaMage.getSpell(endSpell);
            if (spell != null) {
                spell.cast();
            }
        }
        reset();
    }

    public boolean isFinished() {
        checkSpawns();
        return spawned.isEmpty();
    }

    public void checkSpawnsAndArena() {
        checkSpawns(true, true);
    }

    public void checkSpawns() {
        checkSpawns(false, false);
    }

    public void checkSpawns(boolean checkAggro, boolean checkArena) {
        Iterator<Map.Entry<Entity, ArenaPlayer>> it = spawned.entrySet().iterator();
        boolean mobsDied = false;
        while (it.hasNext()) {
            Entity entity = it.next().getKey();
            if (entity.isDead() || !entity.isValid()) {
                mobsDied = true;
                arena.getController().unregister(entity);
                it.remove();
            } else if (checkAggro && forceTarget) {
                checkAggro(entity);
            }
        }
        if (checkArena && (mobsDied || spawned.isEmpty()) && arena.isStarted()) {
            arena.check();
        }
    }

    @Override
    public String getName() {
        if (name != null) {
            return ChatColor.translateAlternateColorCodes('&', name);
        }
        return "Stage " + getNumber();
    }

    @Override
    public void setName(String name) {
        this.name = name;
        configuration.set("name", name);
        arena.saveEditingStage();
    }

    public int getNumber() {
        return index + 1;
    }

    public void reset() {
        for (Entity entity : spawned.keySet()) {
            if (entity.isValid()) {
                arena.getController().unregister(entity);
                entity.remove();
            }
        }
        spawned.clear();
    }

    public int getActiveMobs() {
        checkSpawns();
        return spawned.size();
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isRespawning() {
        return (arena.hasDeadPlayers() && respawnDuration > 0);
    }

    public void tick() {
        checkSpawnsAndArena();

        if (duration <= 0 && respawnDuration <= 0) {
            return;
        }

        long now = System.currentTimeMillis();
        long previousTick = lastTick;
        lastTick = now;
        long previousTime = previousTick - started;
        long currentTime = now - started;

        if (duration > 0) {
            long previousSecondsRemaining = (duration - previousTime) / 1000;
            long secondsRemaining = (duration - currentTime) / 1000;
            if (secondsRemaining > 0 && secondsRemaining < previousSecondsRemaining) {
                if (secondsRemaining == 10 || secondsRemaining == 30) {
                    arena.messageInGamePlayers(getMessage("duration_10").replace("$countdown", Long.toString(secondsRemaining)));
                } else if (secondsRemaining <= 5) {
                    arena.messageInGamePlayers(getMessage("duration").replace("$countdown", Long.toString(secondsRemaining)));
                }
            }

            if (currentTime > duration) {
                arena.draw();
                return;
            }
        }

        if (respawnDuration > 0 && arena.hasDeadPlayers()) {
            long lastDeathTime = arena.getLastDeathTime();
            long deathTime = now - lastDeathTime;
            long respawnSecondsRemaining = (respawnDuration - deathTime) / 1000;
            if (duration > 0) {
                long secondsRemaining = (duration - currentTime) / 1000;
                if (secondsRemaining <= respawnSecondsRemaining) {
                    return;
                }
            }

            long previousDeathTime = previousTick - lastDeathTime;
            long previousRespawnSecondsRemaining = (respawnDuration - previousDeathTime) / 1000;

            if (respawnSecondsRemaining > 0 && respawnSecondsRemaining < previousRespawnSecondsRemaining) {
                if (respawnSecondsRemaining == 10) {
                    arena.messageDeadPlayers("t:" + ChatColor.GREEN + "Respawning\n" + ChatColor.GRAY + "in 10 Seconds!");
                } else if (respawnSecondsRemaining <= 5) {
                    arena.messageDeadPlayers("t:" + ChatColor.GREEN + respawnSecondsRemaining);
                }
            }

            if (deathTime > respawnDuration) {
                arena.respawn();
                return;
            } else {
                arena.showRespawnBossBar((double)deathTime / respawnDuration);
            }
        }
    }
}
