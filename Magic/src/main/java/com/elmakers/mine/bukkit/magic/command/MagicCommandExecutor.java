package com.elmakers.mine.bukkit.magic.command;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.security.CodeSource;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.batch.Batch;
import com.elmakers.mine.bukkit.api.block.BlockData;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.LostWand;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.block.UndoList;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.BoundingBox;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.HitboxUtils;
import com.elmakers.mine.bukkit.utility.LogMessage;
import com.elmakers.mine.bukkit.utility.MagicLogger;
import com.elmakers.mine.bukkit.utility.NMSUtils;
import com.elmakers.mine.bukkit.utility.RunnableJob;
import com.elmakers.mine.bukkit.wand.WandCleanupRunnable;

public class MagicCommandExecutor extends MagicMapExecutor {

    private RunnableJob runningTask = null;

    public MagicCommandExecutor(MagicAPI api) {
        super(api, "magic");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0)
        {
            if (!api.hasPermission(sender, getPermissionNode())) {
                sendNoPermission(sender);
                return true;
            }
            sender.sendMessage("Magic " + getMagicVersion());
            sender.sendMessage("Use /magic help for more info");
            return true;
        }

        String subCommand = args[0];
        if (sender instanceof Player)
        {
            if (!api.hasPermission(sender, "Magic.commands.magic." + subCommand)) {
                sendNoPermission(sender);
                return true;
            }
        }
        if (subCommand.equalsIgnoreCase("help"))
        {
            sender.sendMessage(controller.getMessages().get("commands.magic.help_header"));
            if (controller instanceof MagicController) {
                ((MagicController)controller).showExampleInstructions(sender);
            }
            if (sender instanceof Player) {
                Mage mage = controller.getMage(sender);
                Wand wand = mage.getActiveWand();
                if (wand != null) {
                    wand.showInstructions();
                }
            }
            sender.sendMessage(controller.getMessages().get("commands.magic.help"));
            sender.sendMessage(controller.getMessages().get("commands.magic.help_footer"));
            return true;
        }
        if (subCommand.equalsIgnoreCase("rpcheck"))
        {
            api.getController().checkResourcePack(sender);
            return true;
        }

        if (subCommand.equalsIgnoreCase("rpsend"))
        {
            api.getController().sendResourcePackToAllPlayers(sender);
            return true;
        }
        if (subCommand.equalsIgnoreCase("save"))
        {
            api.save();
            sender.sendMessage("Data saved.");
            return true;
        }
        if (subCommand.equalsIgnoreCase("load") || subCommand.equalsIgnoreCase("reload"))
        {
            api.getController().updateConfiguration(sender);
            return true;
        }
        if (subCommand.equalsIgnoreCase("clearcache"))
        {
            api.clearCache();
            sender.sendMessage("Image map and schematic caches cleared.");
            return true;
        }
        if (subCommand.equalsIgnoreCase("commit"))
        {
            if (api.commit()) {
                sender.sendMessage("All changes committed");
            } else {
                sender.sendMessage("Nothing to commit");
            }
            return true;
        }
        if (subCommand.equalsIgnoreCase("give") || subCommand.equalsIgnoreCase("sell"))
        {
            Player player = null;
            int argStart = 1;

            if (sender instanceof Player) {
                if (args.length > 1)
                {
                    player = DeprecatedUtils.getPlayer(args[1]);
                }
                if (player == null)
                {
                    player = (Player)sender;
                }
                else
                {
                    argStart = 2;
                }
            } else {
                if (args.length <= 1) {
                    sender.sendMessage("Must specify a player name");
                    return true;
                }
                argStart = 2;
                player = DeprecatedUtils.getPlayer(args[1]);
                if (player == null) {
                    sender.sendMessage("Can't find player " + args[1]);
                    return true;
                }
                if (!player.isOnline()) {
                    sender.sendMessage("Player " + args[1] + " is not online");
                    return true;
                }
            }
            String[] args2 = Arrays.copyOfRange(args, argStart, args.length);
            if (subCommand.equalsIgnoreCase("give") || subCommand.equalsIgnoreCase("sell"))
            {
                return onMagicGive(sender, player, subCommand, args2);
            }
        }
        if (subCommand.equalsIgnoreCase("worth"))
        {
            if (!(sender instanceof Player))
            {
                sender.sendMessage("This command may only be used in-game");
                return true;
            }

            Player player = (Player)sender;
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item == null || item.getType() == Material.AIR)
            {
                player.sendMessage("You must be holding an item");
                return true;
            }
            showWorth(player, item);
            return true;
        }
        if (subCommand.equalsIgnoreCase("list"))
        {
            return onMagicList(sender, subCommand, args);
        }
        if (subCommand.equalsIgnoreCase("register"))
        {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command may only be used in-game");
                return true;
            }
            if (args.length != 2 || args[1].isEmpty()) {
                sender.sendMessage(ChatColor.RED + "Usage: " + ChatColor.WHITE + "/magic register <code>");
                return true;
            }
            return onMagicRegister((Player)sender, args[1]);
        }
        if (subCommand.equalsIgnoreCase("migrate") && controller instanceof MagicController)
        {
            ((MagicController)controller).migratePlayerData(sender);
            return true;
        }
        if (subCommand.equalsIgnoreCase("cancel"))
        {
            checkRunningTask();
            if (runningTask != null) {
                runningTask.cancel();
                runningTask = null;
                sender.sendMessage("Job cancelled");
            }

            int stoppedPending = 0;
            for (Mage mage : controller.getMages()) {
                while (mage.cancelPending() != null) stoppedPending++;
            }
            // This can catch orphaned mages that are still running spells
            for (Mage mage : api.getMagesWithPendingBatches()) {
                while (mage.cancelPending() != null) stoppedPending++;
            }

            sender.sendMessage("Stopped " + stoppedPending + " pending spell casts");

            return true;
        }
        if (subCommand.equalsIgnoreCase("clean"))
        {
            checkRunningTask();
            if (runningTask != null) {
                sender.sendMessage("Cancel current job first");
                return true;
            }
            World world = null;
            String owner = null;
            if (args.length > 1) {
                owner = args[1];
            }
            if (sender instanceof Player) {
                world = ((Player)sender).getWorld();
            } else {
                if (args.length > 2) {
                    String worldName = args[2];
                    world = Bukkit.getWorld(worldName);
                }
            }

            boolean check = false;
            if (owner != null && owner.equals("check")) {
                check = true;
                owner = "ALL";
            }
            String description = check ? "Checking for" : "Cleaning up";
            String ownerName = owner == null ? "(Unowned)" : owner;
            if (world == null) {
                sender.sendMessage(description + " lost wands in all worlds for owner: " + ownerName);
            } else if (ownerName.equals("ALL")) {
                sender.sendMessage(description + " lost wands in world '" + world.getName() + "' for ALL owners");
            } else {
                sender.sendMessage(description + " lost wands in world '" + world.getName() + "' for owner " + ownerName);
            }
            runningTask = new WandCleanupRunnable(api, world, owner, check);
            runningTask.runTaskTimer(api.getPlugin(), 5, 5);

            return true;
        }
        if (subCommand.equalsIgnoreCase("logs") && controller instanceof MagicController) {

            MagicLogger magicLogger = ((MagicController) api.getController()).getLogger();

            List<LogMessage> logs = new ArrayList<>();
            String type = args.length >= 2 ? args[1] : subCommand.toLowerCase();
            if (type.equalsIgnoreCase("errors")) {
                logs.addAll(magicLogger.getErrors());
            }
            else if (type.equalsIgnoreCase("warnings")) {
                logs.addAll(magicLogger.getWarnings());
            }
            else {
                logs.addAll(magicLogger.getErrors());
                logs.addAll(magicLogger.getWarnings());
            }

            if (logs.isEmpty()) {
                sender.sendMessage("There have been no logs since the config has loaded");
                return true;
            }

            sender.sendMessage(ChatColor.AQUA + "There are " + logs.size() + " " + type.toLowerCase() + " from Magic:");
            for (LogMessage logMessage : logs) {
                sender.sendMessage(logMessage.getMessage());
            }

            return true;
        }

        sender.sendMessage("Unknown magic command: " + subCommand);
        return true;
    }

    protected boolean onMagicRegister(Player player, final String code) {
        Plugin plugin = controller.getPlugin();
        Bukkit.getScheduler().runTaskAsynchronously(controller.getPlugin(), new RegisterTask(plugin, player, code));
        player.sendMessage(ChatColor.AQUA + "Sending registration code...");
        return true;
    }

    protected boolean onMagicList(CommandSender sender, String subCommand, String[] args)
    {
        String usage = "Usage: magic list <wands|map|automata|tasks|schematics|entities|blocks>";
        String listCommand = "";
        if (args.length > 1)
        {
            listCommand = args[1];
            if (!api.hasPermission(sender, "Magic.commands.magic." + subCommand + "." + listCommand)) {
                sendNoPermission(sender);
                return false;
            }
        }
        else
        {
            sender.sendMessage(ChatColor.GRAY + "For more specific information, add 'tasks', 'wands', 'maps', 'schematics', 'entities', 'blocks' or 'automata' parameter.");

            MageController apiController = api.getController();
            if (apiController != null && apiController instanceof MagicController) {
                MagicController controller = (MagicController)apiController;
                long timeout = controller.getPhysicsTimeout();
                if (timeout > 0) {
                    long seconds = (timeout - System.currentTimeMillis()) / 1000;
                    sender.sendMessage(ChatColor.GREEN + "Physics handler active for another " + ChatColor.DARK_GREEN + seconds + ChatColor.GREEN + " seconds");
                }
                int spawnsProcessed = controller.getWorlds().getSpawnListener().getProcessedSpawns();
                if (spawnsProcessed > 0) {
                    sender.sendMessage(ChatColor.AQUA + "Spawns Replaced: " + ChatColor.LIGHT_PURPLE + spawnsProcessed);
                }
                int chunkSpawnsProcessed = controller.getWorlds().getSpawnListener().getProcessedChunkSpawns();
                if (chunkSpawnsProcessed > 0) {
                    sender.sendMessage(ChatColor.AQUA + "Chunk Gen Spawns Replaced: " + ChatColor.LIGHT_PURPLE + chunkSpawnsProcessed);
                }
            }

            Collection<Mage> mages = controller.getMages();
            int lockedChunks = controller.getLockedChunks().size();
            if (lockedChunks > 0) {
                sender.sendMessage(ChatColor.AQUA + "Locked Chunks: " + ChatColor.LIGHT_PURPLE + lockedChunks);
            }
            sender.sendMessage(ChatColor.AQUA + "Modified blocks: " + ChatColor.LIGHT_PURPLE + UndoList.getRegistry().getModified().size());
            sender.sendMessage(ChatColor.AQUA + "Watching blocks: " + ChatColor.LIGHT_PURPLE + UndoList.getRegistry().getWatching().size());
            int breakingCount = UndoList.getRegistry().getBreaking().size();
            if (breakingCount > 0) {
                sender.sendMessage(ChatColor.AQUA + "Registered breaking: " + ChatColor.LIGHT_PURPLE + breakingCount);
            }
            int breakableCount = UndoList.getRegistry().getBreakable().size();
            if (breakableCount > 0) {
                sender.sendMessage(ChatColor.AQUA + "Registered breakable: " + ChatColor.LIGHT_PURPLE + breakableCount);
            }
            int reflectiveCount = UndoList.getRegistry().getReflective().size();
            if (reflectiveCount > 0) {
                sender.sendMessage(ChatColor.AQUA + "Registered reflective: " + ChatColor.LIGHT_PURPLE + reflectiveCount);
            }
            int lightCount = controller.getLightCount();
            if (lightCount > 0) {
                sender.sendMessage(ChatColor.AQUA + "Registered lights: " + ChatColor.LIGHT_PURPLE + lightCount);
            }

            sender.sendMessage(ChatColor.LIGHT_PURPLE + "Active mages: " + ChatColor.LIGHT_PURPLE + mages.size());
            Collection<com.elmakers.mine.bukkit.api.block.UndoList> pendingUndo = api.getPendingUndo();
            sender.sendMessage(ChatColor.AQUA + "Pending undo (" + ChatColor.LIGHT_PURPLE + pendingUndo.size() + ChatColor.AQUA + "): ");

            long now = System.currentTimeMillis();
            for (com.elmakers.mine.bukkit.api.block.UndoList undo : pendingUndo) {
                long remainingTime = (undo.getScheduledTime() - now) / 1000;

                sender.sendMessage(ChatColor.AQUA + undo.getName() + ChatColor.GRAY + " will undo in "
                        + ChatColor.WHITE + "" + remainingTime + "" + ChatColor.GRAY
                        + " seconds");
            }

            Collection<Mage> pending = api.getMagesWithPendingBatches();
            sender.sendMessage(ChatColor.AQUA + "Pending casts (" + ChatColor.LIGHT_PURPLE + pending.size() + ChatColor.AQUA + "): ");
            for (Mage mage : pending) {
                int totalSize = 0;
                int totalRemaining = 0;
                Collection<Batch> pendingBatches = mage.getPendingBatches();
                String names = "";
                if (pendingBatches.size() > 0) {
                    List<String> nameList = new ArrayList<>();
                    for (Batch batch : pendingBatches) {
                        nameList.add(batch.getName());
                        totalSize += batch.size();
                        totalRemaining += batch.remaining();
                    }
                    names = StringUtils.join(nameList, ChatColor.GRAY + "," + ChatColor.YELLOW);
                }
                int percent = totalSize > 0 ? totalRemaining * 100 / totalSize : 100;
                percent = 100 - Math.min(100, percent);
                sender.sendMessage(ChatColor.AQUA + mage.getName() + ChatColor.GRAY + " has "
                        + ChatColor.WHITE + "" + pendingBatches.size() + "" + ChatColor.GRAY
                        + " pending (" + ChatColor.GOLD + percent + ChatColor.WHITE + "%"
                        + ChatColor.GRAY + ") ("
                        + ChatColor.YELLOW + names + ChatColor.GRAY + ")");
            }
            ((MagicController)api.getController()).checkLogs(sender);
            return true;
        }
        if (listCommand.equalsIgnoreCase("schematics")) {
            List<String> schematics = new ArrayList<>();
            try {
                Plugin plugin = (Plugin)api;
                MagicController controller = (MagicController)api.getController();

                // Find built-in schematics
                CodeSource src = MagicAPI.class.getProtectionDomain().getCodeSource();
                if (src != null) {
                    URL jar = src.getLocation();
                    try (InputStream is = jar.openStream();
                            ZipInputStream zip = new ZipInputStream(is)) {
                        while (true) {
                            ZipEntry e = zip.getNextEntry();
                            if (e == null)
                                break;
                            String name = e.getName();
                            if (name.startsWith("schematics/")) {
                                schematics.add(name.replace("schematics/", ""));
                            }
                        }
                    }
                }

                // Check extra path first
                File configFolder = plugin.getDataFolder();
                File magicSchematicFolder = new File(configFolder, "schematics");
                if (magicSchematicFolder.exists()) {
                    for (File nextFile : magicSchematicFolder.listFiles()) {
                        schematics.add(nextFile.getName());
                    }
                }
                String extraSchematicFilePath = controller.getExtraSchematicFilePath();
                if (extraSchematicFilePath != null && extraSchematicFilePath.length() > 0) {
                    File schematicFolder = new File(configFolder, "../" + extraSchematicFilePath);
                    if (schematicFolder.exists() && !schematicFolder.equals(magicSchematicFolder)) {
                        for (File nextFile : schematicFolder.listFiles()) {
                            schematics.add(nextFile.getName());
                        }
                    }
                }
            } catch (Exception ex) {
                sender.sendMessage("Error loading schematics: " + ex.getMessage());
                ex.printStackTrace();;
            }

            sender.sendMessage(ChatColor.DARK_AQUA + "Found " + ChatColor.LIGHT_PURPLE + schematics.size() + ChatColor.DARK_AQUA + " schematics");
            Collections.sort(schematics);
            for (String schematic : schematics) {
                if (schematic.indexOf(".schematic") > 0) {
                    sender.sendMessage(ChatColor.AQUA + schematic.replace(".schematic", ""));
                }
            }

            return true;
        }

        if (listCommand.equalsIgnoreCase("tasks")) {
            List<BukkitTask> tasks = Bukkit.getScheduler().getPendingTasks();
            HashMap<String, Integer> pluginCounts = new HashMap<>();
            HashMap<String, HashMap<String, Integer>> taskCounts = new HashMap<>();
            for (BukkitTask task : tasks)  {
                String pluginName = task.getOwner().getName();
                HashMap<String, Integer> pluginTaskCounts = taskCounts.get(pluginName);
                if (pluginTaskCounts == null) {
                    pluginTaskCounts = new HashMap<>();
                    taskCounts.put(pluginName, pluginTaskCounts);
                }
                String className = "(Unknown)";
                Runnable taskRunnable = CompatibilityUtils.getTaskRunnable(task);
                if (taskRunnable != null) {
                    Class<? extends Runnable> taskClass = taskRunnable.getClass();
                    className = taskClass.getName();
                }
                Integer count = pluginTaskCounts.get(className);
                if (count == null) count = 0;
                count++;
                pluginTaskCounts.put(className, count);

                Integer totalCount = pluginCounts.get(pluginName);
                if (totalCount == null) totalCount = 0;
                totalCount++;
                pluginCounts.put(pluginName, totalCount);
            }
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "Active tasks: " + tasks.size());
            for (Entry<String, HashMap<String, Integer>> pluginEntry : taskCounts.entrySet()) {
                String pluginName = pluginEntry.getKey();
                sender.sendMessage(" " + ChatColor.DARK_PURPLE + pluginName + ": " + ChatColor.LIGHT_PURPLE + pluginCounts.get(pluginName));
                for (Entry<String, Integer> taskEntry : pluginEntry.getValue().entrySet()) {
                    sender.sendMessage("  " + ChatColor.DARK_PURPLE + taskEntry.getKey() + ": " + ChatColor.LIGHT_PURPLE + taskEntry.getValue());
                }
            }

            return true;
        }

        if (listCommand.equalsIgnoreCase("wands")) {
            String owner = "";
            if (args.length > 2) {
                owner = args[2];
            }
            Collection<LostWand> lostWands = api.getLostWands();
            int shown = 0;
            for (LostWand lostWand : lostWands) {
                Location location = lostWand.getLocation();
                if (location == null) continue;
                if (owner.length() > 0 && !owner.equalsIgnoreCase(lostWand.getOwner())) {
                    continue;
                }
                shown++;
                sender.sendMessage(ChatColor.AQUA + lostWand.getName() + ChatColor.WHITE + " (" + lostWand.getOwner() + ") @ " + ChatColor.BLUE + location.getWorld().getName() + " "
                        + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ());
            }

            sender.sendMessage(shown + " lost wands found" + (owner.length() > 0 ? " for " + owner : ""));
            return true;
        }

        if (listCommand.equalsIgnoreCase("automata")) {
            Collection<Mage> automata = api.getAutomata();
            for (Mage automaton : automata) {
                Location location = automaton.getLocation();
                String worldName = location.getWorld().getName();
                boolean isOnline = false;
                World world = Bukkit.getWorld(worldName);
                if (worldName != null) {
                    isOnline = world.isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4);
                }
                ChatColor nameColor = isOnline ? ChatColor.AQUA : ChatColor.GRAY;
                sender.sendMessage(nameColor + automaton.getName() + ChatColor.WHITE + " @ " + ChatColor.BLUE + worldName + " "
                        + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ());
            }

            sender.sendMessage(automata.size() + " automata active");
            return true;
        }

        if (listCommand.equalsIgnoreCase("maps")) {
            String keyword = "";
            for (int i = 2; i < args.length; i++)
            {
                if (i != 2) keyword = keyword + " ";
                keyword = keyword + args[i];
            }
            onMapList(sender, keyword);
            return true;
        }

        if (listCommand.equalsIgnoreCase("blocks")) {
            for (BlockData blockData : UndoList.getRegistry().getModified().values())
            {
                BlockVector blockLocation = blockData.getLocation();
                Block block = blockData.getBlock();
                com.elmakers.mine.bukkit.api.block.UndoList undoList = blockData.getUndoList();
                String listName = undoList == null ? "Unknown" : undoList.getName();
                String blockDescription = block == null ? "Unloaded" : block.getType().toString();
                sender.sendMessage(ChatColor.BLUE + "Block at "
                        + ChatColor.GRAY + blockLocation.getBlockX() + ChatColor.DARK_GRAY + ","
                        + ChatColor.GRAY + blockLocation.getBlockY() + ChatColor.DARK_GRAY + ","
                        + ChatColor.GRAY + blockLocation.getBlockZ()
                        + ChatColor.BLUE + " stored as " + ChatColor.AQUA + blockData.getMaterial()
                        + ChatColor.BLUE + " is currently " + ChatColor.AQUA + blockDescription
                        + ChatColor.BLUE + " from " + ChatColor.GOLD + listName);
            }
            return true;
        }

        if (listCommand.equalsIgnoreCase("mages")) {
            for (Mage mage : api.getController().getMages())
            {
                Entity mageEntity = mage.getEntity();
                Location location = mage.getLocation();
                ChatColor mageColor = ChatColor.YELLOW;
                if (mage instanceof com.elmakers.mine.bukkit.magic.Mage && ((com.elmakers.mine.bukkit.magic.Mage)mage).isForget()) {
                    mageColor = ChatColor.RED;
                } else if (mage.isAutomaton()) {
                    mageColor = ChatColor.GOLD;
                }
                String mageType = mageEntity == null ? "Non-Entity" : mageEntity.getType().name();
                String message = ChatColor.AQUA + "Mage " + mageColor + mage.getId()
                        + ChatColor.GRAY + " (" + mage.getName() + ")" + ChatColor.AQUA + " of type "
                        + ChatColor.DARK_AQUA + mageType + ChatColor.AQUA;
                if (location != null) {
                    String worldName = location.getWorld() != null ? location.getWorld().getName() : "(Unknown world)";
                    message = message + " is at " + ChatColor.BLUE + worldName + " " + ChatColor.DARK_PURPLE
                        + " " + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ();
                }
                sender.sendMessage(message);
            }
            return true;
        }

        if (listCommand.equalsIgnoreCase("entities")) {
            World world = Bukkit.getWorlds().get(0);
            NumberFormat formatter = new DecimalFormat("#0.0");
            List<EntityType> types = Arrays.asList(EntityType.values());
            Collections.sort(types, new Comparator<EntityType>() {
                @Override
                public int compare(EntityType o1, EntityType o2) {
                    return o1.name().compareTo(o2.name());
                }
            });
            Collection<? extends Player> players = Bukkit.getServer().getOnlinePlayers();
            for (Player player : players)
            {
                showEntityInfo(sender, player, EntityType.PLAYER.name() + ChatColor.GRAY + " (" + player.getName() + " [" + (player.isSneaking() ? "sneaking" : "standing") + "])", formatter);
                break;
            }

            final Class<?> worldClass = NMSUtils.getBukkitClass("net.minecraft.server.World");
            for (EntityType entityType : types)
            {
                if (entityType.isSpawnable())
                {
                    Entity testEntity = null;
                    String errorMessage = null;
                    String entityName = "Entity" + entityType.getEntityClass().getSimpleName();
                    // A few hacky special cases :(
                    // Still better than actually adding all of these entities to the world!
                    if (entityName.equals("EntityGiant")) {
                        entityName = "EntityGiantZombie";
                    } else if (entityName.equals("EntityLeashHitch")) {
                        entityName = "EntityLeash";
                    } else if (entityName.equals("EntityStorageMinecart")) {
                        entityName = "EntityMinecartChest";
                    } else if (entityName.equals("EntitySpawnerMinecart")) {
                        entityName = "EntityMinecartMobSpawner";
                    } else if (entityName.equals("EntityCommandMinecart")) {
                        entityName = "EntityMinecartCommandBlock";
                    } else if (entityName.equals("EntityPoweredMinecart")) {
                        entityName = "EntityMinecartFurnace";
                    } else if (entityName.equals("EntityExplosiveMinecart")) {
                        entityName = "EntityMinecartTNT";
                    } else if (entityName.contains("Minecart")) {
                        entityName = entityType.getEntityClass().getSimpleName();
                        entityName = entityName.replace("Minecart", "");
                        entityName = "EntityMinecart" + entityName;
                    }
                    try {
                        Class<?> entityClass = NMSUtils.getBukkitClass("net.minecraft.server." + entityName);
                        if (entityClass != null) {
                            Constructor<? extends Object> constructor = entityClass.getConstructor(worldClass);
                            Object nmsWorld = NMSUtils.getHandle(world);
                            Object nmsEntity = constructor.newInstance(nmsWorld);
                            testEntity = CompatibilityUtils.getBukkitEntity(nmsEntity);
                            if (testEntity == null) {
                                errorMessage = "Failed to get Bukkit entity for class " + entityName;
                            }
                        } else {
                            errorMessage = "Could not load class " + entityName;
                        }
                    } catch (Exception ex) {
                        testEntity = null;
                        errorMessage = ex.getClass().getSimpleName() + " [" + entityName + "]";
                        String message = ex.getMessage();
                        if (message != null && !message.isEmpty()) {
                            errorMessage += ": " + message;
                        }
                    }
                    if (testEntity == null) {
                        sender.sendMessage(ChatColor.BLACK + entityType.name() + ": " + ChatColor.RED + "Spawning error " + ChatColor.DARK_RED + "(" + errorMessage + ")");
                        continue;
                    }
                    String label = entityType.name();

                    Ageable ageable = (testEntity instanceof Ageable) ? (Ageable)testEntity : null;
                    Zombie zombie = (testEntity instanceof Zombie) ? (Zombie)testEntity : null;
                    Slime slime = (testEntity instanceof Slime) ? (Slime)testEntity : null;
                    if (ageable != null)
                    {
                        label = label + ChatColor.GRAY + " (Adult)";
                        ageable.setAdult();
                    }
                    else if (zombie != null)
                    {
                        label = label + ChatColor.GRAY + " (Adult)";
                        zombie.setBaby(false);
                    }
                    else if (slime != null)
                    {
                        label = label + ChatColor.GRAY + " (Size 1)";
                        slime.setSize(1);
                    }

                    showEntityInfo(sender, testEntity, label, formatter);
                    if (ageable != null)
                    {
                        label = entityType.name() + ChatColor.GRAY + " (Baby)";
                        ageable.setBaby();
                        showEntityInfo(sender, testEntity, label, formatter);
                    }
                    else if (zombie != null)
                    {
                        label = entityType.name() + ChatColor.GRAY + " (Baby)";
                        zombie.setBaby(true);
                        showEntityInfo(sender, testEntity, label, formatter);
                    }
                    else if (slime != null)
                    {
                        label = entityType.name() + ChatColor.GRAY + " (Size 2)";
                        slime.setSize(2);
                        showEntityInfo(sender, testEntity, label, formatter);
                        label = entityType.name() + ChatColor.GRAY + " (Size 4)";
                        slime.setSize(4);
                        showEntityInfo(sender, testEntity, label, formatter);
                        label = entityType.name() + ChatColor.GRAY + " (Size 8)";
                        slime.setSize(8);
                        showEntityInfo(sender, testEntity, label, formatter);
                        label = entityType.name() + ChatColor.GRAY + " (Size 16)";
                        slime.setSize(16);
                        showEntityInfo(sender, testEntity, label, formatter);
                    }
                }
            }
            return true;
        }

        sender.sendMessage(usage);
        return true;
    }

    private void showEntityInfo(CommandSender sender, Entity entity, String label, NumberFormat formatter)
    {
        BoundingBox hitbox = HitboxUtils.getHitbox(entity);
        Vector size = hitbox.size();
        String message = ChatColor.BLACK + label + ": "
                + ChatColor.AQUA + formatter.format(size.getX()) + ChatColor.DARK_GRAY + "x"
                + ChatColor.AQUA + formatter.format(size.getY()) + ChatColor.DARK_GRAY + "x"
                + ChatColor.AQUA + formatter.format(size.getZ());

        if (entity instanceof LivingEntity)
        {
            LivingEntity li = (LivingEntity)entity;
            message += ChatColor.DARK_GRAY + ", " + ChatColor.GREEN + ((int)CompatibilityUtils.getMaxHealth(li)) + "hp";
        }
        sender.sendMessage(message);
    }

    protected boolean onMagicGive(CommandSender sender, Player player, String command, String[] args)
    {
        String playerCommand = (sender instanceof Player) ? "" : "<player> ";
        String usageString = "Usage: /magic give " + playerCommand + "<spellname|'material'|'upgrade'|'wand'> [materialname|wandname]";
        if (args.length == 0) {
            sender.sendMessage(usageString);
            return true;
        }

        String key = "";
        boolean isMaterial = false;
        boolean isWand = false;
        boolean isUpgrade = false;

        if (args.length > 1 && !args[0].equals("material") && !args[0].equals("wand") && !args[0].equals("upgrade")) {
            sender.sendMessage(usageString);
            return true;
        }

        if (args[0].equals("wand")) {
            isWand = true;
            key = args.length > 1 ? args[1] : "";
        } else if (args[0].equals("upgrade")) {
            isUpgrade = true;
            key =  args.length > 1 ? args[1] : "";
        } else if (args[0].equals("material")) {
            if (args.length < 2) {
                sender.sendMessage(usageString);
                return true;
            }
            isMaterial = true;
            key = args[1];
        } else {
            key = args[0];
        }

        boolean giveItem = command.equals("give") || command.equals("sell");
        boolean showWorth = command.equals("worth") || command.equals("sell");
        boolean giveValue = command.equals("sell");

        if (isWand) {
            giveWand(sender, player, key, false, giveItem, giveValue, showWorth);
        } else if (isMaterial) {
            onGiveBrush(sender, player, key, false, giveItem, giveValue, showWorth);
        } else if (isUpgrade) {
            onGiveUpgrade(sender, player, key, false, giveItem, giveValue, showWorth);
        } else {
            onGive(sender, player, key, giveItem, giveValue, showWorth);
        }

        return true;
    }

    protected void onGive(CommandSender sender, Player player, String key, boolean giveItem, boolean giveValue, boolean showWorth)
    {
        if (!onGiveSpell(sender, player, key, true, giveItem, giveValue, showWorth)) {
            if (!onGiveBrush(sender, player, key, true, giveItem, giveValue, showWorth))
            {
                if (!giveWand(sender, player, key, true, giveItem, giveValue, showWorth))
                {
                    sender.sendMessage("Failed to create a spell, brush or wand item for " + key);
                }
            }
        }
    }

    protected boolean onGiveSpell(CommandSender sender, Player player, String spellKey, boolean quiet, boolean giveItem, boolean giveValue, boolean showWorth)
    {
        ItemStack itemStack = api.createSpellItem(spellKey);
        if (itemStack == null) {
            if (!quiet) sender.sendMessage("Failed to create spell item for " + spellKey);
            return false;
        }

        if (giveItem) {
            api.giveItemToPlayer(player, itemStack);
            if (sender != player && !quiet) {
                sender.sendMessage("Gave spell " + spellKey + " to " + player.getName());
            }
        }
        if (showWorth) {
            showWorth(sender, itemStack);
        }
        return true;
    }

    protected boolean onGiveBrush(CommandSender sender, Player player, String materialKey, boolean quiet, boolean giveItem, boolean giveValue, boolean showWorth)
    {
        ItemStack itemStack = api.createBrushItem(materialKey);
        if (itemStack == null) {
            if (!quiet) sender.sendMessage("Failed to create material item for " + materialKey);
            return false;
        }

        if (giveItem) {
            api.giveItemToPlayer(player, itemStack);
            if (sender != player && !quiet) {
                sender.sendMessage("Gave brush " + materialKey + " to " + player.getName());
            }
        }
        if (showWorth) {
            showWorth(sender, itemStack);
        }
        return true;
    }

    protected boolean onGiveUpgrade(CommandSender sender, Player player, String wandKey, boolean quiet, boolean giveItem, boolean giveValue, boolean showWorth)
    {
        Mage mage = controller.getMage(player);
        Wand currentWand =  mage.getActiveWand();
        if (currentWand != null) {
            currentWand.closeInventory();
        }

        Wand wand = api.createWand(wandKey);
        if (wand != null) {
            wand.makeUpgrade();
            if (giveItem) {
                api.giveItemToPlayer(player, wand.getItem());
                if (sender != player && !quiet) {
                    sender.sendMessage("Gave upgrade " + wand.getName() + " to " + player.getName());
                }
            }
            if (showWorth) {
                showWorth(sender, wand.getItem());
            }
        } else  {
            if (!quiet) sender.sendMessage(api.getMessages().getParameterized("wand.unknown_template", "$name", wandKey));
            return false;
        }
        return true;
    }

    protected void checkRunningTask()
    {
        if (runningTask != null && runningTask.isFinished()) {
            runningTask = null;
        }
    }

    @Override
    public Collection<String> onTabComplete(CommandSender sender, String commandName, String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length == 1) {
            addIfPermissible(sender, options, "Magic.commands.magic.", "clean");
            addIfPermissible(sender, options, "Magic.commands.magic.", "clearcache");
            addIfPermissible(sender, options, "Magic.commands.magic.", "cancel");
            addIfPermissible(sender, options, "Magic.commands.magic.", "load");
            addIfPermissible(sender, options, "Magic.commands.magic.", "save");
            addIfPermissible(sender, options, "Magic.commands.magic.", "commit");
            addIfPermissible(sender, options, "Magic.commands.magic.", "give");
            addIfPermissible(sender, options, "Magic.commands.magic.", "worth");
            addIfPermissible(sender, options, "Magic.commands.magic.", "sell");
            addIfPermissible(sender, options, "Magic.commands.magic.", "list");
            addIfPermissible(sender, options, "Magic.commands.magic.", "rpcheck");
            addIfPermissible(sender, options, "Magic.commands.magic.", "rpsend");
            addIfPermissible(sender, options, "Magic.commands.magic.", "register");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("list")) {
                addIfPermissible(sender, options, "Magic.commands.magic.list", "maps");
                addIfPermissible(sender, options, "Magic.commands.magic.list", "wands");
                addIfPermissible(sender, options, "Magic.commands.magic.list", "automata");
                addIfPermissible(sender, options, "Magic.commands.magic.list", "schematics");
                addIfPermissible(sender, options, "Magic.commands.magic.list", "entities");
                addIfPermissible(sender, options, "Magic.commands.magic.list", "tasks");
                addIfPermissible(sender, options, "Magic.commands.magic.list", "blocks");
                addIfPermissible(sender, options, "Magic.commands.magic.list", "mages");
            } else if (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("worth") || args[0].equalsIgnoreCase("sell")) {
                options.add("wand");
                options.add("material");
                options.add("upgrade");
                Collection<SpellTemplate> spellList = api.getSpellTemplates(sender.hasPermission("Magic.bypass_hidden"));
                for (SpellTemplate spell : spellList) {
                    options.add(spell.getKey());
                }
                Collection<String> allWands = api.getWandKeys();
                for (String wandKey : allWands) {
                    options.add(wandKey);
                }
                options.addAll(api.getBrushes());
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("sell")) {
                if (args[1].equalsIgnoreCase("upgrade") || args[1].equalsIgnoreCase("wand")) {
                    Collection<String> allWands = api.getWandKeys();
                    for (String wandKey : allWands) {
                        options.add(wandKey);
                    }
                } else if (args[1].equalsIgnoreCase("material")) {
                    options.addAll(api.getBrushes());
                }
            } else if (args[0].equalsIgnoreCase("configure") || args[0].equalsIgnoreCase("describe") || args[0].equalsIgnoreCase("desc")) {
                Player player = DeprecatedUtils.getPlayer(args[1]);
                if (player != null) {
                    Mage mage = controller.getMage(player);
                    ConfigurationSection data = mage.getData();
                    options.addAll(data.getKeys(false));
                }
            }
        }
        return options;
    }
}
