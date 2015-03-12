package com.elmakers.mine.bukkit.magic;

import java.io.File;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.CodeSource;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.elmakers.mine.bukkit.api.action.GUIAction;
import com.elmakers.mine.bukkit.api.event.SaveEvent;
import com.elmakers.mine.bukkit.api.spell.*;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.citizens.CitizensController;
import com.elmakers.mine.bukkit.maps.MapController;
import com.elmakers.mine.bukkit.protection.LocketteManager;
import com.elmakers.mine.bukkit.protection.MultiverseManager;
import com.elmakers.mine.bukkit.protection.PreciousStonesManager;
import com.elmakers.mine.bukkit.protection.PvPManagerManager;
import com.elmakers.mine.bukkit.protection.TownyManager;
import com.elmakers.mine.bukkit.action.ActionHandler;
import com.elmakers.mine.bukkit.utility.*;
import com.elmakers.mine.bukkit.wand.*;
import com.elmakers.mine.bukkit.wand.LostWand;
import com.elmakers.mine.bukkit.wand.Wand;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.*;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.mcstats.Metrics;
import org.mcstats.Metrics.Graph;

import com.elmakers.mine.bukkit.api.block.BoundingBox;
import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.block.Automaton;
import com.elmakers.mine.bukkit.block.BlockData;
import com.elmakers.mine.bukkit.block.MaterialBrush;
import com.elmakers.mine.bukkit.block.WorldEditSchematic;
import com.elmakers.mine.bukkit.dynmap.DynmapController;
import com.elmakers.mine.bukkit.effect.EffectPlayer;
import com.elmakers.mine.bukkit.elementals.ElementalsController;
import com.elmakers.mine.bukkit.essentials.MagicItemDb;
import com.elmakers.mine.bukkit.essentials.Mailer;
import com.elmakers.mine.bukkit.magic.command.MagicTabExecutor;
import com.elmakers.mine.bukkit.magic.listener.AnvilController;
import com.elmakers.mine.bukkit.magic.listener.CraftingController;
import com.elmakers.mine.bukkit.magic.listener.EnchantingController;
import com.elmakers.mine.bukkit.metrics.CategoryCastPlotter;
import com.elmakers.mine.bukkit.metrics.DeltaPlotter;
import com.elmakers.mine.bukkit.metrics.SpellCastPlotter;
import com.elmakers.mine.bukkit.protection.FactionsManager;
import com.elmakers.mine.bukkit.protection.WorldGuardManager;
import com.elmakers.mine.bukkit.spell.SpellCategory;
import com.elmakers.mine.bukkit.traders.TradersController;
import com.elmakers.mine.bukkit.utilities.CompleteDragTask;
import com.elmakers.mine.bukkit.utilities.DataStore;
import com.elmakers.mine.bukkit.warp.WarpController;

public class MagicController implements Listener, MageController {
    public MagicController(final MagicPlugin plugin) {
        this.plugin = plugin;

        configFolder = plugin.getDataFolder();
        configFolder.mkdirs();

        dataFolder = new File(configFolder, "data");
        dataFolder.mkdirs();

        schematicFolder = new File(configFolder, "schematics");
        schematicFolder.mkdirs();

        playerDataFolder = new File(dataFolder, "players");
        playerDataFolder.mkdirs();

        defaultsFolder = new File(configFolder, "defaults");
        defaultsFolder.mkdirs();
    }

    @Override
    public Mage getMage(String mageId, String mageName) {
        return getMage(mageId, mageName, null, null);
    }

    public Mage getMage(String mageId, CommandSender commandSender, Entity entity) {
        return getMage(mageId, null, commandSender, entity);
    }

    protected Mage getMage(String mageId, String mageName, CommandSender commandSender, Entity entity) {
        Mage apiMage = null;
        if (!mages.containsKey(mageId)) {
            final com.elmakers.mine.bukkit.magic.Mage mage = new com.elmakers.mine.bukkit.magic.Mage(mageId, this);
            mages.put(mageId, mage);
            mage.setName(mageName);
            mage.setCommandSender(commandSender);
            mage.setEntity(entity);
            if (commandSender instanceof Player) {
                mage.setPlayer((Player) commandSender);
            }

            // Check for existing data file
            // For now we only do async loads for Players
            final File playerFile = new File(playerDataFolder, mageId + ".dat");
            if (playerFile.exists()) {
                if (commandSender instanceof Player) {
                    mage.setLoading(true);
                    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                        @Override
                        public void run() {
                            synchronized (saveLock) {
                                getLogger().info("Loading mage data from file " + playerFile.getName());
                                try {
                                    final Configuration playerData = YamlConfiguration.loadConfiguration(playerFile);
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                        @Override
                                        public void run() {
                                            mage.load(playerData);
                                        }
                                    }, 1);
                                } catch (Exception ex) {
                                    getLogger().warning("Failed to load mage data from file " + playerFile.getName());
                                    ex.printStackTrace();
                                }
                                mage.setLoading(false);
                            }
                        }
                    });
                } else {
                    getLogger().info("Loading mage data from file " + playerFile.getName() + " synchronously");
                    synchronized (saveLock) {
                        try {
                            final Configuration playerData = YamlConfiguration.loadConfiguration(playerFile);
                            mage.load(playerData);
                        } catch (Exception ex) {
                            getLogger().warning("Failed to load mage data from file " + playerFile.getName());
                            ex.printStackTrace();
                        }
                    }
                }
            } else {
                mage.load(null);
            }

            apiMage = mage;
        } else {
            apiMage = mages.get(mageId);
            if (apiMage instanceof com.elmakers.mine.bukkit.magic.Mage) {
                com.elmakers.mine.bukkit.magic.Mage mage = (com.elmakers.mine.bukkit.magic.Mage) apiMage;

                // Re-set mage properties
                mage.setName(mageName);
                mage.setCommandSender(commandSender);
                mage.setEntity(entity);
                if (commandSender instanceof Player) {
                    mage.setPlayer((Player) commandSender);
                }
            }
        }
        return apiMage;
    }

    @Override
    public com.elmakers.mine.bukkit.api.magic.Mage getMage(Player player) {
        return getMage((Entity) player, player);
    }

    @Override
    public com.elmakers.mine.bukkit.api.magic.Mage getMage(Entity entity) {
        CommandSender commandSender = (entity instanceof Player) ? (Player) entity : null;
        return getMage(entity, commandSender);
    }

    protected com.elmakers.mine.bukkit.api.magic.Mage getMage(Entity entity, CommandSender commandSender) {
        if (entity == null) return getMage(commandSender);
        String id = entity.getUniqueId().toString();

        // Check for Citizens NPC
        if (isNPC(entity)) {
            id = "NPC-" + id;
        }
        return getMage(id, commandSender, entity);
    }

    @Override
    public Mage getMage(CommandSender commandSender) {
        String mageId = "COMMAND";
        if (commandSender instanceof ConsoleCommandSender) {
            mageId = "CONSOLE";
        } else if (commandSender instanceof Player) {
            return getMage((Player) commandSender);
        } else if (commandSender instanceof BlockCommandSender) {
            BlockCommandSender commandBlock = (BlockCommandSender) commandSender;
            String commandName = commandBlock.getName();
            if (commandName != null && commandName.length() > 0) {
                mageId = "COMMAND-" + commandBlock.getName();
            }
        }

        return getMage(mageId, commandSender, null);
    }

    public void addSpell(Spell variant) {
        SpellTemplate conflict = spells.get(variant.getKey());
        if (conflict != null) {
            getLogger().log(Level.WARNING, "Duplicate spell key: '" + conflict.getKey() + "'");
        } else {
            spells.put(variant.getKey(), variant);
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

    public float getMaxHaste() {
        return maxHaste;
    }

    public float getMaxHealthRegeneration() {
        return maxHealthRegeneration;
    }

    public float getMaxHungerRegeneration() {
        return maxHungerRegeneration;
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

    public boolean soundsEnabled() {
        return soundsEnabled;
    }

    public boolean fillWands() {
        return fillingEnabled;
    }

    public boolean bindWands() {
        return bindingEnabled;
    }

    public boolean keepWands() {
        return keepingEnabled;
    }

    /*
	 * Get the log, if you need to debug or log errors.
	 */
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
        // Check the region manager, or Factions
        boolean allowed = true;
        if (bypassBuildPermissions) return true;
        if (player != null && player.hasPermission("Magic.bypass_build")) return true;

        allowed = allowed && worldGuardManager.hasBuildPermission(player, block);
        allowed = allowed && factionsManager.hasBuildPermission(player, block);
        allowed = allowed && locketteManager.hasBuildPermission(player, block);
        allowed = allowed && preciousStonesManager.hasBuildPermission(player, block);
        allowed = allowed && townyManager.hasBuildPermission(player, block);

        return allowed;
    }

    public boolean schematicsEnabled() {
        return cuboidClipboardClass != null;
    }

    public void clearCache() {
        // Only delete schematics that we have builtins for.
        String[] schematicFiles = schematicFolder.list();
        for (String schematicFilename : schematicFiles) {
            if (!schematicFilename.endsWith(".schematic")) continue;
            InputStream builtin = plugin.getResource("schematics/" + schematicFilename);
            if (builtin == null) continue;
            File schematicFile = new File(schematicFolder, schematicFilename);
            schematicFile.delete();
            plugin.getLogger().info("Deleted file " + schematicFile.getAbsolutePath());
        }

        schematics.clear();
        for (Mage mage : mages.values()) {
            if (mage instanceof com.elmakers.mine.bukkit.magic.Mage) {
                ((com.elmakers.mine.bukkit.magic.Mage) mage).clearCache();
            }
        }

        maps.clearCache();
    }

    public WorldEditSchematic loadSchematic(String schematicName) {
        if (schematicName == null || schematicName.length() == 0 || !schematicsEnabled()) return null;

        if (schematics.containsKey(schematicName)) {
            WeakReference<WorldEditSchematic> schematic = schematics.get(schematicName);
            if (schematic != null) {
                WorldEditSchematic cached = schematic.get();
                if (cached != null) {
                    return cached;
                }
            }
        }

        String fileName = schematicName + ".schematic";
        File schematicFile = new File(schematicFolder, fileName);
        if (!schematicFile.exists()) {
            try {
                // Check extra path first
                File extraSchematicFile = null;
                if (extraSchematicFilePath != null && extraSchematicFilePath.length() > 0) {
                    File schematicFolder = new File(configFolder, "../" + extraSchematicFilePath);
                    extraSchematicFile = new File(schematicFolder, schematicName + ".schematic");
                    getLogger().info("Checking for external schematic: " + extraSchematicFile.getAbsolutePath());
                }

                if (extraSchematicFile != null && extraSchematicFile.exists()) {
                    schematicFile = extraSchematicFile;
                    getLogger().info("Loading file: " + extraSchematicFile.getAbsolutePath());
                } else {
                    plugin.saveResource("schematics/" + fileName, true);
                    getLogger().info("Adding builtin schematic: schematics/" + fileName);
                }
            } catch (Exception ex) {

            }
        }

        if (!schematicFile.exists()) {
            getLogger().warning("Could not load file: " + schematicFile.getAbsolutePath());
            return null;
        }

        try {
            Method loadSchematicMethod = cuboidClipboardClass.getMethod("loadSchematic", File.class);
            getLogger().info("Loading schematic file: " + schematicFile.getAbsolutePath());
            WorldEditSchematic schematic = new WorldEditSchematic(loadSchematicMethod.invoke(null, schematicFile));
            schematics.put(schematicName, new WeakReference<WorldEditSchematic>(schematic));
            return schematic;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    @Override
    public Collection<String> getBrushKeys() {
        List<String> names = new ArrayList<String>();
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
        Collection<String> schematicNames = new ArrayList<String>();
        if (!MaterialBrush.SchematicsEnabled) return schematicNames;

        // Load internal schematics.. this may be a bit expensive.
        try {
            CodeSource codeSource = MagicTabExecutor.class.getProtectionDomain().getCodeSource();
            if (codeSource != null) {
                URL jar = codeSource.getLocation();
                ZipInputStream zip = new ZipInputStream(jar.openStream());
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
        enchanting = new EnchantingController(this);
        anvil = new AnvilController(this);
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

        // Try to link to WorldEdit
        // Do this prior to load(), because the CraftingController
        // May need to know whether or not schematics are enabled.
        // TODO: Make wrapper class to avoid this reflection.
        try {
            cuboidClipboardClass = Class.forName("com.sk89q.worldedit.CuboidClipboard");
            Method loadSchematicMethod = cuboidClipboardClass.getMethod("loadSchematic", File.class);
            if (loadSchematicMethod != null) {
                getLogger().info("WorldEdit found, schematic brushes enabled.");
                MaterialBrush.SchematicsEnabled = true;
                hasWorldEdit = true;
            } else {
                cuboidClipboardClass = null;
            }
        } catch (Throwable ex) {
        }

        load();

        // Try to link to Essentials:
        Plugin essentials = plugin.getServer().getPluginManager().getPlugin("Essentials");
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

        // Check for dtlTraders
        tradersController = null;
        try {
            Plugin tradersPlugin = plugin.getServer().getPluginManager().getPlugin("dtlTraders");
            if (tradersPlugin != null) {
                tradersController = new TradersController();
                tradersController.initialize(this, tradersPlugin);
                getLogger().info("dtlTraders found, integrating for selling Wands, Spells, Brushes and Upgrades");
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
            tradersController = null;
        }

        if (tradersController == null) {
            getLogger().info("dtlTraders not found, will not integrate.");
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

        if (cuboidClipboardClass == null) {
            getLogger().info("WorldEdit not found, schematic brushes will not work.");
            MaterialBrush.SchematicsEnabled = false;
            hasWorldEdit = false;
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
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new TimedRunnable("Mage Tick") {
            public void onRun() {
                long threshold = System.currentTimeMillis() - MAGE_FORGET_THRESHOLD;
                for (Entry<String, Long> mageEntry : forgetMages.entrySet()) {
                    if (mageEntry.getValue() < threshold)
                        mages.remove(mageEntry.getKey());
                }
                forgetMages.clear();

                for (Mage mage : mages.values()) {
                    if (mage instanceof com.elmakers.mine.bukkit.magic.Mage) {
                        try {
                            ((com.elmakers.mine.bukkit.magic.Mage) mage).tick();
                        } catch (Exception ex) {
                            getLogger().log(Level.WARNING, "Error ticking Mage " + mage.getName(), ex);
                        }
                    }
                }
            }
        }, 0, mageUpdateFrequency);

        // Set up the Block update timer
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new TimedRunnable("Block Updates") {
            public void onRun() {
                for (Mage mage : pendingConstruction) {
                    if (mage instanceof com.elmakers.mine.bukkit.magic.Mage) {
                        ((com.elmakers.mine.bukkit.magic.Mage) mage).processPendingBatches(maxBlockUpdates);
                    }
                }
                pendingConstruction.removeAll(pendingConstructionRemoval);
                pendingConstructionRemoval.clear();
            }
        }, 0, blockUpdateFrequency);

        // Set up the Update check timer
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new TimedRunnable("Undo Scheduler") {
            public void onRun() {
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
        }, 0, undoFrequency);
        registerListeners();

        // Activate/load any active player Mages
        Player[] players = plugin.getServer().getOnlinePlayers();
        for (Player player : players) {
            getMage(player);
        }

        // Register crafting recipes
        crafting.register(plugin);

        initialized = true;
    }

    protected void activateMetrics() {
        // Activate Metrics
        final MagicController controller = this;
        metrics = null;
        if (metricsLevel > 0) {
            try {
                metrics = new Metrics(plugin);

                if (metricsLevel > 1) {
                    Graph integrationGraph = metrics.createGraph("Plugin Integration");
                    integrationGraph.addPlotter(new Metrics.Plotter("Essentials") {
                        @Override
                        public int getValue() {
                            return controller.hasEssentials ? 1 : 0;
                        }
                    });
                    integrationGraph.addPlotter(new Metrics.Plotter("WorldEdit") {
                        @Override
                        public int getValue() {
                            return controller.hasWorldEdit ? 1 : 0;
                        }
                    });
                    integrationGraph.addPlotter(new Metrics.Plotter("Dynmap") {
                        @Override
                        public int getValue() {
                            return controller.hasDynmap ? 1 : 0;
                        }
                    });
                    integrationGraph.addPlotter(new Metrics.Plotter("Factions") {
                        @Override
                        public int getValue() {
                            return controller.factionsManager.isEnabled() ? 1 : 0;
                        }
                    });
                    integrationGraph.addPlotter(new Metrics.Plotter("WorldGuard") {
                        @Override
                        public int getValue() {
                            return controller.worldGuardManager.isEnabled() ? 1 : 0;
                        }
                    });
                    integrationGraph.addPlotter(new Metrics.Plotter("Elementals") {
                        @Override
                        public int getValue() {
                            return controller.elementalsEnabled() ? 1 : 0;
                        }
                    });
                    integrationGraph.addPlotter(new Metrics.Plotter("Citizens") {
                        @Override
                        public int getValue() {
                            return controller.citizens != null ? 1 : 0;
                        }
                    });
                    integrationGraph.addPlotter(new Metrics.Plotter("Traders") {
                        @Override
                        public int getValue() {
                            return controller.tradersController != null ? 1 : 0;
                        }
                    });
                    integrationGraph.addPlotter(new Metrics.Plotter("CommandBook") {
                        @Override
                        public int getValue() {
                            return controller.hasCommandBook ? 1 : 0;
                        }
                    });
                    integrationGraph.addPlotter(new Metrics.Plotter("PvpManager") {
                        @Override
                        public int getValue() {
                            return controller.pvpManager.isEnabled() ? 1 : 0;
                        }
                    });
                    integrationGraph.addPlotter(new Metrics.Plotter("Multiverse-Core") {
                        @Override
                        public int getValue() {
                            return controller.multiverseManager.isEnabled() ? 1 : 0;
                        }
                    });
                    integrationGraph.addPlotter(new Metrics.Plotter("Towny") {
                        @Override
                        public int getValue() {
                            return controller.townyManager.isEnabled() ? 1 : 0;
                        }
                    });
                    integrationGraph.addPlotter(new Metrics.Plotter("PreciousStones") {
                        @Override
                        public int getValue() {
                            return controller.preciousStonesManager.isEnabled() ? 1 : 0;
                        }
                    });
                    integrationGraph.addPlotter(new Metrics.Plotter("Lockette") {
                        @Override
                        public int getValue() {
                            return controller.locketteManager.isEnabled() ? 1 : 0;
                        }
                    });

                    Graph featuresGraph = metrics.createGraph("Features Enabled");
                    featuresGraph.addPlotter(new Metrics.Plotter("Crafting") {
                        @Override
                        public int getValue() {
                            return controller.crafting.isEnabled() ? 1 : 0;
                        }
                    });
                    featuresGraph.addPlotter(new Metrics.Plotter("Enchanting") {
                        @Override
                        public int getValue() {
                            return controller.enchanting.isEnabled() ? 1 : 0;
                        }
                    });
                    featuresGraph.addPlotter(new Metrics.Plotter("Anvil Combining") {
                        @Override
                        public int getValue() {
                            return controller.anvil.isCombiningEnabled() ? 1 : 0;
                        }
                    });
                    featuresGraph.addPlotter(new Metrics.Plotter("Anvil Organizing") {
                        @Override
                        public int getValue() {
                            return controller.anvil.isOrganizingEnabled() ? 1 : 0;
                        }
                    });
                    featuresGraph.addPlotter(new Metrics.Plotter("Anvil Binding") {
                        @Override
                        public int getValue() {
                            return controller.bindingEnabled ? 1 : 0;
                        }
                    });
                    featuresGraph.addPlotter(new Metrics.Plotter("Anvil Keeping") {
                        @Override
                        public int getValue() {
                            return controller.keepingEnabled ? 1 : 0;
                        }
                    });
                }

                if (metricsLevel > 2) {
                    Graph categoryGraph = metrics.createGraph("Casts by Category");
                    for (final SpellCategory category : categories.values()) {
                        categoryGraph.addPlotter(new DeltaPlotter(new CategoryCastPlotter(category)));
                    }

                    Graph totalCategoryGraph = metrics.createGraph("Total Casts by Category");
                    for (final SpellCategory category : categories.values()) {
                        totalCategoryGraph.addPlotter(new CategoryCastPlotter(category));
                    }
                }

                if (metricsLevel > 3) {
                    Graph spellGraph = metrics.createGraph("Casts");
                    for (final SpellTemplate spell : spells.values()) {
                        if (!(spell instanceof Spell)) continue;
                        spellGraph.addPlotter(new DeltaPlotter(new SpellCastPlotter((Spell) spell)));
                    }

                    Graph totalCastGraph = metrics.createGraph("Total Casts");
                    for (final SpellTemplate spell : spells.values()) {
                        if (!(spell instanceof Spell)) continue;
                        totalCastGraph.addPlotter(new SpellCastPlotter((Spell) spell));
                    }
                }

                metrics.start();
                plugin.getLogger().info("Activated MCStats");
            } catch (Exception ex) {
                plugin.getLogger().warning("Failed to load MCStats: " + ex.getMessage());
            }
        }
    }

    protected void registerListeners() {
        PluginManager pm = plugin.getServer().getPluginManager();
        pm.registerEvents(this, plugin);
        pm.registerEvents(crafting, plugin);
        pm.registerEvents(enchanting, plugin);
        pm.registerEvents(anvil, plugin);
    }

    public Collection<Mage> getPending() {
        return pendingConstruction;
    }

    protected void addPending(Mage mage) {
        pendingConstruction.add(mage);
    }

    protected void removePending(Mage mage) {
        pendingConstructionRemoval.add(mage);
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

    protected DataStore createDataFile(String fileName) {
        File dataFile = new File(dataFolder, fileName + ".yml");
        DataStore configuration = new DataStore(getLogger(), dataFile);
        return configuration;
    }
    protected ConfigurationSection loadConfigFile(String fileName, boolean loadDefaults) {
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

    protected ConfigurationSection loadConfigFile(String fileName, boolean loadDefaults, boolean disableDefaults) {
        String configFileName = fileName + ".yml";
        File configFile = new File(configFolder, configFileName);
        if (!configFile.exists()) {
            getLogger().info("Saving template " + configFileName + ", edit to customize configuration.");
            plugin.saveResource(configFileName, false);
        }

        boolean usingExample = exampleDefaults != null && exampleDefaults.length() > 0;

        String examplesFileName = usingExample ? "examples/" + exampleDefaults + "/" + fileName + ".yml" : null;
        String defaultsFileName = "defaults/" + fileName + ".defaults.yml";

        plugin.saveResource(defaultsFileName, true);

        getLogger().info("Loading " + configFile.getName());
        ConfigurationSection overrides = YamlConfiguration.loadConfiguration(configFile);
        ConfigurationSection config = new MemoryConfiguration();

        if (loadDefaults) {
            getLogger().info(" Based on defaults " + defaultsFileName);
            Configuration defaultConfig = YamlConfiguration.loadConfiguration(plugin.getResource(defaultsFileName));
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
            try {
                InputStream input = plugin.getResource(examplesFileName);
                if (input != null)
                {
                    Configuration exampleConfig = YamlConfiguration.loadConfiguration(input);
                    if (disableDefaults) {
                        enableAll(exampleConfig);
                    }
                    config = ConfigurationUtils.addConfigurations(config, exampleConfig);
                    getLogger().info(" Using " + examplesFileName);
                }
            } catch (Exception ex) {
                getLogger().info(ex.getMessage());
            }
        }

        if (addExamples != null && addExamples.size() > 0) {
            for (String example : addExamples) {
                try {
                    examplesFileName = "examples/" + example + "/" + fileName + ".yml";
                    plugin.saveResource(examplesFileName, true);

                    InputStream input = plugin.getResource(examplesFileName);
                    if (input != null)
                    {
                        Configuration exampleConfig = YamlConfiguration.loadConfiguration(input);
                        if (disableDefaults) {
                            enableAll(exampleConfig);
                        }
                        config = ConfigurationUtils.addConfigurations(config, exampleConfig);
                        getLogger().info(" Added " + examplesFileName);
                    }
                } catch (Exception ex) {
                    getLogger().info(ex.getMessage());
                }
            }
        }

        config = ConfigurationUtils.addConfigurations(config, overrides);

        return config;
    }

    public void loadConfiguration() {
        // Clear some cache stuff... mainly this is for debuggin/testing.
        schematics.clear();

        // Load main configuration
        try {
            loadProperties(loadConfigFile(CONFIG_FILE, true));
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
                loadProperties(loadConfigFile(CONFIG_FILE, true));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Load localizations
        try {
            messages.reset();
            messages.load(loadConfigFile(MESSAGES_FILE, true));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Load materials configuration
        try {
            loadMaterials(loadConfigFile(MATERIALS_FILE, true));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Load spells
        try {
            loadSpells(loadConfigFile(SPELLS_FILE, loadDefaultSpells, disableDefaultSpells));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        getLogger().info("Loaded " + spells.size() + " spells");

        // Load enchanting paths
        try {
            enchanting.load(loadConfigFile(ENCHANTING_FILE, loadDefaultEnchanting));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        getLogger().info("Loaded " + enchanting.getCount() + " enchanting paths");

        // Load wand templates
        try {
            Wand.loadTemplates(loadConfigFile(WANDS_FILE, loadDefaultWands));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        getLogger().info("Loaded " + Wand.getWandTemplates().size() + " wands");

        // Load crafting recipes
        try {
            crafting.load(loadConfigFile(CRAFTING_FILE, loadDefaultCrafting));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        getLogger().info("Loaded " + crafting.getCount() + " crafting recipes");
    }

    protected void loadSpellData() {
        try {
            ConfigurationSection configNode = loadDataFile(SPELLS_DATA_FILE);
            if (configNode == null) return;

            Set<String> keys = configNode.getKeys(false);
            for (String key : keys) {
                SpellTemplate spell = getSpellTemplate(key);

                // Bit hacky to use the Spell load method on a SpellTemplate, but... meh!
                if (spell != null && spell instanceof MageSpell) {
                    ConfigurationSection spellSection = configNode.getConfigurationSection(key);
                    ((MageSpell) spell).load(spellSection);
                }
            }
        } catch (Exception ex) {
            getLogger().warning("Failed to load spell metrics");
        }
    }

    public void load() {
        loadConfiguration();
        loadSpellData();

        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            public void run() {
                // Load lost wands
                getLogger().info("Loading lost wand data");
                loadLostWands();

                // Load toggle-on-load blocks
                getLogger().info("Loading automata data");
                loadAutomata();

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

    protected void saveSpellData(Collection<DataStore> stores) {
        String lastKey = "";
        try {
            DataStore spellsDataFile = createDataFile(SPELLS_DATA_FILE);
            for (SpellTemplate spell : spells.values()) {
                lastKey = spell.getKey();
                ConfigurationSection spellNode = spellsDataFile.createSection(lastKey);
                if (spellNode == null) {
                    getLogger().warning("Error saving spell data for " + lastKey);
                    continue;
                }
                // Hackily re-using save
                if (spell != null && spell instanceof MageSpell) {
                    ((MageSpell) spell).save(spellNode);
                }
            }
            stores.add(spellsDataFile);
        } catch (Throwable ex) {
            getLogger().warning("Error saving spell data for " + lastKey);
            ex.printStackTrace();
        }
    }

    protected void saveLostWands(Collection<DataStore> stores) {
        String lastKey = "";
        try {
            DataStore lostWandsConfiguration = createDataFile(LOST_WANDS_FILE);
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

    protected void loadAutomata() {
        int automataCount = 0;
        try {
            ConfigurationSection toggleBlockData = loadDataFile(AUTOMATA_FILE);
            if (toggleBlockData != null) {
                Set<String> chunkIds = toggleBlockData.getKeys(false);
                for (String chunkId : chunkIds) {
                    ConfigurationSection chunkNode = toggleBlockData.getConfigurationSection(chunkId);
                    Map<Long, Automaton> restoreChunk = new HashMap<Long, Automaton>();
                    automata.put(chunkId, restoreChunk);
                    Set<String> blockIds = chunkNode.getKeys(false);
                    for (String blockId : blockIds) {
                        ConfigurationSection toggleConfig = chunkNode.getConfigurationSection(blockId);
                        Automaton toggle = new Automaton(toggleConfig);
                        restoreChunk.put(toggle.getId(), toggle);
                        automataCount++;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        getLogger().info("Loaded " + automataCount + " automata");
    }

    protected void saveAutomata(Collection<DataStore> stores) {
        try {
            DataStore automataData = createDataFile(AUTOMATA_FILE);
            for (Entry<String, Map<Long, Automaton>> toggleEntry : automata.entrySet()) {
                Collection<Automaton> blocks = toggleEntry.getValue().values();
                if (blocks.size() > 0) {
                    ConfigurationSection chunkNode = automataData.createSection(toggleEntry.getKey());
                    for (Automaton block : blocks) {
                        ConfigurationSection node = chunkNode.createSection(Long.toString(block.getId()));
                        block.save(node);
                    }
                }
            }
            stores.add(automataData);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected String getChunkKey(Chunk chunk) {
        return chunk.getWorld().getName() + "|" + chunk.getX() + "," + chunk.getZ();
    }

    public boolean addLostWand(LostWand lostWand) {
        lostWands.put(lostWand.getId(), lostWand);
        String chunkKey = getChunkKey(lostWand.getLocation().getChunk());
        Set<String> chunkWands = lostWandChunks.get(chunkKey);
        if (chunkWands == null) {
            chunkWands = new HashSet<String>();
            lostWandChunks.put(chunkKey, chunkWands);
        }
        chunkWands.add(lostWand.getId());

        if (dynmapShowWands) {
            addLostWandMarker(lostWand);
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
        String chunkKey = getChunkKey(lostWand.getLocation().getChunk());
        Set<String> chunkWands = lostWandChunks.get(chunkKey);
        if (chunkWands != null) {
            chunkWands.remove(wandId);
            if (chunkWands.size() == 0) {
                lostWandChunks.remove(chunkKey);
            }
        }

        if (dynmapShowWands) {
            if (removeMarker("wand-" + wandId, "Wands")) {
                getLogger().info("Wand removed from map");
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

    protected void savePlayerData(Collection<DataStore> stores) {
        try {
            for (Entry<String, Mage> mageEntry : mages.entrySet()) {
                File playerData = new File(playerDataFolder, mageEntry.getKey() + ".dat");
                DataStore playerConfig = new DataStore(getLogger(), playerData);
                Mage mage = mageEntry.getValue();
                if (!mage.isPlayer() && !saveNonPlayerMages)
                {
                    if (!mage.isValid())
                    {
                        forgetMages.put(mageEntry.getKey(), 0l);
                    }
                    continue;
                }

                if (!mage.isLoading()) {
                    mage.save(playerConfig);
                    stores.add(playerConfig);
                } else {
                    getLogger().info("Skipping save of mage, already loading: " + mage.getName());
                }

                // Check for players we can forget
                if (!mage.isValid())
                {
                    getLogger().info("Forgetting Offline mage " + mage.getName());
                    forgetMages.put(mageEntry.getKey(), 0l);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Forget players we don't need to keep in memory
        for (String forgetId : forgetMages.keySet()) {
            mages.remove(forgetId);
        }
        forgetMages.clear();
    }

    public void save()
    {
        save(false);
    }

	public void save(boolean asynchronous)
	{
        maps.save(asynchronous);

        final List<DataStore> saveData = new ArrayList<DataStore>();
		savePlayerData(saveData);
        getLogger().info("Saving " + saveData.size() + " players");
		saveSpellData(saveData);
		saveLostWands(saveData);
		saveAutomata(saveData);

        if (asynchronous) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    synchronized (saveLock) {
                        for (DataStore config : saveData) {
                            config.save();
                        }
                        getLogger().info("Finished saving");
                    }
                }
            });
        } else {
            synchronized (saveLock) {
                for (DataStore config : saveData) {
                    config.save();
                }
                getLogger().info("Finished saving");
            }
        }

        SaveEvent saveEvent = new SaveEvent(asynchronous);
        Bukkit.getPluginManager().callEvent(saveEvent);
	}

    protected ConfigurationSection getSpellConfig(String key, ConfigurationSection config, ConfigurationSection originalConfig)
    {
        return getSpellConfig(key, config, originalConfig, true);
    }

    protected ConfigurationSection getSpellConfig(String key, ConfigurationSection config, ConfigurationSection originalConfig, boolean addInherited)
    {
        ConfigurationSection spellNode = addInherited ?
                config.getConfigurationSection(key) :
                ConfigurationUtils.addConfigurations(new MemoryConfiguration(), originalConfig.getConfigurationSection(key));
        if (spellNode == null)
        {
            getLogger().warning("Spell " + key + " not known");
            return null;
        }

        SpellKey spellKey = new SpellKey(key);
        String inheritFrom = spellNode.getString("inherit");
        String upgradeInheritsFrom = null;
        if (spellKey.isVariant()) {
            int level = spellKey.getLevel();
            upgradeInheritsFrom = spellKey.getBaseKey();
            if (level != 2) {
                upgradeInheritsFrom += "|" + (level - 1);
            }
        }

        addInherited = addInherited && inheritFrom != null;
        if (addInherited || upgradeInheritsFrom != null)
        {
            if (addInherited)
            {
                ConfigurationSection inheritConfig = getSpellConfig(inheritFrom, config, originalConfig);
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
                    ConfigurationSection baseInheritConfig = getSpellConfig(upgradeInheritsFrom, config, originalConfig, inheritFrom == null);
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

        return spellNode;
    }

	protected void loadSpells(ConfigurationSection config)
	{
		if (config == null) return;
		
		// Reset existing spells.
		spells.clear();
        spellAliases.clear();

        // Configuration will be modified in-place, but inherited spells
        // need access to the original configs
        ConfigurationSection originalConfig = ConfigurationUtils.addConfigurations(new MemoryConfiguration(), config);

        Set<String> spellKeys = config.getKeys(false);
		for (String key : spellKeys)
		{
            if (key.equals("default")) continue;

            ConfigurationSection spellNode = getSpellConfig(key, config, originalConfig);
            if (spellNode == null || !spellNode.getBoolean("enabled", true)) {
                continue;
            }

            // Kind of a hacky way to do this, and only works with BaseSpell spells.
            if (allPvpRestricted) {
                spellNode.set("pvp_restricted", true);
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
		
		// Update registered mages so their spells are current
		for (Mage mage : mages.values()) {
            if (mage instanceof com.elmakers.mine.bukkit.magic.Mage) {
                ((com.elmakers.mine.bukkit.magic.Mage)mage).loadSpells(config);
            }
		}
	}

	public static Spell loadSpell(String name, ConfigurationSection node, MageController controller)
	{
		String className = node.getString("class");
		if (className == null)
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
			controller.getLogger().warning("Error loading spell: " + className);
			return null;
		}

		Object newObject;
		try
		{
			newObject = spellClass.newInstance();
		}
		catch (Throwable ex)
		{

			controller.getLogger().warning("Error loading spell: " + className);
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
	
	protected void loadMaterials(ConfigurationSection materialNode)
	{
		if (materialNode == null) return;
		
		Set<String> keys = materialNode.getKeys(false);
		for (String key : keys) {
			materialSets.put(key, ConfigurationUtils.getMaterials(materialNode, key));
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
        if (materialSets.containsKey("wearable")) {
            wearableMaterials = materialSets.get("wearable");
        }
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

        exampleDefaults = properties.getString("example", exampleDefaults);
        addExamples = properties.getStringList("add_examples");

        showCastHoloText = properties.getBoolean("show_cast_holotext", showCastHoloText);
        showActivateHoloText = properties.getBoolean("show_activate_holotext", showCastHoloText);
        castHoloTextRange = properties.getInt("cast_holotext_range", castHoloTextRange);
        activateHoloTextRange = properties.getInt("activate_holotext_range", activateHoloTextRange);
        urlIconsEnabled = properties.getBoolean("url_icons_enabled", urlIconsEnabled);

		loadDefaultSpells = properties.getBoolean("load_default_spells", loadDefaultSpells);
        disableDefaultSpells = properties.getBoolean("disable_default_spells", disableDefaultSpells);
		loadDefaultWands = properties.getBoolean("load_default_wands", loadDefaultWands);
        loadDefaultCrafting = properties.getBoolean("load_default_crafting", loadDefaultCrafting);
        loadDefaultEnchanting = properties.getBoolean("load_default_enchanting", loadDefaultEnchanting);
        maxTNTPerChunk = properties.getInt("max_tnt_per_chunk", maxTNTPerChunk);
		undoQueueDepth = properties.getInt("undo_depth", undoQueueDepth);
        blockUpdateFrequency = properties.getInt("block_update_frequency", blockUpdateFrequency);
        mageUpdateFrequency = properties.getInt("mage_update_frequency", mageUpdateFrequency);
        undoFrequency = properties.getInt("undo_frequency", undoFrequency);
		pendingQueueDepth = properties.getInt("pending_depth", pendingQueueDepth);
		undoMaxPersistSize = properties.getInt("undo_max_persist_size", undoMaxPersistSize);
		commitOnQuit = properties.getBoolean("commit_on_quit", commitOnQuit);
        saveNonPlayerMages = properties.getBoolean("save_non_player_mages", saveNonPlayerMages);
        undoOnWorldSave = properties.getBoolean("undo_on_world_save", undoOnWorldSave);
        backupInventory = properties.getBoolean("backup_inventory", backupInventory);
        defaultWandPath = properties.getString("default_wand_path", "");
        defaultWandMode = Wand.parseWandMode(properties.getString("default_wand_mode", ""), defaultWandMode);
        defaultBrushMode = Wand.parseWandMode(properties.getString("default_brush_mode", ""), defaultBrushMode);
        brushSelectSpell = properties.getString("brush_select_spell", brushSelectSpell);
		showMessages = properties.getBoolean("show_messages", showMessages);
		showCastMessages = properties.getBoolean("show_cast_messages", showCastMessages);
		clickCooldown = properties.getInt("click_cooldown", clickCooldown);
		messageThrottle = properties.getInt("message_throttle", 0);
		maxBlockUpdates = properties.getInt("max_block_updates", maxBlockUpdates);
		ageDroppedItems = properties.getInt("age_dropped_items", ageDroppedItems);
		enableItemHacks = properties.getBoolean("enable_custom_item_hacks", enableItemHacks);
        enableCreativeModeEjecting = properties.getBoolean("enable_creative_mode_ejecting", enableCreativeModeEjecting);
		soundsEnabled = properties.getBoolean("sounds", soundsEnabled);
		fillingEnabled = properties.getBoolean("fill_wands", fillingEnabled);
		indestructibleWands = properties.getBoolean("indestructible_wands", indestructibleWands);
		keepWandsOnDeath = properties.getBoolean("keep_wands_on_death", keepWandsOnDeath);
		welcomeWand = properties.getString("welcome_wand", "");
		maxDamagePowerMultiplier = (float)properties.getDouble("max_power_damage_multiplier", maxDamagePowerMultiplier);
		maxConstructionPowerMultiplier = (float)properties.getDouble("max_power_construction_multiplier", maxConstructionPowerMultiplier);
		maxRangePowerMultiplier = (float)properties.getDouble("max_power_range_multiplier", maxRangePowerMultiplier);
		maxRangePowerMultiplierMax = (float)properties.getDouble("max_power_range_multiplier_max", maxRangePowerMultiplierMax);
		maxRadiusPowerMultiplier = (float)properties.getDouble("max_power_radius_multiplier", maxRadiusPowerMultiplier);
		maxRadiusPowerMultiplierMax = (float)properties.getDouble("max_power_radius_multiplier_max", maxRadiusPowerMultiplierMax);

        maxPower = (float)properties.getDouble("max_power", maxPower);
        maxHaste = (float)properties.getDouble("max_haste", maxHaste);
        maxHealthRegeneration = (float)properties.getDouble("max_health_regeneration", maxHealthRegeneration);
        maxHungerRegeneration = (float)properties.getDouble("max_hunger_regeneration", maxHungerRegeneration);
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

        costReduction = (float)properties.getDouble("cost_reduction", costReduction);
		cooldownReduction = (float)properties.getDouble("cooldown_reduction", cooldownReduction);
		castCommandCostReduction = (float)properties.getDouble("cast_command_cost_reduction", castCommandCostReduction);
		castCommandCooldownReduction = (float)properties.getDouble("cast_command_cooldown_reduction", castCommandCooldownReduction);
		castCommandPowerMultiplier = (float)properties.getDouble("cast_command_power_multiplier", castCommandPowerMultiplier);
		autoUndo = properties.getInt("auto_undo", autoUndo);
        spellDroppingEnabled = properties.getBoolean("allow_spell_dropping", spellDroppingEnabled);
		bindingEnabled = properties.getBoolean("enable_binding", bindingEnabled);
		keepingEnabled = properties.getBoolean("enable_keeping", keepingEnabled);
		essentialsSignsEnabled = properties.getBoolean("enable_essentials_signs", essentialsSignsEnabled);
        citizensEnabled = properties.getBoolean("enable_citizens", citizensEnabled);
		dynmapShowWands = properties.getBoolean("dynmap_show_wands", dynmapShowWands);
		dynmapShowSpells = properties.getBoolean("dynmap_show_spells", dynmapShowSpells);
        dynmapOnlyPlayerSpells = properties.getBoolean("dynmap_only_player_spells", dynmapOnlyPlayerSpells);
		dynmapUpdate = properties.getBoolean("dynmap_update", dynmapUpdate);
		bypassBuildPermissions = properties.getBoolean("bypass_build", bypassBuildPermissions);
		bypassPvpPermissions = properties.getBoolean("bypass_pvp", bypassPvpPermissions);
        allPvpRestricted = properties.getBoolean("pvp_restricted", allPvpRestricted);
		extraSchematicFilePath = properties.getString("schematic_files", extraSchematicFilePath);
		createWorldsEnabled = properties.getBoolean("enable_world_creation", createWorldsEnabled);

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
        townyManager.setEnabled(properties.getBoolean("towny_enabled", townyManager.isEnabled()));
        locketteManager.setEnabled(properties.getBoolean("lockette_enabled", locketteManager.isEnabled()));

        metricsLevel = properties.getInt("metrics_level", metricsLevel);

		if (properties.contains("mana_display")) {
			Wand.retainLevelDisplay = properties.getString("mana_display").equals("hybrid");
			Wand.displayManaAsBar = !properties.getString("mana_display").equals("number");
            Wand.displayManaAsDurability = properties.getString("mana_display").equals("durability");
            Wand.displayManaAsGlow = properties.getString("mana_display").equals("glow");
		}

		// Parse wand settings
		Wand.DefaultUpgradeMaterial = ConfigurationUtils.getMaterial(properties, "wand_upgrade_item", Wand.DefaultUpgradeMaterial);
		Wand.EnableGlow = properties.getBoolean("wand_glow", Wand.EnableGlow);
        Wand.SpellGlow = properties.getBoolean("spell_glow", Wand.SpellGlow);
        Wand.LiveHotbar = properties.getBoolean("live_hotbar", Wand.LiveHotbar);
        Wand.BrushGlow = properties.getBoolean("brush_glow", Wand.BrushGlow);
        Wand.BrushItemGlow = properties.getBoolean("brush_item_glow", Wand.BrushItemGlow);
        Wand.WAND_KEY = properties.getString("wand_key", "wand");

        MaterialBrush.CopyMaterial = ConfigurationUtils.getMaterial(properties, "copy_item", MaterialBrush.CopyMaterial);
		MaterialBrush.EraseMaterial = ConfigurationUtils.getMaterial(properties, "erase_item", MaterialBrush.EraseMaterial);
		MaterialBrush.CloneMaterial = ConfigurationUtils.getMaterial(properties, "clone_item", MaterialBrush.CloneMaterial);
		MaterialBrush.ReplicateMaterial = ConfigurationUtils.getMaterial(properties, "replicate_item", MaterialBrush.ReplicateMaterial);
		MaterialBrush.SchematicMaterial = ConfigurationUtils.getMaterial(properties, "schematic_item", MaterialBrush.SchematicMaterial);
		MaterialBrush.MapMaterial = ConfigurationUtils.getMaterial(properties, "map_item", MaterialBrush.MapMaterial);
        MaterialBrush.DefaultBrushMaterial = ConfigurationUtils.getMaterial(properties, "default_brush_item", MaterialBrush.DefaultBrushMaterial);

        MaterialBrush.CopyCustomIcon = properties.getString("copy_icon_url", MaterialBrush.CopyCustomIcon);
        MaterialBrush.EraseCustomIcon = properties.getString("erase_icon_url", MaterialBrush.EraseCustomIcon);
        MaterialBrush.CloneCustomIcon = properties.getString("clone_icon_url", MaterialBrush.CloneCustomIcon);
        MaterialBrush.ReplicateCustomIcon = properties.getString("replicate_icon_url", MaterialBrush.ReplicateCustomIcon);
        MaterialBrush.SchematicCustomIcon = properties.getString("schematic_icon_url", MaterialBrush.SchematicCustomIcon);
        MaterialBrush.MapCustomIcon = properties.getString("map_icon_url", MaterialBrush.MapCustomIcon);
        MaterialBrush.DefaultBrushCustomIcon = properties.getString("default_brush_icon_url", MaterialBrush.DefaultBrushCustomIcon);

        Wand.inventoryOpenSound = ConfigurationUtils.toSoundEffect(properties.getString("wand_inventory_open_sound"));
        Wand.inventoryCloseSound = ConfigurationUtils.toSoundEffect(properties.getString("wand_inventory_close_sound"));
        Wand.inventoryCycleSound = ConfigurationUtils.toSoundEffect(properties.getString("wand_inventory_cycle_sound"));

        wandAbuseDamage = properties.getDouble("wand_abuse_damage", 0);
        preventMeleeDamage = properties.getBoolean("prevent_melee_damage", false);

		// Set up other systems
		EffectPlayer.SOUNDS_ENABLED = soundsEnabled;

		// Set up auto-save timer
		final MagicController saveController = this;
		int autoSaveIntervalTicks = properties.getInt("auto_save", 0) * 20 / 1000;;
		if (autoSaveIntervalTicks > 1) {
			autoSaveTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin,
					new TimedRunnable("Auto Save") {
						public void onRun() {
                            saveController.getLogger().info("Auto-saving Magic data");
							saveController.save(true);
						}
					},
					autoSaveIntervalTicks, autoSaveIntervalTicks);
		}

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
        pendingConstructionRemoval.clear();
		spells.clear();
	}
	
	protected void unregisterPhysicsHandler(Listener listener)
	{
		BlockPhysicsEvent.getHandlerList().unregister(listener);
		physicsHandler = null;
	}
	
	protected void registerForUndo(Mage mage)
	{
		pendingUndo.add(mage.getId());
	}

    @Override
    public void scheduleUndo(UndoList undoList)
    {
        scheduledUndo.add(undoList);
    }

	public boolean hasWandPermission(Player player)
	{
		return hasPermission(player, "Magic.wand.use", true);
	}

    public boolean hasCastPermission(CommandSender sender, SpellTemplate spell)
    {
        if (sender == null) return true;

        if (sender instanceof Player && ((Player)sender).hasPermission("Magic.bypass")) {
            return true;
        }
        return hasPermission(sender, spell.getPermissionNode(), true);
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
	
	public boolean hasPermission(CommandSender sender, String pNode)
	{
		if (!(sender instanceof Player)) return true;
		return hasPermission((Player)sender, pNode, false);
	}
	
	public boolean hasPermission(CommandSender sender, String pNode, boolean defaultValue)
	{
		if (!(sender instanceof Player)) return true;
		return hasPermission((Player)sender, pNode, defaultValue);
	}

	/*
	 * Listeners / callbacks
	 */

    @EventHandler
    public void onWorldSaveEvent(WorldSaveEvent event) {
        if (!undoOnWorldSave) return;

        World world = event.getWorld();
        List<Player> players = world.getPlayers();
        for (Player player : players) {
            if (isMage(player)) {
                com.elmakers.mine.bukkit.api.block.UndoQueue queue = getMage(player).getUndoQueue();
                if (queue != null) {
                    int undone = queue.undoScheduled();
                    if (undone  > 0) {
                        getLogger().info("Undid " + undone + " spells for " + player.getName() + "prior to save of world " + world.getName());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        final Projectile projectile = event.getEntity();

        // This is delayed so that the EntityDamage version takes precedence
        if (ActionHandler.hasActions(projectile))
        {
            Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(), new Runnable() {
                @Override
                public void run() {
                    ActionHandler.runActions(projectile, projectile.getLocation(), null);
                    ActionHandler.runEffects(projectile);
                }
            }, 1L);
        }
    }

	@EventHandler
	public void onEntityChangeBlockEvent(EntityChangeBlockEvent event) {
		Entity entity = event.getEntity();

		if (entity instanceof FallingBlock) {
            ActionHandler.runActions(entity, entity.getLocation(), null);
            ActionHandler.runEffects(entity);
            UndoList blockList = com.elmakers.mine.bukkit.block.UndoList.getUndoList(entity);
			if (blockList != null) {
                blockList.convert(entity, event.getBlock());
			} else {
				registerFallingBlock(entity, event.getBlock());
			}
		}
	}

    @EventHandler
    public void onEntityCombust(EntityCombustEvent event)
    {
        Entity entity = event.getEntity();
        if (isMage(entity)) {
            Mage apiMage = getMage(event.getEntity());
            if (!(apiMage instanceof com.elmakers.mine.bukkit.magic.Mage)) return;
            com.elmakers.mine.bukkit.magic.Mage mage = (com.elmakers.mine.bukkit.magic.Mage) apiMage;

            mage.onPlayerCombust(event);
        }

        if (!event.isCancelled())
        {
            UndoList undoList = getPendingUndo(entity.getLocation());
            if (undoList != null)
            {
                undoList.modify(entity);
            }
        }
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        Block targetBlock = event.getToBlock();
        Block sourceBlock = event.getBlock();
        UndoList undoList = getPendingUndo(sourceBlock.getLocation());
        if (undoList != null)
        {
            undoList.add(targetBlock, true);
        }
        else
        {
            undoList = getPendingUndo(targetBlock.getLocation());
            if (undoList != null)
            {
                undoList.add(targetBlock, true);
            }
        }
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        Block targetBlock = event.getBlock();
        UndoList undoList = getPendingUndo(targetBlock.getLocation());
        if (undoList != null)
        {
            undoList.add(targetBlock, true);
        }
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        BlockIgniteEvent.IgniteCause cause = event.getCause();
        if (cause == BlockIgniteEvent.IgniteCause.ENDER_CRYSTAL || cause == BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL)
        {
            return;
        }

        Entity entity = event.getIgnitingEntity();
        UndoList entityList = getEntityUndo(entity);
        if (entityList != null)
        {
            entityList.add(event.getBlock(), true);
            return;
        }

        Block ignitingBlock = event.getIgnitingBlock();
        Block targetBlock = event.getBlock();
        if (ignitingBlock != null)
        {
            UndoList undoList = getPendingUndo(ignitingBlock.getLocation());
            if (undoList != null)
            {
                undoList.add(event.getBlock(), true);
                return;
            }
        }

        UndoList undoList = getPendingUndo(targetBlock.getLocation());
        if (undoList != null)
        {
            undoList.add(targetBlock, true);
        }
    }

    protected UndoList getPendingUndo(Location location)
    {
        return com.elmakers.mine.bukkit.block.UndoList.getUndoList(location);
    }
	
	protected void registerFallingBlock(Entity fallingBlock, Block block) {
        UndoList undoList = getPendingUndo(fallingBlock.getLocation());
        if (undoList != null) {
            undoList.fall(fallingBlock, block);
        }
	}
	
	@EventHandler
	public void onInventoryDrag(InventoryDragEvent event) {
		if (!enableItemHacks || event.isCancelled()) return;
		
		// this is a huge hack! :\
		// I apologize for any weird behavior this causes.
		// Bukkit, unfortunately, will blow away NBT data for anything you drag
		// Which will nuke a wand or spell.
		// To make matters worse, Bukkit passes a copy of the item in the event, so we can't 
		// even check for metadata and only cancel the event if it involves one of our special items.
		// The best I can do is look for metadata at all, since Bukkit will retain the name and lore.
		
		// I have now decided to copy over the CB default handler for this, and cancel the event.
		// The only change I have made is that *real* ItemStack copies are made, instead of shallow Bukkit ones.
		ItemStack oldStack = event.getOldCursor();
		HumanEntity entity = event.getWhoClicked();
		if (oldStack != null && oldStack.hasItemMeta() && entity instanceof Player) {
			// Only do this if we're only dragging one item, since I don't 
			// really know what happens or how you would drag more than one.
			Map<Integer, ItemStack> draggedSlots = event.getNewItems();
			if (draggedSlots.size() != 1) return;
			
			event.setCancelled(true);
			
			// Cancelling the event will put the item back on the cursor,
			// and skip updating the inventory.
			
			// So we will wait one tick and then fix this up using the original item.
			InventoryView view = event.getView();
			for (Integer dslot : draggedSlots.keySet()) {
				CompleteDragTask completeDrag = new CompleteDragTask((Player)entity, view, dslot);
				completeDrag.runTaskLater(plugin, 1);
            }
			
			return;
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (event.isCancelled()) return;
		Entity entity = event.getEntity();

		if (entity instanceof Projectile || entity instanceof TNTPrimed) return;

		Entity damager = event.getDamager();
		UndoList undoList = getEntityUndo(damager);
		if (undoList != null)
        {
            // Prevent dropping items from frames,
            undoList.modify(entity);
            if (entity instanceof ItemFrame && event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK)
            {
                ItemFrame frame = (ItemFrame)entity;
                frame.setItem(null);
            }
		}

        ActionHandler.targetEffects(damager, entity);
        ActionHandler.runActions(damager, entity.getLocation(), entity);

        if (preventMeleeDamage && event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK && damager instanceof Player && entity instanceof Player)
        {
            Player player = (Player)damager;
            ItemStack itemInHand = player.getItemInHand();
            if (!isSword(itemInHand))
            {
                event.setCancelled(true);
            }
        }
        else if (wandAbuseDamage > 0 && event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK && damager instanceof Player && isMage(damager) && entity instanceof Player)
        {
            Player player = (Player)damager;
            Mage mage = getMage(player);
            com.elmakers.mine.bukkit.api.wand.Wand wand = mage.getActiveWand();
            if (wand != null && !isSword(wand.getItem()))
            {
                event.setCancelled(true);
                player.playEffect(EntityEffect.HURT);
                player.damage(wandAbuseDamage);
            }
        }
	}

    protected boolean isSword(ItemStack item)
    {
        return item.getType() == Material.DIAMOND_SWORD ||
            item.getType() == Material.WOOD_SWORD ||
            item.getType() == Material.IRON_SWORD ||
            item.getType() == Material.STONE_SWORD ||
            item.getType() == Material.GOLD_SWORD;
    }
	
	protected UndoList getEntityUndo(Entity entity) {
		UndoList blockList = null;
		if (entity == null) return null;
        if (isMage(entity)) {
			Mage mage = getMage(entity);
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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent event) {
        Hanging entity = event.getEntity();
        // Early-out for performance, if we already detected the Entity
        if (entity.hasMetadata("broken")) return;

        Location location = entity.getLocation();
        location = location.getBlock().getRelative(entity.getAttachedFace()).getLocation();

        UndoList undoList = getPendingUndo(location);
        if (undoList != null) {
            // Prevent item drops, but still remove it
            // Else it'll probably just break again.
            event.setCancelled(true);
            undoList.modify(entity);
            entity.remove();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        Entity breakingEntity = event.getRemover();
        if (breakingEntity == null) return;

        Hanging entity = event.getEntity();
        Location location = entity.getLocation();
        location = location.getBlock().getRelative(entity.getAttachedFace()).getLocation();
        UndoList undoList = getPendingUndo(location);
        if (undoList != null) {
            entity.setMetadata("broken", new FixedMetadataValue(plugin, true));
            undoList.modify(entity);

            // Prevent item drops, but still remove it
            // Else it'll probably just break again.
            event.setCancelled(true);
            undoList.modify(entity);
            entity.remove();
        }
    }

    @EventHandler
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        Entity explodingEntity = event.getEntity();
        ActionHandler.runActions(explodingEntity, explodingEntity.getLocation(), null);
        ActionHandler.runEffects(explodingEntity);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityExplode(EntityExplodeEvent event) {
        Entity explodingEntity = event.getEntity();
        if (explodingEntity == null) return;

        UndoList blockList = getEntityUndo(explodingEntity);
        boolean cancel = event.isCancelled();
        cancel = cancel || explodingEntity.hasMetadata("cancel_explosion");
        if (cancel) {
            event.setCancelled(true);
        }
        else if (maxTNTPerChunk > 0 && explodingEntity.getType() == EntityType.PRIMED_TNT) {
            Chunk chunk = explodingEntity.getLocation().getChunk();
            if (chunk == null || !chunk.isLoaded()) return;

            int tntCount = 0;
            Entity[] entities = chunk.getEntities();
            for (Entity entity : entities) {
                if (entity != null && entity.getType() == EntityType.PRIMED_TNT) {
                    tntCount++;
                }
            }
            if (tntCount > maxTNTPerChunk) {
                event.setCancelled(true);
            } else {
                if (blockList != null) {
                    blockList.explode(explodingEntity, event.blockList());
                }
            }
        }
        else if (blockList != null) {
            blockList.explode(explodingEntity, event.blockList());
        }
    }
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityFinalizeExplode(EntityExplodeEvent event) {
		Entity explodingEntity = event.getEntity();
		if (explodingEntity == null) return;

		UndoList blockList = getEntityUndo(explodingEntity);
        if (blockList == null) return;

		if (event.isCancelled()) {
			blockList.cancelExplosion(explodingEntity);
		} else {
            blockList.finalizeExplosion(explodingEntity, event.blockList());
		}
	}
	
	protected void onPlayerActivateIcon(Mage mage, Wand activeWand, ItemStack icon)
	{
		// Check for spell or material selection
		if (icon != null && icon.getType() != Material.AIR) {
			com.elmakers.mine.bukkit.api.spell.Spell spell = mage.getSpell(Wand.getSpell(icon));
			if (spell != null) {
				activeWand.setActiveSpell(spell.getKey());
            } else if (Wand.isBrush(icon)){
				activeWand.setActiveBrush(icon);
			}
		} else {
			activeWand.setActiveSpell("");
		}
        mage.getPlayer().updateInventory();
	}
	
	@EventHandler
	public void onPlayerEquip(PlayerItemHeldEvent event)
	{
		Player player = event.getPlayer();
		PlayerInventory inventory = player.getInventory();
		ItemStack next = inventory.getItem(event.getNewSlot());
		ItemStack previous = inventory.getItem(event.getPreviousSlot());

		Mage apiMage = getMage(player);
        if (!(apiMage instanceof com.elmakers.mine.bukkit.magic.Mage)) return;
        com.elmakers.mine.bukkit.magic.Mage mage = (com.elmakers.mine.bukkit.magic.Mage)apiMage;

		Wand activeWand = mage.getActiveWand();
		
		// Check for active Wand
		if (activeWand != null && Wand.isWand(previous)) {
			// If the wand inventory is open, we're going to let them select a spell or material
			if (activeWand.isInventoryOpen()) {
				// Check for spell or material selection
                if (!Wand.isWand(next)) {
                    onPlayerActivateIcon(mage, activeWand, next);
                }
				
				event.setCancelled(true);
				return;
			} else {
				// Otherwise, we're switching away from the wand, so deactivate it.
				activeWand.deactivate();
			}
		}
		
		// If we're switching to a wand, activate it.
		if (next != null && Wand.isWand(next)) {
			Wand newWand = new Wand(this, next);
			newWand.activate(mage, next, event.getNewSlot());
		}
		
		// Check for map selection if no wand is active
		activeWand = mage.getActiveWand();
		if (activeWand == null && next != null) {
			if (next.getType() == Material.MAP) {
				mage.setLastHeldMapId(next.getDurability());
			}
		}
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event)
	{
		final Player player = event.getPlayer();
        Mage apiMage = getMage(player);
        if (!(apiMage instanceof com.elmakers.mine.bukkit.magic.Mage)) return;
        com.elmakers.mine.bukkit.magic.Mage mage = (com.elmakers.mine.bukkit.magic.Mage)apiMage;

        // Catch lag-related glitches dropping items from GUIs
        if (mage.getActiveGUI() != null) {
            event.setCancelled(true);
            return;
        }

        final Wand activeWand = mage.getActiveWand();
        ItemStack droppedItem = event.getItemDrop().getItemStack();

        boolean cancelEvent = false;
        if (Wand.isWand(droppedItem) && activeWand != null && activeWand.isUndroppable()) {
            // Postpone cycling until after this event unwinds
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    if (activeWand.getHotbars().size() > 1) {
                        activeWand.cycleHotbar(1);
                    } else {
                        activeWand.closeInventory();
                    }
               }
            });
            cancelEvent = true;
        } else if (activeWand != null) {
			ItemStack inHand = event.getPlayer().getInventory().getItemInHand();
			// Kind of a hack- check if we just dropped a wand, and now have an empty hand
			if (Wand.isWand(droppedItem) && (inHand == null || inHand.getType() == Material.AIR)) {
				activeWand.deactivate();
				// Clear after inventory restore (potentially with deactivate), since that will put the wand back
				if (Wand.hasActiveWand(player)) {
					player.setItemInHand(new ItemStack(Material.AIR, 1));
				}
			} else if (activeWand.isInventoryOpen()) {
                if (!spellDroppingEnabled) {
                    cancelEvent = true;
                } else {
                    // The item is already removed from the wand's inventory, but that should be ok
                    removeItemFromWand(activeWand, droppedItem);
                }
			}
		}

        if (cancelEvent) {
            event.setCancelled(true);
        }
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityDeath(EntityDeathEvent event)
	{
        Entity entity = event.getEntity();
        if (entity.hasMetadata("nodrops")) {
            event.setDroppedExp(0);
            event.getDrops().clear();
        }

        if (!isMage(entity)) return;

        Mage apiMage = getMage(entity);
        if (!(apiMage instanceof com.elmakers.mine.bukkit.magic.Mage)) return;
        com.elmakers.mine.bukkit.magic.Mage mage = (com.elmakers.mine.bukkit.magic.Mage)apiMage;

        mage.onPlayerDeath(event);

        if (!(entity instanceof Player)) {
            return;
        }
        final Player player = (Player)entity;

		String rule = entity.getWorld().getGameRuleValue("keepInventory");
		if (rule.equals("true")) return;

        List<ItemStack> drops = event.getDrops();
		Wand wand = mage.getActiveWand();
		if (wand != null) {
			// Retrieve stored inventory before deactivating the wand
			if (mage.hasStoredInventory()) {
				drops.clear();

				ItemStack[] stored = mage.getStoredInventory().getContents();
				
				// Deactivate the wand.
				wand.deactivate();
	
				for (ItemStack stack : stored) {
					if (stack != null) {
						drops.add(stack);
					}
				}
				
				// Drop armor also
				ItemStack[] armor = player.getInventory().getArmorContents();
				for (ItemStack stack : armor) {
					if (stack != null) {
						drops.add(stack);
					}
				}
			} else {
				wand.deactivate();
			}
		}
		
		List<ItemStack> oldDrops = new ArrayList<ItemStack>(drops);
		final List<ItemStack> keepWands = new ArrayList<ItemStack>();
		drops.clear();
        PlayerInventory inventory = player.getInventory();
        ItemStack[] contents = inventory.getContents();
		for (int index = 0; index < contents.length; index++)
		{
            ItemStack itemStack = contents[index];
			boolean keepItem = false;
			if (Wand.isWand(itemStack)) {
				keepItem = keepWandsOnDeath;	
				if (!keepItem) {
					Wand testWand = new Wand(this, itemStack);
					keepItem = testWand.keepOnDeath();
				}
			}
			if (keepItem)
			{
				mage.addToRespawnInventory(index, itemStack);
			}
			else
			{
				drops.add(itemStack);
			}
		}
	}

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Mage apiMage = getMage(event.getPlayer());
        if (!(apiMage instanceof com.elmakers.mine.bukkit.magic.Mage)) return;
        com.elmakers.mine.bukkit.magic.Mage mage = (com.elmakers.mine.bukkit.magic.Mage)apiMage;
        mage.restoreRespawnInventories();
    }
	
	@EventHandler
	public void onItemDespawn(ItemDespawnEvent event)
	{
        Item entity = event.getEntity();
        ActionHandler.runEffects(entity);
        ActionHandler.runActions(entity, entity.getLocation(), null);
		if (Wand.isWand(event.getEntity().getItemStack()))
		{
			Wand wand = new Wand(this, entity.getItemStack());
			if (wand.isIndestructible()) {
				event.getEntity().setTicksLived(1);
				event.setCancelled(true);
			} else if (dynmapShowWands) {
				removeLostWand(wand.getLostId());
			}
		}
	}
	
	@EventHandler
	public void onItemSpawn(ItemSpawnEvent event)
	{
		if (Wand.isWand(event.getEntity().getItemStack()))
		{
			Wand wand = new Wand(this, event.getEntity().getItemStack());
			if (wand.isIndestructible()) {
				CompatibilityUtils.setInvulnerable(event.getEntity());

				// Don't show non-indestructible wands on dynmap
				addLostWand(wand, event.getEntity().getLocation());		
				Location dropLocation = event.getLocation();
				getLogger().info("Wand " + wand.getName() + ", id " + wand.getLostId() + " spawned at " + dropLocation.getBlockX() + " " + dropLocation.getBlockY() + " " + dropLocation.getBlockZ());
			}
		} else  {
			registerEntityForUndo(event.getEntity());
			if (ageDroppedItems > 0) {
				int ticks = ageDroppedItems * 20 / 1000;
				Item item = event.getEntity();
				CompatibilityUtils.ageItem(item, ticks);
			}
		}
	}

	protected void registerEntityForUndo(Entity entity) {
		long now = System.currentTimeMillis();
		Collection<String> keys = new ArrayList<String>(pendingUndo);
		
		for (String key : keys) {
			if (mages.containsKey(key)) {
				Mage apiMage = mages.get(key);

                if (!(apiMage instanceof com.elmakers.mine.bukkit.magic.Mage)) continue;
                com.elmakers.mine.bukkit.magic.Mage mage = (com.elmakers.mine.bukkit.magic.Mage)apiMage;

                UndoList lastUndo = mage.getLastUndoList();
				if (lastUndo == null || lastUndo.getModifiedTime() < now - undoTimeWindow) {
					pendingUndo.remove(key);
				} else if (lastUndo.contains(entity.getLocation(), undoBlockBorderSize)) {
					lastUndo.add(entity);
                    break;
				}
			} else {
				pendingUndo.remove(key);
			}
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event)
	{
		try {
			Entity entity = event.getEntity();

			if (isMage(entity))
			{
                Mage apiMage = getMage(event.getEntity());
                if (!(apiMage instanceof com.elmakers.mine.bukkit.magic.Mage)) return;
                com.elmakers.mine.bukkit.magic.Mage mage = (com.elmakers.mine.bukkit.magic.Mage) apiMage;

                mage.onPlayerDamage(event);
			}
	        if (entity instanceof Item)
	        {
	   		 	Item item = (Item)entity;
	   		 	ItemStack itemStack = item.getItemStack();
	            if (Wand.isWand(itemStack))
	            {
                	Wand wand = new Wand(this, item.getItemStack());
	            	if (wand.isIndestructible()) {
	                     event.setCancelled(true);
	            	} else if (event.getDamage() >= itemStack.getDurability()) {
	                	if (removeLostWand(wand.getLostId())) {
	                		plugin.getLogger().info("Wand " + wand.getName() + ", id " + wand.getLostId() + " destroyed");
	                	}
	                }
				}  
	        }
		} catch (Exception ex) {
			// TODO: Trying to track down a stacktrace-less NPE that seemed to come from here:
			// [06:22:34] [Server thread/ERROR]: Could not pass event EntityDamageEvent to Magic v2.9.0
			// Caused by: java.lang.NullPointerException
			ex.printStackTrace();
		}
	}
	
	@EventHandler(priority=EventPriority.LOW)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.isCancelled())
            return;

        Player player = event.getPlayer();

        Mage apiMage = getMage(player);
        if (!(apiMage instanceof com.elmakers.mine.bukkit.magic.Mage)) return;
        com.elmakers.mine.bukkit.magic.Mage mage = (com.elmakers.mine.bukkit.magic.Mage)apiMage;
        Wand wand = mage.getActiveWand();

        // Check for a player placing a wand in an item frame
        if (wand != null && event.getRightClicked() instanceof ItemFrame) {
            if (wand.isUndroppable()) {
                event.setCancelled(true);
                return;
            } else {
                wand.deactivate();
            }
        }

        // Check for clicking on a Citizens NPC, in case
        // this hasn't been cancelled yet
        if (isNPC(event.getRightClicked())) {
        	if (wand != null) {
        		wand.closeInventory();
        	}
        	
        	// Don't let it re-open right away
        	mage.checkLastClick(0);
        } else {
            // Don't allow interacting while holding spells, brushes or upgrades
            ItemStack itemInHand = player.getItemInHand();
            if (Wand.isSpell(itemInHand) || Wand.isBrush(itemInHand) || Wand.isUpgrade(itemInHand)) {
                event.setCancelled(true);
                return;
            }
        }
    }

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		// Note that an interact on air event will arrive pre-cancelled
		// So this is kind of useless. :\
		//if (event.isCancelled()) return;
		
		// Block block = event.getClickedBlock();
		// getLogger().info("INTERACT: " + event.getAction() + " on " + (block == null ? "NOTHING" : block.getType()));
		
		Player player = event.getPlayer();

        // Don't allow interacting while holding spells, brushes or upgrades
        ItemStack itemInHand = player.getItemInHand();
        if (Wand.isSpell(itemInHand) || Wand.isBrush(itemInHand) || Wand.isUpgrade(itemInHand)) {
            event.setCancelled(true);
            return;
        }

        Mage apiMage = getMage(player);
        if (!(apiMage instanceof com.elmakers.mine.bukkit.magic.Mage)) return;
        com.elmakers.mine.bukkit.magic.Mage mage = (com.elmakers.mine.bukkit.magic.Mage)apiMage;

        if (!mage.checkLastClick(clickCooldown)) {
			return;
		}

		Wand wand = mage.getActiveWand();
        boolean hasWand = Wand.hasActiveWand(player);

        // Safety check for a wand getting removed from the player's inventory
        if ((itemInHand == null || itemInHand.getType() == Material.AIR) && wand != null)
        {
            getLogger().warning("Mage had an active wand, but player is not holding anything");
            wand.deactivate();
            return;
        }


        // Hacky check for immediately activating a wand if for some reason it was
		// not active
		if (wand == null && hasWand) {
			wand = Wand.getActiveWand(this, player);
			wand.activate(mage);
			getLogger().warning("Player was holding an inactive wand on interact- activating.");
		}

		if (wand == null) return;

		if (!hasWandPermission(player))
		{
			// Check for self-destruct
			if (hasPermission(player, "Magic.wand.destruct", false)) {
				wand.deactivate();
				PlayerInventory inventory = player.getInventory();
				ItemStack[] items = inventory.getContents();
				for (int i = 0; i < items.length; i++) {
					ItemStack item = items[i];
					if (Wand.isWand(item) || Wand.isSpell(item) || Wand.isBrush(item) || Wand.isUpgrade(item)) {
						items[i] = null;
					}
				}
				inventory.setContents(items);
				mage.sendMessage(messages.get("wand.self_destruct"));
			}
			return;
		}

		if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK && !wand.isUpgrade())
		{
			wand.cast();
			event.setCancelled(true);
			return;
		}

		boolean toggleInventory = (event.getAction() == Action.RIGHT_CLICK_AIR);
		if (!toggleInventory && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Material material = event.getClickedBlock().getType();
			toggleInventory = !interactibleMaterials.contains(material);

			// This is to prevent Essentials signs from giving you an item in your wand inventory.
			if (material== Material.SIGN_POST || material == Material.WALL_SIGN) {
				wand.closeInventory();
			}
		}
		if (toggleInventory)
		{
			// Check for spell cancel first, e.g. fill or force
			if (!mage.cancel()) {

				// Check for wand cycling
                WandMode wandMode = wand.getMode();
				if (wandMode == WandMode.CYCLE) {
					if (player.isSneaking()) {
						com.elmakers.mine.bukkit.api.spell.Spell activeSpell = wand.getActiveSpell();
						boolean cycleMaterials = false;
						if (activeSpell != null) {
							cycleMaterials = activeSpell.hasBrushOverride() && wand.getBrushes().size() > 0;
						}
						if (cycleMaterials) {
							wand.cycleMaterials();
						} else {
							wand.cycleSpells();
						}
					} else {
						wand.cycleSpells();
					}
				} else if (wandMode == WandMode.CAST) {
                    wand.cast();
				} else {
                    Spell currentSpell = wand.getActiveSpell();
                    if (wand.getBrushMode() == WandMode.CHEST && brushSelectSpell != null && !brushSelectSpell.isEmpty() && player.isSneaking() && currentSpell != null && currentSpell.usesBrushSelection())
                    {
                        Spell brushSelect = mage.getSpell(brushSelectSpell);
                        if (brushSelect == null)
                        {
                            wand.toggleInventory();
                        }
                        else
                        {
                            brushSelect.cast();
                        }
                    }
                    else
                    {
                        wand.toggleInventory();
                    }
                }
				event.setCancelled(true);
			} else {
				mage.playSound(Sound.NOTE_BASS, 1.0f, 0.7f);
			}
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		// Automatically re-activate mages.
        getMage(event.getPlayer());
	}

	@Override
	public void giveItemToPlayer(Player player, ItemStack itemStack) {
        // Check for wand inventory
        Mage apiMage = getMage(player);
        if (!(apiMage instanceof com.elmakers.mine.bukkit.magic.Mage)) return;
        com.elmakers.mine.bukkit.magic.Mage mage = (com.elmakers.mine.bukkit.magic.Mage)apiMage;

        if (mage.hasStoredInventory()) {
            mage.addToStoredInventory(itemStack);
            return;
        }
		// Place directly in hand if possible
		PlayerInventory inventory = player.getInventory();
		ItemStack inHand = inventory.getItemInHand();
		if (inHand == null || inHand.getType() == Material.AIR) {
			inventory.setItem(inventory.getHeldItemSlot(), itemStack);
			if (Wand.isWand(itemStack)) {
				Wand wand = new Wand(this, itemStack);
				wand.activate(this.getMage((CommandSender)player));
			}
		} else {
			HashMap<Integer, ItemStack> returned = player.getInventory().addItem(itemStack);
			if (returned.size() > 0) {
				player.getWorld().dropItem(player.getLocation(), itemStack);
			}
		}
	}

	@EventHandler
	public void onPlayerExpChange(PlayerExpChangeEvent event)
	{
		// We don't care about exp loss events
		if (event.getAmount() <= 0) return;

		Player player = event.getPlayer();
        Mage apiMage = getMage(player);

        if (!(apiMage instanceof com.elmakers.mine.bukkit.magic.Mage)) return;
        com.elmakers.mine.bukkit.magic.Mage mage = (com.elmakers.mine.bukkit.magic.Mage)apiMage;

        Wand wand = mage.getActiveWand();
		if (wand != null) {
			wand.onPlayerExpChange(event);
		}
	}

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event)
    {
        handlePlayerQuitEvent(event);
    }

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
    {
        handlePlayerQuitEvent(event);
    }

    protected void handlePlayerQuitEvent(PlayerEvent event) {
        Player player = event.getPlayer();
        if (isMage(player)) {
            Mage mage = getMage(player);
            if (mage instanceof com.elmakers.mine.bukkit.magic.Mage)
            {
                ((com.elmakers.mine.bukkit.magic.Mage)mage).onPlayerQuit(event);
            }
            playerQuit(mage);
        }
    }

    protected void playerQuit(Mage mage) {

		// Make sure they get their portraits re-rendered on relogin.
        maps.resend(mage.getName());

		com.elmakers.mine.bukkit.api.block.UndoQueue undoQueue = mage.getUndoQueue();
        if (undoQueue != null) {
            int undid = undoQueue.undoScheduled();
            if (undid != 0) {
                getLogger().info("Player " + mage.getName() + " logged out, auto-undid " + undid + " spells");
            }

            if (!undoQueue.isEmpty()) {
                if (commitOnQuit) {
                    getLogger().info("Player logged out, committing constructions: " + mage.getName());
                    undoQueue.commit();
                } else {
                    getLogger().info("Player " + mage.getName() + " logged out with " + undoQueue.getSize() + " spells in their undo queue");
                }
            }
        }

        if (!mage.isLoading() && (mage.isPlayer() || saveNonPlayerMages))
        {
            final File playerData = new File(playerDataFolder, mage.getId() + ".dat");
            getLogger().info("Player logged out, saving data to " + playerData.getName());
            final DataStore playerConfig = new DataStore(getLogger(), playerData);
            mage.save(playerConfig);

            // Save synchronously on shutdown
            if (initialized)
            {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                    @Override
                    public void run() {
                        synchronized (saveLock) {
                            try {
                                playerConfig.save();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                });
            }
            else
            {
                synchronized (saveLock) {
                    try {
                        playerConfig.save();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }

		// Close the wand inventory to make sure the player's normal inventory gets saved
		com.elmakers.mine.bukkit.api.wand.Wand wand = mage.getActiveWand();
		if (wand != null) {
			wand.deactivate();
		}
        mage.deactivateAllSpells(true, true);

		// Let the GC collect the mage
        mages.remove(mage.getId());
	}

	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent event) {
		Player player = (Player)event.getPlayer();
        Mage apiMage = getMage(player);

        if (!(apiMage instanceof com.elmakers.mine.bukkit.magic.Mage)) return;
        com.elmakers.mine.bukkit.magic.Mage mage = (com.elmakers.mine.bukkit.magic.Mage)apiMage;

        Wand wand = mage.getActiveWand();
        GUIAction gui = mage.getActiveGUI();
		if (wand != null && gui == null) {
			// NOTE: The type will never actually be CRAFTING, at least for now.
			// But we can hope for server-side player inventory open notification one day, right?
			// Anyway, check for opening another inventory and close the wand.
			if (event.getView().getType() != InventoryType.CRAFTING) {
				if (wand.getMode() == WandMode.INVENTORY || !wand.isInventoryOpen()) {
					wand.deactivate();
				}
			}
		}
	}

	protected ItemStack removeItemFromWand(Wand wand, ItemStack droppedItem) {
		if (wand == null || droppedItem == null || Wand.isWand(droppedItem)) {
			return null;
		}

		if (Wand.isSpell(droppedItem)) {
			String spellKey = Wand.getSpell(droppedItem);
			wand.removeSpell(spellKey);

			// Update the item for proper naming and lore
			SpellTemplate spell = getSpellTemplate(spellKey);
			if (spell != null) {
				Wand.updateSpellItem(messages, droppedItem, spell, null, null, true);
			}
		} else if (Wand.isBrush(droppedItem)) {
			String brushKey = Wand.getBrush(droppedItem);
			wand.removeBrush(brushKey);

			// Update the item for proper naming and lore
			Wand.updateBrushItem(getMessages(), droppedItem, brushKey, null);
		}
		return droppedItem;
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		// getLogger().info("CLICK: " + event.getAction() + ", " + event.getClick() + " on " + event.getSlotType() + " in "+ event.getInventory().getType() + " slots: " + event.getSlot() + ":" + event.getRawSlot());

		if (event.isCancelled()) return;
		if (!(event.getWhoClicked() instanceof Player)) return;

		Player player = (Player)event.getWhoClicked();
        Mage apiMage = getMage(player);

        if (!(apiMage instanceof com.elmakers.mine.bukkit.magic.Mage)) return;
        com.elmakers.mine.bukkit.magic.Mage mage = (com.elmakers.mine.bukkit.magic.Mage)apiMage;

        GUIAction gui = mage.getActiveGUI();
        if (gui != null)
        {
            gui.clicked(event);
            return;
        }

        // Check for temporary items
		ItemStack clickedItem = event.getCurrentItem();
		
		if (clickedItem != null && NMSUtils.isTemporary(clickedItem)) {
			String message = NMSUtils.getTemporaryMessage(clickedItem);
			if (message != null && message.length() > 1) {
				mage.sendMessage(message);
			}
            ItemStack replacement = NMSUtils.getReplacement(clickedItem);
            event.setCurrentItem(replacement);
			event.setCancelled(true);
			return;
		}

        // Check for wearing spells
        ItemStack heldItem = event.getCursor();
        if (heldItem != null && event.getSlotType() == SlotType.ARMOR && (Wand.isSpell(heldItem) || Wand.isWand(heldItem)))
        {
            event.setCancelled(true);
            return;
        }
        boolean isHotbar = event.getAction() == InventoryAction.HOTBAR_SWAP || event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD;
        if (event.getAction() == InventoryAction.HOTBAR_SWAP && event.getSlotType() == SlotType.ARMOR)
        {
            int slot = event.getHotbarButton();
            ItemStack item =  mage.getPlayer().getInventory().getItem(slot);
            if (item != null && (Wand.isSpell(item) || Wand.isWand(item)))
            {
                event.setCancelled(true);
                return;
            }
        }

		Wand activeWand = mage.getActiveWand();
		InventoryType inventoryType = event.getInventory().getType();

        boolean clickedWand = Wand.isWand(clickedItem);
        if (activeWand != null && activeWand.isInventoryOpen())
        {
            if (Wand.isSpell(clickedItem) && clickedItem.getAmount() != 1)
            {
                clickedItem.setAmount(1);
            }
            if (clickedWand)
            {
                event.setCancelled(true);
                activeWand.cycleHotbar(1);
                return;
            }

            // So many ways to try and move the wand around, that we have to watch for!
            if (isHotbar && Wand.isWand(player.getInventory().getItem(event.getHotbarButton())))
            {
                event.setCancelled(true);
                return;
            }

            // Can't wear spells
            if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY && clickedItem != null)
            {

                Material itemType = clickedItem.getType();
                if (wearableMaterials.contains(itemType))
                {
                    event.setCancelled(true);
                    return;
                }
            }

            // Safety check for something that ought not to be possible
            // but perhaps happens with lag?
            if (Wand.isWand(event.getCursor()))
            {
                activeWand.closeInventory();
                event.setCursor(null);
                event.setCancelled(true);
                return;
            }
        } else if (activeWand != null) {
            // Check for changes that could have been made to the active wand
            Integer activeSlot = activeWand.getPlayerInventorySlot();
            if (activeSlot != null
                && event.getSlot() == activeSlot
                || (event.getAction() == InventoryAction.HOTBAR_SWAP && event.getHotbarButton() == activeSlot)
            )
            {
                activeWand.deactivate();
                activeWand = null;
            }
        }

		// Check for dropping items out of a wand's inventory
        // or dropping undroppable wands
		if (event.getAction() == InventoryAction.DROP_ONE_SLOT) {
            if (clickedWand) {
                Wand wand = new Wand(this, clickedItem);
                if (wand.isUndroppable()) {
                    event.setCancelled(true);
                    if (activeWand.getHotbars().size() > 1) {
                        activeWand.cycleHotbar(1);
                    } else {
                        activeWand.closeInventory();
                    }
                    return;
                }
            }
            if (activeWand != null && activeWand.isInventoryOpen()) {
                if (!spellDroppingEnabled) {
                    event.setCancelled(true);
                    return;
                }
                ItemStack droppedItem = clickedItem;
                ItemStack newDrop = removeItemFromWand(activeWand, droppedItem);

                if (newDrop != null) {
                    Location location = player.getLocation();
                    Item item = location.getWorld().dropItem(location, newDrop);
                    item.setVelocity(location.getDirection().normalize());
                } else {
                    event.setCancelled(true);
                }
            }
			return;
		}
		
		// Check for wand cycling with active inventory
    	if (activeWand != null) {
			WandMode wandMode = activeWand.getMode();
			if ((wandMode == WandMode.INVENTORY && inventoryType == InventoryType.CRAFTING) || 
			    (wandMode == WandMode.CHEST && inventoryType == InventoryType.CHEST)) {
				if (activeWand != null && activeWand.isInventoryOpen()) {
					if (event.getAction() == InventoryAction.NOTHING) {
						int direction = event.getClick() == ClickType.LEFT ? 1 : -1;
                        activeWand.cycleInventory(direction);
						event.setCancelled(true);
						return;
					}

					if (event.getSlotType() == SlotType.ARMOR) {
						event.setCancelled(true);
						return;
					}
					
					// Chest mode falls back to selection from here.
					if (event.getAction() == InventoryAction.PICKUP_HALF || wandMode == WandMode.CHEST) {
						onPlayerActivateIcon(mage, activeWand, clickedItem);
						player.closeInventory();
						event.setCancelled(true);
						return;
					}
				}
			}
			
			return;
		}
	}

	@EventHandler
	public void onInventoryClosed(InventoryCloseEvent event) {
		if (!(event.getPlayer() instanceof Player)) return;

		// Update the active wand, it may have changed around
		Player player = (Player)event.getPlayer();
        Mage apiMage = getMage(player);

        if (!(apiMage instanceof com.elmakers.mine.bukkit.magic.Mage)) return;
        com.elmakers.mine.bukkit.magic.Mage mage = (com.elmakers.mine.bukkit.magic.Mage)apiMage;

        GUIAction gui = mage.getActiveGUI();
        if (gui != null)
        {
            mage.deactivateGUI();
        }

        Wand previousWand = mage.getActiveWand();

		// Save the inventory state of the current wand if its spell inventory is open
		// This is just to make sure we don't lose changes made to the inventory
		if (previousWand != null && previousWand.isInventoryOpen()) {
			if (previousWand.getMode() == WandMode.INVENTORY) {
				previousWand.saveInventory();
                // Update hotbar names
                previousWand.updateHotbar();
			} else if (previousWand.getMode() == WandMode.CHEST) {
				// Check for chest inventory mode, we may just be closing a display inventory.
				// In theory you can't re-arrange items in here.
                previousWand.closeInventory();
			}
		} else {
            if (previousWand != null) {
                if (player.getInventory().getHeldItemSlot() != previousWand.getPlayerInventorySlot()) {
                    previousWand.deactivate();
                    previousWand = null;
                }
            }
            ItemStack currentItem = player.getItemInHand();

            // If we're not in a wand inventory, check for the player
            // having re-arranged their items such that a new wand is now active
            // we don't get an equip event for this.
            // Note that ".equals" is very strong and will detect any changes at all
            // in the wand item, including an active spell change.
            boolean changedWands = false;
            boolean itemIsWand = Wand.isWand(currentItem);
            if (previousWand != null && !itemIsWand) changedWands = true;
            if (previousWand == null && itemIsWand) changedWands = true;
            if (previousWand != null && itemIsWand && !previousWand.getItem().equals(currentItem)) changedWands = true;

            if (changedWands) {
                if (previousWand != null) {
                    previousWand.deactivate();
                }
                if (itemIsWand) {
                    Wand newWand = new Wand(this, currentItem);
                    newWand.activate(mage);
                }
            }
        }
	}
	
	@EventHandler
	public void onPlayerGameModeChange(PlayerGameModeChangeEvent event)
	{
		if (event.getNewGameMode() == GameMode.CREATIVE && enableCreativeModeEjecting) {
			boolean ejected = false;
			Player player = event.getPlayer();
            Mage apiMage = getMage(player);

            if (!(apiMage instanceof com.elmakers.mine.bukkit.magic.Mage)) return;
            com.elmakers.mine.bukkit.magic.Mage mage = (com.elmakers.mine.bukkit.magic.Mage)apiMage;

            Wand activeWand = mage.getActiveWand();
			if (activeWand != null) {
				activeWand.deactivate();
			}
			Inventory inventory = player.getInventory();
			ItemStack[] contents = inventory.getContents();
			for (int i = 0; i < contents.length; i++) {
				ItemStack item = contents[i];
				if (Wand.isWand(item)) {
					ejected = true;
					inventory.setItem(i, null);
					player.getWorld().dropItemNaturally(player.getLocation(), item);
				}
			}
			if (ejected) {
				mage.sendMessage("Ejecting wands, creative mode will destroy them!");
			}
		}
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerPickupItem(PlayerPickupItemEvent event)
	{
		if (event.isCancelled()) return;

        Player player = event.getPlayer();
        Mage apiMage = getMage(player);

        if (!(apiMage instanceof com.elmakers.mine.bukkit.magic.Mage)) return;
        com.elmakers.mine.bukkit.magic.Mage mage = (com.elmakers.mine.bukkit.magic.Mage)apiMage;

        Item item = event.getItem();
        ItemStack pickup = item.getItemStack();
        if (NMSUtils.isTemporary(pickup) || item.hasMetadata("temporary"))
        {
            item.remove();
            event.setCancelled(true);
            return;
        }

		boolean isWand = Wand.isWand(pickup);

		// Creative mode inventory hacky work-around :\
		if (event.getPlayer().getGameMode() == GameMode.CREATIVE && isWand && enableCreativeModeEjecting) {
			event.setCancelled(true);
			return;
		}

        // Remove lost wands from records
        if (isWand) {
            Wand wand = new Wand(this, pickup);
            if (!wand.canUse(player)) {
                mage.sendMessage(messages.get("wand.bound").replace("$name", wand.getOwner()));
                event.setCancelled(true);
                Item droppedItem = event.getItem();
                org.bukkit.util.Vector velocity = droppedItem.getVelocity();
                velocity.setY(velocity.getY() * 2 + 1);
                droppedItem.setVelocity(velocity);
                return;
            }

            if (removeLostWand(wand.getLostId())) {
                plugin.getLogger().info("Player " + mage.getName() + " picked up wand " + wand.getName() + ", id " + wand.getLostId());
            }
            wand.clearLostId();
        }

        // Wands will absorb spells and upgrade items
		Wand activeWand = mage.getActiveWand();
		if (activeWand != null
            && activeWand.isModifiable()
            && (Wand.isSpell(pickup) || Wand.isBrush(pickup) || Wand.isUpgrade(pickup))
			&& activeWand.addItem(pickup)) {
			event.getItem().remove();
			event.setCancelled(true);   
			return;
		}

        // If a wand's inventory is active, add the item there
		if (mage.hasStoredInventory()) {
			event.setCancelled(true);   		
			if (mage.addToStoredInventory(event.getItem().getItemStack())) {
				event.getItem().remove();
			}
		} else {
			// Hackiness needed because we don't get an equip event for this!
			PlayerInventory inventory = event.getPlayer().getInventory();
			ItemStack inHand = inventory.getItemInHand();
			if (isWand && (inHand == null || inHand.getType() == Material.AIR)) {
				Wand wand = new Wand(this, pickup);
				event.setCancelled(true);
				event.getItem().remove();
				inventory.setItem(inventory.getHeldItemSlot(), pickup);
				wand.activate(mage);
			} 
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event)
	{
		Player player = event.getPlayer();
        Mage apiMage = getMage(player);

        if (!(apiMage instanceof com.elmakers.mine.bukkit.magic.Mage)) return;
        com.elmakers.mine.bukkit.magic.Mage mage = (com.elmakers.mine.bukkit.magic.Mage)apiMage;

        if (mage.hasStoredInventory() || mage.getBlockPlaceTimeout() > System.currentTimeMillis()) {
			event.setCancelled(true);
		}
		
		ItemStack itemStack = event.getItemInHand();
		if (Wand.isWand(itemStack) || Wand.isBrush(itemStack) || Wand.isSpell(itemStack) || Wand.isUpgrade(itemStack)) {
			event.setCancelled(true);
		}
	}
	
	protected boolean addLostWandMarker(LostWand lostWand) {
		Location location = lostWand.getLocation();
		if (!lostWand.isIndestructible()) {
			return true;
		}
		return addMarker("wand-" + lostWand.getId(), "Wands", lostWand.getName(), location.getWorld().getName(),
			location.getBlockX(), location.getBlockY(), location.getBlockZ(), lostWand.getDescription()
		);
	}

	@EventHandler
	public void onChunkLoad(ChunkLoadEvent e) {
		// Check for any blocks we need to toggle.
		triggerBlockToggle(e.getChunk());
	}
	
	public void toggleCastCommandOverrides(Mage apiMage, boolean override) {
		// Reach into internals a bit here.
		if (apiMage instanceof com.elmakers.mine.bukkit.magic.Mage) {
            com.elmakers.mine.bukkit.magic.Mage mage = (com.elmakers.mine.bukkit.magic.Mage)apiMage;
			mage.setCostReduction(override ? castCommandCostReduction : 0);
			mage.setCooldownReduction(override ? castCommandCooldownReduction : 0);
			mage.setPowerMultiplier(override ? castCommandPowerMultiplier : 1);	
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
	
	public Collection<com.elmakers.mine.bukkit.api.wand.LostWand> getLostWands() {
		return new ArrayList<com.elmakers.mine.bukkit.api.wand.LostWand>(lostWands.values());
	}
	
	public Collection<Automaton> getAutomata() {
		Collection<Automaton> all = new ArrayList<Automaton>();
		for (Map<Long, Automaton> chunkList : automata.values()) {
			all.addAll(chunkList.values());
		}
		return all;
	}
	
	public boolean cast(Mage mage, String spellName, String[] parameters, CommandSender sender, Entity entity)
	{
		Player usePermissions = (sender == entity && entity instanceof Player) ? (Player)entity
                : (sender instanceof Player ? (Player)sender : null);
        if (entity == null && sender instanceof Player) {
            entity = (Player)sender;
        }
		Location targetLocation = null;
		if (mage == null) {
			CommandSender mageController = (entity != null && entity instanceof Player) ? (Player)entity : sender;
			if (sender != null) {
                if (sender instanceof BlockCommandSender) {
                    targetLocation = ((BlockCommandSender) sender).getBlock().getLocation();
                } else if (entity != null && sender != entity) {
                    targetLocation = entity.getLocation();
                }
            }
            if (mageController == null) {
                mage = getMage(entity);
            } else {
                mage = getMage(mageController);
            }
		}

        // This is a bit of a hack to make automata maintain direction
        if (mage != null && targetLocation != null) {
            Location mageLocation = mage.getLocation();
            targetLocation.setPitch(mageLocation.getPitch());
            targetLocation.setYaw(mageLocation.getYaw());
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

		// Make it free and skip cooldowns, if configured to do so.
		toggleCastCommandOverrides(mage, true);
        try {
            spell.cast(parameters, targetLocation);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
		toggleCastCommandOverrides(mage, false);
		if (sender != entity && sender != null) {
			String castMessage = "Cast " + spellName;
			if (entity != null) {
				castMessage += " on " + getEntityDisplayName(entity);
			}
			sender.sendMessage(castMessage);
		}

		return true;
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

    public MapController getMaps() {
        return maps;
    }

    public String getWelcomeWand() {
        return welcomeWand;
    }
	
	protected void triggerBlockToggle(final Chunk chunk) {
		String chunkKey = getChunkKey(chunk);
		Map<Long, Automaton> chunkData = automata.get(chunkKey);
		if (chunkData != null) {
			final List<Automaton> restored = new ArrayList<Automaton>();
			Collection<Long> blockKeys = new ArrayList<Long>(chunkData.keySet());
			long timeThreshold = System.currentTimeMillis() - toggleCooldown;
			for (Long blockKey : blockKeys) {
				Automaton toggleBlock = chunkData.get(blockKey);
				
				// Skip it for now if the chunk was recently loaded
				if (toggleBlock.getCreatedTime() < timeThreshold) {
					Block current = toggleBlock.getBlock();
					// Don't toggle the block if it has changed to something else.
					if (current.getType() == toggleBlock.getMaterial()) {
                        getLogger().info("Resuming block at " + toggleBlock.getPosition() + ": " + toggleBlock.getName() + " with " + toggleBlock.getMaterial());

                        redstoneReplacement.modify(current, true);
						restored.add(toggleBlock);
					}
					
					chunkData.remove(blockKey);
				}
			}
			if (restored.size() > 0) {
                // Hacky double-hit ...
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, 
					new Runnable() {
						public void run() {
							for (Automaton restoreBlock : restored) {
								restoreBlock.restore(true);
							}
						}
				}, 5);
			}
			if (chunkData.size() == 0) {
				automata.remove(chunkKey);
			}
		}
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
	
	public boolean getIndestructibleWands() {
		return indestructibleWands;
	}
	
	public void forgetMage(Mage mage) {
		forgetMages.put(mage.getId(), System.currentTimeMillis());
	}

    public Automaton getAutomaton(Block block) {
        String chunkId = getChunkKey(block.getChunk());
        Map<Long, Automaton> toReload = automata.get(chunkId);
        if (toReload != null) {
            return toReload.get(BlockData.getBlockId(block));
        }
        return null;
    }

    public ItemStack createItem(String key) {
        ConfigurationSection template = Wand.getWandTemplate(key);
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
        List<String> lore = new ArrayList<String>();
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

	/*
	 * API Implementation
	 */
	
	@Override
	public boolean isAutomata(Block block) {
		String chunkId = getChunkKey(block.getChunk());
		Map<Long, Automaton> toReload = automata.get(chunkId);
		if (toReload != null) {
			return toReload.containsKey(BlockData.getBlockId(block));
		}
		return false;
	}

    @Override
    public boolean isNPC(Entity entity) {
        return (entity != null && (entity.hasMetadata("NPC") || entity.hasMetadata("shopkeeper")));
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
			if (blockList.size() > VOLUME_UPDATE_THRESHOLD) {
				update(blockList.getWorldName(), blockList.getArea());
			} else {
				for (com.elmakers.mine.bukkit.api.block.BlockData blockData : blockList) {
					updateBlock(blockData.getWorldName(), blockData.getPosition().getBlockX(), blockData.getPosition().getBlockY(), blockData.getPosition().getBlockZ());
				}
			}
		}
	}
	
	@Override
	public boolean canCreateWorlds()
	{
		return createWorldsEnabled;
	}

	@Override
	public Set<Material> getMaterialSet(String name)
	{
		if (name.contains(",")) {
			return ConfigurationUtils.parseMaterials(name);
		}
		if (!materialSets.containsKey(name)) {
			return ConfigurationUtils.parseMaterials(name);
		}
		return materialSets.get(name);
	}
	
	@Override public int getMaxY() {
		return MAX_Y;
	}
	
	@Override
	public void sendToMages(String message, Location location) {
		sendToMages(message, location, toggleMessageRange);
	}
	
	@Override
	public void registerAutomata(Block block, String name, String message) {
		String chunkId = getChunkKey(block.getChunk());
		Map<Long, Automaton> toReload = automata.get(chunkId);
		if (toReload == null) {
			toReload = new HashMap<Long, Automaton>();
			automata.put(chunkId, toReload);
		}
		Automaton data = new Automaton(block, name, message);
		toReload.put(data.getId(), data);
	}

	@Override
	public boolean unregisterAutomata(Block block) {
		// Note that we currently don't clean up an empty entry,
		// purposefully, to prevent thrashing the main map and adding lots
		// of HashMap creation.
		String chunkId = getChunkKey(block.getChunk());
		Map<Long, Automaton> toReload = automata.get(chunkId);
		if (toReload != null) {
			toReload.remove(BlockData.getBlockId(block));
		}
		
		return toReload != null;
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
	public Collection<Mage> getMages()
	{
		Collection<Mage> mageInterfaces = new ArrayList<Mage>(mages.values());
		return mageInterfaces;
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
        return mages.containsKey(entity.getUniqueId().toString());
    }

    @Override
	public Collection<String> getMaterialSets()
	{
		return materialSets.keySet();
	}
	
	@Override
	public Collection<String> getPlayerNames() 
	{
		List<String> playerNames = new ArrayList<String>();
		List<World> worlds = Bukkit.getWorlds();
		for (World world : worlds) {
			List<Player> players = world.getPlayers();
			for (Player player : players) {
				if (isNPC(player)) continue;
				playerNames.add(player.getName());
			}
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
		return undid;
	}

    @Override
    public boolean isPVPAllowed(Player player, Location location)
    {
        if (bypassPvpPermissions) return true;
        if (player != null && player.hasPermission("Magic.bypass_pvp")) return true;
        if (location == null && player != null) location = player.getLocation();
        return worldGuardManager.isPVPAllowed(player, location)
            && pvpManager.isPVPAllowed(player)
            && multiverseManager.isPVPAllowed(location.getWorld())
            && preciousStonesManager.isPVPAllowed(player, location)
            && townyManager.isPVPAllowed(location);
    }

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
	public com.elmakers.mine.bukkit.api.wand.Wand createWand(String wandKey) 
	{
		return Wand.createWand(this, wandKey);
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
		List<com.elmakers.mine.bukkit.api.spell.SpellCategory> allCategories = new ArrayList<com.elmakers.mine.bukkit.api.spell.SpellCategory>();
		allCategories.addAll(categories.values());
		return allCategories;
	}

	@Override
	public Collection<SpellTemplate> getSpellTemplates()
	{
		List<SpellTemplate> allSpells = new ArrayList<SpellTemplate>();
		allSpells.addAll(spells.values());
		return allSpells;
	}

    @Override
	public SpellTemplate getSpellTemplate(String name) 
	{
		if (name == null || name.length() == 0) return null;
        SpellTemplate spell = spells.get(name);
        if (spell == null) {
            spell = spellAliases.get(name);
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

    public boolean isInventoryBackupEnabled() {
        return backupInventory;
    }

    public ItemStack getSpellBook(com.elmakers.mine.bukkit.api.spell.SpellCategory category, int count) {
        Map<String, List<SpellTemplate>> categories = new HashMap<String, List<SpellTemplate>>();
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
                    categorySpells = new ArrayList<SpellTemplate>();
                    categories.put(spellCategoryKey, categorySpells);
                }
                categorySpells.add(spell);
            }
        }

        List<String> categoryKeys = new ArrayList<String>(categories.keySet());
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
        List<String> pages = new ArrayList<String>();

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
                List<String> lines = new ArrayList<String>();
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

                Collection<CastingCost> costs = spell.getCosts();
                if (costs != null) {
                    for (CastingCost cost : costs) {
                        if (cost.hasCosts(reducer)) {
                            lines.add(ChatColor.DARK_PURPLE + messages.get("wand.costs_description").replace("$description", cost.getFullDescription(messages, reducer)));
                        }
                    }
                }
                Collection<CastingCost> activeCosts = spell.getActiveCosts();
                if (activeCosts != null) {
                    for (CastingCost cost : activeCosts) {
                        if (cost.hasCosts(reducer)) {
                            lines.add(ChatColor.DARK_PURPLE + messages.get("wand.active_costs_description").replace("$description", cost.getFullDescription(messages, reducer)));
                        }
                    }
                }

                for (String pathKey : paths) {
                    WandUpgradePath checkPath = WandUpgradePath.getPath(pathKey);
                    if (!checkPath.isHidden() && checkPath.hasSpell(spell.getKey())) {
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
                else if (spell.isUndoable()) {
                    lines.add(ChatColor.GRAY + messages.get("spell.undoable"));
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

    public MaterialAndData getRedstoneReplacement() {
        return redstoneReplacement;
    }

    public boolean isUrlIconsEnabled() {
        return urlIconsEnabled && NMSUtils.hasURLSkullSupport();
    }

	/*
	 * Private data
	 */

    private final static int                    MAX_Y = 255;
    private static final String                 BUILTIN_SPELL_CLASSPATH = "com.elmakers.mine.bukkit.spell.builtin";
    private static int                          VOLUME_UPDATE_THRESHOLD = 32;
    private static int                          MAGE_FORGET_THRESHOLD = 30000;

    private final String                        SPELLS_FILE                 = "spells";
    private final String                        CONFIG_FILE             	= "config";
    private final String                        WANDS_FILE             		= "wands";
    private final String                        ENCHANTING_FILE             = "enchanting";
    private final String                        CRAFTING_FILE             	= "crafting";
    private final String                        MESSAGES_FILE             	= "messages";
    private final String                        MATERIALS_FILE             	= "materials";
    private final String						LOST_WANDS_FILE				= "lostwands";
    private final String						SPELLS_DATA_FILE			= "spells";
    private final String						AUTOMATA_FILE				= "automata";
    private final String						URL_MAPS_FILE				= "imagemaps";

    private boolean                             disableDefaultSpells        = false;
    private boolean 							loadDefaultSpells			= true;
    private boolean 							loadDefaultWands			= true;
    private boolean 							loadDefaultEnchanting		= true;
    private boolean 							loadDefaultCrafting			= true;

    private MaterialAndData                     redstoneReplacement             = new MaterialAndData(Material.OBSIDIAN);
    private Set<Material>                       buildingMaterials               = new HashSet<Material>();
    private Set<Material>                       indestructibleMaterials         = new HashSet<Material>();
    private Set<Material>                       restrictedMaterials	 	        = new HashSet<Material>();
    private Set<Material>                       destructibleMaterials           = new HashSet<Material>();
    private Set<Material>                       interactibleMaterials           = new HashSet<Material>();
    private Set<Material>                       wearableMaterials               = new HashSet<Material>();
    private Map<String, Set<Material>>		    materialSets				    = new HashMap<String, Set<Material>>();

    private int								    undoTimeWindow				    = 6000;
    private int								    undoBlockBorderSize			    = 2;
    private int								    maxTNTPerChunk					= 0;
    private int                                 undoQueueDepth                  = 256;
    private int								    pendingQueueDepth				= 16;
    private int                                 undoMaxPersistSize              = 0;
    private boolean                             undoOnWorldSave                 = false;
    private boolean                             backupInventory                 = false;
    private boolean                             commitOnQuit             		= false;
    private boolean                             saveNonPlayerMages              = false;
    private String                              defaultWandPath                 = "";
    private WandMode							defaultWandMode				    = WandMode.INVENTORY;
    private WandMode							defaultBrushMode				= WandMode.CHEST;
    private String                              brushSelectSpell                = "";
    private boolean                             showMessages                    = true;
    private boolean                             showCastMessages                = false;
    private String								messagePrefix					= "";
    private String								castMessagePrefix				= "";
    private boolean                             soundsEnabled                   = true;
    private boolean                             indestructibleWands             = true;
    private boolean                             keepWandsOnDeath	            = true;
    private String								welcomeWand					    = "";
    private int								    messageThrottle				    = 0;
    private int								    clickCooldown					= 150;
    private boolean							    bindingEnabled					= false;
    private boolean							    spellDroppingEnabled			= false;
    private boolean							    keepingEnabled					= false;
    private boolean                             fillingEnabled                  = false;
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
    private float								maxHaste						= 5.0f;
    private float								maxHealthRegeneration			= 1.0f;
    private float								maxHungerRegeneration			= 1.0f;
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

    private float							 	castCommandCostReduction	    = 1.0f;
    private float							 	castCommandCooldownReduction	= 1.0f;
    private float								castCommandPowerMultiplier      = 0.0f;
    private float							 	costReduction	    			= 0.0f;
    private float							 	cooldownReduction				= 0.0f;
    private int								    maxBlockUpdates				    = 100;
    private int								    ageDroppedItems				    = 0;
    private int								    autoUndo						= 0;
    private int								    autoSaveTaskId					= 0;
    private double                              wandAbuseDamage                 = 0;
    private boolean                             preventMeleeDamage              = false;
    private WarpController						warpController					= null;

    private final Map<String, SpellTemplate>    spells              		= new HashMap<String, SpellTemplate>();
    private final Map<String, SpellTemplate>    spellAliases                = new HashMap<String, SpellTemplate>();
    private final Map<String, SpellCategory>    categories              	= new HashMap<String, SpellCategory>();
    private final Map<String, Mage> 		    mages                  		= new HashMap<String, Mage>();
    private final Map<String, Long>			    forgetMages					= new HashMap<String, Long>();
    private final Set<Mage>		 	            pendingConstruction			= new HashSet<Mage>();
    private final Set<Mage>                     pendingConstructionRemoval  = new HashSet<Mage>();
    private final PriorityQueue<UndoList>       scheduledUndo               = new PriorityQueue<UndoList>();
    private final Set<String>  	 			    pendingUndo					= new HashSet<String>();
    private final Map<String, WeakReference<WorldEditSchematic>> schematics	= new HashMap<String, WeakReference<WorldEditSchematic>>();

    private MagicPlugin                         plugin                      = null;
    private final File							configFolder;
    private final File							dataFolder;
    private final File							schematicFolder;
    private final File							defaultsFolder;
    private final File							playerDataFolder;
    private boolean							    enableItemHacks			 	= true;
    private boolean                             enableCreativeModeEjecting  = true;

    private int								    toggleCooldown				= 1000;
    private int								    toggleMessageRange			= 1024;

    private int                                 mageUpdateFrequency         = 20;
    private int                                 blockUpdateFrequency        = 1;
    private int                                 undoFrequency               = 10;

    private boolean                             showCastHoloText            = false;
    private boolean                             showActivateHoloText        = false;
    private int                                 castHoloTextRange           = 0;
    private int                                 activateHoloTextRange       = 0;
    private boolean							    urlIconsEnabled             = true;

    private boolean							    bypassBuildPermissions      = false;
    private boolean							    bypassPvpPermissions        = false;
    private boolean							    allPvpRestricted            = false;

    private String								extraSchematicFilePath		= null;
    private Class<?>							cuboidClipboardClass        = null;
    private Mailer								mailer						= null;
    private Material							defaultMaterial				= Material.DIRT;

    private PhysicsHandler						physicsHandler				= null;

    private Map<String, Map<Long, Automaton>> 	automata			    	= new HashMap<String, Map<Long, Automaton>>();
    private Map<String, LostWand>				lostWands					= new HashMap<String, LostWand>();
    private Map<String, Set<String>>		 	lostWandChunks				= new HashMap<String, Set<String>>();

    private int								    metricsLevel				= 5;
    private Metrics							    metrics						= null;
    private boolean							    hasDynmap					= false;
    private boolean							    hasEssentials				= false;
    private boolean							    hasCommandBook				= false;
    private boolean							    hasWorldEdit				= false;

    private String                              exampleDefaults             = null;
    private Collection<String>                  addExamples                 = null;
    private boolean                             initialized                 = false;

    // Synchronization
    private final Object                        saveLock                    = new Object();

    // Sub-Controllers
    private CraftingController					crafting					= null;
    private EnchantingController				enchanting					= null;
    private AnvilController					    anvil						= null;
    private Messages                            messages                    = null;
    private MapController                       maps                        = null;
    private TradersController					tradersController			= null;
    private DynmapController					dynmap						= null;
    private ElementalsController				elementals					= null;
    private CitizensController                  citizens					= null;
    private boolean                             citizensEnabled			    = true;

    private FactionsManager					    factionsManager				= new FactionsManager();
    private LocketteManager                     locketteManager				= new LocketteManager();
    private WorldGuardManager					worldGuardManager			= new WorldGuardManager();
    private PvPManagerManager                   pvpManager                  = new PvPManagerManager();
    private MultiverseManager                   multiverseManager           = new MultiverseManager();
    private PreciousStonesManager				preciousStonesManager		= new PreciousStonesManager();
    private TownyManager						townyManager				= new TownyManager();
}