package com.elmakers.mine.bukkit.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

/**
 * A custom event that the Magic plugin will fire any time a
 * Mage casts a Spell.
 * This event happens between PreCastEvent and CastEvent. It is when
 * the spell has officially started, but not necessarily finished completely.
 */
public class StartCastEvent extends Event {
    private final Mage mage;
    private final Spell spell;
    private final SpellResult spellResult;
    private final boolean success;

    private static final HandlerList handlers = new HandlerList();

    public StartCastEvent(Mage mage, Spell spell, SpellResult result, boolean success) {
        this.mage = mage;
        this.spell = spell;
        this.spellResult = result;
        this.success = success;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Mage getMage() {
        return mage;
    }

    public Spell getSpell() {
        return spell;
    }

    public SpellResult getInitialResult() {
        return spellResult;
    }

    public boolean isSuccess() {
        return this.success;
    }
}
