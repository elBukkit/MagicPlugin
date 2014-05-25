package com.elmakers.mine.bukkit.spell.builtin;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Arrays;
import java.util.Collection;

public class CommandSpell extends BaseSpell {
    public final static String[] PARAMETERS = {
            "command", "console", "op"
    };

    @Override
    public SpellResult onCast(ConfigurationSection parameters)
    {
        String command = parameters.getString("command");
        if (command == null || command.length() == 0) {
            return SpellResult.FAIL;
        }

        boolean asConsole = parameters.getBoolean("console", false);
        CommandSender sender = asConsole ? Bukkit.getConsoleSender() : mage.getCommandSender();
        if (sender == null) {
            return SpellResult.FAIL;
        }

        boolean opPlayer = parameters.getBoolean("op", false);
        boolean isOp = sender.isOp();
        if (opPlayer && !isOp) {
            sender.setOp(true);
        }

        controller.getPlugin().getServer().dispatchCommand(sender, command);

        if (opPlayer && !isOp) {
            sender.setOp(false);
        }
        return SpellResult.CAST;
    }

    @Override
    public void getParameters(Collection<String> parameters)
    {
        super.getParameters(parameters);
        parameters.addAll(Arrays.asList(PARAMETERS));
    }

    @Override
    public void getParameterOptions(Collection<String> examples, String parameterKey)
    {
        super.getParameterOptions(examples, parameterKey);

        if (parameterKey.equals("command")) {
            examples.add("spawn");
            examples.add("clear");
        } else if (parameterKey.equals("console")) {
            examples.addAll(Arrays.asList(EXAMPLE_BOOLEANS));
        } else if (parameterKey.equals("op")) {
            examples.addAll(Arrays.asList(EXAMPLE_BOOLEANS));
        }
    }
}
