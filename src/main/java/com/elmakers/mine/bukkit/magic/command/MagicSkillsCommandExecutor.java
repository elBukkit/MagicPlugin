package com.elmakers.mine.bukkit.magic.command;

import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.heroes.SkillsSelector;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;

public class MagicSkillsCommandExecutor extends MagicTabExecutor {

	public MagicSkillsCommandExecutor(MagicAPI api) {
		super(api);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!api.hasPermission(sender, "Magic.commands.mskills"))
        {
            sendNoPermission(sender);
            return true;
        }
        if (!(sender instanceof Player))
        {
            sender.sendMessage(ChatColor.RED + "This command may only be used in-game");
            return true;
        }
        Player player = (Player)sender;
        SkillsSelector selector = new SkillsSelector(api, player);
        int page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (Exception ex) {
                sender.sendMessage(ChatColor.RED + "Expect page number, got " + args[0]);
                return true;
            }
        }
        selector.show(page);
        return true;
	}

    @Override
    public Collection<String> onTabComplete(CommandSender sender, String commandName, String[] args) {
        return new ArrayList<String>();
    }
}
