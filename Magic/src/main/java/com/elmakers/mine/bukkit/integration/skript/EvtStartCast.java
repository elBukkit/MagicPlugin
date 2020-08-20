package com.elmakers.mine.bukkit.integration.skript;

import org.bukkit.ChatColor;
import org.bukkit.event.Event;

import com.elmakers.mine.bukkit.api.event.StartCastEvent;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;

public class EvtStartCast extends SkriptEvent {
    private Literal<String> spells;

    public static void register() {
        Skript.registerEvent("Started Spell", EvtStartCast.class, StartCastEvent.class, "casted [[of] [spell] %-string%]")
            .description("Called when a player or magic mob starts casting a spell")
            .examples("on casted", "on casted of missile", "on casted of spell blink");
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Literal<?>[] args, int i, SkriptParser.ParseResult parseResult) {
        spells = (Literal<String>)args[0];
        return true;
    }

    @Override
    public boolean check(Event event) {
        if (!(event instanceof StartCastEvent)) return false;
        final StartCastEvent spellCast = (StartCastEvent)event;
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
        return "casted" + (spells != null ? " of " + spells.toString(event, debug) : "");
    }
}
