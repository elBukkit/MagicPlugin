package com.elmakers.mine.bukkit.api.magic;

import java.util.Collection;
import javax.annotation.Nonnull;

import com.elmakers.mine.bukkit.api.item.Cost;

public interface ProgressionLevel {
    @Nonnull
    Collection<? extends Cost> getCosts();
    int getLevel();
}
