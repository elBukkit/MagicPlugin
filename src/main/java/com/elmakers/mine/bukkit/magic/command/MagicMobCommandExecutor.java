package com.elmakers.mine.bukkit.magic.command;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

        if (args.length == 0 || args.length > 2)
		{
            sender.sendMessage(ChatColor.RED + "Usage: mmob [spawn|clear] <type>");
			return true;
		}
        
        if (args[0].equalsIgnoreCase("clear"))
        {
            sender.sendMessage(ChatColor.RED + "Not yet implemented, sorry!");
            return true;
        }

        if (!args[0].equalsIgnoreCase("spawn") || args.length != 2)
        {
            sender.sendMessage(ChatColor.RED + "Usage: mmob [spawn|clear] <type>");
            return true;
        }

        if (!(sender instanceof Player))
        {
            sender.sendMessage(ChatColor.RED + "Must be used in-game");
            return true;
        }

        String mobKey = args[1];
        Player player = (Player)sender;
        Location location = player.getEyeLocation();
        BlockIterator iterator = new BlockIterator(location.getWorld(), location.toVector(), location.getDirection(), 0, 64);
        Block block = location.getBlock();
        while (block.getType() == Material.AIR && iterator.hasNext()) {
            block = iterator.next();
        }
        block = block.getRelative(BlockFace.UP);

        MageController controller = api.getController();
        Entity spawned = controller.spawnMob(mobKey, block.getLocation());
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

	@Override
	public Collection<String> onTabComplete(CommandSender sender, String commandName, String[] args) {
		List<String> options = new ArrayList<String>();
        if (!sender.hasPermission("Magic.commands.mmob")) return options;

		if (args.length == 1) {
            options.add("spawn");
            options.add("clear");
		}

        if (args.length == 2 && args[0].equalsIgnoreCase("spawn")) {
            options.addAll(api.getController().getMobKeys());
		}
		return options;
	}
}
