package com.elmakers.mine.bukkit.api.kit;

import com.elmakers.mine.bukkit.api.magic.Mage;

public interface Kit {
    long getRemainingCooldown(Mage mage);
    boolean isAllowed(Mage mage);
    void give(Mage mage);
}
