package com.elmakers.mine.bukkit.magic;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.bstats.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitTask;

import com.elmakers.mine.bukkit.action.ActionHandler;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.attributes.AttributeProvider;
import com.elmakers.mine.bukkit.api.block.BoundingBox;
import com.elmakers.mine.bukkit.api.block.CurrencyItem;
import com.elmakers.mine.bukkit.api.block.Schematic;
import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.data.MageData;
import com.elmakers.mine.bukkit.api.data.MageDataCallback;
import com.elmakers.mine.bukkit.api.data.MageDataStore;
import com.elmakers.mine.bukkit.api.data.SpellData;
import com.elmakers.mine.bukkit.api.economy.Currency;
import com.elmakers.mine.bukkit.api.effect.EffectContext;
import com.elmakers.mine.bukkit.api.effect.EffectPlayer;
import com.elmakers.mine.bukkit.api.entity.EntityData;
import com.elmakers.mine.bukkit.api.entity.TeamProvider;
import com.elmakers.mine.bukkit.api.event.LoadEvent;
import com.elmakers.mine.bukkit.api.event.PreLoadEvent;
import com.elmakers.mine.bukkit.api.event.SaveEvent;
import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.item.ItemUpdatedCallback;
import com.elmakers.mine.bukkit.api.magic.CastSourceLocation;
import com.elmakers.mine.bukkit.api.magic.DeathLocation;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.magic.MagicAttribute;
import com.elmakers.mine.bukkit.api.magic.MagicProvider;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.api.magic.MaterialSetManager;
import com.elmakers.mine.bukkit.api.protection.BlockBreakManager;
import com.elmakers.mine.bukkit.api.protection.BlockBuildManager;
import com.elmakers.mine.bukkit.api.protection.CastPermissionManager;
import com.elmakers.mine.bukkit.api.protection.EntityTargetingManager;
import com.elmakers.mine.bukkit.api.protection.PVPManager;
import com.elmakers.mine.bukkit.api.protection.PlayerWarp;
import com.elmakers.mine.bukkit.api.protection.PlayerWarpManager;
import com.elmakers.mine.bukkit.api.protection.PlayerWarpProvider;
import com.elmakers.mine.bukkit.api.requirements.Requirement;
import com.elmakers.mine.bukkit.api.requirements.RequirementsProcessor;
import com.elmakers.mine.bukkit.api.requirements.RequirementsProvider;
import com.elmakers.mine.bukkit.api.spell.CastingCost;
import com.elmakers.mine.bukkit.api.spell.MageSpell;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellKey;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.automata.Automaton;
import com.elmakers.mine.bukkit.automata.AutomatonTemplate;
import com.elmakers.mine.bukkit.block.BlockData;
import com.elmakers.mine.bukkit.block.DefaultMaterials;
import com.elmakers.mine.bukkit.block.LegacySchematic;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.block.MaterialBrush;
import com.elmakers.mine.bukkit.citizens.CitizensController;
import com.elmakers.mine.bukkit.configuration.MageParameters;
import com.elmakers.mine.bukkit.data.YamlDataFile;
import com.elmakers.mine.bukkit.dynmap.DynmapController;
import com.elmakers.mine.bukkit.economy.BaseMagicCurrency;
import com.elmakers.mine.bukkit.economy.CustomCurrency;
import com.elmakers.mine.bukkit.economy.ExperienceCurrency;
import com.elmakers.mine.bukkit.economy.HealthCurrency;
import com.elmakers.mine.bukkit.economy.HungerCurrency;
import com.elmakers.mine.bukkit.economy.ItemCurrency;
import com.elmakers.mine.bukkit.economy.LevelCurrency;
import com.elmakers.mine.bukkit.economy.ManaCurrency;
import com.elmakers.mine.bukkit.economy.SpellPointCurrency;
import com.elmakers.mine.bukkit.economy.VaultCurrency;
import com.elmakers.mine.bukkit.elementals.ElementalsController;
import com.elmakers.mine.bukkit.entity.PermissionsTeamProvider;
import com.elmakers.mine.bukkit.entity.ScoreboardTeamProvider;
import com.elmakers.mine.bukkit.essentials.EssentialsController;
import com.elmakers.mine.bukkit.essentials.Mailer;
import com.elmakers.mine.bukkit.heroes.HeroesManager;
import com.elmakers.mine.bukkit.integration.BattleArenaManager;
import com.elmakers.mine.bukkit.integration.GenericMetadataNPCSupplier;
import com.elmakers.mine.bukkit.integration.LibsDisguiseManager;
import com.elmakers.mine.bukkit.integration.LightAPIManager;
import com.elmakers.mine.bukkit.integration.LogBlockManager;
import com.elmakers.mine.bukkit.integration.NPCSupplierSet;
import com.elmakers.mine.bukkit.integration.PlaceholderAPIManager;
import com.elmakers.mine.bukkit.integration.SkillAPIManager;
import com.elmakers.mine.bukkit.integration.SkriptManager;
import com.elmakers.mine.bukkit.integration.VaultController;
import com.elmakers.mine.bukkit.integration.mobarena.MobArenaManager;
import com.elmakers.mine.bukkit.magic.command.MagicTabExecutor;
import com.elmakers.mine.bukkit.magic.command.WandCommandExecutor;
import com.elmakers.mine.bukkit.magic.listener.AnvilController;
import com.elmakers.mine.bukkit.magic.listener.BlockController;
import com.elmakers.mine.bukkit.magic.listener.CraftingController;
import com.elmakers.mine.bukkit.magic.listener.EnchantingController;
import com.elmakers.mine.bukkit.magic.listener.EntityController;
import com.elmakers.mine.bukkit.magic.listener.ErrorNotifier;
import com.elmakers.mine.bukkit.magic.listener.ExplosionController;
import com.elmakers.mine.bukkit.magic.listener.HangingController;
import com.elmakers.mine.bukkit.magic.listener.InventoryController;
import com.elmakers.mine.bukkit.magic.listener.ItemController;
import com.elmakers.mine.bukkit.magic.listener.JumpController;
import com.elmakers.mine.bukkit.magic.listener.MinigamesListener;
import com.elmakers.mine.bukkit.magic.listener.MobController;
import com.elmakers.mine.bukkit.magic.listener.MobController2;
import com.elmakers.mine.bukkit.magic.listener.PlayerController;
import com.elmakers.mine.bukkit.magic.listener.WildStackerListener;
import com.elmakers.mine.bukkit.maps.MapController;
import com.elmakers.mine.bukkit.materials.MaterialSets;
import com.elmakers.mine.bukkit.materials.SimpleMaterialSetManager;
import com.elmakers.mine.bukkit.npc.MagicNPC;
import com.elmakers.mine.bukkit.protection.AJParkourManager;
import com.elmakers.mine.bukkit.protection.CitadelManager;
import com.elmakers.mine.bukkit.protection.DeadSoulsManager;
import com.elmakers.mine.bukkit.protection.FactionsManager;
import com.elmakers.mine.bukkit.protection.GriefPreventionManager;
import com.elmakers.mine.bukkit.protection.LocketteManager;
import com.elmakers.mine.bukkit.protection.MultiverseManager;
import com.elmakers.mine.bukkit.protection.NCPManager;
import com.elmakers.mine.bukkit.protection.PreciousStonesManager;
import com.elmakers.mine.bukkit.protection.ProtectionManager;
import com.elmakers.mine.bukkit.protection.PvPManagerManager;
import com.elmakers.mine.bukkit.protection.RedProtectManager;
import com.elmakers.mine.bukkit.protection.ResidenceManager;
import com.elmakers.mine.bukkit.protection.TownyManager;
import com.elmakers.mine.bukkit.protection.WorldGuardManager;
import com.elmakers.mine.bukkit.requirements.RequirementsController;
import com.elmakers.mine.bukkit.resourcepack.ResourcePackManager;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.spell.SpellCategory;
import com.elmakers.mine.bukkit.tasks.ArmorUpdatedTask;
import com.elmakers.mine.bukkit.tasks.AutoSaveTask;
import com.elmakers.mine.bukkit.tasks.AutomataUpdateTask;
import com.elmakers.mine.bukkit.tasks.BatchUpdateTask;
import com.elmakers.mine.bukkit.tasks.ChangeServerTask;
import com.elmakers.mine.bukkit.tasks.ConfigCheckTask;
import com.elmakers.mine.bukkit.tasks.ConfigurationLoadTask;
import com.elmakers.mine.bukkit.tasks.DoMageLoadTask;
import com.elmakers.mine.bukkit.tasks.EssentialsItemIntegrationTask;
import com.elmakers.mine.bukkit.tasks.FinishGenericIntegrationTask;
import com.elmakers.mine.bukkit.tasks.LoadDataTask;
import com.elmakers.mine.bukkit.tasks.LogNotifyTask;
import com.elmakers.mine.bukkit.tasks.LogWatchdogTask;
import com.elmakers.mine.bukkit.tasks.MageQuitTask;
import com.elmakers.mine.bukkit.tasks.MageUpdateTask;
import com.elmakers.mine.bukkit.tasks.MigrateDataTask;
import com.elmakers.mine.bukkit.tasks.MigrationTask;
import com.elmakers.mine.bukkit.tasks.SaveDataTask;
import com.elmakers.mine.bukkit.tasks.SaveMageDataTask;
import com.elmakers.mine.bukkit.tasks.SaveMageTask;
import com.elmakers.mine.bukkit.tasks.UndoUpdateTask;
import com.elmakers.mine.bukkit.tasks.ValidateSpellsTask;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.EntityMetadataUtils;
import com.elmakers.mine.bukkit.utility.HitboxUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import com.elmakers.mine.bukkit.utility.LogMessage;
import com.elmakers.mine.bukkit.utility.MagicLogger;
import com.elmakers.mine.bukkit.utility.Messages;
import com.elmakers.mine.bukkit.utility.NMSUtils;
import com.elmakers.mine.bukkit.utility.SafetyUtils;
import com.elmakers.mine.bukkit.utility.SchematicUtils;
import com.elmakers.mine.bukkit.utility.SkinUtils;
import com.elmakers.mine.bukkit.utility.SkullLoadedCallback;
import com.elmakers.mine.bukkit.wand.LostWand;
import com.elmakers.mine.bukkit.wand.Wand;
import com.elmakers.mine.bukkit.wand.WandManaMode;
import com.elmakers.mine.bukkit.wand.WandMode;
import com.elmakers.mine.bukkit.wand.WandTemplate;
import com.elmakers.mine.bukkit.wand.WandUpgradePath;
import com.elmakers.mine.bukkit.warp.WarpController;
import com.elmakers.mine.bukkit.world.MagicWorld;
import com.elmakers.mine.bukkit.world.WorldController;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import de.slikey.effectlib.math.EquationStore;

public class MagicController implements MageController {
    private static String DEFAULT_DATASTORE_PACKAGE = "com.elmakers.mine.bukkit.data";
    private static long MAGE_CACHE_EXPIRY = 10000;
    private static long LOG_WATCHDOG_TIMEOUT = 30000;
    private static int MAX_WARNINGS = 10;
    private static int MAX_ERRORS = 10;

    // Special constructor used for interrogation
    public MagicController() {
        configFolder = null;
        dataFolder = null;
        defaultsFolder = null;
        this.logger = new MagicLogger(Logger.getLogger("Magic"));
        this.materialSetManager.setLogger(logger);
    }

    public MagicController(final MagicPlugin plugin) {
        this.plugin = plugin;
        this.logger = new MagicLogger(plugin.getLogger());
        resourcePacks = new ResourcePackManager(this);

        configFolder = plugin.getDataFolder();
        configFolder.mkdirs();

        dataFolder = new File(configFolder, "data");
        dataFolder.mkdirs();

        defaultsFolder = new File(configFolder, "defaults");
        defaultsFolder.mkdirs();
    }

    public boolean registerNMSBindings() {
        if (!NMSUtils.initialize(getLogger())) {
            return false;
        }
        SkinUtils.initialize(plugin);
        EntityMetadataUtils.initialize(plugin);
        return true;
    }

    public void onPlayerJump(Player player) {
        if (climbableMaterials.testBlock(player.getLocation().getBlock())) {
            return;
        }
        Mage mage = getRegisteredMage(player);
        if (mage != null) {
            mage.trigger("jump");
        }
    }

    @Nullable
    @Override
    public com.elmakers.mine.bukkit.magic.Mage getRegisteredMage(String mageId) {
        checkNotNull(mageId);

        if (!loaded || shuttingDown) {
            return null;
        }
        return mages.get(mageId);
    }

    @Nullable
    public com.elmakers.mine.bukkit.magic.Mage getRegisteredMage(@Nonnull CommandSender commandSender) {
        checkNotNull(commandSender);
        if (commandSender instanceof Player) {
            return getRegisteredMage((Player)commandSender);
        }

        String mageId = mageIdentifier.fromCommandSender(commandSender);
        return getRegisteredMage(mageId);
    }

    @Nullable
    @Override
    public com.elmakers.mine.bukkit.magic.Mage getRegisteredMage(@Nonnull Entity entity) {
        checkNotNull(entity);
        String id = mageIdentifier.fromEntity(entity);
        return mages.get(id);
    }

    @Nonnull
    protected com.elmakers.mine.bukkit.magic.Mage getMageFromEntity(
            @Nonnull Entity entity, @Nullable CommandSender commandSender) {
        checkNotNull(entity);

        String id = mageIdentifier.fromEntity(entity);
        return getMage(id, commandSender, entity);
    }

    @Override
    public com.elmakers.mine.bukkit.magic.Mage getAutomaton(String mageId, String mageName) {
        checkNotNull(mageId);
        checkNotNull(mageName);

        com.elmakers.mine.bukkit.magic.Mage mage = getMage(mageId, mageName, null, null);
        mage.setIsAutomaton(true);
        return mage;
    }

    @Override
    public com.elmakers.mine.bukkit.magic.Mage getMage(String mageId, String mageName) {
        checkNotNull(mageId);
        checkNotNull(mageName);

        return getMage(mageId, mageName, null, null);
    }

    @Nonnull
    public com.elmakers.mine.bukkit.magic.Mage getMage(
            @Nonnull String mageId,
            @Nullable CommandSender commandSender, @Nullable Entity entity) {
        checkState(
                commandSender != null || entity != null,
                "Need to provide either an entity or a command sender for a non-automata mage.");
        return getMage(mageId, null, commandSender, entity);
    }


    @Nonnull
    @Override
    public com.elmakers.mine.bukkit.magic.Mage getMage(@Nonnull Player player) {
        checkNotNull(player);
        return getMageFromEntity(player, player);
    }

    @Nonnull
    @Override
    public com.elmakers.mine.bukkit.magic.Mage getMage(@Nonnull Entity entity) {
        checkNotNull(entity);
        CommandSender commandSender = (entity instanceof Player) ? (Player) entity : null;
        return getMageFromEntity(entity, commandSender);
    }

    @Nonnull
    @Override
    public com.elmakers.mine.bukkit.magic.Mage getMage(@Nonnull CommandSender commandSender) {
        checkNotNull(commandSender);
        if (commandSender instanceof Player) {
            return getMage((Player) commandSender);
        }

        String mageId = mageIdentifier.fromCommandSender(commandSender);
        return getMage(mageId, commandSender, null);
    }

    @Nonnull
    protected com.elmakers.mine.bukkit.magic.Mage getMage(
            @Nonnull String mageId, @Nullable String mageName,
            @Nullable CommandSender commandSender, @Nullable Entity entity)
            throws PluginNotLoadedException, NoSuchMageException {
        checkNotNull(mageId);

        // Should only happen for Automata.
        if (commandSender == null && entity == null) {
            commandSender = Bukkit.getConsoleSender();
        }
        if (!loaded) {
            if (entity instanceof Player) {
                getLogger().warning("Player data request for " + mageId + " (" + ((Player)commandSender).getName() + ") failed, plugin not loaded yet");
            }

            throw new PluginNotLoadedException();
        }

        com.elmakers.mine.bukkit.magic.Mage apiMage = null;
        if (!mages.containsKey(mageId)) {
            if (shuttingDown) {
                if (entity instanceof Player) {
                    getLogger().warning("Player data request for " + mageId + " (" + ((Player)commandSender).getName() + ") failed, plugin is shutting down");
                }

                throw new PluginNotLoadedException();
            }
            if (entity instanceof Player && !((Player)entity).isOnline() && !isNPC(entity))
            {
                getLogger().warning("Player data for " + mageId + " (" + entity.getName() + ") loaded while offline!");
                Thread.dumpStack();
                // This will cause some really bad things to happen if using file locking, so we're going to just skip it.
                if (isFileLockingEnabled) {
                    getLogger().warning("Returning dummy Mage to avoid locking issues");
                    return new com.elmakers.mine.bukkit.magic.Mage(mageId, this);
                }
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
                if (isPlayer) {
                    mage.setLoading(true);
                    plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, new DoMageLoadTask(this, mage), fileLoadDelay * 20 / 1000);
                } else if (saveNonPlayerMages) {
                    info("Loading mage data for " + mage.getName() + " (" + mage.getId() + ") synchronously");
                    doLoadData(mage);
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
            throw new NoSuchMageException(mageId);
        }
        return apiMage;
    }

    public void doSynchronizedLoadData(Mage mage) {
        synchronized (saveLock) {
            info("Loading mage data for " + mage.getName() + " (" + mage.getId() + ") at " + System.currentTimeMillis());
            doLoadData(mage);
        }
    }

    private void doLoadData(Mage mage) {
        getMageData(mage.getId(), new MageDataCallback() {
            @Override
            public void run(MageData data) {
                mage.load(data);
            }
        });
    }

    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        String id = mageIdentifier.fromPreLogin(event);
        Iterator<Map.Entry<String, MageData>> it = mageDataPreCache.entrySet().iterator();
        while (it.hasNext()) {
            MageData data = it.next().getValue();
            if (data.getId().equals(id)) continue;
            if (data.getCachedTimestamp() < System.currentTimeMillis() - MAGE_CACHE_EXPIRY) {
                it.remove();
                info("Removed expired pre-login mage data cache for id " + data.getId());
            }
        }

        if (mageDataPreCache.containsKey(id)) return;
        getMageData(id, new MageDataCallback() {
            @Override
            public void run(MageData data) {
                if (data != null) {
                    info("Cached preloaded mage data cache for id " + data.getId());
                    mageDataPreCache.put(id, data);
                }
            }
        });
    }

    private void getMageData(String id, MageDataCallback callback) {
        synchronized (saveLock) {
            MageData cached = mageDataPreCache.get(id);
            if (cached != null) {
                info("Loaded preloaded mage data from cache for id " + id);
                mageDataPreCache.remove(id);
                callback.run(cached);
                return;
            }
            try {
                mageDataStore.load(id, new MageDataCallback() {
                    @Override
                    public void run(MageData data) {
                        if (data == null && migrateDataStore != null) {
                            info(" Checking migration data store for mage data for " + id);
                            migrateDataStore.load(id, new MageDataCallback() {
                                @Override
                                public void run(MageData data) {
                                    if (data != null) {
                                        migrateDataStore.migrate(id);
                                        info(" Auto-migrated mage data for " + id + " on load");
                                    }
                                    callback.run(data);
                                    info(" Finished Loading mage data for " + id + " from migration store at " + System.currentTimeMillis());
                                }
                            });
                        } else {
                            callback.run(data);
                            info(" Finished Loading mage data for " + id + " at " + System.currentTimeMillis());
                        }
                    }
                });
            } catch (Exception ex) {
                getLogger().warning("Failed to load mage data for " + id);
                ex.printStackTrace();
            }
        }
    }

    @Nonnull
    @Override
    public Mage getConsoleMage() {
        return getMage(plugin.getServer().getConsoleSender());
    }

    public void log(String message)
    {
        info(message, 0);
    }

    @Override
    public void info(String message)
    {
        info(message, 1);
    }

    public void info(String message, int verbosity)
    {
        if (loading && !reloadVerboseLogging) {
            return;
        }
        if (logVerbosity >= verbosity)
        {
            getLogger().info(message);
        }
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

    public double getMaxDamageReduction(String protectionType) {
        DamageType damageType = damageTypes.get(protectionType);
        return damageType == null ? 0 : damageType.getMaxReduction();
    }

    public double getMaxAttackMultiplier(String protectionType) {
        DamageType damageType = damageTypes.get(protectionType);
        return damageType == null ? 1 : damageType.getMaxAttackMultiplier();
    }

    public double getMaxDefendMultiplier(String protectionType) {
        DamageType damageType = damageTypes.get(protectionType);
        return damageType == null ? 1 : damageType.getMaxDefendMultiplier();
    }

    @Override
    public @Nonnull Set<String> getDamageTypes() {
        return damageTypes.keySet();
    }

    @Override
    public @Nonnull Set<String> getAttributes() {
        return registeredAttributes;
    }

    @Override
    public @Nonnull Set<String> getInternalAttributes() {
        return attributes.keySet();
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

    @Nullable
    @Override
    public ItemStack getWorthItem() {
        return currencyItem == null ? null : currencyItem.getItem();
    }

    @Override
    public double getWorthItemAmount() {
        return currencyItem == null ? 0 : currencyItem.getWorth();
    }

    @Override
    @Deprecated
    public CurrencyItem getCurrency() {
        return currencyItem;
    }

    @Override
    @Nullable
    public Currency getCurrency(String key) {
        return currencies.get(key);
    }

    @Override
    @Nonnull
    public Set<String> getCurrencyKeys() {
        return currencies.keySet();
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
    public MagicLogger getLogger() {
        return logger;
    }

    public boolean isIndestructible(Location location) {
        return isIndestructible(location.getBlock());
    }

    public boolean isIndestructible(Block block) {
        return indestructibleMaterials.testBlock(block);
    }

    public boolean isDestructible(Block block) {
        return destructibleMaterials.testBlock(block);
    }

    @Override
    public boolean isUndoable(Material material) {
        return undoableMaterials.testMaterial(material);
    }

    @Deprecated // Material
    protected boolean isRestricted(Material material) {
        return restrictedMaterials.testMaterial(material);
    }

    protected boolean isRestricted(Material material, @Nullable Short data) {
        if (restrictedMaterials.testMaterial(material)) {
            // Fast path
            return true;
        }

        MaterialAndData materialAndData = new MaterialAndData(material, data);
        return restrictedMaterials.testMaterialAndData(materialAndData);
    }

    public boolean hasBuildPermission(Player player, Location location) {
        return hasBuildPermission(player, location.getBlock());
    }

    public boolean hasBuildPermission(Player player, Block block) {
        // Check all protection plugins
        if (bypassBuildPermissions) return true;
        if (player != null && player.hasPermission("Magic.bypass_build")) return true;
        if (hasBypassPermission(player)) return true;

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
        if (hasBypassPermission(player)) return true;

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

    @Nullable
    protected InputStream findSchematic(String schematicName, String extension) {
        InputStream inputSchematic;
        try {
            // Check extra path first
            File extraSchematicFile = null;
            File magicSchematicFolder = new File(plugin.getDataFolder(), "schematics");
            if (magicSchematicFolder.exists()) {
                extraSchematicFile = new File(magicSchematicFolder, schematicName + "." + extension);
                info("Checking for schematic: " + extraSchematicFile.getAbsolutePath(), 2);
                if (!extraSchematicFile.exists()) {
                    extraSchematicFile = null;
                }
            }
            if (extraSchematicFile == null && extraSchematicFilePath != null && extraSchematicFilePath.length() > 0) {
                File schematicFolder = new File(configFolder, "../" + extraSchematicFilePath);
                if (schematicFolder.exists()) {
                    extraSchematicFile = new File(schematicFolder, schematicName + "." + extension);
                    info("Checking for external schematic: " + extraSchematicFile.getAbsolutePath(), 2);
                }
            }

            if (extraSchematicFile != null && extraSchematicFile.exists()) {
                inputSchematic = new FileInputStream(extraSchematicFile);
                info("Loading file: " + extraSchematicFile.getAbsolutePath());
            } else {
                String fileName = schematicName + "." + extension;
                inputSchematic = plugin.getResource("schematics/" + fileName);
                info("Loading builtin schematic: " + fileName);
            }
            if (inputSchematic == null) {
                throw new FileNotFoundException();
            }
        } catch (Exception ignored) {
            getLogger().warning("Could not load schematic: " + schematicName);
            inputSchematic = null;
        }
        return inputSchematic;
    }

    @Nullable
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

        // Look for new schematic format first
        if (CompatibilityUtils.hasBlockDataSupport()) {
            final InputStream inputSchematic = findSchematic(schematicName, "schem");
            if (inputSchematic != null) {
                com.elmakers.mine.bukkit.block.Schematic schematic = new com.elmakers.mine.bukkit.block.Schematic(this);
                schematics.put(schematicName, new WeakReference<>(schematic));
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    try {
                        SchematicUtils.loadSchematic(inputSchematic, schematic, getLogger());
                        info("Finished loading schematic");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

                return schematic;
            }
        }

        // Look for legacy schematic
        final InputStream legacySchematic = findSchematic(schematicName, "schematic");
        if (legacySchematic == null) {
            return null;
        }
        LegacySchematic schematic = new LegacySchematic(this);
        schematics.put(schematicName, new WeakReference<>(schematic));
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                SchematicUtils.loadLegacySchematic(legacySchematic, schematic);
                info("Finished loading schematic");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

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
                try (ZipInputStream zip = new ZipInputStream(jar.openStream())) {
                    ZipEntry entry = zip.getNextEntry();
                    while (entry != null) {
                        String name = entry.getName();
                        if (name.startsWith("schematics/") && (name.endsWith(".schem") || name.endsWith(".schematic"))) {
                            String schematicName = name
                                .replace(".schematic", "")
                                .replace(".schem", "")
                                .replace("schematics/", "");
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
                    if (schematicFile.getName().endsWith(".schematic") || schematicFile.getName().endsWith(".schem")) {
                        String schematicName = schematicFile.getName()
                            .replace(".schematic", "")
                            .replace(".schem", "");
                        schematicNames.add(schematicName);
                    }
                }
            }
        } catch (Exception ignored) {

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
        warpController = new WarpController(this);
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
        requirementsController = new RequirementsController(this);
        worldController = new WorldController(this);
        if (NMSUtils.hasStatistics()) {
            jumpController = new JumpController(this);
        }
        if (NMSUtils.hasEntityTransformEvent()) {
            mobs2 = new MobController2(this);
        }
        File examplesFolder = new File(getPlugin().getDataFolder(), "examples");
        examplesFolder.mkdirs();

        File urlMapFile = getDataFile(URL_MAPS_FILE);
        File imageCache = new File(dataFolder, "imagemapcache");
        imageCache.mkdirs();
        maps = new MapController(plugin, urlMapFile, imageCache);

        // Initialize EffectLib.
        if (com.elmakers.mine.bukkit.effect.EffectPlayer.initialize(plugin, getLogger())) {
            getLogger().info("EffectLib initialized");
        } else {
            getLogger().warning("Failed to initialize EffectLib");
        }

        // Pre-create schematic folder
        File magicSchematicFolder = new File(plugin.getDataFolder(), "schematics");
        magicSchematicFolder.mkdirs();

        // One-time migration of enchanting.yml
        File legacyPathConfig = new File(configFolder, "enchanting.yml");
        File pathConfig = new File(configFolder, "paths.yml");

        if (!pathConfig.exists() && legacyPathConfig.exists()) {
            getLogger().info("Renaming enchanting.yml to paths.yml, please update paths.yml from now on");
            legacyPathConfig.renameTo(pathConfig);
        }
        load();
        resourcePacks.startResourcePackChecks();
    }

    protected void finalizeIntegration() {
        final PluginManager pluginManager = plugin.getServer().getPluginManager();

        // Check for SkillAPI
        Plugin skillAPIPlugin = pluginManager.getPlugin("SkillAPI");
        if (skillAPIPlugin != null && skillAPIEnabled && skillAPIPlugin.isEnabled()) {
            skillAPIManager = new SkillAPIManager(this, skillAPIPlugin);
            if (skillAPIManager.initialize()) {
                getLogger().info("SkillAPI found, attributes can be used in spell parameters. Classes and skills can be used in requirements.");
                if (useSkillAPIAllies) {
                    getLogger().info("SKillAPI allies will be respected in friendly fire checks");
                }
                if (useSkillAPIMana) {
                    getLogger().info("SkillAPI mana will be used by spells and wands");
                }
            } else {
                skillAPIManager = null;
                getLogger().warning("SkillAPI integration failed");
            }
        } else if (!skillAPIEnabled) {
            skillAPIManager = null;
            getLogger().info("SkillAPI integration disabled");
        }

        // Check for BattleArenas
        Plugin battleArenaPlugin = pluginManager.getPlugin("BattleArena");
        if (battleArenaPlugin != null) {
            if (useBattleArenaTeams) {
                try {
                    battleArenaManager = new BattleArenaManager();
                } catch (Throwable ex) {
                    getLogger().log(Level.SEVERE, "Error integrating with BattleArena", ex);
                }
                getLogger().info("BattleArena found, teams will be respected in friendly fire checks");
            } else {
                battleArenaManager = null;
                getLogger().info("BattleArena integration disabled");
            }
        }

        // Check for WildStacker
        if (pluginManager.isPluginEnabled("WildStacker")) {
            if (useWildStacker) {
                getLogger().info("Wild Stacker integration enabled");
                pluginManager.registerEvents(new WildStackerListener(), plugin);
            } else {
                getLogger().info("Wild Stacker found, but integration disabled");
            }
        }

        // Try to link to Heroes:
        try {
            Plugin heroesPlugin = pluginManager.getPlugin("Heroes");
            if (heroesPlugin != null) {
                heroesManager = new HeroesManager(plugin, heroesPlugin);
            } else {
                heroesManager = null;
            }
        } catch (Throwable ex) {
            getLogger().warning(ex.getMessage());
        }

        // Vault integration
        if (!vaultEnabled) {
            getLogger().info("Vault integration disabled");
        } else {
            Plugin vaultPlugin = pluginManager.getPlugin("Vault");
            if (vaultPlugin == null || !vaultPlugin.isEnabled()) {
                getLogger().info("Vault not found, 'currency' cost types unavailable");
            } else {
                if (!VaultController.initialize(plugin, vaultPlugin)) {
                    getLogger().warning("Vault integration failed");
                }
            }
        }

        // Check for Minigames
        Plugin minigamesPlugin = pluginManager.getPlugin("Minigames");
        if (minigamesPlugin != null && minigamesPlugin.isEnabled()) {
            pluginManager.registerEvents(new MinigamesListener(this), plugin);
            getLogger().info("Minigames found, wands will deactivate before joining a minigame");
        }

        // Check for LibsDisguise
        Plugin libsDisguisePlugin = pluginManager.getPlugin("LibsDisguises");
        if (libsDisguisePlugin == null || !libsDisguisePlugin.isEnabled()) {
            getLogger().info("LibsDisguises not found, magic mob disguises will not be available");
        } else if (libsDisguiseEnabled) {
            libsDisguiseManager = new LibsDisguiseManager(this, libsDisguisePlugin);
            if (libsDisguiseManager.initialize()) {
                getLogger().info("LibsDisguises found, mob disguises and disguise_restricted features enabled");
            } else {
                getLogger().warning("LibsDisguises integration failed");
            }
        } else {
            libsDisguiseManager = null;
            getLogger().info("LibsDisguises integration disabled");
        }

        // Check for MobArena
        Plugin mobArenaPlugin = pluginManager.getPlugin("MobArena");
        if (mobArenaPlugin == null) {
            getLogger().info("MobArena not found");
        } else if (mobArenaConfiguration.getBoolean("enabled", true)) {
            try {
                mobArenaManager = new MobArenaManager(this, mobArenaPlugin, mobArenaConfiguration);
                getLogger().info("Integrated with MobArena, use \"magic:<itemkey>\" in arena configs for Magic items, magic mobs can be used in monster configurations");
            } catch (Throwable ex) {
                getLogger().warning("MobArena integration failed, you may need to update the MobArena plugin to use Magic items");
            }
        } else {
            getLogger().info("MobArena integration disabled");
        }

        // Check for LogBlock
        Plugin logBlockPlugin = pluginManager.getPlugin("LogBlock");
        if (logBlockPlugin == null || !logBlockPlugin.isEnabled()) {
            getLogger().info("LogBlock not found");
        } else if (logBlockEnabled) {
            try {
                logBlockManager = new LogBlockManager(plugin, logBlockPlugin);
                getLogger().info("Integrated with LogBlock, engineering magic will be logged");
            } catch (Throwable ex) {
                getLogger().log(Level.WARNING, "LogBlock integration failed", ex);
            }
        } else {
            getLogger().info("LogBlock integration disabled");
        }

        // Try to link to Essentials:
        Plugin essentials = pluginManager.getPlugin("Essentials");
        essentialsController = null;
        hasEssentials = essentials != null && essentials.isEnabled();
        if (hasEssentials) {
            essentialsController = EssentialsController.initialize(essentials);
            if (essentialsController == null) {
                getLogger().warning("Error integrating with Essentials");
            } else {
                getLogger().info("Integrating with Essentials for vanish detection");
            }
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
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new EssentialsItemIntegrationTask(this), 5);
        }

        // Try to link to CommandBook
        hasCommandBook = false;
        try {
            Plugin commandBookPlugin = plugin.getServer().getPluginManager().getPlugin("CommandBook");
            if (commandBookPlugin != null && commandBookPlugin.isEnabled()) {
                if (warpController.setCommandBook(commandBookPlugin)) {
                    getLogger().info("CommandBook found, integrating for Recall warps");
                    hasCommandBook = true;
                } else {
                    getLogger().warning("CommandBook integration failed");
                }
            }
        } catch (Throwable ignored) {

        }

        // Link to factions
        factionsManager.initialize(plugin);

        // Try to (dynamically) link to WorldGuard:
        worldGuardManager.initialize(plugin);

        // Link to PvpManager
        pvpManager.initialize(plugin);

        // Link to Multiverse
        multiverseManager.initialize(plugin);

        // Link to DeadSouls
        Plugin deadSoulsPlugin = plugin.getServer().getPluginManager().getPlugin("DeadSouls");
        if (deadSoulsPlugin != null) {
            try {
                deadSoulsController = new DeadSoulsManager(this);
            } catch (Exception ex) {
                getLogger().log(Level.WARNING, "Error integrating with DeadSouls, is it up to date? Version 1.6 or higher required.", ex);
            }
        }

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

        // Try to link to dynmap:
        try {
            Plugin dynmapPlugin = plugin.getServer().getPluginManager().getPlugin("dynmap");
            if (dynmapPlugin != null && dynmapPlugin.isEnabled()) {
                dynmap = new DynmapController(plugin, dynmapPlugin, messages);
            } else {
                dynmap = null;
            }
        } catch (Throwable ex) {
            getLogger().warning(ex.getMessage());
        }

        if (dynmap == null) {
            getLogger().info("dynmap not found, not integrating.");
        } else {
            getLogger().info("dynmap found, integrating.");
        }

        // Try to link to Elementals:
        try {
            Plugin elementalsPlugin = plugin.getServer().getPluginManager().getPlugin("Splateds_Elementals");
            if (elementalsPlugin != null && elementalsPlugin.isEnabled()) {
                elementals = new ElementalsController(elementalsPlugin);
            } else {
                elementals = null;
            }
        } catch (Throwable ex) {
            getLogger().warning(ex.getMessage());
        }

        if (elementals != null) {
            getLogger().info("Elementals found, integrating.");
        }

        // Check for Shopkeepers, this is an optimization to avoid scanning for metadata if the plugin is not
        // present
        hasShopkeepers = pluginManager.isPluginEnabled("Shopkeepers");
        if (hasShopkeepers) {
            npcSuppliers.register(new GenericMetadataNPCSupplier("shopkeeper"));
        }

        // Try to link to Citizens
        try {
            Plugin citizensPlugin = plugin.getServer().getPluginManager().getPlugin("Citizens");
            if (citizensPlugin != null && citizensPlugin.isEnabled()) {
                citizens = new CitizensController(citizensPlugin, this, citizensEnabled);
            } else {
                citizens = null;
                getLogger().info("Citizens not found, Magic trait unavailable.");
            }
        } catch (Throwable ex) {
            citizens = null;
            getLogger().warning("Error integrating with Citizens");
            getLogger().warning(ex.getMessage());
        }

        if (citizens != null) {
            npcSuppliers.register(citizens);
        }

        // Placeholder API
        if (placeholdersEnabled) {
            if (pluginManager.isPluginEnabled("PlaceholderAPI")) {
               try {
                   // Can only register this once
                   if (placeholderAPIManager == null) {
                       placeholderAPIManager = new PlaceholderAPIManager(this);
                   }
                } catch (Throwable ex) {
                    getLogger().log(Level.WARNING, "Error integrating with PlaceholderAPI", ex);
                }
            }
        } else {
            getLogger().info("PlaceholderAPI integration disabled.");
        }

        // Light API
        if (lightAPIEnabled) {
            if (pluginManager.isPluginEnabled("LightAPI")) {
                try {
                    lightAPIManager = new LightAPIManager(plugin);
                } catch (Throwable ex) {
                    getLogger().log(Level.WARNING, "Error integrating with LightAPI", ex);
                }
            } else {
                getLogger().info("LightAPI not found, Light action will not work");
            }
        } else {
            lightAPIManager = null;
            getLogger().info("LightAPI integration disabled.");
        }

        // Skript
        if (skriptEnabled) {
            if (pluginManager.isPluginEnabled("Skript")) {
                try {
                    new SkriptManager(this);
                } catch (Throwable ex) {
                    getLogger().log(Level.WARNING, "Error integrating with Skript", ex);
                }
            }
        } else {
            getLogger().info("Skript integration disabled.");
        }

        // ajParkour
        if (ajParkourConfiguration.getBoolean("enabled")) {
            if (pluginManager.isPluginEnabled("ajParkour")) {
                try {
                    ajParkourManager = new AJParkourManager(this);
                } catch (Throwable ex) {
                    getLogger().log(Level.WARNING, "Error integrating with ajParkour", ex);
                }
            }
        } else {
            getLogger().info("ajParkour integration disabled.");
        }

        // Citadel
        if (citadelConfiguration.getBoolean("enabled")) {
            if (pluginManager.isPluginEnabled("Citadel")) {
                try {
                    citadelManager = new CitadelManager(this, citadelConfiguration);
                } catch (Throwable ex) {
                    getLogger().log(Level.WARNING, "Error integrating with Citadel", ex);
                }
            }
        } else {
            getLogger().info("Citadel integration disabled.");
        }

        // Residence
        if (residenceConfiguration.getBoolean("enabled")) {
            if (pluginManager.isPluginEnabled("Residence")) {
                try {
                    residenceManager = new ResidenceManager(pluginManager.getPlugin("Residence"), this, residenceConfiguration);
                    getLogger().info("Integrated with residence for build/break/pvp/target checks");
                    getLogger().info("Disable warping to residences in recall config with allow_residence: false");
                } catch (Throwable ex) {
                    getLogger().log(Level.WARNING, "Error integrating with Residence", ex);
                }
            }
        } else {
            getLogger().info("Residence integration disabled.");
        }

        // RedProtect
        if (redProtectConfiguration.getBoolean("enabled")) {
            if (pluginManager.isPluginEnabled("RedProtect")) {
                try {
                    redProtectManager = new RedProtectManager(pluginManager.getPlugin("RedProtect"), this, redProtectConfiguration);
                    getLogger().info("Integrated with RedProtect for build/break/pvp/target checks");
                    getLogger().info("Disable warping to fields in recall config with allow_redprotect: false");
                    if (redProtectManager.isFlagsEnabled()) {
                        getLogger().info("Added custom flags: " + StringUtils.join(RedProtectManager.flags, ','));
                    }
                } catch (Throwable ex) {
                    getLogger().log(Level.WARNING, "Error integrating with RedProtect", ex);
                }
            }
        } else {
            getLogger().info("RedProtect integration disabled.");
        }

        // Activate Metrics
        activateMetrics();

        // Set up the Mage update timer
        final MageUpdateTask mageTask = new MageUpdateTask(this);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, mageTask, 0, mageUpdateFrequency);

        // Set up the Block update timer
        final BatchUpdateTask blockTask = new BatchUpdateTask(this);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, blockTask, 0, workFrequency);

        // Set up the Automata timer
        final AutomataUpdateTask automataTaks = new AutomataUpdateTask(this);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, automataTaks, 0, automataUpdateFrequency);

        // Set up the Update check timer
        final UndoUpdateTask undoTask = new UndoUpdateTask(this);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, undoTask, 0, undoFrequency);
        registerListeners();
    }

    protected void postLoadIntegration() {
        if (mobArenaManager != null) {
            mobArenaManager.loaded();
        }
    }

    public void processUndo()
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

    public void processPendingBatches()
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

                getLogger().info("Activated BStats");
            } catch (Exception ex) {
                getLogger().warning("Failed to load BStats: " + ex.getMessage());
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
        if (jumpController != null) {
            pm.registerEvents(jumpController, plugin);
        }
        if (mobs2 != null) {
            pm.registerEvents(mobs2, plugin);
        }

        worldController.registerEvents();
    }

    public Collection<Mage> getPending() {
        return pendingConstruction;
    }

    public Collection<UndoList> getPendingUndo() {
        return scheduledUndo;
    }

    @Nullable
    public UndoList getPendingUndo(Location location) {
        return com.elmakers.mine.bukkit.block.UndoList.getUndoList(location);
    }

    protected void addPending(Mage mage) {
        pendingConstruction.add(mage);
    }

    public boolean removeMarker(String id, String group) {
        boolean removed = false;
        if (dynmap != null) {
            return dynmap.removeMarker(id, group);
        }

        return removed;
    }

    public boolean addMarker(String id, String icon, String group, String title, Location location, String description) {
        if (location == null || location.getWorld() == null) return false;
        return addMarker(id, icon, group, title, location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), description);
    }

    public boolean addMarker(String id, String icon, String group, String title, String world, int x, int y, int z, String description) {
        boolean created = false;
        if (dynmap != null) {
            created = dynmap.addMarker(id, icon, group, title, world, x, y, z, description);
        }

        return created;
    }

    @Nullable
    public Collection<String> getMarkerIcons() {
        if (dynmap == null) {
            return null;
        }
        return dynmap.getIcons();
    }

    @Nullable
    public Collection<String> getMarkerSets() {
        if (dynmap == null) {
            return null;
        }
        return dynmap.getSets();
    }

    @Override
    public File getConfigFolder() {
        return configFolder;
    }

    @Override
    public File getDataFolder() {
        return dataFolder;
    }

    protected File getDataFile(String fileName) {
        return new File(dataFolder, fileName + ".yml");
    }

    @Nullable
    protected ConfigurationSection loadDataFile(String fileName) {
        File dataFile = getDataFile(fileName);
        if (!dataFile.exists()) {
            return null;
        }
        Configuration configuration = YamlConfiguration.loadConfiguration(dataFile);
        return configuration;
    }

    protected YamlDataFile createDataFile(String fileName) {
        return createDataFile(fileName, true);
    }

    protected YamlDataFile createDataFile(String fileName, boolean checkBackupSize) {
        File dataFile = new File(dataFolder, fileName + ".yml");
        YamlDataFile configuration = new YamlDataFile(getLogger(), dataFile, checkBackupSize);
        return configuration;
    }

    protected void notify(CommandSender sender, String message) {
        if (sender != null) {
            sender.sendMessage(message);
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player != sender && player.hasPermission("Magic.notify")) {
                player.sendMessage(message);
            }
        }
    }

    public void finalizeLoad(ConfigurationLoadTask loader, CommandSender sender) {
        if (!loader.isSuccessful()) {
            notify(sender, ChatColor.RED + "An error occurred reloading configurations, please check server logs!");

            // Check for initial load failure on startup
            if (!loaded) {
                getLogger().severe("*** An error occurred while loading configurations ***");
                getLogger().severe("*** Magic will be disabled until the next restart  ***");
                getLogger().severe("***   Please check the errors above, fix configs   ***");
                getLogger().severe("***             And restart the server             ***");
                getLogger().warning("");
                getLogger().warning("Note that if you start the server with working configs and");
                getLogger().warning("Then use /magic load to test changes, Magic won't break");
                getLogger().warning("if there are config issues.");

                PluginManager pm = plugin.getServer().getPluginManager();
                pm.registerEvents(new ErrorNotifier(), plugin);
            }
            loading = false;
            resetLoading(sender);
            return;
        }

        // Clear some cache stuff... mainly this is for debugging/testing.
        schematics.clear();

        // Clear the equation store to flush out any equations that failed to parse
        EquationStore.clear();

        // Process loaded data
        exampleDefaults = loader.getExampleDefaults();
        addExamples = loader.getAddExamples();

        // Main configuration
        logger.setContext("config");
        loadProperties(sender, loader.getMainConfiguration());

        // Configurations that don't rely on any external integrations
        logger.setContext("messages");
        messages.load(loader.getMessages());
        processMessages();
        logger.setContext("materials");
        loadMaterials(loader.getMaterials());

        // Finalize integrations, we only do this one time at startup.
        logger.setContext("integration");
        if (!loaded) {
            finalizeIntegration();
        }

        // Register currencies and other preload integrations
        registerPreLoad();
        log("Registered currencies: " + StringUtils.join(currencies.keySet(), ","));

        // Load custom attributes, do this prior to loadAttributes
        logger.setContext("attributes");
        loadAttributes(loader.getAttributes());
        logger.setContext(null);
        log("Loaded " + attributes.size() + " attributes");

        // Do this before spell loading in case of attribute or requirement providers
        logger.setContext("integration");
        registerManagers();

        logger.setContext("effects");
        loadEffects(loader.getEffects());
        logger.setContext(null);
        log("Loaded " + effects.size() + " effect lists");

        logger.setContext("items");
        items.load(loader.getItems());
        logger.setContext(null);
        log("Loaded " + items.getCount() + " items");

        logger.setContext("wands");
        loadWandTemplates(loader.getWands());
        logger.setContext(null);
        log("Loaded " + getWandTemplates().size() + " wands");

        logger.setContext("spells");
        loadSpells(sender, loader.getSpells());
        logger.setContext(null);
        log("Loaded " + spells.size() + " spells");

        logger.setContext("classes");
        loadMageClasses(loader.getClasses());
        logger.setContext(null);
        log("Loaded " + mageClasses.size() + " classes");

        logger.setContext("modifiers");
        loadModifiers(loader.getModifiers());
        logger.setContext(null);
        log("Loaded " + modifiers.size() + " classes");

        logger.setContext("paths");
        loadPaths(loader.getPaths());
        logger.setContext(null);
        log("Loaded " + getPathCount() + " progression paths");

        logger.setContext("mobs");
        loadMobs(loader.getMobs());
        logger.setContext(null);
        log("Loaded " + mobs.getCount() + " mob templates");

        logger.setContext("automata");
        loadAutomatonTemplates(loader.getAutomata());
        logger.setContext(null);
        log("Loaded " + automatonTemplates.size() + " automata templates");

        logger.setContext("crafting");
        crafting.load(loader.getCrafting());

        // Register crafting recipes
        crafting.register(this, plugin);
        MagicRecipe.FIRST_REGISTER = false;

        logger.setContext(null);
        log("Loaded " + crafting.getCount() + " crafting recipes");

        // Load worlds
        logger.setContext("worlds");
        loadWorlds(loader.getWorlds());
        logger.setContext(null);
        log("Loaded " + worldController.getCount() + " customized worlds");

        if (!loaded) {
            postLoadIntegration();
        }

        // Notify plugins that we've finished loading.
        logger.setContext(null);
        LoadEvent loadEvent = new LoadEvent(this);
        Bukkit.getPluginManager().callEvent(loadEvent);
        loaded = true;
        loading = false;

        // Activate/load any active player Mages
        Collection<? extends Player> allPlayers = plugin.getServer().getOnlinePlayers();
        for (Player player : allPlayers) {
            getMage(player);
        }

        if (!(sender instanceof ConsoleCommandSender)) {
            getLogger().info("Finished reloading configuration");
        }
        if (sender != null && logger.isCapturing() && isLoaded()) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new ValidateSpellsTask(this, sender));
        } else {
            resetLoading(sender);
            notify(sender, ChatColor.AQUA + "Magic " + ChatColor.DARK_AQUA + "configuration reloaded.");
        }

        if (reloadingMage != null) {
            Player player = reloadingMage.getPlayer();
            if (!player.hasPermission("Magic.notify")) {
                player.sendMessage(ChatColor.AQUA + "Spells reloaded.");
            }
            reloadingMage.deactivate();
            reloadingMage.checkWand();
            reloadingMage = null;
        }

        if (showExampleInstructions) {
            showExampleInstructions = false;
            showExampleInstructions(sender);
        }

        Bukkit.getScheduler().runTaskLater(plugin, new MigrationTask(this), 20 * 5);
    }

    private void processMessages() {
        BaseMagicCurrency.formatter = new DecimalFormat(messages.get("numbers.decimal", "#,###.00"));
        BaseMagicCurrency.intFormatter = new DecimalFormat(messages.get("numbers.integer", "#,###"));
    }

    private void registerManagers() {
        // Cast Managers
        if (worldGuardManager.isEnabled()) castManagers.add(worldGuardManager);
        if (preciousStonesManager.isEnabled()) castManagers.add(preciousStonesManager);
        if (redProtectManager != null && redProtectManager.isFlagsEnabled()) castManagers.add(redProtectManager);

        // Entity Targeting Managers
        if (preciousStonesManager.isEnabled()) targetingProviders.add(preciousStonesManager);
        if (townyManager.isEnabled()) targetingProviders.add(townyManager);
        if (residenceManager != null) targetingProviders.add(residenceManager);
        if (redProtectManager != null) targetingProviders.add(redProtectManager);

        // PVP Managers
        if (worldGuardManager.isEnabled()) pvpManagers.add(worldGuardManager);
        if (pvpManager.isEnabled()) pvpManagers.add(pvpManager);
        if (multiverseManager.isEnabled()) pvpManagers.add(multiverseManager);
        if (preciousStonesManager.isEnabled()) pvpManagers.add(preciousStonesManager);
        if (townyManager.isEnabled()) pvpManagers.add(townyManager);
        if (griefPreventionManager.isEnabled()) pvpManagers.add(griefPreventionManager);
        if (factionsManager.isEnabled()) pvpManagers.add(factionsManager);
        if (residenceManager != null) pvpManagers.add(residenceManager);
        if (redProtectManager != null) pvpManagers.add(redProtectManager);

        // Build Managers
        if (worldGuardManager.isEnabled()) blockBuildManagers.add(worldGuardManager);
        if (factionsManager.isEnabled()) blockBuildManagers.add(factionsManager);
        if (locketteManager.isEnabled()) blockBuildManagers.add(locketteManager);
        if (preciousStonesManager.isEnabled()) blockBuildManagers.add(preciousStonesManager);
        if (townyManager.isEnabled()) blockBuildManagers.add(townyManager);
        if (griefPreventionManager.isEnabled()) blockBuildManagers.add(griefPreventionManager);
        if (mobArenaManager != null && mobArenaManager.isProtected()) blockBuildManagers.add(mobArenaManager);
        if (residenceManager != null) blockBuildManagers.add(residenceManager);
        if (redProtectManager != null) blockBuildManagers.add(redProtectManager);

        // Break Managers
        if (worldGuardManager.isEnabled()) blockBreakManagers.add(worldGuardManager);
        if (factionsManager.isEnabled()) blockBreakManagers.add(factionsManager);
        if (locketteManager.isEnabled()) blockBreakManagers.add(locketteManager);
        if (preciousStonesManager.isEnabled()) blockBreakManagers.add(preciousStonesManager);
        if (townyManager.isEnabled()) blockBreakManagers.add(townyManager);
        if (griefPreventionManager.isEnabled()) blockBreakManagers.add(griefPreventionManager);
        if (mobArenaManager != null && mobArenaManager.isProtected()) blockBreakManagers.add(mobArenaManager);
        if (citadelManager != null) blockBreakManagers.add(citadelManager);
        if (residenceManager != null) blockBreakManagers.add(residenceManager);
        if (redProtectManager != null) blockBreakManagers.add(redProtectManager);

        // Attribute providers
        if (skillAPIManager != null) {
            attributeProviders.add(skillAPIManager);
        }
        if (heroesManager != null) {
            attributeProviders.add(heroesManager);
        }

        // We should be done adding attributes now
        finalizeAttributes();

        // Team providers
        if (heroesManager != null && useHeroesParties) {
            teamProviders.add(heroesManager);
        }
        if (skillAPIManager != null && useSkillAPIAllies) {
            teamProviders.add(skillAPIManager);
        }
        if (useScoreboardTeams) {
            teamProviders.add(new ScoreboardTeamProvider());
        }
        if (permissionTeams != null && !permissionTeams.isEmpty()) {
            teamProviders.add(new PermissionsTeamProvider(permissionTeams));
        }
        if (factionsManager != null) {
            teamProviders.add(factionsManager);
        }
        if (battleArenaManager != null && useBattleArenaTeams) {
            teamProviders.add(battleArenaManager);
        }

        // Player warp providers
        if (preciousStonesManager != null && preciousStonesManager.isEnabled()) {
            playerWarpManagers.put("fields", preciousStonesManager);
        }
        if (redProtectManager != null) {
            playerWarpManagers.put("redprotect", redProtectManager);
        }
        if (residenceManager != null) {
            playerWarpManagers.put("residence", residenceManager);
        }

        Runnable genericIntegrationTask = new FinishGenericIntegrationTask(this);

        // Delay loading generic integration by one tick since we can't add depends: for these plugins
        if (!loaded) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, genericIntegrationTask, 1);
        } else {
            genericIntegrationTask.run();
        }
    }

    public void finishGenericIntegration() {
        protectionManager.check();
        if (protectionManager.isEnabled()) {
            blockBreakManagers.add(protectionManager);
            blockBuildManagers.add(protectionManager);
        }
    }

    public void showExampleInstructions(CommandSender sender) {
        Mage mage = getMage(sender);
        List<String> instructions = new ArrayList<>();
        for (String example : getLoadedExamples()) {
            String exampleInstructions = messages.get("examples." + example + ".instructions", "");
            if (exampleInstructions != null && !exampleInstructions.isEmpty()) {
                instructions.add(exampleInstructions);
            }
        }
        if (!instructions.isEmpty()) {
            mage.sendMessage(messages.get("examples.instructions_header"));
            for (String exampleInstructions : instructions) {
                mage.sendMessage(exampleInstructions);
            }
            mage.sendMessage(messages.get("examples.instructions_footer"));
        }
    }

    private int getPathCount() {
        return WandUpgradePath.getPathKeys().size();
    }

    private void loadPaths(ConfigurationSection pathConfiguration) {
        WandUpgradePath.loadPaths(this, pathConfiguration);
    }

    private void loadAttributes(ConfigurationSection attributeConfiguration) {
        Set<String> keys = attributeConfiguration.getKeys(false);
        attributes.clear();
        for (String key : keys) {
            logger.setContext("attribute." + key);
            MagicAttribute attribute = new MagicAttribute(key, attributeConfiguration.getConfigurationSection(key));
            attributes.put(key, attribute);
        }
    }

    private void loadAutomatonTemplates(ConfigurationSection automataConfiguration) {
        Set<String> keys = automataConfiguration.getKeys(false);
        Map<String, ConfigurationSection> templateConfigurations = new HashMap<>();
        automatonTemplates.clear();
        for (String key : keys) {
            logger.setContext("automata." + key);
            ConfigurationSection config = resolveConfiguration(key, automataConfiguration, templateConfigurations);
            if (!ConfigurationUtils.isEnabled(config)) continue;

            AutomatonTemplate template = new AutomatonTemplate(this, key, config);
            automatonTemplates.put(key, template);
        }

        // Update existing automata
        for (Automaton active : activeAutomata.values()) {
            active.pause();
        }
        for (Map<Long, Automaton> chunk : automata.values()) {
            for (Automaton automaton : chunk.values()) {
                automaton.reload();
            }
        }
        for (Automaton active : activeAutomata.values()) {
            active.resume();
        }
    }

    public boolean isAutomataTemplate(@Nonnull String key) {
        return automatonTemplates.containsKey(key);
    }

    @Nonnull
    @Override
    public Collection<String> getAutomatonTemplateKeys() {
        return automatonTemplates.keySet();
    }

    @Nonnull
    public Collection<Automaton> getActiveAutomata() {
        return activeAutomata.values();
    }

    public Automaton getActiveAutomaton(long id) {
        return activeAutomata.get(id);
    }

    public Collection<Automaton> getAutomata() {
        List<Automaton> list = new ArrayList<>();
        for (Map<Long, Automaton> chunk : automata.values()) {
            list.addAll(chunk.values());
        }
        return list;
    }

    public Collection<Mage> getAutomataMages() {
        Collection<Mage> all = new ArrayList<>();
        for (Mage mage : mages.values()) {
            if (mage.isAutomaton()) {
                all.add(mage);
            }
        }
        return all;
    }

    public boolean isActive(@Nonnull Automaton automaton) {
        return activeAutomata.containsKey(automaton.getId());
    }

    @Nullable
    public Automaton getAutomatonAt(@Nonnull Location location) {
        String chunkId = getChunkKey(location);
        if (chunkId == null) {
            return null;
        }

        Map<Long, Automaton> restoreChunk = automata.get(chunkId);
        if (restoreChunk == null) {
            return null;
        }

        long blockId = BlockData.getBlockId(location);
        return restoreChunk.get(blockId);
    }

    @Nullable
    public AutomatonTemplate getAutomatonTemplate(String key) {
        return automatonTemplates.get(key);
    }

    private void loadEffects(ConfigurationSection effectsNode) {
        effects.clear();
        Collection<String> effectKeys = effectsNode.getKeys(false);
        for (String effectKey : effectKeys) {
            logger.setContext("effects." + effectKey);
            effects.put(effectKey, loadEffects(effectsNode, effectKey));
        }
    }

    @Override
    @Nullable
    public Collection<EffectPlayer> loadEffects(ConfigurationSection configuration, String effectKey) {
        return loadEffects(configuration, effectKey, null);
    }

    @Override
    @Nullable
    public Collection<EffectPlayer> loadEffects(ConfigurationSection configuration, String effectKey, String logContext) {
        if (configuration.isString(effectKey)) {
            return getEffects(configuration.getString(effectKey));
        }
        return com.elmakers.mine.bukkit.effect.EffectPlayer.loadEffects(getPlugin(), configuration, effectKey, getLogger(), logContext);
    }

    public void resetLoading(CommandSender sender) {
        synchronized (logger) {
            com.elmakers.mine.bukkit.effect.EffectPlayer.debugEffects(debugEffectLib);
            if (sender != null) {
                List<LogMessage> errors = logger.getErrors();
                List<LogMessage> warnings = logger.getWarnings();

                if (!warnings.isEmpty()) {
                    if (warnings.size() == 1) {
                        sender.sendMessage(ChatColor.YELLOW + "WARNING: " + ChatColor.WHITE + warnings.get(0).getMessage());
                    } else {
                        sender.sendMessage(ChatColor.YELLOW + "WARNINGS: " + ChatColor.WHITE + warnings.size());
                        for (int i = 0; i < warnings.size() && i < MAX_WARNINGS; i++) {
                            sender.sendMessage(ChatColor.WHITE + " " + warnings.get(i).getMessage());
                        }
                        if (warnings.size() > MAX_WARNINGS) {
                            sender.sendMessage(ChatColor.GRAY + "  ...");
                        }
                    }
                }

                if (!errors.isEmpty()) {
                    if (errors.size() == 1) {
                        sender.sendMessage(ChatColor.RED + "ERROR: " + ChatColor.WHITE + errors.get(0).getMessage());
                    } else {
                        sender.sendMessage(ChatColor.RED + "ERRORS: " + ChatColor.WHITE + errors.size());
                        for (int i = 0; i < errors.size() && i < MAX_ERRORS; i++) {
                            sender.sendMessage(ChatColor.WHITE + " " + errors.get(i).getMessage());
                        }
                        if (errors.size() > MAX_ERRORS) {
                            sender.sendMessage(ChatColor.GRAY + "  ...");
                        }
                    }
                }
                if (warnings.isEmpty() && errors.isEmpty()) {
                    sender.sendMessage(ChatColor.GREEN + "Finished loading, No issues found!");
                } else {
                    if (!errors.isEmpty()) {
                        sender.sendMessage(ChatColor.RED + "Finished loading " + ChatColor.DARK_RED + "with errors");
                    } else {
                        sender.sendMessage(ChatColor.GOLD + "Finished loading " + ChatColor.YELLOW + "with warnings");
                    }
                }
            }

            logger.enableCapture(false);
             if (logWatchdogTimer != null) {
                 logWatchdogTimer.cancel();
                 logWatchdogTimer = null;
             }
        }
    }

    @Override
    public void loadConfigurationQuietly(CommandSender sender) {
        loadConfiguration(sender, false, false);
    }

    public void loadConfiguration() {
        loadConfiguration(null);
    }

    public void loadConfiguration(CommandSender sender) {
        if (sender != null && !loaded) {
            getLogger().warning("Can't reload configuration, Magic did not start up properly. Please restart your server.");
            return;
        }
        loadConfiguration(sender, false);
    }

    public void loadConfiguration(CommandSender sender, boolean forceSynchronous) {
        loadConfiguration(sender, forceSynchronous, true);
    }

    public void loadConfiguration(CommandSender sender, boolean forceSynchronous, boolean verboseLogging) {
        if (!plugin.isEnabled()) return;
        if (sender != null) {
            synchronized (logger) {
                 com.elmakers.mine.bukkit.effect.EffectPlayer.debugEffects(true);
                 logger.enableCapture(true);
                 if (logWatchdogTimer != null) {
                     logWatchdogTimer.cancel();
                 }
                 logWatchdogTimer = plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, new LogWatchdogTask(this, sender), LOG_WATCHDOG_TIMEOUT / 50);
                 sender.sendMessage(ChatColor.DARK_AQUA + "Please wait while the configuration is reloaded and validated");
            }
        }
        reloadVerboseLogging = verboseLogging;
        loading = true;
        ConfigurationLoadTask loadTask = new ConfigurationLoadTask(this, sender);
        loadTask.setVerbose(verboseLogging);
        if (loaded && !forceSynchronous) {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, loadTask);
        } else {
            loadTask.runNow();
        }
    }

    public void loadConfigurationExamples(CommandSender sender) {
        showExampleInstructions = true;
        loadConfiguration(sender, false, false);
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
        loadData();
    }

    protected void loadData() {
        loadSpellData();
        Bukkit.getScheduler().runTaskLater(plugin, new LoadDataTask(this), 2);
    }

    public void finishLoadData() {
        if (!loaded) {
            getLogger().info("Magic did not load properly, skipping data load");
            return;
        }
        loadSpellData();
        loadLostWands();
        loadAutomata();
        loadNPCs();

        // Load URL Map Data
        try {
            maps.resetAll();
            maps.loadConfiguration();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        ConfigurationSection warps = loadDataFile(WARPS_FILE);
        if (warps != null) {
            warpController.load(warps);
            info("Loaded " + warpController.getCustomWarps().size() + " warps");
        }

        getLogger().info("Finished loading data.");
        dataLoaded = true;
    }

    public void migratePlayerData(CommandSender sender) {
        if (migrateDataTask == null) {
            if (migrateDataStore != null) {
                migrateDataTask = new MigrateDataTask(this, mageDataStore, migrateDataStore, sender);
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, migrateDataTask);
            } else {
                sender.sendMessage(ChatColor.RED + "You must first configure 'migrate_data_store' in config.yml");
            }
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Data migration is already in progress");
        }
    }

    public void finishMigratingPlayerData() {
        migrateDataTask = null;
    }

    public void checkForMigration() {
        checkForMigration(plugin.getServer().getConsoleSender());
    }

    public void checkForMigration(CommandSender sender) {
        if (migrateDataStore != null) {
            Collection<String> ids = migrateDataStore.getAllIds();
            if (ids.isEmpty()) {
                sender.sendMessage(ChatColor.RED + "Migration is complete, please remove migrate_data_store from config.yml");
            } else {
                sender.sendMessage(ChatColor.YELLOW + "Please use the command 'magic migrate' to migrate player data");
            }
        }
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

        info("Loaded " + lostWands.size() + " lost wands");
    }

    public void checkNPCs(World world) {
        if (this.invalidNPCs.isEmpty()) return;
        List<ConfigurationSection> check = this.invalidNPCs;
        this.invalidNPCs = new ArrayList<>();
        int npcCount = loadNPCs(check);
        if (npcCount > 0) {
            info("Loaded " + npcCount + " NPCs in world " + world.getName());
            for (Chunk chunk : world.getLoadedChunks()) {
                restoreNPCs(chunk);
            }
        }
    }

    protected void loadNPCs() {
        ConfigurationSection npcData = loadDataFile(NPC_DATA_FILE);
        if (npcData != null) {
            Collection<ConfigurationSection> list = ConfigurationUtils.getNodeList(npcData, "npcs");
            int npcCount = loadNPCs(list);
            if (npcCount > 0) {
                for (World world : Bukkit.getWorlds()) {
                    for (Chunk chunk : world.getLoadedChunks()) {
                        restoreNPCs(chunk);
                    }
                }
                info("Loaded " + npcCount + " NPCs");
            }
        }
    }

    protected int loadNPCs(Collection<ConfigurationSection> list) {
        int npcCount = 0;
        try {
            for (ConfigurationSection node : list) {
                MagicNPC npc = new MagicNPC(this, node);
                if (!npc.isValid()) {
                    invalidNPCs.add(node);
                    continue;
                }

                String chunkId = getChunkKey(npc.getLocation());
                if (chunkId == null) {
                    invalidNPCs.add(node);
                    continue;
                }

                List<MagicNPC> restoreChunk = npcsByChunk.get(chunkId);
                if (restoreChunk == null) {
                    restoreChunk = new ArrayList<>();
                    npcsByChunk.put(chunkId, restoreChunk);
                }

                npcCount++;
                restoreChunk.add(npc);
                npcs.put(npc.getId(), npc);
            }
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, "Something went wrong loading NPC data", ex);
        }
        return npcCount;
    }

    public void checkAutomata(World world) {
        if (this.invalidAutomata.isEmpty()) return;
        List<ConfigurationSection> check = this.invalidAutomata;
        this.invalidAutomata = new ArrayList<>();
        int automataCount = loadAutomata(check);
        if (automataCount > 0) {
            info("Loaded " + automataCount + " automata in world " + world.getName());
            for (Chunk chunk : world.getLoadedChunks()) {
                resumeAutomata(chunk);
            }
        }
    }

    protected void loadAutomata() {
        ConfigurationSection toggleBlockData = loadDataFile(AUTOMATA_DATA_FILE);
        if (toggleBlockData != null) {
            Collection<ConfigurationSection> list = ConfigurationUtils.getNodeList(toggleBlockData, "automata");
            int automataCount = loadAutomata(list);
            if (automataCount > 0) {
                info("Loaded " + automataCount + " automata");
                for (World world : Bukkit.getWorlds()) {
                    for (Chunk chunk : world.getLoadedChunks()) {
                        resumeAutomata(chunk);
                    }
                }
            }
        }
    }

    protected int loadAutomata(Collection<ConfigurationSection> list) {
        int automataCount = 0;
        try {
            for (ConfigurationSection node : list) {
                Automaton automaton = new Automaton(this, node);
                if (!automaton.isValid()) {
                    invalidAutomata.add(node);
                    continue;
                }

                String chunkId = getChunkKey(automaton.getLocation());
                if (chunkId == null) {
                    invalidAutomata.add(node);
                    continue;
                }

                Map<Long, Automaton> restoreChunk = automata.get(chunkId);
                if (restoreChunk == null) {
                    restoreChunk = new HashMap<>();
                    automata.put(chunkId, restoreChunk);
                }

                long id = automaton.getId();
                Automaton existing = restoreChunk.get(id);
                if (existing != null) {
                    getLogger().warning("Duplicate automata exist at " + automaton.getLocation() + ", one will be removed!");
                    continue;
                }

                automataCount++;
                restoreChunk.put(id, automaton);
            }
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, "Something went wrong loading automata data", ex);
        }
        return automataCount;
    }

    protected void saveWarps(Collection<YamlDataFile> stores) {
        try {
            YamlDataFile warpData = createDataFile(WARPS_FILE);
            warpController.save(warpData);
            stores.add(warpData);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected void saveAutomata(Collection<YamlDataFile> stores) {
        try {
            YamlDataFile automataData = createDataFile(AUTOMATA_DATA_FILE);
            List<ConfigurationSection> nodes = new ArrayList<>();
            for (Entry<String, Map<Long, Automaton>> toggleEntry : automata.entrySet()) {
                Collection<Automaton> blocks = toggleEntry.getValue().values();
                if (blocks.size() > 0) {
                    for (Automaton block : blocks) {
                        ConfigurationSection node = ConfigurationUtils.newConfigurationSection();
                        block.save(node);
                        nodes.add(node);
                    }
                }
            }
            nodes.addAll(invalidAutomata);
            automataData.set("automata", nodes);
            stores.add(automataData);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected void saveNPCs(Collection<YamlDataFile> stores) {
        try {
            YamlDataFile npcData = createDataFile(NPC_DATA_FILE);
            List<ConfigurationSection> nodes = new ArrayList<>();
            for (MagicNPC npc : npcs.values()) {
                ConfigurationSection node = ConfigurationUtils.newConfigurationSection();
                npc.save(node);
                nodes.add(node);
            }
            nodes.addAll(invalidNPCs);
            npcData.set("npcs", nodes);
            stores.add(npcData);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void moveAutomaton(Automaton automaton, Location location) {
        unregisterAutomaton(automaton);
        automaton.setLocation(location);
        registerAutomaton(automaton);
    }

    public void registerAutomaton(Automaton automaton) {
        String chunkId = getChunkKey(automaton.getLocation());
        if (chunkId == null) return;

        Map<Long, Automaton> chunkAutomata = automata.get(chunkId);
        if (chunkAutomata == null) {
            chunkAutomata = new HashMap<>();
            automata.put(chunkId, chunkAutomata);
        }
        long id = automaton.getId();
        chunkAutomata.put(id, automaton);

        if (CompatibilityUtils.isChunkLoaded(automaton.getLocation())) {
            activeAutomata.put(id, automaton);
            automaton.resume();
        }
    }

    public boolean unregisterAutomaton(Automaton automaton) {
        boolean removed = false;
        String chunkId = getChunkKey(automaton.getLocation());
        long id = automaton.getId();
        Map<Long, Automaton> chunkAutomata = automata.get(chunkId);
        if (chunkAutomata != null) {
            removed = chunkAutomata.remove(id) != null;
            if (chunkAutomata.size() == 0) {
                automata.remove(chunkId);
            }
        }
        if (activeAutomata.remove(id) != null) {
            automaton.pause();
        }

        return removed;
    }

    public void resumeAutomata(final Chunk chunk) {
        String chunkKey = getChunkKey(chunk);
        Map<Long, Automaton> chunkData = automata.get(chunkKey);
        if (chunkData != null) {
            activeAutomata.putAll(chunkData);
            for (Automaton automata : chunkData.values()) {
                automata.resume();
            }
        }
    }

    public void pauseAutomata(final Chunk chunk) {
        String chunkKey = getChunkKey(chunk);
        Map<Long, Automaton> chunkData = automata.get(chunkKey);
        if (chunkData != null) {
            for (Automaton automata : chunkData.values()) {
                automata.pause();
            }
            activeAutomata.keySet().removeAll(chunkData.keySet());
        }
    }

    public void tickAutomata() {
        for (Automaton automaton : activeAutomata.values()) {
            automaton.tick();
        }
    }

    protected void saveSpellData(Collection<YamlDataFile> stores) {
        String lastKey = "";
        try {
            YamlDataFile spellsDataFile = createDataFile(SPELLS_DATA_FILE, false);
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
            YamlDataFile lostWandsConfiguration = createDataFile(LOST_WANDS_FILE, false);
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

    @Nullable
    protected String getChunkKey(Block block) {
        return getChunkKey(block.getLocation());
    }

    @Nullable
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
            if (removeMarker("wand-" + wandId, "wands")) {
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

    protected void saveMageData(Collection<MageData> stores) {
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
        if (!loaded || !dataLoaded) return;
        maps.save(asynchronous);

        final List<YamlDataFile> saveData = new ArrayList<>();
        final List<MageData> saveMages = new ArrayList<>();

        // Player data will be saved as each player quits on shutdown, so skip it here.
        if (savePlayerData && mageDataStore != null && !shuttingDown) {
            saveMageData(saveMages);
            info("Saving " + saveMages.size() + " players");
        }
        saveSpellData(saveData);
        saveLostWands(saveData);
        saveAutomata(saveData);
        saveWarps(saveData);
        saveNPCs(saveData);

        if (mageDataStore != null && !shuttingDown) {
            if (asynchronous) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, new SaveMageDataTask(this, saveMages));
            } else {
                persistMageData(saveMages);
            }
        }

        if (asynchronous) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new SaveDataTask(this, saveData));
        } else {
            saveData(saveData);
        }

        SaveEvent saveEvent = new SaveEvent(asynchronous);
        Bukkit.getPluginManager().callEvent(saveEvent);
    }

    public void saveData(Collection<YamlDataFile> saveData) {
        synchronized (saveLock) {
            for (YamlDataFile config : saveData) {
                config.save();
            }
            info("Finished saving");
        }
    }

    public void persistMageData(Collection<MageData> saveMages) {
        synchronized (saveLock) {
            for (MageData mageData : saveMages) {
                mageDataStore.save(mageData, null, false);
            }
        }
    }

    protected void loadSpells(CommandSender sender, ConfigurationSection spellConfigs)
    {
        if (spellConfigs == null) return;

        // Reset existing spells.
        spells.clear();
        spellAliases.clear();
        categories.clear();

        Set<String> keys = spellConfigs.getKeys(false);
        for (String key : keys)
        {
            if (key.equals("default") || key.equals("override")) continue;
            logger.setContext("spells." + key);

            ConfigurationSection spellNode = spellConfigs.getConfigurationSection(key);
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
        for (String key : keys) {
            logger.setContext("spells." + key);
            SpellTemplate template = getSpellTemplate(key);
            if (template != null) {
                template.loadPrerequisites(spellConfigs.getConfigurationSection(key));
            }
        }

        // Update registered mages so their spells are current
        for (Mage mage : mages.values()) {
            if (mage instanceof com.elmakers.mine.bukkit.magic.Mage) {
                ((com.elmakers.mine.bukkit.magic.Mage)mage).loadSpells(spellConfigs);
            }
        }
    }

    @Nullable
    public static Spell loadSpell(String name, ConfigurationSection node, MageController controller) {
        String className = node.getString("class");
        if (className == null || className.equalsIgnoreCase("action") || className.equalsIgnoreCase("actionspell"))
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
        if (spellClass.getAnnotation(Deprecated.class) != null) {
            controller.getLogger().warning("Spell " + name + " is using a deprecated spell class " + className + ". This will be removed in the future, please see the default configs for alternatives.");
        }

        Object newObject;
        try
        {
            newObject = spellClass.getDeclaredConstructor().newInstance();
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

    @Nullable
    @Override
    public String getReflectiveMaterials(Mage mage, Location location) {
        return worldGuardManager.getReflective(mage.getPlayer(), location);
    }

    @Nullable
    @Override
    public String getDestructibleMaterials(Mage mage, Location location) {
        return worldGuardManager.getDestructible(mage.getPlayer(), location);
    }

    @Override
    @Deprecated
    public Set<Material> getDestructibleMaterials() {
        return MaterialSets.toLegacyNN(destructibleMaterials);
    }

    @Nullable
    @Override
    public Set<String> getSpellOverrides(Mage mage, Location location) {
        return worldGuardManager.getSpellOverrides(mage.getPlayer(), location);
    }

    protected void loadMaterials(ConfigurationSection materialNode) {
        if (materialNode == null)
            return;

        materialSetManager.loadMaterials(materialNode);
        DefaultMaterials defaultMaterials = DefaultMaterials.getInstance();
        defaultMaterials.initialize(materialSetManager);
        defaultMaterials.loadColors(materialColors);
        defaultMaterials.loadVariants(materialVariants);
        defaultMaterials.loadBlockItems(blockItems);
        defaultMaterials.setPlayerSkullItem(skullItems.get(EntityType.PLAYER));
        defaultMaterials.setPlayerSkullWallBlock(skullWallBlocks.get(EntityType.PLAYER));
        defaultMaterials.setSkeletonSkullItem(skullItems.get(EntityType.SKELETON));

        buildingMaterials = materialSetManager.getMaterialSetEmpty("building");
        indestructibleMaterials = materialSetManager
                .getMaterialSetEmpty("indestructible");
        restrictedMaterials = materialSetManager
                .getMaterialSetEmpty("restricted");
        destructibleMaterials = materialSetManager
                .getMaterialSetEmpty("destructible");
        interactibleMaterials = materialSetManager
                .getMaterialSetEmpty("interactible");
        containerMaterials = materialSetManager
                .getMaterialSetEmpty("containers");
        climbableMaterials = materialSetManager.getMaterialSetEmpty("climbable");
        undoableMaterials = materialSetManager.getMaterialSetEmpty("undoable");
        wearableMaterials = materialSetManager.getMaterialSetEmpty("wearable");
        meleeMaterials = materialSetManager.getMaterialSetEmpty("melee");
        com.elmakers.mine.bukkit.block.UndoList.attachables = materialSetManager
                .getMaterialSetEmpty("attachable");
        com.elmakers.mine.bukkit.block.UndoList.attachablesWall = materialSetManager
                .getMaterialSetEmpty("attachable_wall");
        com.elmakers.mine.bukkit.block.UndoList.attachablesDouble = materialSetManager
                .getMaterialSetEmpty("attachable_double");
    }

    public boolean hasAddedExamples() {
        return addExamples != null && addExamples.size() > 0;
    }

    protected void loadProperties(CommandSender sender, ConfigurationSection properties)
    {
        if (properties == null) return;

        // Delegate to resource pack handler
        resourcePacks.load(properties, sender, !loaded);

        logVerbosity = properties.getInt("log_verbosity", 0);
        SkinUtils.DEBUG = logVerbosity >= 5;
        LOG_WATCHDOG_TIMEOUT = properties.getInt("load_watchdog_timeout", 30000);
        logger.setColorize(properties.getBoolean("colored_logs", true));

        // Cancel any pending save tasks
        if (autoSaveTaskId > 0) {
            Bukkit.getScheduler().cancelTask(autoSaveTaskId);
            autoSaveTaskId = 0;
        }
        if (configCheckTask != null) {
            configCheckTask.cancel();
            configCheckTask = null;
        }
        if (logNotifyTask != null) {
            logNotifyTask.cancel();
            logNotifyTask = null;
        }

        debugEffectLib = properties.getBoolean("debug_effects", false);
        com.elmakers.mine.bukkit.effect.EffectPlayer.debugEffects(debugEffectLib);
        boolean effectLibStackTraces = properties.getBoolean("debug_effects_stack_traces", false);
        com.elmakers.mine.bukkit.effect.EffectPlayer.showStackTraces(effectLibStackTraces);
        CompatibilityUtils.USE_MAGIC_DAMAGE = properties.getBoolean("use_magic_damage", CompatibilityUtils.USE_MAGIC_DAMAGE);
        com.elmakers.mine.bukkit.effect.EffectPlayer.setParticleRange(properties.getInt("particle_range", com.elmakers.mine.bukkit.effect.EffectPlayer.PARTICLE_RANGE));

        showCastHoloText = properties.getBoolean("show_cast_holotext", showCastHoloText);
        showActivateHoloText = properties.getBoolean("show_activate_holotext", showCastHoloText);
        castHoloTextRange = properties.getInt("cast_holotext_range", castHoloTextRange);
        activateHoloTextRange = properties.getInt("activate_holotext_range", activateHoloTextRange);
        urlIconsEnabled = properties.getBoolean("url_icons_enabled", urlIconsEnabled);
        legacyIconsEnabled = properties.getBoolean("legacy_icons_enabled", legacyIconsEnabled);
        spellProgressionEnabled = properties.getBoolean("enable_spell_progression", spellProgressionEnabled);
        autoSpellUpgradesEnabled = properties.getBoolean("enable_automatic_spell_upgrades", autoSpellUpgradesEnabled);
        autoPathUpgradesEnabled = properties.getBoolean("enable_automatic_spell_upgrades", autoPathUpgradesEnabled);
        undoQueueDepth = properties.getInt("undo_depth", undoQueueDepth);
        workPerUpdate = properties.getInt("work_per_update", workPerUpdate);
        workFrequency = properties.getInt("work_frequency", workFrequency);
        automataUpdateFrequency = properties.getInt("automata_update_frequency", automataUpdateFrequency);
        mageUpdateFrequency = properties.getInt("mage_update_frequency", mageUpdateFrequency);
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
        Wand.FILL_CREATOR = properties.getBoolean("fill_wand_creator", Wand.FILL_CREATOR);
        Wand.CREATIVE_CHEST_MODE = properties.getBoolean("wand_creative_chest_switch", Wand.CREATIVE_CHEST_MODE);
        maxFillLevel = properties.getInt("fill_wand_level", maxFillLevel);
        welcomeWand = properties.getString("welcome_wand", "");
        maxDamagePowerMultiplier = (float)properties.getDouble("max_power_damage_multiplier", maxDamagePowerMultiplier);
        maxConstructionPowerMultiplier = (float)properties.getDouble("max_power_construction_multiplier", maxConstructionPowerMultiplier);
        maxRangePowerMultiplier = (float)properties.getDouble("max_power_range_multiplier", maxRangePowerMultiplier);
        maxRangePowerMultiplierMax = (float)properties.getDouble("max_power_range_multiplier_max", maxRangePowerMultiplierMax);
        maxRadiusPowerMultiplier = (float)properties.getDouble("max_power_radius_multiplier", maxRadiusPowerMultiplier);
        maxRadiusPowerMultiplierMax = (float)properties.getDouble("max_power_radius_multiplier_max", maxRadiusPowerMultiplierMax);
        materialColors = ConfigurationUtils.getNodeList(properties, "material_colors");
        materialVariants = ConfigurationUtils.getList(properties, "material_variants");
        blockItems = properties.getConfigurationSection("block_items");
        currencyConfiguration = properties.getConfigurationSection("custom_currency");
        loadBlockSkins(properties.getConfigurationSection("block_skins"));
        loadMobSkins(properties.getConfigurationSection("mob_skins"));
        loadMobEggs(properties.getConfigurationSection("mob_eggs"));
        loadSkulls(properties.getConfigurationSection("skulls"));
        loadOtherMaterials(properties);
        WandCommandExecutor.CONSOLE_BYPASS_LOCKED = properties.getBoolean("console_bypass_locked_wands", true);

        maxPower = (float)properties.getDouble("max_power", maxPower);
        ConfigurationSection damageTypes = properties.getConfigurationSection("damage_types");
        if (damageTypes != null) {
            Set<String> typeKeys = damageTypes.getKeys(false);
            for (String typeKey : typeKeys) {
                ConfigurationSection damageType = damageTypes.getConfigurationSection(typeKey);
                this.damageTypes.put(typeKey, new DamageType(damageType));
            }
        }
        maxCostReduction = (float)properties.getDouble("max_cost_reduction", maxCostReduction);
        maxCooldownReduction = (float)properties.getDouble("max_cooldown_reduction", maxCooldownReduction);
        maxMana = properties.getInt("max_mana", maxMana);
        maxManaRegeneration = properties.getInt("max_mana_regeneration", maxManaRegeneration);
        worthSkillPoints = properties.getDouble("worth_sp", 1);
        skillPointIcon = properties.getString("sp_item_icon_url");
        skillPointItemsEnabled = properties.getBoolean("sp_items_enabled", true);
        worthBase = properties.getDouble("worth_base", 1);
        worthXP = properties.getDouble("worth_xp", 1);
        com.elmakers.mine.bukkit.item.ItemData.EARN_SCALE = properties.getDouble("default_earn_scale", 0.5);
        ConfigurationSection currencies = properties.getConfigurationSection("currency");
        if (currencies != null)
        {
            Collection<String> worthItemKeys = currencies.getKeys(false);
            for (String worthItemKey : worthItemKeys) {
                ConfigurationSection currencyConfig = currencies.getConfigurationSection(worthItemKey);
                if (!currencyConfig.getBoolean("enabled", true)) continue;
                MaterialAndData material = new MaterialAndData(worthItemKey);
                ItemStack worthItemType = material.getItemStack(1);
                double worthItemAmount = currencyConfig.getDouble("worth");
                String worthItemName = currencyConfig.getString("name");
                String worthItemNamePlural = currencyConfig.getString("name_plural");

                currencyItem = new CurrencyItem(worthItemType, worthItemAmount, worthItemName, worthItemNamePlural);

                // This is kind of a hack, but makes it easier to override the default ...
                if (!worthItemKey.equals("emerald")) {
                    break;
                }
            }
        }
        else
        {
            currencyItem = null;
        }

        SafetyUtils.MAX_VELOCITY = properties.getDouble("max_velocity", 10);
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

        // These were changed from set values to multipliers, we're going to translate for backwards compatibility.
        // The default configs used to have these set to either 0 or 100, where 100 indicated that we should be
        // turning off the costs/cooldowns.

        if (properties.contains("cast_command_cost_reduction")) {
            castCommandCostFree = (properties.getDouble("cast_command_cost_reduction") > 0);
        } else {
            castCommandCostFree = properties.getBoolean("cast_command_cost_free", castCommandCostFree);
        }
        if (properties.contains("cast_command_cooldown_reduction")) {
            castCommandCooldownFree = (properties.getDouble("cast_command_cooldown_reduction") > 0);
        } else {
            castCommandCooldownFree =  properties.getBoolean("cast_command_cooldown_free", castCommandCooldownFree);
        }
        if (properties.contains("cast_console_cost_reduction")) {
            castConsoleCostFree = (properties.getDouble("cast_console_cost_reduction") > 0);
        } else {
            castConsoleCostFree = properties.getBoolean("cast_console_cost_free", castConsoleCostFree);
        }
        if (properties.contains("cast_console_cooldown_reduction")) {
            castConsoleCooldownFree = (properties.getDouble("cast_console_cooldown_reduction") > 0);
        } else {
            castConsoleCooldownFree = properties.getBoolean("cast_console_cooldown_free", castConsoleCooldownFree);
        }

        castConsoleFeedback = properties.getBoolean("cast_console_feedback", false);
        editorURL = properties.getString("editor_url");

        castCommandPowerMultiplier = (float)properties.getDouble("cast_command_power_multiplier", castCommandPowerMultiplier);
        castConsolePowerMultiplier = (float)properties.getDouble("cast_console_power_multiplier", castConsolePowerMultiplier);

        maps.setAnimationAllowed(properties.getBoolean("enable_map_animations", true));
        costReduction = (float)properties.getDouble("cost_reduction", costReduction);
        cooldownReduction = (float)properties.getDouble("cooldown_reduction", cooldownReduction);
        autoUndo = properties.getInt("auto_undo", autoUndo);
        spellDroppingEnabled = properties.getBoolean("allow_spell_dropping", spellDroppingEnabled);
        essentialsSignsEnabled = properties.getBoolean("enable_essentials_signs", essentialsSignsEnabled);
        logBlockEnabled = properties.getBoolean("logblock_enabled", logBlockEnabled);
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
        skillsSpell = properties.getString("mskills_spell", skillsSpell);
        InventoryUtils.MAX_LORE_LENGTH = properties.getInt("lore_wrap_limit", InventoryUtils.MAX_LORE_LENGTH);
        libsDisguiseEnabled = properties.getBoolean("enable_libsdisguises", libsDisguiseEnabled);
        skillAPIEnabled = properties.getBoolean("skillapi_enabled", skillAPIEnabled);
        useSkillAPIMana = properties.getBoolean("use_skillapi_mana", useSkillAPIMana);
        placeholdersEnabled = properties.getBoolean("placeholder_api_enabled", placeholdersEnabled);
        lightAPIEnabled = properties.getBoolean("light_api_enabled", lightAPIEnabled);
        skriptEnabled = properties.getBoolean("skript_enabled", skriptEnabled);
        vaultEnabled = properties.getConfigurationSection("vault").getBoolean("enabled");
        citadelConfiguration = properties.getConfigurationSection("citadel");
        mobArenaConfiguration = properties.getConfigurationSection("mobarena");
        residenceConfiguration = properties.getConfigurationSection("residence");
        redProtectConfiguration = properties.getConfigurationSection("redprotect");
        ajParkourConfiguration = properties.getConfigurationSection("ajparkour");
        if (mobArenaManager != null) {
            mobArenaManager.configure(mobArenaConfiguration);
        }
        String swingTypeString = properties.getString("left_click_type");
        try {
            swingType = SwingType.valueOf(swingTypeString.toUpperCase());
        } catch (Exception ex) {
            getLogger().warning("Invalid left_click_type: " + swingTypeString);
        }

        List<? extends Object> permissionTeams = properties.getList("permission_teams");
        if (permissionTeams != null) {
            this.permissionTeams = new ArrayList<>();
            for (Object o : permissionTeams) {
                if (o instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> stringList = (List<String>)o;
                    this.permissionTeams.add(stringList);
                } else if (o instanceof String) {
                    List<String> newList = new ArrayList<>();
                    newList.add((String)o);
                    this.permissionTeams.add(newList);
                }
            }
        }

        String defaultSpellIcon = properties.getString("default_spell_icon");
        try {
            BaseSpell.DEFAULT_SPELL_ICON = Material.valueOf(defaultSpellIcon.toUpperCase());
        } catch (Exception ex) {
            getLogger().warning("Invalid default_spell_icon: " + defaultSpellIcon);
        }

        skillsUseHeroes = properties.getBoolean("skills_use_heroes", skillsUseHeroes);
        useHeroesParties = properties.getBoolean("use_heroes_parties", useHeroesParties);
        useSkillAPIAllies = properties.getBoolean("use_skillapi_allies", useSkillAPIAllies);
        useBattleArenaTeams = properties.getBoolean("use_battlearena_teams", useBattleArenaTeams);
        useHeroesMana = properties.getBoolean("use_heroes_mana", useHeroesMana);
        heroesSkillPrefix = properties.getString("heroes_skill_prefix", heroesSkillPrefix);
        skillsUsePermissions = properties.getBoolean("skills_use_permissions", skillsUsePermissions);

        messagePrefix = properties.getString("message_prefix", messagePrefix);
        castMessagePrefix = properties.getString("cast_message_prefix", castMessagePrefix);
        Messages.RANGE_FORMATTER = new DecimalFormat(properties.getString("range_formatter"));
        Messages.MOMENT_SECONDS_FORMATTER = new DecimalFormat(properties.getString("moment_seconds_formatter"));
        Messages.MOMENT_MILLISECONDS_FORMATTER = new DecimalFormat(properties.getString("moment_milliseconds_formatter"));
        Messages.SECONDS_FORMATTER = new DecimalFormat(properties.getString("seconds_formatter"));
        Messages.MINUTES_FORMATTER = new DecimalFormat(properties.getString("minutes_formatter"));
        Messages.HOURS_FORMATTER = new DecimalFormat(properties.getString("hours_formatter"));

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
        useWildStacker = properties.getBoolean("wildstacker.enabled", true);
        com.elmakers.mine.bukkit.magic.Mage.DEFAULT_CLASS = properties.getString("default_mage_class", "");

        metricsLevel = properties.getInt("metrics_level", metricsLevel);

        ConfigurationSection autoWandsConfig = properties.getConfigurationSection("auto_wands");
        Set<String> autoWandsKeys = autoWandsConfig.getKeys(false);
        autoWands.clear();
        for (String autoWandKey : autoWandsKeys) {
            try {
                Material autoWandMaterial = Material.valueOf(autoWandKey.toUpperCase());
                autoWands.put(autoWandMaterial, autoWandsConfig.getString(autoWandKey));
            } catch (Exception ex) {
                getLogger().warning("Invalid material in auto_wands config: " + autoWandKey);
            }
        }

        ConfigurationSection builtinExampleConfigs = properties.getConfigurationSection("external_examples");
        Set<String> exampleKeys = builtinExampleConfigs.getKeys(false);
        builtinExternalExamples.clear();
        for (String exampleKey : exampleKeys) {
            builtinExternalExamples.put(exampleKey, builtinExampleConfigs.getString(exampleKey));
        }

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
                Wand.currencyMode = WandManaMode.NUMBER;
            } else {
                Wand.currencyMode = WandManaMode.NONE;
            }
        }
        spEnabled = properties.getBoolean("sp_enabled", true);
        spEarnEnabled = properties.getBoolean("sp_earn_enabled", true);
        spMaximum = properties.getInt("sp_max", 9999);

        populateEntityTypes(undoEntityTypes, properties, "entity_undo_types");
        populateEntityTypes(friendlyEntityTypes, properties, "friendly_entity_types");

        ActionHandler.setRestrictedActions(properties.getStringList("restricted_spell_actions"));

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
        Wand.LiveHotbarCooldown = properties.getBoolean("live_hotbar_cooldown", Wand.LiveHotbarCooldown);
        Wand.LiveHotbarMana = properties.getBoolean("live_hotbar_mana", Wand.LiveHotbarMana);
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
        Wand.Unstashable = properties.getBoolean("wand_undroppable", properties.getBoolean("wand_unstashable", Wand.Unstashable));

        MaterialBrush.CopyMaterial = ConfigurationUtils.getIconMaterialAndData(properties, "copy_item", legacyIconsEnabled, MaterialBrush.CopyMaterial);
        MaterialBrush.EraseMaterial = ConfigurationUtils.getIconMaterialAndData(properties, "erase_item", legacyIconsEnabled, MaterialBrush.EraseMaterial);
        MaterialBrush.CloneMaterial = ConfigurationUtils.getIconMaterialAndData(properties, "clone_item", legacyIconsEnabled, MaterialBrush.CloneMaterial);
        MaterialBrush.ReplicateMaterial = ConfigurationUtils.getIconMaterialAndData(properties, "replicate_item", legacyIconsEnabled, MaterialBrush.ReplicateMaterial);
        MaterialBrush.SchematicMaterial = ConfigurationUtils.getIconMaterialAndData(properties, "schematic_item", legacyIconsEnabled, MaterialBrush.SchematicMaterial);
        MaterialBrush.MapMaterial = ConfigurationUtils.getIconMaterialAndData(properties, "map_item", legacyIconsEnabled, MaterialBrush.MapMaterial);
        MaterialBrush.DefaultBrushMaterial = ConfigurationUtils.getIconMaterialAndData(properties, "default_brush_item", legacyIconsEnabled, MaterialBrush.DefaultBrushMaterial);
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
        Wand.itemPickupSound = ConfigurationUtils.toSoundEffect(properties.getString("wand_pickup_item_sound"));

        // Configure sub-controllers
        explosionController.loadProperties(properties);
        inventoryController.loadProperties(properties);
        entityController.loadProperties(properties);
        playerController.loadProperties(properties);
        blockController.loadProperties(properties);

        // Set up other systems
        com.elmakers.mine.bukkit.effect.EffectPlayer.SOUNDS_ENABLED = soundsEnabled;

        // Set up auto-save timer
        int autoSaveIntervalTicks = properties.getInt("auto_save", 0) * 20 / 1000;
        if (autoSaveIntervalTicks > 1) {
            final AutoSaveTask autoSave = new AutoSaveTask(this);
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
        isFileLockingEnabled = properties.getBoolean("use_file_locking", false);
        fileLoadDelay = properties.getInt("file_load_delay", 0);
        despawnMagicMobs = properties.getBoolean("despawn_magic_mobs", false);
        MobController.REMOVE_INVULNERABLE = properties.getBoolean("remove_invulnerable_mobs", false);
        com.elmakers.mine.bukkit.effect.EffectPlayer.ENABLE_VANILLA_SOUNDS = properties.getBoolean("enable_vanilla_sounds", true);

        if (mageDataStore != null) {
            mageDataStore.close();
        }

        ConfigurationSection mageDataStoreConfiguration = properties.getConfigurationSection("player_data_store");
        if (mageDataStoreConfiguration != null) {
            mageDataStore = loadMageDataStore(mageDataStoreConfiguration);
            if (mageDataStore == null) {
                getLogger().log(Level.WARNING, "Failed to load player_data_store configuration, player data saving disabled!");
            }
        } else {
            getLogger().log(Level.WARNING, "Missing player_data_store configuration, player data saving disabled!");
            mageDataStore = null;
        }

        ConfigurationSection migrateDataStoreConfiguration = properties.getConfigurationSection("migrate_data_store");
        if (migrateDataStoreConfiguration != null) {
            migrateDataStore = loadMageDataStore(migrateDataStoreConfiguration);
            if (migrateDataStore == null) {
                getLogger().log(Level.WARNING, "Failed to load migrate_data_store configuration, migration will not work");
            }
        } else {
            migrateDataStore = null;
        }

        if (migrateDataStore != null) {
            migrateDataStore.close();
        }

        // Semi-deprecated Wand defaults
        Wand.DefaultWandMaterial = ConfigurationUtils.getMaterial(properties, "wand_item", Wand.DefaultWandMaterial);
        Wand.EnchantableWandMaterial = ConfigurationUtils.getMaterial(properties, "wand_item_enchantable", Wand.EnchantableWandMaterial);

        // Load sub-controllers
        enchanting.setEnabled(properties.getBoolean("enable_enchanting", enchanting.isEnabled()));
        if (enchanting.isEnabled()) {
            log("Wand enchanting is enabled");
        }
        crafting.loadMainConfiguration(properties);
        if (crafting.isEnabled()) {
            log("Wand crafting is enabled");
        }
        anvil.load(properties);
        if (anvil.isCombiningEnabled()) {
            log("Wand anvil combining is enabled");
        }
        if (anvil.isOrganizingEnabled()) {
            log("Wand anvil organizing is enabled");
        }
        if (isUrlIconsEnabled()) {
            log("Skin-based spell icons enabled");
        } else {
            log("Skin-based spell icons disabled");
        }

        // Set up sandbox config update timer
        int configUpdateInterval = properties.getInt("config_update_interval");
        if (configUpdateInterval > 0) {
            log("Sandbox enabled, will check for updates from the web UI");
            final ConfigCheckTask configCheck = new ConfigCheckTask(this);
            configCheckTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, configCheck,
                configUpdateInterval * 20 / 1000, configUpdateInterval * 20 / 1000);
        }

        // Set up log notify timer
        logger.clearNotify();
        int logNotifyInterval = properties.getInt("log_notify_interval");
        if (logNotifyInterval > 0) {
            final LogNotifyTask logNotify = new LogNotifyTask(this);
            logNotifyTask = Bukkit.getScheduler().runTaskTimer(plugin, logNotify,
                logNotifyInterval * 20 / 1000, logNotifyInterval * 20 / 1000);
        }

        // Configure world generation and spawn replacement
        worldController.load(properties.getConfigurationSection("world_modification"));

        // Link to generic protection plugins
        protectionManager.initialize(plugin, properties.getStringList("generic_protection"));
    }

    @Nullable
    private MageDataStore loadMageDataStore(ConfigurationSection configuration) {
        MageDataStore mageDataStore = null;
        String dataStoreClassName = configuration.getString("class");
        if (!dataStoreClassName.contains(".")) {
            dataStoreClassName = DEFAULT_DATASTORE_PACKAGE + "." + dataStoreClassName + "MageDataStore";
        }
        try {
            Class<?> dataStoreClass = Class.forName(dataStoreClassName);
            Object dataStore = dataStoreClass.getDeclaredConstructor().newInstance();
            if (dataStore == null || !(dataStore instanceof MageDataStore)) {
                getLogger().log(Level.WARNING, "Invalid player_data_store class " + dataStoreClassName + ", does it implement MageDataStore? Player data saving is disabled!");
                mageDataStore = null;
            } else {
                mageDataStore = (MageDataStore)dataStore;
                mageDataStore.initialize(this, configuration);
            }
        } catch (Exception ex) {
            getLogger().log(Level.WARNING, "Failed to create player_data_store class from " + dataStoreClassName, ex);
            mageDataStore = null;
        }

        return mageDataStore;
    }

    protected void loadMobEggs(ConfigurationSection skins) {
        mobEggs.clear();
        Set<String> keys = skins.getKeys(false);
        for (String key : keys) {
            try {
                EntityType entityType = EntityType.valueOf(key.toUpperCase());
                Material material = getVersionedMaterial(skins, key);
                if (material != null) {
                    mobEggs.put(entityType, material);
                }
            } catch (Exception ignore) {
            }
        }
    }

    protected void loadMobSkins(ConfigurationSection skins) {
        mobSkins.clear();
        Set<String> keys = skins.getKeys(false);
        for (String key : keys) {
            try {
                EntityType entityType = EntityType.valueOf(key.toUpperCase());
                mobSkins.put(entityType, skins.getString(key));
            } catch (Exception ignore) {
            }
        }
    }

    protected void loadBlockSkins(ConfigurationSection skins) {
        blockSkins.clear();
        Set<String> keys = skins.getKeys(false);
        for (String key : keys) {
            try {
                Material material = Material.getMaterial(key.toUpperCase());
                blockSkins.put(material, skins.getString(key));
            } catch (Exception ignore) {
            }
        }
    }

    @Nullable
    protected Material getVersionedMaterial(ConfigurationSection configuration, String key) {
        Material material = null;
        Collection<String> candidates = ConfigurationUtils.getStringList(configuration, key);
        for (String candidate : candidates) {
            try {
                material = Material.valueOf(candidate.toUpperCase());
                break;
            } catch (Exception ignore) {
            }
        }
        return material;
    }

    @Nullable
    protected MaterialAndData getVersionedMaterialAndData(ConfigurationSection configuration, String key) {
        Collection<String> candidates = ConfigurationUtils.getStringList(configuration, key);
        for (String candidate : candidates) {
            MaterialAndData test = new MaterialAndData(candidate);
            if (test.isValid()) {
                return test;
            }
        }
        return null;
    }

    protected void loadOtherMaterials(ConfigurationSection configuration) {
        DefaultMaterials defaultMaterials = DefaultMaterials.getInstance();
        defaultMaterials.setGroundSignBlock(getVersionedMaterial(configuration, "ground_sign_block"));
        defaultMaterials.setWallSignBlock(getVersionedMaterial(configuration, "wall_sign_block"));
        defaultMaterials.setFirework(getVersionedMaterial(configuration, "firework"));
        defaultMaterials.setWallTorch(getVersionedMaterialAndData(configuration, "wall_torch"));
        defaultMaterials.setRedstoneTorchOn(getVersionedMaterialAndData(configuration, "redstone_torch_on"));
        defaultMaterials.setRedstoneTorchOff(getVersionedMaterialAndData(configuration, "redstone_torch_off"));
        defaultMaterials.setRedstoneWallTorchOn(getVersionedMaterialAndData(configuration, "redstone_wall_torch_on"));
        defaultMaterials.setRedstoneWallTorchOff(getVersionedMaterialAndData(configuration, "redstone_wall_torch_off"));
        defaultMaterials.setMobSpawner(getVersionedMaterial(configuration, "mob_spawner"));
        defaultMaterials.setNetherPortal(getVersionedMaterial(configuration, "nether_portal"));
        defaultMaterials.setWriteableBook(getVersionedMaterial(configuration, "writable_book"));
        defaultMaterials.setFilledMap(getVersionedMaterial(configuration, "filled_map"));
    }

    protected void loadSkulls(ConfigurationSection skulls) {
        skullItems.clear();
        skullGroundBlocks.clear();
        skullWallBlocks.clear();
        Set<String> keys = skulls.getKeys(false);
        for (String key : keys) {
            try {
                ConfigurationSection types = skulls.getConfigurationSection(key);
                EntityType entityType = EntityType.valueOf(key.toUpperCase());
                MaterialAndData item = parseSkullCandidate(types, "item");
                if (item != null) {
                    skullItems.put(entityType, item);
                }
                MaterialAndData floor = parseSkullCandidate(types, "ground");
                if (item != null) {
                    skullGroundBlocks.put(entityType, floor);
                }
                MaterialAndData wall = parseSkullCandidate(types, "wall");
                if (item != null) {
                    skullWallBlocks.put(entityType, wall);
                }
            } catch (Exception ignore) {
            }
        }
    }

    @Nullable
    protected MaterialAndData parseSkullCandidate(ConfigurationSection section, String key) {
        Collection<String> candidates = ConfigurationUtils.getStringList(section, key);
        for (String candidate : candidates) {
            MaterialAndData test = new MaterialAndData(candidate.trim());
            if (test.isValid()) {
                return test;
            }
        }
        return null;
    }

    protected void populateEntityTypes(Set<EntityType> entityTypes, ConfigurationSection configuration, String key) {

        entityTypes.clear();
        if (configuration.contains(key))
        {
            Collection<String> typeStrings = ConfigurationUtils.getStringList(configuration, key);
            for (String typeString : typeStrings)
            {
                try {
                    entityTypes.add(EntityType.valueOf(typeString.toUpperCase()));
                } catch (Exception ex) {
                    getLogger().warning("Unknown entity type: " + typeString + " in " + key);
                }
            }
        }
    }

    protected void addCurrency(Currency currency) {
        currencies.put(currency.getKey(), currency);
    }

    protected void registerPreLoad() {
        // Setup custom providers
        currencies.clear();
        attributeProviders.clear();
        teamProviders.clear();
        requirementProcessors.clear();

        // Set up Break/Build/PVP Managers
        blockBreakManagers.clear();
        blockBuildManagers.clear();
        pvpManagers.clear();
        castManagers.clear();
        playerWarpManagers.clear();
        targetingProviders.clear();
        registeredAttributes.clear();

        PreLoadEvent loadEvent = new PreLoadEvent(this);
        Bukkit.getPluginManager().callEvent(loadEvent);

        blockBreakManagers.addAll(loadEvent.getBlockBreakManagers());
        blockBuildManagers.addAll(loadEvent.getBlockBuildManagers());
        pvpManagers.addAll(loadEvent.getPVPManagers());
        attributeProviders.addAll(loadEvent.getAttributeProviders());
        teamProviders.addAll(loadEvent.getTeamProviders());
        castManagers.addAll(loadEvent.getCastManagers());
        targetingProviders.addAll(loadEvent.getTargetingManagers());

        // Load builtin default currencies
        addCurrency(new ItemCurrency(this, getWorthItem(), getWorthItemAmount(), currencyItem.getName(), currencyItem.getPluralName()));
        addCurrency(new ManaCurrency(this));
        addCurrency(new ExperienceCurrency(this, getWorthXP()));
        addCurrency(new HealthCurrency(this));
        addCurrency(new HungerCurrency(this));
        addCurrency(new LevelCurrency(this));
        addCurrency(new ManaCurrency(this));
        addCurrency(new SpellPointCurrency(this, getWorthSkillPoints()));
        addCurrency(new VaultCurrency(this));

        // Custom currencies can override the defaults
        for (Currency currency : loadEvent.getCurrencies()) {
            addCurrency(currency);
        }

        // Configured currencies override everything else
        Set<String> keys = currencyConfiguration.getKeys(false);
        for (String key : keys) {
            addCurrency(new CustomCurrency(this, key, currencyConfiguration.getConfigurationSection(key)));
        }

        // Register attribute providers
        attributeProviders.addAll(loadEvent.getAttributeProviders());

        // Register team providers
        teamProviders.addAll(loadEvent.getTeamProviders());

        // Register player warp managers=
        playerWarpManagers.putAll(loadEvent.getWarpManagers());

        // Register requirement processors
        requirementProcessors.putAll(loadEvent.getRequirementProcessors());
        if (skillAPIManager != null) {
            requirementProcessors.put("skillapi", skillAPIManager);
        }

        // Re-register any providers previously registered by external plugins via register()
        for (MagicProvider provider : externalProviders) {
            register(provider);
        }

        // Don't allow overriding Magic requirements
        checkMagicRequirements();
    }

    private void finalizeAttributes() {
        registeredAttributes.addAll(builtinAttributes);
        registeredAttributes.addAll(this.attributes.keySet());
        for (AttributeProvider provider : attributeProviders) {
            Set<String> providerAttributes = provider.getAllAttributes();
            if (providerAttributes != null) {
                registeredAttributes.addAll(providerAttributes);
            }
        }

        MageParameters.initializeAttributes(registeredAttributes);
        MageParameters.setLogger(getLogger());
        log("Registered attributes: " + registeredAttributes);
    }

    private void checkMagicRequirements() {
        if (requirementProcessors.containsKey(Requirement.DEFAULT_TYPE)) {
            getLogger().warning("Something tried to register requirements for the " + Requirement.DEFAULT_TYPE + " type, but that is Magic's job.");
        }
        requirementProcessors.put(Requirement.DEFAULT_TYPE, requirementsController);
    }

    @Override
    public boolean register(MagicProvider provider) {
        boolean added = false;
        if (provider instanceof EntityTargetingManager) {
            added = true;
            targetingProviders.add((EntityTargetingManager)provider);
        }
        if (provider instanceof AttributeProvider) {
            added = true;
            AttributeProvider attributes = (AttributeProvider)provider;
            attributeProviders.add(attributes);
            if (!loading) {
                Set<String> providerAttributes = attributes.getAllAttributes();
                if (providerAttributes != null) {
                    registeredAttributes.addAll(providerAttributes);
                    MageParameters.initializeAttributes(registeredAttributes);
                    log("Registered additional attributes: " + providerAttributes);
                }
            }
        }
        if (provider instanceof TeamProvider) {
            added = true;
            teamProviders.add((TeamProvider)provider);
        }
        if (provider instanceof Currency) {
            added = true;
            addCurrency((Currency)provider);
        }
        if (provider instanceof RequirementsProvider) {
            added = true;
            RequirementsProvider requirements = (RequirementsProvider)provider;
            requirementProcessors.put(requirements.getKey(), requirements);
            if (!loading) {
                checkMagicRequirements();
            }
        }
        if (provider instanceof PlayerWarpProvider) {
            added = true;
            PlayerWarpProvider warp = (PlayerWarpProvider)provider;
            playerWarpManagers.put(warp.getKey(), warp);
        }
        if (provider instanceof BlockBreakManager) {
            added = true;
            blockBreakManagers.add((BlockBreakManager)provider);
        }
        if (provider instanceof PVPManager) {
            added = true;
            pvpManagers.add((PVPManager)provider);
        }
        if (provider instanceof BlockBuildManager) {
            added = true;
            blockBuildManagers.add((BlockBuildManager)provider);
        }
        if (provider instanceof CastPermissionManager) {
            added = true;
            castManagers.add((CastPermissionManager)provider);
        }
        if (added && !loading) {
            externalProviders.add(provider);
        }
        return added;
    }

    protected void clear()
    {
        if (!loaded) {
            return;
        }
        Collection<Mage> saveMages = new ArrayList<>(mages.values());
        for (Mage mage : saveMages)
        {
            playerQuit(mage);
        }

        mages.clear();
        pendingConstruction.clear();
        spells.clear();
        loaded = false;
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
        if (hasBypassPermission(player)) return true;
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
        if (hasBypassPermission(sender)) {
            return true;
        }
        String categoryPermission = spell.getCategoryPermissionNode();
        if (categoryPermission != null && !hasPermission(sender, categoryPermission)) {
            return false;
        }
        return hasPermission(sender, spell.getPermissionNode());
    }

    @Nullable
    @Override
    public Boolean getRegionCastPermission(Player player, SpellTemplate spell, Location location) {
        if (hasBypassPermission(player)) return true;
        Boolean result = null;
        for (CastPermissionManager manager : castManagers) {
            Boolean managerResult = manager.getRegionCastPermission(player, spell, location);
            if (managerResult != null) {
                if (!managerResult) {
                    return false;
                }
                if (result == null) {
                    result = managerResult;
                }
            }
        }
        return result;
    }

    @Nullable
    @Override
    public Boolean getPersonalCastPermission(Player player, SpellTemplate spell, Location location) {
        if (hasBypassPermission(player)) return true;
        Boolean result = null;
        for (CastPermissionManager manager : castManagers) {
            Boolean managerResult = manager.getPersonalCastPermission(player, spell, location);
            if (managerResult != null) {
                if (!managerResult) {
                    return false;
                }
                if (result == null) {
                    result = managerResult;
                }
            }
        }
        return result;
    }

    @Override
    public boolean hasBypassPermission(CommandSender sender) {
        if (sender == null) return false;
        if (sender instanceof Player && sender.hasPermission("Magic.bypass")) return true;
        Mage mage = getRegisteredMage(sender);
        if (mage == null) return false;
        return mage.isBypassEnabled();
    }

    @Override
    public boolean inTaggedRegion(Location location, Set<String> tags) {
        Boolean inRegion = worldGuardManager.inTaggedRegion(location, tags);
        return inRegion == null ? false : inRegion;
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

    public void registerFallingBlock(Entity fallingBlock, Block block) {
        UndoList undoList = getPendingUndo(fallingBlock.getLocation());
        if (undoList != null) {
            undoList.fall(fallingBlock, block);
        }
    }

    @Nullable
    public UndoList getEntityUndo(Entity entity) {
        UndoList blockList = null;
        if (entity == null) return null;
        blockList = com.elmakers.mine.bukkit.block.UndoList.getUndoList(entity);
        if (blockList != null) return blockList;

        if (entity instanceof Projectile) {
            Projectile projectile = (Projectile)entity;
            ProjectileSource source = projectile.getShooter();
            if (source instanceof Entity) {
                entity = (Entity)source;

                blockList = com.elmakers.mine.bukkit.block.UndoList.getUndoList(entity);
                if (blockList != null) return blockList;
            }
        }

        Mage mage = getRegisteredMage(entity);
        if (mage != null) {
            UndoList undoList = mage.getLastUndoList();
            if (undoList != null) {
                long now = System.currentTimeMillis();
                if (undoList.getModifiedTime() > now - undoTimeWindow) {
                    blockList = undoList;
                }
            }
        }

        return blockList;
    }

    public boolean isBindOnGive() {
        return bindOnGive;
    }

    @Override
    public void giveItemToPlayer(Player player, ItemStack itemStack) {
        Mage mage = getMage(player);
        mage.giveItem(itemStack);
    }

    @Override
    public boolean commitOnQuit() {
        return commitOnQuit;
    }

    public void onShutdown() {
        shuttingDown = true;
        if (despawnMagicMobs) {
            for (Mage mobMage : getMobMages()) {
                Entity entity = mobMage.getEntity();
                if (entity != null) {
                    entity.remove();
                }
            }
        }
        if (mageDataStore != null) {
            mageDataStore.close();
        }
        if (migrateDataStore != null) {
            migrateDataStore.close();
        }
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
        com.elmakers.mine.bukkit.magic.Mage implementation = null;
        if (mage instanceof com.elmakers.mine.bukkit.magic.Mage) {
            implementation = (com.elmakers.mine.bukkit.magic.Mage)mage;
            implementation.flagForReactivation();
        }
        mage.deactivate();
        mage.undoScheduled();
        mage.deactivateClasses();
        mage.deactivateModifiers();

        // Delay removal one tick to avoid issues with plugins that kill
        // players on logout (CombatTagPlus, etc)
        // Don't delay on shutdown, though.
        if (loaded && implementation != null && !shuttingDown) {
            final com.elmakers.mine.bukkit.magic.Mage quitMage = implementation;
            quitMage.setUnloading(true);
            plugin.getServer().getScheduler().runTaskLater(plugin, new MageQuitTask(this, quitMage, callback, isOpen), 1);
        } else {
            finalizeMageQuit(mage, callback, isOpen);
        }
    }

    public void finalizeMageQuit(final Mage mage, final MageDataCallback callback, final boolean isOpen) {
        // Unregister
        if (!externalPlayerData || !mage.isPlayer()) {
            removeMage(mage);
        }
        if (!mage.isLoading() && (mage.isPlayer() || saveNonPlayerMages) && loaded)
        {
            // Save synchronously on shutdown
            saveMage(mage, !shuttingDown, callback, isOpen, true);
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

    public void playerQuit(Mage mage) {
        playerQuit(mage, null);
    }

    @Override
    public void forgetMage(Mage mage) {
        if (mage instanceof com.elmakers.mine.bukkit.magic.Mage) {
            ((com.elmakers.mine.bukkit.magic.Mage)mage).setForget(true);
        }
    }

    @Override
    public void removeMage(Mage mage) {
        removeMage(mage.getId());
    }

    @Override
    public void removeMage(String id) {
        Mage mage = mages.remove(id);
        if (mage != null) {
            mage.removed();
        }
    }

    public void saveMage(Mage mage, boolean asynchronous)
    {
        saveMage(mage, asynchronous, null);
    }

    public void saveMage(Mage mage, boolean asynchronous, final MageDataCallback callback) {
        saveMage(mage, asynchronous, callback, false, false);
    }

    public void saveMage(Mage mage, boolean asynchronous, final MageDataCallback callback, boolean wandInventoryOpen, boolean releaseLock)
    {
        if (!savePlayerData) {
            if (callback != null) {
                callback.run(null);
            }
            return;
        }
        asynchronous = asynchronous && asynchronousSaving;
        info("Saving player data for " + mage.getName() + " (" + mage.getId() + ") " + ((asynchronous ? "" : " synchronously ") + "at " + System.currentTimeMillis()));
        final MageData mageData = new MageData(mage.getId());
        if (mageDataStore != null && mage.save(mageData)) {
            if (wandInventoryOpen) {
                mageData.setOpenWand(true);
            }
            if (asynchronous) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, new SaveMageTask(this, mageData, callback, releaseLock));
            } else {
                doSaveMage(mageData, callback, releaseLock);
            }
        } else if (releaseLock && mageDataStore != null) {
            getLogger().warning("Player logging out, but data never loaded. Force-releasing lock");
            mageDataStore.releaseLock(mageData);
        }
    }

    public void doSaveMage(MageData mageData, MageDataCallback callback, boolean releaseLock) {
        synchronized (saveLock) {
            try {
                mageDataStore.save(mageData, callback, releaseLock);
            } catch (Exception ex) {
                getLogger().log(Level.SEVERE, "Error saving mage data for mage " + mageData.getId(), ex);
            }
        }
    }

    @Nullable
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
        plugin.getServer().getScheduler().runTaskLater(plugin, new ArmorUpdatedTask(mage), 1);
    }

    @Override
    public boolean isLocked(Block block) {
        return protectLocked && containerMaterials.testBlock(block) && CompatibilityUtils.isLocked(block);
    }

    protected boolean addLostWandMarker(LostWand lostWand) {
        if (!dynmapShowWands) {
            return false;
        }
        Location location = lostWand.getLocation();
        return addMarker("wand-" + lostWand.getId(), "wand",  "wands", lostWand.getName(), location.getWorld().getName(),
            location.getBlockX(), location.getBlockY(), location.getBlockZ(), lostWand.getDescription()
        );
    }

    public void toggleCastCommandOverrides(Mage apiMage, CommandSender sender, boolean override) {
        // Don't track command-line casts
        // Reach into internals a bit here.
        if (apiMage instanceof com.elmakers.mine.bukkit.magic.Mage) {
            com.elmakers.mine.bukkit.magic.Mage mage = (com.elmakers.mine.bukkit.magic.Mage)apiMage;
            if (sender != null && sender instanceof BlockCommandSender)
            {
                mage.setCostFree(override ? castCommandCostFree : false);
                mage.setCooldownFree(override ? castCommandCooldownFree : false);
                mage.setPowerMultiplier(override ? castCommandPowerMultiplier : 1);
            }
            else
            {
                mage.setCostFree(override ? castConsoleCostFree : false);
                mage.setCooldownFree(override ? castConsoleCooldownFree : false);
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
        return new ArrayList<>(lostWands.values());
    }

    @Override
    public boolean cast(String spellName, String[] parameters) {
        return cast(spellName, parameters, Bukkit.getConsoleSender(), null);
    }

    public boolean cast(String spellName, String[] parameters, CommandSender sender, Entity entity) {
        ConfigurationSection config = null;
        if (parameters != null && parameters.length > 0) {
            config = ConfigurationUtils.newConfigurationSection();
            ConfigurationUtils.addParameters(parameters, config);
        }
        return cast(null, spellName, config, sender, entity);
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
            if (entity == null) {
                mage = getMage(mageController);
            } else {
                mage = getMageFromEntity(entity, mageController);
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
    public Messages getMessages() {
        return messages;
    }

    @Override
    public MapController getMaps() {
        return maps;
    }

    public String getWelcomeWand() {
        return welcomeWand;
    }

    @Override
    public void sendToMages(String message, Location location) {
        sendToMages(message, location, toggleMessageRange);
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
        if (isMagicNPC(entity)) {
            return true;
        }
        return npcSuppliers.isNPC(entity);
    }

    @Override
    public boolean isStaticNPC(Entity entity) {
        if (isMagicNPC(entity)) {
            return true;
        }
        return npcSuppliers.isStaticNPC(entity);
    }

    @Override
    public boolean isPet(Entity entity) {
        // This currently only looks for pets from SimplePets
        return entity.hasMetadata("pet");
    }

    @Override
    public boolean isMagicNPC(Entity entity) {
        return npcsByEntity.containsKey(entity.getUniqueId());
    }

    @Override
    public boolean isVanished(Entity entity) {
        if (entity == null) return false;
        Mage mage = getRegisteredMage(entity);
        if (mage != null && mage.isVanished()) {
            return true;
        }
        if (essentialsController != null && essentialsController.isVanished(entity)) {
            return true;
        }
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
        InventoryUtils.removeMeta(item, "keep");
        InventoryUtils.removeMeta(item, "temporary");
        InventoryUtils.removeMeta(item, "undroppable");
        InventoryUtils.removeMeta(item, "unplaceable");
        InventoryUtils.removeMeta(item, "unstashable");
        InventoryUtils.removeMeta(item, "unmoveable");
    }

    @Override
    public boolean canCreateWorlds()
    {
        return createWorldsEnabled;
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
    @Deprecated
    public Set<Material> getBuildingMaterials() {
        return MaterialSets.toLegacyNN(buildingMaterials);
    }

    @Override
    public Collection<Mage> getMobMages() {
        Collection<Mage> mobMages = new ArrayList<>();
        for (Mage mage : mages.values()) {
            if (mage.getEntityData() != null) {
                mobMages.add(mage);
            }
        }
        return Collections.unmodifiableCollection(mobMages);
    }

    @Override
    public Collection<Entity> getActiveMobs() {
        return mobs.getActiveMobs();
    }

    @Override
    @Deprecated
    public Set<Material> getRestrictedMaterials() {
        return MaterialSets.toLegacyNN(restrictedMaterials);
    }

    @Override
    public MaterialSet getBuildingMaterialSet() {
        return buildingMaterials;
    }

    @Override
    public MaterialSet getDestructibleMaterialSet() {
        return destructibleMaterials;
    }

    @Override
    public MaterialSet getRestrictedMaterialSet() {
        return restrictedMaterials;
    }

    @Override
    public int getMessageThrottle()
    {
        return messageThrottle;
    }

    // TODO: Remove the if and replace it with a precondition
    // once we're sure nothing is calling this with a null value.
    @SuppressWarnings({ "null", "unused" })
    @Override
    public boolean isMage(Entity entity) {
        if (entity == null) return false;
        String id = mageIdentifier.fromEntity(entity);
        return mages.containsKey(id);
    }

    @Override
    public MaterialSetManager getMaterialSetManager() {
        return materialSetManager;
    }

    @Override
    @Deprecated
    public Collection<String> getMaterialSets() {
        return getMaterialSetManager().getMaterialSets();
    }

    @Nullable
    @Override
    @Deprecated
    public Set<Material> getMaterialSet(String string) {
        return MaterialSets.toLegacy(getMaterialSetManager().fromConfig(string));
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
        // We can always target ourselves at this level
        if (attacker == entity) return true;

        // We don't handle non-entities here
        if (attacker == null || entity == null) return true;

        // We can't target our friends (bypassing happens at a higher level)
        if (isFriendly(attacker, entity, false))
        {
            return false;
        }
        for (EntityTargetingManager manager : targetingProviders) {
            if (!manager.canTarget(attacker, entity)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isFriendly(Entity attacker, Entity entity) {
        return isFriendly(attacker, entity, true);
    }

    public boolean isFriendly(Entity attacker, Entity entity, boolean friendlyByDefault)
    {
        // We are always friends with ourselves
        if (attacker == entity) return true;

        for (TeamProvider provider : teamProviders) {
            if (provider.isFriendly(attacker, entity)) {
                return true;
            }
        }

        if (friendlyByDefault) {
            // Mobs can always target players, just to avoid any confusion there.
            if (!(attacker instanceof Player)) return true;

            // Player vs Player is controlled by a special config flag
            if (entity instanceof Player) return defaultFriendly;

            // Otherwise we look at the friendly entity types
            if (friendlyEntityTypes.contains(entity.getType())) return true;
        }
        return false;
    }

    @Nullable
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

    public WarpController getWarps() {
        return warpController;
    }

    @Nullable
    @Override
    public Location getTownLocation(Player player) {
        return townyManager.getTownLocation(player);
    }

    @Nullable
    @Override
    public Map<String, Location> getHomeLocations(Player player) {
        return preciousStonesManager.getFieldLocations(player);
    }

    @Nonnull
    @Override
    public Set<String> getPlayerWarpProviderKeys() {
        return playerWarpManagers.keySet();
    }

    @Nullable
    @Override
    public Collection<PlayerWarp> getPlayerWarps(Player player, String key) {
        PlayerWarpManager manager = playerWarpManagers.get(key);
        if (manager == null) {
            return null;
        }
        return manager.getWarps(player);
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

    @Nullable
    @Override
    public UndoList undoAny(Block target) {
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

    @Nullable
    @Override
    public UndoList undoRecent(Block target, int timeout) {
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

    @Nullable
    @Override
    public Wand getIfWand(ItemStack itemStack) {
        if (Wand.isWand(itemStack)) {
            return getWand(itemStack);
        }
        return null;
    }

    @Override
    public Wand getWand(ItemStack itemStack) {
        @SuppressWarnings("deprecation")
        Wand wand = new Wand(this, itemStack);
        return wand;
    }

    @Override
    public Wand getWand(ConfigurationSection config) {
        return new Wand(this, config);
    }

    @Nullable
    @Override
    public Wand createWand(String wandKey) {
        return Wand.createWand(this, wandKey);
    }

    @Override
    @Nonnull
    public Wand createWand(@Nonnull ItemStack itemStack) {
        return Wand.createWand(this, itemStack);
    }

    @Nullable
    @Override
    public WandTemplate getWandTemplate(String key) {
        if (key == null || key.isEmpty()) return null;
        return wandTemplates.get(key);
    }

    @Override
    public Collection<com.elmakers.mine.bukkit.api.wand.WandTemplate> getWandTemplates() {
        return new ArrayList<>(wandTemplates.values());
    }

    @Override
    @Nullable
    public String getAutoWandKey(@Nonnull Material material) {
        return autoWands.get(material);
    }

    @Nullable
    public ItemStack getAutoWand(ItemStack itemStack) {
        if (itemStack == null) return null;
        String templateKey = getAutoWandKey(itemStack.getType());
        if (templateKey != null && !templateKey.isEmpty()) {
            Wand wand = createWand(templateKey);
            if (wand == null) {
                getLogger().warning("Invalid wand template in auto_wands config: " + templateKey);
            } else {
                return wand.getItem();
            }
        }
        return null;
    }

    @Nullable
    protected ConfigurationSection resolveConfiguration(String key, ConfigurationSection properties, Map<String, ConfigurationSection> configurations) {
        resolvingKeys.clear();
        return resolveConfiguration(key, properties, configurations, resolvingKeys);
    }

    @Nullable
    protected ConfigurationSection resolveConfiguration(String key, ConfigurationSection properties, Map<String, ConfigurationSection> configurations, Set<String> resolving) {
        // Catch circular dependencies
        if (resolving.contains(key)) {
            getLogger().log(Level.WARNING, "Circular dependency detected: " + StringUtils.join(resolving, " -> ") + " -> " + key);
            return properties;
        }
        resolving.add(key);

        ConfigurationSection configuration = configurations.get(key);
        if (configuration == null) {
            configuration = properties.getConfigurationSection(key);
            if (configuration == null) {
                return null;
            }
            String inherits = configuration.getString("inherit");
            if (inherits != null) {
                ConfigurationSection baseConfiguration = resolveConfiguration(inherits, properties, configurations, resolving);
                if (baseConfiguration != null) {
                    ConfigurationSection newConfiguration = ConfigurationUtils.cloneConfiguration(baseConfiguration);
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
            logger.setContext("classes." + key);
            loadMageClassTemplate(key, resolveConfiguration(key, properties, templateConfigurations));
        }

        // Resolve parents, we don't check for an inherited "parent" property, so it's important
        // to use the original un-inherited configs for parenting.
        for (String key : classKeys)
        {
            logger.setContext("classes." + key);
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

        // Update registered mages so their classes are current
        for (Mage mage : mages.values()) {
            if (mage instanceof com.elmakers.mine.bukkit.magic.Mage) {
                ((com.elmakers.mine.bukkit.magic.Mage)mage).reloadClasses();
            }
        }
    }

    public void loadModifiers(ConfigurationSection properties) {
        modifiers.clear();

        Set<String> modifierKeys = properties.getKeys(false);
        Map<String, ConfigurationSection> templateConfigurations = new HashMap<>();
        for (String key : modifierKeys) {
            logger.setContext("modifiers." + key);
            loadModifierTemplate(key, resolveConfiguration(key, properties, templateConfigurations));
        }

        // Resolve parents, we don't check for an inherited "parent" property, so it's important
        // to use the original un-inherited configs for parenting.
        for (String key : modifierKeys) {
            logger.setContext("modifiers." + key);
            ModifierTemplate template = modifiers.get(key);
            if (template != null) {
                String parentKey = properties.getConfigurationSection(key).getString("parent");
                if (parentKey != null) {
                    ModifierTemplate parent = modifiers.get(parentKey);
                    if (parent == null) {
                        getLogger().warning("Modifier '" + key + "' has unknown parent: " + parentKey);
                    } else {
                        template.setParent(parent);
                    }
                }
            }
        }

        // Update registered mages so their classes are current
        for (Mage mage : mages.values()) {
            if (mage instanceof com.elmakers.mine.bukkit.magic.Mage) {
                ((com.elmakers.mine.bukkit.magic.Mage)mage).reloadModifiers();
            }
        }
    }

    @Override
    public Set<String> getMageClassKeys() {
        return mageClasses.keySet();
    }

    @Nonnull
    public MageClassTemplate getMageClass(String key) {
        MageClassTemplate template = mageClasses.get(key);
        if (template == null) {
            ConfigurationSection configuration = ConfigurationUtils.newConfigurationSection();
            template = new MageClassTemplate(this, key, configuration);
            mageClasses.put(key, template);
        }
        return template;
    }

    public void loadMageClassTemplate(String key, ConfigurationSection classNode) {
        if (ConfigurationUtils.isEnabled(classNode)) {
            mageClasses.put(key, new MageClassTemplate(this, key, classNode));
        }
    }

    public void loadModifierTemplate(String key, ConfigurationSection modifierNode) {
        if (ConfigurationUtils.isEnabled(modifierNode)) {
            modifiers.put(key, new ModifierTemplate(this, key, modifierNode));
        }
    }

    public void loadWandTemplates(ConfigurationSection properties) {
        wandTemplates.clear();

        Set<String> wandKeys = properties.getKeys(false);
        Map<String, ConfigurationSection> templateConfigurations = new HashMap<>();
        for (String key : wandKeys)
        {
            logger.setContext("wands." + key);
            loadWandTemplate(key, resolveConfiguration(key, properties, templateConfigurations));
        }
    }

    public void loadMobs(ConfigurationSection properties) {
        mobs.clear();

        Set<String> mobKeys = properties.getKeys(false);
        Map<String, ConfigurationSection> templateConfigurations = new HashMap<>();
        for (String key : mobKeys) {
            logger.setContext("mobs." + key);
            mobs.load(key, resolveConfiguration(key, properties, templateConfigurations));
        }
    }

    public void loadWorlds(ConfigurationSection properties) {
        Set<String> worldKeys = properties.getKeys(false);
        Map<String, ConfigurationSection> templateConfigurations = new HashMap<>();
        for (String key : worldKeys) {
            if (key.equalsIgnoreCase("worlds")) continue;
            logger.setContext("worlds." + key);
            properties.set(key, resolveConfiguration(key, properties, templateConfigurations));
        }
        worldController.loadWorlds(properties);
    }

    @Override
    public MageClassTemplate getMageClassTemplate(String key) {
        return mageClasses.get(key);
    }

    @Override
    @Nullable
    public ModifierTemplate getModifierTemplate(String key) {
        return modifiers.get(key);
    }

    @Override
    @Nonnull
    public Collection<String> getModifierTemplateKeys() {
        return modifiers.keySet();
    }

    @Override
    public void loadWandTemplate(String key, ConfigurationSection wandNode) {
        if (ConfigurationUtils.isEnabled(wandNode)) {
            wandTemplates.put(key, new com.elmakers.mine.bukkit.wand.WandTemplate(this, key, wandNode));
        }
    }

    @Override
    public void unloadWandTemplate(String key) {
        wandTemplates.remove(key);
    }

    @Override
    public Collection<String> getWandTemplateKeys() {
        return wandTemplates.keySet();
    }

    @Nullable
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

    @Nullable
    @Override
    public com.elmakers.mine.bukkit.api.spell.SpellCategory getCategory(String key) {
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
    public Collection<String> getSpellTemplateKeys() {
        return spells.keySet();
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

    @Nullable
    @Override
    public SpellTemplate getSpellTemplate(String name) {
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
    public String getEntityDisplayName(Entity target) {
        return getEntityName(target, true);
    }

    @Override
    public String getEntityName(Entity target) {
        return getEntityName(target, false);
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
        for (String key : categoryKeys) {
            category = getCategory(key);
            title = messages.get("books.default.title").replace("$category", category.getName());
            String description = "" + ChatColor.BOLD + ChatColor.BLUE + title + "\n\n";
            description += "" + ChatColor.RESET + ChatColor.DARK_BLUE + category.getDescription();
            pages.add(description);

            List<SpellTemplate> categorySpells = categories.get(key);
            Collections.sort(categorySpells);

            for (SpellTemplate spell : categorySpells) {
                List<String> lines = getSpellBookDescription(spell);
                pages.add(StringUtils.join(lines, "\n"));
            }
        }

        book.setPages(pages);
        bookItem.setItemMeta(book);
        return bookItem;
    }

    public ItemStack getSpellBook(com.elmakers.mine.bukkit.api.spell.SpellTemplate spell, int count) {
        ItemStack bookItem = new ItemStack(Material.WRITTEN_BOOK, count);
        BookMeta book = (BookMeta)bookItem.getItemMeta();
        book.setAuthor(messages.get("books.default.author"));
        book.setTitle(messages.get("books.spell.title").replace("$spell", spell.getName()));
        List<String> pages = new ArrayList<>();
        List<String> lines = getSpellBookDescription(spell);
        pages.add(StringUtils.join(lines, "\n"));
        book.setPages(pages);
        bookItem.setItemMeta(book);
        return bookItem;
    }

    protected List<String> getSpellBookDescription(SpellTemplate spell) {
        Set<String> paths = WandUpgradePath.getPathKeys();
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
                if (!cost.isEmpty()) {
                    lines.add(ChatColor.DARK_PURPLE + messages.get("wand.costs_description").replace("$description", cost.getFullDescription(messages)));
                }
            }
        }
        Collection<CastingCost> activeCosts = spell.getActiveCosts();
        if (activeCosts != null) {
            for (CastingCost cost : activeCosts) {
                if (!cost.isEmpty()) {
                    lines.add(ChatColor.DARK_PURPLE + messages.get("wand.active_costs_description").replace("$description", cost.getFullDescription(messages)));
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

        String duration = spell.getDurationDescription(messages);
        if (duration != null) {
            lines.add(ChatColor.DARK_GREEN + duration);
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

        return lines;
    }

    public ItemStack getLearnSpellBook(SpellTemplate spell, int amount) {
        ConfigurationSection wandConfiguration = ConfigurationUtils.newConfigurationSection();
        wandConfiguration.set("template", "learnspell");
        wandConfiguration.set("icon", "book:" + spell.getKey());
        wandConfiguration.set("name", messages.get("books.learnspell.name").replace("$spell", spell.getName()));
        wandConfiguration.set("description", messages.get("books.learnspell.description").replace("$spell", spell.getName()));
        wandConfiguration.set("overrides", "spell " + spell.getKey());
        Wand wand = new Wand(this, wandConfiguration);
        ItemStack item = wand.getItem();
        item.setAmount(amount);
        return item;
    }

    @Override
    public MaterialAndData getRedstoneReplacement() {
        return redstoneReplacement;
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
    public boolean isWand(ItemStack item) {
        return Wand.isWand(item);
    }

    @Override
    public boolean isWandUpgrade(ItemStack item) {
        return Wand.isUpgrade(item);
    }

    @Override
    public boolean isSkill(ItemStack item) {
        return Wand.isSkill(item);
    }

    @Override
    public boolean isMagic(ItemStack item) {
        return Wand.isSpecial(item);
    }

    @Nullable
    @Override
    public String getWandKey(ItemStack item) {
        if (Wand.isWand(item)) {
            return Wand.getWandTemplate(item);
        }
        return null;
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

        MaterialAndData material = new MaterialAndData(item);
        return material.getKey();
    }

    @Nullable
    @Override
    public ItemStack createItem(String magicItemKey) {
        return createItem(magicItemKey, false);
    }

    @Nullable
    @Override
    public ItemStack createItem(String magicItemKey, boolean brief) {
        return createItem(magicItemKey, null, brief, null);
    }

    @Nullable
    @Override
    public ItemStack createItem(String magicItemKey, Mage mage, boolean brief, ItemUpdatedCallback callback) {
        ItemStack itemStack = null;
        if (magicItemKey == null || magicItemKey.isEmpty()) {
            if (callback != null) {
                callback.updated(null);
            }
            return null;
        }

        if (magicItemKey.contains("skill:")) {
            String spellKey = magicItemKey.substring(6);
            itemStack = Wand.createSpellItem(spellKey, this, mage, null, false);
            InventoryUtils.setMeta(itemStack, "skill", "true");
            if (callback != null) {
                callback.updated(itemStack);
            }
            return itemStack;
        }

        // Check for amounts
        int amount = 1;
        if (magicItemKey.contains("@")) {
            String[] pieces = StringUtils.split(magicItemKey, '@');
            magicItemKey = pieces[0];
            try {
                amount = Integer.parseInt(pieces[1]);
            } catch (Exception ignored) {

            }
        }

        // Handle : or | as delimiter
        magicItemKey = magicItemKey.replace("|", ":");
        try {
            if (magicItemKey.startsWith("book:")) {
                String bookCategory = magicItemKey.substring(5);
                com.elmakers.mine.bukkit.api.spell.SpellCategory category = null;

                if (!bookCategory.isEmpty() && !bookCategory.equalsIgnoreCase("all")) {
                    category = categories.get(bookCategory);
                    if (category == null) {
                        SpellTemplate spell = getSpellTemplate(bookCategory);
                        if (spell == null) {
                            if (callback != null) {
                                callback.updated(null);
                            }
                            return null;
                        } else {
                            itemStack = getSpellBook(spell, amount);
                        }
                    } else {
                        itemStack = getSpellBook(category, amount);
                    }
                }
            } else if (magicItemKey.startsWith("learnbook:")) {
                String bookSpell = magicItemKey.substring(10);
                SpellTemplate spell = getSpellTemplate(bookSpell);
                if (spell == null) {
                    if (callback != null) {
                        callback.updated(null);
                    }
                    return null;
                }
                itemStack = getLearnSpellBook(spell, amount);
            } else if (magicItemKey.startsWith("recipe:")) {
                String recipeKey = magicItemKey.substring(7);
                itemStack = CompatibilityUtils.getKnowledgeBook();
                if (itemStack != null) {
                    if (recipeKey.equals("*")) {
                        Collection<String> keys = crafting.getRecipeKeys();
                        for (String key : keys) {
                            CompatibilityUtils.addRecipeToBook(itemStack, plugin, key);
                        }
                    } else {
                        String[] recipeKeys = StringUtils.split(recipeKey, ",");
                        for (String recipe : recipeKeys) {
                            CompatibilityUtils.addRecipeToBook(itemStack, plugin, recipe);
                        }
                    }
                }
            } else if (magicItemKey.startsWith("recipes:")) {
                String recipeKey = magicItemKey.substring(8);
                itemStack = CompatibilityUtils.getKnowledgeBook();
                if (itemStack != null) {
                    if (recipeKey.equals("*")) {
                        Collection<String> keys = crafting.getRecipeKeys();
                        for (String key : keys) {
                            CompatibilityUtils.addRecipeToBook(itemStack, plugin, key);
                        }
                    } else {
                        String[] recipeKeys = StringUtils.split(recipeKey, ",");
                        for (String recipe : recipeKeys) {
                            MageClassTemplate mageClass = getMageClassTemplate(recipe);
                            if (mageClass != null) {
                                for (String key : mageClass.getRecipies()) {
                                    CompatibilityUtils.addRecipeToBook(itemStack, plugin, key);
                                }
                            }
                        }
                    }
                }
            } else if (skillPointItemsEnabled && magicItemKey.startsWith("sp:")) {
                String spAmountString = magicItemKey.substring(3);
                int spAmount = 0;
                try {
                    spAmount = Integer.parseInt(spAmountString);
                } catch (Exception ex) {
                    if (mage != null) {
                        mage.sendMessage(ChatColor.RED + "SP amount should be a number");
                    }
                }
                itemStack = getURLSkull(skillPointIcon);
                ItemMeta meta = itemStack.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', messages.get("sp.name")).replace("$amount", Integer.toString(spAmount)));
                String spDescription = messages.get("sp.description");
                if (spDescription.length() > 0)
                {
                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.translateAlternateColorCodes('&', spDescription));
                    meta.setLore(lore);
                }
                itemStack.setItemMeta(meta);
                InventoryUtils.setMetaInt(itemStack, "sp", spAmount);
            } else if (magicItemKey.startsWith("spell:")) {
                // Fix delimiter replaced above, to handle spell levels
                magicItemKey = magicItemKey.replace(":", "|");
                String spellKey = magicItemKey.substring(6);
                itemStack = createSpellItem(spellKey, brief);
            } else if (magicItemKey.startsWith("wand:")) {
                String wandKey = magicItemKey.substring(5);
                com.elmakers.mine.bukkit.api.wand.Wand wand = createWand(wandKey);
                if (wand != null) {
                    itemStack = wand.getItem();
                }
            } else if (magicItemKey.startsWith("upgrade:")) {
                String wandKey = magicItemKey.substring(8);
                com.elmakers.mine.bukkit.api.wand.Wand wand = createWand(wandKey);
                if (wand != null) {
                    wand.makeUpgrade();
                    itemStack = wand.getItem();
                }
            } else if (magicItemKey.startsWith("brush:")) {
                String brushKey = magicItemKey.substring(6);
                itemStack = createBrushItem(brushKey);
            } else if (magicItemKey.startsWith("item:")) {
                String itemKey = magicItemKey.substring(5);
                itemStack = createGenericItem(itemKey);
            } else {
                String[] pieces = StringUtils.split(magicItemKey, ':');
                Currency currency = currencies.get(pieces[0]);
                if (pieces.length > 1 && currency != null) {
                    String costKey = pieces[0];
                    String costAmount = pieces[1];

                    com.elmakers.mine.bukkit.api.block.MaterialAndData itemType = currency.getIcon();
                    if (itemType == null) {
                        itemStack = getURLSkull(skillPointIcon);
                    } else {
                        itemStack = itemType.getItemStack(1);
                    }

                    ItemMeta meta = itemStack.getItemMeta();
                    String name = messages.get("currency." + costKey + ".name", costKey);
                    String itemName = messages.get("currency." + costKey + ".item_name", messages.get("currency.item_name"));
                    itemName = itemName.replace("$type", name);
                    itemName = itemName.replace("$amount", costAmount);
                    meta.setDisplayName(itemName);
                    int intAmount;
                    try {
                        intAmount = Integer.parseInt(costAmount);
                    } catch (Exception ex) {
                        getLogger().warning("Invalid amount in custom cost: " + magicItemKey);
                        if (callback != null) {
                            callback.updated(null);
                        }
                        return null;
                    }

                    String currencyDescription = messages.get("currency." + costKey + ".description", messages.get("currency.description"));
                    if (currencyDescription.length() > 0)
                    {
                        List<String> lore = new ArrayList<>();
                        InventoryUtils.wrapText(ChatColor.translateAlternateColorCodes('&', currencyDescription), lore);
                        meta.setLore(lore);
                    }
                    itemStack.setItemMeta(meta);
                    itemStack = CompatibilityUtils.makeReal(itemStack);
                    InventoryUtils.makeUnbreakable(itemStack);
                    InventoryUtils.hideFlags(itemStack, 63);
                    Object currencyNode = InventoryUtils.createNode(itemStack, "currency");
                    InventoryUtils.setMetaInt(currencyNode, "amount", intAmount);
                    InventoryUtils.setMeta(currencyNode, "type", costKey);
                }
                if (itemStack == null && items != null) {
                    ItemData itemData = items.get(magicItemKey);
                    if (itemData != null) {
                        itemStack = itemData.getItemStack(amount);
                        if (callback != null) {
                            callback.updated(itemStack);
                        }
                        return itemStack;
                    }
                    MaterialAndData item = new MaterialAndData(magicItemKey);
                    if (item.isValid() && CompatibilityUtils.isLegacy(item.getMaterial())) {
                        short convertData = (item.getData() == null ? 0 : item.getData());
                        item = new MaterialAndData(CompatibilityUtils.migrateMaterial(item.getMaterial(), (byte)convertData));
                    }
                    if (item.isValid()) {
                        return item.getItemStack(amount, callback);
                    }
                    com.elmakers.mine.bukkit.api.wand.Wand wand = createWand(magicItemKey);
                    if (wand != null) {
                        ItemStack wandItem = wand.getItem();
                        if (wandItem != null) {
                            wandItem.setAmount(amount);
                        }
                        if (callback != null) {
                            callback.updated(wandItem);
                        }
                        return wandItem;
                    }
                    // Spells may be using the | delimiter for levels
                    // I am regretting overloading this delimiter!
                    String spellKey = magicItemKey.replace(":", "|");
                    itemStack = createSpellItem(spellKey, brief);
                    if (itemStack != null) {
                        itemStack.setAmount(amount);
                        if (callback != null) {
                            callback.updated(itemStack);
                        }
                        return itemStack;
                    }
                    itemStack = createBrushItem(magicItemKey);
                    if (itemStack != null) {
                        itemStack.setAmount(amount);
                    }
                }
            }

        } catch (Exception ex) {
            getLogger().log(Level.WARNING, "Error creating item: " + magicItemKey, ex);
        }

        if (callback != null) {
            callback.updated(itemStack);
        }
        return itemStack;
    }

    @Nullable
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

    @Nullable
    @Override
    public ItemStack createSpellItem(String spellKey) {
        return Wand.createSpellItem(spellKey, this, null, true);
    }

    @Nullable
    @Override
    public ItemStack createSpellItem(String spellKey, boolean brief) {
        return Wand.createSpellItem(spellKey, this, null, !brief);
    }

    @Nullable
    @Override
    public ItemStack createBrushItem(String brushKey) {
        return Wand.createBrushItem(brushKey, this, null, true);
    }

    public boolean isSameItem(ItemStack first, ItemStack second) {
        if (first.getType() != second.getType()) return false;
        if (first.getDurability() != second.getDurability()) return false;
        if (first.hasItemMeta() != second.hasItemMeta()) return false;
        if (!first.hasItemMeta()) return true;
        return first.getItemMeta().equals(second.getItemMeta());
    }

    @Override
    public boolean itemsAreEqual(ItemStack first, ItemStack second) {
        return itemsAreEqual(first, second, false);
    }

    @Override
    public boolean itemsAreEqual(ItemStack first, ItemStack second, boolean ignoreDamage) {
        boolean firstIsEmpty = CompatibilityUtils.isEmpty(first);
        boolean secondIsEmpty = CompatibilityUtils.isEmpty(second);
        if (secondIsEmpty && firstIsEmpty) return true;
        if (secondIsEmpty || firstIsEmpty) return false;
        if (first.getType() != second.getType()) return false;
        if (!ignoreDamage && first.getDurability() != second.getDurability()) return false;

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
        if (!Objects.equals(firstName, secondName)) {
            return false;
        }

        MaterialAndData firstData = new MaterialAndData(first);
        MaterialAndData secondData = new MaterialAndData(second);
        return firstData.equals(secondData);
    }

    @Override
    public Set<String> getWandPathKeys() {
        return WandUpgradePath.getPathKeys();
    }

    @Override
    public com.elmakers.mine.bukkit.api.wand.WandUpgradePath getPath(String key) {
        return WandUpgradePath.getPath(key);
    }

    @Nullable
    @Override
    public ItemStack deserialize(ConfigurationSection root, String key) {
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
        } else if (Wand.isSpell(item)) {
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

    @Nullable
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
        com.elmakers.mine.bukkit.magic.Mage mage = getMage(player);
        mage.setDestinationWarp(warp);
        info("Cross-server warping " + player.getName() + " to warp " + warp, 1);
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
    public boolean isDisguised(Entity entity) {
        return !libsDisguiseEnabled || libsDisguiseManager == null || entity == null ? false : libsDisguiseManager.isDisguised(entity);
    }

    @Override
    public boolean hasDisguises() {
        return libsDisguiseEnabled && libsDisguiseManager != null;
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
        return loaded && !shuttingDown;
    }

    public boolean isDataLoaded() {
        return loaded && dataLoaded && !shuttingDown;
    }

    public boolean areLocksProtected() {
        return protectLocked;
    }

    public boolean isContainer(Block block) {
        return block != null && containerMaterials.testBlock(block);
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
        return item != null && meleeMaterials.testItem(item);
    }

    public boolean isWearable(ItemStack item) {
        return item != null && wearableMaterials.testItem(item);
    }

    public boolean isInteractible(Block block) {
        return block != null && interactibleMaterials.testBlock(block);
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
    public boolean isVaultCurrencyEnabled() {
        return VaultController.hasEconomy();
    }

    @Override
    public void depositVaultCurrency(OfflinePlayer player, double amount) {
        VaultController.getInstance().depositPlayer(player, amount);
    }

    @Override
    public void deleteMage(final String id) {
        final Mage mage = getRegisteredMage(id);
        if (mage != null) {
            playerQuit(mage, new MageDataCallback() {
                @Override
                public void run(MageData data) {
                    info("Deleted mage id " + id);
                    mageDataStore.delete(id);

                    // If this was a player and that player is online, reload them so they function normally.
                    Player player = mage.getPlayer();
                    if (player != null && player.isOnline()) {
                        getMage(player);
                    }
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

    @Nullable
    @Override
    public String getSpell(ItemStack item) {
        return Wand.getSpell(item);
    }

    @Nullable
    @Override
    public String getSpellArgs(ItemStack item) {
        return Wand.getSpellArgs(item);
    }

    @Override
    public Set<String> getNPCKeys() {
        Set<String> keys = new HashSet<>();
        for (EntityData mob : mobs.getMobs()) {
            if (mob.isNPC() && !mob.isHidden()) {
                keys.add(mob.getKey());
            }
        }
        return keys;
    }

    @Override
    public Set<String> getMobKeys() {
        return mobs.getKeys();
    }

    @Nullable
    @Override
    public Entity spawnMob(String key, Location location) {
        EntityData mobType = mobs.get(key);
        if (mobType != null) {
            return mobType.spawn(location);
        }
        EntityType entityType = com.elmakers.mine.bukkit.entity.EntityData.parseEntityType(key);
        if (entityType == null) {
            return null;
        }
        return location.getWorld().spawnEntity(location, entityType);
    }

    @Nullable
    @Override
    public EntityData getMob(Entity entity) {
        return mobs.getEntityData(entity);
    }

    @Override
    @Nullable
    public com.elmakers.mine.bukkit.entity.EntityData getMob(String key) {
        if (key == null) return null;

        // This null check is hopefully temporary, but deals with actions that look up a mob during interrogation.
        com.elmakers.mine.bukkit.entity.EntityData mob = mobs == null ? null : mobs.get(key);
        if (mob == null && mobs != null) {
            EntityType entityType = com.elmakers.mine.bukkit.entity.EntityData.parseEntityType(key);
            if (entityType != null) {
                mob = mobs.getDefaultMob(entityType);
            }
        }

        return mob;
    }

    @Override
    @Nullable
    public EntityData getMob(ConfigurationSection parameters) {
        String mobType = parameters.getString("type");
        com.elmakers.mine.bukkit.entity.EntityData mob = null;
        if (mobType != null && !mobType.isEmpty()) {
            mob = getMob(mobType);
        }
        if (mob != null && parameters != null && !parameters.getKeys(false).isEmpty()) {
            mob = mob.clone();
            ConfigurationSection effectiveParameters = ConfigurationUtils.cloneConfiguration(mob.getConfiguration());
            // Have to preserve the mob type config, it can't be overridden
            String originalType = effectiveParameters.getString("type", mobType);
            effectiveParameters = ConfigurationUtils.addConfigurations(effectiveParameters, parameters);
            effectiveParameters.set("type", originalType);
            mob.load(effectiveParameters);
        } else if (mob == null) {
            mob = new com.elmakers.mine.bukkit.entity.EntityData(this, parameters);
        }
        return mob;
    }

    @Override
    @Nullable
    public EntityData getMobByName(String name) {
        return mobs.getByName(name);
    }

    @Override
    public EntityData loadMob(ConfigurationSection configuration) {
        return new com.elmakers.mine.bukkit.entity.EntityData(this, configuration);
    }

    @Override
    @Nullable
    public Entity replaceMob(Entity targetEntity, EntityData replaceType, boolean force, CreatureSpawnEvent.SpawnReason reason) {
        EntityData targetData = getMob(targetEntity);
        EntityData newData = replaceType;
        if (targetData != null) {
            newData = targetData.clone();
            ConfigurationSection effectiveParameters = ConfigurationUtils.cloneConfiguration(newData.getConfiguration());
            ConfigurationSection newParameters = replaceType.getConfiguration();
            effectiveParameters = ConfigurationUtils.addConfigurations(effectiveParameters, newParameters);
            // Handle the replacement type being bare
            effectiveParameters.set("type", replaceType.getType().name());
            newData.load(effectiveParameters);
        }

        if (force) {
            setForceSpawn(true);
        }
        Entity spawnedEntity = null;
        try {
            spawnedEntity = newData.spawn(targetEntity.getLocation(), reason);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (force) {
            setForceSpawn(false);
        }
        if (spawnedEntity != null) {
            targetEntity.remove();
        }

        return spawnedEntity;
    }

    @Override
    public Set<String> getItemKeys() {
        return items.getKeys();
    }

    @Override
    @Nullable
    public ItemData getItem(String key) {
        return items.get(key);
    }

    @Override
    @Nullable
    public ItemData getItem(ItemStack match) {
        return items.get(match);
    }

    @Nullable
    @Override
    public ItemData getOrCreateItem(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }
        return items.getOrCreate(key);
    }

    @Nullable
    @Override
    public ItemData getOrCreateItemOrWand(String key) {
        return getOrCreateItem(key);
    }

    @Nullable
    @Override
    public ItemData getOrCreateMagicItem(String key) {
        return getOrCreateItem(key);
    }

    @Override
    public ItemData createItemData(ItemStack itemStack) {
        return new com.elmakers.mine.bukkit.item.ItemData(itemStack, this);
    }

    @Nullable
    public String getLockKey(ItemStack itemStack) {
        String metaKey = Wand.getLockKey(itemStack);
        if (metaKey == null && itemStack != null) {
            ItemData item = getItem(itemStack.getType().name().toLowerCase());
            if (item != null && item.isLocked()) {
                return item.getKey();
            }
        }
        return metaKey;
    }

    @Override
    public void unloadItemTemplate(String key) {
        items.remove(key);
    }

    @Override
    public void loadItemTemplate(String key, ConfigurationSection configuration) {
        items.loadItem(key, configuration);
    }

    @Nullable
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

    @Nullable
    @Override
    public Double getEarns(ItemStack item) {
        int amount = item.getAmount();
        item.setAmount(1);
        ItemData configuredItem = items.get(item);
        item.setAmount(amount);
        if (configuredItem == null) {
            return null;
        }

        return configuredItem.getEarns() * amount;
    }

    public boolean isInventoryBackupEnabled() {
        return backupInventories;
    }

    @Nullable
    @Override
    public String getBlockSkin(Material blockType) {
        return blockSkins.get(blockType);
    }

    @Override
    @Nonnull
    public Random getRandom() {
        return random;
    }

    @Override
    public boolean sendResourcePackToAllPlayers(CommandSender sender) {
        return resourcePacks.sendResourcePackToAllPlayers(sender);
    }

    @Override
    public boolean promptResourcePack(final Player player) {
        return resourcePacks.promptResourcePack(player);
    }

    @Override
    public boolean promptNoResourcePack(final Player player) {
        return resourcePacks.promptNoResourcePack(player);
    }

    @Override
    public boolean sendResourcePack(final Player player) {
        return resourcePacks.sendResourcePack(player);
    }

    @Override
    public void checkResourcePack(CommandSender sender) {
        resourcePacks.clearChecked();
        checkResourcePack(sender, false, true);
    }

    public boolean checkResourcePack(final CommandSender sender, final boolean quiet) {
        return checkResourcePack(sender, quiet, false);
    }

    public boolean checkResourcePack(final CommandSender sender, final boolean quiet, final boolean force) {
        return resourcePacks.checkResourcePack(sender, quiet, force, false);
    }

    @Override
    public boolean isResourcePackEnabled() {
        return resourcePacks.isResourcePackEnabled();
    }

    @Nullable
    @Override
    public Material getMobEgg(EntityType mobType) {
        return mobEggs.get(mobType);
    }

    @Nullable
    @Override
    public String getMobSkin(EntityType mobType) {
        return mobSkins.get(mobType);
    }

    @Nullable
    @Override
    public String getPlayerSkin(Player player) {
        return libsDisguiseManager == null ? null : libsDisguiseManager.getSkin(player);
    }

    @Override
    @Nonnull
    public ItemStack getURLSkull(String url) {
        try {
            ItemStack stack = getURLSkull(new URL(url), InventoryUtils.SKULL_UUID);
            return stack == null ? new ItemStack(Material.AIR) : stack;
        } catch (MalformedURLException e) {
            Bukkit.getLogger().log(Level.WARNING, "Malformed URL: " + url, e);
        }

        return new ItemStack(Material.AIR);
    }

    @Nullable
    private ItemStack getURLSkull(URL url, UUID id) {
        MaterialAndData skullType = skullItems.get(EntityType.PLAYER);
        if (skullType == null) {
            return new ItemStack(Material.AIR);
        }
        ItemStack skull = skullType.getItemStack(1);
        return InventoryUtils.setSkullURL(skull, url, id);
    }

    @Override
    public void setSkullOwner(Skull skull, String ownerName) {
        DeprecatedUtils.setOwner(skull, ownerName);
    }

    @Override
    public void setSkullOwner(Skull skull, UUID uuid) {
        DeprecatedUtils.setOwner(skull, uuid);
    }

    @Override
    @Nonnull
    @Deprecated
    public ItemStack getSkull(String ownerName, String itemName) {
        return getSkull(ownerName, itemName, null);
    }

    @Override
    @Nonnull
    public ItemStack getSkull(String ownerName, String itemName, final ItemUpdatedCallback callback) {
        MaterialAndData skullType = skullItems.get(EntityType.PLAYER);
        if (skullType == null) {
            ItemStack air = new ItemStack(Material.AIR);
            if (callback != null) {
                callback.updated(air);
            }
            return air;
        }
        ItemStack skull = skullType.getItemStack(1);
        ItemMeta meta = skull.getItemMeta();
        if (itemName != null) {
            meta.setDisplayName(itemName);
        }
        skull.setItemMeta(meta);
        SkullLoadedCallback skullCallback = null;
        if (callback != null) {
            skullCallback = new SkullLoadedCallback() {
                @Override
                public void updated(ItemStack itemStack) {
                    callback.updated(itemStack);
                }
            };
        }
        DeprecatedUtils.setSkullOwner(skull, ownerName, skullCallback);
        return skull;
    }

    @Override
    @Nonnull
    public ItemStack getSkull(UUID uuid, String itemName, ItemUpdatedCallback callback) {
        MaterialAndData skullType = skullItems.get(EntityType.PLAYER);
        if (skullType == null) {
            return new ItemStack(Material.AIR);
        }
        ItemStack skull = skullType.getItemStack(1);
        ItemMeta meta = skull.getItemMeta();
        if (itemName != null) {
            meta.setDisplayName(itemName);
        }
        skull.setItemMeta(meta);

        SkullLoadedCallback skullCallback = null;
        if (callback != null) {
            skullCallback = new SkullLoadedCallback() {
                @Override
                public void updated(ItemStack itemStack) {
                    callback.updated(itemStack);
                }
            };
        }
        DeprecatedUtils.setSkullOwner(skull, uuid, skullCallback);
        return skull;
    }


    @Override
    @Nonnull
    public ItemStack getSkull(Player player, String itemName) {
        MaterialAndData skullType = skullItems.get(EntityType.PLAYER);
        if (skullType == null) {
            return new ItemStack(Material.AIR);
        }
        ItemStack skull = skullType.getItemStack(1);
        ItemMeta meta = skull.getItemMeta();
        if (itemName != null) {
            meta.setDisplayName(itemName);
        }
        skull.setItemMeta(meta);
        DeprecatedUtils.setSkullOwner(skull, player.getName(), null);
        return skull;
    }

    @Override
    @Nonnull
    @Deprecated
    public ItemStack getSkull(Entity entity, String itemName) {
        if (entity instanceof Player) {
            return getSkull((Player)entity, itemName);
        }
        return getSkull(entity, itemName, null);
    }

    @Override
    @Nonnull
    public ItemStack getSkull(Entity entity, String itemName, ItemUpdatedCallback callback) {
        String ownerName = null;
        MaterialAndData skullType = skullItems.get(entity.getType());
        if (skullType == null) {
            ownerName = getMobSkin(entity.getType());
            skullType = skullItems.get(EntityType.PLAYER);
            if (skullType == null || ownerName == null) {
                ItemStack air = new ItemStack(Material.AIR);
                if (callback != null) {
                    callback.updated(air);
                }
                return air;
            }
        }
        if (entity instanceof Player) {
            ownerName = entity.getName();
        }

        ItemStack skull = skullType.getItemStack(1);
        ItemMeta meta = skull.getItemMeta();
        if (itemName != null) {
            meta.setDisplayName(itemName);
        }
        skull.setItemMeta(meta);
        if (ownerName != null) {
            SkullLoadedCallback skullCallback = null;
            if (callback != null) {
                skullCallback = new SkullLoadedCallback() {
                    @Override
                    public void updated(ItemStack itemStack) {
                        callback.updated(itemStack);
                    }
                };
            }
            if (ownerName.startsWith("http")) {
                skull = InventoryUtils.setSkullURL(skull, ownerName);
                if (callback != null) {
                    callback.updated(skull);
                }
            } else {
                DeprecatedUtils.setSkullOwner(skull, ownerName, skullCallback);
            }
        } else if (callback != null) {
            callback.updated(skull);
        }
        return skull;
    }

    @Nonnull
    @Override
    public ItemStack getMap(int mapId) {
        short durability = NMSUtils.isCurrentVersion() ? 0 : (short)mapId;
        ItemStack mapItem = new ItemStack(DefaultMaterials.getFilledMap(), 1, durability);
        if (NMSUtils.isCurrentVersion()) {
            mapItem = CompatibilityUtils.makeReal(mapItem);
            InventoryUtils.setMetaInt(mapItem, "map", mapId);
        }
        return mapItem;
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
    public String getDefaultWandTemplate() {
        return Wand.DEFAULT_WAND_TEMPLATE;
    }

    @Nullable
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

    public List<AttributeProvider> getAttributeProviders() {
        return attributeProviders;
    }

    @Override
    @Nullable
    public MagicAttribute getAttribute(String attributeKey) {
        return attributes.get(attributeKey);
    }

    @Override
    public boolean createLight(Location location, int lightLevel, boolean async) {
        if (lightAPIManager == null) return false;
        long blockId = BlockData.getBlockId(location);
        String chunkId = getChunkKey(location);
        Integer chunkRefs = lightChunks.get(chunkId);
        if (chunkRefs == null) {
            lightChunks.put(chunkId, 1);
        } else {
            lightChunks.put(chunkId, chunkRefs + 1);
        }
        Integer refCount = lightBlocks.get(blockId);
        if (refCount != null) {
            lightBlocks.put(blockId, refCount + 1);
            return false;
        }
        lightBlocks.put(blockId, 1);
        return lightAPIManager.createLight(location, lightLevel, async);
    }

    @Override
    public boolean deleteLight(Location location, boolean async) {
        if (lightAPIManager == null) return false;
        long blockId = BlockData.getBlockId(location);
        Integer refCount = lightBlocks.get(blockId);
        String chunkId = getChunkKey(location);
        Integer chunkRefs = lightChunks.get(chunkId);
        if (chunkRefs != null) {
            if (chunkRefs <= 1) {
                lightChunks.remove(chunkId);
            } else {
                lightChunks.put(chunkId, chunkRefs - 1);
            }
        }
        if (refCount != null) {
            if (refCount <= 1) {
                lightBlocks.remove(blockId);
            } else {
                lightBlocks.put(blockId, refCount - 1);
                return false;
            }
        }
        return lightAPIManager.deleteLight(location, async);
    }

    @Override
    public boolean updateLight(Location location) {
        return updateLight(location, true);
    }

    @Override
    public boolean updateLight(Location location, boolean force) {
        if (lightAPIManager == null) return false;
        if (!force) {
            String chunkId = getChunkKey(location);
            Integer chunkRefs = lightChunks.get(chunkId);
            if (chunkRefs != null) return false;
        }
        return lightAPIManager.updateChunks(location);
    }

    @Override
    public int getLightCount() {
        return lightBlocks.size();
    }

    @Override
    public boolean isLightingAvailable() {
        return lightAPIManager != null;
    }

    @Override
    public @Nullable String checkRequirements(@Nonnull CastContext context, @Nullable Collection<Requirement> requirements) {
        if (requirements == null) return null;

        for (Requirement requirement : requirements) {
            String type = requirement.getType();
            RequirementsProcessor processor = requirementProcessors.get(type);
            if (processor != null) {
                if (!processor.checkRequirement(context, requirement)) {
                    String message = processor.getRequirementDescription(context, requirement);
                    if (message == null || message.isEmpty()) {
                        message = messages.get("requirements.unknown");
                    }
                    return message;
                }
            }
        }
        return null;
    }

    @Override
    public @Nonnull Collection<String> getLoadedExamples() {
        List<String> examples = new ArrayList<>();
        if (exampleDefaults != null && !exampleDefaults.isEmpty()) examples.add(exampleDefaults);
        if (addExamples != null) examples.addAll(addExamples);
        return examples;
    }

    @Nullable
    @Override
    public String getExample() {
        return exampleDefaults != null && exampleDefaults.isEmpty() ? null : exampleDefaults;
    }

    @Nonnull
    @Override
    public Collection<String> getExamples() {
        List<String> examples = new ArrayList<>();
        try {
            CodeSource src = MagicController.class.getProtectionDomain().getCodeSource();
            if (src != null) {
                URL jar = src.getLocation();
                try (InputStream is = jar.openStream();
                    ZipInputStream zip = new ZipInputStream(is)) {
                    while (true) {
                        ZipEntry e = zip.getNextEntry();
                        if (e == null)
                            break;
                        String name = e.getName();
                        if (!name.equals("examples/")
                            && !name.equals("examples/localizations/")
                            && name.startsWith("examples/")
                            && name.endsWith("/") && !name.contains(".")) {
                            examples.add(name.replace("examples/", "").replace("/", ""));
                        }
                    }
                }
            }
        } catch (IOException ex) {
            plugin.getLogger().log(Level.WARNING, "Error scanning example files", ex);
        }
        File examplesFolder = new File(getPlugin().getDataFolder(), "examples");
        if (examplesFolder.exists()) {
            for (File file : examplesFolder.listFiles()) {
                if (!file.isDirectory() || file.getName().startsWith(".")) continue;
                examples.add(file.getName());
            }
        }

        return examples;
    }

    @Nonnull
    @Override
    public Collection<String> getLocalizations() {
        List<String> examples = new ArrayList<>();
        try {
            CodeSource src = MagicController.class.getProtectionDomain().getCodeSource();
            if (src != null) {
                URL jar = src.getLocation();
                try (InputStream is = jar.openStream();
                    ZipInputStream zip = new ZipInputStream(is)) {
                    while (true) {
                        ZipEntry e = zip.getNextEntry();
                        if (e == null)
                            break;
                        String name = e.getName();
                        if (!name.equals("examples/")
                            && !name.equals("examples/localizations/")
                            && name.startsWith("examples/localizations/messages.")
                            && name.endsWith(".yml")) {
                            examples.add(name.replace("examples/localizations/messages.", "").replace(".yml", ""));
                        }
                    }
                }
            }
        } catch (IOException ex) {
            plugin.getLogger().log(Level.WARNING, "Error scanning example files", ex);
        }

        return examples;
    }

    @Nonnull
    @Override
    public Collection<String> getExternalExamples() {
        List<String> examples = new ArrayList<>(builtinExternalExamples.keySet());
        File examplesFolder = new File(getPlugin().getDataFolder(), "examples");
        if (examplesFolder.exists()) {
            for (File file : examplesFolder.listFiles()) {
                 if (!file.isDirectory() || file.getName().startsWith(".") || file.getName().endsWith(".bak")) continue;
                examples.add(file.getName());
            }
        }
        return examples;
    }

    @Nullable
    @Override
    public String getExternalExampleURL(String exampleKey) {
        String url = null;
        File exampleFolder = new File(getPlugin().getDataFolder(), "examples");
        exampleFolder = new File(exampleFolder, exampleKey);
        File urlFile = new File(exampleFolder, "url.txt");
        if (urlFile.exists()) {
            try {
                url = new String(Files.readAllBytes(Paths.get(urlFile.getAbsolutePath())), StandardCharsets.UTF_8);
            } catch (Exception ex) {
                getLogger().log(Level.WARNING, "Error loading example url from file: " + urlFile.getAbsolutePath(), ex);
            }
        }
        if (url == null) {
            url = builtinExternalExamples.get(exampleKey);
        }
        return url;
    }

    @Override
    public double getBlockDurability(@Nonnull Block block) {
        double durability = CompatibilityUtils.getDurability(block.getType());
        if (citadelManager != null) {
            Integer reinforcement = citadelManager.getDurability(block.getLocation());
            if (reinforcement != null) {
                durability += reinforcement;
            }
        }
        return durability;
    }

    @Override
    @Nonnull
    public String getSkillsSpell() {
        return skillsSpell;
    }

    @Override
    @Nonnull
    public Collection<EffectPlayer> getEffects(@Nonnull String effectKey) {
        Collection<EffectPlayer> effectList = effects.get(effectKey);
        if (effectList == null) {
            effectList = new ArrayList<>();
        }
        return effectList;
    }

    @Override
    public void playEffects(@Nonnull String effectKey, @Nonnull Location sourceLocation, @Nonnull Location targetLocation) {
        Collection<EffectPlayer> effectPlayers = effects.get(effectKey);
        if (effectPlayers == null) return;

        for (EffectPlayer player : effectPlayers) {
            player.start(sourceLocation, targetLocation);
        }
    }

    @Override
    public void playEffects(@Nonnull String effectKey, @Nonnull EffectContext context) {
        Collection<EffectPlayer> effectPlayers = effects.get(effectKey);
        if (effectPlayers == null) return;

        for (EffectPlayer player : effectPlayers) {
            player.start(context);
        }
    }

    @Override
    @Nonnull
    public Collection<String> getEffectKeys() {
        return effects.keySet();
    }

    @Override
    public Collection<String> getRecipeKeys() {
        return crafting.getRecipeKeys();
    }

    @Override
    public Collection<String> getAutoDiscoverRecipeKeys() {
        return crafting.getAutoDiscoverRecipeKeys();
    }

    public void checkVanished(Player player) {
        for (Mage mage : mages.values()) {
            if (mage.isVanished()) {
                DeprecatedUtils.hidePlayer(plugin, player, mage.getPlayer());
            }
        }
    }

    @Override
    public void logBlockChange(@Nonnull Mage mage, @Nonnull BlockState priorState, @Nonnull BlockState newState) {
        if (logBlockManager != null) {
            Entity entity = mage.getEntity();
            if (entity != null) {
                logBlockManager.logBlockChange(entity, priorState, newState);
            }
        }
    }

    @Override
    public boolean isFileLockingEnabled() {
        return isFileLockingEnabled;
    }

    /**
     * @return The supplier set that is used.
     */
    public NPCSupplierSet getNPCSuppliers() {
        return npcSuppliers;
    }

    @Override
    public Collection<com.elmakers.mine.bukkit.api.npc.MagicNPC> getNPCs() {
        return new ArrayList<>(npcs.values());
    }

    @Override
    public void removeNPC(com.elmakers.mine.bukkit.api.npc.MagicNPC npc) {
        unregisterNPC(npc);
        npc.remove();
        npcs.remove(npc.getId());
        npcsByEntity.remove(npc.getEntityId());
    }

    public void unregisterNPC(com.elmakers.mine.bukkit.api.npc.MagicNPC npc) {
        String chunkId = getChunkKey(npc.getLocation());
        if (chunkId == null) return;
        List<MagicNPC> chunkNPCs = npcsByChunk.get(chunkId);
        if (chunkNPCs == null) {
            return;
        }
        Iterator<MagicNPC> it = chunkNPCs.iterator();
        while (it.hasNext()) {
            if (it.next().getId().equals(npc.getId())) {
                it.remove();
                break;
            }
        }
    }

    @Override
    @Nullable
    public MagicNPC addNPC(com.elmakers.mine.bukkit.api.magic.Mage creator, String name) {
        EntityData template = mobs.get(name);
        MagicNPC npc;
        if (template != null && template instanceof com.elmakers.mine.bukkit.entity.EntityData) {
            npc = new MagicNPC(this, creator, creator.getLocation(), (com.elmakers.mine.bukkit.entity.EntityData)template);
        } else {
            npc = new MagicNPC(this, creator, creator.getLocation(), name);
        }
        if (!registerNPC(npc)) {
            return null;
        }
        return npc;
    }

    public boolean registerNPC(MagicNPC npc) {
        Location location = npc.getLocation();
        String chunkId = getChunkKey(location);
        if (chunkId == null) {
            return false;
        }

        List<MagicNPC> chunkNPCs = npcsByChunk.get(chunkId);
        if (chunkNPCs == null) {
            chunkNPCs = new ArrayList<>();
            npcsByChunk.put(chunkId, chunkNPCs);
        }
        chunkNPCs.add(npc);
        npcs.put(npc.getId(), npc);
        activateNPC(npc);
        return true;
    }

    @Override
    @Nullable
    public MagicNPC getNPC(@Nullable Entity entity) {
        return npcsByEntity.get(entity.getUniqueId());
    }

    @Override
    @Nullable
    public MagicNPC getNPC(UUID id) {
        return npcs.get(id);
    }

    public void restoreNPCs(final Chunk chunk) {
        String chunkKey = getChunkKey(chunk);
        List<MagicNPC> chunkData = npcsByChunk.get(chunkKey);
        if (chunkData != null) {
            for (MagicNPC npc : chunkData) {
                npc.restore();
                activateNPC(npc);
            }
        }
    }

    public void activateNPC(MagicNPC npc) {
        npcsByEntity.put(npc.getEntityId(), npc);
    }

    @Override
    @Nullable
    public String getPlaceholder(Player player, String namespace, String placeholder) {
        return placeholderAPIManager == null ? null : placeholderAPIManager.getPlaceholder(player, namespace, placeholder);
    }

    @Override
    @Nonnull
    public String setPlaceholders(Player player, String message) {
        return placeholderAPIManager == null ? message : placeholderAPIManager.setPlaceholders(player, message);
    }

    @Override
    public void registerMob(@Nonnull Entity entity, @Nonnull EntityData entityData) {
        mobs.register(entity, (com.elmakers.mine.bukkit.entity.EntityData)entityData);
    }

    public CitizensController getCitizensController() {
        return citizens;
    }

    @Override
    public void lockChunk(Chunk chunk) {
        Integer locked = lockedChunks.get(chunk);
        if (locked == null) {
            lockedChunks.put(chunk, 1);
            CompatibilityUtils.lockChunk(chunk, plugin);
        } else {
            lockedChunks.put(chunk, locked + 1);
        }
    }

    @Override
    public void unlockChunk(Chunk chunk) {
        Integer locked = lockedChunks.get(chunk);
        if (locked == null || locked <= 1) {
            lockedChunks.remove(chunk);
            CompatibilityUtils.unlockChunk(chunk, plugin);
        } else {
            lockedChunks.put(chunk, locked - 1);
        }
    }

    @Override
    @Nonnull
    public Collection<Chunk> getLockedChunks() {
        return lockedChunks.keySet();
    }

    @Override
    @Nullable
    public String getResourcePackURL() {
        return resourcePacks.getDefaultResourcePackURL();
    }

    @Override
    @Nullable
    public String getResourcePackURL(CommandSender sender) {
        return resourcePacks.getResourcePackURL(sender);
    }

    @Override
    public boolean isUrlIconsEnabled() {
        return urlIconsEnabled;
    }

    @Override
    public boolean isLegacyIconsEnabled() {
        return legacyIconsEnabled;
    }

    public boolean resourcePackUsesSkulls(String pack) {
        Boolean packOverride = resourcePacks.resourcePackUsesSkulls(pack);
        return packOverride == null ? urlIconsEnabled : packOverride;
    }

    @Override
    public Collection<String> getAlternateResourcePacks() {
        return resourcePacks.getAlternateResourcePacks();
    }

    @Override
    public boolean isResourcePackEnabledByDefault() {
        return resourcePacks.isResourcePackEnabledByDefault();
    }

    @Override
    public boolean showConsoleCastFeedback() {
        return castConsoleFeedback;
    }

    public String getEditorURL() {
        return editorURL;
    }

    public void setReloadingMage(Mage mage) {
        this.reloadingMage = mage;
    }

    public boolean useAnimationEvents(Player player) {
        if (swingType == SwingType.ANIMATE) return true;
        if (swingType == SwingType.INTERACT) return false;
        return player.getGameMode() == GameMode.ADVENTURE;
    }

    @Override
    @Nullable
    public List<DeathLocation> getDeathLocations(Player player) {
        List<DeathLocation> locations = null;
        if (deadSoulsController != null) {
            locations = new ArrayList<>();
            deadSoulsController.getSoulLocations(player, locations);
        }
        return locations;
    }

    public boolean isDespawnMagicMobs() {
        return despawnMagicMobs;
    }

    public void checkLogs(CommandSender sender) {
        logger.notify(messages, sender);
    }

    public MagicWorld getMagicWorld(String name) {
        return worldController.getWorld(name);
    }

    /*
     * Private data
     */
    private static final String BUILTIN_SPELL_CLASSPATH = "com.elmakers.mine.bukkit.spell.builtin";

    private static final String LOST_WANDS_FILE     = "lostwands";
    private static final String WARPS_FILE          = "warps";
    private static final String SPELLS_DATA_FILE    = "spells";
    private static final String AUTOMATA_DATA_FILE  = "automata";
    private static final String NPC_DATA_FILE       = "npcs";
    private static final String URL_MAPS_FILE       = "imagemaps";

    private MaterialAndData                     redstoneReplacement             = new MaterialAndData(Material.OBSIDIAN);
    private @Nonnull MaterialSet                buildingMaterials               = MaterialSets.empty();
    private @Nonnull MaterialSet                indestructibleMaterials         = MaterialSets.empty();
    private @Nonnull MaterialSet                restrictedMaterials             = MaterialSets.empty();
    private @Nonnull MaterialSet                destructibleMaterials           = MaterialSets.empty();
    private @Nonnull MaterialSet                interactibleMaterials           = MaterialSets.empty();
    private @Nonnull MaterialSet                containerMaterials              = MaterialSets.empty();
    private @Nonnull MaterialSet                wearableMaterials               = MaterialSets.empty();
    private @Nonnull MaterialSet                meleeMaterials                  = MaterialSets.empty();
    private @Nonnull MaterialSet                climbableMaterials              = MaterialSets.empty();
    private @Nonnull MaterialSet                undoableMaterials               = MaterialSets.wildcard();

    private boolean                             backupInventories               = true;
    private int                                    undoTimeWindow                    = 6000;
    private int                                 undoQueueDepth                  = 256;
    private int                                    pendingQueueDepth                = 16;
    private int                                 undoMaxPersistSize              = 0;
    private boolean                             commitOnQuit                     = false;
    private boolean                             saveNonPlayerMages              = false;
    private String                              defaultWandPath                 = "";
    private WandMode                            defaultWandMode                    = WandMode.NONE;
    private WandMode                            defaultBrushMode                = WandMode.CHEST;
    private boolean                             showMessages                    = true;
    private boolean                             showCastMessages                = false;
    private String                                messagePrefix                    = "";
    private String                                castMessagePrefix                = "";
    private boolean                             soundsEnabled                   = true;
    private String                                welcomeWand                        = "";
    private int                                    messageThrottle                    = 0;
    private boolean                                spellDroppingEnabled            = false;
    private boolean                             fillingEnabled                  = false;
    private int                                 maxFillLevel                    = 0;
    private boolean                                essentialsSignsEnabled            = false;
    private boolean                                dynmapUpdate                    = true;
    private boolean                                dynmapShowWands                    = true;
    private boolean                                dynmapOnlyPlayerSpells            = false;
    private boolean                                dynmapShowSpells                = true;
    private boolean                                createWorldsEnabled                = true;
    private float                                maxDamagePowerMultiplier        = 2.0f;
    private float                                maxConstructionPowerMultiplier  = 5.0f;
    private float                                maxRadiusPowerMultiplier         = 2.5f;
    private float                                maxRadiusPowerMultiplierMax     = 4.0f;
    private float                                maxRangePowerMultiplier         = 3.0f;
    private float                                maxRangePowerMultiplierMax         = 5.0f;

    private float                                maxPower                        = 100.0f;
    private Map<String, DamageType>             damageTypes                     = new HashMap<>();
    private float                                maxCostReduction                 = 0.5f;
    private float                                maxCooldownReduction            = 0.5f;
    private int                                    maxMana                            = 1000;
    private int                                    maxManaRegeneration                = 100;
    private double                              worthBase                       = 1;
    private double                              worthSkillPoints                = 1;
    private String                              skillPointIcon                  = null;
    private boolean                             skillPointItemsEnabled          = true;
    private double                              worthXP                         = 1;
    private CurrencyItem                        currencyItem                    = null;
    private boolean                             spEnabled                       = true;
    private boolean                             spEarnEnabled                   = true;
    private int                                 spMaximum                       = 0;

    private boolean                             castCommandCostFree             = false;
    private boolean                             castCommandCooldownFree         = false;
    private float                                castCommandPowerMultiplier      = 0.0f;
    private boolean                             castConsoleCostFree             = false;
    private boolean                             castConsoleCooldownFree         = false;
    private float                                castConsolePowerMultiplier      = 0.0f;
    private float                                 costReduction                    = 0.0f;
    private float                                 cooldownReduction                = 0.0f;
    private int                                    autoUndo                        = 0;
    private int                                    autoSaveTaskId                    = 0;
    private BukkitTask                          configCheckTask                 = null;
    private BukkitTask                          logNotifyTask                   = null;
    private boolean                             savePlayerData                  = true;
    private boolean                             externalPlayerData              = false;
    private boolean                             asynchronousSaving              = true;
    private boolean                             debugEffectLib                  = false;
    private WarpController                        warpController                    = null;
    private Collection<ConfigurationSection>    materialColors                  = null;
    private List<Object>                        materialVariants                = null;
    private ConfigurationSection                blockItems                  = null;
    private ConfigurationSection                currencyConfiguration       = null;
    private Map<Material, String>               blockSkins                  = new HashMap<>();
    private Map<EntityType, String>             mobSkins                    = new HashMap<>();
    private Map<EntityType, MaterialAndData>    skullItems                  = new HashMap<>();
    private Map<EntityType, MaterialAndData>    skullWallBlocks             = new HashMap<>();
    private Map<EntityType, MaterialAndData>    skullGroundBlocks           = new HashMap<>();
    private Map<EntityType, Material>           mobEggs                     = new HashMap<>();

    private final Map<String, AutomatonTemplate> automatonTemplates         = new HashMap<>();
    private final Map<String, WandTemplate>     wandTemplates               = new HashMap<>();
    private final Map<String, MageClassTemplate> mageClasses                = new HashMap<>();
    private final Map<String, ModifierTemplate> modifiers                   = new HashMap<>();
    private final Map<String, SpellTemplate>    spells                      = new HashMap<>();
    private final Map<String, SpellTemplate>    spellAliases                = new HashMap<>();
    private final Map<String, SpellData>        templateDataMap             = new HashMap<>();
    private final Map<String, SpellCategory>    categories                  = new HashMap<>();
    private final Map<String, MagicAttribute>   attributes                  = new HashMap<>();
    private final Set<String>                   registeredAttributes        = new HashSet<>();
    private final Set<String>                   builtinAttributes           = ImmutableSet.of(
            "health", "health_max", "target_health", "target_health_max",
            "location_x", "location_y", "location_z",
            "time", "moon", "epoch",
            "mana", "mana_max", "xp", "level", "bowpull", "bowpower", "damage", "damage_dealt",
            "fall_distance"
    );
    private final Map<String, com.elmakers.mine.bukkit.magic.Mage> mages    = Maps.newConcurrentMap();
    private final Set<Mage> pendingConstruction                             = new HashSet<>();
    private final PriorityQueue<UndoList>       scheduledUndo               = new PriorityQueue<>();
    private final Map<String, WeakReference<Schematic>> schematics          = new HashMap<>();
    private final Map<String, Collection<EffectPlayer>> effects             = new HashMap<>();
    private final Map<Chunk, Integer> lockedChunks                          = new HashMap<>();

    private MageDataStore                       mageDataStore               = null;
    private MageDataStore                       migrateDataStore            = null;
    private MigrateDataTask                     migrateDataTask             = null;

    private final MagicLogger                   logger;
    private BukkitTask                          logWatchdogTimer            = null;
    private MagicPlugin                         plugin                      = null;
    private final File                            configFolder;
    private final File                            dataFolder;
    private final File                            defaultsFolder;

    private int                                 toggleMessageRange          = 1024;

    private int                                 automataUpdateFrequency     = 1;
    private int                                 mageUpdateFrequency         = 5;
    private int                                 workFrequency               = 1;
    private int                                 undoFrequency               = 10;
    private int                                    workPerUpdate                = 5000;
    private int                                 logVerbosity                = 0;

    private boolean                             showCastHoloText            = false;
    private boolean                             showActivateHoloText        = false;
    private int                                 castHoloTextRange           = 0;
    private int                                 activateHoloTextRange       = 0;
    private boolean                                urlIconsEnabled             = true;
    private boolean                             legacyIconsEnabled          = false;
    private boolean                             autoSpellUpgradesEnabled    = true;
    private boolean                             autoPathUpgradesEnabled     = true;
    private boolean                             spellProgressionEnabled     = true;

    private boolean                                bypassBuildPermissions      = false;
    private boolean                                bypassBreakPermissions      = false;
    private boolean                                bypassPvpPermissions        = false;
    private boolean                                bypassFriendlyFire          = false;
    private boolean                             useScoreboardTeams          = false;
    private boolean                             defaultFriendly             = true;
    private boolean                                protectLocked               = true;
    private boolean                             bindOnGive                  = false;
    private List<List<String>>                  permissionTeams             = null;

    private String                                extraSchematicFilePath        = null;
    private Mailer                                mailer                        = null;
    private Material                            defaultMaterial                = Material.DIRT;
    private Set<EntityType>                     undoEntityTypes             = new HashSet<>();
    private Set<EntityType>                     friendlyEntityTypes         = new HashSet<>();
    private Map<String, Currency> currencies = new HashMap<>();

    private PhysicsHandler                        physicsHandler                = null;

    private Map<String, List<MagicNPC>>          npcsByChunk                = new HashMap<>();
    private List<ConfigurationSection>           invalidNPCs                = new ArrayList<>();
    private Map<UUID, MagicNPC>                  npcsByEntity               = new HashMap<>();
    private Map<UUID, MagicNPC>                  npcs                       = new HashMap<>();

    private Map<String, Map<Long, Automaton>>    automata                   = new HashMap<>();
    private List<ConfigurationSection>           invalidAutomata            = new ArrayList<>();
    private Map<Long, Automaton>                 activeAutomata             = new HashMap<>();
    private Map<String, LostWand>                lostWands                  = new HashMap<>();
    private Map<String, Set<String>>             lostWandChunks             = new HashMap<>();
    private Map<Long, Integer>                   lightBlocks                = new HashMap<>();
    private Map<String, Integer>                 lightChunks                = new HashMap<>();

    private int                                    metricsLevel                = 5;
    private Metrics                                metrics                        = null;
    private boolean                                hasDynmap                    = false;
    private boolean                                hasEssentials                = false;
    private boolean                                hasCommandBook                = false;

    private String                              exampleDefaults             = null;
    private Collection<String>                  addExamples                 = null;
    private boolean                             loaded                      = false;
    private boolean                             shuttingDown                = false;
    private boolean                             dataLoaded                  = false;
    private String                              defaultSkillIcon            = "stick";
    private boolean despawnMagicMobs = false;
    private int                                 skillInventoryRows          = 6;
    private boolean                             skillsUseHeroes             = true;
    private boolean                             useHeroesMana               = true;
    private boolean                             useHeroesParties            = true;
    private boolean                             useSkillAPIAllies           = true;
    private boolean                             useBattleArenaTeams         = true;
    private boolean                             skillsUsePermissions        = false;
    private boolean                             useWildStacker              = true;
    private String                              heroesSkillPrefix           = "";
    private String                              skillsSpell                 = "";
    private boolean                             isFileLockingEnabled        = false;
    private int                                 fileLoadDelay               = 0;
    private Mage                                reloadingMage               = null;
    private ResourcePackManager                 resourcePacks               = null;

    // Synchronization
    private final Object                        saveLock                    = new Object();

    protected static Random                     random                      = new Random();

    // Sub-Controllers
    private CraftingController                    crafting                    = null;
    private MobController                       mobs                        = null;
    private MobController2                      mobs2                        = null;
    private ItemController                      items                        = null;
    private EnchantingController                enchanting                    = null;
    private AnvilController                        anvil                        = null;
    private Messages                            messages                    = new Messages();
    private MapController                       maps                        = null;
    private DynmapController                    dynmap                        = null;
    private ElementalsController                elementals                    = null;
    private CitizensController                  citizens                    = null;
    private BlockController                     blockController             = null;
    private HangingController                   hangingController           = null;
    private PlayerController                    playerController            = null;
    private EntityController                    entityController            = null;
    private InventoryController                 inventoryController         = null;
    private ExplosionController                 explosionController         = null;
    private JumpController                      jumpController              = null;
    private WorldController                     worldController             = null;
    private @Nonnull MageIdentifier             mageIdentifier              = new MageIdentifier();
    private final SimpleMaterialSetManager materialSetManager               = new SimpleMaterialSetManager();
    private boolean                             citizensEnabled                = true;
    private boolean                             logBlockEnabled             = true;
    private boolean                             libsDisguiseEnabled            = true;
    private boolean                             skillAPIEnabled                = true;
    private boolean                             useSkillAPIMana             = false;
    private boolean                             placeholdersEnabled         = true;
    private boolean                             lightAPIEnabled                = true;
    private boolean                             skriptEnabled                = true;
    private boolean                             vaultEnabled                = true;
    private ConfigurationSection                residenceConfiguration        = null;
    private ConfigurationSection                redProtectConfiguration     = null;
    private ConfigurationSection                citadelConfiguration        = null;
    private ConfigurationSection                mobArenaConfiguration       = null;
    private ConfigurationSection                ajParkourConfiguration      = null;
    private Set<String>                         resolvingKeys               = new LinkedHashSet<>();
    private boolean                             castConsoleFeedback         = false;
    private String                              editorURL                   = null;
    private boolean                             reloadVerboseLogging        = true;
    private Map<String, MageData>               mageDataPreCache            = new ConcurrentHashMap<>();

    private boolean                             hasShopkeepers              = false;
    private FactionsManager                        factionsManager                = new FactionsManager();
    private LocketteManager                     locketteManager                = new LocketteManager();
    private WorldGuardManager                    worldGuardManager            = new WorldGuardManager();
    private PvPManagerManager                   pvpManager                  = new PvPManagerManager();
    private MultiverseManager                   multiverseManager           = new MultiverseManager();
    private PreciousStonesManager                preciousStonesManager        = new PreciousStonesManager();
    private TownyManager                        townyManager                = new TownyManager();
    private GriefPreventionManager              griefPreventionManager        = new GriefPreventionManager();
    private NCPManager                          ncpManager                   = new NCPManager();
    private ProtectionManager                   protectionManager           = new ProtectionManager();
    private AJParkourManager                    ajParkourManager              = null;
    private CitadelManager                      citadelManager              = null;
    private ResidenceManager                    residenceManager            = null;
    private RedProtectManager                   redProtectManager           = null;
    private RequirementsController              requirementsController      = null;
    private HeroesManager                       heroesManager               = null;
    private LibsDisguiseManager                 libsDisguiseManager         = null;
    private SkillAPIManager                     skillAPIManager             = null;
    private BattleArenaManager                  battleArenaManager          = null;
    private PlaceholderAPIManager               placeholderAPIManager       = null;
    private LightAPIManager                     lightAPIManager             = null;
    private MobArenaManager                     mobArenaManager             = null;
    private LogBlockManager                     logBlockManager             = null;
    private EssentialsController                essentialsController        = null;
    private DeadSoulsManager                    deadSoulsController         = null;

    private boolean                             loading                     = false;
    private Set<MagicProvider>                  externalProviders           = new HashSet<>();
    private List<BlockBreakManager>             blockBreakManagers          = new ArrayList<>();
    private List<BlockBuildManager>             blockBuildManagers          = new ArrayList<>();
    private List<PVPManager>                    pvpManagers                 = new ArrayList<>();
    private List<CastPermissionManager>         castManagers                = new ArrayList<>();
    private List<AttributeProvider>             attributeProviders          = new ArrayList<>();
    private List<TeamProvider>                  teamProviders               = new ArrayList<>();
    private List<EntityTargetingManager>        targetingProviders          = new ArrayList<>();
    private NPCSupplierSet                      npcSuppliers                = new NPCSupplierSet();
    private Map<String, RequirementsProcessor>  requirementProcessors       = new HashMap<>();
    private Map<String, PlayerWarpManager>      playerWarpManagers          = new HashMap<>();
    private Map<Material, String>               autoWands                   = new HashMap<>();
    private Map<String, String>                 builtinExternalExamples     = new HashMap<>();
    private boolean                             showExampleInstructions     = false;
    private SwingType                           swingType                   = SwingType.ANIMATE_IF_ADVENTURE;
}
