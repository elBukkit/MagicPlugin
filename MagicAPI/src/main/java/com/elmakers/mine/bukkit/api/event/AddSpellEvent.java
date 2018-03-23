package com.elmakers.mine.bukkit.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.Wand;

/**
 * A custom event that the Magic plugin will fire any time a player
 * gets a spell added to their wand.
 */
public class AddSpellEvent extends Event {
    private final Mage mage;
    private final Wand wand;
    private final SpellTemplate spell;

    private static final HandlerList handlers = new HandlerList();

    public AddSpellEvent(Mage mage, Wand wand, SpellTemplate spell) {
        this.mage = mage;
        this.wand = wand;
        this.spell = spell;
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

    public Wand getWand() {
        return wand;
    }

    public SpellTemplate getSpell() {
        return spell;
    }
}
