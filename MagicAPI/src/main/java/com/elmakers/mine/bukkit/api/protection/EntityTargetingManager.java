package com.elmakers.mine.bukkit.api.protection;

import org.bukkit.entity.Entity;

public interface EntityTargetingManager {
    boolean canTarget(Entity source, Entity target);
}
