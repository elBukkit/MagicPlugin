package com.elmakers.mine.bukkit.spell.builtin;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.spell.TargetType;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.spell.TargetingSpell;
import com.elmakers.mine.bukkit.utility.Target;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import java.util.Arrays;
import java.util.Collection;

/**
 * Can run any Bukkit command as a Spell.
 *
 * This includes running as Console, or opping/deopping the player if needed.
 *
 * This spell can also act as a targeting spell, running commands using the
 * target location or entity.
 *
 * The following parameters will all be substituted in the "command" string
 * if found:
 *
 * @_ - A spell, useful for command-line casting
 * @spell - name of spell being cast
 * @p - mage name
 * @uuid - mage uuid
 * @world, @x, @y, @z - mage location
 *
 * If targeting is used ("target: none" to disable), the following will also be escaped:
 *
 * @t - target mage name
 * @tuuid - target entity uuid
 * @tworld, @tx, @ty, @tz - target location
 */
public class CommandSpell extends TargetingSpell {
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

        Location location = getLocation();

        command = command
                .replace("@_", " ")
                .replace("@spell", getName())
                .replace("@p", mage.getName())
                .replace("@uuid", mage.getId())
                .replace("@world", location.getWorld().getName())
                .replace("@x", Double.toString(location.getX()))
                .replace("@y", Double.toString(location.getY()))
                .replace("@z", Double.toString(location.getZ()));

        if (getTargetType() != TargetType.NONE) {
            Target target = getTarget();
            Location targetLocation = target.getLocation();
            command = command
                    .replace("@tworld", targetLocation.getWorld().getName())
                    .replace("@tx", Double.toString(targetLocation.getX()))
                    .replace("@ty", Double.toString(targetLocation.getY()))
                    .replace("@tz", Double.toString(targetLocation.getZ()));

            if (target.hasEntity()) {
                Entity targetEntity = target.getEntity();
                if (controller.isMage(targetEntity)) {
                    Mage targetMage = controller.getMage(targetEntity);
                    command = command
                            .replace("@t", targetMage.getName())
                            .replace("@tuuid", targetMage.getId());
                } else {
                    command = command
                            .replace("@t", controller.getEntityName(targetEntity))
                            .replace("@tuuid", targetEntity.getUniqueId().toString());
                }
            }
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
