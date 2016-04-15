package com.elmakers.mine.bukkit.magic.command;

import com.elmakers.mine.bukkit.api.entity.EntityData;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
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
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

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
            sender.sendMessage(ChatColor.RED + "Usage: mmob [spawn|list] <type>");
			return true;
		}
        
        if (args[0].equalsIgnoreCase("list"))
        {
            onListMobs(sender);
            return true;
        }

        if (!args[0].equalsIgnoreCase("spawn") || args.length < 2)
        {
            sender.sendMessage(ChatColor.RED + "Usage: mmob [spawn|list] <type>");
            return true;
        }

        if (!(sender instanceof Player) && !(sender instanceof BlockCommandSender) && args.length < 6) {
            sender.sendMessage(ChatColor.RED + "Usage: mmob spawn <type> <x> <y> <z> <world>");
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
                targetLocation = null;
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
            sender.sendMessage(ChatColor.RED + "Usage: mmob spawn <type> <x> <y> <z> <world>");
            return true;
        }
        
        String mobKey = args[1];

        MageController controller = api.getController();
        Entity spawned = controller.spawnMob(mobKey, targetLocation);
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
        Set<String> keys = api.getController().getMobKeys();
        for (String key : keys) {
            EntityData mobType = api.getController().getMob(key);
            sender.sendMessage(ChatColor.AQUA + key + ChatColor.WHITE + " : " + ChatColor.DARK_AQUA + mobType.describe());
        }
    }

	@Override
	public Collection<String> onTabComplete(CommandSender sender, String commandName, String[] args) {
		List<String> options = new ArrayList<String>();
        if (!sender.hasPermission("Magic.commands.mmob")) return options;

		if (args.length == 1) {
            options.add("spawn");
            options.add("list");
		}

        if (args.length == 2 && args[0].equalsIgnoreCase("spawn")) {
            options.addAll(api.getController().getMobKeys());
            for (EntityType entityType : EntityType.values()) {
                if (entityType.isAlive() && entityType.isSpawnable()) {
                    options.add(entityType.name().toLowerCase());
                }
            }
		}
		return options;
	}
}
