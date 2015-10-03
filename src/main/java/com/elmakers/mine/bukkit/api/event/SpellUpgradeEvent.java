package com.elmakers.mine.bukkit.api.event;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * A custom event that the Magic plugin will fire whenever a player upgrades
 * a spell by casting it enough.
 */
public class SpellUpgradeEvent extends Event {
    private final Mage mage;
    private final Spell oldSpell;
    private final Spell newSpell;

    private static final HandlerList handlers = new HandlerList();

    public SpellUpgradeEvent(Mage mage, Spell oldSpell, Spell newSpell) {
        this.mage = mage;
        this.oldSpell = oldSpell;
        this.newSpell = newSpell;
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

    public Spell getOldSpell() {
        return oldSpell;
    }

    public Spell getNewSpell() {
        return newSpell;
    }
}
