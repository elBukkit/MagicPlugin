package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Collection;

public class MessageAction extends BaseSpellAction
{
	private enum MessageType { CHAT, TITLE, ACTION_BAR };

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
        message = ChatColor.translateAlternateColorCodes('&', parameters.getString("message", ""));
		subMessage = ChatColor.translateAlternateColorCodes('&', parameters.getString("sub_message", ""));
		fadeIn = parameters.getInt("fade_in", -1);
		stay = parameters.getInt("stay", -1);
		fadeOut = parameters.getInt("fade_out", -1);
		messageTarget = parameters.getBoolean("message_target", false);

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
		Player player = mage.getPlayer();
		if (player == null) {
			return SpellResult.PLAYER_REQUIRED;
		}
		sendMessage(context, player);
		return SpellResult.CAST;
	}

	private void sendMessage(CastContext context, Player player) {
		String message = context.parameterize(context.getMessage(this.message, this.message));
		message = message.replace("$spell", context.getSpell().getName());
		switch (messageType) {
			case CHAT:
				player.sendMessage(message);
				break;
			case TITLE:
				String subMessage = context.parameterize(context.getMessage(this.subMessage, this.subMessage));
				CompatibilityUtils.sendTitle(player, message, subMessage, fadeIn, stay, fadeOut);
				break;
			case ACTION_BAR:
				if (!CompatibilityUtils.sendActionBar(player, message)) {
					player.sendMessage(message);
				}
				break;
		}
	}

	@Override
	public void getParameterNames(Spell spell, Collection<String> parameters) {
		super.getParameterNames(spell, parameters);
		parameters.add("message");
		parameters.add("sub_message");
		parameters.add("message_type");
		parameters.add("stay");
		parameters.add("fade_in");
		parameters.add("fade_out");
	}

	@Override
	public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
		if (parameterKey.equals("message") || parameterKey.equals("sub_message")) {
			examples.add("You cast $spell on $target");
		} else if (parameterKey.equals("fade_in") || parameterKey.equals("fade_out") || parameterKey.equals("stay")) {
			examples.add("-1");
			examples.add("10");
			examples.add("20");
			examples.add("70");
		} else  if (parameterKey.equals("message_type")) {
			for (MessageType messageType : MessageType.values()) {
				examples.add(messageType.name().toLowerCase());
			}
		} else {
			super.getParameterOptions(spell, parameterKey, examples);
		}
	}
}
