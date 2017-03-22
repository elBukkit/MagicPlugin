package com.elmakers.mine.bukkit.api.event;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.api.wand.WandUpgradePath;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * A custom event that the Magic plugin will fire whenever a player upgrades
 * a Wand to a new level
 */

/**
 * This event is deprecated and will be replaced by PathUpgradeEvent.
 */
// TODO: Actually deprecate this when we can.
//@Deprecated
public class WandUpgradeEvent extends Event {
    private final Mage mage;
    private final Wand wand;
    private final WandUpgradePath oldPath;
    private final WandUpgradePath newPath;

    private static final HandlerList handlers = new HandlerList();

    public WandUpgradeEvent(Mage mage, Wand wand, WandUpgradePath oldPath, WandUpgradePath newPath) {
        this.mage = mage;
        this.wand = wand;
        this.oldPath = oldPath;
        this.newPath = newPath;
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

    public WandUpgradePath getOldPath() {
        return oldPath;
    }

    public WandUpgradePath getNewPath() {
        return newPath;
    }
}
