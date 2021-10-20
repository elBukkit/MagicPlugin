package com.elmakers.mine.bukkit.arena;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.entity.EntityData;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public abstract class ArenaStageTemplate implements EditingStage  {
    protected final Arena arena;
    protected final ConfigurationSection configuration;

    protected List<ArenaMobSpawner> mobs = new ArrayList<>();
    protected List<Location> mobSpawns = new ArrayList<>();
    protected String startSpell;
    protected String endSpell;

    protected Vector randomizeMobSpawn;

    protected int winXP = 0;
    protected int winSP = 0;
    protected int winMoney = 0;
    protected int duration = 0;
    protected int respawnDuration = 0;

    protected boolean defaultDrops = false;
    protected boolean forceTarget = false;

    public ArenaStageTemplate(Arena arena) {
        this.arena = arena;
        configuration = ConfigurationUtils.newConfigurationSection();
        load();
    }

    public ArenaStageTemplate(Arena arena, ConfigurationSection configuration) {
        this.arena = arena;
        this.configuration = configuration;
        load();
    }

    protected void load() {
        load(configuration);
    }

    protected void load(ConfigurationSection configuration) {
        MageController controller = arena.getController().getMagic();
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
        winXP = configuration.getInt("win_xp");
        winSP = configuration.getInt("win_sp");
        winMoney = configuration.getInt("win_money");
        defaultDrops = configuration.getBoolean("drops");
        forceTarget = configuration.getBoolean("aggro", false);
        duration = configuration.getInt("duration", 0);
        respawnDuration = configuration.getInt("respawn_duration", 0);

        if (configuration.contains("randomize_mob_spawn")) {
            randomizeMobSpawn = ConfigurationUtils.toVector(configuration.getString("randomize_mob_spawn"));
        }
    }

    protected void saveMobs() {
        List<ConfigurationSection> mobsConfigurations = new ArrayList<>();
        for (ArenaMobSpawner mob : mobs) {
            if (!mob.isValid()) continue;
            ConfigurationSection section = new MemoryConfiguration();
            mob.save(section);
            mobsConfigurations.add(section);
        }
        configuration.set("mobs", mobsConfigurations);
        arena.saveEditingStage();
    }

    protected void saveMobSpawns() {
        List<String> mobSpawnList = new ArrayList<>();
        for (Location spawn : mobSpawns) {
            mobSpawnList.add(ConfigurationUtils.fromLocation(spawn, arena.getCenter()));
        }
        configuration.set("mob_spawns", mobSpawnList);
        arena.saveEditingStage();
    }

    @Override
    public void addMob(EntityData entityType, int count) {
        mobs.add(new ArenaMobSpawner(entityType, count));
        saveMobs();
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
        saveMobs();
    }

    @Override
    public void describe(CommandSender sender) {
        int mobSpawnSize = mobSpawns.size();
        if (mobSpawnSize == 1) {
            sender.sendMessage(ChatColor.BLUE + "Mob Spawn: " + arena.printLocation(mobSpawns.get(0)));
        } else if (mobSpawnSize > 1) {
            sender.sendMessage(ChatColor.BLUE + "Mob Spawns: " + ChatColor.GRAY + mobSpawnSize);
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
        configuration.set("spell_start", startSpell);
        arena.saveEditingStage();
    }

    public String getEndSpell() {
        return endSpell;
    }

    @Override
    public void setEndSpell(String endSpell) {
        this.endSpell = endSpell;
        configuration.set("spell_end", endSpell);
        arena.saveEditingStage();
    }

    @Override
    public void addMobSpawn(Location location) {
        mobSpawns.add(location.clone());
        saveMobSpawns();
    }

    @Override
    public Location removeMobSpawn(Location location) {
        int rangeSquared = 3 * 3;
        for (Location spawn : mobSpawns) {
            if (spawn.distanceSquared(location) < rangeSquared) {
                mobSpawns.remove(spawn);
                saveMobSpawns();
                return spawn;
            }
        }

        return null;
    }

    public List<Location> getMobSpawns() {
        if (mobSpawns.size() == 0) {
            List<Location> centerList = new ArrayList<>();
            centerList.add(arena.getCenter());
            return centerList;
        }

        return mobSpawns;
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

    public boolean hasMobs() {
        return !mobs.isEmpty();
    }

    @Override
    public Arena getArena() {
        return arena;
    }

    @Override
    public void setRandomizeMobSpawn(Vector vector) {
        randomizeMobSpawn = vector;
        configuration.set("randomize_mob_spawn", ConfigurationUtils.fromVector(randomizeMobSpawn));
        arena.saveEditingStage();
    }

    @Override
    public void setWinXP(int xp) {
        winXP = Math.max(xp, 0);
        configuration.set("win_xp", winXP);
        arena.saveEditingStage();
    }

    @Override
    public void setWinSP(int sp) {
        winSP = Math.max(sp, 0);
        configuration.set("win_sp", winSP);
        arena.saveEditingStage();
    }

    @Override
    public void setWinMoney(int money) {
        winMoney = Math.max(money, 0);
        configuration.set("win_money", winMoney);
        arena.saveEditingStage();
    }

    public List<ArenaMobSpawner> getMobSpawners() {
        return mobs;
    }

    @Override
    public void setDuration(int duration) {
        this.duration = duration;
        configuration.set("duration", duration);
        arena.saveEditingStage();
    }

    @Override
    public void setRespawnDuration(int duration) {
        this.respawnDuration = duration;
        configuration.set("respawn_duration", respawnDuration);
        arena.saveEditingStage();
    }

    public long getRespawnDuration() {
        return respawnDuration;
    }

    public boolean isRespawnEnabled() {
        return respawnDuration > 0;
    }

    public void setForceTarget(boolean forceTarget) {
        this.forceTarget = forceTarget;
        configuration.set("aggro", forceTarget);
        arena.saveEditingStage();
    }

    public void setDefaultDrops(boolean defaultDrops) {
        this.defaultDrops = defaultDrops;
        configuration.set("drops", defaultDrops);
        arena.saveEditingStage();
    }

    public ConfigurationSection getConfiguration() {
        return configuration;
    }
}
