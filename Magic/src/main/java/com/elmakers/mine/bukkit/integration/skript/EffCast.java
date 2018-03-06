package com.elmakers.mine.bukkit.integration.skript;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.magic.MagicPlugin;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;

public class EffCast extends Effect {
	private Expression<CommandSender> senders;
	private Expression<String> spell;
	private Expression<String> arguments;

    public static void register() {
        Skript.registerEffect(EffCast.class,
            "cast [the] [spell] %string% [by %-commandsenders%] [with %-string%]",
            "(let|make) %commandsenders% cast [[the] spell] %string% [with %-string%]");
    }

    @Override
    protected void execute(Event event) {
        String spellKey = spell.getSingle(event);
        String parameterString = arguments == null ? null : arguments.getSingle(event);
        String[] parameters = parameterString == null ? null : StringUtils.split(parameterString, ' ');
        if (senders != null) {
            for (final CommandSender sender : senders.getArray(event)) {
                if (sender != null) {
                    Spell spell = MagicPlugin.getAPI().getController().getMage(sender).getSpell(spellKey);
                    if (spell != null) {
                        spell.cast(parameters);
                    }
                }
            }
        } else {
            Spell spell = MagicPlugin.getAPI().getController().getMage(Bukkit.getConsoleSender()).getSpell(spellKey);
            if (spell != null) {
                spell.cast(parameters);
            }
        }
    }

    @Override
    public String toString(Event event, boolean debug) {
        String parameters = "";
        if (arguments != null) {
            parameters = " with parameters " + arguments.toString(event, debug);
        }
		return "make " + (senders != null ? senders.toString(event, debug) : "the console") + " cast the spell " + spell.toString(event, debug) + parameters;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] vars, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parser) {
        if (matchedPattern == 0) {
			spell = (Expression<String>) vars[0];
			senders = (Expression<CommandSender>) vars[1];
		} else {
			senders = (Expression<CommandSender>) vars[0];
			spell = (Expression<String>) vars[1];
		}
		arguments = (Expression<String>) vars[2];
		return true;
    }
}
