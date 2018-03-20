package com.elmakers.mine.bukkit.api.event;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageClass;
import com.elmakers.mine.bukkit.api.magic.ProgressionPath;
import com.elmakers.mine.bukkit.api.wand.Wand;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A custom event that the Magic plugin will fire whenever a player upgrades to a new level
 */
public class PathUpgradeEvent extends Event {
    private final @Nonnull Mage mage;
    private final @Nullable Wand wand;
    private final @Nullable MageClass mageClass;
    private final @Nonnull ProgressionPath oldPath;
    private final @Nonnull ProgressionPath newPath;

    private static final HandlerList handlers = new HandlerList();

    public PathUpgradeEvent(@Nonnull Mage mage, @Nullable Wand wand, @Nullable MageClass mageClass, @Nonnull ProgressionPath oldPath, @Nonnull ProgressionPath newPath) {
        this.mage = mage;
        this.wand = wand;
        this.oldPath = oldPath;
        this.newPath = newPath;
        this.mageClass = mageClass;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public @Nonnull Mage getMage() {
        return mage;
    }

    public @Nullable Wand getWand() {
        return wand;
    }

    public @Nullable MageClass getMageClass() {
        return mageClass;
    }

    public @Nonnull  ProgressionPath getOldPath() {
        return oldPath;
    }

    public @Nonnull ProgressionPath getNewPath() {
        return newPath;
    }
}
