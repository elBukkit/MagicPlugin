package com.elmakers.mine.bukkit.spell.builtin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.spell.TargetType;
import com.elmakers.mine.bukkit.spell.TargetingSpell;
import com.elmakers.mine.bukkit.utility.Target;

/**
 * Can run any Bukkit command as a Spell.
 *
 * <p>This includes running as Console, or opping/deopping the player if needed.
 *
 * <p>This spell can also act as a targeting spell, running commands using the
 * target location or entity.
 *
 * <p>The following parameters will all be substituted in the "command" string
 * if found:
 *
 * <li><code>@_</code> - A spell, useful for command-line casting
 * <li><code>@spell</code> - name of spell being cast
 * <li><code>@p</code> - mage name
 * <li><code>@uuid</code> - mage uuid
 * <li><code>@world, @x, @y, @z</code> - mage location
 *
 * <p>If targeting is used ("target: none" to disable), the following will also be escaped:
 *
 * <li><code>@t</code> - target mage name
 * <li><code>@tuuid</code> - target entity uuid
 * <li><code>@tworld, @tx, @ty, @tz</code> - target location
 */
@Deprecated
public class CommandSpell extends TargetingSpell {
    public final static String[] PARAMETERS = {
            "command", "console", "op", "radius"
    };

    @Override
    public SpellResult onCast(ConfigurationSection parameters)
    {
        List<String> commands = null;
        if (parameters.contains("command")) {
            String command = parameters.getString("command");
            if (command != null && command.length() > 0) {
                commands = new ArrayList<>();
                commands.add(command);
            }
        } else {
            commands = parameters.getStringList("commands");
        }
        if (commands == null || commands.size() == 0) {
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
        Target target = null;
        Location targetLocation = null;
        if (getTargetType() != TargetType.NONE) {
            target = getTarget();
            targetLocation = target.getLocation();
            if (targetLocation == null) {
                return SpellResult.NO_TARGET;
            }
        }

        for (String command : commands) {
            command = command
                    .replace("@_", " ")
                    .replace("@spell", getName())
                    .replace("@pd", mage.getDisplayName())
                    .replace("@p", mage.getName())
                    .replace("@uuid", mage.getId())
                    .replace("@world", location.getWorld().getName())
                    .replace("@x", Double.toString(location.getX()))
                    .replace("@y", Double.toString(location.getY()))
                    .replace("@z", Double.toString(location.getZ()));

            if (targetLocation != null) {
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
                                .replace("@td", targetMage.getDisplayName())
                                .replace("@t", targetMage.getName())
                                .replace("@tuuid", targetMage.getId());
                    } else {
                        command = command
                                .replace("@td", controller.getEntityDisplayName(targetEntity))
                                .replace("@t", controller.getEntityName(targetEntity))
                                .replace("@tuuid", targetEntity.getUniqueId().toString());
                    }
                } else {
                    return SpellResult.NO_TARGET;
                }
            }

            if (command.contains("@a")) {
                double radius = parameters.getDouble("radius", 0);
                double radiusSquared = radius * radius;
                Player exclude = mage.getPlayer();
                if (parameters.getBoolean("exclude_player", true)) {
                    exclude = null;
                }
                Collection<? extends Player> players = controller.getPlugin().getServer().getOnlinePlayers();
                Location source = targetLocation == null ? getLocation() : targetLocation;
                for (Player player : players) {
                    if (exclude != null && exclude.equals(player)) continue;
                    if (radiusSquared != 0) {
                        Location playerLocation = player.getLocation();
                        if (playerLocation.getWorld().equals(source.getWorld())) {
                            continue;
                        }
                        if (playerLocation.distanceSquared(source) > radiusSquared) {
                            continue;
                        }
                    }

                    String playerCommand = command;
                    playerCommand = playerCommand.replace("@a", player.getName());
                    controller.getPlugin().getServer().dispatchCommand(sender, playerCommand);
                }
            } else {
                controller.getPlugin().getServer().dispatchCommand(sender, command);
            }
        }

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
