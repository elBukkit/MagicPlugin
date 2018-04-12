package com.elmakers.mine.bukkit.magic.command;

import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.spell.Spell;

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
        String skillsSpell = api.getController().getSkillsSpell();
        if (skillsSpell.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "This command has been disabled");
            return true;
        }
        Mage mage = controller.getMage(sender);
        Spell spell = mage.getSpell(skillsSpell);
        if (spell == null) {
            sender.sendMessage(ChatColor.RED + "The skills selector is missing from spell configs");
            return true;
        }
        if (args.length > 0) {
            String[] parameters = {"page", args[0]};
            spell.cast(parameters);
        } else {
            spell.cast();
        }
        return true;
    }

    @Override
    public Collection<String> onTabComplete(CommandSender sender, String commandName, String[] args) {
        return new ArrayList<>();
    }
}
