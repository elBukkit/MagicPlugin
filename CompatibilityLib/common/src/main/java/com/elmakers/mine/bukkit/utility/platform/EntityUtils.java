package com.elmakers.mine.bukkit.utility.platform;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.entity.EntityExtraData;

public interface EntityUtils {
    EntityExtraData getExtraData(MageController controller, Entity entity);
    EntityExtraData getExtraData(MageController controller, EntityType entityType, ConfigurationSection configuration);
}
