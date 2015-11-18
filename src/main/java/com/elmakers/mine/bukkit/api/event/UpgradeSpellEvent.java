package com.elmakers.mine.bukkit.api.event;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.Wand;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * A custom event that the Magic plugin will fire any time a player
 * levels up a spell.
 */
public class UpgradeSpellEvent extends Event {
    private final Mage mage;
    private final Wand wand;
    private final SpellTemplate fromSpell;
    private final SpellTemplate toSpell;

    private static final HandlerList handlers = new HandlerList();

    public UpgradeSpellEvent(Mage mage, Wand wand, SpellTemplate fromSpell, SpellTemplate toSpell) {
        this.mage = mage;
        this.wand = wand;
        this.toSpell = toSpell;
        this.fromSpell = fromSpell;
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

    public SpellTemplate getFromSpell() {
        return fromSpell;
    }

    public SpellTemplate getToSpell() {
        return toSpell;
    }
}
