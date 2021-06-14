package com.elmakers.mine.bukkit.magic.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;

public class CastCommandExecutor extends MagicTabExecutor {

    public CastCommandExecutor(MagicAPI api) {
        super(api, new String[] {"cast", "castp"});
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
            List<Entity> targets = CompatibilityLib.getCompatibilityUtils().selectEntities(sender, playerName);
            List<Mage> mages = new ArrayList<>();
            if (targets != null) {
                MageController controller = api.getController();
                for (Entity entity : targets) {
                    Mage mage = controller.getMage(entity);
                    mages.add(mage);
                }
            } else {
                // Look for Entity-based Mages
                Mage mage = null;
                if (playerName.contains(",")) {
                    String[] idPieces = StringUtils.split(playerName, ',');
                    if (idPieces.length == 4 || idPieces.length == 2) {
                        try {
                            String entityId = idPieces[idPieces.length - 1];
                            Entity entity = CompatibilityLib.getCompatibilityUtils().getEntity(UUID.fromString(entityId));
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

                Player player = CompatibilityLib.getInstance().getPlayer(playerName);
                if (mage == null && player == null && playerName.contains("-")) {
                    try {
                        Entity entity = CompatibilityLib.getCompatibilityUtils().getEntity(UUID.fromString(playerName));
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
                if (mage == null && player != null) {
                    if (!player.isOnline()) {
                        if (sender != null) sender.sendMessage("Player " + playerName + " is not online");
                        return true;
                    }
                    mage = controller.getMage(player);

                }
                if (mage != null) {
                    mages.add(mage);
                }
            }

            if (mages.isEmpty()) {
                if (sender != null) {
                    sender.sendMessage("No targets match selectors");
                }
                return true;
            }

            Location sourceLocation = null;
            if (sender != null && sender instanceof BlockCommandSender) {
                sourceLocation = ((BlockCommandSender) sender).getBlock().getLocation();
            }

            String[] castParameters = Arrays.copyOfRange(args, 1, args.length);
            for (Mage mage : mages) {
                if (mage != null && !mage.isLoading()) {
                    // TODO: Need a way to disable these messsages .. maybe by default?
                    processCastCommand(sender, mage, castParameters, sourceLocation, " as " + mage.getName());
                }
            }
            return true;
        }

        if (commandName.equalsIgnoreCase("cast"))
        {
            if (!api.hasPermission(sender, "Magic.commands.cast")) {
                sendNoPermission(sender);
                return true;
            }
            return processCastCommand(sender, controller.getMage(sender), args, null, null);
        }

        return false;
    }

    public boolean processCastCommand(CommandSender sender, Mage mage, String[] castParameters, Location targetLocation, String messageSuffix) {
        if (castParameters.length < 1) {
            if (sender != null) sender.sendMessage("Missing spell in cast command");
            return false;
        }

        String spellName = castParameters[0];
        if (sender != null && !controller.hasPermission(sender, "Magic.cast_via_command." + spellName)) {
            sender.sendMessage("You do not have permission to do that");
            return true;
        }

        Spell spell = mage.getSpell(spellName);
        if (spell == null) {
            if (sender != null) sender.sendMessage("Unknown spell " + spellName);
            return false;
        }

        if (sender == null || sender.hasPermission("Magic.commands.cast.parameters")) {
            castParameters = Arrays.copyOfRange(castParameters, 1, castParameters.length);
        } else {
            castParameters = null;
        }
        boolean result;
        ((MagicController)controller).toggleCastCommandOverrides(mage, sender, true);
        try {
            result = spell.cast(castParameters, targetLocation);
        } catch (Throwable ex) {
            result = false;
            controller.getLogger().log(Level.SEVERE, "Error casting spell via command", ex);
        }
        boolean showCast = sender != null && !(sender instanceof Player) && controller.showConsoleCastFeedback();
        messageSuffix = messageSuffix == null ? "" : messageSuffix;
        if (result) {
            if (showCast && !messageSuffix.isEmpty()) sender.sendMessage("Cast " + spell.getName() + messageSuffix);
        } else {
            if (showCast) sender.sendMessage("Failed to cast " + spell.getName() + messageSuffix);
        }
        ((MagicController)controller).toggleCastCommandOverrides(mage, sender, false);
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
