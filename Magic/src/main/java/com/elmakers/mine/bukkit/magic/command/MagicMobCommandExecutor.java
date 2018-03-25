package com.elmakers.mine.bukkit.magic.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

import com.elmakers.mine.bukkit.api.entity.EntityData;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class MagicMobCommandExecutor extends MagicTabExecutor {
    public MagicMobCommandExecutor(MagicAPI api) {
        super(api);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!api.hasPermission(sender, "Magic.commands.mmob"))
        {
            sendNoPermission(sender);
            return true;
        }

        if (args.length == 0)
        {
            sender.sendMessage(ChatColor.RED + "Usage: mmob [spawn|list] <type> [count]");
            return true;
        }

        if (args[0].equalsIgnoreCase("list"))
        {
            onListMobs(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("clear"))
        {
            onClearMobs(sender, args);
            return true;
        }

        if (!args[0].equalsIgnoreCase("spawn") || args.length < 2)
        {
            return false;
        }

        if (!(sender instanceof Player) && !(sender instanceof BlockCommandSender) && args.length < 6) {
            sender.sendMessage(ChatColor.RED + "Usage: mmob spawn <type> <x> <y> <z> <world> [count]");
            return true;
        }

        Location targetLocation = null;
        World targetWorld = null;
        Player player = (sender instanceof Player) ? (Player)sender : null;
        BlockCommandSender commandBlock = (sender instanceof BlockCommandSender) ? (BlockCommandSender)sender : null;

        if (args.length >= 6) {
            targetWorld = Bukkit.getWorld(args[5]);
            if (targetWorld == null) {
                sender.sendMessage(ChatColor.RED + "Invalid world: " + ChatColor.GRAY + args[5]);
                return true;
            }
        } else if (player != null) {
            targetWorld = player.getWorld();
        } else if (commandBlock != null) {
            Block block = commandBlock.getBlock();
            targetWorld = block.getWorld();
            targetLocation = block.getLocation();
        }

        if (args.length >= 5) {
            try {
                double currentX = 0;
                double currentY = 0;
                double currentZ = 0;
                if (player != null) {
                    Location currentLocation = player.getLocation();
                    currentX = currentLocation.getX();
                    currentY = currentLocation.getY();
                    currentZ = currentLocation.getZ();
                } else if (commandBlock != null) {
                    Block blockLocation = commandBlock.getBlock();
                    currentX = blockLocation.getX();
                    currentY = blockLocation.getY();
                    currentZ = blockLocation.getZ();
                }
                targetLocation = new Location(targetWorld,
                        ConfigurationUtils.overrideDouble(args[2], currentX),
                        ConfigurationUtils.overrideDouble(args[3], currentY),
                        ConfigurationUtils.overrideDouble(args[4], currentZ));
            } catch (Exception ex) {
                sender.sendMessage(ChatColor.RED + "Usage: mmob spawn <type> <x> <y> <z> <world> [count]");
                return true;
            }
        } else if (player != null) {
            Location location = player.getEyeLocation();
            BlockIterator iterator = new BlockIterator(location.getWorld(), location.toVector(), location.getDirection(), 0, 64);
            Block block = location.getBlock();
            while (block.getType() == Material.AIR && iterator.hasNext()) {
                block = iterator.next();
            }
            block = block.getRelative(BlockFace.UP);
            targetLocation = block.getLocation();
        }

        if (targetLocation == null || targetLocation.getWorld() == null) {
            sender.sendMessage(ChatColor.RED + "Usage: mmob spawn <type> <x> <y> <z> <world> [count]");
            return true;
        }

        String mobKey = args[1];
        int count = 1;
        String countString = null;
        if (args.length == 7) {
            countString = args[6];
        } else if (args.length == 3) {
            countString = args[2];
        }
        if (countString != null) {
            try {
                count = Integer.parseInt(countString);
            } catch (Exception ex) {
                sender.sendMessage(ChatColor.RED + "Invalid count: " + countString);
                return true;
            }
        }

        if (count <= 0) return true;

        MageController controller = api.getController();
        Entity spawned = null;

        for (int i = 0; i < count; i++) {
            spawned = controller.spawnMob(mobKey, targetLocation);
        }
        if (spawned == null) {
            sender.sendMessage(ChatColor.RED + "Unknown mob type " + mobKey);
            return true;
        }

        String name = spawned.getName();
        if (name == null) {
            name = mobKey;
        }
        sender.sendMessage(ChatColor.AQUA + "Spawned mob: " + ChatColor.LIGHT_PURPLE + name);
        return true;
    }

    protected void onListMobs(CommandSender sender) {
        Map<String, Integer> mobCounts = new HashMap<>();

        Collection<Mage> mages = new ArrayList<>(api.getController().getMobMages());
        for (Mage mage : mages) {
            EntityData entityData = mage.getEntityData();
            if (entityData == null) continue;

            Integer mobCount = mobCounts.get(entityData.getKey());
            if (mobCount == null) {
                mobCounts.put(entityData.getKey(), 1);
            } else {
                mobCounts.put(entityData.getKey(), mobCount + 1);
            }
        }

        Set<String> keys = api.getController().getMobKeys();
        for (String key : keys) {
            EntityData mobType = api.getController().getMob(key);
            String message = ChatColor.AQUA + key + ChatColor.WHITE + " : " + ChatColor.DARK_AQUA + mobType.describe();
            Integer mobCount = mobCounts.get(key);
            if (mobCount != null) {
                message = message + ChatColor.GRAY + " (" + ChatColor.GREEN + mobCount + ChatColor.DARK_GREEN + " Active" + ChatColor.GRAY + ")";
            }
            sender.sendMessage(message);
        }
    }

    protected void onClearMobs(CommandSender sender, String[] args) {
        String mobType = args.length > 1 ? args[1] : null;
        if (mobType != null && mobType.equalsIgnoreCase("all")) {
            mobType = null;
        }
        String worldName = null;
        Integer radiusSquared = null;
        Location targetLocation = null;
        Player player = (sender instanceof Player) ? (Player)sender : null;
        BlockCommandSender commandBlock = (sender instanceof BlockCommandSender) ? (BlockCommandSender)sender : null;

        if (args.length == 3) {
            if (!(sender instanceof ConsoleCommandSender) && Bukkit.getWorld(args[2]) == null) {
                try {
                    int radius = Integer.parseInt(args[2]);
                    radiusSquared = radius * radius;
                } catch (Exception ignored) {

                }
            }
            if (radiusSquared == null) {
                worldName = args[2];
            } else {
                if (player != null) {
                    targetLocation = player.getLocation();
                } else if (commandBlock != null) {
                    targetLocation = commandBlock.getBlock().getLocation();
                } else {
                    sender.sendMessage(ChatColor.RED + "Invalid world: " + args[2]);
                }
            }
        } else if (args.length > 5) {
            World world = null;
            if (args.length > 6) {
                worldName = args[6];
                world = Bukkit.getWorld(worldName);
            }
            if (world == null) {
                sender.sendMessage(ChatColor.RED + "Invalid world: " + worldName);
            }
            try {
                int radius = Integer.parseInt(args[2]);
                radiusSquared = radius * radius;

                double currentX = 0;
                double currentY = 0;
                double currentZ = 0;

                if (player != null) {
                    targetLocation = player.getLocation();
                } else if (commandBlock != null) {
                    targetLocation = commandBlock.getBlock().getLocation();
                }

                if (targetLocation != null) {
                    currentX = targetLocation.getX();
                    currentY = targetLocation.getY();
                    currentZ = targetLocation.getZ();
                    if (world == null) {
                        world = targetLocation.getWorld();
                        worldName = world.getName();
                    }
                }
                if (world == null) {
                    sender.sendMessage(ChatColor.RED + "Usage: mmob clear <type> <radius> <x> <y> <z> <world>");
                    return;
                }
                targetLocation = new Location(world,
                        ConfigurationUtils.overrideDouble(args[3], currentX),
                        ConfigurationUtils.overrideDouble(args[4], currentY),
                        ConfigurationUtils.overrideDouble(args[5], currentZ));
            } catch (Exception ex) {
                sender.sendMessage(ChatColor.RED + "Usage: mmob clear <type> <radius> <x> <y> <z> <world>");
                return;
            }
        }

        Collection<Mage> mages = new ArrayList<>(api.getController().getMobMages());
        int removed = 0;
        for (Mage mage : mages) {
            EntityData entityData = mage.getEntityData();
            if (entityData == null) continue;
            if (worldName != null && !mage.getLocation().getWorld().getName().equals(worldName)) continue;
            if (mobType != null && !entityData.getKey().equals(mobType)) continue;
            if (radiusSquared != null && targetLocation != null && mage.getLocation().distanceSquared(targetLocation) > radiusSquared) continue;

            Entity entity = mage.getEntity();
            mage.undoScheduled();
            api.getController().removeMage(mage);
            if (entity != null) {
                entity.remove();
            }
            removed++;
        }
        List<World> worlds = new ArrayList<>();
        if (worldName != null) {
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                sender.sendMessage(ChatColor.RED + "Unknown world: " + ChatColor.WHITE + worldName);
                return;
            }
            worlds.add(world);
        } else {
            worlds.addAll(Bukkit.getWorlds());
        }
        Set<String> mobNames = new HashSet<>();
        if (mobType != null) {
            EntityData mob = api.getController().getMob(mobType);
            if (mob == null) {
                sender.sendMessage(ChatColor.RED + "Unknown mob type: " + ChatColor.WHITE + mobType);
                return;
            }
            mobNames.add(mob.getName());
        } else {
            Set<String> allKeys = api.getController().getMobKeys();
            for (String key : allKeys) {
                EntityData mob = api.getController().getMob(key);
                mobNames.add(mob.getName());
            }
        }
        for (World world : worlds) {
            List<Entity> entities = world.getEntities();
            for (Entity entity : entities) {
                String customName = entity.getCustomName();
                if (entity.isValid() && customName != null && mobNames.contains(customName)) {
                    if (radiusSquared != null && targetLocation != null && entity.getLocation().distanceSquared(targetLocation) > radiusSquared) continue;
                    entity.remove();
                    removed++;
                }
            }
        }
        sender.sendMessage("Removed " + removed + " magic mobs");
    }

    @Override
    public Collection<String> onTabComplete(CommandSender sender, String commandName, String[] args) {
        List<String> options = new ArrayList<>();
        if (!sender.hasPermission("Magic.commands.mmob")) return options;

        if (args.length == 1) {
            options.add("spawn");
            options.add("list");
            options.add("clear");
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("spawn") || args[0].equalsIgnoreCase("clear"))) {
            options.addAll(api.getController().getMobKeys());
            for (EntityType entityType : EntityType.values()) {
                if (entityType.isAlive() && entityType.isSpawnable()) {
                    options.add(entityType.name().toLowerCase());
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("clear")) {
            List<World> worlds = api.getPlugin().getServer().getWorlds();
            for (World world : worlds) {
                options.add(world.getName());
            }
        }
        return options;
    }
}
