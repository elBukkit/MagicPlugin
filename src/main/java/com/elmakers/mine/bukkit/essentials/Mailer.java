package com.elmakers.mine.bukkit.essentials;

import net.ess3.api.IEssentials;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.FormatUtil;
import com.earth2me.essentials.utils.StringUtil;

public class Mailer {
	private final IEssentials essentials;
	
	public Mailer(final Object ess) {
		essentials = (IEssentials)ess;
	}
	
	public boolean sendMail(CommandSender sender, String from, String to, String message) {
		final String mail = ChatColor.UNDERLINE + from + ChatColor.RESET + ": " + StringUtil.sanitizeString(FormatUtil.stripFormat(message));
		
		User toUser = essentials.getUser(to);
		if (toUser == null) {
			sender.sendMessage("Unknown player: " + to);
			return false;
		}
		
		toUser.addMail(mail);
		
		return true;
	}
}
