package com.elmakers.mine.bukkit.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

/**
 * A custom event that the Magic plugin will fire any time a
 * Mage casts a Spell.
 */
public class CastEvent extends Event {
    private final CastContext context;

    private static final HandlerList handlers = new HandlerList();

    public CastEvent(CastContext context) {
        this.context = context;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Mage getMage() {
        return context.getMage();
    }

    public Spell getSpell() {
        return context.getSpell();
    }

    public CastContext getContext() {
        return context;
    }

    public SpellResult getSpellResult() {
        return context.getResult();
    }
}
