package com.elmakers.mine.bukkit.integration.skript;

import org.bukkit.event.Event;

import com.elmakers.mine.bukkit.api.event.EarnEvent;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;

public class EvtEarn extends SkriptEvent {
    private Literal<String> types;

    public static void register() {
        Skript.registerEvent("Earn", EvtEarn.class, EarnEvent.class, "earn [sp]")
            .description("Called when a player earns SP")
            .examples("on earn", "on earn sp");
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Literal<?>[] args, int i, SkriptParser.ParseResult parseResult) {
        types = (Literal<String>)args[0];
        return true;
    }

    @Override
    public boolean check(Event event) {
        if (!(event instanceof EarnEvent)) return false;
        final EarnEvent earnEvent = (EarnEvent)event;
        if (types != null) {
            String earnType = earnEvent.getEarnType();
            for (String type : types.getAll()) {
                if (earnType.equalsIgnoreCase(type)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public String toString(Event event, boolean debug) {
        return "earn" + (types != null ? " " + types.toString(event, debug) : "");
    }
}
