package com.elmakers.mine.bukkit.arena;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.SkullType;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Skull;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.block.magic.MagicBlock;
import com.elmakers.mine.bukkit.api.event.ArenaStartEvent;
import com.elmakers.mine.bukkit.api.event.ArenaStopEvent;
import com.elmakers.mine.bukkit.api.item.ItemUpdatedCallback;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.block.DefaultMaterials;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.DirectionUtils;
import com.elmakers.mine.bukkit.utility.StringUtils;
import com.google.common.base.Splitter;

public class Arena implements com.elmakers.mine.bukkit.api.arena.Arena {
    private static final Random random = new Random();

    private ArenaTemplate template;
    private ConfigurationSection parameters;
    private ArenaState state = ArenaState.LOBBY;
    private long started;
    private long lastTick;
    private long lastDeath;
    private Queue<ArenaPlayer> queue = new LinkedList<>();
    private Set<ArenaPlayer> players = new HashSet<>();
    private Set<ArenaPlayer> deadPlayers = new HashSet<>();

    private List<Location> spawns = new ArrayList<>();
    private List<ArenaStage> stages = new ArrayList<>();
    private DefaultStage defaultStage;
    private int currentStage = 0;
    private int editingStage = 0;
    private boolean editDefaultStage = false;
    private final ArenaController controller;

    private Location center;
    private Location exit;
    private Location lose;
    private Location win;
    private Location lobby;

    private Vector randomizeSpawn;

    private int maxPlayers = 2;
    private int minPlayers = 2;
    private int requiredKills = 1;
    private int winXP = 0;
    private int loseXP = 0;
    private int drawXP = 0;
    private int winSP = 0;
    private int loseSP = 0;
    private int drawSP = 0;
    private int winMoney = 0;
    private int loseMoney = 0;
    private int drawMoney = 0;

    private int countdown = 10;
    private int countdownMax = 10;

    private int leaderboardSize = 5;
    private int leaderboardRecordSize = 30;
    private int leaderboardGamesRequired = 1;

    private int maxTeleportDistance = 64;
    private int announcerRange = 64;

    private boolean opCheck = true;
    private boolean allowInterrupt = false;

    private int duration = 0;
    private int suddenDeath = 0;
    private PotionEffect suddenDeathEffect = null;
    private String startCommands;
    private String endCommands;
    private int borderMin = 0;
    private int borderMax = 0;
    private List<String> magicBlocks;

    private List<ArenaPlayer> leaderboard = new ArrayList<>();
    private Location leaderboardLocation;
    private BlockFace leaderboardFacing;
    private Material signMaterial;

    private int portalDamage;
    private int portalEnterDamage;
    private String portalDeathMessage;

    private final String key;
    private String name;
    private String description;

    private boolean itemWear;
    private boolean keepInventory;
    private boolean keepLevel;
    private boolean allowConsuming = true;
    private boolean allowMelee = true;
    private boolean allowProjectiles = true;
    private boolean heal = true;

    private BossBar respawnBar;

    // Used when loading an arena, load() should be called afterward
    public Arena(final String key, final ArenaTemplate template, final ArenaController controller) {
        this.key = key;
        this.template = template;
        this.controller = controller;
        signMaterial = DefaultMaterials.getWallSignBlock();
    }

    // Used when creating a new arena
    public Arena(final String name, final ArenaTemplate template, final ArenaController plugin, Location location) {
        this(name, template, plugin);
        center = location.clone();
        loadProperties();
    }

    public void loadProperties() {
        if (parameters == null) {
            parameters = ConfigurationUtils.newConfigurationSection("arena " + getKey());
        }
        ConfigurationSection effectiveConfiguration = parameters;
        if (template != null) {
            effectiveConfiguration = ConfigurationUtils.cloneConfiguration(effectiveConfiguration);
            ConfigurationUtils.addConfigurations(effectiveConfiguration, template.getConfiguration(), false);
        }
        loadProperties(effectiveConfiguration);
    }

    private void loadProperties(ConfigurationSection configuration) {
        name = configuration.getString("name", null);
        description = configuration.getString("description", null);
        minPlayers = configuration.getInt("min_players",2);
        minPlayers = Math.max(1, minPlayers);
        maxPlayers = configuration.getInt("max_players", 2);
        requiredKills = configuration.getInt("required_kills", 1);

        portalDamage = configuration.getInt("portal_damage", 0);
        portalEnterDamage = configuration.getInt("portal_enter_damage", 0);
        portalDeathMessage = configuration.getString("portal_death_message");

        leaderboardSize = configuration.getInt("leaderboard_size", 5);
        leaderboardRecordSize = configuration.getInt("leaderboard_record_size", 30);
        leaderboardGamesRequired = configuration.getInt("leaderboard_games_required", 1);

        maxTeleportDistance = configuration.getInt("max_teleport_distance", 64);
        announcerRange = configuration.getInt("announcer_range", 64);

        countdown = configuration.getInt("countdown", 10);
        countdownMax = configuration.getInt("countdown_max", 30);

        opCheck = configuration.getBoolean("op_check", true);
        allowInterrupt = configuration.getBoolean("allow_interrupt", false);
        startCommands = configuration.getString("start_commands");
        endCommands = configuration.getString("end_commands");

        borderMin = configuration.getInt("border_min");
        borderMax = configuration.getInt("border_max");

        itemWear = configuration.getBoolean("item_wear", true);
        keepInventory = configuration.getBoolean("keep_inventory", false);
        keepLevel = configuration.getBoolean("keep_level", false);
        allowConsuming = configuration.getBoolean("allow_consuming", true);
        allowMelee = configuration.getBoolean("allow_melee", true);
        allowProjectiles = configuration.getBoolean("allow_projectiles", true);
        heal = configuration.getBoolean("heal", true);

        lose = ConfigurationUtils.toLocation(configuration.getString("lose"), center);
        win = ConfigurationUtils.toLocation(configuration.getString("win"), center);
        lobby = ConfigurationUtils.toLocation(configuration.getString("lobby"), center);
        exit = ConfigurationUtils.toLocation(configuration.getString("exit"), center);
        String signType = configuration.getString("leaderboard_sign_type", "oak_wall_sign");
        try {
            signMaterial = Material.valueOf(signType.toUpperCase());
        } catch (Exception ex) {
            controller.getPlugin().getLogger().warning("Invalid sign type: " + signType);
        }

        winXP = configuration.getInt("win_xp", 0);
        loseXP = configuration.getInt("lose_xp", 0);
        drawXP = configuration.getInt("draw_xp", 0);

        winSP = configuration.getInt("win_sp", 0);
        loseSP = configuration.getInt("lose_sp", 0);
        drawSP = configuration.getInt("draw_sp", 0);

        winMoney = configuration.getInt("win_money", 0);
        loseMoney = configuration.getInt("lose_money", 0);
        drawMoney = configuration.getInt("draw_money", 0);

        duration = configuration.getInt("duration", 0);
        suddenDeath = configuration.getInt("sudden_death", 0);

        magicBlocks = configuration.getStringList("magic_blocks");

        suddenDeathEffect = null;
        if (configuration.contains("sudden_death_effect")) {
            parseSuddenDeathEffect(configuration.getString("sudden_death_effect"));
        }

        spawns.clear();
        for (String s : configuration.getStringList("spawns")) {
            spawns.add(ConfigurationUtils.toLocation(s, center));
        }

        if (configuration.contains("randomize.spawn")) {
            randomizeSpawn = ConfigurationUtils.toVector(configuration.getString("randomize.spawn"));
        }

        ConfigurationSection defaultStageConfig = configuration.getConfigurationSection("default_stage");
        if (defaultStageConfig != null) {
            defaultStage = new DefaultStage(this, defaultStageConfig);
        } else {
            defaultStage = new DefaultStage(this);
        }

        stages.clear();
        if (configuration.contains("stages")) {
            Collection<ConfigurationSection> stageConfigurations = ConfigurationUtils.getNodeList(configuration, "stages");
            for (ConfigurationSection stageConfiguration : stageConfigurations) {
                stages.add(new ArenaStage(this, stages.size(), stageConfiguration));
            }
        }

        if (configuration.contains("leaderboard_sign_location") && configuration.contains("leaderboard_sign_facing")) {
            leaderboardLocation = ConfigurationUtils.toLocation(configuration.getString("leaderboard_sign_location"), center);
            leaderboardFacing = ConfigurationUtils.toBlockFace(configuration.getString("leaderboard_sign_facing"));
        }
    }

    public String getPlayerMessage(String key, ArenaPlayer player) {
        return convertPlayerMessage(getMessage(key), player);
    }

    public String getAnnounceMessage(String key) {
        return getMessage("announce." + key);
    }

    public String getAnnouncePlayerMessage(String key, ArenaPlayer player) {
        return convertPlayerMessage(getAnnounceMessage(key), player);
    }

    public String convertPlayerMessage(String message, ArenaPlayer player) {
        message = message.replace("$playerDisplay", player.getDisplayName());
        message = message.replace("$playerPath", player.getNameAndPath());
        message = message.replace("$player", player.getName());

        int winCount = player.getWins();
        int lostCount = player.getLosses();
        double health = player.getHealth() / 2;
        int hearts = (int)Math.floor(health);
        String heartDescription = Integer.toString(hearts);
        health = health - hearts;
        if (health >= 0.5) {
            heartDescription = heartDescription + " 1/2";
        }
        message = message.replace("$hearts", heartDescription);
        message = message.replace("$wins", Integer.toString(winCount));
        message = message.replace("$losses", Integer.toString(lostCount));
        return message;
    }

    public String getMessage(String key) {
        Messages messages = controller.getMagic().getMessages();
        String message = messages.get("arenas." + getKey() + "." + key, messages.get("arena." + key));
        return message.replace("$arena", getName());
    }

    public void load(ConfigurationSection configuration) {
        parameters = configuration.getConfigurationSection("parameters");
        center = ConfigurationUtils.toLocation(configuration.getString("location"));
        if (configuration.contains("leaderboard")) {
            leaderboard.clear();
            ConfigurationSection leaders = configuration.getConfigurationSection("leaderboard");
            Collection<String> leaderboardKeys = leaders.getKeys(false);
            for (String key : leaderboardKeys) {
                ConfigurationSection leaderConfig = leaders.getConfigurationSection(key);
                ArenaPlayer loadedPlayer = new ArenaPlayer(this, leaderConfig);
                leaderboard.add(loadedPlayer);
            }
            Collections.sort(leaderboard, new ArenaPlayerComparator());
        }
        loadProperties();
    }

    public void reload() {
        if (template != null) {
            template = controller.getTemplate(template.getKey());
        }
        loadProperties();
    }

    private void parseSuddenDeathEffect(String value) {
        if (value == null || value.isEmpty()) {
            suddenDeathEffect = null;
            return;
        }
        int ticks = 100;
        int power = 1;
        PotionEffectType effectType;
        try {
            String effectName;
            if (value.contains(":")) {
                String[] pieces = value.split(":");
                effectName = pieces[0];
                if (pieces.length > 1) {
                    power = (int)Float.parseFloat(pieces[1]);
                }
                if (pieces.length > 2) {
                    ticks = (int) Float.parseFloat(pieces[2]);
                }
            } else {
                effectName = value;
            }
            effectType = PotionEffectType.getByName(effectName.toUpperCase());
            suddenDeathEffect = new PotionEffect(effectType, ticks, power, true);
        } catch (Exception ex) {
            Bukkit.getLogger().warning("Error parsing potion effect: " + value);
            suddenDeathEffect = null;
        }
    }

    public boolean setSuddenDeathEffect(String value) {
        parseSuddenDeathEffect(value);
        saveSuddenDeathEffect();
        return suddenDeathEffect != null;
    }

    protected void saveSuddenDeathEffect() {
        if (suddenDeathEffect != null) {
            parameters.set("sudden_death_effect",
                    suddenDeathEffect.getType().getName().toLowerCase() + ":"
                            + suddenDeathEffect.getAmplifier() + ":"
                            + suddenDeathEffect.getDuration()
            );
        } else {
            parameters.set("sudden_death_effect", null);
        }
    }

    protected void saveStages() {
        if (stages.isEmpty()) {
            if (parameters != null) {
                parameters.set("stages", null);
            }
            return;
        }

        List<ConfigurationSection> stageConfigurations = new ArrayList<>();
        for (ArenaStage stage : stages) {
            stageConfigurations.add(stage.getConfiguration());
        }
        parameters.set("stages", stageConfigurations);
    }

    protected void saveEditingStage() {
        if (editDefaultStage) {
            saveDefaultStage();
        } else {
            saveStages();
        }
    }

    protected void saveDefaultStage() {
        parameters.set("default_stage", defaultStage.getConfiguration());
    }

    protected void saveSpawns() {
        List<String> spawnList = new ArrayList<>();
        for (Location spawn : spawns) {
            spawnList.add(ConfigurationUtils.fromLocation(spawn, center));
        }
        parameters.set("spawns", spawnList);
    }

    public void save(ConfigurationSection configuration) {
        if (!isValid()) return;

        if (leaderboard.size() > 0) {
            ConfigurationSection leaders = configuration.createSection("leaderboard");
            for (ArenaPlayer player : leaderboard) {
                String key = player.getUUID().toString();
                ConfigurationSection playerData = leaders.createSection(key);
                player.save(playerData);
            }
        }
        configuration.set("location", ConfigurationUtils.fromLocation(center));
        configuration.set("template", template == null ? null : template.getKey());
        configuration.set("parameters", parameters);
    }

    public void respawn() {
        spawnPlayers(new ArrayList<>(this.deadPlayers));
        for (ArenaPlayer dead : deadPlayers) {
            if (dead.isValid() && !dead.isDead()) {
                players.add(dead);
            }
        }
        deadPlayers.clear();
        hideRespawnBossBar();
    }

    protected void runCommands(String runCommands) {
        if (runCommands != null && !runCommands.isEmpty()) {
            String[] commands = StringUtils.split(runCommands, ';');
            CommandSender sender = Bukkit.getConsoleSender();
            for (String command : commands) {
                controller.getPlugin().getServer().dispatchCommand(sender, command);
            }
        }
    }

    public void start() {
        if (!isValid()) return;

        ArenaStartEvent startEvent = new ArenaStartEvent(this);
        Bukkit.getPluginManager().callEvent(startEvent);

        state = ArenaState.ACTIVE;
        started = System.currentTimeMillis();
        lastTick = started;
        currentStage = 0;
        for (ArenaStage stage : stages) {
            stage.reset();
        }
        runCommands(startCommands);
        if (magicBlocks != null) {
            for (String magicBlockKey : magicBlocks) {
                MagicBlock magicBlock = controller.getMagic().getMagicBlock(magicBlockKey);
                if (magicBlock == null) {
                    controller.getMagic().getLogger().warning("Invalid magic block: " + magicBlockKey + " in arena config " + getKey());
                    continue;
                }
                magicBlock.enable();
            }
        }
        if (borderMax > 0 && duration > 0) {
            World world = getCenter().getWorld();
            WorldBorder border = world.getWorldBorder();
            border.setSize(borderMax);
            border.setSize(borderMin, duration / 1000);
        }

        while (queue.size() > 0 && players.size() < maxPlayers) {
            ArenaPlayer queuedPlayer = queue.remove();
            if (queuedPlayer.isValid() && !queuedPlayer.isDead()) {
                players.add(queuedPlayer);
            }
        }
        if (players.size() < minPlayers) {
            queue.addAll(players);
            players.clear();
            state = ArenaState.LOBBY;
            messagePlayers(ChatColor.RED + " the match did not have enough players to start.");
            return;
        }

        spawnPlayers(new ArrayList<>(this.players));

        ArenaStage currentStage = getCurrentStage();
        if (currentStage != null) {
            currentStage.start();
        }

        messageNextRoundPlayerList(getMessage("next"));
    }

    protected List<Location> getRandomSpawns() {
        List<Location> spawns = getSpawns();
        Collections.shuffle(spawns);
        return spawns;
    }

    protected void heal(ArenaPlayer player) {
        if (heal) {
            player.heal();
        }
    }

    protected void spawnPlayers(Collection<ArenaPlayer> players, List<Location> spawns) {
        int num = 0;
        for (ArenaPlayer arenaPlayer : players) {
            if (!arenaPlayer.isValid() || arenaPlayer.isDead()) {
                continue;
            }
            heal(arenaPlayer);
            arenaPlayer.sendMessage(getMessage("start"));

            Location spawn = spawns.get(num);
            if (randomizeSpawn != null) {
                spawn = spawn.clone();
                spawn.add(
                (2 * random.nextDouble() - 1) * randomizeSpawn.getX(),
                (2 * random.nextDouble() - 1) * randomizeSpawn.getY(),
                (2 * random.nextDouble() - 1) * randomizeSpawn.getZ()
                );
            }

            // Wrap index around to player
            num = (num + 1) % spawns.size();
            arenaPlayer.teleport(spawn);
        }
    }

    protected void spawnPlayers(Collection<ArenaPlayer> players) {
        spawnPlayers(players, getRandomSpawns());
    }

    protected void spawnPlayer(ArenaPlayer player) {
        List<ArenaPlayer> playerList = new ArrayList<>();
        playerList.add(player);
        spawnPlayers(playerList, getRandomSpawns());
    }

    protected void messageNextRoundPlayerList(String message) {
        Collection<ArenaPlayer> nextUpPlayers = getNextRoundPlayers();
        for (ArenaPlayer messagePlayer : nextUpPlayers) {
            Player player = messagePlayer.getPlayer();
            if (player != null) {
                String messagePlayerName = messagePlayer.getNameAndPath();
                player.sendMessage(message);
                for (ArenaPlayer otherArenaPlayer : nextUpPlayers) {
                    String otherPlayerName = otherArenaPlayer.getNameAndPath();
                    String opponentMessage = getPlayerMessage("opponent", otherArenaPlayer);
                    if (!otherPlayerName.equals(messagePlayerName) && !opponentMessage.isEmpty()) {
                        player.sendMessage(opponentMessage);
                    }
                }
            }
        }
    }

    public void remove(Player player) {
        ArenaPlayer removePlayer = new ArenaPlayer(this, player);
        players.remove(removePlayer);
        deadPlayers.remove(removePlayer);
        queue.remove(removePlayer);
        controller.unregister(player);
        if (respawnBar != null) {
            respawnBar.removePlayer(player);
        }
    }

    public void remove() {
        messagePlayers(ChatColor.RED + "This arena has been removed");
        stop();
        clearQueue();
    }

    public ArenaPlayer getWinner() {
        if (players.size() == 1) {
            ArenaPlayer winner = players.iterator().next();
            return winner;
        }

        return null;
    }

    public Location getLobby() {
        return lobby == null ? center : lobby;
    }

    public Location getLoseLocation() {
        return lose == null ? getExit() : lose;
    }

    public Location getWinLocation() {
        return win == null ? getExit() : win;
    }

    public Location getCenter() {
        return center;
    }

    public Location getExit() {
        return exit == null ? getLobby() : exit;
    }

    public boolean isReady() {
        return state == ArenaState.LOBBY && queue.size() >= minPlayers;
    }

    public void lobbyMessage() {
        int playerCount = queue.size();
        if (playerCount < minPlayers) {
            int playersRemaining = minPlayers - playerCount;
            if (playersRemaining == 1) {
                messageNextRoundPlayers(getMessage("waiting_1").replace("$count", Integer.toString(playersRemaining)));
            } else {
                messageNextRoundPlayers(getMessage("waiting").replace("$count", Integer.toString(playersRemaining)));
            }
        }
    }

    public void announce(String message) {
       List<String> lines = Splitter.on("\n").trimResults().splitToList(message);
       for (String line : lines) {
           sendAnnounce(line);
       }
    }

    private void sendAnnounce(String message) {
        if (message.isEmpty()) return;
        int rangeSquared = announcerRange * announcerRange;
        Collection<? extends Player> players = controller.getPlugin().getServer().getOnlinePlayers();
        for (Player player : players) {
            Location playerLocation = player.getLocation();
            if (center == null || !playerLocation.getWorld().equals(center.getWorld())) continue;
            if (playerLocation.distanceSquared(center) < rangeSquared) {
                player.sendMessage(message);
            }
        }
    }

    protected void messagePlayers(String message, Collection<ArenaPlayer> players) {
        if (message.isEmpty()) return;
        for (ArenaPlayer arenaPlayer : players) {
            arenaPlayer.sendMessage(message);
        }
    }

    public void messagePlayers(String message) {
        messagePlayers(message, getAllPlayers());
    }

    public void messageInGamePlayers(String message) {
        messagePlayers(message, players);
    }

    public void messageDeadPlayers(String message) {
        messagePlayers(message, deadPlayers);
    }

    public void messageNextRoundPlayers(String message) {
        messagePlayers(message, getNextRoundPlayers());
    }

    public void startCountdown() {
        startCountdown(countdown);
    }

    public void startCountdown(int time) {
        if (state != ArenaState.LOBBY) return;
        state = ArenaState.COUNTDOWN;
        messageNextRoundPlayerList(getMessage("starting"));
        countdown(time);
    }

    private void countdown(final int time) {
        if (state != ArenaState.COUNTDOWN) {
            return;
        }

        if (time <= 0) {
            start();
            return;
        }

        if (time % 10 == 0) {
            String message = getMessage("countdown_10");
            messageNextRoundPlayers(message.replace("$countdown", Integer.toString(time)));
        } else if (time <= 5) {
            String message = getMessage("countdown");
            messageNextRoundPlayers(message.replace("$countdown", Integer.toString(time)));
        }
        BukkitScheduler scheduler = controller.getPlugin().getServer().getScheduler();
        scheduler.runTaskLater(controller.getPlugin(), new Runnable() {
            @Override
            public void run() {
                countdown(time - 1);
            }
        }, 20);
    }

    public boolean stop() {
        if (state == ArenaState.LOBBY) return false;
        messageInGamePlayers(getMessage("cancelled"));
        finish();
        return true;
    }

    protected void finish() {
        ArenaStage currentStage = getCurrentStage();
        if (currentStage != null) {
            currentStage.finish();
        }
        runCommands(endCommands);
        if (magicBlocks != null) {
            for (String magicBlockKey : magicBlocks) {
                MagicBlock magicBlock = controller.getMagic().getMagicBlock(magicBlockKey);
                if (magicBlock == null) {
                    controller.getMagic().getLogger().warning("Invalid magic block: " + magicBlockKey + " in arena config " + getKey());
                    continue;
                }
                magicBlock.disable();
            }
        }
        state = ArenaState.LOBBY;
        exitPlayers();
        hideRespawnBossBar();
        clearPlayers();

        ArenaStopEvent stopEvent = new ArenaStopEvent(this);
        Bukkit.getPluginManager().callEvent(stopEvent);

        // Check for a new start
        checkStart();
    }

    protected Collection<ArenaPlayer> getParticipants() {
        ArenaStage stage = getCurrentStage();
        if (stage == null || !stage.isRespawnEnabled() || deadPlayers.isEmpty()) {
            return players;
        }
        List<ArenaPlayer> participants = new ArrayList<>(players);
        participants.addAll(deadPlayers);
        return participants;
    }

    protected void exitPlayers() {
        for (ArenaPlayer arenaPlayer : getParticipants()) {
            arenaPlayer.teleport(getExit());
        }
    }

    protected void clearPlayers() {
        for (ArenaPlayer arenaPlayer : players) {
            Player player = arenaPlayer.getPlayer();
            if (player != null && !queue.contains(arenaPlayer)) {
                controller.unregister(player);
            }
        }
        for (ArenaPlayer arenaPlayer : deadPlayers) {
            Player player = arenaPlayer.getPlayer();
            if (player != null && !queue.contains(arenaPlayer)) {
                controller.unregister(player);
            }
        }
        players.clear();
        deadPlayers.clear();
    }

    protected void clearQueue() {
        for (ArenaPlayer arenaPlayer : queue) {
            Player player = arenaPlayer.getPlayer();
            if (player != null) {
                Location exit = getExit();
                if (exit != null) {
                    arenaPlayer.teleport(getExit());
                }
                controller.unregister(player);
            }
        }
        queue.clear();
    }

    public boolean isStarted() {
        return state == ArenaState.ACTIVE || state == ArenaState.WON;
    }

    public boolean isFull() {
        return queue.size() >= maxPlayers;
    }

    public boolean isPlayersFull() {
        return players.size() + deadPlayers.size() >= maxPlayers;
    }

    public ArenaPlayer add(Player player) {
        ArenaPlayer arenaPlayer = new ArenaPlayer(this, player);
        queue.add(arenaPlayer);
        arenaPlayer.teleport(getLobby());
        controller.register(player, arenaPlayer);
        return arenaPlayer;
    }

    public ArenaPlayer interrupt(Player player) {
        ArenaPlayer arenaPlayer = new ArenaPlayer(this, player);
        if (!arenaPlayer.isValid() || arenaPlayer.isDead()) return arenaPlayer;

        ArenaStage stage = getCurrentStage();
        if (stage != null && stage.isRespawning()) {
            deadPlayers.add(arenaPlayer);
            arenaPlayer.teleport(getLobby());
        } else {
            players.add(arenaPlayer);
            spawnPlayer(arenaPlayer);
        }
        controller.register(player, arenaPlayer);

        return arenaPlayer;
    }

    public boolean isDead(ArenaPlayer arenaPlayer) {
        return deadPlayers.contains(arenaPlayer);
    }

    public ArenaStage getCurrentStage() {
        if (stages.isEmpty()) {
            return null;
        }
        if (currentStage >= stages.size() - 1) {
            return stages.get(stages.size() - 1);
        }
        if (currentStage >= 0) {
            return stages.get(currentStage);
        }
        return null;
    }

    public boolean nextStage() {
        if (stages.isEmpty()) {
            return false;
        }
        if (currentStage >= stages.size() - 1) {
            return false;
        }
        currentStage++;
        ArenaStage currentStage = getCurrentStage();
        // This should really never be null now, since we know we have stages
        if (currentStage != null) {
            currentStage.start();
        }
        return currentStage != null;
    }

    public void addStage() {
        stages.add(new ArenaStage(this, stages.size()));
        editingStage = stages.size() - 1;
        saveStages();
    }

    public void addStageBeforeCurrent() {
        stages.add(editingStage, new ArenaStage(this, stages.size()));
        reindexStages();
        saveStages();
    }

    public void addStageAfterCurrent() {
        editingStage++;
        stages.add(editingStage, new ArenaStage(this, stages.size()));
        reindexStages();
        saveStages();
    }

    public void moveCurrentStage(int newIndex) {
        ArenaStage moveStage = stages.remove(editingStage);
        editingStage = newIndex;
        stages.add(editingStage, moveStage);
        reindexStages();
        saveStages();
    }

    public void removeStage() {
        if (stages.size() <= 1) return;
        if (editingStage < 0 || editingStage >= stages.size()) return;
        stages.remove(editingStage);
        editingStage = 0;
        reindexStages();
        saveStages();
    }

    public void reindexStages() {
        for (int i = 0; i < stages.size(); i++) {
            stages.get(i).setIndex(i);
        }
    }

    public DefaultStage getDefaultStage() {
        return defaultStage;
    }

    public EditingStage getEditingStage() {
        if (editDefaultStage) {
            return defaultStage;
        }
        return stages.get(getEditingStageIndex());
    }

    public EditingStage getIfEditingStage() {
        if (editDefaultStage) {
            return defaultStage;
        }
        if (editingStage < 0 || editingStage >= stages.size()) {
            return null;
        }
        return stages.get(editingStage);
    }

    public void setEditingStage(int stage) {
        editingStage = stage;
        editDefaultStage = false;
    }

    public int getEditingStageIndex() {
        if (stages.isEmpty()) {
            stages.add(new ArenaStage(this, 0));
            editingStage = 0;
        }
        if (editingStage < 0 || editingStage >= stages.size()) {
            editingStage = 0;
        }
        return editingStage;
    }

    public int getStageCount() {
        return stages.size();
    }

    public void check() {
        if (state == ArenaState.COUNTDOWN) {
            if (!isReady()) {
                messagePlayers(ChatColor.RED + " Countdown cancelled");
                state = ArenaState.LOBBY;
                checkStart();
            }
            return;
        }

        if (state == ArenaState.LOBBY) {
            return;
        }

        final Server server = controller.getPlugin().getServer();
        if (players.size() == 0 && state != ArenaState.WON) {
            if (isMobArena()) {
                for (ArenaPlayer loser : deadPlayers) {
                    loser.lost();
                    updateLeaderboard(loser);
                }
                announce(getAnnounceMessage("lose"));
            } else {
                announce(getAnnounceMessage("default"));
            }
            exitPlayers();
            finish();
            return;
        }

        if (state != ArenaState.WON && isMobArena()) {
            ArenaStage currentStage = getCurrentStage();
            if (currentStage == null || currentStage.isFinished()) {
                if (currentStage != null) {
                    currentStage.completed();
                }
                if (!nextStage()) {
                    state = ArenaState.WON;
                    server.getScheduler().runTaskLater(controller.getPlugin(), new Runnable() {
                        @Override
                        public void run() {
                            for (final ArenaPlayer winner : getParticipants()) {
                                if (winner != null)
                                {
                                    playerWon(winner);
                                    heal(winner);
                                }
                            }
                            finish();
                        }
                    }, 5 * 20);
                }
            }
        } else if (players.size() == 1 && state != ArenaState.WON) {
            state = ArenaState.WON;
            server.getScheduler().runTaskLater(controller.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    final ArenaPlayer winner = getWinner();
                    final boolean defaulted = deadPlayers.size() < requiredKills;
                    final boolean won = winner != null && winner.isValid() && !winner.isDead();
                    if (defaulted) {
                        if (winner != null) {
                            winner.teleport(getExit());
                        }
                        announce(getAnnounceMessage("default"));
                    } else if (won) {
                        playerWon(winner);
                    } else {
                        if (winner != null) {
                            winner.draw();
                        }
                        for (ArenaPlayer loser : deadPlayers) {
                            loser.draw();
                            heal(loser);
                        }
                        announce(getAnnounceMessage("draw"));
                    }
                    if (winner != null) {
                        heal(winner);
                    }
                    finish();
                }
            }, 5 * 20);
        }
    }

    protected void playerWon(ArenaPlayer winner) {
        winner.won();
        updateLeaderboard(winner);
        for (ArenaPlayer loser : deadPlayers) {
            loser.lost();
            updateLeaderboard(loser);
        }
        updateLeaderboard();
        winner.sendMessage(getMessage("win"));
        announce(getAnnouncePlayerMessage("win", winner));
        winner.teleport(getWinLocation());
    }

    public void join(Player player) {
        ArenaPlayer arenaPlayer = controller.getArenaPlayer(player);
        Arena currentArena = arenaPlayer == null ? null : arenaPlayer.getArena();
        if (currentArena != null) {
            if (currentArena == this) {
                // If we have lost, we can queue for the next round
                if (currentArena.isDead(arenaPlayer)) {
                    if (currentArena.queue.contains(arenaPlayer)) {
                        player.sendMessage(getMessage("queued"));
                        return;
                    }
                } else {
                    player.sendMessage(getMessage("already"));
                    return;
                }
            } else {
                controller.leave(player);
            }
        }

        boolean queue = true;
        boolean started = isStarted();
        if (started && allowInterrupt && !isPlayersFull()) {
            queue = false;
            player.sendMessage(getMessage("joined"));
        } else {
            if (isFull()) {
                player.sendMessage(getMessage("joined_queue"));
            } else {
                player.sendMessage(getMessage("joined_next_queue"));
            }
        }
        if (description != null) {
            player.sendMessage(ChatColor.LIGHT_PURPLE + getDescription());
        }
        arenaPlayer = queue ? add(player) : interrupt(player);

        int winCount = arenaPlayer.getWins();
        int lostCount = arenaPlayer.getLosses();
        int joinedCount = arenaPlayer.getJoins();

        arenaPlayer.joined();

        if (winCount == 0 && lostCount == 0 && joinedCount == 0) {
            announce(getAnnouncePlayerMessage("join_first", arenaPlayer));
        } else {
            announce(getAnnouncePlayerMessage("join", arenaPlayer));
        }
        checkStart();
    }

    protected void checkStart() {
        if (isStarted()) return;

        if (isReady()) {
            if (isFull()) {
                startCountdown(countdown);
            } else {
                startCountdown(countdownMax);
            }
        } else {
            lobbyMessage();
        }
    }

    protected Collection<ArenaPlayer> getAllPlayers() {
        List<ArenaPlayer> allPlayers = new ArrayList<>(players);
        allPlayers.addAll(queue);
        allPlayers.addAll(deadPlayers);
        return allPlayers;
    }

    protected Collection<ArenaPlayer> getNextRoundPlayers() {
        List<ArenaPlayer> allPlayers = new ArrayList<>();
        for (ArenaPlayer queuedPlayer : queue) {
            if (allPlayers.size() >= maxPlayers) break;
            allPlayers.add(queuedPlayer);
        }
        return allPlayers;
    }

    public void describe(CommandSender sender) {
        String displayName;
        if (name == null) {
            displayName = ChatColor.DARK_AQUA + getName();
        } else {
            displayName = ChatColor.DARK_AQUA + getName() + ChatColor.DARK_GRAY + " (" + ChatColor.GRAY + getKey() + ChatColor.DARK_GRAY + ")";
        }
        if (template != null) {
            displayName += ChatColor.DARK_GRAY + " [" + ChatColor.GRAY + template.getKey() + ChatColor.DARK_GRAY + "]";
        }
        sender.sendMessage(displayName);
        if (description != null) {
            sender.sendMessage(ChatColor.LIGHT_PURPLE + getDescription());
        }
        if (heal) {
            sender.sendMessage(ChatColor.GREEN + "Players will be healed before and after a match");
        }
        if (opCheck) {
            sender.sendMessage(ChatColor.RED + "OP Wand Check Enabled");
        }
        if (allowInterrupt) {
            sender.sendMessage(ChatColor.YELLOW + "Allows joining mid-match");
        }
        if (keepInventory) {
            sender.sendMessage(ChatColor.GREEN + "Players keep their inventory on death");
        }
        if (!itemWear) {
            sender.sendMessage(ChatColor.GREEN + "Players' items do not get worn out");
        }
        if (keepLevel) {
            sender.sendMessage(ChatColor.GREEN + "Players keep their XP levels on death");
        }
        if (!allowConsuming) {
            sender.sendMessage(ChatColor.GREEN + "Players may not eat or drink potions");
        }
        if (!allowMelee) {
            sender.sendMessage(ChatColor.GREEN + "Players may not use melee weapons");
        }
        if (!allowProjectiles) {
            sender.sendMessage(ChatColor.GREEN + "Players may not use bows or other projectile weapons");
        }
        int minPlayers = getMinPlayers();
        int maxPlayers = getMaxPlayers();
        sender.sendMessage(ChatColor.AQUA + "Min / Max Players: " + ChatColor.DARK_AQUA + minPlayers
                + ChatColor.WHITE + " / " + ChatColor.DARK_AQUA + maxPlayers);
        sender.sendMessage(ChatColor.AQUA + "Required Kills: " + ChatColor.DARK_AQUA + requiredKills);
        sender.sendMessage(ChatColor.AQUA + "Countdown: " + ChatColor.DARK_AQUA + countdown
                + ChatColor.WHITE + " / " + ChatColor.DARK_AQUA + countdownMax);

        if (duration > 0) {
            int minutes = (int)Math.ceil((double)duration / 60 / 1000);
            int sd = (int)Math.ceil((double)suddenDeath / 1000);
            sender.sendMessage(ChatColor.AQUA + "Duration: " + ChatColor.DARK_AQUA + minutes
                    + ChatColor.WHITE + " minutes");
            if (suddenDeathEffect != null && suddenDeath > 0) {
                sender.sendMessage(ChatColor.DARK_RED + " Sudden death " + ChatColor.RED + sd + ChatColor.DARK_RED
                + " seconds before end with " + ChatColor.RED + suddenDeathEffect.getType().getName().toLowerCase()
                + "@" + suddenDeathEffect.getAmplifier());
            }
        }
        if (startCommands != null && !startCommands.isEmpty()) {
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "Start Commands: " + ChatColor.AQUA + startCommands);
        }
        if (endCommands != null && !endCommands.isEmpty()) {
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "End Commands: " + ChatColor.AQUA + endCommands);
        }
        if (borderMax > 0) {
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "Border: " + ChatColor.AQUA + borderMax + ChatColor.LIGHT_PURPLE + " to " + ChatColor.AQUA + borderMin);
        }

        if (winXP > 0) {
            sender.sendMessage(ChatColor.AQUA + "Winning Reward: " + ChatColor.LIGHT_PURPLE + winXP + ChatColor.AQUA + " xp");
        }
        if (loseXP > 0) {
            sender.sendMessage(ChatColor.AQUA + "Losing Reward: " + ChatColor.LIGHT_PURPLE + loseXP + ChatColor.AQUA + " xp");
        }
        if (drawXP > 0) {
            sender.sendMessage(ChatColor.AQUA + "Draw Reward: " + ChatColor.LIGHT_PURPLE + drawXP + ChatColor.AQUA + " xp");
        }

        if (winSP > 0) {
            sender.sendMessage(ChatColor.AQUA + "Winning Reward: " + ChatColor.LIGHT_PURPLE + winSP + ChatColor.AQUA + " sp");
        }
        if (loseSP > 0) {
            sender.sendMessage(ChatColor.AQUA + "Losing Reward: " + ChatColor.LIGHT_PURPLE + loseSP + ChatColor.AQUA + " sp");
        }
        if (drawSP > 0) {
            sender.sendMessage(ChatColor.AQUA + "Draw Reward: " + ChatColor.LIGHT_PURPLE + drawSP + ChatColor.AQUA + " sp");
        }

        if (winMoney > 0) {
            sender.sendMessage(ChatColor.AQUA + "Winning Reward: $" + ChatColor.LIGHT_PURPLE + winMoney + ChatColor.AQUA);
        }
        if (loseMoney > 0) {
            sender.sendMessage(ChatColor.AQUA + "Losing Reward: $" + ChatColor.LIGHT_PURPLE + loseMoney + ChatColor.AQUA);
        }
        if (drawMoney > 0) {
            sender.sendMessage(ChatColor.AQUA + "Draw Reward: $" + ChatColor.LIGHT_PURPLE + drawMoney + ChatColor.AQUA);
        }

        int spawnSize = spawns.size();
        if (spawnSize == 1) {
            sender.sendMessage(ChatColor.BLUE + "Spawn: " + printLocation(spawns.get(0)));
        } else {
            sender.sendMessage(ChatColor.BLUE + "Spawns: " + ChatColor.GRAY + spawnSize);
            for (Location spawn : spawns) {
                sender.sendMessage(ChatColor.GRAY + " " + printLocation(spawn));
            }
        }
        if (randomizeSpawn != null) {
            sender.sendMessage(ChatColor.DARK_BLUE + " Randomize: " + ChatColor.BLUE + randomizeSpawn);
        }
        sender.sendMessage(ChatColor.BLUE + "Lobby: " + printLocation(lobby));
        sender.sendMessage(ChatColor.BLUE + "Win: " + printLocation(win));
        sender.sendMessage(ChatColor.BLUE + "Lose: " + printLocation(lose));
        sender.sendMessage(ChatColor.BLUE + "Exit: " + printLocation(exit));
        sender.sendMessage(ChatColor.BLUE + "Center: " + printLocation(center));
        int numStages = stages.size();
        if (numStages > 0) {
            sender.sendMessage(ChatColor.BLUE + "Stages: " + ChatColor.GRAY + numStages);
        }
        if (state == ArenaState.ACTIVE) {
            ArenaStage currentStage = getCurrentStage();
            if (currentStage != null) {
                sender.sendMessage(ChatColor.BLUE + "Active mobs: " + ChatColor.GRAY + currentStage.getActiveMobs());
            }
        }
        if (magicBlocks != null && !magicBlocks.isEmpty()) {
            sender.sendMessage(ChatColor.BLUE + "Magic blocks: " + ChatColor.AQUA + StringUtils.join(magicBlocks, ChatColor.GRAY + "," + ChatColor.AQUA));
        }
        if (portalDamage > 0 || portalEnterDamage > 0) {
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "Portal Entry Damage: " + ChatColor.DARK_PURPLE + portalEnterDamage);
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "Portal Damage: " + ChatColor.DARK_PURPLE + portalDamage);
            if (portalDeathMessage != null && !portalDeathMessage.isEmpty()) {
                sender.sendMessage(ChatColor.LIGHT_PURPLE + "Portal Death Message: " + ChatColor.DARK_PURPLE + portalDeathMessage);
            }
        }
        sender.sendMessage(ChatColor.YELLOW + "Announcer Range: " + ChatColor.GOLD + announcerRange);
        sender.sendMessage(ChatColor.YELLOW + "Leaderboard Size: " + ChatColor.GOLD + leaderboardSize + ChatColor.WHITE + "/" + ChatColor.GOLD + leaderboardRecordSize);
        sender.sendMessage(ChatColor.AQUA + "State: " + ChatColor.DARK_AQUA + state);

        int inGamePlayers = getInGamePlayers();
        sender.sendMessage(ChatColor.DARK_GREEN + "Active Players: " + ChatColor.GREEN + inGamePlayers);
        for (ArenaPlayer player : players) {
            sender.sendMessage(ChatColor.GOLD + " " + player.getDisplayName());
        }
        int deathCount = deadPlayers.size();
        sender.sendMessage(ChatColor.DARK_RED + "Dead Players: " + ChatColor.RED + deathCount);
        for (ArenaPlayer player : deadPlayers) {
            sender.sendMessage(ChatColor.RED + " " + player.getDisplayName());
        }
        int queuedPlayers = getQueuedPlayers();
        sender.sendMessage(ChatColor.YELLOW + "Queued Players: " + ChatColor.GOLD + queuedPlayers);
        for (ArenaPlayer player : queue) {
            sender.sendMessage(ChatColor.YELLOW + " " + player.getDisplayName());
        }
    }

    public void describeStages(CommandSender sender) {
        int numStages = stages.size();
        sender.sendMessage(ChatColor.BLUE + "Stages: " + ChatColor.GRAY + numStages);
        int stageNumber = 1;
        for (ArenaStage stage : stages) {
            String prefix = " ";
            if (editDefaultStage || stageNumber == stage.getNumber()) {
                prefix = ChatColor.YELLOW + "*";
            } else {
                prefix = " ";
            }
            stageNumber++;
            sender.sendMessage(prefix + ChatColor.GRAY + stage.getName() + ": " + ChatColor.AQUA + stage.getName());
        }
    }

    protected String printLocation(Location location) {
        if (location == null) return ChatColor.DARK_GRAY + "(None)";

        return "" + ChatColor.GRAY + location.getBlockX() + ChatColor.DARK_GRAY + ","
                + ChatColor.GRAY + location.getBlockY() + ChatColor.DARK_GRAY + ","
                + ChatColor.GRAY + location.getBlockZ() + ChatColor.DARK_GRAY + " : "
                + ChatColor.GRAY + location.getWorld().getName();
    }

    public void mobDied(Entity entity) {
        ArenaStage currentStage = getCurrentStage();
        if (currentStage != null) {
            currentStage.mobDied(entity);
            if (isStarted()) {
                check();
            }
        }
    }

    public void died(Player player) {
        ArenaPlayer arenaPlayer = new ArenaPlayer(this, player);
        if (isStarted()) {
            if (players.contains(arenaPlayer)) {
                lastDeath = System.currentTimeMillis();
                deadPlayers.add(arenaPlayer);
                players.remove(arenaPlayer);

                ArenaStage currentStage = getCurrentStage();
                long respawnDuration = currentStage != null ? currentStage.getRespawnDuration() : 0;
                if (respawnDuration > 0) {
                    Location lobby = getLobby();
                    player.setMetadata("respawnLocation", new FixedMetadataValue(controller.getPlugin(), lobby));
                    long seconds = respawnDuration / 1000;
                    String message = getMessage("died");
                    message =  message.replace("$respawn", Long.toString(seconds));
                    player.sendMessage(message);
                } else {
                    Location specroom = getLoseLocation();
                    player.setMetadata("respawnLocation", new FixedMetadataValue(controller.getPlugin(), specroom));
                    player.sendMessage(getMessage("lost"));
                }
            }
        } else {
            if (queue.contains(arenaPlayer)) {
                player.sendMessage(getMessage("false_start"));
            }
            queue.remove(arenaPlayer);
        }
        check();
    }

    public ArenaController getController() {
        return controller;
    }

    public void removeFromLeaderboard(ArenaPlayer removePlayer) {
        leaderboard.remove(removePlayer);
    }

    public void updateLeaderboard(ArenaPlayer changedPlayer) {
        int wins = changedPlayer.getWins();
        int losses = changedPlayer.getLosses();

        leaderboard.remove(changedPlayer);
        if (wins + losses < leaderboardGamesRequired) {
            return;
        }

        leaderboard.add(changedPlayer);
        Collections.sort(leaderboard, new ArenaPlayerComparator());
        setLeaderboardSize(leaderboardSize);
    }

    public void updateLeaderboard() {
        MaterialAndData skullMaterial = DefaultMaterials.getPlayerSkullWallBlock();
        Block leaderboardBlock = getLeaderboardBlock();
        if (leaderboardBlock != null && leaderboardFacing != null) {
            BlockFace rightDirection = DirectionUtils.goLeft(leaderboardFacing);
            leaderboardBlock = leaderboardBlock.getRelative(BlockFace.UP);
            int size = Math.min(leaderboard.size(), leaderboardSize);
            BlockFace skullFace = leaderboardFacing;
            for (int i = size - 1; i >= 0; i--) {
                ArenaPlayer player = leaderboard.get(i);
                if (canReplace(leaderboardBlock)) {
                    skullMaterial.modify(leaderboardBlock);

                    BlockState blockState = leaderboardBlock.getState();

                    // Apply rotation for legacy versions
                    if (!CompatibilityLib.isCurrentVersion()) {
                        MaterialData data = blockState.getData();
                        if (data instanceof Skull) {
                            ((Skull) data).setFacingDirection(skullFace);
                            blockState.setData(data);
                            blockState.update(false, false);
                        }
                    }

                    if (blockState instanceof org.bukkit.block.Skull) {
                        org.bukkit.block.Skull skullBlock = (org.bukkit.block.Skull)blockState;
                        if (!CompatibilityLib.isCurrentVersion()) {
                            skullBlock.setSkullType(SkullType.PLAYER);
                        }
                        skullBlock.setRotation(skullFace);
                        controller.getMagic().setSkullOwner(skullBlock, player.getUUID());
                    }
                }
                Block neighborBlock = leaderboardBlock.getRelative(rightDirection);
                if (canReplace(neighborBlock)) {
                    neighborBlock.setType(signMaterial);

                    BlockState blockState = neighborBlock.getState();
                    Object data = blockState.getData();
                    if (data instanceof org.bukkit.material.Sign) {
                        org.bukkit.material.Sign signData = (org.bukkit.material.Sign)data;
                        signData.setFacingDirection(leaderboardFacing);
                        blockState.setData(signData);
                    }
                    if (blockState instanceof org.bukkit.block.Sign) {
                        org.bukkit.block.Sign signBlock = (org.bukkit.block.Sign)blockState;
                        String playerName = ChatColor.DARK_PURPLE + player.getDisplayName();
                        signBlock.setLine(0, playerName);
                        signBlock.setLine(1, ChatColor.LIGHT_PURPLE + "#" + Integer.toString(i + 1) + " " + ChatColor.WHITE
                            + " : " + ChatColor.BLACK + Integer.toString((int)(player.getWinRatio() * 100))
                            + "% " + String.format("(%.2f)", player.getWinConfidence()));
                        signBlock.setLine(2, ChatColor.GREEN + "Wins   : " + ChatColor.DARK_GREEN + player.getWins());
                        signBlock.setLine(3, ChatColor.RED + "Losses : " + ChatColor.DARK_RED + player.getLosses());

                    }
                    blockState.update();
                }
                leaderboardBlock = leaderboardBlock.getRelative(BlockFace.UP);
            }
        }
    }

    protected void clearLeaderboardBlock(Block block) {
        Material blockType = block.getType();
        MaterialAndData skullMaterial = DefaultMaterials.getPlayerSkullWallBlock();
        if (blockType == skullMaterial.getMaterial() || DefaultMaterials.isSign(blockType)) {
            block.setType(Material.AIR);
        }
    }

    protected boolean canReplace(Block block) {
        Material blockType = block.getType();
        if (controller.getMagic().getMaterialSetManager().getMaterialSet("all_air").testBlock(block)) {
            return true;
        }
        MaterialAndData skullMaterial = DefaultMaterials.getPlayerSkullWallBlock();
        return DefaultMaterials.isSign(blockType) || blockType == skullMaterial.getMaterial();
    }

    protected Block getLeaderboardBlock() {
        Block block = null;
        if (leaderboardLocation != null) {
            Block testBlock = leaderboardLocation.getBlock();
            if (DefaultMaterials.isSign(testBlock.getType())) {
                block = testBlock;
            } else {
                leaderboardLocation = null;
                leaderboardFacing = null;
            }
        }
        return block;
    }

    public void removeLeaderboard() {
        Block leaderboardBlock = getLeaderboardBlock();
        if (leaderboardBlock != null && leaderboardFacing != null) {
            BlockFace rightDirection = DirectionUtils.goLeft(leaderboardFacing);
            for (int y = 0; y <= leaderboardSize; y++) {
                Block neighborBlock = leaderboardBlock.getRelative(rightDirection);
                clearLeaderboardBlock(neighborBlock);
                if (y != 0) {
                    clearLeaderboardBlock(leaderboardBlock);
                }
                leaderboardBlock = leaderboardBlock.getRelative(BlockFace.UP);
            }
        }
    }

    protected void trimLeaderboard() {
        removeLeaderboard();
        while (leaderboard.size() > leaderboardRecordSize) {
            leaderboard.remove(leaderboard.size() - 1);
        }
        updateLeaderboard();
    }

    public void setLeaderboardRecordSize(int size) {
        leaderboardRecordSize = size;
        if (leaderboardSize > leaderboardRecordSize) {
            leaderboardSize = leaderboardRecordSize;
        }
        trimLeaderboard();
        parameters.set("leaderboard_record_size", leaderboardRecordSize);
    }

    public void setLeaderboardSize(int size) {
        leaderboardSize = size;
        if (leaderboardSize > leaderboardRecordSize) {
            leaderboardRecordSize = leaderboardSize;
        }
        trimLeaderboard();
        parameters.set("leaderboard_size", leaderboardSize);
    }

    public void setLeaderboardGamesRequired(int required) {
        leaderboardGamesRequired = required;
        Collection<ArenaPlayer> currentLeaderboard = new ArrayList<>(leaderboard);
        leaderboard.clear();
        for (ArenaPlayer player : currentLeaderboard) {
            updateLeaderboard(player);
        }
        updateLeaderboard();
        parameters.set("leaderboard_games_required", leaderboardGamesRequired);
    }

    public void describeStats(CommandSender sender, Player player) {
        ArenaPlayer arenaPlayer = new ArenaPlayer(this, player);

        int wins = arenaPlayer.getWins();
        int losses = arenaPlayer.getLosses();
        int draws = arenaPlayer.getDraws();
        int quits = arenaPlayer.getQuits();
        float ratio = arenaPlayer.getWinRatio();
        double confidence = arenaPlayer.getWinConfidence();

        Integer rank = null;
        int ranking = 1;
        for (ArenaPlayer testPlayer : leaderboard) {
            if (testPlayer.equals(arenaPlayer)) {
                rank = ranking;
                break;
            }
            ranking++;
        }
        if (rank != null) {
            sender.sendMessage(ChatColor.DARK_PURPLE + player.getDisplayName() + ChatColor.DARK_PURPLE
                + " is ranked " + ChatColor.AQUA + "#" + Integer.toString(rank)
                + ChatColor.DARK_PURPLE + " for " + ChatColor.GOLD + getName());
        } else {
            sender.sendMessage(ChatColor.DARK_PURPLE + player.getDisplayName() + ChatColor.DARK_RED
                    + " is not on the leaderboard for " + ChatColor.GOLD + getName());
        }

        Arena currentArena = controller.getQueuedArena(player);
        if (currentArena != null) {
            sender.sendMessage(ChatColor.DARK_PURPLE + player.getDisplayName()
                + ChatColor.LIGHT_PURPLE + " is currently in " + ChatColor.GOLD + currentArena.getName());
        }

        sender.sendMessage(ChatColor.GREEN + "Wins: " + ChatColor.WHITE + Integer.toString(wins));
        sender.sendMessage(ChatColor.RED + "Losses: " + ChatColor.WHITE + Integer.toString(losses));
        sender.sendMessage(ChatColor.GOLD + "Win Ratio: " + ChatColor.WHITE + Integer.toString((int)(ratio * 100)) + "% " + String.format("(%.2f)", confidence));
        sender.sendMessage(ChatColor.YELLOW + "Draws: " + ChatColor.WHITE + Integer.toString(draws));
        sender.sendMessage(ChatColor.GRAY + "Defaults: " + ChatColor.WHITE + Integer.toString(quits));
    }

    public void describeLeaderboard(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + getName() + ChatColor.YELLOW + " Leaderboard: ");
        sender.sendMessage(ChatColor.AQUA + Integer.toString(leaderboard.size()) + ChatColor.DARK_AQUA
                + " players with at least " +  ChatColor.AQUA + Integer.toString(leaderboardGamesRequired)
                + ChatColor.DARK_AQUA + " games:");
        int position = 1;
        for (ArenaPlayer arenaPlayer : leaderboard) {
            int wins = arenaPlayer.getWins();
            int losses = arenaPlayer.getLosses();
            float ratio = arenaPlayer.getWinRatio();
            sender.sendMessage(ChatColor.LIGHT_PURPLE + Integer.toString(position) + ": " + ChatColor.WHITE
                    + ChatColor.DARK_PURPLE + arenaPlayer.getDisplayName()
                    + ChatColor.WHITE + ": " + ChatColor.GREEN + Integer.toString(wins) + "W"
                    + ChatColor.WHITE + " / " + ChatColor.RED + Integer.toString(losses) + "L"
                    + ChatColor.WHITE + " = " + ChatColor.GOLD + Integer.toString((int)(ratio * 100)) + "%");
            position++;
        }
    }

    public boolean placeLeaderboard(Block leaderboardBlock) {
        if (!DefaultMaterials.isSign(leaderboardBlock.getType())) {
            return false;
        }
        BlockFace signDirection = CompatibilityLib.getCompatibilityUtils().getSignFacing(leaderboardBlock);
        if (signDirection == null) {
            controller.getPlugin().getLogger().warning("Block at " + leaderboardBlock.getLocation() + " has no sign data");
            return false;
        }
        BlockFace rightDirection = DirectionUtils.goLeft(signDirection);
        Block checkBlock = leaderboardBlock;
        for (int y = 0; y <= leaderboardSize; y++) {
            Block neighborBlock = checkBlock.getRelative(rightDirection);
            if (!canReplace(neighborBlock)) {
                return false;
            }
            if (y != 0 && !canReplace(checkBlock)) {
                return false;
            }
            checkBlock = checkBlock.getRelative(BlockFace.UP);
        }

        removeLeaderboard();
        leaderboardLocation = leaderboardBlock.getLocation();
        leaderboardFacing = signDirection;
        updateLeaderboard();
        saveLeaderboardLocation();
        return true;
    }

    private void saveLeaderboardLocation() {
        parameters.set("leaderboard_sign_location", ConfigurationUtils.fromLocation(leaderboardLocation, center));
        parameters.set("leaderboard_sign_facing", ConfigurationUtils.fromBlockFace(leaderboardFacing));
    }

    public int getLeaderboardSize() {
        return leaderboardSize;
    }

    public void reset(Player player) {
        remove(player);
        ArenaPlayer arenaPlayer = new ArenaPlayer(this, player);
        removeFromLeaderboard(arenaPlayer);
        // Note that we don't rebuild the leaderboard here, just let that happen later.
        // You can force a rebuild with a break and re-place of the block
        arenaPlayer.reset();
    }

    public void reset() {
        leaderboard.clear();
    }

    public void showLeaderboard(Player player) {
        int inventorySize = leaderboard.size() + 1;
        int leaderboardRows = (int)Math.ceil((float)inventorySize / 9);
        leaderboardRows = Math.min(8, leaderboardRows);
        inventorySize = leaderboardRows * 9;
        boolean shownPlayer = false;
        String arenaName = ChatColor.DARK_AQUA + "Leaderboard: " + ChatColor.GOLD + getName();
        if (arenaName.length() > 32) {
            arenaName = arenaName.substring(0, 31);
        }
        Inventory leaderboardInventory = Bukkit.createInventory(null, inventorySize, arenaName);
        int leaderboardSize = Math.min(leaderboard.size(), inventorySize);
        for (int i = 0; i < leaderboardSize; i++) {
            ArenaPlayer arenaPlayer = leaderboard.get(i);
            final int slot = i;
            createLeaderboardIcon(i + 1, arenaPlayer, new ItemUpdatedCallback() {
                @Override
                public void updated(ItemStack itemStack) {
                    leaderboardInventory.setItem(slot, itemStack);
                }
            });
            if (player.getUniqueId().equals(arenaPlayer.getUUID())) {
                shownPlayer = true;
            }
        }

        if (!shownPlayer && leaderboardSize > 0) {
            ArenaPlayer arenaPlayer = new ArenaPlayer(this, player);
            createLeaderboardIcon(null, arenaPlayer, new ItemUpdatedCallback() {
                @Override
                public void updated(ItemStack itemStack) {
                    leaderboardInventory.setItem(leaderboardSize - 1, itemStack);
                }
            });
        }

        player.openInventory(leaderboardInventory);
    }

    protected void createLeaderboardIcon(Integer rank, ArenaPlayer player, ItemUpdatedCallback callback) {
        controller.getMagic().getSkull(player.getUUID(),
                ChatColor.GOLD + player.getDisplayName(),
                new ItemUpdatedCallback() {
                    @Override
                    public void updated(ItemStack itemStack) {
                        ItemMeta meta = itemStack.getItemMeta();
                        List<String> lore = new ArrayList<>();

                        if (rank != null) {
                            lore.add(ChatColor.DARK_PURPLE + "Ranked " + ChatColor.AQUA + "#" + Integer.toString(rank) + ChatColor.DARK_PURPLE + " for " + ChatColor.GOLD + getName());
                        } else {
                            lore.add(ChatColor.DARK_PURPLE + "Not ranked for " + ChatColor.GOLD + getName());
                        }

                        lore.add(ChatColor.GREEN + "Wins: " + ChatColor.WHITE + Integer.toString(player.getWins()));
                        lore.add(ChatColor.RED + "Losses: " + ChatColor.WHITE + Integer.toString(player.getLosses()));
                        lore.add(ChatColor.GOLD + "Win Ratio: " + ChatColor.WHITE + Integer.toString((int) (player.getWinRatio() * 100)) + "% " + String.format("(%.2f)", player.getWinConfidence()));
                        lore.add(ChatColor.YELLOW + "Draws: " + ChatColor.WHITE + Integer.toString(player.getDraws()));
                        lore.add(ChatColor.GRAY + "Defaults: " + ChatColor.WHITE + Integer.toString(player.getQuits()));
                        meta.setLore(lore);
                        itemStack.setItemMeta(meta);
                        callback.updated(itemStack);
                    }
                });
    }

    public void draw() {
        messageInGamePlayers(getMessage("draw"));
        announce(getAnnounceMessage("draw"));
        for (ArenaPlayer player : players) {
            player.draw();
            heal(player);
        }
        for (ArenaPlayer loser : deadPlayers) {
            loser.draw();
            heal(loser);
        }
        finish();
    }

    public void tick() {
        ArenaStage currentStage = getCurrentStage();
        if (currentStage != null) {
            currentStage.tick();
        }
        if (duration <= 0) {
            return;
        }
        long now = System.currentTimeMillis();
        long previousTime = lastTick - started;
        long currentTime = now - started;
        lastTick = now;

        if (currentTime > duration) {
            draw();
            return;
        }

        boolean hasSuddenDeath = suddenDeath > 0 && suddenDeathEffect != null && suddenDeath < duration;
        if (currentTime >= duration - 120000 && previousTime < duration - 1200000) {
            announce(getAnnounceMessage("duration_minute_2"));
        }
        if (currentTime >= duration - 60000 && previousTime < duration - 60000) {
            announce(getAnnounceMessage("duration_minute_1"));
        }
        if (currentTime >= duration - 30000 && previousTime < duration - 30000) {
            announce(getAnnounceMessage("duration_seconds_30"));
        }
        if (currentTime >= duration - 10000 && previousTime < duration - 10000) {
            announce(getAnnounceMessage("duration_seconds_10"));
        }
        if (currentTime >= duration - 5000 && previousTime < duration - 5000) {
            announce(getAnnounceMessage("duration_seconds_5"));
        }

        if (hasSuddenDeath) {
            long suddenDeathDuration = duration - suddenDeath;
            if (currentTime >= suddenDeathDuration) {
                if (previousTime < suddenDeathDuration) {
                    announce(getAnnounceMessage("sudden_death"));
                }
                for (ArenaPlayer player : players) {
                    player.getPlayer().addPotionEffect(suddenDeathEffect, true);
                }
            }
        }
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public int getQueuedPlayers() {
        return queue.size();
    }

    public int getInGamePlayers() {
        return players.size();
    }

    public Set<ArenaPlayer> getLivingParticipants() {
        return players;
    }

    public boolean isValid() {
        return center != null && center.getWorld() != null;
    }

    public boolean isKeepInventory() {
        return keepInventory;
    }

    public boolean isKeepLevel() {
        return keepLevel;
    }

    public boolean isItemWear() {
        return itemWear;
    }

    public boolean isAllowConsuming() {
        return allowConsuming;
    }

    public boolean isAllowMelee() {
        return allowMelee;
    }

    public boolean isAllowProjectiles() {
        return allowProjectiles;
    }

    public boolean isMobArena() {
        ArenaStage currentStage = getCurrentStage();
        if (currentStage == null) return false;
        return currentStage.hasMobs();
    }

    public Mage getMage() {
        return controller.getMagic().getMage("ARENA: " + getKey(), getName());
    }

    public boolean isBattling(ArenaPlayer player) {
        return players.contains(player);
    }

    public Collection<ArenaStage> getStages() {
        return stages;
    }

    public void setEditDefaultStage(boolean all) {
        this.editDefaultStage = all;
    }

    public long getLastDeathTime() {
        return lastDeath;
    }

    public void showRespawnBossBar(double progress) {
        if (respawnBar == null) {
            respawnBar = controller.getPlugin().getServer().createBossBar(ChatColor.GREEN + "Respawning", BarColor.GREEN, BarStyle.SOLID);
        }
        for (ArenaPlayer dead : deadPlayers) {
            respawnBar.addPlayer(dead.getPlayer());
        }
        respawnBar.setProgress(progress);
    }

    public void hideRespawnBossBar() {
        if (respawnBar != null) {
            respawnBar.removeAll();
        }
    }

    public boolean hasDeadPlayers() {
        return !deadPlayers.isEmpty();
    }

    public void setRequiredKills(int requiedKils) {
        this.requiredKills = requiedKils;
        parameters.set("required_kills", requiredKills);
    }

    public void setWinXP(int xp) {
        winXP = Math.max(xp, 0);
        parameters.set("win_xp", winXP);
    }

    public void setLoseXP(int xp) {
        loseXP = Math.max(xp, 0);
        parameters.set("lose_xp", loseXP);
    }

    public void setDrawXP(int xp) {
        drawXP = Math.max(xp, 0);
        parameters.set("draw_xp", drawXP);
    }

    public int getWinXP() {
        return winXP;
    }

    public int getLoseXP() {
        return loseXP;
    }

    public int getDrawXP() {
        return drawXP;
    }

    public void setWinSP(int sp) {
        winSP = Math.max(sp, 0);
        parameters.set("win_sp", winSP);
    }

    public void setLoseSP(int sp) {
        loseSP = Math.max(sp, 0);
        parameters.set("lose_sp", loseSP);
    }

    public void setDrawSP(int sp) {
        drawSP = Math.max(sp, 0);
        parameters.set("draw_sp", drawSP);
    }

    public int getWinSP() {
        return winSP;
    }

    public int getLoseSP() {
        return loseSP;
    }

    public int getDrawSP() {
        return drawSP;
    }

    public void setWinMoney(int money) {
        winMoney = Math.max(money, 0);
        parameters.set("win_money", winMoney);
    }

    public void setLoseMoney(int money) {
        loseMoney = Math.max(money, 0);
        parameters.set("lose_money", loseMoney);
    }

    public void setDrawMoney(int money) {
        drawMoney = Math.max(money, 0);
        parameters.set("draw_money", drawMoney);
    }

    public int getWinMoney() {
        return winMoney;
    }

    public int getLoseMoney() {
        return loseMoney;
    }

    public int getDrawMoney() {
        return drawMoney;
    }

    public void setMaxTeleportDistance(int distance) {
        maxTeleportDistance = distance;
        parameters.set("max_teleport_distance", maxTeleportDistance);
    }

    public int getMaxTeleportDistance() {
        return maxTeleportDistance;
    }

    public void setCountdown(int countdown) {
        this.countdown = countdown;
        parameters.set("countdown", countdown);
    }

    public void setCountdownMax(int countdownMax) {
        this.countdownMax = countdownMax;
        parameters.set("countdown_max", countdownMax);
    }

    public boolean hasOpCheck() {
        return opCheck;
    }

    public void setOpCheck(boolean check) {
        opCheck = check;
        parameters.set("op_check", opCheck);
    }

    public boolean getAllowInterrupt() {
        return allowInterrupt;
    }

    public void setHeal(boolean heal) {
        this.heal = heal;
        parameters.set("heal", heal);
    }

    public void setAllowInterrupt(boolean interrupt) {
        allowInterrupt = interrupt;
        parameters.set("allow_interrupt", allowInterrupt);
    }

    public void setKeepInventory(boolean keep) {
        keepInventory = keep;
        parameters.set("keep_inventory", keepInventory);
    }

    public void setItemWear(boolean wear) {
        itemWear = wear;
        parameters.set("item_wear", itemWear);
    }

    public void setAllowConsuming(boolean consume) {
        allowConsuming = consume;
        parameters.set("allow_consuming", allowConsuming);
    }

    public void setAllowMelee(boolean allow) {
        allowMelee = allow;
        parameters.set("allow_melee", allowMelee);
    }

    public void setAllowProjectiles(boolean allow) {
        allowProjectiles = allow;
        parameters.set("allow_projectiles", allowProjectiles);
    }

    public void setKeepLevel(boolean keep) {
        keepLevel = keep;
        parameters.set("keep_level", keepLevel);
    }

    public void setAnnouncerRange(int range) {
        this.announcerRange = range;
        parameters.set("announcer_range", announcerRange);
    }

    public int getAnnouncerRange() {
        return announcerRange;
    }

    public void setDuration(int duration) {
        this.duration = duration;
        parameters.set("duration", duration);
    }

    public void setSuddenDeath(int suddenDeath) {
        this.suddenDeath = suddenDeath;
        parameters.set("sudden_death", suddenDeath);
    }

    public void setStartCommands(String commands) {
        startCommands = commands;
        parameters.set("start_commands", startCommands);
    }

    public void setEndCommands(String commands) {
        endCommands = commands;
        parameters.set("end_commands", endCommands);
    }

    public void setBorder(int min, int max) {
        borderMin = min;
        borderMax = max;
        parameters.set("border_min", borderMin);
        parameters.set("border_max", borderMax);
    }

    public void setTemplate(ArenaTemplate template) {
        this.template = template;
        reload();
    }

    public void setLeaderboardSignType(Material material) {
        this.signMaterial = material;
        parameters.set("leaderboard_sign_type", signMaterial.name().toLowerCase());
    }

    public void setMinPlayers(int players) {
        minPlayers = Math.max(1, players);
        parameters.set("min_players", minPlayers);
    }

    public void setMaxPlayers(int players) {
        maxPlayers = players;
        parameters.set("max_players", maxPlayers);
    }

    public void setRandomizeSpawn(Vector vector) {
        randomizeSpawn = vector;
        parameters.set("randomize.spawn", ConfigurationUtils.fromVector(randomizeSpawn));
    }

    public String getDescription() {
        return description == null ? "" : description;
    }

    public String getName() {
        return name == null ? key : name;
    }

    public String getKey() {
        return key;
    }

    public void setName(String name) {
        this.name = name;
        parameters.set("name", name);
    }

    public void setDescription(String description) {
        this.description = description;
        parameters.set("description", description);
    }

    public void setPortalEnterDamage(int damage) {
        this.portalEnterDamage = damage;
        parameters.set("portal_enter_damage", portalEnterDamage);
    }

    public int getPortalEnterDamage() {
        return portalEnterDamage;
    }

    public void setPortalDamage(int damage) {
        this.portalDamage = damage;
        parameters.set("portal_damage", portalDamage);
    }

    public int getPortalDamage() {
        return portalDamage;
    }

    public String getPortalDeathMessage() {
        return portalDeathMessage;
    }

    public void setPortalDeathMessage(String message) {
        this.portalDeathMessage = message;
        parameters.set("portal_death_message", portalDeathMessage);
    }

    public void setCenter(Location location) {
        center = location.clone();
        // These are all saved relative to the center so we need to update them all
        if (parameters.contains("exit")) setExit(exit);
        if (parameters.contains("lobby")) setLobby(lobby);
        if (parameters.contains("win")) setWinLocation(win);
        if (parameters.contains("lose")) setLoseLocation(lose);
        if (parameters.contains("spawns")) saveSpawns();
        if (parameters.contains("leaderboard_sign_location")) saveLeaderboardLocation();
    }

    public void setExit(Location location) {
        exit = location == null ? null : location.clone();
        parameters.set("exit", ConfigurationUtils.fromLocation(exit, center));
    }

    public void setLobby(Location location) {
        lobby = location == null ? null : location.clone();
        parameters.set("lobby", ConfigurationUtils.fromLocation(lobby, center));
    }

    public void setWinLocation(Location location) {
        win = location == null ? null : location.clone();
        parameters.set("win", ConfigurationUtils.fromLocation(win, center));
    }

    public void setLoseLocation(Location location) {
        lose = location == null ? null : location.clone();
        parameters.set("lose", ConfigurationUtils.fromLocation(lose, center));
    }

    public void addSpawn(Location location) {
        spawns.add(location.clone());
        saveSpawns();
    }

    public void setSpawn(Location location) {
        spawns.clear();
        if (location != null) {
            addSpawn(location);
        }
        saveSpawns();
    }

    public Location removeSpawn(Location location) {
        int rangeSquared = 3 * 3;
        for (Location spawn : spawns) {
            if (spawn.distanceSquared(location) < rangeSquared) {
                spawns.remove(spawn);
                saveSpawns();
                return spawn;
            }
        }

        return null;
    }

    public List<Location> getSpawns() {
        if (spawns.size() == 0) {
            List<Location> centerList = new ArrayList<>();
            centerList.add(center);
            return centerList;
        }

        return spawns;
    }

    public void addMagicBlock(String magicBlock) {
        if (magicBlocks == null) {
            magicBlocks = new ArrayList<>();
        }
        magicBlocks.add(magicBlock);
        parameters.set("magic_blocks", magicBlocks);
    }

    public boolean removeMagicBlock(String magicBlock) {
        if (magicBlocks == null) {
            return false;
        }
        boolean removed = magicBlocks.remove(magicBlock);
        if (removed) {
            parameters.set("magic_blocks", magicBlocks);
        }
        return removed;
    }
}
