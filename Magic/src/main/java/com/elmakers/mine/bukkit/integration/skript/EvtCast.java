package com.elmakers.mine.bukkit.integration.skript;

import org.bukkit.ChatColor;
import org.bukkit.event.Event;

import com.elmakers.mine.bukkit.api.event.CastEvent;
import com.elmakers.mine.bukkit.api.event.PreCastEvent;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;

public class EvtCast extends SkriptEvent {
    private Literal<String> spells;

    public static void register() {
        Skript.registerEvent("Cast Spell", EvtCast.class, CastEvent.class, "cast [[of] [spell] %-string%]")
            .description("Called when a player or magic mob casts a spell")
            .examples("on cast", "on cast of missile", "on cast of spell blink");

        Skript.registerEvent("Casting Spell", EvtCast.class, PreCastEvent.class, "casting [[of] [spell] %-string%]")
            .description("Called when a player or magic mob is about to cast a spell")
            .examples("on cast", "on casting of missile", "on casting of spell blink");
    }

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
