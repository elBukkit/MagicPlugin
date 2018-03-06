package com.elmakers.mine.bukkit.integration.skript;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import com.elmakers.mine.bukkit.api.event.CastEvent;
import org.bukkit.ChatColor;
import org.bukkit.event.Event;

public class SkriptCastEvent extends SkriptEvent {
	private Literal<String> spells;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Literal<?>[] args, int i, SkriptParser.ParseResult parseResult) {
        spells = (Literal<String>)args[0];
        return true;
    }

    @Override
    public boolean check(Event event) {
        if (!(event instanceof CastEvent)) return false;
        final CastEvent spellCast = (CastEvent)event;
        if (spells != null) {
            String spellKey = spellCast.getSpell().getKey();
            String spellName = ChatColor.stripColor(spellCast.getSpell().getName());
            for (String spell : spells.getAll()) {
                if (spellKey.equalsIgnoreCase(spell)) {
                    return true;
                }
                if (spellName.equalsIgnoreCase(spell)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public String toString(Event event, boolean debug) {
        return "cast" + (spells != null ? " of " + spells.toString(event, debug) : "");
    }
}
