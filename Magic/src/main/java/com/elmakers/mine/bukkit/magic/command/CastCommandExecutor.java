package com.elmakers.mine.bukkit.magic.command;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.utility.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.NMSUtils;

public class CastCommandExecutor extends MagicTabExecutor {

    public CastCommandExecutor(MagicAPI api) {
        super(api);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        String commandName = command.getName();
        if (commandName.equalsIgnoreCase("castp"))
        {
            if (!api.hasPermission(sender, "Magic.commands.castp")) {
                sendNoPermission(sender);
                return true;
            }
            if (args.length < 1) {
                if (sender != null) sender.sendMessage("Usage: /castp [player] [spell] <parameters>");
                return true;
            }
            String playerName = args[0];

            // Look for Entity-based Mages
            Mage mage = null;
            if (playerName.contains(",")) {
                String[] idPieces = StringUtils.split(playerName, ',');
                if (idPieces.length == 4 || idPieces.length == 2) {
                    try {
                        String entityId = idPieces[idPieces.length - 1];
                        Entity entity = controller.getPlugin().getServer().getEntity(UUID.fromString(entityId));
                        if (entity == null) {
                            if (sender != null) sender.sendMessage("Entity not found with id " + entityId);
                            return false;
                        }

                        MageController controller = api.getController();
                        mage = controller.getMage(entity);

                        // If we have the mage, we no longer want to send anything to the console.
                        sender = null;

                    } catch (Throwable ex) {
                        if (sender != null) sender.sendMessage("Failed to find entity by id, check server logs for errors");
                        ex.printStackTrace();
                        return false;
                    }
                }
            }
            else if (playerName.contains(":")) {
                // Look for custom id/name Mages
                String[] pieces = StringUtils.split(playerName, ':');
                String mageId = pieces[0];
                String mageName = (pieces.length > 0) ? pieces[1] : mageId;

                MageController controller = api.getController();
                mage = controller.getMage(mageId, mageName);
            }

            Player player = DeprecatedUtils.getPlayer(playerName);
            if (mage == null && player == null && playerName.contains("-")) {
                try {
                    Entity entity = controller.getPlugin().getServer().getEntity(UUID.fromString(playerName));
                    if (entity != null) {
                        mage = api.getController().getMage(entity);

                        // If we have the mage, we no longer want to send anything to the console.
                        sender = null;
                    }
                } catch (Throwable ex) {
                    if (sender != null) sender.sendMessage("Failed to find entity " + playerName + ", check server logs for errors");
                    ex.printStackTrace();
                    return false;
                }
            }

            if (mage != null && !mage.isLoading()) {
                String[] castParameters = Arrays.copyOfRange(args, 1, args.length);
                if (castParameters.length < 1) {
                    if (sender != null) sender.sendMessage("Invalid command line, expecting more parameters");
                    return false;
                }

                String spellName = castParameters[0];
                Spell spell = mage.getSpell(spellName);
                if (spell == null) {
                    if (sender != null) sender.sendMessage("Unknown spell " + spellName);
                    return false;
                }

                String[] parameters = new String[castParameters.length - 1];
                for (int i = 1; i < castParameters.length; i++)
                {
                    parameters[i - 1] = castParameters[i];
                }

                if (spell.cast(parameters)) {
                    if (sender != null) sender.sendMessage("Cast " + spell.getName() + " as " + mage.getName());
                } else {
                    if (sender != null) sender.sendMessage("Failed to cast " + spell.getName() + " as " + mage.getName());
                }
                return true;
            }

            if (player == null) {
                if (sender != null) sender.sendMessage("Can't find player " + playerName);
                return true;
            }
            if (!player.isOnline()) {
                if (sender != null) sender.sendMessage("Player " + playerName + " is not online");
                return true;
            }
            String[] args2 = Arrays.copyOfRange(args, 1, args.length);
            return processCastCommand(sender, player, args2);
        }

        if (commandName.equalsIgnoreCase("cast"))
        {
            if (!api.hasPermission(sender, "Magic.commands.cast")) {
                sendNoPermission(sender);
                return true;
            }
            Player player = null;
            if (sender instanceof Player) {
                player = (Player)sender;
            }
            return processCastCommand(sender, player, args);
        }

        return false;
    }

    public boolean processCastCommand(CommandSender sender, Entity entity, String[] castParameters)
    {
        if (castParameters.length < 1) return false;

        String spellName = castParameters[0];
        String[] parameters = null;
        if (sender.hasPermission("Magic.commands.cast.parameters"))
        {
            parameters = new String[castParameters.length - 1];
            for (int i = 1; i < castParameters.length; i++)
            {
                parameters[i - 1] = castParameters[i];
            }
        }
        api.cast(spellName, parameters, sender, entity);
        return true;
    }

    @Override
    public Collection<String> onTabComplete(CommandSender sender, String commandName, String[] args) {
        Collection<String> options = new HashSet<>();

        String permissionKey = "cast";
        if (commandName.contains("castp"))
        {
            permissionKey = "castp";
            if (args.length == 1) {
                options.addAll(api.getPlayerNames());
                return options;
            } else if (args.length > 1) {
                args = Arrays.copyOfRange(args, 1, args.length);
            }
        }

        if (args.length == 1) {
            Collection<SpellTemplate> spellList = api.getController().getSpellTemplates(true);
            for (SpellTemplate spell : spellList) {
                addIfPermissible(sender, options, "Magic." + permissionKey + ".", spell.getKey());
            }
        }

        if (args.length > 1 && sender.hasPermission("Magic.commands.cast.parameters"))
        {
            String spellName = args[0];
            SpellTemplate spell = api.getSpellTemplate(spellName);
            if (spell != null) {
                if (args.length % 2 == 0) {
                    spell.getParameters(options);
                } else {
                    spell.getParameterOptions(options, args[args.length - 2]);
                }
            }
        }

        return options;
    }

}
