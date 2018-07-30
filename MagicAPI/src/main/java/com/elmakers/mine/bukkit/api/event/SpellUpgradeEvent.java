package com.elmakers.mine.bukkit.api.event;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.Wand;

/**
 * A custom event that the Magic plugin will fire whenever a player upgrades
 * a spell by casting it enough.
 */
public class SpellUpgradeEvent extends Event {
    private final Mage mage;
    private final Wand wand;
    private final SpellTemplate oldSpell;
    private final SpellTemplate newSpell;

    private static final HandlerList handlers = new HandlerList();

    public SpellUpgradeEvent(Mage mage, Wand wand, SpellTemplate oldSpell, SpellTemplate newSpell) {
        this.mage = mage;
        this.wand = wand;
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

    @Nonnull
    public Mage getMage() {
        return mage;
    }

    @Nonnull
    public SpellTemplate getOldSpell() {
        return oldSpell;
    }

    @Nonnull
    public SpellTemplate getNewSpell() {
        return newSpell;
    }

    @Nullable
    public Wand getWand() {
        return wand;
    }
}
