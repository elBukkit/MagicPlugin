package com.elmakers.mine.bukkit.magic.command;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.magic.MagicController;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MagicSaveCommandExecutor extends MagicTabExecutor {
	public MagicSaveCommandExecutor(MagicAPI api) {
		super(api);
	}
	
	@Override
	public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
        Player player;
        if (args.length < 1)
        {
            return false;
        }
        if (sender instanceof Player)
        {
            player = (Player)sender;
            if (!player.hasPermission("Magic.commands.server"))
            {
                return false;
            }
        }

        player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            return false;
        }
        String executeCommand = "";
        for (int i = 1; i < args.length; i++) {
            executeCommand = executeCommand + args[i] + " ";
        }
        MagicController controller = (MagicController)api.getController();
        Mage mage = controller.getMage(player);
        final String cmd = executeCommand.trim().replace("@p", mage.getName());
        final Plugin plugin = controller.getPlugin();
        controller.saveMage(mage, true, new Runnable() {
            @Override
            public void run() {
                if (cmd.length() > 0) {
                    plugin.getServer().dispatchCommand(sender, cmd);
                }
            }
        });
        return true;
	}

	@Override
	public Collection<String> onTabComplete(CommandSender sender, String commandName, String[] args) {
		List<String> options = new ArrayList<String>();
        if (!sender.hasPermission("Magic.commands.msave")) return options;

		if (args.length == 1) {
            options.addAll(api.getPlayerNames());
		}
		return options;
	}
}
