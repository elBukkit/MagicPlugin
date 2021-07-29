package com.elmakers.mine.bukkit.arena;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.entity.EntityData;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;

public class ArenaStage implements EditingStage {
    private static Random random = new Random();
    private final Arena arena;
    private int index;
    private List<ArenaMobSpawner> mobs = new ArrayList<ArenaMobSpawner>();
    private List<Location> mobSpawns = new ArrayList<Location>();
    private Set<Entity> spawned = new HashSet<Entity>();
    private String startSpell;
    private String endSpell;
    private String name;

    private Vector randomizeMobSpawn;

    private int winXP = 0;
    private int winSP = 0;
    private int winMoney = 0;
    private int duration = 0;
    private int respawnDuration = 0;

    private boolean defaultDrops = false;
    private boolean forceTarget = true;

    private long started;
    private long lastTick;

    public ArenaStage(Arena arena, int index) {
        this.arena = arena;
        this.index = index;
    }

    public ArenaStage(Arena arena, int index, MageController controller, ConfigurationSection configuration) {
        this.arena = arena;
        this.index = index;
        if (configuration.contains("mobs")) {
            Collection<ConfigurationSection> mobConfigurations = ConfigurationUtils.getNodeList(configuration, "mobs");
            for (ConfigurationSection mobConfiguration : mobConfigurations) {
                ArenaMobSpawner mob = new ArenaMobSpawner(controller, mobConfiguration);
                if (mob.isValid()) {
                    mobs.add(mob);
                }
            }
        }
        startSpell = configuration.getString("spell_start");
        endSpell = configuration.getString("spell_end");

        for (String s : configuration.getStringList("mob_spawns")) {
            mobSpawns.add(ConfigurationUtils.toLocation(s, arena.getCenter()));
        }
        name = configuration.getString("name");
        winXP = configuration.getInt("win_xp");
        winSP = configuration.getInt("win_sp");
        winMoney = configuration.getInt("win_money");
        defaultDrops = configuration.getBoolean("drops");
        forceTarget = configuration.getBoolean("aggro", true);
        duration = configuration.getInt("duration", 0);
        respawnDuration = configuration.getInt("respawn_duration", 0);

        if (configuration.contains("randomize_mob_spawn")) {
            randomizeMobSpawn = ConfigurationUtils.toVector(configuration.getString("randomize_mob_spawn"));
        }
    }

    public void save(ConfigurationSection configuration) {
        List<ConfigurationSection> mobsConfigurations = new ArrayList<ConfigurationSection>();
        for (ArenaMobSpawner mob : mobs) {
            if (!mob.isValid()) continue;
            ConfigurationSection section = new MemoryConfiguration();
            mob.save(section);
            mobsConfigurations.add(section);
        }
        configuration.set("mobs", mobsConfigurations);
        configuration.set("spell_start", startSpell);
        configuration.set("spell_end", endSpell);

        List<String> mobSpawnList = new ArrayList<>();
        for (Location spawn : mobSpawns) {
            mobSpawnList.add(ConfigurationUtils.fromLocation(spawn, arena.getCenter()));
        }
        configuration.set("mob_spawns", mobSpawnList);
        if (name != null && !name.isEmpty()) configuration.set("name", name);
        if (winXP != 0) configuration.set("win_xp", winXP);
        if (winSP != 0) configuration.set("win_sp", winSP);
        if (winMoney != 0) configuration.set("win_money", winMoney);
        configuration.set("drops", defaultDrops);
        configuration.set("aggro", forceTarget);
        if (duration != 0) configuration.set("duration", duration);
        if (respawnDuration != 0) configuration.set("respawn_duration", respawnDuration);

        if (randomizeMobSpawn != null) {
            configuration.set("randomize_mob_spawn", ConfigurationUtils.fromVector(randomizeMobSpawn));
        }
    }

    @Override
    public void addMob(EntityData entityType, int count) {
        mobs.add(new ArenaMobSpawner(entityType, count));
    }

    @Override
    public void removeMob(EntityData entityType) {
        Iterator<ArenaMobSpawner> it = mobs.iterator();
        while (it.hasNext()) {
            ArenaMobSpawner spawner = it.next();
            if (spawner.getEntity().getKey().equalsIgnoreCase(entityType.getKey())) {
                it.remove();
            }
        }
    }

    public void describe(CommandSender sender) {
        sender.sendMessage(ChatColor.AQUA + getName() + ChatColor.GRAY + " (" + ChatColor.DARK_AQUA + getNumber() + ChatColor.GRAY + ")");
        int mobSpawnSize = mobSpawns.size();
        if (mobSpawnSize == 1) {
            sender.sendMessage(ChatColor.BLUE + "Spawn Mobs: " + arena.printLocation(mobSpawns.get(0)));
        } else if (mobSpawnSize > 1) {
            sender.sendMessage(ChatColor.BLUE + "Spawns Mobs: " + ChatColor.GRAY + mobSpawnSize);
            for (Location spawn : mobSpawns) {
                sender.sendMessage(arena.printLocation(spawn));
            }
        }

        int numMobs = mobs.size();
        if (numMobs == 0) {
            sender.sendMessage(ChatColor.GRAY + "(No Mobs)");
        } else {
            sender.sendMessage(ChatColor.DARK_GREEN + "Mobs: " + ChatColor.BLUE + numMobs);
            for (ArenaMobSpawner mob : mobs) {
                sender.sendMessage(" " + describeMob(mob));
            }
        }
        if (randomizeMobSpawn != null) {
            sender.sendMessage(ChatColor.DARK_GREEN + " Randomize Spawning: " + ChatColor.BLUE + randomizeMobSpawn);
        }

        if (duration > 0) {
            int minutes = (int)Math.ceil((double)duration / 60 / 1000);
            sender.sendMessage(ChatColor.AQUA + "Duration: " + ChatColor.DARK_AQUA + minutes + ChatColor.WHITE + " minutes");
        }

        if (respawnDuration > 0) {
            int seconds = (int)Math.ceil((double)respawnDuration / 1000);
            sender.sendMessage(ChatColor.AQUA + "Respawn: " + ChatColor.DARK_AQUA + seconds + ChatColor.WHITE + " seconds");
        }

        if (startSpell != null) {
            sender.sendMessage(ChatColor.DARK_AQUA + "Cast at Start: " + ChatColor.AQUA + startSpell);
        }

        if (endSpell != null) {
            sender.sendMessage(ChatColor.DARK_AQUA + "Cast at End: " + ChatColor.AQUA + endSpell);
        }

        if (winXP > 0) {
            sender.sendMessage(ChatColor.AQUA + "Winning Reward: " + ChatColor.LIGHT_PURPLE + winXP + ChatColor.AQUA + " xp");
        }
        if (winSP > 0) {
            sender.sendMessage(ChatColor.AQUA + "Winning Reward: " + ChatColor.LIGHT_PURPLE + winSP + ChatColor.AQUA + " sp");
        }
        if (winMoney > 0) {
            sender.sendMessage(ChatColor.AQUA + "Winning Reward: $" + ChatColor.LIGHT_PURPLE + winMoney);
        }
    }

    protected String describeMob(ArenaMobSpawner mob) {
        if (mob == null) {
            return ChatColor.RED + "(Invalid Mob)";
        }
        if (mob.getEntity() == null) {
            return ChatColor.RED + "(Invalid Mob)" + ChatColor.YELLOW + " x" + mob.getCount();
        }
        return ChatColor.DARK_GREEN + " " + mob.getEntity().describe() + ChatColor.YELLOW + " x" + mob.getCount();
    }

    public String getStartSpell() {
        return startSpell;
    }

    @Override
    public void setStartSpell(String startSpell) {
        this.startSpell = startSpell;
    }

    public String getEndSpell() {
        return endSpell;
    }

    @Override
    public void setEndSpell(String endSpell) {
        this.endSpell = endSpell;
    }

    @Override
    public void addMobSpawn(Location location) {
        mobSpawns.add(location.clone());
    }

    @Override
    public Location removeMobSpawn(Location location) {
        int rangeSquared = 3 * 3;
        for (Location spawn : mobSpawns) {
            if (spawn.distanceSquared(location) < rangeSquared) {
                mobSpawns.remove(spawn);
                return spawn;
            }
        }

        return null;
    }

    public List<Location> getMobSpawns() {
        if (mobSpawns.size() == 0) {
            List<Location> centerList = new ArrayList<Location>();
            centerList.add(arena.getCenter());
            return centerList;
        }

        return mobSpawns;
    }

    public void start() {
        started = System.currentTimeMillis();
        lastTick = started;
        if (!mobs.isEmpty()) {
            arena.messageInGamePlayers("t:" + getName());
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
                            spawned.add(spawnedEntity);
                            if (!defaultDrops) {
                                magic.disableDrops(spawnedEntity);
                            }
                            if (forceTarget && spawnedEntity instanceof Creature) {
                                ArenaPlayer player = RandomUtils.getRandom(players);
                                ((Creature)spawnedEntity).setTarget(player.getPlayer());
                            }
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
        if (mob instanceof Creature) {
            Creature creature = (Creature)mob;
            Entity target = creature.getTarget();
            Set<ArenaPlayer> currentPlayers = arena.getLivingParticipants();
            if (target != null) {
                if (target instanceof Player) {
                    ArenaPlayer targetPlayer = new ArenaPlayer(arena, target.getUniqueId());
                    if (!currentPlayers.contains(targetPlayer)) {
                        target = null;
                    }
                } else {
                    target = null;
                }
            }
            if (target == null) {
                ArenaPlayer player = RandomUtils.getRandom(new ArrayList<>(currentPlayers));
                creature.setTarget(player.getPlayer());
            }
        }
    }

    public void mobDied(Entity entity) {
        arena.getController().unregister(entity);
        spawned.remove(entity);
    }

    public void completed() {
        arena.messageInGamePlayers(ChatColor.GREEN + "Congratulations!" + ChatColor.AQUA + "  You have passed " + ChatColor.DARK_AQUA + getName());

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

    public boolean hasMobs() {
        return !mobs.isEmpty();
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
        Iterator<Entity> it = spawned.iterator();
        boolean mobsDied = false;
        while (it.hasNext()) {
            Entity entity = it.next();
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
    }

    @Override
    public Arena getArena() {
        return arena;
    }

    public int getNumber() {
        return index + 1;
    }

    public void reset() {
        for (Entity entity : spawned) {
            if (entity.isValid()) {
                arena.getController().unregister(entity);
                entity.remove();
            }
        }
        spawned.clear();
    }

    @Override
    public void setRandomizeMobSpawn(Vector vector) {
        randomizeMobSpawn = vector;
    }

    @Override
    public void setWinXP(int xp) {
        winXP = Math.max(xp, 0);
    }

    @Override
    public void setWinSP(int sp) {
        winSP = Math.max(sp, 0);
    }

    @Override
    public void setWinMoney(int money) {
        winMoney = Math.max(money, 0);
    }

    public List<ArenaMobSpawner> getMobSpawners() {
        return mobs;
    }

    public int getActiveMobs() {
        checkSpawns();
        return spawned.size();
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public Collection<EntityData> getSpawns() {
        List<EntityData> spawns = new ArrayList<>();
        List<ArenaMobSpawner> spawners = getMobSpawners();
        for (ArenaMobSpawner spawner : spawners) {
            if (spawner.isValid()) {
                spawns.add(spawner.getEntity());
            }
        }
        return spawns;
    }

    @Override
    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public void setRespawnDuration(int duration) {
        this.respawnDuration = duration;
    }

    public long getRespawnDuration() {
        return respawnDuration;
    }

    public boolean isRespawning() {
        return (arena.hasDeadPlayers() && respawnDuration > 0);
    }

    public boolean isRespawnEnabled() {
        return respawnDuration > 0;
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
                    arena.messageInGamePlayers("t:" + ChatColor.RED + secondsRemaining + " Seconds!");
                } else if (secondsRemaining <= 5) {
                    arena.messageInGamePlayers("t:" + ChatColor.RED + secondsRemaining);
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
