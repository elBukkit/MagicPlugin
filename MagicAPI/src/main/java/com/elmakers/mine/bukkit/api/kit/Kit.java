package com.elmakers.mine.bukkit.api.kit;

import javax.annotation.Nullable;

import com.elmakers.mine.bukkit.api.magic.Mage;

public interface Kit {
    long getRemainingCooldown(Mage mage);
    boolean isAllowed(Mage mage);
    void give(Mage mage);
    String getName();
    String getDescription();
    @Nullable
    String getIconKey();
    @Nullable
    String getIconDisabledKey();
    double getWorth();
}
