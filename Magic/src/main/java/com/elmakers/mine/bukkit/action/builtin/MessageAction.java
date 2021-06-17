package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;

public class MessageAction extends BaseSpellAction
{
    public enum MessageType { CHAT, TITLE, ACTION_BAR }

    private String message = "";
    private String subMessage = "";
    private int fadeIn;
    private int stay;
    private int fadeOut;
    private boolean messageTarget = false;
    private MessageType messageType = MessageType.CHAT;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        String messageKey = parameters.getString("message_key");
        if (messageKey != null) {
            message = context.getMessage(messageKey);
        } else {
            message = CompatibilityLib.getCompatibilityUtils().translateColors(parameters.getString("message", ""));
        }
        String subMessageKey = parameters.getString("sub_message_key");
        if (subMessageKey != null) {
            subMessage = context.getMessage(subMessageKey);
        } else {
            subMessage = CompatibilityLib.getCompatibilityUtils().translateColors(parameters.getString("sub_message", ""));
        }
        fadeIn = parameters.getInt("fade_in", -1);
        stay = parameters.getInt("stay", -1);
        fadeOut = parameters.getInt("fade_out", -1);
        messageTarget = parameters.getBoolean("message_target", false);
        if (parameters.contains("equation")) {
            double value = parameters.getDouble("equation");
            message = message.replace("$equation", Double.toString(value))
                .replace("@equation", Integer.toString((int)value));
        }

        String messageTypeString = parameters.getString("message_type", null);
        if (messageTypeString != null) {
            try {
                messageType = MessageType.valueOf(messageTypeString.toUpperCase());
            } catch (Exception ex) {
                context.getLogger().warning("Not a valid message_type: " + messageTypeString);
            }
        }
    }

    @Override
    public SpellResult perform(CastContext context) {
        if (messageTarget) {
            Entity targetEntity = context.getTargetEntity();
            if (targetEntity == null || !(targetEntity instanceof Player)) {
                return SpellResult.NO_TARGET;
            }
            sendMessage(context, (Player)targetEntity);
            return SpellResult.CAST;
        }
        Mage mage = context.getMage();
        CommandSender commandSender = mage.getCommandSender();
        if (commandSender == null) {
            return SpellResult.FAIL;
        }
        return sendMessage(context, commandSender);
    }

    private SpellResult sendMessage(CastContext context, CommandSender commandSender) {
        String message = context.parameterize(context.getMessage(this.message, this.message));
        // This is leftover but really shouldn't be here, use @spell instead!
        message = message.replace("$spell", context.getSpell().getName());
        Player player = (commandSender instanceof Player) ? (Player)commandSender : null;
        switch (messageType) {
            case CHAT:
                commandSender.sendMessage(message);
                break;
            case TITLE:
                if (player == null) {
                    return SpellResult.PLAYER_REQUIRED;
                }
                String subMessage = context.parameterize(context.getMessage(this.subMessage, this.subMessage));
                CompatibilityLib.getCompatibilityUtils().sendTitle(player, message, subMessage, fadeIn, stay, fadeOut);
                break;
            case ACTION_BAR:
                if (player == null) {
                    return SpellResult.PLAYER_REQUIRED;
                }
                if (!CompatibilityLib.getCompatibilityUtils().sendActionBar(player, message)) {
                    player.sendMessage(message);
                }
                break;
        }
        return SpellResult.CAST;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("message_key");
        parameters.add("message");
        parameters.add("equation");
        parameters.add("sub_message");
        parameters.add("sub_message_key");
        parameters.add("message_type");
        parameters.add("message_target");
        parameters.add("stay");
        parameters.add("fade_in");
        parameters.add("fade_out");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("message") || parameterKey.equals("sub_message")) {
            examples.add("You cast $spell on $target");
        } else if (parameterKey.equals("equation")) {
            examples.add("intelligence + strength / 20");
        } else if (parameterKey.equals("fade_in") || parameterKey.equals("fade_out") || parameterKey.equals("stay")) {
            examples.add("-1");
            examples.add("10");
            examples.add("20");
            examples.add("70");
        } else  if (parameterKey.equals("message_type")) {
            for (MessageType messageType : MessageType.values()) {
                examples.add(messageType.name().toLowerCase());
            }
        } else  if (parameterKey.equals("message_target")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_BOOLEANS));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
