package com.elmakers.mine.bukkit.magic.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.data.MageData;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.DeprecatedUtils;
import com.google.common.util.concurrent.ListenableFuture;

public class MagicSaveCommandExecutor extends MagicTabExecutor {
    public MagicSaveCommandExecutor(MagicAPI api) {
        super(api, "msave");
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
            if (!player.hasPermission(getPermissionNode()))
            {
                return false;
            }
        }

        player = DeprecatedUtils.getPlayer(args[0]);
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

        ListenableFuture<MageData> future = controller.saveMage(mage);
        future.addListener(() -> {
            if (cmd.length() > 0) {
                plugin.getServer().dispatchCommand(sender, cmd);
            }
        }, t -> Bukkit.getScheduler().runTask(controller.getPlugin(), t));
        return true;
    }

    @Override
    public Collection<String> onTabComplete(CommandSender sender, String commandName, String[] args) {
        List<String> options = new ArrayList<>();
        if (!sender.hasPermission("Magic.commands.msave")) return options;

        if (args.length == 1) {
            options.addAll(api.getPlayerNames());
        }
        return options;
    }
}
