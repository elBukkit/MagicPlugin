package com.elmakers.mine.bukkit.integration.skript;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;

import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.magic.MagicPlugin;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;

@Name("Cast")
@Description("Cast a spell, from an entity or the console")
@Examples({"make entity cast the spell \"fling\"", "cast \"day\""})
public class EffCast extends Effect {
    private Expression<Entity> entities;
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
        if (entities != null) {
            for (final Entity entity : entities.getArray(event)) {
                if (entity != null) {
                    Spell spell = MagicPlugin.getAPI().getController().getMage(entity).getSpell(spellKey);
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
        return "make " + (entities != null ? entities.toString(event, debug) : "the console") + " cast the spell " + spell.toString(event, debug) + parameters;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] vars, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parser) {
        if (matchedPattern == 0) {
            spell = (Expression<String>) vars[0];
            entities = (Expression<Entity>) vars[1];
        } else {
            entities = (Expression<Entity>) vars[0];
            spell = (Expression<String>) vars[1];
        }
        arguments = (Expression<String>) vars[2];
        return true;
    }
}
