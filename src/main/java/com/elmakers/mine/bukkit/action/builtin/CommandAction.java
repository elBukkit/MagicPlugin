package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.BlockAction;
import com.elmakers.mine.bukkit.api.action.EntityAction;
import com.elmakers.mine.bukkit.api.action.GeneralAction;
import com.elmakers.mine.bukkit.api.action.SpellAction;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.spell.TargetType;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.spell.BaseSpellAction;
import com.elmakers.mine.bukkit.spell.TargetingSpell;
import com.elmakers.mine.bukkit.utility.Target;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

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
public class CommandAction extends BaseSpellAction implements EntityAction, GeneralAction, BlockAction {
    public final static String[] PARAMETERS = {
            "command", "console", "op", "radius"
    };

    private List<String> commands = new ArrayList<String>();
    private List<String> entityCommands = new ArrayList<String>();
    private List<String> blockCommands = new ArrayList<String>();

    protected void addCommand(String command) {
        if (command.contains("@td") || command.contains("@tn") || command.contains("@tuuid")) {
            entityCommands.add(command);
        } else if (command.contains("@t")) {
            blockCommands.add(command);
        } else {
            commands.add(command);
        }
    }

    protected void addCommands(Collection<String> commands) {
        for (String command : commands) {
            addCommand(command);
        }
    }

    @Override
    public void prepare(ConfigurationSection parameters) {
        commands = new ArrayList<String>();
        entityCommands = new ArrayList<String>();
        blockCommands = new ArrayList<String>();

        if (parameters.contains("command"))
        {
            String command = parameters.getString("command");
            if (command != null && command.length() > 0)
            {
                addCommand(command);
            }
        }
        else
        {
            addCommands(parameters.getStringList("commands"));
        }
    }

    protected String parameterize(String command) {
        Location location = getLocation();
        Mage mage = getMage();

        return command
                .replace("@_", " ")
                .replace("@spell", getSpell().getName())
                .replace("@pd", mage.getDisplayName())
                .replace("@pn", mage.getName())
                .replace("@uuid", mage.getId())
                .replace("@world", location.getWorld().getName())
                .replace("@x", Double.toString(location.getX()))
                .replace("@y", Double.toString(location.getY()))
                .replace("@z", Double.toString(location.getZ()));
    }

    protected String parameterize(String command, Location targetLocation) {
        return command
                .replace("@tworld", targetLocation.getWorld().getName())
                .replace("@tx", Double.toString(targetLocation.getX()))
                .replace("@ty", Double.toString(targetLocation.getY()))
                .replace("@tz", Double.toString(targetLocation.getZ()));
    }

    protected String parameterize(String command, Entity targetEntity) {
        MageController controller = getController();
        if (controller.isMage(targetEntity)) {
            Mage targetMage = controller.getMage(targetEntity);
            return command
                    .replace("@td", targetMage.getDisplayName())
                    .replace("@tn", targetMage.getName())
                    .replace("@tuuid", targetMage.getId());
        }

        return command
                .replace("@td", controller.getEntityDisplayName(targetEntity))
                .replace("@tn", controller.getEntityName(targetEntity))
                .replace("@tuuid", targetEntity.getUniqueId().toString());
    }

    @Override
    public SpellResult perform(ConfigurationSection parameters) {
        SpellResult result = SpellResult.NO_ACTION;
        for (String command : commands) {
            String converted = parameterize(command);
            result = result.min(perform(converted, parameters));
        }
        return result;
    }

    @Override
    public SpellResult perform(ConfigurationSection parameters, Block block) {
        SpellResult result = SpellResult.NO_ACTION;
        for (String command : blockCommands) {
            String converted = parameterize(command);
            converted = parameterize(converted, block.getLocation());
            result = result.min(perform(converted, parameters));
        }
        return result;
    }

    @Override
    public SpellResult perform(ConfigurationSection parameters, Entity entity) {
        SpellResult result = SpellResult.NO_ACTION;
        for (String command : entityCommands) {
            String converted = parameterize(command);
            converted = parameterize(converted, entity.getLocation());
            converted = parameterize(converted, entity);
            result = result.min(perform(converted, parameters));
        }
        return result;
    }

    protected SpellResult perform(String command, ConfigurationSection parameters) {
        Mage mage = getMage();
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

        MageController controller = getController();
        try {
            controller.getPlugin().getServer().dispatchCommand(sender, command);
        } catch (Exception ex) {
            controller.getLogger().log(Level.WARNING, "Error running command: " + command, ex);
        }
        if (opPlayer && !isOp) {
            sender.setOp(false);
        }

        return SpellResult.CAST;
    }

    @Override
    public void getParameterNames(Collection<String> parameters)
    {
        super.getParameterNames(parameters);
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
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_BOOLEANS));
        } else if (parameterKey.equals("op")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_BOOLEANS));
        }
    }
}
