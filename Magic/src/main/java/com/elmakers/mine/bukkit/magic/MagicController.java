package com.elmakers.mine.bukkit.magic;

import com.elmakers.mine.bukkit.api.block.BoundingBox;
import com.elmakers.mine.bukkit.api.block.CurrencyItem;
import com.elmakers.mine.bukkit.api.block.Schematic;
import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.data.MageData;
import com.elmakers.mine.bukkit.api.data.MageDataCallback;
import com.elmakers.mine.bukkit.api.data.MageDataStore;
import com.elmakers.mine.bukkit.api.data.SpellData;
import com.elmakers.mine.bukkit.api.entity.EntityData;
import com.elmakers.mine.bukkit.api.event.LoadEvent;
import com.elmakers.mine.bukkit.api.event.SaveEvent;
import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.magic.CastSourceLocation;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.spell.CastingCost;
import com.elmakers.mine.bukkit.api.spell.CostReducer;
import com.elmakers.mine.bukkit.api.spell.MageSpell;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellKey;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.WandTemplate;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.block.MaterialBrush;
import com.elmakers.mine.bukkit.block.NegatedHashSet;
import com.elmakers.mine.bukkit.block.WildcardHashSet;
import com.elmakers.mine.bukkit.citizens.CitizensController;
import com.elmakers.mine.bukkit.dynmap.DynmapController;
import com.elmakers.mine.bukkit.effect.EffectPlayer;
import com.elmakers.mine.bukkit.elementals.ElementalsController;
import com.elmakers.mine.bukkit.essentials.MagicItemDb;
import com.elmakers.mine.bukkit.essentials.Mailer;
import com.elmakers.mine.bukkit.heroes.HeroesManager;
import com.elmakers.mine.bukkit.integration.BlockPhysicsManager;
import com.elmakers.mine.bukkit.integration.LibsDisguiseManager;
import com.elmakers.mine.bukkit.integration.MobArenaManager;
import com.elmakers.mine.bukkit.integration.SkillAPIManager;
import com.elmakers.mine.bukkit.integration.VaultController;
import com.elmakers.mine.bukkit.magic.command.MagicTabExecutor;
import com.elmakers.mine.bukkit.magic.listener.AnvilController;
import com.elmakers.mine.bukkit.magic.listener.BlockController;
import com.elmakers.mine.bukkit.magic.listener.CraftingController;
import com.elmakers.mine.bukkit.magic.listener.EnchantingController;
import com.elmakers.mine.bukkit.magic.listener.EntityController;
import com.elmakers.mine.bukkit.magic.listener.ExplosionController;
import com.elmakers.mine.bukkit.magic.listener.HangingController;
import com.elmakers.mine.bukkit.magic.listener.InventoryController;
import com.elmakers.mine.bukkit.magic.listener.ItemController;
import com.elmakers.mine.bukkit.magic.listener.LoadSchematicTask;
import com.elmakers.mine.bukkit.magic.listener.MinigamesListener;
import com.elmakers.mine.bukkit.magic.listener.MobController;
import com.elmakers.mine.bukkit.magic.listener.PlayerController;
import com.elmakers.mine.bukkit.maps.MapController;
import com.elmakers.mine.bukkit.protection.BlockBreakManager;
import com.elmakers.mine.bukkit.protection.BlockBuildManager;
import com.elmakers.mine.bukkit.protection.FactionsManager;
import com.elmakers.mine.bukkit.protection.GriefPreventionManager;
import com.elmakers.mine.bukkit.protection.LocketteManager;
import com.elmakers.mine.bukkit.protection.MultiverseManager;
import com.elmakers.mine.bukkit.protection.NCPManager;
import com.elmakers.mine.bukkit.protection.PVPManager;
import com.elmakers.mine.bukkit.protection.PreciousStonesManager;
import com.elmakers.mine.bukkit.protection.PvPManagerManager;
import com.elmakers.mine.bukkit.protection.TownyManager;
import com.elmakers.mine.bukkit.protection.WorldGuardManager;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.spell.SpellCategory;
import com.elmakers.mine.bukkit.data.YamlDataFile;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.HitboxUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import com.elmakers.mine.bukkit.utility.Messages;
import com.elmakers.mine.bukkit.utility.SafetyUtils;
import com.elmakers.mine.bukkit.wand.LostWand;
import com.elmakers.mine.bukkit.wand.Wand;
import com.elmakers.mine.bukkit.wand.WandManaMode;
import com.elmakers.mine.bukkit.wand.WandMode;
import com.elmakers.mine.bukkit.wand.WandUpgradePath;
import com.elmakers.mine.bukkit.warp.WarpController;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import com.google.common.io.BaseEncoding;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;
import org.bstats.Metrics;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.CodeSource;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.annotation.Nonnull;

public class MagicController implements MageController {
    public MagicController(final MagicPlugin plugin) {
        this.plugin = plugin;

        configFolder = plugin.getDataFolder();
        configFolder.mkdirs();

        dataFolder = new File(configFolder, "data");
        dataFolder.mkdirs();

        defaultsFolder = new File(configFolder, "defaults");
        defaultsFolder.mkdirs();
    }

    @Override
    public com.elmakers.mine.bukkit.magic.Mage getRegisteredMage(String mageId) {
        if (!loaded) {
            return null;
        }
        return mages.get(mageId);
    }

    @Override
    public com.elmakers.mine.bukkit.magic.Mage getAutomaton(String mageId, String mageName) {
        com.elmakers.mine.bukkit.magic.Mage mage = getMage(mageId, mageName, null, null);
        mage.setIsAutomaton(true);
        return mage;
    }

    @Override
    public com.elmakers.mine.bukkit.magic.Mage getMage(String mageId, String mageName) {
        return getMage(mageId, mageName, null, null);
    }

    public com.elmakers.mine.bukkit.magic.Mage getMage(String mageId, CommandSender commandSender, Entity entity) {
        return getMage(mageId, null, commandSender, entity);
    }

    protected com.elmakers.mine.bukkit.magic.Mage getMage(String mageId, String mageName, CommandSender commandSender, Entity entity) {
        Preconditions.checkNotNull(mageId);

        com.elmakers.mine.bukkit.magic.Mage apiMage = null;
        if (commandSender == null && entity == null) {
            commandSender = Bukkit.getConsoleSender();
        }
        if (!loaded) {
            if (entity instanceof Player) {
                getLogger().warning("Player data request for " + mageId + " (" + ((Player)commandSender).getName() + ") failed, plugin not loaded yet");
            }
            return null;
        }

        if (!mages.containsKey(mageId)) {
            if (entity instanceof Player && !((Player)entity).isOnline() && !isNPC(entity))
            {
                getLogger().warning("Player data for " + mageId + " (" + entity.getName() + ") loaded while offline!");
                Thread.dumpStack();
            }

            final com.elmakers.mine.bukkit.magic.Mage mage = new com.elmakers.mine.bukkit.magic.Mage(mageId, this);

            mages.put(mageId, mage);
            mage.setName(mageName);
            mage.setCommandSender(commandSender);
            mage.setEntity(entity);
            if (entity instanceof Player) {
                mage.setPlayer((Player) entity);
            }

            // Check for existing data file
            // For now we only do async loads for Players
            boolean isPlayer = (entity instanceof Player);
            isPlayer = (isPlayer && !isNPC(entity));
            if (savePlayerData && mageDataStore != null) {
                if (asynchronousSaving && isPlayer) {
                    mage.setLoading(true);
                    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                        @Override
                        public void run() {
                            synchronized (saveLock) {
                                info("Loading mage data for " + mage.getName() + " (" + mage.getId() + ")");
                                try {
                                    mageDataStore.load(mage.getId(), new MageDataCallback() {
                                        @Override
                                        public void run(MageData data) {
                                        mage.load(data);
                                        info(" Finished Loading mage data for " + mage.getName() + " (" + mage.getId() + ")");
                                        }
                                    });
                                } catch (Exception ex) {
                                    getLogger().warning("Failed to load mage data for " + mage.getName() + " (" + mage.getId() + ")");
                                    ex.printStackTrace();
                                }
                            }
                        }
                    });
                } else if (saveNonPlayerMages) {
                    info("Loading mage data for " + mage.getName() + " (" + mage.getId() + ") synchronously");
                    synchronized (saveLock) {
                        try {
                            mageDataStore.load(mage.getId(), new MageDataCallback() {
                                @Override
                                public void run(MageData data) {
                                mage.load(data);
                                }
                            });
                        } catch (Exception ex) {
                            getLogger().warning("Failed to load mage data for " + mage.getName() + " (" + mage.getId() + ")");
                            ex.printStackTrace();
                        }
                    }
                } else {
                    mage.load(null);
                }
            } else if (externalPlayerData && (isPlayer || saveNonPlayerMages)) {
                mage.setLoading(true);
            } else {
                mage.load(null);
            }

            apiMage = mage;
        } else {
            apiMage = mages.get(mageId);
            com.elmakers.mine.bukkit.magic.Mage mage = apiMage;

            // In case of rapid relog, this mage may have been marked for removal already
            mage.setUnloading(false);

            // Re-set mage properties
            mage.setName(mageName);
            mage.setCommandSender(commandSender);
            mage.setEntity(entity);
            if (entity instanceof Player) {
                mage.setPlayer((Player) entity);
            }
        }
        if (apiMage == null) {
            getLogger().warning("getMage returning null mage for " + entity + " and " + commandSender);
            Thread.dumpStack();
        }
        return apiMage;
    }

    public void info(String message)
    {
        info(message, 1);
    }

    public void info(String message, int verbosity)
    {
        if (logVerbosity >= verbosity)
        {
            getLogger().info(message);
        }
    }

    @Override
    public com.elmakers.mine.bukkit.magic.Mage getMage(Player player) {
        return getMage(player, player);
    }

    @Override
    public com.elmakers.mine.bukkit.magic.Mage getMage(Entity entity) {
        CommandSender commandSender = (entity instanceof Player) ? (Player) entity : null;
        return getMage(entity, commandSender);
    }

    @Override
    public com.elmakers.mine.bukkit.magic.Mage getRegisteredMage(Entity entity) {
        if (entity == null) return null;
        String id = mageIdentifier.fromEntity(entity);
        return mages.get(id);
    }

    protected com.elmakers.mine.bukkit.magic.Mage getMage(Entity entity, CommandSender commandSender) {
        if (entity == null) return getMage(commandSender);
        String id = mageIdentifier.fromEntity(entity);
        return getMage(id, commandSender, entity);
    }

    @Override
    public com.elmakers.mine.bukkit.magic.Mage getMage(CommandSender commandSender) {
        if (commandSender instanceof Player) {
            return getMage((Player) commandSender);
        }

        String mageId = mageIdentifier.fromCommandSender(commandSender);
        return getMage(mageId, commandSender, null);
    }

    public void addSpell(Spell variant) {
        SpellTemplate conflict = spells.get(variant.getKey());
        if (conflict != null) {
            getLogger().log(Level.WARNING, "Duplicate spell key: '" + conflict.getKey() + "'");
        } else {
            spells.put(variant.getKey(), variant);
            SpellData data = templateDataMap.get(variant.getSpellKey().getBaseKey());
            if (data == null) {
                data = new SpellData(variant.getSpellKey().getBaseKey());
                templateDataMap.put(variant.getSpellKey().getBaseKey(), data);
            }
            if (variant instanceof MageSpell) {
                ((MageSpell) variant).setSpellData(data);
            }
            String alias = variant.getAlias();
            if (alias != null && alias.length() > 0) {
                spellAliases.put(alias, variant);
            }
        }
    }

    public float getMaxDamagePowerMultiplier() {
        return maxDamagePowerMultiplier;
    }

    public float getMaxConstructionPowerMultiplier() {
        return maxConstructionPowerMultiplier;
    }

    public float getMaxRadiusPowerMultiplier() {
        return maxRadiusPowerMultiplier;
    }

    public float getMaxRadiusPowerMultiplierMax() {
        return maxRadiusPowerMultiplierMax;
    }

    public float getMaxRangePowerMultiplier() {
        return maxRangePowerMultiplier;
    }

    public float getMaxRangePowerMultiplierMax() {
        return maxRangePowerMultiplierMax;
    }

    public int getAutoUndoInterval() {
        return autoUndo;
    }

    public float getMaxPower() {
        return maxPower;
    }

    public float getMaxDamageReduction() {
        return maxDamageReduction;
    }

    public float getMaxDamageReductionExplosions() {
        return maxDamageReductionExplosions;
    }

    public float getMaxDamageReductionFalling() {
        return maxDamageReductionFalling;
    }

    public float getMaxDamageReductionFire() {
        return maxDamageReductionFire;
    }

    public float getMaxDamageReductionPhysical() {
        return maxDamageReductionPhysical;
    }

    public float getMaxDamageReductionProjectiles() {
        return maxDamageReductionProjectiles;
    }

    public float getMaxCostReduction() {
        return maxCostReduction;
    }

    public float getMaxCooldownReduction() {
        return maxCooldownReduction;
    }

    public int getMaxMana() {
        return maxMana;
    }

    public int getMaxManaRegeneration() {
        return maxManaRegeneration;
    }

    @Override
    public double getWorthBase() {
        return worthBase;
    }

    @Override
    public double getWorthXP() {
        return worthXP;
    }

    @Override
    public double getWorthSkillPoints() {
        return worthSkillPoints;
    }

    @Override
    public ItemStack getWorthItem() {
        return currencyItem == null ? null : currencyItem.getItem();
    }

    @Override
    public double getWorthItemAmount() {
        //TODO: Use a precondition here instead of the NPE of (double) null
        return currencyItem == null ? null : currencyItem.getWorth();
    }

    @Override
    public CurrencyItem getCurrency() {
        return currencyItem;
    }
	
	/*
	 * Undo system
	 */

    public int getUndoQueueDepth() {
        return undoQueueDepth;
    }

    public int getPendingQueueDepth() {
        return pendingQueueDepth;
    }

	/*
	 * Random utility functions
	 */

    @Override
    public String getMessagePrefix() {
        return messagePrefix;
    }

    public String getCastMessagePrefix() {
        return castMessagePrefix;
    }

    public boolean showCastMessages() {
        return showCastMessages;
    }

    public boolean showMessages() {
        return showMessages;
    }

    @Override
    public boolean soundsEnabled() {
        return soundsEnabled;
    }

    public boolean fillWands() {
        return fillingEnabled;
    }

    @Override
    public int getMaxWandFillLevel() {
        return maxFillLevel;
    }

    /*
	 * Get the log, if you need to debug or log errors.
	 */
    @Override
    public Logger getLogger() {
        return plugin.getLogger();
    }

    public boolean isIndestructible(Location location) {
        return isIndestructible(location.getBlock());
    }

    public boolean isIndestructible(Block block) {
        return indestructibleMaterials.contains(block.getType());
    }

    public boolean isDestructible(Block block) {
        return destructibleMaterials.contains(block.getType());
    }

    protected boolean isRestricted(Material material) {
        return restrictedMaterials.contains(material);
    }

    public boolean hasBuildPermission(Player player, Location location) {
        return hasBuildPermission(player, location.getBlock());
    }

    public boolean hasBuildPermission(Player player, Block block) {
        // Check all protection plugins
        if (bypassBuildPermissions) return true;
        if (player != null && player.hasPermission("Magic.bypass_build")) return true;

        boolean allowed = true;
        for (BlockBuildManager manager : blockBuildManagers) {
            if (!manager.hasBuildPermission(player, block)) {
                allowed = false;
                break;
            }
        }
        return allowed;
    }

    public boolean hasBreakPermission(Player player, Block block) {
        // This is the same has hasBuildPermission for everything but Towny!
        if (bypassBreakPermissions) return true;
        if (player != null && player.hasPermission("Magic.bypass_break")) return true;

        boolean allowed = true;
        for (BlockBreakManager manager : blockBreakManagers) {
            if (!manager.hasBreakPermission(player, block)) {
                allowed = false;
                break;
            }
        }

        return allowed;
    }

    @Override
    public boolean isExitAllowed(Player player, Location location) {
        if (location == null) return true;
        return worldGuardManager.isExitAllowed(player, location);
    }

    @Override
    public boolean isPVPAllowed(Player player, Location location)
    {
        if (location == null) return true;
        if (bypassPvpPermissions) return true;
        if (player != null && player.hasPermission("Magic.bypass_pvp")) return true;

        boolean allowed = true;
        for (PVPManager manager : pvpManagers) {
            if (!manager.isPVPAllowed(player, location)) {
                allowed = false;
                break;
            }
        }
        return allowed;
    }

    public void clearCache() {
        schematics.clear();
        for (Mage mage : mages.values()) {
            if (mage instanceof com.elmakers.mine.bukkit.magic.Mage) {
                ((com.elmakers.mine.bukkit.magic.Mage) mage).clearCache();
            }
        }

        maps.clearCache();
        maps.resetAll();
    }

    @Override
    public Schematic loadSchematic(String schematicName) {
        if (schematicName == null || schematicName.length() == 0) return null;

        if (schematics.containsKey(schematicName)) {
            WeakReference<Schematic> schematic = schematics.get(schematicName);
            if (schematic != null) {
                Schematic cached = schematic.get();
                if (cached != null) {
                    return cached;
                }
            }
        }

        InputStream inputSchematic = null;
        try {
            // Check extra path first
            File extraSchematicFile = null;
            File magicSchematicFolder = new File(plugin.getDataFolder(), "schematics");
            if (magicSchematicFolder.exists()) {
                extraSchematicFile = new File(magicSchematicFolder, schematicName + ".schematic");
                info("Checking for schematic: " + extraSchematicFile.getAbsolutePath(), 2);
                if (!extraSchematicFile.exists()) {
                    extraSchematicFile = null;
                }
            }
            if (extraSchematicFile == null && extraSchematicFilePath != null && extraSchematicFilePath.length() > 0) {
                File schematicFolder = new File(configFolder, "../" + extraSchematicFilePath);
                if (schematicFolder.exists()) {
                    extraSchematicFile = new File(schematicFolder, schematicName + ".schematic");
                    info("Checking for external schematic: " + extraSchematicFile.getAbsolutePath(), 2);
                }
            }

            if (extraSchematicFile != null && extraSchematicFile.exists()) {
                inputSchematic = new FileInputStream(extraSchematicFile);
                info("Loading file: " + extraSchematicFile.getAbsolutePath());
            } else {
                String fileName = schematicName + ".schematic";
                inputSchematic = plugin.getResource("schematics/" + fileName);
                info("Loading builtin schematic: " + fileName);
            }
        } catch (Exception ex) {

        }

        if (inputSchematic == null) {
            getLogger().warning("Could not load schematic: " + schematicName);
            return null;
        }

        com.elmakers.mine.bukkit.block.Schematic schematic = new com.elmakers.mine.bukkit.block.Schematic();
        schematics.put(schematicName, new WeakReference<Schematic>(schematic));
        Thread loadThread = new Thread(new LoadSchematicTask(this, inputSchematic, schematic));
        loadThread.start();

        return schematic;
    }

    @Override
    public Collection<String> getBrushKeys() {
        List<String> names = new ArrayList<>();
        Material[] materials = Material.values();
        for (Material material : materials) {
            // Only show blocks
            if (material.isBlock()) {
                names.add(material.name().toLowerCase());
            }
        }

        // Add special materials
        for (String brushName : MaterialBrush.SPECIAL_MATERIAL_KEYS) {
            names.add(brushName.toLowerCase());
        }

        // Add schematics
        Collection<String> schematics = getSchematicNames();
        for (String schematic : schematics) {
            names.add("schematic:" + schematic);
        }

        return names;
    }

    public Collection<String> getSchematicNames() {
        Collection<String> schematicNames = new ArrayList<>();

        // Load internal schematics.. this may be a bit expensive.
        try {
            CodeSource codeSource = MagicTabExecutor.class.getProtectionDomain().getCodeSource();
            if (codeSource != null) {
                URL jar = codeSource.getLocation();
                try(ZipInputStream zip = new ZipInputStream(jar.openStream())) {
                    ZipEntry entry = zip.getNextEntry();
                    while (entry != null) {
                        String name = entry.getName();
                        if (name.startsWith("schematics/") && name.endsWith(".schematic")) {
                            String schematicName = name.replace(".schematic", "").replace("schematics/", "");
                            schematicNames.add(schematicName);
                        }
                        entry = zip.getNextEntry();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Load external schematics
        try {
            // Check extra path first
            if (extraSchematicFilePath != null && extraSchematicFilePath.length() > 0) {
                File schematicFolder = new File(configFolder, "../" + extraSchematicFilePath);
                for (File schematicFile : schematicFolder.listFiles()) {
                    if (schematicFile.getName().endsWith(".schematic")) {
                        String schematicName = schematicFile.getName().replace(".schematic", "");
                        schematicNames.add(schematicName);
                    }
                }
            }
        } catch (Exception ex) {

        }

        return schematicNames;
    }
	
	/*
	 * Internal functions - don't call these, or really anything below here.
	 */

    /*
	 * Saving and loading
	 */
    public void initialize() {
        warpController = new WarpController();
        crafting = new CraftingController(this);
        mobs = new MobController(this);
        items = new ItemController(this);
        enchanting = new EnchantingController(this);
        anvil = new AnvilController(this);
        blockController = new BlockController(this);
        hangingController = new HangingController(this);
        entityController = new EntityController(this);
        playerController = new PlayerController(this);
        inventoryController = new InventoryController(this);
        explosionController = new ExplosionController(this);
        messages = new Messages();

        File urlMapFile = getDataFile(URL_MAPS_FILE);
        File imageCache = new File(dataFolder, "imagemapcache");
        imageCache.mkdirs();
        maps = new MapController(plugin, urlMapFile, imageCache);

        // Initialize EffectLib.
        if (EffectPlayer.initialize(plugin)) {
            getLogger().info("EffectLib initialized");
        } else {
            getLogger().warning("Failed to initialize EffectLib");
        }

        // Pre-create schematic folder
        File magicSchematicFolder = new File(plugin.getDataFolder(), "schematics");
        magicSchematicFolder.mkdirs();

        // One-time migration of enchanting.yml
        File legacyPathConfig = new File(configFolder, ENCHANTING_FILE + ".yml");
        File pathConfig = new File(configFolder, PATHS_FILE + ".yml");

        if (!pathConfig.exists() && legacyPathConfig.exists()) {
            getLogger().info("Renaming enchanting.yml to paths.yml, please update paths.yml from now on");
            legacyPathConfig.renameTo(pathConfig);
        }
        load();
        if (checkResourcePack(Bukkit.getConsoleSender(), false) && resourcePackCheckInterval > 0 && enableResourcePackCheck) {
            int intervalTicks = resourcePackCheckInterval * 60 * 20;
            resourcePackCheckTimer = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new RPCheckTask(this), intervalTicks, intervalTicks);
        }
    }

    protected void cancelResourcePackChecks() {
        if (resourcePackCheckTimer != 0) {
            Bukkit.getScheduler().cancelTask(resourcePackCheckTimer);
            resourcePackCheckTimer = 0;
        }
    }

    protected void finalizeIntegration() {
        final PluginManager pluginManager = plugin.getServer().getPluginManager();

        // Check for BlockPhysics
        if (useBlockPhysics) {
            Plugin blockPhysicsPlugin = pluginManager.getPlugin("BlockPhysics");
            if (blockPhysicsPlugin != null) {
                blockPhysicsManager = new BlockPhysicsManager(plugin, blockPhysicsPlugin);
                if (blockPhysicsManager.isEnabled()) {
                    getLogger().info("Integrated with BlockPhysics, some spells will now use physics-based block effects");
                } else {
                    getLogger().warning("Error integrating with BlockPhysics, you may want to set 'enable_block_physics: false' in config.yml");
                }
            }
        }

        // Check for Minigames
        Plugin minigamesPlugin = pluginManager.getPlugin("Minigames");
        if (minigamesPlugin != null) {
            pluginManager.registerEvents(new MinigamesListener(this), plugin);
            getLogger().info("Integrated with Minigames plugin, wands will deactivate before joining a minigame");
        }

        // Check for LibsDisguise
        Plugin libsDisguisePlugin = pluginManager.getPlugin("LibsDisguises");
        if (libsDisguisePlugin == null) {
            getLogger().info("LibsDisguises not found");
        } else if (libsDisguiseEnabled) {
            libsDisguiseManager = new LibsDisguiseManager(plugin, libsDisguisePlugin);
            if (libsDisguiseManager.initialize()) {
                getLogger().info("Integrated with LibsDisguises, mob disguises and disguise_restricted features enabled");
            } else {
                getLogger().warning("LibsDisguises integration failed");
            }
        } else {
            getLogger().info("LibsDisguises integration disabled");
        }

        // Check for SkillAPI
        Plugin skillAPIPlugin = pluginManager.getPlugin("SkillAPI");
        if (skillAPIPlugin != null && skillAPIEnabled) {
            skillAPIManager = new SkillAPIManager(plugin, skillAPIPlugin);
            if (skillAPIManager.initialize()) {
                BaseSpell.initializeAttributes(skillAPIManager.getAttributeKeys());
                getLogger().info("Integrated with SkillAPI, attributes can be used in spell parameters." );
                getLogger().info("Attributes must prefixed with a space and an underscore e.g.");
                getLogger().info("   damage: 1 + 3 * _intelligence");
                if (useSkillAPIMana) {
                    getLogger().info("SkillAPI mana will be used by spells and wands");
                }
            } else {
                getLogger().warning("SkillAPI integration failed");
            }
        } else {
            getLogger().info("SkillAPI integration disabled");
        }

        // Check for MobArena
        Plugin mobArenaPlugin = pluginManager.getPlugin("MobArena");
        if (mobArenaPlugin == null) {
            getLogger().info("MobArena not found");
        } else {
            try {
                new MobArenaManager(this);
                // getLogger().info("Integrated with MobArena, use \"magic:<itemkey>\" in arena configs for Magic items, magic mobs can be used in monster configurations");
                getLogger().info("Integrated with MobArena, magic mobs can be used in monster configurations");
            } catch (Throwable ex) {
                getLogger().warning("MobArena integration failed, you may need to update the MobArena plugin to use Magic items");
            }
        }

        // Vault integration is handled internally in MagicLib
        Plugin vaultPlugin = pluginManager.getPlugin("Vault");
        if (vaultPlugin == null) {
            getLogger().info("Vault not found, virtual economy unavailable");
        } else {
            if (VaultController.initialize(plugin, vaultPlugin)) {
                getLogger().info("Integrated with Vault, virtual economy and descriptive item names available");
            } else {
                getLogger().warning("Vault integration failed");
            }
        }

        // Try to link to Essentials:
        Plugin essentials = pluginManager.getPlugin("Essentials");
        hasEssentials = essentials != null;
        if (hasEssentials) {
            if (warpController.setEssentials(essentials)) {
                getLogger().info("Integrating with Essentials for Recall warps");
            }
            try {
                mailer = new Mailer(essentials);
            } catch (Exception ex) {
                getLogger().warning("Essentials found, but failed to hook up to Mailer");
                mailer = null;
            }
        }

        if (essentialsSignsEnabled) {
            final MagicController me = this;
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    try {
                        Object essentials = me.plugin.getServer().getPluginManager().getPlugin("Essentials");
                        if (essentials != null) {
                            Class<?> essentialsClass = essentials.getClass();
                            Field itemDbField = essentialsClass.getDeclaredField("itemDb");
                            itemDbField.setAccessible(true);
                            Object oldEntry = itemDbField.get(essentials);
                            if (oldEntry == null) {
                                getLogger().info("Essentials integration failure");
                                return;
                            }
                            if (oldEntry instanceof MagicItemDb) {
                                getLogger().info("Essentials integration already set up, skipping");
                                return;
                            }
                            if (!oldEntry.getClass().getName().equals("com.earth2me.essentials.ItemDb")) {
                                getLogger().info("Essentials Item DB class unexepcted: " + oldEntry.getClass().getName() + ", skipping integration");
                                return;
                            }
                            Object newEntry = new MagicItemDb(me, essentials);
                            itemDbField.set(essentials, newEntry);
                            Field confListField = essentialsClass.getDeclaredField("confList");
                            confListField.setAccessible(true);
                            @SuppressWarnings("unchecked")
                            List<Object> confList = (List<Object>) confListField.get(essentials);
                            confList.remove(oldEntry);
                            confList.add(newEntry);
                            getLogger().info("Essentials found, hooked up custom item handler");
                        }
                    } catch (Throwable ex) {
                        ex.printStackTrace();
                    }
                }
            }, 5);
        }

        // Try to link to CommandBook
        hasCommandBook = false;
        try {
            Plugin commandBookPlugin = plugin.getServer().getPluginManager().getPlugin("CommandBook");
            if (commandBookPlugin != null) {
                if (warpController.setCommandBook(commandBookPlugin)) {
                    getLogger().info("CommandBook found, integrating for Recall warps");
                    hasCommandBook = true;
                } else {
                    getLogger().warning("CommandBook integration failed");
                }
            }
        } catch (Throwable ex) {

        }

        // Link to factions
        factionsManager.initialize(plugin);

        // Try to (dynamically) link to WorldGuard:
        worldGuardManager.initialize(plugin);

        // Link to PvpManager
        pvpManager.initialize(plugin);

        // Link to Multiverse
        multiverseManager.initialize(plugin);
        
        // Link to PreciousStones
        preciousStonesManager.initialize(plugin);
        
        // Link to Towny
        townyManager.initialize(plugin);

        // Link to Lockette
        locketteManager.initialize(plugin);

        // Link to GriefPrevention
        griefPreventionManager.initialize(plugin);

        // Link to NoCheatPlus
        ncpManager.initialize(plugin);

        // Try to link to Heroes:
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    Plugin heroesPlugin = plugin.getServer().getPluginManager().getPlugin("Heroes");
                    if (heroesPlugin != null) {
                        heroesManager = new HeroesManager(plugin, heroesPlugin);
                    } else {
                        heroesManager = null;
                    }
                } catch (Throwable ex) {
                    plugin.getLogger().warning(ex.getMessage());
                }
            }
        }, 2);

        // Try to link to dynmap:
        try {
            Plugin dynmapPlugin = plugin.getServer().getPluginManager().getPlugin("dynmap");
            if (dynmapPlugin != null) {
                dynmap = new DynmapController(plugin, dynmapPlugin);
            } else {
                dynmap = null;
            }
        } catch (Throwable ex) {
            plugin.getLogger().warning(ex.getMessage());
        }

        if (dynmap == null) {
            getLogger().info("dynmap not found, not integrating.");
        } else {
            getLogger().info("dynmap found, integrating.");
        }

        // Try to link to Elementals:
        try {
            Plugin elementalsPlugin = plugin.getServer().getPluginManager().getPlugin("Splateds_Elementals");
            if (elementalsPlugin != null) {
                elementals = new ElementalsController(elementalsPlugin);
            } else {
                elementals = null;
            }
        } catch (Throwable ex) {
            plugin.getLogger().warning(ex.getMessage());
        }

        if (elementals != null) {
            getLogger().info("Elementals found, integrating.");
        }

        // Try to link to Citizens
        if (citizensEnabled) {
            try {
                Plugin citizensPlugin = plugin.getServer().getPluginManager().getPlugin("Citizens");
                if (citizensPlugin != null) {
                    citizens = new CitizensController(citizensPlugin);
                } else {
                    citizens = null;
                    getLogger().info("Citizens not found, Magic trait unavailable.");
                }
            } catch (Throwable ex) {
                citizens = null;
                getLogger().warning("Error integrating with Citizens");
                plugin.getLogger().warning(ex.getMessage());
            }
        } else {
            citizens = null;
            getLogger().info("Citizens integration disabled.");
        }

        // Activate Metrics
        activateMetrics();

        // Set up the PlayerSpells timer
        final MageUpdateTask mageTask = new MageUpdateTask(this);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, mageTask, 0, workFrequency);

        // Set up the Block update timer
        final BatchUpdateTask blockTask = new BatchUpdateTask(this);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, blockTask, 0, workFrequency);

        // Set up the Update check timer
        final UndoUpdateTask undoTask = new UndoUpdateTask(this);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, undoTask, 0, undoFrequency);
        registerListeners();

        // Activate/load any active player Mages
        Collection<? extends Player> allPlayers = plugin.getServer().getOnlinePlayers();
        for (Player player : allPlayers) {
            getMage(player);
        }

        // Register crafting recipes
        crafting.register(plugin);

        // Set up Break/Build/PVP Managers
        blockBreakManagers.clear();
        blockBuildManagers.clear();
        pvpManagers.clear();

        // PVP Managers
        if (worldGuardManager.isEnabled()) pvpManagers.add(worldGuardManager);
        if (pvpManager.isEnabled()) pvpManagers.add(pvpManager);
        if (multiverseManager.isEnabled()) pvpManagers.add(multiverseManager);
        if (preciousStonesManager.isEnabled()) pvpManagers.add(preciousStonesManager);
        if (townyManager.isEnabled()) pvpManagers.add(townyManager);
        if (griefPreventionManager.isEnabled()) pvpManagers.add(griefPreventionManager);

        // Build Managers
        if (worldGuardManager.isEnabled()) blockBuildManagers.add(worldGuardManager);
        if (factionsManager.isEnabled()) blockBuildManagers.add(factionsManager);
        if (locketteManager.isEnabled()) blockBuildManagers.add(locketteManager);
        if (preciousStonesManager.isEnabled()) blockBuildManagers.add(preciousStonesManager);
        if (townyManager.isEnabled()) blockBuildManagers.add(townyManager);
        if (griefPreventionManager.isEnabled()) blockBuildManagers.add(griefPreventionManager);

        // Break Managers
        if (worldGuardManager.isEnabled()) blockBreakManagers.add(worldGuardManager);
        if (factionsManager.isEnabled()) blockBreakManagers.add(factionsManager);
        if (locketteManager.isEnabled()) blockBreakManagers.add(locketteManager);
        if (preciousStonesManager.isEnabled()) blockBreakManagers.add(preciousStonesManager);
        if (townyManager.isEnabled()) blockBreakManagers.add(townyManager);
        if (griefPreventionManager.isEnabled()) blockBreakManagers.add(griefPreventionManager);

        initialized = true;
    }

    protected void processUndo()
    {
        long now = System.currentTimeMillis();
        while (scheduledUndo.size() > 0) {
            UndoList undo = scheduledUndo.peek();
            if (now < undo.getScheduledTime()) {
                break;
            }
            scheduledUndo.poll();
            undo.undoScheduled();
        }
    }

    protected void processPendingBatches()
    {
        int remainingWork = workPerUpdate;
        if (pendingConstruction.isEmpty()) return;

        List<Mage> pending = new ArrayList<>(pendingConstruction);
        while (remainingWork > 0 && !pending.isEmpty()) {
            int workPerMage = Math.max(10, remainingWork / pending.size());
            for (Iterator<Mage> iterator = pending.iterator(); iterator.hasNext();) {
                Mage apiMage = iterator.next();
                if (apiMage instanceof com.elmakers.mine.bukkit.magic.Mage) {
                    com.elmakers.mine.bukkit.magic.Mage mage = ((com.elmakers.mine.bukkit.magic.Mage) apiMage);
                    int workPerformed = mage.processPendingBatches(workPerMage);
                    if (!mage.hasPendingBatches()) {
                        iterator.remove();
                        pendingConstruction.remove(mage);
                    } else if (workPerformed < workPerMage) {
                        // Wait for next tick to process this action further since it's sleeping
                        iterator.remove();
                    }
                    remainingWork -= workPerformed;
                }
            }
        }
    }

    protected void activateMetrics() {
        // Activate Metrics
        final MagicController controller = this;
        metrics = null;
        if (metricsLevel > 0) {
            try {
                metrics = new Metrics(plugin);

                if (metricsLevel > 1) {
                    metrics.addCustomChart(new Metrics.MultiLineChart("Plugin Integration") {
                        @Override
                        public HashMap<String, Integer> getValues(HashMap<String, Integer> valueMap) {
                            valueMap.put("Essentials", controller.hasEssentials ? 1 : 0);
                            valueMap.put("Dynmap", controller.hasDynmap ? 1 : 0);
                            valueMap.put("Factions", controller.factionsManager.isEnabled() ? 1 : 0);
                            valueMap.put("WorldGuard", controller.worldGuardManager.isEnabled()  ? 1 : 0);
                            valueMap.put("Elementals", controller.elementalsEnabled() ? 1 : 0);
                            valueMap.put("Citizens", controller.citizens != null  ? 1 : 0);
                            valueMap.put("CommandBook", controller.hasCommandBook ? 1 : 0);
                            valueMap.put("PvpManager", controller.pvpManager.isEnabled() ? 1 : 0);
                            valueMap.put("Multiverse-Core", controller.multiverseManager.isEnabled() ? 1 : 0);
                            valueMap.put("Towny", controller.townyManager.isEnabled() ? 1 : 0);
                            valueMap.put("GriefPrevention", controller.griefPreventionManager.isEnabled() ? 1 : 0);
                            valueMap.put("PreciousStones", controller.preciousStonesManager.isEnabled()  ? 1 : 0);
                            valueMap.put("Lockette", controller.locketteManager.isEnabled() ? 1 : 0);
                            valueMap.put("NoCheatPlus", controller.ncpManager.isEnabled() ? 1 : 0);
                            return valueMap;
                        }
                    });

                    metrics.addCustomChart(new Metrics.MultiLineChart("Features Enabled") {
                        @Override
                        public HashMap<String, Integer> getValues(HashMap<String, Integer> valueMap) {
                            valueMap.put("Crafting", controller.crafting.isEnabled() ? 1 : 0);
                            valueMap.put("Enchanting", controller.enchanting.isEnabled() ? 1 : 0);
                            valueMap.put("SP", controller.isSPEnabled() ? 1 : 0);
                            return valueMap;
                        }
                    });
                }

                if (metricsLevel > 2) {
                    metrics.addCustomChart(new Metrics.MultiLineChart("Total Casts by Category") {
                        @Override
                        public HashMap<String, Integer> getValues(HashMap<String, Integer> valueMap) {
                            for (final SpellCategory category : categories.values()) {
                                valueMap.put(category.getName(), (int)category.getCastCount());
                            }
                            return valueMap;
                        }
                    });
                }

                if (metricsLevel > 3) {
                    metrics.addCustomChart(new Metrics.MultiLineChart("Total Casts") {
                        @Override
                        public HashMap<String, Integer> getValues(HashMap<String, Integer> valueMap) {
                            for (final SpellTemplate spell : spells.values()) {
                                if (!(spell instanceof Spell)) continue;
                                valueMap.put(spell.getName(), (int)((Spell)spell).getCastCount());
                            }
                            return valueMap;
                        }
                    });
                }

                plugin.getLogger().info("Activated BStats");
            } catch (Exception ex) {
                plugin.getLogger().warning("Failed to load BStats: " + ex.getMessage());
            }
        }
    }

    protected void registerListeners() {
        PluginManager pm = plugin.getServer().getPluginManager();
        pm.registerEvents(crafting, plugin);
        pm.registerEvents(mobs, plugin);
        pm.registerEvents(enchanting, plugin);
        pm.registerEvents(anvil, plugin);
        pm.registerEvents(blockController, plugin);
        pm.registerEvents(hangingController, plugin);
        pm.registerEvents(entityController, plugin);
        pm.registerEvents(playerController, plugin);
        pm.registerEvents(inventoryController, plugin);
        pm.registerEvents(explosionController, plugin);
    }

    public Collection<Mage> getPending() {
        return pendingConstruction;
    }

    public Collection<UndoList> getPendingUndo() {
        return scheduledUndo;
    }

    protected void addPending(Mage mage) {
        pendingConstruction.add(mage);
    }

    public boolean removeMarker(String id, String group) {
        boolean removed = false;
        if (dynmap != null && dynmapShowWands) {
            return dynmap.removeMarker(id, group);
        }

        return removed;
    }

    public boolean addMarker(String id, String group, String title, String world, int x, int y, int z, String description) {
        boolean created = false;
        if (dynmap != null && dynmapShowWands) {
            created = dynmap.addMarker(id, group, title, world, x, y, z, description);
        }

        return created;
    }

    @Override
    public File getConfigFolder() {
        return configFolder;
    }

    protected File getDataFile(String fileName) {
        return new File(dataFolder, fileName + ".yml");
    }

    protected ConfigurationSection loadDataFile(String fileName) {
        File dataFile = getDataFile(fileName);
        if (!dataFile.exists()) {
            return null;
        }
        Configuration configuration = YamlConfiguration.loadConfiguration(dataFile);
        return configuration;
    }

    protected YamlDataFile createDataFile(String fileName) {
        File dataFile = new File(dataFolder, fileName + ".yml");
        YamlDataFile configuration = new YamlDataFile(getLogger(), dataFile);
        return configuration;
    }

    protected ConfigurationSection loadConfigFile(String fileName, boolean loadDefaults)
        throws IOException, InvalidConfigurationException {
        return loadConfigFile(fileName, loadDefaults, false);
    }

    protected void enableAll(ConfigurationSection rootSection) {
        Set<String> keys = rootSection.getKeys(false);
        for (String key : keys)
        {
            ConfigurationSection section = rootSection.getConfigurationSection(key);
            if (!section.isSet("enabled")) {
                section.set("enabled", true);
            }
        }
    }

    protected ConfigurationSection loadConfigFile(String fileName, boolean loadDefaults, boolean disableDefaults)
        throws IOException, InvalidConfigurationException {
        return loadConfigFile(fileName, loadDefaults, disableDefaults, false);
    }

    protected ConfigurationSection loadConfigFile(String fileName, boolean loadDefaults, boolean disableDefaults, boolean filesReplace)
        throws IOException, InvalidConfigurationException {
        String configFileName = fileName + ".yml";
        File configFile = new File(configFolder, configFileName);
        if (!configFile.exists()) {
            getLogger().info("Saving template " + configFileName + ", edit to customize configuration.");
            plugin.saveResource(configFileName, false);
        }

        boolean usingExample = exampleDefaults != null && exampleDefaults.length() > 0;

        String examplesFileName = usingExample ? "examples/" + exampleDefaults + "/" + fileName + ".yml" : null;
        String defaultsFileName = "defaults/" + fileName + ".defaults.yml";

        File savedDefaults = new File(configFolder, defaultsFileName);
        if (saveDefaultConfigs) {
            plugin.saveResource(defaultsFileName, true);
        } else if (savedDefaults.exists()) {
            getLogger().info("Deleting defaults file: " + defaultsFileName + ", these have been removed to avoid confusion");
            savedDefaults.delete();
        }
        
        getLogger().info("Loading " + configFile.getName());
        ConfigurationSection overrides = CompatibilityUtils.loadConfiguration(configFile);
        ConfigurationSection config = new MemoryConfiguration();

        if (loadDefaults) {
            getLogger().info(" Based on defaults " + defaultsFileName);
            ConfigurationSection defaultConfig = CompatibilityUtils.loadConfiguration(plugin.getResource(defaultsFileName));
            if (disableDefaults) {
                Set<String> keys = defaultConfig.getKeys(false);
                for (String key : keys)
                {
                    defaultConfig.getConfigurationSection(key).set("enabled", false);
                }
                enableAll(overrides);
            }
            config = ConfigurationUtils.addConfigurations(config, defaultConfig);
        }

        if (usingExample) {
            InputStream input = plugin.getResource(examplesFileName);
            if (input != null)
            {
                ConfigurationSection exampleConfig = CompatibilityUtils.loadConfiguration(input);
                if (disableDefaults) {
                    enableAll(exampleConfig);
                }
                config = ConfigurationUtils.addConfigurations(config, exampleConfig);
                getLogger().info(" Using " + examplesFileName);
            }
        }

        if (addExamples != null && addExamples.size() > 0) {
            for (String example : addExamples) {
                examplesFileName = "examples/" + example + "/" + fileName + ".yml";
                InputStream input = plugin.getResource(examplesFileName);
                if (input != null)
                {
                    ConfigurationSection exampleConfig = CompatibilityUtils.loadConfiguration(input);
                    if (disableDefaults) {
                        enableAll(exampleConfig);
                    }
                    config = ConfigurationUtils.addConfigurations(config, exampleConfig, false);
                    getLogger().info(" Added " + examplesFileName);
                }
            }
        }

        // Apply overrides after loading defaults and examples
        config = ConfigurationUtils.addConfigurations(config, overrides);

        // Apply file overrides last
        File configSubFolder = new File(configFolder, fileName);
        config = loadConfigFolder(config, configSubFolder, filesReplace, disableDefaults);

        return config;
    }
    
    protected ConfigurationSection loadConfigFolder(ConfigurationSection config, File configSubFolder, boolean filesReplace, boolean setEnabled)
        throws IOException, InvalidConfigurationException {
        if (configSubFolder.exists()) {
            File[] files = configSubFolder.listFiles();
            for (File file : files) {
                if (file.getName().startsWith(".")) continue;
                if (file.isDirectory()) {
                    config = loadConfigFolder(config, file, filesReplace, setEnabled);
                } else {
                    ConfigurationSection fileOverrides = CompatibilityUtils.loadConfiguration(file);
                    getLogger().info("  Loading " + file.getName());
                    if (setEnabled) {
                        enableAll(fileOverrides);
                    }
                    if (filesReplace) {
                        config = ConfigurationUtils.replaceConfigurations(config, fileOverrides);
                    } else {
                        config = ConfigurationUtils.addConfigurations(config, fileOverrides);
                    }
                }
            }
        } else {
            configSubFolder.mkdir();
        }
        
        return config;
    }

    protected ConfigurationSection loadExamples(ConfigurationSection properties) throws InvalidConfigurationException, IOException {
        logVerbosity = properties.getInt("log_verbosity", 0);
        exampleDefaults = properties.getString("example", exampleDefaults);
        addExamples = properties.getStringList("add_examples");

        if ((exampleDefaults != null && exampleDefaults.length() > 0) || (addExamples != null && addExamples.size() > 0)) {
            // Reload config, example will be used this time.
            if (exampleDefaults != null && exampleDefaults.length() > 0)
            {
                getLogger().info("Overriding configuration with example: " + exampleDefaults);
            }
            if (addExamples != null && addExamples.size() > 0)
            {
                getLogger().info("Adding examples: " + StringUtils.join(addExamples, ","));
            }
            properties = loadConfigFile(CONFIG_FILE, true);
        }

        loadDefaultSpells = properties.getBoolean("load_default_spells", loadDefaultSpells);
        disableDefaultSpells = properties.getBoolean("disable_default_spells", disableDefaultSpells);
        loadDefaultWands = properties.getBoolean("load_default_wands", loadDefaultWands);
        disableDefaultWands = properties.getBoolean("disable_default_wands", disableDefaultWands);
        loadDefaultCrafting = properties.getBoolean("load_default_crafting", loadDefaultCrafting);
        loadDefaultClasses = properties.getBoolean("load_default_classes", loadDefaultClasses);
        loadDefaultPaths = properties.getBoolean("load_default_enchanting", loadDefaultPaths);
        loadDefaultPaths = properties.getBoolean("load_default_paths", loadDefaultPaths);
        loadDefaultMobs = properties.getBoolean("load_default_mobs", loadDefaultMobs);
        loadDefaultItems = properties.getBoolean("load_default_items", loadDefaultItems);

        return properties;
    }

    protected ConfigurationSection loadMainConfiguration() throws InvalidConfigurationException, IOException {
        ConfigurationSection configuration = loadConfigFile(CONFIG_FILE, true);
        loadInitialProperties(configuration);
        return configuration;
    }

    protected ConfigurationSection loadMessageConfiguration() throws InvalidConfigurationException, IOException {
        return loadConfigFile(MESSAGES_FILE, true);
    }

    protected ConfigurationSection loadMaterialsConfiguration() throws InvalidConfigurationException, IOException {
        return loadConfigFile(MATERIALS_FILE, true);
    }

    protected ConfigurationSection loadWandConfiguration() throws InvalidConfigurationException, IOException {
        return loadConfigFile(WANDS_FILE, loadDefaultWands, disableDefaultWands, true);
    }

    protected ConfigurationSection loadPathConfiguration() throws InvalidConfigurationException, IOException {
        return loadConfigFile(PATHS_FILE, loadDefaultPaths);
    }

    protected ConfigurationSection loadCraftingConfiguration() throws InvalidConfigurationException, IOException {
        return loadConfigFile(CRAFTING_FILE, loadDefaultCrafting);
    }

    protected ConfigurationSection loadClassConfiguration() throws InvalidConfigurationException, IOException {
        return loadConfigFile(CLASSES_FILE, loadDefaultClasses);
    }

    protected ConfigurationSection loadMobsConfiguration() throws InvalidConfigurationException, IOException {
        return loadConfigFile(MOBS_FILE, loadDefaultMobs);
    }

    protected ConfigurationSection loadItemsConfiguration() throws InvalidConfigurationException, IOException {
        return loadConfigFile(ITEMS_FILE, loadDefaultItems);
    }

    protected Map<String, ConfigurationSection> loadAndMapSpells() throws InvalidConfigurationException, IOException {
        Map<String, ConfigurationSection> spellConfigs = new HashMap<>();
        ConfigurationSection config = loadConfigFile(SPELLS_FILE, loadDefaultSpells, disableDefaultSpells);
        if (config == null) return spellConfigs;

        // Reset cached spell configs
        spellConfigurations.clear();
        baseSpellConfigurations.clear();

        Set<String> spellKeys = config.getKeys(false);
        for (String key : spellKeys) {
            if (key.equals("default") || key.equals("override")) continue;

            ConfigurationSection spellNode = getSpellConfig(key, config);
            if (spellNode == null || !spellNode.getBoolean("enabled", true)) {
                continue;
            }

            // Kind of a hacky way to do this, and only works with BaseSpell spells.
            if (noPvpRestricted) {
                spellNode.set("pvp_restricted", false);
            } else if (allPvpRestricted) {
                spellNode.set("pvp_restricted", true);
            }

            spellConfigs.put(key, spellNode);
        }

        return spellConfigs;
    }

    protected void finalizeLoad(ConfigurationLoadTask loader, CommandSender sender) {
        if (!loader.success) {
            if (sender != null) {
                sender.sendMessage(ChatColor.RED + "An error occurred reloading configurations, please check server logs!");
            }

            // Check for initial load failure on startup
            if (!loaded) {
                getLogger().warning("*** An error occurred while loading configurations ***");
                getLogger().warning("***         Magic is temporarily disabled          ***");
                getLogger().warning("***   Please check the errors above, fix configs   ***");
                getLogger().warning("***    And '/magic load' or restart the server     ***");
            }

            return;
        }

        // Clear some cache stuff... mainly this is for debugging/testing.
        schematics.clear();

        // Process loaded data

        // Main configuration
        loadProperties(loader.configuration);

        // Sub-configurations
        messages.load(loader.messages);
        loadMaterials(loader.materials);

        items.load(loader.items);
        getLogger().info("Loaded " + items.getCount() + " items");

        mobs.load(loader.mobs);
        getLogger().info("Loaded " + mobs.getCount() + " mob templates");

        loadSpells(loader.spells);
        getLogger().info("Loaded " + spells.size() + " spells");

        loadMageClasses(loader.classes);
        getLogger().info("Loaded " + mageClasses.size() + " classes");

        loadPaths(loader.paths);
        getLogger().info("Loaded " + getPathCount() + " progression paths");

        loadWandTemplates(loader.wands);
        getLogger().info("Loaded " + getWandTemplates().size() + " wands");

        crafting.load(loader.crafting);
        getLogger().info("Loaded " + crafting.getCount() + " crafting recipes");

        // Finalize integrations, we only do this one time at startup.
        if (!initialized) {
            finalizeIntegration();
        }
        
        LoadEvent loadEvent = new LoadEvent(this);
        Bukkit.getPluginManager().callEvent(loadEvent);

        loaded = true;
        if (sender != null) {
            sender.sendMessage(ChatColor.AQUA + "Configuration reloaded.");
        }
    }

    private int getPathCount() {
        return WandUpgradePath.getPathKeys().size();
    }

    private void loadPaths(ConfigurationSection pathConfiguration) {
        WandUpgradePath.loadPaths(this, pathConfiguration);
    }

    public void loadConfiguration() {
        loadConfiguration(null);
    }

    public void loadConfiguration(CommandSender sender) {
        ConfigurationLoadTask loadTask = new ConfigurationLoadTask(this, sender);
        if (initialized) {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, loadTask);
        } else {
            loadTask.runNow();
        }
    }

    protected void loadSpellData() {
        try {
            ConfigurationSection configNode = loadDataFile(SPELLS_DATA_FILE);

            if (configNode == null) return;

            Set<String> keys = configNode.getKeys(false);

            for (String key : keys) {
                ConfigurationSection node = configNode.getConfigurationSection(key);
                SpellKey spellKey = new SpellKey(key);
                SpellData templateData = templateDataMap.get(spellKey.getBaseKey());
                if (templateData == null) {
                    templateData = new SpellData(spellKey.getBaseKey());
                    templateDataMap.put(templateData.getKey().getBaseKey(), templateData);
                }
                templateData.setCastCount(templateData.getCastCount() + node.getLong("cast_count", 0));
                templateData.setLastCast(Math.max(templateData.getLastCast(), node.getLong("last_cast", 0)));
            }
        } catch (Exception ex) {
            getLogger().warning("Failed to load spell metrics");
        }
    }

    public void load() {
        loadConfiguration();
        loadSpellData();

        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                // Load lost wands
                getLogger().info("Loading lost wand data");
                loadLostWands();

                // Load URL Map Data
                try {
                    maps.resetAll();
                    maps.loadConfiguration();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                getLogger().info("Finished loading data.");
            }
        }, 10);
    }

    protected void loadLostWands() {
        try {
            ConfigurationSection lostWandConfiguration = loadDataFile(LOST_WANDS_FILE);
            if (lostWandConfiguration != null) {
                Set<String> wandIds = lostWandConfiguration.getKeys(false);
                for (String wandId : wandIds) {
                    if (wandId == null || wandId.length() == 0) continue;
                    LostWand lostWand = new LostWand(wandId, lostWandConfiguration.getConfigurationSection(wandId));
                    if (!lostWand.isValid()) {
                        getLogger().info("Skipped invalid entry in lostwands.yml file, entry will be deleted. The wand is really lost now!");
                        continue;
                    }
                    addLostWand(lostWand);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        getLogger().info("Loaded " + lostWands.size() + " lost wands");
    }

    protected void saveSpellData(Collection<YamlDataFile> stores) {
        String lastKey = "";
        try {
            YamlDataFile spellsDataFile = createDataFile(SPELLS_DATA_FILE);
            for (SpellData data : templateDataMap.values()) {
                lastKey = data.getKey().getBaseKey();
                ConfigurationSection spellNode = spellsDataFile.createSection(lastKey);
                if (spellNode == null) {
                    getLogger().warning("Error saving spell data for " + lastKey);
                    continue;
                }
                spellNode.set("cast_count", data.getCastCount());
                spellNode.set("last_cast", data.getLastCast());
            }
            stores.add(spellsDataFile);
        } catch (Throwable ex) {
            getLogger().warning("Error saving spell data for " + lastKey);
            ex.printStackTrace();
        }
    }

    protected void saveLostWands(Collection<YamlDataFile> stores) {
        String lastKey = "";
        try {
            YamlDataFile lostWandsConfiguration = createDataFile(LOST_WANDS_FILE);
            for (Entry<String, LostWand> wandEntry : lostWands.entrySet()) {
                lastKey = wandEntry.getKey();
                if (lastKey == null || lastKey.length() == 0) continue;
                ConfigurationSection wandNode = lostWandsConfiguration.createSection(lastKey);
                if (wandNode == null) {
                    getLogger().warning("Error saving lost wand data for " + lastKey);
                    continue;
                }
                if (!wandEntry.getValue().isValid()) {
                    getLogger().warning("Invalid lost and data for " + lastKey);
                    continue;
                }
                wandEntry.getValue().save(wandNode);
            }
            stores.add(lostWandsConfiguration);
        } catch (Throwable ex) {
            getLogger().warning("Error saving lost wand data for " + lastKey);
            ex.printStackTrace();
        }
    }

    protected String getChunkKey(Block block) {
        return getChunkKey(block.getLocation());
    }

    protected String getChunkKey(Location location) {
        World world = location.getWorld();
        if (world == null) return null;
        return world.getName() + "|" + (location.getBlockX() >> 4) + "," + (location.getBlockZ() >> 4);
    }

    protected String getChunkKey(Chunk chunk) {
        return chunk.getWorld().getName() + "|" + chunk.getX() + "," + chunk.getZ();
    }

    public boolean addLostWand(LostWand lostWand) {
        lostWands.put(lostWand.getId(), lostWand);
        try {
            String chunkKey = getChunkKey(lostWand.getLocation());
            if (chunkKey == null) return false;

            Set<String> chunkWands = lostWandChunks.get(chunkKey);
            if (chunkWands == null) {
                chunkWands = new HashSet<>();
                lostWandChunks.put(chunkKey, chunkWands);
            }
            chunkWands.add(lostWand.getId());

            if (dynmapShowWands) {
                addLostWandMarker(lostWand);
            }
        } catch (Exception ex) {
            getLogger().log(Level.WARNING, "Error loading lost wand id " + lostWand.getId() + " - is it in an unloaded world?", ex);
        }

        return true;
    }

    public boolean addLostWand(Wand wand, Location dropLocation) {
        addLostWand(wand.makeLost(dropLocation));
        return true;
    }

    public boolean removeLostWand(String wandId) {
        if (wandId == null || wandId.length() == 0 || !lostWands.containsKey(wandId)) return false;

        LostWand lostWand = lostWands.get(wandId);
        lostWands.remove(wandId);
        String chunkKey = getChunkKey(lostWand.getLocation());
        if (chunkKey == null) return false;

        Set<String> chunkWands = lostWandChunks.get(chunkKey);
        if (chunkWands != null) {
            chunkWands.remove(wandId);
            if (chunkWands.size() == 0) {
                lostWandChunks.remove(chunkKey);
            }
        }

        if (dynmapShowWands) {
            if (removeMarker("wand-" + wandId, "Wands")) {
                info("Wand removed from map");
            }
        }

        return true;
    }

    public WandMode getDefaultWandMode() {
        return defaultWandMode;
    }

    public WandMode getDefaultBrushMode() {
        return defaultBrushMode;
    }

    public String getDefaultWandPath() {
        return defaultWandPath;
    }

    protected void savePlayerData(Collection<MageData> stores) {
        try {
            for (Entry<String, ? extends Mage> mageEntry : mages.entrySet()) {
                Mage mage = mageEntry.getValue();
                if (!mage.isPlayer() && !saveNonPlayerMages)
                {
                    continue;
                }

                if (!mage.isLoading()) {
                    MageData mageData = new MageData(mage.getId());
                    if (mage.save(mageData)) {
                        stores.add(mageData);
                    }
                } else {
                    getLogger().info("Skipping save of mage, already loading: " + mage.getName());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void save()
    {
        save(false);
    }

	public void save(boolean asynchronous)
	{
        if (!initialized) return;
        maps.save(asynchronous);

        final List<YamlDataFile> saveData = new ArrayList<>();
        final List<MageData> saveMages = new ArrayList<>();
        if (savePlayerData && mageDataStore != null) {
            savePlayerData(saveMages);
        }
        info("Saving " + saveMages.size() + " players");
		saveSpellData(saveData);
		saveLostWands(saveData);

        if (mageDataStore != null) {
            if (asynchronous) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                    @Override
                    public void run() {
                        synchronized (saveLock) {
                            for (MageData mageData : saveMages) {
                                mageDataStore.save(mageData, null);
                            }
                            for (YamlDataFile config : saveData) {
                                config.save();
                            }
                            info("Finished saving");
                        }
                    }
                });
            } else {
                synchronized (saveLock) {
                    for (MageData mageData : saveMages) {
                        mageDataStore.save(mageData, null);
                    }
                    for (YamlDataFile config : saveData) {
                        config.save();
                    }
                    info("Finished saving");
                }
            }
        }

        SaveEvent saveEvent = new SaveEvent(asynchronous);
        Bukkit.getPluginManager().callEvent(saveEvent);
	}

    protected ConfigurationSection getSpellConfig(String key, ConfigurationSection config)
    {
        return getSpellConfig(key, config, true);
    }

    protected ConfigurationSection getSpellConfig(String key, ConfigurationSection config, boolean addInherited)
    {
        if (addInherited) {
            ConfigurationSection built = spellConfigurations.get(key);
            if (built != null) {
                return built;
            }
        } else {
            ConfigurationSection built = baseSpellConfigurations.get(key);
            if (built != null) {
                return built;
            }
        }
        ConfigurationSection spellNode = config.getConfigurationSection(key);
        if (spellNode == null)
        {
            getLogger().warning("Spell " + key + " not known");
            return null;
        }
        spellNode = ConfigurationUtils.addConfigurations(new MemoryConfiguration(), spellNode);

        SpellKey spellKey = new SpellKey(key);
        String inheritFrom = spellNode.getString("inherit");
        if (inheritFrom != null && inheritFrom.equalsIgnoreCase("false"))
        {
            inheritFrom = null;
        }
        String upgradeInheritsFrom = null;
        if (spellKey.isVariant()) {
            if (!spellUpgradesEnabled) {
                return null;
            }
            int level = spellKey.getLevel();
            upgradeInheritsFrom = spellKey.getBaseKey();
            if (level != 2) {
                upgradeInheritsFrom += "|" + (level - 1);
            }
        }

        boolean processInherited = addInherited && inheritFrom != null;
        if (processInherited || upgradeInheritsFrom != null)
        {
            if (processInherited && key.equals(inheritFrom))
            {
                getLogger().warning("Spell " + key + " inherits from itself");
            }
            else if (processInherited)
            {
                ConfigurationSection inheritConfig = getSpellConfig(inheritFrom, config);
                if (inheritConfig != null)
                {
                    spellNode = ConfigurationUtils.addConfigurations(spellNode, inheritConfig, false);
                }
                else
                {
                    getLogger().warning("Spell " + key + " inherits from unknown ancestor " + inheritFrom);
                }
            }

            if (upgradeInheritsFrom != null)
            {
                if (config.contains(upgradeInheritsFrom))
                {
                    ConfigurationSection baseInheritConfig = getSpellConfig(upgradeInheritsFrom, config, inheritFrom == null);
                    spellNode = ConfigurationUtils.addConfigurations(spellNode, baseInheritConfig, inheritFrom != null);
                } else {
                    getLogger().warning("Spell upgrade " + key + " inherits from unknown level " + upgradeInheritsFrom);
                }
            }
        } else {
            ConfigurationSection defaults = config.getConfigurationSection("default");
            if (defaults != null) {
                spellNode = ConfigurationUtils.addConfigurations(spellNode, defaults, false);
            }
        }

        if (addInherited) {
            spellConfigurations.put(key, spellNode);
        } else {
            baseSpellConfigurations.put(key, spellNode);
        }

        // Apply spell override last
        ConfigurationSection override = config.getConfigurationSection("override");
        if (override != null) {
            spellNode = ConfigurationUtils.addConfigurations(spellNode, override, true);
        }
        
        return spellNode;
    }

	protected void loadSpells(Map<String, ConfigurationSection> spellConfigs)
	{
		if (spellConfigs == null) return;
		
		// Reset existing spells.
		spells.clear();
        spellAliases.clear();
        categories.clear();

		for (Entry<String, ConfigurationSection> entry : spellConfigs.entrySet())
		{
            String key = entry.getKey();
            if (key.equals("default") || key.equals("override")) continue;

            ConfigurationSection spellNode = entry.getValue();
            if (spellNode == null) {
                continue;
            }

			Spell newSpell = null;
            try {
                newSpell = loadSpell(key, spellNode, this);
            } catch (Exception ex) {
                newSpell = null;
                ex.printStackTrace();
            }

			if (newSpell == null)
			{
				getLogger().warning("Magic: Error loading spell " + key);
				continue;
			}

            if (!newSpell.hasIcon())
            {
                String icon = spellNode.getString("icon");
                if (icon != null && !icon.isEmpty())
                {
                    getLogger().info("Couldn't load spell icon '" + icon + "' for spell: " + newSpell.getKey());
                }
            }

			addSpell(newSpell);
		}

		// Second pass to fulfill requirements, which needs all spells loaded
        for (Entry<String, ConfigurationSection> entry : spellConfigs.entrySet()) {
            SpellTemplate template = getSpellTemplate(entry.getKey());
            if (template != null) {
                template.loadPrerequisites(entry.getValue());
            }
        }
		
		// Update registered mages so their spells are current
		for (Mage mage : mages.values()) {
            if (mage instanceof com.elmakers.mine.bukkit.magic.Mage) {
                ((com.elmakers.mine.bukkit.magic.Mage)mage).loadSpells(spellConfigurations);
            }
		}
	}

	public static Spell loadSpell(String name, ConfigurationSection node, MageController controller)
	{
		String className = node.getString("class");
		if (className == null || className.equalsIgnoreCase("action") || className.equalsIgnoreCase("actionspell") )
        {
            className = "com.elmakers.mine.bukkit.spell.ActionSpell";
        }
        else if (className.indexOf('.') <= 0)
		{
			className = BUILTIN_SPELL_CLASSPATH + "." + className;
		}

		Class<?> spellClass = null;
		try
		{
			spellClass = Class.forName(className);
		}
		catch (Throwable ex)
		{
			controller.getLogger().log(Level.WARNING, "Error loading spell: " + className, ex);
			return null;
		}

		Object newObject;
		try
		{
			newObject = spellClass.newInstance();
		}
		catch (Throwable ex)
		{

			controller.getLogger().log(Level.WARNING, "Error loading spell: " + className, ex);
			return null;
		}

		if (newObject == null || !(newObject instanceof MageSpell))
		{
			controller.getLogger().warning("Error loading spell: " + className + ", does it implement MageSpell?");
			return null;
		}

		MageSpell newSpell = (MageSpell)newObject;
		newSpell.initialize(controller);
		newSpell.loadTemplate(name, node);
		com.elmakers.mine.bukkit.api.spell.SpellCategory category = newSpell.getCategory();
		if (category instanceof SpellCategory) {
			((SpellCategory)category).addSpellTemplate(newSpell);
		}
		return newSpell;
	}

    @Override
    public String getReflectiveMaterials(Mage mage, Location location) {
        return worldGuardManager.getReflective(mage.getPlayer(), location);
    }

    @Override
    public String getDestructibleMaterials(Mage mage, Location location) {
        return worldGuardManager.getDestructible(mage.getPlayer(), location);
    }

    @Override
    public Set<String> getSpellOverrides(Mage mage, Location location) {
        return worldGuardManager.getSpellOverrides(mage.getPlayer(), location);
    }
	
	protected void loadMaterials(ConfigurationSection materialNode)
	{
		if (materialNode == null) return;
		
		Set<String> keys = materialNode.getKeys(false);
		for (String key : keys) {
			materialSets.put(key, getMaterials(materialNode, key));
		}
		if (materialSets.containsKey("building")) {
			buildingMaterials = materialSets.get("building");
		}
		if (materialSets.containsKey("indestructible")) {
			indestructibleMaterials = materialSets.get("indestructible");
		}
		if (materialSets.containsKey("restricted")) {
			restrictedMaterials = materialSets.get("restricted");
		}
		if (materialSets.containsKey("destructible")) {
			destructibleMaterials = materialSets.get("destructible");
		}
        if (materialSets.containsKey("interactible")) {
            interactibleMaterials = materialSets.get("interactible");
        }
        if (materialSets.containsKey("containers")) {
            containerMaterials = materialSets.get("containers");
        }
        if (materialSets.containsKey("wearable")) {
            wearableMaterials = materialSets.get("wearable");
        }
        if (materialSets.containsKey("melee")) {
            meleeMaterials = materialSets.get("melee");
        }
        if (materialSets.containsKey("attachable")) {
            com.elmakers.mine.bukkit.block.UndoList.attachables = materialSets.get("attachable");
        }
        if (materialSets.containsKey("attachable_wall")) {
            com.elmakers.mine.bukkit.block.UndoList.attachablesWall = materialSets.get("attachable_wall");
        }
        if (materialSets.containsKey("attachable_double")) {
            com.elmakers.mine.bukkit.block.UndoList.attachablesDouble = materialSets.get("attachable_double");
        }
	}

    public void loadInitialProperties(ConfigurationSection properties) {
        allPvpRestricted = properties.getBoolean("pvp_restricted", allPvpRestricted);
        noPvpRestricted = properties.getBoolean("allow_pvp_restricted", noPvpRestricted);
        saveDefaultConfigs = properties.getBoolean("save_default_configs", false);
    }

	protected void loadProperties(ConfigurationSection properties)
	{
		if (properties == null) return;

		// Cancel any pending save tasks
		if (autoSaveTaskId > 0) {
			Bukkit.getScheduler().cancelTask(autoSaveTaskId);
			autoSaveTaskId = 0;
		}

        EffectPlayer.debugEffects(properties.getBoolean("debug_effects", false));
        CompatibilityUtils.USE_MAGIC_DAMAGE = properties.getBoolean("use_magic_damage", CompatibilityUtils.USE_MAGIC_DAMAGE);
        EffectPlayer.setParticleRange(properties.getInt("particle_range", EffectPlayer.PARTICLE_RANGE));

        enableResourcePackCheck = properties.getBoolean("enable_resource_pack_check", true);
        resourcePackCheckInterval = properties.getInt("resource_pack_check_interval", 0);
        defaultResourcePack = properties.getString("resource_pack", null);
        // For legacy configs
        defaultResourcePack = properties.getString("default_resource_pack", defaultResourcePack);
        // For combined configs
        if (addExamples != null && addExamples.size() > 0 && !defaultResourcePack.isEmpty())
        {
            defaultResourcePack = properties.getString("add_resource_pack", defaultResourcePack);
        }

        // For reloading after disabling the RP
        if (defaultResourcePack == null || defaultResourcePack.isEmpty()) {
            resourcePack = null;
            resourcePackHash = null;
        }

        resourcePackDelay = properties.getLong("resource_pack_delay", 0);
        showCastHoloText = properties.getBoolean("show_cast_holotext", showCastHoloText);
        showActivateHoloText = properties.getBoolean("show_activate_holotext", showCastHoloText);
        castHoloTextRange = properties.getInt("cast_holotext_range", castHoloTextRange);
        activateHoloTextRange = properties.getInt("activate_holotext_range", activateHoloTextRange);
        urlIconsEnabled = properties.getBoolean("url_icons_enabled", urlIconsEnabled);
        spellUpgradesEnabled = properties.getBoolean("enable_spell_upgrades", spellUpgradesEnabled);
        spellProgressionEnabled = properties.getBoolean("enable_spell_progression", spellProgressionEnabled);
        autoSpellUpgradesEnabled = properties.getBoolean("enable_automatic_spell_upgrades", autoSpellUpgradesEnabled);
        autoPathUpgradesEnabled = properties.getBoolean("enable_automatic_spell_upgrades", autoPathUpgradesEnabled);
		undoQueueDepth = properties.getInt("undo_depth", undoQueueDepth);
        workPerUpdate = properties.getInt("work_per_update", workPerUpdate);
        workFrequency = properties.getInt("work_frequency", workFrequency);
        com.elmakers.mine.bukkit.magic.Mage.UPDATE_FREQUENCY = properties.getInt("mage_update_frequency", com.elmakers.mine.bukkit.magic.Mage.UPDATE_FREQUENCY);
        undoFrequency = properties.getInt("undo_frequency", undoFrequency);
		pendingQueueDepth = properties.getInt("pending_depth", pendingQueueDepth);
		undoMaxPersistSize = properties.getInt("undo_max_persist_size", undoMaxPersistSize);
		commitOnQuit = properties.getBoolean("commit_on_quit", commitOnQuit);
        saveNonPlayerMages = properties.getBoolean("save_non_player_mages", saveNonPlayerMages);
        defaultWandPath = properties.getString("default_wand_path", "");
        Wand.DEFAULT_WAND_TEMPLATE = properties.getString("default_wand", "");
        defaultWandMode = Wand.parseWandMode(properties.getString("default_wand_mode", ""), defaultWandMode);
        defaultBrushMode = Wand.parseWandMode(properties.getString("default_brush_mode", ""), defaultBrushMode);
        backupInventories = properties.getBoolean("backup_player_inventory", true);
        Wand.brushSelectSpell = properties.getString("brush_select_spell", Wand.brushSelectSpell);
		showMessages = properties.getBoolean("show_messages", showMessages);
        showCastMessages = properties.getBoolean("show_cast_messages", showCastMessages);
		messageThrottle = properties.getInt("message_throttle", 0);
		soundsEnabled = properties.getBoolean("sounds", soundsEnabled);
		fillingEnabled = properties.getBoolean("fill_wands", fillingEnabled);
        maxFillLevel = properties.getInt("fill_wand_level", maxFillLevel);
		welcomeWand = properties.getString("welcome_wand", "");
		maxDamagePowerMultiplier = (float)properties.getDouble("max_power_damage_multiplier", maxDamagePowerMultiplier);
		maxConstructionPowerMultiplier = (float)properties.getDouble("max_power_construction_multiplier", maxConstructionPowerMultiplier);
		maxRangePowerMultiplier = (float)properties.getDouble("max_power_range_multiplier", maxRangePowerMultiplier);
		maxRangePowerMultiplierMax = (float)properties.getDouble("max_power_range_multiplier_max", maxRangePowerMultiplierMax);
		maxRadiusPowerMultiplier = (float)properties.getDouble("max_power_radius_multiplier", maxRadiusPowerMultiplier);
		maxRadiusPowerMultiplierMax = (float)properties.getDouble("max_power_radius_multiplier_max", maxRadiusPowerMultiplierMax);

        maxPower = (float)properties.getDouble("max_power", maxPower);
        maxDamageReduction = (float)properties.getDouble("max_damage_reduction", maxDamageReduction);
        maxDamageReductionExplosions = (float)properties.getDouble("max_damage_reduction_explosions", maxDamageReductionExplosions);
        maxDamageReductionFalling = (float)properties.getDouble("max_damage_reduction_falling", maxDamageReductionFalling);
        maxDamageReductionFire = (float)properties.getDouble("max_damage_reduction_fire", maxDamageReductionFire);
        maxDamageReductionPhysical = (float)properties.getDouble("max_damage_reduction_physical", maxDamageReductionPhysical);
        maxDamageReductionProjectiles = (float)properties.getDouble("max_damage_reduction_projectiles", maxDamageReductionProjectiles);
        maxCostReduction = (float)properties.getDouble("max_cost_reduction", maxCostReduction);
        maxCooldownReduction = (float)properties.getDouble("max_cooldown_reduction", maxCooldownReduction);
        maxMana = properties.getInt("max_mana", maxMana);
        maxManaRegeneration = properties.getInt("max_mana_regeneration", maxManaRegeneration);
        worthSkillPoints = properties.getDouble("worth_sp", 1);
        skillPointIcon = properties.getString("sp_item_icon_url");
        skillPointItemsEnabled = properties.getBoolean("sp_items_enabled", true);
        worthBase = properties.getDouble("worth_base", 1);
        worthXP = properties.getDouble("worth_xp", 1);
        ConfigurationSection currencies = properties.getConfigurationSection("currency");
        if (currencies != null)
        {
            Collection<String> worthItemKeys = currencies.getKeys(true);
            for (String worthItemKey : worthItemKeys) {
                MaterialAndData material = new MaterialAndData(worthItemKey);
                ConfigurationSection currencyConfig = currencies.getConfigurationSection(worthItemKey);
                ItemStack worthItemType = material.getItemStack(1);
                double worthItemAmount = currencyConfig.getDouble("worth");
                String worthItemName = currencyConfig.getString("name");
                String worthItemNamePlural = currencyConfig.getString("name_plural");

                currencyItem = new CurrencyItem(worthItemType, worthItemAmount, worthItemName, worthItemNamePlural);
                break;
            }
        }
        else
        {
            currencyItem = null;
        }

        SafetyUtils.MAX_VELOCITY = properties.getDouble("max_velocity", 10 );
        HitboxUtils.setHitboxScale(properties.getDouble("hitbox_scale", 1.0));
        HitboxUtils.setHitboxScaleY(properties.getDouble("hitbox_scale_y", 1.0));
        HitboxUtils.setHitboxSneakScaleY(properties.getDouble("hitbox_sneaking_scale_y", 0.75));
        if (properties.contains("hitboxes"))
        {
            HitboxUtils.configureHitboxes(properties.getConfigurationSection("hitboxes"));
        }
        if (properties.contains("head_sizes"))
        {
            HitboxUtils.configureHeadSizes(properties.getConfigurationSection("head_sizes"));
        }
        if (properties.contains("max_height"))
        {
            HitboxUtils.configureMaxHeights(properties.getConfigurationSection("max_height"));
        }

        maps.setAnimationAllowed(properties.getBoolean("enable_map_animations", true));
        costReduction = (float)properties.getDouble("cost_reduction", costReduction);
		cooldownReduction = (float)properties.getDouble("cooldown_reduction", cooldownReduction);
		castCommandCostReduction = (float)properties.getDouble("cast_command_cost_reduction", castCommandCostReduction);
		castCommandCooldownReduction = (float)properties.getDouble("cast_command_cooldown_reduction", castCommandCooldownReduction);
		castCommandPowerMultiplier = (float)properties.getDouble("cast_command_power_multiplier", castCommandPowerMultiplier);
        castConsoleCostReduction = (float)properties.getDouble("cast_console_cost_reduction", castConsoleCostReduction);
        castConsoleCooldownReduction = (float)properties.getDouble("cast_console_cooldown_reduction", castConsoleCooldownReduction);
        castConsolePowerMultiplier = (float)properties.getDouble("cast_console_power_multiplier", castConsolePowerMultiplier);
		autoUndo = properties.getInt("auto_undo", autoUndo);
        spellDroppingEnabled = properties.getBoolean("allow_spell_dropping", spellDroppingEnabled);
		essentialsSignsEnabled = properties.getBoolean("enable_essentials_signs", essentialsSignsEnabled);
        citizensEnabled = properties.getBoolean("enable_citizens", citizensEnabled);
		dynmapShowWands = properties.getBoolean("dynmap_show_wands", dynmapShowWands);
		dynmapShowSpells = properties.getBoolean("dynmap_show_spells", dynmapShowSpells);
        dynmapOnlyPlayerSpells = properties.getBoolean("dynmap_only_player_spells", dynmapOnlyPlayerSpells);
		dynmapUpdate = properties.getBoolean("dynmap_update", dynmapUpdate);
        protectLocked = properties.getBoolean("protected_locked", protectLocked);
        bindOnGive = properties.getBoolean("bind_on_give", bindOnGive);
		bypassBuildPermissions = properties.getBoolean("bypass_build", bypassBuildPermissions);
        bypassBreakPermissions = properties.getBoolean("bypass_break", bypassBreakPermissions);
		bypassPvpPermissions = properties.getBoolean("bypass_pvp", bypassPvpPermissions);
        bypassFriendlyFire = properties.getBoolean("bypass_friendly_fire", bypassFriendlyFire);
        useScoreboardTeams = properties.getBoolean("use_scoreboard_teams", useScoreboardTeams);
        defaultFriendly = properties.getBoolean("default_friendly", defaultFriendly);
		extraSchematicFilePath = properties.getString("schematic_files", extraSchematicFilePath);
		createWorldsEnabled = properties.getBoolean("enable_world_creation", createWorldsEnabled);
        defaultSkillIcon = properties.getString("default_skill_icon", defaultSkillIcon);
        skillInventoryRows = properties.getInt("skill_inventory_max_rows", skillInventoryRows);
        BaseSpell.MAX_LORE_LENGTH = properties.getInt("lore_wrap_limit", BaseSpell.MAX_LORE_LENGTH);
        Wand.MAX_LORE_LENGTH = BaseSpell.MAX_LORE_LENGTH;
        libsDisguiseEnabled = properties.getBoolean("enable_libsdisguises", libsDisguiseEnabled);
        skillAPIEnabled = properties.getBoolean("skillapi_enabled", skillAPIEnabled);
        useSkillAPIMana = properties.getBoolean("use_skillapi_mana", useSkillAPIMana);

        skillsUseHeroes = properties.getBoolean("skills_use_heroes", skillsUseHeroes);
        useHeroesParties = properties.getBoolean("use_heroes_parties", useHeroesParties);
        useHeroesMana = properties.getBoolean("use_heroes_mana", useHeroesMana);
        heroesSkillPrefix = properties.getString("heroes_skill_prefix", heroesSkillPrefix);
        skillsUsePermissions = properties.getBoolean("skills_use_permissions", skillsUsePermissions);

		messagePrefix = properties.getString("message_prefix", messagePrefix);
		castMessagePrefix = properties.getString("cast_message_prefix", castMessagePrefix);

        redstoneReplacement = ConfigurationUtils.getMaterialAndData(properties, "redstone_replacement", redstoneReplacement);

		messagePrefix = ChatColor.translateAlternateColorCodes('&', messagePrefix);
		castMessagePrefix = ChatColor.translateAlternateColorCodes('&', castMessagePrefix);

		worldGuardManager.setEnabled(properties.getBoolean("region_manager_enabled", worldGuardManager.isEnabled()));
		factionsManager.setEnabled(properties.getBoolean("factions_enabled", factionsManager.isEnabled()));
        pvpManager.setEnabled(properties.getBoolean("pvp_manager_enabled", pvpManager.isEnabled()));
        multiverseManager.setEnabled(properties.getBoolean("multiverse_enabled", multiverseManager.isEnabled()));
        preciousStonesManager.setEnabled(properties.getBoolean("precious_stones_enabled", preciousStonesManager.isEnabled()));
        preciousStonesManager.setOverride(properties.getBoolean("precious_stones_override", true));
        townyManager.setEnabled(properties.getBoolean("towny_enabled", townyManager.isEnabled()));
        townyManager.setWildernessBypass(properties.getBoolean("towny_wilderness_bypass", true));
        locketteManager.setEnabled(properties.getBoolean("lockette_enabled", locketteManager.isEnabled()));
        griefPreventionManager.setEnabled(properties.getBoolean("grief_prevention_enabled", griefPreventionManager.isEnabled()));
        ncpManager.setEnabled(properties.getBoolean("ncp_enabled", false));

        metricsLevel = properties.getInt("metrics_level", metricsLevel);

        Wand.regenWhileInactive = properties.getBoolean("regenerate_while_inactive", Wand.regenWhileInactive);
		if (properties.contains("mana_display")) {
            String manaDisplay = properties.getString("mana_display");
            if (manaDisplay.equalsIgnoreCase("bar") || manaDisplay.equalsIgnoreCase("hybrid")) {
                Wand.manaMode = WandManaMode.BAR;
            } else if (manaDisplay.equalsIgnoreCase("number")) {
                Wand.manaMode = WandManaMode.NUMBER;
            } else if (manaDisplay.equalsIgnoreCase("durability")) {
                Wand.manaMode = WandManaMode.DURABILITY;
            } else if (manaDisplay.equalsIgnoreCase("glow")) {
                Wand.manaMode = WandManaMode.GLOW;
            } else if (manaDisplay.equalsIgnoreCase("none")) {
                Wand.manaMode = WandManaMode.NONE;
            }
		}
        if (properties.contains("sp_display")) {
            String spDisplay = properties.getString("sp_display");
            if (spDisplay.equalsIgnoreCase("number")) {
                Wand.spMode = WandManaMode.NUMBER;
            } else {
                Wand.spMode = WandManaMode.NONE;
            }
        }
        spEnabled = properties.getBoolean("sp_enabled", true);
        spEarnEnabled = properties.getBoolean("sp_earn_enabled", true);
        spMaximum = properties.getInt("sp_max", 9999);

        undoEntityTypes.clear();
        if (properties.contains("entity_undo_types"))
        {
            undoEntityTypes = new HashSet<>();
            Collection<String> typeStrings = ConfigurationUtils.getStringList(properties, "entity_undo_types");
            for (String typeString : typeStrings)
            {
                try {
                    undoEntityTypes.add(EntityType.valueOf(typeString.toUpperCase()));
                } catch (Exception ex) {
                    getLogger().warning("Unknown entity type: " + typeString);
                }
            }
        }

        String defaultLocationString = properties.getString("default_cast_location");
        try {
            com.elmakers.mine.bukkit.magic.Mage.DEFAULT_CAST_LOCATION = CastSourceLocation.valueOf(defaultLocationString.toUpperCase());
        } catch (Exception ex) {
            com.elmakers.mine.bukkit.magic.Mage.DEFAULT_CAST_LOCATION = CastSourceLocation.MAINHAND;
            getLogger().warning("Invalid default_cast_location: " + defaultLocationString);
        }
        com.elmakers.mine.bukkit.magic.Mage.DEFAULT_CAST_OFFSET.setZ(properties.getDouble("default_cast_location_offset", com.elmakers.mine.bukkit.magic.Mage.DEFAULT_CAST_OFFSET.getZ()));
        com.elmakers.mine.bukkit.magic.Mage.DEFAULT_CAST_OFFSET.setY(properties.getDouble("default_cast_location_offset_vertical", com.elmakers.mine.bukkit.magic.Mage.DEFAULT_CAST_OFFSET.getY()));
        com.elmakers.mine.bukkit.magic.Mage.OFFHAND_CAST_COOLDOWN = properties.getInt("offhand_cast_cooldown", com.elmakers.mine.bukkit.magic.Mage.OFFHAND_CAST_COOLDOWN);
        com.elmakers.mine.bukkit.magic.Mage.SNEAKING_CAST_OFFSET = properties.getDouble("sneaking_cast_location_offset_vertical", com.elmakers.mine.bukkit.magic.Mage.SNEAKING_CAST_OFFSET);

		// Parse wand settings
		Wand.DefaultUpgradeMaterial = ConfigurationUtils.getMaterial(properties, "wand_upgrade_item", Wand.DefaultUpgradeMaterial);
        Wand.SpellGlow = properties.getBoolean("spell_glow", Wand.SpellGlow);
        Wand.LiveHotbarSkills = properties.getBoolean("live_hotbar_skills", Wand.LiveHotbarSkills);
        Wand.LiveHotbar = properties.getBoolean("live_hotbar", Wand.LiveHotbar);
        Wand.LiveHotbarCooldown = properties.getBoolean("live_hotbar_cooldown", Wand.LiveHotbar);
        Wand.BrushGlow = properties.getBoolean("brush_glow", Wand.BrushGlow);
        Wand.BrushItemGlow = properties.getBoolean("brush_item_glow", Wand.BrushItemGlow);
        Wand.WAND_KEY = properties.getString("wand_key", "wand");
        Wand.UPGRADE_KEY = properties.getString("wand_upgrade_key", "wand");
        Wand.WAND_SELF_DESTRUCT_KEY = properties.getString("wand_self_destruct_key", "");
        if (Wand.WAND_SELF_DESTRUCT_KEY.isEmpty()) {
            Wand.WAND_SELF_DESTRUCT_KEY = null;
        }
        Wand.HIDE_FLAGS = (byte)properties.getInt("wand_hide_flags", Wand.HIDE_FLAGS);
        Wand.Unbreakable = properties.getBoolean("wand_unbreakable", Wand.Unbreakable);
        Wand.Undroppable = properties.getBoolean("wand_undroppable", Wand.Undroppable);

        MaterialBrush.CopyMaterial = ConfigurationUtils.getMaterialAndData(properties, "copy_item", MaterialBrush.CopyMaterial);
		MaterialBrush.EraseMaterial = ConfigurationUtils.getMaterialAndData(properties, "erase_item", MaterialBrush.EraseMaterial);
		MaterialBrush.CloneMaterial = ConfigurationUtils.getMaterialAndData(properties, "clone_item", MaterialBrush.CloneMaterial);
		MaterialBrush.ReplicateMaterial = ConfigurationUtils.getMaterialAndData(properties, "replicate_item", MaterialBrush.ReplicateMaterial);
		MaterialBrush.SchematicMaterial = ConfigurationUtils.getMaterialAndData(properties, "schematic_item", MaterialBrush.SchematicMaterial);
		MaterialBrush.MapMaterial = ConfigurationUtils.getMaterialAndData(properties, "map_item", MaterialBrush.MapMaterial);
        MaterialBrush.DefaultBrushMaterial = ConfigurationUtils.getMaterialAndData(properties, "default_brush_item", MaterialBrush.DefaultBrushMaterial);
        MaterialBrush.configureReplacements(properties.getConfigurationSection("brush_replacements"));

        MaterialBrush.CopyCustomIcon = properties.getString("copy_icon_url", MaterialBrush.CopyCustomIcon);
        MaterialBrush.EraseCustomIcon = properties.getString("erase_icon_url", MaterialBrush.EraseCustomIcon);
        MaterialBrush.CloneCustomIcon = properties.getString("clone_icon_url", MaterialBrush.CloneCustomIcon);
        MaterialBrush.ReplicateCustomIcon = properties.getString("replicate_icon_url", MaterialBrush.ReplicateCustomIcon);
        MaterialBrush.SchematicCustomIcon = properties.getString("schematic_icon_url", MaterialBrush.SchematicCustomIcon);
        MaterialBrush.MapCustomIcon = properties.getString("map_icon_url", MaterialBrush.MapCustomIcon);
        MaterialBrush.DefaultBrushCustomIcon = properties.getString("default_brush_icon_url", MaterialBrush.DefaultBrushCustomIcon);

        BaseSpell.DEFAULT_DISABLED_ICON_URL = properties.getString("disabled_icon_url", BaseSpell.DEFAULT_DISABLED_ICON_URL);

        Wand.DEFAULT_CAST_OFFSET.setZ(properties.getDouble("wand_location_offset", Wand.DEFAULT_CAST_OFFSET.getZ()));
        Wand.DEFAULT_CAST_OFFSET.setY(properties.getDouble("wand_location_offset_vertical", Wand.DEFAULT_CAST_OFFSET.getY()));
        com.elmakers.mine.bukkit.magic.Mage.JUMP_EFFECT_FLIGHT_EXEMPTION_DURATION = properties.getInt("jump_exemption", 0);
        com.elmakers.mine.bukkit.magic.Mage.CHANGE_WORLD_EQUIP_COOLDOWN = properties.getInt("change_world_equip_cooldown", 0);
        com.elmakers.mine.bukkit.magic.Mage.DEACTIVATE_WAND_ON_WORLD_CHANGE = properties.getBoolean("close_wand_on_world_change", false);
        com.elmakers.mine.bukkit.magic.Mage.DEFAULT_SP = properties.getInt("sp_default", 0);

        Wand.inventoryOpenSound = ConfigurationUtils.toSoundEffect(properties.getString("wand_inventory_open_sound"));
        Wand.inventoryCloseSound = ConfigurationUtils.toSoundEffect(properties.getString("wand_inventory_close_sound"));
        Wand.inventoryCycleSound = ConfigurationUtils.toSoundEffect(properties.getString("wand_inventory_cycle_sound"));
        Wand.noActionSound = ConfigurationUtils.toSoundEffect(properties.getString("wand_no_action_sound"));

        if (blockPhysicsManager != null) {
            blockPhysicsManager.setVelocityScale(properties.getDouble("block_physics_velocity_scale", 1));
        }

        // Configure sub-controllers
        explosionController.loadProperties(properties);
        inventoryController.loadProperties(properties);
        blockController.setUndoOnWorldSave(properties.getBoolean("undo_on_world_save", false));
        blockController.setCreativeBreakFrequency(properties.getInt("prevent_creative_breaking", 0));
        entityController.loadProperties(properties);
        playerController.loadProperties(properties);

		// Set up other systems
		EffectPlayer.SOUNDS_ENABLED = soundsEnabled;

		// Set up auto-save timer
        final AutoSaveTask autoSave = new AutoSaveTask(this);
		int autoSaveIntervalTicks = properties.getInt("auto_save", 0) * 20 / 1000;;
		if (autoSaveIntervalTicks > 1) {
			autoSaveTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, autoSave,
					autoSaveIntervalTicks, autoSaveIntervalTicks);
		}

        savePlayerData = properties.getBoolean("save_player_data", true);
        externalPlayerData = properties.getBoolean("external_player_data", false);
        if (externalPlayerData) {
            getLogger().info("Magic is expecting player data to be loaded from an external source");
        } else if (!savePlayerData) {
            getLogger().info("Magic player data saving is disabled");
        }
        asynchronousSaving = properties.getBoolean("save_player_data_asynchronously", true);

        ConfigurationSection mageDataStore = properties.getConfigurationSection("player_data_store");
        if (mageDataStore != null) {
            String dataStoreClassName = mageDataStore.getString("class");
            try {
                Class<?> dataStoreClass = Class.forName(dataStoreClassName);
                Object dataStore = dataStoreClass.newInstance();
                if (dataStore == null || !(dataStore instanceof MageDataStore))
                {
                    getLogger().log(Level.WARNING, "Invalid player_data_store class " + dataStoreClassName + ", does it implement MageDataStore? Player data saving is disabled!");
                    this.mageDataStore = null;
                }
                else
                {
                    this.mageDataStore = (MageDataStore)dataStore;
                    this.mageDataStore.initialize(this, mageDataStore);
                }
            } catch (Exception ex) {
                getLogger().log(Level.WARNING, "Failed to create player_data_store class from " + dataStoreClassName + " player data saving is disabled!", ex);
                this.mageDataStore = null;
            }
        } else {
            getLogger().log(Level.WARNING, "Missing player_data_store configuration, player data saving disabled!");
            this.mageDataStore = null;
        }

        useBlockPhysics = properties.getBoolean("enable_block_physics", true);

        // Semi-deprecated Wand defaults
        Wand.DefaultWandMaterial = ConfigurationUtils.getMaterial(properties, "wand_item", Wand.DefaultWandMaterial);
        Wand.EnchantableWandMaterial = ConfigurationUtils.getMaterial(properties, "wand_item_enchantable", Wand.EnchantableWandMaterial);

		// Load sub-controllers
        enchanting.setEnabled(properties.getBoolean("enable_enchanting", enchanting.isEnabled()));
        if (enchanting.isEnabled()) {
            getLogger().info("Wand enchanting is enabled");
        }
        crafting.setEnabled(properties.getBoolean("enable_crafting", crafting.isEnabled()));
        if (crafting.isEnabled()) {
            getLogger().info("Wand crafting is enabled");
        }
		anvil.load(properties);
        if (anvil.isCombiningEnabled()) {
            getLogger().info("Wand anvil combining is enabled");
        }
        if (anvil.isOrganizingEnabled()) {
            getLogger().info("Wand anvil organizing is enabled");
        }
        if (isUrlIconsEnabled()) {
            getLogger().info("Skin-based spell icons enabled");
        } else {
            getLogger().info("Skin-based spell icons disabled");
        }
	}

	protected void clear()
	{
        initialized = false;
        Collection<Mage> saveMages = new ArrayList<Mage>(mages.values());
        for (Mage mage : saveMages)
        {
            playerQuit(mage);
        }

		mages.clear();
		pendingConstruction.clear();
		spells.clear();
	}
	
	protected void unregisterPhysicsHandler(Listener listener)
	{
		BlockPhysicsEvent.getHandlerList().unregister(listener);
		physicsHandler = null;
	}

    @Override
    public void scheduleUndo(UndoList undoList)
    {
        undoList.setHasBeenScheduled();
        scheduledUndo.add(undoList);
    }

    @Override
    public void cancelScheduledUndo(UndoList undoList)
    {
        scheduledUndo.remove(undoList);
    }

	public boolean hasWandPermission(Player player)
	{
		return hasPermission(player, "Magic.wand.use", true);
	}

    public boolean hasWandPermission(Player player, Wand wand)
    {
        if (player.hasPermission("Magic.bypass")) return true;
        if (wand.isSuperPowered() && !player.hasPermission("Magic.wand.use.powered")) return false;
        if (wand.isSuperProtected() && !player.hasPermission("Magic.wand.use.protected")) return false;

        String template = wand.getTemplateKey();
        if (template != null && !template.isEmpty())
        {
            String pNode = "Magic.use." + template;
            if (!hasPermission(player, pNode, true)) return false;
        }
        Location location = player.getLocation();
        Boolean override = worldGuardManager.getWandPermission(player, wand, location);
        return override == null || override;
    }

    @Override
    public boolean hasCastPermission(CommandSender sender, SpellTemplate spell)
    {
        if (sender == null) return true;

        if (sender instanceof Player && ((Player)sender).hasPermission("Magic.bypass")) {
            return true;
        }
        return hasPermission(sender, spell.getPermissionNode());
    }

    @Override
    public Boolean getRegionCastPermission(Player player, SpellTemplate spell, Location location)
    {
        if (player != null && player.hasPermission("Magic.bypass")) return true;
        return worldGuardManager.getCastPermission(player, spell, location);
    }

    @Override
    public Boolean getPersonalCastPermission(Player player, SpellTemplate spell, Location location)
    {
        if (player != null && player.hasPermission("Magic.bypass")) return true;
        return preciousStonesManager.getCastPermission(player, spell, location);
    }

	public boolean hasPermission(Player player, String pNode, boolean defaultValue)
	{
		// Should this return defaultValue? Can't give perms to console.
		if (player == null) return true;
		
		// The GM won't handle this properly because we are unable to register
        // dynamic lists (spells, wands, brushes) in plugin.yml
		if (pNode.contains(".")) {
			String parentNode = pNode.substring(0, pNode.lastIndexOf('.') + 1) + "*";
			boolean isParentSet = player.isPermissionSet(parentNode);
            if (isParentSet) {
				defaultValue = player.hasPermission(parentNode);
			}
		}

        boolean isSet = player.isPermissionSet(pNode);
        return isSet ? player.hasPermission(pNode) : defaultValue;
    }

	public boolean hasPermission(Player player, String pNode)
	{
		return hasPermission(player, pNode, false);
	}

	@Override
	public boolean hasPermission(CommandSender sender, String pNode)
	{
		if (!(sender instanceof Player)) return true;
		return hasPermission((Player) sender, pNode, false);
	}
	
	@Override
    public boolean hasPermission(CommandSender sender, String pNode, boolean defaultValue)
	{
		if (!(sender instanceof Player)) return true;
		return hasPermission((Player) sender, pNode, defaultValue);
	}

    public UndoList getPendingUndo(Location location)
    {
        return com.elmakers.mine.bukkit.block.UndoList.getUndoList(location);
    }
	
	public void registerFallingBlock(Entity fallingBlock, Block block) {
        UndoList undoList = getPendingUndo(fallingBlock.getLocation());
        if (undoList != null) {
            undoList.fall(fallingBlock, block);
        }
	}
	
	public UndoList getEntityUndo(Entity entity) {
		UndoList blockList = null;
		if (entity == null) return null;
        Mage mage = getRegisteredMage(entity);
        if (mage == null && entity instanceof Projectile) {
            Projectile projectile = (Projectile)entity;
            ProjectileSource source = projectile.getShooter();
            if (source instanceof LivingEntity) {
                entity = (LivingEntity)source;
                mage = getRegisteredMage(entity);
            }
        }
        if (mage != null) {
            if (mage instanceof com.elmakers.mine.bukkit.magic.Mage) {
                UndoList undoList = mage.getLastUndoList();
                if (undoList != null) {
                    long now = System.currentTimeMillis();
                    if (undoList.getModifiedTime() > now - undoTimeWindow) {
                        blockList = undoList;
                    }
                }
            }
		} else {
            blockList = com.elmakers.mine.bukkit.block.UndoList.getUndoList(entity);
        }
		
		return blockList;
	}
    
	@Override
	public void giveItemToPlayer(Player player, ItemStack itemStack) {
        // Bind item if configured to do so
        if (bindOnGive && Wand.isWand(itemStack)) {
            Wand wand = getWand(itemStack);
            if (wand.isBound()) {
                wand.tryToOwn(player);
                itemStack = wand.getItem();
            }
        }

        Mage mage = getMage(player);
        mage.giveItem(itemStack);
	}
    
    @Override
    public boolean commitOnQuit() {
        return commitOnQuit;
    }

    public void playerQuit(Mage mage) {
        playerQuit(mage, null);
    }

    public void undoScheduled() {
        int undid = 0;
        while (!scheduledUndo.isEmpty()) {
            UndoList undoList = scheduledUndo.poll();
            undoList.undoScheduled(true);
        }
        if (undid > 0) {
            info("Undid " + undid + " pending spells");
        }
    }

    protected void mageQuit(final Mage mage, final MageDataCallback callback) {
        com.elmakers.mine.bukkit.api.wand.Wand wand = mage.getActiveWand();
        final boolean isOpen = wand != null && wand.isInventoryOpen();
        mage.deactivate();
        mage.undoScheduled();

        // Delay removal one tick to avoid issues with plugins that kill
        // players on logout (CombatTagPlus, etc)
        // Don't delay on shutdown, though.
        if (initialized && mage instanceof com.elmakers.mine.bukkit.magic.Mage) {
            final com.elmakers.mine.bukkit.magic.Mage quitMage = (com.elmakers.mine.bukkit.magic.Mage)mage;
            quitMage.setUnloading(true);
            plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    // Just in case the player relogged in that one tick..
                    if (quitMage.isUnloading()) {
                        finalizeMageQuit(quitMage, callback, isOpen);
                    }
                }
            },1 );
        } else {
            finalizeMageQuit(mage, callback, isOpen);
        }
    }
    
    protected void finalizeMageQuit(final Mage mage, final MageDataCallback callback, final boolean isOpen) {
        // Unregister
        if (!externalPlayerData || !mage.isPlayer()) {
            removeMage(mage);
        }
        if (!mage.isLoading() && (mage.isPlayer() || saveNonPlayerMages) && loaded)
        {
            // Save synchronously on shutdown
            saveMage(mage, initialized, callback, isOpen);
        }
        else if (callback != null)
        {
            callback.run(null);
        }
    }

    protected void playerQuit(Mage mage, MageDataCallback callback) {
		// Make sure they get their portraits re-rendered on relogin.
        maps.resend(mage.getName());

        mageQuit(mage, callback);
	}

    @Override
    public void forgetMage(Mage mage) {
        if (mage instanceof com.elmakers.mine.bukkit.magic.Mage) {
            ((com.elmakers.mine.bukkit.magic.Mage)mage).setForget(true);
        }
    }

    @Override
    public void removeMage(Mage mage) {
        mages.remove(mage.getId());
    }

    @Override
    public void removeMage(String id) {
        mages.remove(id);
    }

    public void saveMage(Mage mage, boolean asynchronous)
    {
        saveMage(mage, asynchronous, null);
    }

    public void saveMage(Mage mage, boolean asynchronous, final MageDataCallback callback) {
        saveMage(mage, asynchronous, null, false);
    }

    public void saveMage(Mage mage, boolean asynchronous, final MageDataCallback callback, boolean wandInventoryOpen)
    {
        if (!savePlayerData) {
            if (callback != null) {
                callback.run(null);
            }
            return;
        }
        info("Saving player data for " + mage.getName() + " (" + mage.getId() + ") " + (asynchronous ? "" : " synchronously"));
        final MageData mageData = new MageData(mage.getId());
        if (mageDataStore != null && mage.save(mageData)) {
            if (wandInventoryOpen) {
                mageData.setOpenWand(true);
            }
            if (asynchronous) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                    @Override
                    public void run() {
                        synchronized (saveLock) {
                            try {
                                mageDataStore.save(mageData, callback);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                });
            } else {
                synchronized (saveLock) {
                    try {
                        mageDataStore.save(mageData, callback);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

	public ItemStack removeItemFromWand(Wand wand, ItemStack droppedItem) {
		if (wand == null || droppedItem == null || Wand.isWand(droppedItem)) {
			return null;
		}

		if (Wand.isSpell(droppedItem)) {
			String spellKey = Wand.getSpell(droppedItem);
			wand.removeSpell(spellKey);

			// Update the item for proper naming and lore
			SpellTemplate spell = getSpellTemplate(spellKey);
			if (spell != null) {
				Wand.updateSpellItem(messages, droppedItem, spell, "", null, null, true);
			}
		} else if (Wand.isBrush(droppedItem)) {
			String brushKey = Wand.getBrush(droppedItem);
			wand.removeBrush(brushKey);

			// Update the item for proper naming and lore
			Wand.updateBrushItem(getMessages(), droppedItem, brushKey, null);
		}
		return droppedItem;
	}

    public void onArmorUpdated(final com.elmakers.mine.bukkit.magic.Mage mage) {
        plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                mage.armorUpdated();
            }
        }, 1);
    }

    @Override
    public boolean isLocked(Block block) {
        return protectLocked && containerMaterials.contains(block.getType()) && CompatibilityUtils.isLocked(block);
    }
	
	protected boolean addLostWandMarker(LostWand lostWand) {
		Location location = lostWand.getLocation();
		return addMarker("wand-" + lostWand.getId(), "Wands", lostWand.getName(), location.getWorld().getName(),
			location.getBlockX(), location.getBlockY(), location.getBlockZ(), lostWand.getDescription()
		);
	}
	
	public void toggleCastCommandOverrides(Mage apiMage, CommandSender sender, boolean override) {
        // Don't track command-line casts
        // Reach into internals a bit here.
		if (apiMage instanceof com.elmakers.mine.bukkit.magic.Mage) {
            com.elmakers.mine.bukkit.magic.Mage mage = (com.elmakers.mine.bukkit.magic.Mage)apiMage;
			if (sender instanceof BlockCommandSender)
            {
                mage.setCostReduction(override ? castCommandCostReduction : 0);
                mage.setCooldownReduction(override ? castCommandCooldownReduction : 0);
                mage.setPowerMultiplier(override ? castCommandPowerMultiplier : 1);
            }
            else
            {
                mage.setCostReduction(override ? castConsoleCostReduction : 0);
                mage.setCooldownReduction(override ? castConsoleCooldownReduction : 0);
                mage.setPowerMultiplier(override ? castConsolePowerMultiplier : 1);
            }
		}
	}
	
	public float getCooldownReduction() {
		return cooldownReduction;
	}
	
	public float getCostReduction() {
		return costReduction;
	}
	
	public Material getDefaultMaterial() {
		return defaultMaterial;
	}
	
	@Override
    public Collection<com.elmakers.mine.bukkit.api.wand.LostWand> getLostWands() {
		return new ArrayList<com.elmakers.mine.bukkit.api.wand.LostWand>(lostWands.values());
	}
	
	public Collection<Mage> getAutomata() {
		Collection<Mage> all = new ArrayList<>();
		for (Mage mage : mages.values()) {
		    if (mage.isAutomaton()) {
                all.add(mage);
            }
		}
		return all;
	}
	
	public boolean cast(Mage mage, String spellName, ConfigurationSection parameters, CommandSender sender, Entity entity)
	{
		Player usePermissions = (sender == entity && entity instanceof Player) ? (Player)entity
                : (sender instanceof Player ? (Player)sender : null);
        if (entity == null && sender instanceof Player) {
            entity = (Player)sender;
        }
		Location targetLocation = null;
		if (mage == null) {
			CommandSender mageController = (entity != null && entity instanceof Player) ? (Player)entity : sender;
			if (sender != null && sender instanceof BlockCommandSender) {
                targetLocation = ((BlockCommandSender) sender).getBlock().getLocation();
            }
            if (mageController == null) {
                mage = getMage(entity);
            } else {
                mage = getMage(entity, mageController);
            }
		}

        if (mage == null) return false;

        // This is a bit of a hack to make automata maintain direction
        if (targetLocation != null) {
            Location mageLocation = mage.getLocation();
            if (mageLocation != null) {
                targetLocation.setPitch(mageLocation.getPitch());
                targetLocation.setYaw(mageLocation.getYaw());
            }
        }
		
		SpellTemplate template = getSpellTemplate(spellName);
		if (template == null || !template.hasCastPermission(usePermissions))
		{
			if (sender != null) {
				sender.sendMessage("Spell " + spellName + " unknown");
			}
			return false;
		}
		com.elmakers.mine.bukkit.api.spell.Spell spell = mage.getSpell(spellName);
		if (spell == null)
		{
			if (sender != null) {
				sender.sendMessage("Spell " + spellName + " unknown");
			}
			return false;
		}

        // TODO: Load configured list of parameters!
		// Make it free and skip cooldowns, if configured to do so.
		toggleCastCommandOverrides(mage, sender, true);
        boolean success = false;
        try {
            success = spell.cast(parameters, targetLocation);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
		toggleCastCommandOverrides(mage, sender, false);
        // Removed sending messages here due to the log spam in WG region messages
        // Maybe should be a parameter option or something?

		return success;
	}
	
	public void onCast(Mage mage, com.elmakers.mine.bukkit.api.spell.Spell spell, SpellResult result) {
		if (dynmapShowSpells && dynmap != null && result.isSuccess()) {
            if (dynmapOnlyPlayerSpells && (mage == null || !mage.isPlayer())) {
                return;
            }
			dynmap.showCastMarker(mage, spell, result);
		}

        if (result.isSuccess() && getShowCastHoloText()) {
            mage.showHoloText(mage.getEyeLocation(), spell.getName(), 10000);
        }
	}

    @Override
    public com.elmakers.mine.bukkit.api.magic.Messages getMessages() {
        return messages;
    }

    @Override
    public MapController getMaps() {
        return maps;
    }

    public String getWelcomeWand() {
        return welcomeWand;
    }
	
	public void sendToMages(String message, Location location, int range) {
		int rangeSquared = range * range;
		if (message != null && message.length() > 0) {
			for (Mage mage : mages.values())
			{
				if (!mage.isPlayer() || mage.isDead() || !mage.isOnline() || !mage.hasLocation()) continue;
				if (!mage.getLocation().getWorld().equals(location.getWorld())) continue;
				if (mage.getLocation().toVector().distanceSquared(location.toVector()) < rangeSquared) {
					mage.sendMessage(message);
				}
			}
		}
	}

    @Override
    public boolean isNPC(Entity entity) {
        return (entity != null && (entity.hasMetadata("NPC") || entity.hasMetadata("shopkeeper")));
    }

    @Override
    public boolean isVanished(Entity entity) {
        if (entity == null) return false;
        for (MetadataValue meta : entity.getMetadata("vanished")) {
            return meta.asBoolean();
        }
        return false;
    }
	
	@Override
	public void updateBlock(Block block)
	{
		updateBlock(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
	}
	
	@Override
	public void updateBlock(String worldName, int x, int y, int z)
	{
		if (dynmap != null && dynmapUpdate)
		{
			dynmap.triggerRenderOfBlock(worldName, x, y, z);
		}
	}
	
	@Override
	public void updateVolume(String worldName, int minx, int miny, int minz, int maxx, int maxy, int maxz)
	{
		if (dynmap != null && dynmapUpdate && worldName != null && worldName.length() > 0)
		{
			dynmap.triggerRenderOfVolume(worldName, minx, miny, minz, maxx, maxy, maxz);
		}
	}
	
	public void update(String worldName, BoundingBox area)
	{
		if (dynmap != null && dynmapUpdate && area != null && worldName != null && worldName.length() > 0)
		{
			dynmap.triggerRenderOfVolume(worldName, 
				area.getMin().getBlockX(), area.getMin().getBlockY(), area.getMin().getBlockZ(), 
				area.getMax().getBlockX(), area.getMax().getBlockY(), area.getMax().getBlockZ());
		}
	}
	
	@Override
	public void update(com.elmakers.mine.bukkit.api.block.BlockList blockList)
	{
		if (blockList != null) {
            update(blockList.getWorldName(), blockList.getArea());
		}
	}

	@Override
    public void cleanItem(ItemStack item) {
        InventoryUtils.removeMeta(item, Wand.WAND_KEY);
        InventoryUtils.removeMeta(item, Wand.UPGRADE_KEY);
        InventoryUtils.removeMeta(item, "spell");
        InventoryUtils.removeMeta(item, "skill");
        InventoryUtils.removeMeta(item, "brush");
        InventoryUtils.removeMeta(item, "sp");
    }
	
	@Override
	public boolean canCreateWorlds()
	{
		return createWorldsEnabled;
	}

    private Set<Material> getMaterials(String materialSet) {
        Set<Material> materials;
        String materialString = materialSet;
        if (materialSet.equals("*")) {
            materials = new WildcardHashSet<>();
        } else if (materialSet.startsWith("!")) {
            materialString = materialString.substring(1);
            materials = new NegatedHashSet<>();
        } else {
            materials = new HashSet<>();
        }
        String[] nameList = StringUtils.split(materialString, ',');
        for (String matName : nameList)
        {
            if (materialSets.containsKey(matName)) {
                materials.addAll(materialSets.get(matName));
            } else {
                Material material = ConfigurationUtils.toMaterial(matName);
                if (material != null) {
                    materials.add(material);
                }
            }
        }

        return materials;
    }

    private Set<Material> getMaterials(ConfigurationSection node, String key)
    {
        if (node.isString(key)) {
            return getMaterials(node.getString(key));
        }
        List<String> materialData = node.getStringList(key);
        if (materialData == null) {
            return null;
        }

        Set<Material> materials = new HashSet<>();
        for (String matName : materialData)
        {
            Material material = ConfigurationUtils.toMaterial(matName);
            if (material != null) {
                materials.add(material);
            }
        }

        return materials;
    }

	@Override
    public Set<Material> getMaterialSet(String name)
	{
        if (name == null || name.isEmpty()) return null;
        
        Set<Material> materials = materialSets.get(name);
        if (materials == null) {
            materials = getMaterials(name);
            materialSets.put(name, materials);
        }
		return materials;
	}
	
	@Override
	public void sendToMages(String message, Location location) {
		sendToMages(message, location, toggleMessageRange);
	}
	
	@Override
	public int getMaxUndoPersistSize() {
		return undoMaxPersistSize;
	}

	@Override
	public MagicPlugin getPlugin()
	{
		return plugin;
	}

    @Override
    public MagicAPI getAPI() {
        return plugin;
    }

    public Collection<? extends Mage> getMutableMages() {
        return mages.values();
    }

    @Override
    public Collection<Mage> getMages() {
        Collection<? extends Mage> values = mages.values();
        return Collections.unmodifiableCollection(values);
    }

	@Override
	public Set<Material> getBuildingMaterials()
	{
		return buildingMaterials;
	}

	@Override
	public Set<Material> getDestructibleMaterials()
	{
		return destructibleMaterials;
	}

	@Override
	public Set<Material> getRestrictedMaterials()
	{
		return restrictedMaterials;
	}
	
	@Override
	public int getMessageThrottle()
	{
		return messageThrottle;
	}

    @Override
    public boolean isMage(Entity entity) {
        if (entity == null) return false;
        String id = mageIdentifier.fromEntity(entity);
        return mages.containsKey(id);
    }

    @Override
	public Collection<String> getMaterialSets()
	{
		return materialSets.keySet();
	}
	
	@Override
	public Collection<String> getPlayerNames() 
	{
		List<String> playerNames = new ArrayList<>();
        Collection<? extends Player> players = plugin.getServer().getOnlinePlayers();
        for (Player player : players) {
            if (isNPC(player)) continue;
            playerNames.add(player.getName());
        }
		return playerNames;
	}

	@Override
	public void disablePhysics(int interval)
	{
		if (physicsHandler == null && interval > 0) {
			physicsHandler = new PhysicsHandler(this);
			Bukkit.getPluginManager().registerEvents(physicsHandler, plugin);
		}
        if (physicsHandler != null) {
            physicsHandler.setInterval(interval);
        }
	}
	
	@Override
	public boolean commitAll()
	{
		boolean undid = false;
		for (Mage mage : mages.values()) {
			undid = mage.commit() || undid;
		}
        com.elmakers.mine.bukkit.block.UndoList.commitAll();
		return undid;
	}

    @Override
    public boolean canTarget(Entity attacker, Entity entity)
    {
        if (useHeroesParties && heroesManager != null && attacker instanceof Player && entity instanceof Player)
        {
            if (heroesManager.isInParty((Player)attacker, (Player)entity, true))
            {
                return false;
            }
        }
        if (useScoreboardTeams && attacker instanceof Player && entity instanceof Player)
        {
            Player player1 = (Player)attacker;
            Player player2 = (Player)entity;

            Scoreboard scoreboard1 = player1.getScoreboard();
            Scoreboard scoreboard2 = player2.getScoreboard();

            if (scoreboard1 != null && scoreboard2 != null)
            {
                Team team1 = scoreboard1.getEntryTeam(player1.getName());
                Team team2 = scoreboard2.getEntryTeam(player2.getName());
                if (team1 != null && team2 != null && team1.equals(team2))
                {
                    return false;
                }
            }
        }
        return preciousStonesManager.canTarget(attacker, entity) && townyManager.canTarget(attacker, entity);
    }

    @Override
    public boolean isFriendly(Entity attacker, Entity entity)
    {
        // We are always friends with ourselves
        if (attacker == entity) return true;

        // Check to see if we are not using a team or party system
        if (!useScoreboardTeams && (!useHeroesParties || heroesManager == null))
        {
            return defaultFriendly;
        }

        // Check for Heroes party
        if (useHeroesParties && heroesManager != null && attacker instanceof Player && entity instanceof Player)
        {
            if (heroesManager.isInParty((Player)attacker, (Player)entity, false))
            {
                return true;
            }
        }

        // Check for scoreboard teams
        if (useScoreboardTeams && attacker instanceof Player && entity instanceof Player)
        {
            Player player1 = (Player)attacker;
            Player player2 = (Player)entity;

            Scoreboard scoreboard1 = player1.getScoreboard();
            Scoreboard scoreboard2 = player2.getScoreboard();

            if (scoreboard1 != null && scoreboard2 != null)
            {
                Team team1 = scoreboard1.getEntryTeam(player1.getName());
                Team team2 = scoreboard2.getEntryTeam(player2.getName());
                if (team1 != null && team2 != null && team1.equals(team2))
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
	public Location getWarp(String warpName) {
        Location location = null;
		if (warpController != null) {
            try {
                location = warpController.getWarp(warpName);
            } catch (Exception ex) {
                location = null;
            }
        }
		return location;
	}

    @Override
    public Location getTownLocation(Player player) {
        return townyManager.getTownLocation(player);
    }

    @Override
    public Map<String, Location> getHomeLocations(Player player) {
        return preciousStonesManager.getFieldLocations(player);
    }

    public TownyManager getTowny() {
        return townyManager;
    }

    public PreciousStonesManager getPreciousStones() {
        return preciousStonesManager;
    }
	
	@Override
	public boolean sendMail(CommandSender sender, String fromPlayer, String toPlayer, String message) {
		if (mailer != null) {
			return mailer.sendMail(sender, fromPlayer, toPlayer, message);
		}
		
		return false;
	}

	@Override
	public UndoList undoAny(Block target)
	{
		for (Mage mage : mages.values())
		{
			UndoList undid = mage.undo(target);
			if (undid != null)
			{
				return undid;
			}
		}

		return null;
	}

    @Override
    public UndoList undoRecent(Block target, int timeout)
    {
        for (Mage mage : mages.values())
        {
            com.elmakers.mine.bukkit.api.block.UndoQueue queue = mage.getUndoQueue();
            UndoList undid = queue.undoRecent(target, timeout);
            if (undid != null)
            {
                return undid;
            }
        }

        return null;
    }

    public CitizensController getCitizens() {
        return citizens;
    }

    @Override
    public Wand getWand(ItemStack itemStack) {
        @SuppressWarnings("deprecation")
        Wand wand = new Wand(this, itemStack);
        return wand;
    }

    @Override
    public com.elmakers.mine.bukkit.api.wand.Wand getWand(ConfigurationSection config) {
        return new Wand(this, config);
    }

    @Override
    public com.elmakers.mine.bukkit.api.wand.Wand createWand(String wandKey) {
        return Wand.createWand(this, wandKey);
    }

    @Override
    public WandTemplate getWandTemplate(String key) {
        if (key == null || key.isEmpty()) return null;
        return wandTemplates.get(key);
    }
    
    @Override
    public Collection<WandTemplate> getWandTemplates() {
        return wandTemplates.values();
    }

    protected ConfigurationSection resolveConfiguration(String key, ConfigurationSection properties, Map<String, ConfigurationSection> configurations) {
        ConfigurationSection configuration = configurations.get(key);
        if (configuration == null) {
            configuration = properties.getConfigurationSection(key);
            if (configuration == null) {
                return null;
            }
            String inherits = configuration.getString("inherit");
            if (inherits != null) {
                ConfigurationSection baseConfiguration = resolveConfiguration(inherits, properties, configurations);
                if (baseConfiguration != null) {
                    ConfigurationSection newConfiguration = new MemoryConfiguration();
                    ConfigurationUtils.addConfigurations(newConfiguration, baseConfiguration);
                    ConfigurationUtils.addConfigurations(newConfiguration, configuration);
                    
                    // Some properties don't inherit, this is kind of hacky.
                    newConfiguration.set("hidden", configuration.get("hidden"));
                    configuration = newConfiguration;
                }
            }
            configurations.put(key, configuration);
        }
        
        return configuration;
    }

    public void loadMageClasses(ConfigurationSection properties) {
        mageClasses.clear();

        Set<String> classKeys = properties.getKeys(false);
        Map<String, ConfigurationSection> templateConfigurations = new HashMap<>();
        for (String key : classKeys)
        {
            loadMageClassTemplate(key, resolveConfiguration(key, properties, templateConfigurations));
        }

        // Resolve parents, we don't check for an inherited "parent" property, so it's important
        // to use the original un-inherited configs for parenting.
        for (String key : classKeys)
        {
            MageClassTemplate template = mageClasses.get(key);
            if (template != null) {
                String parentKey = properties.getConfigurationSection(key).getString("parent");
                if (parentKey != null) {
                    MageClassTemplate parent = mageClasses.get(parentKey);
                    if (parent == null) {
                        getLogger().warning("Class '" + key + "' has unknown parent: " + parentKey);
                    } else {
                        template.setParent(parent);
                    }
                }
            }
        }
    }

    @Override
    public Set<String> getMageClassKeys() {
	    return mageClasses.keySet();
    }

    public MageClassTemplate getMageClass(String key) {
	    return mageClasses.get(key);
    }

    public void loadMageClassTemplate(String key, ConfigurationSection classNode) {
        if (classNode.getBoolean("enabled", true)) {
            mageClasses.put(key, new MageClassTemplate(this, key, classNode));
        }
    }
    
    public void loadWandTemplates(ConfigurationSection properties) {
        wandTemplates.clear();

        Set<String> wandKeys = properties.getKeys(false);
        Map<String, ConfigurationSection> templateConfigurations = new HashMap<>();
        for (String key : wandKeys)
        {
            loadWandTemplate(key, resolveConfiguration(key, properties, templateConfigurations));
        }
    }

    @Override
    public void loadWandTemplate(String key, ConfigurationSection wandNode) {
        if (wandNode.getBoolean("enabled", true)) {
            wandTemplates.put(key, new com.elmakers.mine.bukkit.wand.WandTemplate(this, key, wandNode));
        }
    }
    
    @Override
    public void unloadWandTemplate(String key) {
        wandTemplates.remove(key);
    }

    public Collection<String> getWandTemplateKeys() {
        return wandTemplates.keySet();
    }

    public ConfigurationSection getWandTemplateConfiguration(String key) {
        WandTemplate template = getWandTemplate(key);
        return template == null ? null : template.getConfiguration();
    }
    
	@Override
	public boolean elementalsEnabled() 
	{
		return (elementals != null);
	}

	@Override
	public boolean createElemental(Location location, String templateName, CommandSender creator) 
	{
		return elementals.createElemental(location, templateName, creator);
	}

	@Override
	public boolean isElemental(Entity entity) 
	{
		if (elementals == null || entity.getType() != EntityType.FALLING_BLOCK) return false;
		return elementals.isElemental(entity);
	}

	@Override
	public boolean damageElemental(Entity entity, double damage, int fireTicks, CommandSender attacker) 
	{
		if (elementals == null) return false;
		return elementals.damageElemental(entity, damage, fireTicks, attacker);
	}

	@Override
	public boolean setElementalScale(Entity entity, double scale) 
	{
		if (elementals == null) return false;
		return elementals.setElementalScale(entity, scale);
	}

	@Override
	public double getElementalScale(Entity entity) 
	{
		if (elementals == null) return 0;
		return elementals.getElementalScale(entity);
	}

	@Override
	public com.elmakers.mine.bukkit.api.spell.SpellCategory getCategory(String key) 
	{
        if (key == null || key.isEmpty()) {
            return null;
        }
		SpellCategory category = categories.get(key);
		if (category == null) {
			category = new com.elmakers.mine.bukkit.spell.SpellCategory(key, this);
			categories.put(key, category);
		}
		return category;
	}

	@Override
	public Collection<com.elmakers.mine.bukkit.api.spell.SpellCategory> getCategories()
	{
		List<com.elmakers.mine.bukkit.api.spell.SpellCategory> allCategories = new ArrayList<>();
		allCategories.addAll(categories.values());
		return allCategories;
	}

	@Override
	public Collection<SpellTemplate> getSpellTemplates()
	{
		return getSpellTemplates(false);
	}

    @Override
    public Collection<SpellTemplate> getSpellTemplates(boolean showHidden)
    {
        List<SpellTemplate> allSpells = new ArrayList<>();
        for (SpellTemplate spell : spells.values())
        {
            if (showHidden || !spell.isHidden())
            {
                allSpells.add(spell);
            }
        }
        return allSpells;
    }

    @Override
	public SpellTemplate getSpellTemplate(String name) 
	{
		if (name == null || name.length() == 0) return null;
        SpellTemplate spell = spellAliases.get(name);
        if (spell == null) {
            spell = spells.get(name);
        }
        if (spell == null && name.startsWith("heroes*")) {
            if (heroesManager == null) return null;
            spell = heroesManager.createSkillSpell(this, name.substring(7));
            if (spell != null) {
                spells.put(name, spell);
            }
        }
		return spell;
	}

    @Override
    public String getEntityName(Entity target) {
        return getEntityName(target, false);
    }

    @Override
    public String getEntityDisplayName(Entity target) {
        return getEntityName(target, true);
    }

    protected String getEntityName(Entity target, boolean display) {
        if (target == null)
        {
            return "Unknown";
        }
        if (target instanceof Player)
        {
            return display ? ((Player)target).getDisplayName() : ((Player)target).getName();
        }

        if (isElemental(target))
        {
            return "Elemental";
        }

        if (display) {
            if (target instanceof LivingEntity) {
                LivingEntity li = (LivingEntity) target;
                String customName = li.getCustomName();
                if (customName != null && customName.length() > 0) {
                    return customName;
                }
            } else if (target instanceof Item) {
                Item item = (Item) target;
                ItemStack itemStack = item.getItemStack();
                if (itemStack.hasItemMeta()) {
                    ItemMeta meta = itemStack.getItemMeta();
                    if (meta.hasDisplayName()) {
                        return meta.getDisplayName();
                    }
                }

                MaterialAndData material = new MaterialAndData(itemStack);
                return material.getName();
            }
        }

        return target.getType().name().toLowerCase().replace('_', ' ');
    }

    public boolean getShowCastHoloText() {
        return showCastHoloText;
    }

    public boolean getShowActivateHoloText() {
        return showActivateHoloText;
    }

    public int getCastHoloTextRange() {
        return castHoloTextRange;
    }

    public int getActiveHoloTextRange() {
        return activateHoloTextRange;
    }

    public ItemStack getSpellBook(com.elmakers.mine.bukkit.api.spell.SpellCategory category, int count) {
        Map<String, List<SpellTemplate>> categories = new HashMap<>();
        Collection<SpellTemplate> spellVariants = spells.values();
        String categoryKey = category == null ? null : category.getKey();
        for (SpellTemplate spell : spellVariants)
        {
            if (spell.isHidden() || spell.getSpellKey().isVariant()) continue;
            com.elmakers.mine.bukkit.api.spell.SpellCategory spellCategory = spell.getCategory();
            if (spellCategory == null) continue;

            String spellCategoryKey = spellCategory.getKey();
            if (categoryKey == null || spellCategoryKey.equalsIgnoreCase(categoryKey))
            {
                List<SpellTemplate> categorySpells = categories.get(spellCategoryKey);
                if (categorySpells == null) {
                    categorySpells = new ArrayList<>();
                    categories.put(spellCategoryKey, categorySpells);
                }
                categorySpells.add(spell);
            }
        }

        List<String> categoryKeys = new ArrayList<>(categories.keySet());
        Collections.sort(categoryKeys);

        // Hrm? So much Copy+paste! :(
        CostReducer reducer = null;
        ItemStack bookItem = new ItemStack(Material.WRITTEN_BOOK, count);
        BookMeta book = (BookMeta)bookItem.getItemMeta();
        book.setAuthor(messages.get("books.default.author"));
        String title = null;
        if (category != null) {
            title = messages.get("books.default.title").replace("$category", category.getName());
        } else {
            title = messages.get("books.all.title");
        }
        book.setTitle(title);
        List<String> pages = new ArrayList<>();

        Set<String> paths = WandUpgradePath.getPathKeys();

        for (String key : categoryKeys) {
            category = getCategory(key);
            title = messages.get("books.default.title").replace("$category", category.getName());
            String description = "" + ChatColor.BOLD + ChatColor.BLUE + title + "\n\n";
            description += "" + ChatColor.RESET + ChatColor.DARK_BLUE + category.getDescription();
            pages.add(description);

            List<SpellTemplate> categorySpells = categories.get(key);
            Collections.sort(categorySpells);
            for (SpellTemplate spell : categorySpells) {
                List<String> lines = new ArrayList<>();
                lines.add("" + ChatColor.GOLD + ChatColor.BOLD + spell.getName());
                lines.add("" + ChatColor.RESET);

                String spellDescription = spell.getDescription();
                if (spellDescription != null && spellDescription.length() > 0) {
                    lines.add("" + ChatColor.BLACK + spellDescription);
                    lines.add("");
                }

                String spellCooldownDescription = spell.getCooldownDescription();
                if (spellCooldownDescription != null && spellCooldownDescription.length() > 0) {
                    spellCooldownDescription = messages.get("cooldown.description").replace("$time", spellCooldownDescription);
                    lines.add("" + ChatColor.DARK_PURPLE + spellCooldownDescription);
                }

                String spellMageCooldownDescription = spell.getMageCooldownDescription();
                if (spellMageCooldownDescription != null && spellMageCooldownDescription.length() > 0) {
                    spellMageCooldownDescription = messages.get("cooldown.mage_description").replace("$time", spellMageCooldownDescription);
                    lines.add("" + ChatColor.RED + spellMageCooldownDescription);
                }

                Collection<CastingCost> costs = spell.getCosts();
                if (costs != null) {
                    for (CastingCost cost : costs) {
                        if (!cost.isEmpty(reducer)) {
                            lines.add(ChatColor.DARK_PURPLE + messages.get("wand.costs_description").replace("$description", cost.getFullDescription(messages, reducer)));
                        }
                    }
                }
                Collection<CastingCost> activeCosts = spell.getActiveCosts();
                if (activeCosts != null) {
                    for (CastingCost cost : activeCosts) {
                        if (!cost.isEmpty(reducer)) {
                            lines.add(ChatColor.DARK_PURPLE + messages.get("wand.active_costs_description").replace("$description", cost.getFullDescription(messages, reducer)));
                        }
                    }
                }

                for (String pathKey : paths) {
                    WandUpgradePath checkPath = WandUpgradePath.getPath(pathKey);
                    if (!checkPath.isHidden() && (checkPath.hasSpell(spell.getKey()) || checkPath.hasExtraSpell(spell.getKey()))) {
                        lines.add(ChatColor.DARK_BLUE + messages.get("spell.available_path").replace("$path", checkPath.getName()));
                        break;
                    }
                }

                for (String pathKey : paths) {
                    WandUpgradePath checkPath = WandUpgradePath.getPath(pathKey);
                    if (checkPath.requiresSpell(spell.getKey())) {
                        lines.add(ChatColor.DARK_RED + messages.get("spell.required_path").replace("$path", checkPath.getName()));
                        break;
                    }
                }

                long duration = spell.getDuration();
                if (duration > 0) {
                    long seconds = duration / 1000;
                    if (seconds > 60 * 60) {
                        long hours = seconds / (60 * 60);
                        lines.add(ChatColor.DARK_GREEN + messages.get("duration.lasts_hours").replace("$hours", ((Long) hours).toString()));
                    } else if (seconds > 60) {
                        long minutes = seconds / 60;
                        lines.add(ChatColor.DARK_GREEN + messages.get("duration.lasts_minutes").replace("$minutes", ((Long) minutes).toString()));
                    } else {
                        lines.add(ChatColor.DARK_GREEN + messages.get("duration.lasts_seconds").replace("$seconds", ((Long) seconds).toString()));
                    }
                }
                else if (spell.showUndoable())
                {
                    if (spell.isUndoable()) {
                        String undoable = messages.get("spell.undoable", "");
                        if (undoable != null && !undoable.isEmpty())
                        {
                            lines.add(undoable);
                        }
                    } else {
                        String notUndoable = messages.get("spell.not_undoable", "");
                        if (notUndoable != null && !notUndoable.isEmpty())
                        {
                            lines.add(notUndoable);
                        }
                    }
                }

                if (spell.usesBrush()) {
                    lines.add(ChatColor.DARK_GRAY + messages.get("spell.brush"));
                }

                SpellKey baseKey = spell.getSpellKey();
                SpellKey upgradeKey = new SpellKey(baseKey.getBaseKey(), baseKey.getLevel() + 1);
                SpellTemplate upgradeSpell = getSpellTemplate(upgradeKey.getKey());
                int spellLevels = 0;
                while (upgradeSpell != null) {
                    spellLevels++;
                    upgradeKey = new SpellKey(upgradeKey.getBaseKey(), upgradeKey.getLevel() + 1);
                    upgradeSpell = getSpellTemplate(upgradeKey.getKey());
                }
                if (spellLevels > 0) {
                    spellLevels++;
                    lines.add(ChatColor.DARK_AQUA + messages.get("spell.levels_available").replace("$levels", Integer.toString(spellLevels)));
                }

                String usage = spell.getUsage();
                if (usage != null && usage.length() > 0) {
                    lines.add("" + ChatColor.GRAY + ChatColor.ITALIC + usage + ChatColor.RESET);
                    lines.add("");
                }

                String spellExtendedDescription = spell.getExtendedDescription();
                if (spellExtendedDescription != null && spellExtendedDescription.length() > 0) {
                    lines.add("" + ChatColor.BLACK + spellExtendedDescription);
                    lines.add("");
                }

                pages.add(StringUtils.join(lines, "\n"));
            }
        }

        book.setPages(pages);
        bookItem.setItemMeta(book);
        return bookItem;
    }

    @Override
    public MaterialAndData getRedstoneReplacement() {
        return redstoneReplacement;
    }

    @Override
    public boolean isUrlIconsEnabled() {
        return urlIconsEnabled;
    }

    @Override
    public Set<EntityType> getUndoEntityTypes() {
        return undoEntityTypes;
    }

    @Override
    public String describeItem(ItemStack item) {
        return messages.describeItem(item);
    }

    public boolean checkForItem(Player player, ItemStack requireItem, boolean take) {
        boolean foundItem = false;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (itemsAreEqual(item, requireItem)) {
                Wand wand = null;
                if (Wand.isWand(item) && Wand.isBound(item)) {
                    wand = getWand(item);
                    if (!wand.canUse(player)) continue;
                }
                if (take) {
                    player.getInventory().setItem(i, null);
                    if (wand != null) {
                        wand.unbind();
                    }
                }
                foundItem = true;
                break;
            }
        }

        return foundItem;
    }

    @Override
    public boolean hasItem(Player player, ItemStack requireItem) {
        return checkForItem(player, requireItem, false);
    }

    @Override
    public boolean takeItem(Player player, ItemStack requireItem) {
        return checkForItem(player, requireItem, true);
    }

    @Override
    public String getItemKey(ItemStack item) {
        if (item == null) {
            return "";
        }
        if (Wand.isUpgrade(item)) {
            return "upgrade:" + Wand.getWandTemplate(item);
        }
        if (Wand.isWand(item)) {
            return "wand:" + Wand.getWandTemplate(item);
        }
        if (Wand.isSpell(item)) {
            return "spell:" + Wand.getSpell(item);
        }
        if (Wand.isBrush(item)) {
            return "brush:" + Wand.getBrush(item);
        }
        ItemData mappedItem = getItem(item);
        if (mappedItem != null) {
            return mappedItem.getKey();
        }
        if (item.getType() == Material.SKULL_ITEM) {
            String url = InventoryUtils.getSkullURL(item);
            if (url != null && url.length() > 0) {
                return "skull_item:" + url;
            }
        }

        MaterialAndData material = new MaterialAndData(item);
        return material.getKey();
    }

    @Override
    public ItemStack createItem(String magicItemKey) {
        return createItem(magicItemKey, false);
    }

    @Override
    public ItemStack createItem(String magicItemKey, boolean brief) {
        ItemStack itemStack = null;
        if (magicItemKey == null) {
            return null;
        }

        // Check for amounts
        int amount = 1;
        if (magicItemKey.contains("@")) {
            String[] pieces = StringUtils.split(magicItemKey, '@');
            magicItemKey = pieces[0];
            try {
                amount = Integer.parseInt(pieces[1]);
            } catch (Exception ex) {

            }
        }

        // Handle : or | as delimiter
        magicItemKey = magicItemKey.replace("|", ":");
        try {
            if (magicItemKey.contains("skull:") || magicItemKey.contains("skull_item:")) {
                magicItemKey = magicItemKey.replace("skull:", "skull_item:");
                MaterialAndData skullData = new MaterialAndData(magicItemKey);
                itemStack = skullData.getItemStack(amount);
            } else if (magicItemKey.contains("book:")) {
                String bookCategory = magicItemKey.substring(5);
                com.elmakers.mine.bukkit.api.spell.SpellCategory category = null;

                if (!bookCategory.isEmpty() && !bookCategory.equalsIgnoreCase("all")) {
                    category = getCategory(bookCategory);
                    if (category == null) {
                        return null;
                    }
                }
                itemStack = getSpellBook(category, amount);
            } else if (skillPointItemsEnabled && magicItemKey.contains("sp:")) {
                String spAmount = magicItemKey.substring(3);
                itemStack = InventoryUtils.getURLSkull(skillPointIcon);
                ItemMeta meta = itemStack.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', messages.get("sp.name")).replace("$amount", spAmount));
                String spDescription = messages.get("sp.description");
                if (spDescription.length() > 0)
                {
                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.translateAlternateColorCodes('&', spDescription));
                    meta.setLore(lore);
                }
                itemStack.setItemMeta(meta);
                InventoryUtils.setMeta(itemStack, "sp", spAmount);
            } else if (magicItemKey.contains("spell:")) {
                String spellKey = magicItemKey.substring(6);
                itemStack = createSpellItem(spellKey, brief);
            } else if (magicItemKey.contains("wand:")) {
                String wandKey = magicItemKey.substring(5);
                com.elmakers.mine.bukkit.api.wand.Wand wand = createWand(wandKey);
                if (wand != null) {
                    itemStack = wand.getItem();
                }
            } else if (magicItemKey.contains("upgrade:")) {
                String wandKey = magicItemKey.substring(8);
                com.elmakers.mine.bukkit.api.wand.Wand wand = createWand(wandKey);
                if (wand != null) {
                    wand.makeUpgrade();
                    itemStack = wand.getItem();
                }
            } else if (magicItemKey.contains("brush:")) {
                String brushKey = magicItemKey.substring(6);
                itemStack = createBrushItem(brushKey);
            } else if (magicItemKey.contains("item:")) {
                String itemKey = magicItemKey.substring(5);
                itemStack = createGenericItem(itemKey);
            } else {
                ItemData itemData = items.get(magicItemKey);
                if (itemData != null) {
                    return itemData.getItemStack(amount);
                }
                MaterialAndData item = new MaterialAndData(magicItemKey);
                if (item.isValid()) {
                    return item.getItemStack(amount);
                }
                com.elmakers.mine.bukkit.api.wand.Wand wand = createWand(magicItemKey);
                if (wand != null) {
                    ItemStack wandItem = wand.getItem();
                    if (wandItem != null) {
                        wandItem.setAmount(amount);
                    }
                    return wandItem;
                }
                // Spells may be using the | delimiter for levels
                // I am regretting overloading this delimiter!
                String spellKey = magicItemKey.replace(":", "|");
                itemStack = createSpellItem(spellKey, brief);
                if (itemStack != null) {
                    itemStack.setAmount(amount);
                    return itemStack;
                }
                itemStack = createBrushItem(magicItemKey);
                if (itemStack != null) {
                    itemStack.setAmount(amount);
                }
            }

        } catch (Exception ex) {
            getLogger().log(Level.WARNING, "Error creating item: " + magicItemKey, ex);
        }

        return itemStack;
    }

    @Override
    public ItemStack createGenericItem(String key) {
        ConfigurationSection template = getWandTemplateConfiguration(key);
        if (template == null || !template.contains("icon")) {
            return null;
        }
        MaterialAndData icon = ConfigurationUtils.toMaterialAndData(template.getString("icon"));
        ItemStack item = icon.getItemStack(1);
        ItemMeta meta = item.getItemMeta();
        if (template.contains("name")) {
            meta.setDisplayName(template.getString("name"));
        } else {
            String name = messages.get("wands." + key + ".name");
            if (name != null && !name.isEmpty()) {
                meta.setDisplayName(name);
            }
        }
        List<String> lore = new ArrayList<>();
        if (template.contains("description")) {
            lore.add(template.getString("description"));
        } else {
            String description = messages.get("wands." + key + ".description");
            if (description != null && !description.isEmpty()) {
                lore.add(description);
            }
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public com.elmakers.mine.bukkit.api.wand.Wand createUpgrade(String wandKey) {
        Wand wand = Wand.createWand(this, wandKey);
        if (!wand.isUpgrade()) {
            wand.makeUpgrade();
        }
        return wand;
    }

    @Override
    public ItemStack createSpellItem(String spellKey) {
        return Wand.createSpellItem(spellKey, this, null, true);
    }

    @Override
    public ItemStack createSpellItem(String spellKey, boolean brief) {
        return Wand.createSpellItem(spellKey, this, null, !brief);
    }

    @Override
    public ItemStack createBrushItem(String brushKey) {
        return Wand.createBrushItem(brushKey, this, null, true);
    }

    @Override
    public boolean itemsAreEqual(ItemStack first, ItemStack second) {
        if (first == null || second == null) return false;
        if (first.getType() != second.getType() || first.getDurability() != second.getDurability()) return false;

        boolean firstIsWand = Wand.isWandOrUpgrade(first);
        boolean secondIsWand = Wand.isWandOrUpgrade(second);
        if (firstIsWand || secondIsWand)
        {
            if (!firstIsWand || !secondIsWand) return false;
            Wand firstWand = getWand(InventoryUtils.getCopy(first));
            Wand secondWand = getWand(InventoryUtils.getCopy(second));
            String firstTemplate = firstWand.getTemplateKey();
            String secondTemplate = secondWand.getTemplateKey();
            if (firstTemplate == null || secondTemplate == null) return false;
            return firstTemplate.equalsIgnoreCase(secondTemplate);
        }

        String firstSpellKey = Wand.getSpell(first);
        String secondSpellKey = Wand.getSpell(second);
        if (firstSpellKey != null || secondSpellKey != null)
        {
            if (firstSpellKey == null || secondSpellKey == null) return false;
            return firstSpellKey.equalsIgnoreCase(secondSpellKey);
        }

        String firstBrushKey = Wand.getBrush(first);
        String secondBrushKey = Wand.getBrush(second);
        if (firstBrushKey != null || secondBrushKey != null)
        {
            if (firstBrushKey == null || secondBrushKey == null) return false;
            return firstBrushKey.equalsIgnoreCase(secondBrushKey);
        }

        String firstName = first.hasItemMeta() ? first.getItemMeta().getDisplayName() : null;
        String secondName = second.hasItemMeta() ? second.getItemMeta().getDisplayName() : null;
        return Objects.equals(firstName, secondName);
    }

    @Override
    public Set<String> getWandPathKeys() {
        return WandUpgradePath.getPathKeys();
    }

    @Override
    public com.elmakers.mine.bukkit.api.wand.WandUpgradePath getPath(String key) {
        return WandUpgradePath.getPath(key);
    }

    @Override
    public ItemStack deserialize(ConfigurationSection root, String key)
    {
        ConfigurationSection itemSection = root.getConfigurationSection(key);
        if (itemSection == null) {
            return null;
        }
        // Fix up busted items
        if (itemSection.getInt("amount", 0) == 0) {
            itemSection.set("amount", 1);
        }

        ItemStack item = itemSection.getItemStack("item");
        if (item == null) {
            return null;
        }
        if (itemSection.contains("wand"))
        {
            item = InventoryUtils.makeReal(item);
            Wand.configToItem(itemSection, item);
        }
        else if (itemSection.contains("spell"))
        {
            item = InventoryUtils.makeReal(item);
            InventoryUtils.setMeta(item, "spell", itemSection.getString("spell"));
            if (itemSection.contains("skill")) {
                InventoryUtils.setMeta(item, "skill", "true");
            }
        }
        else if (itemSection.contains("brush"))
        {
            item = InventoryUtils.makeReal(item);
            InventoryUtils.setMeta(item, "brush", itemSection.getString("brush"));
        }
        return item;
    }

    @Override
    public void serialize(ConfigurationSection root, String key, ItemStack item)
    {
        ConfigurationSection itemSection = root.createSection(key);
        itemSection.set("item", item);
        if (Wand.isWandOrUpgrade(item))
        {
            ConfigurationSection stateNode = itemSection.createSection("wand");
            Wand.itemToConfig(item, stateNode);
        }
        else if(Wand.isSpell(item))
        {
            itemSection.set("spell", Wand.getSpell(item));
            if (Wand.isSkill(item)) {
                itemSection.set("skill", "true");
            }
        }
        else if (Wand.isBrush(item))
        {
            itemSection.set("brush", Wand.getBrush(item));
        }
    }

    @Override
    public void disableItemSpawn()
    {
        entityController.setDisableItemSpawn(true);
    }

    @Override
    public void enableItemSpawn()
    {
        entityController.setDisableItemSpawn(false);
    }

    @Override
    public void setForceSpawn(boolean force)
    {
        entityController.setForceSpawn(force);
    }

    public HeroesManager getHeroes() {
        return heroesManager;
    }

    public ManaController getManaController() {
        if (useHeroesMana && heroesManager != null) return heroesManager;
        if (useSkillAPIMana && skillAPIManager != null) return skillAPIManager;
        return null;
    }

    public String getDefaultSkillIcon() {
        return defaultSkillIcon;
    }

    public int getSkillInventoryRows() {
        return skillInventoryRows;
    }

    public boolean usePermissionSkills() {
        return skillsUsePermissions;
    }

    public boolean useHeroesSkills() {
        return skillsUseHeroes;
    }

    @Override
    public void addFlightExemption(Player player, int duration) {
        ncpManager.addFlightExemption(player, duration);
        CompatibilityUtils.addFlightExemption(player, duration * 20 / 1000);
    }

    @Override
    public void addFlightExemption(Player player) {
        ncpManager.addFlightExemption(player);
    }

    @Override
    public void removeFlightExemption(Player player) {
        ncpManager.removeFlightExemption(player);
    }

    public String getExtraSchematicFilePath() {
        return extraSchematicFilePath;
    }

    @Override
    public void warpPlayerToServer(Player player, String server, String warp) {
        Mage apiMage = getMage(player);
        if (apiMage instanceof com.elmakers.mine.bukkit.magic.Mage)
        {
            ((com.elmakers.mine.bukkit.magic.Mage)apiMage).setDestinationWarp(warp);
            info("Cross-server warping " + player.getName() + " to warp " + warp, 1);
        }
        sendPlayerToServer(player, server);
    }

    @Override
    public void sendPlayerToServer(final Player player, final String server) {
        MageDataCallback callback = new MageDataCallback() {
            @Override
            public void run(MageData data) {
                Bukkit.getScheduler().runTaskLater(plugin, new ChangeServerTask(plugin, player, server), 1);
            }
        };
        info("Moving " + player.getName() + " to server " + server, 1);
        Mage mage = getRegisteredMage(player);
        if (mage != null) {
            playerQuit(mage, callback);
        } else {
            callback.run(null);
        }
    }

    @Override
    public boolean spawnPhysicsBlock(Location location, Material material, short data, Vector velocity) {
        if (blockPhysicsManager == null) return false;

        blockPhysicsManager.spawnPhysicsBlock(location, material, data, velocity);
        return true;
    }

    @Override
    public boolean isDisguised(Entity entity) {
        return !libsDisguiseEnabled || libsDisguiseManager == null || entity == null ? false : libsDisguiseManager.isDisguised(entity);
    }
    
    @Override
    public boolean disguise(Entity entity, ConfigurationSection configuration) {
        if (!libsDisguiseEnabled || libsDisguiseManager == null || entity == null) {
            return false;
        }
        return libsDisguiseManager.disguise(entity, configuration);
    }

    @Override
    public boolean isPathUpgradingEnabled() {
        return autoPathUpgradesEnabled;
    }

    @Override
    public boolean isSpellUpgradingEnabled() {
        return autoSpellUpgradesEnabled;
    }

    @Override
    public boolean isSpellProgressionEnabled() {
        return spellProgressionEnabled;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public boolean areLocksProtected() {
        return protectLocked;
    }

    public boolean isContainer(Block block) {
        return block != null && containerMaterials.contains(block.getType());
    }

    /**
     * Checks if an item is a melee material, as specified by the {@code melee}
     * list in {@code materials.yml}. This is primarily used to detect if left
     * clicking an entity should indicate melee damage or a spell being cast.
     *
     * @param item
     *            The item to check.
     * @return Whether or not this is a melee weapon.
     */
    public boolean isMeleeWeapon(ItemStack item) {
        return item != null && meleeMaterials.contains(item.getType());
    }

    public boolean isWearable(ItemStack item) {
        return item != null && wearableMaterials.contains(item.getType());
    }

    public boolean isInteractable(Block block) {
        return block != null && interactibleMaterials.contains(block.getType());
    }

    public boolean isSpellDroppingEnabled() {
        return spellDroppingEnabled;
    }

    @Override
    public boolean isSPEnabled() {
        return spEnabled;
    }

    @Override
    public boolean isSPEarnEnabled() {
        return spEarnEnabled;
    }

    @Override
    public int getSPMaximum() {
        return spMaximum;
    }

    @Override
    public void deleteMage(final String id) {
        Mage mage = getRegisteredMage(id);
        if (mage != null) {
            playerQuit(mage, new MageDataCallback() {
                @Override
                public void run(MageData data) {
                    info("Deleted player id " + id);
                    mageDataStore.delete(id);
                }
            });
        }
    }

    public long getPhysicsTimeout() {
        if (physicsHandler != null) {
            return physicsHandler.getTimeout();
        }

        return 0;
    }

    @Override
    public String getSpell(ItemStack item) {
        return Wand.getSpell(item);
    }

    @Override
    public String getSpellArgs(ItemStack item) {
        return Wand.getSpellArgs(item);
    }

    @Override
    public Set<String> getMobKeys() {
        return mobs.getKeys();
    }

    @Override
    public Entity spawnMob(String key, Location location) {
        EntityData mobType = mobs.get(key);
        if (mobType != null) {
            return mobType.spawn(this, location);
        }
        EntityType entityType = com.elmakers.mine.bukkit.entity.EntityData.parseEntityType(key);
        if (entityType == null) {
            return null;
        }
        return location.getWorld().spawnEntity(location, entityType);
    }

    @Override
    public EntityData getMob(String key) {
        return mobs.get(key);
    }

    @Override
    public EntityData getMobByName(String key) {
        return mobs.getByName(key);
    }

    @Override
    public EntityData loadMob(ConfigurationSection configuration) {
        return new com.elmakers.mine.bukkit.entity.EntityData(this, configuration);
    }

    @Override
    public Set<String> getItemKeys() {
        return items.getKeys();
    }
    
    @Override
    public ItemData getItem(String key) {
        return items.get(key);
    }
    
    @Override
    public ItemData getOrCreateItem(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }
        return items.getOrCreate(key);
    }
    
    @Override
    public ItemData getItem(ItemStack match) {
        return items.get(match);
    }
    
    @Override
    public void unloadItemTemplate(String key) {
        items.remove(key);
    }
    
    @Override
    public void loadItemTemplate(String key, ConfigurationSection configuration) {
        items.loadItem(key, configuration);
    }
    
    @Override
    public Double getWorth(ItemStack item) {
        String spellKey = Wand.getSpell(item);
        if (spellKey != null) {
            SpellTemplate spell = getSpellTemplate(spellKey);
            if (spell != null) {
                return spell.getWorth();
            }
        }
        int amount = item.getAmount();
        item.setAmount(1);
        ItemData configuredItem = items.get(item);
        item.setAmount(amount);
        if (configuredItem == null) {
            return null;
        }

        return configuredItem.getWorth() * amount;
    }

    public boolean isInventoryBackupEnabled() {
        return backupInventories;
    }

    @Override
    public String getBlockSkin(Material blockType) {
        String skinName = null;
        switch (blockType) {
            case CACTUS:
                skinName = "MHF_Cactus";
                break;
            case CHEST:
                skinName = "MHF_Chest";
                break;
            case MELON_BLOCK:
                skinName = "MHF_Melon";
                break;
            case TNT:
                if (random.nextDouble() > 0.5) {
                    skinName = "MHF_TNT";
                } else {
                    skinName = "MHF_TNT2";
                }
                break;
            case LOG:
                skinName = "MHF_OakLog";
                break;
            case PUMPKIN:
                skinName = "MHF_Pumpkin";
                break;
            default:
                // TODO .. ?
            /*
             * Blocks:
                Bonus:
                MHF_ArrowUp
                MHF_ArrowDown
                MHF_ArrowLeft
                MHF_ArrowRight
                MHF_Exclamation
                MHF_Question
             */
        }

        return skinName;
    }

    @Override
    public boolean sendResourcePackToAllPlayers(CommandSender sender) {
        if (resourcePack == null || resourcePackHash == null) {
            if (sender != null) {
                sender.sendMessage(ChatColor.RED + "No RP set or RP already set in server.properties, not sending.");
            }
            return false;
        }
        int sent = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendResourcePack(player);
            sent++;
        }
        if (sender != null) {
            sender.sendMessage(ChatColor.AQUA + "Sent current RP to " + sent + " players");
        }

        return true;
    }

    @Override
    public boolean sendResourcePack(final Player player) {
        if (resourcePack == null || resourcePackHash == null) {
            return false;
        }
        String message = messages.get("resource_pack.sending");
        if (message != null && !message.isEmpty()) {
            player.sendMessage(message);
        }

        // Give them 2 seconds to read the message
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                CompatibilityUtils.setResourcePack(player, resourcePack, resourcePackHash);
            }
        }, resourcePackDelay * 20 / 1000);

        return true;
    }

    @Override
    public void checkResourcePack(CommandSender sender) {
        checkResourcePack(sender, false);
    }

    public boolean checkResourcePack(final CommandSender sender, final boolean quiet) {
        final Server server = plugin.getServer();
        resourcePack = null;
        resourcePackHash = null;
        final boolean initialLoad = !checkedResourcePack;
        String serverResourcePack = CompatibilityUtils.getResourcePack(server);
        if (serverResourcePack != null) serverResourcePack = serverResourcePack.trim();
        
        if (serverResourcePack != null && !serverResourcePack.isEmpty()) {
            if (!quiet) sender.sendMessage("Resource pack configured in server.properties, Magic not using RP from config.yml");
            return false;
        }
        if (defaultResourcePack == null || defaultResourcePack.isEmpty()) {
            if (!quiet) sender.sendMessage("Resource pack in config.yml has been cleared- Magic skipping RP check");
            return false;
        }
        resourcePack = defaultResourcePack;

        checkedResourcePack = true;
        if (!quiet) sender.sendMessage("Magic checking resource pack for updates: " + ChatColor.GRAY + resourcePack);
        
        long modifiedTime = 0;
        String currentSHA = null;
        final YamlConfiguration rpConfig = new YamlConfiguration();
        final File rpFile = new File(plugin.getDataFolder(), "data/" + RP_FILE + ".yml");
        final String rpKey = resourcePack.replace(".", "_");
        if (rpFile.exists()) {
            try {
                rpConfig.load(rpFile);
                ConfigurationSection rpSection = rpConfig.getConfigurationSection(rpKey);
                if (rpSection != null) {
                    currentSHA = rpSection.getString("sha1");
                    modifiedTime = rpSection.getLong("modified");

                    // Ignore old encoding, we will need to update
                    if (currentSHA != null && currentSHA.length() < 40 ) {
                        resourcePackHash = BaseEncoding.base64().decode(currentSHA);
                    }
                }
            } catch (Exception ex) {
            }
        }

        final String finalResourcePack = resourcePack;
        final long modifiedTimestamp = modifiedTime;
        final String currentHash = currentSHA;
        server.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                String response;
                String newResourcePackHash = currentHash;
                try {
                    HttpURLConnection.setFollowRedirects(false);
                    URL rpURL = new URL(finalResourcePack);
                    HttpURLConnection connection = (HttpURLConnection)rpURL.openConnection();
                    connection.setInstanceFollowRedirects(false);
                    connection.setRequestMethod("HEAD");
                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
                    {
                        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
                        final String lastModified = connection.getHeaderField("Last-Modified");
                        if (lastModified == null || lastModified.isEmpty()) {
                            response = ChatColor.YELLOW + "Server did not return a Last-Modified field, cancelling checks until restart";
                            cancelResourcePackChecks();
                        } else {
                            Date tryParseDate;
                            try {
                                tryParseDate = format.parse(lastModified);
                            } catch(ParseException dateFormat) {
                                cancelResourcePackChecks();
                                server.getScheduler().runTask(plugin, new Runnable() {
                                    @Override
                                    public void run() {
                                        sender.sendMessage("Error parsing resource pack modified time, cancelling checks until restart: " + lastModified);
                                    }
                                });
                                return;
                            }
                            final Date modifiedDate = tryParseDate;
                            if (modifiedDate.getTime() > modifiedTimestamp || resourcePackHash == null) {
                                final boolean isUnset = (resourcePackHash == null);
                                server.getScheduler().runTask(plugin, new Runnable() {
                                    @Override
                                    public void run() {
                                        if (modifiedTimestamp <= 0) {
                                            if (!quiet) sender.sendMessage(ChatColor.YELLOW + "Checking resource pack for the first time");
                                        } else if (isUnset) {
                                            sender.sendMessage(ChatColor.YELLOW + "Resource pack hash format changed, downloading for one-time update");
                                        } else {
                                            sender.sendMessage(ChatColor.YELLOW + "Resource pack modified, redownloading (" + modifiedDate.getTime() + " > " + modifiedTimestamp + ")");
                                        }
                                    }
                                });

                                MessageDigest digest = MessageDigest.getInstance("SHA1");
                                try(BufferedInputStream in = new BufferedInputStream(rpURL.openStream())) {
                                    final byte data[] = new byte[1024];
                                    int count;
                                    while ((count = in.read(data, 0, 1024)) != -1) {
                                        digest.update(data, 0, count);
                                    }
                                }
                                resourcePackHash = digest.digest();
                                newResourcePackHash = BaseEncoding.base64().encode(resourcePackHash);

                                if (initialLoad) {
                                    response = ChatColor.GREEN + "Resource pack hash set to " + ChatColor.GRAY + newResourcePackHash;
                                } else {
                                    response = ChatColor.YELLOW + "Resource pack hash changed, use " + ChatColor.AQUA + "/magic rpsend" + ChatColor.YELLOW + " to update connected players";
                                }

                                ConfigurationSection rpSection = rpConfig.createSection(rpKey);

                                rpSection.set("sha1", newResourcePackHash);
                                rpSection.set("modified", modifiedDate.getTime());
                                rpConfig.save(rpFile);
                            } else {
                                response = ChatColor.GREEN + "Resource pack has not changed, using hash " + newResourcePackHash +  " (" + modifiedDate.getTime() + " <= " + modifiedTimestamp + ")";
                            }
                        }
                    }
                    else
                    {
                        response = ChatColor.RED + "Could not find resource pack at: " + ChatColor.DARK_RED + finalResourcePack;
                        cancelResourcePackChecks();
                    }
                }
                catch (Exception e) {
                    cancelResourcePackChecks();
                    response = ChatColor.RED + "An unexpected error occurred while checking your resource pack, cancelling checks until restart (see logs): " + ChatColor.DARK_RED + finalResourcePack;
                    e.printStackTrace();
                }

                if (!quiet) {
                    final String finalResponse = response;
                    server.getScheduler().runTask(plugin, new Runnable() {
                        @Override
                        public void run() {
                        sender.sendMessage(finalResponse);
                        }
                    });
                }
            }
        });
        return true;
    }

    @Override
    public String getMobSkin(EntityType mobType)
    {
        String mobSkin = null;
        switch (mobType) {
            case BLAZE:
                mobSkin = "MHF_Blaze";
                break;
            case CAVE_SPIDER:
                mobSkin = "MHF_CaveSpider";
                break;
            case CHICKEN:
                mobSkin = "MHF_Chicken";
                break;
            case COW:
                mobSkin = "MHF_Cow";
                break;
            case ENDERMAN:
                mobSkin = "MHF_Enderman";
                break;
            case GHAST:
                mobSkin = "MHF_Ghast";
                break;
            case IRON_GOLEM:
                mobSkin = "MHF_Golem";
                break;
            case MAGMA_CUBE:
                mobSkin = "MHF_LavaSlime";
                break;
            case MUSHROOM_COW:
                mobSkin = "MHF_MushroomCow";
                break;
            case OCELOT:
                mobSkin = "MHF_Ocelot";
                break;
            case PIG:
                mobSkin = "MHF_Pig";
                break;
            case PIG_ZOMBIE:
                mobSkin = "MHF_PigZombie";
                break;
            case SHEEP:
                mobSkin = "MHF_Sheep";
                break;
            case SLIME:
                mobSkin = "MHF_Slime";
                break;
            case SPIDER:
                mobSkin = "MHF_Spider";
                break;
            case SQUID:
                mobSkin = "MHF_Squid";
                break;
            case VILLAGER:
                mobSkin = "MHF_Villager";
                break;
            case WOLF:
                mobSkin = "MHF_Wolf";
                break;
            case CREEPER:
                mobSkin = "MHF_Creeper";
                break;
            case ZOMBIE:
                mobSkin = "MHF_Zombie";
                break;
            case SKELETON:
                mobSkin = "MHF_Skeleton";
                break;
            case GUARDIAN:
                mobSkin = "MHF_Guardian";
                break;
            case WITCH:
                mobSkin = "MHF_Witch";
                break;
            default:
        }

        return mobSkin;
    }

    @Override
    public void managePlayerData(boolean external, boolean backupInventories) {
        savePlayerData = !external;
        externalPlayerData = external;
        this.backupInventories = backupInventories;
    }
    
    public void initializeWorldGuardFlags() {
        worldGuardManager.initializeFlags(plugin);
    }

    @Override
    public Object getWandProperty(ItemStack item, String key) {
        Preconditions.checkNotNull(key, "key");
        if (InventoryUtils.isEmpty(item)) return null;
        Object wandNode = InventoryUtils.getNode(item, Wand.WAND_KEY);
        if (wandNode == null) return null;
        Object value = InventoryUtils.getMetaObject(wandNode, key);
        if (value == null) {
            WandTemplate template = getWandTemplate(InventoryUtils.getMetaString(wandNode, "template"));
            if (template != null) {
                value = template.getProperty(key);
            }
        }

        return value;
    }

    @Override
    public String getDefaultWandTemplate() {
        return Wand.DEFAULT_WAND_TEMPLATE;
    }

    @Override
    public <T> T getWandProperty(ItemStack item, String key, T defaultValue) {
        Preconditions.checkNotNull(key, "key");
        Preconditions.checkNotNull(defaultValue, "defaultValue");

        if (InventoryUtils.isEmpty(item)) {
            return defaultValue;
        }

        Object wandNode = InventoryUtils.getNode(item, Wand.WAND_KEY);
        if (wandNode == null) {
            return defaultValue;
        }

        // Obtain the type via the default value.
        // (This is unchecked because of type erasure)
        @SuppressWarnings("unchecked")
        Class<? extends T> clazz = (Class<? extends T>) defaultValue.getClass();

        // Value directly stored on wand
        Object value = InventoryUtils.getMetaObject(wandNode, key);
        if (value != null) {
            if (clazz.isInstance(value)) {
                return clazz.cast(value);
            }

            return defaultValue;
        }

        String tplName = InventoryUtils.getMetaString(wandNode, "template");
        WandTemplate template = getWandTemplate(tplName);
        if (template != null) {
            return template.getProperty(key, defaultValue);
        }

        return defaultValue;
    }

    public boolean useHeroesMana() {
        return useHeroesMana;
    }

    public boolean useSkillAPIMana() {
        return useSkillAPIMana;
    }

    public @Nonnull MageIdentifier getMageIdentifier() {
        return mageIdentifier;
    }

    public void setMageIdentifier(@Nonnull MageIdentifier mageIdentifier) {
        Preconditions.checkNotNull(mageIdentifier, "mageIdentifier");
        this.mageIdentifier = mageIdentifier;
    }

    @Override
    public String getHeroesSkillPrefix() {
        return heroesSkillPrefix;
    }

    public boolean skillPointItemsEnabled() {
        return skillPointItemsEnabled;
    }

    @Override
    public Map<String, Integer> getAttributes(Player player) {
        if (skillAPIManager == null) return null;
        return skillAPIManager.getAttributes(player);
    }

    /*
	 * Private data
	 */
    private static final String                 BUILTIN_SPELL_CLASSPATH = "com.elmakers.mine.bukkit.spell.builtin";

    private final String                        SPELLS_FILE                 = "spells";
    private final String                        CONFIG_FILE             	= "config";
    private final String                        WANDS_FILE             		= "wands";
    private final String                        ENCHANTING_FILE             = "enchanting";
    private final String                        PATHS_FILE                  = "paths";
    private final String                        CRAFTING_FILE             	= "crafting";
    private final String                        CLASSES_FILE             	= "classes";
    private final String                        MESSAGES_FILE             	= "messages";
    private final String                        MATERIALS_FILE             	= "materials";
    private final String                        MOBS_FILE             	    = "mobs";
    private final String                        ITEMS_FILE             	    = "items";
    private final String                        RP_FILE             	    = "resourcepack";
    
    private final String						LOST_WANDS_FILE				= "lostwands";
    private final String						SPELLS_DATA_FILE			= "spells";
    private final String						URL_MAPS_FILE				= "imagemaps";

    private boolean                             disableDefaultSpells        = false;
    private boolean                             disableDefaultWands         = false;
    private boolean 							loadDefaultSpells			= true;
    private boolean 							loadDefaultWands			= true;
    private boolean                             loadDefaultPaths            = true;
    private boolean 							loadDefaultCrafting			= true;
    private boolean                             loadDefaultClasses          = true;
    private boolean 							loadDefaultMobs 			= true;
    private boolean 							loadDefaultItems 			= true;

    private MaterialAndData                     redstoneReplacement             = new MaterialAndData(Material.OBSIDIAN);
    private Set<Material>                       buildingMaterials               = new HashSet<>();
    private Set<Material>                       indestructibleMaterials         = new HashSet<>();
    private Set<Material>                       restrictedMaterials	 	        = new HashSet<>();
    private Set<Material>                       destructibleMaterials           = new HashSet<>();
    private Set<Material>                       interactibleMaterials           = new HashSet<>();
    private Set<Material>                       containerMaterials              = new HashSet<>();
    private Set<Material>                       wearableMaterials               = new HashSet<>();
    private Set<Material>                       meleeMaterials                  = new HashSet<>();
    private Map<String, Set<Material>>		    materialSets				    = new HashMap<>();

    private boolean                             backupInventories               = true;
    private int								    undoTimeWindow				    = 6000;
    private int                                 undoQueueDepth                  = 256;
    private int								    pendingQueueDepth				= 16;
    private int                                 undoMaxPersistSize              = 0;
    private boolean                             commitOnQuit             		= false;
    private boolean                             saveNonPlayerMages              = false;
    private String                              defaultWandPath                 = "";
    private WandMode                            defaultWandMode				    = WandMode.NONE;
    private WandMode							defaultBrushMode				= WandMode.CHEST;
    private boolean                             showMessages                    = true;
    private boolean                             showCastMessages                = false;
    private String								messagePrefix					= "";
    private String								castMessagePrefix				= "";
    private boolean                             soundsEnabled                   = true;
    private String								welcomeWand					    = "";
    private int								    messageThrottle				    = 0;
    private boolean							    spellDroppingEnabled			= false;
    private boolean                             fillingEnabled                  = false;
    private int                                 maxFillLevel                    = 0;
    private boolean							    essentialsSignsEnabled			= false;
    private boolean							    dynmapUpdate					= true;
    private boolean							    dynmapShowWands				    = true;
    private boolean							    dynmapOnlyPlayerSpells	        = false;
    private boolean							    dynmapShowSpells				= true;
    private boolean							    createWorldsEnabled			    = true;
    private float							    maxDamagePowerMultiplier	    = 2.0f;
    private float								maxConstructionPowerMultiplier  = 5.0f;
    private float								maxRadiusPowerMultiplier 		= 2.5f;
    private float								maxRadiusPowerMultiplierMax     = 4.0f;
    private float								maxRangePowerMultiplier 		= 3.0f;
    private float								maxRangePowerMultiplierMax 	    = 5.0f;

    private float								maxPower						= 100.0f;
    private float								maxDamageReduction 			    = 0.2f;
    private float								maxDamageReductionExplosions 	= 0.2f;
    private float								maxDamageReductionFalling   	= 0.2f;
    private float								maxDamageReductionFire 	        = 0.2f;
    private float								maxDamageReductionPhysical 	    = 0.2f;
    private float								maxDamageReductionProjectiles 	= 0.2f;
    private float								maxCostReduction 	            = 0.5f;
    private float								maxCooldownReduction        	= 0.5f;
    private int								    maxMana        	                = 1000;
    private int								    maxManaRegeneration        	    = 100;
    private double                              worthBase                       = 1;
    private double                              worthSkillPoints                = 1;
    private String                              skillPointIcon                  = null;
    private boolean                             skillPointItemsEnabled          = true;
    private double                              worthXP                         = 1;
    private CurrencyItem                        currencyItem                    = null;
    private boolean                             spEnabled                       = true;
    private boolean                             spEarnEnabled                   = true;
    private int                                 spMaximum                       = 0;

    private float							 	castCommandCostReduction	    = 1.0f;
    private float							 	castCommandCooldownReduction	= 1.0f;
    private float								castCommandPowerMultiplier      = 0.0f;
    private float							 	castConsoleCostReduction	    = 1.0f;
    private float							 	castConsoleCooldownReduction	= 1.0f;
    private float								castConsolePowerMultiplier      = 0.0f;
    private float							 	costReduction	    			= 0.0f;
    private float							 	cooldownReduction				= 0.0f;
    private int								    autoUndo						= 0;
    private int								    autoSaveTaskId					= 0;
    private boolean                             savePlayerData                  = true;
    private boolean                             externalPlayerData              = false;
    private boolean                             asynchronousSaving              = true;
    private WarpController						warpController					= null;

    private final Map<String, WandTemplate>     wandTemplates               = new HashMap<>();
    private final Map<String, MageClassTemplate> mageClasses                = new HashMap<>();
    private final Map<String, SpellTemplate>    spells              		= new HashMap<>();
    private final Map<String, SpellTemplate>    spellAliases                = new HashMap<>();
    private final Map<String, SpellData>        templateDataMap             = new HashMap<>();
    private final Map<String, SpellCategory>    categories              	= new HashMap<>();
    private final Map<String, ConfigurationSection> spellConfigurations     = new HashMap<>();
    private final Map<String, ConfigurationSection> baseSpellConfigurations = new HashMap<>();
    private final Map<String, com.elmakers.mine.bukkit.magic.Mage> mages    = Maps.newConcurrentMap();
    private final Set<Mage>		 	            pendingConstruction			= new HashSet<>();
    private final PriorityQueue<UndoList>       scheduledUndo               = new PriorityQueue<>();
    private final Map<String, WeakReference<Schematic>> schematics	= new HashMap<>();

    private MageDataStore                       mageDataStore               = null;

    private MagicPlugin                         plugin                      = null;
    private final File							configFolder;
    private final File							dataFolder;
    private final File							defaultsFolder;

    private int								    toggleMessageRange			= 1024;

    private int                                 workFrequency               = 1;
    private int                                 undoFrequency               = 10;
    private int								    workPerUpdate				= 5000;
    private int                                 logVerbosity                = 0;

    private boolean                             showCastHoloText            = false;
    private boolean                             showActivateHoloText        = false;
    private int                                 castHoloTextRange           = 0;
    private int                                 activateHoloTextRange       = 0;
    private boolean							    urlIconsEnabled             = true;
    private boolean                             spellUpgradesEnabled        = true;
    private boolean                             autoSpellUpgradesEnabled    = true;
    private boolean                             autoPathUpgradesEnabled     = true;
    private boolean                             spellProgressionEnabled     = true;

    private boolean							    bypassBuildPermissions      = false;
    private boolean							    bypassBreakPermissions      = false;
    private boolean							    bypassPvpPermissions        = false;
    private boolean							    bypassFriendlyFire          = false;
    private boolean							    allPvpRestricted            = false;
    private boolean							    noPvpRestricted             = false;
    private boolean                             useScoreboardTeams          = false;
    private boolean                             defaultFriendly             = true;
    private boolean							    protectLocked               = true;
    private boolean                             bindOnGive                  = false;

    private String								extraSchematicFilePath		= null;
    private Mailer								mailer						= null;
    private Material							defaultMaterial				= Material.DIRT;
    private Set<EntityType>                     undoEntityTypes             = new HashSet<>();

    private PhysicsHandler						physicsHandler				= null;

    private Map<String, LostWand>				lostWands					= new HashMap<>();
    private Map<String, Set<String>>		 	lostWandChunks				= new HashMap<>();

    private int								    metricsLevel				= 5;
    private Metrics							    metrics						= null;
    private boolean							    hasDynmap					= false;
    private boolean							    hasEssentials				= false;
    private boolean							    hasCommandBook				= false;

    private String                              exampleDefaults             = null;
    private Collection<String>                  addExamples                 = null;
    private boolean                             initialized                 = false;
    private boolean                             loaded                      = false;
    private String                              defaultSkillIcon            = "stick";
    private int                                 skillInventoryRows          = 6;
    private boolean                             skillsUseHeroes             = true;
    private boolean                             useHeroesMana               = true;
    private boolean                             useHeroesParties            = true;
    private boolean                             skillsUsePermissions        = false;
    private String                              heroesSkillPrefix           = "";

    // Synchronization
    private final Object                        saveLock                    = new Object();

    protected static Random                     random                      = new Random();

    // Sub-Controllers
    private CraftingController					crafting					= null;
    private MobController                       mobs    					= null;
    private ItemController                      items    					= null;
    private EnchantingController				enchanting					= null;
    private AnvilController					    anvil						= null;
    private Messages                            messages                    = null;
    private MapController                       maps                        = null;
    private DynmapController					dynmap						= null;
    private ElementalsController				elementals					= null;
    private CitizensController                  citizens					= null;
    private BlockController                     blockController             = null;
    private HangingController                   hangingController           = null;
    private PlayerController                    playerController            = null;
    private EntityController                    entityController            = null;
    private InventoryController                 inventoryController         = null;
    private ExplosionController                 explosionController         = null;
    private MageIdentifier                      mageIdentifier              = new MageIdentifier();
    private boolean                             citizensEnabled			    = true;
    private boolean                             libsDisguiseEnabled			= true;
    private boolean                             skillAPIEnabled			    = true;
    private boolean                             useSkillAPIMana             = false;
    private boolean                             enableResourcePackCheck     = true;
    private int                                 resourcePackCheckInterval   = 0;
    private int                                 resourcePackCheckTimer      = 0;
    private String                              defaultResourcePack         = null;
    private boolean                             checkedResourcePack         = false;
    private String                              resourcePack                = null;
    private byte[]                              resourcePackHash            = null;
    private boolean                             saveDefaultConfigs          = true;
    private long                                resourcePackDelay           = 0;

    private FactionsManager					    factionsManager				= new FactionsManager();
    private LocketteManager                     locketteManager				= new LocketteManager();
    private WorldGuardManager					worldGuardManager			= new WorldGuardManager();
    private PvPManagerManager                   pvpManager                  = new PvPManagerManager();
    private MultiverseManager                   multiverseManager           = new MultiverseManager();
    private PreciousStonesManager				preciousStonesManager		= new PreciousStonesManager();
    private TownyManager						townyManager				= new TownyManager();
    private GriefPreventionManager              griefPreventionManager		= new GriefPreventionManager();
    private NCPManager                          ncpManager       		    = new NCPManager();
    private HeroesManager                       heroesManager       		= null;
    private BlockPhysicsManager                 blockPhysicsManager         = null;
    private boolean                             useBlockPhysics             = true;
    private LibsDisguiseManager                 libsDisguiseManager         = null;
    private SkillAPIManager                     skillAPIManager             = null;

    private List<BlockBreakManager>             blockBreakManagers          = new ArrayList<>();
    private List<BlockBuildManager>             blockBuildManagers          = new ArrayList<>();
    private List<PVPManager>                    pvpManagers                 = new ArrayList<>();
}
