package com.elmakers.mine.bukkit.spell.builtin;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

public class CommandSpell extends BaseSpell {
    @Override
    public SpellResult onCast(ConfigurationSection parameters)
    {
        String command = parameters.getString("command");
        if (command == null || command.length() == 0) {
            return SpellResult.FAIL;
        }

        CommandSender sender = mage.getCommandSender();
        if (sender == null) {
            return SpellResult.FAIL;
        }
        controller.getPlugin().getServer().dispatchCommand(sender, command);
        return SpellResult.CAST;
    }
}
