package com.elmakers.mine.bukkit.api.magic;

import javax.annotation.Nullable;

public interface ModifierTemplate extends MagicProperties {
    String getName();
    String getDescription();
    @Nullable
    String getIconKey();
    @Nullable
    String getIconDisabledKey();
}
